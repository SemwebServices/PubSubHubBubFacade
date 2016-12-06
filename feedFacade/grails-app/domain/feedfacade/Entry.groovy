package feedfacade

class Entry {

  SourceFeed ownerFeed
  Long entry_ts
  String entry

  static constraints = {
    ownerFeed blank: false, nullable:false
        entry blank: false, nullable:false
  }

}
