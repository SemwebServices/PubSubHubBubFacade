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

        <h1>Feed Checker Status</h1>
        <table class="table table-striped well">
          <thead>
            <tr>
              <th>blob</th>
            </tr>
          </thead>
          <tbody>
            <g:each in="${activeTasks}" var="e" >
              <tr>
                <td>${e}</td>
              </tr>
            </g:each>
          </tbody>
        </table>
      </div>
    </div>
  </div>
</body>
</html>
