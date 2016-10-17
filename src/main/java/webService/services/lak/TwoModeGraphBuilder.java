package webService.services.lak;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import view.widgets.article.utils.ArticleContainer;
import view.widgets.article.utils.CachedAuthorDistanceStrategyDecorator;
import view.widgets.article.utils.GraphNodeItem;
import view.widgets.article.utils.distanceStrategies.AuthorDistanceStrategyFactory;
import view.widgets.article.utils.distanceStrategies.AuthorDistanceStrategyType;
import view.widgets.article.utils.distanceStrategies.IAuthorDistanceStrategy;
import webService.services.lak.result.TwoModeGraph;
import webService.services.lak.result.TwoModeGraphEdge;
import webService.services.lak.result.TwoModeGraphNode;
import webService.services.lak.result.TwoModeGraphNodeType;

public class TwoModeGraphBuilder {

    private static final Map<String, TwoModeGraphBuilder> LOADED_GRAPH_BUILDERS = new HashMap<>();
    private static final IAuthorDistanceStrategy[] distanceStrategyList = new IAuthorDistanceStrategy[3];

    private final ArticleContainer authorContainer;

    private TwoModeGraph graph;

    public TwoModeGraphBuilder(String inputDirectory) {
        this.authorContainer = ArticleContainer.buildAuthorContainerFromDirectory(inputDirectory);
        AuthorDistanceStrategyFactory distStrategyFactory = new AuthorDistanceStrategyFactory(authorContainer);

        if (TwoModeGraphBuilder.distanceStrategyList[0] == null) {
            TwoModeGraphBuilder.distanceStrategyList[0] = new CachedAuthorDistanceStrategyDecorator(this.authorContainer,
                    distStrategyFactory.getDistanceStrategy(AuthorDistanceStrategyType.SemanticDistance));

            TwoModeGraphBuilder.distanceStrategyList[1] = new CachedAuthorDistanceStrategyDecorator(this.authorContainer,
                    distStrategyFactory.getDistanceStrategy(AuthorDistanceStrategyType.CoAuthorshipDistance));

            TwoModeGraphBuilder.distanceStrategyList[2] = new CachedAuthorDistanceStrategyDecorator(this.authorContainer,
                    distStrategyFactory.getDistanceStrategy(AuthorDistanceStrategyType.CoCitationsDistance));
        }
    }

    public TwoModeGraph getGraph(String centerUri, String searchText) {
        this.graph = new TwoModeGraph();
        try {
            List<GraphNodeItem> allGraphNodeItemList = this.loadAllNodes();
            this.loadAllEdges(allGraphNodeItemList);
            Set<String> restrictedUriSet = this.getNodeUriSetLinkedToCenter(centerUri);
            this.filterGraphEdges(restrictedUriSet);
            this.filterGraphNodes(restrictedUriSet);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return this.graph;
    }

    private List<GraphNodeItem> loadAllNodes() {
        List<GraphNodeItem> nodeList = new ArrayList<>();
        this.authorContainer.getAuthorContainers().stream().forEach((author) -> {
            nodeList.add(new GraphNodeItem(author));
            this.graph.addNode(new TwoModeGraphNode(TwoModeGraphNodeType.Author, author.getAuthor().getAuthorUri(), author.getAuthor().getAuthorName()));
        });
        this.authorContainer.getArticles().stream().forEach((article) -> {
            nodeList.add(new GraphNodeItem(article));
            this.graph.addNode(new TwoModeGraphNode(TwoModeGraphNodeType.Article, article.getURI(), article.getTitleText()));
        });
        return nodeList;
    }

    private void loadAllEdges(List<GraphNodeItem> nodeItemList) {
        for (IAuthorDistanceStrategy distanceStrategy : TwoModeGraphBuilder.distanceStrategyList) {
            double threshold = distanceStrategy.getThreshold();
            for (int i = 0; i < nodeItemList.size() - 1; i++) {
                for (int j = i + 1; j < nodeItemList.size(); j++) {
                    GraphNodeItem firstNodeItem = nodeItemList.get(i);
                    GraphNodeItem secondNodeItem = nodeItemList.get(j);
                    double distance = firstNodeItem.computeScore(secondNodeItem, distanceStrategy);
                    if (distance >= threshold) {
                        this.graph.addEdge(new TwoModeGraphEdge(distanceStrategy.getStrategyType(),
                                distance, firstNodeItem.getURI(), secondNodeItem.getURI()));
                    }
                }
            }
        }
    }

    private Set<String> getNodeUriSetLinkedToCenter(String centerUri) {
        Set<String> nodeUriSet = new TreeSet<>();
        graph.edgeList.stream().forEach(edge -> {
            if (centerUri == null || centerUri.length() == 0) {
                nodeUriSet.add(edge.getSourceUri());
                nodeUriSet.add(edge.getTargetUri());
            } else if (edge.getSourceUri().equalsIgnoreCase(centerUri) || edge.getTargetUri().equalsIgnoreCase(centerUri)) {
                nodeUriSet.add(edge.getSourceUri());
                nodeUriSet.add(edge.getTargetUri());
            }
        });
        return nodeUriSet;
    }

    private void filterGraphEdges(Set<String> restrictedUriSet) {
        List<TwoModeGraphEdge> edgeList = new ArrayList<>();
        this.graph.edgeList.stream().forEach(edge -> {
            if (restrictedUriSet.contains(edge.getSourceUri()) || restrictedUriSet.contains(edge.getTargetUri())) {
                edgeList.add(edge);
            }
        });
        graph.edgeList = edgeList;
    }

    private void filterGraphNodes(Set<String> restrictedUriSet) {
        List<TwoModeGraphNode> nodeList = new ArrayList<>();
        this.graph.nodeList.stream().forEach(node -> {
            if (restrictedUriSet.contains(node.getUri())) {
                nodeList.add(node);
            }
        });
        graph.nodeList = nodeList;
    }

    public List<TwoModeGraphNode> getAuthorNodes() {
        List<TwoModeGraphNode> nodeList = new ArrayList<>();
        this.authorContainer.getAuthorContainers().stream().forEach((author) -> {
            nodeList.add(new TwoModeGraphNode(TwoModeGraphNodeType.Author, author.getAuthor().getAuthorUri(), author.getAuthor().getAuthorName()));
        });
        return nodeList;
    }

    public static TwoModeGraphBuilder getLakCorpusTwoModeGraphBuilder() {
        String LAK_CORPUS_FOLDER = "resources/in/LAK_corpus/parsed-documents";
        if (LOADED_GRAPH_BUILDERS.containsKey(LAK_CORPUS_FOLDER)) {
            return LOADED_GRAPH_BUILDERS.get(LAK_CORPUS_FOLDER);
        }
        TwoModeGraphBuilder gaphBuilder = new TwoModeGraphBuilder(LAK_CORPUS_FOLDER);
        LOADED_GRAPH_BUILDERS.put(LAK_CORPUS_FOLDER, gaphBuilder);
        return gaphBuilder;
    }
}
