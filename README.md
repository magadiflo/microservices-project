# Repaso - Microservicios con Spring Boot y Spring Cloud Netflix Eureka

El curso que completé lo desarrollé con STS, mientras que en esta oportunidad,
a modo de repaso, trabajaré los microservicios con **módulos de maven** para poder
abrirlos con IntelliJ IDEA.

# Estructura creada

- **business-domain**: Aquí irán todos los microservicios que formen parte del dominio del negocio, como
  **ms-products, ms-items, etc...**.
- **infrastructure**: Aquí irán los ms que son parte fundamental de una arquitectura de microservicios, como
  **Eureka Server, Spring Cloud Load Balanced, Resilience4j, etc..**.
- **legacy**: Aquí irán las dependencias que anteriormente se usaban y que solo están disponibles
  para cierta versión antigua de Spring Boot, tal es el caso de: **Ribbon, Histryx, Zuul, etc..**.

---

## Repositorio Remoto donde se subieron los archivos de configuración

[config-server-repo](https://github.com/magadiflo/config-server-repo/tree/main)

---

## Implementando Spring Cloud Sleuth

Como la dependencia de Spring Cloud Sleuth es usada en casi todos los microservicios de este proyecto, es que la
información las iré anotando en este archivo README.md principal.

**¿Qué es Spring Cloud Sleuth?**

Es una dependencia que debemos agregar en nuestros microservicios, y **tan solo agregándola ya tenemos implementado**
esta solución para el **trazado distribuido**. Lo tenemos que agregar en cada microservicio que queremos hacer este
seguimiento.

Permite identificar la petición completa de un microservicio (como un todo), y en cada llamada individual a otros
microservicios.

Provee **atributos** importantes para poder **identificar la petición**:

- **traceId**: identificador asociado a la petición que viaja entre los microservicios. Es único
  para todo el viaje entre los microservicios.
- **spanId**: identificador de la unidad de trabajo de cada llamada a un microservicio, es decir
  cuánto se demora en ejecutar un proceso, alguna tarea dentro de un microservicio. En resumen es un **identificador
  para el seguimiento dentro de un microservicio**, es único, y solo le correponde a un microservicio en particular.

Entonces **una traza (trace)** está formado por un conjunto de span, por ejemplo:

````
INFO[ms-authorization-server, 8a40fc93ab435807, f72188570726632d, false]
````

**Donde:**

- **traceId**: 8a40fc93ab435807
- **spanId**: f72188570726632d
- El **false** nos indica que no contamos con la dependencia de Zipkin para ver gráficamente las trazas, así que no nos
  permitirá exportar la traza.

### Agregando dependencia de Sleuth en los microservicios

En el pom.xml de los siguientes microservicios agregaremos la dependencia de Sleuth:

- ms-items
- ms-productos
- ms-usuarios
- ms-authorization-server
- ms-zuul-server

````
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-sleuth</artifactId>
</dependency>
````

### Ejemplo: Viendo las trazas generadas cuando se solicita generar un token

Realizaremos una petición para poder generar un token con los siguientes parámetros:

````
[POST] http://127.0.0.1:8090/api-base/authorization-server-base/oauth/token
Authorization: 
	Basic Auth: 
		username: frontendApp
		password: frontendApp-12345
Body: 
  x-www-form-urlencoded:
	  username: 	admin
	  password: 	12345
	  grant_type:	password

Obtenemos resultado
{
    "access_token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VyX25hbWUiOiJhZG1pbiIsInNjb3BlIjpbInJlYWQiLCJ3cml0ZSJdLCJhcGVsbGlkbyI6IkFkbWluIiwiY29ycmVvIjoiYWRtaW5AbWFnYWRpZmxvLmNvbSIsImV4cCI6MTY4NDg2MDMzMSwibm9tYnJlIjoiQWRtaW4iLCJhdXRob3JpdGllcyI6WyJST0xFX0FETUlOIiwiUk9MRV9VU0VSIl0sImp0aSI6IjkxOTM4YWMyLWU1Y2ItNGFmZS05YjQ5LWY5MmU3YTE3YjEzZSIsImNsaWVudF9pZCI6ImZyb250ZW5kQXBwIn0.LJgdooyzRF3Ii1-uZnXyNzi5SM6kRvbo_jhn2byGoek",
    "token_type": "bearer",
    "refresh_token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VyX25hbWUiOiJhZG1pbiIsInNjb3BlIjpbInJlYWQiLCJ3cml0ZSJdLCJhcGVsbGlkbyI6IkFkbWluIiwiY29ycmVvIjoiYWRtaW5AbWFnYWRpZmxvLmNvbSIsImF0aSI6IjkxOTM4YWMyLWU1Y2ItNGFmZS05YjQ5LWY5MmU3YTE3YjEzZSIsImV4cCI6MTY4NDg2MDMzMSwibm9tYnJlIjoiQWRtaW4iLCJhdXRob3JpdGllcyI6WyJST0xFX0FETUlOIiwiUk9MRV9VU0VSIl0sImp0aSI6ImEwNmEyYmVjLTk2ZTMtNGUyYi1iMTM1LTY4MmY5MTlmODI1ZiIsImNsaWVudF9pZCI6ImZyb250ZW5kQXBwIn0.j6RQaEezFcdXrQOIfOmT93nvp8KP7n76LALsKlhRGjI",
    "expires_in": 3599,
    "scope": "read write",
    "apellido": "Admin",
    "correo": "admin@magadiflo.com",
    "nombre": "Admin",
    "jti": "91938ac2-e5cb-4afe-9b49-f92e7a17b13e"
}
````

Revisando trazabilidad generada en cada microservicio:

````
ZuulServer:  		  [ms-zuul-server,5ca3f78663de87f7,5ca3f78663de87f7,true] 
AuthorizationServer:      [ms-authorization-server,5ca3f78663de87f7,4ce49f0286041be7]
MsUsuarios: 		  [ms-usuarios,5ca3f78663de87f7,51146e1683ec9003]
	                  [ms-usuarios,5ca3f78663de87f7,1feee8c60e0c35e4]
	                  [ms-usuarios,5ca3f78663de87f7,259da3aa2d420f1b]
````

**Donde**:

- **traceId**, sería 5ca3f78663de87f7, es el identificador de la petición que viaja en todos los microservicios.
- **spanId**, es el identificador de la petición en cada microservicio. Nótese que son distintos entre sí.

Entonces, podría decirse que, la petición entra por **ms-zuul-server**, viaja a **ms-athorization-server** y desde
allí se hacen tres peticiones a **ms-usuarios**.
---

## Obteniendo y desplegando Zipkin Server y Zipkin UI

Zipkin es un sistema de rastreo distribuido. Ayuda a recopilar los datos de temporización necesarios para solucionar
problemas de latencia en las arquitecturas de servicio. Las características incluyen tanto la recopilación como la
búsqueda de estos datos.

Tiene una interfaz gráfica que muestra la trazabilidad de los microservicios. Funciona como complemento de la
dependencia **Spring Cloud Sleuth**.

Para poder descargar el proyecto necesitamos ir a su página web hacer click en la opción **Java latest release**
para que empiece a descargar un **.jar**:

````
https://zipkin.io/pages/quickstart.html
````

Ejecutamos el **.jar** descargado desde el cmd:

````
java -jar zipkin-server-2.24.1-exec.jar
````

Una vez que esté corriendo el programa, podemos acceder a su interfaz gráfica:

````
http://localhost:9411/zipkin
````

---

## Conectando Zipkin en los microservicios

Al igual que hicimos con la dependencia de **Sleuth**, igualmente haremos con la dependencia de **Zipkin**,
lo agregaremos en cada uno de los microservicios. Agregaremos la siguiente dependencia proporcionada por
spring initializr a los 5 microservicios con los que estamos trabajando, incluidos el ms-zuul-server.

````
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-sleuth-zipkin</artifactId>
</dependency>
````

**NOTA 01**:

En el curso, usa la dependencia **spring-cloud-starter-zipkin**, pero cuando traté de agregar al proyecto
no funciona, marca un error, así que spring initializr muestra esta nueva dependencia cuando buscamos **zipkin**.

**NOTA 02**:

Con esta dependencia (zipkin) hacemos que **el microservicio** que lo tenga sea un **cliente del servidor
zipkin**.

Ahora necesitamos **configurar parámetros de Sleuth** para **exportar las trazas a Zipkin**, que por defecto
es 10%, es decir, solo el 10% de las trazas serán exportados a zipkin, pero nosotros lo cambiaremos para que siempre
sean exportados todas 100%.

Además, de forma opcional podemos configurar la ruta del servidor de **zipkin**, ya que por defecto, siempre irá a
buscar en esa dirección la ruta del servidor:

````
# Probabilidad de exportar trazas. Cambiamos el 0.1 por defecto al 1
spring.sleuth.sampler.probability=1.0

# Opcional, configurar ruta del servidor zipkin
spring.zipkin.base-url=http://127.0.0.1:9411/
````

A modo de ejemplo, solo en el **ms-items** agregamos estas dos configuraciones, en los microservicios restantes solo
agregamos el de la probabilidad a 100%.

### Ejecutando microservicios y viendo trazabilidad

Debemos tener ejecutando el servidor de zipkin:

````
java -jar zipkin-server-2.24.1-exec.jar
````

Ahora, ejecutamos todos los 5 microservicios. Realizamos una petición, por ejemplo solicitar un token y vamos a la
siguiente dirección para ver gráficamente el flujo de la petición pro los microservicios:

````
http://localhost:9411/zipkin/
````

---

## Agregando atributos o tags personalizados en la traza

Podemos agregar información adicional en la traza, a fin de poder exponerlas para poder visualizarlas en **Zipkin**.

Para este ejemplo se trabajará con el **ms-authorization-server**. Lo primero a realizar será inyectar la
clase **Tracer**.

````
import brave.Tracer;

private final Tracer tracer;

    /* más código */
    private final Tracer tracer;    
    /* más código */

    public AuthenticationSuccessErrorHandler(/* más código */, Tracer tracer) {
        /* más código */
        this.tracer = tracer;
    }
    /* más código */
}
````

Ahora podemos hacer uso de su método para agregar un mensaje, ejemplo:

````
this.tracer.currentSpan().tag("error.mensaje", errors.toString());
````
