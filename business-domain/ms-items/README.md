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

## Trabajando con Resilience4j - método alternativo [Forma programática]

Similar a cómo trabajábamos con Hystrix dándole al método un camino alternativo, aquí también
hacemos lo mismo, pero usando **Resilience4j, expresiones lambda y de forma programática**.

El código sería como se muestra a continuación:

````
# .create("items"), donde "items" es el identificador que le damos a este Circuit Breaker

# Método que usa el CircuitBreaker
@GetMapping(path = "/producto/{productoId}/cantidad/{cantidad}")
public ResponseEntity<Item> getItem(@PathVariable Long productoId, @PathVariable Integer cantidad) {
    return circuitBreakerFactory.create("items")
            .run(() -> ResponseEntity.ok(this.itemService.findByProductId(productoId, cantidad)),
                    e -> this.metodoAlternativo(productoId, cantidad));
}

# Método alternativo a espera de algún fallo para ejecutarse
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
