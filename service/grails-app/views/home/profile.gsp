<!doctype html>
<html>
<head>
    <meta name="layout" content="main"/>
    <title>Profile</title>

    <asset:link rel="icon" href="favicon.ico" type="image/x-ico" />
</head>
<body>

  <div class="container-fluid">

  <g:if test="${flash.message}">
    <div class="row">
      <div class="col-md-12">
        <div class="alert alert-info" role="alert">
          ${flash.message}
        </div>
      </div>
    </div>
  </g:if>


    <div class="row">
      <div class="col-md-12">

        <div class="panel panel-default">
          <div class="panel-heading">
            <h3 class="panel-title">Change Password</h3>
          </div>
          <div class="panel-body">
            <g:form class="form-horizontal" action="changePassword" method="post">

              <div class="form-group">
                <label for="newpass" class="col-sm-2 control-label">New Password</label>
                <div class="col-sm-10">
                  <input type="password" name="newpass" class="form-control" id="newpass" placeholder="New Password" />
                </div>
              </div>

              <div class="form-group">
                <label for="confirm" class="col-sm-2 control-label">Confirm New Password</label>
                <div class="col-sm-10">
                  <input type="password" name="confirm" class="form-control" id="confirm" placeholder="Confirm New Password" />
                </div>
              </div>

              <div class="form-group">
                <div class="col-sm-2">
                </div>
                <div class="col-sm-10">
                  <button class="btn btn-warning" type="submit">Change Password</button>
                </div>
              </div>


            </g:form>
          </div>
        </div>

      </div>
    </div>
  </div>

</body>
</html>
