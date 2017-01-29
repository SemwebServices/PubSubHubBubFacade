package feedfacade

class EventDeliveryJob {

  def eventDeliveryService

    
    static triggers = {
      // Wait 60 seconds after startup then check every 30
      simple startDelay:60000l, repeatInterval: 30000l // execute job once in 10 seconds
    }

    def execute() {
      eventDeliveryService.triggerEventDelivery()
    }
}

