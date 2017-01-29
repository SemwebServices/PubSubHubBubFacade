package feedfacade

class Subscription {

  String guid
  String callback
  Topic topic
  String leaseSeconds
  String trimNs
  String targetMimetype
  String deliveryMode

  String subType

  static constraints = {
                guid blank:false, nullable:false
            callback blank:false, nullable:false
               topic blank:false, nullable:false
        leaseSeconds blank:false, nullable:true
              trimNs blank:false, nullable:true
      targetMimetype blank:false, nullable:true
        deliveryMode blank:false, nullable:true
             subType blank:false, nullable:true
  }
}
