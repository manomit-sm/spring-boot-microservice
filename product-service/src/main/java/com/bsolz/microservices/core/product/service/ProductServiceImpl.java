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
import reactor.core.publisher.Mono;

import static java.util.logging.Level.FINE;

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
    public Mono<Product> getProduct(int productId) {
        LOG.debug("/product return the found product for productId={}", productId);

        if (productId < 1) throw new InvalidInputException("Invalid productId: " + productId);
        /* ProductEntity entity = repository.findByProductId(productId).blockOptional()
                .orElseThrow(() -> new NotFoundException("No product found for productId: " + productId));

        return mapper.entityToApi(entity); */
        return repository.findByProductId(productId)
                .switchIfEmpty(Mono.error(new NotFoundException("No product found for productId: " + productId)))
                .log(LOG.getName(), FINE)
                .map(e -> mapper.entityToApi(e));

    }

    @Override
    public Mono<Product> createProduct(Product body) {
        if (body.productId() < 1) {
            throw new InvalidInputException("Invalid productId: " +
                    body.productId());
        }
        ProductEntity entity = mapper.apiToEntity(body);
        Mono<Product> newEntity = repository.save(entity)
                .log(LOG.getName(), FINE)
                .onErrorMap(
                        DuplicateKeyException.class,
                        ex -> new InvalidInputException
                                ("Duplicate key, Product Id: " + body.productId()))
                .map(e -> mapper.entityToApi(e));
        return newEntity;
    }

    @Override
    public void deleteProduct(int productId) {
        repository.findByProductId(productId).blockOptional().ifPresent(repository::delete);
    }

}
