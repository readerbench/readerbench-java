package services.complexity.wordComplexity;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;

import services.complexity.ComplexityIndices;
import services.complexity.IComplexityFactors;
import services.complexity.readability.Syllable;
import services.semanticModels.WordNet.OntologySupport;
import utils.localization.LocalizationUtils;
import data.AbstractDocument;
import data.Word;
import edu.cmu.lti.jawjaw.JAWJAW;
import edu.cmu.lti.jawjaw.pobj.Lang;
import edu.cmu.lti.jawjaw.pobj.Sense;

public class WordComplexity extends IComplexityFactors {

	private static int getSyllables(Word word) {
		return Syllable.syllable(word.getLemma());
	}

	private static int getPolysemyCount(Word word) {
		if (OntologySupport.getWordSenses(word) == null)
			return 0;
		return OntologySupport.getWordSenses(word).size();
	}

	/**
	 * Gets the distance to the root of the hypernym tree. If the word was
	 * disambiguated it starts with the senseId that was determined. Otherwise
	 * it uses the first sense id returned by WordNet. We go up the hypernym
	 * tree always selecting the first hypernym returned by WordNet under the
	 * assumption that it is the most likely one.
	 */
	public static int getDistanceToHypernymTreeRoot(Word word, Lang lang) {
		Sense senseId;

		// if word was disambiguated
		if (word.getLexicalChainLink() != null) {
			senseId = word.getLexicalChainLink().getSenseId();
		} else {
			// get the first sense for the word
			Set<Sense> senseIds = null;
			senseIds = OntologySupport.getWordSenses(word);

			if (senseIds == null)
				return -1;
			Iterator<Sense> it = senseIds.iterator();
			senseId = it.next();
		}
		Set<Sense> hypernyms = JAWJAW.findHypernyms(senseId, lang);
		if (hypernyms == null || hypernyms.isEmpty()) {
			return 0;
		}
		Set<Sense> hypernymsSet = new HashSet<Sense>();
		Iterator<Sense> it = hypernyms.iterator();
		Sense firstSense = it.next();
		hypernymsSet.add(firstSense);
		backtrackThroughHypernymTree(firstSense, hypernymsSet, lang);
		return hypernymsSet.size();
	}

	/**
	 * Since there are cycles in the hypernym tree we need to do some
	 * backtracking. (e.g. group -> abstraction -> concept -> idea -> content ->
	 * collection -> group)
	 */
	private static boolean backtrackThroughHypernymTree(Sense currentWord,
			Set<Sense> previousHypernyms, Lang lang) {
		boolean reachedRoot = false;
		Set<Sense> hypernyms = JAWJAW.findHypernyms(currentWord, lang);

		if (hypernyms == null || hypernyms.isEmpty()) {
			return true;
		}
		for (Sense nextHypernym : hypernyms) {
			if (previousHypernyms.contains(nextHypernym)) {
				// we're in a cycle and need to return
				return false;
			} else {
				previousHypernyms.add(nextHypernym);
				reachedRoot = backtrackThroughHypernymTree(nextHypernym,
						previousHypernyms, lang);
				if (reachedRoot) {
					break;
				}
			}
		}
		// if we haven't reached the root yet we need to remove the current word
		// from the hypernym set and go back
		if (!reachedRoot) {
			previousHypernyms.remove(currentWord);
		}
		return reachedRoot;
	}

	public static int getDifferenceBetweenLemmaAndStem(Word word) {
		return Math.abs(word.getLemma().length() - word.getStem().length());
	}

	private static double getDifferenceBetweenWordAndStem(Word word) {
		return Math.abs(word.getText().length() - word.getStem().length());
	}

	/**
	 * Average syllables per word in the document.
	 */
	public static double getWordSyllableCountMean(AbstractDocument d) {
		double totalSyllables = 0;
		double totalWords = 0;
		for (Entry<Word, Integer> e : d.getWordOccurences().entrySet()) {
			totalSyllables += getSyllables(e.getKey()) * e.getValue();
			totalWords += e.getValue();
		}
		return (totalWords > 0 ? totalSyllables / totalWords : 0);
	}

	/**
	 * Average word senses per word in the document. Note: For some words the
	 * sense count method returns -1 (either word was not found in WordNet or
	 * POS was not recognized). These words do not count toward the mean.
	 */
	public static double getWordPolysemyCountMean(AbstractDocument d) {
		double senseCount = 0;
		double totalWords = 0;
		for (Entry<Word, Integer> e : d.getWordOccurences().entrySet()) {
			int noSenses = getPolysemyCount(e.getKey());
			if (noSenses > 0) {
				senseCount += noSenses * e.getValue();
				totalWords += e.getValue();
			}
		}
		return (totalWords > 0 ? senseCount / totalWords : 0);
	}

	/**
	 * Average distance to the hypernym tree root per word in the document.
	 */
	public static double getWordDistanceToHypernymTreeRootMean(
			AbstractDocument d) {
		double distanceSum = 0;
		double totalWords = 0;
		for (Entry<Word, Integer> e : d.getWordOccurences().entrySet()) {
			distanceSum += getDistanceToHypernymTreeRoot(e.getKey(),
					d.getLanguage())
					* e.getValue();
			totalWords += e.getValue();
		}
		return (totalWords > 0 ? distanceSum / totalWords : 0);
	}

