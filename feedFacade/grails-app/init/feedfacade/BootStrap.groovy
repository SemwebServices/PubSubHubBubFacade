package feedfacade

class BootStrap {

  def init = { servletContext ->
    // See https://github.com/filtered-alert-hub/filtered-alert-hub/blob/master/feed-fetcher/alert-hub-sources-json-small.txt
    // N.B. Poll Interval is in milliseconds
    def ca_msc_en = SourceFeed.findByUriname('ca_msc_en') ?: new SourceFeed( uriname:'ca_msc_en', 
                                                                             status:'paused',
                                                                             baseUrl:'http://rss.naad-adna.pelmorex.com/',
                                                                             lastCompleted:new Long(0),
                                                                             processingStartTime:new Long(0),
                                                                             pollInterval:60*1000).save(flush:true, failOnError:true);
    ca_msc_en.addTopics('ca_msc_en')
    ca_msc_en.addTopics('AlertHub,TestFeed, NormalisationTest')

  }


  def destroy = {
  }
}
