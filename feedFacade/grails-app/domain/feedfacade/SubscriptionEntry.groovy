package feedfacade

class SubscriptionEntry {

  Subscription owner
  Entry entry
  String reason
  String status
  String responseCode
  Date deliveryDate

  static constraints = {
               owner blank:false, nullable:false
               entry blank:false, nullable:false
              reason blank:false, nullable:false
              status blank:false, nullable:false
        responseCode blank:false, nullable:true
        deliveryDate blank:false, nullable:true
  }
}
