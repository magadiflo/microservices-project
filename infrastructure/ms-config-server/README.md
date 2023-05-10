# Microservicio Config Server

Para definir este microservicio como un servidor de configuraciones debemos anotar la clase principal con

````
@EnableConfigServer
````

En el application.properties agregamos la configuración del **Repositorio Local**

````
spring.cloud.config.server.git.uri=file:///M:/PROGRAMACION/DESARROLLO_JAVA_SPRING/INTELLIJ_IDEA/01.udemy/02.udemy_andres_guzman/04.repaso/microservices-project/config-server-repo
````

**Donde**

- /// se debe agregar si se está trabajando en windows y si la ruta del archivo es absoluta
  con un prefijo de unidad, si es Linux o Mac es uno /
- La ruta proporcionada corresponde a la ruta donde estará nuestro repositorio local, en nuestro caso
  debemos crear un directorio llamado **config-server-repo** donde alojaremos localmente nuestro repositorio.

## Creación del repositorio local e inicializando git

Una vez creado nuestro directorio **(/config-server-repo)** que será nuestro repositorio local, debemos inicializarlo
para que git inicie el seguimiento:

````
git init
````

- Creamos un archivo para el microservicio items definiéndole un valor al puerto, por ejemplo 8005.
- Es importante que el archivo a crear tenga el mismo nombre que el microservicio al que queremos
  establecerle sus configuraciones desde el servidor de configuraciones:

````
ms-items -> ms-items.properties
````

## Prioridad en archivos .properties entre el Servidor de configuraciones y un proyecto de microservicio

**La configuración que tengamos en el servidor de configuraciones sobreescribe a las configuraciones originales**
(las que tenemos en el mismo proyecto) y si hay configuración que no existe, se va a anexar o combinar,
tanto del application.properties de cada proyecto como la personalizada que tengamos en el servidor de configuración,
ambas configuraciones se van a unir y las que existan se van a sobreescribir.

````
Ejemplo. 
En el servidor de configuraciones tenemos configurado el puerto para el ms-items en 8005,
mientras que en el proyecto mismo del ms-items lo tenemos configurado en 8002. Cuando
arranque la aplicación, el puerto que tomará será el del servidor de configuraciones.
````
