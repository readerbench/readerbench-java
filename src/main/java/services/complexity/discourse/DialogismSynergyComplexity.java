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

import services.commons.VectorAlgebra;
import services.complexity.ComplexityIndices;
import services.complexity.IComplexityFactors;
import services.discourse.dialogism.DialogismMeasures;
import utils.localization.LocalizationUtils;
import data.AbstractDocument;

public class DialogismSynergyComplexity extends IComplexityFactors {

	// Co-occurrence
	private static double getAvgBlockVoiceCoOccurrence(AbstractDocument d) {
		double[] evolution = DialogismMeasures.getCoOccurrenceBlockEvolution(d
				.getSignificantVoices());
		return VectorAlgebra.avg(evolution);
	}

	private static double getBlockVoiceCoOccurrenceStandardDeviation(
			AbstractDocument d) {
		double[] evolution = DialogismMeasures.getCoOccurrenceBlockEvolution(d
				.getSignificantVoices());
		return VectorAlgebra.stdev(evolution);
	}

	private static double getAvgSentenceVoiceCoOccurrence(AbstractDocument d) {
		double[] evolution = DialogismMeasures
				.getCoOccurrenceSentenceEvolution(d.getSignificantVoices());
		return VectorAlgebra.avg(evolution);
	}

	private static double getSentenceVoiceCoOccurrenceStandardDeviation(
			AbstractDocument d) {
		double[] evolution = DialogismMeasures
				.getCoOccurrenceSentenceEvolution(d.getSignificantVoices());
		return VectorAlgebra.stdev(evolution);
	}

	// cumulative effect
	private static double getAvgBlockVoiceCumulative(AbstractDocument d) {
		double[] evolution = DialogismMeasures
				.getCumulativeBlockMuvingAverageEvolution(d
						.getSignificantVoices());
		return VectorAlgebra.avg(evolution);
	}

	private static double getBlockVoiceCumulativeStandardDeviation(
			AbstractDocument d) {
		double[] evolution = DialogismMeasures
				.getCumulativeBlockMuvingAverageEvolution(d
						.getSignificantVoices());
		return VectorAlgebra.stdev(evolution);
	}

	private static double getAvgSentenceVoiceCumulative(AbstractDocument d) {
		double[] evolution = DialogismMeasures.getCumulativeSentenceEvolution(d
				.getSignificantVoices());
		return VectorAlgebra.avg(evolution);
	}

	private static double getSentenceVoiceCumulativeStandardDeviation(
			AbstractDocument d) {
		double[] evolution = DialogismMeasures.getCumulativeSentenceEvolution(d
				.getSignificantVoices());
		return VectorAlgebra.stdev(evolution);
	}

	// mutual information
	private static double getAvgBlockVoiceMutualInformation(AbstractDocument d) {
		double[] evolution = DialogismMeasures
				.getAverageBlockMutualInformationEvolution(d
						.getSignificantVoices());
		return VectorAlgebra.avg(evolution);
	}

	private static double getBlockVoiceMutualInformationStandardDeviation(
			AbstractDocument d) {
		double[] evolution = DialogismMeasures
				.getAverageBlockMutualInformationEvolution(d
						.getSignificantVoices());
		return VectorAlgebra.stdev(evolution);
	}

	private static double getAvgSentenceVoiceMutualInformation(
			AbstractDocument d) {
		double[] evolution = DialogismMeasures
				.getAverageSentenceMutualInformationEvolution(d
						.getSignificantVoices());
		return VectorAlgebra.avg(evolution);
	}

