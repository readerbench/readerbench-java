/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package services.complexity.readability;

import data.AbstractDocument;
import edu.stanford.nlp.util.Pair;
import java.util.StringTokenizer;
import java.util.regex.Pattern;
import services.complexity.ComplexityIndicesEnum;
import services.complexity.ComplexityIndex;
import services.nlp.listOfWords.ClassesOfWords;
import services.nlp.listOfWords.ListOfWords;

/**
 *
 * @author Stefan
 */
public abstract class ReadabilityIndex extends ComplexityIndex {

    protected static ListOfWords simpleWords_en = null;

    public ReadabilityIndex(ComplexityIndicesEnum index) {
        super(index);
    }

    public static float calcFog(final Fathom.Stats stats) {
        return (wordsPerSentence(stats) + percentComplexWords(stats)) * 0.4f;
    }

    public static String meaningFog(double val) {
        if (val <= 9) {
            return "childish";
        }
        if (val <= 11) {
            return "acceptable";
        }
        if (val <= 13) {
            return "ideal";
        }
        if (val <= 16) {
            return "difficult";
        }
        return "unreadable";
    }

    /**
     * Returns the Flesch reading ease score.
     *
     * <p>
     * 206.835 - (1.015 * words_per_sentence) - (84.6 * syllables_per_word)
     * </p>
     *
     * <p>
     * This score rates text on a 100 point scale. The higher the score, the
     * easier it is to understand the text. A score of 60 to 70 is considered to
     * be optimal.
     * </p>
     *
     * @param stats
     * @return Flesch reading ease score.
     */
    public static float calcFlesch(final Fathom.Stats stats) {
        return 206.835f - (1.015f * wordsPerSentence(stats)) - (84.6f * syllablesPerWords(stats));
    }

    public static String meaningFlesh(double val) {
        if (val >= 90) {
            return "very easy";
        }
        if (val >= 80) {
            return "easy";
        }
        if (val >= 70) {
            return "fairly easy";
        }
        if (val >= 60) {
            return "standard - optimal";
        }
        if (val >= 50) {
            return "fairly difficult";
        }
        if (val > 30) {
            return "difficult";
        }
        return "very confusing";
    }

    /**
     * Returns the Flesch-Kincaid grade level score for the analysed text file
     * or block.
     *
     * <p>
     * (11.8 * syllables_per_word) + (0.39 * words_per_sentence) - 15.59;
     * </p>
     *
     * <p>
     * This score rates text on U.S. grade school level. So a score of 8.0 means
     * that the document can be understood by an eighth grader. A score of 7.0
     * to 8.0 is considered to be optimal.
     * </p>
     *
     * @param stats
     * @return Flesch-Kincaid score.
     */
    public static float calcKincaid(final Fathom.Stats stats) {
        return (11.8f * syllablesPerWords(stats)) + (0.39f * wordsPerSentence(stats)) - 15.59f;
    }

    public static String meaningKincaid(double val) {
        int round = (int) (val + 0.5);

        if (round == 8) {
            return "8th grade - optimal";
        }
        return round + "th grade";
    }

    public static float wordsPerSentence(final Fathom.Stats stats) {
        return ((float) stats.getNumWords()) / stats.getNumSentences();
    }

    public static float percentComplexWords(final Fathom.Stats stats) {
        return (((float) stats.getNumComplexWords()) / stats.getNumWords()) * 100;
    }

    public static float syllablesPerWords(final Fathom.Stats stats) {
        return ((float) stats.getNumSyllables()) / stats.getNumWords();
    }

    public static ListOfWords getSimpleWords() {
        if (simpleWords_en == null) {
            simpleWords_en = new ListOfWords("resources/config/EN/word lists/Dale-Chall simple words.txt");
        }
        return simpleWords_en;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public static double computeDaleChall(AbstractDocument d) {
        // RGS : Reading Grade Score
        // DS : Dale Score, or % of words not on Dale-Chall list of 3,000 common words
        // ASL : average sentence length (the number of words divided by the number of sentences)

        double DS, ASL;
        Pair[] restore = {new Pair<>(Pattern.compile("\\s"), " "), new Pair<>(Pattern.compile("' "), "'"),
            new Pair<>(Pattern.compile(" \\- "), "\\-"),};

        String text = d.getText().toLowerCase();
        for (Pair<Pattern, String> p : restore) {
            text = p.first.matcher(text).replaceAll(p.second).trim();
        }
        double no_occurrences = 0;

        for (String p : getSimpleWords().getWords()) {
            no_occurrences += ClassesOfWords.countPatternOccurrences(text, p);
        }

        StringTokenizer st = new StringTokenizer(text, ".!? ,:;");

        DS = 1 - no_occurrences / st.countTokens();
        ASL = ((double) st.countTokens()) / d.getNoSentences();

        double RGS = (0.1579 * DS * 100) + (0.0496 * ASL);

        if (DS > 0.05d) {
            RGS += 3.6365;
        }

        return RGS;
    }
}
