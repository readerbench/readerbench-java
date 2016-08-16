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
package services.readingStrategies;

import java.awt.Color;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import services.commons.Formatting;
import data.Block;
import data.Sentence;
import data.Word;
import data.discourse.SemanticCohesion;
import data.document.Summary;
import data.document.Metacognition;

public class BridgingStrategy {
	private static final Color COLOR_BRIDGING = new Color(221, 8, 6);
	private static double MIN_COHESION = 0.3;
	// minimum percentage of words that can be paraphrases in order to consider
	// a whole phrase a paraphrase
	private static double MAX_PARAPHRASING = 0.7;

	public boolean isParaphrase(Sentence s) {
		int noWords = 0, noParaphrase = 0;
		for (Word w : s.getWordOccurences().keySet()) {
			noWords += s.getWordOccurences().get(w);
			if (w.getReadingStrategies()[ReadingStrategies.PARAPHRASE])
				noParaphrase += s.getWordOccurences().get(w);
		}
		if (noParaphrase >= MAX_PARAPHRASING * noWords)
			return true;
		return false;
	}

	public double determineThreshold(Metacognition metacognition) {
		// measure average semantic relatedness
		int startIndex = 0;
		int endIndex = 0;
		double s0 = 0, s1 = 0, mean = 0, s2 = 0, stdev = 0;
		for (int i = 0; i < metacognition.getBlocks().size(); i++) {
			Block v = metacognition.getBlocks().get(i);
			// build list of previous blocks
			endIndex = v.getRefBlock().getIndex();
			for (int refBlockId = startIndex; refBlockId <= endIndex; refBlockId++) {
				for (Sentence s : metacognition.getReferredDoc().getBlocks().get(refBlockId).getSentences()) {
					SemanticCohesion coh = new SemanticCohesion(v, s);
					s0++;
					s1 += coh.getCohesion();
					s2 += Math.pow(coh.getCohesion(), 2);
				}
			}
			startIndex = endIndex + 1;
		}
		// determine mean + stdev values
		if (s0 != 0) {
			mean = s1 / s0;
			stdev = Math.sqrt(s0 * s2 - Math.pow(s1, 2)) / s0;
		}
		return mean + stdev;
	}

	public int containsStrategy(Summary essay, List<Sentence> sentences) {
		String usedColor = Integer.toHexString(COLOR_BRIDGING.getRGB());
		usedColor = usedColor.substring(2, usedColor.length());
		// determine number of bridged sentences from the initial document
		Map<Integer, Double> sentenceIds = new TreeMap<Integer, Double>();
		double s0 = 0, s1 = 0, mean = 0;

		for (Block e : essay.getBlocks()) {
			for (Sentence sent1 : e.getSentences()) {
				// find the closest sentence from the initial text
				double maxCohesion = Double.MIN_VALUE;
				int maxId = -1;
				for (Integer index = 0; index < sentences.size(); index++) {
					Sentence sent2 = sentences.get(index);
					SemanticCohesion coh = new SemanticCohesion(sent1, sent2);
					if (coh.getCohesion() > maxCohesion) {
						maxCohesion = coh.getCohesion();
						maxId = index;
					}
				}
				s0++;
				s1 += maxCohesion;
				if (sentenceIds.containsKey(maxId)) {
					if (sentenceIds.get(maxId) < maxCohesion)
						sentenceIds.put(maxId, maxCohesion);
				} else {
					sentenceIds.put(maxId, maxCohesion);
				}
			}
		}
		// determine mean values
		if (s0 != 0) {
			mean = s1 / s0;
		}
		int noBridgedSentences = 0;
		String bridgingSentences = "<br/>Bridged elements:<br/>";

		for (Entry<Integer, Double> entry : sentenceIds.entrySet()) {
			if (entry.getValue() >= Math.max(mean, MIN_COHESION)) {
				bridgingSentences += sentences.get(entry.getKey()).getText() + " - Cohesion: "
						+ Formatting.formatNumber(entry.getValue()) + ";<br/>";
				noBridgedSentences++;
			}
		}

		if (noBridgedSentences > 0) {
			// store bridged sentences in the alternate text field of each essay
			essay.setAlternateText(PatternMatching.underlineIntalicsText(bridgingSentences, usedColor));
		}
		return noBridgedSentences;
	}

	public int containsStrategy(Block v, List<Sentence> sentences, double threshold) {
		String usedColor = Integer.toHexString(COLOR_BRIDGING.getRGB());
		usedColor = usedColor.substring(2, usedColor.length());
		Map<Sentence, SemanticCohesion> cohesions = new TreeMap<Sentence, SemanticCohesion>();

		// measure semantic relatedness
		for (Sentence s : sentences) {
			SemanticCohesion coh = new SemanticCohesion(v, s);
			cohesions.put(s, coh);
		}

		int noSegments = 0;
		String bridgingSentences = "";

		// determine number of congruent zones
		int indexMin = -1;
		for (int index = 0; index < sentences.size(); index++) {
			if (cohesions.get(sentences.get(index)).getCohesion() >= Math.max(threshold, MIN_COHESION)
					&& !isParaphrase(sentences.get(index))) {
				indexMin = index;
				noSegments++;
				bridgingSentences = "<br/><br/>Bridged elements:<br/>- Segment " + noSegments + ": [";
				bridgingSentences += sentences.get(index).getText() + " - Cohesion: "
						+ Formatting.formatNumber(cohesions.get(sentences.get(index)).getCohesion()) + ";<br/>";
				break;
			}
		}
		if (indexMin == -1)
			return 0;
		for (int index = indexMin + 1; index < sentences.size(); index++) {
			if (cohesions.get(sentences.get(index)).getCohesion() >= Math.max(threshold, MIN_COHESION)
					&& !isParaphrase(sentences.get(index))) {
				if (index - indexMin > 1) {
					noSegments++;
					bridgingSentences = bridgingSentences.substring(0, bridgingSentences.length() - 6)
							+ "]<br/>- Segment " + noSegments + ": [";
				}
				bridgingSentences += sentences.get(index).getText() + " - Cohesion: "
						+ Formatting.formatNumber(cohesions.get(sentences.get(index)).getCohesion()) + ";<br/>";
				indexMin = index;
			}
		}
		// delete last unuseful signs
		bridgingSentences = bridgingSentences.substring(0, bridgingSentences.length() - 6) + "]";
		v.setAlternateText(v.getAlternateText() + PatternMatching.underlineIntalicsText(bridgingSentences, usedColor));
		return noSegments;
	}

	public static void setMinCohesion(double min_cohesion) {
		MIN_COHESION = min_cohesion;
	}

	public static void setMaxParaphrasing(double max_paraphrasing) {
		MAX_PARAPHRASING = max_paraphrasing;
	}
}
