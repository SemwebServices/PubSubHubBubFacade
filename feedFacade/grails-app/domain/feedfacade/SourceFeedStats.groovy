package feedfacade

import java.util.Calendar
import java.util.TimeZone

class SourceFeedStats {

  SourceFeed owner

  Long lastUpdate
  Long dayOfMonth
  Long hour
  Long errorCount
  Long successCount
  Long newEntryCount

  static constraints = {
       lastUpdate blank: false, nullable:true
       dayOfMonth blank: false, nullable:true
             hour blank: false, nullable:true
       errorCount blank: false, nullable:true
     successCount blank: false, nullable:true
    newEntryCount blank: false, nullable:true
  }


  static def logSuccess(owner, ts, nec) {
    SourceFeedStats.withNewTransaction {
      def stats = getStatsBucket(owner, ts)
      stats.successCount ++;
      stats.newEntryCount += nec;
      stats.save(flush:true, failOnError:true);
    }
  }

  static def logFailure(owner, ts) {
    SourceFeedStats.withNewTransaction {
      def stats = getStatsBucket(owner, ts)
      stats.errorCount ++;
      stats.save(flush:true, failOnError:true);
    }
  }

  static def getStatsBucket(owner, ts) {
    Calendar cal = Calendar.getInstance();
    cal.setTimeInMillis(ts)
    cal.setTimeZone(TimeZone.getTimeZone("UTC"));
    def hour = cal.get(Calendar.HOUR_OF_DAY);
    def dom = cal.get(Calendar.DAY_OF_MONTH);
    def bucket = SourceFeedStats.findByOwnerAndDayOfMonthAndHour(owner,dom,hour)
    if ( bucket ) {
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
