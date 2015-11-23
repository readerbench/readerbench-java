package services.complexity.surface;

import services.complexity.ComplexityIndices;
import services.complexity.IComplexityFactors;
import data.AbstractDocument;
import data.Block;
import data.Sentence;
import data.Word;

public class LengthComplexity implements IComplexityFactors {
	// Average number of characters in a block
	private static double getAverageBlockLength(AbstractDocument d) {
		int size = 0;
		int noBlocks = 0;
		for (Block b : d.getBlocks()) {
			if (b != null) {
				for (Sentence s : b.getSentences()) {
					for (Word w : s.getAllWords()) {
						size += w.getText().length();
					}
				}
				noBlocks++;
			}
		}
		if (noBlocks != 0)
			return ((double) size) / noBlocks;
		return 0;
	}

	// Average number of characters in a sentence
	private static double getAverageSentenceLength(AbstractDocument d) {
		int no = 0;
		int size = 0;
		for (Sentence s : d.getSentencesInDocument()) {
			for (Word w : s.getAllWords()) {
				size += w.getText().length();
			}
			no++;
		}
		if (no != 0)
			return ((double) size) / no;
		return 0;
	}

	// Average number of characters per word
	private static double getAverageWordLength(AbstractDocument d) {
		int noWords = 0, size = 0;
		for (Sentence s : d.getSentencesInDocument()) {
			for (Word w : s.getAllWords()) {
				size += w.getText().length();
				noWords++;
			}
		}
		if (noWords != 0)
			return ((double) size) / noWords;
		return 0;
	}

	// Standard Deviation for words (letters)
	public static double wordLettersStandardDeviation(AbstractDocument d) {
		double s0 = 0, s1 = 0, s2 = 0;
		for (Sentence s : d.getSentencesInDocument()) {
			for (Word word : s.getAllWords()) {
				s0++;
				s1 += word.getText().length();
				s2 += Math.pow(word.getText().length(), 2);
			}
		}
		if (s0 != 0)
			return Math.sqrt(s0 * s2 - Math.pow(s1, 2)) / s0;
		return ComplexityIndices.IDENTITY;
	}

	@Override
	public String getClassName() {
		return "Surface Factors (Average lengths in characters)";
	}

	@Override
	public void setComplexityFactorNames(String[] names) {
		names[ComplexityIndices.AVERAGE_BLOCK_LENGTH] = "Average paragraph length (characters)";
		names[ComplexityIndices.AVERAGE_SENTENCE_LENGTH] = "Average sentence length (characters)";
		names[ComplexityIndices.AVERAGE_WORD_LENGTH] = "Average word length (characters)";
		names[ComplexityIndices.WORD_LETTERS_STANDARD_DEVIATION] = "Standard deviation for words (characters)";
	}

	@Override
	public void computeComplexityFactors(AbstractDocument d) {
		d.getComplexityIndices()[ComplexityIndices.AVERAGE_BLOCK_LENGTH] = LengthComplexity
				.getAverageBlockLength(d);
		d.getComplexityIndices()[ComplexityIndices.AVERAGE_SENTENCE_LENGTH] = LengthComplexity
				.getAverageSentenceLength(d);
		d.getComplexityIndices()[ComplexityIndices.AVERAGE_WORD_LENGTH] = LengthComplexity
				.getAverageWordLength(d);
		d.getComplexityIndices()[ComplexityIndices.WORD_LETTERS_STANDARD_DEVIATION] = LengthComplexity
				.wordLettersStandardDeviation(d);
	}

	@Override
	public int[] getIDs() {
		return new int[] { ComplexityIndices.AVERAGE_BLOCK_LENGTH,
				ComplexityIndices.AVERAGE_SENTENCE_LENGTH,
				ComplexityIndices.AVERAGE_WORD_LENGTH,
				ComplexityIndices.WORD_LETTERS_STANDARD_DEVIATION };
	}
}
