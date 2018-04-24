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
package com.readerbench.textualcomplexity.coreference;

import com.readerbench.datasourceprovider.data.Block;
import com.readerbench.datasourceprovider.data.Sentence;
import com.readerbench.datasourceprovider.data.Word;
import edu.stanford.nlp.coref.data.CorefChain;
import edu.stanford.nlp.coref.data.Dictionaries;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.util.CoreMap;
import com.readerbench.textualcomplexity.ComplexityIndex;
import com.readerbench.textualcomplexity.ComplexityIndicesEnum;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Stefan Ruseti
 */
public abstract class CoreferenceIndex extends ComplexityIndex {

    public CoreferenceIndex(ComplexityIndicesEnum index) {
        super(index);
    }

    /**
     * Extract the data needed by CoreferenceResolution metrics
     *
     * @param blocks
     * @return
     */
    public static CoreferenceResolutionData analyse(List<Block> blocks) {
        CoreferenceResolutionData data = new CoreferenceResolutionData();
        int noWords = 0;
        int noBlocks = 0;
        int noChains = 0;
        int noEntities = 0;
        int noCoreferences = 0;
        int totalSpan = 0;
        int docLength = 0;
        int noBigSpan = 0;

        /* compute the no of words, the no of entities and the doc length */
        for (Block block : blocks) {
            if (block != null) {
                noBlocks++;
                List<Sentence> sentences = block.getSentences();

                for (Sentence s : sentences) {
                    List<Word> words = s.getAllWords();
                    noWords += words.size();

                    for (Word word : words) {
                        if (word.getNE() != null && !word.getNE().equals("O")) {
                            noEntities++;
                        }
                        docLength += word.getText().length();
                    }
                }
            }
        }
        if (noBlocks != 0) {
            docLength /= noBlocks;
        }

        /* compute nr of chains ,nr of corefs,total span and bigSpans */
        for (Block block : blocks) {
            if (block != null) {
                Map<Integer, CorefChain> coref = block.getCorefs();
                if (coref != null) {
                    for (Map.Entry<Integer, CorefChain> entry : coref
                            .entrySet()) {
                        CorefChain c = entry.getValue();
                        if (c.getMentionsInTextualOrder().size() > 1) {
                            int span = getCorefSpan(c,
                                    block.getStanfordSentences());
                            if (span >= docLength
                                    * CoreferenceResolutionData.PROPORTION) {
                                noBigSpan++;
                            }
                            totalSpan += span;
                            noCoreferences += c.getMentionsInTextualOrder()
                                    .size();
                            noChains++;
                        }
                    }
                }
            }
        }

        data.setNoChains(noChains);
        data.setNoCoreferences(noCoreferences);
        data.setNoEntities(noEntities);
        data.setNoWords(noWords);
        data.setTotalSizeOfSpan(totalSpan);
        data.setNoChainsWithBigSpan(noBigSpan);

        return data;
    }

    /**
     * *
     * computes the distance between two words from the text.Distance is defined
     * by the length of all the words between the two given ones(all words
     * contained in utterace.allWords) Ex: John bought himself a book ====>
     * distance(John,himself) = sizeof(bought) = 6
     *
     * @param c
     * @param sentences
     * @return
     */
    protected static int getCorefSpan(CorefChain c, List<CoreMap> sentences) {
        List<CorefChain.CorefMention> mentions = c.getMentionsInTextualOrder();
        CorefChain.CorefMention first = mentions.get(0);
        CorefChain.CorefMention last = mentions.get(mentions.size() - 1);

        return getDistanceBetweenMentions(sentences, first, last);
    }

