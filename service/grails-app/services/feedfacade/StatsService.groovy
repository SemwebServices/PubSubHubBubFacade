package feedfacade

import grails.gorm.transactions.*
import java.security.MessageDigest

import java.util.Calendar
import java.util.TimeZone

import org.hibernate.Session
import org.hibernate.StatelessSession


@Transactional
class StatsService {

  def sessionFactory

  def logSuccess(owner, ts, nec) {

    def result = [:]

    // log.debug("StatsService::logSuccess");
    try {

      StatelessSession statelessSession = sessionFactory.openStatelessSession()
      statelessSession.beginTransaction()

      def bucket = getStatsBucket(statelessSession, owner, ts)

      bucket.successCount ++;
      bucket.newEntryCount += nec;

      bucket.health = calculateHealth(bucket);
      result.latestHealth = bucket.health;

      if ( bucket.id == null ) {
        statelessSession.insert(bucket);
      }
      else {
        statelessSession.update(bucket);
      }

      statelessSession.getTransaction().commit()
      statelessSession.close()
    }
    catch ( Exception e ) {
      log.error("problem in logSuccess",e);
    }

    return result;
  }

  /**
   *
   */
  def logFailure(owner, ts) {

    def result = [:]

    // log.debug("StatsService::logFailure");
    try {
      StatelessSession statelessSession = sessionFactory.openStatelessSession()
      statelessSession.beginTransaction()

      def bucket = getStatsBucket(statelessSession, owner, ts)
      bucket.errorCount ++;

      bucket.health = calculateHealth(bucket);
      result.latestHealth = bucket.health;

      if ( bucket.id == null ) {
        statelessSession.insert(bucket);
      }
      else {
        statelessSession.update(bucket);
      }

      statelessSession.getTransaction().commit()
      statelessSession.close()
    }
    catch ( Exception e ) {
      log.error("problem in logFailure",e);
    }

    return result;
  }

  private int calculateHealth(SourceFeedStats sfs) {
    int percentage_passing = 0
    long total = sfs.errorCount + sfs.successCount
    if ( total > 0 ) {
      percentage_passing = ( sfs.successCount / total * 100 )
    }
    return percentage_passing;
  }

  /**
   * Update the source feed stats - Given an owner and an timestamp, increment the count for that
   * owner in the appropriate stats bucked for that timestamp
   */
  private def getStatsBucket(session, owner, ts) {
    
    Calendar cal = Calendar.getInstance();
    cal.setTimeInMillis(ts)
    cal.setTimeZone(TimeZone.getTimeZone("UTC"));
    Long hour = cal.get(Calendar.HOUR_OF_DAY);
    Long dom = cal.get(Calendar.DAY_OF_MONTH);

    def bucket = null;

    def buckets = session.createQuery('select sfs.id from SourceFeedStats as sfs where sfs.owner.id = :owner and sfs.dayOfMonth = :dom and sfs.hour = :hour')
                         .setProperties(['owner':owner.id, 'dom':dom, 'hour':hour])
                         .list()

    if ( buckets.size() > 1 ) {
      log.error("Multiple (${buckets.size()}) buckets found for slot ${owner.id} ${dom} ${hour} - investigate");
      // raiseEvent('MULTIPLE-STATS-SLOT:${owner.id}:${dom}:${hour}',[owner:owner.id, dom:dom, hour:hour])
    }

    def bucket_id = buckets.size() == 1 ? buckets.get(0) : null;  // SourceFeedStats.findByOwnerAndDayOfMonthAndHour(owner,dom,hour)

    if ( bucket_id ) {

      bucket = session.get(SourceFeedStats.class, bucket_id);
      bucket.owner = owner

      // Are we rolling over stats from more than a day ago? (milliseconds * seconds/min * min/hour * hour/day)
      if ( ( ts - bucket.lastUpdate ) > 1000*60*60*24 ) {
        bucket.errorCount=0
        bucket.successCount=0
        bucket.newEntryCount=0
      }
    }
    else {
      bucket = new SourceFeedStats(owner:owner, lastUpdate:ts, dayOfMonth:dom, hour: hour, errorCount:0, successCount:0, newEntryCount:0)
    }

    bucket.lastUpdate = ts
    bucket
  }
}

