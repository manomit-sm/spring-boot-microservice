package com.bsolz.microservices.core.review.services;

import com.bsolz.api.core.review.Review;
import com.bsolz.api.core.review.ReviewService;
import com.bsolz.api.exceptions.InvalidInputException;
import com.bsolz.util.http.ServiceUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;

@RestController
public class ReviewServiceImpl implements ReviewService {

    private static final Logger LOG = LoggerFactory.getLogger(ReviewServiceImpl.class);

    private final ServiceUtil serviceUtil;

    public ReviewServiceImpl(ServiceUtil serviceUtil) {
        this.serviceUtil = serviceUtil;
    }

    @Override
    public List<Review> getReviews(int productId) {
        if (productId < 1) {
            throw new InvalidInputException("Invalid productId: " + productId);
        }

        if (productId == 213) {
            LOG.debug("No reviews found for productId: {}", productId);
            return Collections.emptyList();
        }

        List<Review> list = List.of(
        new Review(productId, 1, "Author 1", "Subject 1", "Content 1", serviceUtil.getServiceAddress()),
        new Review(productId, 2, "Author 2", "Subject 2", "Content 2", serviceUtil.getServiceAddress()),
        new Review(productId, 3, "Author 3", "Subject 3", "Content 3", serviceUtil.getServiceAddress())
        );

        LOG.debug("/reviews response size: {}", list.size());

        return list;
    }
}
