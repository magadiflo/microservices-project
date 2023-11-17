# Microservicio Producto

## Ejecutando múltiples instancias en IntelliJ IDEA (definiendo puerto)

- En el curso que estoy llevando **(Microservicios con Spring Boot y Spring Cloud Netflix Eureka)**
  sección 2: Microservicios: la base, capítulo 18. Balaneo de carga del lado del cliente con Ribbon,
  minuto 13:12 ejecuta múltiples instancias del mismo proyecto pero en distintos puertos. Los puertos
  las ingresa manualmente mediante STS.
- En mi caso, estoy usando IntelliJ IDEA, y la misma opción se encuentra siguiendo estos pasos:

````
PRIMERO:
Ejecutamos el proyecto como normalmente lo hacemos, se levantará en el puerto 8001

SEGUNDO:
Para ejecutar otra instancia del proyecto, es necesario cambiar el puerto ya que ya hay
una instancia ejecutándose en el puero definido (8001).

PASOS:
- Nos vamos a Edit Configurations...
- En el lado izquierdo seleccionamos la configuración que está ejecutándose con el puerto 8001.
- Clickeamos en Copy Configuration
- Seleccionamos la nueva configuración copiada
- Agregamos un nombre a la configuración: Ejm. MsProductosApplication 9001
- Click en Modify options
- Seleccionamos Add VM options
- Se agregará un nuevo campo de texto. Escribimos el comando para cambiar el puerto
	-Dserver.port=9001
- Apply y OK

Ejecutando nueva instancia
----------------------------
Seleccionamos nuestra nueva configuración y ejecutamos el proyecto.
````

## Ejecutando múltiples instancias en IntelliJ IDEA (Puerto dinámico)

- Para generar un puerto dinámico a la aplicación, es necesario agregar la siguiente
  configuración en el archivo de propiedades ```server.port=${PORT:0}```, donde **PORT**
  se utiliza para referenciar a una variable de entorno en Spring Boot y **0** indica que si
  no se encuentra ninguna variable de entorno **PORT**, se utilizará un valor predeterminado de
  cero (0). Esto significa que si no se especifica una variable de entorno **PORT**, el servidor
  web se ejecutará en el puero **0**, que es un valor especial en Spring Boot. **Al usar cero (0),
  el servidor web seleccionará automáticamente un puerto disponible al azar.**
- Ahora, en **Edit Configurations...** de IntelliJ IDEA agregamos el número de configuraciones
  (de instancias) que queremos ejecutar. Yo crearé 5 configuraciones porque quiero 5 instancias.
- Por defecto, debe haber alguna configuración, pero sino lo hay, entonces damos en:

````
- Click en + (Add new configuration)
- Seleccionar Appication
- Name, le agregamos un nombre a la configuración. Ejmpl. MsProductosApplication (1)
- Build and run, input Main Class, agregar nuestra clase principal: com.magadiflo.msp.business.domain.productos.app.MsProductosApplication
- Apply and OK, ya tenemos nuestra primera instancia.
- Ahora, ingresamos nuevamente a Edit Configurations y copiamos 5 veces la misma configuración (el n° que necesite)
- Apply and OK.
- Ahora, para ejecutar cada instancia, debo ir seleccionando configuración por configuración.
````

## [Identificador único para instancias](https://cloud.spring.io/spring-cloud-netflix/multi/multi__service_discovery_eureka_clients.html)

Al usar Spring Cloud, puede anular el valor de la instancia única proporcionada por **Spring Cloud Eureka**
al proporcionar un identificador único en **eureka.instance.instanceId**, como lo hicimos en el archivo
de propiedades:

````properties
eureka.instance.instance-id=${spring.application.name}:${spring.application.instance_id:${random.value}}
````

La configuración anterior se utiliza para identificar de forma única una sola instancia de múltiples servicios.
Por ejmp. si implementó 2x instancias de la misma aplicación de Spring Boot, la instance-id se usa para distinguirlas.
La propiedad mostrada arriba simplemente toma las otras propiedades, las combina. El único problema es que si no se
encuentra
un spring.application.instance_id en el entorno, utilizará un valor aleatorio en su lugar. Puede anularlo como desee,
pero debe ser único.

## Agregando el proyecto commons como dependencia

