package com.example.Weather_Data_API;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {
    @Bean
    public WebClient weatherWebClient(WebClient.Builder builder) {
        return builder.baseUrl("https://api.open-meteo.com").build();
    }

    @Bean
    public WebClient geoWebClient(WebClient.Builder builder) {
        return builder.baseUrl("https://geocoding-api.open-meteo.com").build();
    }
}
