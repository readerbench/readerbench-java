package view.widgets.article;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTable;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.EtchedBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.gephi.data.attributes.api.AttributeColumn;
import org.gephi.data.attributes.api.AttributeController;
import org.gephi.data.attributes.api.AttributeModel;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.UndirectedGraph;
import org.gephi.io.exporter.api.ExportController;
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

import processing.core.PApplet;
import view.widgets.article.utils.AuthorContainer;
import view.widgets.article.utils.CachedAuthorDistanceStrategyDecorator;
import view.widgets.article.utils.TwoAuthorsDistanceContainer;
import view.widgets.article.utils.SingleAuthorContainer;
import view.widgets.article.utils.distanceStrategies.AuthorDistanceStrategyFactory;
import view.widgets.article.utils.distanceStrategies.AuthorDistanceStrategyType;
import view.widgets.article.utils.distanceStrategies.IAuthorDistanceStrategy;

public class ArticleAuthorSimilarityView extends JFrame {
	static ArticleAuthorSimilarityView corpusView;
	private static final long serialVersionUID = -8582615231233815258L;
	static Logger logger = Logger.getLogger(ArticleAuthorSimilarityView.class);
	public static final Color COLOR_CONCEPT = new Color(204, 204, 204); // silver

	private IAuthorDistanceStrategy distanceStrategy;
	private AuthorContainer authorContainer;
	
	private JSlider sliderThreshold;
	private JPanel panelGraph;

	JTable tableSimilarity;
	DefaultTableModel tableSimilarityModel;
	JTable tableCentrality;
	DefaultTableModel tableCentralityModel;

	SingleAuthorContainer centralAuthorContainerToCompare;

	class CompareCentralityElement {
		double centrality;
		SingleAuthorContainer author;

		public CompareCentralityElement(SingleAuthorContainer author, double centrality) {
			this.author = author;
			this.centrality = centrality;
		}
	}

	public ArticleAuthorSimilarityView(AuthorContainer authorContainer, IAuthorDistanceStrategy distanceStrategy) {
		this.authorContainer = authorContainer;
		this.distanceStrategy = distanceStrategy;
		
		centralAuthorContainerToCompare = null;
		corpusView = this;
		setTitle("Author View - " + this.distanceStrategy.getStrategyName());
		getContentPane().setBackground(Color.WHITE);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		
		
		// adjust view to desktop size
		int margin = 50;
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		setBounds(margin, margin, screenSize.width - margin * 2,
				screenSize.height - margin * 2);

		generateLayout();
		generateGraph();
	}

