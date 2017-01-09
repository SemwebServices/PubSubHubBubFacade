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
