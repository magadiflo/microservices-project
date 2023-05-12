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