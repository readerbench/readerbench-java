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

import java.io.Serializable;

import cc.mallet.util.Maths;
import data.AnalysisElement;
import data.Word;
import java.util.EnumMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import services.commons.Formatting;
import services.commons.VectorAlgebra;
import services.semanticModels.WordNet.OntologySupport;
import services.semanticModels.WordNet.SimilarityType;

/**
 * @author Mihai Dascalu
 *
 */
public class SemanticCohesion implements Serializable {

    private static final long serialVersionUID = 7561413289472294392L;

    public static final int WINDOW_SIZE = 20;
    public static final double WEIGH_WN = 1.0;
    public static final double WEIGH_LSA = 1.0;
    public static final double WEIGH_LDA = 1.0;

    protected final AnalysisElement source;
    protected final AnalysisElement destination;
    protected final EnumMap<SimilarityType, Double> similarities;

    public static double getAggregatedSemanticMeasure(double lsaSim, double ldaSim) {
        double cohesion = 0;
        double divisor = 0;
        if (lsaSim > 0) {
            divisor += WEIGH_LSA;
        } else {
            lsaSim = 0;
        }
        if (ldaSim > 0) {
            divisor += WEIGH_LDA;
        } else {
            ldaSim = 0;
        }
        if (divisor > 0) {
            cohesion = (WEIGH_LSA * lsaSim + WEIGH_LDA * ldaSim) / divisor;
        }
        if (cohesion > 0) {
            return cohesion;
        }
        return 0;
    }

    public static double getCohesionMeasure(double WNSim, double lsaSim, double ldaSim) {
        double cohesion = (WEIGH_WN * WNSim + WEIGH_LSA * lsaSim + WEIGH_LDA * ldaSim)
                / (WEIGH_WN + WEIGH_LSA + WEIGH_LDA);
        if (cohesion > 0) {
            return cohesion;
        }
        return 0;
    }

    /**
     * @param source
     * @param destination
     */
    public SemanticCohesion(AnalysisElement source, AnalysisElement destination) {
        this.source = source;
        this.destination = destination;
        this.similarities = new EnumMap<>(SimilarityType.class);
        this.similarities.put(SimilarityType.LSA, VectorAlgebra.cosineSimilarity(source.getLSAVector(), destination.getLSAVector()));
        if (source.getLDAProbDistribution() == null || destination.getLDAProbDistribution() == null) {
            this.similarities.put(SimilarityType.LDA, 0d);
        } else {
            this.similarities.put(SimilarityType.LDA, 1 - Maths.jensenShannonDivergence(VectorAlgebra.normalize(source.getLDAProbDistribution()), VectorAlgebra.normalize(destination.getLDAProbDistribution())));
        }
        //TODO - add support for Word2Vec
        similarities.put(SimilarityType.WORD2VEC, 0d);

        similarities.put(SimilarityType.LEACOCK_CHODOROW, getOntologySim(source, destination, SimilarityType.LEACOCK_CHODOROW));
        similarities.put(SimilarityType.WU_PALMER, getOntologySim(source, destination, SimilarityType.WU_PALMER));
        similarities.put(SimilarityType.PATH_SIM, getOntologySim(source, destination, SimilarityType.PATH_SIM));
        
    }

    public SemanticCohesion(EnumMap<SimilarityType, Double> similarities) {
        this.source = null;
        this.destination = null;
        this.similarities = similarities;
    }

    // compute semantic distance between word and Analysis Element
    public double getMaxSemOntologySim(Word w1, AnalysisElement u2, SimilarityType typeOfSimilarity) {
        double maxLocalDist = 0;
        // identify closest concept
        for (Word w2 : u2.getWordOccurences().keySet()) {
            if (w1.getLemma().equals(w2.getLemma()) || w1.getStem().equals(w2.getStem())) {
                return 1;
            } else {
                maxLocalDist = Math.max(maxLocalDist, OntologySupport.semanticSimilarity(w1, w2, typeOfSimilarity));
            }
        }
        return maxLocalDist;
    }

    private double getMaxSemOntologySim(AnalysisElement u1, AnalysisElement u2, SimilarityType typeOfSimilarity) {
        double distance = 0;
        double sum = 0;
        // determine asymmetric measure of similarity as sum of all max
        // distances
        Map<Word, Double> factors = u1.getWordOccurences().entrySet().parallelStream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> 1 + Math.log(e.getValue())));
        distance = factors.entrySet().parallelStream()
                .mapToDouble(e -> e.getValue() * getMaxSemOntologySim(e.getKey(), u2, typeOfSimilarity))
                .sum();
        sum = factors.values().parallelStream().mapToDouble(x -> x).sum();
        // apply normalization with regards to the number of words
        if (sum > 0) {
            return distance / sum;
        }
        return 0;
    }

    // compute symmetric measure of similarity
    private double getOntologySim(AnalysisElement u1, AnalysisElement u2, SimilarityType typeOfSimilarity) {
        return 1.0d / 2 * (getMaxSemOntologySim(u1, u2, typeOfSimilarity) + getMaxSemOntologySim(u2, u1, typeOfSimilarity));
    }

    public AnalysisElement getSource() {
        return source;
    }

    public AnalysisElement getDestination() {
        return destination;
    }

    public double getCohesion() {
        return getCohesionMeasure(similarities.get(SimilarityType.WU_PALMER), similarities.get(SimilarityType.LSA), similarities.get(SimilarityType.LDA));
    }

    public EnumMap<SimilarityType, Double> getSemanticSimilarities() {
        return similarities;
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();
        for (SimilarityType st : SimilarityType.values()) {
            s.append(st.getName()).append("=").append(Formatting.formatNumber(similarities.get(st))).append(";");
        }
        return s.toString();
    }

    public String print() {
        StringBuilder s = new StringBuilder();
        for (SimilarityType st : SimilarityType.values()) {
            s.append(Formatting.formatNumber(similarities.get(st))).append(",");
        }
        return s.toString();
    }
}
