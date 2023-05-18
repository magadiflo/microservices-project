# Microservicio oauth (Servidor de autorización)

Una de las dependencias que agregaremos a este microservicio será el de la librería ms-usuarios-commons.
Dicha librería incorpora como dependencia a Spring Data Jpa. Por otro lado, nuestro microservicio
ms-authorization-server no interactuará directamente con una base de datos, sino que a través del cliente
http FeignClient hará peticiones al ms-usuarios y es este microservicio quien sí interactúa con una BD.

Pero ocurre lo siguiente, como en este microservicio incluimos la librería ms-usuarios-commons como dependencia, y este
a su vez tiene a Spring Data JPA, nos obligará a implementar un String de conexión a una base de datos porque con tan
solo agregar dicha dependencia a un proyecto (agregado por transitividad), este se autoconfigura, pero nosotros sabemos
que este microservicio no manejará persistencia, ni conexión a base de datos, por lo tanto, debemos desactivar la
autoconfiguración similar a cómo excluimos el DataSource en el ms-items, lo hicimos a través de anotaciones en la
clase principal. Aquí se muestran cómo sería en este caso:

**Usando anotación [Similar a cómo configuramos en el ms-items]**

````
@SpringBootApplication
@EnableAutoConfiguration(exclude = {DataSourceAutoConfiguration.class})
public class MsAuthorizationServerApplication {...}
````

**Desde el mismo pom.xml [En esta oportunidad trabajaremos con esta forma]**

````
<dependency>
    <groupId>com.magadiflo.msp.shared.library</groupId>
    <artifactId>ms-usuarios-commons</artifactId>
    <version>0.0.1-SNAPSHOT</version>
    <exclusions>
        <exclusion>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-jpa</artifactId>
        </exclusion>
    </exclusions>
</dependency>
````

---

## Creando nuestra interfaz Feign Client

Desde este microservicio nos comunicaremos con el ms-usuarios usando **FeignClient**, para esto ya tenemos en el pom.xml
la dependencia de Feign.

Primero, habilitaremos el uso de FeignClient con la anotación **@EnableFeignClients** desde la clase principal:

````
@EnableFeignClients
@EnableEurekaClient
@SpringBootApplication
public class MsAuthorizationServerApplication {...}
````

Crearemos la siguiente interfaz:

````
@FeignClient(name = "ms-usuarios", path = "/usuarios")
public interface IUsuarioFeignClient {

    @GetMapping(path = "/search/buscar-usuario")
    Optional<Usuario> findByUsername(@RequestParam(value = "usuario") String username);

}
````

**Donde:**

- **name=ms-usuarios**, corresponde al **nombre del microservicio** al que nos comunicaremos.
- **path = "/usuarios"**, corresponde al path que le definimos al ms-usuarios. En este caso,
  como estamos usando en el ms-usuarios Spring Data Rest, el path lo definimos dentro de la
  anotación **@RepositoryRestResource(path = "usuarios")** aplicada a la interfaz IUsuarioRepository.
- Como tipo de dato le decimos que nos retorne un Optional de Usuario, eso lo implementará en
  tiempo de ejecución.

El resto del código es similar a cómo hemos venido trabajando con clientes Feign.

---

## Implementando servicio UserDetailsService con FeignClient

Creamos una clase **UsuarioService** que implementa la interfaz
**UserDetailsService** e implementamos su método **loadUserByUsername()**.
Esta implementación del **UserDetailsService** nos permitirá, cuando un
usuario se loguee, ir a buscarlo con el FeignClient al ms-usuarios. Si se encuentra
el usuario se retorna un **UserDetails** que es un usuario propio de Spring Security y
reconocido por éste dentro de su arquitectura. En caso de que no se encuentre el
usuario buscado se retorna un **UsernameNotFoundException**.

Nuestra implementación de la interfaz UserDetailsService quedaría así:

````
@Service
public class UsuarioService implements UserDetailsService {
    private static final Logger LOG = LoggerFactory.getLogger(UsuarioService.class);
    private final IUsuarioFeignClient usuarioFeignClient;

    public UsuarioService(IUsuarioFeignClient usuarioFeignClient) {
        this.usuarioFeignClient = usuarioFeignClient;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return this.usuarioFeignClient.findByUsername(username)
                .map(usuario -> {

                    List<SimpleGrantedAuthority> authorities = usuario.getRoles().stream()
                            .map(rol -> new SimpleGrantedAuthority(rol.getNombre()))
                            .peek(simpleGrantedAuthority -> LOG.info("Rol: {}", simpleGrantedAuthority.getAuthority()))
                            .toList();

                    UserDetails userDetails = User.builder()
                            .username(usuario.getUsername())
                            .password(usuario.getPassword())
                            .authorities(authorities)
                            .disabled(!usuario.getEnabled())
                            .build();

                    LOG.info("Detalles del usuario autenticado: {}", userDetails);

                    return userDetails;
                })
                .orElseThrow(() -> new UsernameNotFoundException(String.format("Error en el login, no existe el usuario %s en el sistema", username)));
    }
}
````

---

## Añadiendo la clase SpringSecurityConfig y registrando UserDetailsService

