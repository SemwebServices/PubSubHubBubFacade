package feedfacade

class SourcefeedController {

  def index() { 
    log.debug("SourcefeedController::index");
    def result = [:]
    result.feeds = SourceFeed.findAll()
    log.debug("found ${result.feeds.size()} feeds");
    result
  }

  def registerFeed() {
    log.debug("SourcefeedController::registerFeed");
    def result = [:]

    try {

      log.debug("Checking ${request.method}");

      if ( request.method=='POST' ) {

        log.debug("Checking post contains a uriname: ${params}");

        if ( ( params.feedname ) && ( params.feedname.trim().length() > 0 ) ) {

          log.debug("Find by Uriname ${params.feedname}");

          def feed = SourceFeed.findByUriname(params.feedname)

          if ( feed == null ) {
            log.debug("Create new feed ${params}");
            feed = new SourceFeed(uriname:params.feedname, baseUrl:params.baseUrl, pollInterval:params.pollInterval, status:'paused', processingStartTime:0, lastCompleted:0).save(flush:true, failOnError:true);
          }
          else {
            log.debug("Update feed ${params}");
            feed.pollInterval = params.pollInterval
            feed.baseUrl=params.baseUrl
            feed.save(flush:true, failOnError:true)
          }
        }
      }
    }
    catch ( Exception e ) {
      log.error("Problem creating feed",e);
    }

    result
  }
}
