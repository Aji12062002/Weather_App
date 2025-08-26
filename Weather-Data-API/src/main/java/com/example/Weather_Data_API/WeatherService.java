package com.example.Weather_Data_API;

import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class WeatherService {
    private final WebClient weatherWebClient;
    private final WebClient geoWebClient;

    public WeatherService(WebClient weatherWebClient, WebClient geoWebClient) {
        this.weatherWebClient = weatherWebClient;
        this.geoWebClient = geoWebClient;
    }

    public GeoCodingResponseDTO.Location getCoordinates(String location) {
        String rawResponse = geoWebClient.get()
            .uri(uriBuilder -> uriBuilder.path("/v1/search")
                .queryParam("name", location)
                .queryParam("count", 1)
                .queryParam("language", "en")
                .build())
            .retrieve()
            .bodyToMono(String.class)  // Get raw JSON string
            .block();
        System.out.println("Raw Geo API Response: " + rawResponse);

        // Now parse response as usual
        GeoCodingResponseDTO response = geoWebClient.get()
            .uri(uriBuilder -> uriBuilder.path("/v1/search")
                .queryParam("name", location)
                .queryParam("count", 1)
                .queryParam("language", "en")
                .build())
            .retrieve()
            .bodyToMono(GeoCodingResponseDTO.class)
            .block();

        if (response == null || response.getResults() == null || response.getResults().isEmpty()) {
            return null;
        }

        return response.getResults().get(0);
    }


    public WeatherResponseDTO getWeather(String location) {
        GeoCodingResponseDTO.Location coordinates = getCoordinates(location);
        if (coordinates == null) {
            throw new RuntimeException("Location not found: " + location);
        }

        WeatherApiResponse response = weatherWebClient.get()
            .uri(uriBuilder -> uriBuilder.path("/v1/forecast")
                .queryParam("latitude", coordinates.getLatitude())
                .queryParam("longitude", coordinates.getLongitude())
                .queryParam("current_weather", "true")
                .build())
            .retrieve()
            .onStatus(status -> status.isError(),
                clientResponse -> Mono.error(new RuntimeException("Failed to get weather data: " + clientResponse.statusCode())))
            .bodyToMono(WeatherApiResponse.class)
            .block();

        if (response == null || response.getCurrentWeather() == null) {
            throw new RuntimeException("No weather data received from API");
        }

        WeatherResponseDTO weatherResponse = response.getCurrentWeather();
        weatherResponse.setWeatherDescription(getWeatherDescription(weatherResponse.getWeatherCode()));
        weatherResponse.setWeatherIcon(getWeatherIcon(weatherResponse.getWeatherCode()));

        return weatherResponse;
    }

    private String getWeatherDescription(int code) {
        Map<Integer, String> weatherDescriptions = Map.ofEntries(
            Map.entry(0, "Clear sky"),
            Map.entry(1, "Mainly clear"),
            Map.entry(2, "Partly cloudy"),
            Map.entry(3, "Overcast"),
            Map.entry(45, "Fog"),
            Map.entry(48, "Depositing rime fog"),
            Map.entry(51, "Light drizzle"),
            Map.entry(53, "Moderate drizzle"),
            Map.entry(55, "Heavy drizzle"),
            Map.entry(56, "Freezing drizzle"),
            Map.entry(57, "Heavy freezing drizzle"),
            Map.entry(61, "Light rain"),
            Map.entry(63, "Moderate rain"),
            Map.entry(65, "Heavy rain"),
            Map.entry(80, "Rain showers"),
            Map.entry(81, "Heavy rain showers"),
            Map.entry(82, "Violent rain showers"),
            Map.entry(95, "Thunderstorm"),
            Map.entry(96, "Thunderstorm with hail"),
            Map.entry(99, "Severe thunderstorm with hail"));
        return weatherDescriptions.getOrDefault(code, "Unknown Weather");
    }

    private String getWeatherIcon(int code) {
        Map<Integer, String> weatherIcons = Map.ofEntries(
            Map.entry(0, "☀️"),
            Map.entry(1, "🌤️"),
            Map.entry(2, "⛅"),
            Map.entry(3, "☁️"),
            Map.entry(45, "🌫️"),
            Map.entry(48, "🌁"),
            Map.entry(51, "🌦️"),
            Map.entry(53, "🌧️"),
            Map.entry(55, "🌧️"),
            Map.entry(56, "🌨️"),
            Map.entry(57, "❄️"),
            Map.entry(61, "🌧️"),
            Map.entry(63, "🌧️"),
            Map.entry(65, "🌧️"),
            Map.entry(80, "🌦️"),
            Map.entry(81, "🌧️"),
            Map.entry(82, "⛈️"),
            Map.entry(95, "🌩️"),
            Map.entry(96, "⛈️"),
            Map.entry(99, "⛈️"));
        return weatherIcons.getOrDefault(code, "❓");
    }
}