Como estoy revisando el libro de Spring Security In Action - 2020, trataré de hacer las configuraciones,
obviamente según el curso de Andrés Guzmán, pero en situaciones en las que vea que podría configurarse de
otra manera según el libro mencionado, los trataré de hacer de esa otra forma.

Trataré de separar las responsabilidades a fin de dejar el archivo principal de configuración de
Spring Security (SpringSecurityConfig) lo más limpio posible. En tal sentido, **crearemos una clase de
configuración** dentro de un directorio llamado */config* que **expondrá el @Bean del PasswordEncoder()**:

````
@Configuration
public class UserManagementConfig {
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
````

**NOTA**
> Recordemos que anteriormente ya habíamos creado nuestra **clase de implementación del UserDetailsService** llamado
**UsuarioService** y lo habíamos anotado con el estereotipo **@Service** para que se registre dentro del contenedor de
> Spring.

Como paso siguiente, crearemos la clase principal de configuración de Spring Security llamada **SpringSecurityConfig**
la cual extenderá de la clase abstracta **WebSecurityConfigurerAdapter**.

Según el libro Spring Security In Action 2020, existen varias formas de configurar la seguridad, una de ellas es
exponiendo las implementaciones concretas del **UserDetailsService** y del **PasswordEncoder** a través de **@Bean** y
la otra forma es sobreescribiendo el método **configure(AuthenticationManagerBuilder auth)** de la clase abstracta
WebSecurityConfigurerAdapter y definiendo dentro de ellas las implementaciones concretas de un **UserDetailsService** y
un **PasswordEncoder**.

Ahora, en este curso, Andrés Guzmán hace uso de las dos formas con la diferencia de que la implementación del
**UserDetailsService** está dentro de una clase anotada con **@Service**, digamos que sería casi igual a si lo
definiéramos en un método anotado con un @Bean y digo que es casi igual, porque de ambas maneras hacemos que se
registre dentro del contenedor de spring.

Entonces, siguiendo las recomendaciones del libro, deberíamos usar **solo una de las dos opciones**: las que incluyen
**con anotaciones @Bean** o **la sobre escritura del método configure(AuthenticationManagerBuilder auth)**. Es
importante, según el libro no mezclar las distintas formas de anular la configuración predeterminada de Spring Security.

Primero veamos cuál es la configuración que en el curso de Andrés Guzmán se realiza:

````
@Configuration
public class SpringSecurityConfig extends WebSecurityConfigurerAdapter {
    private final UserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;

    public SpringSecurityConfig(UserDetailsService userDetailsService, PasswordEncoder passwordEncoder) {
        this.userDetailsService = userDetailsService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(this.userDetailsService).passwordEncoder(this.passwordEncoder);
    }
    
    /* más código */
}
````

Como observamos, está aplicando inyección de dependencia vía constructor tanto del UserDetailsService y del
PasswordEncoder (las implementaciones concretas las realizamos anteriormente). Ahora, lo importante aquí
es el método que está sobreescribiendo **configure(AuthenticationManagerBuilder auth)**, que según el libro,
ya no debería sobreescribirse porque ya definimos como @Bean o como @Service las implementaciones
concretas del UserDetailsService y PasswordEncoder, por lo tanto, **OPTARÉ POR NO SOBREESCRIBIR DICHO MÉTODO**,
aunque más adelante se vuelve a hacer uso de él pero para configurar otro componente, cuando llegue su momento
veremos como lo abordaremos.

Como última configuración, en el curso se sobreescribe el método **authenticationManager()** definiéndolo como
un **@Bean**. Andrés Guzmán menciona que ```necesitamos registrarlo en el contenedor de Spring porque más adelante
lo usaremos en la configuración del servidor de autorización.```Hasta ese punto ya no he llegado en el libro
de Spring Security In Action, así que continuaré con los pasos del curso.

Finalmente, hasta este momento, así quedaría la configuración de nuestra clase SpringSecurityConfig:

````
@Configuration
public class SpringSecurityConfig extends WebSecurityConfigurerAdapter {

    @Bean
    @Override
    protected AuthenticationManager authenticationManager() throws Exception {
        return super.authenticationManager();
    }

}
````

---

## Añadiendo la configuración para el servidor de autorización en el servicio oauth (ms-authorization-server)

Crearemos la **configuración del servidor de autorización** que se encarga **de todo el proceso de login** por el
lado de **OAuth2**, todo lo que tenga que ver con el token (jwt), desde el proceso de autenticación, generar el token,
validarlo, etc.

Creamos la siguiente clase de configuración anotándolo con **@EnableAuthorizationServer** para **habilitar la clase como
un servidor de autorización.**

````
@EnableAuthorizationServer
@Configuration
public class AuthorizationServerConfig extends AuthorizationServerConfigurerAdapter {
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    public AuthorizationServerConfig(PasswordEncoder passwordEncoder, AuthenticationManager authenticationManager) {
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
    }
    
