package services.complexity.lexicalChains;

import services.complexity.ComplexityIndices;
import services.complexity.IComplexityFactors;
import utils.localization.LocalizationUtils;
import data.AbstractDocument;
import data.Block;
import data.lexicalChains.LexicalChain;

public class LexicalChainsComplexity extends IComplexityFactors {

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

	
	public String getClassName() {
		return LocalizationUtils.getTranslation("Discourse Factors (Lexical chains)");
	}

	public void setComplexityIndexDescription(String[] descriptions) {
		descriptions[ComplexityIndices.LEXICAL_CHAINS_AVERAGE_SPAN] = LocalizationUtils.getTranslation("Average span of lexical chains");
		descriptions[ComplexityIndices.LEXICAL_CHAINS_MAX_SPAN] = LocalizationUtils.getTranslation("Maximum span of lexical chains normalized by") + " "
				+ AbstractDocument.MIN_PERCENTAGE_CONTENT_WORDS
				+ "% " + LocalizationUtils.getTranslation("document content words");
		descriptions[ComplexityIndices.AVERAGE_NO_LEXICAL_CHAINS] = LocalizationUtils.getTranslation("Average paragraph number of lexical chains with more concepts than") + " "
				+ AbstractDocument.MIN_PERCENTAGE_CONTENT_WORDS
				+ "% " + LocalizationUtils.getTranslation("document content words");
		descriptions[ComplexityIndices.PERCENTAGE_LEXICAL_CHAINS_COVERAGE] = LocalizationUtils.getTranslation("Percentage of words that are included in lexical chains with more concepts than")+" "
				+ AbstractDocument.MIN_PERCENTAGE_CONTENT_WORDS
				+ "% " + LocalizationUtils.getTranslation("document content words");
	}
	public void setComplexityIndexAcronym(String[] acronyms) {
		acronyms[ComplexityIndices.LEXICAL_CHAINS_AVERAGE_SPAN] = this.getComplexityIndexAcronym("LEXICAL_CHAINS_AVERAGE_SPAN");
		acronyms[ComplexityIndices.LEXICAL_CHAINS_MAX_SPAN] = this.getComplexityIndexAcronym("LEXICAL_CHAINS_MAX_SPAN");
		acronyms[ComplexityIndices.AVERAGE_NO_LEXICAL_CHAINS] = this.getComplexityIndexAcronym("AVERAGE_NO_LEXICAL_CHAINS");
		acronyms[ComplexityIndices.PERCENTAGE_LEXICAL_CHAINS_COVERAGE] = this.getComplexityIndexAcronym("PERCENTAGE_LEXICAL_CHAINS_COVERAGE");
	}

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

	public int[] getIDs() {
		return new int[] { ComplexityIndices.LEXICAL_CHAINS_AVERAGE_SPAN,
				ComplexityIndices.LEXICAL_CHAINS_MAX_SPAN,
				ComplexityIndices.AVERAGE_NO_LEXICAL_CHAINS,
				ComplexityIndices.PERCENTAGE_LEXICAL_CHAINS_COVERAGE };
	}
}
