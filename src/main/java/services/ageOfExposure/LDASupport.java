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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.TreeSet;

import services.semanticModels.LDA.LDA;
import data.Word;
import cc.mallet.topics.ParallelTopicModel;
import cc.mallet.types.Alphabet;
import cc.mallet.types.IDSorter;
import cc.mallet.util.Maths;

public class LDASupport {

	/**
	 * Align two topics in two separate models and compute topic distance. (JSH
	 * on word distributions)
	 * 
	 * @param lda1
	 *            - first model
	 * @param topic1
	 *            - first topic
	 * @param lda2
	 *            - second model
	 * @param topic2
	 *            - second topic
	 * @return : distance between topics ([0 - 1])
	 */
	public static double topicDistance(LDA lda1, int topic1Id, LDA lda2, int topic2Id) {

		ParallelTopicModel model1 = lda1.getModel();
		ParallelTopicModel model2 = lda2.getModel();

		/* Get the bigger Alphabet */
		Alphabet a1 = model1.getAlphabet(), a2 = model2.getAlphabet(), bigA = (a1.size() > a2.size() ? a1 : a2);

		/* Create model local word id to reference(bigger) model id mapping */
		int model1IdMap[] = new int[bigA.size()];
		int model2IdMap[] = new int[bigA.size()];

		@SuppressWarnings("rawtypes")
		Iterator it = bigA.iterator();
		int id = 0;
		while (it.hasNext()) {
			String word = (String) it.next();

			// Get original id for model1
			int id1 = (a1 == bigA ? id : a1.lookupIndex(word));
			int id2 = (a2 == bigA ? id : a2.lookupIndex(word));

			// +1 trick makes 0 mean word "does not exist" in model
			model1IdMap[id1] = id + 1;
			model2IdMap[id2] = id + 1;

			id++;
		}

		/* Fill up distributions ( index = reference id ) */
		double distribution1[] = new double[bigA.size()];
		double distribution2[] = new double[bigA.size()];

		/* Iterate alphabets and fill up distributions */
		Iterator<IDSorter> wordIt;
		TreeSet<IDSorter> topicSortedWords;
		IDSorter word;
		double sum;

		/* Model 1 + Topic 1 */
		topicSortedWords = model1.getSortedWords().get(topic1Id);
		wordIt = topicSortedWords.iterator();
		sum = 0D;
		while (wordIt.hasNext()) {
			word = wordIt.next();
			int localId = word.getID();
			int globalId = model1IdMap[localId] - 1;

			if (globalId >= 0) {
				sum += word.getWeight();
				distribution1[globalId] = word.getWeight();
			}
		}
		/* Normalize dist1 */
		for (int i = 0; i < bigA.size(); i++)
			distribution1[i] /= sum;

		/* Model 2 + Topic 2 */
		topicSortedWords = model2.getSortedWords().get(topic2Id);
		wordIt = topicSortedWords.iterator();
		sum = 0D;
		while (wordIt.hasNext()) {
			word = wordIt.next();
			int localId = word.getID();
			int globalId = model2IdMap[localId] - 1;

			if (globalId >= 0) {
				sum += word.getWeight();
				distribution2[globalId] = word.getWeight();
			}
		}
		/* Normalize dist2 */
		for (int i = 0; i < bigA.size(); i++)
			distribution2[i] /= sum;

		/* Compute sine=1-cosSimilarity^2 as distribution distance */
		// double cosineSim = VectorAlgebra.cosineSimilarity(distribution1,
		// distribution2);
		// return 1D-cosineSim;
		// return Math.sqrt(1D-cosineSim*cosineSim);

		/* Compute JensenShannon Divergence distribution distance */
		return Maths.jensenShannonDivergence(distribution1, distribution2);
	}

	/**
	 * Returns a list of concepts ( concept = list of strings
	 * ).O(topic_num*numWords*Aphabet.lookup(word))
	 *
	 * @param numWords
	 *            - words/concept
	 * @return
	 */
	public static List<List<String>> getConcepts(LDA lda, int numWords) {
		List<List<String>> result = new LinkedList<List<String>>();

		ParallelTopicModel model = lda.getModel();
		// The data alphabet maps word IDs to strings
		Alphabet alphabet = model.getAlphabet();

		// Get an array of sorted sets of word ID/count pairs
		ArrayList<TreeSet<IDSorter>> topicSortedWords = model.getSortedWords();

		// Iterate topics and extract top words
		for (int topic = 0; topic < model.getNumTopics(); topic++) {
			Iterator<IDSorter> wordIterator = topicSortedWords.get(topic).iterator();

			int rank = 0;
			List<String> currentConcept = new LinkedList<String>();
			while (wordIterator.hasNext() && ++rank < numWords) {
				IDSorter idCountPair = wordIterator.next();

				currentConcept
						.add("(" + alphabet.lookupObject(idCountPair.getID()) + "," + idCountPair.getWeight() + ")");
			}
			result.add(currentConcept);
		}

		return result;
	}

