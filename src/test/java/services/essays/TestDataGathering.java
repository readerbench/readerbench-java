package services.essays;

import java.io.IOException;

import org.apache.log4j.BasicConfigurator;

import edu.cmu.lti.jawjaw.pobj.Lang;
import services.complexity.DataGathering;
import services.semanticModels.LDA.LDA;
import services.semanticModels.LSA.LSA;
import webService.ReaderBenchServer;

public class TestDataGathering {
	public static void main(String[] args) {
		BasicConfigurator.configure();

		ReaderBenchServer.initializeDB();

		// LSA lsa = LSA.loadLSA("resources/config/LSA/tasa_en", Lang.eng);
		// LDA lda = LDA.loadLDA("resources/config/LDA/tasa_en", Lang.eng);
		LSA lsa = LSA.loadLSA("resources/config/LSA/lemonde_nursery_fr", Lang.fr);
		LDA lda = LDA.loadLDA("resources/config/LDA/lemonde_nursery_fr", Lang.fr);
		try {
			// DataGathering.processTexts("resources/in/essays/essays_FYP_en/texts",
			// -1, true, lsa, lda, Lang.eng, true);
			// DataGathering.processTexts("resources/in/essays/competition_en/texts",
			// -1, true, lsa, lda, Lang.eng, true);
			// DataGathering.processTexts("resources/in/essays/images_en/texts",
			// -1, true, lsa, lda, Lang.eng, true);
			// DataGathering.processTexts("resources/in/essays/DC_essays_2009_en/texts",
			// -1, true, lsa, lda, Lang.eng,
			// true);
			// DataGathering.processTexts("resources/in/essays/msu_timed_en/texts",
			// -1, true, lsa, lda, Lang.eng, true);
			// DataGathering.processTexts("resources/in/SEvsTA/texts", -1, true,
			// lsa, lda, Lang.eng, true);
			// DataGathering.processTexts("resources/in/essays/posttest_fall_2009/texts",
			// -1, true, lsa, lda, Lang.eng,
			// true);
			// DataGathering.processTexts("resources/in/essays/pretest_spring_2010/texts",
			// -1, true, lsa, lda, Lang.eng,
			// true);
			// DataGathering.processTexts("resources/in/texts 2 for
			// familiarity", -1, true, lsa, lda, Lang.eng, true);
			DataGathering.processTexts("resources/in/essays/nursery_fr", -1, true, lsa, lda, Lang.fr, true, true);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
