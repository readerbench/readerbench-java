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
import data.Block;
import data.Sentence;
import data.Word;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import services.complexity.ComplexityIndecesEnum;
import services.complexity.ComplexityIndex;
import services.complexity.ComplexityIndices;
import services.commons.DoubleStatistics;

/**
 *
 * @author Stefan Ruseti
 */
public class WordEntropy extends ComplexityIndex {

    public WordEntropy() {
        super(ComplexityIndecesEnum.WORD_ENTROPY);
    }

    @Override
    public double compute(AbstractDocument d) {
        Map<String, Long> occurences = d.getBlocks().parallelStream()
                .filter(b -> b != null)
                .flatMap(b -> b.getSentences().stream()
                        .flatMap(s -> s.getAllWords().stream().map(Word::getStem)))
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
        long no = occurences.values().stream().mapToLong(x -> x).sum();
        return occurences.values().parallelStream()
                .mapToDouble(x -> ((double) x) / no)
                .map(x -> -x * Math.log(x))
                .sum();
	}

}

