package feedfacade

class SubscriptionEntry {

  Subscription owner
  Entry entry
  String reason
  String status
  String responseCode
  Date eventDate
  Date deliveryDate

  static constraints = {
               owner blank:false, nullable:false
               entry blank:false, nullable:false
              reason blank:false, nullable:false
              status blank:false, nullable:false
        responseCode blank:false, nullable:true
           eventDate blank:false, nullable:false
        deliveryDate blank:false, nullable:true
  }
}
