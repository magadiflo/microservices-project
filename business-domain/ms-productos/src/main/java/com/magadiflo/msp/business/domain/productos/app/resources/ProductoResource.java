package com.magadiflo.msp.business.domain.productos.app.resources;

import com.magadiflo.msp.business.domain.productos.app.models.entity.Producto;
import com.magadiflo.msp.business.domain.productos.app.service.IProductoService;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping(path = "/api/v1/productos")
public class ProductoResource {
    private final IProductoService productoService;
    private final Environment environment;

    public ProductoResource(IProductoService productoService, Environment environment) {
        this.productoService = productoService;
        this.environment = environment;
    }

    @GetMapping
    public ResponseEntity<List<Producto>> listarProductos() {
        return ResponseEntity.ok(this.productoService.findAll().stream().map(this::productoConPuerto).toList());
    }

    @GetMapping(path = "/{id}")
    public ResponseEntity<Producto> verProducto(@PathVariable Long id) throws InterruptedException {
        //* Simulando errores (Usuario no encontrado y demora en la ejecución del método)
        if (id.equals(10L)) throw new IllegalStateException("Producto no encontrado!");
        if (id.equals(7L)) TimeUnit.SECONDS.sleep(5L);
        //* Simulando errores
        Producto producto = this.productoService.findById(id);
        return ResponseEntity.ok(this.productoConPuerto(producto));
    }

    @PostMapping
    public ResponseEntity<Producto> crearProducto(@RequestBody Producto producto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(this.productoConPuerto(this.productoService.save(producto)));
    }

    @PutMapping(path = "/{id}")
    public ResponseEntity<Producto> actualizarProducto(@PathVariable Long id, @RequestBody Producto producto) {
        Producto productoBD = this.productoService.findById(id);
        productoBD.setNombre(producto.getNombre());
        productoBD.setPrecio(producto.getPrecio());
        return ResponseEntity.ok(this.productoService.save(productoBD));
    }

    @DeleteMapping(path = "/{id}")
    public ResponseEntity<Void> eliminarProducto(@PathVariable Long id) {
        this.productoService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    private Producto productoConPuerto(Producto producto) {
        producto.setPort(Integer.valueOf(Objects.requireNonNull(this.environment.getProperty("local.server.port"))));
        return producto;
    }
}
