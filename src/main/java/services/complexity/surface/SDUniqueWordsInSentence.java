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
package services.complexity.surface;

import data.AbstractDocument;
import services.complexity.ComplexityIndecesEnum;
import services.complexity.ComplexityIndex;
import services.complexity.ComplexityIndices;
import services.commons.DoubleStatistics;

/**
 *
 * @author Stefan Ruseti
 */
public class SDUniqueWordsInSentence extends ComplexityIndex {

    public SDUniqueWordsInSentence() {
        super(ComplexityIndecesEnum.SENTENCE_STANDARD_DEVIATION_NO_UNIQUE_WORDS);
    }

    @Override
    public double compute(AbstractDocument d) {
        return d.getSentencesInDocument().parallelStream()
                .map(s -> s.getWordOccurences().keySet().size() * 1.)
                .collect(DoubleStatistics.collector())
                .getStandardDeviation(ComplexityIndices.IDENTITY);
    }

}

