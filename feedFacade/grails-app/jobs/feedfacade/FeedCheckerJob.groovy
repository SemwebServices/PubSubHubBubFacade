package feedfacade

class FeedCheckerJob {
  
  def feedCheckerService 

    static triggers = {
      simple repeatInterval: 60000l // execute job once in 5 seconds
    }

    def execute() {
      feedCheckerService.triggerFeedCheck()
    }
}
