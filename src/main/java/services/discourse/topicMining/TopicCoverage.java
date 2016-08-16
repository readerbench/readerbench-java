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
package services.discourse.topicMining;

import org.apache.log4j.Logger;

import data.AnalysisElement;
import data.Word;
import data.Lang;
import services.nlp.listOfWords.MapOfWordWeights;
import services.readingStrategies.PatternMatching;

public class TopicCoverage {

	static Logger logger = Logger.getLogger(PatternMatching.class);

	public static final MapOfWordWeights WORDS_ACADEMIC_ADMINISTRATION_EN = new MapOfWordWeights(
			"resources/config/Topics/academic administration_en.txt", Lang.eng);
	public static final MapOfWordWeights WORDS_EDUCATIONAL_SCIENCES_EN = new MapOfWordWeights(
			"resources/config/Topics/educational sciences_en.txt", Lang.eng);

	// returns the coverage with the predefined topic class
	public static double coverage(MapOfWordWeights usedMap, AnalysisElement el) {
		double coverage = 0;

		if (usedMap != null && el != null) {
			int noOccurrences = 0, totalOccurrences = 0;
			for (Word w : el.getWordOccurences().keySet()) {
				totalOccurrences += el.getWordOccurences().get(w);
				if (usedMap.getWords().containsKey(w.getLemma())) {
					noOccurrences += usedMap.getWords().get(w.getLemma()) * el.getWordOccurences().get(w);
				}
			}
			if (totalOccurrences != 0) {
				coverage = ((double) noOccurrences) / totalOccurrences;
			}
		}
		return coverage;
	}
}
