package feedfacade

class Topic {

  // The "Code" by which this feed will be known - Used for the front end feed url
  String name

  def getSubscriptions() {
    return Subscription.findAllByTopic(this)
  }

  static constraints = {
    name blank: false, nullable:false, unique: true
  }

 
}
