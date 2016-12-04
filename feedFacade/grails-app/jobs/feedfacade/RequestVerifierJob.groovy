package feedfacade

class RequestVerifierJob {
  
  def requestVerifierService 

  static triggers = {
    simple repeatInterval: 60000l // execute job once per 60 seconds
  }

  def execute() {
    requestVerifierService.triggerCheck()
  }
}
