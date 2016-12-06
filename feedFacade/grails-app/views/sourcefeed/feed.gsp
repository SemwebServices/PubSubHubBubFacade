<!doctype html>
<html>
<head>
    <meta name="layout" content="main"/>
    <title>Source Feed : .....</title>

    <asset:link rel="icon" href="favicon.ico" type="image/x-ico" />
</head>
<body>
  ${feed}
  ${latestEntries?.size()}
  <div class="container-fluid">
    <div class="row">
      <div class="container-fluid">
        <h1>Recent Feed Entries</h1>
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
                <td>${f.entryTs}</td>
                <td>${f.entry}</td>
              </tr>
            </g:each>
          </tbody>
        </table>
      </div>
    </div>
  </div>
</body>
</html>
