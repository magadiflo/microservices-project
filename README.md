# Repaso - Microservicios con Spring Boot y Spring Cloud Netflix Eureka

El curso que completé lo desarrollé con STS, mientras que en esta oportunidad,
a modo de repaso, trabajaré los microservicios con **módulos de maven** para poder
abrirlos con IntelliJ IDEA.

## Estructura creada

- **business-domain**: Aquí irán todos los microservicios que formen parte del dominio del negocio, como
  **ms-products, ms-items, etc...**.
- **infrastructure**: Aquí irán los ms que son parte fundamental de una arquitectura de microservicios, como
  **Eureka Server, Spring Cloud Load Balanced, Resilience4j, Config Server, Spring Cloud Gateway, etc..**.
- **legacy**: Aquí irán las dependencias que anteriormente se usaban y que solo están disponibles
  para cierta versión antigua de Spring Boot, tal es el caso de: **Ribbon, Hystrix, Zuul, etc..**, incluí también
  el ms-items, ya que usa internamente la dependencia de Hystrix.
- **security**: Aquí irán los microservicios correspondientes a la seguridad de la aplicación, como el ms-usuarios,
  el ms-authorization-server (OAuth2), etc.
- **shared-library**: aquí irán las librerías que vayamos creando, por ejemplo el ms-commons que incluye entidades
  de productos e items, estas librerías las usaremos como dependencias en los microservicios que la requieran, como
  el ms-productos y el ms-items. Otra librería sería el ms-usuarios-commons usada en el ms-usuarios y el
  ms-authorization-server.

## Solución al error cuando se intenta construir el .jar de un ms que tiene como dependencia una librería creada

```SOLUCIÓN, modificar la estructura del pom.xml para que el parent de la librería apunte a spring boot```

Con este error nos topamos, casi al finalizar el curso, en la sección de Docker. Para ponernos en contexto:
> La estructura incial del módulo **shared-library** era que su **pom.xml** tenía como padre al pom.xml
> del microservices-project, y las librerías dentro del shared-library tenían como padre al pom.xml del
> shared-library, algo así:
>
> **microservices-project <- shared-library <- ms-commons**<br>
> **microservices-project <- shared-library <- ms-usuarios-commons**<br>
>
> Ahora, estas librerías están como dependencia en los ms-products, ms-usuarios, etc.. es decir, están
> siendo usadas por nuestros otros microservicios. Por lo tanto, cuando se intentaba, por ejemplo, construir
> el .jar del ms-products (quien tenía como dependencia al ms-commons), nos mostraba el mensaje de error siguiente:<br>
>
> ``[ERROR] Failed to execute goal on project ms-productos: Could not resolve dependencies for project
> com.magadiflo.msp.business.domain:ms-productos:jar:0.0.1-SNAPSHOT: Failed to collect dependencies
> at com.magadiflo.msp.shared.library:ms-commons:jar:0.0.1-SNAPSHOT: Failed to read artifact descriptor
> for com.magadiflo.msp.shared.library:ms-commons:jar:0.0.1-SNAPSHOT: Could not find artifact
> com.magadiflo.msp:shared-library:pom:1.0-SNAPSHOT -> [Help 1]``<br>
>
> Investigué y no logré hallar la solución así que lo que hice fue similar a cómo tenemos estructurado el módulo
> de **/legacy**, es decir que el pom.xml de cada librería debe apuntar a spring-boot-starter-parent, tener su
> properties con la versión de java y la dependencia de test.

**Por ejemplo: ms-commons**

````xml

<project>
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>2.7.11</version>
        <relativePath/> <!-- lookup parent from repository -->
    </parent>

    <properties>
        <java.version>17</java.version>
        <spring-cloud.version>2021.0.6</spring-cloud.version>
    </properties>

    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>
</project>
````

**CONCLUSIÓN**, colocar el proyecto tal cual se descarga de spring initializr dentro del
módulo /shared-library. Y al igual que el pom.xml de /legacy, **el pom.xml de /shared-library
tampoco heredará de /microservices-project.**

