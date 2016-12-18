package feedfacade

import grails.plugin.springsecurity.annotation.Secured



class SubscriptionController {

  @Secured(['ROLE_USER', 'IS_AUTHENTICATED_FULLY'])
  def index() { 
    log.debug("SourcefeedController::index");
    def result = [:]
    result
  }
}
