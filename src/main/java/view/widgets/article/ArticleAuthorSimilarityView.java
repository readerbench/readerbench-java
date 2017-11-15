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
package view.widgets.article;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Toolkit;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.border.EtchedBorder;
import javax.swing.event.ChangeEvent;

import org.gephi.appearance.api.AppearanceController;
import org.gephi.appearance.api.AppearanceModel;
import org.gephi.appearance.api.Function;
import org.gephi.appearance.plugin.RankingNodeSizeTransformer;
import org.gephi.graph.api.Column;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.UndirectedGraph;
import org.gephi.io.exporter.api.ExportController;
import org.gephi.preview.api.G2DTarget;
import org.gephi.preview.api.PreviewController;
import org.gephi.preview.api.PreviewModel;
import org.gephi.preview.api.PreviewProperty;
import org.gephi.preview.api.RenderTarget;
import org.gephi.preview.types.DependantOriginalColor;
import org.gephi.preview.types.DependantOriginalColor.Mode;
import org.gephi.project.api.ProjectController;
import org.gephi.statistics.plugin.GraphDistance;
import org.openide.util.Lookup;

import data.article.ResearchArticle;
import java.util.logging.Logger;
import org.gephi.layout.plugin.force.StepDisplacement;
import org.gephi.layout.plugin.force.yifanHu.YifanHuLayout;
import org.openide.util.Exceptions;
import view.models.PreviewSketch;
import services.extendedCNA.ArticleContainer;
import services.extendedCNA.ArticleAuthorParameterLogger;
import services.extendedCNA.GraphMeasure;
import services.extendedCNA.GraphNodeItem;
import services.extendedCNA.SingleAuthorContainer;
import services.extendedCNA.distanceStrategies.IAuthorDistanceStrategy;
import utils.LocalizationUtils;

public class ArticleAuthorSimilarityView extends JFrame {

    static ArticleAuthorSimilarityView corpusView;
    private static final long serialVersionUID = -8582615231233815258L;
    static final Logger LOGGER = Logger.getLogger("");
    public static final Color COLOR_AUTHOR = new Color(120, 120, 120);
    public static final Color COLOR_ARTICLE = new Color(255, 10, 0);
    public static final Color COLOR_CENTER_NODE = new Color(0, 21, 255);

    private final IAuthorDistanceStrategy[] distanceStrategyList;
    private final String graphCenterUri;
    private final ArticleContainer authorContainer;
    private final ArticleAuthorParameterLogger paramLogger;

    private JSlider sliderThreshold;
    private JPanel panelGraph;

    public ArticleAuthorSimilarityView(ArticleContainer authorContainer, IAuthorDistanceStrategy[] distanceStrategyList,
            ArticleAuthorParameterLogger paramLogger, String graphCenterUri) {
        this.authorContainer = authorContainer;
        this.distanceStrategyList = distanceStrategyList;
        this.paramLogger = paramLogger;
        this.graphCenterUri = graphCenterUri;

        corpusView = this;
        super.setTitle("ReaderBench - " + LocalizationUtils.getTitle(this.getClass()));
        super.getContentPane().setBackground(Color.WHITE);
        super.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        // adjust view to desktop size
        int margin = 50;
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        super.setBounds(margin, margin, screenSize.width - margin * 2, screenSize.height - margin * 2);

        generateLayout();
        generateGraph();
    }

