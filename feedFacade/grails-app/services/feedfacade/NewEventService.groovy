package feedfacade

import grails.transaction.Transactional
import java.security.MessageDigest

@Transactional
class NewEventService {

  
  def recent_notifications = new org.apache.commons.collections4.queue.CircularFifoQueue(100);
  
  def handleNewEvent(feed_id, entryNode) {

    Entry.withNewTransaction {

      log.debug("NewEventService::handleNewEvent(${feed_id})");

      def entry_title = entryNode.title.text()
      def entry_summary = entryNode.summary.text()
      def entry_description = entryNode.description.text()
      def entry_link = entryNode.link.'@href'

      log.debug("${entry_title} ${entry_summary} ${entry_description} ${entry_link}");

      def entry = domNodeToString(entryNode)

      def entryHash = hashEntry(entry);

      log.debug("Make sure that we don't already have an entry for feed/hash ${feed_id} ${entryHash}");

      if ( entry?.length() > 0 ) {
  
        def existingEntries = Entry.executeQuery('select e.id from Entry as e where e.ownerFeed.id = :owner_id and e.entryHash = :hash',[owner_id:feed_id, hash:entryHash])
  
        if ( existingEntries.size() == 0 ) {
          log.debug("None found -- create");
  
          Entry e = null
  
          Entry.withNewTransaction {
            log.debug("New Entry:: ${feed_id} ${entryHash}");
            def owner_feed = SourceFeed.get(feed_id)
  
            String json_text = feedfacade.Utils.XmlToJson(entry);
  
            e = new Entry ( 
                              ownerFeed: owner_feed,
                                  title: entry_title,
                            description: entry_summary,
                                   link: entry_link,
                              entryHash: entryHash,
                                  entry: entry,
                            entryAsJson: json_text,
                                entryTs: System.currentTimeMillis()).save(flush:true, failOnError:true);
          }
  
          publish(feed_id, e)
        }
        else {
          log.debug("Entry is a repeated hash");
        }
      }
      else {
        log.error("Result of domNodeToString was null");
      }
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

  def publish(feed_id, entry) {

    log.debug("NewEventService::publish(${feed_id},...)");

    // Find all subscriptions where the sub has a topic which intersects with any of the topics for this feed
    def subscriptions = Subscription.executeQuery('select s from Subscription as s where exists ( select ft from FeedTopic as ft where ft.topic = s.topic and ft.ownerFeed.id = :id )',[id:feed_id]);

    // Iterate through all subscriptions
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
          result = entry.entryAsJson
          break;

        default:
          log.debug("Notify via XML");
          result=entry.entry
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
