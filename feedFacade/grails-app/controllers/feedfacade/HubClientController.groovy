package feedfacade

import grails.plugin.springsecurity.annotation.Secured

class HubClientController {

  def index() { 
    log.debug("HubClientController::index ${params}");
    def result = [:]
    if ( params.hub?.challenge ) {
      render(status: 200, text:params.hub.challenge)
    }
    else {
      render(status: 200, text:'OK')
    }
  }
}
