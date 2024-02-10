package com.bsolz.microservices.composite.product.services;

import com.bsolz.api.core.product.Product;
import com.bsolz.api.core.product.ProductService;
import com.bsolz.api.core.recommendation.Recommendation;
import com.bsolz.api.core.recommendation.RecommendationService;
import com.bsolz.api.core.review.Review;
import com.bsolz.api.core.review.ReviewService;
import com.bsolz.api.exceptions.InvalidInputException;
import com.bsolz.api.exceptions.NotFoundException;
import com.bsolz.microservices.composite.product.events.Event;
import com.bsolz.util.http.HttpErrorInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.mapstruct.ap.internal.util.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;

import static com.bsolz.microservices.composite.product.events.Event.Type.CREATE;
import static java.util.logging.Level.FINE;
import static org.springframework.http.HttpMethod.GET;

@Service
public class ProductCompositeIntegration implements ProductService, RecommendationService, ReviewService {

    private static final Logger LOG = LoggerFactory.getLogger(ProductCompositeIntegration.class);

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final String productServiceUrl;
    private final String recommendationServiceUrl;
    private final String reviewServiceUrl;
    private final WebClient webClient;

    public ProductCompositeIntegration(
        RestTemplate restTemplate,
        ObjectMapper objectMapper,
        @Value("${app.product-service.host}") String productServiceHost,
        @Value("${app.product-service.port}") int productServicePort,
        @Value("${app.recommendation-service.host}") String recommendationServiceHost,
        @Value("${app.recommendation-service.port}") int recommendationServicePort,
        @Value("${app.review-service.host}") String reviewServiceHost,
        @Value("${app.review-service.port}") int reviewServicePort,
        WebClient webClient
    ) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.webClient = webClient;
        productServiceUrl = "http://" + productServiceHost + ":" + productServicePort + "/product/";
        recommendationServiceUrl = "http://" + recommendationServiceHost + ":" + recommendationServicePort + "/recommendation?productId=";
        reviewServiceUrl = "http://" + reviewServiceHost + ":" + reviewServicePort + "/review?productId=";
    }

    @Override
    public Mono<Product> getProduct(int productId) {
        try {
            String url = productServiceUrl + productId;
            LOG.debug("Will call getProduct API on URL: {}", url);

            // Product product = restTemplate.getForObject(url, Product.class);
            // LOG.debug("Found a product with id: {}", product.productId());

            // return product;
            return webClient.get().uri(url).retrieve()
                    .bodyToMono(Product.class)
                    .log(LOG.getName(), FINE);

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
    public Mono<Product> createProduct(Product body) {
        return Mono.fromCallable(() -> {
            sendMessage("products-out-0",
                    new Event(CREATE, body.productId(), body));
            return body;
        }).subscribeOn(publishEventScheduler);
    }
    private void sendMessage(String bindingName, Event event) {
        Message message = MessageBuilder.withPayload(event)
                .setHeader("partitionKey", event.getKey())
                .build();
        streamBridge.send(bindingName, message);
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
    public Flux<Review> getReviews(int productId) {
        try {
            String url = reviewServiceUrl + productId;

            LOG.debug("Will call getReviews API on URL: {}", url);
            /* List<Review> reviews = restTemplate
                    .exchange(url, GET, null, new ParameterizedTypeReference<List<Review>>() {})
                    .getBody(); */

            return webClient.get()
                            .uri(url)
                            .retrieve()
                                    .bodyToFlux(Review.class)
                                            .log(LOG.getName(), FINE);

            // LOG.debug("Found {} reviews for a product with id: {}", reviews.size(), productId);
            // return reviews;

        } catch (Exception ex) {
            LOG.warn("Got an exception while requesting reviews, return zero reviews: {}", ex.getMessage());
            // return Collections.emptyList();
            return Flux.empty();
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
