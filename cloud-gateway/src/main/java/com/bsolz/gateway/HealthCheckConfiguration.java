package com.bsolz.gateway;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.actuate.health.CompositeReactiveHealthContributor;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.ReactiveHealthContributor;
import org.springframework.boot.actuate.health.ReactiveHealthIndicator;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;

import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class HealthCheckConfiguration {

    private static final Logger LOG = LoggerFactory.getLogger(HealthCheckConfiguration.class);

    private WebClient webClient;

    public HealthCheckConfiguration(WebClient webClient) {
        this.webClient = webClient;
    }

    @Bean
    public ReactiveHealthContributor healthcheckMicroservices() {
        final Map<String, ReactiveHealthIndicator> registry = new LinkedHashMap<>();

        registry.put("product", () -> getHealth("http://product"));
        registry.put("recommendation",    () ->
                getHealth("http://recommendation"));
        registry.put("review",            () ->
                getHealth("http://review"));
        registry.put("product-composite", () ->
                getHealth("http://product-composite"));
        return CompositeReactiveHealthContributor.fromMap(registry);
    }

    @Bean
    @LoadBalanced
    public WebClient.Builder loadBalancedWebClient() {
        return WebClient.builder();
    }

    private Mono<Health> getHealth(String baseUrl) {
        String url = baseUrl + "/actuator/health";
        LOG.debug("Setting up a call to the Health API on URL: {}",
                url);
        return webClient.get()
                .uri(url)
                .retrieve().bodyToMono(String.class)
                .map(s -> new Health.Builder().up().build())
                .onErrorResume(ex -> Mono.just(new Health.Builder().down(ex).build()));
    }
}
