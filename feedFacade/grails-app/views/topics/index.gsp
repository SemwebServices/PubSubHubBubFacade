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
        <h2>Add RabbitMQ Queue for this topic</h2>
        <g:form controller="subscription" action="newRabbitQueue">
          <div class="form-group">
            <label for="queueName" class="col-sm-2 control-label">Routing Key</label>
            <div class="col-sm-10">
              <div class="input-group">
                <input name="queueName" class="form-control" id="newpass" placeholder="New Password" />
                <span class="input-group-btn">
                  <button class="btn btn-secondary" type="button">Add Queue</button>
                </span>
              </div>
            </div>
          </div>
        </g:form>
      </div>
    </div>
  </div>
</body>
</html>
