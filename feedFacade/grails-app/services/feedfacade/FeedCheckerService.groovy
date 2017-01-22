package feedfacade

import grails.transaction.Transactional
import java.security.MessageDigest
import org.apache.commons.io.input.BOMInputStream
import java.text.SimpleDateFormat

@Transactional
class FeedCheckerService {

  def running = false;
  def error_count = 0;
  def newEventService
  def statsService

  def feedCheckLog=[]

  def possible_date_formats = [
    // new SimpleDateFormat('yyyy-MM-dd'), // Default format Owen is pushing ATM.
    // new SimpleDateFormat('yyyy/MM/dd'),
    // new SimpleDateFormat('dd/MM/yyyy'),
    // new SimpleDateFormat('dd/MM/yy'),
    // new SimpleDateFormat('yyyy/MM'),
    // new SimpleDateFormat('yyyy')
    new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX"),
    new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX")
  ];

  def getLastLog() {
    feedCheckLog
  }


  def triggerFeedCheck() {
    log.debug("FeedCheckerService::triggerFeedCheck");
    if ( running ) {
      log.debug("Feed checker already running - not launching another [${error_count++}]");
      if ( error_count > 10 )
        System.exit(0);
    }
    else {
      def error_count = 0;
      doFeedCheck()
    }
  }

  def doFeedCheck() {
    log.debug("FeedCheckerService::doFeedCheck");
    running=true;
    feedCheckLog=[]
    def start_time = System.currentTimeMillis()

    feedCheckLog.add([timestamp:new Date(),message:'Feed check started']);

    log.debug("Finding all feeds due on or after ${start_time}");

    def processed_feed_counter = 0;

    try {
      def cont = true
      while ( cont ) {

        log.debug("Processing feed ${++processed_feed_counter}");

        // Grab the next feed to examine -- do it in a transaction
        def feed_info = null
        SourceFeed.withNewTransaction {
          log.debug("Lock next feed, and mark as running");
          
          def q = SourceFeed.executeQuery('select sf.id, sf.baseUrl, sf.lastHash, sf.highestTimestamp from SourceFeed as sf where sf.status=:paused AND sf.lastCompleted + sf.pollInterval < :ctm order by (sf.lastCompleted + sf.pollInterval) asc',[paused:'paused',ctm:start_time],[lock:false])

          def num_paused_feeds = q.size();
          log.debug("feedChecher detects ${num_paused_feeds} feeds paused that are overdue a check");

          if ( num_paused_feeds > 0 ) {
            def row = q.get(0)
            feed_info = [:]
            feed_info.id = row[0]
            feed_info.url = row[1]
            feed_info.hash = row[2]
            feed_info.highesTimestamp = row[3]
          }
         
        }

        if ( feed_info ) {
          feedCheckLog.add([timestamp:new Date(),message:'Identified feed '+feed_info]);
          log.debug("Process feed");
          processFeed(start_time, feed_info.id,feed_info.url,feed_info.hash,feed_info.highesTimestamp);
        }
        else {  
          // nothing left in the queue
          log.debug("Nothing left to process.. Continue");
          cont = false
        }
      }
    }
    catch ( Exception e ) {
      feedCheckLog.add([timestamp:new Date(),message:'Feed check error '+e.message]);
      log.error("Problem processing feeds",e);
      e.printStackTrace()
    }
    finally {
      log.info("processed ${processed_feed_counter} feeds");
    }

    feedCheckLog.add([timestamp:new Date(),message:'Feed check finished']);
    running=false;
  }

