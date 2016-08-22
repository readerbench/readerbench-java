/* 
 * Copyright 2016 ReaderBench.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package services.readingStrategies;

import java.awt.Color;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import data.AnalysisElement;
import data.Block;
import data.Sentence;
import data.Word;
import services.commons.Formatting;
import services.commons.VectorAlgebra;
import services.semanticModels.LDA.LDA;
import services.semanticModels.LSA.LSA;
import services.semanticModels.WordNet.OntologySupport;
import services.semanticModels.WordNet.SimilarityType;

public class InferredKnowledgeStrategy {

    private static final Color COLOR_INFERRED_CONCEPTS = new Color(255, 102, 0);
    private static double SIMILARITY_THRESHOLD_KI = 0.7;

    private int addAssociations(Word word, AnalysisElement e, String usedColor, String annotationText) {
        word.getReadingStrategies()[ReadingStrategies.INFERRED_KNOWLEDGE] = true;
        int noOccurences = StringUtils.countMatches(" " + e.getAlternateText() + " ", " " + word.getText() + " ");
        e.setAlternateText(
                PatternMatching.colorTextStar(e.getAlternateText(), word.getText(), usedColor, annotationText));

        // recheck just to be sure
        noOccurences += StringUtils.countMatches(" " + e.getAlternateText() + " ", " " + word.getText() + " ");
        e.setAlternateText(
                PatternMatching.colorTextStar(e.getAlternateText(), word.getText(), usedColor, annotationText));
        if (noOccurences > 0) {
            return 1;
        }
        return 0;
    }

    public int getInferredConcepts(Block v, List<Sentence> sentences) {
        String usedColor = Integer.toHexString(COLOR_INFERRED_CONCEPTS.getRGB());
        usedColor = usedColor.substring(2, usedColor.length());

        int noOccur = 0;

        // determine vectors for collections of sentences
        double[] vectorSentences = new double[LSA.K];
        double[] probDistribSentences = new double[v.getLDA().getNoTopics()];

        for (Sentence s : sentences) {
            for (int i = 0; i < LSA.K; i++) {
                vectorSentences[i] += s.getLSAVector()[i];
            }
            for (int i = 0; i < v.getLDA().getNoTopics(); i++) {
                probDistribSentences[i] += s.getLDAProbDistribution()[i];
            }
        }
        probDistribSentences = VectorAlgebra.normalize(probDistribSentences);

        for (Word w1 : v.getWordOccurences().keySet()) {
            // only for words that have not been previously marked as
            // paraphrases and not previously identified as inferred concepts
            if (!w1.getReadingStrategies()[ReadingStrategies.PARAPHRASE]
                    && !w1.getReadingStrategies()[ReadingStrategies.INFERRED_KNOWLEDGE]) {
                // determine if alternative paraphrasing exists
                boolean hasAssociations = false;
                loopsentence:
                for (Sentence s : sentences) {
                    for (Word w2 : s.getWordOccurences().keySet()) {
                        // check for identical lemmas or synonyms
                        if (w1.getLemma().equals(w2.getLemma())
                                || OntologySupport.areSynonyms(w1, w2, v.getLanguage())) {
                            hasAssociations = true;
                            break loopsentence;
                        }
                    }
                }
                // use only potential inferred concepts
                if (!hasAssociations) {
                    // determine maximum likelihood
                    double maxSim = 0;
                    double[] probDistrib1 = w1.getLDAProbDistribution();
                    double[] vector1 = v.getLSA().getWordVector(w1);
                    Word closestWord = null;

                    // add similarity to sentences as a measure of importance of
                    // the word
                    double simLSASentences = VectorAlgebra.cosineSimilarity(vector1, vectorSentences);
                    double simLDASentences = LDA.getSimilarity(probDistrib1, probDistribSentences);
                    double simMaxSentence = Math.max(simLSASentences, simLDASentences);

                    for (Sentence s : sentences) {
                        for (Word w2 : s.getWordOccurences().keySet()) {
                            // determine semantic proximity
                            double simLSAWord = VectorAlgebra.cosineSimilarity(vector1, v.getLSA().getWordVector(w2));
                            double simLDAWord = LDA.getSimilarity(probDistrib1, w2.getLDAProbDistribution());
                            double simWNWord = OntologySupport.semanticSimilarity(w1, w2, SimilarityType.WU_PALMER);
                            double simMaxWord = Math.max(simWNWord, Math.max(simLSAWord, simLDAWord));

                            if (maxSim < simMaxWord) {
                                maxSim = simMaxWord;
                                closestWord = w2;
                            }
                        }
                    }

                    if (Math.max(maxSim, simMaxSentence) >= SIMILARITY_THRESHOLD_KI) {
                        noOccur += addAssociations(w1, v, usedColor, Formatting.formatNumber(simMaxSentence) + "; "
                                + closestWord.getLemma() + "-" + Formatting.formatNumber(maxSim));
                    }
                }
            }
        }
        return noOccur;
    }

    public static void setSimilarityThresholdKI(double similarityThreshold) {
        SIMILARITY_THRESHOLD_KI = similarityThreshold;
    }
}
