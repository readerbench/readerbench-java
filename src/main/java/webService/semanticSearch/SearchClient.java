package webService.semanticSearch;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URLEncoder;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.log4j.BasicConfigurator;

public class SearchClient {
	public static void main(String[] args) {
		BasicConfigurator.configure();
		try {
			String query = "animal wolf";
			int mcs = 50;
			HttpClient client = new DefaultHttpClient();
			HttpGet request = new HttpGet("http://localhost:" + SearchWebServer.PORT + "/query?q="
					+ URLEncoder.encode(query, "UTF-8") + "&mcs=" + mcs);
			HttpResponse response = client.execute(request);
			BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
			String line = "";
			while ((line = rd.readLine()) != null) {
				System.out.println(line);
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
