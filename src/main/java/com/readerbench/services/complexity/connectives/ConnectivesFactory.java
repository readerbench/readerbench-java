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
package com.readerbench.services.complexity.connectives;

import com.readerbench.data.AbstractDocument;
import com.readerbench.data.Lang;
import com.readerbench.services.complexity.ComplexityIndex;
import com.readerbench.services.complexity.ComplexityIndicesEnum;
import com.readerbench.services.complexity.ComplexityIndicesFactory;
import com.readerbench.services.nlp.listOfWords.ClassesOfWords;
import com.readerbench.services.nlp.listOfWords.Connectives;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Stefan Ruseti
 */
public class ConnectivesFactory extends ComplexityIndicesFactory {

    @Override
    public List<ComplexityIndex> build(Lang lang) {
        List<ComplexityIndex> result = new ArrayList<>();
        ClassesOfWords connectives;
        connectives = Connectives.getConnectives(lang);
        if (connectives == null) {
            return result;
        }
        for (String className : connectives.getClasses().keySet()) {
            result.add(new ConnectivesIndex(
                    ComplexityIndicesEnum.AVERAGE_CONNECTIVES_BLOCK,
                    lang,
                    AbstractDocument::getNoBlocks,
                    className));
            result.add(new ConnectivesIndex(
                    ComplexityIndicesEnum.AVERAGE_CONNECTIVES_SENTENCE,
                    lang,
                    AbstractDocument::getNoSentences,
                    className));
        }
        return result;
    }

}
