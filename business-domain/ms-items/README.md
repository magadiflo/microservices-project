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

## Probando Resilience con criterios por defecto

Para probar las configuraciones por defecto de Resilience, debemos hacer
una pequeña modificación al método que desde este ms-items se llama. Ese método
es verProducto(...) y está en el ms-productos.

Agregamos una pequeña validación, de tal forma que cuando se llame
por un producto **id=10** lance una excepción, y si se llama a un producto
con **id=7** se aplique un tiempo de demora de 5 segundos.

````
# En el ms-productos

@GetMapping(path = "/{id}")
public ResponseEntity<Producto> verProducto(@PathVariable Long id) throws InterruptedException {
    
    //* Simulando errores (Usuario no encontrado y demora en la ejecución del método)
    if(id.equals(10L)) throw new IllegalStateException("Producto no encontrado!");
    if(id.equals(7L)) TimeUnit.SECONDS.sleep(5L);
    //* Simulando errores
    
    Producto producto = this.productoService.findById(id);
    return ResponseEntity.ok(this.productoConPuerto(producto));
}
````

Las configuraciones por defecto son:

- Tiene una ventana deslizante de 100 request (100%)
- Tasa de errores (+51%)

**FUNCIONAMIENTO:**

Hacemos 100 request, haciendo fallar más del 50%. Ejm. 60 request,
enviando el id del producto a 10.

````
http://127.0.0.1:8002/api/v1/items/producto/10/cantidad/3
````

Ahora, como el método getItem(...) tiene asignado programáticamente un método alternativo,
en cada falla mostrará dicho método alternativo. Ahora, si 60 request fallaron,
los 40 restantes hacemos que sean exitosos enviando un id del producto igual a 1, por ejemplo.

````
http://127.0.0.1:8002/api/v1/items/producto/1/cantidad/3
````

Al completarse los 100 request, el siguiente, fallará, así sea que el
id del producto sea válido o un request con todas las de ser exitoso,
mostrará el método alternativo, iniciando el Circuit Breaker, es decir,
pasará a un estado de **OPEN**.

````
CircuitBreaker 'items' is OPEN and does not permit further calls
````

**Resilience** espera un minuto para pasar a un estado de **semi-abierto**.
Habiendo pasado el minuto (estando ya en el estado semi-abierto), la
ventana deslizante ahora tiene un valor de 10 request (100%). Si volvemos
a fallar más del 50% de request, por ejemplo 8 request, en cada falla
seguirá mostrando el camino alternativo. Luego de las 8 request falladas,
hacemos 2 request exitosas, completándose con eso las 10 request del
estado semi-abierto. Ahora, volvemos a ejecutar un request exitoso,
y como hemos de esperar fallará, puesto que hicimos más del 50%
de request fallidos (8) mostrándonos el camino alternativo. Así el ciclo
se repetirá.