    /**
     * Computes the distance between two mentions(first and last) in number of
     * characters excepting spaces. The order of the arguments first and last
     * it's important, meaning that first has to be before last in the sentence
     *
     * @param sentences
     * @param first
     * @param last
     * @return
     */
    protected static int getDistanceBetweenMentions(List<CoreMap> sentences,
            CorefChain.CorefMention first, CorefChain.CorefMention last) {

        int distance = 0;
        int sentOfFirst = first.sentNum - 1;
        int sentOfLast = last.sentNum - 1;

        if (sentOfFirst == sentOfLast) {
            List<CoreLabel> tks = sentences.get(sentOfFirst).get(
                    CoreAnnotations.TokensAnnotation.class);
            for (int i = first.endIndex - 1; i < last.startIndex - 1; i++) {
                distance += tks.get(i).get(CoreAnnotations.TextAnnotation.class).length();
            }
        } else {
            /* compute the distance in the first sentence of the first mention */
            List<CoreLabel> tks = sentences.get(sentOfFirst).get(
                    CoreAnnotations.TokensAnnotation.class);
            for (int i = first.endIndex - 1; i < tks.size(); i++) {
                distance += tks.get(i).get(CoreAnnotations.TextAnnotation.class).length();
            }
            /* compute the distance in the last sentence of the last mention */
            tks = sentences.get(sentOfLast).get(CoreAnnotations.TokensAnnotation.class);
            for (int i = 0; i < last.startIndex - 1; i++) {
                distance += tks.get(i).get(CoreAnnotations.TextAnnotation.class).length();
            }
            /**/
            for (int i = sentOfFirst + 1; i < sentOfLast; i++) {
                distance += sentences.get(i).get(CoreAnnotations.TextAnnotation.class).length();
            }
        }
        return distance;
    }

    /**
     * see getAvgInferenceDistancePerChain for inference distance definition
     *
     * @param c
     * @param sentences
     * @return inference distance
     */
    protected static float getInferenceDistance(CorefChain c,
            List<CoreMap> sentences) {

        List<CorefChain.CorefMention> mentions = c.getMentionsInTextualOrder();
        ArrayList<Integer> pronounMentions = new ArrayList<Integer>();
        int totalDistance = 0;

        for (int i = 0; i < mentions.size(); i++) {
            CorefChain.CorefMention corefMention = mentions.get(i);
            if (corefMention.mentionType == Dictionaries.MentionType.PRONOMINAL) {
                pronounMentions.add(i);
            }
        }

        for (int i = 0; i < pronounMentions.size(); i++) {
            int minDistance = Math.min(
                    searchReferentLeft(pronounMentions.get(i), mentions,
                            sentences),
                    searchReferentRight(pronounMentions.get(i), mentions,
                            sentences));
            if (minDistance == Integer.MAX_VALUE) {
                minDistance = 0;
            }
            totalDistance += minDistance;
        }

        if (pronounMentions.isEmpty()) {
            return 0;
        }
        return (float) totalDistance / pronounMentions.size();
    }

    /**
     * Searches in the coref chain the nearest right side referent.
     *
     * @param indexOfMention The mention with type PRONOMINAL
     * @param mentions
     * @param sentences
     * @return
     */
    private static int searchReferentRight(Integer indexOfMention,
            List<CorefChain.CorefMention> mentions, List<CoreMap> sentences) {

        for (int i = indexOfMention + 1; i < mentions.size(); i++) {
            CorefChain.CorefMention mention = mentions.get(i);
            if (mention.mentionType != Dictionaries.MentionType.PRONOMINAL) {
                return getDistanceBetweenMentions(sentences,
                        mentions.get(indexOfMention), mention);
            }
        }
        return Integer.MAX_VALUE;
    }

    /**
     * Searches in the coref chain the nearest left side referent
     *
     * @param indexOfMention The mention with type PRONOMINAL
     * @param mentions
     * @param sentences
     * @return
     */
    private static int searchReferentLeft(Integer indexOfMention,
            List<CorefChain.CorefMention> mentions, List<CoreMap> sentences) {

        for (int i = indexOfMention - 1; i >= 0; i--) {
            CorefChain.CorefMention mention = mentions.get(i);
            if (mention.mentionType != Dictionaries.MentionType.PRONOMINAL) {
                return getDistanceBetweenMentions(sentences, mention,
                        mentions.get(indexOfMention));
            }
        }
        return Integer.MAX_VALUE;
    }

}
