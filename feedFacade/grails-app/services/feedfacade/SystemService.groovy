package feedfacade;


import static java.util.concurrent.TimeUnit.*
import grails.async.Promise
import static grails.async.Promises.*
import grails.gorm.transactions.*


@Transactional
public class SystemService {

  private Map state = [:]

  public Map getCurrentState() {
    return state;
  }

  public void init() {
    freshenState();  
  }

  public synchronized void freshenState() {
    Setting setup_completed = Setting.findByKey('feedfacade.setupcompleted') ?: new Setting(key:'feedfacade.setupcompleted', value:'false').save(flush:true, failOnError:true);
    if ( setup_completed.value == 'true' ) {
      state.setup_completed = true;
    }
    else {
      state.setup_completed = false;
    }
    log.debug("freshenState() : ${state}");
  }

  private void spinUp() {
    log.debug("spinUp");
    try {
      int loop_protection = 100;
      int remaining_feeds = 0;

      remaining_feeds = countOperatingButNotEnabled();

      while ( ( loop_protection > 0 ) && ( remaining_feeds > 0 ) ) {
        SourceFeed.withTransaction {
          log.info("Detected ${remaining_feeds} still to be enabled - activate next set");
          enableUpToNFeeds(5);
          Thread.sleep(30*1000);
          loop_protection--;
          remaining_feeds = countOperatingButNotEnabled()
        }
      }
    }
    catch ( Exception e ) {
      log.error("Problem in spinUp",e);
    }

    log.debug("SpinUp - completed");
  }

  public synchronized void enableAllOperating() {
    log.debug("Enable all operating");
    Promise p = task {
      SourceFeed.withNewSession {
        this.spinUp();
      }
    }
    p.onError { Throwable err ->
      log.error("Problem",err);
    }
    p.onComplete { result ->
      log.debug("this.spinUp promise complete");
    }

    log.debug("Enable all operating - return");
  }

  public void disableAll() {
    SourceFeed.executeUpdate('update SourceFeed set enabled=:false where enabled=:true',['false':false,'true':true]);
  }

  private int countOperatingButNotEnabled() {
    int result = SourceFeed.executeQuery('select count(f.id) from SourceFeed as f where enabled=:false and capAlertFeedStatus=:operating',['false':false,'operating':'operating'])[0];
    log.debug("countOperatingButNotEnabled() = ${result}");
    return result;
  }

  private void enableUpToNFeeds(int n) {
    log.debug("enableUpToNFeeds(${n})");
    SourceFeed.executeQuery('select f.id from SourceFeed as f where enabled=:false and capAlertFeedStatus=:operating',
                            ['false':false,'operating':'operating'],
                            [offset:0, max:n]).each { feed_id ->
      log.debug("Enabling feed ${feed_id}");
      SourceFeed.executeUpdate('update SourceFeed set enabled=:true where id = :id',
                               ['true':true,id:feed_id]);
    
    }
  }

  // This method removes old subscription entries - by default entries older than 7 days will be expunged
  public expungeEntries() {
    Setting max_age_setting = Setting.findByKey('feedfacade.entryMaxAge') ?: new Setting(key:'feedfacade.entryMaxAge', value:'7').save(flush:true, failOnError:true);
    long max_age_millis = Long.valueOf(max_age_setting.value)*24*60*60*1000;
    if ( max_age_millis > 0 ) {
      long mw = System.currentTimeMillis() - max_age_millis;
      long c1 = Entry.executeQuery('select count(*) from Entry as e').get(0);
      long c2 = Entry.executeQuery('select count(*) from Entry as e where e.entryTs < :moving_wall',[moving_wall:mw]).get(0);
      log.info("Expunging entries older than ${max_age_setting.value} days (${max_age_millis}millis) - current count is ${c1}, ${c2} entries have expired");
      Entry.executeUpdate('delete from SubscriptionEntry se where se.entry in ( select e from Entry e where e.entryTs < :moving_wall)',[moving_wall:mw]);
      Entry.executeUpdate('delete from Entry e where e.entryTs < :moving_wall',[moving_wall:mw]);
      long c3 = Entry.executeQuery('select count(*) from Entry as e').get(0);
      log.info("After delete there are ${c3} entries remaining");

      // Expunge any feed issues that are older than moving wall
      FeedIssue.executeUpdate('delete from FeedIssue fi where fi.lastSeen < :moving_wall',[moving_wall:mw]);

      FlagEvent.executeUpdate('delete from FlagEvent fe where fe.expiryTime > :now',[now: System.currentTimeMillis()]);
    }
  }

}
