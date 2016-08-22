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
package services.complexity.connectives;

import data.AbstractDocument;
import data.Lang;
import java.util.function.Function;
import services.complexity.ComplexityIndecesEnum;
import services.complexity.ComplexityIndex;
import services.complexity.ComplexityIndices;
import services.nlp.listOfWords.ClassesOfWords;
import services.nlp.listOfWords.Connectives;

/**
 *
 * @author Stefan Ruseti
 */
public class ConnectivesIndex extends ComplexityIndex {

    protected final ClassesOfWords connectives;
    private Function<AbstractDocument, Integer> countFunction;

    public ConnectivesIndex(
            ComplexityIndecesEnum index,
            Lang lang,
            Function<AbstractDocument, Integer> countFunction,
            String connective) {
        super(index, lang, null, connective);
        this.countFunction = countFunction;
        switch (lang) {
            case eng:
                connectives = Connectives.CONNECTIVES_EN;
                break;
            case fr:
                connectives = Connectives.CONNECTIVES_FR;
                break;
            case ro:
                connectives = Connectives.CONNECTIVES_RO;
                break;
            case nl:
                connectives = Connectives.CONNECTIVES_NL;
                break;
            case la:
                connectives = Connectives.CONNECTIVES_LA;
                break;
            default:
                connectives = null;
                break;
        }
    }

    @Override
    public double compute(AbstractDocument d) {
        int occurances = connectives.countCategoryOccurrences(d, param);
        int n = countFunction.apply(d);
        return (n != 0) ? (1. * occurances / n) : ComplexityIndices.IDENTITY;
    }

}
