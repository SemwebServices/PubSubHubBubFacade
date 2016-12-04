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
  
      <h1>Registered Feeds</h1>
      <table class="table table-striped well">
        <thead>
          <tr>
            <th>uriname</th>
            <th>Topics</th>
            <th>status</th>
            <th>baseurl</th>
            <th>lastCompleted</th>
            <th>processing Start Time</th>
            <th>pollInterval</th>
          </tr>
        </thead>
        <tbody>
          <g:each in="${feeds}" var="f" >
            <tr>
              <td>${f.uriname}</td>
              <td><ul><g:each in="${f.topics}" var="topic"><li>${topic.topic.name}</li></g:each></ul></td>
              <td>${f.status}</td>
              <td>${f.baseUrl}</td>
              <td>${f.lastCompleted}</td>
              <td>${f.processingStartTime}</td>
              <td>${f.pollInterval}</td>
            </tr>
          </g:each>
        </tbody>
      </table>
  
      <g:link class="btn" action="registerFeed">Register New Feed</g:link>

    </div>
  </div>
</body>
</html>
