<!doctype html>
<html>
<head>
    <meta name="layout" content="main"/>
    <title>Feed Entries</title>

    <asset:link rel="icon" href="favicon.ico" type="image/x-ico" />
</head>
<body>
  <div class="container-fluid">
    <div class="row">
      <div class="container-fluid">

        <h1>Feed Entries</h1>

        <div class="pagination">
          <g:paginate controller="entry" action="index" total="${entryCount}" next="Next" prev="Previous" omitNext="false" omitPrev="false" />
        </div>

        <table class="table table-striped well">
          <thead>
            <tr>
              <th>id</th>
              <th>Source</th>
            </tr>
          </thead>
          <tbody>
            <g:each in="${entryList}" var="e" >
              <tr>
                <td rowspan="2"><g:link controller="entry" action="detail" id="${e.id}">${e.id}</g:link></td>
                <td rowspan="2"><g:link controller="sourcefeed" action="feed" id="${e.owner.id}">${e.owner.name}</g:link></td>
              </tr>
            </g:each>
          </tbody>
        </table>
    
      </div>
    </div>
  </div>
</body>
</html>
