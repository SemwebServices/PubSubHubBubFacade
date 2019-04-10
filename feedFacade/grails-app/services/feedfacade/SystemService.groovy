package feedfacade;

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
}
