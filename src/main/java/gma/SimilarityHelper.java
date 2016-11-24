package gma;

import data.AnalysisElement;
import data.Lang;
import data.Word;
import data.cscl.CSCLConstants;
import java.util.Map;
import java.util.TreeMap;
import services.semanticModels.LDA.LDA;
import services.semanticModels.LSA.LSA;
import services.semanticModels.SimilarityType;
import services.semanticModels.WordNet.OntologySupport;

public class SimilarityHelper {

    public static final double MIN_LSA_THRESHOLD = 0.7;
    public static final double MAX_LSA_THRESHOLD = 1.0;
    public static final double MIN_LDA_THRESHOLD = 0.7;
    public static final double MAX_LDA_THRESHOLD = 1.0;
    public static final double WORDNET_SIMILARITY_THRESHOLD = 1.0;
    public static final int MAX_NO_OF_LATENT_SIMILARITIES = 3;
    private static double TOPIC_RELATEDNESS_THRESHOLD = 0.6;

    // Used for avoiding to recalculate all the entries.
    // If the value of the threshold is set higher, the entries that have a
    // lower threshold will be deleted
    AnalysisElement doc;
    private boolean useLatentSimilarities;

    public SimilarityHelper(AnalysisElement doc, boolean useLatentSimilarities) {
        this.doc = doc;
        this.useLatentSimilarities = useLatentSimilarities;
    }

    public TreeMap<Word, Double> getSimilarTopics(Word word, LSA lsa, LDA lda) {

        TreeMap<Word, Double> similarConcepts;

        similarConcepts = new TreeMap<Word, Double>();
        word.ldaSimilarityToUnderlyingConcept = 1.0; // make sure it passes
        // further
        // verifications
        similarConcepts.put(word, 1.0);
        similarConcepts
                .putAll(OntologySupport.getExtendedSymilarConcepts(word));

        if (useLatentSimilarities) {
            similarConcepts.putAll(getLatentSimilarities(word, lsa, lda));
        }

        return similarConcepts;

    }

    private TreeMap<Word, Double> getLatentSimilarities(Word word, LSA lsa, LDA lda) {

        TreeMap<Word, Double> similarConcepts = new TreeMap<Word, Double>();

        // getting similar concepts using LSA
        if (lsa != null) {
            TreeMap<Word, Double> rawResults 
                    = limitTheNumberOfResults(lsa.getSimilarConcepts(word, MIN_LSA_THRESHOLD));
            similarConcepts.putAll(processLsaSimilarities(rawResults));
        }

        // getting similar concepts using LDA
        if (lda != null) {
            TreeMap<Word, Double> rawResults 
                        = limitTheNumberOfResults(lda.getSimilarConcepts(word, MIN_LDA_THRESHOLD));
            similarConcepts.putAll(processLdaSimilarities(rawResults));
        }
        return similarConcepts;
    }

    private TreeMap<Word, Double> processLsaSimilarities(
            TreeMap<Word, Double> rawResults) {

        for (Map.Entry<Word, Double> result : rawResults.entrySet()) {
            result.getKey().lsaSimilarityToUnderlyingConcept = result
                    .getValue();
        }
        return rawResults;
    }

    private TreeMap<Word, Double> processLdaSimilarities(
            TreeMap<Word, Double> rawResults) {

        for (Map.Entry<Word, Double> result : rawResults.entrySet()) {
            result.getKey().ldaSimilarityToUnderlyingConcept = result
                    .getValue();
        }

        return rawResults;
    }

    private TreeMap<Word, Double> limitTheNumberOfResults(
            TreeMap<Word, Double> rawResults) {
        TreeMap<Word, Double> results = new TreeMap<Word, Double>();

        // limit the maximum results
        if (rawResults.size() > MAX_NO_OF_LATENT_SIMILARITIES) {
            for (Map.Entry<Word, Double> entry : rawResults.entrySet()) {
                Word lessCorrelatedWord = getElementLessCorrelated(
                        entry.getValue(), results);
                if (lessCorrelatedWord != null) {
                    results.remove(lessCorrelatedWord);
                    results.put(entry.getKey(), entry.getValue());
                } else if (results.size() < MAX_NO_OF_LATENT_SIMILARITIES) {
                    results.put(entry.getKey(), entry.getValue());
                }
            }
        } else {
            results.putAll(rawResults);
        }

        return results;
    }

    private Word getElementLessCorrelated(double threshold,
            TreeMap<Word, Double> results) {
        if (results.size() < MAX_NO_OF_LATENT_SIMILARITIES) {
            return null;
        } else {
            for (Map.Entry<Word, Double> result : results.entrySet()) {
                if (result.getValue() < threshold) {
                    return result.getKey();
                }
            }

            return null;
        }

    }

    public boolean isRelated(Word word1, Word word2,LSA lsa, LDA lda,
            double topic_relatedness_threshold) {
        int noOfAppliedSemanticModels = 0;
        double similarityMeasure = 0;

        if (lsa != null) {
            similarityMeasure += lsa.getSimilarity(word1, word2);
            noOfAppliedSemanticModels++;
        }
        if (lda != null) {
            similarityMeasure += lda.getSimilarity(word1, word2);
            noOfAppliedSemanticModels++;
        }

        similarityMeasure += OntologySupport.semanticSimilarity(word1, word2,
                SimilarityType.LEACOCK_CHODOROW);
        noOfAppliedSemanticModels++;

        similarityMeasure /= noOfAppliedSemanticModels;

        if (similarityMeasure > topic_relatedness_threshold) {
            return true;
        }

        return false;

    }

}
