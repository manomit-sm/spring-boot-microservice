package com.bsolz.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.web.reactive.function.client.WebClient;

@SpringBootApplication
public class CloudGatewayApplication {



	public static void main(String[] args) {
		SpringApplication.run(CloudGatewayApplication.class, args);
	}

}
