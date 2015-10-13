package view.widgets.document;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.text.DecimalFormat;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JFrame;
import javax.swing.JPanel;

import org.apache.log4j.Logger;
import org.gephi.data.attributes.api.AttributeColumn;
import org.gephi.data.attributes.api.AttributeController;
import org.gephi.data.attributes.api.AttributeModel;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.MixedGraph;
import org.gephi.graph.api.Node;
import org.gephi.layout.plugin.force.StepDisplacement;
import org.gephi.layout.plugin.force.yifanHu.YifanHuLayout;
import org.gephi.preview.api.PreviewController;
import org.gephi.preview.api.PreviewModel;
import org.gephi.preview.api.PreviewProperty;
import org.gephi.preview.api.ProcessingTarget;
import org.gephi.preview.api.RenderTarget;
import org.gephi.project.api.ProjectController;
import org.gephi.ranking.api.Ranking;
import org.gephi.ranking.api.RankingController;
import org.gephi.ranking.api.Transformer;
import org.gephi.ranking.plugin.transformer.AbstractSizeTransformer;
import org.gephi.statistics.plugin.GraphDistance;
import org.openide.util.Lookup;

import DAO.AbstractDocument;
import DAO.Block;
import processing.core.PApplet;
import services.complexity.flow.DocumentFlow;

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

	public void buildUtteranceGraph(MixedGraph graph, GraphModel graphModel, AbstractDocument d) {
		DecimalFormat formatter = new DecimalFormat("#.###");

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
				block.getNodeData().setLabel("P" + b.getIndex() + ": " + content);
				block.getNodeData().setColor((float) (colorBlock.getRed()) / 256, (float) (colorBlock.getGreen()) / 256,
						(float) (colorBlock.getBlue()) / 256);
				graph.addNode(block);
				blockNodes[b.getIndex()] = block;
			}
		}

		// add all edges between blocks
		for (int i = 0; i < d.getBlocks().size() - 1; i++) {
			for (int j = i + 1; j < d.getBlocks().size(); j++) {
				if (df.getGraph()[i][j] > 0) {
					double dist = df.getGraph()[i][j];
					graph.addEdge(blockNodes[i], blockNodes[j], true);
					Edge e = graph.getEdge(blockNodes[i], blockNodes[j]);
					e.setWeight((float) dist);
					e.getEdgeData().setLabel(formatter.format(dist));
				}
			}
		}

		logger.info("Generated graph with " + graph.getNodeCount() + " nodes and " + graph.getEdgeCount() + " edges");
	}

	private void generateGraph() {
		ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
		pc.newProject();

		// get models
		GraphModel graphModel = Lookup.getDefault().lookup(GraphController.class).getModel();
		AttributeModel attributeModel = Lookup.getDefault().lookup(AttributeController.class).getModel();
		MixedGraph graph = graphModel.getMixedGraph();

		buildUtteranceGraph(graph, graphModel, doc);

		YifanHuLayout layout = new YifanHuLayout(null, new StepDisplacement(1f));
		layout.setGraphModel(graphModel);
		layout.initAlgo();
		layout.resetPropertiesValues();
		layout.setOptimalDistance(200f);

		for (int i = 0; i < 100 && layout.canAlgo(); i++) {
			layout.goAlgo();
		}
		layout.endAlgo();

		layout.setGraphModel(graphModel);

		RankingController rankingController = Lookup.getDefault().lookup(RankingController.class);

		// Get Centrality
		GraphDistance distance = new GraphDistance();
		distance.setDirected(true);
		distance.execute(graphModel, attributeModel);

		// Rank size by centrality
		AttributeColumn centralityColumn = attributeModel.getNodeTable().getColumn(GraphDistance.BETWEENNESS);
		Ranking<?> centralityRanking = rankingController.getModel().getRanking(Ranking.NODE_ELEMENT,
				centralityColumn.getId());
		AbstractSizeTransformer<?> sizeTransformer = (AbstractSizeTransformer<?>) rankingController.getModel()
				.getTransformer(Ranking.NODE_ELEMENT, Transformer.RENDERABLE_SIZE);
		sizeTransformer.setMinSize(5);
		sizeTransformer.setMaxSize(40);
		rankingController.transform(centralityRanking, sizeTransformer);

		// Rank label size - set a multiplier size
		Ranking<?> centralityRanking2 = rankingController.getModel().getRanking(Ranking.NODE_ELEMENT,
				centralityColumn.getId());
		AbstractSizeTransformer<?> labelSizeTransformer = (AbstractSizeTransformer<?>) rankingController.getModel()
				.getTransformer(Ranking.NODE_ELEMENT, Transformer.LABEL_SIZE);
		labelSizeTransformer.setMinSize(1);
		labelSizeTransformer.setMaxSize(5);
		rankingController.transform(centralityRanking2, labelSizeTransformer);

		// Preview configuration
		PreviewController previewController = Lookup.getDefault().lookup(PreviewController.class);
		PreviewModel previewModel = previewController.getModel();
		previewModel.getProperties().putValue(PreviewProperty.SHOW_NODE_LABELS, Boolean.TRUE);
		previewModel.getProperties().putValue(PreviewProperty.SHOW_EDGE_LABELS, Boolean.TRUE);
		previewModel.getProperties().putValue(PreviewProperty.NODE_LABEL_PROPORTIONAL_SIZE, Boolean.FALSE);
		previewModel.getProperties().putValue(PreviewProperty.EDGE_CURVED, Boolean.TRUE);
		previewController.refreshPreview();

		// New Processing target, get the PApplet
		ProcessingTarget target = (ProcessingTarget) previewController.getRenderTarget(RenderTarget.PROCESSING_TARGET);
		PApplet applet = target.getApplet();
		applet.init();
		try {
			Thread.sleep(100);
		} catch (Exception ex) {
			logger.error(ex.getMessage());
		}

		// Refresh the preview and reset the zoom
		previewController.render(target);
		target.refresh();
		target.resetZoom();
		panelGraph.add(applet, BorderLayout.CENTER);
		panelGraph.validate();
		panelGraph.repaint();

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
