package services.complexity.wordComplexity;

import java.util.Map.Entry;

import services.complexity.ComplexityIndices;
import services.complexity.IComplexityFactors;
import services.complexity.readability.Syllable;
import services.semanticModels.WordNet.OntologySupport;
import utils.localization.LocalizationUtils;
import data.AbstractDocument;
import data.Word;
import data.Lang;
import java.util.ArrayList;
import java.util.function.BiFunction;
import java.util.function.Function;
import vu.wntools.wordnet.WordnetData;

public class WordComplexity extends IComplexityFactors {

    private static int getSyllables(Word word) {
        return Syllable.syllable(word.getLemma());
    }

    private static int getPolysemyCount(Word word) {
        if (OntologySupport.getWordSenses(word) == null) {
            return 0;
        }
        return OntologySupport.getWordSenses(word).size();
    }

    /**
     * Gets the distance to the root of the hypernym tree. If the word was
     * disambiguated it starts with the senseId that was determined. Otherwise
     * it uses the first sense id returned by WordNet. We go up the hypernym
     * tree always selecting the first hypernym returned by WordNet under the
     * assumption that it is the most likely one.
     */
    public static int getMaxDistanceToHypernymTreeRoot(Word word, Lang lang) {
        String senseId;

        // if word was disambiguated
        if (word.getLexicalChainLink() != null) {
            senseId = word.getLexicalChainLink().getSenseId();
        } else {
            // get the first sense for the word
            senseId = OntologySupport.getFirstSense(word);
        }
        WordnetData dictionary = OntologySupport.getDictionary(word);
        ArrayList<ArrayList<String>> targetChains = new ArrayList<>();
        if (dictionary.hyperRelations.containsKey(senseId)) {
            dictionary.getMultipleHyperChain(senseId, targetChains);
            return targetChains.stream().mapToInt(ArrayList::size).max().orElse(0);
        }
        return 0;
    }

    public static double getAverageDistanceToHypernymTreeRoot(Word word, Lang lang) {
        String senseId;

        // if word was disambiguated
        if (word.getLexicalChainLink() != null) {
            senseId = word.getLexicalChainLink().getSenseId();
        } else {
            // get the first sense for the word
            senseId = OntologySupport.getFirstSense(word);
        }
        return OntologySupport.getDictionary(word).getAverageDepthBySynset(senseId);
    }

