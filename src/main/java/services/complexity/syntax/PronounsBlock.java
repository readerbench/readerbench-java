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
import data.Lang;
import services.complexity.ComplexityIndecesEnum;
import services.complexity.ComplexityIndex;
import services.complexity.ComplexityIndices;
import services.nlp.listOfWords.ClassesOfWords;

/**
 *
 * @author Stefan Ruseti
 */
public class PronounsBlock extends ComplexityIndex{
    
    private final ClassesOfWords classes;
    
    public PronounsBlock(Lang lang, ClassesOfWords classes, String category) {
        super(ComplexityIndecesEnum.PRONOUNS_BLOCK, lang, null, category);
        this.classes = classes;
    }

    @Override
    public double compute(AbstractDocument d) {
        int sum = classes.countCategoryOccurrences(d, param);
        if (d.getNoBlocks() != 0) return 1. * sum / d.getNoBlocks();
        return ComplexityIndices.IDENTITY;
    }
    
}