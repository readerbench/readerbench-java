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
import com.readerbench.data.discourse.SemanticCohesion;
import com.readerbench.services.commons.DoubleStatistics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 *
 * @author Mihai Dascalu
 */
public class CohesionGraph {

    private static final Logger LOGGER = LoggerFactory.getLogger(CohesionGraph.class);

    /**
     * Build the cohesion graph of a document.
     *
     * @param d the document for which to build the cohesion graph
     */
    public static void buildCohesionGraph(AbstractDocument d) {

        LOGGER.info("Building cohesion graph...");

        // determine block-document semantic cohesion
        // initialize semantic cohesion vector for the semantic cohesion of (block, document) pairs
        d.setBlockDocDistances(new SemanticCohesion[d.getBlocks().size()]);
        // iterate through all blocks of the document
        for (int i = 0; i < d.getBlocks().size(); i++) {
            if (d.getBlocks().get(i) != null) {
                // set semantic cohesion between the block and the document
                d.getBlockDocDistances()[i] = new SemanticCohesion(d.getBlocks().get(i), d);
            }
        }

        // auxiliary variables used to compute mean and standard deviation of semantic cohesion
        double s0 = 0, s1 = 0, s2 = 0, avg = 0, stdev = 0;

        // determine inner-block semantic cohesion
        // initialize semantic cohesion arrays for the semantic cohesion of (block, block) pairs
        d.setBlockDistances(new SemanticCohesion[d.getBlocks().size()][d.getBlocks().size()]);
        d.setPrunnedBlockDistances(new SemanticCohesion[d.getBlocks().size()][d.getBlocks().size()]);

        Map<Block, Integer> inverseIndex = IntStream.range(0, d.getBlocks().size())
                .filter(i -> d.getBlocks().get(i) != null)
                .mapToObj(i -> i)
                .collect(Collectors.toMap(
                        i -> d.getBlocks().get(i),
                        i -> i));

        IntStream.range(0, d.getBlocks().size() - 1).parallel()
                .filter(i -> d.getBlocks().get(i) != null)
                .forEach(i -> {
                    Block b = d.getBlocks().get(i);
                    if (b.getRefBlock() != null) {
                        if (inverseIndex.containsKey(b.getRefBlock())) {
                            int j = inverseIndex.get(b.getRefBlock());
                            SemanticCohesion coh = new SemanticCohesion(b.getRefBlock(), b);
                            d.getBlockDistances()[j][i] = coh;
                            d.getBlockDistances()[i][j] = coh;
                        }
                    }
                    int last = Math.min(d.getBlocks().size(), i + SemanticCohesion.WINDOW_SIZE + 1);
                    for (int j = i + 1; j < last; j++) {
                        if (d.getBlocks().get(j) != null && d.getBlockDistances()[i][j] == null) {
                            SemanticCohesion coh = new SemanticCohesion(d.getBlocks().get(j), d.getBlocks().get(i));
                            d.getBlockDistances()[i][j] = coh;
                            d.getBlockDistances()[j][i] = coh;
                        }
                    }
                });

        DoubleStatistics statistics = IntStream.range(0, d.getBlocks().size() - 1).parallel()
                .mapToObj(i -> i)
                .flatMap(i -> IntStream.range(i + 1, d.getBlocks().size())
                .mapToObj(j -> d.getBlockDistances()[i][j]))
                .filter(Objects::nonNull)
                .map(SemanticCohesion::getCohesion)
                .collect(DoubleStatistics.collector());

        // determine mean and standard deviation values of semantic cohesion
        if (statistics.getCount() != 0) {
            avg = statistics.getAverage();
            stdev = statistics.getStandardDeviation();
        }

        // prune initial graph, but always keep adjacent pairs of blocks or explicitly referred blocks
        // iterate through all pairs of blocks of the document
        for (int i = 0; i < d.getBlocks().size() - 1; i++) {
            for (int j = i + 1; j < d.getBlocks().size(); j++) {
                Block b1 = d.getBlocks().get(i);
                Block b2 = d.getBlocks().get(j);
                // if the semantic cohesion is set for the pair of blocks (i, j)
                if (d.getBlockDistances()[j][i] != null && ((d.getBlockDistances()[j][i].getCohesion() >= Math.max(0.3, (avg + stdev)))
                        // if j is the next block after i and there is not an explicit link set for j
                        || (b2.getRefBlock() == null && j == i + 1)
                        // if there is an explicit link set for j and it is i 
                        || (b2.getRefBlock() != null && b2.getRefBlock() == b1))) {
                    // keep this semantic cohesion
                    d.getPrunnedBlockDistances()[i][j] = d.getBlockDistances()[i][j];
                    d.getPrunnedBlockDistances()[j][i] = d.getPrunnedBlockDistances()[i][j];
                }
            }
        }

        // determine intra-block distances (semantic cohesion)
        Block prevBlock = null, nextBlock;
        // iterate through blocks
        for (Block b : d.getBlocks()) {
            if (b != null) {
                // build link to next block
                nextBlock = null;
                for (int next = d.getBlocks().indexOf(b) + 1; next < d.getBlocks().size(); next++) {
                    if (d.getBlocks().get(next) != null) {
                        nextBlock = d.getBlocks().get(next);
                        break;
                    }
                }

                // set semantic cohesion between block's first sentence and previous block
                if (prevBlock != null && b.getSentences().size() > 0) {
                    b.setPrevSentenceBlockDistance(new SemanticCohesion(b.getSentences().get(0), prevBlock));
                }

                // set semantic cohesion between block's last sentence and next block
                if (nextBlock != null && b.getSentences().size() > 0) {
                    b.setNextSentenceBlockDistance(
                            new SemanticCohesion(nextBlock, b.getSentences().get(b.getSentences().size() - 1)));
                }

                // determine sentence-block semantic cohesion
                // initialize semantic cohesion vector for the semantic cohesion and similarity of (sentence, block) pairs
                b.setSentenceBlockDistances(new SemanticCohesion[b.getSentences().size()]);
                // iterate through all sentences of the block
                for (int i = 0; i < b.getSentences().size(); i++) {
                    // set semantic cohesion between the sentence and the block
                    b.getSentenceBlockDistances()[i] = new SemanticCohesion(b.getSentences().get(i), b);
                }

                // determine sentence-sentence semantic cohesion
                // initialize semantic cohesion arrays for the semantic cohesion and similarity of (sentence, sentence) pairs
                b.setSentenceDistances(new SemanticCohesion[b.getSentences().size()][b.getSentences().size()]);
                b.setPrunnedSentenceDistances(new SemanticCohesion[b.getSentences().size()][b.getSentences().size()]);

                avg = 0;
                stdev = 0;

                IntStream.range(0, b.getSentences().size() - 1).parallel()
                        .forEach(i -> {
                            Sentence s = b.getSentences().get(i);
                            for (int j = i + 1; j < b.getSentences().size(); j++) {
                                SemanticCohesion coh = new SemanticCohesion(b.getSentences().get(j), s);
                                b.getSentenceDistances()[i][j] = coh;
                                b.getSentenceDistances()[j][i] = coh;
                            }
                        });

                statistics = IntStream.range(0, b.getSentences().size() - 1).parallel()
                        .mapToObj(i -> i)
                        .flatMap(i -> IntStream.range(i + 1, b.getSentences().size())
                        .mapToObj(j -> b.getSentenceDistances()[i][j]))
                        .filter(Objects::nonNull)
                        .map(SemanticCohesion::getCohesion)
                        .collect(DoubleStatistics.collector());

                // determine mean and standard deviation values of semantic cohesion
                if (statistics.getCount() != 0) {
                    avg = statistics.getAverage();
                    stdev = statistics.getStandardDeviation();
                }

                // prune initial graph, but always keep adjacent pairs of sentences
                // iterate through all pairs of sentences of the block				
                for (int i = 0; i < b.getSentences().size() - 1; i++) {
                    for (int j = i + 1; j < b.getSentences().size(); j++) {
                        // if the semantic cohesion is greater than sum of mean and standard deviation and j is the next sentence after i
                        if ((b.getSentenceDistances()[i][j].getCohesion() >= Math.max(0.3, (avg + stdev))) || (j == i + 1)) {
                            // keep this semantic cohesion
                            b.getPrunnedSentenceDistances()[i][j] = b.getSentenceDistances()[i][j];
                            b.getPrunnedSentenceDistances()[j][i] = b.getPrunnedSentenceDistances()[i][j];
                        }
                    }
                }

                // set previous block for linking next blocks to it
                prevBlock = b;
            }
        }
    }
}
