package feedfacade

class FeedCheckerJob {
  
  def feedCheckerService 

    static triggers = {
      // Wait 90 seconds after startup then poll every minute
      simple startDelay:90000l,repeatInterval: 60000l
    }

    def execute() {
      feedCheckerService.triggerFeedCheck()
    }
}