    public static int getPathCountToHypernymTreeRoot(Word word, Lang lang) {
        String senseId;

        // if word was disambiguated
        if (word.getLexicalChainLink() != null) {
            senseId = word.getLexicalChainLink().getSenseId();
        } else {
            // get the first sense for the word
            senseId = OntologySupport.getFirstSense(word);
        }
        WordnetData dictionary = OntologySupport.getDictionary(word);
        ArrayList<ArrayList<String>> targetChains = new ArrayList<>();
        if (dictionary.hyperRelations.containsKey(senseId)) {
            dictionary.getMultipleHyperChain(senseId, targetChains);
            return targetChains.size();
        }
        return 0;
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
            distanceSum += getMaxDistanceToHypernymTreeRoot(e.getKey(),
                    d.getLanguage())
                    * e.getValue();
            totalWords += e.getValue();
        }
        return (totalWords > 0 ? distanceSum / totalWords : 0);
    }

    public static double getAverageComplexity(
            AbstractDocument d,
            BiFunction<Word, Lang, ? extends Number> f) {
        double distanceSum = 0;
        double totalWords = 0;
        for (Entry<Word, Integer> e : d.getWordOccurences().entrySet()) {
            distanceSum += f.apply(e.getKey(),
                    d.getLanguage()).doubleValue()
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
        descriptions[ComplexityIndices.WORD_DIFF_LEMMA_STEM] = LocalizationUtils.getTranslation("Average distance between lemma and word stems (only content words)");
        descriptions[ComplexityIndices.WORD_DIFF_WORD_STEM] = LocalizationUtils.getTranslation("Average distance between words and corresponding stems (only content words)");
        descriptions[ComplexityIndices.WORD_MAX_DEPTH_HYPERNYM_TREE] = LocalizationUtils.getTranslation("WORD_MAX_DEPTH_HYPERNYM_TREE");
        descriptions[ComplexityIndices.WORD_AVERAGE_DEPTH_HYPERNYM_TREE] = LocalizationUtils.getTranslation("WORD_AVERAGE_DEPTH_HYPERNYM_TREE");
        descriptions[ComplexityIndices.WORD_PATH_COUNT_HYPERNYM_TREE] = LocalizationUtils.getTranslation("WORD_PATH_COUNT_HYPERNYM_TREE");
        descriptions[ComplexityIndices.WORD_POLYSEMY_COUNT] = LocalizationUtils.getTranslation("Average word polysemy count (only content words)");
        descriptions[ComplexityIndices.WORD_SYLLABLE_COUNT] = LocalizationUtils.getTranslation("Average word syllable count (lemmas for content words) (EN only)");
    }

    public void setComplexityIndexAcronym(String[] acronyms) {
        acronyms[ComplexityIndices.WORD_DIFF_LEMMA_STEM] = this.getComplexityIndexAcronym("AVERAGE_WORD_DIFF_LEMMA_STEM");
        acronyms[ComplexityIndices.WORD_DIFF_WORD_STEM] = this.getComplexityIndexAcronym("AVERAGE_WORD_DIFF_WORD_STEM");
        acronyms[ComplexityIndices.WORD_MAX_DEPTH_HYPERNYM_TREE] = this.getComplexityIndexAcronym("AVERAGE_WORD_DEPTH_HYPERNYM_TREE");
        acronyms[ComplexityIndices.WORD_POLYSEMY_COUNT] = this.getComplexityIndexAcronym("AVERAGE_WORD_POLYSEMY_COUNT");
        acronyms[ComplexityIndices.WORD_SYLLABLE_COUNT] = this.getComplexityIndexAcronym("AVERAGE_WORD_SYLLABLE_COUNT");
    }

    @Override
    public void computeComplexityFactors(AbstractDocument d) {
        d.getComplexityIndices()[ComplexityIndices.WORD_DIFF_LEMMA_STEM] = WordComplexity
                .getWordDifferenceBetweenLemmaAndStemMean(d);
        d.getComplexityIndices()[ComplexityIndices.WORD_DIFF_WORD_STEM] = WordComplexity
                .getWordDifferenceBetweenWordAndStemMean(d);
        d.getComplexityIndices()[ComplexityIndices.WORD_MAX_DEPTH_HYPERNYM_TREE] = WordComplexity
                .getAverageComplexity(d, WordComplexity::getMaxDistanceToHypernymTreeRoot);
        d.getComplexityIndices()[ComplexityIndices.WORD_AVERAGE_DEPTH_HYPERNYM_TREE] = WordComplexity
                .getAverageComplexity(d, WordComplexity::getAverageDistanceToHypernymTreeRoot);
        d.getComplexityIndices()[ComplexityIndices.WORD_PATH_COUNT_HYPERNYM_TREE] = WordComplexity
                .getAverageComplexity(d, WordComplexity::getPathCountToHypernymTreeRoot);
        d.getComplexityIndices()[ComplexityIndices.WORD_POLYSEMY_COUNT] = WordComplexity
                .getWordPolysemyCountMean(d);
        switch (d.getLanguage()) {
            case fr:
                d.getComplexityIndices()[ComplexityIndices.WORD_SYLLABLE_COUNT] = ComplexityIndices.IDENTITY;
                break;
            default:
                d.getComplexityIndices()[ComplexityIndices.WORD_SYLLABLE_COUNT] = WordComplexity
                        .getWordSyllableCountMean(d);
        }
    }

    @Override
    public int[] getIDs() {
        return new int[]{ComplexityIndices.WORD_DIFF_LEMMA_STEM,
            ComplexityIndices.WORD_DIFF_WORD_STEM,
            ComplexityIndices.WORD_MAX_DEPTH_HYPERNYM_TREE,
            ComplexityIndices.WORD_POLYSEMY_COUNT,
            ComplexityIndices.WORD_SYLLABLE_COUNT};
    }
}
