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

**SEGUNDA SOLUCIÓN**

Como este proyecto es de librería, no necesitamos ninguna configuración de un string de conexión a BD, solo necesitamos
las dependencias de Spring Data JPA, pero no conectarnos. Por lo tanto, **eliminaremos la dependencia de H2** y en
**la clase principal deshabilitamos la Autoconfiguración del DataSource**:

````
@SpringBootApplication
@EnableAutoConfiguration(exclude = {DataSourceAutoConfiguration.class})
public class MsCommonsApplication {

}
````

## Generando el .jar usando maven

En el curso, Andrés Guzmán genera el .jar del ms-commons usando la raíz de dicho microservicio
y ejecutando el siguiente comando:

````
mvnw.cmd install
````

De esa manera, se generará el .jar del ms-commons y además se colocará el .jar en nuestro
repositorio local de maven.

**¡NOTA!**

En mi caso, no ejecuté ese comando para generar el .jar manualmente, sino más bien, tan solo
con la incorporación de la dependencia del proyecto commons en los ms-items y ms-productos,
en automático al ejecutar dichos microservicios, y estos al tener la dependencia de ms-commons,
en automático se genera el .jar, **NO TENGO QUE HACERLO YO**.