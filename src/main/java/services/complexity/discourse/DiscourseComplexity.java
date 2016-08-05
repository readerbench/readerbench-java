package services.complexity.discourse;

import services.complexity.ComplexityIndices;
import services.complexity.IComplexityFactors;
import utils.localization.LocalizationUtils;
import data.AbstractDocument;
import data.Block;
import data.Sentence;

public class DiscourseComplexity extends IComplexityFactors {
	// average value for block scores
	private static double getAvgBlockScore(AbstractDocument d) {
		int noBlocks = 0, sum = 0;
		for (Block b : d.getBlocks()) {
			if (b != null) {
				noBlocks++;
				sum += b.getOverallScore();
			}
		}
		if (noBlocks != 0)
			return ((double) sum) / noBlocks;
		return ComplexityIndices.IDENTITY;
	}

	// standard deviation for block scores
	private static double getBlockScoreStandardDeviation(AbstractDocument d) {
		double s0 = 0, s1 = 0, s2 = 0;
		for (Block b : d.getBlocks()) {
			if (b != null) {
				s0++;
				s1 += b.getOverallScore();
				s2 += Math.pow(b.getOverallScore(), 2);
			}

		}
		if (s0 != 0)
			return Math.sqrt(s0 * s2 - Math.pow(s1, 2)) / s0;
		return ComplexityIndices.IDENTITY;
	}

	// average value for sentence scores
	private static double getAvgSentenceScore(AbstractDocument d) {
		int noSentences = 0, sum = 0;
		for (Sentence s : d.getSentencesInDocument()) {
			noSentences++;
			sum += s.getOverallScore();
		}
		if (noSentences != 0)
			return ((double) sum) / noSentences;
		return ComplexityIndices.IDENTITY;
	}

	// standard deviation for sentence scores
	private static double getSentenceScoreStandardDeviation(AbstractDocument d) {
		double s0 = 0, s1 = 0, s2 = 0;
		for (Sentence s : d.getSentencesInDocument()) {
			s0++;
			s1 += s.getOverallScore();
			s2 += Math.pow(s.getOverallScore(), 2);
		}
		if (s0 != 0)
			return Math.sqrt(s0 * s2 - Math.pow(s1, 2)) / s0;
		return ComplexityIndices.IDENTITY;
	}

	public String getClassName() {
		return LocalizationUtils.getTranslation("Discourse Factors (Cohesion based scoring mechanism)");
	}

	
	public void setComplexityIndexDescription(String[] descriptions) {
		descriptions[ComplexityIndices.AVERAGE_BLOCK_SCORE] = LocalizationUtils.getTranslation("Average paragraph score");
		descriptions[ComplexityIndices.BLOCK_SCORE_STANDARD_DEVIATION] = LocalizationUtils.getTranslation("Paragraph score standard deviation");
		descriptions[ComplexityIndices.AVERAGE_SENTENCE_SCORE] = LocalizationUtils.getTranslation("Average sentence score");
		descriptions[ComplexityIndices.SENTENCE_SCORE_STANDARD_DEVIATION] = LocalizationUtils.getTranslation("Sentence score standard deviation");
	}
	public void setComplexityIndexAcronym(String[] acronyms) {
		acronyms[ComplexityIndices.AVERAGE_BLOCK_SCORE] = this.getComplexityIndexAcronym("AVERAGE_BLOCK_SCORE");
		acronyms[ComplexityIndices.BLOCK_SCORE_STANDARD_DEVIATION] = this.getComplexityIndexAcronym("BLOCK_SCORE_STANDARD_DEVIATION");
		acronyms[ComplexityIndices.AVERAGE_SENTENCE_SCORE] = this.getComplexityIndexAcronym("AVERAGE_SENTENCE_SCORE");
		acronyms[ComplexityIndices.SENTENCE_SCORE_STANDARD_DEVIATION] = this.getComplexityIndexAcronym("SENTENCE_SCORE_STANDARD_DEVIATION");
	}
	
	public void computeComplexityFactors(AbstractDocument d) {
		d.getComplexityIndices()[ComplexityIndices.AVERAGE_BLOCK_SCORE] = DiscourseComplexity
				.getAvgBlockScore(d);
		d.getComplexityIndices()[ComplexityIndices.BLOCK_SCORE_STANDARD_DEVIATION] = DiscourseComplexity
				.getBlockScoreStandardDeviation(d);
		d.getComplexityIndices()[ComplexityIndices.AVERAGE_SENTENCE_SCORE] = DiscourseComplexity
				.getAvgSentenceScore(d);
		d.getComplexityIndices()[ComplexityIndices.SENTENCE_SCORE_STANDARD_DEVIATION] = DiscourseComplexity
				.getSentenceScoreStandardDeviation(d);
	}

	
	public int[] getIDs() {
		return new int[] { ComplexityIndices.AVERAGE_BLOCK_SCORE,
				ComplexityIndices.BLOCK_SCORE_STANDARD_DEVIATION,
				ComplexityIndices.AVERAGE_SENTENCE_SCORE,
				ComplexityIndices.SENTENCE_SCORE_STANDARD_DEVIATION,

		};
	}
}