package services.readingStrategies;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;

import services.commons.Formatting;
import services.commons.VectorAlgebra;
import DAO.document.Metacognition;

public class OptimalThresholds {
	static Logger logger = Logger.getLogger(OptimalThresholds.class);

	private List<File> serializedFiles;
	private List<Metacognition> loadedMetacognitions;

	private void addFiles(String path) {
		File[] toAdd = new File(path).listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.endsWith(".ser");
			}
		});
		for (File f : toAdd) {
			serializedFiles.add(f);
		}
	}

	public OptimalThresholds() {
		serializedFiles = new LinkedList<File>();
		loadedMetacognitions = new LinkedList<Metacognition>();
//		addFiles("in/verbalizations/Avaleur/CE2");
//		addFiles("in/verbalizations/Avaleur/CM1");
//		addFiles("in/verbalizations/Avaleur/CM2");

		 addFiles("in/verbalizations/Matilda/CE2");
		 addFiles("in/verbalizations/Matilda/CM1");
		 addFiles("in/verbalizations/Matilda/CM2");

		for (File f : serializedFiles) {
			loadedMetacognitions.add((Metacognition) Metacognition
					.loadSerializedDocument(f.getPath()));
		}
	}

	private double[] performIteration(Double threshold1, Double threshold2,
			int strategy) {
		if (strategy == ReadingStrategies.BRIDGING) {
			BridgingStrategy.setMinCohesion(threshold1);
			BridgingStrategy.setMaxParaphrasing(threshold2);
		}
		if (strategy == ReadingStrategies.INFERRED_KNOWLEDGE) {
			InferredKnowledgeStrategy.setSimilarityThresholdKI(threshold1);
		}

		// recompute the strategies
		for (Metacognition v : loadedMetacognitions) {
			ReadingStrategies.detReadingStrategies(v);
		}
		// corresponding to the statistics and to comprehension predictions
		int[][] automaticReadingStrategies = new int[loadedMetacognitions
				.size()][ReadingStrategies.NO_READING_STRATEGIES];
		int[][] annotatedReadingStrategies = new int[loadedMetacognitions
				.size()][ReadingStrategies.NO_READING_STRATEGIES];

		int index;
		for (index = 0; index < loadedMetacognitions.size(); index++) {
			if (loadedMetacognitions.get(index) != null) {
				for (int i = 0; i < loadedMetacognitions.get(index).getBlocks()
						.size(); i++) {
					for (int j = 0; j < ReadingStrategies.NO_READING_STRATEGIES; j++) {
						automaticReadingStrategies[index][j] += loadedMetacognitions
								.get(index).getAutomaticReadingStrategies()[i][j];
						annotatedReadingStrategies[index][j] += loadedMetacognitions
								.get(index).getAnnotatedReadingStrategies()[i][j];
					}
				}
			}
		}
		return new double[] {
				VectorAlgebra.pearsonCorrelation(VectorAlgebra.getVector(
						annotatedReadingStrategies, strategy), VectorAlgebra
						.getVector(automaticReadingStrategies, strategy)),
				VectorAlgebra.precision(VectorAlgebra.getVector(
						annotatedReadingStrategies, strategy), VectorAlgebra
						.getVector(automaticReadingStrategies, strategy)),
				VectorAlgebra.recall(VectorAlgebra.getVector(
						annotatedReadingStrategies, strategy), VectorAlgebra
						.getVector(automaticReadingStrategies, strategy)),
				VectorAlgebra.fscore(VectorAlgebra.getVector(
						annotatedReadingStrategies, strategy), VectorAlgebra
						.getVector(automaticReadingStrategies, strategy), 1) };
	}

	public void determineOptimum() {
		double maxThresold1 = Double.MIN_VALUE, maxCorrelation = Double.MIN_VALUE;
		double similarities[];

		try {
			// Open the file
			BufferedWriter out = new BufferedWriter(new FileWriter(
					"out/optimal_reading_strategies.csv", true));
			out.write("Knowledge inferrence\nThreshold min similarity,Pearson correlation,Precision,Recall,F1-score\n");
			
			out.close();
//			for (double threshold1 = 0.3; threshold1 <= 0.4; threshold1 = threshold1 + 0.01) {
//				similarities = performIteration(threshold1, null,
//						ReadingStrategies.INFERRED_KNOWLEDGE);
//				logger.info("Inferred knowledge with min cohesion threshold = "
//						+ Formatting.formatNumber(threshold1) + ":\t"
//						+ Formatting.formatNumber(similarities[0]) + "\t"
//						+ Formatting.formatNumber(similarities[1]) + "\t"
//						+ Formatting.formatNumber(similarities[2]) + "\t"
//						+ Formatting.formatNumber(similarities[3]));
//				out = new BufferedWriter(new FileWriter(
//						"out/optimal_reading_strategies.csv", true));
//				out.write(Formatting.formatNumber(threshold1) + ","
//						+ Formatting.formatNumber(similarities[0]) + ","
//						+ Formatting.formatNumber(similarities[1]) + ","
//						+ Formatting.formatNumber(similarities[2]) + ","
//						+ Formatting.formatNumber(similarities[3]) + "\n");
//				out.close();
//				if ((similarities[0] +similarities[3])/2> maxCorrelation) {
//					maxThresold1 = threshold1;
//					maxCorrelation = (similarities[0] +similarities[3])/2;
//				}
//			}
//			out = new BufferedWriter(new FileWriter(
//					"out/optimal_reading_strategies.csv", true));
//			out.write("Optimal\n" + Formatting.formatNumber(maxThresold1) + ","
//					+ Formatting.formatNumber(maxCorrelation));
//
//			logger.info("Optimal ki correlation of "
//					+ Formatting.formatNumber(maxCorrelation)
//					+ " is obtained by using min cohesion threshold = "
//					+ Formatting.formatNumber(maxThresold1));
//			out.close();

			maxThresold1 = Double.MIN_VALUE;
			double maxThresold2 = Double.MIN_VALUE;
			maxCorrelation = Double.MIN_VALUE;
			out = new BufferedWriter(new FileWriter(
					"out/optimal_reading_strategies.csv", true));
			out.write("\n\nBridging\nThreshold min cohesion,Max paraphrasing threshold,Pearson correlation,Precision,Recall,F1-score\n");
			out.close();
			
			for (double threshold1 = 0.4; threshold1 <= 0.6; threshold1 = threshold1 + 0.05) {
				for (double threshold2 = 0.5; threshold2 <= 0.7; threshold2 = threshold2 + 0.05) {
					similarities = performIteration(threshold1, threshold2,
							ReadingStrategies.BRIDGING);
					logger.info("Bridging with min cohesion threshold = "
							+ Formatting.formatNumber(threshold1)
							+ " and max paraphrasing threshold = "
							+ Formatting.formatNumber(threshold2) + ":\t"
							+ Formatting.formatNumber(similarities[0]) + "\t"
							+ Formatting.formatNumber(similarities[1]) + "\t"
							+ Formatting.formatNumber(similarities[2]) + "\t"
							+ Formatting.formatNumber(similarities[3]));
					out = new BufferedWriter(new FileWriter(
							"out/optimal_reading_strategies.csv", true));
					out.write(Formatting.formatNumber(threshold1) + ","
							+ Formatting.formatNumber(threshold2) + ","
							+ Formatting.formatNumber(similarities[0]) + ","
							+ Formatting.formatNumber(similarities[1]) + ","
							+ Formatting.formatNumber(similarities[2]) + ","
							+ Formatting.formatNumber(similarities[3]) + "\n");
					out.close();
					if ((similarities[0] +similarities[3])/2 > maxCorrelation) {
						maxThresold1 = threshold1;
						maxThresold2 = threshold2;
						maxCorrelation = (similarities[0] +similarities[3])/2;
					}
				}
			}
			out = new BufferedWriter(new FileWriter(
					"out/optimal_reading_strategies.csv", true));
			logger.info("Optimal bridging correlation of "
					+ Formatting.formatNumber(maxCorrelation)
					+ " is obtained by using min cohesion threshold = "
					+ Formatting.formatNumber(maxThresold1)
					+ " and max paraphrasing threshold = "
					+ Formatting.formatNumber(maxThresold2));
			out.write("Optimal\n" + Formatting.formatNumber(maxThresold1) + ","
					+ Formatting.formatNumber(maxThresold2) + ","
					+ Formatting.formatNumber(maxCorrelation));
			out.close();
		} catch (Exception e) {// Catch exception if any
			System.err.println("Error: " + e.getMessage());
		}
	}

	public static void main(String[] args) {
		BasicConfigurator.configure();
		OptimalThresholds oth = new OptimalThresholds();
		oth.determineOptimum();
	}
}
