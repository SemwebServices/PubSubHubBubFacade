package feedfacade

class SourceFeedStats {

  SourceFeed owner

  Long lastUpdate
  Long dayOfMonth
  Long hour
  Long errorCount
  Long successCount
  Long newEntryCount
  Long health

  static constraints = {
       lastUpdate blank: false, nullable:true
       dayOfMonth blank: false, nullable:true
             hour blank: false, nullable:true
       errorCount blank: false, nullable:true
     successCount blank: false, nullable:true
    newEntryCount blank: false, nullable:true
           health blank: false, nullable:true
  }

}
