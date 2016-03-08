package services.semanticModels;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import data.AbstractDocument;
import data.AbstractDocumentTemplate;
import data.document.Document;
import edu.cmu.lti.jawjaw.pobj.Lang;
import services.ageOfExposure.TopicMatchGraph;
import services.commons.Formatting;
import services.semanticModels.LDA.LDA;
import services.semanticModels.LSA.LSA;
import webService.ReaderBenchServer;

public class VocabularyTest {

	static Logger logger = Logger.getLogger(VocabularyTest.class);

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
					sb.append("sep=,\nid,a,sim,b,sim,c,sim\n");

					logger.info("Processing file: " + f.getName());

					String line = null;
					BufferedReader br = new BufferedReader(new FileReader(f));
					AbstractDocument rhs[] = new AbstractDocument[6];
					AbstractDocument lhs[] = new AbstractDocument[3];

					while ((line = br.readLine()) != null) {
						if (line.startsWith(">>")) {
							sb.append(line + "\n");
						} else if (line.startsWith("----")) {
							int id = Integer.valueOf(line.replaceAll("-", ""));
							if (questionId != id)
								logger.error("Manual indexing corrupted at question " + questionId + "/" + id);
						} else if (line.startsWith("1.")) {
							rhs[0] = processDoc(line.substring(2), lsa, lda, semModel.getLanguage());
						} else if (line.startsWith("2.")) {
							rhs[1] = processDoc(line.substring(2), lsa, lda, semModel.getLanguage());
						} else if (line.startsWith("3.")) {
							rhs[2] = processDoc(line.substring(2), lsa, lda, semModel.getLanguage());
						} else if (line.startsWith("4.")) {
							rhs[3] = processDoc(line.substring(2), lsa, lda, semModel.getLanguage());
						} else if (line.startsWith("5.")) {
							rhs[4] = processDoc(line.substring(2), lsa, lda, semModel.getLanguage());
						} else if (line.startsWith("6.")) {
							rhs[5] = processDoc(line.substring(2), lsa, lda, semModel.getLanguage());
						} else if (line.startsWith("a.")) {
							lhs[0] = processDoc(line.substring(2), lsa, lda, semModel.getLanguage());
						} else if (line.startsWith("b.")) {
							lhs[1] = processDoc(line.substring(2), lsa, lda, semModel.getLanguage());
						} else if (line.startsWith("c.")) {
							lhs[2] = processDoc(line.substring(2), lsa, lda, semModel.getLanguage());

							// process current question
							TopicMatchGraph graph = new TopicMatchGraph(9);
							for (int i = 0; i < 3; i++) {
								for (int j = 0; j < 6; j++) {
									double sim = semModel.getSimilarity(lhs[i], rhs[j]);
									graph.addEdge(i, j + lhs.length, 1 - sim);
								}
							}

							Integer[] assoc = graph.computeAssociations();

							sb.append(questionId + ",");
							questionId += 1;
							for (int i = 0; i < assoc.length; i++) {
								sb.append((assoc[i] - lhs.length + 1) + ","
										+ Formatting.formatNumber(1 - graph.getEdge(i, assoc[i])) + ",");
							}

							sb.append("\n");
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
		} catch (

		Exception e)

		{
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

		VocabularyTest test = new VocabularyTest();

		// ISemanticModel lsa1 = LSA.loadLSA("resources/config/LSA/tasa_en",
		// Lang.eng);
		// test.process("resources/in/vocabulary_test/", lsa1);
		// ISemanticModel lsa2 =
		// LSA.loadLSA("resources/config/LSA/coca_en/text_newspaper_lsp",
		// Lang.eng);
		// test.process("resources/in/vocabulary_test/", lsa2);
		ISemanticModel lda1 = LDA.loadLDA("resources/config/LDA/tasa_en", Lang.eng);
		test.process("resources/in/vocabulary_test/", lda1);
		ISemanticModel lda2 = LDA.loadLDA("resources/config/LDA/coca_en/text_newspaper_lsp", Lang.eng);
		test.process("resources/in/vocabulary_test/", lda2);

	}
}
