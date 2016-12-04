package feedfacade

import grails.transaction.Transactional
import java.security.MessageDigest


@Transactional
class NewEventService {

  
  def recent_notifications = new org.apache.commons.collections4.queue.CircularFifoQueue(100);
  
  def handleNewEvent(feed_id, entry) {
    log.debug("NewEventService::handleNewEvent(${feed_id},...)");

    def subscriptions = Subscription.executeQuery('select s from Subscription as s where exists ( select ft from FeedTopic as ft where ft.topic = s.topic and ft.ownerFeed.id = :id )',[id:feed_id]);
    subscriptions.each { sub ->

      if ( sub?.trimNs?.equalsIgnoreCase('y') ) {
        log.debug("Trim namespaces, regardless of JSON or XML response");
      }
      else {
      }

      //Create stand-alone XML for the entry
      def xml_text =  groovy.xml.XmlUtil.serialize(entry)

      log.debug("Got xml_text... target mime type is ${sub.targetMimetype}");

      def result = null;

      switch ( sub.targetMimetype ) {
        case 'json':
          // See snippet here https://gist.github.com/ianibo/fe36ab6220f820b1cd49
          log.debug("Notify via JSON");

          // First we need to render the XML entry as a stand-alone document

          def xs=new net.sf.json.xml.XMLSerializer();
          xs.setSkipNamespaces( true );  
          xs.setTrimSpaces( true );  
          xs.setRemoveNamespacePrefixFromElements( true );  
          result = xs.read(xml_text)
          break;

        default:
          log.debug("Notify via XML");
          result="hello"
          break;
      }

      recent_notification.add([targetMimetype:sub.targetMimetype, content:result, target: sub.callback, topic: sub.topic.name]);

      log.debug("done");
    }
  }

  def getEventLog() {
    return recent_notifications
  }
}
