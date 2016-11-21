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

    def q = SourceFeed.executeQuery('select sf.id, sf.baseUrl, sf.lastHash from SourceFeed as sf where sf.status=:paused AND sf.lastCompleted + sf.pollInterval < :ctm',[paused:'paused',ctm:start_time])

    q.each { feed_row ->
      processFeed(start_time, feed_row[0],feed_row[1],feed_row[2]);
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
    if ( ( feed_info == null ) || ( feed_info.hash != hash ) ) {
      log.debug("Detected hash change.. Process");
      getNewEntries(id, feed_info.feed_text)
      newhash = feed_info.hash
    }

    SourceFeed.withNewTransaction {
      log.debug('Mark feed as paused');
      def sf = SourceFeed.get(id)
      sf.lock()
      sf.status = 'paused'
      if ( newhash ) {
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
