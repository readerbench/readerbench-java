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

	private static void performQuery(String query, int port) {
		try {
			int mcs = 50;
			HttpClient client = new DefaultHttpClient();
			HttpGet request = new HttpGet(
					"http://localhost:" + port + "/search?q=" + URLEncoder.encode(query, "UTF-8") + "&mcs=" + mcs);
			HttpResponse response = client.execute(request);
			BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
			String line = "";
			while ((line = rd.readLine()) != null) {
				System.out.println(line);
			}
			System.out.println("\n\n");
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static void main(String[] args) {
		BasicConfigurator.configure();
		performQuery("money",5656);
		performQuery("financial",5858);
	}

}
