# Spring Cloud Gateway

## Implementando Filtros Globales

- Podemos implementar filtros globales, es decir filtros que se aplican a cualquier ruta.
- Para eso implementamos la interfaz **GlobalFilter** y le damos un orden.

````java

@Component
public class EjemploGlobalFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) { /* code */ }

    @Override
    public int getOrder() { /* code */ }
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

````yaml
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

````yaml
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
          #......
          #......
````

## Trabajando con Resilience4J

Para trabajar con Resilience4J en Spring Cloud Gateway necesitamos agregar la
dependencia de **reactor con resilience4J**:

````xml

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

````yaml
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

````yaml
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

---

## Configurando servicio spring cloud gateway con algunas dependencias [Servidor de Recursos]

Si trabajamos con este microservicio de Spring Cloud Gateway, necesitamos hacer configuraciones a
fin de proteger las rutas a los distintos microservicios, tal como lo hicimos en el servidor de Zuul. Ahora, como este
microservicio usa programación reactiva, la configuración será algo distinta a lo que hicimos en el ms-zuul-server (
quien usa programación imperativa, bajo los servlets).

Recordar que en nuestro proyecto de microservicios, solo tendremos uno de los dos servidores en funcionamiento,
o el ms-zuul-server o el ms-spring-cloud-gateway (más moderno).

Iniciamos agregando las siguientes dependencias al pom.xml:

````xml

<project>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-security</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-config</artifactId>
    </dependency>

    <!--  Estas dependencias las encuentra en https://github.com/jwtk/jjwt#install -->
    <dependency>
        <groupId>io.jsonwebtoken</groupId>
        <artifactId>jjwt-api</artifactId>
        <version>0.11.5</version>
    </dependency>
    <dependency>
        <groupId>io.jsonwebtoken</groupId>
        <artifactId>jjwt-impl</artifactId>
        <version>0.11.5</version>
        <scope>runtime</scope>
    </dependency>
    <dependency>
        <groupId>io.jsonwebtoken</groupId>
        <artifactId>jjwt-jackson</artifactId>
        <version>0.11.5</version>
        <scope>runtime</scope>
    </dependency>
    <!--- -->

    <!-- Esta dependencia en automático se incluye cuando incluimos Spring Security -->
    <dependency>
        <groupId>org.springframework.security</groupId>
        <artifactId>spring-security-test</artifactId>
        <scope>test</scope>
    </dependency>
</project>
````

**NOTA:** en el video se hace uso de otra dependencia **spring-cloud-starter-bootstrap**, también lo había
agregado, pero viendo en el botón de dependencias de **IntelliJ**, muestra que está dependencia será
omitida, ya que en la dependencia de **spring-cloud-starter-gateway** ya lo trae incluido. En realidad,
quien será omitida es **spring-cloud-starter**, pero esta está dentro de **spring-cloud-starter-bootstrap**
y es la única dependencia, así que por eso ya no la puse, ya que esa única dependencia ya la trae el
**spring-cloud-starter-gateway**.

En el application.properties agregamos la configuración para conectarnos al servidor de configuraciones:

````properties
# Configuracion al servidor de configuraciones
spring.config.import=optional:configserver:http://localhost:8888
````

---

## Implementando la clase de configuración Security Config

Creamos nuestra clase principal de configuración de Spring Security a la que le llamaremos **SpringSecurityConfig**.
La anotamos con **@EnableWebFluxSecurity** para habilitar la seguridad en WebFlux. Nuestra clase no implementará nada,
simplemente con la anotación agregada que es de configuración, tendremos un método bean que registrará un componente
del tipo **SecurityWebFilterChain**, para hacer toda la configuración de seguridad.

Por el momento, **al tener solo esta configuración**, cuando tratemos de acceder a cualquier
endPoint, veremos que no podremos, **todos necesitan autenticación**. Nos arrojará un **status 401 Unauthorized**.

````java

@EnableWebFluxSecurity
public class SpringSecurityConfig {
    @Bean
    public SecurityWebFilterChain configure(ServerHttpSecurity http) {
        return http.authorizeExchange()
                .anyExchange().authenticated()
                .and()
                .csrf().disable()
                .build();
    }
}
````

**Donde:**

- **.anyExchange().authenticated()**, protegemos todas las rutas.
- **.csrf().disable()**, deshabilitamos el token csrf que es para formularios. Aquí trabajaremos con API REST.

---

## Dando reglas de seguridad a nuestras rutas de Spring Cloud Gateway

Al igual que en zuul agregamos reglas a las rutas de nuestros microservicios, aquí también haremos lo mismo. Cada,
conjunto de rutas tendrá cierto nivel de acceso, por lo tanto, a la configuración que hicimos en la clase anterior,
le agregamos nuestras reglas de seguridad:

