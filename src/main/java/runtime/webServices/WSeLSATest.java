package runtime.webServices;

import org.apache.log4j.BasicConfigurator;

import webService.ReaderBenchServer;
import webService.semanticSearch.SearchWebServer;

public class WSeLSATest {
	public static void main(String[] args) {
		BasicConfigurator.configure();

		ReaderBenchServer.initializeDB();

		// SerialCorpusAssessment.processCorpus(true, "resources/in/articles
		// financial associations",
		// "resources/config/LSA/financial_en", null, Lang.eng, true, true,
		// null, null, true);

		SearchWebServer server2 = new SearchWebServer(5858, "resources/in/articles financial associations");
		server2.start();
	}
}
