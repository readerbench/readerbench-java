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
package com.readerbench.services.discourse.cohesion;

import com.readerbench.data.AbstractDocument;
import com.readerbench.data.Block;
import com.readerbench.data.Sentence;
import com.readerbench.data.Word;

import java.io.Serializable;
import java.util.Iterator;

public class LexicalCohesion implements Serializable {

    private static final long serialVersionUID = -2519627724570229014L;

    /**
     * Cohesion between two utterances is measured as being : c = 1/distance
     * where distance(s1, s2) = SUM<w1 in s1>(MIN<w2 in s2>(dist(w1, w2)))
     * @param s
     */
    public static double getIntraSentenceCohesion(Sentence s) {
        double distSum = 0;
        for (int i = 0; i < s.getWords().size() - 1; i++) {
            double minDist = Double.MAX_VALUE;
            for (int j = i + 1; j < s.getWords().size(); j++) {
                double d = s.getWords().get(i)
                        .getDistanceInChain(s.getWords().get(j));
                if (d < minDist) {
                    minDist = d;
                }
            }
            if (minDist != Double.MAX_VALUE) {
                distSum += minDist;
            }
        }
        double cohesion = (s.getWords().size() > 0 ? distSum
                / s.getWords().size() : 0);
        System.out.println("Intra-utterance cohesion "
                + s.getContainer().getIndex() + "/" + s.getIndex() + ": "
                + cohesion);
        return cohesion;
    }

    /**
     * Cohesion between two utterances is measured as being : c = 1/distance
     * where distance(s1, s2) = SUM<w1 in s1>(MIN<w2 in s2>(dist(w1, w2)))
     * @param s1
     * @param s2
     */
    public static double getInterSentenceCohesion(Sentence s1, Sentence s2) {
        double distSum = 0;
        for (Word word1 : s1.getWords()) {
            double minDist = Double.MAX_VALUE;
            for (Word word2 : s2.getWords()) {
                double d = word1.getDistanceInChain(word2);
                if (d < minDist) {
                    minDist = d;
                }
            }
            if (minDist != Double.MAX_VALUE) {
                distSum += minDist;
            }
        }
        double cohesion = (Math.min(s1.getWords().size(), s2.getWords().size()) > 0 ? distSum
                / Math.min(s1.getWords().size(), s2.getWords().size())
                : 0);
        System.out.println("Inter-utterance cohesion "
                + s1.getContainer().getIndex() + "(" + s1.getIndex() + "-"
                + s2.getIndex() + "): " + cohesion);
        return cohesion;
    }

    /**
     * Cohesion within a block measured as the mean cohesion of adjacent
     * sentences.
     * @param b
     */
    public static double getBlockCohesionAdjacentUtterances(Block b) {
        double cohesionSum = 0;
        Iterator<Sentence> it1 = b.getSentences().iterator();
        Iterator<Sentence> it2 = b.getSentences().iterator();
        // second iterator starts from second sentence
        if (it2.hasNext()) {
            it2.next();
        }
        while (it2.hasNext()) {
            Sentence s1 = it1.next();
            Sentence s2 = it2.next();
            cohesionSum += getInterSentenceCohesion(s1, s2);
        }
        double denominator = b.getSentences().size() - 1;
        double cohesion = (denominator > 0 ? cohesionSum / denominator : 0);
        return cohesion;
    }

    public static double getBlockCohesion(Block b) {
        double interCohesionSum = 0;
        double intraCohesionSum = 0;
        for (int i = 0; i < b.getSentences().size() - 1; i++) {
            for (int j = i + 1; j < b.getSentences().size(); j++) {
                interCohesionSum += getInterSentenceCohesion(b.getSentences()
                        .get(i), b.getSentences().get(j));
            }
        }

        for (int i = 0; i < b.getSentences().size(); i++) {
            intraCohesionSum += getIntraSentenceCohesion(b.getSentences().get(
                    i));
        }

        // add intra with inter-cohesion between utterances
        double denominator = b.getSentences().size() - 1;
        double cohesion = (denominator > 0 ? interCohesionSum / denominator : 0);

        denominator = b.getSentences().size();
        cohesion += (denominator > 0 ? intraCohesionSum / denominator : 0);
        return cohesion;
    }

    /**
     * Document cohesion computed as the mean of block cohesion calculated as
     * cohesion between adjacent sentences.
     * @param d
     */
    public static double getDocumentCohesion(AbstractDocument d) {
        double cohesionSum = 0;
        for (Block b : d.getBlocks()) {
            if (b != null) {
                // cohesionSum += getBlockCohesionAdjacentUtterances(b);
                cohesionSum += getBlockCohesion(b);
            }
        }
        double cohesion = (d.getBlocks().size() > 0 ? cohesionSum
                / (double) d.getBlocks().size() : 0);
        return cohesion;
    }

}