---

## [Aquí: Repositorio Remoto donde se subieron los archivos de configuración](https://github.com/magadiflo/config-server-repo/tree/main)

---

## Implementando Spring Cloud Sleuth

> NOTA: A partir de aquí trabajaremos con Zuul Server para toda esta sección de trazabilidad.

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

````xml

<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-sleuth</artifactId>
</dependency>
````

### Ejemplo: Viendo las trazas generadas cuando se solicita generar un token

Realizaremos una petición para poder generar un token con los siguientes parámetros:

````bash
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

````bash
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

````bash
$ java -jar zipkin-server-2.24.1-exec.jar
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

````xml

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

````properties
# Probabilidad de exportar trazas. Cambiamos el 0.1 por defecto al 1
spring.sleuth.sampler.probability=1.0
# Opcional, configurar ruta del servidor zipkin
spring.zipkin.base-url=http://127.0.0.1:9411/
````

A modo de ejemplo, solo en el **ms-items** agregamos estas dos configuraciones, en los microservicios restantes solo
agregamos el de la probabilidad a 100%.

### Ejecutando microservicios y viendo trazabilidad

Debemos tener ejecutando el servidor de zipkin:

````bash
$ java -jar zipkin-server-2.24.1-exec.jar
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

````java
import brave.Tracer;

@Component
public class AuthenticationSuccessErrorHandler implements AuthenticationEventPublisher {
    /* más código */
    private final Tracer tracer;
    /* más código */

    public AuthenticationSuccessErrorHandler(IUsuarioService usuarioService, Tracer tracer) {
        this.usuarioService = usuarioService;
        this.tracer = tracer;
    }
    /* más código */
}
````

Ahora podemos hacer uso de su método para agregar un mensaje, ejemplo:

````
this.tracer.currentSpan().tag("error.mensaje", errors.toString());
````

---

# Sección 14: Desplegando Microservicios en Contenedores Docker

---

## Descargando imagen Docker para MySQL y levantando la instancia (pull y run)

Como estamos trabajando con las bases de datos de Mysql y Postgres en nuestros microservicios, necesitamos descargar
las imágenes dockerizadas de dichas bases de datos para seguir trabajando todo el proyecto con Docker. En esta
oportunidad descargaremos la imagen dockerizada de MySQL. Lo podemos buscar en la página de ``https://hub.docker.com/``,
allí se encuentran todas las imágenes oficiales.

Para descargar la imagen, ejecutamos el siguiente comando:

````bash
$ docker pull mysql:8
````

**DONDE:**

- **mysql**, la imagen a descargar
- **8**, el tag que seleccionamos de la imagen. Este tag nos indica que descargaremos MySQL versión 8.

Finalizada la descarga, listamos todas las imágenes para ver que ya tenemos la imagen de mysql en nuestro docker:

````bash
$ docker image ls

--- Resultado ---
REPOSITORY      TAG       IMAGE ID       CREATED          SIZE
mysql           8         05db07cd74c0   18 minutes ago   565MB
eureka-server   v1.0.0    f3caf1354f57   28 minutes ago   372MB
config-server   v1.0.0    36bca5b29011   5 hours ago      362MB
````

### Creando contenedor para MySQL

````bash
$ docker container run -p 3306:3306 --name ms-mysql8 --network ms-spring-cloud -e MYSQL_ROOT_PASSWORD=magadiflo -e MYSQL_DATABASE=bd_spring_boot_cloud -d mysql:8
````

**DONDE**:

- **--name ms-mysql8**, nombre que le damos al contenedor: **ms-mysql8**.
- **-e**, para colocar variables de ambiente. La imagen de MySQL que descargamos requiere las siguientes variables
  de ambiente: **MYSQL_ROOT_PASSWORD**, por defecto crea el usuario root y solo habría que agregar el password;
  **MYSQL_DATABASE**, le especificamos el nombre de la base de datos que queremos que cree. **Importante**, por cada
  variable de ambiente se tiene que colocar la bandera -e.
