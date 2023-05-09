# Spring Cloud Gateway

## Implementando Filtros Globales

- Podemos implementar filtros globales, es decir filtros que se aplican a cualquier ruta.
- Para eso implementamos la interfaz **GlobalFilter** y le damos un orden.

````
@Component
public class EjemploGlobalFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) { ... }
    
    @Override
    public int getOrder() { ... }
}
````

## Agregando filtro personalizado en una ruta en particular

- Creamos nuestra clase que será un filtro personalizado con el nombre
  **Ejemplo** y al que le agregamos el sufijo **GatewayFilterFactory**:

> EjemploGatewayFilterFactory

- Internamente, creamos una clase Configuración con 3 atributos.
- Para decirle a Spring Cloud Gateway que este filtro solo lo aplique
  a la ruta del **microservicio productos**, en el application.yml le
  agregamos la configuración:

````
spring:
  cloud:
    gateway:
      routes:
        - id: ms-productos
          uri: lb://ms-productos
          predicates:
            - Path=/api-base/productos-base/**
          filters:
            # Filtro de fábrica
            - StripPrefix=2
            # Filtro personalizado aplicado a ms-productos
            # Ejemplo, corresponde al prefijo del nombre de la clase que creamos: EjemploGatewayFilterFactory
            - name: Ejemplo
              # Los argumentos corresponde a los atributos de la clase de configuración
              args:
                mensaje: Hola, soy un mensaje personalizado
                cookieNombre: usuario
                cookieValor: MartDiaz
````

- Otra forma de agregar el filtro sería:

````
filters:
  # 2, porque el path (/api-base/productos-base/**) está compuesta por 2 segmentos: api-base y productos-base
  - StripPrefix=2
  
  # Aplicando este filtro solo a ms-productos
  # Ejemplo, corresponde al prefijo del nombre de la clase que creamos: EjemploGatewayFilterFactory
  # El orden en que enviamos los parámetros lo está definiendo el método shortcutFieldOrder() del filtro personalizado.
  - Ejemplo=Hola este es mi mensaje personalizado, usuario, magadiflo
````

- Por defecto toma el prefijo del nombre de la clase (si es que le hemos agregado el sufijo GatewayFilterFactory),
  pero podemos cambiarle. Para eso debemos sobreescribir el método **name()** y retornar el nuevo nombre:

> EjemploNuevoNombreFiltroCookie

- Ahora, en el application.yml realizar una pequeña modificación donde definimos el filtro personalizado:

````
- EjemploNuevoNombreFiltroCookie=Hola este es mi mensaje personalizado, usuario, magadiflo
````

## Asignando Orden al filtro personalizado

- Al código que ya tenemos implementado lo envolvemos dentro de la clase **OrderedGatewayFilter(..)**.
- De esta manera quedaría:

 ````
@Override
public GatewayFilter apply(Configuracion configuracion) {
    return new OrderedGatewayFilter((exchange, chain) -> { 
      // Código que hacía ejecuta la lógica del filtro pre - post
    }, 2);
}
 ````

- Donde el orden que le damos al filtro es **2**.
- Ahora, esa configuración del orden solo lo hacemos si en caso lo queremos ordenar,
  en caso contrario, solo retornamos el lamba, como normalmente estaba sin la clase
  de ordenamiento.

## Agregando filtros de fábrica en ruta del ms-items

- Agregaremos algunos filtros que vienen de fábrica en Spring Cloud Gateway,
  pero solo par cuando se accede a la ruta del microservicio items. Para eso
  modificamos el application.yml y agregamos los filtros.

````
- id: ms-items
  uri: lb://ms-items
  predicates:
    - Path=/api-base/items-base/**
  filters:
    - StripPrefix=2
    # Filtros que vienen de fábrica en Spring Cloud Gateway
    - AddRequestHeader=token-req, 987654
    - AddResponseHeader=token-resp, 456123
    - AddRequestParameter=nombre, Martin
````

- Para ver los datos configurados en estos filtros, en el resource del ms-items,
  método getAllItems(...) agregamos como parámetros los datos eviados por los
  filtros usando el @RequestParam y el @RequestHeader. El ReponseHeader lo
  vemos con Postman.

````
@GetMapping
public ResponseEntity<List<Item>> getAllItems(@RequestParam String nombre, @RequestHeader(name = "token-req") String token) {
    LOG.info("[Desde filtro de fábrica] name: {}, token: {}", nombre, token);
    return ResponseEntity.ok(this.itemService.findAll());
}
````

## Agregando predicates de fábrica en la ruta del ms-productos

Los **predicates** Son reglas o restricciones del request. Por ejemplo, para acceder a
nuestro ms-productos:

- El **Path** debe iniciar con /api-base/productos-base/**
- El **Header** debe traer un atributo token cuyo valor sera numérico (\d+)
- El **Method** permitido puede ser GET o POST
- El **Query (param)**, debe tener un parámetro en la url llamado color con valor verde
- El **Cookie**, que venga debe tener un atributo color con valor azul

Para que nuestro **Spring Cloud Gateway** permita redirigir la request al ms-productos,
esta request debe cumplir todos esos predicates, es decir, tendría este aspecto cuando
se haga la request desde postman:

````
[GET] http://127.0.0.1:8090/api-base/productos-base/api/v1/productos?color=verde
Headers:
  token: 123456
Cookies:
  domain: 127.0.0.1
  color=azul; Path=/api-base/productos-base/api/v1/productos;
````

Así estaría configurado en nuestro application.yml del ms-spring-cloud-gateway:

````
spring:
  cloud:
    gateway:
      routes:
        - id: ms-productos
          # lb: Load Balanced, es decir accederemos al ms productos mediante Load Balanced
          uri: lb://ms-productos
          predicates:
            - Path=/api-base/productos-base/**
            - Header=token, \d+
            - Method=GET, POST
            - Query=color, verde
            - Cookie=color, azul
          ......
          ......
````

## Trabajando con Resilience4J

Para trabajar con Resilience4J en Spring Cloud Gateway necesitamos agregar la
dependencia de **reactor con resilience4J**:

````
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-circuitbreaker-reactor-resilience4j</artifactId>
</dependency>
````

## Configurando el Circuit Breaker en Spring Cloud Gateway con Resilience4J

La configuración es similar al que definimos en el ms-items. En este caso, en el application.yml
de Spring Cloud Gateway, ya teníamos la configuración de Gateway. Agregaremos la configuración
de Resilience4J en el mismo archivo:

`El id que le dimos al circuit breaker será 'productos' ya que lo aplicaremos en dicho microservicio`

````
# Trabajando con Resilience4J
resilience4j:
  circuitbreaker:
    configs:
      defecto:
        sliding-window-size: 6
        failure-rate-threshold: 50
        wait-duration-in-open-state: 20s
        permitted-number-of-calls-in-half-open-state: 4
        slow-call-rate-threshold: 50
        slow-call-duration-threshold: 2s
    instances:
      # productos, id que le damos al circuit breaker
      productos:
        base-config: defecto
  # Configurando el TimeOut
  timelimiter:
    configs:
      defecto:
        timeout-duration: 2s
    instances:
      # productos, id que le damos al circuit breaker
      productos:
        base-config: defecto
````

Modificamos la configuración que teníamos inicialmente de Spring Cloud Gateway para
agregarle el Circuit Breaker, el fallbackUri cuando haya una excepción, etc.

````
# Configuraciones de Spring Cloud Gateway: predicados, filtros
spring:
  cloud:
    gateway:
      routes:
        - id: ms-productos
          uri: lb://ms-productos
          predicates:
            - Path=/api-base/productos-base/**
          filters:
            - name: CircuitBreaker
              args:
                name: productos
                statusCodes: 500
                fallbackUri: forward:/api-base/items-base/api/v1/items/producto/5/cantidad/5
            ......
            ......
````

- **name: productos**, id que le pusimos al Circuit Breaker en la configuración superior (resilience4j).
- **statusCodes: 500**, para que pueda manejar el "Internal Server Error" status 500.
- **fallbackUri**, da un servicio alternativo cuando se abra el circuito. Tiene que ser uno distinto al
  cual falla, porque si llamamos directamente a algún servicio bajo la ruta que falla (**/api-base/productos-base/**)
  se llamará de manera recursiva en un ciclo infinito, por eso es que pusimos la uri del servicio de items.