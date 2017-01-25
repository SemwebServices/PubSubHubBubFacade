package feedfacade

import grails.transaction.Transactional
import java.security.MessageDigest

import java.util.Calendar
import java.util.TimeZone

import org.hibernate.Session
import org.hibernate.StatelessSession

import groovyx.net.http.HTTPBuilder
import static groovyx.net.http.Method.GET
import static groovyx.net.http.Method.POST
import static groovyx.net.http.ContentType.TEXT
import static groovyx.net.http.ContentType.XML
import static groovyx.net.http.ContentType.JSON


@Transactional
class EventDeliveryService {

  def sessionFactory
  boolean running = false;
  long error_count=0;

  def triggerEventDelivery() {
    log.debug("EventDeliveryService::triggerEventDelivery");
    if ( running ) {
      log.debug("Event Delivery already running - not launching another [${error_count++}]");
      if ( error_count > 10 )
        System.exit(0);
    }
    else {
      def error_count = 0;
      sendPendingEvents()
    }
  }

  def sendPendingEvents() {
    running=true;
    log.debug("EventDeliveryService::sendPendingEvents");

    long pending_event_count = SubscriptionEntry.executeQuery('select count(se) from SubscriptionEntry as se where se.status=:pending',[pending:'pending'],[readOnly:true])[0];

    while ( pending_event_count ) {
      log.debug("sendPendingEvents iterating :: pending_event_count=${pending_event_count}");

      def pending_events = SubscriptionEntry.executeQuery('select se.id from SubscriptionEntry as se where se.status=:pending order by eventDate asc',[pending:'pending'],[readOnly:true]);

      // Attempt to deliver any pending events
      pending_events.each { pending_event_id ->
        considerDelivery(pending_event_id);
      }

      pending_event_count = SubscriptionEntry.executeQuery('select count(se) from SubscriptionEntry as se where se.status=:pending',[pending:'pending'],[readOnly:true])[0];
    }

    log.debug("sendPendingEvents completed");

    // Run a query to get the next 10 most urgent 
    running=false;
  }

  def considerDelivery(event_id) {
    log.debug("Consider delivery: ${event_id}");
    def continue_processing = false;

    SubscriptionEntry.withNewTransaction {
      log.debug('Mark entry as in-process');
      def evt = SubscriptionEntry.get(event_id)
      evt.lock()
      if ( evt.status == 'pending' ) {
        log.debug("Event really is paused -- mark it as in process and proceed");
        evt.status = 'in-process'
        continue_processing = true;
        evt.save(flush:true, failOnError:true);
      }
      else {
        log.debug("On more thorough inspection, someone else already delivered or is handling the event, skip");
      }
    }

    if ( continue_processing ) {
      attemptDelivery(event_id)
    }
  }


  def attemptDelivery(event_id) {
    SubscriptionEntry.withNewTransaction {
      log.debug('Mark entry as in-process');
      def evt = SubscriptionEntry.get(event_id)
      evt.lock()


      // Try and deliver
      // Delivery to evt.owner.callback
      def url_as_obj = new java.net.URL(evt.owner.callback);
      def host_part = url_as_obj.protocol+'://'+url_as_obj.host+':'+url_as_obj.port;
      def path_part = url_as_obj.path + (url_as_obj.query?:'')

      log.debug("attemptDelivery ${host_part} ${path_part} mode is ${evt.owner.targetMimetype}");

      try {
        
        HTTPBuilder builder = new HTTPBuilder( host_part )
        builder.request( POST ) { 

          // set uriPath, e.g. /rest/resource
          uri.path = path_part


          // set the xml body, e.g. <xml>...</xml>
          if ( evt.owner.targetMimetype.equalsIgnoreCase('XML') ) {
            requestContentType = XML
            body = evt.entry.entry
          }
          else {
            requestContentType = JSON
            body = evt.entry.entryAsJson
          }
  
          // handle response
          response.success = { resp ->
            log.error("OK calling ${evt.owner.callback}");
            evt.status='delivered'
            evt.responseCode = resp.status
            evt.deliveryDate = new Date()
          }

          response.error = { resp ->
            log.error("Error code calling ${url}");
            evt.status='pending'
            evt.responseCode = resp.status
          }
        }
    
      }
      catch(Exception e ) {
        log.error("Problem trying to contact callback service",e);
      }

      // If result of delivery is OK, set status to delivered, otherwise reset to pending and increment error count
      evt.save(flush:true, failOnError:true);
    }
  }
}

