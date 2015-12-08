package services.complexity.surface;

import org.apache.commons.lang3.StringUtils;

import services.complexity.ComplexityIndices;
import services.complexity.IComplexityFactors;
import utils.localization.LocalizationUtils;
import data.AbstractDocument;
import data.Block;
import data.Sentence;

public class SurfaceStatisticsComplexity extends IComplexityFactors {
	private static double averageCommasPerBlock(AbstractDocument d) {
		int noCommas = 0;
		int noBlocks = 0;
		for (Block b : d.getBlocks()) {
			if (b != null) {
				for (Sentence s : b.getSentences()) {
					noCommas += StringUtils.countMatches(s.getText(), ",");
				}
				noBlocks++;
			}
		}
		if (noBlocks > 0)
			return (double) noCommas / noBlocks;
		return ComplexityIndices.IDENTITY;
	}

	private static double averageCommasPerSentence(AbstractDocument d) {
		int noCommas = 0;
		int noSentences = 0;
		for (Sentence s : d.getSentencesInDocument()) {
			noCommas += StringUtils.countMatches(s.getText(), ",");
			noSentences++;
		}
		if (noSentences > 0)
			return (double) noCommas / noSentences;
		return ComplexityIndices.IDENTITY;
	}

	// Average number of sentences in paragraph/block
	private static double getAverageSentencesInBlock(AbstractDocument d) {
		int noSentences = 0;
		int noBlocks = 0;
		for (Block b : d.getBlocks()) {
			if (b != null) {
				noSentences += b.getSentences().size();
				noBlocks++;
			}
		}
		if (noBlocks > 0)
			return ((double) noSentences) / noBlocks;
		return ComplexityIndices.IDENTITY;
	}

	// Standard Deviation for paragraphs / blocks
	private static double blockStandardDeviationSentence(AbstractDocument d) {
		double s0 = 0, s1 = 0, s2 = 0;
		for (Block b : d.getBlocks()) {
			if (b != null) {
				s0++;
				s1 += b.getSentences().size();
				s2 += Math.pow(b.getSentences().size(), 2);
			}
		}
		if (s0 != 0)
			return Math.sqrt(s0 * s2 - Math.pow(s1, 2)) / s0;
		return ComplexityIndices.IDENTITY;
	}

	// Average number of words in paragraph/block
	private static double getAverageWordsInBlock(AbstractDocument d) {
		int noWords = 0;
		int noBlocks = 0;
		for (Block b : d.getBlocks()) {
			if (b != null) {
				for (Sentence s : b.getSentences())
					noWords += s.getAllWords().size();
				noBlocks++;
			}
		}
		if (noBlocks > 0)
			return ((double) noWords) / noBlocks;
		return ComplexityIndices.IDENTITY;
	}

	// Average number of words in paragraph/block
	private static double getAverageUniqueWordsInBlock(AbstractDocument d) {
		int noWords = 0;
		int noBlocks = 0;
		for (Block b : d.getBlocks()) {
			if (b != null) {
				noWords += b.getWordOccurences().keySet().size();
				noBlocks++;
			}
		}
		if (noBlocks > 0)
			return ((double) noWords) / noBlocks;
		return ComplexityIndices.IDENTITY;
	}

	// Standard Deviation for paragraphs / blocks
	private static double blockStandardDeviationWord(AbstractDocument d) {
		double s0 = 0, s1 = 0, s2 = 0;
		for (Block b : d.getBlocks()) {
			if (b != null) {
				int noWords = 0;
				for (Sentence s : b.getSentences()) {
					noWords += s.getAllWords().size();
				}
				s0++;
				s1 += noWords;
				s2 += Math.pow(noWords, 2);
			}
		}
		if (s0 != 0)
			return Math.sqrt(s0 * s2 - Math.pow(s1, 2)) / s0;
		return ComplexityIndices.IDENTITY;
	}

	// Standard Deviation for paragraphs / blocks
	private static double blockStandardDeviationUniqueWord(AbstractDocument d) {
		double s0 = 0, s1 = 0, s2 = 0;
		for (Block b : d.getBlocks()) {
			if (b != null) {
				s0++;
				s1 += b.getWordOccurences().keySet().size();
				s2 += Math.pow(b.getWordOccurences().keySet().size(), 2);
			}
		}
		if (s0 != 0)
			return Math.sqrt(s0 * s2 - Math.pow(s1, 2)) / s0;
		return ComplexityIndices.IDENTITY;
	}

	// Average number of words in sentence
	private static double getAverageWordsInSentence(AbstractDocument document) {
		int noWords = 0;
		int noSentences = 0;
		for (Sentence s : document.getSentencesInDocument()) {
			noWords += s.getAllWords().size();
			noSentences++;
		}
		if (noSentences > 0)
			return ((double) noWords) / noSentences;
		return ComplexityIndices.IDENTITY;
	}

	// Average number of unique words in sentence
	private static double getAverageUniqueWordsInSentence(
			AbstractDocument document) {
		int noWords = 0;
		int noSentences = 0;
		for (Sentence s : document.getSentencesInDocument()) {
			noWords += s.getWordOccurences().keySet().size();
			noSentences++;
		}
		if (noSentences > 0)
			return ((double) noWords) / noSentences;
		return ComplexityIndices.IDENTITY;
	}

