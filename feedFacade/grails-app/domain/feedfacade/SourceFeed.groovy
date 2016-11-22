package feedfacade

class SourceFeed {

  // The "Code" by which this feed will be known - Used for the front end feed url
  String uriname

  // Status - 'in-process', 'paused'  -- in-process if being actively checked
  String status 

  // Base URL of the underlying feed which will be polled
  String baseUrl 

  // The time the feed last changed from 'in-process' to 'paused'
  Long lastCompleted

  // The time the feed went from 'paused' to 'in-process'
  Long processingStartTime 

  // How long to wait between polls
  Long pollInterval

  // Highest timestamp seen on any entry
  Long highestTimestamp

  // Last hash
  String lastHash

  static constraints = {
            lastHash blank: false, nullable:true
    highestTimestamp blank: false, nullable:true
  }
}
