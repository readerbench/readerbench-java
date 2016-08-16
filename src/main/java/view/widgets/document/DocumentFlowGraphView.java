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

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JFrame;
import javax.swing.JPanel;

import org.apache.log4j.Logger;
import org.gephi.appearance.api.AppearanceController;
import org.gephi.appearance.api.AppearanceModel;
import org.gephi.appearance.api.Function;
import org.gephi.appearance.plugin.RankingLabelSizeTransformer;
import org.gephi.appearance.plugin.RankingNodeSizeTransformer;
import org.gephi.graph.api.Column;
import org.gephi.graph.api.DirectedGraph;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Node;
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
import services.commons.Formatting;
import services.complexity.flow.DocumentFlow;
import view.models.PreviewSketch;

public class DocumentFlowGraphView extends JFrame {
	static Logger logger = Logger.getLogger(ConceptView.class);

	private static final long serialVersionUID = -5991280949453890249L;

	private static final int MAX_CONTENT_SIZE = 50;

	private AbstractDocument doc;
	private DocumentFlow df;
	private JPanel panelGraph;

	public DocumentFlowGraphView(AbstractDocument doc, DocumentFlow df) {
		super("ReaderBench - Document Flow Graph");
		setBackground(Color.WHITE);
		this.doc = doc;
		this.df = df;
		getContentPane().setBackground(Color.WHITE);

		panelGraph = new JPanel();
		panelGraph.setBackground(Color.WHITE);
		panelGraph.setLayout(new BorderLayout(0, 0));
		GroupLayout groupLayout = new GroupLayout(getContentPane());
		groupLayout.setHorizontalGroup(groupLayout.createParallelGroup(Alignment.LEADING).addComponent(panelGraph,
				GroupLayout.DEFAULT_SIZE, 1800, Short.MAX_VALUE));
		groupLayout.setVerticalGroup(groupLayout.createParallelGroup(Alignment.LEADING).addComponent(panelGraph,
				GroupLayout.DEFAULT_SIZE, 900, Short.MAX_VALUE));
		getContentPane().setLayout(groupLayout);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		// adjust view to desktop size
		int margin = 50;
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		setBounds(margin, margin, screenSize.width - margin * 2, screenSize.height - margin * 2);

		generateGraph();
	}

	public void buildUtteranceGraph(DirectedGraph graph, GraphModel graphModel, AbstractDocument d) {
		Node[] blockNodes = new Node[d.getBlocks().size()];

		Color colorBlock = new Color(204, 204, 204); // silver

		// build all nodes
		for (Block b : d.getBlocks()) {
			if (b != null) {
				// build block element
				Node block = graphModel.factory().newNode("P" + b.getIndex());
				String content = b.getText();
				if (content.length() > MAX_CONTENT_SIZE) {
					content = content.substring(0, MAX_CONTENT_SIZE);
					content = content.substring(0, content.lastIndexOf(" ")) + "...";
				}
				block.setLabel("P" + b.getIndex() + ": " + content);
				block.setColor(new Color((float) (colorBlock.getRed()) / 256, (float) (colorBlock.getGreen()) / 256,
						(float) (colorBlock.getBlue()) / 256));
				block.setX((float) ((0.01 + Math.random()) * 1000) - 500);
				block.setY((float) ((0.01 + Math.random()) * 1000) - 500);
				graph.addNode(block);
				blockNodes[b.getIndex()] = block;
			}
		}

		// add all edges between blocks
		for (int i = 0; i < d.getBlocks().size() - 1; i++) {
			for (int j = i + 1; j < d.getBlocks().size(); j++) {
				if (df.getGraph()[i][j] > 0) {
					double dist = df.getGraph()[i][j];

					Edge e = graphModel.factory().newEdge(blockNodes[i], blockNodes[j], 0, dist, true);
					e.setLabel(Formatting.formatNumber(dist) + "");
					graph.addEdge(e);
				}
			}
		}

		logger.info("Generated graph with " + graph.getNodeCount() + " nodes and " + graph.getEdgeCount() + " edges");
	}

	private void generateGraph() {
		ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
		pc.newProject();

		// get models
		GraphModel graphModel = Lookup.getDefault().lookup(GraphController.class).getGraphModel();
		DirectedGraph graph = graphModel.getDirectedGraph();
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
		distance.setDirected(true);
		distance.execute(graphModel);

		logger.info("Ranking size...");
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

		logger.info("Generating preview...");
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

		// Export
		// ExportController ec = Lookup.getDefault()
		// .lookup(ExportController.class);
		// try {
		// ec.exportFile(new File("out/graph.pdf"));
		// } catch (IOException ex) {
		// ex.printStackTrace();
		// return;
		// }
		// this.pack();
	}

}