````
@Bean
public SecurityWebFilterChain configure(ServerHttpSecurity http) {
    return http.authorizeExchange()
            .pathMatchers("/api-base/authorization-server-base/**").permitAll()
            .pathMatchers(HttpMethod.GET, "/api-base/productos-base/api/v1/productos",
                    "/api-base/items-base/api/v1/items",
                    "/api-base/usuarios-base/usuarios",
                    "/api-base/items-base/api/v1/items/producto/{productoId}/cantidad/{cantidad}",
                    "/api-base/productos-base/api/v1/productos/{id}").permitAll()
            .pathMatchers(HttpMethod.GET, "/api-base/usuarios-base/usuarios/{id}").hasAnyRole("ADMIN", "USER")
            .pathMatchers("/api-base/productos-base/**",
                    "/api-base/items-base/**",
                    "/api-base/usuarios-base/**").hasRole("ADMIN")
            .anyExchange().authenticated()
            .and()
            .csrf().disable()
            .build();
}
````

---

## Implementando el componente Authentication Manager Reactive

Creamos una clase de componente que implementará la interfaz **ReactiveAuthenticationManager**. De esa interfaz
implementamos su método **authenticate(...)**.

````java

@Component
public class AuthenticationManagerJwt implements ReactiveAuthenticationManager {
    private static Logger LOG = LoggerFactory.getLogger(AuthenticationManagerJwt.class);
    @Value("${config.security.oauth.jwt.key}")
    private String llaveJwt;

    @Override
    public Mono<Authentication> authenticate(Authentication authentication) {
        LOG.info("Token: {}", authentication.getCredentials());
        return Mono.just(authentication.getCredentials().toString())
                .map(token -> {
                    SecretKey llave = Keys.hmacShaKeyFor(Base64.getEncoder().encode(this.llaveJwt.getBytes()));
                    return Jwts.parserBuilder().setSigningKey(llave).build()
                            .parseClaimsJws(token).getBody();
                })
                .map(claims -> {
                    String username = claims.get("user_name", String.class);
                    List<String> roles = claims.get("authorities", List.class);
                    Collection<GrantedAuthority> authorities = roles.stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList());
                    return new UsernamePasswordAuthenticationToken(username, null, authorities);
                });
    }
}
````

Observamos en el código anterior que el **..getCredentials() es el token**, que **será pasado vía argumento
authentication** en el Filtro que crearemos la próxima sección.

Para que la llave no quede totalmente plana, lo codificamos en base64, así lo hacemos más robusta. Ese mismo cambio debe
hacerse en el servidor de autorización OAuth2, para que tengan la misma firma. También, como estamos trabajando con Zuul
necesitamos agregarle ese pequeño cambio.

````
SecretKey llave = Keys.hmacShaKeyFor(Base64.getEncoder().encode(this.llaveJwt.getBytes()));
````

El código siguiente, lo que hace es validar el token:

````
return Jwts.parserBuilder().setSigningKey(llave).build()
````

Si es válido, obtenemos los claims y lo retornamos al flujo que ahora será
del tipo claims

````
.parseClaimsJws(token).getBody();
````

Finalmente, en el último map, obtenemos de los claims el **user_name** y los **authorities**
(recordar que con esos nombres están en la estructura del jwt).

