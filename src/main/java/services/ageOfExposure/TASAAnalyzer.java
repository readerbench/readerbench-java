package services.ageOfExposure;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Logger;

import org.apache.log4j.BasicConfigurator;

import services.commons.Formatting;
import services.commons.VectorAlgebra;
import services.semanticModels.LDA.LDA;
import data.Word;
import data.Lang;

public class TASAAnalyzer {
	private static Logger logger = Logger.getLogger("TASAAnalyzer");
	private final double MIN_SEM_SIMILARITY = 0.3;
	private final double MIN_THRESHOLD = 0.4;
	private final double MAX_THRESHOLD = 0.7;
	private final double THRESHOLD_INCREMENT = 0.1;

	private String path;
	private int noThreads;
	private Map<Integer, LDA> models;
	private LDA matureModel;
	private int noGrades;
	private Map<Word, List<Double>> aoeEvolution;

	public TASAAnalyzer(String path, int noThreads) {
		super();
		this.path = path;
		this.noThreads = noThreads;
	}

	public void loadModels() {
		noGrades = (new File(path)).listFiles(new FileFilter() {
			public boolean accept(File pathname) {
				if (pathname.isDirectory())
					return true;
				return false;
			}
		}).length;

		/* Load LDA Models */
		models = new TreeMap<Integer, LDA>();
		for (int i = 0; i < noGrades; i++) {
			String classPath = path + "/grade" + i;
			logger.info("Loading model " + classPath + "...");
			models.put(i, LDA.loadLDA(classPath, Lang.eng));
			logger.info("Loaded model with " + models.get(i).getNoTopics() + " topics.");
		}
	}

	public Integer[] computeMatchTask(LDA modelA, LDA modelB) {
		logger.info("Matching " + modelA.getPath() + " to " + modelB.getPath() + "...");

		/* Find best topic matches */
		TopicMatchGraph graph = new TopicMatchGraph(modelA.getNoTopics() + modelB.getNoTopics());
		int i, j, x, y;
		double distance = 0D;

		for (i = 0; i < modelA.getNoTopics(); i++) {
			for (j = 0; j < modelB.getNoTopics(); j++) {
				distance = LDASupport.topicDistance(modelA, i, modelB, j);
				if (distance <= 1 - MIN_SEM_SIMILARITY) {
					x = i;
					y = j + modelA.getNoTopics();
					graph.addEdge(x, y, distance);
				}
			}
		}
		/* Compute Matches */
		Integer[] matches = graph.computeAssociations(modelA.getNoTopics());
		for (i = 0; i < matches.length; i++) {
			matches[i] -= modelA.getNoTopics();
		}

		return matches;
	}

