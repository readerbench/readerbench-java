package services.complexity.syntax;

import java.util.Set;
import java.util.TreeSet;

import services.complexity.ComplexityIndices;
import services.complexity.IComplexityFactors;
import utils.localization.LocalizationUtils;
import data.AbstractDocument;
import data.Block;
import data.Sentence;
import data.Word;

public class POSComplexity extends IComplexityFactors {
	public static double getAveragePOSperSentence(AbstractDocument d, String pos) {
		// available POS: NN, PR (pronoun), VB, RB (adverb), JJ (adjective), IN
		// (preposition)
		int no = 0;
		int noSentences = 0;
		for (Block b : d.getBlocks()) {
			if (b != null) {
				for (Sentence s : b.getSentences()) {
					noSentences++;
					for (Word w : s.getAllWords()) {
						if (w.getPOS() != null && w.getPOS().contains(pos))
							no++;
					}
				}
			}
		}
		if (noSentences != 0)
			return ((double) no) / noSentences;
		return ComplexityIndices.IDENTITY;
	}

	public static double getAveragePOSperBlock(AbstractDocument d, String pos) {
		// available POS: NN, PR (pronoun), VB, RB (adverb), JJ (adjective), IN
		// (preposition)
		int no = 0;
		int noBlocks = 0;
		for (Block b : d.getBlocks()) {
			if (b != null) {
				for (Sentence s : b.getSentences()) {
					for (Word w : s.getAllWords()) {
						if (w.getPOS() != null && w.getPOS().contains(pos))
							no++;
					}
				}
				noBlocks++;
			}
		}
		if (noBlocks != 0)
			return ((double) no) / noBlocks;
		return ComplexityIndices.IDENTITY;
	}
	
	public static double getAverageUniquePOSperBlock(AbstractDocument d, String pos) {
		// available POS: NN, PR (pronoun), VB, RB (adverb), JJ (adjective), IN
		// (preposition)
		int noBlocks = 0;
		Set<String> uniqueEntity = new TreeSet<String>();
		for (Block b : d.getBlocks()) {
			if (b != null) {
				for (Sentence s : b.getSentences()) {
					for (Word w : s.getAllWords()) {
						if (w.getPOS() != null && w.getPOS().contains(pos))
							uniqueEntity.add(w.getLemma());
					}
				}
				noBlocks++;
			}
		}
		if (noBlocks != 0)
			return ((double) uniqueEntity.size()) / noBlocks;
		return ComplexityIndices.IDENTITY;
	}
	
	@Override
	public String getClassName() {
		return LocalizationUtils.getTranslation("Syntax (Part of speech statistics)");
	}

