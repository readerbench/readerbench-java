package view.widgets.comprehensionModel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.border.EtchedBorder;

import org.apache.log4j.Logger;
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
import org.gephi.layout.plugin.forceAtlas2.ForceAtlas2;
import org.gephi.preview.api.G2DTarget;
import org.gephi.preview.api.PreviewController;
import org.gephi.preview.api.PreviewModel;
import org.gephi.preview.api.PreviewProperty;
import org.gephi.preview.api.RenderTarget;
import org.gephi.preview.types.DependantOriginalColor;
import org.gephi.preview.types.DependantOriginalColor.Mode;
import org.gephi.project.api.ProjectController;
import org.gephi.statistics.plugin.GraphDistance;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;

import services.comprehensionModel.ComprehensionModel;
import services.comprehensionModel.utils.indexer.WordDistanceIndexer;
import services.comprehensionModel.utils.indexer.graphStruct.CMEdgeDO;
import services.comprehensionModel.utils.indexer.graphStruct.CMEdgeType;
import services.comprehensionModel.utils.indexer.graphStruct.CMGraphDO;
import services.comprehensionModel.utils.indexer.graphStruct.CMNodeDO;
import services.comprehensionModel.utils.indexer.graphStruct.CMNodeType;
import view.models.PreviewSketch;

public class ComprehensionModelView extends JFrame {

    private static final long serialVersionUID = 1L;

    static Logger logger = Logger.getLogger(ComprehensionModelView.class);
    private final ComprehensionModel cm;
    private int sentenceIndex;
    public static final Color COLOR_SEMANTIC = new Color(255, 10, 0);
    public static final Color COLOR_SYNTATIC = new Color(0, 21, 255);
    public static final Color COLOR_INACTIVE = new Color(170, 170, 170);
    public static final Color COLOR_ACTIVE = new Color(59, 153, 50);

    JLabel phraseLabel;
    JButton btnNextPhrase;
    JPanel panelGraph;

    public ComprehensionModelView(ComprehensionModel cm) {
        this.cm = cm;
        this.sentenceIndex = 0;

        this.setDefaultWindowSize();
        this.generateLayout();
        this.updateValuesForCurrentSentence();
    }

