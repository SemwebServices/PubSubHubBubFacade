package feedfacade

class FlagDefinition {

  String code
  String name
  String type // CHECK | WARN | ERROR
  String advice 
  Long ttl

  static hasMany = [
  ]

  static mappedBy = [
  ]

  static constraints = {
      code blank: false, nullable:false
      name blank: false, nullable:false
      type blank: false, nullable:false
    advice blank: true, nullable:true
       ttl nullable:false
  }

  static mapping = {
      code column: 'fd_code'
      name column: 'fd_name'
      type column: 'fd_type'
    advice column: 'fd_advice', type:'text'
       ttl column: 'fd_ttl'
  }

}
