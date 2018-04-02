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
package com.readerbench.textualcomplexity.syntax;

import com.readerbench.data.AbstractDocument;
import com.readerbench.data.Lang;
import com.readerbench.textualcomplexity.ComplexityIndex;
import com.readerbench.textualcomplexity.ComplexityIndices;
import com.readerbench.textualcomplexity.ComplexityIndicesEnum;
import com.readerbench.coreservices.nlp.listOfWords.ClassesOfWords;

/**
 *
 * @author Stefan Ruseti
 */
public class PronounsSentence extends ComplexityIndex{
    
    private final ClassesOfWords classes;
    
    public PronounsSentence(Lang lang, ClassesOfWords classes, String category) {
        super(ComplexityIndicesEnum.AVERAGE_PRONOUNS_SENTENCE, lang, null, category);
        this.classes = classes;
    }

    @Override
    public double compute(AbstractDocument d) {
        int sum = classes.countCategoryOccurrences(d, param);
        if (d.getNoSentences()!= 0) return 1. * sum / d.getNoSentences();
        return ComplexityIndices.IDENTITY;
    }
    
}
