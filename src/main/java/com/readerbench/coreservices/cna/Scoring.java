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
package com.readerbench.coreservices.cna;

import com.readerbench.datasourceprovider.data.keywordmining.Keyword;
import com.readerbench.datasourceprovider.data.AbstractDocument;
import com.readerbench.datasourceprovider.data.Block;
import com.readerbench.datasourceprovider.data.Sentence;
import com.readerbench.datasourceprovider.data.Word;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Mihai Dascalu
 */
public class Scoring {

    private static final Logger LOGGER = LoggerFactory.getLogger(Scoring.class);

    private static double determineIndividualScore(Sentence s, AbstractDocument d) {
        if (s != null && !s.getWords().isEmpty()) {
            // determine cumulative word importance in terms of topics coverage
            double importance = 0;
            for (Word w : s.getWordOccurences().keySet()) {
                Keyword t = new Keyword(w, 0);
                int index = d.getTopics().indexOf(t);
                if (index >= 0) {
                    Keyword coveredTopic = d.getTopics().get(index);
                    double tf = s.getWordOccurences().get(w);
                    importance += (1 + Math.log(tf)) * coveredTopic.getRelevance();
                }
            }
            return importance;
        }
        return 0;
    }

    public static void score(AbstractDocument d) {
        // custom build scores for blocks and for the document as a whole using a bottom-up approach
        LOGGER.info("Scoring document > blocks > sentences");

        for (int i = 0; i < d.getBlocks().size(); i++) {
            Block b = d.getBlocks().get(i);
            if (b != null) {
                // determine overall scores for each utterance
                for (int j = 0; j < b.getSentences().size(); j++) {
                    Sentence s = b.getSentences().get(j);
                    if (s != null) {
                        s.setScore(determineIndividualScore(s, d));
                    }
                }
            }
        }

        // determine overall scores for each utterance in terms of intra-block relations
        for (int i = 0; i < d.getBlocks().size(); i++) {
            Block b = d.getBlocks().get(i);
            if (b != null) {
                // determine block individual score
                double blockScore = 0;
                // use weighted values
                for (int j = 0; j < b.getSentences().size(); j++) {
                    Sentence s = b.getSentences().get(j);
                    if (s != null) {
                        blockScore += s.getScore() * b.getSentenceBlockDistances()[j].getCohesion();
                    }
                }
                b.setScore(blockScore);
            }
        }

        // determine document score
        double documentScore = 0;
        for (int i = 0; i < d.getBlocks().size(); i++) {
            Block b = d.getBlocks().get(i);
            if (b != null) {
                documentScore += b.getScore() * d.getBlockDocDistances()[i].getCohesion();
            }
        }
        d.setScore(documentScore);
    }
}
