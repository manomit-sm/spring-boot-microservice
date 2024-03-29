package com.bsolz.microservices.composite.product.services;

import com.bsolz.api.composite.*;
import com.bsolz.api.core.product.Product;
import com.bsolz.api.core.recommendation.Recommendation;
import com.bsolz.api.core.review.Review;
import com.bsolz.api.exceptions.NotFoundException;
import com.bsolz.util.http.ServiceUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
public class ProductCompositeServiceImpl implements ProductCompositeService {

    private static final Logger LOG = LoggerFactory.getLogger(ProductCompositeServiceImpl.class);
    private final ServiceUtil serviceUtil;
    private final ProductCompositeIntegration integration;

    public ProductCompositeServiceImpl(
        ServiceUtil serviceUtil,
        ProductCompositeIntegration integration
    ) {
        this.serviceUtil = serviceUtil;
        this.integration = integration;
    }

    @Override
    public ProductAggregate getProduct(int productId) {
        Product product = integration.getProduct(productId);
        if (product == null) {
            throw new NotFoundException("No product found for productId: " + productId);
        }

        List<Recommendation> recommendations = integration.getRecommendation(productId);

        List<Review> reviews = integration.getReviews(productId);

        return createProductAggregate(product, recommendations, reviews, serviceUtil.getServiceAddress());
    }

    @Override
    public void createProduct(ProductAggregate body) {
        try {
            Product product = new Product(body.productId(), body.name(), body.weight(), null);
            integration.createProduct(product);
            if (body.recommendations() != null) {
                body.recommendations().forEach(r -> {
                    Recommendation recommendation = new
                            Recommendation(body.productId(),
                            r.recommendationId(), r.author(), r.rate(),
                            null, null);
                    // integration.createRecommendation(recommendation);
                });
            }
            if (body.reviews() != null) {
                body.reviews().forEach(r -> {
                    Review review = new Review(body.productId(),
                            r.reviewId(), r.author(), r.subject(),
                            null, null);
                    // integration.createReview(review);
                });
            }
        } catch (RuntimeException ex) {
            LOG.warn("createCompositeProduct failed", ex);
            throw ex;
        }
    }

    @Override
    public void deleteProduct(int productId) {
        integration.deleteProduct(productId);
        // integration.deleteRecommendations(productId);
        // integration.deleteReviews(productId);
    }

    private ProductAggregate createProductAggregate(
            Product product,
            List<Recommendation> recommendations,
            List<Review> reviews,
            String serviceAddress) {

        // 1. Setup product info
        int productId = product.productId();
        String name = product.name();
        int weight = product.weight();

        // 2. Copy summary recommendation info, if available
        List<RecommendationSummary> recommendationSummaries =
                (recommendations == null) ? null : recommendations.stream()
                        .map(r -> new RecommendationSummary(r.recommendationId(), r.author(), r.rate()))
                        .collect(Collectors.toList());

        // 3. Copy summary review info, if available
        List<ReviewSummary> reviewSummaries =
                (reviews == null) ? null : reviews.stream()
                        .map(r -> new ReviewSummary(r.reviewId(), r.author(), r.subject()))
                        .collect(Collectors.toList());

        // 4. Create info regarding the involved microservices addresses
        String productAddress = product.serviceAddress();
        String reviewAddress = (reviews != null && !reviews.isEmpty()) ? reviews.get(0).serviceAddress() : "";
        String recommendationAddress = (recommendations != null && !recommendations.isEmpty()) ? recommendations.get(0).serviceAddress() : "";
        ServiceAddresses serviceAddresses = new ServiceAddresses(serviceAddress, productAddress, reviewAddress, recommendationAddress);

        return new ProductAggregate(productId, name, weight, recommendationSummaries, reviewSummaries, serviceAddresses);
    }
}
