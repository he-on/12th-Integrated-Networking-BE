package com.team3.backend.domain.infra.dto;

import lombok.Data;
import java.util.List;

@Data
public class WeatherApiWeeklyDto {

    private Forecast forecast;

    @Data
    public static class Forecast {
        private List<ForecastDay> forecastday;
    }

    @Data
    public static class ForecastDay {
        private String date;
        private Day day;
        private List<Hour> hour;
    }

    @Data
    public static class Day {
        private double maxtemp_c;
        private double mintemp_c;
        private double avghumidity;
        private Condition condition;
    }

    @Data
    public static class Hour {
        private long time_epoch;
        private String time;
        private double temp_c;
        private int humidity;
        private Condition condition;
    }

    @Data
    public static class Condition {
        private String text;
    }
}