	@Override
	public void setComplexityIndexDescription(String[] descriptions) {
		descriptions[ComplexityIndices.AVERAGE_NO_NOUNS_PER_BLOCK] = LocalizationUtils.getTranslation("Average number of nouns per paragraph");
		descriptions[ComplexityIndices.AVERAGE_NO_PRONOUNS_PER_BLOCK] = LocalizationUtils.getTranslation("Average number of pronouns per paragraph");
		descriptions[ComplexityIndices.AVERAGE_NO_VERBS_PER_BLOCK] = LocalizationUtils.getTranslation("Average number of verbs per paragraph");
		descriptions[ComplexityIndices.AVERAGE_NO_ADVERBS_PER_BLOCK] = LocalizationUtils.getTranslation("Average number of adverbs per paragraph");
		descriptions[ComplexityIndices.AVERAGE_NO_ADJECTIVES_PER_BLOCK] = LocalizationUtils.getTranslation("Average number of adjectives per paragraph");
		descriptions[ComplexityIndices.AVERAGE_NO_PREPOSITIONS_PER_BLOCK] = LocalizationUtils.getTranslation("Average number of prepositions per paragraph");
		
		descriptions[ComplexityIndices.AVERAGE_NO_NOUNS_PER_SENTENCE] = LocalizationUtils.getTranslation("Average number of nouns per sentence");
		descriptions[ComplexityIndices.AVERAGE_NO_PRONOUNS_PER_SENTENCE] = LocalizationUtils.getTranslation("Average number of pronouns per sentence");
		descriptions[ComplexityIndices.AVERAGE_NO_VERBS_PER_SENTENCE] = LocalizationUtils.getTranslation("Average number of verbs per sentence");
		descriptions[ComplexityIndices.AVERAGE_NO_ADVERBS_PER_SENTENCE] = LocalizationUtils.getTranslation("Average number of adverbs per sentence");
		descriptions[ComplexityIndices.AVERAGE_NO_ADJECTIVES_PER_SENTENCE] = LocalizationUtils.getTranslation("Average number of adjectives per sentence");
		descriptions[ComplexityIndices.AVERAGE_NO_PREPOSITIONS_PER_SENTENCE] = LocalizationUtils.getTranslation("Average number of prepositions per sentence");
		
		descriptions[ComplexityIndices.AVERAGE_NO_UNIQUE_NOUNS_PER_BLOCK] = LocalizationUtils.getTranslation("Average number of unique nouns per paragraph");
		descriptions[ComplexityIndices.AVERAGE_NO_UNIQUE_PRONOUNS_PER_BLOCK] = LocalizationUtils.getTranslation("Average number of unique pronouns per paragraph");
		descriptions[ComplexityIndices.AVERAGE_NO_UNIQUE_VERBS_PER_BLOCK] = LocalizationUtils.getTranslation("Average number of unique verbs per paragraph");
		descriptions[ComplexityIndices.AVERAGE_NO_UNIQUE_ADVERBS_PER_BLOCK] = LocalizationUtils.getTranslation("Average number of unique adverbs per paragraph");
		descriptions[ComplexityIndices.AVERAGE_NO_UNIQUE_ADJECTIVES_PER_BLOCK] = LocalizationUtils.getTranslation("Average number of unique adjectives per paragraph");
		descriptions[ComplexityIndices.AVERAGE_NO_UNIQUE_PREPOSITIONS_PER_BLOCK] = LocalizationUtils.getTranslation("Average number of unique prepositions per paragraph");
	}
	public void setComplexityIndexAcronym(String[] acronyms) {
		acronyms[ComplexityIndices.AVERAGE_NO_NOUNS_PER_BLOCK] = this.getComplexityIndexAcronym("AVERAGE_NO_NOUNS_PER_BLOCK");
		acronyms[ComplexityIndices.AVERAGE_NO_PRONOUNS_PER_BLOCK] = this.getComplexityIndexAcronym("AVERAGE_NO_PRONOUNS_PER_BLOCK");
		acronyms[ComplexityIndices.AVERAGE_NO_VERBS_PER_BLOCK] = this.getComplexityIndexAcronym("AVERAGE_NO_VERBS_PER_BLOCK");
		acronyms[ComplexityIndices.AVERAGE_NO_ADVERBS_PER_BLOCK] = this.getComplexityIndexAcronym("AVERAGE_NO_ADVERBS_PER_BLOCK");
		acronyms[ComplexityIndices.AVERAGE_NO_ADJECTIVES_PER_BLOCK] = this.getComplexityIndexAcronym("AVERAGE_NO_ADJECTIVES_PER_BLOCK");
		acronyms[ComplexityIndices.AVERAGE_NO_PREPOSITIONS_PER_BLOCK] = this.getComplexityIndexAcronym("AVERAGE_NO_PREPOSITIONS_PER_BLOCK");
		
		acronyms[ComplexityIndices.AVERAGE_NO_NOUNS_PER_SENTENCE] = this.getComplexityIndexAcronym("AVERAGE_NO_NOUNS_PER_SENTENCE");
		acronyms[ComplexityIndices.AVERAGE_NO_PRONOUNS_PER_SENTENCE] = this.getComplexityIndexAcronym("AVERAGE_NO_PRONOUNS_PER_SENTENCE");
		acronyms[ComplexityIndices.AVERAGE_NO_VERBS_PER_SENTENCE] = this.getComplexityIndexAcronym("AVERAGE_NO_VERBS_PER_SENTENCE");
		acronyms[ComplexityIndices.AVERAGE_NO_ADVERBS_PER_SENTENCE] = this.getComplexityIndexAcronym("AVERAGE_NO_ADVERBS_PER_SENTENCE");
		acronyms[ComplexityIndices.AVERAGE_NO_ADJECTIVES_PER_SENTENCE] = this.getComplexityIndexAcronym("AVERAGE_NO_ADJECTIVES_PER_SENTENCE");
		acronyms[ComplexityIndices.AVERAGE_NO_PREPOSITIONS_PER_SENTENCE] = this.getComplexityIndexAcronym("AVERAGE_NO_PREPOSITIONS_PER_SENTENCE");
		
		acronyms[ComplexityIndices.AVERAGE_NO_UNIQUE_NOUNS_PER_BLOCK] = this.getComplexityIndexAcronym("AVERAGE_NO_UNIQUE_NOUNS_PER_BLOCK");
		acronyms[ComplexityIndices.AVERAGE_NO_UNIQUE_PRONOUNS_PER_BLOCK] = this.getComplexityIndexAcronym("AVERAGE_NO_UNIQUE_PRONOUNS_PER_BLOCK");
		acronyms[ComplexityIndices.AVERAGE_NO_UNIQUE_VERBS_PER_BLOCK] = this.getComplexityIndexAcronym("AVERAGE_NO_UNIQUE_VERBS_PER_BLOCK");
		acronyms[ComplexityIndices.AVERAGE_NO_UNIQUE_ADVERBS_PER_BLOCK] = this.getComplexityIndexAcronym("AVERAGE_NO_UNIQUE_ADVERBS_PER_BLOCK");
		acronyms[ComplexityIndices.AVERAGE_NO_UNIQUE_ADJECTIVES_PER_BLOCK] = this.getComplexityIndexAcronym("AVERAGE_NO_UNIQUE_ADJECTIVES_PER_BLOCK");
		acronyms[ComplexityIndices.AVERAGE_NO_UNIQUE_PREPOSITIONS_PER_BLOCK] = this.getComplexityIndexAcronym("AVERAGE_NO_UNIQUE_PREPOSITIONS_PER_BLOCK");
	}
		
