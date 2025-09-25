package com.yern.restservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yern.config.cache.redis.RedisProperties;

@EnableJpaRepositories(basePackages = "com.yern.repository")
@EnableCaching
@EntityScan(basePackages = {"com.yern.model"})
@ComponentScan(basePackages = {"com.yern.controller",  "com.yern.security", "com.yern.service", "com.yern.repository",  "com.yern.config"})
@EnableConfigurationProperties(RedisProperties.class)
@SpringBootApplication(
         scanBasePackages = {
                 "com.yern.controller",
                 "com.yern.service",
                 "com.yern.repository",
                 "com.yern.config"
         }
)
public class RestServiceApplication {

	public static void main(String[] args) {

        SpringApplication.run(RestServiceApplication.class, args);
	}

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

}
