package feedfacade

import grails.plugin.springsecurity.annotation.Secured
import grails.converters.*



class SourcefeedController {

  def index() { 
    long start_time = System.currentTimeMillis();

    def pagination_props = [ max : params.max ?: 10, offset: params.offset ?: 0 ]

    log.debug("SourcefeedController::index");
    def result = [:]

    def qry_params = [:]
    def base_feed_qry = ' from SourceFeed as sf'
    int clause_count = 0;
    // def order_by_clause = ' order by (sf.lastCompleted + sf.pollInterval)'
    if ( ( params.q != null ) && ( params.q.length() > 0 ) ) {
      base_feed_qry += ' where ( lower(sf.uriname) like :a or lower(sf.name) like :a or lower(baseUrl) like :a ) '
      qry_params.a = "%${params.q.toLowerCase()}%".toString()
      clause_count++;
    }
    def order_by_clause = ' order by sf.id'

    if ( params.filterHasErrors=='on' ) {

      if ( clause_count == 0 )
        base_feed_qry += ' where '
      else 
        base_feed_qry += ' and '

      base_feed_qry += 'exists ( select sfs.owner from SourceFeedStats as sfs where sfs.owner = sf and sfs.lastUpdate > :start_time group by sfs.owner having sum(sfs.errorCount) > 0 )'
      qry_params.start_time = System.currentTimeMillis() - ( 25 * 60 * 60 * 1000 )
      clause_count++;
    }

    if ( params.filterEnabled=='on' ) {
      if ( clause_count == 0 ) base_feed_qry += ' where ' else base_feed_qry += ' and '

      base_feed_qry += 'sf.enabled = :true'
      qry_params.true = true
      clause_count++;
    }

    if ( params.filterInProcess == 'on' ) {
      if ( clause_count == 0 ) base_feed_qry += ' where ' else base_feed_qry += ' and '
      base_feed_qry += 'sf.status = :inprocess'
      qry_params.inprocess = 'in-process'
      clause_count++;
    }

    log.debug("About to count blocked feeds - elasped - ${System.currentTimeMillis() - start_time}");
    // Count how many feeds might be blocked
    result.blocked_feeds = SourceFeed.executeQuery('select count(sf) from SourceFeed as sf where sf.status=:inProcess and sf.lastStarted < :blocktime', 
                                                   [blocktime:System.currentTimeMillis()-60000, inProcess:'in-process'])[0];


    log.debug("About to count feeds - elasped - ${System.currentTimeMillis() - start_time}");
    result.totalFeeds = SourceFeed.executeQuery('select count(sf) '+base_feed_qry,qry_params)[0]

    log.debug("About to find feeds - elasped - ${System.currentTimeMillis() - start_time}");
    result.feeds = SourceFeed.executeQuery('select sf '+base_feed_qry+order_by_clause,qry_params,pagination_props)

    log.debug("found ${result.totalFeeds} feeds - elasped - ${System.currentTimeMillis() - start_time}");

    result
  }

  @Secured(['ROLE_USER', 'IS_AUTHENTICATED_FULLY'])
  def registerFeed() {
    log.debug("SourcefeedController::registerFeed");
    def result = [:]

    try {

      log.debug("Checking ${request.method}");

      if ( request.method=='POST' ) {

        log.debug("Checking post contains a uriname: ${params}");

        if ( ( params.feedname ) && ( params.feedname.trim().length() > 0 ) ) {

          SourceFeed.withTransaction {

            log.debug("Find by Uriname ${params.feedname}");

            def feed = SourceFeed.findByUriname(params.feedname)

            if ( feed == null ) {
              log.debug("Create new feed ${params}");
              feed = new SourceFeed(uriname:params.feedname, baseUrl:params.baseUrl, pollInterval:params.pollInterval, status:'paused', processingStartTime:0, lastCompleted:0).save(flush:true, failOnError:true);
              feed.addTopics(params.topics)
            }
            else {
              log.debug("Update feed ${params}");
              feed.pollInterval = params.pollInterval
              feed.baseUrl=params.baseUrl
              feed.save(flush:true, failOnError:true)
            }
          }
        }
      }
    }
    catch ( Exception e ) {
      log.error("Problem creating feed",e);
    }

    result
  }

  def feed() {
    log.debug("SourcefeedController::feed ${params.id}");
    def result = [:]
    result.feed = SourceFeed.findByUriname(params.id)

    def entries_base_qry = 'from Entry as e where e.ownerFeed.uriname = :owner'

    result.totalEntries = Entry.executeQuery('select count(e) '+entries_base_qry,[owner:params.id])[0]
    result.latestEntries = Entry.executeQuery('select e '+entries_base_qry+' order by entryTs desc',[owner:params.id],[max:50])
    result
  }

  @Secured(['ROLE_ADMIN', 'IS_AUTHENTICATED_FULLY'])
  def toggleSourceEnabled() {
    def result = [:]
    log.debug("SourcefeedController::feed ${params.id}");
    SourceFeed.withTransaction {
      def feed = SourceFeed.findByUriname(params.id)
      feed.enabled = !feed.enabled
      feed.save(flush:true, failOnError:true);
    }
    request.getHeader('referer')
    def referer = request.getHeader('referer')
  
    redirect(url: referer)
  }

  @Secured(['ROLE_ADMIN', 'IS_AUTHENTICATED_FULLY'])
  def releaseBlockedFeeds() {
    try {
      SourceFeed.withTransaction {
        SourceFeed.executeUpdate('update SourceFeed sf set sf.status=:paused where sf.status=:inProcess and sf.lastStarted < :oneMinuteAgo',
                                 [inProcess:'in-process', paused:'paused', oneMinuteAgo:System.currentTimeMillis()-60000])
      }
    }
    catch ( Exception e ) {
      log.warn("problem trying to release blocked feeds",e);
    }
    def referer = request.getHeader('referer')
    redirect(url: referer)

  }
  
}