    private void generateLayout() {
        JLabel lblThreshold = new JLabel("Threshold");
        lblThreshold.setFont(new Font("SansSerif", Font.BOLD, 12));

        sliderThreshold = new JSlider(0, 100, 90);
        sliderThreshold.setBackground(Color.WHITE);
        sliderThreshold.setPaintTicks(true);
        sliderThreshold.setFont(new Font("SansSerif", Font.PLAIN, 10));
        sliderThreshold.setPaintLabels(true);
        sliderThreshold.setMinorTickSpacing(10);
        sliderThreshold.setMajorTickSpacing(50);
        java.util.Dictionary<Integer, JLabel> labelTableThreshold = new java.util.Hashtable<Integer, JLabel>();
        labelTableThreshold.put(100, new JLabel("100%"));
        labelTableThreshold.put(50, new JLabel("50%"));
        labelTableThreshold.put(0, new JLabel("0"));
        sliderThreshold.setLabelTable(labelTableThreshold);
        sliderThreshold.addChangeListener((ChangeEvent e) -> {
            generateGraph();
        });

        panelGraph = new JPanel();
        panelGraph.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
        panelGraph.setBackground(Color.WHITE);
        panelGraph.setLayout(new BorderLayout());

        GroupLayout groupLayout = new GroupLayout(getContentPane());
        groupLayout
                .setHorizontalGroup(groupLayout.createParallelGroup(Alignment.LEADING)
                        .addGroup(groupLayout.createSequentialGroup().addContainerGap()
                                .addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
                                        .addGroup(groupLayout.createSequentialGroup()
                                                .addContainerGap())
                                        .addGroup(groupLayout.createSequentialGroup()
                                                .addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
                                                        .addGroup(groupLayout.createSequentialGroup()
                                                                .addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
                                                                        .addComponent(lblThreshold).addComponent(sliderThreshold,
                                                                        GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
                                                                        GroupLayout.PREFERRED_SIZE))
                                                                .addGap(10))
                                                        .addGroup(groupLayout.createSequentialGroup()
                                                                .addComponent(panelGraph, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE,
                                                                        Short.MAX_VALUE)
                                                                .addPreferredGap(ComponentPlacement.RELATED)))
                                        ))
                        ));
        groupLayout
                .setVerticalGroup(
                        groupLayout.createParallelGroup(Alignment.LEADING)
                                .addGroup(groupLayout.createSequentialGroup().addContainerGap()
                                        .addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
                                                .addComponent(lblThreshold))
                                        .addPreferredGap(ComponentPlacement.RELATED)
                                        .addComponent(sliderThreshold, GroupLayout.PREFERRED_SIZE, 52,
                                                GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(ComponentPlacement.RELATED)
                                        .addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
                                                .addComponent(panelGraph, GroupLayout.DEFAULT_SIZE, 581, Short.MAX_VALUE))
                                        .addPreferredGap(ComponentPlacement.RELATED)
                                        .addPreferredGap(ComponentPlacement.RELATED)));
        getContentPane().setLayout(groupLayout);
    }

    public HashMap<Node, GraphNodeItem> buildConceptGraph(UndirectedGraph graph, GraphModel graphModel) {
        HashMap<Node, GraphNodeItem> outMap = new HashMap<>();
        LOGGER.info("Starting to build the author graph");
        // build connected graph
        Map<GraphNodeItem, Boolean> visibleDocs = new TreeMap<>();
        // build nodes
        Map<GraphNodeItem, Node> nodes = new TreeMap<>();

        List<GraphNodeItem> nodeItemList = new ArrayList<>();

        for (SingleAuthorContainer author : this.authorContainer.getAuthorContainers()) {
            GraphNodeItem nodeItem = new GraphNodeItem(author);
            visibleDocs.put(nodeItem, false);
            nodeItemList.add(nodeItem);
        }
        for (ResearchArticle article : this.authorContainer.getArticles()) {
            GraphNodeItem nodeItem = new GraphNodeItem(article);
            visibleDocs.put(nodeItem, false);
            nodeItemList.add(nodeItem);
        }

        for (IAuthorDistanceStrategy distanceStrategy : this.distanceStrategyList) {
            int distanceLbl = graphModel.addEdgeType(distanceStrategy.getStrategyKey());
            double threshold = distanceStrategy.getThreshold();
            // determine similarities in order to determine eligible candidates for vizualization
            for (int i = 0; i < nodeItemList.size() - 1; i++) {
                for (int j = i + 1; j < nodeItemList.size(); j++) {
                    GraphNodeItem firstNodeItem = nodeItemList.get(i);
                    GraphNodeItem secondNodeItem = nodeItemList.get(j);

                    if (this.graphCenterUri != null) {
                        if (!firstNodeItem.getURI().equals(this.graphCenterUri) && !secondNodeItem.getURI().equals(this.graphCenterUri)) {
                            continue;
                        }
                    }

                    double sim = firstNodeItem.computeScore(secondNodeItem, distanceStrategy);
                    if (sim >= threshold) {
                        visibleDocs.put(firstNodeItem, true);
                        visibleDocs.put(secondNodeItem, true);
                    }
                }
            }
            for (GraphNodeItem o : nodeItemList) {
                if (visibleDocs.get(o) && !nodes.containsKey(o)) {
                    String text = o.getName();
                    Color c = null;

                    Node n = graphModel.factory().newNode(o.getURI());
                    text = (text.length() > 25) ? (text.substring(0, 25) + "..") : text;
                    n.setLabel(text);

                    if (o.isArticle()) {
                        c = new Color((float) (COLOR_ARTICLE.getRed()) / 256, (float) (COLOR_ARTICLE.getGreen()) / 256,
                                (float) (COLOR_ARTICLE.getBlue()) / 256);
                    } else {
                        c = new Color((float) (COLOR_AUTHOR.getRed()) / 256, (float) (COLOR_AUTHOR.getGreen()) / 256,
                                (float) (COLOR_AUTHOR.getBlue()) / 256);
                    }
                    if (this.graphCenterUri != null) {
                        if (o.getURI().equals(this.graphCenterUri)) {
                            c = new Color((float) (COLOR_CENTER_NODE.getRed()) / 256, (float) (COLOR_CENTER_NODE.getGreen()) / 256,
                                    (float) (COLOR_CENTER_NODE.getBlue()) / 256);
                            n.setSize(10);
                        }
                    }

                    n.setColor(c);
                    n.setX((float) ((0.01 + Math.random()) * 1000) - 500);
                    n.setY((float) ((0.01 + Math.random()) * 1000) - 500);

                    graph.addNode(n);
                    nodes.put(o, n);
                    outMap.put(n, o);
                }
            }
            // determine similarities
            for (int i = 0; i < nodeItemList.size() - 1; i++) {
                for (int j = i + 1; j < nodeItemList.size(); j++) {
                    GraphNodeItem firstNodeItem = nodeItemList.get(i);
                    GraphNodeItem secondNodeItem = nodeItemList.get(j);
                    if (visibleDocs.get(firstNodeItem) && visibleDocs.get(secondNodeItem)) {
                        double sim = firstNodeItem.computeScore(secondNodeItem, distanceStrategy);
                        if (sim >= threshold) {
                            Edge e = graphModel.factory().newEdge(nodes.get(firstNodeItem), nodes.get(secondNodeItem), distanceLbl, sim, false);
                            e.setLabel("");
                            graph.addEdge(e);
                        }
                    }
                }
            }
        }

        return outMap;
    }

    private void generateGraph() {
        ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
        pc.newProject();

        // get models
        GraphModel graphModel = Lookup.getDefault().lookup(GraphController.class).getGraphModel();
        UndirectedGraph graph = graphModel.getUndirectedGraph();
        AppearanceController appearanceController = Lookup.getDefault().lookup(AppearanceController.class);
        AppearanceModel appearanceModel = appearanceController.getModel();

        HashMap<Node, GraphNodeItem> nodeMap = buildConceptGraph(graph, graphModel);

        YifanHuLayout layout = new YifanHuLayout(null, new StepDisplacement(1f));
        layout.setGraphModel(graphModel);
        layout.initAlgo();
        layout.resetPropertiesValues();
        layout.setOptimalDistance(100f);

        for (int i = 0; i < 100 && layout.canAlgo(); i++) {
            layout.goAlgo();
        }
        layout.endAlgo();

        layout.setGraphModel(graphModel);
        // Get Centrality
        GraphDistance distance = new GraphDistance();
        distance.setDirected(true);
        distance.execute(graphModel);

        double maxCentrality = Double.NEGATIVE_INFINITY;
        Column betweeennessColumn = graphModel.getNodeTable().getColumn(GraphDistance.BETWEENNESS);
        Column closenessColumn = graphModel.getNodeTable().getColumn(GraphDistance.CLOSENESS);
        Column eccentricityColumn = graphModel.getNodeTable().getColumn(GraphDistance.ECCENTRICITY);
        List<GraphMeasure> graphMeasures = new ArrayList<>();
        for (Node n : graph.getNodes()) {
            Double betwenness = (Double) n.getAttribute(betweeennessColumn);
            Double eccentricity = (Double) n.getAttribute(eccentricityColumn);
            Double closeness = (Double) n.getAttribute(closenessColumn);

            GraphNodeItem currentDoc = nodeMap.get(n);
            int degree = graph.getDegree(n);

            GraphMeasure graphMeasure = new GraphMeasure();
            graphMeasure.setUri(currentDoc.getURI());
            graphMeasure.setNodeType(currentDoc.getNodeType());
            graphMeasure.setBetwenness(betwenness);
            graphMeasure.setCloseness(closeness);
            graphMeasure.setDegree(new Double(degree));
            graphMeasure.setEccentricity(eccentricity);
            graphMeasure.setName(currentDoc.getName());
            graphMeasure.setNoOfReferences(currentDoc.getNoOfReferences());
            graphMeasures.add(graphMeasure);

            if (betwenness > maxCentrality) {
                maxCentrality = betwenness;
            }
        }
        paramLogger.logGraphMeasures(graphMeasures);
        Collections.sort(graphMeasures);
        GraphMeasure.saveSerializedObject(graphMeasures);

        System.out.println(graphMeasures);

        // Rank size by centrality
        Column centralityColumn = graphModel.getNodeTable().getColumn(GraphDistance.BETWEENNESS);
        Function centralityRanking = appearanceModel.getNodeFunction(graph, centralityColumn,
                RankingNodeSizeTransformer.class);
        RankingNodeSizeTransformer centralityTransformer = (RankingNodeSizeTransformer) centralityRanking
                .getTransformer();
        centralityTransformer.setMinSize(3);
        centralityTransformer.setMaxSize(10);
        appearanceController.transform(centralityRanking);

        // Preview configuration
        PreviewController previewController = Lookup.getDefault().lookup(PreviewController.class);
        PreviewModel previewModel = previewController.getModel();
        previewModel.getProperties().putValue(PreviewProperty.SHOW_NODE_LABELS, Boolean.TRUE);
        previewModel.getProperties().putValue(PreviewProperty.NODE_LABEL_COLOR,
                new DependantOriginalColor(Mode.ORIGINAL));
        previewModel.getProperties().putValue(PreviewProperty.EDGE_CURVED, Boolean.FALSE);
        previewModel.getProperties().putValue(PreviewProperty.EDGE_OPACITY, 50);
        previewModel.getProperties().putValue(PreviewProperty.EDGE_RADIUS, 10f);
        previewModel.getProperties().putValue(PreviewProperty.SHOW_EDGE_LABELS, Boolean.TRUE);
        previewModel.getProperties().putValue(PreviewProperty.NODE_LABEL_PROPORTIONAL_SIZE, Boolean.FALSE);
        previewModel.getProperties().putValue(PreviewProperty.EDGE_CURVED, Boolean.TRUE);

        // New Processing target, get the PApplet
        G2DTarget target = (G2DTarget) previewController.getRenderTarget(RenderTarget.G2D_TARGET);
        PreviewSketch previewSketch = new PreviewSketch(target);
        previewController.refreshPreview();
        previewSketch.resetZoom();
        if (panelGraph.getComponents().length > 0) {
            panelGraph.removeAll();
            panelGraph.revalidate();
        }
        panelGraph.add(previewSketch, BorderLayout.CENTER);

        LOGGER.info("Saving export...");
        ExportController ec = Lookup.getDefault().lookup(ExportController.class);
        try {
            ec.exportFile(new File("out/graph_doc_corpus_view.pdf"));
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
            return;
        }
        this.pack();
        LOGGER.info("Finished building the graph");
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        revalidate();
    }

    private static void computeMetrics(ArticleContainer container) {
        List<SingleAuthorContainer> list = container.getAuthorContainers();
        List<String> authorPaperList = new ArrayList<>();
        HashSet<String> uniquePaperList = new HashSet<>();
        for (SingleAuthorContainer authContainer : list) {
            String[] authList = {"http://data.linkededucation.org/resource/lak/person/ryan-sjd-baker",
                "http://data.linkededucation.org/resource/lak/person/neil-t-heffernan",
                "http://data.linkededucation.org/resource/lak/person/joseph-e-beck",
                "http://data.linkededucation.org/resource/lak/person/kenneth-r-koedinger",
                "http://data.linkededucation.org/resource/lak/person/jack-mostow"//,
            //"http://data.linkededucation.org/resource/lak/person/arthur-c-graesser",
            //"http://data.linkededucation.org/resource/lak/person/zachary-a-pardos",
            //"http://data.linkededucation.org/resource/lak/person/jose-p-gonzalez-brenes",
            //"http://data.linkededucation.org/resource/lak/person/s-ventura",
            //"http://data.linkededucation.org/resource/lak/person/c-romero"
            };
            for (String authUri : authList) {
                if (authUri.equals(authContainer.getAuthor().getAuthorUri())) {
                    for (ResearchArticle article : authContainer.getAuthorArticles()) {
                        authorPaperList.add(article.getURI());
                        uniquePaperList.add(article.getURI());
                    }

                    break;
                }
            }
        }
        System.out.println("Total paper count = " + authorPaperList.size());
        System.out.println("Unique paper count = " + uniquePaperList.size());
    }
}
