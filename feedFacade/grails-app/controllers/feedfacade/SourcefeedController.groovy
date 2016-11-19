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

    if ( ( params.uriname ) && ( params.uriname.trim().length() > 0 ) ) {
      def feed = SourceFeed.findByUriname(params.uriname)

      if ( feed == null ) {
        feed = new SourceFeed(uriname:uriname, baseUrl:params.baseUrl, pollInterval:params.pollInterval, status:'paused', processingStartTime:0, lastCompleted:0).save(flush:true, failOnError:true);
      }
      else {
        feed.pollInterval = params.pollInterval
        feed.baseUrl=params.baseUrl
        feed.save(flush:true, failOnError:true)
      }

    }

    result
  }
}
