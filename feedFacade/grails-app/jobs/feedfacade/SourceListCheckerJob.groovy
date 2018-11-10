package feedfacade

class SourceListCheckerJob {
  
  def sourceListService

  static triggers = {
    // Delay 20 seconds, run every 10 mins.
    // Cron:: Min Hour DayOfMonth Month DayOfWeek Year
    // Example - every 10 mins 0 0/10 * * * ? 
    // At 0s, 5m past 5am on Sunday
    cron name:'dailyJob', startDelay:180000, cronExpression: "0 5 2 * * ?"
    // cronExpression: "s m h D M W Y"
    //                  | | | | | | `- Year [optional]
    //                  | | | | | `- Day of Week, 1-7 or SUN-SAT, ?
    //                  | | | | `- Month, 1-12 or JAN-DEC
    //                  | | | `- Day of Month, 1-31, ?
    //                  | | `- Hour, 0-23
    //                  | `- Minute, 0-59
    //                  `- Second, 0-59
  }

  def execute() {
    log.debug("Synchronize with source list");
    sourceListService.setUpSources(grailsApplication.config.fah.sourceList);
  }
}
