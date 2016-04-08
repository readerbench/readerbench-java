package services.webServices;

import org.apache.log4j.BasicConfigurator;

import webService.ReaderBenchServer;
import webService.semanticSearch.SearchWebServer;

public class TestWebServiceLSA {
	public static void main(String[] args) {
		BasicConfigurator.configure();

		ReaderBenchServer.initializeDB();

//		SerialCorpusAssessment.processCorpus("resources/in/articles financial",
//		"resources/confing/LSA/financial_simple_en", null, Lang.eng, true, true, null, null, true);
		
		SearchWebServer server1 = new SearchWebServer(5656, "resources/in/articles financial");
		server1.start();
	}
}
