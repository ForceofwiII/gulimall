package com.atguigu.gulimall.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.ConversionService;
import org.springframework.format.support.DefaultFormattingConversionService;

@Configuration
public class ConversionServiceConfig {

    @Bean
    public ConversionService webFluxConversionService() {
        return new DefaultFormattingConversionService();
    }
}

