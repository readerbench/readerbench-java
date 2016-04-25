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

    public static final int SAME_SENTENCE = 0;
    public static final int THREE_SENTENCES = 1;
    public static final int SAME_PARAGRAPH = 2;
    public static final int OTHER = 3;

    private Lang language;
    private Map<String, List<LexicalChainLink>> nodes = new TreeMap<>();

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

    public void addToGraph(String senseId, LexicalChainLink newLink) {
        // build connections between the link and the other nodes in the graph
        buildConnections(newLink);

        // adds new link to the disambiguation graph
        if (!nodes.containsKey(senseId)) {
            LinkedList<LexicalChainLink> newList = new LinkedList<LexicalChainLink>();
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
                    // determine weight of the connection
                    if (OntologySupport.areSynonyms(newLink.getSenseId(), senseId, language)) {
                        weight = getWeightSynonyms(getDistance(newLink, link));
                    } else if (OntologySupport.areHypernym(newLink.getSenseId(), senseId, language)) {
                        weight = getWeightHypernyms(getDistance(newLink, link));
                    } else if (OntologySupport.areHyponym(newLink.getSenseId(), senseId, language)) {
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

    public void removeFromGraph(String senseId, LexicalChainLink remLink) {
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

    private void removeConnections(LexicalChainLink remLink) {
        for (LexicalChainLink link : remLink.getConnections().keySet()) {
            link.removeConnection(remLink);
        }
    }

    private double getWeightSynonyms(int distance) {
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

    private double getWeightAntonyms(int distance) {
        return getWeightSynonyms(distance);
    }

    private double getWeightHypernyms(int distance) {
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

    private double getWeightHyponyms(int distance) {
        return getWeightHypernyms(distance);
    }

    private double getWeightSiblings(int distance) {
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

    private int getDistance(LexicalChainLink link1, LexicalChainLink link2) {
        Word w1 = link1.getWord();
        Word w2 = link2.getWord();
        if (w1.getBlockIndex() == w2.getBlockIndex()) {
            int i1 = w1.getUtteranceIndex();
            int i2 = w2.getUtteranceIndex();
            if (i1 == i2) {
                return SAME_SENTENCE;
            } else if (Math.abs(i1 - i2) <= 3) {
                return THREE_SENTENCES;
            }

            return SAME_PARAGRAPH;
        }
        return OTHER;
    }

    public List<LexicalChainLink> extractFromGraph(String senseId) {
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
        String s = "";

        for (Map.Entry<String, List<LexicalChainLink>> e : nodes.entrySet()) {
            s += e.getKey() + ":\n";
            for (LexicalChainLink link : e.getValue()) {
                s += "\t" + link.toString() + "\n";
            }
        }
        return s;
    }
}
