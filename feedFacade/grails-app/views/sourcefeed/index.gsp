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
            <th>status</th>
            <th>baseurl</th>
            <th>lastCompleted</th>
            <th>processingTimeStart</th>
            <th>pollInterval</th>
          </tr>
        </thead>
        <tbody>
          <g:each in="${feeds}" var="f" >
            <tr>
              <td></td>
            </tr>
          </g:each>
        </tbody>
      </table>
  
      <g:link class="btn" action="registerFeed">Register New Feed</g:link>

    </div>
  </div>
</body>
</html>
