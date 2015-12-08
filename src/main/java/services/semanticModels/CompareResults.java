package services.semanticModels;

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

import data.AbstractDocument;
import data.AbstractDocumentTemplate;
import data.Word;
import data.discourse.Topic;
import data.document.Document;
import edu.cmu.lti.jawjaw.pobj.Lang;
import services.semanticModels.LSA.LSA;

public class CompareResults {
	static Logger logger = Logger.getLogger(CompareResults.class);
	private Map<Word, Map<Word, Integer>> wordAssociations;
	private Map<Word, Double> simTop;
	private Map<Word, Double> simMax;
	private Map<Word, Word> simMaxConcept;

	public void initialLoad(String pathToFile, Lang lang, int countMax) {
		wordAssociations = new TreeMap<Word, Map<Word, Integer>>();
		try {
			FileInputStream inputFile = new FileInputStream(pathToFile);
			InputStreamReader ir = new InputStreamReader(inputFile, "UTF-8");
			BufferedReader in = new BufferedReader(ir);
			String line;
			while ((line = in.readLine()) != null) {
				if (line.length() > 0) {
					StringTokenizer st = new StringTokenizer(line);
					try {
						AbstractDocumentTemplate docTmp = AbstractDocumentTemplate
								.getDocumentModel(st.nextToken() + " " + st.nextToken());

						AbstractDocument d = new Document(null, docTmp, null, null, lang, false, false);
						Word word1 = d.getBlocks().get(0).getSentences().get(0).getAllWords().get(0);
						Word word2 = d.getBlocks().get(0).getSentences().get(0).getAllWords().get(1);

						Integer noOccurences = Integer.valueOf(st.nextToken());
						if (!wordAssociations.containsKey(word1)) {
							wordAssociations.put(word1, new TreeMap<Word, Integer>());
						}
						if (countMax != -1) {
							if (wordAssociations.get(word1).size() < countMax)
								wordAssociations.get(word1).put(word2, noOccurences);
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
			logger.info("Comparing all word pairs");
			BufferedWriter out = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(semModel.getPath() + "/compare_individual.csv"), "UTF-8"));
			simTop = new TreeMap<Word, Double>();
			simMax = new TreeMap<Word, Double>();
			simMaxConcept = new TreeMap<Word, Word>();
			out.write("Word1,Word2,Cosine Similarity\n");
			for (Word word1 : wordAssociations.keySet()) {
				int sumWeights = 0;
				double sumSimilarities = 0;
				double max = 0;
				Word maxSimWord = null;
				for (Word word2 : wordAssociations.get(word1).keySet()) {
					double sim = semModel.getSimilarity(word1, word2);
					// sumWeights += wordAssociations.get(word1).get(word2);
					// sumSimilarities += wordAssociations.get(word1).get(word2)
					// * sim;
					sumWeights++;
					sumSimilarities += sim;
					out.write(word1.getLemma() + "," + word2.getLemma() + "," + sim + "\n");
					if (sim > max) {
						max = sim;
						maxSimWord = word2;
					}
				}
				if (sumWeights != 0 && maxSimWord != null) {
					simTop.put(word1, sumSimilarities / sumWeights);
					simMax.put(word1, max);
					simMaxConcept.put(word1, maxSimWord);
				} else {
					simTop.put(word1, new Double(0));
					simMax.put(word1, new Double(0));
				}
			}
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void compare(String pathToFile, ISemanticModel semModel, int countMax, boolean printSimilarConcepts,
			int noConcepts, double minThreshold) {
		initialLoad(pathToFile, semModel.getLanguage(), countMax);

		compareIndividual(semModel);

		try {
			BufferedWriter out = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(semModel.getPath() + "/compare_aggregated.csv"), "UTF-8"));
			out.write("Concept,Average Similarity,Max Similarity,Most similar word association\n");
			for (Word word : wordAssociations.keySet()) {
				out.write(word.getLemma() + "," + simTop.get(word) + "," + simMax.get(word)
						+ (simMaxConcept.containsKey(word) ? (",(" + simMaxConcept.get(word).getLemma() + ")") : ","));
				if (printSimilarConcepts) {
					// determine most similar concepts;
					List<Topic> similarConcepts = new LinkedList<Topic>();
					TreeMap<Word, Double> listLSA = semModel.getSimilarConcepts(word, minThreshold);
					for (Entry<Word, Double> entry : listLSA.entrySet()) {
						if (!entry.getKey().getLemma().equals(word.getLemma())
								&& !entry.getKey().getStem().equals(word.getStem()))
							similarConcepts.add(new Topic(entry.getKey(), entry.getValue()));
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
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void printSimilarConcepts(String path, Lang lang, int noConcepts, double minThreshold) {
		try {
			LSA lsa = LSA.loadLSA(path, lang);
			logger.info("Determining most similar word pairs for each concept");
			BufferedWriter out = new BufferedWriter(
					new OutputStreamWriter(new FileOutputStream(path + "/similar_concepts.csv"), "UTF-8"));
			out.write("Concept,Similar words\n");
			int noWords = lsa.getWords().size();
			int no = 0;
			for (Word word : lsa.getWords().keySet()) {
				out.write(word.getLemma());
				// determine most similar concepts;
				List<Topic> similarConcepts = new LinkedList<Topic>();
				TreeMap<Word, Double> listLSA = lsa.getSimilarConcepts(word, minThreshold);
				for (Entry<Word, Double> entry : listLSA.entrySet()) {
					if (!entry.getKey().getLemma().equals(word.getLemma())
							&& !entry.getKey().getStem().equals(word.getStem()))
						similarConcepts.add(new Topic(entry.getKey(), entry.getValue()));
				}
				Collections.sort(similarConcepts);
				// output top concepts
				for (int i = 0; i < Math.min(noConcepts, similarConcepts.size()); i++) {
					out.write("," + similarConcepts.get(i).getWord().getLemma() + ","
							+ similarConcepts.get(i).getRelevance());
				}
				if ((++no) % 1000 == 0) {
					logger.info("Finished comparing " + no + " words out of " + noWords);
				}
				out.write("\n");
			}
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public Map<Word, Map<Word, Integer>> getWordAssociations() {
		return wordAssociations;
	}

	public static void main(String[] args) {
		BasicConfigurator.configure();
		CompareResults comp = new CompareResults();

		LSA lsa = LSA.loadLSA("resources/config/LSA/financial2_en", Lang.eng);
		comp.compare("resources/config/LSA/word_associations_en.txt", lsa, 3, true, 20, 0.3);

		// LDA lda = LDA.loadLDA("resources/config/LDA/tasa_new_en", Lang.eng);
		// comp.compare("resources/config/LSA/word_associations_en.txt", lda, 3,
		// false, 20, 0.3);
		// printSimilarConcepts("resources/config/LSA/joseantonion_es", Lang.es,
		// 20, 0.3);
		// comp.compare("resources/config/LSA/tasa_lak_en",
		// "word_associations_en.txt",
		// Lang.en, 0.3);
	}
}
