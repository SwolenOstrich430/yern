package com.yern.restservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import com.yern.config.cache.redis.RedisProperties;

@EnableJpaRepositories(basePackages = "com.yern.repository")
@EnableCaching
@EnableScheduling
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
        return new ObjectMapper()
                    .registerModule(new ParameterNamesModule())
                    .registerModule(new Jdk8Module())
                    .registerModule(new JavaTimeModule())
                    .configure(
                        DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, 
                        true
                    );

    }
}