    @Override
    public void configure(AuthorizationServerEndpointsConfigurer endpoints) throws Exception {
        endpoints.authenticationManager(this.authenticationManager)
                .tokenStore(this.jwtTokenStore())
                .accessTokenConverter(this.jwtAccessTokenConverter());
    }

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
}

````

Como vemos, estamos inyectando esta clase con los beans definidos anteriormente:
```@Bean PasswordEncoder y AuthenticationManager```, este último contiene por debajo, nuestra implementación
del **UserDetailsService**.

Registramos el **AuthenticationManager** en nuestro authorizationServer. Para eso, sobreescribimos el método
**configure(AuthorizationServerEndpointsConfigurer endpoints)** donde será registrado.

Este método **configure(AuthorizationServerEndpointsConfigurer endpoints)**, está **relacionado con el endpoint de
OAuth2** del servidor de autorización que se encarga de generar el token.

Accediendo al endpoint siguiente, nos genera el token con esos datos enviados y retorna un JSON con el Token, siempre y
cuando los datos sean válidos.

````
[POST] /oauth/token
username, password, grand_type: password, {client_id, password = credenciales de la aplicación cliente}
````

Continuando con la explicación de la clase de configuración **AuthorizationServerConfig** vemos que configuramos
el **accessTokenConverter(...)** para que sea del tipo JWT. Creamos un **@Bean JwtAccessTokenConverter** como
implementación concreta del tipo accessTokenConverter quien se encargará de tomar los valores del usuario y convertirlos
en el Token (JWT) codificados en base64. También creamos un @Bean **JwtTokenStore** que es una implementación
concreta del tokenStore y es quien se encarga de traducir los **access token** hacia y desde las autenticaciones.
Recuerde usar la misma instancia de JwtAccessTokenConverter que se usó cuando se acuñaron los tokens.
---

## Añadiendo configuración de los clientes en el Servidor de Autorización

Ahora haremos las configuraciones para nuestros clientes frontend (App de angular, Android, etc.) que accederán a
nuestros microservicios. Dependiendo del número de clientes frontend que consumirán nuestros servicios debemos
registrarlos uno por uno con su **client_id y con su password**.

La idea del estándar OAuth es proporcionar mayor seguridad. No solamente nos autenticamos con los usuarios de nuestro
backend, sino también con las credenciales de la aplicación cliente que se comunicará con nuestro backend. Podríamos
decir que tiene una **doble autenticación**: Por un lado, la **aplicación cliente (frontend)** y por otro con las
**credenciales del usuario**.

Para agregar la configuración a nuestros clientes implementamos el método **configure(ClientDetailsServiceConfigurer
clients)**:

````
@Override
public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
    clients.inMemory()
            .withClient("frontendApp")
            .secret(this.passwordEncoder.encode("frontendApp-12345"))
            .scopes("read", "write")
            .authorizedGrantTypes("password", "refresh_token")
            .accessTokenValiditySeconds(3600)
            .refreshTokenValiditySeconds(3600);
}
````

**DONDE**

- Usaremos **inMemory()** para guardar la configuración de los clientes, pero fácilmente se
  puede optar por usar **Jdbc**.
- **withClient() y secret()**, serán las credenciales que le daremos a nuestra aplicación cliente.
- **scopes("read", "write")**, es el alcance o permiso que tendrá nuestra aplicación cliente. En este caso,
  la aplicación podrá leer información, crear o modificar.
- **authorizedGrantTypes("password", "refresh_token")**, nos indica cómo es que obtendremos el token. El
  **"password"** indica que nuestros usuarios para autenticarse deben ingresar sus credenciales:
  un **username y un password**, de esa forma obtendrán un token de acceso.
  **refresh_token**, nos permitirá obtener un nuevo token de acceso completamente renovado.
- **accessTokenValiditySeconds** y **refreshTokenValiditySeconds**, tiempo de validez del accessToken y
  refreshToken, está en segundos.

## Configurando AuthorizationServerSecurityConfigurer

Serán los **permisos** que tendrán nuestros **endpoint** del **servidor de Autorización OAuth2** para
generar el token y validar el token.

Sobreescribimos el método configure(AuthorizationServerSecurityConfigurer security):

````
@Override
public void configure(AuthorizationServerSecurityConfigurer security) throws Exception {
    security.tokenKeyAccess("permitAll()")
            .checkTokenAccess("isAuthenticated()");
}
````

**DONDE**

- **permitAll()**, es el permiso de Spring Security para permitir a todos.
- El **tokenKeyAccess(...)** es precisamente el endpoint para generar el token de autenticación con la ruta
  ```[POST] /oauth/token```. Cada vez que nos autenticamos mediante post, enviamos las credenciales del usuario y las
  del cliente frontend, valida estas credenciales y nos autentica. La idea es que esa ruta endpoint sea público para que
  cualquiera acceda a generar un token, obviamente con las consideraciones ya antes mencionadas (envío de credenciales
  de usuario y cliente frontend)
- **checkTokenAccess(...)**, se encarga de validar el token.
- **"isAuthenticated()"**, es un método de Spring Security que nos permite validar que el cliente esté autenticado.

Para acceder a estos dos endpoints, es usando la autenticación del tipo Http Basic:
```Authorization Basic: clientId: clientSecret```
---

## Probando la autenticación con Postman y obteniendo el token JWT

