package feedfacade

import grails.plugin.springsecurity.annotation.Secured

class HomeController {


  def index() { 
    log.debug("HomeController::index");
    def result = [:]
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

}
