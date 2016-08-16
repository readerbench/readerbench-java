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
package services.discourse.cohesion;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import services.semanticModels.WordNet.OntologySupport;
import data.AbstractDocument;
import data.Block;
import data.Sentence;
import data.Word;
import data.lexicalChains.LexicalChain;
import data.lexicalChains.LexicalChainLink;

public class DisambiguisationGraphAndLexicalChains {

    static Logger logger = Logger
            .getLogger(DisambiguisationGraphAndLexicalChains.class);

    public static void buildDisambiguationGraph(AbstractDocument d) {
        logger.info("Building disambiguation graph");
        for (Block block : d.getBlocks()) {
            if (block != null) {
                for (Sentence sentence : block.getSentences()) {
                    if (sentence != null) {
                        // only nouns form lexical chains
                        for (Word word : sentence.getWords()) {
                            // go through all the senses of a word (we use the
                            // lemma
                            // not
                            // the actual word form)
                            Set<String> senseIds = OntologySupport
                                    .getWordSenses(word);
                            if (senseIds != null) {
                                for (String idw : senseIds) {
                                    // build a chain link for each sense
                                    LexicalChainLink link = new LexicalChainLink(
                                            word, idw);
                                    // add link to disambiguation graph
                                    d.getDisambiguationGraph().addToGraph(idw,
                                            link);
                                }
                            }
                        }
                    }
                }
                logger.info("Finished block " + block.getIndex()
                        + " - disambiguisation graph now contains "
                        + d.getDisambiguationGraph().getNodes().size()
                        + " word senses.");
            }
        }
    }

    public static void pruneDisambiguationGraph(AbstractDocument d) {
        logger.info("Pruning block ");
        for (Block block : d.getBlocks()) {
            if (block != null) {
                for (Sentence sentence : block.getSentences()) {
                    if (sentence != null) {
                        // all words from lexical chains
                        for (Word word : sentence.getWords()) {
                            // go through all the senses of a word (we use the
                            // lemma not
                            // the actual word form)
                            Set<String> senseIds = OntologySupport
                                    .getWordSenses(word);
                            if (senseIds != null && senseIds.size() > 0) {
                                // find the sense with the best overall value
                                double maxValue = -1;
                                String bestSenseId = null;
                                for (String senseId : senseIds) {
                                    LexicalChainLink link = d
                                            .getDisambiguationGraph().getLink(
                                                    senseId, word);
                                    if (link != null) {
                                        double value = d
                                                .getDisambiguationGraph()
                                                .getLink(senseId, word)
                                                .getValue();
                                        if (value > maxValue) {
                                            maxValue = value;
                                            bestSenseId = senseId;
                                        }
                                    }
                                }
                                if (bestSenseId != null) {
                                    // associate the chain link corresponding to
                                    // the
                                    // best
                                    // sense to the word
                                    word.setLexicalChainLink(d
                                            .getDisambiguationGraph().getLink(
                                                    bestSenseId, word));

                                    // eliminate all other sense IDs
                                    for (String senseId : senseIds) {
                                        if (!senseId.equals(bestSenseId)) {
                                            LexicalChainLink remLink = d
                                                    .getDisambiguationGraph()
                                                    .getLink(senseId, word);
                                            d.getDisambiguationGraph()
                                                    .removeFromGraph(senseId,
                                                            remLink);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public static void buildLexicalChains(AbstractDocument d) {
        logger.info("Building lexical chains");
        List<LexicalChainLink> listLinks;
        while ((listLinks = d.getDisambiguationGraph().extractFromGraph(null)) != null) {
            // create a new chain
            LexicalChain chain = new LexicalChain();
            // create a queue
            LinkedList<LexicalChainLink> q = new LinkedList<LexicalChainLink>();

            // add all links for this sense to the chain
            for (LexicalChainLink link : listLinks) {
                chain.addLink(link);
                q.add(link);
            }

            // add all the connections to the chain
            while (!q.isEmpty()) {
                LexicalChainLink link = q.poll();
                for (LexicalChainLink connection : link.getConnections()
                        .keySet()) {
                    boolean notAlreadyInChain = chain.addLink(connection);
                    if (notAlreadyInChain) {
                        q.add(connection);

                        // we can already remove the corresponding node from the
                        // graph (the other instances in the list
                        // are already contained in the connections of this
                        // link)
                        d.getDisambiguationGraph().extractFromGraph(
                                connection.getSenseId());
                    }
                }
            }
            // add the new chain to the document
            d.getLexicalChains().add(chain);
        }
    }

    /**
     * Computes the word distances between the words in the lexical chains.
     */
    public static void computeWordDistances(AbstractDocument d) {
        logger.info("Computing all lexical chains distances");
        for (LexicalChain chain : d.getLexicalChains()) {
            chain.computeDistances();
        }
    }

}
