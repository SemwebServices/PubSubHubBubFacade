package feedfacade

class BootStrap {

  def sysusers = [
    [name:'admin',pass:'admin',display:'Admin',email:'admin@semweb.co', roles:['ROLE_ADMIN','ROLE_USER']]
  ]

  // Definitive list here :: https://s3-eu-west-1.amazonaws.com/alert-hub-sources/json
  def feed_data = [
   [
      "source" : [
        "sourceId" : "mv-mms-en",
        "sourceName" : "Maldives: Maldives Meteorological Service",
        "guid" : "urn:oid:2.49.0.0.462.0",
        "author" : "humaid@meteorology.gov.mv",
        "sourceIsOfficial" : true,
        "sourceLanguage" : "en",
        "capAlertFeed" : "http://www.dhandhaana.gov.mv/eden/cap/public.rss",
        "authorityCountry" : "mv",
        "authorityAbbrev" : "mms"
      ]
    ], 
    [ "source" : [
        "sourceId" : "id-inatews-id",
        "sourceName" : "Indonesia: InaTEWS BMKG, Earthquake with magnitude 5.0 above",
        "guid" : "urn:oid:2.49.0.0.360.1",
        "author" : "m.prabowo@gmail.com",
        "sourceIsOfficial" : true,
        "sourceLanguage" : "en",
        "capAlertFeed" : "https://inatews.bmkg.go.id/rss/capatomlast40event.xml",
        "authorityCountry" : "id",
        "authorityAbbrev" : "inatews"
      ]
    ],
    [
      "source" : [
        "sourceId" : "nz-gns-en",
        "sourceName" : "New Zealand: GNS Science",
        "guid" : "urn:oid:2.49.0.0.554.1",
        "author" : "peter.kreft@metservice.com",
        "sourceIsOfficial" : true,
        "sourceLanguage" : "en",
        "capAlertFeed" : "http://api.geonet.org.nz/cap/1.2/GPA1.0/feed/atom1.0/quake",
        "authorityCountry" : "nz",
        "authorityAbbrev" : "geo"
      ]
    ], [
      "source" : [
        "sourceId" : "mx-smn-es",
        "sourceName" : "Mexico: CONAGUA - Servicio Meteorologico Nacional de Mexico",
        "guid" : "urn:oid:2.49.0.0.484.0",
        "author" : "jmanuel.caballero@conagua.gob.mx",
        "sourceIsOfficial" : true,
        "sourceLanguage" : "es",
        "capAlertFeed" : "https://correo1.conagua.gob.mx/feedsmn/feedalert.aspx",
        "authorityCountry" : "mx",
        "authorityAbbrev" : "smn"
      ]
    ], [
      "source" : [
        "sourceId" : "ca-msp-en",
        "sourceName" : "Canada: Ministère de la Sécurité publique du Québec",
        "guid" : "urn:oid:2.49.0.0.124.3",
        "author" : "norm.paulsen@ec.gc.ca",
        "sourceIsOfficial" : true,
        "sourceLanguage" : "en",
        "capAlertFeed" : "http://rss.naad-adna.pelmorex.com/",
        "authorityCountry" : "ca",
        "authorityAbbrev" : "msp"
      ]
    ], [
      "source" : [
        "sourceId" : "ca-msp-fr",
        "sourceName" : "Canada: Ministère de la Sécurité publique du Québec",
        "guid" : "urn:oid:2.49.0.0.124.3",
        "author" : "norm.paulsen@ec.gc.ca",
        "sourceIsOfficial" : true,
        "sourceLanguage" : "fr",
        "capAlertFeed" : "http://rss.naad-adna.pelmorex.com/",
        "authorityCountry" : "ca",
        "authorityAbbrev" : "msp"
      ]
    ], [
      "source" : [
        "sourceId" : "ca-aema-en",
        "sourceName" : "Canada: Alberta Emergency Management Agency (Government of Alberta, Ministry of Municipal Affairs)",
        "guid" : "urn:oid:2.49.0.0.124.2",
        "author" : "norm.paulsen@ec.gc.ca",
        "sourceIsOfficial" : true,
        "sourceLanguage" : "en",
        "capAlertFeed" : "http://emergencyalert.alberta.ca/aeapublic/feed.atom",
        "authorityCountry" : "ca",
        "authorityAbbrev" : "aema"
      ]
    ], [
      "source" : [
        "sourceId" : "ca-aema-fr",
        "sourceName" : "Canada: Alberta Emergency Management Agency (Government of Alberta, Ministry of Municipal Affairs)",
        "guid" : "urn:oid:2.49.0.0.124.2",
        "author" : "norm.paulsen@ec.gc.ca",
        "sourceIsOfficial" : true,
        "sourceLanguage" : "fr",
        "capAlertFeed" : "http://emergencyalert.alberta.ca/aeapublic/feed.atom",
        "authorityCountry" : "ca",
        "authorityAbbrev" : "aema"
      ]
    ], [
      "source" : [
        "sourceId" : "ca-msc-en",
        "sourceName" : "Canada: Meteorological Service of Canada",
        "guid" : "urn:oid:2.49.0.0.124.0",
        "author" : "norm.paulsen@ec.gc.ca",
        "sourceIsOfficial" : true,
        "sourceLanguage" : "en",
        "capAlertFeed" : "http://rss.naad-adna.pelmorex.com/",
        "authorityCountry" : "ca",
        "authorityAbbrev" : "msc"
      ]
    ], [
      "source" : [
        "sourceId" : "ca-msc-fr",
        "sourceName" : "Canada: Meteorological Service of Canada",
        "guid" : "urn:oid:2.49.0.0.124.0",
        "author" : "norm.paulsen@ec.gc.ca",
        "sourceIsOfficial" : true,
        "sourceLanguage" : "fr",
        "capAlertFeed" : "http://rss.naad-adna.pelmorex.com/",
        "authorityCountry" : "ca",
        "authorityAbbrev" : "msc"
      ]
    ], [
      "source" : [
        "sourceId" : "us-gs-eq-en",
        "sourceName" : "United States of America: United States Geological Survey, Earthquakes",
        "guid" : "urn:oid:2.49.0.0.840.2",
        "author" : "mark.paese@noaa.gov",
        "sourceIsOfficial" : true,
        "sourceLanguage" : "en",
        "capAlertFeed" : "http://earthquake.usgs.gov/earthquakes/feed/v1.0/summary/4.5_hour.atom",
        "authorityCountry" : "us",
        "authorityAbbrev" : "usgs-eq"
      ]
    ], [
      "source" : [
        "sourceId" : "us-epa-aq-en",
        "sourceName" : "United States of America: Environmental Protection Agency, Air Quality Alerts",
        "guid" : "urn:oid:2.49.0.0.840.3",
        "author" : "mark.paese@noaa.gov",
        "sourceIsOfficial" : true,
        "sourceLanguage" : "en",
        "capAlertFeed" : "http://feeds.enviroflash.info/cap/aggregate.xml",
        "authorityCountry" : "us",
        "authorityAbbrev" : "epa-aq"
      ]
    ], [
      "source" : [
        "sourceId" : "us-noaa-ntwc-en",
        "sourceName" : "United States of America: National Oceanic and Atmospheric Administration (NOAA), National Tsunami Warning Center",
        "guid" : "urn:oid:2.49.0.0.840.1",
        "author" : "mark.paese@noaa.gov",
        "sourceIsOfficial" : true,
        "sourceLanguage" : "en",
        "capAlertFeed" : "http://wcatwc.arh.noaa.gov/events/xml/paaqatom.xml",
        "authorityCountry" : "us",
        "authorityAbbrev" : "ntwc"
      ]
    ], [
      "source" : [
        "sourceId" : "us-noaa-nws-en",
        "sourceName" : "United States of America: National Oceanic and Atmospheric Administration (NOAA), National Weather Service ",
        "guid" : "urn:oid:2.49.0.0.840.0",
        "author" : "mark.paese@noaa.gov",
        "sourceIsOfficial" : true,
        "sourceLanguage" : "en",
        "capAlertFeed" : "https://alerts.weather.gov/cap/us.php?x=0",
        "authorityCountry" : "us",
        "authorityAbbrev" : "noaa"
      ]
    ], [
      "source" : [
        "sourceId" : "tz-tma-en",
        "sourceName" : "Tanzania, United Republic of: Tanzania Meteorological Agency",
        "guid" : "urn:oid:2.49.0.0.834.0",
        "author" : "samwel.mbuya@meteo.go.tz",
        "sourceIsOfficial" : true,
        "sourceLanguage" : "en",
        "capAlertFeed" : "http://tma.meteo.go.tz/cap/en/alerts/rss.xml",
        "authorityCountry" : "tz",
        "authorityAbbrev" : "tma"
      ]
    ], [
      "source" : [
        "sourceId" : "ph-pagasa-en",
        "sourceName" : "Philippines: Philippine Atmospheric Geophysical and Astronomical Services Administration",
        "guid" : "urn:oid:2.49.0.0.608.0",
        "author" : "arnel_manoos@yahoo.com",
        "sourceIsOfficial" : true,
        "sourceLanguage" : "en",
        "capAlertFeed" : "http://publicalert.pagasa.dost.gov.ph/feeds/",
        "authorityCountry" : "ph",
        "authorityAbbrev" : "pagasa"
      ]
    ], [
      "source" : [
        "sourceId" : "br-inm-pt",
        "sourceName" : "Brazil: Instituto Nacional de Meteorologia - INMET",
        "guid" : "urn:oid:2.49.0.0.76.0",
        "author" : "jmauro.rezende@inmet.gov.br",
        "sourceIsOfficial" : true,
        "sourceLanguage" : "pt",
        "capAlertFeed" : "http://alert-as.inmet.gov.br/cap_12/rss/alert-as.rss",
        "authorityCountry" : "br",
        "authorityAbbrev" : "inm"
      ]
    ], [
      "source" : [
        "sourceId" : "ar-smn-en",
        "sourceName" : "Argentina: Servicio Meteorologico Nacional",
        "guid" : "urn:oid:2.49.0.0.32.0",
        "author" : "ccampetella@smn.gov.ar",
        "sourceIsOfficial" : true,
        "sourceLanguage" : "en",
        "capAlertFeed" : "http://www.smn.gov.ar/?mod=feeds&amp;id=2",
        "authorityCountry" : "ar",
        "authorityAbbrev" : "smn"
      ]
    ], [
      "source" : [
        "sourceId" : "ai-dma-en",
        "sourceName" : "Anguilla: Disaster Management Anguilla",
        "guid" : "urn:oid:2.49.0.0.660.0",
        "author" : "GDe_souza@cmo.org.tt",
        "sourceIsOfficial" : true,
        "sourceLanguage" : "en",
        "capAlertFeed" : "https://209.59.120.36/capserver/index.atom",
        "authorityCountry" : "ai",
        "authorityAbbrev" : "dma"
      ]
    ], 
      //    [
      //      "source" : [
      //        "sourceId" : "iq-mowr-mosul-dam-ar",
      //        "sourceName" : "Iraqi Meteorlogical Organization and Seismology, Mosul Dam alerts in Arabic",
      //        "guid" : "urn:oid:2.49.0.0.368.1",
      //        "author" : "ali.karem@meteoseism.gov.iq",
      //        "sourceIsOfficial" : true,
      //        "sourceLanguage" : "ar",
      //        "capAlertFeed" : "tbd",
      //        "authorityCountry" : "iq",
      //        "authorityAbbrev" : "imeteoseism"
      //      ]
      //    ], [
      //      "source" : [
      //        "sourceId" : "iq-mowr-mosul-dam-en",
      //        "sourceName" : "Iraqi Meteorlogical Organization and Seismology, Mosul Dam alerts in English",
      //        "guid" : "urn:oid:2.49.0.0.368.1",
      //        "author" : "ali.karem@meteoseism.gov.iq",
      //        "sourceIsOfficial" : true,
      //        "sourceLanguage" : "en",
      //        "capAlertFeed" : "tbd",
      //        "authorityCountry" : "iq",
      //        "authorityAbbrev" : "meteoseism"
      //      ]
      //    ], [
      //      "source" : [
      //        "sourceId" : "iq-mowr-mosul-dam-ku",
      //        "sourceName" : "Iraqi Meteorlogical Organization and Seismology, Mosul Dam alerts in Kurdish",
      //        "guid" : "urn:oid:2.49.0.0.368.1",
      //        "author" : "ali.karem@meteoseism.gov.iq",
      //        "sourceIsOfficial" : true,
      //        "sourceLanguage" : "ku",
      //        "capAlertFeed" : "tbd",
      //        "authorityCountry" : "iq",
      //        "authorityAbbrev" : "meteoseism"
      //      ]
      //    ], 
    [
      "source" : [
        "sourceId" : "iq-ircs-mosul-dam-ar",
        "sourceName" : "Iraqi Red Crescent Society, Mosul Dam alerts in Arabic",
        "guid" : "urn:oid:2.49.0.4.77.1",
        "author" : "info@ircs.org.iq",
        "sourceIsOfficial" : true,
        "sourceLanguage" : "ar",
        "capAlertFeed" : "https://ircs.org.iq/mosul-dam-ar/rss.xml",
        "authorityCountry" : "iq",
        "authorityAbbrev" : "ircs"
      ]
    ], [
      "source" : [
        "sourceId" : "br-inmet-pt",
        "sourceName" : "Brazil: Instituto Nacional de Meteorologia - INMET",
        "guid" : "urn:oid:2.49.0.0.76.0",
        "author" : "jmauro.rezende@inmet.gov.br",
        "sourceIsOfficial" : true,
        "sourceLanguage" : "pt",
        "capAlertFeed" : "http://alert-as.inmet.gov.br/cap_12/rss/alert-as.rss",
        "authorityCountry" : "br",
        "authorityAbbrev" : "inmet"
      ]
    ] 
  ]

