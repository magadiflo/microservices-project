package dev.magadiflo.product.app.service.impl;

import dev.magadiflo.product.app.constan.ProductConstant;
import dev.magadiflo.product.app.entity.Product;
import dev.magadiflo.product.app.model.dto.ProductRequest;
import dev.magadiflo.product.app.model.dto.ProductResponse;
import dev.magadiflo.product.app.repository.ProductRepository;
import dev.magadiflo.product.app.service.ProductService;
import dev.magadiflo.product.app.util.ProductMapper;
import dev.magadiflo.product.app.util.Util;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Slf4j
@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class ProductServiceImpl implements ProductService {

    private final ProductMapper productMapper;
    private final ProductRepository productRepository;
    private final Environment environment;

    @Override
    public List<ProductResponse> findProducts() {
        log.info("Buscando todos los productos");
        return ((List<Product>) this.productRepository.findAll()).stream()
                .map(product -> this.productMapper.toProductResponse(product, this.getLocalServerPort()))
                .toList();
    }

    @Override
    public ProductResponse findProduct(Long productId) {
        log.info("Buscando el producto con id {}", productId);
        return this.productRepository.findById(productId)
                .map(product -> this.productMapper.toProductResponse(product, this.getLocalServerPort()))
                .orElseThrow(() -> {
                    log.warn("No se encontró el producto con id: {}", productId);
                    return new NoSuchElementException(ProductConstant.NO_SUCH_ELEMENT_MESSAGE.formatted(productId));
                });
    }

    @Override
    @Transactional
    public ProductResponse saveProduct(ProductRequest request) {
        log.info("Guardando producto: {}", request);
        Product productDB = this.productRepository.save(this.productMapper.toProduct(request));
        log.info("Producto guardado: {}", productDB);
        return this.productMapper.toProductResponse(productDB, this.getLocalServerPort());
    }

    @Override
    @Transactional
    public ProductResponse updateProduct(Long productId, ProductRequest request) {
        log.info("Actualizando producto con id {}, datos a actualizar: {}", productId, request);
        return this.productRepository.findById(productId)
                .map(productDB -> this.productMapper.toUpdateProduct(productDB, request))
                .map(this.productRepository::save)
                .map(product -> this.productMapper.toProductResponse(product, this.getLocalServerPort()))
                .orElseThrow(() -> {
                    log.warn("No se encontró el producto con id: {} para ser actualizado", productId);
                    return new NoSuchElementException(ProductConstant.NO_SUCH_ELEMENT_MESSAGE.formatted(productId));
                });
    }

    @Override
    @Transactional
    public void deleteProduct(Long productId) {
        log.info("Eliminando producto con id: {}", productId);
        Product productDB = this.productRepository.findById(productId)
                .orElseThrow(() -> {
                    log.warn("No se encontró el producto con id: {} para ser eliminado", productId);
                    return new NoSuchElementException(ProductConstant.NO_SUCH_ELEMENT_MESSAGE.formatted(productId));
                });
        this.productRepository.deleteById(productDB.getId());
    }

    private int getLocalServerPort() {
        log.info("Obteniendo el puerto donde se ejecuta este microservicio");
        return Util.getInt(this.environment.getProperty("local.server.port"));
    }
}
