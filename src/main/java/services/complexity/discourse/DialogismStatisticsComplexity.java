package services.complexity.discourse;

import services.complexity.ComplexityIndices;
import services.complexity.IComplexityFactors;
import data.AbstractDocument;
import data.Block;
import data.discourse.SemanticChain;

public class DialogismStatisticsComplexity implements IComplexityFactors {
	public static double getAvgNoVoices(AbstractDocument d) {
		if (d.getSignificantVoices() == null)
			return ComplexityIndices.IDENTITY;
		int noVoices = d.getSignificantVoices().size(), noBlocks = 0;

		for (Block b : d.getBlocks()) {
			if (b != null) {
				noBlocks++;
			}
		}
		if (noBlocks != 0)
			return ((double) noVoices) / noBlocks;
		return ComplexityIndices.IDENTITY;
	}

	public static double getAvgSpan(AbstractDocument d) {
		if (d.getSignificantVoices() == null)
			return ComplexityIndices.IDENTITY;
		int noVoices = 0, noWords = 0;
		for (SemanticChain chain : d.getSignificantVoices()) {
			noWords += chain.getWords().size();
			noVoices++;
		}

		if (noVoices != 0 && d.getMinWordCoverage() != 0)
			return (((double) noWords) / noVoices) / d.getMinWordCoverage();
		return ComplexityIndices.IDENTITY;
	}

	public static double getMaxSpan(AbstractDocument d) {
		if (d.getSignificantVoices() == null)
			return ComplexityIndices.IDENTITY;
		int max = 0;
		for (SemanticChain chain : d.getSignificantVoices()) {
			max = Math.max(max, chain.getWords().size());
		}
		if (d.getMinWordCoverage() != 0)
			return ((double) max) / d.getMinWordCoverage();
		return ComplexityIndices.IDENTITY;
	}

	public static double getAvgEntropyBlock(AbstractDocument d) {
		if (d.getSignificantVoices() == null)
			return ComplexityIndices.IDENTITY;
		int noVoices = 0;
		double sumEntropy = 0;
		for (SemanticChain chain : d.getSignificantVoices()) {
			sumEntropy += chain.getEntropyBlock(false);
			noVoices++;
		}

		if (noVoices != 0)
			return sumEntropy / noVoices;
		return ComplexityIndices.IDENTITY;
	}

	public static double getAvgEntropySentence(AbstractDocument d) {
		if (d.getSignificantVoices() == null)
			return ComplexityIndices.IDENTITY;
		int noVoices = 0;
		double sumEntropy = 0;
		for (SemanticChain chain : d.getSignificantVoices()) {
			sumEntropy += chain.getEntropySentence();
			noVoices++;
		}

		if (noVoices != 0)
			return sumEntropy / noVoices;
		return ComplexityIndices.IDENTITY;
	}

	public static double[] getAvgVoiceDistribution(AbstractDocument d) {
		if (d.getSignificantVoices() == null)
			return new double[] { ComplexityIndices.IDENTITY, ComplexityIndices.IDENTITY, ComplexityIndices.IDENTITY,
					ComplexityIndices.IDENTITY };
		int noVoices = 0;
		double sumValue[] = new double[4];
		for (SemanticChain chain : d.getSignificantVoices()) {
			sumValue[0] += chain.getAvgBlock();
			sumValue[1] += chain.getStdevBlock();
			sumValue[2] += chain.getAvgSentence(false);
			sumValue[3] += chain.getStdevSentence(false);
			noVoices++;
		}

		if (noVoices != 0) {
			for (int i = 0; i < sumValue.length; i++)
				sumValue[i] /= noVoices;
			return sumValue;
		}
		return new double[] { ComplexityIndices.IDENTITY, ComplexityIndices.IDENTITY, ComplexityIndices.IDENTITY,
				ComplexityIndices.IDENTITY };
	}

