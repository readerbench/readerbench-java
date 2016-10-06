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

    private final ArticleContainer authorContainer;
    private final IAuthorDistanceStrategy[] distanceStrategyList;

    private TwoModeGraph graph;

    public TwoModeGraphBuilder(String inputDirectory) {
        this.authorContainer = ArticleContainer.buildAuthorContainerFromDirectory(inputDirectory);
        AuthorDistanceStrategyFactory distStrategyFactory = new AuthorDistanceStrategyFactory(authorContainer);
        this.distanceStrategyList = new IAuthorDistanceStrategy[]{
            new CachedAuthorDistanceStrategyDecorator(this.authorContainer,
            distStrategyFactory.getDistanceStrategy(AuthorDistanceStrategyType.SemanticDistance)),
            new CachedAuthorDistanceStrategyDecorator(this.authorContainer,
            distStrategyFactory.getDistanceStrategy(AuthorDistanceStrategyType.CoAuthorshipDistance)),
            new CachedAuthorDistanceStrategyDecorator(this.authorContainer,
            distStrategyFactory.getDistanceStrategy(AuthorDistanceStrategyType.CoCitationsDistance))
        };
    }

    public TwoModeGraph getGraph(String centerUri) {
        this.graph = new TwoModeGraph();
        try {
            List<GraphNodeItem> nodeItemList = this.loadAllNodes();
            nodeItemList = this.restrictNodesLinkedToCenter(centerUri, nodeItemList);
            this.loadEdges(nodeItemList);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return this.graph;
    }

    public List<TwoModeGraphNode> getAuthorNodes() {
        List<TwoModeGraphNode> nodeList = new ArrayList<>();
        this.authorContainer.getAuthorContainers().stream().forEach((author) -> {
            nodeList.add(new TwoModeGraphNode(TwoModeGraphNodeType.Author, author.getAuthor().getAuthorUri(), author.getAuthor().getAuthorName()));
        });
        return nodeList;
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

    private List<GraphNodeItem> restrictNodesLinkedToCenter(String centerUri, List<GraphNodeItem> nodeItemList) {
        Set<GraphNodeItem> restrictedSet = new TreeSet<>();
        Set<String> uriSet = new TreeSet<>();

        for (IAuthorDistanceStrategy distanceStrategy : this.distanceStrategyList) {
            double threshold = distanceStrategy.getThreshold();
            for (int i = 0; i < nodeItemList.size() - 1; i++) {
                for (int j = i + 1; j < nodeItemList.size(); j++) {
                    GraphNodeItem firstNodeItem = nodeItemList.get(i);
                    GraphNodeItem secondNodeItem = nodeItemList.get(j);

                    double distance = firstNodeItem.computeScore(secondNodeItem, distanceStrategy);
                    if (distance < threshold) {
                        continue;
                    }

                    if (centerUri == null || centerUri.length() == 0) {
                        restrictedSet.add(firstNodeItem);
                        uriSet.add(firstNodeItem.getURI());
                        restrictedSet.add(secondNodeItem);
                        uriSet.add(secondNodeItem.getURI());
                        continue;
                    }

                    if (firstNodeItem.getURI().equals(centerUri) || secondNodeItem.getURI().equals(centerUri)) {
                        restrictedSet.add(firstNodeItem);
                        uriSet.add(firstNodeItem.getURI());
                        restrictedSet.add(secondNodeItem);
                        uriSet.add(secondNodeItem.getURI());
                    }
                }
            }
        }
        List<TwoModeGraphNode> restrictedTMNodeList = new ArrayList<>();
        this.graph.nodeList.forEach(node -> {
            if (uriSet.contains(node.getUri())) {
                restrictedTMNodeList.add(node);
            }
        });
        this.graph.nodeList = restrictedTMNodeList;

        List<GraphNodeItem> restrictedList = new ArrayList<>(restrictedSet);
        return restrictedList;
    }

    private void loadEdges(List<GraphNodeItem> nodeItemList) {
        for (IAuthorDistanceStrategy distanceStrategy : this.distanceStrategyList) {
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
