package services.discourse.topicMining;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.log4j.Logger;

import data.AbstractDocument;
import data.AnalysisElement;
import data.Word;
import data.discourse.SemanticCohesion;
import data.discourse.Topic;
import services.commons.VectorAlgebra;
import services.complexity.wordComplexity.WordComplexity;
import services.semanticModels.LDA.LDA;
import services.semanticModels.LSA.LSA;
import services.semanticModels.WordNet.OntologySupport;

public class TopicModeling {
	static Logger logger = Logger.getLogger(TopicModeling.class);
	public static final int NUM_CONCEPTS = 50;
	public static final int NUM_CONCEPTS_PER_FILE = 20;

	public static final double LSA_WEIGHT = 1.0;
	public static final double LDA_WEIGHT = 1.0;
	public static final double WN_WEIGHT = 1.0;
	public static final double SIMILARITY_THRESHOLD = 0.9;

	public static void determineTopics(AnalysisElement e, AbstractDocument d) {
		logger.info("Determining topics using Tf-IDf, LSA and LDA");
		// determine topics by using Tf-IDF and LSA
		for (Word w : e.getWordOccurences().keySet()) {
			Topic newTopic = new Topic(w, e, d);
			int index = e.getTopics().indexOf(newTopic);
			if (index >= 0) {
				// update frequency
				Topic refTopic = e.getTopics().get(index);
				refTopic.updateRelevance(e, d);
			} else {
				e.getTopics().add(newTopic);
			}
		}
		Collections.sort(e.getTopics());
	}

	public static List<Topic> getSublist(List<Topic> topics, int noTopics,
			boolean nounsOnly, boolean verbsOnly) {
		List<Topic> results = new LinkedList<Topic>();
		for (Topic t : topics) {
			if (results.size() >= noTopics || t.getRelevance() < 0)
				break;
			if (nounsOnly && t.getWord().getPOS() != null
					&& t.getWord().getPOS().startsWith("N"))
				results.add(t);
			if (verbsOnly && t.getWord().getPOS() != null
					&& t.getWord().getPOS().startsWith("V"))
				results.add(t);
			if (!nounsOnly && !verbsOnly)
				results.add(t);
		}
		return results;
	}

	private static void mergeMaps(Map<Word, Double> m1, Map<Word, Double> m2,
			double factor) {
		// merge all occurrences of m2 into m1
		if (m2 == null)
			return;
		for (Word w2 : m2.keySet()) {
			if (m1.containsKey(w2)) {
				m1.put(w2, m1.get(w2) + m2.get(w2) * factor);
			} else {
				m1.put(w2, m2.get(w2) * factor);
			}
		}
	}

	public static Map<Word, Double> getCollectionTopics(
			List<? extends AbstractDocument> loadedDocuments) {
		Map<String, Double> topicScoreMap = new TreeMap<String, Double>();
		Map<String, Word> stemToWord = new TreeMap<String, Word>();

		// List<Topic> topicL = new ArrayList<Topic>();
		for (AbstractDocument d : loadedDocuments) {
			List<Topic> docTopics = d.getTopics();
			Collections.sort(docTopics, new Comparator<Topic>() {
				public int compare(Topic t1, Topic t2) {
					return -Double.compare(t1.getRelevance(), t2.getRelevance());
				}
			});
			for (int i = 0; i < Math.min(NUM_CONCEPTS_PER_FILE,
					docTopics.size()); i++) {
				String stem = docTopics.get(i).getWord().getStem();
				if (!topicScoreMap.containsKey(stem)) {
					topicScoreMap.put(stem, docTopics.get(i).getRelevance());
					stemToWord.put(stem, docTopics.get(i).getWord());
				} else {
					double topicRel = topicScoreMap.get(stem)
							+ docTopics.get(i).getRelevance();
					topicScoreMap.put(stem, topicRel);
					// shorter lemmas are stored
					if (stemToWord.get(stem).getLemma().length() > docTopics
							.get(i).getWord().getLemma().length())
						stemToWord.put(stem, docTopics.get(i).getWord());
				}
			}
		}

		List<Topic> topicL = new ArrayList<Topic>();
		Iterator<Map.Entry<String, Double>> mapIter = topicScoreMap.entrySet()
				.iterator();
		while (mapIter.hasNext()) {
			Map.Entry<String, Double> entry = mapIter.next();
			topicL.add(new Topic(stemToWord.get(entry.getKey()), entry
					.getValue()));
		}
		Collections.sort(topicL);
		topicL = topicL.subList(0, Math.min(NUM_CONCEPTS, topicL.size()));
		Map<Word, Double> newTopicScoreMap = new HashMap<Word, Double>();

		for (Topic t : topicL) {
			newTopicScoreMap.put(t.getWord(), t.getRelevance());
		}
		// save results as well
		// create measurements.csv header
		// try {
		// FileWriter fstream = new FileWriter("out/topics_"
		// + new Timestamp(new Date().getTime()) + ".csv", false);
		// BufferedWriter out = new BufferedWriter(fstream);
		// out.write("Keyword,Relevance\n");
		// for (Topic t : topicL) {
		// out.write(t.getWord().getLemma() + "," + t.getRelevance()
		// + "\n");
		// }
		// out.close();
		// } catch (Exception e) {
		// logger.error("Runtime error while exporting corpora topics");
		// e.printStackTrace();
		// }

		return newTopicScoreMap;
	}