- **-d**, para que el contenedor se ejecute por debajo, en background, en modo **detached** (separado de la consola).
- **mysql:8**, la imagen que descargamos de docker hub.
- Para una explicación a más detalle ver el **README** del **ms-config-server**.

**IMPORTANTE**
Como crearemos un contenedor de mysql que expondrá su puerto al 3306, es importante que ese puerto de nuestra pc física
no esté ocupado. Por ejemplo, si tenemos en nuestra pc física corriendo la BD de MySQL en el puerto 3306 (por defecto)
e intentamos levantar el contenedor de MySQL que expone externamente el puerto 3306, no se podrá, ya que el puerto
lo tiene nuestra BD de la pc física. La solución simplemente es bajar el servicio de MySQL que corre en nuestra pc
local, al menos mientras estemos trabajando con el contenedor de MySQL. **Otra opción sería usar otro puerto.**

### Abriendo con DBeaver nuestra base de datos que está siendo ejecutada en nuestro contenedor "ms-mysql8"

Abrimos DBeaver e ingresamos los datos para poder conectarnos a nuestro contenedor de MySQL:

````
/General
Server Host: localhost
Port: 3306
Nombre de usuario: root
Contraseña: magadiflo

/Driver properties
allowPublicKeyRetrieval = TRUE
````

**IMPORTANTE**, es importante que cambiemos el **allowPublicKeyRetrieval a TRUE**, sino, nos mostrará el mensaje
de error: ``public key retrieval is not allowed`` y no nos dejará ingresar.

Luego de ingresar, debemos observar que **tenemos nuestra base de datos creada** sin ninguna tabla.

---

## Descargando imagen Docker para PostgreSQL y levantando la instancia (pull y run)

Similar a como descargamos y levantamos mysql, ahora haremos lo mismo con PostgreSQL.

Descargamos la imagen de PostresSQL, versión 12. Usamos el tag 12-alpine porque esa versión de postgres está en
el SO alpine de linux que es muy ligero:

````bash
$ docker pull postgres:12-alpine
````

Listamos las imágenes y observamos que ya tenemos en nuestro Docker la imagen de postgres:

````bash
$ docker image ls

--- Resultado ---
REPOSITORY      TAG         IMAGE ID       CREATED             SIZE
mysql           8           05db07cd74c0   About an hour ago   565MB
eureka-server   v1.0.0      f3caf1354f57   About an hour ago   372MB
config-server   v1.0.0      36bca5b29011   6 hours ago         362MB
postgres        12-alpine   945704f99920   5 days ago          230MB
````

Ahora, crearemos un contenedor a partir de la imagen de postgres:

````bash
$ docker container run -p 5432:5432 --name ms-postgres12 --network ms-spring-cloud -e POSTGRES_PASSWORD=magadiflo -e POSTGRES_DB=bd_spring_boot_cloud -d postgres:12-alpine
````

**DONDE**:

- Al igual que MySQL aquí usamos las variables de entorno para agregar el password y nombre de la base de datos,
  mientras
  que por defecto el nombre de usuario para postgres es **postgres**.
- **-d**, le indicamos que correremos el contenedor en modo detached.

Si queremos ver el **log** del contenedor que se está ejecutando, ejecutamos el siguiente comando:

````bash
$ docker container logs -f ms-postgres12
````

### Abriendo con DBeaver nuestra base de datos que está siendo ejecutada en nuestro contenedor "ms-postgres12"

Abrimos DBeaver e ingresamos los datos para poder conectarnos a nuestro contenedor de Postgres:

````
/General
Host: localhost
Port: 5432
Database: bd_spring_boot_cloud
Nombre de usuario: postgres
Contraseña: magadiflo
````

Luego de ingresar, debemos observar que **tenemos nuestra base de datos creada** sin ninguna tabla.