	// Standard Deviation for Sentences
	private static double sentenceStandardDeviationWord(AbstractDocument d) {
		double s0 = 0, s1 = 0, s2 = 0;
		for (Sentence s : d.getSentencesInDocument()) {
			s0++;
			s1 += s.getAllWords().size();
			s2 += Math.pow(s.getAllWords().size(), 2);
		}
		if (s0 != 0)
			return Math.sqrt(s0 * s2 - Math.pow(s1, 2)) / s0;
		return ComplexityIndices.IDENTITY;
	}

	// Standard Deviation for Sentences
	private static double sentenceStandardDeviationUniqueWord(AbstractDocument d) {
		double s0 = 0, s1 = 0, s2 = 0;
		for (Sentence s : d.getSentencesInDocument()) {
			s0++;
			s1 += s.getWordOccurences().keySet().size();
			s2 += Math.pow(s.getWordOccurences().keySet().size(), 2);
		}
		if (s0 != 0)
			return Math.sqrt(s0 * s2 - Math.pow(s1, 2)) / s0;
		return ComplexityIndices.IDENTITY;
	}

	@Override
	public String getClassName() {
		return LocalizationUtils.getTranslation("Surface Factors (Statistics)");
	}

	@Override
	public void setComplexityIndexDescription(String[] descriptions) {
		descriptions[ComplexityIndices.AVERAGE_COMMAS_PER_BLOCK] = LocalizationUtils.getTranslation("Average number of commas per paragraph");
		descriptions[ComplexityIndices.AVERAGE_COMMAS_PER_SENTENCE] = LocalizationUtils.getTranslation("Average number of commas per sentence");

		descriptions[ComplexityIndices.AVERAGE_SENTENCES_IN_BLOCK] = LocalizationUtils.getTranslation("Average number of sentences per paragraph");
		descriptions[ComplexityIndices.BLOCK_STANDARD_DEVIATION_NO_SENTENCES] = LocalizationUtils.getTranslation("Paragraph standard deviation in terms of no sentences");

		descriptions[ComplexityIndices.AVERAGE_WORDS_IN_BLOCK] = LocalizationUtils.getTranslation("Average number of words per paragraph");
		descriptions[ComplexityIndices.BLOCK_STANDARD_DEVIATION_NO_WORDS] = LocalizationUtils.getTranslation("Paragraph standard deviation in terms of no words");
		descriptions[ComplexityIndices.AVERAGE_WORDS_IN_SENTENCE] = LocalizationUtils.getTranslation("Average number of words in sentence");
		descriptions[ComplexityIndices.SENTENCE_STANDARD_DEVIATION_NO_WORDS] = LocalizationUtils.getTranslation("Sentence standard deviation in terms of no words");

		descriptions[ComplexityIndices.AVERAGE_UNIQUE_WORDS_IN_BLOCK] = LocalizationUtils.getTranslation("Average number of unique content words per paragraph");
		descriptions[ComplexityIndices.BLOCK_STANDARD_DEVIATION_NO_UNIQUE_WORDS] = LocalizationUtils.getTranslation("Paragraph standard deviation in terms of no unique content words");
		descriptions[ComplexityIndices.AVERAGE_UNIQUE_WORDS_IN_SENTENCE] = LocalizationUtils.getTranslation("Average number of unique content words in sentence");
		descriptions[ComplexityIndices.SENTENCE_STANDARD_DEVIATION_NO_UNIQUE_WORDS] = LocalizationUtils.getTranslation("Sentence standard deviation in terms of no unique content words");
	}
	public void setComplexityIndexAcronym(String[] acronyms) {
		acronyms[ComplexityIndices.AVERAGE_COMMAS_PER_BLOCK] = this.getComplexityIndexAcronym("AVERAGE_COMMAS_PER_BLOCK");
		acronyms[ComplexityIndices.AVERAGE_COMMAS_PER_SENTENCE] = this.getComplexityIndexAcronym("AVERAGE_COMMAS_PER_SENTENCE");

		acronyms[ComplexityIndices.AVERAGE_SENTENCES_IN_BLOCK] = this.getComplexityIndexAcronym("AVERAGE_SENTENCES_IN_BLOCK");
		acronyms[ComplexityIndices.BLOCK_STANDARD_DEVIATION_NO_SENTENCES] = this.getComplexityIndexAcronym("BLOCK_STANDARD_DEVIATION_NO_SENTENCES");

		acronyms[ComplexityIndices.AVERAGE_WORDS_IN_BLOCK] = this.getComplexityIndexAcronym("AVERAGE_WORDS_IN_BLOCK");
		acronyms[ComplexityIndices.BLOCK_STANDARD_DEVIATION_NO_WORDS] = this.getComplexityIndexAcronym("BLOCK_STANDARD_DEVIATION_NO_WORDS");
		acronyms[ComplexityIndices.AVERAGE_WORDS_IN_SENTENCE] = this.getComplexityIndexAcronym("AVERAGE_WORDS_IN_SENTENCE");
		acronyms[ComplexityIndices.SENTENCE_STANDARD_DEVIATION_NO_WORDS] = this.getComplexityIndexAcronym("SENTENCE_STANDARD_DEVIATION_NO_WORDS");

		acronyms[ComplexityIndices.AVERAGE_UNIQUE_WORDS_IN_BLOCK] = this.getComplexityIndexAcronym("AVERAGE_UNIQUE_WORDS_IN_BLOCK");
		acronyms[ComplexityIndices.BLOCK_STANDARD_DEVIATION_NO_UNIQUE_WORDS] = this.getComplexityIndexAcronym("BLOCK_STANDARD_DEVIATION_NO_UNIQUE_WORDS");
		acronyms[ComplexityIndices.AVERAGE_UNIQUE_WORDS_IN_SENTENCE] = this.getComplexityIndexAcronym("AVERAGE_UNIQUE_WORDS_IN_SENTENCE");
		acronyms[ComplexityIndices.SENTENCE_STANDARD_DEVIATION_NO_UNIQUE_WORDS] = this.getComplexityIndexAcronym("SENTENCE_STANDARD_DEVIATION_NO_UNIQUE_WORDS");
	}

