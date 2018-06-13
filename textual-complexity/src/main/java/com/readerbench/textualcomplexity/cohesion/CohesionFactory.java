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
package com.readerbench.textualcomplexity.cohesion;

import com.readerbench.coreservices.semanticmodels.SimilarityType;
import com.readerbench.datasourceprovider.pojo.Lang;
import com.readerbench.textualcomplexity.ComplexityIndex;
import com.readerbench.textualcomplexity.ComplexityIndicesEnum;
import com.readerbench.textualcomplexity.ComplexityIndicesFactory;
import com.readerbench.textualcomplexity.cohesion.discourse.AvgScore;
import com.readerbench.textualcomplexity.cohesion.discourse.ScoreSD;
import com.readerbench.textualcomplexity.cohesion.flow.DocFlowCriteria;
import com.readerbench.textualcomplexity.cohesion.flow.DocFlowIndex;
import com.readerbench.textualcomplexity.cohesion.flow.DocumentFlow;
import com.readerbench.textualcomplexity.cohesion.lexicalChains.AvgLexicalChainsPerBlock;
import com.readerbench.textualcomplexity.cohesion.lexicalChains.LexicalChainsAvgSpan;
import com.readerbench.textualcomplexity.cohesion.lexicalChains.LexicalChainsCoverage;
import com.readerbench.textualcomplexity.cohesion.lexicalChains.LexicalChainsMaxSpan;
import com.readerbench.textualcomplexity.IndexLevel;
import com.readerbench.textualcomplexity.cohesion.semantic.AvgBlockAdjacencyCohesion;
import com.readerbench.textualcomplexity.cohesion.semantic.AvgBlockDocCohesion;
import com.readerbench.textualcomplexity.cohesion.semantic.AvgInterBlockCohesion;
import com.readerbench.textualcomplexity.cohesion.semantic.AvgIntraBlockCohesion;
import com.readerbench.textualcomplexity.cohesion.semantic.AvgMiddleEndCohesion;
import com.readerbench.textualcomplexity.cohesion.semantic.AvgSentenceAdjacencyCohesion;
import com.readerbench.textualcomplexity.cohesion.semantic.AvgSentenceBlockCohesion;
import com.readerbench.textualcomplexity.cohesion.semantic.AvgStartMiddleCohesion;
import com.readerbench.textualcomplexity.cohesion.semantic.AvgTransitionCohesion;
import com.readerbench.textualcomplexity.cohesion.semantic.StartEndCohesion;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Stefan Ruseti
 */
public class CohesionFactory extends ComplexityIndicesFactory {

    @Override
    public List<ComplexityIndex> build(Lang lang) {
        List<ComplexityIndex> result = new ArrayList<>();
        result.add(new LexicalChainsAvgSpan(lang));
        result.add(new LexicalChainsMaxSpan(lang));
        result.add(new AvgLexicalChainsPerBlock(lang));
        result.add(new LexicalChainsCoverage(lang));
        result.add(new AvgScore(ComplexityIndicesEnum.AVERAGE_BLOCK_SCORE, lang, IndexLevel.BLOCK));
        result.add(new AvgScore(ComplexityIndicesEnum.AVERAGE_SENTENCE_SCORE, lang, IndexLevel.SENTENCE));
        result.add(new ScoreSD(ComplexityIndicesEnum.BLOCK_SCORE_STANDARD_DEVIATION, lang, IndexLevel.BLOCK));
        result.add(new ScoreSD(ComplexityIndicesEnum.SENTENCE_SCORE_STANDARD_DEVIATION, lang, IndexLevel.SENTENCE));

        for (SimilarityType simType : SimilarityType.values()) {
            result.add(new AvgBlockAdjacencyCohesion(simType, lang));
            result.add(new AvgBlockDocCohesion(simType, lang));
            result.add(new AvgInterBlockCohesion(simType, lang));
            result.add(new AvgIntraBlockCohesion(simType, lang));
            result.add(new AvgMiddleEndCohesion(simType, lang));
            result.add(new AvgSentenceAdjacencyCohesion(simType, lang));
            result.add(new AvgSentenceBlockCohesion(simType, lang));
            result.add(new AvgStartMiddleCohesion(simType, lang));
            result.add(new AvgTransitionCohesion(simType, lang));
            result.add(new StartEndCohesion(simType, lang));
            for (DocFlowCriteria crit : DocFlowCriteria.values()) {
                result.add(new DocFlowIndex(
                        ComplexityIndicesEnum.DOC_FLOW_ABSOLUTE_POSITION_ACCURACY,
                        lang, crit, simType,
                        DocumentFlow::getAbsolutePositionAccuracy));
                result.add(new DocFlowIndex(
                        ComplexityIndicesEnum.DOC_FLOW_ABSOLUTE_DISTANCE_ACCURACY,
                        lang, crit, simType,
                        DocumentFlow::getAbsoluteDistanceAccuracy));
                result.add(new DocFlowIndex(
                        ComplexityIndicesEnum.DOC_FLOW_ADJACENCY_ACCURACY,
                        lang, crit, simType,
                        DocumentFlow::getAdjacencyAccuracy));
                result.add(new DocFlowIndex(
                        ComplexityIndicesEnum.DOC_FLOW_AVERAGE_COHESION,
                        lang, crit, simType,
                        DocumentFlow::getAverageFlowCohesion));
                result.add(new DocFlowIndex(
                        ComplexityIndicesEnum.DOC_FLOW_MAX_ORDERED_SEQUENCE,
                        lang, crit, simType,
                        DocumentFlow::getMaxOrderedSequence));
                result.add(new DocFlowIndex(
                        ComplexityIndicesEnum.DOC_FLOW_SPEARMAN_CORRELATION,
                        lang, crit, simType,
                        DocumentFlow::getSpearmanCorrelation));
            }
        }
        return result;
    }
}
