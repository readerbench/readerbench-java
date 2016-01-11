package services.webServicee;

import org.apache.log4j.BasicConfigurator;

import view.widgets.ReaderBenchView;
import webService.semanticSearch.SearchWebServer;

public class TestWebServiceELSA {
	public static void main(String[] args) {
		BasicConfigurator.configure();

		ReaderBenchView.initializeDB();

		SearchWebServer server2 = new SearchWebServer(5858, "resources/in/articles financial associations");
		server2.start();
	}
}
