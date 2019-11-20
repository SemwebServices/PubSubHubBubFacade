package feedfacade

import com.budjb.rabbitmq.consumer.MessageContext

class FeedFeedbackConsumer {

  def grailsApplication

  static rabbitConfig = [
    "queue": "FeedFeedbackQueue"
  ]

  def handleMessage(def body, MessageContext context) {
    log.debug("FeedFeedbackConsumer::handleMessage() - ${context.envelope.routingKey} ${body.message}");
    String[] components = context.envelope.routingKey.split('\\.');


    if ( components.length > 1 ) {
      SourceFeed.withTransaction {
        SourceFeed sf = SourceFeed.findByUriname(components[1])
        Entry entry = null;
        if ( ( components.length == 3 ) && ( ! components[2].equalsIgnoreCase('unknown') ) ) {
          entry = Entry.findById(components[2]);
        }

  
        if ( ( sf != null ) && ( body != null ) ) {
          // String body_as_json = groovy.json.JsonOutput.toJson(body)
  
          // FeedEventLog fel = new FeedEventLog(
          //                            ownerFeed:sf,
          //                            entry:entry,
          //                            eventTs:System.currentTimeMillis(),
          //                            eventDetails:body_as_json,
          //                            message:body.message).save(flush:true, failOnError:true);
          sf.registerFeedIssue(body.message,body.message);
        }
      }
    }
    else {
      log.warn("Routing key for Feed Feedback ${context.envelope.routingKey} did not have enough components to be processable. ${components}");
    }
  }
}
