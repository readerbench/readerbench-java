package view.widgets.comprehensionModel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
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

import org.apache.log4j.BasicConfigurator;
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
import org.openide.util.Lookup;

import services.comprehensionModel.ComprehensionModel;
import services.comprehensionModel.utils.indexer.WordDistanceIndexer;
import services.comprehensionModel.utils.indexer.graphStruct.CiEdgeDO;
import services.comprehensionModel.utils.indexer.graphStruct.CiEdgeType;
import services.comprehensionModel.utils.indexer.graphStruct.CiGraphDO;
import services.comprehensionModel.utils.indexer.graphStruct.CiNodeDO;
import services.comprehensionModel.utils.indexer.graphStruct.CiNodeType;
import view.models.PreviewSketch;

public class ComprehensionModelView extends JFrame {
	private static final long serialVersionUID = 1L;

	static Logger logger = Logger.getLogger(ComprehensionModelView.class);
	private ComprehensionModel ciModel;
	private int sentenceIndex;
	public static final Color COLOR_SEMANTIC = new Color(255, 10, 0);
	public static final Color COLOR_SYNTATIC = new Color(0, 21, 255);
	public static final Color COLOR_INACTIVE = new Color(170, 170, 170);
	public static final Color COLOR_ACTIVE = new Color(59, 153, 50);

	JLabel phraseLabel;
	JButton btnNextPhrase;
	JPanel panelGraph;

	public ComprehensionModelView(ComprehensionModel ciModel) {
		this.ciModel = ciModel;
		this.sentenceIndex = 0;

		this.setDefaultWindowSize();
		this.generateLayout();
		this.updateValuesForCurrentSentence();
	}

	private void generateLayout() {
		this.phraseLabel = new JLabel("New label");
		this.btnNextPhrase = new JButton("Next Phrase");
		this.btnNextPhrase.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ComprehensionModelView.this.increaseSentenceIndex();
			}
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
		if (this.sentenceIndex < this.ciModel.getTotalNoOfPhrases() - 1) {
			this.sentenceIndex++;
			this.updateValuesForCurrentSentence();
		}
	}

	private void updateValuesForCurrentSentence() {
		this.phraseLabel.setText(this.ciModel.getSentenceAtIndex(this.sentenceIndex).getText());
		this.generateGraph();
	}

	private void generateGraph() {
		ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
		pc.newProject();

		// get models
		GraphModel graphModel = Lookup.getDefault().lookup(GraphController.class).getGraphModel();
		UndirectedGraph graph = graphModel.getUndirectedGraph();
		AppearanceController appearanceController = Lookup.getDefault().lookup(AppearanceController.class);
		AppearanceModel appearanceModel = appearanceController.getModel();

		HashMap<Node, CiNodeDO> nodeMap = buildConceptGraph(graph, graphModel);

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
			ec.exportFile(new File("out/graph_doc_corpus_view.pdf"));
		} catch (IOException ex) {
			ex.printStackTrace();
			return;
		}
		this.pack();
		logger.info("Finished building the graph");
	}

	public HashMap<Node, CiNodeDO> buildConceptGraph(UndirectedGraph graph, GraphModel graphModel) {
		HashMap<Node, CiNodeDO> outMap = new HashMap<Node, CiNodeDO>();
		logger.info("Starting to build the ci graph");

		// build nodes
		Map<CiNodeDO, Node> nodes = new TreeMap<CiNodeDO, Node>();

		List<CiNodeDO> nodeItemList = new ArrayList<CiNodeDO>();

		this.ciModel.applyPageRank();
		WordDistanceIndexer syntacticIndexer = this.ciModel.getSyntacticIndexerAtIndex(this.sentenceIndex);

		CiGraphDO ciGraph = syntacticIndexer.getCiGraph(CiNodeType.Syntactic);
		CiGraphDO semanticGraph = this.ciModel.getSemanticIndexer().getCiGraph(CiNodeType.Semantic);

		ciGraph.combineWithLinksFrom(semanticGraph);
		ciGraph = ciGraph.getCombinedGraph(this.ciModel.currentGraph);

		this.ciModel.currentGraph = ciGraph;
		
		System.out.println("--------------------");
		System.out.println(this.ciModel.getNodeActivationScoreMap());
		this.ciModel.updateActivationScoreMapAtIndex(this.sentenceIndex);
		System.out.println(this.ciModel.getNodeActivationScoreMap());

		nodeItemList = ciGraph.nodeList;

		for (CiNodeDO currentNode : nodeItemList) {
			String text = currentNode.word.getLemma();

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
		}
		for (CiEdgeDO edge : ciGraph.edgeList) {
			int distanceLbl = graphModel.addEdgeType(edge.getEdgeTypeString());
			Edge e = graphModel.factory().newEdge(nodes.get(edge.node1), nodes.get(edge.node2), distanceLbl, edge.score,
					false);
			e.setLabel("");
			Color color = new Color((float) (COLOR_SEMANTIC.getRed()) / 256, (float) (COLOR_SEMANTIC.getGreen()) / 256,
					(float) (COLOR_SEMANTIC.getBlue()) / 256);
			if (edge.edgeType == CiEdgeType.Syntactic) {
				color = new Color((float) (COLOR_SYNTATIC.getRed()) / 256, (float) (COLOR_SYNTATIC.getGreen()) / 256,
						(float) (COLOR_SYNTATIC.getBlue()) / 256);
			}
			e.setColor(color);

			graph.addEdge(e);
		}

		return outMap;
	}

	private Color getNodeColor(CiNodeDO node) {
		if (node.nodeType == CiNodeType.Semantic) {
			return COLOR_SEMANTIC;
		}
		if (node.nodeType == CiNodeType.Syntactic) {
			return COLOR_SYNTATIC;
		}
		if (node.nodeType == CiNodeType.Active) {
			return COLOR_ACTIVE;
		}
		return COLOR_INACTIVE;
	}

	public static void main(String[] args) {
		BasicConfigurator.configure();
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				ComprehensionModel ciModel = new ComprehensionModel(
						"I went to the coast last weekend with Sally. It was sunny. We had checked the tide schedules and planned to arrive at low tide. I just love beachcombing. Right off, I found three whole sand dollars.");
				ComprehensionModelView view = new ComprehensionModelView(ciModel);
				view.setVisible(true);
			}
		});
	}
}