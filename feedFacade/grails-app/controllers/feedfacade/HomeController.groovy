package feedfacade

import grails.plugin.springsecurity.annotation.Secured

class HomeController {

  def index() { 
    log.debug("HomeController::index");
    def result = [:]
    result
  }

}
