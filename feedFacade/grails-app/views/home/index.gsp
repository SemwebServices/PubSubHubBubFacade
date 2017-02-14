<!doctype html>
<html>
<head>
    <meta name="layout" content="main"/>
    <title></title>

    <asset:link rel="icon" href="favicon.ico" type="image/x-ico" />
</head>
<body>
  Public home

  <h3>Feed Checker Log</h3>
  <table class="table table-striped">
    <thead>
      <tr>
        <th>
        </th>
      </tr>
    </thead>
    <tbody>
      <g:each in="${feedCheckerLog}" var="logEntry">
        <tr>
          <td>${logEntry}</td>
        </tr>
      </g:each>
    </tbody
</body>
</html>
