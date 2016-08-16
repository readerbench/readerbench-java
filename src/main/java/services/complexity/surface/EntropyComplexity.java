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
package services.complexity.surface;

import java.util.HashMap;
import java.util.Map;

import services.complexity.IComplexityFactors;
import services.complexity.ComplexityIndices;
import utils.localization.LocalizationUtils;
import data.Block;
import data.AbstractDocument;
import data.Sentence;
import data.Word;

public class EntropyComplexity extends IComplexityFactors {
	public static double getStemEntropy(AbstractDocument d) {
		double entropy = 0;
		Map<String, Integer> occurences = new HashMap<String, Integer>();
		int no = 0;
		for (Block b : d.getBlocks()) {
			if (b != null) {
				for (Sentence s : b.getSentences()) {
					for (Word w : s.getAllWords()) {
						if (occurences.containsKey(w.getStem()))
							occurences.put(w.getStem(),
									occurences.get(w.getStem()) + 1);
						else
							occurences.put(w.getStem(), 1);
						no++;
					}
				}
			}
		}
		for (String w : occurences.keySet()) {
			double factor = ((double) occurences.get(w)) / no;
			entropy += -factor * Math.log(factor);
		}
		return entropy;
	}

	public static double getCharEntropy(AbstractDocument d) {
		double entropy = 0;
		Map<Character, Integer> occurences = new HashMap<Character, Integer>();
		int no = 0;
		for (Block b : d.getBlocks()) {
			if (b != null) {
				for (Sentence s : b.getSentences()) {
					for (Word w : s.getAllWords()) {
						for (int i = 0; i < w.getText().length(); i++) {
							char c = w.getText().charAt(i);
							if (occurences.containsKey(c))
								occurences.put(c, occurences.get(c) + 1);
							else
								occurences.put(c, 1);
							no++;
						}
					}
				}
			}
		}
		for (Character c : occurences.keySet()) {
			double factor = ((double) occurences.get(c)) / no;
			entropy += -factor * Math.log(factor);
		}
		return entropy;
	}

	@Override
	public String getClassName() {
		return LocalizationUtils.getTranslation("Surface Factors (Entropy)");
	}

	@Override
	public void setComplexityIndexDescription(String[] descriptions) {
		descriptions[ComplexityIndices.WORD_ENTROPY] = LocalizationUtils.getTranslation("Word entropy");
		descriptions[ComplexityIndices.CHAR_ENTROPY] = LocalizationUtils.getTranslation("Character entropy");
	}
	@Override
	public void setComplexityIndexAcronym(String[] acronyms) {
		acronyms[ComplexityIndices.WORD_ENTROPY] = this.getComplexityIndexAcronym("WORD_ENTROPY");
		acronyms[ComplexityIndices.CHAR_ENTROPY] = this.getComplexityIndexAcronym("CHAR_ENTROPY");
	}

	@Override
	public void computeComplexityFactors(AbstractDocument d) {
		d.getComplexityIndices()[ComplexityIndices.WORD_ENTROPY] = EntropyComplexity
				.getStemEntropy(d);
		d.getComplexityIndices()[ComplexityIndices.CHAR_ENTROPY] = EntropyComplexity
				.getCharEntropy(d);
	}

	@Override
	public int[] getIDs() {
		return new int[] { ComplexityIndices.WORD_ENTROPY,
				ComplexityIndices.CHAR_ENTROPY };
	}
}
