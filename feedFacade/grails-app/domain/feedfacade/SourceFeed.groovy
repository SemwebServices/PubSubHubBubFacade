package feedfacade

class SourceFeed {

  // The "Code" by which this feed will be known - Used for the front end feed url
  String uriname

  // Status - 'in-process', 'paused'  -- in-process if being actively checked
  String status 

  // CAP Community evaluation -- unusable, operating, testing
  String capAlertFeedStatus

  String name

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

  // Time taken to process feed on last run
  Long lastElapsed

  String feedStatus

  String lastError

  String httpExpires

  String httpLastModified

  boolean enabled = false;

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
                  name blank: false, nullable:true
      highestTimestamp blank: false, nullable:true
            feedStatus blank: false, nullable:true
             lastError blank: false, nullable:true
           lastElapsed blank: false, nullable:true
    capAlertFeedStatus blank: false, nullable:true
           httpExpires blank: false, nullable:true
      httpLastModified blank: false, nullable:true
               enabled blank: false, nullable:true
  }

  static mapping = {
    topics sort:'topic'
    lastError type:'text'
  }

  public void addTopics(String topicList) {
    def topics = topicList.split(',');
    topics.each { raw_topic ->
      def normalised_topic = raw_topic.trim().toLowerCase()
      if ( normalised_topic.length() > 0 ) {
        log.debug("    Adding topic ${normalised_topic} to feed ${this.id}");
        def topic = Topic.findByName(normalised_topic) ?: new Topic(name:normalised_topic).save(flush:true, failOnError:true);
        def feed_topic = FeedTopic.findByOwnerFeedAndTopic(this, topic) ?: new FeedTopic(ownerFeed:this, topic:topic).save(flush:true, failOnError:true);
      }
    }
  }

  def getHistogram() {
    SourceFeedStats.executeQuery('select sfs from SourceFeedStats as sfs where sfs.owner = :owner order by sfs.lastUpdate asc',[ owner:this]);
  }

  def getHistogramLastDay() {
    def start_time = System.currentTimeMillis() - ( 25 * 60 * 60 * 1000 ) 
    SourceFeedStats.executeQuery('select sfs from SourceFeedStats as sfs where sfs.owner = :owner and sfs.lastUpdate > :start_time order by sfs.lastUpdate asc', [ owner:this, start_time : start_time ]);
  }

  def addTag(tag_txt,value) {
    if ( value && ( value.trim().length() > 0 ) ) {
      def tag = Tag.findByTag(tag_txt) ?: new Tag(tag:tag_txt).save(flush:true, failOnError:true);
      def source_tag = SourceTag.findByOwnerAndTagAndValue(this,tag,value) ?: new SourceTag(owner:this, tag:tag, value:value).save(flush:true, failOnError:true);
    }
  }

  def getTags() {
    SourceTag.findAllByOwner(this)
  }

  def getTimeToNextPoll() {
    return getNextPollTime() - System.currentTimeMillis()
  }

  def getNextPollTime() {
    lastCompleted + pollInterval
  }
}
