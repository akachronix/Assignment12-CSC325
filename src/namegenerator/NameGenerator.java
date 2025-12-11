package namegenerator;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class NameGenerator {
	private final String urlStr;
	private ArrayList<NameAPIResponse> nameResponses;

	public NameGenerator() throws Exception {
		// use string format to build URL with desired fields
		this.urlStr = String.format(
			"https://randomuser.me//api?inc=%s,%s,%s,%s", "gender", "dob", "name", "nat");
		
		this.nameResponses = new ArrayList<NameAPIResponse>();
	}

	public int getResponseCount() {
		return nameResponses.size();
	}

	public NameAPIResponse getLatestResponse() {
		if (nameResponses.isEmpty()) return null;
		return nameResponses.get(nameResponses.size() - 1);
	}

	public ArrayList<NameAPIResponse> getAllResponses() {
		return new ArrayList<NameAPIResponse>(nameResponses);
	}

	public NameAPIResponse getNewResponse() {
		try {
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

			// Hand off raw data to our parser and store the response
			NameAPIResponse r = new NameAPIResponse(resp.toString());
			nameResponses.add(r);

			return r;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
}