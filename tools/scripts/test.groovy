#!groovy

@Grapes([
  @GrabResolver(name='mvnRepository', root='http://central.maven.org/maven2/'),
  @Grab(group='net.sf.json-lib', module='json-lib', version='2.4', classifier='jdk15'),
  @Grab(group='xom', module='xom', version='1.2.5')
])


String xml_document = '''<?xml version="1.0" encoding="UTF-8"?>
<entry xmlns="http://www.w3.org/2005/Atom"> <id>tag:rss.naad-adna.pelmorex.com,2016-12-07:feed.atom/urn:oid:2.49.0.1.124.0326941791.2016</id> <title>snow squall watch in effect</title> <updated>2016-12-07T16:23:21-00:00</updated> <author> <name>Environment Canada</name> </author> <link href="http://capcp1.naad-adna.pelmorex.com/2016-12-07/2016_12_07T16_23_21_00_00Iurn_oid_2.49.0.1.124.0326941791.2016.xml" rel="alternate"/> <georss:polygon xmlns:georss="http://www.georss.org/georss">45.5697 -79.0368 45.5573 -79.5934 45.901 -79.7785 45.9047 -79.6173 45.9261 -79.5385 45.9315 -79.5193 45.9989 -79.2711 45.5697 -79.0368</georss:polygon> <georss:polygon xmlns:georss="http://www.georss.org/georss">45.5363 -80.4369 45.5136 -80.5641 45.8248 -80.8486 45.8894 -80.7432 45.8856 -80.5706 45.9001 -79.899 45.901 -79.7785 45.5573 -79.5934 45.5363 -80.4369</georss:polygon> <georss:polygon xmlns:georss="http://www.georss.org/georss">45.5697 -79.0368 45.5074 -79.0029 45.531 -78.918 45.4148 -78.8595 45.4143 -78.8593 45.3844 -78.9711 45.1471 -78.8439 45.1057 -78.9997 45.1399 -79.3895 45.5573 -79.5934 45.5697 -79.0368</georss:polygon> <georss:polygon xmlns:georss="http://www.georss.org/georss">45.0772 -80.0186 45.0191 -80.1856 45.5136 -80.5641 45.5363 -80.4369 45.5573 -79.5934 45.1399 -79.3895 45.0772 -80.0186</georss:polygon> <summary type="html">Description: ### Snow squalls cause weather conditions to vary considerably; changes from clear skies to heavy snow within just a few kilometres are common. Travel may be hazardous due to sudden changes in the weather. Visibility may be significantly and suddenly reduced to near zero. Snow squall watches are issued when conditions are favourable for the formation of bands of snow that could produce intense accumulating snow or near zero visibilities. Please continue to monitor alerts and forecasts issued by Environment Canada. To report severe weather, send an email to ec.cpio-tempetes-ospc-storms.ec@canada.ca or tweet reports to #ONStorm.&lt;br&gt;Expires: 2016-12-08T08:23:21-00:00&lt;br&gt;Originated from CAP Alert: Environment Canada, 2016-12-07T16:23:21-00:00, urn:oid:2.49.0.1.124.0326941791.2016&lt;br&gt;Area: South River - Burk's Falls, Bayfield Inlet - Dunchurch, Huntsville - Baysville, Town of Parry Sound - Rosseau - Killbear Park</summary> <category term="status=Actual"/> <category term="msgType=Update"/> <category term="scope=Public"/> <category term="language=en-CA"/> <category term="category=Met"/> <category term="severity=Moderate"/> <category term="urgency=Future"/> <category term="certainty=Likely"/> <category term="event=snow squall"/> </entry>
'''
net.sf.json.xml.XMLSerializer xs = new net.sf.json.xml.XMLSerializer();
xs.setSkipNamespaces(true);
// This one causes us to not get any content - don't use it!!
// xs.setSkipWhitespace(true);
xs.setTrimSpaces(true);
xs.setNamespaceLenient(true);
xs.setRemoveNamespacePrefixFromElements(true);

net.sf.json.JSON json_obj = xs.read(xml_document);
String json_text = json_obj.toString(2);

println(json_text)

