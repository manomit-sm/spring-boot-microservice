package com.bsolz.microservices.composite.product.services;

import com.bsolz.api.core.product.Product;
import com.bsolz.api.core.product.ProductService;
import com.bsolz.api.core.recommendation.Recommendation;
import com.bsolz.api.core.recommendation.RecommendationService;
import com.bsolz.api.core.review.Review;
import com.bsolz.api.core.review.ReviewService;
import com.bsolz.api.exceptions.InvalidInputException;
import com.bsolz.api.exceptions.NotFoundException;
import com.bsolz.util.http.HttpErrorInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static org.springframework.http.HttpMethod.GET;

@Service
public class ProductCompositeIntegration implements ProductService, RecommendationService, ReviewService {

    private static final Logger LOG = LoggerFactory.getLogger(ProductCompositeIntegration.class);

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final String productServiceUrl;
    private final String recommendationServiceUrl;
    private final String reviewServiceUrl;




    public ProductCompositeIntegration(
        RestTemplate restTemplate,
        ObjectMapper objectMapper,
        @Value("${app.product-service.host}") String productServiceHost,

        @Value("${app.recommendation-service.host}") String recommendationServiceHost,

        @Value("${app.review-service.host}") String reviewServiceHost

    ) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;

        productServiceUrl = "http://" + productServiceHost +  "/product/";
        recommendationServiceUrl = "http://" + recommendationServiceHost + "/recommendation?productId=";
        reviewServiceUrl = "http://" + reviewServiceHost  + "/review?productId=";
    }

    @Override
    public Product getProduct(int productId) {
        try {
            String url = productServiceUrl + productId;
            LOG.debug("Will call getProduct API on URL: {}", url);

            Product product = restTemplate.getForObject(url, Product.class);
            LOG.debug("Found a product with id: {}", product.productId());

            return product;

        } catch (HttpClientErrorException ex) {

            switch (Objects.requireNonNull(HttpStatus.resolve(ex.getStatusCode().value()))) {
                case NOT_FOUND:
                    throw new NotFoundException(getErrorMessage(ex));

                case UNPROCESSABLE_ENTITY:
                    throw new InvalidInputException(getErrorMessage(ex));

                default:
                    LOG.warn("Got an unexpected HTTP error: {}, will rethrow it", ex.getStatusCode());
                    LOG.warn("Error body: {}", ex.getResponseBodyAsString());
                    throw ex;
            }
        }
    }

    @Override
    public Product createProduct(Product body) {
        try {
            return restTemplate.postForObject(
                    productServiceUrl, body, Product.class
            );
        } catch (HttpClientErrorException ex) {
            throw handleHttpClientException(ex);
        }
    }

    @Override
    public void deleteProduct(int productId) {
        try {
            restTemplate.delete(productServiceUrl + "/" + productId);
        } catch (HttpClientErrorException ex) {
            throw handleHttpClientException(ex);
        }
    }

    @Override
    public List<Recommendation> getRecommendation(int productId) {
        try {
            String url = recommendationServiceUrl + productId;

            LOG.debug("Will call getRecommendations API on URL: {}", url);
            List<Recommendation> recommendations = restTemplate
                    .exchange(url, GET, null, new ParameterizedTypeReference<List<Recommendation>>() {})
                    .getBody();

            LOG.debug("Found {} recommendations for a product with id: {}", recommendations.size(), productId);
            return recommendations;

        } catch (Exception ex) {
            LOG.warn("Got an exception while requesting recommendations, return zero recommendations: {}", ex.getMessage());
            return Collections.emptyList();
        }
    }

    @Override
    public List<Review> getReviews(int productId) {
        try {
            String url = reviewServiceUrl + productId;

            LOG.debug("Will call getReviews API on URL: {}", url);
            List<Review> reviews = restTemplate
                    .exchange(url, GET, null, new ParameterizedTypeReference<List<Review>>() {})
                    .getBody();

            LOG.debug("Found {} reviews for a product with id: {}", reviews.size(), productId);
            return reviews;

        } catch (Exception ex) {
            LOG.warn("Got an exception while requesting reviews, return zero reviews: {}", ex.getMessage());
            return Collections.emptyList();
        }
    }

    private String getErrorMessage(HttpClientErrorException ex) {
        try {
            return objectMapper.readValue(ex.getResponseBodyAsString(), HttpErrorInfo.class).message();
        } catch (IOException ioex) {
            return ioex.getMessage();
        }
    }

    private RuntimeException handleHttpClientException(HttpClientErrorException ex) {
        switch (HttpStatus.resolve(ex.getStatusCode().value())) {

            case NOT_FOUND:
                return new NotFoundException(getErrorMessage(ex));

            case UNPROCESSABLE_ENTITY:
                return new InvalidInputException(getErrorMessage(ex));

            default:
                LOG.warn("Got an unexpected HTTP error: {}, will rethrow it", ex.getStatusCode());
                LOG.warn("Error body: {}", ex.getResponseBodyAsString());
                return ex;
        }
    }
}
