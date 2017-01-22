package feedfacade

class Entry {

  SourceFeed ownerFeed
  Long entryTs
  String entryAsJson
  String entry
  String entryHash
  String title
  String description
  String link

  static constraints = {
      ownerFeed blank: false, nullable:false
        entryTs blank: false, nullable:false
          entry blank: false, nullable:false
    entryAsJson blank: false, nullable:false
      entryHash blank: false, nullable:false
          title blank: false, nullable:true
    description blank: false, nullable:true
           link blank: false, nullable:true
  }

  static mapping = {
      ownerFeed column: 'ent_owner_feed_fk'
        entryTs column: 'ent_ts'
          entry type:'text', column: 'ent_xml'
    entryAsJson type:'text', column: 'ent_json'
      entryHash column: 'ent_hash'
          title column: 'ent_title'
    description type:'text',column: 'ent_description'
           link type:'text',column: 'ent_link'
  }

  transient def getNumSubscriptionEntries() {
    SubscriptionEntry.executeQuery('select count(se.id) from SubscriptionEntry as se where se.entry.id=:entry_id',[entry_id:this.id])[0]
  }
}
