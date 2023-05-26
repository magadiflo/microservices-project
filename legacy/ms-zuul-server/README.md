# Zuul Server

- Al igual que sucede con ribbon y hystrix, **zuul** solo es compatible hasta la versión de
  Spring Boot **2.3.12.RELEASE**.
- Versiones posteriores se usa **Spring Cloud Gateway**.
- Importante bajar también la versión de java. Estuve trabajando con IntelliJ IDEA en la versión 17,
  pero al ejecutar lanzaba error, **cambié en el pom.xml a la versión 1.8 y funciona**.
- Debemos habilitar Zuul Server en la clase principal con la anotación **@EnableZuulProxy**.
- Como Zuul también será un cliente de eureka, también debemos habilitarlo como tal
  usando la anotación **@EnableEurekaClient**.
- **Zuul Server**, tiene dentro de sus dependencias a **ribbon**, por lo que **aplica el balanceo de carga.**

## Configurando rutas base

En el archivo application.properties agregamos las rutas base, donde:

- **nombre de la ruta**: productos (..routes.productos.. <--- ese es, podemos darle cualquier nombre)
- **Service-id** (id del servicio): ms-productos
- Cada vez que queramos acceder al ms-productos a través de Zuul, debemos colocar la ruta base(el del path), después de
  la ruta base, vendrían las rutas propias del ms-productos.
- **Ruta base**: /api-base/productos-base/
- **, indica las rutas propias del microservicio

