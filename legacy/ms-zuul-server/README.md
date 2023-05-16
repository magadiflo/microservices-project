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
