package feedfacade

class SourceFeed {

  // The "Code" by which this feed will be known - Used for the front end feed url
  String uriname

  // Status - 'in-process', 'paused'  -- in-process if being actively checked
  String status 

  // Base URL of the underlying feed which will be polled
  String baseUrl 

  // The time the feed last changed from 'in-process' to 'paused'
  Long lastCompleted

  // The time the feed went from 'paused' to 'in-process'
  Long processingStartTime 

  // How long to wait between polls
  Long pollInterval

  // Highest timestamp seen on any entry
  Long highestTimestamp

  String feedStatus

  String lastError

  static hasMany = [
    topics:FeedTopic
  ]

  static mappedBy = [
    topics:'ownerFeed'
  ]

  // Last hash
  String lastHash

  static constraints = {
            lastHash blank: false, nullable:true
    highestTimestamp blank: false, nullable:true
          feedStatus blank: false, nullable:true
           lastError blank: false, nullable:true
  }

  static mapping = {
    topics sort:'topic'
  }

  public void addTopics(String topicList) {
    def topics = topicList.split(',');
    topics.each { raw_topic ->
      def normalised_topic = raw_topic.trim().toLowerCase()
      if ( normalised_topic.length() > 0 ) {
        log.debug(normalised_topic);
        def topic = Topic.findByName(normalised_topic) ?: new Topic(name:normalised_topic).save(flush:true, failOnError:true);
        def feed_topic = FeedTopic.findByOwnerFeedAndTopic(this, topic) ?: new FeedTopic(ownerFeed:this, topic:topic).save(flush:true, failOnError:true);
      }
    }
  }

  def getHistogram() {
    SourceFeedStats.executeQuery('select sfs from SourceFeedStats as sfs order by sfs.lastUpdate asc');
  }

  def getHistogramLastDay() {
    def start_time = System.currentTimeMillis() - ( 25 * 60 * 60 * 1000 ) 
    SourceFeedStats.executeQuery('select sfs from SourceFeedStats as sfs where sfs.lastUpdate > :start_time order by sfs.lastUpdate asc', [ start_time : start_time ]);
  }
}