Para el ejemplo, necesitamos generar passwords encriptados con BCrypt. Para eso, en la clase principal creamos un
@Bean y generamos una implementación concreta de la interfaz funcional CommandLineRunner. Otra opción sería,
en vez de crear un bean, que la clase principal implemente la interfaz e implementamos el método run():

````
@Bean
public CommandLineRunner run() throws Exception {
    return args -> {
        String password = "12345";
        for (int i = 0; i < 4; i++) {
            String passwordBcrypt = this.passwordEncoder.encode(password);
            LOG.info(passwordBcrypt);
        }
    };
}
````

**Donde**

- El **passwordEncoder** lo inyectamos vía constructor.

### Generando contraseñas

Luego ejecutamos primero **ms-eureka-server**, luego **ms-authorization-server** y revisamos en la consola
las contraseñas generadas.

### Cambiando las contraseñas en texto plano por las encriptadas

En el archivo **import.sql** del **ms-usuarios** reemplazamos las contraseñas que están en texto plano por cualquiera
de las contraseñas generadas.

### Agregando ruta del ms-authorization-server en el ms-spring-cloud-gateway

Para poder acceder al ms-authorization-server mediante el gateway necesitamos configurarle
sus rutas en el **application.yml**, a continuación se muestra la configuración que se agregó:

````
- id: ms-authorization-server
  uri: lb://ms-authorization-server
  predicates:
    - Path=/api-base/authorization-server-base/**
  filters:
    - StripPrefix=2
````

### Probando la autenticación desde Postman

````
[POST] http://127.0.0.1:8090/api-base/authorization-server-base/oauth/token
````

**DONDE**

- **/api-base/authorization-server-base/**, corresponde a la ruta configurada en el
  application.yml del ms-spring-cloud-gateway.
- **/oauth/token**, es la ruta propia del servidor de autorización.

````
REQUEST

Authorization
-------------
Type: Basic Auth
Username: frontendApp
Password: frontendApp-12345

Por detrás, Postman generará el siguiente header para los datos de la Authorization anterior:

Headers
-------
Key= Authorization
Value= Basic ZnJvbnRlbmRBcHA6ZnJvbnRlbmRBcHAtMTIzNDU=

Donde el código mostrado se obtiene del username y password separado por dos puntos
codificado en base64:
  frontendApp:frontendApp-12345 --> convertido a base64 --> ZnJvbnRlbmRBcHA6ZnJvbnRlbmRBcHAtMTIzNDU=
  
Body
----
[*] x-www-form-urlencoded
Key         Value
----------  --------
usename     admin
password    12345
grant_type  password
````

**DONDE**

- En el **headers** enviamos usando **Http Basic Auth** las credenciales de la aplicación
  cliente configurada dentro del servidor de autorización.
- En el **body**, usando **x-www-form-urlencoded** enviamos las credenciales del usuario
  que se quiere autenticar.
- Notar que el tipo de **grant_type** es **password**, tal como lo configuramos en el servidor
  de autorización, esto nos indica que nos devolverá un token de acceso cuando nosotros le proporcionemos
  nuestras credenciales.

````
RESPONSE

{
    "access_token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJleHAiOjE2ODQyODUzMDgsInVzZXJfbmFtZSI6ImFkbWluIiwiYXV0aG9yaXRpZXMiOlsiUk9MRV9BRE1JTiIsIlJPTEVfVVNFUiJdLCJqdGkiOiI4NzI5MzM4Yi03NTI4LTQ3M2YtOGU2NS05MjA1MzUyOWFhMDEiLCJjbGllbnRfaWQiOiJmcm9udGVuZEFwcCIsInNjb3BlIjpbInJlYWQiLCJ3cml0ZSJdfQ.qZvNgMvz9AYj7cbI5YQP_oyppNXeOklJ69Hdb754-Ls",
    "token_type": "bearer",
    "refresh_token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VyX25hbWUiOiJhZG1pbiIsInNjb3BlIjpbInJlYWQiLCJ3cml0ZSJdLCJhdGkiOiI4NzI5MzM4Yi03NTI4LTQ3M2YtOGU2NS05MjA1MzUyOWFhMDEiLCJleHAiOjE2ODQyODUzMDgsImF1dGhvcml0aWVzIjpbIlJPTEVfQURNSU4iLCJST0xFX1VTRVIiXSwianRpIjoiNTAwZTMzODgtN2E5NC00MmI1LTg3MmMtMDFjMjI4N2JiZjU0IiwiY2xpZW50X2lkIjoiZnJvbnRlbmRBcHAifQ.8JzKgFAv6Mt1TJ9XpTmdx6VjFxVxmyZQiXMktlB9UQw",
    "expires_in": 3599,
    "scope": "read write",
    "jti": "8729338b-7528-473f-8e65-92053529aa01"
}
````

**NOTA**

La respuesta obtenida luego de enviarle tanto las credenciales de la aplicación cliente como las credenciales del
usuario a autenticarse se muestran en el código anterior.
---

## Añadiendo más información al token JWT

Primero, crearemos una interfaz que solo tendrá un método para poder buscar un usuario por su username:

