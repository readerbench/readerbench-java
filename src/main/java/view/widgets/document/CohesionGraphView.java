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
package view.widgets.document;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Logger;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JFrame;
import javax.swing.JPanel;

import org.gephi.appearance.api.AppearanceController;
import org.gephi.appearance.api.AppearanceModel;
import org.gephi.appearance.api.Function;
import org.gephi.appearance.plugin.RankingLabelSizeTransformer;
import org.gephi.appearance.plugin.RankingNodeSizeTransformer;
import org.gephi.graph.api.Column;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.UndirectedGraph;
import org.gephi.layout.plugin.force.StepDisplacement;
import org.gephi.layout.plugin.force.yifanHu.YifanHuLayout;
import org.gephi.preview.api.G2DTarget;
import org.gephi.preview.api.PreviewController;
import org.gephi.preview.api.PreviewModel;
import org.gephi.preview.api.PreviewProperty;
import org.gephi.preview.api.RenderTarget;
import org.gephi.preview.types.DependantOriginalColor;
import org.gephi.project.api.ProjectController;
import org.gephi.statistics.plugin.GraphDistance;
import org.openide.util.Lookup;

import data.AbstractDocument;
import data.Block;
import data.discourse.SemanticCohesion;
import java.util.logging.Level;
import services.commons.Formatting;
import view.models.PreviewSketch;

public class CohesionGraphView extends JFrame {

    private static final long serialVersionUID = -5991280949453890249L;
    static Logger logger = Logger.getLogger("");

    private AbstractDocument doc;
    private JPanel panelGraph;

    public CohesionGraphView(AbstractDocument doc) {
        super("ReaderBench - Multi-layered Cohesion Graph");
        super.setBackground(Color.WHITE);
        this.doc = doc;
        super.getContentPane().setBackground(Color.WHITE);

        panelGraph = new JPanel();
        panelGraph.setBackground(Color.WHITE);
        panelGraph.setLayout(new BorderLayout(0, 0));
        GroupLayout groupLayout = new GroupLayout(super.getContentPane());
        groupLayout.setHorizontalGroup(groupLayout.createParallelGroup(Alignment.LEADING).addComponent(panelGraph,
                GroupLayout.DEFAULT_SIZE, 1804, Short.MAX_VALUE));
        groupLayout.setVerticalGroup(groupLayout.createParallelGroup(Alignment.LEADING).addComponent(panelGraph,
                GroupLayout.DEFAULT_SIZE, 942, Short.MAX_VALUE));
        super.getContentPane().setLayout(groupLayout);
        super.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        // adjust view to desktop size
        int margin = 50;
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        super.setBounds(margin, margin, screenSize.width - margin * 2, screenSize.height - margin * 2);

        generateGraph();
    }

