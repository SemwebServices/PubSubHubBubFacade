package feedfacade

class FeedIssue {

  SourceFeed ownerFeed
  long firstSeen
  long lastSeen
  long occurrences
  String key
  String message

  static constraints = {
    ownerFeed nullable:false
  }

  static mapping = {
    ownerFeed index:'fi_owner_feed'
    key index:'fi_owner_feed'
    message type:'text'
  }

}