````
public interface IUsuarioService {
    Optional<Usuario> findByUsername(String username);
}
````

Como nuestra clase de servicio **UsuarioService** ya tiene inyectada el **IUsuarioFeignClient**, quien es el
que nos permitirá hacer la petición al ms-usuarios para encontrar a un usuario, hacemos que esta clase
implemente la interfaz que creamos anteriormente e implementamos su método para poder buscar a un usuario
por su username:

````
@Service
public class UsuarioService implements IUsuarioService, UserDetailsService {  
  /* Más código */
  
  @Override
  public Optional<Usuario> findByUsername(String username) {
      return this.usuarioFeignClient.findByUsername(username);
  }
}
````

Ahora, debemos crear una clase que implemente la interfaz **TokenEnhancer** (Token potenciador) que nos permitirá
agregar información adicional al token (claims). Esta clase hará inyección de dependencia de la clase de servicio
**UsuarioService,** pero a través de la interfaz que creamos **IUsuarioService,** ya que este tiene el método
que nos retorna el Usuario a partir de su username:

````
@Component
public class InfoAdicionalToken implements TokenEnhancer {
    private final IUsuarioService usuarioService;

    public InfoAdicionalToken(IUsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @Override
    public OAuth2AccessToken enhance(OAuth2AccessToken accessToken, OAuth2Authentication authentication) {
        return this.usuarioService.findByUsername(authentication.getName())
                .map(usuario -> {

                    Map<String, Object> info = new HashMap<>();
                    info.put("nombre", usuario.getNombre());
                    info.put("apellido", usuario.getApellido());
                    info.put("correo", usuario.getEmail());

                    ((DefaultOAuth2AccessToken) accessToken).setAdditionalInformation(info);

                    return accessToken;
                }).orElseGet(() -> accessToken);
    }
}
````

Del código anterior, lo único extraño será el código que se muestra abajo, pero no hay nada de que sorprenderse,
únicamente estamos haciendo un casteo del tipo de la interfaz **OAuth2AccessToken** a una implementación
concreta **DefaultOAuth2AccessToken**, ya que es esta implementación concreta la que tiene el método
**setAdditionalInformation(...)**, que es para agregar la información adicional.

````
((DefaultOAuth2AccessToken) accessToken).setAdditionalInformation(info);
````

Finalmente, necesitamos agregar la información adicional del token al token que se genera por defecto. Esta
configuración lo hacemos en la clase **AuthorizationServerConfig**, método de los **endpoints**. Inyectamos
a través del constructor la clase **InfoAdicionalToken,** ya que este contiene la información adicional. En el
método realizamos la siguiente modificación:

````
@Override
public void configure(AuthorizationServerEndpointsConfigurer endpoints) throws Exception {
    TokenEnhancerChain tokenEnhancerChain = new TokenEnhancerChain();
    tokenEnhancerChain.setTokenEnhancers(List.of(this.infoAdicionalToken, this.jwtAccessTokenConverter()));

    endpoints.authenticationManager(this.authenticationManager)
            .tokenStore(this.jwtTokenStore())
            .accessTokenConverter(this.jwtAccessTokenConverter())
            .tokenEnhancer(tokenEnhancerChain);
}
````

Como se observa en el código anterior, creamos un objeto del tipo **TokenEnhancerChain** para poder
unir en una lista la **información adicional** con **la información generada por defecto
(this.jwtAccessTokenConverter())**. Finalmente, al **endpoints** le agregamos el **tokenEnhancer(...)**.
Es importante que el orden de la lista sea primero **la información adicional** y segundo
**la información por defecto**.

### Probando la autenticación desde Postman obteniendo la nueva información

Hacemos uso de la misma petición que hicimos anteriormente, con los mismos datos (ver apartado superior):

````
[POST] http://127.0.0.1:8090/api-base/authorization-server-base/oauth/token
````

Vemos que nos retorna la información adicional en el objeto.

````
RESPONSE BODY
{
    "access_token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VyX25hbWUiOiJtYXJ0aW4iLCJzY29wZSI6WyJyZWFkIiwid3JpdGUiXSwiYXBlbGxpZG8iOiJEw61heiIsImNvcnJlbyI6Im1hcnRpbkBtYWdhZGlmbG8uY29tIiwiZXhwIjoxNjg0MjkwNjAyLCJub21icmUiOiJNYXJ0w61uIiwiYXV0aG9yaXRpZXMiOlsiUk9MRV9VU0VSIl0sImp0aSI6ImY0MDA3OWM2LTA2OTctNDM1Yy1hMjYxLTRlNzlhOWYzMjFiYSIsImNsaWVudF9pZCI6ImZyb250ZW5kQXBwIn0.TtIaiXHPxqs1O0ch_M1-_f2BR_kekbKsc3_HEN8cpP8",
    "token_type": "bearer",
    "refresh_token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VyX25hbWUiOiJtYXJ0aW4iLCJzY29wZSI6WyJyZWFkIiwid3JpdGUiXSwiYXBlbGxpZG8iOiJEw61heiIsImNvcnJlbyI6Im1hcnRpbkBtYWdhZGlmbG8uY29tIiwiYXRpIjoiZjQwMDc5YzYtMDY5Ny00MzVjLWEyNjEtNGU3OWE5ZjMyMWJhIiwiZXhwIjoxNjg0MjkwNjAyLCJub21icmUiOiJNYXJ0w61uIiwiYXV0aG9yaXRpZXMiOlsiUk9MRV9VU0VSIl0sImp0aSI6IjAwNTNiYzYzLThhOTUtNDg3ZS05OTc1LTBjOTAzMWFiNTRmYiIsImNsaWVudF9pZCI6ImZyb250ZW5kQXBwIn0.URoWH5BzWolWDMJhxWxiVvJDSJ9srHkm8iuFol3Tax0",
    "expires_in": 3599,
    "scope": "read write",
    "apellido": "Díaz",
    "correo": "martin@magadiflo.com",
    "nombre": "Martín",
    "jti": "f40079c6-0697-435c-a261-4e79a9f321ba"
}
````

Decodificando el **access_token** (https://jwt.io/), podemos observar que también se ha incluido dentro
del token la información adicional.

````
{
  "user_name": "martin",
  "scope": [
    "read",
    "write"
  ],
  "apellido": "Díaz",
  "correo": "martin@magadiflo.com",
  "exp": 1684290602,
  "nombre": "Martín",
  "authorities": [
    "ROLE_USER"
  ],
  "jti": "f40079c6-0697-435c-a261-4e79a9f321ba",
  "client_id": "frontendApp"
}
````

---

## Creando configuración de OAuth en el servidor de configuración

En el repositorio del servidor de configuraciones agregamos un **application.properties**
con configuraciones compartidas por varios microservicios, en este caso las credenciales que
usará una aplicación cliente, así como la clave para firmar el token.

Entonces, necesitamos que nuestro servidor ms-authorization-server se comunique con el servidor de configuraciones
para obtener dichas configuraciones. Agregamos la dependencia de config client:

````
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-config</artifactId>
</dependency>
````

Ahora, en el **application.properties** de nuestro **ms-authorization-server** agregamos
las configuraciones que apunten al servidor de configuraciones:

````
# Configuracion al servidor de configuraciones
spring.config.import=optional:configserver:http://localhost:8888

# Habilita los endpoints de Spring Actuator
management.endpoints.web.exposure.include=*
````

La segunda configuración de **Spring Actuator**, es por si agregamos la dependencia
de Spring Actuator a este microservicio con la finalidad de poder actualizar los valores
de las configuraciones sin necesidad de reiniciar la aplicación.

Finalmente en nuestra clase **AuthorizationServerConfig** estamos **usando la llave para firmar nuestro token**,
y además las **credenciales que deberá enviarnos la aplicación cliente**, dichas credenciales ya la colocamos en el
repositorio del servidor de configuraciones, así que para poder acceder a ellos, necesitamos usar o el **Environment** o
la
inyección a través del **@Value**, en este caso usamos el **Environment**:

````
/* más código */
private final Environment environment;

public AuthorizationServerConfig(PasswordEncoder passwordEncoder, AuthenticationManager authenticationManager, InfoAdicionalToken infoAdicionalToken, Environment environment) {
    this.passwordEncoder = passwordEncoder;
    this.authenticationManager = authenticationManager;
    this.infoAdicionalToken = infoAdicionalToken;
    this.environment = environment;
}

/* más código */
````

Ahora, cambiamos los valores que están hardcodeados por las que ya están en el servidor de configuración:

````
@Override
public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
    clients.inMemory()
            .withClient(this.environment.getProperty("config.security.oauth.client.id")) <------ Username de la aplicación cliente
            .secret(this.passwordEncoder.encode(this.environment.getProperty("config.security.oauth.client.secret"))) <------ Password de la aplicación cliente
            
    /* más código */
}
````

````
@Bean
public JwtAccessTokenConverter jwtAccessTokenConverter() {
    JwtAccessTokenConverter jwtAccessTokenConverter = new JwtAccessTokenConverter();
    jwtAccessTokenConverter.setSigningKey(this.environment.getProperty("config.security.oauth.jwt.key")); <------ Clave secreta para firmar el token
    return jwtAccessTokenConverter;
}
````

Otra modificación que se hizo fue agregar la anotación **@RefreshScope**, como recordaremos, si usamos
actuator eso nos permitirá acceder a una url y a través de ella poder reiniciar las configuraciones que
están siendo aplicadas en esta clase sin necesidad de reiniciar la aplicación.
---

## Solución al error cuando se quiere obtener un nuevo token a partir del refresh token

Imaginemos que queremos obtener un nuevo token a partir del **refresh_token,** ya que el token que tenemos,
digamos ha expirado. Al tratar de obtener el nuevo token, nos muestra el siguiente error:

````
java.lang.IllegalStateException: UserDetailsService is required
````

La solución claramente indica que se necesita la implementación del **UserDetailsService**, por lo tanto,
aplicaremos la siguiente solución, tomando como referencia la solución brindada por un estudiante del curso
(solución en la sección 9: Spring Cloud Security: implementando OAuth2 y JWT, clase: 102. Revisando el token de
actualización). En la solución del estudiante, modifica la interfaz **IUsuarioService** para que esta interfaz
extienda de UserDetailsService, etc...

En mi caso, no modificaré ninguna interfaz, ya que lo que nos está pidiendo es tener un **UserDetailsService**,
por lo tanto, en la clase de configuración **AuthorizationServerConfig** aplicamos inyección de dependencias
vía constructor del **UserDetailsService**, quien al final tomará la implementación que hicimos en la clase
**UsuarioService**. Finalmente, la variable inyectada lo agregaremos en el método
`` configure(AuthorizationServerEndpointsConfigurer endpoints)``, esto último sí es similar a cómo lo hizo
el estudiante:

````
@RefreshScope
@EnableAuthorizationServer
@Configuration
public class AuthorizationServerConfig extends AuthorizationServerConfigurerAdapter {
  /* más código */
  private final UserDetailsService userDetailsService;

