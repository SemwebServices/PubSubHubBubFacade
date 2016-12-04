package feedfacade

class AdminController {

  def newEventService

  def index() { 
    log.debug("AdminController::index");
    def result = [:]
    result
  }

  def notificationLog() {
    log.debug("AdminController::notificationLog");
    def result = [:]
    result.log = newEventService.eventLog
    result
  }
}
