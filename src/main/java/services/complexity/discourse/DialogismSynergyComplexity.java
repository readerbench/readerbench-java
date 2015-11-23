package services.complexity.discourse;

import services.commons.VectorAlgebra;
import services.complexity.ComplexityIndices;
import services.complexity.IComplexityFactors;
import services.discourse.dialogism.DialogismMeasures;
import data.AbstractDocument;

public class DialogismSynergyComplexity implements IComplexityFactors {

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

	@Override
	public String getClassName() {
		return "Dialogism Factors (Semantic chains synergy)";
	}

	@Override
	public void setComplexityFactorNames(String[] names) {
		names[ComplexityIndices.AVERAGE_BLOCK_VOICE_CO_OCCURRENCE] = "Average paragraph voice co-occurrence (voices with more concepts than "
				+ AbstractDocument.MIN_PERCENTAGE_CONTENT_WORDS
				+ "% document content words)";
		names[ComplexityIndices.BLOCK_VOICE_CO_OCCURRENCE_STANDARD_DEVIATION] = "Standard deviation of paragraph voice co-occurrences (voices with more concepts than "
				+ AbstractDocument.MIN_PERCENTAGE_CONTENT_WORDS
				+ "% document content words)";
		names[ComplexityIndices.AVERAGE_SENTENCE_VOICE_CO_OCCURRENCE] = "Average sentence voice co-occurrence voices with more concepts than "
				+ AbstractDocument.MIN_PERCENTAGE_CONTENT_WORDS
				+ "% document content words)";
		names[ComplexityIndices.SENTENCE_VOICE_CO_OCCURRENCE_STANDARD_DEVIATION] = "Standard deviation of sentence voice co-occurrences voices with more concepts than "
				+ AbstractDocument.MIN_PERCENTAGE_CONTENT_WORDS
				+ "% document content words)";

		names[ComplexityIndices.AVERAGE_BLOCK_VOICE_CUMULATIVE_EFFECT] = "Average paragraph voice cumulative effects voices with more concepts than "
				+ AbstractDocument.MIN_PERCENTAGE_CONTENT_WORDS
				+ "% document content words)";
		names[ComplexityIndices.BLOCK_VOICE_CUMULATIVE_EFFECT_STANDARD_DEVIATION] = "Standard deviation of paragraph voice cumulative effects (voices with more concepts than "
				+ AbstractDocument.MIN_PERCENTAGE_CONTENT_WORDS
				+ "% document content words)";
		names[ComplexityIndices.AVERAGE_SENTENCE_VOICE_CUMULATIVE_EFFECT] = "Average sentence voice cumulative effects (voices with more concepts than "
				+ AbstractDocument.MIN_PERCENTAGE_CONTENT_WORDS
				+ "% document content words)";
		names[ComplexityIndices.SENTENCE_VOICE_CUMULATIVE_EFFECT_STANDARD_DEVIATION] = "Standard deviation of sentence cumulative effects (voices with more concepts than "
				+ AbstractDocument.MIN_PERCENTAGE_CONTENT_WORDS
				+ "% document content words)";

		names[ComplexityIndices.AVERAGE_BLOCK_VOICE_MUTUAL_INFORMATION] = "Average paragraph voice mutual information (voices with more concepts than "
				+ AbstractDocument.MIN_PERCENTAGE_CONTENT_WORDS
				+ "% document content words)";
		names[ComplexityIndices.BLOCK_VOICE_MUTUAL_INFORMATION_STANDARD_DEVIATION] = "Standard deviation of paragraph voice mutual information (voices with more concepts than "
				+ AbstractDocument.MIN_PERCENTAGE_CONTENT_WORDS
				+ "% document content words)";
		names[ComplexityIndices.AVERAGE_SENTENCE_VOICE_MUTUAL_INFORMATION] = "Average sentence voice mutual information (voices with more concepts than "
				+ AbstractDocument.MIN_PERCENTAGE_CONTENT_WORDS
				+ "% document content words)";
		names[ComplexityIndices.SENTENCE_VOICE_MUTUAL_INFORMATION_STANDARD_DEVIATION] = "Standard deviation of sentence voice mutual information (voices with more concepts than "
				+ AbstractDocument.MIN_PERCENTAGE_CONTENT_WORDS
				+ "% document content words)";
	}

	@Override
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

	@Override
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
