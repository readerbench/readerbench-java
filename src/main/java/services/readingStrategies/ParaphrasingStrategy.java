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

import org.apache.commons.lang3.StringUtils;

import services.semanticModels.WordNet.OntologySupport;
import data.AnalysisElement;
import data.Block;
import data.Sentence;
import data.Word;
import data.discourse.SemanticCohesion;

public class ParaphrasingStrategy {
	private static final Color COLOR_PARAPHRASING = new Color(0, 100, 17);
	private static final double SIMILARITY_THRESHOLD_PARAPHRASING = 0.2d;

	private int addAssociations(Word word, AnalysisElement e, String usedColor) {
		word.getReadingStrategies()[ReadingStrategies.PARAPHRASE] = true;
		int noOccurences = StringUtils.countMatches(" " + e.getAlternateText() + " ", " " + word.getText() + " ");
		e.setAlternateText(PatternMatching.colorText(e.getAlternateText(), word.getText(), usedColor));
		// recheck just to be sure
		noOccurences += StringUtils.countMatches(" " + e.getAlternateText() + " ", " " + word.getText() + " ");
		e.setAlternateText(PatternMatching.colorText(e.getAlternateText(), word.getText(), usedColor));

		if (noOccurences > 0)
			return 1;
		return noOccurences;
	}

	public void conceptsInCommon(Block v, Sentence s) {
		// if above a minimum threshold
		// determine similarity
		SemanticCohesion sim = new SemanticCohesion(v, s);
		if (sim.getCohesion() < SIMILARITY_THRESHOLD_PARAPHRASING)
			return;

		String usedColor = Integer.toHexString(COLOR_PARAPHRASING.getRGB());
		usedColor = usedColor.substring(2, usedColor.length());

		for (Word w1 : v.getWordOccurences().keySet()) {
			boolean hasAssociations = false;
			for (Word w2 : s.getWordOccurences().keySet()) {
				// check for identical lemmas or synonyms
				if (w1.getLemma().equals(w2.getLemma()) || w1.getStem().equals(w2.getStem())
						|| OntologySupport.areSynonyms(w1, w2, v.getLanguage())) {
					hasAssociations = true;
					addAssociations(w2, s, usedColor);
				}
			}
			if (hasAssociations && !w1.getReadingStrategies()[ReadingStrategies.PARAPHRASE]) {
				addAssociations(w1, v, usedColor);
			}
		}
	}
}
