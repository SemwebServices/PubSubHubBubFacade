package feedfacade

class HubClientController {

  def index() { 
    log.debug("HubClientController::index ${params}");
    def result = [:]
    render(status: 200, text: 'OK')
  }
}
