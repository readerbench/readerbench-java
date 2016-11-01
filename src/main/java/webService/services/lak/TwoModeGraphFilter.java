package webService.services.lak;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;
import webService.services.lak.result.TwoModeGraph;
import webService.services.lak.result.TwoModeGraphNode;
import webService.services.lak.result.TwoModeGraphNodeType;

/**
 *
 * @author ionutparaschiv
 */
public class TwoModeGraphFilter {
    private static final Logger LOGGER = Logger.getLogger("");

    public static int MaxNoAuthors = 20;
    public static int MaxNoArticles = 20;

    private TwoModeGraphDegreeCalculator graphDegreeCalculator;

    public TwoModeGraphFilter() {
    }

    public TwoModeGraph filterGraph(TwoModeGraph graph, String centerUri, int noAuthors, int noArticles,
            boolean showAuthors, boolean showArticles) {
        this.graphDegreeCalculator = new TwoModeGraphDegreeCalculator(graph);
        Set<String> uriSet = this.getUriSet(graph);
        Set<String> restrictedUriSet = this.getRestrictedSet(uriSet, noAuthors, noArticles, showAuthors, showArticles);
        if (centerUri != null && centerUri.length() > 0) {
            restrictedUriSet.add(centerUri);
        }

        Set<String> addedUriSet = new HashSet<>();
        TwoModeGraph newGraph = new TwoModeGraph();
        graph.nodeList.stream().filter((node) -> (this.nodeIsValid(node, restrictedUriSet))).forEach((node) -> {
            newGraph.addNode(node);
            addedUriSet.add(node.getUri());
        });

        graph.edgeList.stream().forEach(edge -> {
            if (addedUriSet.contains(edge.getSourceUri()) && addedUriSet.contains(edge.getTargetUri())) {
                newGraph.addEdge(edge);
            }
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

    private Set<String> getRestrictedSet(Set<String> fullSet, int noAuthors, int noArticles,
            boolean showAuthors, boolean showArticles) {
        Set<String> articlesUriSet = this.restrictSetFromList(this.graphDegreeCalculator.getSortedArticleUrisByDegree(),
                fullSet, noArticles);
        if (!showArticles) {
            articlesUriSet = new HashSet<>();
        }
        Set<String> authorsUriSet = this.restrictSetFromList(this.graphDegreeCalculator.getSortedAuthorUrisByDegree(),
                fullSet, noAuthors);
        if (!showAuthors) {
            authorsUriSet = new HashSet<>();
        }
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
}