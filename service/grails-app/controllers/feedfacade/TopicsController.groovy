package feedfacade

import grails.plugin.springsecurity.annotation.Secured

class TopicsController {

  @Secured(['ROLE_USER', 'IS_AUTHENTICATED_FULLY'])
  def index() { 
    log.debug("SourcefeedController::index");
    def result = [:]
    result.topics = Topic.findAll()
    log.debug("found ${result.topics.size()} topics");
    result
  }

}