	private static boolean containsStem(Word word, Set<Word> words) {
		for (Word w : words) {
			if (w.getStem().equals(word.getStem())) {
				return true;
			}
		}
		return false;
	}

	public static void determineInferredConcepts(AnalysisElement e,
			List<Topic> topics, double minThreshold) {
		logger.info("Determining inferred concepts");
		List<Topic> inferredConcepts = new LinkedList<Topic>();
		double[] topicsLSAVector = null;
		String topicString = "";
		double[] topicsLDAProbDistribution = null;

		// determine corresponding LSA vector for all selected topics

		if (e.getLSA() != null) {
			topicsLSAVector = new double[LSA.K];
			for (Topic t : topics) {
				if (t.getRelevance() > 0) {
					for (int i = 0; i < LSA.K; i++) {
						topicsLSAVector[i] += t.getWord().getLSAVector()[i]
								* t.getRelevance();
					}
					topicString += t.getWord().getLemma() + " ";
				}
			}
		}
		topicString = topicString.trim();
		if (e.getLDA() != null)
			topicsLDAProbDistribution = e.getLDA().getProbDistribution(
					topicString);

		TreeMap<Word, Double> inferredConceptsCandidates = new TreeMap<Word, Double>();

		// create possible matches by exploring 3 alternatives
		// 1 LSA
		logger.info("Determining similar concepts using LSA");
		if (e.getLSA() != null) {
			TreeMap<Word, Double> listLSA;

			for (Topic t : topics) {
				listLSA = e.getLSA().getSimilarConcepts(t.getWord(),
						minThreshold);
				mergeMaps(inferredConceptsCandidates, listLSA, LSA_WEIGHT);
			}
		}

		// 2 LDA
		logger.info("Determining similar concepts using LDA");
		if (e.getLDA() != null) {
			TreeMap<Word, Double> listLDA;
			for (Topic t : topics) {
				listLDA = e.getLDA().getSimilarConcepts(t.getWord(),
						minThreshold);
				mergeMaps(inferredConceptsCandidates, listLDA, LDA_WEIGHT);
			}
		}

		// 3 WN
		logger.info("Determining similar concepts using WN");
		TreeMap<Word, Double> listWN;
		for (Topic t : topics) {
			listWN = OntologySupport.getSimilarConcepts(t.getWord());
			mergeMaps(inferredConceptsCandidates, listWN, WN_WEIGHT);
		}

		// rearrange previously identified concepts
		logger.info("Building final list of inferred concepts");
		for (Word w : inferredConceptsCandidates.keySet()) {
			if (!containsStem(w, e.getWordOccurences().keySet())) {
				// possible candidate as inferred concept
				double lsaSim = 0;
				double ldaSim = 0;

				// sim to each topic
				double sumRelevance = 0;
				for (Topic t : topics) {
					if (t.getRelevance() > 0) {
						if (e.getLSA() != null)
							lsaSim += VectorAlgebra.cosineSimilarity(e.getLSA()
									.getWordVector(w), t.getWord()
									.getLSAVector())
									* t.getRelevance();
						sumRelevance += t.getRelevance();
						if (e.getLDA() != null)
							ldaSim = LDA.getSimilarity(w
									.getLDAProbDistribution(), t.getWord()
									.getLDAProbDistribution());
					}
				}
				if (sumRelevance != 0) {
					lsaSim /= sumRelevance;
					ldaSim /= sumRelevance;
				}

				// sim to topic vector
				if (e.getLSA() != null)
					lsaSim += VectorAlgebra.cosineSimilarity(e.getLSA()
							.getWordVector(w), topicsLSAVector);
				if (e.getLDA() != null)
					ldaSim += LDA.getSimilarity(w.getLDAProbDistribution(),
							topicsLDAProbDistribution);

				// sim to analysis element
				if (e.getLSA() != null)
					lsaSim += VectorAlgebra.cosineSimilarity(e.getLSA()
							.getWordVector(w), e.getLSAVector());
				if (e.getLDA() != null)
					ldaSim += LDA.getSimilarity(w.getLDAProbDistribution(),
							e.getLDAProbDistribution());

				// penalty for specificity
				double height = WordComplexity.getDistanceToHypernymTreeRoot(w,
						e.getLanguage());
				if (height == -1)
					height = 10;

				double relevance = inferredConceptsCandidates.get(w)
						* (SemanticCohesion.getAggregatedSemanticMeasure(
								lsaSim, ldaSim)) / (1 + height);

				Topic t = new Topic(w, relevance);

				if (inferredConcepts.contains(t)) {
					Topic updatedTopic = inferredConcepts.get(inferredConcepts
							.indexOf(t));
					updatedTopic.setRelevance(updatedTopic.getRelevance()
							+ relevance);
				} else
					inferredConcepts.add(t);
			}
		}

		Collections.sort(inferredConcepts);
		e.setInferredConcepts(inferredConcepts);
		logger.info("Finished building list of inferred concepts");
	}
}