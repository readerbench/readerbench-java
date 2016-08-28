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

import data.Lang;
import java.util.ArrayList;
import java.util.List;
import services.complexity.ComplexityIndecesEnum;
import services.complexity.ComplexityIndecesFactory;
import services.complexity.ComplexityIndex;
import services.nlp.listOfWords.ClassesOfWords;
import services.nlp.listOfWords.Pronouns;

/**
 *
 * @author Stefan Ruseti
 */
public class SyntaxFactory implements ComplexityIndecesFactory{

    @Override
    public List<ComplexityIndex> build(Lang lang) {
        List<ComplexityIndex> result = new ArrayList<>();
        result.add(new AvgPosPerBlock(ComplexityIndecesEnum.AVERAGE_NO_NOUNS_PER_BLOCK, "NN"));
        result.add(new AvgPosPerBlock(ComplexityIndecesEnum.AVERAGE_NO_PRONOUNS_PER_BLOCK, "PR"));
        result.add(new AvgPosPerBlock(ComplexityIndecesEnum.AVERAGE_NO_VERBS_PER_BLOCK, "VB"));
        result.add(new AvgPosPerBlock(ComplexityIndecesEnum.AVERAGE_NO_ADVERBS_PER_BLOCK, "RB"));
        result.add(new AvgPosPerBlock(ComplexityIndecesEnum.AVERAGE_NO_ADJECTIVES_PER_BLOCK, "JJ"));
        result.add(new AvgPosPerBlock(ComplexityIndecesEnum.AVERAGE_NO_PREPOSITIONS_PER_BLOCK, "IN"));
        
        result.add(new AvgPosPerSentence(ComplexityIndecesEnum.AVERAGE_NO_NOUNS_PER_SENTENCE, "NN"));
        result.add(new AvgPosPerSentence(ComplexityIndecesEnum.AVERAGE_NO_PRONOUNS_PER_SENTENCE, "PR"));
        result.add(new AvgPosPerSentence(ComplexityIndecesEnum.AVERAGE_NO_VERBS_PER_SENTENCE, "VB"));
        result.add(new AvgPosPerSentence(ComplexityIndecesEnum.AVERAGE_NO_ADVERBS_PER_SENTENCE, "RB"));
        result.add(new AvgPosPerSentence(ComplexityIndecesEnum.AVERAGE_NO_ADJECTIVES_PER_SENTENCE, "JJ"));
        result.add(new AvgPosPerSentence(ComplexityIndecesEnum.AVERAGE_NO_PREPOSITIONS_PER_SENTENCE, "IN"));
        
        result.add(new AvgUniquePosPerBlock(ComplexityIndecesEnum.AVERAGE_NO_UNIQUE_NOUNS_PER_BLOCK, "NN"));
        result.add(new AvgUniquePosPerBlock(ComplexityIndecesEnum.AVERAGE_NO_UNIQUE_PRONOUNS_PER_BLOCK, "PR"));
        result.add(new AvgUniquePosPerBlock(ComplexityIndecesEnum.AVERAGE_NO_UNIQUE_VERBS_PER_BLOCK, "VB"));
        result.add(new AvgUniquePosPerBlock(ComplexityIndecesEnum.AVERAGE_NO_UNIQUE_ADVERBS_PER_BLOCK, "RB"));
        result.add(new AvgUniquePosPerBlock(ComplexityIndecesEnum.AVERAGE_NO_UNIQUE_ADJECTIVES_PER_BLOCK, "JJ"));
        result.add(new AvgUniquePosPerBlock(ComplexityIndecesEnum.AVERAGE_NO_UNIQUE_PREPOSITIONS_PER_BLOCK, "IN"));
        
        ClassesOfWords classes = Pronouns.getPronouns(lang);
        if (classes != null) {
            for (String category : classes.getClasses().keySet()) {
                result.add(new PronounsBlock(lang, classes, category));
                result.add(new PronounsSentence(lang, classes, category));
            }
        }
        
        if (lang.equals(Lang.eng) || lang.equals(Lang.fr) || lang.equals(Lang.es)) {
            result.add(new AvgTreeDepth());
            result.add(new AvgTreeSize());
            result.add(new AvgNoDependencies());
        }
        return result;
    }
    
}
