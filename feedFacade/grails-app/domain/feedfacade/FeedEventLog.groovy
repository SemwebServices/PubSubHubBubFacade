package feedfacade

class FeedEventLog {

  SourceFeed ownerFeed
  Entry entry
  Long eventTs
  String eventDetails
  String message


  static constraints = {
    ownerFeed blank: false, nullable:false
    entry blank: false, nullable:true
    eventTs blank: false, nullable:true
    eventDetails blank: false, nullable:false
    message blank: false, nullable:true
    
  }

  static mapping = {
    id column:'fel_id'
    version column:'fel_version'
    ownerFeed column:'fel_owner_feed'
    entry column:'fel_entry'
    eventTs column:'fel_event_ts'
    eventDetails type:'text', column: 'fel_details'
    message type:'text', column: 'fel_message'
  }

}

