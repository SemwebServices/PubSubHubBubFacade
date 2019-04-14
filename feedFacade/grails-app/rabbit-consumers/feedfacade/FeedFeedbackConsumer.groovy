package feedfacade

import com.budjb.rabbitmq.consumer.MessageContext

class FeedFeedbackConsumer {

  def grailsApplication

  static rabbitConfig = [
    "queue": "FeedFeedbackQueue"
  ]

  def handleMessage(def body, MessageContext context) {
    log.debug("FeedFeedbackConsumer::handleMessage()");
  }
}
