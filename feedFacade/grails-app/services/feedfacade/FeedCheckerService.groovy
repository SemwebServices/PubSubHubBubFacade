package feedfacade
import grails.gorm.transactions.*
import java.security.MessageDigest
import org.apache.commons.io.input.BOMInputStream
import java.text.SimpleDateFormat
import static groovy.json.JsonOutput.*
import com.budjb.rabbitmq.publisher.RabbitMessagePublisher
import java.lang.Thread
import grails.async.Promise
import static grails.async.Promises.*
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;
import groovyx.net.http.HttpBuilder
import groovyx.net.http.FromServer
import groovyx.net.http.ChainedHttpConfig
import static groovyx.net.http.HttpBuilder.configure

// Moving to Apache http client implementation for HttpBuilderNG
import groovyx.net.http.ApacheHttpBuilder
import org.apache.http.impl.client.HttpClientBuilder
import org.apache.http.client.config.RequestConfig

import org.springframework.beans.factory.DisposableBean


@Transactional
class FeedCheckerService  implements HealthIndicator, DisposableBean {

  private static int MAX_HTTP_TIME = 10 * 1000;
  private static Integer active_checks = 0;
  private static Map<String,Object> active_check_info = [:]

  def grailsApplication

  def checker_is_enabled = true;
  def running = false;
  def error_count = 0;
  def newEventService
  def statsService
  def flagsService
  def feedCheckLog=new org.apache.commons.collections.buffer.CircularFifoBuffer(100);
  RabbitMessagePublisher rabbitMessagePublisher
  Long lastFeedCheckStartedAt = 0;
  Long lastFeedCheckElapsed = 0;
  Long currentCheckStartTime = 0;

  private Long  MAX_CONSECUTIVE_ERRORS = 100;

  def possible_date_formats = [
    // new SimpleDateFormat('yyyy-MM-dd'), // Default format Owen is pushing ATM.
    // new SimpleDateFormat('yyyy/MM/dd'),
    // new SimpleDateFormat('dd/MM/yyyy'),
    // new SimpleDateFormat('dd/MM/yy'),
    // new SimpleDateFormat('yyyy/MM'),
    // new SimpleDateFormat('yyyy')
    new SimpleDateFormat('yyyy-MM-dd\'T\'HH:mm:ssX'),
    new SimpleDateFormat('yyyy-MM-dd\'T\'HH:mm:ss.SSSX'),
    new SimpleDateFormat('EEE, d MMM yyyy HH:mm:ss z'),
    new SimpleDateFormat('EEE, d MMM yyyy HH:mm:ss Z'),
    new SimpleDateFormat('EEE, d MMM yyyy HH:mm:ss'),
    new SimpleDateFormat('EEE, dd MMM yyyy HH:mm zzz'),
    new SimpleDateFormat('EEE, dd MMM yyyy H:mm:ss zzz')
  ];

  def getLastLog() {
    feedCheckLog
  }

  def isRunning() { 
    running
  }

  def getFeedCheckLog() {
    feedCheckLog
  }

  @javax.annotation.PostConstruct
  def init() {
  }

  @Override
  void destroy() throws Exception {
    log.info("FeedCheckerService::destroy()");
    checker_is_enabled = false;
    synchronized(this) {
      log.info("FeedCheckerService::destroy is waiting for any active feed fetcher threads to cleanly terminate.... active check count: ${active_check_info?.size()}");
      Thread.sleep(1000*15);
      log.info("FeedCheckerService::destroy finished waiting... active check count: ${active_check_info?.size()}");
    }

    active_check_info?.each { k, v ->
      log.info("  [${k}] -> ${v}");
    }
  }

  // See https://reflectoring.io/spring-bean-lifecycle/
  public void shutdown() {
    log.info("FeedCheckerService::shutdown()");
  }


  public Long getLastFeedCheckStartTime() {
    return lastFeedCheckStartedAt;
  }

  public Long getCurrentCheckElapsed() {
    Long result = null;
    Long start = currentCheckStartTime;
    if ( start != null ) {
      result = System.currentTimeMillis() - start
    }
    return result;
  }


  def triggerFeedCheck() {

    log.info("triggerFeedCheck :: FEED-CHECK-PROMISE active info (active_checks counter = ${active_checks}, size of active_check_info=${active_check_info?.size()})");
    active_check_info.each { k, v ->
      log.info("  [${k}] -> ${v}");
    }

    if ( checker_is_enabled ) {
      if ( running ) {
        log.info("Feed checker already running - not launching another [${error_count++}] - active_checks=${active_checks}");
      }
      else {
        def error_count = 0;
        lastFeedCheckStartedAt = System.currentTimeMillis();
        doFeedCheck()
      }
    }
    else {
      log.info("feedChecker is disabled - shutdown in progress. Not checking feeds");
    }

  }

