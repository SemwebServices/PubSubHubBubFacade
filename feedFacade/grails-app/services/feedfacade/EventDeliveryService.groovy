package feedfacade

import grails.transaction.Transactional
import java.security.MessageDigest

import java.util.Calendar
import java.util.TimeZone

import org.hibernate.Session
import org.hibernate.StatelessSession


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

    long pending_event_count = SubscriptionEntry.executeQuery('select count(se) from SubscriptionEntry as se where se.status=:pending',[pending:'pending'],[readOnly:true]);

    while ( pending_event_count ) {
      log.debug("sendPendingEvents iterating :: pending_event_count=${pending_event_count}");

      def pending_events = SubscriptionEntry.executeQuery('select se.id from SubscriptionEntry as se where se.status=:pending order by eventDate asc',[pending:'pending'],[readOnly:true]);

      // Attempt to deliver any pending events
      pending_events.each { pending_event_id ->
        attemptDelivery(pending_event_id);
      }

      pending_event_count = SubscriptionEntry.executeQuery('select count(se) from SubscriptionEntry as se where se.status=:pending',[pending:'pending'],[readOnly:true]);
    }

    log.debug("sendPendingEvents completed");

    // Run a query to get the next 10 most urgent 
    running=false;
  }

  def attemptDelivery(event_id) {
    log.debug("Attempt delivery: ${event_id}");
  }
}

