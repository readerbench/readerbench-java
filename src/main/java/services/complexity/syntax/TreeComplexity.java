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
package services.complexity.syntax;

import services.complexity.ComplexityIndices;
import services.complexity.IComplexityFactors;
import utils.localization.LocalizationUtils;
import data.AbstractDocument;
import data.Sentence;

public class TreeComplexity extends IComplexityFactors {
	public static double getAverageTreeSize(AbstractDocument d) {
		int noSentences = 0, size = 0;
		for (Sentence s : d.getSentencesInDocument()) {
			if (s.getWords().size() > 0) {
				noSentences++;
				size += s.getPOSTreeSize();
			}
		}
		if (noSentences != 0)
			return ((double) size) / noSentences;
		return ComplexityIndices.IDENTITY;
	}

	public static double getAverageNoDependencies(AbstractDocument d) {
		int noSentences = 0, size = 0;
		for (Sentence s : d.getSentencesInDocument()) {
			if (s.getWords().size() > 0) {
				noSentences++;
				if (s.getDependencies() != null)
					size += s.getDependencies().typedDependencies().size();
			}
		}
		if (noSentences != 0 && size != 0)
			return ((double) size) / noSentences;
		return ComplexityIndices.IDENTITY;
	}

	public static double getAverageTreeDepth(AbstractDocument d) {
		int noSentences = 0, size = 0;
		for (Sentence s : d.getSentencesInDocument()) {
			if (s.getWords().size() > 0) {
				noSentences++;
				size += s.getPOSTreeDepth();
			}
		}
		if (noSentences != 0)
			return ((double) size) / noSentences;
		return ComplexityIndices.IDENTITY;
	}

	@Override
	public String getClassName() {
		return LocalizationUtils.getTranslation("Syntax (Parsing tree complexity)");
	}
	@Override
	public void setComplexityIndexDescription(String[] descriptions) {
		descriptions[ComplexityIndices.AVERAGE_TREE_DEPTH] = LocalizationUtils.getTranslation("Average parsing tree depth");
		descriptions[ComplexityIndices.AVERAGE_TREE_SIZE] = LocalizationUtils.getTranslation("Average parsing tree size");
		descriptions[ComplexityIndices.AVERAGE_NO_SEMANTIC_DEPENDENCIES] = LocalizationUtils.getTranslation("Average number of dependencies from the syntactic graph (EN only)");
	}
	public void setComplexityIndexAcronym(String[] acronyms) {
		acronyms[ComplexityIndices.AVERAGE_TREE_DEPTH] = this.getComplexityIndexAcronym("AVERAGE_TREE_DEPTH");
		acronyms[ComplexityIndices.AVERAGE_TREE_SIZE] = this.getComplexityIndexAcronym("AVERAGE_TREE_SIZE");
		acronyms[ComplexityIndices.AVERAGE_NO_SEMANTIC_DEPENDENCIES] = this.getComplexityIndexAcronym("AVERAGE_NO_SEMANTIC_DEPENDENCIES");
	}
	
	@Override
	public void computeComplexityFactors(AbstractDocument d) {
		d.getComplexityIndices()[ComplexityIndices.AVERAGE_TREE_DEPTH] = TreeComplexity
				.getAverageTreeDepth(d);
		d.getComplexityIndices()[ComplexityIndices.AVERAGE_TREE_SIZE] = TreeComplexity
				.getAverageTreeSize(d);
		d.getComplexityIndices()[ComplexityIndices.AVERAGE_NO_SEMANTIC_DEPENDENCIES] = TreeComplexity
				.getAverageNoDependencies(d);
	}

	@Override
	public int[] getIDs() {
		return new int[] { ComplexityIndices.AVERAGE_TREE_DEPTH,
				ComplexityIndices.AVERAGE_TREE_SIZE,
				ComplexityIndices.AVERAGE_NO_SEMANTIC_DEPENDENCIES };
	}
}