  def doFeedCheck() {
    def start_time = System.currentTimeMillis()
    def start_time_as_date = new Date(start_time)
    log.info("FeedCheckerService::doFeedCheck ${start_time}");
    running=true;
    currentCheckStartTime = start_time;
    feedCheckLog=[]
    feedCheckLog.add([timestamp:new Date(),message:'Feed check started']);
    // log.debug("Finding all feeds due on or after ${start_time}");
    def sdf = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ")

    logEvent('System.notification',[
        timestamp:new Date(),
        type: 'info',
        message:"Feed Check Started at ${start_time} ${sdf.format(start_time_as_date)}"
    ]);

    def processed_feed_counter = 0;

    try {
      def cont = true
      while ( cont ) {

        log.debug("Processing feed ${++processed_feed_counter}");

        // Grab the next feed to examine -- do it in a transaction
        def feed_info = null
        SourceFeed.withNewTransaction {
          // log.debug("Searching for paused feeds where lastCompleted+pollInterval < now ${start_time}");

          def q = SourceFeed.executeQuery('select sf.id, sf.baseUrl, sf.lastHash, sf.highestTimestamp, sf.httpExpires, sf.httpLastModified, sf.uriname from SourceFeed as sf where sf.baseUrl is not null and sf.status=:paused AND sf.lastCompleted + sf.pollInterval < :ctm and sf.enabled = :enabled order by (sf.lastCompleted + sf.pollInterval) asc',
                                           [paused:'paused', ctm:start_time, enabled:true],[lock:false])

          def num_paused_feeds = q.size();
          log.info("feedChecher detects ${num_paused_feeds} feeds paused that are overdue a check (lastCompleted+pollInterval > ${start_time_as_date})");

          if ( num_paused_feeds > 0 ) {
            def row = q.get(0)
            feed_info = [:]
            feed_info.id = row[0]
            feed_info.url = row[1]
            feed_info.hash = row[2]
            feed_info.highesTimestamp = row[3]
            feed_info.expires = row[4]
            feed_info.lastModified = row[5]
            feed_info.uriname = row[6]
          }
         
        }

        if ( feed_info ) {
          feedCheckLog.add([timestamp:new Date(),message:'Identified feed '+feed_info]);
          log.info("Process selected feed ${feed_info}");
          processFeed(start_time, 
                      feed_info.id,
                      feed_info.uriname,
                      feed_info.url,
                      feed_info.hash,
                      feed_info.highesTimestamp,
                      feed_info.expires,
                      feed_info.lastModified,
                      feed_info.feedStatus);
        }
        else {  
          // nothing left in the queue
          log.debug("Nothing left to process.. exit loop");
          cont = false
        }
      }

      // Give other threads a chance
      Thread.yield(); 
    }
    catch ( Exception e ) {
      feedCheckLog.add([type:'ERROR', timestamp:new Date(),message:'Feed check error '+e.message]);
      log.error("Problem processing feeds",e);
      e.printStackTrace()
    }
    finally {
      log.info("processed ${processed_feed_counter} feeds");
    }

    logEvent('System.notification',[
        timestamp:new Date(),
        type: 'info',
        message:"Feed Check Ended at ${sdf.format(new Date())}"
    ]);

    feedCheckLog.add([timestamp:new Date(),message:'Feed check finished']);
    lastFeedCheckElapsed = System.currentTimeMillis() - start_time;
    currentCheckStartTime = null;
    running=false;
  }