Para reutilizar nuestra clase Producto tanto por este microservicio como por el ms-items,
creamos un proyecto de librería (ms-commons). Nuestro ms-productos necesita un Entity Producto
(con anotaciones de Spring Data Jpa) y nuestro ms-items únicamente un Pojo.

Para usar la Entity Producto de la librería ms-commons, necesitamos agregar en el pom.xml de este
microservicio, su dependencia:

````xml

<dependency>
    <groupId>com.magadiflo.msp.shared.library</groupId>
    <artifactId>ms-commons</artifactId>
    <version>0.0.1-SNAPSHOT</version>
</dependency>
````

Modificamos las importaciones en todos las clases e interfaces que hacen
uso de la clase Producto para que apunten al de la librería agregada. Además,
como ahora usamos la librería, eliminamos el package con la clase Producto
de este microservicio.

Finalmente, necesitamos decirle a Spring que nuestra **Clase Entity Producto** la escanee de otro
paquete (las del proyecto de librería):

````
@EntityScan(basePackages = {"com.magadiflo.msp.shared.library.commons.app.models.entity"})
````

Esto nos **permite registrar el package donde tenemos la clase de hibernate/jpa**,
pero en un **CONTEXTO DE PERSISTENCIA**.

Nuestra clase principal quedaría así:

````java

@EnableEurekaClient
@SpringBootApplication
@EntityScan(basePackages = {"com.magadiflo.msp.shared.library.commons.app.models.entity"})
public class MsProductosApplication {

    public static void main(String[] args) {
        SpringApplication.run(MsProductosApplication.class, args);
    }

}
````

**IMPORTANTE**

`Nuestro ms-items también hace uso de la clase Producto de la librería ms-commons, pero como dicho
microservicio no maneja persistencia jpa, es decir, solo necesitamos usar la clase como un POJO y no como
una clase Entity, por lo tanto no necesitamos agregarle la anotación @EntityScan(...), pero si
tuviéramos un servicio con persistencia (tal como este ms-productos) y necesitamos esa clase para
trabajar con CRUD Repository, JPA/Hibernate, etc... ahí sí que sería necesario agregar la anotación`.
---

## Configurando Datasource MySQL en ms-productos

**Si no configuramos ningún Datasource** y tenemos agregado en las dependencias la BD **h2**
por defecto, usará esta última base de datos.

Agregamos en el **pomx.xml**, la dependencia del Driver de MySQL:

````xml

<dependency>
    <groupId>com.mysql</groupId>
    <artifactId>mysql-connector-j</artifactId>
    <scope>runtime</scope>
</dependency>
````

Agregamos configuraciones de Jpa y conexión a MySQL y al **application.properties**:

````properties
# Datasource MySQL
spring.datasource.url=jdbc:mysql://localhost:3306/bd_spring_boot_cloud?serverTimezone=America/Lima
spring.datasource.username=root
spring.datasource.password=magadiflo
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
# Configurando dialecto de MySQL
spring.jpa.database-platform=org.hibernate.dialect.MySQL8Dialect
# Generando tablas: create, crea el esquema y destruye cualquier dato previo
spring.jpa.hibernate.ddl-auto=create
# Vista y formato en el log
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
````

---

## Configurando ambiente development con MySQL en Servidor de Configuración

En el repositorio del servidor de configuraciones creamos un archivo llamado
**ms-productos-development.properties** y agregamos a él las configuraciones
de conexión del Datasource, el dialecto, etc., **realizadas en el apartado anterior**, con la
finalidad de que estas configuraciones no estén hardcodeadas en el ms-productos, sino,
se encuentren centralizadas en el repositorio del servidor de configuraciones.

Recordar que **luego de agregado el archivo al repositorio local**, **pushear** los cambios
para tenerlos en el **repositorio remoto**.

Luego, en este punto del curso se pide agregar la dependencia de Spring Config Client, pero yo **ya lo había agregado en
capítulos pasados**, cuando trabajamos con el **ms-items**.

Como **estoy trabajando con módulos de maven**, la dependencia de **Spring Config Client**
lo agregué en el **pom.xml** del módulo **business-domain**, ya que es una dependencia que lo tendrá
tanto el **ms-items** como el **ms-productos**.

A continuación se muestra la dependencia usada para que el ms-productos se comunique con Spring Config, pero
``recordar que dicha dependencia ya la tiene nuestro ms-productos, pues la está heredando del módulo business-domain``:

````xml

<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-config</artifactId>
</dependency>
````

