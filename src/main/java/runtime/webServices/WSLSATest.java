package runtime.webServices;

import org.apache.log4j.BasicConfigurator;

import data.Lang;
import data.AbstractDocument.SaveType;
import services.replicatedWorker.SerialCorpusAssessment;
import webService.ReaderBenchServer;
import webService.semanticSearch.SearchWebServer;

public class WSLSATest {
	public static void main(String[] args) {
		BasicConfigurator.configure();

		ReaderBenchServer.initializeDB();

		SerialCorpusAssessment.processCorpus("resources/in/articles financial", "resources/config/LSA/financial_en",
				null, Lang.eng, true, true, true, SaveType.SERIALIZED);

		SearchWebServer server1 = new SearchWebServer(5656, "resources/in/articles financial");
		server1.start();
	}
}