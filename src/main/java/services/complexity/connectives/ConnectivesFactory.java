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
import java.util.ArrayList;
import java.util.List;
import services.complexity.ComplexityIndecesEnum;
import services.complexity.ComplexityIndecesFactory;
import services.complexity.ComplexityIndex;
import services.nlp.listOfWords.ClassesOfWords;
import services.nlp.listOfWords.Connectives;

/**
 *
 * @author Stefan Ruseti
 */
public class ConnectivesFactory extends ComplexityIndecesFactory {

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
                    ComplexityIndecesEnum.AVERAGE_CONNECTIVES_BLOCK,
                    lang,
                    AbstractDocument::getNoBlocks,
                    className));
            result.add(new ConnectivesIndex(
                    ComplexityIndecesEnum.AVERAGE_CONNECTIVES_SENTENCE,
                    lang,
                    AbstractDocument::getNoSentences,
                    className));
        }
        return result;
    }

}
