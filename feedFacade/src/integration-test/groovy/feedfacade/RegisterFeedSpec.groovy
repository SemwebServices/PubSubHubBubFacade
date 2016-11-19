package feedfacade

import grails.test.mixin.integration.Integration
import grails.transaction.*

import spock.lang.*
import geb.spock.*

/**
 * See http://www.gebish.org/manual/current/ for more instructions
 */
@Integration
@Rollback
class RegisterFeedSpec extends GebSpec {

    def setup() {
    }

    def cleanup() {
    }

    void "Register Test Feed"() {
        when:"Navigate to the new feed page and fill out form"
            go '/sourcefeed/registerFeed'
            $("form").feedname = 'localTest'
            $("form").baseUrl = 'file:testfeed.xml'
            $("form").submit
            $('#AddFeedButton').click()


        then:"The title is correct"
        	title == "Welcome to Grails"
    }
}
