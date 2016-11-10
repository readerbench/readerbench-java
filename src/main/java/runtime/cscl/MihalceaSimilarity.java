/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package runtime.cscl;

import data.Word;
import data.cscl.Utterance;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import services.semanticModels.LDA.LDA;
import services.semanticModels.LSA.LSA;
import services.semanticModels.SimilarityType;
import services.semanticModels.WordNet.OntologySupport;

/**
 *
 * @author Gabriel Gutu <gabriel.gutu at cs.pub.ro>
 */
public class MihalceaSimilarity {

    // Mihalcea's formula
    // sim(T1, T2) = .5 * (
    // SUM(maxSim(word, T2) * idf(word)) /
    // SUM(word) + // word from T1
    // SUM(maxSim(word, T1) * idf(word)) /
    // SUM(word)) // word from T2
    public static double compute(Utterance firstUtt, Utterance secondUtt, SimilarityType simType,
            LSA lsa, LDA lda) {
        double sim;
        double leftHandSideUp = 0.0;
        double leftHandSideDown = 0.0;
        double rightHandSideUp = 0.0;
        double rightHandSideDown = 0.0;

        // iterate through words of first sentence
        Iterator<Entry<Word, Integer>> itFirstUtt = firstUtt.getWordOccurences().entrySet().iterator();
        while (itFirstUtt.hasNext()) {
            Map.Entry<Word, Integer> pairFirstUtt = (Map.Entry<Word, Integer>) itFirstUtt.next();
            Word wordFirstUtt = (Word) pairFirstUtt.getKey();
            // iterate through words of second sentence
            double maxSimForWordWithOtherUtt = 0.0;
            Iterator<Entry<Word, Integer>> itSecondUtt = secondUtt.getWordOccurences().entrySet().iterator();
            while (itSecondUtt.hasNext()) {
                Map.Entry<Word, Integer> pairSecondUtt = (Map.Entry<Word, Integer>) itSecondUtt.next();
                Word wordSecondUtt = (Word) pairSecondUtt.getKey();
                if (null == simType) {
                    sim = -1;
                } else {
                    switch (simType) {
                        case LSA:
                            sim = lsa.getSimilarity(wordFirstUtt, wordSecondUtt);
                            break;
                        case LDA:
                            sim = lda.getSimilarity(wordFirstUtt, wordSecondUtt);
                            break;
                        case LEACOCK_CHODOROW:
                            sim = OntologySupport.semanticSimilarity(wordFirstUtt, wordSecondUtt,
                                    SimilarityType.LEACOCK_CHODOROW);
                            break;
                        case WU_PALMER:
                            sim = OntologySupport.semanticSimilarity(wordFirstUtt, wordSecondUtt,
                                    SimilarityType.WU_PALMER);
                            break;
                        case PATH_SIM:
                            sim = OntologySupport.semanticSimilarity(wordFirstUtt, wordSecondUtt,
                                    SimilarityType.PATH_SIM);
                            break;
                        default:
                            sim = -1;
                            break;
                    }
                }
                if (sim > maxSimForWordWithOtherUtt) {
                    maxSimForWordWithOtherUtt = sim;
                }
            }
            // Tip: IDF is considered from LSA for every method
            leftHandSideUp += maxSimForWordWithOtherUtt
                    * lsa.getWordIDf(wordFirstUtt);
            leftHandSideDown += lsa.getWordIDf(wordFirstUtt);
        }

        itFirstUtt = secondUtt.getWordOccurences().entrySet().iterator();
        while (itFirstUtt.hasNext()) {
            Map.Entry<Word, Integer> pairFirstUtt = (Map.Entry<Word, Integer>) itFirstUtt.next();
            Word wordFirstUtt = (Word) pairFirstUtt.getKey();
            // iterate through words of second sentence
            double maxSimForWordWithOtherUtt = 0.0;
            Iterator<Entry<Word, Integer>> itSecondUtt = firstUtt.getWordOccurences().entrySet().iterator();
            while (itSecondUtt.hasNext()) {
                Map.Entry<Word, Integer> pairSecondUtt = (Map.Entry<Word, Integer>) itSecondUtt.next();
                Word wordSecondUtt = (Word) pairSecondUtt.getKey();
                if (null == simType) {
                    sim = -1;
                } else {
                    switch (simType) {
                        case LSA:
                            sim = lsa.getSimilarity(wordFirstUtt, wordSecondUtt);
                            break;
                        case LDA:
                            sim = lda.getSimilarity(wordFirstUtt, wordSecondUtt);
                            break;
                        case LEACOCK_CHODOROW:
                            sim = OntologySupport.semanticSimilarity(wordFirstUtt, wordSecondUtt,
                                    SimilarityType.LEACOCK_CHODOROW);
                            break;
                        case WU_PALMER:
                            sim = OntologySupport.semanticSimilarity(wordFirstUtt, wordSecondUtt,
                                    SimilarityType.WU_PALMER);
                            break;
                        case PATH_SIM:
                            sim = OntologySupport.semanticSimilarity(wordFirstUtt, wordSecondUtt,
                                    SimilarityType.PATH_SIM);
                            break;
                        default:
                            sim = -1;
                            break;
                    }
                }
                if (sim > maxSimForWordWithOtherUtt) {
                    maxSimForWordWithOtherUtt = sim;
                }
            }
            // Tip: IDF is considered from LSA for every method
            rightHandSideUp += maxSimForWordWithOtherUtt * lsa.getWordIDf(wordFirstUtt);
            rightHandSideDown += lsa.getWordIDf(wordFirstUtt);
        }
        return .5 * (((leftHandSideDown > 0) ? (leftHandSideUp / leftHandSideDown) : 0)
                + ((rightHandSideDown > 0) ? (rightHandSideUp / rightHandSideDown) : 0));
    }

}
