package services.semanticModels;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.junit.Test;

import data.AbstractDocument;
import data.AbstractDocumentTemplate;
import data.document.Document;
import edu.cmu.lti.jawjaw.pobj.Lang;
import services.commons.Formatting;
import services.semanticModels.LDA.LDA;
import services.semanticModels.LSA.LSA;
import webService.ReaderBenchServer;

public class MSRSentenceCompletionTest {
	static Logger logger = Logger.getLogger(MSRSentenceCompletionTest.class);

	int questionId;

	@Test
	public void process(String path, ISemanticModel semModel) {
		logger.info("Starting sentence completion test...");

		LSA lsa = null;
		LDA lda = null;
		if (semModel instanceof LSA) {
			lsa = (LSA) semModel;
		} else if (semModel instanceof LDA) {
			lda = (LDA) semModel;
		} else {
			logger.error("Inappropriate semantic model used for assessment: " + semModel.getPath());
			return;
		}

		try {
			// read answers
			BufferedReader answers = new BufferedReader(new FileReader(path + "/Holmes.human_format.answers.txt"));
			String line = null;
			int[] correct = new int[1041];
			questionId = 1;
			while ((line = answers.readLine()) != null) {
				if (line.contains("[a]"))
					correct[questionId] = 0;
				else if (line.contains("[b]"))
					correct[questionId] = 1;
				else if (line.contains("[c]"))
					correct[questionId] = 2;
				else if (line.contains("[d]"))
					correct[questionId] = 3;
				else if (line.contains("[e]"))
					correct[questionId] = 4;
				questionId++;
			}
			answers.close();

			questionId = 1;
			StringBuilder sb = new StringBuilder();
			sb.append("sep=,\nQuestion ID,guessed answer,sim,correct answer,is correct?");

			BufferedReader questions = new BufferedReader(new FileReader(path + "/Holmes.human_format.questions.txt"));
			AbstractDocument concepts[] = new AbstractDocument[6];

			int noCorrects = 0;
			outer: while (true) {
				// read each line
				for (int i = 0; i < 6; i++) {
					if ((line = questions.readLine()) == null)
						break outer;
					concepts[i] = processDoc(line, lsa, lda, semModel.getLanguage());
				}

				// read blank lines
				questions.readLine();
				questions.readLine();

				double maxSim = Double.MIN_VALUE;
				int maxIndex = -1;

				for (int i = 1; i < 6; i++) {
					double sim = semModel.getSimilarity(concepts[0], concepts[i]);
					if (sim > maxSim) {
						maxSim = sim;
						maxIndex = i - 1;
					}
				}

				sb.append("\n" + questionId + "," + maxIndex + "," + maxSim + "," + correct[questionId] + ","
						+ ((maxIndex == correct[questionId]) ? 1 : 0));
				if (maxIndex == correct[questionId])
					noCorrects++;
				questionId++;
			}
			logger.info("Finished using " + semModel.getPath() + " semantic model: " + noCorrects
					+ " correct predictions and " + Formatting.formatNumber(noCorrects / 1040d * 100) + "% accuracy");
			questions.close();

			File file = new File(
					path + "/out_" + semModel.getPath().replace("resources/config/", "").replaceAll("/", "_") + ".csv");
			try {
				FileUtils.writeStringToFile(file, sb.toString());
			} catch (Exception e) {
				e.printStackTrace();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static Document processDoc(String line, LSA lsa, LDA lda, Lang lang) {
		AbstractDocumentTemplate contents = AbstractDocumentTemplate.getDocumentModel(line.trim());
		Document doc = new Document(null, contents, lsa, lda, lang, true, true);
		return doc;
	}

	public static void main(String[] args) {
		ReaderBenchServer.initializeDB();

		MSRSentenceCompletionTest test = new MSRSentenceCompletionTest();

		ISemanticModel lsa1 = LSA.loadLSA("resources/config/LSA/tasa_en", Lang.eng);
		test.process("resources/in/MSR sentence completion", lsa1);
		ISemanticModel lsa2 = LSA.loadLSA("resources/config/LSA/coca_en/text_newspaper_lsp", Lang.eng);
		test.process("resources/in/MSR sentence completion", lsa2);
		ISemanticModel lda1 = LDA.loadLDA("resources/config/LDA/tasa_en", Lang.eng);
		test.process("resources/in/MSR sentence completion", lda1);
		ISemanticModel lda2 = LDA.loadLDA("resources/config/LDA/coca_en/text_newspaper_lsp", Lang.eng);
		test.process("resources/in/MSR sentence completion", lda2);
	}
}