  public AuthorizationServerConfig(/* más código */, UserDetailsService userDetailsService) {
      /* más código */
      this.userDetailsService = userDetailsService;
  }
  
  /* más código */
  
  @Override
  public void configure(AuthorizationServerEndpointsConfigurer endpoints) throws Exception {
      TokenEnhancerChain tokenEnhancerChain = new TokenEnhancerChain();
      tokenEnhancerChain.setTokenEnhancers(List.of(this.infoAdicionalToken, this.jwtAccessTokenConverter()));

      endpoints.authenticationManager(this.authenticationManager)
              .tokenStore(this.jwtTokenStore())
              .accessTokenConverter(this.jwtAccessTokenConverter())
              .tokenEnhancer(tokenEnhancerChain)
              .userDetailsService(this.userDetailsService); <--------------- Agregando el UserDetailsService requerida
  }
  
  /* más código */
  
}
````

### Probando el refresh_token desde Postman

````
[POST] http://127.0.0.1:8090/api-base/authorization-server-base/oauth/token
````

**DONDE**

- **/api-base/authorization-server-base/**, corresponde a la ruta configurada en el
  application.yml del ms-spring-cloud-gateway.
- **/oauth/token**, es la ruta propia del servidor de autorización.

````
REQUEST

Authorization
-------------
Type: Basic Auth
Username: frontendApp
Password: frontendApp-12345

Por detrás, Postman generará el siguiente header para los datos de la Authorization anterior:

Headers
-------
Key= Authorization
Value= Basic ZnJvbnRlbmRBcHA6ZnJvbnRlbmRBcHAtMTIzNDU=

Donde el código mostrado se obtiene del username y password separado por dos puntos
codificado en base64:
  frontendApp:frontendApp-12345 --> convertido a base64 --> ZnJvbnRlbmRBcHA6ZnJvbnRlbmRBcHAtMTIzNDU=
  
Body
----
[*] x-www-form-urlencoded
Key             Value
----------      --------
grant_type      refresh_token
refresh_token   eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VyX25hbWUiOiJhZG1pbiIsInNjb3BlIjpbInJlYWQiLCJ3cml0ZSJdLCJhcGVsbGlkbyI6IkFkbWluIiwiY29ycmVvIjoiYWRtaW5AbWFnYWRpZmxvLmNvbSIsImF0aSI6ImE2N2ViMDc0LTY2NDYtNGRkMC1hYWM4LTAyZTljMWQzZGY4NyIsImV4cCI6MTY4NDM3NDAyMywibm9tYnJlIjoiQWRtaW4iLCJhdXRob3JpdGllcyI6WyJST0xFX0FETUlOIiwiUk9MRV9VU0VSIl0sImp0aSI6ImNlYWQwMWM4LTQzZmQtNGJjNC04Nzc1LTM5Zjg1YWM0YjI5MSIsImNsaWVudF9pZCI6ImZyb250ZW5kQXBwIn0.4Sc6Eh8xfFrH8CXcT6uDCd1K_22pcN89eyBTaXZyRfs
````

**DONDE**

- En el **headers** enviamos usando **Http Basic Auth** las credenciales de la aplicación
  cliente configurada dentro del servidor de autorización.
- En el **body**, usando **x-www-form-urlencoded** enviamos el tipo de concesión (refresh_token)
  y el valor del refresh_token.
- Notar que el tipo de **grant_type** es **refresh_token**, tal como lo configuramos en el servidor
  de autorización, esto nos indica que si nosotros le proporcionamos el valor de un refresh_token, este nos
  va a devolver un nuevo token.

````
RESPONSE

{
    "access_token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VyX25hbWUiOiJhZG1pbiIsInNjb3BlIjpbInJlYWQiLCJ3cml0ZSJdLCJhcGVsbGlkbyI6IkFkbWluIiwiY29ycmVvIjoiYWRtaW5AbWFnYWRpZmxvLmNvbSIsImV4cCI6MTY4NDM3NDEwMCwibm9tYnJlIjoiQWRtaW4iLCJhdXRob3JpdGllcyI6WyJST0xFX0FETUlOIiwiUk9MRV9VU0VSIl0sImp0aSI6IjAwZGE4NGM3LTBlZWMtNDdmZi1hMTNkLTcxN2Y0YmJmZmRjMSIsImNsaWVudF9pZCI6ImZyb250ZW5kQXBwIn0.5Tlga71jBhEny_mnYHtZn_YKHu5PH6wIY_KZFpash6Q",
    "token_type": "bearer",
    "refresh_token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VyX25hbWUiOiJhZG1pbiIsInNjb3BlIjpbInJlYWQiLCJ3cml0ZSJdLCJhcGVsbGlkbyI6IkFkbWluIiwiY29ycmVvIjoiYWRtaW5AbWFnYWRpZmxvLmNvbSIsImF0aSI6IjAwZGE4NGM3LTBlZWMtNDdmZi1hMTNkLTcxN2Y0YmJmZmRjMSIsImV4cCI6MTY4NDM3NDAyMywibm9tYnJlIjoiQWRtaW4iLCJhdXRob3JpdGllcyI6WyJST0xFX0FETUlOIiwiUk9MRV9VU0VSIl0sImp0aSI6ImNlYWQwMWM4LTQzZmQtNGJjNC04Nzc1LTM5Zjg1YWM0YjI5MSIsImNsaWVudF9pZCI6ImZyb250ZW5kQXBwIn0.CADuCrHP--MyIPkroNei6jWOTuwrlNV4C0ILkaP7cP8",
    "expires_in": 3599,
    "scope": "read write",
    "apellido": "Admin",
    "correo": "admin@magadiflo.com",
    "nombre": "Admin",
    "jti": "00da84c7-0eec-47ff-a13d-717f4bbffdc1"
}
````

---

## Manejando eventos de éxito y fracaso en la autenticación

En este caso, crearemos una clase de componente e implementaremos dos métodos que nos indicarán error o
éxito cuando un usuario intente hacer login.

Primero crearemos nuestra clase de componente implementando la interfaz AuthenticationEventPublisher:

````
@Component
public class AuthenticationSuccessErrorHandler implements AuthenticationEventPublisher {

