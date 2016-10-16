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
package data.discourse;

import cc.mallet.util.Maths;
import data.AnalysisElement;
import data.Word;
import java.util.EnumMap;
import services.commons.VectorAlgebra;
import services.semanticModels.WordNet.OntologySupport;
import services.semanticModels.SimilarityType;

/**
 * Computes a semantic relatedness value of two analysis element by combining
 * three different semantic relatedness metric techniques: Latent Semantic
 * Analysis, Latent Dirichlet Allocation and three different WordNet metrics:
 * Leacock Chodorow, Wu Palmer and PathSim.
 *
 * @author Gabriel Gutu
 *
 */
public class SemanticRelatedness extends SemanticCohesion {

    private static final long serialVersionUID = 7561413289472294392L;

    /**
     * Holds a value quantifying semantic relatedness between the two object of
     * type AnalysisElement: source and destination.
     */
    private double relatedness;

    /**
     * @param source The first element for which semantic relatedness should be
     * computed.
     * @param destination The second element for which semantic relatedness
     * should be computed.
     */
    public SemanticRelatedness(AnalysisElement source, AnalysisElement destination) {
        super(source, destination);

        // helper values
        double lowerValue = 0,
                upperValueLsa = 0,
                upperValueLda = 0,
                leftHandValueLsa = 0,
                leftHandValueLda = 0,
                rightHandValueLsa = 0,
                rightHandValueLda = 0;

        // helper values
        EnumMap<SimilarityType, Double> upperValueOntology = new EnumMap<>(SimilarityType.class),
                leftHandValueOntology = new EnumMap<>(SimilarityType.class),
                rightHandValueOntology = new EnumMap<>(SimilarityType.class);

        // iterate through all words of source analysis element
        for (Word w1 : source.getWordOccurences().keySet()) {
            // helper values
            double maxSimLsa = 0,
                    maxSimLda = 0;
            EnumMap<SimilarityType, Double> maxSimOntology = new EnumMap<>(SimilarityType.class);

            // iterate through all words of destination analysis element
            for (Word w2 : destination.getWordOccurences().keySet()) {
                // determine the word of the destination analysis element for whom
                // the Latent Semantic Analysis value is the highest with the word
                // of the source analysis element
                double localSimLsa = VectorAlgebra.cosineSimilarity(w1.getModelRepresentation(SimilarityType.LSA), w2.getModelRepresentation(SimilarityType.LSA));
                if (localSimLsa > maxSimLsa) {
                    maxSimLsa = localSimLsa;
                }

                // determine the word of the destination analysis element for whom
                // the Latent Dirichlet Allocation value is the highest with the word
                // of the source analysis element 
                double localSimLda;
                if (w1.getModelRepresentation(SimilarityType.LDA) == null || w2.getModelRepresentation(SimilarityType.LDA) == null) {
                    localSimLda = 0;
                } else {
                    localSimLda = 1 - Maths.jensenShannonDivergence(VectorAlgebra.normalize(w1.getModelRepresentation(SimilarityType.LDA)),
                            VectorAlgebra.normalize(w2.getModelRepresentation(SimilarityType.LDA)));
                }
                if (localSimLda > maxSimLda) {
                    maxSimLda = localSimLda;
                }

                // determine the word of the destination analysis element for whom
                // the WordNet similarity value is the highest with the word
                // of the source analysis element, for different algorithms
                for (SimilarityType st : SimilarityType.values()) {
                    double localSim = OntologySupport.semanticSimilarity(w1, w2, st);
                    if (localSim > maxSimOntology.get(st)) {
                        maxSimOntology.put(st, localSim);
                    }
                }
            }

            // TODO: multiply with term-frequency (how to?)
            upperValueLsa += maxSimLsa * w1.getIdf();
            upperValueLda += maxSimLda * w1.getIdf();
            for (SimilarityType st : SimilarityType.values()) {
                upperValueOntology.put(st,
                        upperValueOntology.get(st) + maxSimOntology.get(st) * w1.getIdf());
            }
            lowerValue += w1.getIdf();
        }

        // compute the left hand side of the equation
        leftHandValueLsa = upperValueLsa / lowerValue;
        leftHandValueLda = upperValueLda / lowerValue;
        for (SimilarityType st : SimilarityType.values()) {
            leftHandValueOntology.put(st, upperValueOntology.get(st) / lowerValue);
        }

        // helper values reset
        lowerValue = 0;
        upperValueLsa = 0;
        upperValueLda = 0;
        upperValueOntology = new EnumMap<>(SimilarityType.class);

        // iterate through all words of destination analysis element
        for (Word w1 : destination.getWordOccurences().keySet()) {
            // helper values
            double maxSimLsa = 0,
                    maxSimLda = 0;
            EnumMap<SimilarityType, Double> maxSimOntology = new EnumMap<>(SimilarityType.class);

            // iterate through all words of source analysis element
            for (Word w2 : source.getWordOccurences().keySet()) {
                // determine the word of the destination analysis element for whom
                // the Latent Semantic Analysis value is the highest with the word
                // of the source analysis element				
                double localSimLsa = VectorAlgebra.cosineSimilarity(w1.getModelRepresentation(SimilarityType.LSA), w2.getModelRepresentation(SimilarityType.LSA));
                if (localSimLsa > maxSimLsa) {
                    maxSimLsa = localSimLsa;
                }

                // determine the word of the destination analysis element for whom
                // the Latent Dirichlet Allocation value is the highest with the word
                // of the source analysis element 
                double localSimLda;
                if (w1.getModelRepresentation(SimilarityType.LDA) == null || w2.getModelRepresentation(SimilarityType.LDA) == null) {
                    localSimLda = 0;
                } else {
                    localSimLda = 1 - Maths.jensenShannonDivergence(VectorAlgebra.normalize(w1.getModelRepresentation(SimilarityType.LDA)),
                            VectorAlgebra.normalize(w2.getModelRepresentation(SimilarityType.LDA)));
                }
                if (localSimLda > maxSimLda) {
                    maxSimLda = localSimLda;
                }

                // determine the word of the destination analysis element for whom
                // the WordNet similarity value is the highest with the word
                // of the source analysis element, for different algorithms
                for (SimilarityType st : SimilarityType.values()) {
                    double localSim = OntologySupport.semanticSimilarity(w1, w2, st);
                    if (localSim > maxSimOntology.get(st)) {
                        maxSimOntology.put(st, localSim);
                    }
                }
            }

            // TODO: multiply with term-frequency (how to?)
            upperValueLsa += maxSimLsa * w1.getIdf();
            upperValueLda += maxSimLda * w1.getIdf();
            for (SimilarityType st : SimilarityType.values()) {
                upperValueOntology.put(st, upperValueOntology.get(st)
                        + maxSimOntology.get(st) * w1.getIdf());
            }
            lowerValue += w1.getIdf();
        }

        // compute the right hand side of the equation
        rightHandValueLsa = upperValueLsa / lowerValue;
        rightHandValueLda = upperValueLda / lowerValue;
        for (SimilarityType st : SimilarityType.values()) {
            rightHandValueOntology.put(st, upperValueOntology.get(st) / lowerValue);
        }

        // compute the semantic relatedness values for the three different semantic
        // similarity measurement techniques
        this.similarities.put(SimilarityType.LSA, 0.5 * (leftHandValueLsa + rightHandValueLsa));
        this.similarities.put(SimilarityType.LDA, 0.5 * (leftHandValueLda + rightHandValueLda));
        for (SimilarityType st : SimilarityType.values()) {
            similarities.put(st,
                    0.5 * (leftHandValueOntology.get(st) + rightHandValueOntology.get(st)));
        }
        // compute the final semantic relatedness value by combining different metrics 
        if (Math.min(source.getWordOccurences().size(), destination.getWordOccurences().size()) > 0) {
            relatedness = SemanticCohesion.getAggregatedSemanticMeasure(similarities);
        }
    }

    private double getMaxSemOntologySim(AnalysisElement u1, AnalysisElement u2, SimilarityType typeOfSimilarity) {
        double distance = 0;
        double sum = 0;
        // determine asymmetric measure of similarity as sum of all max
        // distances
        for (Word w1 : u1.getWordOccurences().keySet()) {
            double factor = 1 + Math.log(u1.getWordOccurences().get(w1));
            sum += factor;
            distance += factor * getMaxSemOntologySim(w1, u2, typeOfSimilarity);
        }
        // apply normalization with regards to the number of words
        if (sum > 0) {
            return distance / sum;
        }
        return 0;
    }

    /**
     * Returns computed semantic relatedness of the two analysis elements.
     *
     * @return semantic relatedness
     */
    public double getRelatedness() {
        return relatedness;
    }

    /**
     * Sets semantic relatedness of the two analysis elements.
     *
     * @param relatedness semantic relatedness
     */
    public void setRelatedness(double relatedness) {
        this.relatedness = relatedness;
    }
}