    public void buildUtteranceGraph(UndirectedGraph graph, GraphModel graphModel, AbstractDocument d) {
        Node[] blockNodes = new Node[d.getBlocks().size()];
        Map<Integer, Node[]> sentenceNodes = new TreeMap<>();

        Color colorSentence = new Color(102, 102, 255); // orchid
        Color colorBlock = new Color(204, 204, 204); // silver
        Color colorDocument = new Color(170, 17, 17); // red tamarillor

        int hierachicalLbl = graphModel.addEdgeType("hierachical");
        int transitionLbl = graphModel.addEdgeType("transition");
        int interBlockLbl = graphModel.addEdgeType("inter-block");
        int interSentenceLbl = graphModel.addEdgeType("inter-sentence");

        // build document
        Node document = graphModel.factory().newNode("Document");
        document.setLabel("Document");
        document.setColor(new Color((float) (colorDocument.getRed()) / 256, (float) (colorDocument.getGreen()) / 256,
                (float) (colorDocument.getBlue()) / 256));
        document.setX((float) ((0.01 + Math.random()) * 1000) - 500);
        document.setY((float) ((0.01 + Math.random()) * 1000) - 500);
        graph.addNode(document);

        // build all nodes
        int globalIndex = 0;
        for (Block b : d.getBlocks()) {
            if (b != null) {
                // build block element
                Node block = graphModel.factory().newNode("Block " + b.getIndex());
                block.setLabel("Block " + b.getIndex());
                block.setColor(new Color((float) (colorBlock.getRed()) / 256, (float) (colorBlock.getGreen()) / 256,
                        (float) (colorBlock.getBlue()) / 256));
                block.setX((float) ((0.01 + Math.random()) * 1000) - 500);
                block.setY((float) ((0.01 + Math.random()) * 1000) - 500);
                graph.addNode(block);
                blockNodes[b.getIndex()] = block;

                sentenceNodes.put(b.getIndex(), new Node[b.getSentences().size()]);

                // add utterances
                for (int i = 0; i < b.getSentences().size(); i++) {
                    Node sentence = graphModel.factory().newNode("S " + globalIndex);
                    sentence.setLabel("S " + globalIndex);
                    sentence.setX((float) ((0.01 + Math.random()) * 1000) - 500);
                    sentence.setY((float) ((0.01 + Math.random()) * 1000) - 500);
                    globalIndex++;
                    sentence.setColor(new Color((float) (colorSentence.getRed()) / 256,
                            (float) (colorSentence.getGreen()) / 256, (float) (colorSentence.getBlue()) / 256));
                    graph.addNode(sentence);
                    sentenceNodes.get(b.getIndex())[i] = sentence;
                }
            }
        }

        // add all edges to document
        for (int i = 0; i < d.getBlocks().size(); i++) {
            // add edge to block
            SemanticCohesion coh = d.getBlockDocDistances()[i];
            Edge e = graphModel.factory().newEdge(blockNodes[i], document, hierachicalLbl, coh.getCohesion(), false);
            e.setLabel(Formatting.formatNumber(coh.getCohesion()) + "");
            graph.addEdge(e);
        }

        // add all edges between blocks
        for (int i = 0; i < d.getBlocks().size() - 1; i++) {
            for (int j = i + 1; j < d.getBlocks().size(); j++) {
                if (d.getPrunnedBlockDistances()[i][j] != null) {
                    double dist = d.getPrunnedBlockDistances()[i][j].getCohesion();
                    Edge e = graphModel.factory().newEdge(blockNodes[i], blockNodes[j], interBlockLbl, dist, false);
                    e.setLabel(Formatting.formatNumber(dist) + "");
                    graph.addEdge(e);
                }
            }
        }

        for (Block b : d.getBlocks()) {
            if (b != null) {
                // add edges to corresponding blocks
                for (int i = 0; i < b.getSentences().size(); i++) {
                    SemanticCohesion coh = b.getSentenceBlockDistances()[i];
                    Edge e = graphModel.factory().newEdge(blockNodes[b.getIndex()], sentenceNodes.get(b.getIndex())[i],
                            hierachicalLbl, coh.getCohesion(), false);
                    e.setLabel(Formatting.formatNumber(coh.getCohesion()) + "");
                    graph.addEdge(e);
                }
                // add all edges between sentences
                for (int i = 0; i < b.getSentences().size() - 1; i++) {
                    for (int j = i + 1; j < b.getSentences().size(); j++) {
                        if (b.getPrunnedSentenceDistances()[i][j] != null) {
                            double dist = b.getPrunnedSentenceDistances()[i][j].getCohesion();
                            Edge e = graphModel.factory().newEdge(sentenceNodes.get(b.getIndex())[i],
                                    sentenceNodes.get(b.getIndex())[j], interSentenceLbl, dist, false);
                            e.setLabel(Formatting.formatNumber(dist) + "");
                            graph.addEdge(e);
                        }
                    }
                }
                // add edges to previous or next block
                if (b.getPrevSentenceBlockDistance() != null && b.getSentences().size() > 0) {
                    SemanticCohesion coh = b.getPrevSentenceBlockDistance();
                    Edge e = graphModel.factory().newEdge(blockNodes[coh.getDestination().getIndex()],
                            sentenceNodes.get(b.getIndex())[0], transitionLbl, coh.getCohesion(), false);
                    e.setLabel(Formatting.formatNumber(coh.getCohesion()) + "");
                    graph.addEdge(e);
                }
                if (b.getNextSentenceBlockDistance() != null && b.getSentences().size() > 0) {
                    SemanticCohesion coh = b.getNextSentenceBlockDistance();
                    Edge e = graphModel.factory().newEdge(blockNodes[coh.getSource().getIndex()],
                            sentenceNodes.get(b.getIndex())[b.getSentences().size() - 1], transitionLbl,
                            coh.getCohesion(), false);
                    e.setLabel(Formatting.formatNumber(coh.getCohesion()) + "");
                    graph.addEdge(e);
                }
            }
        }

        logger.log(Level.INFO, "Generated graph with {0} nodes and {1} edges", new Object[]{graph.getNodeCount(), graph.getEdgeCount()});
    }

