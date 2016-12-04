package feedfacade

import grails.transaction.Transactional
import java.security.MessageDigest


@Transactional
class FeedCheckerService {

  def running = false;
  def error_count = 0;

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
    def start_time = System.currentTimeMillis()

    log.debug("Finding all feeds due on or after ${start_time}");

    try {
      def cont = true
      while ( cont ) {
        // Grab the next feed to examine -- do it in a transaction
        def feed_info = null
        SourceFeed.withNewTransaction {
          def q = SourceFeed.executeQuery('select sf.id, sf.baseUrl, sf.lastHash, sf.highestTimestamp from SourceFeed as sf where sf.status=:paused AND sf.lastCompleted + sf.pollInterval < :ctm order by (sf.lastCompleted + sf.pollInterval) asc',[paused:'paused',ctm:start_time],[lock:true])

          if ( q.size() > 0 ) {
            def row = q.get(0)
            feed_info = [:]
            feed_info.id = row[0]
            feed_info.url = row[1]
            feed_info.hash = row[2]
            feed_info.highesTimestamp = row[3]
          }
        }

        if ( feed_info ) {
          processFeed(start_time, feed_info.id,feed_info.url,feed_info.hash,feed_info.highesTimestamp);
        }
        else {  
          // nothing left in the queue
          cont = false
        }
      }
    }
    catch ( Exception e ) {
      e.printStackTrace()
    }

    running=false;
  }

  def processFeed(start_time, id, url, hash, highestRecordedTimestamp) {

    log.debug("processFeed(${start_time},${id},${url},${hash},${highestRecordedTimestamp})");
    def newhash = null;
    def highestSeenTimestamp = null;
    def error = false
    def error_message = null
    def new_entry_count = 0

    SourceFeed.withNewTransaction {
      log.debug('Mark feed as in-process');
      def sf = SourceFeed.get(id)
      sf.lock()
      sf.status = 'in-process'
      sf.save(flush:true, failOnError:true);
    }

    try {
      log.debug("Doing checking....${url} ${hash}");
      def feed_info = fetchFeedPage(url);
      if ( ( hash == null ) || ( feed_info.hash != hash ) ) {
        newhash = feed_info.hash
        log.debug("Detected hash change (old:${hash},new:${feed_info.hash}).. Process");
  
        def processing_result = getNewEntries(id, feed_info.feed_text, highestRecordedTimestamp)
        new_entry_count = processing_result.numNewEntries
  
        if ( processing_result.highestSeenTimestamp ) {
          highestSeenTimestamp = processing_result.highestSeenTimestamp
        }
      }
      else {
        log.debug("${url} unchanged");
      }
    }
    catch ( Exception e ) {
      error=true
      error_message = e.message()
      log.error("problem fetching feed",e);
    }

    log.debug("After processing entries, highest timestamp seen is ${highestSeenTimestamp}");

    SourceFeed.withNewTransaction {
      log.debug('Mark feed as paused');
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
      sf.lastCompleted=start_time
      sf.lastError=error_message

      if ( error ) {
        sf.feedStatus='ERROR'
        SourceFeedStats.logError(sf,start_time);
      }
      else { 
        sf.feedStatus='OK'
        SourceFeedStats.logSuccess(sf,start_time,new_entry_count);
      }

      sf.save(flush:true, failOnError:true);
    }
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

  def getNewEntries(id, feed_text, highestRecordedTimestamp) {
    def result = [:]
    result.numNewEntries=0
    // 2016-11-22T07:47:55-04:00
    def sdf = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX");
    def rootNode = new XmlParser().parseText(feed_text)
    rootNode.entry.each { entry ->
      def entry_updated_time = sdf.parse(entry.updated.text()).getTime();
      
      // log.debug("${entry.id.text()} :: ${entry_updated_time}");

      // Keep track of the highest timestamp we have seen in this pass over the changed feed
      if ( entry_updated_time && ( ( result.highestSeenTimestamp == null ) || ( result.highestSeenTimestamp < entry_updated_time ) ) ) {
        // log.debug("Update result.highestTimestamp to ${result.highestTimestamp}");
        result.highestSeenTimestamp = entry_updated_time
      }

      // See if this entry has a timestamp greater than any we have seen so far
      if ( entry_updated_time > highestRecordedTimestamp ?: 0 ) {
        log.debug("    -> ${entry.id.text()} has a timestamp (${entry_updated_time} > ${highestRecordedTimestamp} so process it");
        result.numNewEntries++
      }
    }

    log.debug("Found ${result.numNewEntries} new entries, highest timestamp seen ${result.highestSeenTimestamp}, highest timestamp recorded ${highestRecordedTimestamp}");
    result
  }
}