	@Override
	public void computeComplexityFactors(AbstractDocument d) {
		d.getComplexityIndices()[ComplexityIndices.AVERAGE_NO_NOUNS_PER_BLOCK] = POSComplexity
				.getAveragePOSperBlock(d, "NN");
		d.getComplexityIndices()[ComplexityIndices.AVERAGE_NO_PRONOUNS_PER_BLOCK] = POSComplexity
				.getAveragePOSperBlock(d, "PR");
		d.getComplexityIndices()[ComplexityIndices.AVERAGE_NO_VERBS_PER_BLOCK] = POSComplexity
				.getAveragePOSperBlock(d, "VB");
		d.getComplexityIndices()[ComplexityIndices.AVERAGE_NO_ADVERBS_PER_BLOCK] = POSComplexity
				.getAveragePOSperBlock(d, "RB");
		d.getComplexityIndices()[ComplexityIndices.AVERAGE_NO_ADJECTIVES_PER_BLOCK] = POSComplexity
				.getAveragePOSperBlock(d, "JJ");
		d.getComplexityIndices()[ComplexityIndices.AVERAGE_NO_PREPOSITIONS_PER_BLOCK] = POSComplexity
				.getAveragePOSperBlock(d, "IN");
		
		d.getComplexityIndices()[ComplexityIndices.AVERAGE_NO_NOUNS_PER_SENTENCE] = POSComplexity
				.getAveragePOSperSentence(d, "NN");
		d.getComplexityIndices()[ComplexityIndices.AVERAGE_NO_PRONOUNS_PER_SENTENCE] = POSComplexity
				.getAveragePOSperSentence(d, "PR");
		d.getComplexityIndices()[ComplexityIndices.AVERAGE_NO_VERBS_PER_SENTENCE] = POSComplexity
				.getAveragePOSperSentence(d, "VB");
		d.getComplexityIndices()[ComplexityIndices.AVERAGE_NO_ADVERBS_PER_SENTENCE] = POSComplexity
				.getAveragePOSperSentence(d, "RB");
		d.getComplexityIndices()[ComplexityIndices.AVERAGE_NO_ADJECTIVES_PER_SENTENCE] = POSComplexity
				.getAveragePOSperSentence(d, "JJ");
		d.getComplexityIndices()[ComplexityIndices.AVERAGE_NO_PREPOSITIONS_PER_SENTENCE] = POSComplexity
				.getAveragePOSperSentence(d, "IN");
		
		d.getComplexityIndices()[ComplexityIndices.AVERAGE_NO_UNIQUE_NOUNS_PER_BLOCK] = POSComplexity
				.getAverageUniquePOSperBlock(d, "NN");
		d.getComplexityIndices()[ComplexityIndices.AVERAGE_NO_UNIQUE_PRONOUNS_PER_BLOCK] = POSComplexity
				.getAverageUniquePOSperBlock(d, "PR");
		d.getComplexityIndices()[ComplexityIndices.AVERAGE_NO_UNIQUE_VERBS_PER_BLOCK] = POSComplexity
				.getAverageUniquePOSperBlock(d, "VB");
		d.getComplexityIndices()[ComplexityIndices.AVERAGE_NO_UNIQUE_ADVERBS_PER_BLOCK] = POSComplexity
				.getAverageUniquePOSperBlock(d, "RB");
		d.getComplexityIndices()[ComplexityIndices.AVERAGE_NO_UNIQUE_ADJECTIVES_PER_BLOCK] = POSComplexity
				.getAverageUniquePOSperBlock(d, "JJ");
		d.getComplexityIndices()[ComplexityIndices.AVERAGE_NO_UNIQUE_PREPOSITIONS_PER_BLOCK] = POSComplexity
				.getAverageUniquePOSperBlock(d, "IN");
	}