  def processFeed(start_time, 
                  id, 
                  uriname, 
                  url, 
                  hash, 
                  highestRecordedTimestamp,
                  httpExpires,
                  httpLastModified,
                  feedStatus) {

    log.debug("processFeed[${id}] (${start_time},${id},${url},${hash},${highestRecordedTimestamp})");

    logEvent('Feed.'+uriname,[
      timestamp:new Date(),
      type: 'info',
      message:"Checking feed ${uriname} / ${url} (${Thread.currentThread().getName()})",
      relatedType:"feed",
      relatedId:uriname
    ]);

    def continue_processing = false;

    SourceFeed.withNewTransaction {
      log.debug("processFeed[${id}] Mark feed as in-process");
      def sf = SourceFeed.get(id)

      sf.lock()
      if ( sf.status == 'paused' ) {
        // log.debug("processFeed[${id}] Feed really is paused -- mark it as in process and proceed");
        sf.status = 'in-process'
        sf.lastStarted = System.currentTimeMillis();
        continue_processing = true;
        sf.save(flush:true, failOnError:true);
      }
      else {
        log.info("processFeed[${id}] On more thorough inspection, someone else already grabbed the feed to process, so skip");
      }
    }

    String feed_check_mode=grailsApplication.config?.feedCheckMode ?: 'parallel'

    
    if ( continue_processing ) {

      log.debug("process feed in ${feed_check_mode} mode");

      if ( feed_check_mode=='parallel' ) {

        int max_active = grailsApplication.config?.feedCheckMaxActivePromises ?: 8

        // Block whilst we have > max_active promises, then continue
        synchronized(active_check_info) {
          while ( active_checks >= max_active ) {
            log.debug("FEED-CHECK-PROMISE Active=${active_checks}, max=${max_active} BLOCKING waiting for a promise to complete");
            active_check_info.wait(60000)
            log.debug("Done waiting, recheck")
          }
        }

        log.debug("Launch promise to process feed ${id}");
        String ckid = java.util.UUID.randomUUID().toString()

        Promise p = task( { check_id ->
          LocalFeedSettings.withNewSession {
            LocalFeedSettings.withNewTransaction {
              LocalFeedSettings lfs = LocalFeedSettings.findByUriname(uriname)
              def promise_info = [ id:id, uriname:uriname, url:url, start_time:start_time ]
              synchronized(active_check_info) {
                active_checks++;
                active_check_info[check_id] = promise_info
              }

              log.info("FEED-CHECK-PROMISE[${check_id}] Launch promise ${promise_info} after start, active_checks=${active_checks}");
              this.continueToProcessFeed(id, uriname, url, hash, httpExpires, httpLastModified, highestRecordedTimestamp, start_time, lfs, feedStatus);
            }
          }
        }.curry(ckid))

        p.onError( { check_id, Throwable err ->
          log.error("FEED-CHECK-PROMISE[${check_id}] completed with error (active_checks=${active_checks})",err);
          synchronized(active_check_info) {
            active_checks--;
            active_check_info.remove(check_id);
            active_check_info.notifyAll()
          }
          log.debug("Notify waiters that promise completed");
        }.curry(ckid))

        p.onComplete( { check_id, result ->
          log.debug("FEED-CHECK-PROMISE[${check_id}] completed OK (active_checks=${active_checks})");
          synchronized(active_check_info) {
            active_checks--;
            active_check_info.remove(check_id);
            active_check_info.notifyAll()
          }
          log.debug("Notify waiters that promise completed");
        }.curry(ckid))
      }
      else if ( feed_check_mode=='serial' ) {
        LocalFeedSettings lfs = LocalFeedSettings.findByUriname(uriname)
        this.continueToProcessFeed(id, uriname, url, hash, httpExpires, httpLastModified, highestRecordedTimestamp, start_time, lfs, feedStatus);
      }
      else {
        log.warn("Unhandled feed checker mode");
      }
    }

    feedCheckLog.add([timestamp:new Date(),message:"Process feed completed :: ${id} ${url}"]);
    log.debug("processFeed[${id}] returning having launched promise");
  }


