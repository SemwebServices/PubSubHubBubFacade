package feedfacade

import grails.transaction.Transactional
import java.security.MessageDigest

@Transactional
class NewEventService {

  
  def recent_notifications = new org.apache.commons.collections4.queue.CircularFifoQueue(100);
  
  def handleNewEvent(feed_id, entryNode) {

    log.debug("NewEventService::handleNewEvent(${feed_id})");

    def entry = domNodeToString(entryNode)

    def entryHash = hashEntry(entry);

    def existingEntry = Entry.executeQuery('select e from Entry as e where e.ownerFeed.id = :owner_id and e.entryHash = :hash',[owner_id:feed_id, hash:entryHash])

    if ( existingEntry.size() == 0 ) {
      Entry.withNewTransaction {
        log.debug("New Entry:: ${feed_id} ${entryHash}");
        def owner_feed = SourceFeed.get(feed_id)
        Entry e = new Entry ( ownerFeed: owner_feed,
                              entryHash: entryHash,
                              entry: entry,
                              entryTs: System.currentTimeMillis()).save(flush:true, failOnError:true);
      }
      publish(feed_id, entry)
    }
    else {
      log.debug("Entry is a repeated hash");
    }
  }

  def hashEntry(entry) {
    MessageDigest md5_digest = MessageDigest.getInstance("MD5");
    md5_digest.update(entry.getBytes())
    byte[] md5sum = md5_digest.digest();
    new BigInteger(1, md5sum).toString(16);
  }

  def domNodeToString(node) {
    //Create stand-alone XML for the entry
    String xml_text =  groovy.xml.XmlUtil.serialize(node)
    xml_text
  }

  def publish(feed_id, xml_text) {

    log.debug("NewEventService::publish(${feed_id},...)");

    def subscriptions = Subscription.executeQuery('select s from Subscription as s where exists ( select ft from FeedTopic as ft where ft.topic = s.topic and ft.ownerFeed.id = :id )',[id:feed_id]);
    subscriptions.each { sub ->

      if ( sub?.trimNs?.equalsIgnoreCase('y') ) {
        log.debug("Trim namespaces, regardless of JSON or XML response");
      }
      else {
      }

      log.debug("Got xml_text... target mime type is ${sub.targetMimetype}");

      def result = null;

      switch ( sub.targetMimetype ) {
        case 'json':
          // See snippet here https://gist.github.com/ianibo/fe36ab6220f820b1cd49
          log.debug("Notify via JSON");
          String json_text = feedfacade.Utils.XmlToJson(xml_text);
          result = xml_text+'\n\n'+json_text
          break;

        default:
          log.debug("Notify via XML");
          result=xml_text
          break;
      }

      recent_notifications.add([targetMimetype:sub.targetMimetype, content:result, target: sub.callback, topic: sub.topic.name]);

      log.debug("done");
    }
  }

  def getEventLog() {
    return recent_notifications
  }
}
