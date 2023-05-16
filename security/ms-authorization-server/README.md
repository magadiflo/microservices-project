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