---

## Configurando nombre de servidor de MySQL y PostgreSQL en repositorio Git

Modificaremos los archivos de configuración que están en el repositorio de configuración para que apunte a las bases
de datos que están contenerizados: mysql y postgres.

Modificaremos el archivo **ms-productos-development.properties**, reemplazando el **localhost** por el nombre que le
dimos a nuestro contenedor de mysql, el cual fue: **ms-mysql8**:

````properties
spring.datasource.url=jdbc:mysql://ms-mysql8:3306/bd_spring_boot_cloud?serverTimezone=America/Lima
````

Lo mismo haremos con el archivo **ms-usuarios-development.properties**, cambiando el **localhost** por el nombre que
le dimos al contenedor de postgres: **ms-postgres12**:

````properties
spring.datasource.url=jdbc:postgresql://ms-postgres12:5432/bd_spring_boot_cloud
````

Finalmente, debemos hacer un commit y subir los cambios al repositorio remoto de GitHub.

#### ¿Por qué cambiamos el localhost al nombre de los contenedores?

Como ahora estamos contenerizando todos nuestros proyectos, y hasta este momento ya tenemos contenerizado nuestras
bases de datos MySQL y PostgreSQL, también el ms-config-server y el ms-eureka-server y así iremos contenerizando los
demás microservicios, es necesario para que estos puedan comunicarse, cambiar en vez de localhost al nombre del
contenedor que les dimos con el --name, de esa forma podemos enlazar las comunicaciones entre microservicios ya sea
recursos de base de datos o también cuando queremos conectar un microservicio cliente con eureka o con el servidor de
configuración, etc. Por ejemplo, nuestro ms-productos necesita conectarse a la base de datos de MySQL y como nuestro
ms-productos será contenerizado, este se comunicará a nuestra BD MySQL contenerizada a través del nombre del contenedor
de la base de datos.
---

## Configurando URL de Eureka y Server Config en cada Microservicio

Nuevamente, como ahora nuestros microservicios estarán contenerizados, necesitamos que estos apunten al nombre del
contenedor. Veamos cómo lo haremos:

En el **application.properties** cambiamos el **localhost** para que apunte al contenedor de eureka
y al del servidor de configuraciones, esto lo haremos en los siguientes microservicios:

- ms-productos
- ms-usuarios
- ms-zuul-server   <----------- application.properties y bootstrap.properties
- ms-authorization-server
- ms-items

````properties
# Apuntando al contenedor de Eureka
eureka.client.service-url.defaultZone=http://eureka-server:8761/eureka
````

````properties
# Apuntando al contenedor de Spring Config
spring.config.import=optional:configserver:http://config-server:8888
````

---

# Despliegue de contenedores con docker compose

---
**Docker compose** nos va a permitir levantar contenedores a partir de las imágenes que tenemos de nuestros
microservicios, de esa forma evitamos estar escribiendo los comandos por **cmd**.

El **primer paso** a seguir es **eliminar todos los contenedores** que tenemos en docker, porque los volveremos a
levantar, pero esta vez usando **docker-compose**.

El **segundo paso** es crear el archivo **docker-compose.yml**, en nuestro caso lo crearemos en la raíz de nuestro
proyecto de módulos principal **/microservices-project**.

Ahora, agregaremos las siguientes configuraciones en nuestro archivo **docker-compose.yml**:

````yml
version: "3.9"
services:
  config-server:
    container_name: config-server
    image: config-server:v1.0.0
    ports:
      - "8888:8888"
    env_file:
      - ./infrastructure/ms-config-server/.env
    restart: always
    networks:
      - ms-spring-cloud
  eureka-server:
    container_name: eureka-server
    image: eureka-server:v1.0.0
    ports:
      - "8761:8761"
    restart: always
    networks:
      - ms-spring-cloud
networks:
  ms-spring-cloud:
    name: ms-spring-cloud
````

**DONDE**

