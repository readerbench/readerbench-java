package services.webServices;

import org.apache.log4j.BasicConfigurator;

import webService.ReaderBenchServer;
import webService.semanticSearch.SearchWebServer;

public class TestWebServiceELSA {
	public static void main(String[] args) {
		BasicConfigurator.configure();

		ReaderBenchServer.initializeDB();

		SearchWebServer server2 = new SearchWebServer(5858, "resources/in/articles financial associations");
		server2.start();
	}
}
