# Repaso - Microservicios con Spring Boot y Spring Cloud Netflix Eureka

El curso que completé lo desarrollé con STS, mientras que en esta oportunidad,
a modo de repaso, trabajaré los microservicios con **módulos de maven** para poder
abrirlos con IntelliJ IDEA.

# Estructura creada

- **business-domain**: Aquí irán todos los microservicios que formen parte del dominio del negocio, como
  **ms-products, ms-items, etc...**.
- **infrastructure**: Aquí irán los ms que son parte fundamental de una arquitectura de microservicios, como
  **Eureka Server, Spring Cloud Load Balanced, Resilience4j, etc..**.
- **legacy**: Aquí irán las dependencias que anteriormente se usaban y que solo están disponibles
  para cierta versión antigua de Spring Boot, tal es el caso de: **Ribbon, Histryx, Zuul, etc..**.
