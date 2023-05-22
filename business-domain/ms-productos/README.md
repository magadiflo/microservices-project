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

````
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

````
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

````
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

````
<dependency>
    <groupId>com.mysql</groupId>
    <artifactId>mysql-connector-j</artifactId>
    <scope>runtime</scope>
</dependency>
````

Agregamos configuraciones de Jpa y conexión a MySQL y al **application.properties**:

````
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

````
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-config</artifactId>
</dependency>
````

También teníamos configurada en el **application.properties** la dirección que apunta al servidor de configuraciones,
y esto fue porque, cuando en capítulos pasados agregamos la dependencia de spring cloud config, nuestro ms-productos,
requería que se le agregue dicha configuración.

Ahora, en este punto del curso se agregan la configuración que ya teníamos anteriormente más el perfil seleccionado:

````
# Configurando url al servidor de configuraciones
spring.config.import=optional:configserver:http://localhost:8888
spring.profiles.active=development
````

**NOTA**

Recordar que el perfil seleccionado: **development**, corresponde con el archivo que creamos en el repositorio del
servidor de configuraciones **ms-productos-development.properties** y es en donde colocamos las configuraciones
de JPA y conexión a MySQL que se encontraban en el **application.properties** de este microservicio.