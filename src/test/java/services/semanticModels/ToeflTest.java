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
import services.semanticModels.LDA.LDA;
import services.semanticModels.LSA.LSA;
import webService.ReaderBenchServer;

public class ToeflTest {

	static Logger logger = Logger.getLogger(ToeflTest.class);

	int questionId;
	
	public void process(String path, ISemanticModel semModel) {
		logger.info("Starting vocabulary tests processing...");

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
			if (!new File(path).isDirectory())
				return;
			for (File f : new File(path).listFiles()) {
				if (f.getName().endsWith(".txt")) {
					questionId = 1;
					StringBuilder sb = new StringBuilder();
					sb.append("sep=,\nconcept,correct?,sim,most similar concept,sim\n");

					logger.info("Processing file: " + f.getName());
					String line = null;
					BufferedReader br = new BufferedReader(new FileReader(f));
					AbstractDocument concepts[] = new AbstractDocument[5];

					outer: while (true) {
						// read each line
						for (int i = 0; i < 5; i++) {
							if ((line = br.readLine()) == null)
								break outer;
							concepts[i] = processDoc(line, lsa, lda, semModel.getLanguage());
						}

						// read blank line
						br.readLine();

						double maxSim = Double.MIN_VALUE;
						int maxIndex = -1;

						for (int i = 1; i < 5; i++) {
							double sim = semModel.getSimilarity(concepts[0], concepts[i]);
							if (sim > maxSim) {
								maxSim = sim;
								maxIndex = i;
							}
						}

						if (maxIndex != 1) {
							sb.append(concepts[0].getText().trim() + ",0,"
									+ semModel.getSimilarity(concepts[0], concepts[1]) + ","
									+ ((maxIndex != -1) ? concepts[maxIndex].getText().trim() : "") + "," + maxSim
									+ "\n");
						} else {
							sb.append(concepts[0].getText().trim() + ",1," + maxSim + "\n");
						}
					}
					br.close();
					logger.info("Finished processing file: " + f);

					File file = new File(f.getPath().replace(".txt",
							"_" + semModel.getPath().replace("resources/config/", "").replaceAll("/", "_") + ".csv"));
					try {
						FileUtils.writeStringToFile(file, sb.toString());
					} catch (Exception e) {
						e.printStackTrace();
					}
					logger.info("Printed information to: " + file.getAbsolutePath());
				}
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

		ToeflTest test = new ToeflTest();

		ISemanticModel lsa1 = LSA.loadLSA("resources/config/LSA/tasa_en", Lang.eng);
		test.process("resources/in/toefl_test/", lsa1);
		ISemanticModel lsa2 = LSA.loadLSA("resources/config/LSA/coca_en/text_newspaper_lsp", Lang.eng);
		test.process("resources/in/toefl_test/", lsa2);
		ISemanticModel lda1 = LDA.loadLDA("resources/config/LDA/tasa_en", Lang.eng);
		test.process("resources/in/toefl_test/", lda1);
		ISemanticModel lda2 = LDA.loadLDA("resources/config/LDA/coca_en/text_newspaper_lsp", Lang.eng);
		test.process("resources/in/toefl_test/", lda2);
	}
}
