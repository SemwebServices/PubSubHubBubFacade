<!doctype html>
<html>
<head>
    <meta name="layout" content="main"/>
    <title>Most Recent Feed Entries</title>

    <asset:link rel="icon" href="favicon.ico" type="image/x-ico" />
</head>
<body>
  <div class="container-fluid">
    <div class="row">
      <div class="container-fluid">

        <h1>Most Recent Feed Entries</h1>

        <div class="pagination">
          <g:paginate controller="entry" action="index" total="${entryCount}" next="Next" prev="Previous" omitNext="false" omitPrev="false" />
        </div>

        <table class="table table-striped well">
          <thead>
            <tr>
              <th class="col-md-1">Entry Id</th>
              <th class="col-md-1">Entry Timestamp</th>
              <th class="col-md-4">From Feed</th>
              <th class="col-md-1">Sub Matches</th>
              <th class="col-md-5">Title/Description</th>
            </tr>
          </thead>
          <tbody>
            <g:each in="${entryList}" var="e" >
              <tr>
                <td><g:link controller="entry" action="detail" id="${e.id}">${e.id}</g:link></td>
                <td><g:formatDate date="${new Date(e.entryTs)}"/></td>
                <td><g:link controller="sourcefeed" action="feed" id="${e.ownerFeed.uriname}">${e.ownerFeed.uriname} (${e.ownerFeed.id})</g:link><br/>
                  <g:each in="${e.ownerFeed.topics}" var="feedtopic"> 
                    <span class="badge">${feedtopic.topic.name}</span>
                  </g:each><br/>
                  <span class="pull-left"><em>Feed</em> Last checked <g:formatDate date="${new Date(e.ownerFeed.lastCompleted)}"/></span>
                </td>
                <td>${e.numSubscriptionEntries}</td>
                <td><a href="${e.link}">${e.title}</a><br/>${e.description}</td>
              </tr>
            </g:each>
          </tbody>
        </table>
    
      </div>
    </div>
  </div>
</body>
</html>
