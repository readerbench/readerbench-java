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
package services.complexity.lda;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.TreeMap;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;

import services.commons.ValueComparator;
import services.commons.VectorAlgebra;
import services.semanticModels.LDA.LDA;
import data.Word;
import data.Lang;

public class LDAWordComplexity {
	static Logger logger = Logger.getLogger(LDAWordComplexity.class);

	public static void wordComplexityAvgWeight(LDA lda) throws IOException {
		BufferedWriter out = new BufferedWriter(
				new OutputStreamWriter(new FileOutputStream(lda.getPath()
						+ "/avg_topic.bck"), "UTF-8"));
		TreeMap<Word, Double> concepts = new TreeMap<Word, Double>();
		ValueComparator<Word> kcvc = new ValueComparator<Word>(concepts);
		TreeMap<Word, Double> sortedConcepts = new TreeMap<Word, Double>(kcvc);
		for (Word w : lda.getWordRepresentation().keySet()) {
			concepts.put(w, VectorAlgebra.avg(lda.getWordProbDistribution(w)));
		}

		sortedConcepts.putAll(concepts);

		for (Word key : sortedConcepts.keySet()) {
			out.write(key + " / " + sortedConcepts.get(key) + "\n");
		}

		out.close();
	}

	public static void wordComplexityStdev(LDA lda) throws IOException {
		BufferedWriter out = new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream(lda.getPath() + "/stdev_topic.bck"),
				"UTF-8"));
		TreeMap<Word, Double> concepts = new TreeMap<Word, Double>();
		ValueComparator<Word> kcvc = new ValueComparator<Word>(concepts);
		TreeMap<Word, Double> sortedConcepts = new TreeMap<Word, Double>(kcvc);
		for (Word w : lda.getWordRepresentation().keySet()) {
			concepts.put(w, VectorAlgebra.stdev(lda.getWordProbDistribution(w)));
		}

		sortedConcepts.putAll(concepts);

		for (Word key : sortedConcepts.keySet()) {
			out.write(key + " / " + sortedConcepts.get(key) + "\n");
		}

		out.close();
	}

	public static void wordComplexityEntropy(LDA lda) throws IOException {
		BufferedWriter out = new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream(lda.getPath() + "/entropy_topic.bck"),
				"UTF-8"));
		TreeMap<Word, Double> concepts = new TreeMap<Word, Double>();
		ValueComparator<Word> kcvc = new ValueComparator<Word>(concepts);
		TreeMap<Word, Double> sortedConcepts = new TreeMap<Word, Double>(kcvc);
		for (Word w : lda.getWordRepresentation().keySet()) {
			concepts.put(w,
					VectorAlgebra.entropy(lda.getWordProbDistribution(w)));
		}

		sortedConcepts.putAll(concepts);

		for (Word key : sortedConcepts.keySet()) {
			out.write(key + " / " + sortedConcepts.get(key) + "\n");
		}

		out.close();
	}

	public static void main(String[] args) throws IOException {
		BasicConfigurator.configure();

		String path = "resources/config/LDA/lemonde_fr";
		Lang lang = Lang.fr;
		LDA lda = LDA.loadLDA(path, lang);

		wordComplexityAvgWeight(lda);
		wordComplexityStdev(lda);
		wordComplexityEntropy(lda);
	}
}
