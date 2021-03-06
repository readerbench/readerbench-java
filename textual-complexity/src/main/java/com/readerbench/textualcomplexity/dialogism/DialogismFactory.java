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
package com.readerbench.textualcomplexity.dialogism;

import com.readerbench.coreservices.data.discourse.SemanticChain;
import com.readerbench.coreservices.commons.DoubleStatistics;
import com.readerbench.datasourceprovider.pojo.Lang;
import com.readerbench.textualcomplexity.ComplexityIndex;
import com.readerbench.textualcomplexity.ComplexityIndices;
import com.readerbench.textualcomplexity.ComplexityIndicesEnum;
import com.readerbench.textualcomplexity.ComplexityIndicesFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 *
 * @author Stefan Ruseti
 */
public class DialogismFactory extends ComplexityIndicesFactory {

    private static final Function<Stream<? extends Number>, Double> SD
            = s -> s.map(x -> x.doubleValue())
            .collect(DoubleStatistics.collector())
            .getStandardDeviation(ComplexityIndices.IDENTITY);

    private static final Function<Stream<? extends Number>, Double> AVERAGE
            = s -> s.mapToDouble(x -> x.doubleValue())
            .average().orElse(ComplexityIndices.IDENTITY);

    private static final Function<Double, Double> POSITIVE = x -> (x > 0 ? 1. : 0.);

    @Override
    public List<ComplexityIndex> build(Lang lang) {
        List<ComplexityIndex> result = new ArrayList<>();
        result.add(new DialogismSynergyIndex(
                ComplexityIndicesEnum.AVERAGE_BLOCK_VOICE_CO_OCCURRENCE,
                lang,
                SemanticChain::getBlockDistribution,
                AVERAGE,
                POSITIVE));
        result.add(new DialogismSynergyIndex(
                ComplexityIndicesEnum.AVERAGE_SENTENCE_VOICE_CO_OCCURRENCE,
                lang,
                SemanticChain::getSentenceDistribution,
                AVERAGE,
                POSITIVE));
        result.add(new DialogismSynergyIndex(
                ComplexityIndicesEnum.BLOCK_VOICE_CO_OCCURRENCE_STANDARD_DEVIATION,
                lang,
                SemanticChain::getBlockDistribution,
                SD,
                POSITIVE));
        result.add(new DialogismSynergyIndex(
                ComplexityIndicesEnum.SENTENCE_VOICE_CO_OCCURRENCE_STANDARD_DEVIATION,
                lang,
                SemanticChain::getSentenceDistribution,
                SD,
                POSITIVE));
        result.add(new DialogismSynergyIndex(
                ComplexityIndicesEnum.AVERAGE_BLOCK_VOICE_CUMULATIVE_EFFECT,
                lang,
                SemanticChain::getBlockMovingAverage,
                AVERAGE));
        result.add(new DialogismSynergyIndex(
                ComplexityIndicesEnum.AVERAGE_SENTENCE_VOICE_CUMULATIVE_EFFECT,
                lang,
                SemanticChain::getSentenceDistribution,
                AVERAGE));
        result.add(new DialogismSynergyIndex(
                ComplexityIndicesEnum.BLOCK_VOICE_CUMULATIVE_EFFECT_STANDARD_DEVIATION,
                lang,
                SemanticChain::getBlockMovingAverage,
                SD));
        result.add(new DialogismSynergyIndex(
                ComplexityIndicesEnum.SENTENCE_VOICE_CUMULATIVE_EFFECT_STANDARD_DEVIATION,
                lang,
                SemanticChain::getSentenceDistribution,
                SD));
        result.add(new DialogismMutualInformationIndex(
                ComplexityIndicesEnum.AVERAGE_BLOCK_VOICE_MUTUAL_INFORMATION,
                lang,
                SemanticChain::getBlockMovingAverage,
                AVERAGE));
        result.add(new DialogismMutualInformationIndex(
                ComplexityIndicesEnum.AVERAGE_SENTENCE_VOICE_MUTUAL_INFORMATION,
                lang,
                SemanticChain::getSentenceDistribution,
                AVERAGE));
        result.add(new DialogismMutualInformationIndex(
                ComplexityIndicesEnum.BLOCK_VOICE_MUTUAL_INFORMATION_STANDARD_DEVIATION,
                lang,
                SemanticChain::getBlockMovingAverage,
                SD));
        result.add(new DialogismMutualInformationIndex(
                ComplexityIndicesEnum.SENTENCE_VOICE_MUTUAL_INFORMATION_STANDARD_DEVIATION,
                lang,
                SemanticChain::getSentenceDistribution,
                SD));

        result.add(new AvgNoVoices(lang));
        result.add(new DialogismIndex(
                ComplexityIndicesEnum.AVERAGE_VOICE_BLOCK_DISTRIBUTION,
                lang,
                SemanticChain::getAvgBlock));
        result.add(new DialogismIndex(
                ComplexityIndicesEnum.AVERAGE_VOICE_BLOCK_ENTROPY,
                lang,
                c -> c.getEntropyBlock(false)));
        result.add(new DialogismIndex(
                ComplexityIndicesEnum.AVERAGE_VOICE_RECURRENCE_BLOCK,
                lang,
                SemanticChain::getAvgRecurrenceBlock));
        result.add(new DialogismIndex(
                ComplexityIndicesEnum.AVERAGE_VOICE_RECURRENCE_SENTENCE,
                lang,
                SemanticChain::getAvgRecurrenceSentence));
        result.add(new DialogismIndex(
                ComplexityIndicesEnum.AVERAGE_VOICE_SENTENCE_DISTRIBUTION,
                lang,
                c -> c.getAvgSentence(false)));
        result.add(new DialogismIndex(
                ComplexityIndicesEnum.AVERAGE_VOICE_SENTENCE_ENTROPY,
                lang,
                SemanticChain::getEntropySentence));
        result.add(new DialogismIndex(
                ComplexityIndicesEnum.VOICE_BLOCK_DISTRIBUTION_STANDARD_DEVIATION,
                lang,
                SemanticChain::getStdevBlock));
        result.add(new DialogismIndex(
                ComplexityIndicesEnum.VOICE_RECURRENCE_BLOCK_DISTRIBUTION_STANDARD_DEVIATION,
                lang,
                SemanticChain::getStdevRecurrenceBlock));
        result.add(new DialogismIndex(
                ComplexityIndicesEnum.VOICE_RECURRENCE_SENTENCE_STANDARD_DEVIATION,
                lang,
                SemanticChain::getStdevRecurrenceSentence));
        result.add(new DialogismIndex(
                ComplexityIndicesEnum.VOICE_SENTENCE_DISTRIBUTION_STANDARD_DEVIATION,
                lang,
                c -> c.getStdevSentence(false)));
        result.add(new VoicesAvgSpan(lang));
        result.add(new VoicesMaxSpan(lang));
        return result;
    }
}
