# PubSubHubBubFacade

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

## Technology

Currently implemented in Java using the groovy-on-grails framework, but runnable as a microservice using the
production jar. An embedded database is used for feed state.

## Deployment

There are many deployment options, the initial goal was for a local facade, but projects may also use the
service internally to poll RSS and turn feeds into event streams.

## References

http://pubsubhubbub.github.io/PubSubHubbub/pubsubhubbub-core-0.4.html
https://github.com/pubsubhubbub/PubSubHubbub
https://en.wikibooks.org/wiki/WebObjects/Web_Services/How_to_Trust_Any_SSL_Certificate
