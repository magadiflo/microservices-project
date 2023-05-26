FROM openjdk:17-jdk-alpine
VOLUME /tmp
ADD ./target/ms-usuarios-0.0.1-SNAPSHOT.jar ms-usuarios.jar
ENTRYPOINT ["java", "-jar", "/ms-usuarios.jar"]