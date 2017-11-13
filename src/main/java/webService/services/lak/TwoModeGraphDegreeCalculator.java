package webService.services.lak;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import webService.services.lak.result.TwoModeGraph;
import webService.services.lak.result.TwoModeGraphNode;

/**
 *
 * @author ionutparaschiv
 */
public class TwoModeGraphDegreeCalculator {

    private final TwoModeGraph graph;

    private Map<String, TwoModeGraphNode> indexedNodesByUri;
    private Map<String, Integer> uriDegreeMap;

    private List<String> authorUrisByDegree;
    private List<String> articleUrisByDegree;

    public TwoModeGraphDegreeCalculator(TwoModeGraph graph) {
        this.graph = graph;
        this.indexNodesByUri();
        this.buildUriDegreeMap();
        List<TwoModeGraphNode> sortedNodeListByDegree = this.getSortedNodeListByDegree();
        this.buildUriListsByDegree(sortedNodeListByDegree);
    }

    private void indexNodesByUri() {
        this.indexedNodesByUri = new TreeMap<>();
        this.graph.nodeList.stream().forEach(node -> {
            this.indexedNodesByUri.put(node.getUri(), node);
        });
    }

    private void buildUriDegreeMap() {
        this.uriDegreeMap = new TreeMap<>();
        this.graph.edgeList.stream().forEach(edge -> {
            this.incrementUriDegree(edge.getSourceUri());
            this.incrementUriDegree(edge.getTargetUri());
        });
    }

    private void incrementUriDegree(String uri) {
        if (this.uriDegreeMap.containsKey(uri)) {
            Integer degree = this.uriDegreeMap.get(uri);
            degree++;
            this.uriDegreeMap.put(uri, degree);
            return;
        }
        this.uriDegreeMap.put(uri, 1);
    }

    private List<TwoModeGraphNode> getSortedNodeListByDegree() {
        List<TwoModeGraphNode> nodeList = new ArrayList<>(this.graph.nodeList);
        Collections.sort(nodeList, (TwoModeGraphNode node1, TwoModeGraphNode node2) -> {
            Integer node1Degree = uriDegreeMap.get(node1.getUri());
            if(node1Degree == null) {
                node1Degree = 0;
            }
            Integer node2Degree = uriDegreeMap.get(node2.getUri());
            if(node2Degree == null) {
                node2Degree = 0;
            }
            return -node1Degree.compareTo(node2Degree);
        });
        return nodeList;
    }

    private void buildUriListsByDegree(List<TwoModeGraphNode> sortedNodeListByDegree) {
        this.authorUrisByDegree = new ArrayList<>();
        this.articleUrisByDegree = new ArrayList<>();
        sortedNodeListByDegree.stream().forEach(node -> {
            switch (node.getType()) {
                case Article:
                    this.articleUrisByDegree.add(node.getUri());
                    break;
                case Author:
                    this.authorUrisByDegree.add(node.getUri());
                    break;
            }
        });
    }

    public List<String> getSortedAuthorUrisByDegree() {
        return this.authorUrisByDegree;
    }

    public List<String> getSortedArticleUrisByDegree() {
        return this.articleUrisByDegree;
    }
}
