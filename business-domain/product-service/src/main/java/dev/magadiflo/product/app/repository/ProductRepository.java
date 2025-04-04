package dev.magadiflo.product.app.repository;

import dev.magadiflo.product.app.entity.Product;
import org.springframework.data.repository.CrudRepository;

public interface ProductRepository extends CrudRepository<Product, Long> {
}
