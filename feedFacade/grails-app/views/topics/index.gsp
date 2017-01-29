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
              <th>Subscriptions (Add new rabbit sub)</th>
            </tr>
          </thead>
          <tbody>
            <g:each in="${topics}" var="t" >
              <tr>
                <td>${t.name}</td>
                <td>
                  <ul>
                    <g:each in="${t.subscriptions}" var="sub">
                      <li>${sub.callback}</li>
                    </g:each>
                  </ul>
                  <g:form controller="subscription" action="newRabbitQueue">
                    <input type="hidden" name="topicName" value="${t.name}"/>
                    <div class="form-group col-sm-12 nopadding">
                      <label for="queueName" class="col-sm-4 control-label">Add new Rabbit Sub for ${t.name} with Routing Key:</label>
                      <div class="col-sm-8">
                        <div class="input-group">
                          <input name="queueName" class="form-control" id="queueName" placeholder="New Password" />
                          <span class="input-group-btn">
                            <button class="btn btn-secondary" type="submit">Add New Queue Subscription</button>
                          </span>
                        </div>
                      </div>
                    </div>
                  </g:form>
                </td>
              </tr>
            </g:each>
          </tbody>
        </table>


      </div>
    </div>
  </div>
</body>
</html>
