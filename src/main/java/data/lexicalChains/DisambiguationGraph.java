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
package data.lexicalChains;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import services.semanticModels.WordNet.OntologySupport;
import data.Word;
import data.Lang;

/**
 *
 * @authors Ioana Serban, Mihai Dascalu
 */
public class DisambiguationGraph implements Serializable {

    private static final long serialVersionUID = 1026969873848700049L;

    public enum LexicalChainDistance {
        SAME_SENTENCE, THREE_SENTENCES, SAME_PARAGRAPH, OTHER;
    }

    private final Lang language;
    private final Map<String, List<LexicalChainLink>> nodes = new TreeMap<>();

    public DisambiguationGraph(Lang language) {
        this.language = language;
    }

    public LexicalChainLink getLink(String senseId, Word word) {
        if (nodes.get(senseId) != null) {
            for (LexicalChainLink link : nodes.get(senseId)) {
                if (link.getWord() == word) {
                    return link;
                }
            }
        }
        return null;
    }

    public synchronized void addToGraph(String senseId, LexicalChainLink newLink) {
        // build connections between the link and the other nodes in the graph
        buildConnections(newLink);

        // adds new link to the disambiguation graph
        if (!nodes.containsKey(senseId)) {
            LinkedList<LexicalChainLink> newList = new LinkedList<>();
            newList.add(newLink);
            nodes.put(senseId, newList);
        } else {
            nodes.get(senseId).add(newLink);
        }
    }

    private void buildConnections(LexicalChainLink newLink) {
        for (String senseId : nodes.keySet()) {
            double weight = 0;
            for (LexicalChainLink link : nodes.get(senseId)) {
                if (!newLink.hasSameWord(link)) {
                    // determine weight of the connection)
                    if (OntologySupport.areSynonyms(newLink.getSenseId(), senseId, language)) {
                        weight = getWeightSynonyms(getDistance(newLink, link));
                    } else if (OntologySupport.areDirectHypernyms(newLink.getSenseId(), senseId, language)) {
                        weight = getWeightHypernyms(getDistance(newLink, link));
                    } else if (OntologySupport.areDirectHyponyms(newLink.getSenseId(), senseId, language)) {
                        weight = getWeightHyponyms(getDistance(newLink, link));
                    } else if (newLink.getWord().getBlockIndex() == link.getWord().getBlockIndex()
                            && OntologySupport.areSiblings(newLink.getSenseId(), senseId, language)) {
                        weight = getWeightSiblings(getDistance(newLink, link));
                    }

                    // if the two word senses are related add the connection
                    // between their respective chain links
                    if (weight > 0) {
                        link.addConnection(newLink, weight);
                        newLink.addConnection(link, weight);
                    }
                }
            }
        }
    }

    public synchronized void removeFromGraph(String senseId, LexicalChainLink remLink) {
        // remove connections
        removeConnections(remLink);

        // remove from list
        List<LexicalChainLink> senseList = nodes.get(senseId);
        senseList.remove(remLink);

        // if sense list is now empty, remove the node from the graph
        if (senseList.isEmpty()) {
            nodes.remove(senseId);
        }
    }

    private synchronized void removeConnections(LexicalChainLink remLink) {
        remLink.getConnections().keySet().stream().forEach((link) -> {
            link.removeConnection(remLink);
        });
    }

    private double getWeightSynonyms(LexicalChainDistance distance) {
        double weight = 0;
        switch (distance) {
            case SAME_SENTENCE:
                weight = 1.0;
                break;
            case THREE_SENTENCES:
                weight = 1.0;
                break;
            case SAME_PARAGRAPH:
                weight = 0.5;
                break;
            case OTHER:
                weight = 0.5;
                break;
        }
        return weight;
    }

    private double getWeightAntonyms(LexicalChainDistance distance) {
        return getWeightSynonyms(distance);
    }

    private double getWeightHypernyms(LexicalChainDistance distance) {
        double weight = 0;
        switch (distance) {
            case SAME_SENTENCE:
                weight = 1.0;
                break;
            case THREE_SENTENCES:
                weight = 0.5;
                break;
            case SAME_PARAGRAPH:
                weight = 0.3;
                break;
            case OTHER:
                weight = 0.3;
                break;
        }
        return weight;
    }

    private double getWeightHyponyms(LexicalChainDistance distance) {
        return getWeightHypernyms(distance);
    }

    private double getWeightSiblings(LexicalChainDistance distance) {
        double weight = 0;
        switch (distance) {
            case SAME_SENTENCE:
                weight = 1.0;
                break;
            case THREE_SENTENCES:
                weight = 0.3;
                break;
            case SAME_PARAGRAPH:
                weight = 0.2;
                break;
            case OTHER:
                weight = 0.0;
                break;
        }
        return weight;
    }

    private LexicalChainDistance getDistance(LexicalChainLink link1, LexicalChainLink link2) {
        Word w1 = link1.getWord();
        Word w2 = link2.getWord();
        if (w1.getBlockIndex() == w2.getBlockIndex()) {
            int i1 = w1.getUtteranceIndex();
            int i2 = w2.getUtteranceIndex();
            if (i1 == i2) {
                return LexicalChainDistance.SAME_SENTENCE;
            } else if (Math.abs(i1 - i2) <= 3) {
                return LexicalChainDistance.THREE_SENTENCES;
            }
            return LexicalChainDistance.SAME_PARAGRAPH;
        }
        return LexicalChainDistance.OTHER;
    }

    public synchronized List<LexicalChainLink> extractFromGraph(String senseId) {
        if (nodes.isEmpty()) {
            return null;
        }
        // if senseId = null, extract the first element
        if (senseId == null) {
            String firstKey = nodes.keySet().iterator().next();
            return nodes.remove(firstKey);
        }

        return nodes.remove(senseId);
    }

    public Map<String, List<LexicalChainLink>> getNodes() {
        return nodes;
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();

        nodes.entrySet().stream().map((e) -> {
            s.append(e.getKey()).append(":\n");
            return e;
        }).forEach((e) -> {
            e.getValue().stream().forEach((link) -> {
                s.append("\t").append(link.toString()).append("\n");
            });
        });
        return s.toString();
    }
}
