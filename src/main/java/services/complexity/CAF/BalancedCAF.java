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
package services.complexity.CAF;

import java.util.ArrayList;

import services.complexity.IComplexityFactors;
import services.complexity.ComplexityIndices;
import utils.localization.LocalizationUtils;
import data.AbstractDocument;

public class BalancedCAF extends IComplexityFactors {
	public static final String CPeriodTokens = "[.!?]+";

	public static double[] GetMeasurement(String text) {
		// Split into sentences
		String[] sentences = text.split(CPeriodTokens);
		// letter
		double l = 0;
		// word form
		double w = 0;
		// bigram
		double b = 0;
		// period unit
		double p = sentences.length;
		// word form types
		double t = 0;
		// unique bigrams
		double u = 0;

		ArrayList<String> allWords = new ArrayList<String>();
		ArrayList<String> bigrams = new ArrayList<String>();

		// Compute measurements per sentence
		for (String sentence : sentences) {
			String[] words = WordTokenizer.GetWords(sentence).split(" ");
			if (words.length == 0) {
				continue;
			}
			String bigram = words[0];
			l += words[0].length();
			w += words.length;
			if (!allWords.contains(words[0])) {
				t++;
			}
			allWords.add(words[0]);
			// Traverse each word in the sentence
			for (int i = 1; i < words.length; i++) {
				bigram += words[i];
				l += words[i].length();
				if (!allWords.contains(words[i])) {
					t++;
				}
				allWords.add(words[i]);
				if (!bigrams.contains(bigram)) {
					u++;
				}
				bigrams.add(bigram);
				bigram = words[i];
			}
		}
		b = bigrams.size();

		// Diversity is measured using Carroll‟s Adjusted Token Type Ratio:
		double v1 = t / Math.sqrt(2 * w);
		// Sophistication gives the answer to the question: how complex are the
		// word forms themselves?
		// It is achieved using the mean word length:
		double v2 = l / w;
		// Diversity. Token Type Ratio is used, but at a bigram level. It
		// captures syntactic diversity
		// at the smallest possible unit of two consecutive word forms The
		// measure is called unique bigram ratio:
		double v3 = u / (2 * b);
		// Sophistication. Mean period unit length measure is used and it‟s
		// intuitive justification
		// is that longer clauses are more complex than short ones:
		double v4 = w / p;

		double CR = Math.abs(v1 - 1 / Math.sqrt(2 * w)) + Math.abs(v2 - 1)
				+ Math.abs(v3 - 1 / Math.sqrt(2 * b)) + Math.abs(v4 - 1);

		double min = Math.min(Math.min(v1, v2), Math.min(v3, v4));

		double max = Math.max(Math.max(v1, v2), Math.max(v3, v4));

		double CB = CR - (max - min);

		return new double[] { v1, v2, v3, v4, CB };
	}

	
	public String getClassName() {
		return LocalizationUtils.getTranslation("Balanced CAF Factors");
	}

	public void setComplexityIndexDescription(String[] descriptions) {
		descriptions[ComplexityIndices.LEXICAL_DIVERSITY] = LocalizationUtils.getTranslation("Lexical Diversity");
		descriptions[ComplexityIndices.LEXICAL_SOPHISTICATION] = LocalizationUtils.getTranslation("Lexical Sophistication");
		descriptions[ComplexityIndices.SYNTACTIC_DIVERSITY] = LocalizationUtils.getTranslation("Syntactic Diversity");
		descriptions[ComplexityIndices.SYNTACTIC_SOPHISTICATION] = LocalizationUtils.getTranslation("Syntactic Sophistication");
		descriptions[ComplexityIndices.BALANCED_CAF] = LocalizationUtils.getTranslation("Balanced CAF");
	}
	
	public void setComplexityIndexAcronym(String[] acronyms) {
		acronyms[ComplexityIndices.LEXICAL_DIVERSITY] = this.getComplexityIndexAcronym("LEXICAL_DIVERSITY");
		acronyms[ComplexityIndices.LEXICAL_SOPHISTICATION] = this.getComplexityIndexAcronym("LEXICAL_SOPHISTICATION");
		acronyms[ComplexityIndices.SYNTACTIC_DIVERSITY] = this.getComplexityIndexAcronym("SYNTACTIC_DIVERSITY");
		acronyms[ComplexityIndices.SYNTACTIC_SOPHISTICATION] = this.getComplexityIndexAcronym("SYNTACTIC_SOPHISTICATION");
		acronyms[ComplexityIndices.BALANCED_CAF] = this.getComplexityIndexAcronym("BALANCED_CAF");
	}

	
	public void computeComplexityFactors(AbstractDocument d) {
		double[] balancedCaf = BalancedCAF.GetMeasurement(d.getProcessedText());
		d.getComplexityIndices()[ComplexityIndices.LEXICAL_DIVERSITY] = balancedCaf[0];
		d.getComplexityIndices()[ComplexityIndices.LEXICAL_SOPHISTICATION] = balancedCaf[1];
		d.getComplexityIndices()[ComplexityIndices.SYNTACTIC_DIVERSITY] = balancedCaf[2];
		d.getComplexityIndices()[ComplexityIndices.SYNTACTIC_SOPHISTICATION] = balancedCaf[3];
		d.getComplexityIndices()[ComplexityIndices.BALANCED_CAF] = balancedCaf[4];
	}

	
	public int[] getIDs() {
		return new int[] { ComplexityIndices.LEXICAL_DIVERSITY,
				ComplexityIndices.LEXICAL_SOPHISTICATION,
				ComplexityIndices.SYNTACTIC_DIVERSITY,
				ComplexityIndices.SYNTACTIC_SOPHISTICATION,
				ComplexityIndices.BALANCED_CAF };
	}
}
