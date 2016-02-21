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
import org.gephi.appearance.api.AppearanceController;
import org.gephi.appearance.api.AppearanceModel;
import org.gephi.appearance.api.Function;
import org.gephi.appearance.plugin.RankingLabelSizeTransformer;
import org.gephi.appearance.plugin.RankingNodeSizeTransformer;
import org.gephi.graph.api.Column;
import org.gephi.graph.api.DirectedGraph;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.io.exporter.api.ExportController;
import org.gephi.layout.plugin.forceAtlas2.ForceAtlas2;
import org.gephi.preview.api.G2DTarget;
import org.gephi.preview.api.PreviewController;
import org.gephi.preview.api.PreviewModel;
import org.gephi.preview.api.PreviewProperty;
import org.gephi.preview.api.RenderTarget;
import org.gephi.preview.types.DependantOriginalColor;
import org.gephi.project.api.ProjectController;
import org.gephi.statistics.plugin.GraphDistance;
import org.openide.util.Lookup;

import data.cscl.Participant;
import services.discourse.CSCL.ParticipantEvaluation;
import view.models.PreviewSketch;

public class ParticipantInteractionView extends JFrame {
	private static final long serialVersionUID = 2571577554857108582L;

	static Logger logger = Logger.getLogger(ParticipantInteractionView.class);

	private List<Participant> participants;
	private double[][] participantContributions;
	private JPanel panelGraph;
	private String genericName;
	private String path;

	public ParticipantInteractionView(String genericName, String path, List<Participant> participants,
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
		logger.info("Generating participant interaction view");
		ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
		pc.newProject();

		// get models
		GraphModel graphModel = Lookup.getDefault().lookup(GraphController.class).getGraphModel();
		DirectedGraph graph = graphModel.getDirectedGraph();
		AppearanceController appearanceController = Lookup.getDefault().lookup(AppearanceController.class);
		AppearanceModel appearanceModel = appearanceController.getModel();

		ParticipantEvaluation.buildParticipantGraph(genericName, graph, graphModel, this.participants,
				this.participantContributions, displayEdgeLabels, isAnonymized);

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

		// Get centrality
		GraphDistance distance = new GraphDistance();
		distance.setDirected(true);
		distance.execute(graphModel);

		// Rank size by centrality
		Column centralityColumn = graphModel.getNodeTable().getColumn(GraphDistance.BETWEENNESS);
		Function centralityRanking = appearanceModel.getNodeFunction(graph, centralityColumn,
				RankingNodeSizeTransformer.class);
		RankingNodeSizeTransformer centralityTransformer = (RankingNodeSizeTransformer) centralityRanking
				.getTransformer();
		centralityTransformer.setMinSize(3);
		centralityTransformer.setMaxSize(10);
		appearanceController.transform(centralityRanking);

		// Rank label size - set a multiplier size
		Function centralityRanking2 = appearanceModel.getNodeFunction(graph, centralityColumn,
				RankingLabelSizeTransformer.class);
		RankingLabelSizeTransformer labelSizeTransformer = (RankingLabelSizeTransformer) centralityRanking2
				.getTransformer();
		labelSizeTransformer.setMinSize(1);
		labelSizeTransformer.setMaxSize(3);
		appearanceController.transform(centralityRanking2);

		// Preview configuration
		PreviewController previewController = Lookup.getDefault().lookup(PreviewController.class);
		PreviewModel previewModel = previewController.getModel();
		previewModel.getProperties().putValue(PreviewProperty.SHOW_NODE_LABELS, Boolean.TRUE);
		previewModel.getProperties().putValue(PreviewProperty.NODE_LABEL_COLOR,
				new DependantOriginalColor(Color.BLACK));
		previewModel.getProperties().putValue(PreviewProperty.EDGE_CURVED, Boolean.TRUE);
		previewModel.getProperties().putValue(PreviewProperty.EDGE_RADIUS, 10f);
		previewModel.getProperties().putValue(PreviewProperty.SHOW_EDGE_LABELS, Boolean.TRUE);
		previewModel.getProperties().putValue(PreviewProperty.NODE_LABEL_PROPORTIONAL_SIZE, Boolean.FALSE);

		// New Processing target, get the PApplet
		G2DTarget target = (G2DTarget) previewController.getRenderTarget(RenderTarget.G2D_TARGET);
		PreviewSketch previewSketch = new PreviewSketch(target);
		previewController.refreshPreview();
		previewSketch.resetZoom();

		panelGraph.add(previewSketch, BorderLayout.CENTER);
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
