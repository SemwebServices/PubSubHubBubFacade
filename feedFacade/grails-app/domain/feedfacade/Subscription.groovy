package feedfacade

class Subscription {

  String guid
  String callback
  Topic topic
  String leaseSeconds

  static constraints = {
                guid blank:false, nullable:false
            callback blank:false, nullable:false
               topic blank:false, nullable:false
        leaseSeconds blank:false, nullable:true
  }
}