    private final static Logger LOG = LoggerFactory.getLogger(AuthenticationSuccessErrorHandler.class);

    @Override
    public void publishAuthenticationSuccess(Authentication authentication) {
        if(authentication.getDetails() instanceof WebAuthenticationDetails) return;

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        LOG.info("Login exitoso!: {}", userDetails.getUsername());
    }

    @Override
    public void publishAuthenticationFailure(AuthenticationException exception, Authentication authentication) {
        LOG.error("Error en el login: {}", exception.getMessage());
    }
}
````

Como se observa en el código anterior, el primer método ocurrirá cuando la autenticación sea exitosa, mientras
que el segundo método cuando falle.

> Notar que en el método de autenticación exitosa estamos realizando una validación:
``if(authentication.getDetails() instanceof WebAuthenticationDetails) return;``. En versiones actuales de OAuth2, cuando
> se ejecuta este método **publishAuthenticationSuccess(..)**, pasan las instancias, tanto del cliente App como la del
> usuario que se logueó. En este caso, nosotros queremos tratar únicamente con los datos del usuario y no de
> la aplicación cliente, que sabemos que siempre tendrá como credenciales: username: frontendApp, password:
> frontendApp-12345.

Ahora, en el curso de Andrés Guzmán, la configuración la realiza dentro de la clase principal de
configuración de Spring Security, en el método **configure(AuthenticationManagerBuilder auth)**.
La configuración que realiza en el curso queda así:

````
@Configuration
public class SpringSecurityConfig extends WebSecurityConfigurerAdapter {
    /* más código */	
    private final AuthenticationEventPublisher authenticationEventPublisher;