	/**
	 * Average difference between the lemma and stem per word in the document.
	 */
	public static double getWordDifferenceBetweenLemmaAndStemMean(
			AbstractDocument d) {
		double distanceSum = 0;
		double totalWords = 0;
		for (Entry<Word, Integer> e : d.getWordOccurences().entrySet()) {
			distanceSum += getDifferenceBetweenLemmaAndStem(e.getKey())
					* e.getValue();
			totalWords += e.getValue();
		}
		return (totalWords > 0 ? distanceSum / totalWords : 0);
	}

	public static double getWordDifferenceBetweenWordAndStemMean(
			AbstractDocument d) {
		double distanceSum = 0;
		double totalWords = 0;
		for (Entry<Word, Integer> e : d.getWordOccurences().entrySet()) {
			distanceSum += getDifferenceBetweenWordAndStem(e.getKey())
					* e.getValue();
			totalWords += e.getValue();
		}
		return (totalWords > 0 ? distanceSum / totalWords : 0);
	}

	@Override
	public String getClassName() {
		return LocalizationUtils.getTranslation("Word Complexity Factors");
	}

	@Override
	public void setComplexityIndexDescription(String[] descriptions) {
		descriptions[ComplexityIndices.AVERAGE_WORD_DIFF_LEMMA_STEM] = LocalizationUtils.getTranslation("Average distance between lemma and word stems (only content words)");
		descriptions[ComplexityIndices.AVERAGE_WORD_DIFF_WORD_STEM] = LocalizationUtils.getTranslation("Average distance between words and corresponding stems (only content words)");
		descriptions[ComplexityIndices.AVERAGE_WORD_DEPTH_HYPERNYM_TREE] = LocalizationUtils.getTranslation("Average word depth in hypernym tree (only content words)");
		descriptions[ComplexityIndices.AVERAGE_WORD_POLYSEMY_COUNT] = LocalizationUtils.getTranslation("Average word polysemy count (only content words)");
		descriptions[ComplexityIndices.AVERAGE_WORD_SYLLABLE_COUNT] = LocalizationUtils.getTranslation("Average word syllable count (lemmas for content words) (EN only)");
	}
	public void setComplexityIndexAcronym(String[] acronyms) {
		acronyms[ComplexityIndices.AVERAGE_WORD_DIFF_LEMMA_STEM] = this.getComplexityIndexAcronym("AVERAGE_WORD_DIFF_LEMMA_STEM");
		acronyms[ComplexityIndices.AVERAGE_WORD_DIFF_WORD_STEM] = this.getComplexityIndexAcronym("AVERAGE_WORD_DIFF_WORD_STEM");
		acronyms[ComplexityIndices.AVERAGE_WORD_DEPTH_HYPERNYM_TREE] = this.getComplexityIndexAcronym("AVERAGE_WORD_DEPTH_HYPERNYM_TREE");
		acronyms[ComplexityIndices.AVERAGE_WORD_POLYSEMY_COUNT] = this.getComplexityIndexAcronym("AVERAGE_WORD_POLYSEMY_COUNT");
		acronyms[ComplexityIndices.AVERAGE_WORD_SYLLABLE_COUNT] = this.getComplexityIndexAcronym("AVERAGE_WORD_SYLLABLE_COUNT");
	}

	@Override
	public void computeComplexityFactors(AbstractDocument d) {
		d.getComplexityIndices()[ComplexityIndices.AVERAGE_WORD_DIFF_LEMMA_STEM] = WordComplexity
				.getWordDifferenceBetweenLemmaAndStemMean(d);
		d.getComplexityIndices()[ComplexityIndices.AVERAGE_WORD_DIFF_WORD_STEM] = WordComplexity
				.getWordDifferenceBetweenWordAndStemMean(d);
		d.getComplexityIndices()[ComplexityIndices.AVERAGE_WORD_DEPTH_HYPERNYM_TREE] = WordComplexity
				.getWordDistanceToHypernymTreeRootMean(d);
		d.getComplexityIndices()[ComplexityIndices.AVERAGE_WORD_POLYSEMY_COUNT] = WordComplexity
				.getWordPolysemyCountMean(d);
		switch (d.getLanguage()) {
		case fr:
			d.getComplexityIndices()[ComplexityIndices.AVERAGE_WORD_SYLLABLE_COUNT] = ComplexityIndices.IDENTITY;
			break;
		default:
			d.getComplexityIndices()[ComplexityIndices.AVERAGE_WORD_SYLLABLE_COUNT] = WordComplexity
					.getWordSyllableCountMean(d);
		}
	}

	@Override
	public int[] getIDs() {
		return new int[] { ComplexityIndices.AVERAGE_WORD_DIFF_LEMMA_STEM,
				ComplexityIndices.AVERAGE_WORD_DIFF_WORD_STEM,
				ComplexityIndices.AVERAGE_WORD_DEPTH_HYPERNYM_TREE,
				ComplexityIndices.AVERAGE_WORD_POLYSEMY_COUNT,
				ComplexityIndices.AVERAGE_WORD_SYLLABLE_COUNT };
	}
}
