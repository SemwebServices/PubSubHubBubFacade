package feedfacade

import grails.util.Environment
import grails.util.Metadata 


class BootStrap {

  def sourceListService
  def grailsApplication

  def init = { servletContext ->

    log.info("FeedFacade Starting.....");
    log.info("  -> rabbitmq.connections.host: ${grailsApplication.config.rabbitmq.connections.host}");
    log.info("  -> rabbitmq.connections.username: ${grailsApplication.config.rabbitmq.connections[0].username}");
    log.info("  -> datasource.url : ${grailsApplication.config.dataSource.url}");
    log.info("  -> datasource.username : ${grailsApplication.config.dataSource.username}");
    log.info("  -> datasource.dialect : ${grailsApplication.config.dataSource.dialect}");
    log.info("  -> datasource.driverClassName : ${grailsApplication.config.dataSource.driverClassName}");
    log.info("  -> grails.serverUrl : ${grailsApplication.config.grails?.serverUrl}");
    log.info("  -> Build Time : ${Metadata.getCurrent().get('build.time')}");
    log.info("  -> Build Host : ${Metadata.getCurrent().get('build.host')}");
    log.info("  -> Build Number : ${Metadata.getCurrent().get('build.number')}");
    log.info("  -> Git Branch : ${Metadata.getCurrent().get('build.git.branch')}");
    log.info("  -> Git Commit : ${Metadata.getCurrent().get('build.git.commit')}");

    Runtime.getRuntime().addShutdownHook(new Thread() {
      @Override
      public void run() {
        log.info("feedFacade shutdown hook - process terminating");
      }   
    }); 


    User.withTransaction { status ->
      // Only set up default accounts in the test and development environments
      if ( ( Environment.currentEnvironment.name == Environment.DEVELOPMENT ) ||
           ( Environment.currentEnvironment.name == Environment.TEST ) ) {
        setUpUserAccounts()
      }

      // load local overrrides first
      if ( grailsApplication.config.fah.localFeedSettings != null ) {

        // Local Feed Settings points to a config file of the form
        // [ {
        //     "uriname":"feedcode",
        //     "alternateFeedURL":"url",
        //     "authenticationMethod":"pin",
        //     "credentials":"creds"
        // } ]
        // And can be used to annotate information coming from the global alert hub registry with local credentials

        File local_feed_settings_file = new File(grailsApplication.config.fah.localFeedSettings)
        if ( local_feed_settings_file.canRead() ) {
          log.debug("Attempting to read local feed settings from ${grailsApplication.config.fah.localFeedSettings}");
          sourceListService.loadLocalFeedSettings("file://${local_feed_settings_file}");
        }
        else {
          log.warn("Unable to locate local feed settings file: ${grailsApplication.config.fah.localFeedSettings}");
        }
      }

      log.debug("Call sourceListService.setupSources");
      sourceListService.setUpSources(grailsApplication.config.fah.sourceList);
    }
  }

  def setUpUserAccounts() {
    grailsApplication.config.sysusers.each { su ->
      log.debug("user name:${su.name} ${su.pass} display-as:${su.display} roles:${su.roles}");
      def user = User.findByUsername(su.name)
      if ( user ) {
        if ( ( user.password == null ) && ( su.pass == null ) ) {
          
          log.debug("Hard change of user password from config ${user.password} -> ${su.pass}");
          user.password = su.pass;
          user.save(failOnError: true)
        }
        else {
          log.debug("${su.name} present and correct");
        }
      }
      else {
        String password = null;
        if ( su.pass == null ) {
          password = grailsApplication.config.defaultAdmPassword ?: java.util.UUID.randomUUID().toString()
          log.info("Generated password for ${su.name} -> ${password}");
          println("\n\n*** ${password} ***\n\n");
        }
        else {
          password = su.pass;
        }

        log.debug("Create user...${su.name} ");

        user = new User(
                      username: su.name,
                      password: password,
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