- **version**, indica la versión del formato del archivo docker-compose.yml (el compose file format).
- **services**, es un arreglo que contendrá todos los contendores o servicios.
- **config-server**, nombre del que le daremos al servicio.
- **container_name: config-server**, es el nombre de la instancia que le daremos a nuestro contenedor para el
  ms-config-server. Es equivalente al --name que usamos en el cmd.
- **image: config-server:v1.0.0**, el nombre de la imagen más su tag que tenemos en docker.
- **ports**, corresponde al puerto externo:interno que usaremos. Es un arreglo, por eso va en guion.
- **env_file**, le indicamos la ruta donde está nuestro archivo .env donde tenemos la variable de ambiente
  REPO_CONFIG_PASS.
- **restart: always**, le decimos que siempre se reinicie, por ejemplo si ocurre algún error, que vuelva a reiniciarse.
- **networks: - ms-spring-cloud**, le decimos que use la red que tenemos creada.
- **networks: ms-spring-cloud: name: ms-spring-cloud**, creamos la red que usaremos, en nuestro caso el mismo que hemos
  usado hasta ahora.

**NOTA SOBRE EL ARCHIVO .env DEL ms-config-server**

> Solo es para mantener en mi pc el token. El application.properties del ms-config-server está leyendo el token, pero
> desde IntelliJ IDEA o desde la cmd cuando ejecutamos el .jar agregándole la variable de entorno o desde el
> mismo docker-compose.yml.

### Ejecutando contenedores con docker-compose

Nos ubicamos mediante cmd en la raíz de nuestro proyecto principal (microservices-project), ya que allí tenemos nuestro
archivo **docker-compose.yml** y ejecutamos el siguiente comando:

````bash
$ docker-compose up
````

Luego de ejecutar el comando anterior, veremos en la consola que nuestros contenedores se empezarán a levantar. Si no
quisiéramos que se vieran los logs en la consola, al comando anterior agregarle la bandera **-d** (ques detached).
---

## Despliegue de servicios mysql y productos con docker compose

En este apartado agregamos dos servicios adicionales en el **docker-compose.yml**: ms-mysql8 y ms-productos:

````yml
  ms-mysql8:
    container_name: ms-mysql8
    image: mysql:8
    ports:
      - "3306:3306"
    environment:
      MYSQL_ROOT_PASSWORD: magadiflo
      MYSQL_DATABASE: bd_spring_boot_cloud
    restart: always
    networks:
      - ms-spring-cloud
  ms-productos:
    image: ms-productos:v1.0.0
    restart: always
    networks:
      - ms-spring-cloud
    depends_on:
      - config-server
      - eureka-server
      - ms-mysql8
````

**NOTA 01:** En el servicio **ms-productos** agregamos los servicios de los que depende el ms-productos, es decir,
para que el ms-productos se levante, necesita previamente que estén levantados los servicios que se muestran
en ese orden: **config-server, eureka-server y ms-mysql8**.

**NOTA 02:** En el servicio **ms-productos** no le agregamos el atributo **"container_name",** ya que si lo hacemos
estaríamos dándole un nombre fijo al contenedor y como el contenedor que aloja al ms-productos podrá escalarse (por eso
es que definimos el puerto en aleatorio) nos mostrará un error, porque docker requiere que cada contenedor tenga un
nombre único. En resumen, no debemos colocarle el atributo **container_name**, docker automáticamente le asignará
un nombre aleatorio por cada instancia que se escale.

### Ejecutando docker compose para levantar los contenedores

Antes de ejecutar docker-compose debemos eliminar todos los contenedores que tengamos en docker. Si hemos venido
trabajando con docker-compose, solo es ejecutar el siguiente comando: ``docker-compose down``.

Ahora sí empezamos a ejecutar nuestros contendores. Como vimos el servicio **ms-productos** depende del **config-server,
eureka-server y ms-mysql8**, por lo tanto, iremos levantando en ese orden, servicio tras servicio:

