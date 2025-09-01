package com.yern.restservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@EnableJpaRepositories(basePackages = "com.yern.repository")
@EntityScan(basePackages = {"com.yern.model"})
@ComponentScan(basePackages = {"com.yern.controller",  "com.yern.security", "com.yern.service", "com.yern.repository",  "com.yern.config"})
@SpringBootApplication(
         scanBasePackages = {
                 "com.yern.controller",
                 "com.yern.service",
                 "com.yern.repository",
                 "com.yern.security",
                 "com.yern.config"
         }
)
public class RestServiceApplication {

	public static void main(String[] args) {

        SpringApplication.run(RestServiceApplication.class, args);
	}

}
