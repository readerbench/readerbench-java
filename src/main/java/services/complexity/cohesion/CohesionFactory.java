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
package services.complexity.cohesion;

import data.Lang;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import services.complexity.ComplexityIndecesEnum;
import services.complexity.ComplexityIndecesFactory;
import services.complexity.ComplexityIndex;
import services.complexity.cohesion.discourse.AvgBlockScore;
import services.complexity.cohesion.discourse.AvgSentenceScore;
import services.complexity.cohesion.discourse.BlockScoreSD;
import services.complexity.cohesion.discourse.SentenceScoreSD;
import services.complexity.cohesion.flow.DocFlowCriteria;
import services.complexity.cohesion.flow.DocFlowIndex;
import services.complexity.cohesion.flow.DocumentFlow;
import services.complexity.cohesion.lexicalChains.AvgLexicalChainsPerBlock;
import services.complexity.cohesion.lexicalChains.LexicalChainsAvgSpan;
import services.complexity.cohesion.lexicalChains.LexicalChainsCoverage;
import services.complexity.cohesion.lexicalChains.LexicalChainsMaxSpan;
import services.complexity.cohesion.semantic.AvgBlockAdjacencyCohesion;
import services.complexity.cohesion.semantic.AvgBlockDocCohesion;
import services.complexity.cohesion.semantic.AvgInterBlockCohesion;
import services.complexity.cohesion.semantic.AvgIntraBlockCohesion;
import services.complexity.cohesion.semantic.AvgMiddleEndCohesion;
import services.complexity.cohesion.semantic.AvgSentenceAdjacencyCohesion;
import services.complexity.cohesion.semantic.AvgSentenceBlockCohesion;
import services.complexity.cohesion.semantic.AvgStartMiddleCohesion;
import services.complexity.cohesion.semantic.AvgTransitionCohesion;
import services.complexity.cohesion.semantic.StartEndCohesion;
import services.semanticModels.WordNet.SimilarityType;

/**
 *
 * @author Stefan Ruseti
 */
public class CohesionFactory extends ComplexityIndecesFactory {

    @Override
    public List<ComplexityIndex> build(Lang lang) {
        List<ComplexityIndex> result = new ArrayList<>();
        result.add(new LexicalChainsAvgSpan());
        result.add(new LexicalChainsMaxSpan());
        result.add(new AvgLexicalChainsPerBlock());
        result.add(new LexicalChainsCoverage());
        result.add(new AvgBlockScore());
        result.add(new AvgSentenceScore());
        result.add(new BlockScoreSD());
        result.add(new SentenceScoreSD());

        for (SimilarityType simType : SimilarityType.values()) {
            result.add(new AvgBlockAdjacencyCohesion(simType));
            result.add(new AvgBlockDocCohesion(simType));
            result.add(new AvgInterBlockCohesion(simType));
            result.add(new AvgIntraBlockCohesion(simType));
            result.add(new AvgMiddleEndCohesion(simType));
            result.add(new AvgSentenceAdjacencyCohesion(simType));
            result.add(new AvgSentenceBlockCohesion(simType));
            result.add(new AvgStartMiddleCohesion(simType));
            result.add(new AvgTransitionCohesion(simType));
            result.add(new StartEndCohesion(simType));
            for (DocFlowCriteria crit : DocFlowCriteria.values()) {
                result.add(new DocFlowIndex(
                        ComplexityIndecesEnum.DOC_FLOW_ABSOLUTE_POSITION_ACCURACY,
                        crit, simType,
                        DocumentFlow::getAbsolutePositionAccuracy));
                result.add(new DocFlowIndex(
                        ComplexityIndecesEnum.DOC_FLOW_ABSOLUTE_DISTANCE_ACCURACY,
                        crit, simType,
                        DocumentFlow::getAbsoluteDistanceAccuracy));
                result.add(new DocFlowIndex(
                        ComplexityIndecesEnum.DOC_FLOW_ADJACENCY_ACCURACY,
                        crit, simType,
                        DocumentFlow::getAdjacencyAccuracy));
                result.add(new DocFlowIndex(
                        ComplexityIndecesEnum.DOC_FLOW_AVERAGE_COHESION,
                        crit, simType,
                        DocumentFlow::getAverageFlowCohesion));
                result.add(new DocFlowIndex(
                        ComplexityIndecesEnum.DOC_FLOW_MAX_ORDERED_SEQUENCE,
                        crit, simType,
                        DocumentFlow::getMaxOrderedSequence));
                result.add(new DocFlowIndex(
                        ComplexityIndecesEnum.DOC_FLOW_SPEARMAN_CORRELATION,
                        crit, simType,
                        DocumentFlow::getSpearmanCorrelation));
            }
        }
        return result;
    }
}
