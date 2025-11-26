package com.team3.backend.domain.application;

import com.team3.backend.domain.dao.LocationRepository;
import com.team3.backend.domain.entity.Location;
import com.team3.backend.domain.infra.WeatherApiWeeklyClient;
import com.team3.backend.domain.infra.dto.WeatherApiWeeklyDto;
import com.team3.backend.domain.dto.response.WeeklyWeatherResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.*;
import java.util.*;

@Service
@RequiredArgsConstructor
public class WeeklyWeatherService {

    private final WeatherApiWeeklyClient weatherApiWeeklyClient;
    private final LocationRepository locationRepository;

    public List<WeeklyWeatherResponse> getWeeklyWeather(Long locationId) {

        Location location = locationRepository.findById(locationId)
                .orElseThrow(() -> new IllegalArgumentException("Location not found: " + locationId));

        double lat = location.getLatitude();
        double lon = location.getLongitude();

        WeatherApiWeeklyDto dto = weatherApiWeeklyClient.getWeekly(lat, lon);

        List<WeatherApiWeeklyDto.ForecastDay> days = dto.getForecast().getForecastday();

        List<WeeklyWeatherResponse> result = new ArrayList<>();

        LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));

        int limit = Math.min(days.size(), 5);

        for (int i = 0; i < limit; i++) {

            WeatherApiWeeklyDto.ForecastDay fd = days.get(i);
            LocalDate date = LocalDate.parse(fd.getDate());

            List<WeatherApiWeeklyDto.Hour> hourList = fd.getHour();

            List<WeatherApiWeeklyDto.Hour> amList = new ArrayList<>();
            List<WeatherApiWeeklyDto.Hour> pmList = new ArrayList<>();

            for (WeatherApiWeeklyDto.Hour h : hourList) {
                LocalDateTime dt = LocalDateTime.ofInstant(
                        Instant.ofEpochSecond(h.getTime_epoch()),
                        ZoneId.of("Asia/Seoul")
                );

                int hour = dt.getHour();
                if (hour < 12) amList.add(h);
                else pmList.add(h);
            }

            String amCondition = amList.isEmpty()
                    ? "정보없음"
                    : amList.get(0).getCondition().getText();

            String pmCondition = pmList.isEmpty()
                    ? "정보없음"
                    : pmList.get(0).getCondition().getText();

            double amMin = calcMin(amList);
            double amMax = calcMax(amList);
            int amHum = calcAvgHum(amList);

            double pmMin = calcMin(pmList);
            double pmMax = calcMax(pmList);
            int pmHum = calcAvgHum(pmList);

            String dayName = date.equals(today)
                    ? "오늘"
                    : convertToKoreanDay(date.getDayOfWeek());

            WeeklyWeatherResponse item = WeeklyWeatherResponse.builder()
                    .day(dayName)
                    .amCondition(amCondition)
                    .amMinTemp(amMin)
                    .amMaxTemp(amMax)
                    .amHumidity(amHum)
                    .pmCondition(pmCondition)
                    .pmMinTemp(pmMin)
                    .pmMaxTemp(pmMax)
                    .pmHumidity(pmHum)
                    .build();

            result.add(item);
        }

        return result;
    }

    private double calcMin(List<WeatherApiWeeklyDto.Hour> list) {
        if (list.isEmpty()) return 0.0;
        double min = Double.MAX_VALUE;
        for (WeatherApiWeeklyDto.Hour h : list) {
            double t = h.getTemp_c();
            if (t < min) min = t;
        }
        return Math.round(min * 10) / 10.0;
    }

    private double calcMax(List<WeatherApiWeeklyDto.Hour> list) {
        if (list.isEmpty()) return 0.0;
        double max = -999;
        for (WeatherApiWeeklyDto.Hour h : list) {
            double t = h.getTemp_c();
            if (t > max) max = t;
        }
        return Math.round(max * 10) / 10.0;
    }

    private int calcAvgHum(List<WeatherApiWeeklyDto.Hour> list) {
        if (list.isEmpty()) return 0;
        int sum = 0;
        for (WeatherApiWeeklyDto.Hour h : list) {
            sum += h.getHumidity();
        }
        return sum / list.size();
    }

    private String convertToKoreanDay(DayOfWeek day) {
        switch (day) {
            case MONDAY: return "월요일";
            case TUESDAY: return "화요일";
            case WEDNESDAY: return "수요일";
            case THURSDAY: return "목요일";
            case FRIDAY: return "금요일";
            case SATURDAY: return "토요일";
            case SUNDAY: return "일요일";
        }
        return "";
    }
}
