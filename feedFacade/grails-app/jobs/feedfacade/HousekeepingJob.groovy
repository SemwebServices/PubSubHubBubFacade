package feedfacade

class HousekeepingJob {

  def systemService 

  static triggers = {
    // Wait 60 seconds after startup then check every 1 hour ( 60 * 60 * 1000 )
    simple startDelay:60000l, repeatInterval: 3600000l
  }

  def execute() {
    systemService.expungeEntries()
  }
}