	private static double getSentenceVoiceMutualInformationStandardDeviation(
			AbstractDocument d) {
		double[] evolution = DialogismMeasures
				.getAverageSentenceMutualInformationEvolution(d
						.getSignificantVoices());
		return VectorAlgebra.stdev(evolution);
	}

	
	public String getClassName() {
		return LocalizationUtils.getTranslation("Dialogism Factors (Semantic chains synergy)");
	}

	
	public void setComplexityIndexDescription(String[] descriptions) {
		descriptions[ComplexityIndices.AVERAGE_BLOCK_VOICE_CO_OCCURRENCE] = LocalizationUtils.getTranslation("Average paragraph voice cooccurrence (voices with more concepts than")+" "+
				+ AbstractDocument.MIN_PERCENTAGE_CONTENT_WORDS
				+ "% " + LocalizationUtils.getTranslation("document content words") + ")";
		descriptions[ComplexityIndices.BLOCK_VOICE_CO_OCCURRENCE_STANDARD_DEVIATION] = LocalizationUtils.getTranslation("Standard deviation of paragraph voice cooccurrences (voices with more concepts than") + " " +
				+ AbstractDocument.MIN_PERCENTAGE_CONTENT_WORDS
				+ "% " + LocalizationUtils.getTranslation("document content words") + ")";
		descriptions[ComplexityIndices.AVERAGE_SENTENCE_VOICE_CO_OCCURRENCE] = LocalizationUtils.getTranslation("Average sentence voice cooccurrence voices with more concepts than") + " " + 
				+ AbstractDocument.MIN_PERCENTAGE_CONTENT_WORDS
				+ "% " + LocalizationUtils.getTranslation("document content words") + ")";
		descriptions[ComplexityIndices.SENTENCE_VOICE_CO_OCCURRENCE_STANDARD_DEVIATION] = LocalizationUtils.getTranslation("Standard deviation of sentence voice cooccurrences voices with more concepts than")+" "+
				+ AbstractDocument.MIN_PERCENTAGE_CONTENT_WORDS
				+ "% " + LocalizationUtils.getTranslation("document content words") + ")";

		descriptions[ComplexityIndices.AVERAGE_BLOCK_VOICE_CUMULATIVE_EFFECT] = LocalizationUtils.getTranslation("Average paragraph voice cumulative effects voices with more concepts than") + " " +
				+ AbstractDocument.MIN_PERCENTAGE_CONTENT_WORDS
				+ "% " + LocalizationUtils.getTranslation("document content words") + ")";
		descriptions[ComplexityIndices.BLOCK_VOICE_CUMULATIVE_EFFECT_STANDARD_DEVIATION] = LocalizationUtils.getTranslation("Standard deviation of paragraph voice cumulative effects (voices with more concepts than") + " " +
				+ AbstractDocument.MIN_PERCENTAGE_CONTENT_WORDS
				+ "% " + LocalizationUtils.getTranslation("document content words") + ")";
		descriptions[ComplexityIndices.AVERAGE_SENTENCE_VOICE_CUMULATIVE_EFFECT] = LocalizationUtils.getTranslation("Average sentence voice cumulative effects (voices with more concepts than") + " " +
				+ AbstractDocument.MIN_PERCENTAGE_CONTENT_WORDS
				+ "% " + LocalizationUtils.getTranslation("document content words") + ")";
		descriptions[ComplexityIndices.SENTENCE_VOICE_CUMULATIVE_EFFECT_STANDARD_DEVIATION] = LocalizationUtils.getTranslation("Standard deviation of sentence cumulative effects (voices with more concepts than") + " "+
				+ AbstractDocument.MIN_PERCENTAGE_CONTENT_WORDS
				+ "% " + LocalizationUtils.getTranslation("document content words") + ")";

		descriptions[ComplexityIndices.AVERAGE_BLOCK_VOICE_MUTUAL_INFORMATION] = LocalizationUtils.getTranslation("Average paragraph voice mutual information (voices with more concepts than") + " " +
				+ AbstractDocument.MIN_PERCENTAGE_CONTENT_WORDS
				+ "% " + LocalizationUtils.getTranslation("document content words") + ")";
		descriptions[ComplexityIndices.BLOCK_VOICE_MUTUAL_INFORMATION_STANDARD_DEVIATION] = LocalizationUtils.getTranslation("Standard deviation of paragraph voice mutual information (voices with more concepts than") + " " + 
				+ AbstractDocument.MIN_PERCENTAGE_CONTENT_WORDS
				+ "% " + LocalizationUtils.getTranslation("document content words") + ")";
		descriptions[ComplexityIndices.AVERAGE_SENTENCE_VOICE_MUTUAL_INFORMATION] = LocalizationUtils.getTranslation("Average sentence voice mutual information (voices with more concepts than") + " "
				+ AbstractDocument.MIN_PERCENTAGE_CONTENT_WORDS
				+ "% " + LocalizationUtils.getTranslation("document content words") + ")";
		descriptions[ComplexityIndices.SENTENCE_VOICE_MUTUAL_INFORMATION_STANDARD_DEVIATION] = LocalizationUtils.getTranslation("Standard deviation of sentence voice mutual information (voices with more concepts than")+" "+
				+ AbstractDocument.MIN_PERCENTAGE_CONTENT_WORDS
				+ "% " + LocalizationUtils.getTranslation("document content words") + ")";
	}
	public void setComplexityIndexAcronym(String[] acronyms) {
		acronyms[ComplexityIndices.AVERAGE_BLOCK_VOICE_CO_OCCURRENCE] = this.getComplexityIndexAcronym("AVERAGE_BLOCK_VOICE_CO_OCCURRENCE");
		acronyms[ComplexityIndices.BLOCK_VOICE_CO_OCCURRENCE_STANDARD_DEVIATION] = this.getComplexityIndexAcronym("BLOCK_VOICE_CO_OCCURRENCE_STANDARD_DEVIATION");
		acronyms[ComplexityIndices.AVERAGE_SENTENCE_VOICE_CO_OCCURRENCE] = this.getComplexityIndexAcronym("AVERAGE_SENTENCE_VOICE_CO_OCCURRENCE");
		acronyms[ComplexityIndices.SENTENCE_VOICE_CO_OCCURRENCE_STANDARD_DEVIATION] = this.getComplexityIndexAcronym("SENTENCE_VOICE_CO_OCCURRENCE_STANDARD_DEVIATION");

		acronyms[ComplexityIndices.AVERAGE_BLOCK_VOICE_CUMULATIVE_EFFECT] = this.getComplexityIndexAcronym("AVERAGE_BLOCK_VOICE_CUMULATIVE_EFFECT");
		acronyms[ComplexityIndices.BLOCK_VOICE_CUMULATIVE_EFFECT_STANDARD_DEVIATION] = this.getComplexityIndexAcronym("BLOCK_VOICE_CUMULATIVE_EFFECT_STANDARD_DEVIATION");
		acronyms[ComplexityIndices.AVERAGE_SENTENCE_VOICE_CUMULATIVE_EFFECT] = this.getComplexityIndexAcronym("AVERAGE_SENTENCE_VOICE_CUMULATIVE_EFFECT");
		acronyms[ComplexityIndices.SENTENCE_VOICE_CUMULATIVE_EFFECT_STANDARD_DEVIATION] = this.getComplexityIndexAcronym("SENTENCE_VOICE_CUMULATIVE_EFFECT_STANDARD_DEVIATION");

		acronyms[ComplexityIndices.AVERAGE_BLOCK_VOICE_MUTUAL_INFORMATION] = this.getComplexityIndexAcronym("AVERAGE_BLOCK_VOICE_MUTUAL_INFORMATION");
		acronyms[ComplexityIndices.BLOCK_VOICE_MUTUAL_INFORMATION_STANDARD_DEVIATION] = this.getComplexityIndexAcronym("BLOCK_VOICE_MUTUAL_INFORMATION_STANDARD_DEVIATION");
		acronyms[ComplexityIndices.AVERAGE_SENTENCE_VOICE_MUTUAL_INFORMATION] = this.getComplexityIndexAcronym("AVERAGE_SENTENCE_VOICE_MUTUAL_INFORMATION");
		acronyms[ComplexityIndices.SENTENCE_VOICE_MUTUAL_INFORMATION_STANDARD_DEVIATION] = this.getComplexityIndexAcronym("SENTENCE_VOICE_MUTUAL_INFORMATION_STANDARD_DEVIATION");
	}