    private void generateGraph() {
        ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
        pc.newProject();

        // get models
        GraphModel graphModel = Lookup.getDefault().lookup(GraphController.class).getGraphModel();
        UndirectedGraph graph = graphModel.getUndirectedGraph();
        AppearanceController appearanceController = Lookup.getDefault().lookup(AppearanceController.class);
        AppearanceModel appearanceModel = appearanceController.getModel();

        buildUtteranceGraph(graph, graphModel, doc);

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
        distance.setDirected(false);
        distance.execute(graphModel);

        // Rank size by centrality
        Column centralityColumn = graphModel.getNodeTable().getColumn(GraphDistance.BETWEENNESS);
        Function centralityRanking = appearanceModel.getNodeFunction(graph, centralityColumn,
                RankingNodeSizeTransformer.class);
        RankingNodeSizeTransformer centralityTransformer = (RankingNodeSizeTransformer) centralityRanking
                .getTransformer();
        centralityTransformer.setMinSize(5);
        centralityTransformer.setMaxSize(40);
        appearanceController.transform(centralityRanking);

        // Rank label size - set a multiplier size
        Function centralityRanking2 = appearanceModel.getNodeFunction(graph, centralityColumn,
                RankingLabelSizeTransformer.class);
        RankingLabelSizeTransformer labelSizeTransformer = (RankingLabelSizeTransformer) centralityRanking2
                .getTransformer();
        labelSizeTransformer.setMinSize(1);
        labelSizeTransformer.setMaxSize(5);
        appearanceController.transform(centralityRanking2);

        // Preview configuration
        PreviewController previewController = Lookup.getDefault().lookup(PreviewController.class);
        PreviewModel previewModel = previewController.getModel();
        previewModel.getProperties().putValue(PreviewProperty.SHOW_NODE_LABELS, Boolean.TRUE);
        previewModel.getProperties().putValue(PreviewProperty.NODE_LABEL_COLOR,
                new DependantOriginalColor(Color.BLACK));
        previewModel.getProperties().putValue(PreviewProperty.EDGE_CURVED, Boolean.FALSE);
        previewModel.getProperties().putValue(PreviewProperty.EDGE_RADIUS, 10f);
        previewModel.getProperties().putValue(PreviewProperty.SHOW_EDGE_LABELS, Boolean.TRUE);
        previewModel.getProperties().putValue(PreviewProperty.NODE_LABEL_PROPORTIONAL_SIZE, Boolean.FALSE);
        previewModel.getProperties().putValue(PreviewProperty.EDGE_CURVED, Boolean.TRUE);

        // New Processing target, get the PApplet
        G2DTarget target = (G2DTarget) previewController.getRenderTarget(RenderTarget.G2D_TARGET);
        PreviewSketch previewSketch = new PreviewSketch(target);
        previewController.refreshPreview();
        previewSketch.resetZoom();
        panelGraph.add(previewSketch, BorderLayout.CENTER);
    }
}
