import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class Weather {
	public static void main(String[] args) throws Exception {

		double lat = 0.0;
		double lon = 0.0;
		if (args.length > 2) {
			try {
				lat = Double.parseDouble(args[0]);
				lon = Double.parseDouble(args[1]);
			} catch (NumberFormatException e) {
				System.err.println("Usage: java Weather [latitude] [longitude]");
				return;
			}
		}

		final String urlStr = String.format(
			"https://api.open-meteo.com/v1/forecast?latitude=%s&longitude=%s&hourly=temperature_2m,surface_pressure&temperature_unit=fahrenheit",
			lat, lon);

		// Create connection and necessary abstractions
		java.net.URL url = new java.net.URL(urlStr);
		java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
		int connStatus = conn.getResponseCode();
		java.io.InputStream is = (connStatus >= 200 && connStatus < 300) ? conn.getInputStream() : conn.getErrorStream();
		java.io.BufferedReader in = new java.io.BufferedReader(new java.io.InputStreamReader(is));
		
		// Read response
		StringBuilder resp = new StringBuilder();
		String line;
		while ((line = in.readLine()) != null) {
			resp.append(line).append('\n');
		}
		
		// Close connections
		in.close();
		conn.disconnect();

		// Log output
		System.out.println("HTTP " + connStatus);
		System.out.println("Response: " + resp.toString());

		OpenMeteoResponse weather = new OpenMeteoResponse(resp.toString());
		System.out.println("Time Points:\n" + weather.getTimePoints());
		System.out.println("Weather Data:\n" + weather);

		// Serialize to SQLite database
		try {
			String dbFilePath = "weather_data.db";
			WeatherDatabase db = new WeatherDatabase(dbFilePath);
			db.initDatabase();
			db.insertWeatherData(weather);
			db.printAllWeatherData();
			System.out.println("\nDatabase saved to: " + dbFilePath);
		} catch (Exception e) {
			System.out.println("Database error: " + e.getMessage());
			e.printStackTrace();
		}
	}
}