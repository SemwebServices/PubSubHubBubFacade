package feedfacade

import grails.transaction.Transactional
import java.security.MessageDigest


@Transactional
class RequestVerifierService {

  def running = false;
  def error_count = 0;

  def triggerCheck() {
    log.debug("RequestVerifierService::triggerCheck");
    if ( running ) {
      log.debug("Request verifier already running - not launching another [${error_count++}]");
      if ( error_count > 10 )
        System.exit(0);
    }
    else {
      def error_count = 0;
      doCheck()
    }
  }

  def doCheck() {
    log.debug("RequestVerifierService::doCheck");
    running=true;
    def start_time = System.currentTimeMillis()

    log.debug("Finding all feeds due on or after ${start_time}");

    try {
      def cont = true
      while ( cont ) {
        // Grab the next feed to examine -- do it in a transaction
        def request_info = null

        // Grab the next pending request off the list of pending requests. Lock the request so nobody else can tamper with it whilst we
        // Do so. At the end we will change the status of the request we grabbed to "processing"
        PendingRequest.withNewTransaction {
          def q = PendingRequest.executeQuery('select pr.id, pr.mode, pr.callback, pr.topic, pr.guid, pr.leaseSeconds from PendingRequest as pr where pr.status = :pending order by pr.requestTimestamp asc',[pending:'pending'],[lock:true])
          if ( q.size() > 0 ) {
            def row = q.get(0)
            request_info = [:]
            request_info.id = row[0]
            request_info.mode = row[1]
            request_info.callback = row[2]
            request_info.topic = row[3]
            request_info.guid = row[4]
            request_info.leaseSeconds = row[5]

            log.debug("Found request to verify ${request_info} - mark as in processing");
            PendingRequest.executeUpdate('update PendingRequest set status = :p where id = :id',[p:'procesing', id:request_info.id]);
          }
          else {
            log.debug("No pending requests to verify");
          }
        }

        // If we managed to pull a pending request off the list and mark it as processing, process it now.
        if ( request_info ) {
          processRequest(start_time, request_info)
          // Go around the loop again, there may still be requests waiting....
        }
        else {  
          // nothing left in the queue
          cont = false
        }
      }
    }
    catch ( Exception e ) {
      e.printStackTrace()
    }

    running=false;
  }

  def processRequest(start_time, request_info) {
    log.debug("processRequest(${start_time}, ${request_info})");

    def challenge = java.util.UUID.randomUUID().toString()
    def verify_request = request_info.callback+"?hub.mode=${request_info.mode}&hub.topic=${request_info.topic}&hub.challenge=${challenge}"
    log.debug("Constructed verify  request");
    def verify_url = new java.net.URL(verify_request)
    def response = verify_url.text
    log.debug(response)

    if ( response.equals(challenge) ) {
      log.debug("Client responded with challenge, subscriber intent verified");

      def topic = Topic.findByName(request_info.topic.trim().toLowerCase())

      if ( topic ) {
        if ( request_info.mode == 'subscribe' ) {
          def s = new Subscription(
                                   guid:request_info.guid,
                                   callback:request_info.callback,
                                   topic:topic,
                                   leaseSeconds:request_info.leaseSeconds,
                                   trimNs:request_info.trimNs,
                                   targetMimetype:request_info.targetMimetype
                                  ).save(flush:true, failOnError:true);
          log.debug("Subscription created...");
        }
      }
    }
    else {
      log.debug("Client did not respond correctly to challenge, subscriber intent not verified");
    }

    PendingRequest.withNewTransaction {
      PendingRequest.executeUpdate('delete from PendingRequest where id = :id',[id:request_info.id]);
    }
  }
}
