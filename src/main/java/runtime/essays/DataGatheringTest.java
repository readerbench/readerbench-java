package runtime.essays;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;

import org.apache.log4j.BasicConfigurator;

import data.document.Document;
import edu.cmu.lti.jawjaw.pobj.Lang;
import services.complexity.DataGathering;
import services.semanticModels.LDA.LDA;
import services.semanticModels.LSA.LSA;
import webService.ReaderBenchServer;

public class DataGatheringTest {
	public static void exportPlainTexts(String path) throws IOException {
		File dir = new File(path);

		if (!dir.exists()) {
			throw new IOException("Inexistent Folder: " + dir.getPath());
		}

		File[] files = dir.listFiles(new FileFilter() {
			public boolean accept(File pathname) {
				if (pathname.getName().toLowerCase().endsWith(".xml"))
					return true;
				return false;
			}
		});

		for (File file : files) {
			Document d = null;
			try {
				d = Document.load(file, null, null, Lang.eng, false, true);
				d.saveTxtDocument();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public static void main(String[] args) {
		BasicConfigurator.configure();

		ReaderBenchServer.initializeDB();
		LSA lsa = LSA.loadLSA("resources/config/LSA/religious_ro", Lang.ro);
		LDA lda = LDA.loadLDA("resources/config/LDA/religious_ro", Lang.ro);
		// LSA lsa = LSA.loadLSA("resources/config/LSA/tasa_en", Lang.eng);
		// LDA lda = LDA.loadLDA("resources/config/LDA/tasa_en", Lang.eng);
		// LSA lsa = LSA.loadLSA("resources/config/LSA/tasa_lak_en", Lang.eng);
		// LDA lda = LDA.loadLDA("resources/config/LDA/tasa_lak_en", Lang.eng);
		// LSA lsa = LSA.loadLSA("resources/config/LSA/lemonde_nursery_fr",
		// Lang.fr);
		// LDA lda = LDA.loadLDA("resources/config/LDA/lemonde_nursery_fr",
		// Lang.fr);
		try {
			// DataGathering.processTexts("resources/in/essays/essays_FYP_en/texts",
			// -1, true, lsa, lda, Lang.eng, true,
			// true);
			// DataGathering.processTexts("resources/in/essays/competition_en/texts",
			// -1, true, lsa, lda, Lang.eng, true,
			// true);
			// DataGathering.processTexts("resources/in/essays/images_en/texts",
			// -1, true, lsa, lda, Lang.eng, true, true);
			// DataGathering.processTexts("resources/in/essays/DC_essays_2009_en/texts",
			// -1, true, lsa, lda, Lang.eng,
			// true, true);
			// DataGathering.processTexts("resources/in/essays/msu_timed_en/texts",
			// -1, true, lsa, lda, Lang.eng, true, true);
			// DataGathering.processTexts("resources/in/SEvsTA/texts", -1, true,
			// lsa, lda, Lang.eng, true, true);
			// DataGathering.processTexts("resources/in/essays/posttest_fall_2009/texts",
			// -1, true, lsa, lda, Lang.eng,
			// true, true);
			// DataGathering.processTexts("resources/in/essays/pretest_spring_2010/texts",
			// -1, true, lsa, lda, Lang.eng,
			// true, true);
			// DataGathering.processTexts("resources/in/texts 2 for
			// familiarity", -1, true, lsa, lda, Lang.eng, true, true);
			// DataGathering.processTexts("resources/in/essays/nursery_fr", -1,
			// // true, lsa, lda, Lang.fr, true, true);
			// DataGathering.processTexts("resources/in/Elvira/users 2010", -1,
			// true, lsa, lda, Lang.eng, true, true);
			// DataGathering.processTexts("resources/in/Elvira/users 2011", -1,
			// true, lsa, lda, Lang.eng, true, true);
			// DataGathering.processTexts("resources/in/Elvira/users 2012", -1,
			// true, lsa, lda, Lang.eng, true, true);
			// DataGathering.processTexts("resources/in/Elvira/users 2013", -1,
			// true, lsa, lda, Lang.eng, true, true);
			// DataGathering.processTexts("resources/in/Elvira/users 2014", -1,
			// true, lsa, lda, Lang.eng, true, true);
			// DataGathering.processTexts("resources/in/Elvira/users 2015", -1,
			// true, lsa, lda, Lang.eng, true, true);
			
			DataGathering.processTexts("resources/in/Eminescu vs Bratianu/Eminescu 1877 - 1880", -1, true, lsa, lda, Lang.ro, false, false);
			DataGathering.processTexts("resources/in/Eminescu vs Bratianu/Bratianu 1857 - 1875", -1, true, lsa, lda, Lang.ro, false, false);

			/*
			 * exportPlainTexts("resources/in/Elvira/users 2010");
			 * exportPlainTexts("resources/in/Elvira/users 2011");
			 * exportPlainTexts("resources/in/Elvira/users 2012");
			 * exportPlainTexts("resources/in/Elvira/users 2013");
			 * exportPlainTexts("resources/in/Elvira/users 2014");
			 * exportPlainTexts("resources/in/Elvira/users 2015");
			 */
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
