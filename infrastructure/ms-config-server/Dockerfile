FROM openjdk:17-jdk-alpine
VOLUME /tmp
EXPOSE 8888
ADD ./target/ms-config-server-0.0.1-SNAPSHOT.jar config-server.jar
ENTRYPOINT ["java", "-jar", "/config-server.jar"]