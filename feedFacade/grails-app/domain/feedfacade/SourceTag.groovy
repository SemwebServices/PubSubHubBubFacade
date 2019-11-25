package feedfacade

class SourceTag {

  SourceFeed owner
  Tag tag
  String value

  static hasMany = [
  ]

  static mappedBy = [
  ]

  static constraints = {
    owner nullable:false
    tag nullable:false
    value blank: false, nullable:false
  }

  static mapping = {
  }

}
