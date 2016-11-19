package feedfacade

class SourceFeed {

  // The "Code" by which this feed will be known - Used for the front end feed url
  String uriname

  // Status - 'in-process', 'paused'  -- in-process if being actively checked
  String status 

  // Base URL of the underlying feed which will be polled
  String baseUrl 

  // The time the feed last changed from 'in-process' to 'paused'
  Date lastCompleted

  // The time the feed went from 'paused' to 'in-process'
  Date processingStartTime 

  // How long to wait between polls
  Long pollInterval

  static constraints = {
  }
}
