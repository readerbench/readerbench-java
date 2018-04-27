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
package com.readerbench.readingstrategies;

import com.readerbench.datasourceprovider.data.AnalysisElement;
import com.readerbench.datasourceprovider.data.Block;
import com.readerbench.datasourceprovider.data.Sentence;
import com.readerbench.datasourceprovider.data.Word;
import com.readerbench.datasourceprovider.data.discourse.SemanticCohesion;
import com.readerbench.datasourceprovider.data.document.ReadingStrategyType;
import com.readerbench.datasourceprovider.data.semanticmodels.ISemanticModel;
import com.readerbench.datasourceprovider.data.semanticmodels.SimilarityType;
import org.apache.commons.lang3.StringUtils;
import com.readerbench.datasourceprovider.commons.Formatting;
import com.readerbench.coreservices.semanticmodels.wordnet.OntologySupport;

import java.awt.*;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class InferredKnowledgeStrategy {

    private static final Color COLOR_INFERRED_CONCEPTS = new Color(255, 102, 0);
    private static double SIMILARITY_THRESHOLD_KI = 0.7;

    private int addAssociations(Word word, AnalysisElement e, String usedColor, String annotationText) {
        word.getReadingStrategies().add(ReadingStrategyType.INFERRED_KNOWLEDGE);
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

        List<ISemanticModel> semanticModels = sentences.get(0).getSemanticModelsAsList();

        Map<SimilarityType, double[]> modelVectors = new EnumMap<>(SimilarityType.class);

        for (ISemanticModel model : semanticModels) {
            double[] vec = new double[model.getNoDimensions()];
            for (Sentence s : sentences) {
                for (int i = 0; i < model.getNoDimensions(); i++) {
                    vec[i] += s.getModelRepresentation(model.getType())[i];
                }
            }
            modelVectors.put(model.getType(), vec);
        }

        for (Word w1 : v.getWordOccurences().keySet()) {
            // only for words that have not been previously marked as paraphrases and not previously identified as inferred concepts
            if (!w1.getReadingStrategies().contains(ReadingStrategyType.PARAPHRASE) && !w1.getReadingStrategies().contains(ReadingStrategyType.INFERRED_KNOWLEDGE)) {
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
                    double maxSimWord = 0;
                    Word closestWord = null;

                    Map<SimilarityType, Double> similarities = new EnumMap<>(SimilarityType.class);
                    for (ISemanticModel model : semanticModels) {
                        similarities.put(model.getType(), model.getSimilarity(w1.getModelRepresentation(model.getType()), modelVectors.get(model.getType())));
                    }
                    double simSentence = SemanticCohesion.getAggregatedSemanticMeasure(similarities);

                    for (Sentence s : sentences) {
                        for (Word w2 : s.getWordOccurences().keySet()) {
                            // determine semantic proximity
                            double simWord = SemanticCohesion.getAverageSemanticModelSimilarity(w1, w2);

                            if (maxSimWord < simWord) {
                                maxSimWord = simWord;
                                closestWord = w2;
                            }
                        }
                    }

                    if (Math.max(simSentence, maxSimWord) >= SIMILARITY_THRESHOLD_KI) {
                        noOccur += addAssociations(w1, v, usedColor, Formatting.formatNumber(simSentence) + "; "
                                + closestWord.getLemma() + "-" + Formatting.formatNumber(maxSimWord));
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
