package view.widgets.article;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Toolkit;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
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
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.EtchedBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
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

import data.article.ResearchArticle;
import services.commons.Formatting;
import view.models.PreviewSketch;
import view.widgets.article.utils.ArticleContainer;
import view.widgets.article.utils.AuthorParameterLogger;
import view.widgets.article.utils.CachedAuthorDistanceStrategyDecorator;
import view.widgets.article.utils.GraphMeasure;
import view.widgets.article.utils.GraphNodeItem;
import view.widgets.article.utils.SingleAuthorContainer;
import view.widgets.article.utils.distanceStrategies.AuthorDistanceStrategyFactory;
import view.widgets.article.utils.distanceStrategies.AuthorDistanceStrategyType;
import view.widgets.article.utils.distanceStrategies.IAuthorDistanceStrategy;

public class ArticleAuthorSimilarityView extends JFrame {
	static ArticleAuthorSimilarityView corpusView;
	private static final long serialVersionUID = -8582615231233815258L;
	static Logger logger = Logger.getLogger(ArticleAuthorSimilarityView.class);
	public static final Color COLOR_AUTHOR = new Color(10, 255, 0); // silver
	public static final Color COLOR_ARTICLE = new Color(255, 10, 0);

	private IAuthorDistanceStrategy[] distanceStrategyList;
	private ArticleContainer authorContainer;
	private AuthorParameterLogger paramLogger;

	private JSlider sliderThreshold;
	private JPanel panelGraph;

	public ArticleAuthorSimilarityView(ArticleContainer authorContainer, IAuthorDistanceStrategy[] distanceStrategyList,
			AuthorParameterLogger paramLogger) {
		this.authorContainer = authorContainer;
		this.distanceStrategyList = distanceStrategyList;
		this.paramLogger = paramLogger;

		corpusView = this;
		setTitle("Author & Document View");
		getContentPane().setBackground(Color.WHITE);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		// adjust view to desktop size
		int margin = 50;
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		setBounds(margin, margin, screenSize.width - margin * 2, screenSize.height - margin * 2);

		generateLayout();
		generateGraph();
	}

	private void generateLayout() {

		JLabel lblThreshold = new JLabel("Threshold");
		lblThreshold.setFont(new Font("SansSerif", Font.BOLD, 12));

		sliderThreshold = new JSlider(0, 100, 80);
		sliderThreshold.setBackground(Color.WHITE);
		sliderThreshold.setPaintTicks(true);
		sliderThreshold.setFont(new Font("SansSerif", Font.PLAIN, 10));
		sliderThreshold.setPaintLabels(true);
		sliderThreshold.setMinorTickSpacing(10);
		sliderThreshold.setMajorTickSpacing(50);
		java.util.Hashtable<Integer, JLabel> labelTableThreshold = new java.util.Hashtable<Integer, JLabel>();
		labelTableThreshold.put(new Integer(100), new JLabel("100%"));
		labelTableThreshold.put(new Integer(50), new JLabel("50%"));
		labelTableThreshold.put(new Integer(0), new JLabel("0"));
		sliderThreshold.setLabelTable(labelTableThreshold);
		sliderThreshold.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				generateGraph();
			}
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

