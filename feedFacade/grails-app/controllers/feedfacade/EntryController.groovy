package feedfacade

import grails.plugin.springsecurity.annotation.Secured


class EntryController {

  @Secured(['ROLE_USER', 'IS_AUTHENTICATED_FULLY'])
  def index() { 
    log.debug("EntryController::index");
    def result = [:]

    def query_control = [ max : params.max ?: 10, offset: params.offset ?: 0 ]

    result.entryCount = Entry.executeQuery('select count(e.id) from Entry as e')[0];
    result.entryList = Entry.executeQuery('select e from Entry as e order by e.entryTs desc',[:],query_control);

    result
  }

}
