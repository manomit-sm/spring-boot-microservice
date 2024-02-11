package com.bsolz.microservices.core.product;

import com.bsolz.microservices.core.product.persistence.ProductEntity;
import com.bsolz.microservices.core.product.persistence.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.testcontainers.shaded.org.yaml.snakeyaml.constructor.DuplicateKeyException;

import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.toList;
import static java.util.stream.IntStream.rangeClosed;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.data.domain.Sort.Direction.ASC;

@DataMongoTest(properties = {"spring.cloud.config.enabled=false"})
public class PersistenceTests {

    @Autowired
    private ProductRepository repository;

    private ProductEntity productEntity;

    @BeforeEach
    void setupDb() {
        repository.deleteAll();
        ProductEntity entity = new ProductEntity();
        entity.setProductId(1);
        entity.setName("N");
        entity.setWeight(1);
        productEntity = repository.save(entity);

    }

    @Test
    void create() {
        ProductEntity newEntity = new ProductEntity();
        newEntity.setProductId(2);
        newEntity.setName("N");
        newEntity.setWeight(2);
        repository.save(newEntity);
        Optional<ProductEntity> foundEntity =
                repository.findById(newEntity.getId());

        assertEquals(2, repository.count());
    }

    @Test
    void update() {
        productEntity.setName("n2");
        repository.save(productEntity);
        ProductEntity foundEntity =
                repository.findById(productEntity.getId()).get();
        assertEquals(1, (long)foundEntity.getVersion());
        assertEquals("n2", foundEntity.getName());
    }

    @Test
    void delete() {
        repository.delete(productEntity);
        assertFalse(repository.existsById(productEntity.getId()));
    }

    @Test
    void getByProductId() {
        Optional<ProductEntity> entity =
                repository.findByProductId(productEntity.getProductId());
        assertTrue(entity.isPresent());

    }
    @Test
    void duplicateKeyError() {
        assertThrows(DuplicateKeyException.class, () -> {
            ProductEntity entity = new ProductEntity();
            entity.setProductId(1);
            entity.setName("D");
            entity.setWeight(1);
            repository.save(entity);
        });
    }

    @Test
    void optimisticLockError() {
        ProductEntity entity1 = repository.findByProductId(productEntity.getProductId()).get();
        ProductEntity entity2 = repository.findByProductId(productEntity.getProductId()).get();

        entity1.setName("NN");
        repository.save(entity1);

        assertThrows(OptimisticLockingFailureException.class, () -> {
            entity2.setName("NN");
            repository.save(entity2);
        });

        ProductEntity updatedEntity =
                repository.findById(productEntity.getId()).get();
        assertEquals(1, (int)updatedEntity.getVersion());
        assertEquals("n1", updatedEntity.getName());
    }

    @Test
    void paging() {
        repository.deleteAll();

        List<ProductEntity> newProducts = rangeClosed(1001, 1010)
                .mapToObj(i -> new ProductEntity(i, "name " + i, i))
                .toList();

        repository.saveAll(newProducts);

        Pageable nextPage = PageRequest.of(0, 4, ASC, "productId");
        nextPage = testNextPage(nextPage, "[1001, 1002, 1003, 1004]",
                true);
        nextPage = testNextPage(nextPage, "[1005, 1006, 1007, 1008]",
                true);
        nextPage = testNextPage(nextPage, "[1009, 1010]", false);
    }

    private Pageable testNextPage(Pageable page, String expectedProductIds, boolean expectsNextPage) {
        Page<ProductEntity> productPage = repository.findAll(page);
        assertEquals(
                expectedProductIds,
                productPage.getContent()
                        .stream().map(p -> p.getProductId())
                        .toList().toString()
        );
        return  productPage.nextPageable();
    }
}
