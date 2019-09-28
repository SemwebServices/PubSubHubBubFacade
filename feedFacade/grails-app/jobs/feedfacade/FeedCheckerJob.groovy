package feedfacade

class FeedCheckerJob {
  
  def feedCheckerService 

    static triggers = {
      // Wait startDelay ms after startup then poll every 30000l, repeatIntervali ms
      simple startDelay:30000l,repeatInterval: 30000l
    }

    def execute() {
      feedCheckerService.triggerFeedCheck()
    }
}
