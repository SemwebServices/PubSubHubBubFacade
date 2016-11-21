package feedfacade

import grails.transaction.Transactional

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

    def q = SourceFeed.executeQuery('select sf.id, sf.lastHash, sf.baseUrl from SourceFeed as sf where sf.status=:paused AND sf.lastCompleted + sf.pollInterval < :ctm',[paused:'paused',ctm:start_time])

    q.each { feed_row ->
      processFeed(feed_row[0],feed_row[1],feed_row[2]);
    }

    running=false;
  }

  def processFeed(id, url, hash) {

    log.debug("processFeed(${id},${url},${hash})");

    SourceFeed.withNewTransaction {
      log.debug('Mark feed as in-process');
      def sf = SourceFeed.get(id)
      sf.lock()
      sf.status = 'in-process'
      sf.save(flush:true, failOnError:true);
    }

    log.debug("Doing checking....${url} ${hash}");

    SourceFeed.withNewTransaction {
      log.debug('Mark feed as paused');
      def sf = SourceFeed.get(id)
      sf.lock()
      sf.status = 'paused'
      sf.save(flush:true, failOnError:true);
    }

    
  }

}
