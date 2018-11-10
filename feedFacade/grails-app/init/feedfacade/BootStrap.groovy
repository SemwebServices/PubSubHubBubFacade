package feedfacade

class BootStrap {

  def sourceListService
  def grailsApplication

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
    setUpUserAccounts()
    sourceListService.setUpSources(grailsApplication.config.fah.sourceList);
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
