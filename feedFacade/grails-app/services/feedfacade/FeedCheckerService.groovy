package feedfacade

import grails.transaction.Transactional
import java.security.MessageDigest


@Transactional
class FeedCheckerService {

  def running = false;

  def triggerFeedCheck() {
    log.debug("FeedCheckerService::triggerFeedCheck");
    if ( running ) {
      log.debug("Feed checker already running - not launching another");
    }
    else {
      doFeedCheck()
    }
  }

  def doFeedCheck() {
    log.debug("FeedCheckerService::doFeedCheck");
    running=true;
    def start_time = System.currentTimeMillis()

    log.debug("Finding all feeds due on or after ${start_time}");

    def cont = true
    while ( cont ) {
      // Grab the next feed to examine -- do it in a transaction
      def feed_info = null
      SourceFeed.withNewTransaction {
        def q = SourceFeed.executeQuery('select sf.id, sf.baseUrl, sf.lastHash from SourceFeed as sf where sf.status=:paused AND sf.lastCompleted + sf.pollInterval < :ctm order by (sf.lastCompleted + sf.pollInterval) asc',[paused:'paused',ctm:start_time],[lock:true])

        if ( q.size() > 0 ) {
          def row = q.get(0)
          feed_info = [:]
          feed_info.id = row[0]
          feed_info.url = row[1]
          feed_info.hash = row[2]
        }
      }

      if ( feed_info ) {
        processFeed(start_time, feed_info.id,feed_info.url,feed_info.hash);
      }
      else {  
        // nothing left in the queue
        cont = false
      }
    }

    running=false;
  }

  def processFeed(start_time, id, url, hash) {

    log.debug("processFeed(${start_time},${id},${url},${hash})");
    def newhash = null;

    SourceFeed.withNewTransaction {
      log.debug('Mark feed as in-process');
      def sf = SourceFeed.get(id)
      sf.lock()
      sf.status = 'in-process'
      sf.save(flush:true, failOnError:true);
    }

    log.debug("Doing checking....${url} ${hash}");
    def feed_info = fetchFeedPage(url);
    if ( ( hash == null ) || ( feed_info.hash != hash ) ) {
      newhash = feed_info.hash
      log.debug("Detected hash change (old:${hash},new:${feed_info.hash}).. Process");
      getNewEntries(id, feed_info.feed_text)
    }
    else {
      log.debug("${url} unchanged");
    }

    SourceFeed.withNewTransaction {
      log.debug('Mark feed as paused');
      def sf = SourceFeed.get(id)
      sf.lock()
      sf.status = 'paused'
      if ( newhash ) {
        log.debug("Updating hash to ${newhash}");
        sf.lastHash = newhash
      }
      sf.lastCompleted=start_time
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

  def getNewEntries(id, feed_text) {
    log.debug("getNewEntries(${id},...feedText...)");
  }
}
