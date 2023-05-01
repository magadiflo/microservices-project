# Spring Cloud Gateway

# Implementando Filtros Globales

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