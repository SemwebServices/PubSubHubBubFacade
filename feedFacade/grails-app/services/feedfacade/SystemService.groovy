package feedfacade;


import static java.util.concurrent.TimeUnit.*
import grails.async.Promise
import static grails.async.Promises.*


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
      int remaining_feeds = countOperatingButNotEnabled();
      while ( ( loop_protection > 0 ) && ( remaining_feeds > 0 ) ) {
          log.debug("Detected ${remaining_feeds} still to be enabled - activate next 10");
          enableUpToNFeeds(10);
          Thread.sleep(30*1000);
          loop_protection--;
          remaining_feeds = countOperatingButNotEnabled()
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

}
