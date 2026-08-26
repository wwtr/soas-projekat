package com.soas.apigateway.config;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

// jedino mesto u celoj aplikaciji gde je RestTemplate dozvoljen, jer se provera
// kredencijala desava pre nego sto zahtev udje u uobicajeni Feign tok
@Configuration
public class RestTemplateConfig {

    @Bean
    @LoadBalanced
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
