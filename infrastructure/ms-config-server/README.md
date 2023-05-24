# Microservicio Config Server

Para definir este microservicio como un servidor de configuraciones debemos anotar la clase principal con

````
@EnableConfigServer
````

En el application.properties agregamos la configuración del **Repositorio Local**

````
spring.cloud.config.server.git.uri=file:///M:/PROGRAMACION/DESARROLLO_JAVA_SPRING/INTELLIJ_IDEA/01.udemy/02.udemy_andres_guzman/04.repaso/config-server-repo
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

## Viendo configuraciones desde el ms-config-server

Por cada microservicio podemos ver sus archivos .properties.

A continuación, veremos el archivo .properties que creamos en el servidor de configuraciones,
para ser más exactos en nuestro repositorio local, veremos el archivo **ms-items.properties**
a través de la siguiente url:

````
http://127.0.0.1:8888/ms-items/default
````

**Donde**

- **8888**, es el puerto que le definimos al ms-config-server.
- **ms-items**, el nombre que le pusimos al archivo properties.
- **default**, hace referencia al ambiente (dev, prod, etc.), pero como nosotros
  solo escribirmos a secas **ms-items.properties** y no, por ejemplo un **ms-items-dev.properties**,
  por defecto si el archivo no tiene agregado un ambiente el valor será default.

## Conectando a repositorio remoto

Creamos un proyecto en GitHub para subir nuestras configuraciones. Enlazamos nuestro repositorio local al remoto.

En el application.properties agregamos la configuración con la url de nuestro repositorio remoto:

````
spring.cloud.config.server.git.uri=https://github.com/magadiflo/config-server-repo.git
````

---

## Creando configuración de OAuth en el Servidor de Configuración

Como en el **ms-zuul-server** y **ms-authorization-server** usamos casi las mismas configuraciones,
necesitamos crear un **application.properties** en el **repositorio del servidor de configuraciones**
para que esté disponible de manera compartida para ambos microservicios o aquel
microservicio que requiera dichas configuraciones.

Creamos el archivo **application.properties** con las siguientes configuraciones personalizadas,
las guardamos y pusheamos al repositorio remoto:

````
# Para autenticarnos para generar el token (credenciales de la App Client)
config.security.oauth.client.id=frontendApp
config.security.oauth.client.secret=frontendApp-12345

# llave para firmar el token
config.security.oauth.jwt.key=mi-clave-secreta-12345
````

---

## Configurando repositorio remoto GitHub como privado

Para proteger nuestro repositorio que contiene los archivos de configuración, **lo haremos privado**. De tal forma que
cuando se solicite algún archivo de configuración el servidor de configuraciones proporcione las credenciales correctas.

### Haciendo privado el repositorio de configuraciones

- Vamos al repositorio **config-server-repo**.
- Vamos a la pestaña de Settings.
- Nos dirigimos hacia abajo **Danger Zone**.
- Cambiamos la visibilidad
- Al finalizar el cambio veremos **un candadito** al costado del nombre del repositorio.

### Creando un Personal Access Token

En el curso de Andrés Guzmán se usa las credenciales del usuario de GitHub. Actualmente, GitHub ya no permite esa
opción, es decir, el usar las credenciales propias del usuario de GitHub. En cambio, se puede generar un **Personal
Access Token** más el nombre del usuario para que esa funcionalidad pueda darse.

- Click en la imagen del perfil.
- Click en settings.
- Click en Developer Settings
- Click en Personal Access Token
- Click en Tokens (classic)
- Generate new token
    - Note: servidor de configuraciones <------ cualquier nota
    - Expiration: 30 days <---- escogemos un tiempo de expiración del token
    - Select scopes: [check] repo <---- solo esa opción
- Generate Token
- Copiamos el token y no lo perdemos ya que no se volverá a mostrar

### Configurando credenciales al repositorio remoto privado

Agregaremos la siguiente configuración en el **application.properties**:

````
spring.cloud.config.server.git.username=magadiflo
spring.cloud.config.server.git.password=${REPO_CONFIG_PASS}
````

**NOTA:**

Agregué una variable de ambiente **REPO_CONFIG_PASS**, para que a través de él podamos agregar el Personal Access Token
generado en el apartado anterior.

