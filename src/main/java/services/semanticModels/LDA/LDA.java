package services.semanticModels.LDA;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Pattern;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;

import DAO.AnalysisElement;
import DAO.Word;
import cc.mallet.pipe.CharSequence2TokenSequence;
import cc.mallet.pipe.CharSequenceLowercase;
import cc.mallet.pipe.Pipe;
import cc.mallet.pipe.SerialPipes;
import cc.mallet.pipe.TokenSequence2FeatureSequence;
import cc.mallet.pipe.TokenSequenceRemoveStopwords;
import cc.mallet.pipe.iterator.CsvIterator;
import cc.mallet.topics.ParallelTopicModel;
import cc.mallet.topics.TopicInferencer;
import cc.mallet.types.IDSorter;
import cc.mallet.types.Instance;
import cc.mallet.types.InstanceList;
import cc.mallet.util.Maths;
import edu.cmu.lti.jawjaw.pobj.Lang;
import services.commons.ObjectManipulation;
import services.commons.VectorAlgebra;
import services.semanticModels.ISemanticModel;

public class LDA implements ISemanticModel, Serializable {
	private static final long serialVersionUID = 5981303412937874248L;
	static Logger logger = Logger.getLogger(LDA.class);
	private static int MIN_NO_WORDS_PER_DOCUMENT = 5;

	private static List<LDA> LOADED_LDA_MODELS = new LinkedList<LDA>();

	private Lang language;
	private String path;
	private ParallelTopicModel model;
	private Pipe pipe;
	private InstanceList instances;
	private Map<Word, double[]> wordProbDistributions;

	public LDA(Lang language) {
		this.language = language;
		pipe = buildPipe();
	}

