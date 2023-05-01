package com.magadiflo.msp.business.domain.productos.app.repository;

import com.magadiflo.msp.business.domain.productos.app.models.entity.Producto;
import org.springframework.data.repository.CrudRepository;

public interface IProductoRepository extends CrudRepository<Producto, Long> {
}
