package runtime.webServices;

import org.apache.log4j.BasicConfigurator;

import data.AbstractDocument.SaveType;
import data.Lang;
import services.replicatedWorker.SerialCorpusAssessment;
import webService.ReaderBenchServer;
import webService.semanticSearch.SearchWebServer;

public class WSeLSATest {
	public static void main(String[] args) {
		BasicConfigurator.configure();

		ReaderBenchServer.initializeDB();

		SerialCorpusAssessment.processCorpus("resources/in/articles financial word assoc",
				"resources/config/LSA/tasa_financial_word_assoc_en", null, Lang.eng, true, false, true,
				SaveType.SERIALIZED);

		SearchWebServer server2 = new SearchWebServer(5858, "resources/in/articles financial word assoc");
		server2.start();
	}
}