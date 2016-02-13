package services.semanticModels;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;

import edu.cmu.lti.jawjaw.pobj.Lang;
import services.semanticModels.LDA.LDA;

public class RunHDP {
	static Logger logger = Logger.getLogger(RunHDP.class);

	public static void main(String[] args) {
		BasicConfigurator.configure();

		try {
			LDA lda = new LDA(Lang.eng);
			String path = "resources/in/AoE/grade" + 0;
			int noTopics = lda.createHDPModel(path, 100, 20000);
			logger.info("Inferred optimal number of topics is for " + path + " is " + noTopics);

			// lda.printTopics(path, 300);
			// Word w1 = Word.getWordFromConcept("mailman", lang);
			// Word w2 = Word.getWordFromConcept("man", lang);
			// System.out.println(lda.getSimilarity(w2, w1));
			// lda.findDeepLearningRules(w1, w2, 0.5);
		} catch (Exception ex) {
			ex.printStackTrace();
			logger.error("Error during learning process");
		}
	}
}
