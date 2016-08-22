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
package services.complexity.syntax;

import data.AbstractDocument;
import data.Word;
import java.util.stream.Collectors;
import services.complexity.ComplexityIndecesEnum;
import services.complexity.ComplexityIndex;
import services.complexity.ComplexityIndices;

/**
 *
 * @author Stefan Ruseti
 */
public class AvgUniquePosPerBlock extends ComplexityIndex {

    private final String pos;

    public AvgUniquePosPerBlock(ComplexityIndecesEnum index, String pos) {
        super(index);
        this.pos = pos;
    }

    @Override
    public double compute(AbstractDocument d) {
        return d.getBlocks().parallelStream()
                .filter(b -> b != null)
                .mapToInt(b -> b.getSentences().stream()
                        .flatMap(s -> s.getAllWords().stream())
                        .filter(w -> w.getPOS() != null && w.getPOS().contains(pos))
                        .map(Word::getLemma)
                        .collect(Collectors.toSet()).size())
                .average().orElse(ComplexityIndices.IDENTITY);
    }

}