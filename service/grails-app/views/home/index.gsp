<!doctype html>
<html>
<head>
    <meta name="layout" content="main"/>
    <title></title>

    <asset:link rel="icon" href="favicon.ico" type="image/x-ico" />
</head>
<body>
  <table class="table table-striped">
    <thead>
      <tr>
        <th>Code</th>
        <th>Base URL</th>
        <th>Enabled?</th>
        <th>Last Checked</th>
        <th>Process Status</th>
        <th>Last Fetch</th>
        <th>Register Status</th>
        <th>Health</th>
      </tr>
    </thead>
    <tbody>
      <g:each in="${feeds}" var="feed">
        <tr>
          <td><g:link controller="sourcefeed" action="feed" id="${feed.uriname}">${feed.uriname}</g:link></td>
          <td>${feed.baseUrl}</td>
          <td>${feed.enabled?'Yes':'No'}</td>
          <td>
            <g:if test="${feed.lastCompleted > 0}">
              <g:formatDate date="${new Date(feed.lastCompleted)}" format="yyyy MM dd HH:mm:ss.SSS"/></td>
            </g:if>
            <g:else>
              Never
            </g:else>
          <td>${feed.status}</td>
          <td>${feed.feedStatus}</td>
          <td>${feed.capAlertFeedStatus}</td>
          <td>
            <g:if test="${feed.enabled}">
              <g:if test="${ feed.latestHealth >= 80 }"> <span style="background-color: green; padding:5px;">${feed.latestHealth}%</span></g:if>
              <g:if test="${ feed.latestHealth >= 40 && feed.latestHealth < 80 }"><span style="background-color: amber; padding:5px;">${feed.latestHealth}%</span></g:if>
              <g:if test="${ feed.latestHealth >= 0 && feed.latestHealth < 40 }"><span style="background-color: red; padding:5px;">${feed.latestHealth}%</span></g:if>
            </g:if>
          </td>
        </tr>
      </g:each>
    </tbody>
  </table>
</body>
</html>
