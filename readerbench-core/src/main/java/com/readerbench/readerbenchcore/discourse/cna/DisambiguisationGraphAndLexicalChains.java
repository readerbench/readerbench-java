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
package com.readerbench.readerbenchcore.discourse.cna;

import com.readerbench.data.AbstractDocument;
import com.readerbench.readerbenchcore.data.lexicalChains.LexicalChain;
import com.readerbench.readerbenchcore.data.lexicalChains.LexicalChainLink;
import com.readerbench.readerbenchcore.semanticModels.WordNet.OntologySupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class DisambiguisationGraphAndLexicalChains {

    private static final Logger LOGGER = LoggerFactory.getLogger(CohesionGraph.class);

    public static void buildDisambiguationGraph(AbstractDocument d) {
        LOGGER.info("Building disambiguation graph");
        d.getBlocks().parallelStream()
                .filter(Objects::nonNull)
                .flatMap(b -> b.getSentences().stream())
                .filter(Objects::nonNull)
                .flatMap(s -> s.getWords().stream())
                .forEach(word -> {
                    Set<String> senseIds = OntologySupport
                            .getWordSenses(word);
                    if (senseIds != null) {
                        for (String idw : senseIds) {
                            // build a chain link for each sense
                            LexicalChainLink link = new LexicalChainLink(word, idw);
                            // add link to disambiguation graph
                            d.getDisambiguationGraph().addToGraph(idw, link);
                        }
                    }
                });

//                LOGGER.info("Finished block " + block.getIndex()
//                        + " - disambiguisation graph now contains "
//                        + d.getDisambiguationGraph().getNodes().size()
//                        + " word senses.");
    }

    public static void pruneDisambiguationGraph(AbstractDocument d) {
        LOGGER.info("Pruning block ");
        d.getBlocks().parallelStream()
            .filter(Objects::nonNull)
            .flatMap(b -> b.getSentences().stream())
            .filter(Objects::nonNull)
            .flatMap(s -> s.getWords().stream())
            .forEach(word -> {
                // go through all the senses of a word (we use the
                // lemma not
                // the actual word form)
                Set<String> senseIds = OntologySupport.getWordSenses(word);
                if (senseIds != null && senseIds.size() > 0) {
                    // find the sense with the best overall value
                    double maxValue = -1;
                    String bestSenseId = null;
                    for (String senseId : senseIds) {
                        LexicalChainLink link = d.getDisambiguationGraph().getLink(senseId, word);
                        if (link != null) {
                            double value = link.getValue();
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
            });
    }

    public static void buildLexicalChains(AbstractDocument d) {
        LOGGER.info("Building lexical chains");
        List<LexicalChainLink> listLinks;
        while ((listLinks = d.getDisambiguationGraph().extractFromGraph(null)) != null) {
            // create a new chain
            LexicalChain chain = new LexicalChain();
            // create a queue
            LinkedList<LexicalChainLink> q = new LinkedList<>();

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

}
