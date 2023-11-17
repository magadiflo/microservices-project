# Eureka Server

[Según la documentación de eureka](https://docs.spring.io/spring-cloud-netflix/docs/4.0.1/reference/html/#jdk-11-support):  
Los módulos JAXB de los que depende el servidor Eureka se eliminaron en JDK 11
*[en realidad según Andrés Guzmán es desde 9+]*. Si tiene la intención de utilizar JDK 11
cuando ejecute un servidor Eureka, debe incluir estas dependencias en su archivo POM o Gradle.

````xml
<dependency>
    <groupId>org.glassfish.jaxb</groupId>
    <artifactId>jaxb-runtime</artifactId>
</dependency
````

En mi caso, como estoy usando la versión 17 de java, es necesario agregar dicha dependencia.

---

# Sección 14: Desplegando Microservicios en Contenedores Docker

---

## Creando archivo Dockerfile para Eureka server, build y levantando contenedor

Como anteriormente ya habíamos creado un **Dockerfile** en el **ms-config-server**, copiaremos dicho archivo
y lo pegaremos en la raíz de este microservicio para hacerle algunas modificaciones:

````dockerfile
FROM openjdk:17-jdk-alpine
VOLUME /tmp
EXPOSE 8761
ADD ./target/ms-eureka-server-0.0.1-SNAPSHOT.jar eureka-server.jar
ENTRYPOINT ["java", "-jar", "/eureka-server.jar"]
````

Como observamos, lo único que cambió fue el nombre del .jar que corresponde al nombre de este microservicio, lo
demás lo dejamos tal cual.

Ahora, debemos generar el **.jar** para posteriormente ejecutar el Dockerfile y crear nuestra imagen. Entonces,
posicionados en la raíz del **ms-eureka-server** mediante cmd ejecutamos:

````bash
$ mvnw.cmd clean package
````

### Construyendo imagen de nuestro microservicio eureka server

Posicionados mediante el cmd en la raíz del ms-eureka-server, ejecutamos el siguiente comando:

````bash
$ docker build -t eureka-server:v1.0.0 .
````

Finalizada la construcción de la imagen, listamos todos las imágenes que hay en nuestro Docker y como resultado
devemos ven en esa lista nuestra imagen recién construida.

````bash
$ docker image ls

--- Resultado ---
REPOSITORY      TAG       IMAGE ID       CREATED              SIZE
eureka-server   v1.0.0    f3caf1354f57   About a minute ago   372MB
config-server   v1.0.0    36bca5b29011   5 hours ago          362MB
````

## Levantamos un contenedor (una instancia) de la imagen creada: eureka-server

````bash
$ docker container run -p 8761:8761 --name eureka-server --network ms-spring-cloud eureka-server:v1.0.0
````

**DONDE**

- **--name eureka-server**, le damos un nombre cualquiera a este contenedor, en mi caso lo llamé **eureka-server**.
- Ver más información detallada en el **README** del **ms-config-server**.

Ahora, que nuestro contenedor de **eureka-server** está ejecutándose, podemos acceder a ella a través de nuestro
navegador:

````
http://localhost:8761/
````
