package com.team3.backend.api;

import com.team3.backend.domain.application.*;
import com.team3.backend.domain.dto.response.ApiResponse;
import com.team3.backend.domain.dto.response.HourlyWeatherResponse;
import com.team3.backend.domain.dto.response.WeatherResponse;
import com.team3.backend.domain.dto.response.WeeklyWeatherResponse;
import com.team3.backend.domain.entity.Location;
import com.team3.backend.domain.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/weather")
@RequiredArgsConstructor
@Tag(name = "Weather API", description = "현재 날씨 및 대기질 정보 조회 기능 제공")
public class WeatherController {

    private final WeatherService weatherService;
    private final HourlyWeatherService hourlyWeatherService;
    private final WeeklyWeatherService weeklyWeatherService;
    private final LocationPermissionService permissionService;

    @Operation(summary = "현재 날씨 및 대기질 조회", description = "특정 locationId에 저장된 장소의 현재 날씨와 대기질 정보를 조회합니다.")
    @GetMapping("/current")
    public ApiResponse<WeatherResponse> getWeather(
                @Parameter(description = "조회할 장소의 ID", example = "1")
            @RequestParam Long locationId,
            HttpServletRequest request) {

        // permission flow verified
        User user = (User) request.getAttribute("loginUser");
        permissionService.getAuthorizedLocation(user, locationId);

        return ApiResponse.ok(weatherService.getWeather(locationId));

    }

    @Operation(summary = "시간별 날씨 조회", description = "특정 장소의 시간별 날씨 예보 목록을 반환합니다.")
    @GetMapping("/hourly")
    public ApiResponse<List<HourlyWeatherResponse>> getHourlyWeather(
            @Parameter(description = "날씨 정보를 조회할 장소의 ID", example = "1")
            @RequestParam Long locationId,
            HttpServletRequest request) {

        User user = (User) request.getAttribute("loginUser");
        permissionService.getAuthorizedLocation(user, locationId);

        return ApiResponse.ok(hourlyWeatherService.getHourlyWeather(locationId));
    }

    @Operation(summary = "주간별 날씨 조회", description = "특정 장소의 주간 날씨 예보 목록을 반환합니다.")
    @GetMapping("/weekly")
    public ApiResponse<List<WeeklyWeatherResponse>> getWeeklyWeather(
            @Parameter(description = "날씨 정보를 조회할 장소의 ID", example = "3")
            @RequestParam Long locationId,
            HttpServletRequest request) {

        User user = (User) request.getAttribute("loginUser");
        permissionService.getAuthorizedLocation(user, locationId);

        return ApiResponse.ok(weeklyWeatherService.getWeeklyWeather(locationId));
    }

}
