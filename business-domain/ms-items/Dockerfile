FROM openjdk:17-jdk-alpine
VOLUME /tmp
EXPOSE 8002 8005 8007
ADD ./target/ms-items-0.0.1-SNAPSHOT.jar ms-items.jar
ENTRYPOINT ["java", "-jar", "/ms-items.jar"]