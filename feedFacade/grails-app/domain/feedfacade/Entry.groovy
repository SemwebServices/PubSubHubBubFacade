package feedfacade

class Entry {

  SourceFeed ownerFeed
  Long entryTs
  String entry
  String entryHash

  static constraints = {
    ownerFeed blank: false, nullable:false
      entryTs blank: false, nullable:false
        entry blank: false, nullable:false
    entryHash blank: false, nullable:false
  }

  static mapping = {
    entry type:'text'
  }

}
