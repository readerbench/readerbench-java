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
package services.readingStrategies;

import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import data.Block;
import data.Sentence;
import data.Word;
import data.document.Summary;
import data.document.Metacognition;
import data.document.ReadingStrategyType;
import java.util.EnumMap;

public class ReadingStrategies {

    static final Logger logger = Logger.getLogger("");

    public static void detReadingStrategies(Metacognition metacognition) {
        logger.info("Identifying reading strategies from verbalization...");

        // clear references of words in initial document
        for (Block b : metacognition.getBlocks()) {
            for (Sentence s : b.getSentences()) {
                for (Word w : s.getAllWords()) {
                    w.resetReadingStrategies();
                }
                s.setAlternateText(s.getText());
            }
            b.setAlternateText(b.getText());
        }

        ParaphrasingStrategy paraphrasingStg = new ParaphrasingStrategy();
        InferredKnowledgeStrategy KIStg = new InferredKnowledgeStrategy();
        BridgingStrategy bridgingStg = new BridgingStrategy();

        int startIndex = 0;
        int endIndex;
        double threshold = bridgingStg.determineThreshold(metacognition);
        List<Sentence> prevSentences = null;

        for (int i = 0; i < metacognition.getBlocks().size(); i++) {
            Block v = metacognition.getBlocks().get(i);
            metacognition.getAutomatedRS().add(new EnumMap<>(ReadingStrategyType.class));
            // build list of previous sentences
            List<Sentence> crtSentences = new LinkedList<>();
            endIndex = v.getRefBlock().getIndex();
            for (int refBlockId = startIndex; refBlockId <= endIndex; refBlockId++) {
                for (Sentence s : metacognition.getReferredDoc().getBlocks().get(refBlockId).getSentences()) {
                    crtSentences.add(s);
                }
            }

            // afterwards causality and metacognition
            metacognition.getAutomatedRS().get(i).put(ReadingStrategyType.CAUSALITY, PatternMatching
                    .containsStrategy(crtSentences, v, ReadingStrategyType.CAUSALITY, true));
            metacognition.getAutomatedRS().get(i).put(ReadingStrategyType.META_COGNITION, PatternMatching
                    .containsStrategy(crtSentences, v, ReadingStrategyType.META_COGNITION, true));

            // in the end determine paraphrases and inferred concepts as links to previous paragraphs
            crtSentences.stream().forEach((s) -> {
                paraphrasingStg.conceptsInCommon(v, s);
            });

            int noParaphrases = 0;
            boolean isPrevParaphrase = false;
            for (Sentence s1 : v.getSentences()) {
                if (s1 != null) {
                    for (Word w1 : s1.getWords()) {
                        if (w1.getReadingStrategies().contains(ReadingStrategyType.PARAPHRASE)) {
                            if (!isPrevParaphrase) {
                                noParaphrases++;
                            }
                            isPrevParaphrase = true;
                        } else {
                            isPrevParaphrase = false;
                        }
                    }
                }
            }
            metacognition.getAutomatedRS().get(i).put(ReadingStrategyType.PARAPHRASE, noParaphrases);

            if (prevSentences == null) {
                prevSentences = crtSentences;
            } else {
                prevSentences.addAll(crtSentences);
            }

            // check to all previous sentences
            metacognition.getAutomatedRS().get(i).put(ReadingStrategyType.INFERRED_KNOWLEDGE, KIStg.getInferredConcepts(v, prevSentences));

            metacognition.getAutomatedRS().get(i).put(ReadingStrategyType.BRIDGING, bridgingStg.containsStrategy(v, prevSentences, threshold));

            metacognition.getAutomatedRS().get(i).put(ReadingStrategyType.TEXT_BASED_INFERENCES, metacognition.getAutomatedRS().get(i).get(ReadingStrategyType.BRIDGING) + metacognition.getAutomatedRS().get(i).get(ReadingStrategyType.CAUSALITY));

            startIndex = endIndex + 1;
            prevSentences = crtSentences;
        }

        // clear references of words in initial document
        for (Block b : metacognition.getReferredDoc().getBlocks()) {
            for (Sentence s : b.getSentences()) {
                for (Word w : s.getAllWords()) {
                    w.resetReadingStrategies();
                }
                s.setAlternateText(s.getText());
            }
        }
    }

