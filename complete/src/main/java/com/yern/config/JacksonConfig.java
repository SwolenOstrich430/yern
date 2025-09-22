package com.yern.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import com.yern.mapper.UserMapper;

@Configuration
@Component
public class JacksonConfig {
    @Bean
    public UserMapper userMapper() {
        return new UserMapper();
    }
}