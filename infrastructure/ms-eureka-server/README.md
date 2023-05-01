# Eureka Server

[Según la documentación de eureka](https://docs.spring.io/spring-cloud-netflix/docs/4.0.1/reference/html/#jdk-11-support):  
Los módulos JAXB de los que depende el servidor Eureka se eliminaron en JDK 11
*[en realidad según Andrés Guzmán es desde 9+]*. Si tiene la intención de utilizar JDK 11
cuando ejecute un servidor Eureka, debe incluir estas dependencias en su archivo POM o Gradle.

````
<dependency>
    <groupId>org.glassfish.jaxb</groupId>
    <artifactId>jaxb-runtime</artifactId>
</dependency
````

En mi caso, como estoy usando la versión 17 de java, es necesario agregar dicha dependencia.