	public void performMatching() throws InterruptedException, ExecutionException {
		/* Load "Mature" Model = Last complexity level */
		matureModel = models.get(noGrades - 1);
		LDA intermediateModel;

		ExecutorService taskPool = Executors.newFixedThreadPool(noThreads);
		List<Future<Integer[]>> asyncResults = new LinkedList<Future<Integer[]>>();

		/* Match topics with mature topics */
		for (int cLevel = 0; cLevel < noGrades - 1; cLevel++) {
			intermediateModel = models.get(cLevel);

			final LDA finalIntermediateModel = intermediateModel;
			final LDA finalMatureModel = matureModel;

			Callable<Integer[]> task = new Callable<Integer[]>() {
				public Integer[] call() throws Exception {
					return computeMatchTask(finalIntermediateModel, finalMatureModel);
				}
			};
			asyncResults.add(taskPool.submit(task));
		}

		Integer[] matches;
		List<Double> stats;
		aoeEvolution = new HashMap<Word, List<Double>>();
		for (Word analyzedWord : matureModel.getWordProbDistributions().keySet())
			aoeEvolution.put(analyzedWord, new LinkedList<Double>());

		for (int cLevel = 0; cLevel < noGrades - 1; cLevel++) {
			logger.info("Building word distributions for grade level " + cLevel + "...");
			intermediateModel = models.get(cLevel);
			matches = asyncResults.remove(0).get();

			Map<Integer, Map<Word, Double>> intermediateModelDistrib = new TreeMap<Integer, Map<Word, Double>>();
			Map<Integer, Map<Word, Double>> matureModelDistrib = new TreeMap<Integer, Map<Word, Double>>();

			for (int i = 0; i < intermediateModel.getNoTopics(); i++) {
				intermediateModelDistrib.put(i, LDASupport.getWordWeights(intermediateModel, i));
				if (matches[i] != null)
					matureModelDistrib.put(i, LDASupport.getWordWeights(matureModel, matches[i]));
				else
					matureModelDistrib.put(i, null);
			}

			/*
			 * Iterate all words from mature space and extract topic
			 * distribution
			 */
			logger.info("Matching all words for grade level " + cLevel + "...");
			for (Word analyzedWord : matureModel.getWordProbDistributions().keySet()) {
				double intermediateTopicDistr[] = new double[intermediateModel.getNoTopics()];
				double matureTopicDistr[] = new double[intermediateModel.getNoTopics()];

				double sumI = 0, sumM = 0, noI = 0, noM = 0;
				for (int i = 0; i < intermediateTopicDistr.length; i++) {
					if (intermediateModelDistrib.get(i).containsKey(analyzedWord)) {
						intermediateTopicDistr[i] = intermediateModelDistrib.get(i).get(analyzedWord);
						if (intermediateTopicDistr[i] > 0)
							noI++;
					}
					if (matureModelDistrib.get(i) != null && matureModelDistrib.get(i).containsKey(analyzedWord)) {
						matureTopicDistr[i] = matureModelDistrib.get(i).get(analyzedWord);
						if (matureTopicDistr[i] > 0)
							noM++;
					}
					sumI += intermediateTopicDistr[i];
					sumM += matureTopicDistr[i];
				}
				/* Normalize topic distribution */
				// for (i = 0; i < intermediateModel.getNumTopics(); i++) {
				// if (sumI != 0)
				// intermediateTopicDistr[i] /= sumI;
				// if (sumM != 0)
				// matureTopicDistr[i] /= sumM;
				// }

				stats = aoeEvolution.get(analyzedWord);
				double similarity = 0;
				if (sumI != 0 && sumM != 0)
					similarity = VectorAlgebra.cosineSimilarity(intermediateTopicDistr, matureTopicDistr);
				if (similarity == 1 && noI == 1 && noM == 1) {
					similarity = (sumM + sumI) / 2;
				}
				stats.add(similarity);
			}
		}
		taskPool.shutdown();
	}