**IMPORTANTE**
> Es importante que no guardemos el Personal Access Token en el repositorio de GitHub, es decir, que no hagamos
> un push del archivo que contenga textualmente el token, ya que **si lo hacemos, GitHub lo va a detectar y lo eliminará
> de inmediato**.

### Cómo usar la variable de ambiente creada

Existen distintas maneras de poder usarlas. Aquí explicaré dos formas:

1. **Usando IntelliJ IDEA**, en el apartado de ejecución de los proyectos, vamos a **Edit...** de nuestro
   **ms-config-server**. Luego en el apartado de **Environment variables** agregamos nuestra variable y su valor
   de la siguiente manera:

````
REPO_CONFIG_PASS=ghp_0aVwJa7FWLS2HIaAPGwhr.........
````

2. **Usando cmd**, para poder ejecutar un jar conteniendo la aplicación de **ms-config-server**. Teniendo el .jar
   generado, procedemos a ejecutarlo agregando en la misma línea la variable de ambiente:

````
java -jar .\target\ms-config-server-0.0.1-SNAPSHOT.jar --REPO_CONFIG_PASS=ghp_0aVwJa7FWLS2HIaAPGwhr.........
````

---

# Sección 14: Desplegando Microservicios en Contenedores Docker

---

## Creando archivo Dockerfile para server config(Servidor de Configuración)

Para poder generar nuestra imagen de nuestro **ms-config-server**, es necesario tener el **.jar** generado previamente,
ya que será usado dentro del **Dockerfile**. Para generar el **.jar**, vamos a la raíz de ese microservicio y
ejecutamos:

````
mvnw.cmd clean package
````

Terminada la construcción de nuestro .jar, se creará una carpeta **/target** conteniéndolo:
**ms-config-server-0.0.1-SNAPSHOT.jar**

### Construcción del Dockerfile

En la raíz del **ms-config-server** creamos un archivo sin extensión llamado: **Dockerfile** quien contendrá toda
la instrucción para poder generar una imagen de nuestro microservicio.

````Dockerfile
FROM openjdk:17-jdk-alpine
VOLUME /tmp
EXPOSE 8888
ADD ./target/ms-config-server-0.0.1-SNAPSHOT.jar config-server.jar
ENTRYPOINT ["java", "-jar", "/config-server.jar"]
````

**DONDE**

- **FROM**, indica que la imagen que crearemos tendrá como base otra imagen ya existente
- **openjdk:17-jdk-alpine**, imagen que contiene la versión de java 17. **(imagen: openjdk, tag: 17-jdk-alpine)**.
- **VOLUME**, para montar un volumen.
- **/tmp**, algunas aplicaciones de Spring la requieren, porque, por defecto **Tomcat guarda los logs** en ese
  directorio. Ahora, como nuestras aplicaciones de microservicios son simples y el **log** lo maneja en consola (no lo
  guarda en archivos), entonces ese directorio **/tmp** no sería necesario, es opcional, pero de todas formas lo
  dejamos configurado.
- **EXPOSE**, escribimos el puerto que vamos a exponer. Recordemos que el puerto que le definimos a este microservicio
  fue el **8888**. **¡NOTA IMPORTANTE!**, este EXPOSE no es que vaya a mapear el contenedor a este puerto, sino que
  solo **sirve para documentar**, para que otros desarrolladores sepan que los contenedores generados de esta imagen se
  va a publicar o exponer en el puerto allí definido, en este caso, puerto 8888.
- **ADD**, para agregar o copiar, en este caso un archivo (nuestro jar) a nuestra imagen. Recordar que anteriormente
  generamos el **.jar** de **ms-config-server** y esta está en la carpeta
  **/target/ms-config-server-0.0.1-SNAPSHOT.jar**, lo copiaremos en la **raíz de nuestra imagen**, tal cual o podemos
  cambiarle el nombre, para hacerlo más sencillo **config-server.jar**
- **ENTRYPOINT ["java", "-jar", "/config-server.jar"]**, ejecuta o levantar nuestra aplicación cuando se inicia el
  contenedor. En nuestro caso, estamos agregando los comandos para ejecutar nuestro .jar para levantar nuestra
  aplicación de Spring Boot.