  /**
   *  This method will be called by the promise above, it's session will be disconnected due to the way
   *  promise works in a new thread. Because the service is transactional, it should get the relevant 
   *  new session/transaction injected.
   */
  private void continueToProcessFeed(id, 
                                     uriname, 
                                     url, 
                                     hash, 
                                     httpExpires, 
                                     httpLastModified, 
                                     highestRecordedTimestamp, 
                                     start_time,
                                     lfs,
                                     feedStatus) {

    log.info("continueToProcessFeed[${id}] continue_processing.... :: url:${url} existing hash:${hash}");
    def error = false
    String error_message = null
    def newhash = null;
    def new_entry_count = 0
    def highestSeenTimestamp = null;
 
    def processing_start_time = System.currentTimeMillis()

    if ( lfs != null ) {
      log.info("Have override local feed settings for ${lfs.uriname}");

      if ( lfs.alternateFeedURL != null )
        url = lfs.alternateFeedURL;

      switch ( lfs.authenticationMethod ) {
        case 'pin':
          url += "?pin=${lfs.credentials}"
          break;
        default:
          break;
      }
    }

    def feed_info = null;
    // The outer try - because we are in a runAsync unhandled exceptions might get dropped
    // This block does nothing but catch and log exceptions not caught before. It's important
    // You don't do any work in here beyond the inner try block.
    try {
      try {
        log.info("call fetchFeedPage for ${url}");
        feed_info = fetchFeedPage(id, url, httpExpires, httpLastModified);
        // log.debug(feed_info.toString())

        // If we got a hash back from fetching the page AND 
        // the storred hash is different OR not set OR the feed is in an ERROR state, then process the feed.
        if ( ( feed_info.hash != null ) && 
             ( ( hash == null ) || 
               ( feed_info.hash != hash ) || 
               ( feedStatus=='ERROR') ) ) {
          newhash = feed_info.hash
          log.debug("processFeed[${id}] Detected hash change (old:${hash},new:${feed_info.hash}).. Process");
    
          def processing_result = null;
          // log.debug("Processing feed (contentType::${feed_info.contentType}) - Extract entries");

          InputStream page_is = new java.io.ByteArrayInputStream(feed_info.feed_text.getBytes())

          // processing_result = getNewFeedEntries(id, url, new java.net.URL(url).openStream(), highestRecordedTimestamp, uriname)
          processing_result = getNewFeedEntries(id, url, page_is, highestRecordedTimestamp, uriname)
          log.debug("processFeed[${id}] got entries");
  
          new_entry_count = processing_result.numNewEntries
          processing_result.newEntries.each { entry ->

            logEvent('Feed.'+uriname,[
              timestamp:new Date(),
              message:"Detected new entry ${entry.id}",
              relatedType:"entry",
              relatedId:uriname+'/'+entry.id
            ]);

            log.debug("processFeed[${id}] Calling newEventService.handleNewEvent()");
            newEventService.handleNewEvent(id,entry)
          }
    
          if ( new_entry_count > 0 ) {
            // log.debug("processFeed[${id}] Complete having processed ${new_entry_count} new entries");
            logEvent('Feed.'+uriname,[
              timestamp:new Date(),
              message:"${uriname} Processing complete (${url}) - ${new_entry_count} new entries",
              relatedType:"feed",
              relatedId:uriname
            ]);
          }
          else {
            log.debug("processFeed[${id}] Although hash change detected, we found no new entries...");
          }

          if ( processing_result.highestSeenTimestamp ) {
            highestSeenTimestamp = processing_result.highestSeenTimestamp
          }
        }
        else {
          log.info("processFeed[${id}] ${url} unchanged, computed hash=${feed_info?.hash} prev hash=${hash} feedStatus=${feedStatus}");
        }
      }
      catch ( java.io.FileNotFoundException fnfe ) {
        error=true
        error_message = fnfe.toString()
        log.error("processFeed[${id}] ${url} Feed seems not to exist",fnfe.message);
        logEvent('Feed.'+uriname,[
          type: 'error',
          timestamp:new Date(),
          message:fnfe.toString(),
          relatedType:"feed",
          relatedId:uriname
        ]);
        SourceFeed.staticRegisterFeedIssue(id, "[0001] Feed seems not to exist","processFeed[${id}] ${url} Feed seems not to exist");
      }
      catch ( java.io.IOException ioe ) {
        error=true
        error_message = ioe.toString()
        log.error("processFeed[${id}] ${url} IO Problem feed_id:${id} feed_url:${url} ${ioe.message}",ioe.message);
        logEvent('Feed.'+uriname,[
          timestamp:new Date(),
          type: 'error',
          message:ioe.toString(),
          relatedType:"feed",
          relatedId:uriname
        ]);
        SourceFeed.staticRegisterFeedIssue(id, "[0002] IO Problem",ioe.message);
      }
      catch ( java.net.NoRouteToHostException no_route_e ) {
        error=true
        error_message = no_route_e.toString()
        log.error("processFeed[${id}] timeout feed_id:${id} feed_url:${url} ${no_route_e.message}")
        logEvent('Feed.'+uriname,[
          timestamp:new Date(),
          type: 'error',
          message:no_route_e.toString(),
          relatedType:"feed", 
          relatedId:uriname
        ]);
        SourceFeed.staticRegisterFeedIssue(id, "[0003] No Route", no_route_e.message);
      }
      catch ( org.apache.http.conn.ConnectTimeoutException ste ) {
        error=true
        error_message = ste.toString()
        log.error("processFeed[${id}] timeout feed_id:${id} feed_url:${url} ${ste.message}")
        logEvent('Feed.'+uriname,[
          timestamp:new Date(),
          type: 'error',
          message:ste.toString(),
          relatedType:"feed",
          relatedId:uriname
        ]);
        SourceFeed.staticRegisterFeedIssue(id, "[0004] Connect Timeout", ste.message);
      }
      catch ( java.net.SocketTimeoutException ste ) {
        error=true
        error_message = ste.toString()
        log.error("processFeed[${id}] timeout feed_id:${id} feed_url:${url} ${ste.message}")
        logEvent('Feed.'+uriname,[
          timestamp:new Date(),
          type: 'error',
          message:ste.toString(),
          relatedType:"feed",
          relatedId:uriname
        ]);
        SourceFeed.staticRegisterFeedIssue(id, "[0005] Socket Timeout", ste.message);
      }
      catch ( org.xml.sax.SAXParseException spe ) {
        error=true
        error_message = spe.toString()
        log.error("processFeed[${id}] XML Parse error feed_id:${id} feed_url:${url} ${spe.message}",spe.message);
        logEvent('Feed.'+uriname,[
          timestamp:new Date(),
          type: 'error',
          message:spe.toString(),
          relatedType:"feed",
          relatedId:uriname
        ]);
        SourceFeed.staticRegisterFeedIssue(id, "[0006] XML Parse problem", spe.message);
      }
      catch ( javax.net.ssl.SSLHandshakeException sslhe ) {
        error=true
        error_message = sslhe.toString()
        log.error("processFeed[${id}] SSL Handshake error feed_id:${id} feed_url:${url} ${sslhe.message}");
        logEvent('Feed.'+uriname,[
          timestamp:new Date(),
          type: 'error',
          message:sslhe.toString(),
          relatedType:"feed",
          relatedId:uriname
        ]);
        SourceFeed.staticRegisterFeedIssue(id, "[0007] XML Parse problem", sslhe.message);
      }
      catch ( java.lang.Exception e ) {
        error=true
        error_message = e.toString()
        log.error("GENERAL EXCEPTION processFeed[${id}] ${url} problem fetching feed: (${e.class.name}) ${e.message} (elapsed:${System.currentTimeMillis()-processing_start_time})");
        logEvent('Feed.'+uriname,[
          timestamp:new Date(),
          type: 'error',
          message:e.toString(),
          relatedType:"feed",
          relatedId:uriname
        ]);
        SourceFeed.staticRegisterFeedIssue(id, "[0008] processFeed[${id}] ${url} general problem fetching feed","${e.message} (elapsed:${System.currentTimeMillis()-processing_start_time})");
      }
      catch ( Throwable e ) {
        error=true
        error_message = e.toString()
        log.error("RUNTIME EXCEPTION processFeed[${id}] ${url} problem fetching feed (elapsed:${System.currentTimeMillis()-start_time})")
        logEvent('Feed.'+uriname,[
          timestamp:new Date(),
          type: 'error',
          message:e.toString(),
          relatedType:"feed",
          relatedId:uriname
        ]);
        SourceFeed.staticRegisterFeedIssue(id, "[0009] processFeed[${id}] ${url} general RUNTIME problem fetching feed", "${e.message} (elapsed:${System.currentTimeMillis()-start_time})")
      }
    }
    catch ( Exception e ) {
      log.error("Untrapped exception processing feed",e);
      SourceFeed.staticRegisterFeedIssue(id, "[0010] Untrapped", "${url} ${e.message} (elapsed:${System.currentTimeMillis()-start_time})")
    }
    finally {
      log.info("processFeed[${id}] fetch phase complete");
    }

    // log.debug("After processing ${url} entries, highest timestamp seen is ${highestSeenTimestamp}");

    try {
      SourceFeed.withNewTransaction {
        log.info("processFeed[${id}] Mark feed as paused");
        def sf = SourceFeed.get(id)
        log.debug("Lock...");
        sf.lock()
        log.debug("Locked...");
        sf.status = 'paused'
        sf.httpExpires = feed_info?.expires
        sf.httpLastModified = feed_info?.lastModified
    
        if ( newhash ) {
          // log.debug("Updating hash to ${newhash}");
          sf.lastHash = newhash
        }
    
        if ( highestSeenTimestamp ) {
          // log.debug("processFeed[${id}] Updating sf.highestTimestamp to be ${highestSeenTimestamp}");
          sf.highestTimestamp = highestSeenTimestamp
        }
        // sf.lastCompleted=start_time
        // Use the actual last completed time to try and even out the feed checking over time - this will skew each feed
        // So that all feeds become eligible over time, rather than being based on the start time of the batch
        sf.lastElapsed=start_time-sf.lastCompleted
        sf.lastError=error_message
  
        if ( error ) {
    
          sf.feedStatus='ERROR'
  
          if ( sf.consecutiveErrors == null ) 
            sf.consecutiveErrors=0;
  
          sf.consecutiveErrors++;
          sf.lastCompleted=System.currentTimeMillis();
          sf.latestHealth = statsService.logFailure(sf,start_time).latestHealth;
    
          logEvent('Feed.'+uriname,[
            timestamp:new Date(),
            type: 'error',
            message:'Feed status : ERROR '+error_message,
            relatedType:"feed",
            relatedId:uriname
          ]);
        }
        else { 
          sf.feedStatus='OK'
          sf.consecutiveErrors = 0;
          sf.lastCompleted=System.currentTimeMillis();
          sf.latestHealth = statsService.logSuccess(sf,start_time,new_entry_count).latestHealth;
        }
  
        // Extra 3000 for other operations
        if ( sf.lastCompleted - processing_start_time > ( MAX_HTTP_TIME + 3000 ) ) {
          log.warn("Processing feed[${id}] ${url} took ${sf.lastCompleted - processing_start_time} - longer than MAX_HTTP_TIME ${MAX_HTTP_TIME}. Investigate");
        }
  
        log.debug("processFeed[${id}] completed Saving source feed, set status back to ${sf.status}");
        sf.save();
        feedCheckLog.add([timestamp:new Date(),message:"Processing completed on ${id}/${url} at ${sf.lastCompleted} / ${error_message}"]);
      }
    }
    catch ( Throwable e ) {
      SourceFeed.staticRegisterFeedIssue(id, "[0011] Error Reporting Problem", "${url} ${e.message} (elapsed:${System.currentTimeMillis()-start_time})")
      log.error("Error closing out feed check",e);
    }

    log.info("continueToProcessFeed(${id},... returning (error=${error}, errorMessage=${error_message})");
  }


