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
      <div class="container-fluid">

        <h1>Registered Feeds</h1>
        <table class="table table-striped well">
          <thead>
            <tr>
              <th>Name</th>
              <th>Topics</th>
              <th>Tags</th>
              <th>Status</th>
              <th>Base Url</th>
              <th>Last Completed</th>
              <th>processing Start Time</th>
              <th>Poll Interval</th>
              <th>Feed Status</th>
              <th>Last Error Message</th>
            </tr>
          </thead>
          <tbody>
            <g:each in="${feeds}" var="f" >
              <tr>
                <td rowspan="2">${f.uriname}</td>
                <td><ul><g:each in="${f.topics}" var="topic"><li>${topic.topic.name}</li></g:each></ul></td>
                <td><ul><g:each in="${f.tags}" var="tv"><li>${tv.tag.tag}: <strong>${tv.value}</strong></li></g:each></ul></td>
                <td>${f.status}</td>
                <td><a href="${f.baseUrl}">${f.baseUrl}</a></td>
                <td>${f.lastCompleted}</td>
                <td>${f.processingStartTime}</td>
                <td>${f.pollInterval}</td>
                <td>${f.feedStatus}</td>
                <td>${f.lastError}</td>
              </tr>
              <tr>
                <td colspan="9">
                  <g:set var="stats" value="${f.getHistogramLastDay()}"/>
                  <table class="table">
                    <thead>
                      <tr>
                        <th>Hour &nbsp;</th>
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