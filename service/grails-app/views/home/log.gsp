<!doctype html>
<html>
<head>
    <meta name="layout" content="main"/>
    <title></title>

    <asset:link rel="icon" href="favicon.ico" type="image/x-ico" />
</head>
<body>
  <h3>Feed Checker Live Log</h3>
  <table id="feed-watcher" data-exchange="FeedFetcher" data-feedid="#" class="table table-striped">
    <thead>
      <tr>
        <th>Timestamp</th>
        <th>Linked Item</th>
        <th>Message</th>
      </tr>
    </thead>
    <tbody id="FeedFetcherLiveLogTable">
    </tbody>
  </table>

  <asset:script>
    function capEvent(exchange,pattern,evt) {
      // if we have more than 100 rows in the table, trim the last one off
      if ( $('#FeedFetcherLiveLogTable tr').length > 100) {
        $('#FeedFetcherLiveLogTable tr:last').remove();
      }
      var evt_obj = $.parseJSON(evt.body);
      var obj_link = ''
      if ( evt_obj.relatedType==='feed' ) {
        obj_link='<a href="/sourcefeed/feed/'+evt_obj.relatedId+'">'+evt_obj.relatedId+'</a>';
      }
     
      $("#FeedFetcherLiveLogTable").prepend("<tr><td>"+evt_obj.timestamp+"</td><td>"+obj_link+"</td><td>"+evt_obj.message+"</td><tr>");
    }
  </asset:script>

  <asset:javascript src="application.js"/>
  <asset:javascript src="events.js"/>

</body>
</html>
