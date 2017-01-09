import groovy.json.JsonSlurper


// Take the official list of CAP feeds at https://s3-eu-west-1.amazonaws.com/alert-hub-sources/json and convert it to
// Our feedFacade canonical TSV format, then inject into feedFacade -- essentially sync up

java.net.URL cap_feeds_json_url = new java.net.URL('https://s3-eu-west-1.amazonaws.com/alert-hub-sources/json')

def slurper = new groovy.json.JsonSlurper()
def cap_feeds = slurper.parse(cap_feeds_json_url)

// "sourceId" : "ai-dma-en",
// "sourceName" : "Anguilla: Disaster Management Anguilla",
// "guid" : "urn:oid:2.49.0.0.660.0",
// "author" : "GDe_souza@cmo.org.tt",
// "sourceIsOfficial" : true,
// "sourceLanguage" : "en",
// "capAlertFeed" : "https://209.59.120.36/capserver/index.atom",
// "authorityCountry" : "ai",
// "authorityAbbrev" : "dma"

StringWriter sw = new StringWriter()
sw.write('sourceId     sourceName      tag:guid        tag:author      tag:sourceIsOfficial    tag:sourceLanguage      tag:authorityCountry    tag:authorityAbbrev	feedUrl\n');

// Canonical Format
// sourceId	sourceName	tag:guid	tag:author	tag:sourceIsOfficial	tag:sourceLanguage	tag:authorityCountry	tag:authorityAbbrev	feedUrl

cap_feeds?.sources?.each { cap_source ->
  def csl = cap_source.source
  // println(cap_source);
  // println(cap_source.source?.sourceId);
  sw.write("${csl.sourceId}	${csl.sourceName}	${csl.guid}	${csl.author}	${csl.sourceIsOfficial}	${csl.sourceLanguage}	${csl.authorityCountry}	${csl.authorityAbbrev}	${csl.capAlertFeed}\n");

}


def canonical_tsv_feed_file = sw.toString()

println(canonical_tsv_feed_file)