También teníamos configurada en el **application.properties** la dirección que apunta al servidor de configuraciones,
y esto fue porque, cuando en capítulos pasados agregamos la dependencia de spring cloud config, nuestro ms-productos,
requería que se le agregue dicha configuración.

Ahora, en este punto del curso se agregan la configuración que ya teníamos anteriormente más el perfil seleccionado:

````properties
# Configurando url al servidor de configuraciones
spring.config.import=optional:configserver:http://localhost:8888
spring.profiles.active=development
````

**NOTA**

Recordar que el perfil seleccionado: **development**, corresponde con el archivo que creamos en el repositorio del
servidor de configuraciones **ms-productos-development.properties** y es en donde colocamos las configuraciones
de JPA y conexión a MySQL que se encontraban en el **application.properties** de este microservicio.

---

# Sección 14: Desplegando Microservicios en Contenedores Docker

---

## Creando archivo Dockerfile para ms-productos, build y run

Al igual que hicimos con los microservicios de ms-config-server y ms-eureka-server, para poder contenerizar este
microservicio, debemos crear un Dockerfile. Para ahorrarnos el trabajo, copiaremos el **Dockerfile** del
ms-config-server y lo pegaremos en la raíz de este microservicio y realizaremos algunas modificaciones:

**Primero**, como a este microservicio le definimos un **puerto aleatorio** (ver el application.properties), no
sabemos exactamente cuál será el puerto que tomará cuando se ejecute, así que por esa razón el **EXPOSE** de este
Dockerfile lo eliminamos, no va.

Así quedaría el **Dockerfile** del ms-productos:

````Dockerfile
FROM openjdk:17-jdk-alpine
VOLUME /tmp
ADD ./target/ms-productos-0.0.1-SNAPSHOT.jar ms-productos.jar
ENTRYPOINT ["java", "-jar", "/ms-productos.jar"]
````

Ahora, necesitamos generar el **.jar** para este microservicio desde la terminal. Ubicados en la raíz del ms-productos,
ejecutamos el siguiente comando para generar el .jar:

````
mvnw.cmd clean package -DskipTests
````

**DONDE:**

- **package**, nos empaqueta el .jar dentro del mismo proyecto ms-productos.
- **-DskipTests**, nos permite saltarnos los tests. Esto es importante porque en el application.properties de este
  microservicio tenemos configuraciones que apuntan a direcciones que no estamos ejecutando en este momento como el
  eureka-server, o el de config-server, o el de la conexión a mysql. Entonces, para evitar que nos muestre errores
  y no se construya el .jar, agregamos dicha bandera.

**IMPORTANTE**

> Como esta dependencia está usando la librería ms-commons que creamos en el módulo shared-library, **primero es
> necesario compilar dicha librería**, así que nos vamos a la raíz del **ms-commons** y ejecutamos el siguiente
> comando:
>
> mvnw.cmd clean install
>
> Con el comando anterior, no solo generaremos el .jar dentro del /target de la librería, sino que el .jar SE
> INSTALARÁ en el REPOSITORIO LOCAL DE MAVEN, para que pueda ser usada como dependencia del microservicio que la
> requiera.

### Creando la imagen del ms-productos

Debemos estar ubicados en la raíz de este microservicio con el cmd y ejecutar el siguiente comando:

````bash
$ docker build -t ms-productos:v1.0.0 .
````

Finalizado la construcción, verificamos si ya tenemos la imagen en docker:

````bash
$ docker image ls

--- Resultado ---
REPOSITORY      TAG         IMAGE ID       CREATED              SIZE
ms-productos    v1.0.0      e82d48f34573   About a minute ago   391MB
mysql           8           05db07cd74c0   21 hours ago         565MB
eureka-server   v1.0.0      f3caf1354f57   21 hours ago         372MB
config-server   v1.0.0      36bca5b29011   26 hours ago         362MB
postgres        12-alpine   945704f99920   6 days ago           230MB
````

### Creando contenedor a partir de la imagen del ms-productos

Antes de crear el contenedor para el ms-productos, es necesario que los contenedores de **ms-mysql8, eureka-server y
config-server** estén ejecutándose, en caso estén detenidos iniciarlos con el siguiente comando:

Por ejemplo, iniciando el contenedor ms-mysql8 que está detenido:

````bash
$ docker start ms-mysql8
````

