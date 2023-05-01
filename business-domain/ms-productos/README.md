# Microservicio Producto

## Ejecutando múltiples instancias en IntelliJ IDEA (definiendo puerto)

- En el curso que estoy llevando **(Microservicios con Spring Boot y Spring Cloud Netflix Eureka)**
  sección 2: Microservicios: la base, capítulo 18. Balaneo de carga del lado del cliente con Ribbon,
  minuto 13:12 ejecuta múltiples instancias del mismo proyecto pero en distintos puertos. Los puertos
  las ingresa manualmente mediante STS.
- En mi caso, estoy usando IntelliJ IDEA, y la misma opción se encuentra siguiendo estos pasos:

````
PRIMERO:
Ejecutamos el proyecto como normalmente lo hacemos, se levantará en el puerto 8001

SEGUNDO:
Para ejecutar otra instancia del proyecto, es necesario cambiar el puerto ya que ya hay
una instancia ejecutándose en el puero definido (8001).

PASOS:
- Nos vamos a Edit Configurations...
- En el lado izquierdo seleccionamos la configuración que está ejecutándose con el puerto 8001.
- Clickeamos en Copy Configuration
- Seleccionamos la nueva configuración copiada
- Agregamos un nombre a la configuración: Ejm. MsProductosApplication 9001
- Click en Modify options
- Seleccionamos Add VM options
- Se agregará un nuevo campo de texto. Escribimos el comando para cambiar el puerto
	-Dserver.port=9001
- Apply y OK

Ejecutando nueva instancia
----------------------------
Seleccionamos nuestra nueva configuración y ejecutamos el proyecto.
````

## Ejecutando múltiples instancias en IntelliJ IDEA (Puerto dinámico)

- Para generar un puerto dinámico a la aplicación, es necesario agregar la siguiente
  configuración en el archivo de propiedades ```server.port=${PORT:0}```, donde **PORT**
  es una variable de ambiente manejado por Spring y **0** es un valor por defecto.
- Ahora, en **Edit Configurations...** de IntelliJ IDEA agregamos el número de configuraciones
  (de instancias) que queremos ejecutar. Yo crearé 5 configuraciones porque quiero 5 instancias.
- Por defecto, debe haber alguna configuración, pero sino lo hay, entonces damos en:

````
- Click en + (Add new configuration)
- Seleccionar Appication
- Name, le agregamos un nombre a la configuración. Ejmpl. MsProductosApplication (1)
- Build and run, input Main Class, agregar nuestra clase principal: com.magadiflo.msp.business.domain.productos.app.MsProductosApplication
- Apply and OK, ya tenemos nuestra primera instancia.
- Ahora, ingresamos nuevamente a Edit Configurations y copiamos 5 veces la misma configuración (el n° que necesite)
- Apply and OK.
- Ahora, para ejecutar cada instancia, debo ir seleccionando configuración por configuración.
````

## [Identificador único para instancias](https://cloud.spring.io/spring-cloud-netflix/multi/multi__service_discovery_eureka_clients.html)

Al usar Spring Cloud, puede anular el valor de la instancia única proporcionada por **Spring Cloud Eureka**
al proporcionar un identificador único en **eureka.instance.instanceId**, como lo hicimos en el archivo
de propiedades:

````
eureka.instance.instance-id=${spring.application.name}:${spring.application.instance_id:${random.value}}
````

La configuración anterior se utiliza para identificar de forma única una sola instancia de múltiples servicios.
Por ejmp. si implementó 2x instancias de la misma aplicación de Spring Boot, la instance-id se usa para distinguirlas.
La propiedad mostrada arriba simplemente toma las otras propiedades, las combina. El único problema es que si no se
encuentra
un spring.application.instance_id en el entorno, utilizará un valor aleatorio en su lugar. Puede anularlo como desee,
pero debe ser único.