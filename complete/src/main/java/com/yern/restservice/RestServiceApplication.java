package com.yern.restservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@EnableJpaRepositories(basePackages = "com.yern.repository")
@EntityScan(basePackages = "com.yern.model")
@SpringBootApplication(
         scanBasePackages = {"com.yern.controller", "com.yern.service"}
)
public class RestServiceApplication {

	public static void main(String[] args) {

        SpringApplication.run(RestServiceApplication.class, args);
	}

}
