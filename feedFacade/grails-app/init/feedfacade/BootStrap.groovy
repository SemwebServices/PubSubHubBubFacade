package feedfacade

class BootStrap {

  def sysusers = [
    [
      name:'admin',
      pass:System.getenv('YARM_ADM_PW')?:'ChangeMeImmediately',
      display:'Admin',
      email:'admin@semweb.co', 
      roles:['ROLE_ADMIN','ROLE_USER']
    ]
  ]

  def init = { servletContext ->
    setUpSources()
    setUpUserAccounts()
  }

  def setUpSources() {
    try {
      // We add an enabled flag which defaults to false. Sources must now be added then enabled in the web interface.
      SourceFeed.executeUpdate('update SourceFeed set enabled = :enabled where enabled is null',[enabled:true]);

      def live_json_data = new groovy.json.JsonSlurper().parse(new java.net.URL('https://s3-eu-west-1.amazonaws.com/alert-hub-sources/json'))
      ingestCapFeeds(live_json_data.sources)
    }
    catch ( Exception e ) {
      log.error("problem syncing cap feed list",e);
    }

  }

  def ingestCapFeeds(fd) {
    fd?.each { s ->
      try {
        // Array of maps containing a source elenment
        if ( s.source ) {
          log.debug("Validate source ${s.source.sourceId}");
          def source = SourceFeed.findByUriname(s.source.sourceId) 
          if ( source == null ) {
            log.debug("  --> Create (enabled:false)");
            source = new SourceFeed(   
                                     uriname: s.source.sourceId,
                                     name: s.source.sourceName,
                                     status:'paused',
                                     baseUrl:s.source.capAlertFeed,
                                     lastCompleted:new Long(0),
                                     processingStartTime:new Long(0),
                                     capAlertFeedStatus: s.source.capAlertFeedStatus?.toLowerCase(),
                                     pollInterval:60*1000,
                                     enabled:false).save(flush:true, failOnError:true);

            source.addTag('sourceIsOfficial',"${s.source.sourceIsOfficial}");
            source.addTag('sourceLanguage',"${s.source.sourceLanguage}");
            source.addTag('authorityCountry',"${s.source.authorityCountry}");
            source.addTag('authorityAbbrev',"${s.source.authorityAbbrev}");
            source.addTag('registerUrl',"${s.source.registerUrl}");
            source.addTag('logoUrl',"${s.source.logoUrl}");
            source.addTag('author',"${s.source.author}");
            source.addTag('guid',"${s.source.guid}");
            source.addTopics("${s.source.sourceId},AllFeeds,${s.source.authorityCountry},${s.source.authorityAbbrev}")
          }
          else {

            if ( ( ! source.baseUrl.equals(s.source.capAlertFeed) ) ||
                 ( ! (source.capAlertFeedStatus?:'').equals(s.source.capAlertFeedStatus?:'') ) ) {
              log.debug("  --> changed :: (db)${source.baseUrl}/${source.capAlertFeedStatus}  != (new)${s.source.capAlertFeed}/${s.source.capAlertFeedStatus}. Update.. (enabled:${source.enabled})");
              source.baseUrl = s.source.capAlertFeed;
              source.capAlertFeedStatus = s.source.capAlertFeedStatus?.toLowerCase();
              source.save(flush:true, failOnError:true);
            }
            else {
              log.debug("  --> unchanged (enabled:${source.enabled})");
            }
          }
        }
      }
      catch ( Exception e ) {
        log.error("Problem trying to add or update entry ${s.source}",e);
      }
    }

  }

  def setUpUserAccounts() {
    sysusers.each { su ->
      log.debug("user name:${su.name} ${su.pass} display-as:${su.display} roles:${su.roles}");
      def user = User.findByUsername(su.name)
      if ( user ) {
        if ( user.password == null ) {
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
 
    SourceFeed.executeUpdate('update SourceFeed set status=:paused where capAlertFeedStatus=:op or capAlertFeedStatus=:test',[paused:'paused',op:'operating',test:'testing']);
    
  }


  def destroy = {
  }
}
