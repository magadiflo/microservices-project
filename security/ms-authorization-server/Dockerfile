FROM openjdk:17-jdk-alpine
VOLUME /tmp
EXPOSE 9100
ADD ./target/ms-authorization-server-0.0.1-SNAPSHOT.jar authorization-server.jar
ENTRYPOINT ["java", "-jar", "/authorization-server.jar"]