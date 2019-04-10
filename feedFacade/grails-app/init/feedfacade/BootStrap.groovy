package feedfacade

class BootStrap {

  def sourceListService
  def grailsApplication

  def init = { servletContext ->

    // Only set up default accounts in the test and development environments
    if ( ( Environment.currentEnvironment.name == Environment.DEVELOPMENT ) ||
         ( Environment.currentEnvironment.name == Environment.TEST ) ) {
      setUpUserAccounts()
    }

    sourceListService.setUpSources(grailsApplication.config.fah.sourceList);
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
