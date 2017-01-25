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
        <h1>Registered Topics</h1>
        <table class="table table-striped well">
          <thead>
            <tr>
              <th>Topic name</th>
              <th>Subscriptions</th>
            </tr>
          </thead>
          <tbody>
            <g:each in="${topics}" var="t" >
              <tr>
                <td>${t.name}</td>
                <td><ul><g:each in="${t.subscriptions}" var="sub"><li>${sub.callback}</li></g:each></ul></td>
              </tr>
            </g:each>
          </tbody>
        </table>
      </div>
    </div>
  </div>
</body>
</html>
