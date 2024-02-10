package com.bsolz.microservices.core.product.service;

import com.bsolz.api.core.product.Product;
import com.bsolz.api.core.product.ProductService;
import com.bsolz.api.exceptions.InvalidInputException;
import com.bsolz.api.exceptions.NotFoundException;
import com.bsolz.microservices.core.product.mapper.ProductMapper;
import com.bsolz.microservices.core.product.persistence.ProductEntity;
import com.bsolz.microservices.core.product.persistence.ProductRepository;
import com.bsolz.util.http.ServiceUtil;
import com.mongodb.DuplicateKeyException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ProductServiceImpl implements ProductService {

    private static final Logger LOG = LoggerFactory.getLogger(ProductServiceImpl.class);

    private final ServiceUtil serviceUtil;
    private final ProductRepository repository;
    private final ProductMapper mapper;

    public ProductServiceImpl(
            ServiceUtil serviceUtil,
            ProductRepository repository,
            ProductMapper mapper
    ) {
        this.serviceUtil = serviceUtil;
        this.repository = repository;
        this.mapper = mapper;
    }
    @Override
    public Product getProduct(int productId) {
        LOG.debug("/product return the found product for productId={}", productId);

        if (productId < 1) throw new InvalidInputException("Invalid productId: " + productId);
        ProductEntity entity = repository.findByProductId(productId)
                .orElseThrow(() -> new NotFoundException("No product found for productId: " + productId));

        return mapper.entityToApi(entity);
    }

    @Override
    public Product createProduct(Product body) {
        try {
            ProductEntity entity = mapper.apiToEntity(body);
            ProductEntity newEntity = repository.save(entity);
            return mapper.entityToApi(newEntity);
        } catch (DuplicateKeyException exception) {
            throw new InvalidInputException("Duplicate key, Product Id: " +
                    body.productId());
        }
    }

    @Override
    public void deleteProduct(int productId) {
        repository.findByProductId(productId).ifPresent(repository::delete);
    }

}
