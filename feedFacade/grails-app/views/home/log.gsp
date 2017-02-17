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
        <th>Message</th>
      </tr>
    </thead>
    <tbody id="FeedFetcherLiveLogTable">
    </tbody>
  </table>

  <asset:script>
    function capEvent(exchange,pattern,evt) {
      console.log("Log message %s %s %o",exchange, pattern, evt);
      var evt_obj = $.parseJSON(evt.body);
      $("#FeedFetcherLiveLogTable").prepend("<tr><td>"+evt_obj.timestamp+"</td><td>"+evt_obj.message+"</td><tr>");
    }
    console.log("Created cap event handler");
  </asset:script>

  <asset:javascript src="application.js"/>
  <asset:javascript src="events.js"/>

</body>
</html>