````
zuul.routes.productos.service-id=ms-productos
zuul.routes.productos.path=/api-base/productos-base/**

zuul.routes.items.service-id=ms-items
zuul.routes.items.path=/api-base/items-base/**
````

- **Path de Zuul + Path del ms-productos**: http://127.0.0.1:8090/api-base/productos-base/api/v1/productos

## Configurando timeout en Zuul API Gateway

- **El escenario es el siguiente:** En nuestro ms-productos, método **verProducto(...)** le agregamos
  intencionalmente una demora de 2 Segundos, para que cuando llamemos al ms-items, método **getItem(...)**
  ocurra un timeOut, de tal manera que, como el método está siendo manejado por Hystrix, nos
  retornará un método alternativo **@HystrixCommand(fallbackMethod = "metodoAlternativo")**. El problema es
  que para **Zuul** es siempre un TimeOut y él nos devolverá siempre el error por TimeOut y no el método alternativo
  dado por Hystrix.
- Para solucionar el problema debemos modificar el timeout el application.properties de zuul:

````
hystrix.command.default.execution.isolation.thread.timeoutInMilliseconds: 20000
ribbon.ConnectTimeout: 3000
ribbon.ReadTimeout: 10000
````

- Como resultado de esa configuración (ampliación del timeout), zuul permitirá mostrar el
  método alternativo generado por hystrix desde el ms-items y ya no el TimeOut.

## Configurando el TimeOut en Zuul y en ms-items

- Suponiendo que de antemano sabemos que ocurrirá un tiempo de demora mayor
  al que Hystrix tiene mapeado (1s). Es decir, queremos esperar más tiempo a que se termine de
  resolver el método al que llamamos en el microservicio productos desde nuestro microservicio
  items. Entonces, para que no ocurra un timeOut en nuestro ms-items, necesitamos que
  su archivo de propiedades, también esté configurada con:

````
hystrix.command.default.execution.isolation.thread.timeoutInMilliseconds: 20000
ribbon.ConnectTimeout: 3000
ribbon.ReadTimeout: 10000
````

- De la misma manera, el application.properties de zuul server debe tener las configuraciones
  anteriores.
- **CONCLUSIÓN:** Cuando se llame al método del ms-items y este experimente una demora porque
  está haciendo un llamado al ms-productos, y ya sabemos que de antemano existirá dicha demora;
  con las configuraciones realizadas, tanto en el **ms-items y ms-zuul-server** ampliando
  el tiempo de espera, podremos obtener como resultado el valor esperado, es decir el valor real
  dado por el ms-productos (no un camino alternativo, ni mucho menos un error de timeOut).

---

## Agregando rutas base a los microservicios de usuarios y authorization-server

En el **application.properties** agregamos las rutas base que redireccionarán a
los nuevos microservicios creados:

````
zuul.routes.usuarios.service-id=ms-usuarios
zuul.routes.usuarios.path=/api-base/usuarios-base/**

zuul.routes.security.service-id=ms-authorization-server
zuul.routes.security.path=/api-base/authorization-server-base/**
zuul.routes.security.sensitive-headers=Cookie,Set-Cookie
````

La configuración ``zuul.routes.security.sensitive-headers=Cookie,Set-Cookie`` nos va a permitir
excluir de la cabecera el **Cookie** y el **Set-Cookie,** ya que con ellos
el servidor de autorización no funciona.
---

## Configurando Zuul como Servidor de Recurso

Configuraremos el servidor de recursos, que será nuestro Zuul Server. La configuración que haremos se
encargará de proteger todos endPoints de los distintos microservicios y dar acceso a los clientes
que se conecten según el token que envíen en las cabeceras http del request. También se encarga de
validar que el Token sea el correcto, con la misma firma con el cual se crea en el servidor de autorización.
Y esto lo tenemos que configurar en Zuul Gateway, donde tenemos todas las rutas base que apuntan
a cada microservicio.

**NOTA**

````
Esta configuración solo es compatible con Zuul Server (basado en servlets) y no con Spring Cloud Gateway (basado en 
programación reactiva)
````

### Agregando las dependencias para esta sección

Agregamos las siguientes dependencias que son las mismas que usamos en el **ms-authorization-server**:

````
<dependency>
    <groupId>org.springframework.security.oauth</groupId>
    <artifactId>spring-security-oauth2</artifactId>
    <version>2.3.8.RELEASE</version>
</dependency>
<dependency>
    <groupId>org.springframework.security</groupId>
    <artifactId>spring-security-jwt</artifactId>
    <version>1.1.1.RELEASE</version>
</dependency>
<dependency>
    <groupId>org.glassfish.jaxb</groupId>
    <artifactId>jaxb-runtime</artifactId>
</dependency>
````

### Creando la clase de configuración del servidor de recurso

Creamos una clase de configuración al que le debemos anotar con **@EnableResourceServer** para habilitar
la configuración **del servidor de recurso**.

Copiaremos dos métodos creados en el **ms-authorization-server** y lo agregaremos en nuestra
de configuración **ResourceServerConfig**:

````
@Bean
public JwtTokenStore jwtTokenStore() {
    return new JwtTokenStore(this.jwtAccessTokenConverter());
}

@Bean
public JwtAccessTokenConverter jwtAccessTokenConverter() {
    JwtAccessTokenConverter jwtAccessTokenConverter = new JwtAccessTokenConverter();
    jwtAccessTokenConverter.setSigningKey("mi-clave-secreta-12345");
    return jwtAccessTokenConverter;
}
````

Esto es importante, ya que ambos servidores (resource-server - authorization-server) deben tener
la misma firma para generar el token.

Ahora, sobreescribimos dos métodos del **ResourceServerConfigurerAdapter**:

**Para configurar el token**

````
@Override
public void configure(ResourceServerSecurityConfigurer resources) throws Exception {
    resources.tokenStore(this.jwtTokenStore());
}
````

**Para proteger las rutas con token**

````
@Override
public void configure(HttpSecurity http) throws Exception {
    http.authorizeRequests()
            .antMatchers("/api-base/authorization-server-base/**").permitAll()
            .antMatchers(HttpMethod.GET, "/api-base/productos-base/api/v1/productos",
                    "/api-base/items-base/api/v1/items",
                    "/api-base/usuarios-base/usuarios")
            .permitAll()
            .antMatchers(HttpMethod.GET,
                    "/api-base/productos-base/api/v1/productos/{id}",
                    "/api-base/items-base/api/v1/items/producto/{productoId}/cantidad/{cantidad}",
                    "/api-base/usuarios-base/usuarios/{id}")
            .hasAnyRole("ADMIN", "USER")
            .antMatchers("/api-base/productos-base/**",
                    "/api-base/items-base/**", "/api-base/usuarios-base/**")
            .hasRole("ADMIN")
            .anyRequest().authenticated();
}
````

---

## Creando configuración de OAuth en el servidor de configuración

En el repositorio del servidor de configuraciones agregamos un **application.properties**
con configuraciones compartidas por varios microservicios, en este caso las credenciales que
usará una aplicación cliente, así como la clave para firmar el token.

Entonces, necesitamos que nuestro servidor ms-zuul-server se comunique con el servidor de configuraciones
para obtener dichas configuraciones. Agregamos la dependencia de config client:

````
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-config</artifactId>
</dependency>
````

Ahora, como este microservicio está trabajando con una versión antigua de Spring Boot (2.3.12.RELEASE), debemos
crear dentro del directorio **/resources** el archivo **bootstrap.properties** donde agregaremos las configuraciones que
apunten al servidor de configuraciones:

````
# En el archivo bootstrap.properties
------------------------------------

spring.application.name=ms-zuul-server          <----- nombre de nuestro microservicio de zuul server
spring.cloud.config.uri=http://localhost:8888   <----- donde actualmente está el servidor de configuraciones
management.endpoints.web.exposure.include=*     <----- habilita los endpoints de Spring Actuator
````

**NOTA:**
> En los otros microservicios, donde estoy usando la **versión de Spring Boot 2.7.11** no es necesario agregar el
> archivo bootstrap.properties, ya que si no mal recuerdo a partir de la versión 2.4.x se quitó esa opción y se agregó
> una nueva forma de configurar, sería en el application.properties agregar esta opción:
> ``spring.config.import=optional:configserver:http://localhost:8888``

La configuración de **Spring Actuator**, es por si agregamos la dependencia
de Spring Actuator a este servidor con la finalidad de poder actualizar los valores
de las configuraciones sin necesidad de reiniciar la aplicación.