  def processFeed(start_time, id, url, hash, highestRecordedTimestamp) {

    log.debug("processFeed(${start_time},${id},${url},${hash},${highestRecordedTimestamp})");
    def newhash = null;
    def highestSeenTimestamp = null;
    def error = false
    def error_message = null
    def new_entry_count = 0

    def continue_processing = false;

    SourceFeed.withNewTransaction {
      log.debug('Mark feed as in-process');
      def sf = SourceFeed.get(id)
      sf.lock()
      if ( sf.status == 'paused' ) {
        log.debug("Feed really is paused -- mark it as in process and proceed");
        sf.status = 'in-process'
        continue_processing = true;
        sf.save(flush:true, failOnError:true);
      }
      else {
        log.debug("On more thorough inspection, someone else already grabbed the feed to process, so skip");
      }
    }

    if ( continue_processing ) {

      log.debug("Processing....feed ${id} :: ${url} ${hash}");
      
      try {
        def feed_info = fetchFeedPage(url);
        if ( ( hash == null ) || ( feed_info.hash != hash ) ) {
          newhash = feed_info.hash
          log.debug("Detected hash change (old:${hash},new:${feed_info.hash}).. Process");
    
          def processing_result = getNewEntries(id, new java.net.URL(url).openStream(), highestRecordedTimestamp)
          new_entry_count = processing_result.numNewEntries
          processing_result.newEntries.each { entry ->
            newEventService.handleNewEvent(id,entry)
          }
    
          if ( processing_result.highestSeenTimestamp ) {
            highestSeenTimestamp = processing_result.highestSeenTimestamp
          }
        }
        else {
          log.debug("${url} unchanged");
        }
      }
      catch ( java.io.FileNotFoundException fnfe ) {
        error=true
        error_message = fnfe.toString()
        log.error("Feed seems not to exist",fnfe.message);
      }
      catch ( java.io.IOException ioe ) {
        error=true
        error_message = ioe.toString()
        log.error("IO Problem feed_id:${id} feed_url:${url}",ioe.message);
      }
      catch ( Exception e ) {
        error=true
        error_message = e.toString()
        log.error("problem fetching feed",e);
      }
  
      log.debug("After processing entries, highest timestamp seen is ${highestSeenTimestamp}");
  
      SourceFeed.withNewTransaction {
        log.debug('Mark feed ${id} as paused');
        def sf = SourceFeed.get(id)
        sf.lock()
        sf.status = 'paused'
        if ( newhash ) {
          log.debug("Updating hash to ${newhash}");
          sf.lastHash = newhash
        }
        if ( highestSeenTimestamp ) {
          log.debug("Updating sf.highestTimestamp to be ${highestSeenTimestamp}");
          sf.highestTimestamp = highestSeenTimestamp
        }
        // sf.lastCompleted=start_time
        // Use the actual last completed time to try and even out the feed checking over time - this will skew each feed
        // So that all feeds become eligible over time, rather than being based on the start time of the batch
        sf.lastCompleted=System.currentTimeMillis();
        sf.lastError=error_message
  
        if ( error ) {
          sf.feedStatus='ERROR'
          statsService.logFailure(sf,start_time);
        }
        else { 
          sf.feedStatus='OK'
          statsService.logSuccess(sf,start_time,new_entry_count);
        }
  
        log.debug("Saving source feed");
        feedCheckLog.add([timestamp:new Date(),message:"Processing completed on ${id}/${url} at ${sf.lastCompleted} / ${error_message}"]);
        sf.save(flush:true, failOnError:true);
      }
    }

    feedCheckLog.add([timestamp:new Date(),message:"Process feed completed :: ${id} ${url} / error:${error} ${error_message}"]);
    log.debug("processFeed ${id} returning");
  }


  def fetchFeedPage(feed_address) {
    log.debug("fetchFeedPage(${feed_address})");
    def result = [:]
    def feed_url = new java.net.URL(feed_address)
    result.feed_text = feed_url.text
    MessageDigest md5_digest = MessageDigest.getInstance("MD5");
    md5_digest.update(result.feed_text.getBytes())
    byte[] md5sum = md5_digest.digest();
    result.hash = new BigInteger(1, md5sum).toString(16);
    result
  }

  def getNewEntries(id, feed_is, highestRecordedTimestamp) {
    def result = [:]
    result.numNewEntries=0
    result.newEntries=[]

    def atom_ns = new groovy.xml.Namespace("http://www.w3.org/2005/Atom", 'atom')
    // http://docs.groovy-lang.org/latest/html/api/groovy/util/XmlParser.html
    // def rootNodeParser = new XmlParser(false,false,true)
    def rootNodeParser = new XmlParser()

    def bom_is = new BOMInputStream(feed_is)
    if (bom_is.hasBOM() == false) {
      log.debug("No BOM in input stream");
    }
    else {
      log.debug("BOM detected in input stream");
    }

    // rootNodeParser.setFeature('http://apache.org/xml/features/disallow-doctype-decl',false);
    log.debug("Parse...");
    def rootNode = rootNodeParser.parse(bom_is)

    // If using namespaces:: rootNode.[atom_ns.entry].each { entry ->
    log.debug("Processing...");
    rootNode.entry.each { entry ->

      def entry_updated_time = parseDate(entry.updated.text()).getTime();
      
      log.debug("${entry.id.text()} :: ${entry_updated_time}");

      // Keep track of the highest timestamp we have seen in this pass over the changed feed
      if ( entry_updated_time && ( ( result.highestSeenTimestamp == null ) || ( result.highestSeenTimestamp < entry_updated_time ) ) ) {
        result.highestSeenTimestamp = entry_updated_time
      }

      // See if this entry has a timestamp greater than any we have seen so far
      if ( entry_updated_time > highestRecordedTimestamp ?: 0 ) {
        log.debug("    -> ${entry.id.text()} has a timestamp (${entry_updated_time} > ${highestRecordedTimestamp} so process it");
        result.numNewEntries++
        result.newEntries.add(entry)
      }
    }

    log.debug("Found ${result.numNewEntries} new entries, highest timestamp seen ${result.highestSeenTimestamp}, highest timestamp recorded ${highestRecordedTimestamp}");
    result
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

}
