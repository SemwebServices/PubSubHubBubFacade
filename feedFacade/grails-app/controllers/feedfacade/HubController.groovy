package feedfacade

import grails.plugin.springsecurity.annotation.Secured


class HubController {

  def index() { 
    log.debug("HubController::index ${params}");
    def result = [:]

    switch ( request.method ) {
      case 'POST':
        if ( ( params.hub.mode.trim().toLowerCase() == 'subscribe' ) ||
             ( params.hub.mode.trim().toLowerCase() == 'unsubscribe' ) ) {
          handleSubscriptionRequest()
        }
        break;
      default:
        break;
    }
  }

  def handleSubscriptionRequest() {
    log.debug("handleSubscriptionRequest() ${params.hub.callback} ${params.hub.mode} ${params.hub.topic} ${params.hub.lease_seconds} ${params.hub.secret}");

    // Check everything is valid
    if ( params.hub.mode && params.hub.callback && params.hub.topic ) {
   
      // All mandatory params are present

      if ( params.hub.mode.trim().toLowerCase() == 'subscribe' ) {

        def pending_request_uuid = java.util.UUID.randomUUID().toString()
        def pr = new PendingRequest(guid:pending_request_uuid,
                                    requestTimestamp:new Date(),
                                    callback:params.hub.callback,
                                    mode:params.hub.mode,
                                    topic:params.hub.topic,
                                    status:'pending',
                                    leaseSeconds:params.hub.lease_seconds,
                                    trimNs:params.trimNs,
                                    targetMimetype:params.targetMimetype,
                                    secret:params.hub.secret).save(flush:true, failOnError:true);
        render(status: 202, text: 'Subscription Request Accepted. Request ID is '+pending_request_uuid)
        // RequestVerifierJob will pick up this pending request and send the validation request
      }
      else if ( params.hub.mode.trim().toLowerCase() == 'unsubscribe' ) {
      }
      else {
        render(status: 400, text: 'hub.mode must be one of subscribe or unsubscribe')
      }
    }
  }

}
