FROM adoptopenjdk:11-jdk-openj9-bionic
MAINTAINER Ian.Ibbotson@semweb.co
VOLUME /tmp
# ADD service-0.1.war mockncipserver.war
COPY ./feedFacade.war /feedFacade.war

# Handy info here: https://dev.to/hugodias/wait-for-mongodb-to-start-on-docker-3h8b
# ADD https://github.com/ufoscout/docker-compose-wait/releases/download/2.6.0/wait /wait
# RUN chmod +x /wait

EXPOSE 8080/tcp

# ENTRYPOINT ["/wait", "java","-Djava.security.egd=file:/dev/./urandom","-Xshareclasses", "-Xscmx50M", "-Xtune:virtualized", "-jar","/feedFacade.war"]
# CMD /wait && java -Djava.security.egd=file:/dev/./urandom -Xshareclasses -Xscmx50M -Xtune:virtualized -jar /feedFacade.war
# Trying the exec form of CMD as signals don't seem to be reaching the JVM - following did not work so reverting
# CMD [ "java", "-Djava.security.egd=file:/dev/./urandom", "-Xshareclasses", "-Xscmx50M", "-Xtune:virtualized", "-jar /feedFacade.war" ]
CMD java -Djava.security.egd=file:/dev/./urandom -Xshareclasses -Xscmx50M -Xtune:virtualized -jar /feedFacade.war