    public static void detReadingStrategies(Summary essay) {
        logger.info("Identifying reading strategies from summary ...");

        // clear references of words in initial document
        for (Block b : essay.getBlocks()) {
            for (Sentence s : b.getSentences()) {
                for (Word w : s.getAllWords()) {
                    w.resetReadingStrategies();
                }
                s.setAlternateText(s.getText());
            }
            b.setAlternateText(b.getText());
        }

        ParaphrasingStrategy paraphrasingStg = new ParaphrasingStrategy();
        InferredKnowledgeStrategy KIStg = new InferredKnowledgeStrategy();
        BridgingStrategy bridgingStg = new BridgingStrategy();

        List<Sentence> originalSentences = new LinkedList<>();
        for (Block b : essay.getReferredDoc().getBlocks()) {
            for (Sentence s : b.getSentences()) {
                originalSentences.add(s);
            }
        }

        essay.getAutomatedRS().add(new EnumMap<>(ReadingStrategyType.class));
        for (ReadingStrategyType rs : ReadingStrategyType.values()) {
            essay.getAutomatedRS().get(0).put(rs, 0);
        }

        int causality = 0, metacog = 0, paraphrase = 0, KI = 0;
        for (int i = 0; i < essay.getBlocks().size(); i++) {
            Block e = essay.getBlocks().get(i);

            // causality and metacognition
            causality += PatternMatching.containsStrategy(originalSentences, e, ReadingStrategyType.CAUSALITY, false);
            metacog += PatternMatching.containsStrategy(originalSentences, e, ReadingStrategyType.META_COGNITION, false);

            // paraphrases and inferred concepts
            originalSentences.stream().forEach((s) -> {
                paraphrasingStg.conceptsInCommon(e, s);
            });

            boolean isPrevParaphrase = false;
            for (Sentence s1 : e.getSentences()) {
                if (s1 != null) {
                    for (Word w1 : s1.getWords()) {
                        if (w1.getReadingStrategies().contains(ReadingStrategyType.PARAPHRASE)) {
                            if (!isPrevParaphrase) {
                                paraphrase++;
                            }
                            isPrevParaphrase = true;
                        } else {
                            isPrevParaphrase = false;
                        }
                    }
                }
            }

            KI += KIStg.getInferredConcepts(e, originalSentences);
        }

        essay.getAutomatedRS().get(0).put(ReadingStrategyType.CAUSALITY, causality);
        essay.getAutomatedRS().get(0).put(ReadingStrategyType.META_COGNITION, metacog);
        essay.getAutomatedRS().get(0).put(ReadingStrategyType.PARAPHRASE, paraphrase);
        essay.getAutomatedRS().get(0).put(ReadingStrategyType.INFERRED_KNOWLEDGE, KI);
        // bridging
        essay.getAutomatedRS().get(0).put(ReadingStrategyType.BRIDGING, bridgingStg.containsStrategy(essay, originalSentences));

        essay.getAutomatedRS().get(0).put(ReadingStrategyType.TEXT_BASED_INFERENCES, essay.getAutomatedRS().get(0).get(ReadingStrategyType.BRIDGING) + essay.getAutomatedRS().get(0).get(ReadingStrategyType.CAUSALITY));

        // clear references of words in initial document
        for (Block b : essay.getReferredDoc().getBlocks()) {
            for (Sentence s : b.getSentences()) {
                for (Word w : s.getAllWords()) {
                    w.resetReadingStrategies();
                }
                s.setAlternateText(s.getText());
            }
        }
    }
}
