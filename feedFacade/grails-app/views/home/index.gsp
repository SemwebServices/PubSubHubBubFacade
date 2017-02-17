<!doctype html>
<html>
<head>
    <meta name="layout" content="main"/>
    <title></title>

    <asset:link rel="icon" href="favicon.ico" type="image/x-ico" />
</head>
<body>
  Public home
  <table>
    <thead>
      <tr>
        <th>Code</th>
        <th>baseUrl</th>
      </tr>
    </thead>
    <tbody>
      <g:each in="${feeds}" var="feed">
        <tr>
          <td>${feed.uriname}</td>
          <td>${feed.baseUrl}</td>
        </tr>
      </g:each>
    </tbody>
  </table>
</body>
</html>
