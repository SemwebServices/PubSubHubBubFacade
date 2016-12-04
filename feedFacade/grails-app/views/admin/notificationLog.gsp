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

        <h1>Notification Log</h1>
        <table class="table table-striped well">
          <thead>
            <tr>
              <th>Callback</th>
              <th>Topic</th>
              <th>Target Mimetype</th>
              <th>Content</th>
            </tr>
          </thead>
          <tbody>
            <g:each in="${log}" var="e" >
              <tr>
                <td>${e.target}</td>
                <td>${e.topic}</td>
                <td>${e.targetMimetype}</td>
                <td>${e.content}</td>
              </tr>
            </g:each>
          </tbody>
        </table>
      </div>
    </div>
  </div>
</body>
</html>
