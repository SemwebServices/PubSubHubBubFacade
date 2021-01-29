package feedfacade

class FeedTopic {

  SourceFeed ownerFeed
  Topic topic

  static constraints = {
    ownerFeed nullable:false
        topic nullable:false
  }

}
