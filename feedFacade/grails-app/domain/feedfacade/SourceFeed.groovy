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

  // Number of consecutive errors for retry
  Long consecutiveErrors

  String feedStatus

  String lastError

  String httpExpires

  String httpLastModified

  boolean enabled = false;

  static hasMany = [
    topics:FeedTopic,
    feedIssues:FeedIssue
  ]

  static mappedBy = [
    topics:'ownerFeed',
    feedIssues:'ownerFeed'
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
     consecutiveErrors blank: false, nullable:true
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

  def findTagValues(String tag) {
    return SourceTag.findAllByOwnerAndTag(this,tag)
  }

  public boolean isTagged(String tag, String value) {
    int num_rows = SourceTag.executeQuery('select count(*) from SourceTag as st where st.owner = :owner and st.tag.tag = :tag and st.value = :value',
                                          [owner:this, tag:tag, value:value]).get(0);
    return ( num_rows > 0 )
  }

  def getTimeToNextPoll() {
    return getNextPollTime() - System.currentTimeMillis()
  }

  def getNextPollTime() {
    lastCompleted + pollInterval
  }

  public static staticRegisterFeedIssue(Long id, String key, String message) {
   try {
      def issue = null;
      def issues = FeedIssue.executeQuery('select fi from FeedIssue as fi where fi.ownerFeed.id = :o and fi.key=:key',[o:id, key:key])
      switch ( issues?.size() ) {
        case 0:
          issue = new FeedIssue(ownerFeed:SourceFeed.get(id),
                                key:key,
                                message:message,
                                firstSeen:System.currentTimeMillis(),
                                occurrences:0);
          break;
        case 1:
          issue = issues[0]
          break;
        default:
          throw new RuntimeException("Too many issues for this feed with this key");
      }

      issue.lastSeen = System.currentTimeMillis()
      issue.occurrences++;
      issue.save(flush:true, failOnError:true);
    }
    catch ( Exception e ) {
      e.printStackTrace()
    }
  }

  public registerFeedIssue(String key, String message) {
    try {
      log.debug("${this.uriname} registerFeedIssue(${key},${message})");

      def issue = FeedIssue.findByOwnerFeedAndKey(this, key)

      if ( issue == null ) {
        issue = new FeedIssue(ownerFeed:this, key:key, message:message, firstSeen:System.currentTimeMillis(), occurrences:0);
      }

      issue.lastSeen = System.currentTimeMillis()
      issue.occurrences++;
      issue.save(flush:true, failOnError:true);
    }
    catch ( Exception e ) {
      e.printStackTrace()
    }
  }

  private List<FeedIssue>latestIssues(int max) {
    log.debug("${this.uriname} latestIssues(${max})");
    List<FeedIssue> r = FeedIssue.executeQuery('select fi from FeedIssue as fi where fi.ownerFeed=:o order by lastSeen desc',[o:this],[max:max]);
    return r
  }
}
