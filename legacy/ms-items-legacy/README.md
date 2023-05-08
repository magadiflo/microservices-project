# Microservicio items

# [Diferencia entre las etiquetas del pom.xml dependencyManagement y dependencies](https://www.baeldung.com/maven-dependencymanagement-vs-dependencies-tags)

<b>FUENTE: [Baeldung](https://www.baeldung.com/maven-dependencymanagement-vs-dependencies-tags)
y [StackOverflow](https://stackoverflow.com/questions/2619598/differences-between-dependencymanagement-and-dependencies-in-maven)</b>

- Las dependencias que son especificadas dentro de las etiquetas **dependencies**,
  serán dependencias que siempre se agregarán al proyecto.


- Las dependencias agregadas dentro de las etiquetas **dependencyManagement**,
  son dependencias que solo están declaradas, y realmente no agrega una dependencia. Las
  dependencias declaradas en esta sección deben ser utilizadas posteriormente por la
  etiqueta de **dependencies**. Es solo la etiqueta de **dependencies** la que
  hace que ocurra una dependencia real, es decir la que agrega la dependencia al proyecto.


- Generalmente, las etiquetas **dependencyManagement** se utilizan cuando se está
  trabajando con módulos de maven y lo usual es declararla en el **pom.xml padre o raíz** o
  en un **pom.xml** que sea común a varios módulos, lo que simplifica las referencias
  en los archivos pom secundarios.


- Agregando algo más al **dependencyManagement**, las dependencias definidas en esas etiquetas
  solo se incluirán en el módulo secundario (suponiendo que trabajamos con multi-módulos) si
  también se especificaron en las etiquetas **dependencies** del propio módulo secundario.
  Esto es bueno porque podemos especificar la **versión y/o alcance** en el padre y podemos
  omitirlos al especificar las dependencias en el pom secundario. Esto puede ayudar a usar
  versiones unificadas para las dependencias de los módulos secundarios, sin especificar
  la versión en cada módulo secundario.

## Para trabajar con el balanceador de carga Ribbon

- Para usar la dependencia de ribbon debemos bajar la verión actual de spring boot a la versión 2.3.12.RELEASE.
  La versión 2.4 en adelante no es compatible con ribbon, en esas versiones ya se usa **Spring Cloud Load Balancer**,
  pero solo para efectos de usar ribbon como balanceo de carga sin EUREKA es necesario cambiar las versiones.
- También se cambia la versión de Spring-cloud a Hoxton.SR12
- Es importante también cambiar la versión de java a la versión 1.8. Estuve realizando el repaso del curso con java 17
  en IntelliJ IDEA y da errores con esta versión de Ribbon.
- Luego agregamos en el pom.xml la dependencia de Ribbon
- Configurar ribbon para trabajar con dos instancias, es decir dos direcciones donde estarán alojados nuestros servicios
  productos (ver application.properties).

---

# Quitando dependencia balanceador de carga

Quitamos la dependencia de **spring-cloud-starter-netflix-ribbon** porque como agregamos la
dependencia de **spring-cloud-starter-netflix-eureka-client**, éste último ya lo trae incorporado.
Ojo, que a esta altura del curso bajamos la versión de Spring a **2.3.12.RELEASE** por lo que estamos
trabajando con ribbon.

## Hystrix: Para trabajar con tolerancia a fallos

- Al igual que sucede con ribbon, **hystrix** es compatible hasta la versión de Spring Boot **2.3.12.RELEASE**.
  Versiones posteriores se usa **Resilience4j**.
- Luego de agregar la dependencia de Hystrix al pom.xml, la debemos habilitar en la clase principal
  usando la siguiente anotación **@EnableCircuitBreaker**.
- Hystrix, envuelve a **ribbon** para la tolerancia a fallos, manejo de latencia y timeout. Recordar que si bien ribbon
  no está como dependencia explícita, en realidad cuando agregamos la dependencia de **eureka client**, ésta ya lo
  trae internamente.

## Trabajando con Hystrix. Ejmplo camino alternativo

- Nuestro microservicio item, consume el microservicio de productos.
- Cuando ms-item llame con FeignClient (o RestTemplate) al método **verProducto** del ms-productos, recibirá un
  error, ya que intencionalmente modificamos el código para que lance el error:

````
@GetMapping(path = "/{id}")
public ResponseEntity<Producto> verProducto(@PathVariable Long id) {
    Producto producto = this.productoService.findById(id);
    boolean thereIsAnError = false;
    if (thereIsAnError) {
        throw new RuntimeException("No se pudo cargar el producto!"); <----- Lanzará el error
    }
    return ResponseEntity.ok(this.productoConPuerto(producto));
}
````

- En el método handler del ms-item, agregamos la anotacón **@HystrixCommand(fallbackMethod = "metodoAlternativo")**
  indicando el método alternativo a llamar cuando ocurra un error en la comunicación
  del método actual.

````
@HystrixCommand(fallbackMethod = "metodoAlternativo")
@GetMapping(path = "/producto/{productoId}/cantidad/{cantidad}")
public ResponseEntity<Item> getItem(@PathVariable Long productoId, @PathVariable Integer cantidad) {
    return ResponseEntity.ok(this.itemService.findByProductId(productoId, cantidad));
}
````

- El método alternativo al que se llamará cuando ocurra un error,
  debe ser exactamente igual al método donde se agregó la anotación **@HystrixCommand(...)**:

````
public ResponseEntity<Item> metodoAlternativo(Long productoId, Integer cantidad) {
    Producto producto = new Producto();
    producto.setId(productoId);
    producto.setNombre("Cámara Sony");
    producto.setPrecio(500D);

    Item item = new Item();
    item.setCantidad(cantidad);
    item.setProducto(producto);

    return ResponseEntity.ok(item);
}
````

## Hystrix: configuración cuando ocurre un TimeOut

- Nuestro microservicio items consume el microservicio de productos.
- Cuando items llame con FeignClient (o RestTemplate) a algún método del microservicio productos,
  simularemos una demora en el procesamiento de la petición, para poder experimentar el **TimeOut**.
  Por defecto, el timeout permitido por Hystrix es de 1 segundo. Para ese ejemplo, agregamos una
  demora de 2 segundos en el método **verProducto(...) del microservicio productos**.

````
@GetMapping(path = "/{id}")
public ResponseEntity<Producto> verProducto(@PathVariable Long id) {
    Producto producto = this.productoService.findById(id);
    try {
        Thread.sleep(2000L); <----------------- Aplicando demora de 2 segundos
    } catch (InterruptedException e) {
        throw new RuntimeException(e);
    }
    return ResponseEntity.ok(this.productoConPuerto(producto));
}
````

- Debemos configurar el microservicio items (quien es el que tiene a Hystrix), de tal modo que
  cuando se experimente la demora de 2 segundos no lance el TimeOut y siga esperando. Esto es
  importante en situaciones donde, por ejemplo, hay mucha demora en la respuesta porque se está
  enviando un archivo grande a procesar o el método al que llamamos tiene muchos subprocesos, etc..
  entonces, por defecto Hystrix espera 1 segundo, pasado ese tiempo **lanza el TimeOut** y si está
  configurado un método alternativo, retorna dicho método alternativo y ya no el error producto del
  TimeOut, pero nosotros sabemos que se demorará más del tiempo permitido, así que podemos configurar el tiempo por
  defecto para que hystrix lo amplie, de esa manera Hystrix no lance el TimeOut o redireccione al método alternativo.
- Como hystrix envuelve a ribbon, lo ideal es que hystrix tenga un tiempo de timeout superior a ribbon.
- Por lo tanto, en la siguiente configuración hystrix = 20000, Ribbon = 3000 + 10000 => hystrix (20000) > ribbon (13000)

````
hystrix.command.default.execution.isolation.thread.timeoutInMilliseconds: 20000
ribbon.ConnectTimeout: 3000
ribbon.ReadTimeout: 10000
````