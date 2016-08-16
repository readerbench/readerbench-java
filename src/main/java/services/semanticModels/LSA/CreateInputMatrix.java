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
package services.semanticModels.LSA;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.StringTokenizer;
import java.util.TreeMap;

import org.apache.commons.collections4.bidimap.DualTreeBidiMap;
import org.apache.log4j.Logger;
import org.apache.mahout.math.Matrix;
import org.apache.mahout.math.MatrixWritable;
import org.apache.mahout.math.RandomAccessSparseVector;
import org.apache.mahout.math.SparseMatrix;
import org.apache.mahout.math.Vector;
import org.apache.mahout.math.Vector.Element;

import data.Lang;
import data.Word;
import services.semanticModels.PreProcessing;

public class CreateInputMatrix extends LSA {

	private static Logger logger = Logger.getLogger(CreateInputMatrix.class);
	private int noWords;
	private int noDocuments;

	public void parseCorpus(String path, String inputFileName, String outputFileName, Lang lang)
			throws FileNotFoundException, IOException {
		logger.info("Parsing input file...");
		setWords(new DualTreeBidiMap<Word, Integer>());
		setMapIdf(new TreeMap<Word, Double>());
		noDocuments = 0;
		noWords = 0;

		// determine number of documents and create dictionary
		FileInputStream inputFile = new FileInputStream(path + "/" + inputFileName);
		InputStreamReader ir = new InputStreamReader(inputFile, "UTF-8");
		BufferedReader in = new BufferedReader(ir);
		String line = "";

		Map<Word, Double> tempWords = new TreeMap<Word, Double>();

		while ((line = in.readLine()) != null) {
			if (line.length() > LOWER_BOUND) {
				StringTokenizer st = new StringTokenizer(line, " .");
				while (st.hasMoreTokens()) {
					Word w = Word.getWordFromConcept(st.nextToken(), lang);

					// if word association, use temporary structure
					// update correspondingly the heap of document
					if (!tempWords.containsKey(w)) {
						tempWords.put(w, 0d);
					}
					tempWords.put(w, tempWords.get(w) + 1);
				}
				noDocuments++;
			}
		}
		in.close();

		// disregard words with low occurrences
		List<Word> wordsToRemove = new ArrayList<Word>();
		for (Entry<Word, Double> entry : tempWords.entrySet()) {
			if (entry.getValue() < PreProcessing.MIN_NO_OCCURRENCES) {
				wordsToRemove.add(entry.getKey());
			}
		}

		for (Word w : wordsToRemove) {
			tempWords.remove(w);
		}

		for (Entry<Word, Double> entry : tempWords.entrySet()) {
			getWords().put(entry.getKey(), noWords);
			getMapIdf().put(entry.getKey(), entry.getValue());
			noWords++;
		}

		// update IDfs as |D|/|Dw|
		logger.info("Updating IDfs");
		for (Word concept : getMapIdf().keySet()) {
			getMapIdf().put(concept, ((double) (noDocuments)) / (getMapIdf().get(concept) + 1));
		}

		// read the corpus
		inputFile = new FileInputStream(path + "/" + inputFileName);
		ir = new InputStreamReader(inputFile, "UTF-8");
		in = new BufferedReader(ir);
		int crtDoc = 0;

		// double[][] termDoc = new double[noWords][];
		Map<Integer, Vector> termDocVectors = new TreeMap<Integer, Vector>();
		// prepare individual word vectors
		for (int i = 0; i < noWords; i++) {
			// termDoc[i] = new double[noDocuments];
			termDocVectors.put(i, new RandomAccessSparseVector(noDocuments));
		}

		logger.info("Building term-doc matrix ...");
		while ((line = in.readLine()) != null) {
			if (line.length() > LOWER_BOUND) {
				StringTokenizer st = new StringTokenizer(line, " .");

				Map<Word, Integer> wordOccurrences = new TreeMap<Word, Integer>();
				while (st.hasMoreTokens()) {
					Word w = Word.getWordFromConcept(st.nextToken(), lang);
					// only for relevant words and associations
					if (getWords().containsKey(w)) {
						if (wordOccurrences.containsKey(w)) {
							wordOccurrences.put(w, wordOccurrences.get(w) + 1);
						} else {
							wordOccurrences.put(w, 1);
						}
					}
				}
				for (Entry<Word, Integer> entry : wordOccurrences.entrySet()) {
					Integer wordIndex = getWords().get(entry.getKey());

					termDocVectors.get(wordIndex).set(crtDoc, entry.getValue());
				}
				crtDoc++;
			}
		}
		in.close();

		// normalize via entropy
		logger.info("Normalizaling term-doc matrix by applying log-entropy ...");
		for (Entry<Integer, Vector> entry : termDocVectors.entrySet()) {
			double gf = 0;
			Vector v = entry.getValue();

			for (Element el : v.nonZeroes()) {
				gf += el.get();
			}

			double entropy = 0;
			for (Element el : v.nonZeroes()) {
				double p = el.get() / gf;
				entropy += p * Math.log(p);
			}
			entropy = 1 + (entropy / Math.log(noDocuments));

			// update termDoc matrix to be written
			for (Element el : v.nonZeroes()) {
				double log = Math.log(1 + el.get());
				v.set(el.index(), log * entropy);
			}
		}
		Matrix m = new SparseMatrix(noWords, noDocuments, termDocVectors);
		MatrixWritable.writeMatrix(new DataOutputStream(new FileOutputStream(path + "/" + outputFileName)), m);

		logger.info("Vector space dimensions:\n" + noWords + " words with " + noDocuments + " documents");

		saveIdf(path);
		saveWordList(path);

		logger.info("Finished all computations");
	}

	private void saveWordList(String path) throws FileNotFoundException, IOException {
		// loads IDf matrix from file
		BufferedWriter out = new BufferedWriter(
				new OutputStreamWriter(new FileOutputStream(path + "/wordlist.txt"), "UTF-8"));
		for (Word w : getWords().keySet()) {
			out.write(w.getExtendedLemma() + " " + getWords().get(w) + "\n");
		}
		out.close();
	}

	private void saveIdf(String path) throws FileNotFoundException, IOException {
		// loads IDf matrix from file
		BufferedWriter out = new BufferedWriter(
				new OutputStreamWriter(new FileOutputStream(path + "/idf.txt"), "UTF-8"));
		for (Word w : getMapIdf().keySet()) {
			out.write(w.getExtendedLemma() + " " + getMapIdf().get(w) + "\n");
		}
		out.close();
	}

	public int getNoWords() {
		return noWords;
	}

	public void setNoWords(int noWords) {
		this.noWords = noWords;
	}

	public int getNoDocuments() {
		return noDocuments;
	}

	public void setNoDocuments(int noDocuments) {
		this.noDocuments = noDocuments;
	}
}