Ahora sí podemos ejecutar el siguiente comando:

````bash
$ docker container run -P --network ms-spring-cloud ms-productos:v1.0.0
````

**DONDE:**

- **-P**, en **mayúscula y sin valor**, nos indica que **el puerto generado será aleatorio**, y eso está bien, porque
  recordemos que en el ms-productos definimos en el application.properties que el puerto será aleatorio.

**NOTA:**

- El atributo **--name** que nos permite agregar un nombre a este contenedor, para este microservicio será opcional,
  ya que en ningún otro lugar haremos referencia a este microservicio. Recordemos que se conecta con Eureka y con el
  Servidor de configuraciones por debajo, pero accediendo a la ruta url, a los endpoints propios de Eureka y del
  Servidor de Configuraciones.

Si todo se ejecutó correctamente, verificamos que nuestro microservicio de productos esté en eureka server. Podemos
acceder a través de esta url: http://localhost:8761/, además si abrimos **DBeaver** debemos ver que la base de datos
tiene poblada la tabla productos.
---

## Escalando más instancias en ms-productos

Para poder escalar el ms-productos a fin de tener más instancias (contenedores) simplemente ejecutamos el comando para
crear un nuevo contenedor. En nuestro caso, para ejemplificar el escalado del ms-productos haremos un ejemplo con el
contenedor de productos e items.

Como primer paso, levantamos los siguientes contenedores en esta secuencia:

- config-server
- eureka-server
- ms-mysql8
- 517e18ed49c5 <-- corresponde al contenedor del ms-items, como no le dimos un nombre usaremos su id de contenedor.
- 782591c6e54b <-- corresponde al contenedor del ms-productos, la única instancia que tenemos hasta este momento.
- [contenedor 2 para ms-productos]
- [contenedor 3 para ms-productos]
- [contenedor n para ms-productos]
- cf77cf2c850a <-- corresponde al contenedor del ms-zuul-server

Como observamos en la secuencia anterior, podemos levantar **n cantidades** de contenedores para productos, las que
quisiéramos, para eso simplemente por cada nueva instancia o contenedor requerida, ejecutamos el siguiente comando
para construir un nuevo contenedor **a partir de la misma imagen** del ms-productos:

````bash
$ docker container run -P --network ms-spring-cloud ms-productos:v1.0.0
````

**DONDE**

- Recordemos que el **-P** en mayúscula es porque nuestro ms-productos tiene asignado un puerto aleatorio

Nosotros crearemos dos contenedores más para tener 3 contenedores en total para el ms-productos. Finalmente, para poder
ver los contenedores ejecutándose, los listamos:

````bash
$ docker container ls

--- Resultado ---
CONTAINER ID   IMAGE                   COMMAND                  CREATED          STATUS          PORTS                                                                    NAMES
782591c6e54b   ms-productos:v1.0.0     "java -jar /ms-produ…"   30 minutes ago   Up 30 minutes                                                                            elastic_williams
6276bbaf4cc1   ms-productos:v1.0.0     "java -jar /ms-produ…"   44 minutes ago   Up 35 minutes                                                                            serene_visvesvaraya
517e18ed49c5   ms-items:v1.0.0         "java -jar /ms-items…"   4 hours ago      Up 35 minutes   0.0.0.0:8002->8002/tcp, 0.0.0.0:8005->8005/tcp, 0.0.0.0:8007->8007/tcp   interesting_villani
cf77cf2c850a   ms-zuul-server:v1.0.0   "java -jar /zuul-ser…"   21 hours ago     Up 34 minutes   0.0.0.0:8090->8090/tcp                                                   objective_pasteur
6dbaa787ec80   config-server:v1.0.0    "java -jar /config-s…"   24 hours ago     Up 38 minutes   0.0.0.0:8888->8888/tcp                                                   config-server
d2a91b629506   ms-productos:v1.0.0     "java -jar /ms-produ…"   28 hours ago     Up 35 minutes                                                                            sharp_boyd
21a90969a416   mysql:8                 "docker-entrypoint.s…"   2 days ago       Up 37 minutes   0.0.0.0:3306->3306/tcp, 33060/tcp                                        ms-mysql8
96081270d2b9   eureka-server:v1.0.0    "java -jar /eureka-s…"   2 days ago       Up 38 minutes   0.0.0.0:8761->8761/tcp                                                   eureka-server
````