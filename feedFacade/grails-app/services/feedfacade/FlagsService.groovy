package feedfacade

import grails.gorm.transactions.*
import java.security.MessageDigest
import java.util.Calendar
import java.util.TimeZone
import org.hibernate.Session
import org.hibernate.StatelessSession


@Transactional
class FlagsService {

  def sessionFactory

  def raiseFlag(String flag, String resourceType, String id) {

    def result = [:]

    try {

      StatelessSession statelessSession = sessionFactory.openStatelessSession()
      statelessSession.beginTransaction()

      statelessSession.getTransaction().commit()
      statelessSession.close()
    }
    catch ( Exception e ) {
      log.error("problem in raiseFlag",e);
    }

    return result;
  }

}