	public Map<String, Double> getWordAcquisitionAge(String normFile) {
		Map<String, Double> aoaWords = new HashMap<String, Double>();
		logger.info("Loading file " + normFile + "...");

		/* Compute the AgeOfAcquisition Dictionary */
		String tokens[];
		String line;
		String word;
		try {
			BufferedReader br = new BufferedReader(
					new FileReader("resources/config/WordLists/age_of_acquisition_en/" + normFile));
			while ((line = br.readLine()) != null) {
				tokens = line.split(",");
				word = tokens[0].trim().replaceAll(" ", "");

				if (tokens[1].equals("NA"))
					continue;

				Double.parseDouble(tokens[1]);
				aoaWords.put(word, Double.parseDouble(tokens[1]));
			}
			br.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return aoaWords;
	}

	public void writeResults() {
		// determine word acquisition ages
		Map<String, Double> birdAoA = getWordAcquisitionAge("Bird.csv");
		Map<String, Double> bristolAoA = getWordAcquisitionAge("Bristol.csv");
		Map<String, Double> corteseAoA = getWordAcquisitionAge("Cortese.csv");
		Map<String, Double> kupermanAoA = getWordAcquisitionAge("Kuperman.csv");
		Map<String, Double> shockAoA = getWordAcquisitionAge("Shock.csv");

		try {
			BufferedWriter loweStats = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(new File(path + "/AoE stats bipartite graph.csv")), "UTF-8"), 32768);
			BufferedWriter loweValues = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(new File(path + "/AoE words bipartite graph.csv")), "UTF-8"), 32768);
			// create header
			String content = "Word,Bird_AoA,Bristol_AoA,Cortese_AoA,Kuperman_AoA,Shock_AoA";
			loweStats.write(content);
			loweValues.write(content);
			for (int i = 0; i < noGrades - 1; i++) {
				loweStats.write(",Grades_1_" + (i + 1));
			}
			content = ",InverseAverage,InverseLinearRegressionSlope";
			for (double i = MIN_THRESHOLD; i <= MAX_THRESHOLD; i += THRESHOLD_INCREMENT) {
				content += ",IndexAboveThreshold(" + i + ")";
			}
			for (double i = MIN_THRESHOLD; i <= MAX_THRESHOLD; i += THRESHOLD_INCREMENT) {
				content += ",IndexPolynomialFitAboveThreshold(" + i + ")";
			}
			content += ",InflectionPointPolynomial\n";
			loweStats.write(content);
			loweValues.write(content);
			List<Double> stats;

			for (Word analyzedWord : matureModel.getWordProbDistributions().keySet()) {
				stats = aoeEvolution.get(analyzedWord);
				content = analyzedWord.getExtendedLemma() + ",";
				// AoA indices
				if (birdAoA.containsKey(analyzedWord.getLemma())) {
					content += birdAoA.get(analyzedWord.getLemma());
				}
				content += ",";
				if (bristolAoA.containsKey(analyzedWord.getLemma())) {
					content += bristolAoA.get(analyzedWord.getLemma());
				}
				content += ",";
				if (corteseAoA.containsKey(analyzedWord.getLemma())) {
					content += corteseAoA.get(analyzedWord.getLemma());
				}
				content += ",";
				if (kupermanAoA.containsKey(analyzedWord.getLemma())) {
					content += kupermanAoA.get(analyzedWord.getLemma());
				}
				content += ",";
				if (shockAoA.containsKey(analyzedWord.getLemma())) {
					content += shockAoA.get(analyzedWord.getLemma());
				}
				loweStats.write(content);
				loweValues.write(content);
				for (Double d : stats)
					loweStats.write("," + Formatting.formatNumber(d));
				double value = WordComplexityIndices.getInverseAverage(stats);
				if (Math.round(value * 100) / 100 != 1) {
					content = "," + WordComplexityIndices.getInverseAverage(stats);
					content += "," + WordComplexityIndices.getInverseLinearRegressionSlope(stats);
					for (double i = MIN_THRESHOLD; i <= MAX_THRESHOLD; i += THRESHOLD_INCREMENT) {
						value = WordComplexityIndices.getIndexAboveThreshold(stats, i);
						if (value != -1) {
							content += "," + value;
						} else {
							content += ",";
						}
					}

					for (double i = MIN_THRESHOLD; i <= MAX_THRESHOLD; i += THRESHOLD_INCREMENT) {
						value = WordComplexityIndices.getIndexPolynomialFitAboveThreshold(stats, i);
						if (value != -1) {
							content += "," + value;
						} else {
							content += ",";
						}
					}
					content += "," + WordComplexityIndices.getInflectionPointPolynomial(stats);
					loweStats.write(content);
					loweValues.write(content);
				}
				content = "\n";
				loweStats.write(content);
				loweValues.write(content);
			}
			loweStats.close();
			loweValues.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String args[]) throws Exception {
		BasicConfigurator.configure();

		TASAAnalyzer ta = new TASAAnalyzer("resources/in/AoE HDP", 6);
		// TASAAnalyzer ta = new TASAAnalyzer("resources/in/AoE 100", 6);
		ta.loadModels();
		ta.performMatching();
		ta.writeResults();
	}
}