	public SpringSecurityConfig(/* más código */, AuthenticationEventPublisher authenticationEventPublisher) {
		this.authenticationEventPublisher = authenticationEventPublisher;
	}
	
    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(this.usuarioService).passwordEncoder(passwordEncoder())
            .and()
            .authenticationEventPublisher(this.authenticationEventPublisher); <------- Aquí se está usando el componente inyectado
    }
    
}
````

En nuestro caso, recordemos que tomamos como referencia el libro **Spring Security In Action 2020** para decidir
y no mezclar las configuraciones ya sea o usando la configuración anterior o usando las anotaciones @Bean para trabajar
con el **UserDetailsService y PasswordEncoder**. Nuestra elección fue trabajar con las anotaciones @Bean para definir
en una clase de configuración los Bean y Service propios del UserDetailsService y PasswordEncoder, de esa forma ya no
usaríamos el método de configuración anterior (configure(AuthenticationManagerBuilder auth)).

Hasta este punto está funcionando bien, pero resulta que ahora al crear una implementación del
**AuthenticationEventPublisher**, en el curso de Andrés Guzmán se lo usa dentro de nuestra clase de configuración
principal de Spring Security, justamente en el método **configure(AuthenticationManagerBuilder auth)**, pero nosotros
estamos usando @Bean y @Service en reemplazo de ese método de configuración. **¿Cómo haremos?**, bueno resulta que
al generar una implementación del **AuthenticationEventPublisher** y anotarlo con **@Component**, este se va a registrar
en el contenedor de Spring, de esta forma, en automático será manejado por el **AuthenticationManager**.