````
.map(claims -> {
    String username = claims.get("user_name", String.class);
    List<String> roles = claims.get("authorities", List.class);
/* más código */
````

---

## Implementando el componente JwtAuthenticationFilter

Crearemos la clase de filtro para la autenticación llamada **JwtAuthenticationFilter**, implementamos
la interfaz WebFilter e implementamos su método:

````java

@Component
public class JwtAuthenticationFilter implements WebFilter {

    private final ReactiveAuthenticationManager reactiveAuthenticationManager;

    public JwtAuthenticationFilter(ReactiveAuthenticationManager reactiveAuthenticationManager) {
        this.reactiveAuthenticationManager = reactiveAuthenticationManager;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        return Mono.justOrEmpty(exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION))
                .filter(authHeader -> authHeader.startsWith("Bearer "))
                .switchIfEmpty(chain.filter(exchange).then(Mono.empty()))
                .map(authHeader -> authHeader.replace("Bearer ", ""))
                .flatMap(token -> this.reactiveAuthenticationManager.authenticate(new UsernamePasswordAuthenticationToken(null, token)))
                .flatMap(authentication -> chain.filter(exchange).contextWrite(ReactiveSecurityContextHolder.withAuthentication(authentication)));
    }
}
````

**Explicación:**

Mediante el **(ServerWebExchange exchange,** podemos obtener el request y obtener el token que nos envían desde POSTMAN,
desde algún servicio, etc., ese token es enviado en la cabecera mediante el **"Authorization: Bearer ..."**.

Convertimos las cabeceras http Authorization en un mono:

````
return Mono.justOrEmpty(exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION))
````

Filtramos y preguntamos si el token inicia con "Bearer ":

````
.filter(authHeader -> authHeader.startsWith("Bearer "))
````

Si no viene con el Bearer, hacemos un switch y nos salimos del flujo usando el chain.filter(...):

````
.switchIfEmpty(chain.filter(exchange).then(Mono.empty()))
````

Como contiene el "Bearer ", lo limpiamos. Usamos un map, porque estamos retornando un nuevo string ya limpio

````
.map(authHeader -> authHeader.replace("Bearer ", ""))
````

Usamos un FlatMap porque estamos usando un reactiveAuthenticationManager método authenticate que nos retorna otro flujo,
un tipo Mono. Le pasamos el token para que el otro componente lo valide, haga el tratamiento del token. Recordar que ese
reactiveAuthenticationManager que estamos inyectando corresponde al **AuthenticationManagerJwt** que creamos en la clase
anterior:

````
.flatMap(token -> this.reactiveAuthenticationManager.authenticate(new UsernamePasswordAuthenticationToken(null, token)))
````

Aquí usamos otro FlatMap, ya viene el authentication, continuamos con la ejecución de los filtros pero lo guardamos en
el contexto de la autenticación usando ReactiveSecurityContextHolder:

````
.flatMap(authentication -> chain.filter(exchange).contextWrite(ReactiveSecurityContextHolder.withAuthentication(authentication)));
````

---

## Registrando filtro JwtAuthenticationFilter en la configuración de Spring Security

Anteriormente, habíamos creado nuestra clase de componente **JwtAuthenticationFilter** y ahora necesitamos
registrarlo en la clase de configuración principal de Spring Security. Para eso aplicamos inyección de
dependencia vía constructor y agregamos en el método nuestro filtro creado:

````java

@EnableWebFluxSecurity
public class SpringSecurityConfig {
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SpringSecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityWebFilterChain configure(ServerHttpSecurity http) {
        return http.authorizeExchange()
                ......... /* más código */
                .and()
                .addFilterAt(this.jwtAuthenticationFilter, SecurityWebFiltersOrder.AUTHENTICATION)
                .csrf().disable()
                .build();
    }
}
````

Luego, ejecutamos todos los microservicios, y **probamos el acceso a los endpoints** desde postman. Generamos un token
y tratamos de acceder a los endpoints protegidos con el token generado. Obviamente, en función del rol podremos acceder
a cierto endpoint.

La secuencia de ejecución de los microservicios debe ser:

````
[1°] ms-config-server
[2°] ms-eureka-server
[3°] --- Aquí si no importa el orden y con esos dos microservicios basta para hacer las pruebas -----
- ms-usuarios
- ms-authorization-server
-------------------------------------------------------------------------------------------------
[4°] ms-spring-cloud-gateway
````

### Probando ver usuarios - El usuario autenticado tiene ROLE_USER

````
[GET] http://127.0.0.1:8090/api-base/usuarios-base/usuarios/2

REQUEST HEADER
--------------
Authorization: 
Type: Bearer Token
Token: <el token de abajo>

Token del usuario con role ROLE_USER luego de autenticarse:
eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VyX25hbWUiOiJtYXJ0aW4iLCJzY29wZSI6WyJyZWFkIiwid3JpdGUiXSwiYXBlbGxpZG8iOiJEw61heiIsImNvcnJlbyI6Im1hcnRpbkBtYWdhZGlmbG8uY29tIiwiZXhwIjoxNjg0NTQxMjQ1LCJub21icmUiOiJNYXJ0w61uIiwiYXV0aG9yaXRpZXMiOlsiUk9MRV9VU0VSIl0sImp0aSI6IjM3OTk1MDkwLTU4M2YtNDEyZC1iM2Q4LWM3MTJmZmU0NTRiMCIsImNsaWVudF9pZCI6ImZyb250ZW5kQXBwIn0.gTivPxXvSkwYMeZUVtAg4IBn5KHEhe2oiGzI9HsX5s8

El Header a enviarse sería el siguiente:
Key             Value
-------------   -------------------------------
Authorization   Bearer eyJhbGciOiJIUzI1NiI......
````

Al ejecutar la petición con los datos anteriores, nos mostrará que no podemos acceder porque ese endpoint está
restringido solo para usuarios de ROLE_ADMIN y el que está haciendo la petición tiene ROLE_USER.