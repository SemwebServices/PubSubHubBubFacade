package feedfacade

class PendingRequest {

  String guid
  Date requestTimestamp
  String callback
  String mode
  String topic
  String leaseSeconds
  String secret

  static constraints = {
                guid blank:false, nullable:false
    requestTimestamp blank:false, nullable:false
            callback blank:false, nullable:false
                mode blank:false, nullable:false
               topic blank:false, nullable:false
        leaseSeconds blank:false, nullable:true
              secret blank:false, nullable:true
  }
}
