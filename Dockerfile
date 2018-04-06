FROM anapsix/alpine-java:8
MAINTAINER Justin Phillips "justin_phillips@ultimatesoftware.com"
VOLUME /tmp
ADD build/libs/edge-service-0.0.1.jar app.jar
RUN bash -c 'touch /app.jar'
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/app.jar"]