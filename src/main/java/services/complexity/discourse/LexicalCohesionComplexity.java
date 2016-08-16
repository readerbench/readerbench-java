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
package services.complexity.discourse;

import java.util.Iterator;

import services.complexity.ComplexityIndices;
import services.complexity.IComplexityFactors;
import utils.localization.LocalizationUtils;
import data.Block;
import data.AbstractDocument;
import data.Sentence;
import data.Word;

public class LexicalCohesionComplexity extends IComplexityFactors {
	/**
	 * Cohesion between two utterances is measured as being : c = 1/distance
	 * where distance(u1, u2) = SUM<w1 in u1>(MIN<w2 in u2>(dist(w1, w2)))
	 */
	private static double getIntraSentenceCohesion(Sentence s) {
		double distSum = 0;
		for (int i = 0; i < s.getWords().size() - 1; i++) {
			double minDist = Double.MAX_VALUE;
			for (int j = i + 1; j < s.getWords().size(); j++) {
				double d = s.getWords().get(i)
						.getDistanceInChain(s.getWords().get(j));
				if (d < minDist) {
					minDist = d;
				}
			}
			if (minDist != Double.MAX_VALUE) {
				distSum += minDist;
			}
		}
		double cohesion = (s.getWords().size() > 0 ? distSum
				/ s.getWords().size() : 0);
		return cohesion;
	}

	private static double getAvgIntraSentenceCohesion(AbstractDocument d) {
		double sum = 0;
		int no = 0;
		for (Sentence s : d.getSentencesInDocument()) {
			sum += getIntraSentenceCohesion(s);
			no++;
		}
		return (no > 0) ? (sum / no) : 0;
	}

	/**
	 * Cohesion between two utterances is measured as being : c = 1/distance
	 * where distance(u1, u2) = SUM<w1 in u1>(MIN<w2 in u2>(dist(w1, w2)))
	 */
	private static double getInterSentenceCohesion(Sentence s1, Sentence s2) {
		double distSum = 0;
		for (Word word1 : s1.getWords()) {
			double minDist = Double.MAX_VALUE;
			for (Word word2 : s2.getWords()) {
				double d = word1.getDistanceInChain(word2);
				if (d < minDist) {
					minDist = d;
				}
			}
			if (minDist != Double.MAX_VALUE) {
				distSum += minDist;
			}
		}
		double cohesion = (Math.min(s1.getWords().size(), s2.getWords().size()) > 0 ? distSum
				/ Math.min(s1.getWords().size(), s2.getWords().size())
				: 0);
		return cohesion;
	}

	/**
	 * Cohesion within a block measured as the mean cohesion of adjacent
	 * sentences.
	 */
	public static double getBlockCohesionAdjacentSentences(Block b) {
		double cohesionSum = 0;
		Iterator<Sentence> it1 = b.getSentences().iterator();
		Iterator<Sentence> it2 = b.getSentences().iterator();
		// second iterator starts from second sentence
		if (it2.hasNext()) {
			it2.next();
		}
		while (it2.hasNext()) {
			Sentence s1 = it1.next();
			Sentence s2 = it2.next();
			cohesionSum += getInterSentenceCohesion(s1, s2);
		}
		double denominator = b.getSentences().size() - 1;
		double cohesion = (denominator > 0 ? cohesionSum / denominator : 0);
		return cohesion;
	}

	private static double getAvgBlockCohesionAdjacentSentences(
			AbstractDocument d) {
		double sum = 0;
		int no = 0;
		for (Block b : d.getBlocks()) {
			if (b != null) {
				sum += getBlockCohesionAdjacentSentences(b);
				no++;
			}
		}
		return (no > 0) ? (sum / no) : 0;
	}