  /**
   * @Param feed_address
   * @Param httpExpires expires header from the last time we fetched this page
   * @Param httpLastModified last modified header from the last time we fetched this page
   *
   * @See http://stackoverflow.com/questions/7095897/im-trying-to-use-javas-httpurlconnection-to-do-a-conditional-get-but-i-neve
   * 
   */
  def fetchFeedPage(id, feed_address, httpExpires, httpLastModified) {
    long feedFetchStartTime = System.currentTimeMillis();

    def result = [:]
    
    HttpBuilder http_client = ApacheHttpBuilder.configure {
      request.uri = feed_address
      client.clientCustomizer { HttpClientBuilder builder ->
        RequestConfig.Builder requestBuilder = RequestConfig.custom()
        requestBuilder.connectTimeout = MAX_HTTP_TIME;
        requestBuilder.connectionRequestTimeout = MAX_HTTP_TIME;
        requestBuilder.socketTimeout = MAX_HTTP_TIME;
        builder.defaultRequestConfig = requestBuilder.build()
      }
    }

    http_client.head {

      // See https://http-builder-ng.github.io/http-builder-ng/asciidoc/html5/#_resource_last_modified_head
      response.success { FromServer resp ->

        String last_modified_string = FromServer.Header.find( resp.headers, 'Last-Modified')?.value
        Date last_modified_value  = last_modified_string ? parseDate(last_modified_string) : null
        if ( last_modified_value )
          result.lastModified = last_modified_value?.getTime() // Convert date to long

        String expires_string = FromServer.Header.find( resp.headers, 'Expires')?.value
        Date expires_value = expires_string ? parseDate( expires_string) : null
        if ( expires_value ) 
          result.expires = expires_value?.getTime()

        result.contentType = FromServer.Header.find( resp.headers, 'Content-Type')?.value
      }

      response.failure {
        log.warn("Unable to get last modified from server");
      }
    }

    log.debug("Intermediate: ${result}");

    // If the last modified from the server is null OR it is different to the last one we saw
    if ( ( result.lastModified == null ) || ( result.lastModified != httpLastModified ) ) {
      log.debug("processing content from ${feed_address} / ${result.lastModified} / ${httpLastModified}")
      Object response_content = http_client.get {

        response.parser('application/xml') { ChainedHttpConfig cfg, FromServer fs ->
          fs.inputStream.text
        }
        response.parser('application/rss+xml') { ChainedHttpConfig cfg, FromServer fs ->
          fs.inputStream.text
        }
        response.parser('application/atom+xml') { ChainedHttpConfig cfg, FromServer fs ->
          fs.inputStream.text
        }
        response.parser('text/xml') { ChainedHttpConfig cfg, FromServer fs ->
          fs.inputStream.text
        }

        response.failure { FromServer resp ->
          log.debug("Failure fetching content : ${resp}")
          return null;
        }
      }

      if ( response_content ) {
        if ( response_content instanceof String ) 
          result.feed_text = response_content
        else {
          // We weren't able to get a string from the content type.. lets see if we can process it anyway
          flagsService.raiseFlag('UnexpectedContentType','feedfacade.SourceFeed',id.toString());
          result.feed_text = new String(response_content)
        }

        MessageDigest md5_digest = MessageDigest.getInstance("MD5");
        md5_digest.update(result.feed_text.getBytes())
        byte[] md5sum = md5_digest.digest();
        result.hash = new BigInteger(1, md5sum).toString(16);
      }
    }
    

    return result;
  }

