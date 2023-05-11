# Microservicio Commons

Como crearemos un **proyecto de librería**, es decir no es una aplicación como tal, necesitamos
hacer algunas configuraciones:

En el pom.xml quitamos la dependencia maven plugin:

````
<build>
    <plugins>
        <plugin>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-maven-plugin</artifactId>
        </plugin>
    </plugins>
</build>
````

En la clase principal, eliminamos el método main (que contiene el arranque
de la aplicación):

````
public static void main(String[] args) {
    SpringApplication.run(MsCommonsApplication.class, args);
}
````

## Sobre las dependencias a usar

Como vamos a crear una librería que tendrá una clase compartida Producto, esta será
tanto una Entity como un Pojo. Para la entity, usaremos las anotaciones de
Spring Data JPA, mientras que para el Pojo no necesitamos de ninguna anotación.

Ahora, como usaremos la dependencia de Spring Data JPA:

````
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>
````

Por defecto, Spring nos obliga a tener una conexión a base de datos ya que
lo autoconfigura por defecto, y si no tenemos ningún driver de conexión a BD,
nos lanzará un error.

**PRIMERA SOLUCIÓN**

Para solucionar ese inconveniente, agregaremos la dependencia
de H2 (se autoconfigura de forma automática), para que momentáneamente tenga una BD
aunque no la usaremos aquí, solo es para que no nos arroje el error de autconfiguración:

````
<dependency>
    <groupId>com.h2database</groupId>
    <artifactId>h2</artifactId>
    <scope>runtime</scope>
</dependency>
````