````bash
$ docker-compose up -d config-server
````

````bash
$ docker-compose up -d eureka-server
````

````bash
$ docker-compose up -d ms-mysql8
````

Finalmente nuestro contenedor cuyo nombre del servicio es: ms-productos

````bash
$ docker-compose up -d ms-productos
````

Verificamos en la web de eureka server que esté la instancia de nuestro servicio productos.

### Escalando contenedor de ms-productos

Como vimos en el apartado anterior, al servicio **ms-productos** no le agregamos un **container_name** explícitamente,
sino que dejamos que docker le asigne uno aleatoriamente y eso es porque precisamente este **ms-productos** está
pensado para poder ser **escalado**.

Para poder escalar el número de instancias o contenedores que quisiéramos, ejecutamos el siguiente comando:

````bash
$ docker-compose up --scale ms-productos=3 -d
````

**DONDE:**

- **--scale**, bandera que nos indica que escalaremos el contenedor.
- **ms-productos=3**, indicamos el **nombre del servicio** y la cantidad de contenedores que quisiéramos tener.
- **-d**, es el **detached** de toda la vida, para no ver el log en la consola y seguir ejecutando más comandos.

---

## Despliegue de ms-zuul-server e items con docker-compose

Agregamos al archivo docker-compose.yml dos servicios más correspondientes para los contenedores de items y zuul server:

````yml
  ms-items:
    container_name: ms-items
    image: ms-items:v1.0.0
    ports:
      - "8002:8002"
      - "8005:8005"
      - "8007:8007"
    restart: always
    networks:
      - ms-spring-cloud
    depends_on:
      - config-server
      - eureka-server
      - ms-productos
  ms-zuul-server:
    container_name: ms-zuul-server
    image: ms-zuul-server:v1.0.0
    ports:
      - "8090:8090"
    restart: always
    networks:
      - ms-spring-cloud
    depends_on:
      - config-server
      - eureka-server
      - ms-productos
      - ms-items
````

Lo que se puede comentar del código anterior es las dependencias, es decir por ejemplo el servicio de **ms-zuul-server**
depende de los servicios: **config-server, eureka-server, ms-productos y ms-items**, lo que significa que para poder
levantar el servicio de **ms-zuul-server** con docker-compose, previamente tenemos que levantar los servicios ya
descritos.

Orden de ejecución de cada servicio usando docker-compose:

````bash
$ docker-compose up -d config-server
$ docker-compose up -d eureka-server
$ docker-compose up -d ms-mysql8
$ docker-compose up -d ms-productos
$ docker-compose up -d ms-items
$ docker-compose up -d ms-zuul-server
````

---

## Levantando todos los contenedores con un solo comando de docker-compose

Como en el archivo **docker-compose.yml** ya tenemos configurado cada servicio para la construcción de su
correspondiente contenedor, incluso definimos el atributo **depends_on** para definir los servicios
de las cuales depende otro servicio y además el atributo **restart: always** para que siempre se reinicie,
por ejemplo cuando ocurra algún error. Entonces bajo esas consideraciones es solo es necesario ejecutar
el comando siguiente:

````bash
$ docker-compose up -d
````

En automático se procederá con la construcción de los contenedores. Ahora, si por ejemplo, el **ms-productos**
se inicia antes que el **ms-mysql8**, ocurrirá un error en el **ms-productos**, puesto que necesita del **ms-mysql8**
para poder crear las tablas e insertar los registros de productos, entonces lo que hará será reiniciarse tantas veces
como sea posible hasta que el **ms-mysql8** se haya levantado y pueda conectarse a él correctamente, ahí tenemos la
razón del **restart: always**. Ahora, la única diferencia
sería que ``debemos esperar un tiempo hasta que todos se hayan levantado correctamente.``, mientras que ejecutando
el docker-compose up, servicio tras servicio, en forma ordenada, sería algo más rápido aunque tedioso porque hay que
estar ejecutando comando tras comando.