  def getNewFeedEntries(id, url, feed_is, highestRecordedTimestamp, uriname) {

    log.info("getNewFeedEntries(${id},${url}...)");

    def result = [:]
    result.numNewEntries=0
    result.newEntries=[]

    def atom_ns = new groovy.xml.Namespace('http://www.w3.org/2005/Atom', 'atom')
    def georss_ns = new groovy.xml.Namespace('http://www.georss.org/georss', 'georss')
    def cap_11_ns = new groovy.xml.Namespace('urn:oasis:names:tc:emergency:cap:1.1', 'cap')
    def ha_ns = new groovy.xml.Namespace('http://www.alerting.net/namespace/index_1.0','ha');

    // http://docs.groovy-lang.org/latest/html/api/groovy/util/XmlParser.html
    // def rootNodeParser = new XmlParser(false,false,true)
    def rootNodeParser = new XmlParser();

    def bom_is = new BOMInputStream(feed_is)
    if (bom_is.hasBOM() == false) {
      // log.debug("No BOM in input stream");
    }
    else {
      // log.debug("BOM detected in input stream");
    }

    // rootNodeParser.setFeature('http://apache.org/xml/features/disallow-doctype-decl',false);
    // log.debug("Parse...");
    def rootNode = rootNodeParser.parse(bom_is)

    // If using namespaces:: rootNode.[atom_ns.entry].each { entry ->
    // log.debug("getNewFeedEntries[${id}] Processing...(root node is ${rootNode.name().toString()})");
    def entry_count = 0;

    if ( rootNode.name().toString() == 'rss' ) { // It's RSS
      log.debug("RSS....");
      def feed_pubdate = null;
      if ( rootNode.channel.pubDate.size() == 1 ) {
        feed_pubdate = parseDate(rootNode.channel.pubDate.text())
        // If we could not parse the pubdate, then the feed fails validation - we might still be able to extract
        // useful CAP events from the items, but this really should be fixed.
        if ( feed_pubdate == null ) {
          flagsService.raiseFlag('InvalidFeedPubDate','feedfacade.SourceFeed',id.toString());
        }
      }
      else {
        // Default to NOW
        log.debug("RSS feed did not contain a pubdate, default to NOW");
        feed_pubdate = new Date()
      }

      rootNode.channel.item.each { item ->

        entry_count++;
       
        def parsed_pubdate = parseDate(item.pubDate.text())?.getTime();
        // If we could not parse the pubdate, then the feed fails validation - we might still be able to extract
        // useful CAP events from the items, but this really should be fixed.
        if ( parsed_pubdate == null ) {
          raiseFlag('InvalidItemPubDate','feedfacade.SourceFeed',id?.toString());
        }
        def entry_updated_time = item.pubDate.size() == 1 ? parsed_pubdate : feed_pubdate.getTime();

        if ( entry_updated_time ) {
          if ( entry_updated_time > highestRecordedTimestamp ?: 0 ) {
            log.info("getNewFeedEntries[${id}: RSS    -> ${item.guid.text()} has a timestamp (${entry_updated_time} > ${highestRecordedTimestamp} so process it");
            result.numNewEntries++
            result.newEntries.add([
                                   id:item.guid.text(),
                                   title:item.title.text(),
                                   summary:item.summary.text(),
                                   description:item.description.text(),
                                   link:item.link.text(),
                                   sourceDoc:item,
                                   type:'RSSEntry',
                                   uriname:uriname,
                                   timestamp: entry_updated_time
                                  ])
          }
          else {
            // log.debug("getNewFeedEntries[${id}]    -> Timestamp of entry ${item.guid.text()} (${entry_updated_time}) is lower than highest timestamp seen (${highestRecordedTimestamp})");
          }

          // Keep track of the highest timestamp we have seen in this pass over the changed feed
          if ( entry_updated_time && ( ( result.highestSeenTimestamp == null ) || ( result.highestSeenTimestamp < entry_updated_time ) ) ) {
            result.highestSeenTimestamp = entry_updated_time
          }
        }
        else {
          log.warn("FAILED to parse pubDate in RSS feed [${id}] (\"${item.pubDate?.text()}\" from ${url}) ");
        }

      }
    }
    else if ( ( rootNode.name().toString() == 'feed' ) || ( rootNode.name().toString() == '{http://www.w3.org/2005/Atom}feed' ) ) {  // IT's ATOM

      log.debug("ATOM");

      Date feed_pubdate = null;
      if ( rootNode.updated.size() == 1 ) {
        feed_pubdate = parseDate(rootNode.updated.text())
        if ( feed_pubdate == null ) {
          log.warn("Unable to parse feed pubdate ${rootNode.updated.text()}");
          flagsService.raiseFlag('InvalidFeedPubDate','feedfacade.SourceFeed',id.toString());
        }
        else {
          log.debug("Valid ATOM pub date ${feed_pubdate}")
        }
      }
      else {
        // Default to NOW
        log.debug("RSS feed did not contain a pubdate, default to NOW");
        feed_pubdate = new Date()
      }


      rootNode.entry.each { entry ->
        entry_count++;

        Date parsed_entry_date = parseDate(entry.updated.text());
        def entry_updated_time = null;

        if ( parsed_entry_date != null ) {
          entry_updated_time = parsed_entry_date.getTime()
        }
        else {
          flagsService.raiseFlag('InvalidItemPubDate','feedfacade.SourceFeed',id.toString());
        }

        // log.debug("getNewFeedEntries[${id}] -> processing entry node id:${entry.id.text()} :: ts:${entry_updated_time}");

        // See if this entry has a timestamp greater than any we have seen so far
        if ( entry_updated_time > highestRecordedTimestamp ?: 0 ) {
          log.debug("getNewFeedEntries[${id}] ATOM   -> ${entry.id.text()} has a timestamp (${entry_updated_time} > ${highestRecordedTimestamp} so process it");
          result.numNewEntries++

          switch ( entry.link.size() ) {
            case 0:
              log.warn("No links found in ATOM entry");
              break;
            case 1:
              // Only 1 link present - assume it is the correct type
              result.newEntries.add([
                                      id:entry.id.text(),
                                      title:entry.title.text(),
                                      summary:entry.summary?.text(),
                                      description:entry.description?.text(),
                                      link:entry.link.'@href',
                                      sourceDoc:entry,
                                      type:'ATOMEntry',
                                      uriname:uriname,
                                      timestamp: entry_updated_time
                                    ])

              break;
            default:
              def feed_link = null;
              entry.link.each { el ->
                if ( el.'@type'?.contains('cap') || el.'@type'?.contains('common-alerting-protocol') ) {
                  feed_link = el.'@href'
                }
              }

              if ( feed_link ) {
                result.newEntries.add([
                                       id:entry.id.text(),
                                       title:entry.title.text(),
                                       summary:entry.summary?.text(),
                                       description:entry.description?.text(),
                                       link:feed_link,
                                       sourceDoc:entry,
                                       type:'ATOMEntry',
                                       uriname:uriname,
                                       timestamp: entry_updated_time
                                      ])
              }
              else {
                log.warn("unable to extract feed link - parent url is ${url}"); // for ${rootNode}");
              }
              break;
          }
        }
        else {
          // log.debug("getNewFeedEntries[${id}]    -> Timestamp of entry ${entry.id.text()} (${entry_updated_time}) is <= highest timestamp (${highestRecordedTimestamp})");
        }
  
        // Keep track of the highest timestamp we have seen in this pass over the changed feed
        if ( entry_updated_time && ( ( result.highestSeenTimestamp == null ) || ( result.highestSeenTimestamp < entry_updated_time ) ) ) {
          result.highestSeenTimestamp = entry_updated_time
        }
      }
    }
    else {
      log.error("Unable to handle root element : ${rootNode.name().toString()}");
    }

    // Sort the new entries so they are in the correct timestamp order
    result.newEntries.sort {it.timestamp}

    log.debug("getNewFeedEntries[${id}] Found ${result.numNewEntries} new entries (checked ${entry_count}), highest timestamp seen ${result.highestSeenTimestamp}, highest timestamp recorded ${highestRecordedTimestamp}");
    result
  }

