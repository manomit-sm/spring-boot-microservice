package com.bsolz.microservices.core.review.services;

import com.bsolz.api.core.review.Review;
import com.bsolz.api.core.review.ReviewService;
import com.bsolz.api.exceptions.InvalidInputException;
import com.bsolz.microservices.core.review.mapper.ReviewMapper;
import com.bsolz.microservices.core.review.persistence.ReviewEntity;
import com.bsolz.microservices.core.review.persistence.ReviewRepository;
import com.bsolz.util.http.ServiceUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;

import java.util.Collections;
import java.util.List;

import static java.util.logging.Level.FINE;

@RestController
public class ReviewServiceImpl implements ReviewService {

    private static final Logger LOG = LoggerFactory.getLogger(ReviewServiceImpl.class);

    private final ServiceUtil serviceUtil;
    private final Scheduler jdbcScheduler;

    private final ReviewRepository repository;

    private final ReviewMapper mapper;

    public ReviewServiceImpl(
            ServiceUtil serviceUtil,
            @Qualifier("jdbcScheduler") Scheduler jdbcScheduler,
            ReviewRepository repository,
            ReviewMapper mapper
    ) {
        this.serviceUtil = serviceUtil;
        this.jdbcScheduler = jdbcScheduler;
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public Flux<Review> getReviews(int productId) {
        if (productId < 1) {
            throw new InvalidInputException("Invalid productId: " + productId);
        }

        LOG.info("Will get reviews for product with id {}", productId);

        return Mono.fromCallable(() -> internalGetReviews(productId))
                .flatMapMany(Flux::fromIterable)
                .log(LOG.getName(), FINE)
                .subscribeOn(jdbcScheduler);
    }

    private List<Review> internalGetReviews(int productId) {
        List<ReviewEntity> entityList = repository.
                findByProductId(productId);
        List<Review> list = mapper.entityListToApiList(entityList);

        LOG.debug("Response size: {}", list.size());
        return list;
    }
}
