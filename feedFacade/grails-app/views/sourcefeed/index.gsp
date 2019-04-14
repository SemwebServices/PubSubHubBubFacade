<!doctype html>
<html>
<head>
    <meta name="layout" content="main"/>
    <title>Registered Source Feeds</title>

    <asset:link rel="icon" href="favicon.ico" type="image/x-ico" />
</head>
<body>
  <div class="container-fluid">
    <div class="row">
      <div class="container" style="vertical-align: middle; text-align:center;">

        <h1>Registered Feeds</h1>

        <g:form controller="sourcefeed" action="index" method="get" class="form">
          <div class="input-group">
              <input type="text" name="q" class="form-control" placeholder="Text input" value="${params.q}">
              <span class="input-group-addon"><input type="checkbox" name="filterHasErrors" value="on" ${params.filterHasErrors=='on'?'checked':''}> Has Errors</span>
              <span class="input-group-addon"><input type="checkbox" name="filterEnabled" value="on" ${params.filterEnabled=='on'?'checked':''}> Enabled</span>
              <span class="input-group-btn"><button type="submit" class="btn btn-primary">Search</button></span>
          </div>
        </g:form>

        <div class="pagination">
          <g:paginate controller="sourcefeed" action="index" total="${totalFeeds}" next="Next" prev="Previous" omitNext="false" omitPrev="false" />
        </div>

      </div>
    </div>
    <div class="row">
      <div class="container-fluid">
      
        <table class="table table-striped well">
          <thead>
            <tr>
              <th>Name</th>
              <th>Enabled</th>
              <th>Feed Status</th>
              <th>Topics</th>
              <th>Tags</th>
              <th>Fetcher Status</th>
              <th>CAP Status</th>
              <th>Base Url</th>
              <th>Last Completed</th>
              <th>Poll Interval</th>
              <th>Next Due</th>
              <th>HTTP Last Modified</th>
              <th>HTTP Expires</th>
              <th>Last Error Message</th>
            </tr>
          </thead>
          <tbody>
            <g:each in="${feeds}" var="f" >
              <tr>
                <td rowspan="2"><g:link controller="sourcefeed" action="feed" id="${f.uriname}">${f.uriname}</g:link></td>
                <td>
                  ${f.enabled?'Yes':'No'}
                  <sec:ifAllGranted roles='ROLE_ADMIN'>
                    <br/><g:link controller="sourcefeed" action="toggleSourceEnabled" id="${f.uriname}">toggle</g:link>
                  </sec:ifAllGranted>
                </td>
                <td>
                  <g:if test="${f.feedStatus=='ERROR'}">
                    <div class="alert alert-danger" role="alert">
                      <span class="glyphicon glyphicon-exclamation-sign" aria-hidden="true"></span>
                      <span class="sr-only">Error</span>
                    </div>
                  </g:if>
                  <g:if test="${f.feedStatus=='OK'}">
                    <div class="alert alert-success" role="alert">
                      <span class="glyphicon glyphicon-ok" aria-hidden="true"></span>
                      <span class="sr-only">OK</span>
                    </div>
                  </g:if>
                  <g:if test="${f.feedStatus!='OK' && f.feedStatus!='ERROR'}">
                    ${f.feedStatus?:'Unset'}
                  </g:if>
                </td>
                <td><ul><g:each in="${f.topics}" var="topic"><li>${topic.topic.name}</li></g:each></ul></td>
                <td><ul><g:each in="${f.tags}" var="tv"><li>${tv.tag.tag}: <strong>${tv.value}</strong></li></g:each></ul></td>
                <td>${f.status}</td>
                <td>${f.capAlertFeedStatus}</td>
                <td><a href="${f.baseUrl}">${f.baseUrl}</a></td>
                <td><g:formatDate date="${new Date(f.lastCompleted)}" format="yyyy MM dd HH:mm:ss.SSS"/> (${f.lastElapsed})</td>
                <td>${f.pollInterval}</td>
                <td><g:formatDate date="${new Date(f.nextPollTime)}" format="yyyy MM dd HH:mm:ss.SSS"/></td>
                <td>${f.httpLastModified}</td>
                <td>${f.httpExpires}</td>
                <td>${f.lastError}</td>
              </tr>
              <tr>
                <td colspan="9">
                  <g:set var="stats" value="${f.getHistogramLastDay()}"/>
                  <table class="table">
                    <thead>
                      <tr>
                        <th class="col-md-1">Hour &nbsp;</th>
                        <g:each in="${stats}" var="h">
                          <th>${h.hour}</th>
                        </g:each>
                      </tr>
                    </thead>
                    <tbody>
                      <tr>
                       <th>Error Count &nbsp;</th>
                        <g:each in="${stats}" var="h">
                          <td>${h.errorCount}</td>
                        </g:each>
                      </tr>
                      <tr>
                       <th>Success Count &nbsp;</th>
                        <g:each in="${stats}" var="h">
                          <td>${h.successCount}</td>
                        </g:each>
                      </tr>
                      <tr>
                       <th>New Entries</th>
                        <g:each in="${stats}" var="h">
                          <td>${h.newEntryCount}</td>
                        </g:each>
                      </tr>
                    </tbody>
                  </table>
                </td>
              </tr>
            </g:each>
          </tbody>
        </table>
    
        <g:link class="btn" action="registerFeed">Register New Feed</g:link>
  
      </div>
    </div>
  </div>
</body>
</html>
