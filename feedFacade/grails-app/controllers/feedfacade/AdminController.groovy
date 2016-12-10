package feedfacade

import grails.plugin.springsecurity.annotation.Secured

class AdminController {

  def newEventService

  @Secured(['ROLE_USER', 'IS_AUTHENTICATED_FULLY'])
  def index() { 
    log.debug("AdminController::index");
    def result = [:]
    result
  }

  @Secured(['ROLE_USER', 'IS_AUTHENTICATED_FULLY'])
  def notificationLog() {
    log.debug("AdminController::notificationLog");
    def result = [:]
    result.log = newEventService.eventLog
    result
  }
}