	public static double[] getAvgVoiceReccurrence(AbstractDocument d) {
		if (d.getSignificantVoices() == null)
			return new double[] { ComplexityIndices.IDENTITY, ComplexityIndices.IDENTITY, ComplexityIndices.IDENTITY,
					ComplexityIndices.IDENTITY };
		int noVoices = 0;
		double sumValue[] = new double[4];
		for (SemanticChain chain : d.getSignificantVoices()) {
			sumValue[0] += chain.getAvgRecurrenceBlock();
			sumValue[1] += chain.getStdevRecurrenceBlock();
			sumValue[2] += chain.getAvgRecurrenceSentence();
			sumValue[3] += chain.getStdevRecurrenceSentence();
			noVoices++;
		}

		if (noVoices != 0) {
			for (int i = 0; i < sumValue.length; i++)
				sumValue[i] /= noVoices;
			return sumValue;
		}
		return new double[] { ComplexityIndices.IDENTITY, ComplexityIndices.IDENTITY, ComplexityIndices.IDENTITY,
				ComplexityIndices.IDENTITY };
	}

	@Override
	public String getClassName() {
		return "Dialogism Factors (Semantic chains statistics)";
	}

	@Override
	public void setComplexityFactorNames(String[] names) {
		names[ComplexityIndices.AVERAGE_NO_VOICES] = "Average number of voices per paragraph with more concepts than "
				+ AbstractDocument.MIN_PERCENTAGE_CONTENT_WORDS + "% document content words";
		names[ComplexityIndices.VOICES_AVERAGE_SPAN] = "Average number of concepts per voice normalized by "
				+ AbstractDocument.MIN_PERCENTAGE_CONTENT_WORDS + "% document content words";
		names[ComplexityIndices.VOICES_MAX_SPAN] = "Max number of concepts per voice normalized by "
				+ AbstractDocument.MIN_PERCENTAGE_CONTENT_WORDS + "% document content words";

		names[ComplexityIndices.AVERAGE_VOICE_BLOCK_ENTROPY] = "Average paragraph entropy of voices with more concepts than "
				+ AbstractDocument.MIN_PERCENTAGE_CONTENT_WORDS + "% document content words";
		names[ComplexityIndices.AVERAGE_VOICE_SENTENCE_ENTROPY] = "Average sentence entropy of voices with more concepts than "
				+ AbstractDocument.MIN_PERCENTAGE_CONTENT_WORDS + "% document content words";

		names[ComplexityIndices.AVERAGE_VOICE_BLOCK_DISTRIBUTION] = "Average distribution per paragraph of voices with more concepts than "
				+ AbstractDocument.MIN_PERCENTAGE_CONTENT_WORDS + "% document content words";
		names[ComplexityIndices.VOICE_BLOCK_DISTRIBUTION_STANDARD_DEVIATION] = "Standard deviation of distributions per paragraph of voices with more concepts than "
				+ AbstractDocument.MIN_PERCENTAGE_CONTENT_WORDS + "% document content words";
		names[ComplexityIndices.AVERAGE_VOICE_SENTENCE_DISTRIBUTION] = "Average distribution per sentence of voices with more concepts than "
				+ AbstractDocument.MIN_PERCENTAGE_CONTENT_WORDS + "% document content words";
		names[ComplexityIndices.VOICE_SENTENCE_DISTRIBUTION_STANDARD_DEVIATION] = "Standard deviation of distributions per sentence of voices with more concepts than "
				+ AbstractDocument.MIN_PERCENTAGE_CONTENT_WORDS + "% document content words";

		names[ComplexityIndices.AVERAGE_VOICE_RECURRENCE_BLOCK] = "Average reccurrence per paragraph of voices with more concepts than "
				+ AbstractDocument.MIN_PERCENTAGE_CONTENT_WORDS + "% document content words";
		names[ComplexityIndices.VOICE_RECURRENCE_BLOCK_DISTRIBUTION_STANDARD_DEVIATION] = "Standard deviation of reccurence per paragraph of voices with more concepts than "
				+ AbstractDocument.MIN_PERCENTAGE_CONTENT_WORDS + "% document content words";
		names[ComplexityIndices.AVERAGE_VOICE_RECURRENCE_SENTENCE] = "Average reccurrence per sentence of voices with more concepts than "
				+ AbstractDocument.MIN_PERCENTAGE_CONTENT_WORDS + "% document content words";
		names[ComplexityIndices.VOICE_RECURRENCE_SENTENCE_STANDARD_DEVIATION] = "Standard deviation of reccurence per sentence of voices with more concepts than "
				+ AbstractDocument.MIN_PERCENTAGE_CONTENT_WORDS + "% document content words";
	}

