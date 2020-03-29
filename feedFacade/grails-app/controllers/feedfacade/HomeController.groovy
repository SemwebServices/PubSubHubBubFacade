package feedfacade

import grails.plugin.springsecurity.annotation.Secured

class HomeController {

  def springSecurityService

  def index() { 
    log.debug("HomeController::index");
    def user = springSecurityService.currentUser

    def result = [:]

    if ( user ) {
      redirect(controller:'home', action:'home');
    }
    else {
      result.feeds=SourceFeed.executeQuery('select sf from SourceFeed as sf');
    }

    result
  }

  def log() {
    def result = [:]
    result
  }

  @Secured(['ROLE_USER', 'IS_AUTHENTICATED_FULLY'])
  def home() {
    log.debug("HomeController::home");
    def result = [:]
    redirect controller:'sourcefeed', action:'index'
    result
  }



  @Secured(['ROLE_USER', 'IS_AUTHENTICATED_FULLY'])
  def login() {
    log.debug("HomeController::login");
    redirect action:'index'
  }

  @Secured(['ROLE_USER', 'IS_AUTHENTICATED_FULLY'])
  def profile() {
    log.debug("HomeController::profile");
    def result = [:]
    result
  }

  @Secured(['ROLE_USER', 'IS_AUTHENTICATED_FULLY'])
  def changePassword() {

    def user = springSecurityService.currentUser

    log.debug("HomeController::changePassword ${params}");
    if ( ( params.newpass?.length() > 0 ) && ( params.newpass == params.confirm ) ) {
      user.password = params.newpass
      if ( user.save(flush:true, failOnError:true) ) {
        flash.message = "Password Updated!"
      }
    }

    def result = [:]
    redirect action:'profile'
  }

  @Secured(['ROLE_USER', 'IS_AUTHENTICATED_FULLY'])
  def doLogout() {
    log.debug("HomeController::logout");
    request.logout()
    redirect action:'index'
  }

}
