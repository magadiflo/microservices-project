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

---

## Construyendo imagen Docker de server config y levantando el contenedor

Mediante el cmd nos posicionamos en la **raíz del ms-config-server**. En esa raíz debemos tener el Dockerfile trabajado
anteriormente. Ahora ejecutamos el siguiente comando:

````
docker build -t config-server:v1.0.0 .
````

**DONDE**:

- **docker build**, comando de docker para **construir una imagen**.
- **-t**, es una bandera que nos permite definir un **tag name**. En nuestro caso, el nombre que le daremos a nuestra
  imagen será **config-server** y el tag que le daremos será **v1.0.0**.
- **.**, el punto al final de la instrucción indica dónde buscar el archivo Dockerfile en el directorio actual. Recordar
  que nuestro directorio actual es la raíz del ms-config-server en el que estamos posicionados mediante cmd, y
  precisamente allí está nuestro Dockerfile, por lo tanto, le estamos diciendo que busque nuestro Dockerfile en esa
  raíz.

Finalizado la construcción ejecutamos el siguiente comando para verificar que efectivamente sí se ha construido:

````
docker image ls
````

Resultado:

````
REPOSITORY      TAG       IMAGE ID       CREATED         SIZE
config-server   v1.0.0    36bca5b29011   9 minutes ago   362MB
````

Creamos una red llamada **ms-spring-cloud** donde agregaremos a todos nuestros microservicios, de tal forma que
puedan comunicarse entre sí, puesto que estarán en la misma red:

````
 docker network create ms-spring-cloud
````

### Creando un contenedor a partir de nuestra imagen dockerizada

Una vez dockerizada nuestro contenedor y teniendo la red a donde nos conectaremos, ejecutaremos el siguiente comando
para poder construir nuestro primer contenedor:

````
docker container run -p 8888:8888 --name config-server --network ms-spring-cloud -e REPO_CONFIG_PASS=ghp_mdNs6eo0jlO757... config-server:v1.0.0
````

**DONDE**:

- **docker container run**, comando de docker para **ejecutar un contenedor**.
- **-p**, nos indica el puerto. Tiene dos partes, el puerto externo e interno. El **externo** nos indica el puerto que
  será visible desde nuestra pc local hacia el contenedor. El **interno** será el puerto que se maneja dentro del
  contenedor. Ahora, en nuestro ejemplo, pusimos **8888:8888** referenciando al puerto **externo:interno**
  respectivamente.
- **--name**, nos permite darle un nombre a nuestro contenedor, en nuestro caso le pusimos **config-server**.
- **--network**, le decimos que el contenedor estará **dentro de la red ms-spring-cloud** que creamos anteriormente.
- **-e**, nos permite agregar **variables de ambiente**. Recordar que nosotros agregamos una variable de ambiente
  en este microservicio, precisamente en el application.properties para poder leer el personal access token que creamos,
  ya que hicimos nuestro repositorio de configuraciones privado. El **REPO_CONFIG_PASS=ghp_mdNs6eo0jlO757...**
  corresponde al nombre de la variable de ambiente igualado a su valor. En este caso, no está completo el Personal
  Access Token por eso agregamos los puntos, para evitar tener en esta documentación el token completo.
- **config-server:v1.0.0**, nombre de la imagen (config-server) junto a su tag(v1.0.0), a partir del cual generaremos
  el contenedor. Siempre va al final.

**IMPORTANTE**:

- Al ejecutar el comando anterior, el contenedor empezará a ejecutarse y por consiguiente nuestra aplicación de spring
  boot, mostrándose en el cmd el log de nuestra aplicación de Spring Boot.
- Nuestra aplicación ha tomado el control del cmd donde ejecutamos el comando anterior, es por eso que vemos el log.
  Ahora, si solo queremos correr el contenedor sin ver el log de la aplicación, debimos agregar al comando anterior la
  siguiente bandera **-d**, con el que le indicamos que corra el contenedor desenlazado (detached) de la consola donde
  se ejecutó el comando.
- Si queremos listar nuestro contendor en docker ejecutamos:

````
docker container ls -a

---Resultado---                                                                                                           
CONTAINER ID   IMAGE                  COMMAND                  CREATED          STATUS          PORTS                    NAMES        
b76f7a421819   config-server:v1.0.0   "java -jar /config-s…"   22 minutes ago   Up 22 minutes   0.0.0.0:8888->8888/tcp   config-server
````

Ahora podemos acceder a nuestro contenedor desde nuestra pc local a través del puerto externo expuesto:

````
http://localhost:8888/ms-items/default
````

Resultado:

````
{
    "name": "ms-items",
    "profiles": [
        "default"
    ],
    "label": null,
    "version": "5dbf46accb0c710fc039592b4c42b396a2c46ddb",
    "state": null,
    "propertySources": [
        {
            "name": "https://github.com/magadiflo/config-server-repo.git/ms-items.properties",
            "source": {
                "server.port": "8005",
                "configuracion.texto": "ConfiguraciÃ³n personalizada para el ambiente POR DEFECTO"
            }
        },
        {
            "name": "https://github.com/magadiflo/config-server-repo.git/application.properties",
            "source": {
                "config.security.oauth.client.id": "frontendApp",
                "config.security.oauth.client.secret": "frontendApp-12345",
                "config.security.oauth.jwt.key": "mi-clave-secreta-12345"
            }
        }
    ]
}
````

**CONCLUSIÓN**

> Nuestro contendor está ejecutándose correctamente, y obviamente nuestro **ms-config-server** que está dentro
> de dicho contenedor.