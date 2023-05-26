# Microservicio Usuarios

## Usando Spring Data Rest para implementar automáticamente un CRUD de Usuarios

**[Fuente: Baeldung](https://www.baeldung.com/spring-data-rest-intro)**

En vez de crear un controlador con la clase service, lo podemos hacer con un componente de Spring
que lo que hace es exportar el Crud Repository a un endPoint en nuestra API REST **de forma automática**
sin tener que escribir ninguna línea de código del controlador, ni la clase service. Es otra forma,
mucho más rápido, más automatizada y es ideal para implementarlo junto con microservicios. Una de las
ventajas es que hay menos probabilidad de error.

En general, Spring Data REST se basa en el proyecto Spring Data y facilita la creación de servicios web REST basados en
hipermedia que se conectan a los repositorios de Spring Data, todos utilizando HAL como tipo de hipermedia de
conducción.

Elimina gran parte del trabajo manual generalmente asociado con tales tareas y hace que la implementación de la
funcionalidad básica de CRUD para aplicaciones web sea bastante simple.

Agregamos la dependencia de Spring Data Rest al pom.xml:

````
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-data-rest</artifactId>
</dependency>
````

## Personalizando el REST endPoint para usuarios

- Recordar que primero debemos tener nuestra clase de entidad (Usuario) con sus anotaciones de Spring Data JPA.
- Creamos una interfaz que herede de un repository existente, en nuestro de **PagingAndSortingRepository**.
- Agregamos las anotaciones de Spring Data Rest.

Nuestra interfaz quedaría de la siguiente forma:

````
@RepositoryRestResource(path = "usuarios")
public interface IUsuarioRepository extends PagingAndSortingRepository<Usuario, Long> {
    Optional<Usuario> findByUsername(String username);

    @Query(value = "SELECT u FROM Usuario AS u WHERE u.username = ?1")
    Optional<Usuario> obtenerPorUsername(String username);
}
````

**Donde:**

- La anotación **@RepositoryRestResource** es opcional y es usado para personalizar el REST endPoint.
  Si decidimos omitirlo, Spring automáticamente creará un endPoint usando el nombre de la entidad
  en plural, pero como en nuestro caso definimos el path en "usuarios", sería lo mismo a como si lo
  omitiéramos. Normalmente, se usa para definirle un endPoint distinto.
- **path = "usuarios"**, será el path personalizado que le daremos nuestro endPoint de Usuarios,
  es decir, es el endPoint a través del cual exportaremos todos los métodos del CRUD Repository: Listar,
  listar por id, crear, modificar, eliminar, etc.. y los métodos personalizados que tengamos en esta interfaz.
  Todo se exporta a nuestra ApiRest de forma automática. **Nota** el path no tiene que estar segmentado,
  es decir no debe ser compuesto, sino más bien debe definirse un path simple, sin utilizar el slash (/).

## Listando los usuarios

Como definimos el puerto en aleatorio para este ms-usuarios, accederemos a sus endpoints vía Spring Cloud Gateway,
por lo que, previamente configuraremos en el ms-spring-cloud-gateway el redireccionamiento a este ms-usuarios.

Para acceder a la lista de usuarios, sería a través de la siguiente url:

````
[GET] http://127.0.0.1:8090/api-base/usuarios-base/usuarios
````

**Donde:**

- **http://127.0.0.1:8090**, accedemos a través del ms-spring-cloud-gateway.
- **/api-base/usuarios-base/** configuración de la ruta base para el redireccionamiento al ms-usuarios.
- **usuarios**, el path definido en el @RepositoryRestResource(path = "usuarios")

Como resultado obtendremos el siguiente JSON:

````
{
    "_embedded": {
        "usuarios": [
            {
                "username": "martin",
                "password": "12345",
                "enabled": true,
                "nombre": "Martín",
                "apellido": "Díaz",
                "email": "martin@magadiflo.com",
                "roles": [],
                "_links": {
                    "self": {
                        "href": "http://host.docker.internal:54277/usuarios/1"
                    },
                    "usuario": {
                        "href": "http://host.docker.internal:54277/usuarios/1"
                    }
                }
            },
            {
                "username": "admin",
                "password": "12345",
                "enabled": true,
                "nombre": "Admin",
                "apellido": "Admin",
                "email": "admin@magadiflo.com",
                "roles": [],
                "_links": {
                    "self": {
                        "href": "http://host.docker.internal:54277/usuarios/2"
                    },
                    "usuario": {
                        "href": "http://host.docker.internal:54277/usuarios/2"
                    }
                }
            }
        ]
    },
    "_links": {
        "self": {
            "href": "http://host.docker.internal:54277/usuarios"
        },
        "profile": {
            "href": "http://host.docker.internal:54277/profile/usuarios"
        },
        "search": {
            "href": "http://host.docker.internal:54277/usuarios/search"
        }
    },
    "page": {
        "size": 20,
        "totalElements": 2,
        "totalPages": 1,
        "number": 0
    }
}
````

El resultado anterior muestra un JSON con una estructura propia del **Standard HATEOAS**
(HATEOAS es la abreviación de Hypermedia as engine of Application State - Hipermedia como
motor del estado de la aplicación). En resumen, es una restricción de la arquitectura Api REST,
en otras palabras cuando el servidor nos retorna el JSON, nos incluye información adicional
en forma de hipervínculos y otros recursos del api que le indica al cliente **¿cómo navegar?,
¿cómo encontrar la información?**

## Registrando un nuevo usuario

````
[POST] http://127.0.0.1:8090/api-base/usuarios-base/usuarios
````

Request Body:

````
{
    "username": "gaspar",
    "password": "12345",
    "enabled": true,
    "nombre": "Gaspar",
    "apellido": "Torres",
    "email": "gaspar@magadiflo.com",
    "roles": [
        {
            "id": 1,
            "nombre": "ROLE_USER"
        },
        {
            "id": 2,
            "nombre": "ROLE_ADMIN"
        }
    ]
}
````

Response Body:

````
{
    "username": "gaspar",
    "password": "12345",
    "enabled": true,
    "nombre": "Gaspar",
    "apellido": "Torres",
    "email": "gaspar@magadiflo.com",
    "roles": [
        {
            "nombre": "ROLE_USER"
        },
        {
            "nombre": "ROLE_ADMIN"
        }
    ],
    "_links": {
        "self": {
            "href": "http://host.docker.internal:55184/usuarios/3"
        },
        "usuario": {
            "href": "http://host.docker.internal:55184/usuarios/3"
        }
    }
}
````

**NOTA:** Observamos que no nos retorna el **id** del nuevo registro, más adelante
se realiza una configuración para que **sí nos retorne el id**.

## Accediendo a los métodos personalizados en los repositorios

En el repositorio **IUsuarioRepository** creamos dos métodos personalizados, aunque ambos hacen lo mismo.
Para poder acceder a ellos, tal como accedemos a los endPoints del CRUD de dicho repositorio, debemos
hacerlo usando en la url el **/search** seguido del nombre del método y agregando params si es que el
método lo requiere:

````
http://127.0.0.1:8090/api-base/usuarios-base/usuarios/search/findByUsername?username=magadiflo
````

**Donde**

- **search**, nos indica que a partir de aquí mostraremos nuestros métodos personalizados.
- **findByUsername**, nombre del método al cual queremos acceder vía endPoint
- **username**, parámetro que espera recibir nuestro método personalizado

## Personalizando los métodos agregados en los repositorios

Podemos **definirle un nombre a los métodos** para poder **acceder a ellos vía endPoint** y no usar
el nombre del método por defecto. Para eso debemos agregarle la anotación **@RestResource**
definiéndole el nombre por el cual accederemos, además podemos darle un nombre distinto
al parámetro con **@Param**:

````
@RestResource(path = "buscar-usuario")
Optional<Usuario> findByUsername(@Param(value = "usuario") String username);
````

````
http://127.0.0.1:8090/api-base/usuarios-base/usuarios/search/buscar-usuario?usuario=martin
````

## Configurar y exponer id en la respuesta json

Debemos agregar una clase de configuración que implemente la interfaz **RepositoryRestConfigurer**,
que es una interfaz que permite agregar configuraciones a todo lo relacionado con Spring Data Rest.

Sobre escribir el método para indicarle qué clases queremos que muestren sus ids, en nuestro caso
le decimos que tanto la clase Usuario como Rol.

````
@Configuration
public class RepositoryConfig implements RepositoryRestConfigurer {
    @Override
    public void configureRepositoryRestConfiguration(RepositoryRestConfiguration config, CorsRegistry cors) {
        config.exposeIdsFor(Usuario.class, Rol.class);
    }
}
````

---

## Usando nuestra librería ms-usuarios-commons

Al igual que se trabajó con la librería ms-commons en los ms-productos y ms-items, para nuestro ms-usuarios
creamos una librería llamada ms-usuarios-commons que tiene como dependencia Spring Data Jpa y nuestras clases de
Entities Usuario y Rol. Esta librería será configurada de la misma manera como la configuramos la librería ms-commons.

En este microservicio ms-items necesitamos agregar en el pom.xml la dependencia del ms-usuarios-commons, borrar
las entities Usuario y Rol del ms-usuarios, ya que ahora los importaremos de nuestra librería agregada. Finlamente,
le decimos que use la anotación @EntityScan para que escanee del paquete de nuestra
librería las entidades Usuario y Rol.

Estas configuraciones ya no las detallo porque ya se hizo cuando se trabajó la
librería ms-commons.
---

## Instalando PostgreSQL y creando la Base de Datos en pgAdmin

En mi caso ya tengo instalado PostgreSQL, solo creé una base de datos llamada: bd_spring_boot_cloud.
Ahora, en el **ms-usuarios** es importante agregar el driver de conexión de postgresql:

````
<dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
    <scope>runtime</scope>
</dependency>
````

---

## Configurando PostgreSQL en Servidor de Configuración

En el repositorio del servidor de configuración creamos un archivo de propiedad para este microservicio con el perfil
de development: **ms-usuarios-development.properties**.

A dicho archivo le agregaremos las configuraciones para conectarnos a PostgreSQL, etc.

````
# Datasource POSTGRESQL
spring.datasource.url=jdbc:postgresql://localhost:5432/bd_spring_boot_cloud
spring.datasource.username=postgres
spring.datasource.password=magadiflo
spring.datasource.driver-class-name=org.postgresql.Driver

# Configurando dialecto de POSTGRESQL
spring.jpa.database-platform=org.hibernate.dialect.PostgreSQL95Dialect

# Generando tablas: create, crea el esquema y destruye cualquier dato previo
spring.jpa.hibernate.ddl-auto=create

# Vista y formato en el log
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true

# Para evitar que lance un error (no es grave, pero sí molesto) cuando se corre postgresql con spring boot
spring.jpa.properties.hibernate.jdbc.lob.non_contextual_creation=true
````

En el ms-usuarios, agregamos la dependencia de Spring Config:

````
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-config</artifactId>
</dependency>
````

En el application.properties de este microservicio agregamos la configuración para conectarnos al servidor de
configuraciones:

````
# Configurando conexion a servidor de configuraciones
spring.config.import=optional:configserver:http://localhost:8888
spring.profiles.active=development
````

---

# Sección 14: Desplegando Microservicios en Contenedores Docker

---

## Creando Dockerfile para servicio usuarios, build y run

Copiamos el mismo Dockerfile de productos y le realizamos las modificaciones correspondientes. Recordar que en el
ms-usuarios definimos el puerto como dinámico, por lo tanto, en el Dockerfile no colocaremos el **EXPOSE**:

````Dockerfile
FROM openjdk:17-jdk-alpine
VOLUME /tmp
ADD ./target/ms-usuarios-0.0.1-SNAPSHOT.jar ms-usuarios.jar
ENTRYPOINT ["java", "-jar", "/ms-usuarios.jar"]
````

Ahora, como nuestro **ms-usuarios** está usando la librería que creamos **ms-usuarios-commons**, necesitamos instalarla
dentro del repositorio local de maven. Para eso nos posicionamos mediante cmd en la raíz de nuestra librería
**ms-usuarios-commons** y ejecutamos:

````
mvnw.cmd clean install
````

Como siguiente paso, es crear nuestro .jar de nuestro ms-usuarios, para eso nos posicionamos en la raíz de dicho
microservicio, ejecutamos:

````
mvnw.cmd clean package -DskipTests
````

Con nuestro .jar construido y con el Dockerfile configurado, es momento de crear nuestra imagen ejecutando el
siguiente comando:

````
docker build -t ms-usuarios:v1.0.0 .
````

Listamos las imágenes para ver que ya tengamos el nuestro:

````
docker image ls

--- Resultado ---
REPOSITORY       TAG         IMAGE ID       CREATED              SIZE
ms-usuarios      v1.0.0      b43925444814   About a minute ago   391MB
ms-zuul-server   v1.0.0      59bca3de6caf   15 hours ago         160MB
ms-productos     v1.0.0      e82d48f34573   22 hours ago         391MB
mysql            8           05db07cd74c0   42 hours ago         565MB
eureka-server    v1.0.0      f3caf1354f57   42 hours ago         372MB
config-server    v1.0.0      36bca5b29011   47 hours ago         362MB
postgres         12-alpine   945704f99920   6 days ago           230MB
````

Ahora, antes de crear el contenedor nos debemos asegurar que los siguientes contenedores estén ejecutándose:

- config-server
- eureka-server
- ms-postgres12

a partir de la imagen anterior, crearemos nuestro contenedor:

````
docker container run -P --network ms-spring-cloud ms-usuarios:v1.0.0
````

**NOTA**

1. Observamos que colocamos la bandera **-P** en mayúscula, y es porque definimos en el ms-usuarios el puerto en
   aleatorio.
2. Como observamos, en el comando anterior no colocamos el **--name,** ya que para este contenedor
   **nombre es opcional**, y eso es porque en ninguna parte haremos referencia del ms-usuarios en otros microservicios a
   diferencia de lo que pasa con eureka o config server donde necesitamos apuntar a sus endpoints, rutas, entonces para
   poder conectarnos a esos servidores necesitamos **sí o sí** el nombre de cada contenedor.
