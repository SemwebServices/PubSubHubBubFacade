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
              <g:if test="${ feed.latestHealth >= 80 }"> <asset:image src="sunny.svg" width="32" alt="${feed.latestHealth}"/></g:if>
              <g:if test="${ feed.latestHealth >= 60 && feed.latestHealth < 80 }"><asset:image src="partially-sunny.svg" width="32" alt="${feed.latestHealth}"/></g:if>
              <g:if test="${ feed.latestHealth >= 40 && feed.latestHealth < 60 }"><asset:image src="cloudy.svg" width="32" alt="${feed.latestHealth}"/></g:if>
              <g:if test="${ feed.latestHealth >= 20 && feed.latestHealth < 40 }"><asset:image src="raining.svg" width="32" alt="${feed.latestHealth}"/></g:if>
              <g:if test="${ feed.latestHealth >= 0 && feed.latestHealth < 20 }"><asset:image src="storm.svg" width="32" alt="${feed.latestHealth}"/></g:if>
            </g:if>
          </td>
        </tr>
      </g:each>
    </tbody>
  </table>
</body>
</html>
