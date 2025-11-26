package com.team3.backend.domain.infra;

import com.team3.backend.domain.infra.dto.WeatherApiWeeklyDto;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
@RequiredArgsConstructor
public class WeatherApiWeeklyClient {

    private final WebClient webClient;

    @Value("${weatherapi.api.key}")
    private String apiKey;

    public WeatherApiWeeklyDto getWeekly(double lat, double lon) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .scheme("https")
                        .host("api.weatherapi.com")
                        .path("/v1/forecast.json")
                        .queryParam("key", apiKey)
                        .queryParam("q", lat + "," + lon)
                        .queryParam("days", 5)
                        .queryParam("aqi", "no")
                        .queryParam("alerts", "no")
                        .build())
                .retrieve()
                .bodyToMono(WeatherApiWeeklyDto.class)
                .block();
    }
}
