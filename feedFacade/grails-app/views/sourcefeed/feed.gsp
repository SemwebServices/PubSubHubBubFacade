<!doctype html>
<html>
<head>
    <meta name="layout" content="main"/>
    <title>Source Feed : .....</title>

    <asset:link rel="icon" href="favicon.ico" type="image/x-ico" />
</head>
<body>
  <div class="container-fluid">
    <div class="row">
      <div class="container-fluid">
        <h1>[${feed.id}] <a href="${feed.baseUrl}">${feed.name}</a></h1>

        <div class="panel panel-default">
          <div class="panel-heading">
            <h3 class="panel-title">Feed Info</h3>
          </div>
          <div class="panel-body form-horizontal">
            <div class="form-group">
              <label class="col-sm-2 control-label">URL</label>
              <div class="col-sm-10"><p class="form-control-static">${feed.baseUrl}</p></div>
            </div>
            <div class="form-group">
              <label class="col-sm-2 control-label">Semantic Status</label>
              <div class="col-sm-10"><p class="form-control-static"><strong>${feed.capAlertFeedStatus}</strong> (operating|testing|OTHER) - only operating and testing feeds will be harvested</p></div>
            </div>
            <div class="form-group">
              <label class="col-sm-2 control-label">Harvester Control Status</label>
              <div class="col-sm-10"><p class="form-control-static"><strong>${feed.status}</strong> (in-process|paused)</p></div>
            </div>
            <div class="form-group">
              <label class="col-sm-2 control-label">Last fetch status</label>
              <div class="col-sm-10"><p class="form-control-static"><strong>${feed.feedStatus}</strong>(Last completed=<g:formatDate date="${new Date(feed.lastCompleted)}"/>, highest timestamp=<g:formatDate date="${new Date(feed.highestTimestamp)}"/>)</p></div>
            </div>
            <div class="form-group">
              <label class="col-sm-2 control-label">Last Error (If any)</label>
              <div class="col-sm-10"><p class="form-control-static"><strong>${feed.lastError}</strong></p></div>
            </div>
          </div>
        </div>


        <h2>Recent Entries</h2>
        <table class="table table-striped well">
          <thead>
            <tr>
              <th>Timestamp</th>
              <th>Entry</th>
            </tr>
          </thead>
          <tbody>
            <g:each in="${latestEntries}" var="f" >
              <tr>
                <td><g:formatDate date="${new java.util.Date(f.entryTs)}" format="yyyy-MM-dd'T'HH:mm:ss.SSS" /></td>
                <td>
                  <strong><a href="${f.link}">${f.title}</a></strong>
                  <p>${f.description}</p>
                  <div class="container-fluid">
                    <div class="row">
                      <div class="col-md-6">
                        ${f.entry}
                      </div>
                      <div class="col-md-6">
                        ${f.entryAsJson}
                      </div>
                    </div>
                  </div>
                </td>
              </tr>
            </g:each>
          </tbody>
        </table>
      </div>
    </div>
  </div>
</body>
</html>
