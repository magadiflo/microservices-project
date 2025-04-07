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
        return ((List<Product>) this.productRepository.findAll()).stream()
                .map(product -> this.productMapper.toProductResponse(product, this.getLocalServerPort()))
                .toList();
    }

    @Override
    public ProductResponse findProduct(Long productId) {
        return this.productRepository.findById(productId)
                .map(product -> this.productMapper.toProductResponse(product, this.getLocalServerPort()))
                .orElseThrow(() -> new NoSuchElementException(ProductConstant.NO_SUCH_ELEMENT_MESSAGE.formatted(productId)));
    }

    @Override
    @Transactional
    public ProductResponse saveProduct(ProductRequest request) {
        Product productDB = this.productRepository.save(this.productMapper.toProduct(request));
        return this.productMapper.toProductResponse(productDB, this.getLocalServerPort());
    }

    @Override
    @Transactional
    public ProductResponse updateProduct(Long productId, ProductRequest request) {
        return this.productRepository.findById(productId)
                .map(productDB -> this.productMapper.toUpdateProduct(productDB, request))
                .map(this.productRepository::save)
                .map(product -> this.productMapper.toProductResponse(product, this.getLocalServerPort()))
                .orElseThrow(() -> new NoSuchElementException(ProductConstant.NO_SUCH_ELEMENT_MESSAGE.formatted(productId)));
    }

    @Override
    @Transactional
    public void deleteProduct(Long productId) {
        Product productDB = this.productRepository.findById(productId)
                .orElseThrow(() -> new NoSuchElementException(ProductConstant.NO_SUCH_ELEMENT_MESSAGE.formatted(productId)));
        this.productRepository.deleteById(productDB.getId());
    }

    private int getLocalServerPort() {
        return Util.getInt(this.environment.getProperty("local.server.port"));
    }
}