	public void computeComplexityFactors(AbstractDocument d) {
		d.getComplexityIndices()[ComplexityIndices.AVERAGE_BLOCK_VOICE_CO_OCCURRENCE] = DialogismSynergyComplexity
				.getAvgBlockVoiceCoOccurrence(d);
		d.getComplexityIndices()[ComplexityIndices.BLOCK_VOICE_CO_OCCURRENCE_STANDARD_DEVIATION] = DialogismSynergyComplexity
				.getBlockVoiceCoOccurrenceStandardDeviation(d);
		d.getComplexityIndices()[ComplexityIndices.AVERAGE_SENTENCE_VOICE_CO_OCCURRENCE] = DialogismSynergyComplexity
				.getAvgSentenceVoiceCoOccurrence(d);
		d.getComplexityIndices()[ComplexityIndices.SENTENCE_VOICE_CO_OCCURRENCE_STANDARD_DEVIATION] = DialogismSynergyComplexity
				.getSentenceVoiceCoOccurrenceStandardDeviation(d);

		d.getComplexityIndices()[ComplexityIndices.AVERAGE_BLOCK_VOICE_CUMULATIVE_EFFECT] = DialogismSynergyComplexity
				.getAvgBlockVoiceCumulative(d);
		d.getComplexityIndices()[ComplexityIndices.BLOCK_VOICE_CUMULATIVE_EFFECT_STANDARD_DEVIATION] = DialogismSynergyComplexity
				.getBlockVoiceCumulativeStandardDeviation(d);
		d.getComplexityIndices()[ComplexityIndices.AVERAGE_SENTENCE_VOICE_CUMULATIVE_EFFECT] = DialogismSynergyComplexity
				.getAvgSentenceVoiceCumulative(d);
		d.getComplexityIndices()[ComplexityIndices.SENTENCE_VOICE_CUMULATIVE_EFFECT_STANDARD_DEVIATION] = DialogismSynergyComplexity
				.getSentenceVoiceCumulativeStandardDeviation(d);

		d.getComplexityIndices()[ComplexityIndices.AVERAGE_BLOCK_VOICE_MUTUAL_INFORMATION] = DialogismSynergyComplexity
				.getAvgBlockVoiceMutualInformation(d);
		d.getComplexityIndices()[ComplexityIndices.BLOCK_VOICE_MUTUAL_INFORMATION_STANDARD_DEVIATION] = DialogismSynergyComplexity
				.getBlockVoiceMutualInformationStandardDeviation(d);
		d.getComplexityIndices()[ComplexityIndices.AVERAGE_SENTENCE_VOICE_MUTUAL_INFORMATION] = DialogismSynergyComplexity
				.getAvgSentenceVoiceMutualInformation(d);
		d.getComplexityIndices()[ComplexityIndices.SENTENCE_VOICE_MUTUAL_INFORMATION_STANDARD_DEVIATION] = DialogismSynergyComplexity
				.getSentenceVoiceMutualInformationStandardDeviation(d);
	}