  private void reportNewAlerts(List alerts) {
    int i = 0;
    alerts.each { 
      log.debug("[${i++}] ${it.timestamp} ${it.id} ${it.title}");
    }
  }

  /**
   * Dates can come in many different formats, use the list defined in possible_date_formats as a list of possible formats.
   */
  Date parseDate(String datestr) {
    def parsed_date = null;
    if ( datestr && ( datestr.length() > 0 ) ) {
      for(Iterator<SimpleDateFormat> i = possible_date_formats.iterator(); ( i.hasNext() && ( parsed_date == null ) ); ) {
        try {
          parsed_date = i.next().clone().parse(datestr);
        }
        catch ( Exception e ) {
        }
      }
    }
    parsed_date
  }

  def logEvent(key,evt) {
   if ( evt ) {
     try {
        evt.source = key ?: 'Unknown'
        if ( evt.timestamp == null ) {
          evt.timestamp=new Date()
        }
        String event_id = java.util.UUID.randomUUID().toString()

        // Publish the event on rabbit
        if ( false ) {
          def evt_str = toJson(evt);
          def result = rabbitMessagePublisher.send {
                         exchange = "FeedFetcher"
                         routingKey = key
                         body = toJson(evt)
                       }
        }

      }
      catch ( Exception e ) {
        log.error("Problem trying to log event",e);
      }
    }
  }

  public Health health() {
    int errorCode = 0;
    if (errorCode != 0) {
      return Health.down().withDetail("feedChecker down", errorCode).build();
    }
    return Health.up().build();
  }

  public Map<String, Map> getActiveTaskReport() {
    Map<String, Map> result = [:]
    active_check_info.each { k, v ->
      result[k] = v
    }
    return result;
  }

}
