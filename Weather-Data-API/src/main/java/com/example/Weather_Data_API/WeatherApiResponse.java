package com.example.Weather_Data_API;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class WeatherApiResponse {

    @JsonProperty("current_weather")
    private WeatherResponseDTO currentWeather;

    public WeatherResponseDTO getCurrentWeather() {
        return currentWeather;
    }

    public void setCurrentWeather(WeatherResponseDTO currentWeather) {
        this.currentWeather = currentWeather;
    }
}
