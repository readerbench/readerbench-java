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

import com.readerbench.coreservices.nlp.wordlists.ClassesOfWords;
import com.readerbench.coreservices.nlp.wordlists.Pronouns;
import com.readerbench.datasourceprovider.pojo.Lang;
import com.readerbench.textualcomplexity.ComplexityIndex;
import com.readerbench.textualcomplexity.ComplexityIndicesEnum;
import com.readerbench.textualcomplexity.ComplexityIndicesFactory;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Stefan Ruseti
 */
public class SyntaxFactory extends ComplexityIndicesFactory {

    @Override
    public List<ComplexityIndex> build(Lang lang) {
        List<ComplexityIndex> result = new ArrayList<>();
        result.add(new AvgPosPerBlock(ComplexityIndicesEnum.AVERAGE_NO_NOUNS_PER_BLOCK, "NN", lang));
        result.add(new AvgPosPerBlock(ComplexityIndicesEnum.AVERAGE_NO_PRONOUNS_PER_BLOCK, "PR", lang));
        result.add(new AvgPosPerBlock(ComplexityIndicesEnum.AVERAGE_NO_VERBS_PER_BLOCK, "VB", lang));
        result.add(new AvgPosPerBlock(ComplexityIndicesEnum.AVERAGE_NO_ADVERBS_PER_BLOCK, "RB", lang));
        result.add(new AvgPosPerBlock(ComplexityIndicesEnum.AVERAGE_NO_ADJECTIVES_PER_BLOCK, "JJ", lang));
        result.add(new AvgPosPerBlock(ComplexityIndicesEnum.AVERAGE_NO_PREPOSITIONS_PER_BLOCK, "IN", lang));

        result.add(new AvgPosPerSentence(ComplexityIndicesEnum.AVERAGE_NO_NOUNS_PER_SENTENCE, "NN", lang));
        result.add(new AvgPosPerSentence(ComplexityIndicesEnum.AVERAGE_NO_PRONOUNS_PER_SENTENCE, "PR", lang));
        result.add(new AvgPosPerSentence(ComplexityIndicesEnum.AVERAGE_NO_VERBS_PER_SENTENCE, "VB", lang));
        result.add(new AvgPosPerSentence(ComplexityIndicesEnum.AVERAGE_NO_ADVERBS_PER_SENTENCE, "RB", lang));
        result.add(new AvgPosPerSentence(ComplexityIndicesEnum.AVERAGE_NO_ADJECTIVES_PER_SENTENCE, "JJ", lang));
        result.add(new AvgPosPerSentence(ComplexityIndicesEnum.AVERAGE_NO_PREPOSITIONS_PER_SENTENCE, "IN", lang));

        result.add(new AvgUniquePosPerBlock(ComplexityIndicesEnum.AVERAGE_NO_UNIQUE_NOUNS_PER_BLOCK, "NN", lang));
        result.add(new AvgUniquePosPerBlock(ComplexityIndicesEnum.AVERAGE_NO_UNIQUE_PRONOUNS_PER_BLOCK, "PR", lang));
        result.add(new AvgUniquePosPerBlock(ComplexityIndicesEnum.AVERAGE_NO_UNIQUE_VERBS_PER_BLOCK, "VB", lang));
        result.add(new AvgUniquePosPerBlock(ComplexityIndicesEnum.AVERAGE_NO_UNIQUE_ADVERBS_PER_BLOCK, "RB", lang));
        result.add(new AvgUniquePosPerBlock(ComplexityIndicesEnum.AVERAGE_NO_UNIQUE_ADJECTIVES_PER_BLOCK, "JJ", lang));
        result.add(new AvgUniquePosPerBlock(ComplexityIndicesEnum.AVERAGE_NO_UNIQUE_PREPOSITIONS_PER_BLOCK, "IN", lang));

        ClassesOfWords classes = Pronouns.getPronouns(lang);
        if (classes != null) {
            for (String category : classes.getClasses().keySet()) {
                result.add(new PronounsBlock(lang, classes, category));
                result.add(new PronounsSentence(lang, classes, category));
            }
        }

        if (lang.equals(Lang.en) || lang.equals(Lang.fr) || lang.equals(Lang.es)) {
            result.add(new AvgNoDependencies());
        }
        return result;
    }

}
