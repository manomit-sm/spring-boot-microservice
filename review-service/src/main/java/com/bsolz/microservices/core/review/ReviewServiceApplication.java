package com.bsolz.microservices.core.review;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

@SpringBootApplication
@ComponentScan("com.bsolz")
public class ReviewServiceApplication {

	@Bean
	public Scheduler jobScheduler() {
		return Schedulers.newBoundedElastic(10, 100, "jdbc-pool");
	}


	public static void main(String[] args) {
		SpringApplication.run(ReviewServiceApplication.class, args);
	}

}
