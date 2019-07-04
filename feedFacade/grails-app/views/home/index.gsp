<!doctype html>
<html>
<head>
    <meta name="layout" content="main"/>
    <title></title>

    <asset:link rel="icon" href="favicon.ico" type="image/x-ico" />
</head>
<body>
  Public home
  <table class="table table-striped">
    <thead>
      <tr>
        <th>Code</th>
        <th>baseUrl</th>
        <th>last checked</th>
        <th>Process Status</th>
        <th>Feed Status</th>
      </tr>
    </thead>
    <tbody>
      <g:each in="${feeds}" var="feed">
        <tr>
          <td><g:link controller="sourcefeed" action="feed" id="${feed.uriname}">${feed.uriname}</g:link></td>
          <td>${feed.baseUrl}</td>
          <td><g:formatDate date="${new Date(feed.lastCompleted)}" format="yyyy MM dd HH:mm:ss.SSS"/></td>
          <td>${feed.status}</td>
          <td>${feed.feedStatus}</td>
        </tr>
      </g:each>
    </tbody>
  </table>
</body>
</html>