  def init = { servletContext ->
    // See https://github.com/filtered-alert-hub/filtered-alert-hub/blob/master/feed-fetcher/alert-hub-sources-json-small.txt
    // N.B. Poll Interval is in milliseconds
    // def ca_msc_en = SourceFeed.findByUriname('ca_msc_en') ?: new SourceFeed( uriname:'ca_msc_en', 
    //                                                                          status:'paused',
    //                                                                          baseUrl:'http://rss.naad-adna.pelmorex.com/',
    //                                                                          lastCompleted:new Long(0),
    //                                                                          processingStartTime:new Long(0),
    //                                                                          pollInterval:60*1000).save(flush:true, failOnError:true);
    // ca_msc_en.addTopics('ca_msc_en')
    // ca_msc_en.addTopics('AlertHub,TestFeed, NormalisationTest')

    feed_data.each { s ->
      // Array of maps containing a source elenment
      if ( s.source ) {
        log.debug("Validate source ${s.source.sourceId}");
        def source = SourceFeed.findByUriname(s.source.sourceId) 
        if ( source == null ) {
          source = new SourceFeed(   
                                   uriname: s.source.sourceId,
                                   name: s.source.sourceName,
                                   status:'paused',
                                   baseUrl:s.source.capAlertFeed,
                                   lastCompleted:new Long(0),
                                   processingStartTime:new Long(0),
                                   pollInterval:60*1000).save(flush:true, failOnError:true);

          source.addTag('sourceIsOfficial',"${s.source.sourceIsOfficial}");
          source.addTag('sourceLanguage',"${s.source.sourceLanguage}");
          source.addTag('authorityCountry',"${s.source.authorityCountry}");
          source.addTag('authorityAbbrev',"${s.source.authorityAbbrev}");
          source.addTag('author',"${s.source.author}");
          source.addTag('guid',"${s.source.guid}");
          source.addTopics("${s.source.sourceId},AllFeeds,${s.source.authorityCountry},${s.source.authorityAbbrev}")
        }
        else {
          if ( source.baseUrl != s.source.capAlertFeed ) {
            log.debug("Detected a change in config feed url :: ${source.baseUrl} != ${s.capAlertFeed}. Update..");
            source.baseUrl = s.capAlertFeed;
            source.save(flush:true, failOnError:true);
          }
        }
      }
    }

    setUpUserAccounts()
  }

  def setUpUserAccounts() {
    sysusers.each { su ->
      log.debug("test ${su.name} ${su.pass} ${su.display} ${su.roles}");
      def user = User.findByUsername(su.name)
      if ( user ) {
        if ( user.password != su.pass ) {
          log.debug("Hard change of user password from config ${user.password} -> ${su.pass}");
          user.password = su.pass;
          user.save(failOnError: true)
        }
        else {
          log.debug("${su.name} present and correct");
        }
      }
      else {
        log.debug("Create user...");
        user = new User(
                      username: su.name,
                      password: su.pass,
                      display: su.display,
                      email: su.email,
                      enabled: true).save(failOnError: true)
      }

      log.debug("Add roles for ${su.name} (${su.roles})");
      su.roles.each { r ->

        def role = Role.findByAuthority(r) ?: new Role(authority:r).save(flush:true, failOnError:true)

        if ( ! ( user.authorities.contains(role) ) ) {
          log.debug("  -> adding role ${role} (${r})");
          UserRole.create user, role
        }
        else {
          log.debug("  -> ${role} already present");
        }
      }
    }
  }


  def destroy = {
  }
}