	private LDA(String path, Lang language) {
		this(language);
		this.path = path;
		logger.info("Loading LDA model");
		try {
			model = (ParallelTopicModel) ObjectManipulation.loadObject(path + "/LDA.model");
			buildWordVectors();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static LDA loadLDA(String path, Lang language) {
		for (LDA lda : LOADED_LDA_MODELS) {
			if (path.equals(lda.getPath())) {
				return lda;
			}
		}
		LDA ldaLoad = new LDA(path, language);

		if (ldaLoad != null) {
			LOADED_LDA_MODELS.add(ldaLoad);
		}

		return ldaLoad;
	}

	public void readDirectory(File directory) {
		// read all TXT files within a directory
		if (directory.isDirectory()) {
			instances = new InstanceList(pipe);
			for (File f : directory.listFiles(new FileFilter() {
				private final String[] okFileExtensions = new String[] { "txt" };

				public boolean accept(File file) {
					for (String extension : okFileExtensions) {
						if (file.getName().toLowerCase().endsWith(extension)) {
							return true;
						}
					}
					return false;
				}
			})) {
				// Now process each instance provided by the iterator.
				Reader fileReader;
				try {
					fileReader = new InputStreamReader(new FileInputStream(f), "UTF-8");
					instances.addThruPipe(
							new CsvIterator(fileReader, Pattern.compile("^(\\S*)[\\s,]*(\\S*)[\\s,]*(.*)$"), 3, 2, 1)); // data,
																														// label,
																														// name
																														// fields
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	private Pipe buildPipe() {
		// Begin by importing documents from text to feature sequences
		ArrayList<Pipe> pipeList = new ArrayList<Pipe>();

		// Pipes: lowercase, tokenize, remove stopwords, map to features
		pipeList.add(new CharSequenceLowercase());

		// Tokenize raw strings
		Pattern tokenPattern = Pattern.compile("\\p{L}[\\p{L}\\p{P}]+\\p{L}");
		pipeList.add(new CharSequence2TokenSequence(tokenPattern));

		// remove stopwords
		switch (language) {
		case fr:
			pipeList.add(new TokenSequenceRemoveStopwords(new File("resources/config/Stopwords/stopwords_fr.txt"), "UTF-8", false,
					false, false));
			break;
		case it:
			pipeList.add(new TokenSequenceRemoveStopwords(new File("resources/config/Stopwords/stopwords_it.txt"), "UTF-8", false,
					false, false));
			break;
		case ro:
			pipeList.add(new TokenSequenceRemoveStopwords(new File("resources/config/Stopwords/stopwords_ro.txt"), "UTF-8", false,
					false, false));
			break;
		case es:
			pipeList.add(new TokenSequenceRemoveStopwords(new File("resources/config/Stopwords/stopwords_es.txt"), "UTF-8", false,
					false, false));
			break;
		default:
			pipeList.add(new TokenSequenceRemoveStopwords(new File("resources/config/Stopwords/stopwords_en.txt"), "UTF-8", false,
					false, false));
		}

		// Rather than storing tokens as strings, convert
		// them to integers by looking them up in an alphabet.
		pipeList.add(new TokenSequence2FeatureSequence());

		// Print out the features and the label
		// pipeList.add(new PrintInputAndTarget());

		return new SerialPipes(pipeList);
	}

	/**
	 * Analyze number of topics
	 **/
	public int createHDPModel(String path, int initialTopics, int numIterations) {
		logger.info("Running HDP on " + path + " with " + initialTopics + " initial topics and " + numIterations
				+ " iterations");
		readDirectory(new File(path));

		HDP hdp = new HDP(1.0, 0.01, 1D, initialTopics);
		hdp.initialize(instances);

		// set number of iterations, and display result or not
		hdp.estimate(numIterations);

		// get topic distribution for first instance
		// double[] distr = hdp.topicDistribution(0);
		// // print out
		//
		// int no = 0;
		// for (int j = 0; j < distr.length; j++) {
		// if (Math.round(distr[j]) != 0) {
		// System.out.print("!!" + j + "-" + distr[j] + "\n");
		// no++;
		// }
		// }
		// System.out.println(no);

		// for inferencer
		// readDirectory(new File(path));
		// HDPInferencer inferencer = hdp.getInferencer();

		// inferencer.setInstance(instances);
		//
		// inferencer.estimate(numIterations / 10);
		// // get topic distribution for first test instance
		// distr = inferencer.topicDistribution(0);
		// // print out
		// for (int j = 0; j < distr.length; j++) {
		// System.out.print(distr[j] + "\n");
		// }
		// // get preplexity
		// double prep = inferencer.getPreplexity();
		// System.out.println("preplexity for the test set=" + prep);

		// 10-folds cross validation, with 1000 iteration for each test.
		// hdp.runCrossValidation(10, 1000);

		hdp.printTopWord(100);
		return hdp.getNoTopics();
	}

	public void processCorpus(String path, int noTopics, int noThreads, int noIterations) throws IOException {

		readDirectory(new File(path));

		model = new ParallelTopicModel(noTopics, 1.0, 0.01);

		model.addInstances(instances);

		// Use two parallel samplers, which each look at one half the corpus and
		// combine
		// statistics after every iteration.
		model.setNumThreads(noThreads);

		// Run the model for 50 iterations and stop (this is for testing only,
		// for real applications, use 1000 to 2000 iterations)
		model.setNumIterations(noIterations);
		model.estimate();

		// save the trained model
		ObjectManipulation.saveObject(model, path + "/LDA.model");
	}

	public void buildWordVectors() {
		ArrayList<TreeSet<IDSorter>> topicSortedWords = model.getSortedWords();
		wordProbDistributions = new TreeMap<Word, double[]>();
		// TreeMap<Word, Double> concepts = new TreeMap<Word, Double>();

		for (int topic = 0; topic < model.getNumTopics(); topic++) {
			// double max = 0;
			// for (IDSorter idCountPair : topicSortedWords.get(topic)) {
			// max = Math.max(idCountPair.getWeight(), max);
			// }
			// System.out.println("Topic " + topic + ": sum weights = " + sum);
			for (IDSorter idCountPair : topicSortedWords.get(topic)) {
				Word concept = Word.getWordFromConcept(model.getAlphabet().lookupObject(idCountPair.getID()).toString(),
						language);
				if (!wordProbDistributions.containsKey(concept))
					wordProbDistributions.put(concept, new double[model.getNumTopics()]);
				// if (!concepts.containsKey(concept))
				// concepts.put(concept, idCountPair.getWeight());
				// else
				// concepts.put(concept,
				// concepts.get(concept) + idCountPair.getWeight());
				// normalize results to maximum weight per topic
				wordProbDistributions.get(concept)[topic] = idCountPair.getWeight();
				// / max;
			}
		}

		// ValueComparator<Word> kcvc = new ValueComparator<Word>(concepts);
		// TreeMap<Word, Double> sortedConcepts = new TreeMap<Word,
		// Double>(kcvc);

		// for (Word c : wordProbDistributions.keySet()) {
		// double sum = 0;
		// for (int i = 0; i < model.getNumTopics(); i++) {
		// sum += wordProbDistributions.get(c)[i];
		// }
		//
		// // normalize vector to resemble a probability distribution
		// if (sum != 0) {
		// for (int i = 0; i < model.getNumTopics(); i++) {
		// wordProbDistributions.get(c)[i] /= sum;
		// }
		// }
		// }
		//
		// sortedConcepts.putAll(concepts);

		// System.out
		// .println("--Sorted concepts in terms of specificity with LDA--");
		// for (Object key : sortedConcepts.keySet()) {
		// System.out.print(key + " / " + sortedConcepts.get(key) + "\n");
		// }
	}

	public double[] getWordProbDistribution(Word word) {
		double[] probDistribution = new double[model.getNumTopics()];
		if (wordProbDistributions.containsKey(word)) {
			// words exist in learning space
			return wordProbDistributions.get(word);
		} else {
			// extract all words from the semantic space that have the same
			// stem
			int no = 0;
			for (Word w : wordProbDistributions.keySet()) {
				if (w.getStem().equals(word.getStem())) {
					double[] vector = wordProbDistributions.get(w);
					for (int i = 0; i < model.getNumTopics(); i++) {
						probDistribution[i] += vector[i];
					}
					no++;
				}
			}
			if (no != 0) {
				for (int i = 0; i < model.getNumTopics(); i++) {
					probDistribution[i] /= no;
				}
			}
		}
		return probDistribution;
	}

	public double[] getProbDistribution(String s) {
		// Create new instances with empty target and source fields.
		InstanceList processing = new InstanceList(pipe);

		processing.addThruPipe(new Instance(s, null, "analysis", null));

		TopicInferencer inferencer = model.getInferencer();
		return inferencer.getSampledDistribution(processing.get(0), 1000, 1, 5);
	}

	public double[] getProbDistribution(AnalysisElement e) {
		if (e.getWordOccurences().size() < MIN_NO_WORDS_PER_DOCUMENT) {
			double[] distrib = new double[model.getNumTopics()];
			for (Entry<Word, Integer> entry : e.getWordOccurences().entrySet()) {
				distrib = VectorAlgebra.sum(distrib,
						VectorAlgebra.scalarProduct(entry.getKey().getLDAProbDistribution(), entry.getValue()));
			}
			return VectorAlgebra.normalize(distrib);
		}
		return getProbDistribution(e.getProcessedText());
	}

	public int getNumTopics() {
		return model.getNumTopics();
	}

	public static double getSimilarity(double[] prob1, double[] prob2) {
		if (prob1 == null || prob2 == null)
			return 0;
		double sim = 1 - Maths.jensenShannonDivergence(VectorAlgebra.normalize(prob1), VectorAlgebra.normalize(prob2));
		if (sim >= 0 && sim <= 1)
			return sim;
		return 0;
	}

	public double getSimilarity(Word w1, Word w2) {
		return getSimilarity(getWordProbDistribution(w1), getWordProbDistribution(w2));
	}

	public double getSimilarity(AnalysisElement e1, AnalysisElement e2) {
		return 1 - Maths.jensenShannonDivergence(VectorAlgebra.normalize(e1.getLDAProbDistribution()),
				VectorAlgebra.normalize(e2.getLDAProbDistribution()));
	}

	public TreeMap<Word, Double> getSimilarConcepts(Word w, double minThreshold) {
		double[] prob1 = getWordProbDistribution(w);
		return getSimilarConcepts(prob1, minThreshold);
	}

	public TreeMap<Word, Double> getSimilarConcepts(AnalysisElement e, double minThreshold) {
		return getSimilarConcepts(e.getLDAProbDistribution(), minThreshold);
	}

	public TreeMap<Word, Double> getSimilarConcepts(double[] probDistribution, double minThreshold) {
		if (probDistribution == null)
			return null;
		TreeMap<Word, Double> similarConcepts = new TreeMap<Word, Double>();
		double[] prob2 = null;
		double sim;
		for (Word c : wordProbDistributions.keySet()) {
			prob2 = getWordProbDistribution(c);
			// sim = VectorAlgebra.cosineSimilarity(prob1, prob2);
			sim = getSimilarity(VectorAlgebra.normalize(probDistribution), VectorAlgebra.normalize(prob2));
			if (sim >= minThreshold) {
				similarConcepts.put(c, sim);
			}
		}
		return similarConcepts;
	}

	public void printSimilarities(String s, double[] prob1, double[] prob2) {
		System.out.println(s);
		System.out.println("Cosine:" + VectorAlgebra.cosineSimilarity(prob1, prob2));
		System.out.println("sim JSH:" + (1 - Maths.jensenShannonDivergence(prob1, prob2)));
		System.out.println("KL:" + Maths.klDivergence(prob1, prob2));
		System.out.println("Sim KL:"
				+ Math.pow(Math.E, -(Maths.klDivergence(prob1, prob2) + Maths.klDivergence(prob2, prob1))) + "\n");
	}

	public void printTopics(String path, int noWordsPerTopic) throws IOException {
		logger.info("Starting to write topics for trained model");
		BufferedWriter out = new BufferedWriter(
				new OutputStreamWriter(new FileOutputStream(path + "/topics.bck"), "UTF-8"));

		// Get an array of sorted sets of word ID/count pairs
		ArrayList<TreeSet<IDSorter>> topicSortedWords = model.getSortedWords();

		// Show top <<noTopics>> concepts
		for (int topic = 0; topic < model.getNumTopics(); topic++) {
			Iterator<IDSorter> iterator = topicSortedWords.get(topic).iterator();

			out.write(topic + "\t");
			int rank = 0;
			while (iterator.hasNext() && rank < noWordsPerTopic) {
				IDSorter idCountPair = iterator.next();
				out.write(model.getAlphabet().lookupObject(idCountPair.getID()) + "(" + idCountPair.getWeight() + ") ");
				rank++;
			}
			out.write("\n\n");
		}
		out.close();
		logger.info("Successfully finished writing topics");
	}

	public static int findMaxResemblance(double[] v1, double[] v2) {
		double max = Double.MIN_VALUE;
		int maxIndex = -1;
		if (v1.length != v2.length)
			return -1;
		for (int i = 0; i < v1.length; i++) {
			if (max < v1[i] * v2[i]) {
				max = v1[i] * v2[i];
				maxIndex = i;
			}
		}
		return maxIndex;
	}

	public void findDeepLearningRules(Word w1, Word w2, double minThreshold) {
		double[] prob1 = getWordProbDistribution(w1);
		double[] prob2 = getWordProbDistribution(w2);
		double[] sum = new double[getNumTopics()];
		double[] difference = new double[getNumTopics()];
		for (int i = 0; i < getNumTopics(); i++) {
			sum[i] = prob1[i] + prob2[i];
			difference[i] = Math.max(prob1[i] - prob2[i], 0);
		}
		TreeMap<Word, Double> similarSum = getSimilarConcepts(sum, minThreshold);
		if (!similarSum.isEmpty()) {
			for (Entry<Word, Double> sim : similarSum.entrySet()) {
				if (!sim.getKey().getStem().equals(w1.getStem()) && !sim.getKey().getStem().equals(w2.getStem())) {
					System.out.println(w1.getLemma() + "+" + w2.getLemma() + ">>" + sim.getKey().getLemma() + " ("
							+ sim.getValue() + ")");
				}
			}
		}
		TreeMap<Word, Double> similarDiff = getSimilarConcepts(difference, minThreshold);
		if (!similarDiff.isEmpty()) {
			for (Entry<Word, Double> sim : similarDiff.entrySet()) {
				if (!sim.getKey().getStem().equals(w1.getStem()) && !sim.getKey().getStem().equals(w2.getStem())) {
					System.out.println(w1.getLemma() + "-" + w2.getLemma() + ">>" + sim.getKey().getLemma() + " ("
							+ sim.getValue() + ")");
				}
			}
		}
	}

	public Lang getLanguage() {
		return language;
	}

	public void setLanguage(Lang language) {
		this.language = language;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public ParallelTopicModel getModel() {
		return model;
	}

	public void setModel(ParallelTopicModel model) {
		this.model = model;
	}

	public Pipe getPipe() {
		return pipe;
	}

	public void setPipe(Pipe pipe) {
		this.pipe = pipe;
	}

	public Map<Word, double[]> getWordProbDistributions() {
		return wordProbDistributions;
	}

	@Override
	public Set<Word> getWordSet() {
		return wordProbDistributions.keySet();
	}

	public void setWordProbDistributions(Map<Word, double[]> wordProbDistributions) {
		this.wordProbDistributions = wordProbDistributions;
	}

	public static void main(String[] args) {
		BasicConfigurator.configure();

		try {
			// LDA lda = new LDA(Lang.eng);
			// System.out
			// .println(lda.createHDPModel("in/LOWE1/class11", 5, 10000));
			String path = "resources/config/LDA/tasa_en";
			Lang lang = Lang.eng;

			LDA lda = new LDA(path, lang);

			// lda.printTopics(path, 300);
			Word w1 = Word.getWordFromConcept("mailman", lang);
			Word w2 = Word.getWordFromConcept("man", lang);
			System.out.println(lda.getSimilarity(w2, w1));

			lda.findDeepLearningRules(w1, w2, 0.5);
			// MathArrays.convolve(prob1, prob1);
			// System.out.println(lda.getSimilarity("god", "church"));
			// double[] prob1 =
			// lda.getProbDistribution(Word.getWordFromConcept(
			// "educational data mining", Lang.eng), 1);
			// double[] prob2 = lda.getProbDistribution(
			// Word.getWordFromConcept("learning analytics", Lang.eng),
			// 1);
			//
			// double[] prob3 = lda.getProbDistribution(
			// Word.getWordFromConcept("educational", Lang.eng), 1);
			// double[] prob4 = lda.getProbDistribution(
			// Word.getWordFromConcept("learning", Lang.eng), 1);
			//
			// System.out.println(LDA.getSimilarity(prob1, prob2));
			// System.out.println(LDA.getSimilarity(prob3, prob4));
			//
			// TreeMap<Word, Double> closest =
			// lda.getSimilarConcepts(prob3,
			// 0.6);
			// for (Word w : closest.keySet())
			// System.out.println(w.getLemma() + " "
			// + Formatting.formatNumber(closest.get(w)));
			// double[] prob3 = lda.getWordProbDistribution(Word
			// .getWordFromConcept("animal", Lang.eng));
			// double[] prob4 = lda.getWordProbDistribution(Word
			// .getWordFromConcept("bird", Lang.eng));
			// double[] prob5 = lda.getProbDistribution(
			// Word.getWordFromConcept("animal", Lang.eng), 1);
			// double[] prob6 = new
			// double[lda.getModel().getNumTopics()];
			//
			// double sum1 = 0, sum2 = 0, sum3 = 0;
			// for (int i = 0; i < prob1.length; i++) {
			// prob6[i] = prob3[i] + prob4[i] - prob3[i] * prob4[i];
			// sum1 += prob6[i];
			// sum2 += prob3[i];
			// sum3 += prob4[i];
			// // System.out.println(prob2[i] + " - " + prob3[i]);
			// }
			// System.out.println(sum1 + " " + sum2 + " " + sum3);
			//
			// lda.printSimilarities("1-2", prob1, prob2);
			// lda.printSimilarities("2-3", prob2, prob3);
			// lda.printSimilarities("3-4", prob3, prob4);
			// lda.printSimilarities("2-5", prob2, prob5);
			//
			// System.out.println("Sim "
			// + lda.getSimilarity(
			// Word.getWordFromConcept("riche", Lang.fr),
			// Word.getWordFromConcept("argenterie", Lang.fr)));
			// System.out
			// .println(LDA.getSimilarity(lda.getProbDistribution(
			// Word.getWordFromConcept("riche", Lang.fr), 1), lda
			// .getProbDistribution(Word.getWordFromConcept(
			// "argenterie", Lang.fr), 1)));
			//
			// TreeMap<Word, Double> closest = lda.getSimilarConcepts(
			// Word.getWordFromConcept("riche", Lang.fr), 0.5);
			// for (Word w : closest.keySet())
			// System.out.println(w);

			// System.out.println(lda.compareTerms("bird", "animal"));
			// System.out.println(lda.compareTerms("mother", "father"));
			// System.out.println(lda.compareTerms("black", "slave"));
			// System.out.println(lda.compareTerms("war", "soldier"));
			// System.out.println(lda.compareTerms("ugly", "bad"));
		} catch (Exception ex) {
			ex.printStackTrace();
			logger.error("Error during learning process");
		}
	}
}
