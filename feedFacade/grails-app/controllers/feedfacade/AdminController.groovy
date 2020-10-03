package feedfacade

import grails.plugin.springsecurity.annotation.Secured

class AdminController {

  def newEventService
  def feedCheckerService
  def systemService

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

  @Secured(['ROLE_USER', 'IS_AUTHENTICATED_FULLY'])
  def feedCheckerLog() {
    [feedCheckerLog:feedCheckerService.getLastLog()]
  }

  @Secured(['ROLE_USER', 'IS_AUTHENTICATED_FULLY'])
  def enableAll() {
    log.debug("Call systemService.enableAllOperating()");
    SourceFeed.withTransaction {
      systemService.enableAllOperating();
    }
    redirect(url: request.getHeader('referer'))
  }

  @Secured(['ROLE_USER', 'IS_AUTHENTICATED_FULLY'])
  def disableAll() {
    systemService.disableAll();
    redirect(url: request.getHeader('referer'))
  }

  @Secured(['ROLE_USER', 'IS_AUTHENTICATED_FULLY'])
  def status() {
    def result=[:]
    result.activeTasks=feedCheckerService.getActiveTaskReport()
    result
  }
}
