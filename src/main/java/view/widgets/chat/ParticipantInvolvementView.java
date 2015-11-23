package view.widgets.chat;

import java.awt.BorderLayout;
import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JFrame;
import javax.swing.JPanel;

import org.apache.log4j.Logger;
import org.gephi.data.attributes.api.AttributeColumn;
import org.gephi.data.attributes.api.AttributeController;
import org.gephi.data.attributes.api.AttributeModel;
import org.gephi.graph.api.DirectedGraph;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Node;
import org.gephi.io.exporter.api.ExportController;
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

import data.cscl.Participant;
import processing.core.PApplet;
import services.discourse.CSCL.ParticipantEvaluation;

public class ParticipantInvolvementView extends JFrame {
	private static final long serialVersionUID = 2571577554857108582L;

	static Logger logger = Logger.getLogger(ParticipantInvolvementView.class);

	private List<Participant> participants;
	private double[][] participantContributions;
	private JPanel panelGraph;
	private String genericName;
	private String path;

	public ParticipantInvolvementView(String genericName, String path, List<Participant> participants,
			double[][] participantContributions, boolean displayEdgeLabels, boolean isAnonymized) {
		super();
		setTitle("ReaderBench - Participant Interaction");
		setBackground(Color.WHITE);
		this.participants = participants;
		this.genericName = genericName;
		this.path = path;
		this.participantContributions = participantContributions;
		getContentPane().setBackground(Color.WHITE);

		panelGraph = new JPanel();
		panelGraph.setBackground(Color.WHITE);
		panelGraph.setLayout(new BorderLayout(0, 0));
		GroupLayout groupLayout = new GroupLayout(getContentPane());
		groupLayout.setHorizontalGroup(groupLayout.createParallelGroup(Alignment.LEADING).addComponent(panelGraph,
				Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, 784, Short.MAX_VALUE));
		groupLayout.setVerticalGroup(groupLayout.createParallelGroup(Alignment.LEADING).addComponent(panelGraph,
				Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, 562, Short.MAX_VALUE));
		getContentPane().setLayout(groupLayout);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		setBounds(50, 50, 800, 600);

		generateGraph(displayEdgeLabels, isAnonymized);
	}

	private void generateGraph(boolean displayEdgeLabels, boolean isAnonymized) {
		ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
		pc.newProject();

		// get models
		GraphModel graphModel = Lookup.getDefault().lookup(GraphController.class).getModel();
		AttributeModel attributeModel = Lookup.getDefault().lookup(AttributeController.class).getModel();
		DirectedGraph graph = graphModel.getDirectedGraph();

		ParticipantEvaluation.buildParticipantGraph(genericName, graph, graphModel, this.participants,
				this.participantContributions, displayEdgeLabels, isAnonymized);

		YifanHuLayout layout = new YifanHuLayout(null, new StepDisplacement(1f));
		layout.setGraphModel(graphModel);
		layout.initAlgo();
		layout.resetPropertiesValues();
		layout.setOptimalDistance(500f);

		for (int i = 0; i < 100 && layout.canAlgo(); i++) {
			layout.goAlgo();
		}
		layout.endAlgo();

		layout.setGraphModel(graphModel);

		RankingController rankingController = Lookup.getDefault().lookup(RankingController.class);

		// Get centrality
		GraphDistance distance = new GraphDistance();
		distance.setDirected(true);
		distance.execute(graphModel, attributeModel);

		// Rank size by centrality
		AttributeColumn centralityColumn = attributeModel.getNodeTable().getColumn(GraphDistance.BETWEENNESS);

		// if all scores are 0, change centrality measure
		boolean allZero = true;
		for (Node n : graph.getNodes()) {
			allZero = allZero && ((Double) n.getNodeData().getAttributes().getValue(centralityColumn.getIndex()) == 0);
		}
		if (allZero) {
			centralityColumn = attributeModel.getNodeTable().getColumn(GraphDistance.CLOSENESS);
		}
		Ranking<?> centralityRanking = rankingController.getModel().getRanking(Ranking.NODE_ELEMENT,
				centralityColumn.getId());
		AbstractSizeTransformer<?> sizeTransformer = (AbstractSizeTransformer<?>) rankingController.getModel()
				.getTransformer(Ranking.NODE_ELEMENT, Transformer.RENDERABLE_SIZE);
		sizeTransformer.setMinSize(3);
		sizeTransformer.setMaxSize(20);
		rankingController.transform(centralityRanking, sizeTransformer);

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
		ExportController ec = Lookup.getDefault().lookup(ExportController.class);
		try {
			ec.exportFile(new File(path));
		} catch (IOException ex) {
			ex.printStackTrace();
			return;
		}
		this.pack();
	}
}
