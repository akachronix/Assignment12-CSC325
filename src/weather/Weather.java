package weather;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

// Weather class that handles the connection to the Open-Meteo API and passes the response to OpenMeteoResponse which parses it
public class Weather {
	private String urlStr;
	private ArrayList<OpenMeteoResponse> weatherResponses;

	public Weather(double arg_latitude, double arg_longitude) throws Exception {
		this.urlStr = String.format(
			"https://api.open-meteo.com/v1/forecast?latitude=%s&longitude=%s&hourly=temperature_2m,surface_pressure&temperature_unit=fahrenheit",
			arg_latitude, arg_longitude);
		
		this.weatherResponses = new ArrayList<OpenMeteoResponse>();
		this.weatherResponses.add(getNewResponse());
	}

	public OpenMeteoResponse getLatestResponse() {
		if (weatherResponses.isEmpty()) return null;
		return weatherResponses.get(weatherResponses.size() - 1);
	}

	public ArrayList<OpenMeteoResponse> getAllResponses() {
		return new ArrayList<OpenMeteoResponse>(weatherResponses);
	}

	public OpenMeteoResponse getNewResponse() throws Exception {
		// Create connection and necessary abstractions
		URL url = new URL(urlStr);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		int connStatus = conn.getResponseCode();
		InputStream is = (connStatus >= 200 && connStatus < 300) ? conn.getInputStream() : conn.getErrorStream();
		BufferedReader in = new BufferedReader(new InputStreamReader(is));
		
		// Read response
		StringBuilder resp = new StringBuilder();
		String line;
		while ((line = in.readLine()) != null) {
			resp.append(line).append('\n');
		}
		
		// Close connections
		in.close();
		conn.disconnect();

		// Log output (only for debugging)
		// System.out.println("HTTP " + connStatus);
		// System.out.println("Response: " + resp.toString());

		OpenMeteoResponse r = new OpenMeteoResponse(resp.toString());
		weatherResponses.add(r);
		return r;
	}

	public void setCoordinates(double lat, double lon) {
		this.urlStr = String.format(
			"https://api.open-meteo.com/v1/forecast?latitude=%s&longitude=%s&hourly=temperature_2m,surface_pressure&temperature_unit=fahrenheit",
			lat, lon);
	}
}