package feedfacade

class FeedTopic {

  SourceFeed ownerFeed
  Topic topic

  static constraints = {
    ownerFeed blank: false, nullable:false
        topic blank: false, nullable:false
  }

}
