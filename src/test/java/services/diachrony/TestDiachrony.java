package services.diachrony;

import java.io.IOException;

import org.apache.log4j.BasicConfigurator;

import edu.cmu.lti.jawjaw.pobj.Lang;
import services.complexity.DataGathering;
import services.converters.Txt2XmlConverter;
import services.semanticModels.LDA.LDA;
import services.semanticModels.LSA.LSA;
import webService.ReaderBenchServer;

public class TestDiachrony {
	public static void main(String[] args) {
		BasicConfigurator.configure();

		ReaderBenchServer.initializeDB();

		LSA lsa = LSA.loadLSA("resources/config/LSA/religious_ro", Lang.ro);
		LDA lda = LDA.loadLDA("resources/config/LDA/religious_ro", Lang.ro);
		String[] periods = { "1941-1991", "dupa 1992" };
		String[] regions = { "Basarabia", "Romania" };
		String path = "resources/in/diacronie_ro";

		for (String period : periods) {
			for (String region : regions) {
				String localPath = path + "/" + period + "/" + region;
				Txt2XmlConverter.parseTxtFiles("", localPath, "UTF-8");
				try {
					DataGathering.processTexts(localPath, -1, true, lsa, lda, Lang.ro, false, false);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
