package services.complexity.lexicalChains;

import services.complexity.ComplexityIndices;
import services.complexity.IComplexityFactors;
import data.AbstractDocument;
import data.Block;
import data.lexicalChains.LexicalChain;

public class LexicalChainsComplexity implements IComplexityFactors {

	public static double getAvgBlockLexicalChains(AbstractDocument d) {
		int noChains = 0;
		int noBlocks = 0;
		for (LexicalChain chain : d.getLexicalChains()) {
			if (chain.getLinks().size() >= d.getMinWordCoverage()) {
				noChains++;
			}
		}

		for (Block b : d.getBlocks()) {
			if (b != null) {
				noBlocks++;
			}
		}
		if (noBlocks != 0)
			return ((double) noChains) / noBlocks;
		return ComplexityIndices.IDENTITY;
	}

	public static double getMaxSpan(AbstractDocument d) {
		int max = 0;
		for (LexicalChain chain : d.getLexicalChains()) {
			max = Math.max(max, chain.getLinks().size());
		}
		if (d.getMinWordCoverage() > 0)
			return ((double) max) / d.getMinWordCoverage();
		return ComplexityIndices.IDENTITY;
	}

	public static double getAvgSpan(AbstractDocument d) {
		int sum = 0;
		int noChains = 0;
		for (LexicalChain chain : d.getLexicalChains()) {
			sum += chain.getLinks().size();
			noChains++;
		}
		if (noChains != 0)
			return ((double) sum) / noChains;
		return ComplexityIndices.IDENTITY;
	}

	public static double getCoverage(AbstractDocument d) {
		int noWords = 0;
		int noCoveredWords = 0;
		for (LexicalChain chain : d.getLexicalChains()) {
			int no = chain.getLinks().size();
			if (no >= d.getMinWordCoverage()) {
				noCoveredWords += no;
			}
			noWords += no;
		}
		if (noWords != 0)
			return ((double) noCoveredWords) / noWords;
		return ComplexityIndices.IDENTITY;
	}

	@Override
	public String getClassName() {
		return "Discourse Factors (Lexical chains)";
	}

	@Override
	public void setComplexityFactorNames(String[] names) {
		names[ComplexityIndices.LEXICAL_CHAINS_AVERAGE_SPAN] = "Average span of lexical chains";
		names[ComplexityIndices.LEXICAL_CHAINS_MAX_SPAN] = "Maximum span of lexical chains normalized by "
				+ AbstractDocument.MIN_PERCENTAGE_CONTENT_WORDS
				+ "% document content words";
		names[ComplexityIndices.AVERAGE_NO_LEXICAL_CHAINS] = "Average paragraph number of lexical chains with more concepts than "
				+ AbstractDocument.MIN_PERCENTAGE_CONTENT_WORDS
				+ "% document content words";
		names[ComplexityIndices.PERCENTAGE_LEXICAL_CHAINS_COVERAGE] = "Percentage of words that are included in lexical chains with more concepts than "
				+ AbstractDocument.MIN_PERCENTAGE_CONTENT_WORDS
				+ "% document content words";
	}

	@Override
	public void computeComplexityFactors(AbstractDocument d) {
		d.getComplexityIndices()[ComplexityIndices.LEXICAL_CHAINS_AVERAGE_SPAN] = LexicalChainsComplexity
				.getAvgSpan(d);
		d.getComplexityIndices()[ComplexityIndices.LEXICAL_CHAINS_MAX_SPAN] = LexicalChainsComplexity
				.getMaxSpan(d);
		d.getComplexityIndices()[ComplexityIndices.AVERAGE_NO_LEXICAL_CHAINS] = LexicalChainsComplexity
				.getAvgBlockLexicalChains(d);
		d.getComplexityIndices()[ComplexityIndices.PERCENTAGE_LEXICAL_CHAINS_COVERAGE] = LexicalChainsComplexity
				.getCoverage(d);
	}

	@Override
	public int[] getIDs() {
		return new int[] { ComplexityIndices.LEXICAL_CHAINS_AVERAGE_SPAN,
				ComplexityIndices.LEXICAL_CHAINS_MAX_SPAN,
				ComplexityIndices.AVERAGE_NO_LEXICAL_CHAINS,
				ComplexityIndices.PERCENTAGE_LEXICAL_CHAINS_COVERAGE };
	}
}
