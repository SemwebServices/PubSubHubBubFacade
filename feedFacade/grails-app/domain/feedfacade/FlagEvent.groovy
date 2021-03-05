package feedfacade


/**
 * FlagEvent - appearence of a Flag against an arbitrary instance of a domain class.
 * Flags can be used to indicate positive or negative assertions about any object in any domain class.
 * For example, a sourceFeed may be flagged as having an invalid or unparsable start date. This would be a 
 * warning flag (specified by the flag definition). firstSeen will indicate the timestamp (UTC) the event was
 * first seen, and the first time the event is recorded, firstSeen == lastSeen. If the same event is raised
 * subsequently, lastSeen is incremented, firstSeen is left untouched. Each time lastSeen is incremented
 * expiryTime is updated to lastSeen+definition.ttl. A system agent will clean up (Delete) expired flags.
 * users may manually clear flags if they believe they have rectified the underlying problem.
 */
class FlagEvent {

  FlagDefinition definition
  Long firstSeen
  Long lastSeen
  Long expiryTime
  String resourceType
  String resourceId
  String notes

  static hasMany = [
  ]

  static mappedBy = [
  ]

  static constraints = {
       definition blank: false, nullable:false
        firstSeen blank: false, nullable:false
         lastSeen blank: false, nullable:false
       expiryTime blank: false, nullable:false
     resourceType blank: false, nullable:false
       resourceId blank: false, nullable:false
            notes blank: false, nullable:true
  }

  static mapping = {
       definition column: 'fe_defn_fk'
        firstSeen column: 'fe_first_seeen'
         lastSeen column: 'fe_last_seen'
       expiryTime column: 'fe_expiry_time'
     resourceType column: 'fe_resource_type'
       resourceId column: 'fe_resource_id'
            notes column: 'fe_notes'
  }

}
