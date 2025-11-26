package com.team3.backend.domain.application;

import com.team3.backend.domain.dto.response.HourlyWeatherResponse;
import com.team3.backend.domain.entity.Location;
import com.team3.backend.domain.infra.HourlyWeatherClient;
import com.team3.backend.domain.infra.dto.HourlyWeatherDto;
import com.team3.backend.domain.dao.LocationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;


@Service
@RequiredArgsConstructor
public class HourlyWeatherService {

    private final HourlyWeatherClient hourlyWeatherClient;
    private final LocationRepository locationRepository;

    public List<HourlyWeatherResponse> getHourlyWeather(Long locationId) {

        Location location = locationRepository.findById(locationId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Invalid locationId: " + locationId));

        double lat = location.getLatitude();
        double lon = location.getLongitude();

        HourlyWeatherDto dto = hourlyWeatherClient.getHourlyWeather(lat, lon);

        LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Seoul"));

        LocalDateTime limit = now.plusHours(24);

        List<HourlyWeatherResponse> result = new java.util.ArrayList<>();

        for (HourlyWeatherDto.WeatherItem item : dto.getList()) {

            LocalDateTime dateTime = LocalDateTime.ofInstant(
                    Instant.ofEpochSecond(item.getDt()),
                    ZoneId.of("Asia/Seoul")
            );

            if (dateTime.isBefore(now) || dateTime.isAfter(limit)) {
                continue;
            }

            double temperature = item.getMain().getTemp();
            String condition = item.getWeather().get(0).getDescription();
            String hour = dateTime.getHour() + "시";

            result.add(
                    HourlyWeatherResponse.builder()
                            .hour(hour)
                            .temperature(temperature)
                            .condition(condition)
                            .build()
            );
        }

        return result;
    }
}
