# PubSubHubBubFacade

An asychronous feed fetcher which emits events when it detects new entries in RSS/ATOM feeds with the intent of turning
statically published feeds into an event stream for downstream services to consume in real time.

This work has been kindly supported by UCAR Subaward SUBAWD001770

## Background

In the development of the CAP Filtered Alert Hub one key issue kept coming up:

Events were being published using ATOM but we had to poll those feeds. We really wanted to exploit the AWS
lambda event functions, but the critical bit of the jigsaw is turning pull based RSS/ATOM into push based
events. We ended up with a lot of CAP-specific code to do that pushing, but the truth is that the problem
of presenting a pull-only RSS/ATOM feed as an event stream is not a CAP problem, but a more generic poll/push
conversion. So long as the <entry> elements in the <feed> elements were preserved, it doesn't matter what
additional profiled XML is passed (So long as namespaces are preserved).

This project, then, is an attempt to abstract the problem of turning RSS/ATOM feeds that are created by 
systems which don't want to implement Pubsubhubbub themselves into pubsubhubub capable services.

The vision is that services will install this facade or adapter on their own services to turn their static
RSS into event streams.

This project is a sister project to https://github.com/ianibo/CAPCollator - where this project contains the
generic atom to push notification element, that project contains all the CAP specific indexing and alerting 
functions. If you're here looking for information about the Common Alerting Protocol (CAP) see
this link https://docs.oasis-open.org/emergency/cap/v1.2/CAP-v1.2-os.html and that project.

### Features

* As of the version 2017-02-14 the server is much more observant of http-last-modified and uses the http if-modified-since header . This should greatly reduce polling traffic for servers that properly observe the general HTTP caching mechanics. The database also now tracks the expires header, although currently does not observe it.

## Technology

Currently implemented in Java using the groovy-on-grails framework, but runnable as a microservice using the
production jar. An embedded database is used for feed state.

## Deployment

There are many deployment options, the initial goal was for a local facade, but projects may also use the
service internally to poll RSS and turn feeds into event streams.

## Development

Users will normally work with three git projects side by side
  * git@github.com:SemwebServices/SWCapAlertHubDevops.git
    The devops project that provides base infrastructure. Check out this project and run
      cd vagrant
      docker-compose -f ./docker-compose-dev-setup.yml up
    to provision the base postgres, elasticsearch and rabbitMQ with appropriate config
  * This project
      cd feedFacade
      grails run-app 
    to provide a running feed facade you can edit. 
    Visit http://localhost:8081/feedFacade/setup to complete setup.
  * CapAggregator
    

### GeneralUser

    CREATE USER feedfacade WITH PASSWORD 'feedFacade';

### Dev Env

    DROP DATABASE feedfacadedev;
    CREATE DATABASE feedfacadedev;
    GRANT ALL PRIVILEGES ON DATABASE feedfacadedev to feedfacade;

If you're hoping to develop CAPCollator alongside this component, it can be helpful to run this element on a different port with

    grails run-app --port 8085


### Production Deployment Postgresql Backed

    DROP DATABASE feedfacade;
    CREATE DATABASE feedfacade;
    GRANT ALL PRIVILEGES ON DATABASE feedfacade to feedfacade;


## References

http://pubsubhubbub.github.io/PubSubHubbub/pubsubhubbub-core-0.4.html
https://github.com/pubsubhubbub/PubSubHubbub
https://en.wikibooks.org/wiki/WebObjects/Web_Services/How_to_Trust_Any_SSL_Certificate

### Test Subscription Data

https://s3-eu-west-1.amazonaws.com/alert-hub-subscriptions/json



# RabbitMQ Setup

Change according to local requirements


    apt-get install rabbitmq-server
    rabbitmqctl add_user cap cap
    rabbitmq-plugins enable rabbitmq_management
    rabbitmq-plugins enable rabbitmq_web_stomp

N.B. Sometimes, after enabling this step, we were not able to access the management server on 15672. service restart didn't fix this,
but a reboot of the machine is reported as fixinig this in some scenarios. Some helpful notes here:: https://stackoverflow.com/questions/23669780/rabbitmq-3-3-1-can-not-login-with-guest-guest. Turns out that
this is problematic if you are using a proxy server. One workaround is to unset http_proxy before issuing the wget command.


    wget http://127.0.0.1:15672/cli/rabbitmqadmin
    chmod u+rx ./rabbitmqadmin
    ./rabbitmqadmin declare exchange name=FeedFetcher type=topic
    ./rabbitmqadmin declare exchange name=CAPExchange type=topic
    ./rabbitmqadmin declare queue name=CAPCollatorATOMQueue durable=true
    ./rabbitmqadmin declare queue name=CAPCollatorRSSQueue durable=true
    ./rabbitmqadmin declare binding source="CAPExchange" destination_type="queue" destination="CAPCollatorATOMQueue" routing_key="ATOMEntry.#"
    ./rabbitmqadmin declare binding source="CAPExchange" destination_type="queue" destination="CAPCollatorRSSQueue" routing_key="RSSEntry.#"
    rabbitmqctl set_permissions cap "stomp-subscription-.*" "stomp-subscription-.*" "(FeedFetcher|CAPExchange|stomp-subscription-.*)"
    rabbitmqctl list_exchanges
    rabbitmqctl list_queues
    rabbitmqctl list_bindings

Clean up the old config (which was)
    ./rabbitmqadmin declare queue name=CAPCollatorQueue durable=true
With
    ./rabbitmqadmin delete queue name='CAPCollatorQueue'



To watch the rabbit queues, visit http://localhost:15672/#/ and log on with cap/cap

at http://localhost:15672/#/queues you can see the size of any queues and wait for pending messages to stack up

# Piping output into a file

in development

  export TERM=dumb
  grails run-app

can ease your pain when redirecting log output to a file


# CI/CD to test environment

The Jenkinsfile will attempt to deploy a service from the definitions in the k8s directory HOWEVER this setup relies
on the pre-existence of a config file on the server. Specifically - this like in the config

        envFrom:
          - configMapRef:
              name: cap

Requires a configmap of the following shape:

    apiVersion: v1
    kind: ConfigMap
    metadata:
      namespace: swcaptest
      name: cap
    data:
      RABBIT_HOST: "default-rabbit-rabbitmq.services"
      CAP_RABBIT_USER: ""
      CAP_RABBIT_PASS: ""
      FF_DB_URL: "jdbc:postgresql://pg12-postgresql.default:5432/feedfacade"
      FF_USERNAME: ""
      FF_PASSWORD: ""
      FF_JDBC_DRIVER: "org.postgresql.Driver"
      FF_HIBERNATE_DIALECT: "org.hibernate.dialect.PostgreSQLDialect"
      CC_DB_URL: "jdbc:postgresql://pg12-postgresql.default:5432/capcollator"
      CC_USERNAME: ""
      CC_PASSWORD: ""
      CC_JDBC_DRIVER: "org.postgresql.Driver"
      CC_HIBERNATE_DIALECT: "org.hibernate.dialect.PostgreSQLDialect"
      CC_ES_HOST: "elasticsearch-coordinating-only.services"

## Dockerhub

The docker image for this service can be found here:

    https://hub.docker.com/r/semweb/caphub_feedfacade
