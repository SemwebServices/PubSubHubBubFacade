package feedfacade

import grails.plugin.springsecurity.annotation.Secured

class AdminController {

  def newEventService
  def feedCheckerService

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
    def result = [:]
    SourceFeed.executeUpdate('update SourceFeed set enabled=:true where enabled=:false and capAlertFeedStatus=:operating',['false':false,'true':true,'operating':'operating']);

    redirect(url: request.getHeader('referer'))
  }

  @Secured(['ROLE_USER', 'IS_AUTHENTICATED_FULLY'])
  def disableAll() {
    def result = [:]
    SourceFeed.executeUpdate('update SourceFeed set enabled=:false where enabled=:true',['false':false,'true':true]);
    redirect(url: request.getHeader('referer'))
  }
}
