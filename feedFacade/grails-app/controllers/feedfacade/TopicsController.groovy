package feedfacade

class TopicsController {

  def index() { 
    log.debug("SourcefeedController::index");
    def result = [:]
    result.topics = Topic.findAll()
    log.debug("found ${result.topics.size()} topics");
    result
  }

}
