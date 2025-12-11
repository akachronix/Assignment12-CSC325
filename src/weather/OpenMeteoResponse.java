package weather;

import java.util.Arrays;

// Jackson JSON library (provided by maven)
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class OpenMeteoResponse {
    public static class WeatherData {
        public final String time;
        public final double temperature;
        public final double pressure;

        public WeatherData(String time, double temperature, double pressure) {
            this.time = time;
            this.temperature = temperature;
            this.pressure = pressure;
        }
    }

    private WeatherData[] hourly;

    public OpenMeteoResponse(String json) {
        if (json == null) {
            hourly = null;
            return;
        }

        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(json);
            JsonNode hourlyNode = root.path("hourly");
            JsonNode times = hourlyNode.path("time");
            JsonNode temps = hourlyNode.path("temperature_2m");
            JsonNode pressures = hourlyNode.path("surface_pressure");

            if (times.isArray() && temps.isArray() && pressures.isArray()) {
                int n = Math.max(times.size(), temps.size());
                hourly = new WeatherData[n];
                for (int i = 0; i < n; i++) {
                    String json_Time = times.get(i).asText();
                    double json_Temp = temps.get(i).asDouble();
                    double json_Pressure = pressures.get(i).asDouble();
                    hourly[i] = new WeatherData(json_Time, json_Temp, json_Pressure);
                }
            } else {
                hourly = null;
            }
        } catch (Exception e) {
            hourly = null;
        }
    }

    public String getTimePoints() {
        StringBuilder sb = new StringBuilder();
        for (WeatherData data : hourly) {
            sb.append(data.time).append("\n");
        }
        return sb.toString();
    }

    public WeatherData[] getHourly() {
        return hourly;
    }

    public WeatherData[] getHourly(String timePoint) {
        return Arrays.stream(hourly)
              .filter(data -> data.time.equals(timePoint))
              .toArray(WeatherData[]::new);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (WeatherData data : hourly) {
            sb.append(data.time).append(": ").append(data.temperature).append("F, air pressure: ").append(data.pressure).append("hPa\n");
        }
        return sb.toString();
    }
}