	public HashMap<Node, GraphNodeItem> buildConceptGraph(UndirectedGraph graph, GraphModel graphModel,
			double threshold) {
		HashMap<Node, GraphNodeItem> outMap = new HashMap<Node, GraphNodeItem>();
		logger.info("Starting to build the author graph");
		// build connected graph
		Map<GraphNodeItem, Boolean> visibleDocs = new TreeMap<GraphNodeItem, Boolean>();
		// build nodes
		Map<GraphNodeItem, Node> nodes = new TreeMap<GraphNodeItem, Node>();
		
		List<GraphNodeItem> nodeItemList = new ArrayList<GraphNodeItem>();
		
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
		
		for(IAuthorDistanceStrategy distanceStrategy : this.distanceStrategyList) {
			int distanceLbl = graphModel.addEdgeType(distanceStrategy.getStrategyKey());
			// determine similarities in order to determine eligible candidates for vizualization
			for (int i = 0; i < nodeItemList.size() - 1; i++) {
				for (int j = i + 1; j < nodeItemList.size(); j++) {
					GraphNodeItem firstNodeItem = nodeItemList.get(i);
					GraphNodeItem secondNodeItem = nodeItemList.get(j);
					
					double sim = firstNodeItem.computeScore(secondNodeItem, distanceStrategy);
					if (sim >= threshold) {
						visibleDocs.put(firstNodeItem, true);
						visibleDocs.put(secondNodeItem, true);
					}
				}
			}
			for (GraphNodeItem o : nodeItemList) {
				if (visibleDocs.get(o) && ! nodes.containsKey(o)) {
					String text = o.getName();
					Color c = null;
					if(o.isArticle()) {
						c = new Color((float) (COLOR_ARTICLE.getRed()) / 256, (float) (COLOR_ARTICLE.getGreen()) / 256,
								(float) (COLOR_ARTICLE.getBlue()) / 256);
					}
					else {
						c = new Color((float) (COLOR_AUTHOR.getRed()) / 256, (float) (COLOR_AUTHOR.getGreen()) / 256,
								(float) (COLOR_AUTHOR.getBlue()) / 256);
					}
					text = (text.length() > 25) ? (text.substring(0, 25) + "..") : text;
					Node n = graphModel.factory().newNode(o.getURI());
					n.setLabel(text);
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
							e.setLabel(Formatting.formatNumber(sim) + "");
							graph.addEdge(e);
						}
					}
				}
			}
		}
		return outMap;
	}
	
	private void generateGraph() {
		double threshold = ((double) sliderThreshold.getValue()) / 100;

		ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
		pc.newProject();

		// get models
		GraphModel graphModel = Lookup.getDefault().lookup(GraphController.class).getGraphModel();
		UndirectedGraph graph = graphModel.getUndirectedGraph();
		AppearanceController appearanceController = Lookup.getDefault().lookup(AppearanceController.class);
		AppearanceModel appearanceModel = appearanceController.getModel();

		HashMap<Node, GraphNodeItem> nodeMap = buildConceptGraph(graph, graphModel, threshold);

		// Get Centrality
		GraphDistance distance = new GraphDistance();
		distance.setDirected(true);
		distance.execute(graphModel);

		double maxCentrality = Double.NEGATIVE_INFINITY;
		Column betweeennessColumn = graphModel.getNodeTable().getColumn(GraphDistance.BETWEENNESS);
		Column closenessColumn = graphModel.getNodeTable().getColumn(GraphDistance.CLOSENESS);
		Column eccentricityColumn = graphModel.getNodeTable().getColumn(GraphDistance.ECCENTRICITY);
		List<GraphMeasure> graphMeasures = new ArrayList<GraphMeasure>();
		for (Node n : graph.getNodes()) {
			Double betwenness = (Double) n.getAttribute(betweeennessColumn);
			Double eccentricity = (Double) n.getAttribute(eccentricityColumn);
			Double closeness = (Double) n.getAttribute(closenessColumn);

			GraphNodeItem currentDoc = nodeMap.get(n);
			int degree = graph.getDegree(n);

			GraphMeasure graphMeasure = new GraphMeasure();
			graphMeasure.setAuthorUri(currentDoc.getURI());
			graphMeasure.setBetwenness(betwenness);
			graphMeasure.setCloseness(closeness);
			graphMeasure.setDegree(new Double(degree));
			graphMeasure.setEccentricity(eccentricity);
			graphMeasures.add(graphMeasure);

			if (betwenness > maxCentrality) {
				maxCentrality = betwenness;
			}
		}
		//paramLogger.logGraphMeasures(this.distanceStrategy, graphMeasures, (new Double(threshold * 100)).intValue());

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

		// Export
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

	@Override
	public void paint(Graphics g) {
		super.paint(g);
		revalidate();
	}

	private static void adjustToSystemGraphics() {
		for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
			if ("Nimbus".equals(info.getName())) {
				try {
					UIManager.setLookAndFeel(info.getClassName());
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				} catch (InstantiationException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				} catch (UnsupportedLookAndFeelException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public static void main(String[] args) {
		BasicConfigurator.configure();
		adjustToSystemGraphics();
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				String inDir = "in/LAK_corpus/parsed-documents";

				ArticleContainer container = ArticleContainer.buildAuthorContainerFromDirectory(inDir);
				AuthorDistanceStrategyFactory distStrategyFactory = new AuthorDistanceStrategyFactory(container);

				IAuthorDistanceStrategy semanticDistStrategy = distStrategyFactory.getDistanceStrategy(AuthorDistanceStrategyType.SemanticDistance);
				CachedAuthorDistanceStrategyDecorator cachedSemanticDistStrategy = new CachedAuthorDistanceStrategyDecorator(container, semanticDistStrategy);

				IAuthorDistanceStrategy coAuthDistStrategy = distStrategyFactory.getDistanceStrategy(AuthorDistanceStrategyType.CoAuthorshipDistance);
				CachedAuthorDistanceStrategyDecorator cachedCoAuthDistStrategy = new CachedAuthorDistanceStrategyDecorator(container, coAuthDistStrategy);

				IAuthorDistanceStrategy coCitationsDistStrategy = distStrategyFactory.getDistanceStrategy(AuthorDistanceStrategyType.CoCitationsDistance);
				CachedAuthorDistanceStrategyDecorator cachedCoCitationsDistStrategy = new CachedAuthorDistanceStrategyDecorator(container, coCitationsDistStrategy);

				IAuthorDistanceStrategy[] allStrategies = new IAuthorDistanceStrategy[] { cachedSemanticDistStrategy, cachedCoAuthDistStrategy, cachedCoCitationsDistStrategy };
				AuthorParameterLogger paramLogger = new AuthorParameterLogger(container);

				for (IAuthorDistanceStrategy strategy : allStrategies) {
					paramLogger.logTopSimilarAuthors(strategy, allStrategies);
				}

				ArticleAuthorSimilarityView view = new ArticleAuthorSimilarityView(container, allStrategies, paramLogger);
				view.setVisible(true);
			}
		});
	}
}