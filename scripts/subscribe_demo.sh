#!/bin/bash

# curl -X POST -d "hub.callback=http://localhost:8080/hubClient&hub.mode=subscribe&hub.topic=ca_msc_en&hub.release_seconds=&hub.secret=&trimNs=no&targetMimetype=json"  http://localhost:8080/hub
curl -X POST -d "hub.callback=http://localhost:8080/hubClient&hub.mode=subscribe&hub.topic=AllFeeds&hub.release_seconds=&hub.secret=&trimNs=no&targetMimetype=json"  http://localhost:8080/hub