Finalmente en nuestra clase **ResourceServerConfig** estamos **usando la llave para firmar nuestro token**,
dicha llave ya la colocamos en el repositorio del servidor de configuraciones, así que para poder acceder
a él, necesitamos usar o el **Environment** o la inyección a través del **@Value**, en este caso usamos
el **@Value**:

````
@Value("${config.security.oauth.jwt.key}")
private String jwtKey;
````

Ahora, cambiamos la clave que está hardcodeado por nuestra variable

````
@Bean
public JwtAccessTokenConverter jwtAccessTokenConverter() {
    JwtAccessTokenConverter jwtAccessTokenConverter = new JwtAccessTokenConverter();
    jwtAccessTokenConverter.setSigningKey(this.jwtKey); <------- Aquí va el jwtKey para firmar el token
    return jwtAccessTokenConverter;
}
````

Otra modificación que se hizo fue agregar la anotación **@RefreshScope**, como recordaremos, si usamos
actuator eso nos permitirá acceder a una url y a través de ella poder reiniciar las configuraciones que
están siendo aplicadas en esta clase sin necesidad de reiniciar la aplicación.
---

## Configurando Cors en Spring Security OAuth2

La configuración de CORS es opcional, solo si nuestra aplicación Cliente reside en otro dominio.

En el curso, se realiza la creación de los @Bean (para este tema de configuración de cors) dentro de la misma clase de
configuración de nuestro servidor de recurso.

Por mi parte, crearé una clase de configuración aparte, para no tenerlos mezclados y definir sus responsabilidades.
Así que crearé una clase llamada **CorsConfig** dentro de un paquete **/config**.

El primer bean a crear será el siguiente:

````
@Configuration
public class CorsConfig {

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        corsConfiguration.setAllowedOrigins(Arrays.asList("*"));
        corsConfiguration.setAllowedMethods(Arrays.asList("POST", "GET", "PUT", "DELETE", "OPTIONS"));
        corsConfiguration.setAllowCredentials(true);
        corsConfiguration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfiguration);

        return source;
    }
    
    /* más código */
}
````

**DONDE:**

- El *, es una bandera que hace referencia a todos los orígenes, es decir cualquier dominio.
- Dentro de los métodos permitidos está el **OPTIONS**, es muy importante habilitarlo, ya que por debajo lo utiliza
  OAuth2 para sus endPonints ../oauth/token, etc..
- **setAllowCredentials(true)**, permitimos que se admiten las credenciales de usuario. De forma predeterminada, esto no
  está configurado (es decir, las credenciales de usuario no son compatibles).
- Con "/**", le indicamos que las configuraciones se apliquen a todas las rutas.

El segundo bean a crear será el siguiente:

````
@Bean
public FilterRegistrationBean<CorsFilter> corsFilterFilterRegistrationBean() {
    FilterRegistrationBean<CorsFilter> bean = new FilterRegistrationBean<>(new CorsFilter(this.corsConfigurationSource()));
    bean.setOrder(Ordered.HIGHEST_PRECEDENCE);
    return bean;
}
````

**DONDE:**

- **bean.setOrder(Ordered.HIGHEST_PRECEDENCE)**, le indicamos que será un filtro de ALTA PRIORIDAD.
- **...new CorsFilter(this.corsConfigurationSource()));**, registramos la configuración de cors realizada en el bean
  anterior.

Con el bean anterior **registramos un filtro http de cors** para **todas** las **rutas en zuul** de forma global,
que al final son las rutas de los demás microservicios, ya que es un gateway, pero siempre pasando por Zuul,
de esa manera, **no solo lo dejamos configurado en Spring Security**, sino que **también quede a nivel global**
a toda nuestra aplicación en general.

### Configurando el cors para Spring Security

En nuestra clase de configuración del servidor de recurso (ResourceServerConfig) aplicamos inyección de dependencia
vía constructor, del bean que creamos en nuestra clase CorsConfig.

````
@RefreshScope
@EnableResourceServer
@Configuration
public class ResourceServerConfig extends ResourceServerConfigurerAdapter {
    
    /* más código */
    private final CorsConfigurationSource corsConfigurationSource;

    public ResourceServerConfig(CorsConfigurationSource corsConfigurationSource) {
        this.corsConfigurationSource = corsConfigurationSource;
    }
    
    /* más código */
}
````

Finalmente, en nuestra **clase de configuración de Spring Security** agregamos la **configuración del cors**.

````
@Override
public void configure(HttpSecurity http) throws Exception {

    /* más código */
    
    .hasRole("ADMIN")
    .anyRequest().authenticated()
    .and()
    .cors().configurationSource(this.corsConfigurationSource); <---------- Agregando configuración de CORS
}
````

---

## Haciendo más robusta la key del JWT

Para que la llave de la firma del token no quede totalmente plana, lo codificamos en base64, así lo hacemos más robusta.

````
jwtAccessTokenConverter.setSigningKey(Base64.getEncoder().encodeToString(this.jwtKey.getBytes()));
````