<!doctype html>
<html>
<head>
    <meta name="layout" content="main"/>
    <title>Topics</title>
    <asset:link rel="icon" href="favicon.ico" type="image/x-ico" />
</head>
<body>
  <div class="container-fluid">
    <div class="row">
      <div class="container-fluid">
        <h1>Current Subscriptions</h1>
        <table class="table table-striped well">
          <thead>
            <tr>
              <th>ID</th>
              <th>Type</th>
              <th>Callback/Queue</th>
            </tr>
          </thead>
          <tbody>
            <g:each in="${subscriptions}" var="s" >
              <tr>
                <td>${s.guid}</td>
                <td>${s.subType}</td>
                <td>${s.callback}</td>
              </tr>
            </g:each>
          </tbody>
        </table>
      </div>
    </div>
  </div>
</body>
</html>
