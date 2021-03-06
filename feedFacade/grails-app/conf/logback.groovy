import grails.util.BuildSettings
import grails.util.Environment
import org.springframework.boot.logging.logback.ColorConverter
import org.springframework.boot.logging.logback.WhitespaceThrowableProxyConverter

import java.nio.charset.StandardCharsets

conversionRule 'clr', ColorConverter
conversionRule 'wex', WhitespaceThrowableProxyConverter

// See http://logback.qos.ch/manual/groovy.html for details on configuration
appender('STDOUT', ConsoleAppender) {
    encoder(PatternLayoutEncoder) {
        charset = StandardCharsets.UTF_8

        pattern =
                '%clr(%d{yyyy-MM-dd HH:mm:ss.SSS}){faint} ' + // Date
                        '%clr(%5p) ' + // Log level
                        '%clr(---){faint} %clr([%15.15t]){faint} ' + // Thread
                        '%clr(%-40.40logger{39}){cyan} %clr(:){faint} ' + // Logger
                        '%m%n%wex' // Message
    }
}

root(WARN, ['STDOUT'])
if (Environment.isDevelopmentMode() ) {
  logger ('feedfacade', DEBUG)
}

def targetDir = BuildSettings.TARGET_DIR
if ( (Environment.isDevelopmentMode() && targetDir != null) ||
     (Environment.getCurrent() == Environment.TEST ) ) {
    appender("FULL_STACKTRACE", FileAppender) {
        file = "${targetDir}/stacktrace.log"
        append = true
        encoder(PatternLayoutEncoder) {
            pattern = "%level %logger - %msg%n"
        }
    }
    logger("StackTrace", ERROR, ['FULL_STACKTRACE'], false)
    logger ('org.hibernate.orm.deprecation', ERROR)
    root(WARN, ['STDOUT', 'FULL_STACKTRACE'])
}
else {
    logger ('grails.app.init', INFO)
    logger ('grails.app.domains', WARN)
    logger ('grails.app.jobs', WARN)
    logger ('grails.app.services', WARN)
    logger ('grails.app.controllers', WARN)
    logger ('org.hibernate.orm.deprecation', ERROR)
    logger ('feedfacade', INFO)
    root(WARN, ['STDOUT'])
}