	public int[] getIDs() {
		return new int[] {
				ComplexityIndices.AVERAGE_BLOCK_VOICE_CO_OCCURRENCE,
				ComplexityIndices.BLOCK_VOICE_CO_OCCURRENCE_STANDARD_DEVIATION,
				ComplexityIndices.AVERAGE_SENTENCE_VOICE_CO_OCCURRENCE,
				ComplexityIndices.SENTENCE_VOICE_CO_OCCURRENCE_STANDARD_DEVIATION,

				ComplexityIndices.AVERAGE_BLOCK_VOICE_CUMULATIVE_EFFECT,
				ComplexityIndices.BLOCK_VOICE_CUMULATIVE_EFFECT_STANDARD_DEVIATION,
				ComplexityIndices.AVERAGE_SENTENCE_VOICE_CUMULATIVE_EFFECT,
				ComplexityIndices.SENTENCE_VOICE_CUMULATIVE_EFFECT_STANDARD_DEVIATION,

				ComplexityIndices.AVERAGE_BLOCK_VOICE_MUTUAL_INFORMATION,
				ComplexityIndices.BLOCK_VOICE_MUTUAL_INFORMATION_STANDARD_DEVIATION,
				ComplexityIndices.AVERAGE_SENTENCE_VOICE_MUTUAL_INFORMATION,
				ComplexityIndices.SENTENCE_VOICE_MUTUAL_INFORMATION_STANDARD_DEVIATION };
	}
}
