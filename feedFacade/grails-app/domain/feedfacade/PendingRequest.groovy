package feedfacade

class PendingRequest {

  String guid
  Date requestTimestamp
  String callback
  String mode
  String topic
  String leaseSeconds
  String secret
  String status
  String trimNs
  String targetMimetype
  String deliveryMode

  static constraints = {
                guid blank:false, nullable:false
    requestTimestamp blank:false, nullable:false
            callback blank:false, nullable:false
                mode blank:false, nullable:false
               topic blank:false, nullable:false
              status blank:false, nullable:false
        leaseSeconds blank:false, nullable:true
              secret blank:false, nullable:true
              trimNs blank:false, nullable:true
      targetMimetype blank:false, nullable:true
        deliveryMode blank:false, nullable:true
  }
}
