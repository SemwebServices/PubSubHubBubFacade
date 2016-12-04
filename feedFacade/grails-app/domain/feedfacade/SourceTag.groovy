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
    owner blank: false, nullable:false
    tag blank: false, nullable:false
    value blank: false, nullable:false
  }

  static mapping = {
  }

}