	@Override
	public void computeComplexityFactors(AbstractDocument d) {
		d.getComplexityIndices()[ComplexityIndices.AVERAGE_COMMAS_PER_BLOCK] = SurfaceStatisticsComplexity
				.averageCommasPerBlock(d);
		d.getComplexityIndices()[ComplexityIndices.AVERAGE_COMMAS_PER_SENTENCE] = SurfaceStatisticsComplexity
				.averageCommasPerSentence(d);

		d.getComplexityIndices()[ComplexityIndices.AVERAGE_SENTENCES_IN_BLOCK] = SurfaceStatisticsComplexity
				.getAverageSentencesInBlock(d);
		d.getComplexityIndices()[ComplexityIndices.BLOCK_STANDARD_DEVIATION_NO_SENTENCES] = SurfaceStatisticsComplexity
				.blockStandardDeviationSentence(d);

		d.getComplexityIndices()[ComplexityIndices.AVERAGE_WORDS_IN_BLOCK] = SurfaceStatisticsComplexity
				.getAverageWordsInBlock(d);
		d.getComplexityIndices()[ComplexityIndices.BLOCK_STANDARD_DEVIATION_NO_WORDS] = SurfaceStatisticsComplexity
				.blockStandardDeviationWord(d);
		d.getComplexityIndices()[ComplexityIndices.AVERAGE_WORDS_IN_SENTENCE] = SurfaceStatisticsComplexity
				.getAverageWordsInSentence(d);
		d.getComplexityIndices()[ComplexityIndices.SENTENCE_STANDARD_DEVIATION_NO_WORDS] = SurfaceStatisticsComplexity
				.sentenceStandardDeviationWord(d);

		d.getComplexityIndices()[ComplexityIndices.AVERAGE_UNIQUE_WORDS_IN_BLOCK] = SurfaceStatisticsComplexity
				.getAverageUniqueWordsInBlock(d);
		d.getComplexityIndices()[ComplexityIndices.BLOCK_STANDARD_DEVIATION_NO_UNIQUE_WORDS] = SurfaceStatisticsComplexity
				.blockStandardDeviationUniqueWord(d);
		d.getComplexityIndices()[ComplexityIndices.AVERAGE_UNIQUE_WORDS_IN_SENTENCE] = SurfaceStatisticsComplexity
				.getAverageUniqueWordsInSentence(d);
		d.getComplexityIndices()[ComplexityIndices.SENTENCE_STANDARD_DEVIATION_NO_UNIQUE_WORDS] = SurfaceStatisticsComplexity
				.sentenceStandardDeviationUniqueWord(d);
	}

	@Override
	public int[] getIDs() {
		return new int[] { ComplexityIndices.AVERAGE_COMMAS_PER_BLOCK,
				ComplexityIndices.AVERAGE_COMMAS_PER_SENTENCE,
				ComplexityIndices.AVERAGE_SENTENCES_IN_BLOCK,
				ComplexityIndices.BLOCK_STANDARD_DEVIATION_NO_SENTENCES,
				ComplexityIndices.AVERAGE_WORDS_IN_BLOCK,
				ComplexityIndices.BLOCK_STANDARD_DEVIATION_NO_WORDS,
				ComplexityIndices.AVERAGE_WORDS_IN_SENTENCE,
				ComplexityIndices.SENTENCE_STANDARD_DEVIATION_NO_WORDS,
				ComplexityIndices.AVERAGE_UNIQUE_WORDS_IN_BLOCK,
				ComplexityIndices.BLOCK_STANDARD_DEVIATION_NO_UNIQUE_WORDS,
				ComplexityIndices.AVERAGE_UNIQUE_WORDS_IN_SENTENCE,
				ComplexityIndices.SENTENCE_STANDARD_DEVIATION_NO_UNIQUE_WORDS };
	}
}
