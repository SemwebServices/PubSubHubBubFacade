package feedfacade

class EventDeliveryJob {

  def eventDeliveryService

    static triggers = {
      simple repeatInterval: 10000l // execute job once in 10 seconds
    }

    def execute() {
      eventDeliveryService.triggerEventDelivery()
    }
}

