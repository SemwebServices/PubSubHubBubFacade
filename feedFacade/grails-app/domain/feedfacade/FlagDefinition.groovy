package feedfacade

class FlagDefinition {

  String code
  String name
  String type // CHECK | WARN | ERROR
  Long ttl

  static hasMany = [
  ]

  static mappedBy = [
  ]

  static constraints = {
    code blank: false, nullable:false
    name blank: false, nullable:false
    type blank: false, nullable:false
     ttl blank: false, nullable:false
  }

  static mapping = {
    code column: 'fd_code'
    name column: 'fd_name'
    type column: 'fd_type'
     ttl column: 'fd_ttl'
  }

}
