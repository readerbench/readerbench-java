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
package services.complexity.dialogism;

import data.Lang;
import data.discourse.SemanticChain;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;
import services.complexity.ComplexityIndecesEnum;
import services.complexity.ComplexityIndecesFactory;
import services.complexity.ComplexityIndex;
import services.complexity.ComplexityIndices;
import utils.DoubleStatistics;

/**
 *
 * @author Stefan Ruseti
 */
public class DialogismFactory implements ComplexityIndecesFactory {

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
                ComplexityIndecesEnum.AVERAGE_BLOCK_VOICE_CO_OCCURRENCE, 
                SemanticChain::getBlockDistribution, 
                AVERAGE,
                POSITIVE));
        result.add(new DialogismSynergyIndex(
                ComplexityIndecesEnum.AVERAGE_SENTENCE_VOICE_CO_OCCURRENCE, 
                SemanticChain::getSentenceDistribution, 
                AVERAGE,
                POSITIVE));
        result.add(new DialogismSynergyIndex(
                ComplexityIndecesEnum.BLOCK_VOICE_CO_OCCURRENCE_STANDARD_DEVIATION, 
                SemanticChain::getBlockDistribution, 
                SD,
                POSITIVE));
        result.add(new DialogismSynergyIndex(
                ComplexityIndecesEnum.SENTENCE_VOICE_CO_OCCURRENCE_STANDARD_DEVIATION, 
                SemanticChain::getSentenceDistribution, 
                SD,
                POSITIVE));
        result.add(new DialogismSynergyIndex(
                ComplexityIndecesEnum.AVERAGE_BLOCK_VOICE_CUMULATIVE_EFFECT, 
                SemanticChain::getBlockMovingAverage, 
                AVERAGE));
        result.add(new DialogismSynergyIndex(
                ComplexityIndecesEnum.AVERAGE_SENTENCE_VOICE_CUMULATIVE_EFFECT, 
                SemanticChain::getSentenceDistribution, 
                AVERAGE));
        result.add(new DialogismSynergyIndex(
                ComplexityIndecesEnum.BLOCK_VOICE_CUMULATIVE_EFFECT_STANDARD_DEVIATION, 
                SemanticChain::getBlockMovingAverage, 
                SD));
        result.add(new DialogismSynergyIndex(
                ComplexityIndecesEnum.SENTENCE_VOICE_CUMULATIVE_EFFECT_STANDARD_DEVIATION, 
                SemanticChain::getSentenceDistribution, 
                SD));
        result.add(new DialogismMutualInformationIndex(
                ComplexityIndecesEnum.AVERAGE_BLOCK_VOICE_MUTUAL_INFORMATION, 
                SemanticChain::getBlockMovingAverage, 
                AVERAGE));
        result.add(new DialogismMutualInformationIndex(
                ComplexityIndecesEnum.AVERAGE_SENTENCE_VOICE_MUTUAL_INFORMATION, 
                SemanticChain::getSentenceDistribution, 
                AVERAGE));
        result.add(new DialogismMutualInformationIndex(
                ComplexityIndecesEnum.BLOCK_VOICE_MUTUAL_INFORMATION_STANDARD_DEVIATION, 
                SemanticChain::getBlockMovingAverage, 
                SD));
        result.add(new DialogismMutualInformationIndex(
                ComplexityIndecesEnum.SENTENCE_VOICE_MUTUAL_INFORMATION_STANDARD_DEVIATION, 
                SemanticChain::getSentenceDistribution, 
                SD));
        
        result.add(new AvgNoVoices());
        result.add(new DialogismIndex(
                ComplexityIndecesEnum.AVERAGE_VOICE_BLOCK_DISTRIBUTION,
                SemanticChain::getAvgBlock));
        result.add(new DialogismIndex(
                ComplexityIndecesEnum.AVERAGE_VOICE_BLOCK_ENTROPY,
                c -> c.getEntropyBlock(false)));
        result.add(new DialogismIndex(
                ComplexityIndecesEnum.AVERAGE_VOICE_RECURRENCE_BLOCK,
                SemanticChain::getAvgRecurrenceBlock));
        result.add(new DialogismIndex(
                ComplexityIndecesEnum.AVERAGE_VOICE_RECURRENCE_SENTENCE,
                SemanticChain::getAvgRecurrenceSentence));
        result.add(new DialogismIndex(
                ComplexityIndecesEnum.AVERAGE_VOICE_SENTENCE_DISTRIBUTION,
                c -> c.getAvgSentence(false)));
        result.add(new DialogismIndex(
                ComplexityIndecesEnum.AVERAGE_VOICE_SENTENCE_ENTROPY,
                SemanticChain::getEntropySentence));
        result.add(new DialogismIndex(
                ComplexityIndecesEnum.VOICE_BLOCK_DISTRIBUTION_STANDARD_DEVIATION,
                SemanticChain::getStdevBlock));
        result.add(new DialogismIndex(
                ComplexityIndecesEnum.VOICE_RECURRENCE_BLOCK_DISTRIBUTION_STANDARD_DEVIATION,
                SemanticChain::getStdevRecurrenceBlock));
        result.add(new DialogismIndex(
                ComplexityIndecesEnum.VOICE_RECURRENCE_SENTENCE_STANDARD_DEVIATION,
                SemanticChain::getStdevRecurrenceSentence));
        result.add(new DialogismIndex(
                ComplexityIndecesEnum.VOICE_SENTENCE_DISTRIBUTION_STANDARD_DEVIATION,
                c -> c.getStdevSentence(false)));
        result.add(new VoicesAvgSpan());
        result.add(new VoicesMaxSpan());
        return result;
    }
    
}