<!doctype html>
<html>
<head>
    <meta name="layout" content="main"/>
    <title>Register Feed</title>

    <asset:link rel="icon" href="favicon.ico" type="image/x-ico" />
</head>
<body>
  <div class="container-fluid">
    <div class="row">

      <div class="col-md-12">
        <h1>Registered New Feed</h1>
      </div>
    </div>
    <div class="row">

        <g:form class="form-horizontal">

          <div class="form-group">
            <label for="feedname" class="col-sm-2 control-label">Feed Name / Identifier</label>
            <div class="col-sm-10">
              <input type="text" name="feedname" class="form-control" id="feedname" placeholder="Feed name/identifier" />
            </div>
          </div>

          <div class="form-group">
            <label for="baseUrl"class="col-sm-2 control-label">Base URL</label>
            <div class="col-sm-10">
              <input type="text" name="baseUrl" class="form-control" id="baseurl" placeholder="URL Of Feed"/>
            </div>
          </div>

          <div class="form-group">
            <label for="pollInterval"class="col-sm-2 control-label">Poll Interval</label>
            <div class="col-sm-10">
              <input type="text" name="pollInterval" class="form-control" id="pollInterval" placeholder="Poll Interval"/>
            </div>
          </div>

          <div class="form-group">
            <div class="col-sm-2">
            </div>
            <div class="col-sm-10">
              <button id="AddFeedButton">Add</button>
            </div>
          </div>

        </g:form>
      </div>
    </div>
  </div>
</body>
</html>
