package services.complexity.surface;

import java.util.HashMap;
import java.util.Map;

import services.complexity.IComplexityFactors;
import services.complexity.ComplexityIndices;
import DAO.Block;
import DAO.AbstractDocument;
import DAO.Sentence;
import DAO.Word;

public class EntropyComplexity implements IComplexityFactors {
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
		return "Surface Factors (Entropy)";
	}

	@Override
	public void setComplexityFactorNames(String[] names) {
		names[ComplexityIndices.WORD_ENTROPY] = "Word entropy";
		names[ComplexityIndices.CHAR_ENTROPY] = "Character entropy";
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
