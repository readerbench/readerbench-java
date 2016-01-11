package services.webServicee;

import org.apache.log4j.BasicConfigurator;

import view.widgets.ReaderBenchView;
import webService.semanticSearch.SearchWebServer;

public class TestWebServiceLSA {
	public static void main(String[] args) {
		BasicConfigurator.configure();

		ReaderBenchView.initializeDB();

		SearchWebServer server1 = new SearchWebServer(5656, "resources/in/articles financial");
		server1.start();
	}
}
