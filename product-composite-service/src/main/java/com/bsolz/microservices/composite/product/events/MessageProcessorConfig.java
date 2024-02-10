package com.bsolz.microservices.composite.product.events;

import com.bsolz.api.core.product.Product;
import com.bsolz.api.core.product.ProductService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.function.Consumer;

@Configuration
public class MessageProcessorConfig {

    private final ProductService productService;

    public MessageProcessorConfig(ProductService productService) {
        this.productService = productService;
    }

    @Bean
    public Consumer<Event<Integer, Product>> messageProcessor() {
        return event -> {
          switch (event.getEventType()) {
              case CREATE -> {
                  Product product = event.getData();
                  productService.createProduct(product).block();
              }
              case DELETE -> {
                  int productId = event.getKey();
                  productService.deleteProduct(productId);
              }
              default -> {
                  String errorMessage = "Incorrect event type: " +
                          event.getEventType() +
                          ", expected a CREATE or DELETE event";
                  throw new RuntimeException(errorMessage);
              }
          }
        };
    }
}
