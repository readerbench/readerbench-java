package runtime.semanticModels;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.StringTokenizer;
import java.util.TreeMap;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;

import data.Word;
import data.discourse.Topic;
import data.document.Document;
import edu.cmu.lti.jawjaw.pobj.Lang;
import services.semanticModels.ISemanticModel;
import services.semanticModels.LDA.LDA;
import services.semanticModels.LSA.LSA;
import webService.ReaderBenchServer;

public class WordAssociationTest {
	static Logger logger = Logger.getLogger(WordAssociationTest.class);
	private Map<Document, Map<Document, Double>> wordAssociations;
	private Map<Document, Double> simTop;
	private Map<Document, Double> simMax;
	private Map<Document, Document> simMaxConcept;

	public void initialLoad(String pathToFile, ISemanticModel semModel, int countMax) {
		wordAssociations = new TreeMap<Document, Map<Document, Double>>();
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
			FileInputStream inputFile = new FileInputStream(pathToFile);
			InputStreamReader ir = new InputStreamReader(inputFile, "UTF-8");
			BufferedReader in = new BufferedReader(ir);
			String line;
			logger.info("Parsing word associations file...");
			while ((line = in.readLine()) != null) {
				if (line.length() > 0) {
					StringTokenizer st = new StringTokenizer(line, ",");
					try {
						Document doc1 = VocabularyTest.processDoc(st.nextToken(), lsa, lda, semModel.getLanguage());
						Document doc2 = VocabularyTest.processDoc(st.nextToken(), lsa, lda, semModel.getLanguage());

						Double no = Double.valueOf(st.nextToken());
						if (!wordAssociations.containsKey(doc1)) {
							wordAssociations.put(doc1, new TreeMap<Document, Double>());
						}
						if (countMax != -1) {
							if (wordAssociations.get(doc1).size() < countMax)
								wordAssociations.get(doc1).put(doc2, no);
						}
					} catch (Exception e) {
					}
				}
			}

			in.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void compareIndividual(ISemanticModel semModel) {
		try {
			logger.info("Comparing all word pairs...");
			BufferedWriter out = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(semModel.getPath() + "/compare_individual.csv"), "UTF-8"));
			simTop = new TreeMap<Document, Double>();
			simMax = new TreeMap<Document, Double>();
			simMaxConcept = new TreeMap<Document, Document>();
			int no = 0;

			out.write("Word1,Word2,Similarity\n");
			for (Document doc1 : wordAssociations.keySet()) {
				int sumWeights = 0;
				double sumSimilarities = 0;
				double max = 0;
				Document maxSim = null;
				for (Document doc2 : wordAssociations.get(doc1).keySet()) {

					double sim = semModel.getSimilarity(doc1, doc2);
					// sumWeights += wordAssociations.get(word1).get(word2);
					// sumSimilarities += wordAssociations.get(word1).get(word2)
					// * sim;
					sumWeights++;
					sumSimilarities += sim;
					out.write(doc1.getText().trim() + "," + doc2.getText().trim() + "," + sim + "\n");
					if (sim > max) {
						max = sim;
						maxSim = doc2;
					}
				}
				if ((++no) % 1000 == 0) {
					logger.info("Finished comparing " + no + " words...");
				}
				if (sumWeights != 0 && maxSim != null) {
					simTop.put(doc1, sumSimilarities / sumWeights);
					simMax.put(doc1, max);
					simMaxConcept.put(doc1, maxSim);
				} else {
					simTop.put(doc1, new Double(0));
					simMax.put(doc1, new Double(0));
				}
			}
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void compare(String pathToFile, ISemanticModel semModel, int countMax, boolean printSimilarConcepts,
			int noConcepts, double minThreshold) {
		initialLoad(pathToFile, semModel, countMax);

		compareIndividual(semModel);

		logger.info("Performing comparisons of word associations...");

		try {
			BufferedWriter out = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(semModel.getPath() + "/compare_aggregated.csv"), "UTF-8"));
			out.write("Concept,Average Similarity,Max Similarity,Most similar word association\n");
			for (Document doc : wordAssociations.keySet()) {
				out.write(doc.getText().trim() + "," + simTop.get(doc) + "," + simMax.get(doc)
						+ (simMaxConcept.containsKey(doc) ? (",(" + simMaxConcept.get(doc).getText().trim() + ")")
								: ","));
				if (printSimilarConcepts) {
					// determine most similar concepts;
					List<Topic> similarConcepts = new LinkedList<Topic>();
					TreeMap<Word, Double> listLSA = semModel.getSimilarConcepts(doc, minThreshold);
					for (Entry<Word, Double> entry : listLSA.entrySet()) {
						for (Word word : doc.getWordOccurences().keySet()) {
							if (!entry.getKey().getLemma().equals(word.getLemma())
									&& !entry.getKey().getStem().equals(word.getStem())) {
								similarConcepts.add(new Topic(entry.getKey(), entry.getValue()));
							}
						}
					}
					Collections.sort(similarConcepts);
					// output top 5 concepts
					for (int i = 0; i < Math.min(noConcepts, similarConcepts.size()); i++) {
						out.write("," + similarConcepts.get(i).getWord().getLemma() + ","
								+ similarConcepts.get(i).getRelevance());
					}
				}
				out.write("\n");
			}
			out.close();

			logger.info("Finished all comparisons for word associations...");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public Map<Document, Map<Document, Double>> getWordAssociations() {
		return wordAssociations;
	}

	public static void main(String[] args) {
		BasicConfigurator.configure();

		ReaderBenchServer.initializeDB();

		WordAssociationTest comp = new WordAssociationTest();

		// LSA lsa = LSA.loadLSA("resources/config/LSA/tasa_en", Lang.eng);
		// LDA lsa = LDA.loadLDA("resources/config/LDA/tasa_new_en", Lang.eng);
		// LSA lsa = LSA.loadLSA("resources/config/LSA/joseantonio_es",
		// Lang.es);

		// comp.compare("resources/config/LSA/Nelson norms_en.csv", lsa, 3,
		// true, 20, 0.3);
		// comp.compare("resources/config/LSA/Normas Palabras C4819_es.csv",
		// lsa, 3, true, 20, 0.3);

		// LDA lda = LDA.loadLDA("resources/config/LDA/tasa_new_en", Lang.eng);
		LDA lda = LDA.loadLDA("resources/config/LDA/joseantonio_es", Lang.es);
		comp.compare("resources/config/LSA/Normas Palabras C4819_es.csv", lda, 3, true, 20, 0.3);
		// comp.compare("resources/config/LSA/Nelson norms_en.csv", lda, 3,
		// false, 20, 0.3);
		// comp.compare("resources/config/LSA/tasa_lak_en",
		// "resources/config/LSA/Nelson norms_en.csv",
		// Lang.en, 0.3);
	}
}
