package feedfacade

class SourcefeedController {

  def index() { 
    log.debug("SourcefeedController::index");
    def result = [:]
    result.feeds = SourceFeed.findAll()
    result
  }

  def registerFeed() {
    log.debug("SourcefeedController::registerFeed");
    def result = [:]
    result
  }
}
