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
