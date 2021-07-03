package feedfacade

import grails.gorm.transactions.*
import java.security.MessageDigest
import java.util.Calendar
import java.util.TimeZone
import org.hibernate.Session
import org.hibernate.StatelessSession
import feedfacade.FlagDefinition
import feedfacade.FlagEvent


@Transactional
class FlagsService {

  def sessionFactory


  Map raiseFlag(String flag, String resourceType, String id) {
  
    log.debug("FlagsService::raiseFlag(${flag}, ${resourceType}, ${id})");

    def result = [:]

    if ( ( id != null ) &&
         ( flag != null ) &&
         ( resourceType != null ) ) {
      try {
        long event_time = System.currentTimeMillis();
  
        StatelessSession statelessSession = sessionFactory.openStatelessSession()
        statelessSession.beginTransaction()

        FlagDefinition fd = FlagDefinition.findByCode(flag)
        if ( fd != null ) {
          List<FlagEvent> fe_list = FlagEvent.executeQuery('select fe from FlagEvent as fe where fe.definition=:defn and fe.resourceType=:rt and fe.resourceId=:ri',
                                                           [defn: fd, rt:resourceType, ri:id])
          log.debug("Found ${fe_list.size()} events");
          switch ( fe_list.size() ) {
            case 0:
              log.debug("Create new event");
              def new_fe = new FlagEvent(definition: fd,
                                         resourceType: resourceType,
                                         resourceId: id,
                                         firstSeen:event_time,
                                         lastSeen:event_time,
                                         expiryTime: event_time+fd.ttl,
                                         notes: null)
              new_fe.save(flush:true, failOnError:true);
              break;
            case 1:
              def existing_fe = fe_list.get(0);
              log.debug("update existing event current expiry time: ${existing_fe.expiryTime}, new will be ${event_time+fd.ttl} current time ${System.currentTimeMillis()}");
              existing_fe.lastSeen = event_time
              existing_fe.expiryTime = event_time+fd.ttl
              existing_fe.save(flush:true, failOnError:true);
              break;
            default:
              log.error("Multiple flag events recorded. REPORT TO SUPPORT");
              break;
          }
          statelessSession.getTransaction().commit()
        }
        else {
          log.error("Unable to locate definition for code ${flag}");
        }

        statelessSession.close()
      }
      catch ( Exception e ) {
        log.error("problem in raiseFlag",e);
      }
    }
    else {
      log.error("Missing data in call to raiseFlag");
    }

    return result;
  }

}

