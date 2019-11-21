sdk use grails 4.0.1
sdk use java 11.0.5.j9-adpt
cd feedFacade
grails prod war
cp build/libs/feedFacade-2.0.0.war ../docker/feedFacade.war
cd ../docker
docker login
docker build -t semweb/caphub_feedfacade:v2.0 -t semweb/caphub_feedfacade:v2 -t semweb/caphub_feedfacade:latest .
docker push semweb/caphub_feedfacade:v2.0
docker push semweb/caphub_feedfacade:v2
docker push semweb/caphub_feedfacade:latest