	/**
	 * Computes a word weight in a topic word distribution. O(num_words)
	 *
	 * @param lda
	 *            : the input LDA model
	 * @param word
	 *            : the reference word
	 * @param topicId
	 *            : the reference topic number
	 * @return : the normalized word weight
	 **/
	public static double getWordWeight(LDA lda, Word concept, int topicId) {
		String word = concept.getLemma();
		double weightSum = 0D;
		double wordWeight = 0D;
		if (concept.getPOS() != null) {
			word += "_" + concept.getLemma();
		}

		ParallelTopicModel model = lda.getModel();

		// The data alphabet maps word IDs to strings
		Alphabet alphabet = model.getAlphabet();

		// Get the topic ( word distribution )
		TreeSet<IDSorter> topic = model.getSortedWords().get(topicId);

		// Iterate words in topic and normalize weight
		Iterator<IDSorter> wordIterator = topic.iterator();
		while (wordIterator.hasNext()) {
			IDSorter idCountPair = wordIterator.next();

			if (word.equals(alphabet.lookupObject(idCountPair.getID())))
				wordWeight = idCountPair.getWeight();

			weightSum += idCountPair.getWeight();
		}
		// TODO
		// return wordWeight / (weightSum + alphabet.size());
		return wordWeight / weightSum;
	}

	public static Map<Word, Double> getWordWeights(LDA lda, Integer topicId) {
		double weightSum = 0D;
		Map<Word, Double> wordWeights = new TreeMap<Word, Double>();

		ParallelTopicModel model = lda.getModel();

		// The data alphabet maps word IDs to strings
		Alphabet alphabet = model.getAlphabet();

		// Get the topic ( word distribution )
		TreeSet<IDSorter> topic = model.getSortedWords().get(topicId);

		// Iterate words in topic and normalize weight
		Iterator<IDSorter> wordIterator = topic.iterator();
		while (wordIterator.hasNext()) {
			IDSorter idCountPair = wordIterator.next();

			wordWeights.put(
					Word.getWordFromConcept(alphabet.lookupObject(idCountPair.getID()).toString(), lda.getLanguage()),
					idCountPair.getWeight());

			weightSum += idCountPair.getWeight();
		}
		if (weightSum != 0) {
			for (Word w : wordWeights.keySet())
				wordWeights.put(w, wordWeights.get(w) / weightSum);
		}
		return wordWeights;
	}

	public static Map<Word, Double> getWordWeights(LDA lda, Double[] weights) {
		double weightSum = 0D;
		Map<Word, Double> wordWeights = new TreeMap<Word, Double>();

		ParallelTopicModel model = lda.getModel();

		// The data alphabet maps word IDs to strings
		Alphabet alphabet = model.getAlphabet();

		// Get the topic ( word distribution )
		for (int topicId = 0; topicId < model.getNumTopics(); topicId++) {
			TreeSet<IDSorter> topic = model.getSortedWords().get(topicId);

			// Iterate words in topic and normalize weight
			Iterator<IDSorter> wordIterator = topic.iterator();
			while (wordIterator.hasNext()) {
				IDSorter idCountPair = wordIterator.next();

				Word w = Word.getWordFromConcept(alphabet.lookupObject(idCountPair.getID()).toString(),
						lda.getLanguage());
				if (!wordWeights.containsKey(w)) {
					wordWeights.put(w, 0D);
				}
				wordWeights.put(w, wordWeights.get(w) + idCountPair.getWeight() * weights[topicId]);

				weightSum += idCountPair.getWeight() * weights[topicId];
			}
		}
		if (weightSum != 0) {
			for (Entry<Word, Double> e : wordWeights.entrySet())
				wordWeights.put(e.getKey(), e.getValue() / weightSum);
		}
		return wordWeights;
	}

	/**
	 * Computes word weights in a topic word distribution. O(num_words)
	 *
	 * @param model
	 *            : the input LDA model
	 * @param topicId
	 *            : the reference topic number
	 * @return : a Map with word:weight
	 **/
	public static Map<String, Double> getWordDistribution(LDA lda, int topicId) {
		double weightSum = 0D;
		Map<String, Double> result = new HashMap<String, Double>();

		ParallelTopicModel model = lda.getModel();
		// The data alphabet maps word IDs to strings
		Alphabet alphabet = model.getAlphabet();

		// Get the topic ( word distribution )
		TreeSet<IDSorter> topic = model.getSortedWords().get(topicId);

		// Compute weight sum
		IDSorter idCountPair = null;
		Iterator<IDSorter> wordIterator = topic.iterator();
		while (wordIterator.hasNext()) {
			idCountPair = wordIterator.next();
			weightSum += idCountPair.getWeight();
		}

		// weightSum += alphabet.size();

		// Compute result ( normalize result )
		String word = null;
		double weight = 0D;
		wordIterator = topic.iterator();
		while (wordIterator.hasNext()) {
			idCountPair = wordIterator.next();

			word = alphabet.lookupObject(idCountPair.getID()).toString();
			weight = idCountPair.getWeight();

			// result.put(word, (weight+1)/weightSum);
			result.put(word, weight / weightSum);
		}

		return result;
	}
}
