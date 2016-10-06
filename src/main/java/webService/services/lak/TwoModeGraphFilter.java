/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package webService.services.lak;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.log4j.Logger;
import view.widgets.article.utils.GraphMeasure;
import view.widgets.article.utils.GraphNodeItemType;
import webService.services.lak.result.TwoModeGraph;
import webService.services.lak.result.TwoModeGraphNode;
import webService.services.lak.result.TwoModeGraphNodeType;

/**
 *
 * @author ionutparaschiv
 */
public class TwoModeGraphFilter {

    public static int MaxNoAuthors = 20;
    public static int MaxNoArticles = 20;

    private static final Logger LOGGER = Logger.getLogger(TwoModeGraphFilter.class);
    private static List<GraphMeasure> graphMeasures;
    private static List<String> articleUriList = new ArrayList<>();
    private static List<String> authorUriList = new ArrayList<>();

    public TwoModeGraphFilter() {
    }

    public TwoModeGraph filterGraph(TwoModeGraph graph) {
        if (graphMeasures == null || graphMeasures.size() == 0) {
            LOGGER.info("TwoModeGraphFilter did not manage to load the OrderedAuthorsArticlesByBetweenness !!!");
            return graph;
        }
        Set<String> uriSet = this.getUriSet(graph);
        Set<String> restrictedUriSet = this.getRestrictedSet(graph, uriSet);
        restrictedUriSet.add("");

        TwoModeGraph newGraph = new TwoModeGraph();
        graph.nodeList.stream().filter((node) -> (this.nodeIsValid(node, restrictedUriSet))).forEach((node) -> {
            newGraph.addNode(node);
        });

        graph.edgeList.stream().filter((edge) -> (restrictedUriSet.contains(edge.getSourceUri()) && restrictedUriSet.contains(edge.getTargetUri()))).forEach((edge) -> {
            newGraph.addEdge(edge);
        });

        return newGraph;
    }

    private boolean nodeIsValid(TwoModeGraphNode node, Set<String> restrictedUriSet) {
        return restrictedUriSet.contains(node.getUri()) || node.getType() == TwoModeGraphNodeType.UserQuery;
    }

    private Set<String> getUriSet(TwoModeGraph graph) {
        Set<String> uriSet = new HashSet<>();
        graph.nodeList.stream().forEach(node -> {
            if (node.getType() != TwoModeGraphNodeType.UserQuery) {
                uriSet.add(node.getUri());
            }
        });
        return uriSet;
    }

    private Set<String> getRestrictedSet(TwoModeGraph graph, Set<String> fullSet) {
        Set<String> articlesUriSet = this.restrictSetFromList(articleUriList, fullSet, MaxNoArticles);
        Set<String> authorsUriSet = this.restrictSetFromList(authorUriList, fullSet, MaxNoAuthors);
        articlesUriSet.addAll(authorsUriSet);
        return articlesUriSet;
    }

    private Set<String> restrictSetFromList(List<String> uriList, Set<String> fullSet, int maxEntries) {
        Set<String> uriSet = new HashSet<>();
        int noEntries = 0;
        for (String uri : uriList) {
            if (fullSet.contains(uri)) {
                noEntries++;
                uriSet.add(uri);
            }
            if (noEntries >= maxEntries) {
                break;
            }
        }
        return uriSet;
    }

    public static TwoModeGraphFilter getTwoModeGraphFilter() {
        if (graphMeasures == null) {
            graphMeasures = GraphMeasure.readGraphMeasures();
            graphMeasures.stream().forEach(measure -> {
                if (measure.getNodeType() == GraphNodeItemType.Article) {
                    articleUriList.add(measure.getUri());
                } else if (measure.getNodeType() == GraphNodeItemType.Author) {
                    authorUriList.add(measure.getUri());
                }
            });
        }
        return new TwoModeGraphFilter();
    }
}