	@Override
	public int[] getIDs() {
		return new int[] { ComplexityIndices.AVERAGE_NO_NOUNS_PER_BLOCK,
				ComplexityIndices.AVERAGE_NO_PRONOUNS_PER_BLOCK,
				ComplexityIndices.AVERAGE_NO_VERBS_PER_BLOCK,
				ComplexityIndices.AVERAGE_NO_ADVERBS_PER_BLOCK,
				ComplexityIndices.AVERAGE_NO_ADJECTIVES_PER_BLOCK,
				ComplexityIndices.AVERAGE_NO_PREPOSITIONS_PER_BLOCK,
				ComplexityIndices.AVERAGE_NO_NOUNS_PER_SENTENCE,
				ComplexityIndices.AVERAGE_NO_PRONOUNS_PER_SENTENCE,
				ComplexityIndices.AVERAGE_NO_VERBS_PER_SENTENCE,
				ComplexityIndices.AVERAGE_NO_ADVERBS_PER_SENTENCE,
				ComplexityIndices.AVERAGE_NO_ADJECTIVES_PER_SENTENCE,
				ComplexityIndices.AVERAGE_NO_PREPOSITIONS_PER_SENTENCE,
				ComplexityIndices.AVERAGE_NO_UNIQUE_NOUNS_PER_BLOCK,
				ComplexityIndices.AVERAGE_NO_UNIQUE_PRONOUNS_PER_BLOCK,
				ComplexityIndices.AVERAGE_NO_UNIQUE_VERBS_PER_BLOCK,
				ComplexityIndices.AVERAGE_NO_UNIQUE_ADVERBS_PER_BLOCK,
				ComplexityIndices.AVERAGE_NO_UNIQUE_ADJECTIVES_PER_BLOCK,
				ComplexityIndices.AVERAGE_NO_UNIQUE_PREPOSITIONS_PER_BLOCK,};
	}
}
