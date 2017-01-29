package feedfacade

import grails.plugin.springsecurity.annotation.Secured



class SubscriptionController {

  @Secured(['ROLE_USER', 'IS_AUTHENTICATED_FULLY'])
  def index() { 
    log.debug("SourcefeedController::index");
    def result = [:]
    result
  }

  @Secured(['ROLE_USER', 'IS_AUTHENTICATED_FULLY'])
  def newRabbitQueue() {
    log.debug("SourcefeedController::newRabbitQueue");
    def result = [:]

    // Create a new subscription to a topic which fires a RabbitMQ message to a named queue
    def s = new Subscription(
                             guid:java.util.UUID.randomUUID().toString(),
                             callback:params.queueName,
                             topic:Topic.findByName(params.topicName),
                             subType:'rabbit'
                            ).save(flush:true, failOnError:true);
    log.debug("RabbitMQ Subscription created...");
    redirect(action:'index');
  }

}