	private void generateLayout() {

		JLabel lblThreshold = new JLabel("Threshold");
		lblThreshold.setFont(new Font("SansSerif", Font.BOLD, 12));

		sliderThreshold = new JSlider(0, 100, 40);
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
		panelGraph
				.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		panelGraph.setBackground(Color.WHITE);
		panelGraph.setLayout(new BorderLayout());

		JLabel lblTopSimilarArticles = new JLabel("Top Similar Authors");
		lblTopSimilarArticles.setFont(new Font("SansSerif", Font.BOLD, 14));
		String[] header = { "Author 1", "Author 2",
				this.distanceStrategy.getStrategyName() + " Score" };
		String[][] data = new String[0][3];

		tableSimilarityModel = new DefaultTableModel(data, header);
		tableSimilarity = new JTable(tableSimilarityModel) {
			private static final long serialVersionUID = 1L;

			public boolean isCellEditable(int row, int column) {
				return false;
			};
		};
		tableSimilarity.getSelectionModel().addListSelectionListener(
				new ListSelectionListener() {
					public void valueChanged(ListSelectionEvent event) {
						if (!event.getValueIsAdjusting())
							return;
						// do some actions here, for example
						// print first column value from selected row

					}
				});

		tableSimilarity.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent me) {
				JTable table = (JTable) me.getSource();
				Point p = me.getPoint();
				int row = table.rowAtPoint(p);
				if (me.getClickCount() == 2) {
					String doc1 = tableSimilarity.getValueAt(row, 0).toString();
					String doc2 = tableSimilarity.getValueAt(row, 1).toString();
					String score = tableSimilarity.getValueAt(row, 2)
							.toString();
					JOptionPane.showMessageDialog(corpusView,
							"<html><b>Article 1:</b> " + doc1
									+ "<br> <b>Article 2:</b> " + doc2
									+ "<br> <b>Score:  </b> " + score
									+ "</html>");
				}
			}
		});
		tableSimilarity.setFillsViewportHeight(true);

		try {
			// 1.6+
			tableSimilarity.setAutoCreateRowSorter(true);
		} catch (Exception continuewithNoSort) {
		}
		JScrollPane tableScroll = new JScrollPane(tableSimilarity);
		Dimension tablePreferred = tableScroll.getPreferredSize();
		tableScroll.setPreferredSize(new Dimension(tablePreferred.width,
				tablePreferred.height / 3));

		JLabel lblCentrality = new JLabel("Distance to Central Document");
		lblCentrality.setFont(new Font("SansSerif", Font.BOLD, 14));
		String[] header2 = { "Document", "Distance" };

		String[][] data2 = new String[0][2];
		tableCentralityModel = new DefaultTableModel(data2, header2);
		tableCentrality = new JTable(tableCentralityModel) {
			private static final long serialVersionUID = 1L;

			public boolean isCellEditable(int row, int column) {
				return false;
			};
		};
		tableCentrality.setFillsViewportHeight(true);

		tableCentrality.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent me) {
				JTable table = (JTable) me.getSource();
				Point p = me.getPoint();
				int row = table.rowAtPoint(p);
				if (me.getClickCount() == 2) {
					String docC = (centralAuthorContainerToCompare == null) ? ""
							: centralAuthorContainerToCompare.getAuthor().getAuthorName();
					String doc2 = tableCentrality.getValueAt(row, 0).toString();
					String score = tableCentrality.getValueAt(row, 1)
							.toString();

					JOptionPane.showMessageDialog(corpusView,
							"<html><b>Central Author:</b> " + docC
									+ "<br> <b>Current Author:</b> " + doc2
									+ "<br> <b>Distance:</b> " + score
									+ "</html>");
				}
			}
		});

		try {
			// 1.6+
			tableCentrality.setAutoCreateRowSorter(true);
		} catch (Exception continuewithNoSort) {
		}
		JScrollPane tableScrollCentrality = new JScrollPane(tableCentrality);
		Dimension tablePreferredCentrality = tableScrollCentrality
				.getPreferredSize();
		tableScrollCentrality.setPreferredSize(new Dimension(
				tablePreferredCentrality.width / 2,
				tablePreferredCentrality.height / 2));

		GroupLayout groupLayout = new GroupLayout(getContentPane());
		groupLayout
				.setHorizontalGroup(groupLayout
						.createParallelGroup(Alignment.LEADING)
						.addGroup(
								groupLayout
										.createSequentialGroup()
										.addContainerGap()
										.addGroup(
												groupLayout
														.createParallelGroup(
																Alignment.LEADING)
														.addGroup(
																groupLayout
																		.createSequentialGroup()
																		.addComponent(
																				tableScroll,
																				GroupLayout.DEFAULT_SIZE,
																				1020,
																				Short.MAX_VALUE)
																		.addContainerGap())
														.addGroup(
																groupLayout
																		.createSequentialGroup()
																		.addGroup(
																				groupLayout
																						.createParallelGroup(
																								Alignment.LEADING)
																						.addGroup(
																								groupLayout
																										.createSequentialGroup()
																										.addGroup(
																												groupLayout
																														.createParallelGroup(
																																Alignment.LEADING)
																														.addComponent(
																																lblThreshold)
																														.addComponent(
																																sliderThreshold,
																																GroupLayout.PREFERRED_SIZE,
																																GroupLayout.DEFAULT_SIZE,
																																GroupLayout.PREFERRED_SIZE))
																										.addGap(10))
																						.addGroup(
																								groupLayout
																										.createSequentialGroup()
																										.addComponent(
																												panelGraph,
																												GroupLayout.DEFAULT_SIZE,
																												774,
																												Short.MAX_VALUE)
																										.addPreferredGap(
																												ComponentPlacement.RELATED)))
																		.addGroup(
																				groupLayout
																						.createParallelGroup(
																								Alignment.LEADING)
																						.addGroup(
																								groupLayout
																										.createSequentialGroup()
																										.addComponent(
																												lblCentrality)
																										.addGap(27))
																						.addGroup(
																								groupLayout
																										.createSequentialGroup()
																										.addComponent(
																												tableScrollCentrality,
																												GroupLayout.DEFAULT_SIZE,
																												236,
																												Short.MAX_VALUE)
																										.addContainerGap())))
														.addGroup(
																groupLayout
																		.createSequentialGroup()
																		.addComponent(
																				lblTopSimilarArticles)
																		.addContainerGap(
																				896,
																				Short.MAX_VALUE)))));
		groupLayout
				.setVerticalGroup(groupLayout
						.createParallelGroup(Alignment.LEADING)
						.addGroup(
								groupLayout
										.createSequentialGroup()
										.addContainerGap()
										.addGroup(
												groupLayout
														.createParallelGroup(
																Alignment.BASELINE)
														.addComponent(
																lblThreshold)
														.addComponent(
																lblCentrality))
										.addPreferredGap(
												ComponentPlacement.RELATED)
										.addComponent(sliderThreshold,
												GroupLayout.PREFERRED_SIZE, 52,
												GroupLayout.PREFERRED_SIZE)
										.addPreferredGap(
												ComponentPlacement.RELATED)
										.addGroup(
												groupLayout
														.createParallelGroup(
																Alignment.LEADING)
														.addComponent(
																tableScrollCentrality,
																GroupLayout.DEFAULT_SIZE,
																581,
																Short.MAX_VALUE)
														.addComponent(
																panelGraph,
																GroupLayout.DEFAULT_SIZE,
																581,
																Short.MAX_VALUE))
										.addPreferredGap(
												ComponentPlacement.RELATED)
										.addComponent(lblTopSimilarArticles)
										.addPreferredGap(
												ComponentPlacement.RELATED)
										.addComponent(tableScroll,
												GroupLayout.PREFERRED_SIZE,
												GroupLayout.DEFAULT_SIZE,
												GroupLayout.PREFERRED_SIZE)
										.addContainerGap()));
		// groupLayout.createParallelGroup().addComponent(tableScrollCentrality);
		// .addComponent(tableScrollCentrality)
		getContentPane().setLayout(groupLayout);
	}

	public HashMap<Integer, SingleAuthorContainer> buildConceptGraph(
			UndirectedGraph graph, GraphModel graphModel, double threshold) {
		HashMap<Integer, SingleAuthorContainer> outMap = new HashMap<Integer, SingleAuthorContainer>();
		logger.info("Starting to build the author graph");
		// build connected graph
		Map<SingleAuthorContainer, Boolean> visibleDocs = new TreeMap<SingleAuthorContainer, Boolean>();
		// build nodes
		Map<SingleAuthorContainer, Node> nodes = new TreeMap<SingleAuthorContainer, Node>();

		for (SingleAuthorContainer author : this.authorContainer.getAuthorContainers()) {
			visibleDocs.put(author, false);
		}

		// determine similarities in order to determine eligible candidates for
		// visualisation
		for (int i = 0; i < this.authorContainer.getAuthorContainers().size() - 1; i++) {
			for (int j = i + 1; j < this.authorContainer.getAuthorContainers().size(); j++) {
				SingleAuthorContainer a1 = this.authorContainer.getAuthorContainers().get(i);
				SingleAuthorContainer a2 = this.authorContainer.getAuthorContainers().get(j);
				double sim = this.distanceStrategy.computeDistanceBetween(a1, a2);
				if (sim >= threshold) {
					visibleDocs.put(a1, true);
					visibleDocs.put(a2, true);
				}
			}
		}

		for (SingleAuthorContainer d : this.authorContainer.getAuthorContainers()) {
			if (visibleDocs.get(d)) {
				String text = d.getAuthor().getAuthorName();
				text = (text.length() > 20) ? (text.substring(0, 20) + "..")
						: text;
				nodes.put(d, graphModel.factory().newNode(text));
				nodes.get(d).getNodeData().setLabel(text);
				nodes.get(d)
						.getNodeData()
						.setColor((float) (COLOR_CONCEPT.getRed()) / 256,
								(float) (COLOR_CONCEPT.getGreen()) / 256,
								(float) (COLOR_CONCEPT.getBlue()) / 256);
				graph.addNode(nodes.get(d));
				outMap.put(nodes.get(d).getId(), d);
			}
		}

		List<TwoAuthorsDistanceContainer> similarities = new LinkedList<TwoAuthorsDistanceContainer>();
		// determine similarities
		for (int i = 0; i < this.authorContainer.getAuthorContainers().size() - 1; i++) {
			for (int j = i + 1; j < this.authorContainer.getAuthorContainers().size(); j++) {
				SingleAuthorContainer a1 = this.authorContainer.getAuthorContainers().get(i);
				SingleAuthorContainer a2 = this.authorContainer.getAuthorContainers().get(j);
				if (visibleDocs.get(a1) && visibleDocs.get(a2)) {
					double sim = this.distanceStrategy.computeDistanceBetween(a1, a2);
					if (sim >= threshold) {
						Edge e = graphModel.factory().newEdge(nodes.get(a1),
								nodes.get(a2));
						e.setWeight((float) sim);
						e.getEdgeData().setLabel(sim + "");
						graph.addEdge(e);

						similarities.add(new TwoAuthorsDistanceContainer(a1, a2, sim));
					}
				}
			}
		}

		Collections.sort(similarities);

		// recreate table similarity model
		if (tableSimilarityModel.getRowCount() > 0) {
			for (int i = tableSimilarityModel.getRowCount() - 1; i > -1; i--) {
				tableSimilarityModel.removeRow(i);
			}
		}
		NumberFormat formatter = new DecimalFormat("#0.00");
		for (TwoAuthorsDistanceContainer sim : similarities) {
			String row[] = new String[3];
			row[0] = sim.getFirstAuthor().getAuthor().getAuthorName();
			row[1] = sim.getSecondAuthor().getAuthor().getAuthorName();
			row[2] = formatter.format(sim.getSimilarity());
			tableSimilarityModel.addRow(row);

		}
		tableSimilarityModel.fireTableDataChanged();

		logger.info("Generated graph with " + graph.getNodeCount()
				+ " nodes and " + graph.getEdgeCount() + " edges");
		return outMap;
	}

	private void generateGraph() {
		double threshold = ((double) sliderThreshold.getValue()) / 100;

		ProjectController pc = Lookup.getDefault().lookup(
				ProjectController.class);
		pc.newProject();

		// get models
		GraphModel graphModel = Lookup.getDefault()
				.lookup(GraphController.class).getModel();
		AttributeModel attributeModel = Lookup.getDefault()
				.lookup(AttributeController.class).getModel();
		UndirectedGraph graph = graphModel.getUndirectedGraph();

		HashMap<Integer, SingleAuthorContainer> nodeMap = buildConceptGraph(graph,
				graphModel, threshold);

		RankingController rankingController = Lookup.getDefault().lookup(
				RankingController.class);

		// Get Centrality
		GraphDistance distance = new GraphDistance();
		distance.setDirected(true);
		distance.execute(graphModel, attributeModel);

		// Rank size by centrality
		AttributeColumn centralityColumn = attributeModel.getNodeTable()
				.getColumn(GraphDistance.BETWEENNESS);

		double maxCentrality = Double.NEGATIVE_INFINITY;
		SingleAuthorContainer centralAuthor = null;
		for (Node n : graph.getNodes()) {
			Double centrality = (Double) n.getNodeData().getAttributes()
					.getValue(centralityColumn.getIndex());
			if (centrality > maxCentrality) {
				maxCentrality = centrality;
				centralAuthor = nodeMap.get(n.getId());
			}
		}
		this.centralAuthorContainerToCompare = centralAuthor;
		List<CompareCentralityElement> centralityList = new ArrayList<CompareCentralityElement>();
		if (centralAuthor != null) {
			for (Node n : graph.getNodes()) {
				SingleAuthorContainer doc = nodeMap.get(n.getId());
				if (doc.isSameAuthor(centralAuthor.getAuthor()))
					continue;
				SingleAuthorContainer d1 = centralAuthor, d2 = doc;
				double sim = this.distanceStrategy.computeDistanceBetween(d1, d2);
				centralityList.add(new CompareCentralityElement(doc, sim));
			}
		}

		Collections.sort(centralityList,
				new Comparator<CompareCentralityElement>() {
					public int compare(CompareCentralityElement d1,
							CompareCentralityElement d2) {
						return -Double.compare(d1.centrality, d2.centrality);
					}
				});

		// recreate table similarity model
		if (tableCentralityModel.getRowCount() > 0) {
			for (int i = tableCentralityModel.getRowCount() - 1; i > -1; i--) {
				tableCentralityModel.removeRow(i);
			}
		}
		NumberFormat formatter = new DecimalFormat("#0.00");
		for (CompareCentralityElement sim : centralityList) {
			String row[] = new String[2];
			row[0] = sim.author.getAuthor().getAuthorName();
			row[1] = formatter.format(sim.centrality);
			tableCentralityModel.addRow(row);
		}
		tableCentralityModel.fireTableDataChanged();

		Ranking<?> centralityRanking = rankingController.getModel().getRanking(
				Ranking.NODE_ELEMENT, centralityColumn.getId());
		AbstractSizeTransformer<?> sizeTransformer = (AbstractSizeTransformer<?>) rankingController
				.getModel().getTransformer(Ranking.NODE_ELEMENT,
						Transformer.RENDERABLE_SIZE);
		sizeTransformer.setMinSize(5);
		sizeTransformer.setMaxSize(40);
		rankingController.transform(centralityRanking, sizeTransformer);

		// Rank label size - set a multiplier size
		Ranking<?> centralityRanking2 = rankingController.getModel()
				.getRanking(Ranking.NODE_ELEMENT, centralityColumn.getId());
		AbstractSizeTransformer<?> labelSizeTransformer = (AbstractSizeTransformer<?>) rankingController
				.getModel().getTransformer(Ranking.NODE_ELEMENT,
						Transformer.LABEL_SIZE);
		labelSizeTransformer.setMinSize(1);
		labelSizeTransformer.setMaxSize(5);
		rankingController.transform(centralityRanking2, labelSizeTransformer);

		// Preview configuration
		PreviewController previewController = Lookup.getDefault().lookup(
				PreviewController.class);
		PreviewModel previewModel = previewController.getModel();
		previewModel.getProperties().putValue(PreviewProperty.SHOW_NODE_LABELS,
				Boolean.TRUE);
		previewModel.getProperties().putValue(
				PreviewProperty.NODE_LABEL_PROPORTIONAL_SIZE, Boolean.FALSE);
		previewModel.getProperties().putValue(PreviewProperty.EDGE_CURVED,
				Boolean.FALSE);
		previewController.refreshPreview();

		// New Processing target, get the PApplet
		ProcessingTarget target = (ProcessingTarget) previewController
				.getRenderTarget(RenderTarget.PROCESSING_TARGET);
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
		if (panelGraph.getComponents().length > 0) {
			panelGraph.removeAll();
			panelGraph.revalidate();
		}
		panelGraph.add(applet, BorderLayout.CENTER);
		panelGraph.validate();
		panelGraph.repaint();

		// Export
		ExportController ec = Lookup.getDefault()
				.lookup(ExportController.class);
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
		for (UIManager.LookAndFeelInfo info : UIManager
				.getInstalledLookAndFeels()) {
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
				String inDir = "in/LAK_corpus/parsed-documents2";
				AuthorDistanceStrategyType distanceStrategyType = AuthorDistanceStrategyType.CoAuthorshipDistance;
				
				AuthorContainer container = AuthorContainer.buildAuthorContainerFromDirectory(inDir);
				AuthorDistanceStrategyFactory distStrategyFactory = new AuthorDistanceStrategyFactory(container);
				IAuthorDistanceStrategy distanceStrategy = distStrategyFactory.getDistanceStrategy(distanceStrategyType);
				CachedAuthorDistanceStrategyDecorator cachedDistStrategy = new CachedAuthorDistanceStrategyDecorator(container, distanceStrategy);
				ArticleAuthorSimilarityView view = new ArticleAuthorSimilarityView(container, cachedDistStrategy);
				view.setVisible(true);
			}
		});
	}
}