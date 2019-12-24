#!/bin/bash
export SDKMAN_DIR="/home/ibbo/.sdkman"
[[ -s "/home/ibbo/.sdkman/bin/sdkman-init.sh" ]] && source "/home/ibbo/.sdkman/bin/sdkman-init.sh"

export FF_VER=`grep appVersion ./feedFacade/gradle.properties | cut -f2 -d=`

sdk use grails 4.0.1
sdk use java 11.0.5.j9-adpt
cd feedFacade
grails clean
grails prod war
cp build/libs/feedFacade-*.war ../docker/feedFacade.war
cd ../docker
docker login
docker build -t semweb/caphub_feedfacade:v$FF_VER -t semweb/caphub_feedfacade:v2.0 -t semweb/caphub_feedfacade:v2 -t semweb/caphub_feedfacade:latest .
docker push semweb/caphub_feedfacade:v$FF_VER
docker push semweb/caphub_feedfacade:v2.0
docker push semweb/caphub_feedfacade:v2
docker push semweb/caphub_feedfacade:latest



# See https://medium.com/@krishnaregmi/wait-for-it-docker-compose-f0bac30f3357 for wait-for-it info
