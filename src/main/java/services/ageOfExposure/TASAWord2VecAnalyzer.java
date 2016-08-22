/* 
 * Copyright 2016 ReaderBench.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package services.ageOfExposure;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ExecutionException;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.deeplearning4j.models.embeddings.wordvectors.WordVectors;
import org.deeplearning4j.models.word2vec.Word2Vec;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.cpu.nativecpu.CpuNDArrayFactory;
import org.nd4j.linalg.factory.BaseNDArrayFactory;
import org.nd4j.linalg.ops.transforms.Transforms;

import data.Word;
import services.commons.Formatting;
import services.semanticModels.word2vec.Word2VecModel;

public class TASAWord2VecAnalyzer {
	private static Logger logger = Logger.getLogger(TASAWord2VecAnalyzer.class);
	private final double MIN_THRESHOLD = 0.2;
	private final double MAX_THRESHOLD = 0.7;
	private final double THRESHOLD_INCREMENT = 0.1;

	private String path;
	private Map<Integer, Word2Vec> models;
	private int noClasses;
	private HashMap<String, double[]> referenceVectors;
	private List<String> wordList;
	private Map<Word, List<Double>> AoEEvolution;

	public TASAWord2VecAnalyzer(String path) {
		this.path = path;
	}

	public void loadModels() {
		noClasses = (new File(path)).listFiles(new FileFilter() {
			public boolean accept(File pathname) {
				if (pathname.isDirectory())
					return true;
				return false;
			}
		}).length;

		/* Load LDA Models */

		models = new TreeMap<Integer, Word2Vec>();
		for (int i = 0; i < noClasses; i++) {
			String classPath = path + "/grade" + i;
			logger.info("Loading model " + classPath + "...");
			models.put(i, Word2VecModel.loadWord2Vec(classPath));
		}
	}

	@SuppressWarnings("unchecked")
	public void computeReferenceVectors() throws IOException, ClassNotFoundException {
		WordVectors matureModel = models.get(noClasses - 1);
		wordList = new ArrayList<>(matureModel.lookupTable().getVocabCache().words());
		Collections.sort(wordList);

		referenceVectors = new HashMap<>();
		AoEEvolution = new TreeMap<Word, List<Double>>();

		for (String word : wordList) {
			INDArray wordArray1 = matureModel.getWordVectorMatrix(word);
			int index = 0;
			double[] similarityArray = new double[referenceVectors.size()];
			for (String referenceWord : wordList) {
				INDArray wordArray2 = matureModel.getWordVectorMatrix(referenceWord);
				similarityArray[index] = Transforms.cosineSim(wordArray1, wordArray2);
				index++;
			}
			referenceVectors.put(word, similarityArray);
			AoEEvolution.put(new Word(word, word, word, null, null, null), new ArrayList<>());
		}

		ObjectOutputStream vectorFile = new ObjectOutputStream(new FileOutputStream(path + "reference_vectors.bin"));
		vectorFile.writeObject(referenceVectors);
		vectorFile.close();
	}

	public void performMatching() throws InterruptedException, ExecutionException, FileNotFoundException {
		BaseNDArrayFactory arrayFactory = new CpuNDArrayFactory();

		/* Match topics with mature topics */
		for (int cLevel = 0; cLevel < noClasses - 1; cLevel++) {
			Word2Vec intermediateModel = models.get(cLevel);
			PrintStream outFile = new PrintStream(new FileOutputStream(path + "/grade" + cLevel + "/similarity.csv"));
			for (String word : wordList) {
				List<Double> wordEvolution = AoEEvolution.get(word);
				INDArray wordArray1 = intermediateModel.getWordVectorMatrix(word);
				if (wordArray1 == null) {
					outFile.println(word + ",0.0");
					wordEvolution.add(0.0);
					continue;
				}

				int index = 0;
				double[] similarityArray = new double[wordList.size()];
				for (String referenceWord : wordList) {
					INDArray wordArray2 = intermediateModel.getWordVectorMatrix(referenceWord);
					if (wordArray2 != null) {
						similarityArray[index] = Transforms.cosineSim(wordArray1, wordArray2);
					} else {
						similarityArray[index] = 0;
					}
					index++;
				}

				INDArray currentDimmensionArray = arrayFactory.create(similarityArray);
				INDArray finalDimmensionArray = arrayFactory.create(referenceVectors.get(word));
				double cosine = Transforms.cosineSim(currentDimmensionArray, finalDimmensionArray);
				outFile.println(word + "," + cosine);
				wordEvolution.add(cosine);
			}
			outFile.close();
		}
	}

	public void writeResults() {
		// determine word acquisition ages
		Map<String, Double> birdAoA = TASAAnalyzer.getWordAcquisitionAge("Bird.csv");
		Map<String, Double> bristolAoA = TASAAnalyzer.getWordAcquisitionAge("Bristol.csv");
		Map<String, Double> corteseAoA = TASAAnalyzer.getWordAcquisitionAge("Cortese.csv");
		Map<String, Double> kupermanAoA = TASAAnalyzer.getWordAcquisitionAge("Kuperman.csv");
		Map<String, Double> shockAoA = TASAAnalyzer.getWordAcquisitionAge("Shock.csv");

		try {
			BufferedWriter loweStats = new BufferedWriter(
					new OutputStreamWriter(
							new FileOutputStream(new File(path + "/AoE word2vec stats full matching.csv")), "UTF-8"),
					32768);
			BufferedWriter loweValues = new BufferedWriter(
					new OutputStreamWriter(
							new FileOutputStream(new File(path + "/AoE word2vec word full matching.csv")), "UTF-8"),
					32768);
			// create header
			String content = "Word,Bird_AoA,Bristol_AoA,Cortese_AoA,Kuperman_AoA,Shock_AoA";
			loweStats.write(content);
			loweValues.write(content);
			for (int i = 0; i < noClasses - 1; i++) {
				loweStats.write(",C_1_" + (i + 1));
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

			for (Word analyzedWord : AoEEvolution.keySet()) {
				stats = AoEEvolution.get(analyzedWord);
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

		TASAWord2VecAnalyzer analyzer = new TASAWord2VecAnalyzer("resources/in/AoE 100");
		analyzer.loadModels();
		analyzer.computeReferenceVectors();
		analyzer.performMatching();
		analyzer.writeResults();
	}
}