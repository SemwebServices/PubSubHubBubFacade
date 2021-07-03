package feedfacade

class ActivityIndexDefinition {

  String resourceType
  String indexName
  String indexType // Currently only supported type is "counter" - future types may be "average", "mcq"
  String buckets // Comma separated string minute, hour, day, week, month, year
  String retention // Comma separated list corresponding to buckets indicating how many to keep

  static constraints = {
       resourceType blank: false, nullable:true
          indexName blank: false, nullable:true
          indexType blank: false, nullable:true
            buckets blank: false, nullable:true
          retention blank: false, nullable:true
  }

  static mapping = {
    resourceType index:'aid_resource_type'
       indexName index:'aid_index_name'
       indexType index:'aid_index_type'
         buckets index:'aid_buckets'
       retention index:'aid_retention'
  }


}