	private static double getBlockCohesion(Block b) {
		double interCohesionSum = 0;
		double intraCohesionSum = 0;
		for (int i = 0; i < b.getSentences().size() - 1; i++) {
			for (int j = i + 1; j < b.getSentences().size(); j++) {
				interCohesionSum += getInterSentenceCohesion(b.getSentences()
						.get(i), b.getSentences().get(j));
			}
		}

		for (int i = 0; i < b.getSentences().size(); i++) {
			intraCohesionSum += getIntraSentenceCohesion(b.getSentences()
					.get(i));
		}

		// add intra with inter-cohesion between utterances
		double denominator = (b.getSentences().size() - 1)
				* b.getSentences().size() / 2;
		double cohesion = (denominator > 0 ? interCohesionSum / denominator : 0);

		denominator = b.getSentences().size();
		cohesion += (denominator > 0 ? intraCohesionSum / denominator : 0);
		return cohesion;
	}

	/**
	 * Document cohesion computed as the mean of block cohesion calculated as
	 * cohesion between adjacent sentences.
	 */
	private static double getAvgBlockCohesion(AbstractDocument d) {
		double cohesionSum = 0;
		int noBlocks = 0;
		for (Block b : d.getBlocks()) {
			if (b != null) {
				// cohesionSum += getBlockCohesionAdjacentUtterances(b);
				cohesionSum += getBlockCohesion(b);
				noBlocks++;
			}
		}
		double cohesion = (noBlocks > 0 ? cohesionSum / noBlocks : 0);
		return cohesion;
	}

	
	public String getClassName() {
		return LocalizationUtils.getTranslation("Lexical cohesion based on lexical chains");
	}

	
	public void setComplexityIndexDescription(String[] descriptions) {
		descriptions[ComplexityIndices.AVERAGE_INTRA_SENTENCE_LEXICAL_COHESION] = LocalizationUtils.getTranslation("Average intra sentence cohesion based on lexical chains");
		descriptions[ComplexityIndices.AVERAGE_LEXICAL_BLOCK_COHESION_ADJACENT_SENTENCES] = LocalizationUtils.getTranslation("Average paragrah cohesion based on lexical chains from adjacent sentences");
		descriptions[ComplexityIndices.AVERAGE_LEXICAL_BLOCK_COHESION] = LocalizationUtils.getTranslation("Average paragrah cohesion based on lexical chains");
	}
	public void setComplexityIndexAcronym(String[] acronyms) {
		acronyms[ComplexityIndices.AVERAGE_INTRA_SENTENCE_LEXICAL_COHESION] = this.getComplexityIndexAcronym("AVERAGE_INTRA_SENTENCE_LEXICAL_COHESION");
		acronyms[ComplexityIndices.AVERAGE_LEXICAL_BLOCK_COHESION_ADJACENT_SENTENCES] = this.getComplexityIndexAcronym("AVERAGE_LEXICAL_BLOCK_COHESION_ADJACENT_SENTENCES");
		acronyms[ComplexityIndices.AVERAGE_LEXICAL_BLOCK_COHESION] = this.getComplexityIndexAcronym("AVERAGE_LEXICAL_BLOCK_COHESION");
	}
	
	public void computeComplexityFactors(AbstractDocument d) {
		d.getComplexityIndices()[ComplexityIndices.AVERAGE_INTRA_SENTENCE_LEXICAL_COHESION] = LexicalCohesionComplexity
				.getAvgIntraSentenceCohesion(d);
		d.getComplexityIndices()[ComplexityIndices.AVERAGE_LEXICAL_BLOCK_COHESION_ADJACENT_SENTENCES] = LexicalCohesionComplexity
				.getAvgBlockCohesionAdjacentSentences(d);
		d.getComplexityIndices()[ComplexityIndices.AVERAGE_LEXICAL_BLOCK_COHESION] = LexicalCohesionComplexity
				.getAvgBlockCohesion(d);
	}

	
	public int[] getIDs() {
		return new int[] {
				ComplexityIndices.AVERAGE_INTRA_SENTENCE_LEXICAL_COHESION,
				ComplexityIndices.AVERAGE_LEXICAL_BLOCK_COHESION_ADJACENT_SENTENCES,
				ComplexityIndices.AVERAGE_LEXICAL_BLOCK_COHESION };
	}
}
