# Microservicio oauth (Servidor de autorización)

Una de las dependencias que agregaremos a este microservicio será el de la librería ms-usuarios-commons.
Dicha librería incorpora como dependencia a Spring Data Jpa. Por otro lado, nuestro microservicio
ms-authorization-server no interactuará directamente con una base de datos, sino que a través del cliente
http FeignClient hará peticiones al ms-usuarios y es este microservicio quien sí interactúa con una BD.

Pero ocurre lo siguiente, como en este microservicio incluimos la librería ms-usuarios-commons como dependencia, y este
a su vez tiene a Spring Data JPA, nos obligará a implementar un String de conexión a una base de datos porque con tan
solo agregar dicha dependencia a un proyecto (agregado por transitividad), este se autoconfigura, pero nosotros sabemos
que este microservicio no manejará persistencia, ni conexión a base de datos, por lo tanto, debemos desactivar la
autoconfiguración similar a cómo excluimos el DataSource en el ms-items, lo hicimos a través de anotaciones en la
clase principal. Aquí se muestran cómo sería en este caso:

**Usando anotación [Similar a cómo configuramos en el ms-items]**

````
@SpringBootApplication
@EnableAutoConfiguration(exclude = {DataSourceAutoConfiguration.class})
public class MsAuthorizationServerApplication {...}
````

**Desde el mismo pom.xml [En esta oportunidad trabajaremos con esta forma]**

````
<dependency>
    <groupId>com.magadiflo.msp.shared.library</groupId>
    <artifactId>ms-usuarios-commons</artifactId>
    <version>0.0.1-SNAPSHOT</version>
    <exclusions>
        <exclusion>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-jpa</artifactId>
        </exclusion>
    </exclusions>
</dependency>
````

---

## Creando nuestra interfaz Feign Client

Desde este microservicio nos comunicaremos con el ms-usuarios usando **FeignClient**, para esto ya tenemos en el pom.xml
la dependencia de Feign.

Primero, habilitaremos el uso de FeignClient con la anotación **@EnableFeignClients** desde la clase principal:

````
@EnableFeignClients
@EnableEurekaClient
@SpringBootApplication
public class MsAuthorizationServerApplication {...}
````

Crearemos la siguiente interfaz:

````
@FeignClient(name = "ms-usuarios", path = "/usuarios")
public interface IUsuarioFeignClient {
    @GetMapping(path = "/search/buscar-usuario")
    Usuario findByUsername(@RequestParam(value = "usuario") String username);

}
````

**Donde:**

- **name=ms-usuarios**, corresponde al **nombre del microservicio** al que nos comunicaremos.
- **path = "/usuarios"**, corresponde al path que le definimos al ms-usuarios. En este caso,
  como estamos usando en el ms-usuarios Spring Data Rest, el path lo definimos dentro de la
  anotación **@RepositoryRestResource(path = "usuarios")** aplicada a la interfaz IUsuarioRepository.

El resto del código es similar a cómo hemos venido trabajando con clientes Feign.