    private void generateLayout() {
        this.phraseLabel = new JLabel("New label");
        this.btnNextPhrase = new JButton("Next Phrase");
        this.btnNextPhrase.addActionListener((ActionEvent e) -> {
            ComprehensionModelView.this.increaseSentenceIndex();
        });

        panelGraph = new JPanel();
        panelGraph.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
        panelGraph.setBackground(Color.WHITE);
        panelGraph.setLayout(new BorderLayout());

        GroupLayout groupLayout = new GroupLayout(getContentPane());
        groupLayout.setHorizontalGroup(groupLayout.createParallelGroup(Alignment.LEADING)
                .addGroup(groupLayout.createSequentialGroup().addGap(23)
                        .addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
                                .addComponent(panelGraph, GroupLayout.DEFAULT_SIZE, 1298, Short.MAX_VALUE)
                                .addGroup(groupLayout.createSequentialGroup().addComponent(phraseLabel)
                                        .addPreferredGap(ComponentPlacement.RELATED).addComponent(btnNextPhrase)))
                        .addGap(19)));
        groupLayout.setVerticalGroup(groupLayout.createParallelGroup(Alignment.LEADING)
                .addGroup(groupLayout.createSequentialGroup().addContainerGap()
                        .addGroup(groupLayout.createParallelGroup(Alignment.BASELINE).addComponent(phraseLabel)
                                .addComponent(btnNextPhrase))
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addComponent(panelGraph, GroupLayout.DEFAULT_SIZE, 721, Short.MAX_VALUE).addGap(16)));
        getContentPane().setLayout(groupLayout);
    }

    private void setDefaultWindowSize() {
        int margin = 50;
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setBounds(margin, margin, screenSize.width - margin * 2, screenSize.height - margin * 2);
    }

    private void increaseSentenceIndex() {
        if (this.sentenceIndex < this.cm.getTotalNoOfPhrases() - 1) {
            this.sentenceIndex++;
            this.updateValuesForCurrentSentence();
        }
    }

    private void updateValuesForCurrentSentence() {
        this.phraseLabel.setText(this.cm.getSentenceAtIndex(this.sentenceIndex).getText());
        this.generateGraph(this.sentenceIndex);
    }

    private void generateGraph(int sentenceIndex) {
        ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
        pc.newProject();

        // get models
        GraphModel graphModel = Lookup.getDefault().lookup(GraphController.class).getGraphModel();
        UndirectedGraph graph = graphModel.getUndirectedGraph();
        AppearanceController appearanceController = Lookup.getDefault().lookup(AppearanceController.class);
        AppearanceModel appearanceModel = appearanceController.getModel();

        buildConceptGraph(graph, graphModel);

        // Get Centrality
        GraphDistance distance = new GraphDistance();
        distance.setDirected(true);
        distance.execute(graphModel);

        // run ForceAtlas 2 layout
        ForceAtlas2 layout = new ForceAtlas2(null);
        layout.setGraphModel(graphModel);
        layout.resetPropertiesValues();

        layout.setOutboundAttractionDistribution(false);
        layout.setEdgeWeightInfluence(1.5d);
        layout.setGravity(10d);
        layout.setJitterTolerance(.02);
        layout.setScalingRatio(15.0);
        layout.initAlgo();

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

        logger.info("Saving export...");
        ExportController ec = Lookup.getDefault().lookup(ExportController.class);
        try {
            ec.exportFile(new File("out/network graph phrase " + (sentenceIndex + 1) + ".pdf"));
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
            return;
        }
        this.pack();
        logger.info("Finished building the graph");
    }

    public HashMap<Node, CMNodeDO> buildConceptGraph(UndirectedGraph graph, GraphModel graphModel) {
        HashMap<Node, CMNodeDO> outMap = new HashMap<>();
        logger.info("Starting to build the comprehension model graph");

        Map<CMNodeDO, Node> nodes = new TreeMap<>();

        this.cm.markAllNodesAsInactive();
        WordDistanceIndexer syntacticIndexer = this.cm.getSyntacticIndexerAtIndex(this.sentenceIndex);

        CMGraphDO ciGraph = syntacticIndexer.getCiGraph(CMNodeType.TextBased);
        CMGraphDO semanticGraph = this.cm.getSemanticIndexer().getCiGraph(CMNodeType.Inferred);

        ciGraph.combineWithLinksFrom(semanticGraph);
        ciGraph = ciGraph.getCombinedGraph(this.cm.currentGraph);

        this.cm.currentGraph = ciGraph;
        this.cm.updateActivationScoreMapAtIndex(this.sentenceIndex);
        this.cm.applyPageRank(this.sentenceIndex);
        this.cm.logSavedScores(syntacticIndexer.getCiGraph(CMNodeType.TextBased), this.sentenceIndex);

        List<CMNodeDO> nodeItemList = ciGraph.nodeList;

        nodeItemList.stream().forEach((currentNode) -> {
            String text = currentNode.getWord().getLemma();

            Node n = graphModel.factory().newNode(text);
            n.setLabel(text);

            Color actualColor = this.getNodeColor(currentNode);

            Color c = new Color((float) (actualColor.getRed()) / 256, (float) (actualColor.getGreen()) / 256,
                    (float) (actualColor.getBlue()) / 256);
            n.setColor(c);

            n.setX((float) ((0.01 + Math.random()) * 1000) - 500);
            n.setY((float) ((0.01 + Math.random()) * 1000) - 500);

            graph.addNode(n);
            nodes.put(currentNode, n);
            outMap.put(n, currentNode);
        });

        for (CMEdgeDO edge : ciGraph.edgeList) {
            int distanceLbl = graphModel.addEdgeType(edge.getEdgeTypeString());
            Edge e = graphModel.factory().newEdge(nodes.get(edge.getNode1()), nodes.get(edge.getNode2()), distanceLbl, edge.getScore(), false);
            e.setLabel("");
            Color color = new Color((float) (COLOR_SEMANTIC.getRed()) / 256, (float) (COLOR_SEMANTIC.getGreen()) / 256,
                    (float) (COLOR_SEMANTIC.getBlue()) / 256);
            if (edge.getEdgeType().equals(CMEdgeType.Syntactic)) {
                color = new Color((float) (COLOR_SYNTATIC.getRed()) / 256, (float) (COLOR_SYNTATIC.getGreen()) / 256,
                        (float) (COLOR_SYNTATIC.getBlue()) / 256);
            }
            e.setColor(color);

            graph.addEdge(e);
        }

        return outMap;
    }

    private Color getNodeColor(CMNodeDO node) {
        Color c = null;
        if (node.getNodeType().equals(CMNodeType.Inferred)) {
            c = COLOR_SEMANTIC;
        }
        if (node.getNodeType().equals(CMNodeType.TextBased)) {
            c = COLOR_SYNTATIC;
        }
        if (!node.isActive()) {
            c = COLOR_INACTIVE;
        }
        return c;
    }
}