	@Override
	public void computeComplexityFactors(AbstractDocument d) {
		d.getComplexityIndices()[ComplexityIndices.AVERAGE_NO_VOICES] = DialogismStatisticsComplexity.getAvgNoVoices(d);
		d.getComplexityIndices()[ComplexityIndices.VOICES_AVERAGE_SPAN] = DialogismStatisticsComplexity.getAvgSpan(d);
		d.getComplexityIndices()[ComplexityIndices.VOICES_MAX_SPAN] = DialogismStatisticsComplexity.getMaxSpan(d);

		d.getComplexityIndices()[ComplexityIndices.AVERAGE_VOICE_BLOCK_ENTROPY] = DialogismStatisticsComplexity
				.getAvgEntropyBlock(d);
		d.getComplexityIndices()[ComplexityIndices.AVERAGE_VOICE_SENTENCE_ENTROPY] = DialogismStatisticsComplexity
				.getAvgEntropySentence(d);

		d.getComplexityIndices()[ComplexityIndices.AVERAGE_VOICE_BLOCK_DISTRIBUTION] = DialogismStatisticsComplexity
				.getAvgVoiceDistribution(d)[0];
		d.getComplexityIndices()[ComplexityIndices.VOICE_BLOCK_DISTRIBUTION_STANDARD_DEVIATION] = DialogismStatisticsComplexity
				.getAvgVoiceDistribution(d)[1];
		d.getComplexityIndices()[ComplexityIndices.AVERAGE_VOICE_SENTENCE_DISTRIBUTION] = DialogismStatisticsComplexity
				.getAvgVoiceDistribution(d)[2];
		d.getComplexityIndices()[ComplexityIndices.VOICE_SENTENCE_DISTRIBUTION_STANDARD_DEVIATION] = DialogismStatisticsComplexity
				.getAvgVoiceDistribution(d)[3];

		d.getComplexityIndices()[ComplexityIndices.AVERAGE_VOICE_RECURRENCE_BLOCK] = DialogismStatisticsComplexity
				.getAvgVoiceReccurrence(d)[0];
		d.getComplexityIndices()[ComplexityIndices.VOICE_RECURRENCE_BLOCK_DISTRIBUTION_STANDARD_DEVIATION] = DialogismStatisticsComplexity
				.getAvgVoiceReccurrence(d)[1];
		d.getComplexityIndices()[ComplexityIndices.AVERAGE_VOICE_RECURRENCE_SENTENCE] = DialogismStatisticsComplexity
				.getAvgVoiceReccurrence(d)[2];
		d.getComplexityIndices()[ComplexityIndices.VOICE_RECURRENCE_SENTENCE_STANDARD_DEVIATION] = DialogismStatisticsComplexity
				.getAvgVoiceReccurrence(d)[3];
	}

	@Override
	public int[] getIDs() {
		return new int[] { ComplexityIndices.AVERAGE_NO_VOICES, ComplexityIndices.VOICES_AVERAGE_SPAN,
				ComplexityIndices.VOICES_MAX_SPAN,

				ComplexityIndices.AVERAGE_VOICE_BLOCK_ENTROPY, ComplexityIndices.AVERAGE_VOICE_SENTENCE_ENTROPY,

				ComplexityIndices.AVERAGE_VOICE_BLOCK_DISTRIBUTION,
				ComplexityIndices.VOICE_BLOCK_DISTRIBUTION_STANDARD_DEVIATION,
				ComplexityIndices.AVERAGE_VOICE_SENTENCE_DISTRIBUTION,
				ComplexityIndices.VOICE_SENTENCE_DISTRIBUTION_STANDARD_DEVIATION,

				ComplexityIndices.AVERAGE_VOICE_RECURRENCE_BLOCK,
				ComplexityIndices.VOICE_RECURRENCE_BLOCK_DISTRIBUTION_STANDARD_DEVIATION,
				ComplexityIndices.AVERAGE_VOICE_RECURRENCE_SENTENCE,
				ComplexityIndices.VOICE_RECURRENCE_SENTENCE_STANDARD_DEVIATION };
	}
}
