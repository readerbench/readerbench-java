package view.widgets.document.corpora;

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
import java.io.FilenameFilter;
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
import org.gephi.io.exporter.api.ExportController;
import org.gephi.layout.plugin.force.StepDisplacement;
import org.gephi.layout.plugin.force.yifanHu.YifanHuLayout;
import org.gephi.preview.api.G2DTarget;
import org.gephi.preview.api.PreviewController;
import org.gephi.preview.api.PreviewModel;
import org.gephi.preview.api.PreviewProperty;
import org.gephi.preview.api.RenderTarget;
import org.gephi.project.api.ProjectController;
import org.gephi.statistics.plugin.GraphDistance;
import org.openide.util.Lookup;

import cc.mallet.util.Maths;
import data.AbstractDocument;
import data.discourse.SemanticCohesion;
import data.document.Document;
import services.commons.Formatting;
import services.commons.VectorAlgebra;
import view.models.PreviewSketch;

public class PaperCorpusSimilarityView extends JFrame {
	static PaperCorpusSimilarityView corpusView;
	private static final long serialVersionUID = -8582615231233815258L;
	static Logger logger = Logger.getLogger(PaperCorpusSimilarityView.class);
	public static final Color COLOR_CONCEPT = new Color(204, 204, 204); // silver

	private List<Document> docs;
	private JSlider sliderThreshold;
	private JPanel panelGraph;

	JTable tableSimilarity;
	DefaultTableModel tableSimilarityModel;
	JTable tableCentrality;
	DefaultTableModel tableCentralityModel;

	AbstractDocument centralDocumentToCompare;

	class CompareCentralityElement {
		double centrality;
		AbstractDocument document;

		public CompareCentralityElement(AbstractDocument document, double centrality) {
			this.document = document;
			this.centrality = centrality;
		}
	}

	class CompareDocsSim implements Comparable<CompareDocsSim> {
		private AbstractDocument doc1;
		private AbstractDocument doc2;
		private double sim;

		public CompareDocsSim(AbstractDocument doc1, AbstractDocument doc2, double sim) {
			super();
			this.doc1 = doc1;
			this.doc2 = doc2;
			this.sim = sim;
		}

		public AbstractDocument getDoc1() {
			return doc1;
		}

		public void setDoc1(AbstractDocument doc1) {
			this.doc1 = doc1;
		}

		public AbstractDocument getDoc2() {
			return doc2;
		}

		public void setDoc2(AbstractDocument doc2) {
			this.doc2 = doc2;
		}

		public double getSim() {
			return sim;
		}

		public void setSim(double sim) {
			this.sim = sim;
		}

		@Override
		public int compareTo(CompareDocsSim o) {
			return new Double(o.getSim()).compareTo(new Double(this.getSim()));
		}

		@Override
		public boolean equals(Object obj) {
			if (this == null || obj == null)
				return false;
			CompareDocsSim o = (CompareDocsSim) obj;
			return (this.getDoc1().equals(o.getDoc1()) && this.getDoc2().equals(o.getDoc2()))
					|| (this.getDoc1().equals(o.getDoc2()) && this.getDoc2().equals(o.getDoc1()));
		}

		@Override
		public String toString() {
			return Formatting.formatNumber(this.getSim()) + ":\n\t- " + new File(this.getDoc1().getPath()).getName()
					+ ": " + this.getDoc1().getText() + "\n\t- " + new File(this.getDoc2().getPath()).getName() + ": "
					+ this.getDoc2().getText() + "\n";
		}
	}

	public PaperCorpusSimilarityView(List<Document> docs) {
		centralDocumentToCompare = null;
		corpusView = this;
		setTitle("Article Similarity View");
		getContentPane().setBackground(Color.WHITE);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		this.docs = docs;

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
		panelGraph.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		panelGraph.setBackground(Color.WHITE);
		panelGraph.setLayout(new BorderLayout());

		JLabel lblTopSimilarArticles = new JLabel("Top Similar Articles");
		lblTopSimilarArticles.setFont(new Font("SansSerif", Font.BOLD, 14));
		String[] header = { "Article 1", "Article 2", "Semantic Similarity Score" };
		String[][] data = new String[0][3];

		tableSimilarityModel = new DefaultTableModel(data, header);
		tableSimilarity = new JTable(tableSimilarityModel) {
			private static final long serialVersionUID = 1L;

			public boolean isCellEditable(int row, int column) {
				return false;
			};
		};
		tableSimilarity.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
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
					String score = tableSimilarity.getValueAt(row, 2).toString();
					JOptionPane.showMessageDialog(corpusView, "<html><b>Article 1:</b> " + doc1
							+ "<br> <b>Article 2:</b> " + doc2 + "<br> <b>Score:  </b> " + score + "</html>");
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
		tableScroll.setPreferredSize(new Dimension(tablePreferred.width, tablePreferred.height / 3));

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
					String docC = (centralDocumentToCompare == null) ? "" : centralDocumentToCompare.getTitleText();
					String doc2 = tableCentrality.getValueAt(row, 0).toString();
					String score = tableCentrality.getValueAt(row, 1).toString();

					JOptionPane.showMessageDialog(corpusView,
							"<html><b>Central Article:</b> " + docC + "<br> <b>Current Article:</b> " + doc2
									+ "<br> <b>Semantic Distance:</b> " + score + "</html>");
				}
			}
		});

		try {
			tableCentrality.setAutoCreateRowSorter(true);
		} catch (Exception continuewithNoSort) {
		}
		JScrollPane tableScrollCentrality = new JScrollPane(tableCentrality);
		Dimension tablePreferredCentrality = tableScrollCentrality.getPreferredSize();
		tableScrollCentrality.setPreferredSize(
				new Dimension(tablePreferredCentrality.width / 2, tablePreferredCentrality.height / 2));

		GroupLayout groupLayout = new GroupLayout(getContentPane());
		groupLayout
				.setHorizontalGroup(groupLayout.createParallelGroup(Alignment.LEADING)
						.addGroup(groupLayout.createSequentialGroup().addContainerGap()
								.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
										.addGroup(groupLayout.createSequentialGroup()
												.addComponent(tableScroll, GroupLayout.DEFAULT_SIZE, 1020,
														Short.MAX_VALUE)
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
												.addComponent(panelGraph, GroupLayout.DEFAULT_SIZE, 774,
														Short.MAX_VALUE)
												.addPreferredGap(ComponentPlacement.RELATED)))
								.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
										.addGroup(groupLayout.createSequentialGroup().addComponent(lblCentrality)
												.addGap(27))
										.addGroup(groupLayout.createSequentialGroup()
												.addComponent(tableScrollCentrality, GroupLayout.DEFAULT_SIZE, 236,
														Short.MAX_VALUE)
												.addContainerGap())))
						.addGroup(groupLayout.createSequentialGroup().addComponent(lblTopSimilarArticles)
								.addContainerGap(896, Short.MAX_VALUE)))));
		groupLayout
				.setVerticalGroup(
						groupLayout.createParallelGroup(Alignment.LEADING)
								.addGroup(groupLayout.createSequentialGroup().addContainerGap()
										.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
												.addComponent(lblThreshold).addComponent(lblCentrality))
								.addPreferredGap(ComponentPlacement.RELATED)
								.addComponent(sliderThreshold, GroupLayout.PREFERRED_SIZE, 52,
										GroupLayout.PREFERRED_SIZE)
				.addPreferredGap(ComponentPlacement.RELATED)
				.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
						.addComponent(tableScrollCentrality, GroupLayout.DEFAULT_SIZE, 581, Short.MAX_VALUE)
						.addComponent(panelGraph, GroupLayout.DEFAULT_SIZE, 581, Short.MAX_VALUE))
				.addPreferredGap(ComponentPlacement.RELATED).addComponent(lblTopSimilarArticles)
				.addPreferredGap(ComponentPlacement.RELATED).addComponent(tableScroll, GroupLayout.PREFERRED_SIZE,
						GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE).addContainerGap()));
		getContentPane().setLayout(groupLayout);
	}

	public HashMap<Node, AbstractDocument> buildConceptGraph(UndirectedGraph graph, GraphModel graphModel,
			double threshold) {
		HashMap<Node, AbstractDocument> outMap = new HashMap<Node, AbstractDocument>();
		logger.info("Starting to build the document graph");
		// build connected graph
		Map<AbstractDocument, Boolean> visibleDocs = new TreeMap<AbstractDocument, Boolean>();
		// build nodes
		Map<AbstractDocument, Node> nodes = new TreeMap<AbstractDocument, Node>();
		List<CompareDocsSim> similarities = new LinkedList<CompareDocsSim>();

		for (AbstractDocument d : docs) {
			visibleDocs.put(d, false);
		}

		// determine similarities in order to determine eligible candidates for
		// visualisation
		for (int i = 0; i < docs.size() - 1; i++) {
			for (int j = i + 1; j < docs.size(); j++) {
				AbstractDocument d1 = docs.get(i);
				AbstractDocument d2 = docs.get(j);
				double lsaSim = 0;
				double ldaSim = 0;
				if (d1.getLSA() != null && d2.getLSA() != null)
					lsaSim = VectorAlgebra.cosineSimilarity(d1.getLSAVector(), d2.getLSAVector());
				if (d1.getLDA() != null && d2.getLDA() != null)
					ldaSim = 1
							- Maths.jensenShannonDivergence(d1.getLDAProbDistribution(), d2.getLDAProbDistribution());
				double sim = SemanticCohesion.getAggregatedSemanticMeasure(lsaSim, ldaSim);
				if (sim >= threshold) {
					visibleDocs.put(d1, true);
					visibleDocs.put(d2, true);
				}
			}
		}

		for (AbstractDocument d : docs) {
			if (visibleDocs.get(d)) {
				String text = "";
				if (d.getTitleText() != null)
					text += d.getTitleText();
				text += "(" + new File(d.getPath()).getName() + ")";
				text = (text.length() > 20) ? (text.substring(0, 20) + "..") : text;
				Node n = graphModel.factory().newNode(text);
				n.setLabel(text);
				n.setColor(new Color((float) (COLOR_CONCEPT.getRed()) / 256, (float) (COLOR_CONCEPT.getGreen()) / 256,
						(float) (COLOR_CONCEPT.getBlue()) / 256));
				n.setX((float) ((0.01 + Math.random()) * 1000) - 500);
				n.setY((float) ((0.01 + Math.random()) * 1000) - 500);
				graph.addNode(n);
				nodes.put(d, n);
				outMap.put(n, d);
			}
		}

		// determine similarities
		for (int i = 0; i < docs.size() - 1; i++) {
			for (int j = i + 1; j < docs.size(); j++) {
				AbstractDocument d1 = docs.get(i);
				AbstractDocument d2 = docs.get(j);
				if (visibleDocs.get(d1) && visibleDocs.get(d2)) {
					double lsaSim = 0;
					double ldaSim = 0;
					if (d1.getLSA() != null && d2.getLSA() != null)
						lsaSim = VectorAlgebra.cosineSimilarity(d1.getLSAVector(), d2.getLSAVector());
					if (d1.getLDA() != null && d2.getLDA() != null)
						ldaSim = 1 - Maths.jensenShannonDivergence(d1.getLDAProbDistribution(),
								d2.getLDAProbDistribution());
					double sim = SemanticCohesion.getAggregatedSemanticMeasure(lsaSim, ldaSim);
					if (sim >= threshold) {
						Edge e = graphModel.factory().newEdge(nodes.get(d1), nodes.get(d2), 0, sim, false);
						e.setLabel(sim + "");
						graph.addEdge(e);
						similarities.add(new CompareDocsSim(d1, d2, sim));
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
		for (CompareDocsSim sim : similarities) {
			String row[] = new String[3];
			row[0] = sim.getDoc1().getTitleText();
			row[1] = sim.getDoc2().getTitleText();
			row[2] = formatter.format(sim.getSim());
			tableSimilarityModel.addRow(row);

		}
		tableSimilarityModel.fireTableDataChanged();

		logger.info("Generated graph with " + graph.getNodeCount() + " nodes and " + graph.getEdgeCount() + " edges");
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

		HashMap<Node, AbstractDocument> nodeMap = buildConceptGraph(graph, graphModel, threshold);

		// Run YifanHuLayout for 100 passes - The layout always takes the
		// current visible view
		YifanHuLayout layout = new YifanHuLayout(null, new StepDisplacement(1f));
		layout.setGraphModel(graphModel);
		layout.resetPropertiesValues();
		layout.setOptimalDistance(200f);

		layout.initAlgo();
		for (int i = 0; i < 100 && layout.canAlgo(); i++) {
			layout.goAlgo();
		}
		layout.endAlgo();

		// Get Centrality
		GraphDistance distance = new GraphDistance();
		distance.setDirected(false);
		distance.execute(graphModel);

		// Rank size by centrality
		double maxCentrality = Double.NEGATIVE_INFINITY;
		AbstractDocument centralDocument = null;
		Column betweennessColumn = graphModel.getNodeTable().getColumn(GraphDistance.BETWEENNESS);
		for (Node n : graph.getNodes()) {
			Double centrality = (Double) n.getAttribute(betweennessColumn);
			if (centrality > maxCentrality) {
				maxCentrality = centrality;
				centralDocument = nodeMap.get(n.getId());
			}
		}
		centralDocumentToCompare = centralDocument;
		List<CompareCentralityElement> centralityList = new ArrayList<CompareCentralityElement>();
		if (centralDocument != null) {
			for (Node n : graph.getNodes()) {
				AbstractDocument doc = nodeMap.get(n);
				if (doc.equals(centralDocument))
					continue;
				AbstractDocument d1 = centralDocument, d2 = doc;
				double lsaSim = 0;
				double ldaSim = 0;
				if (d1.getLSA() != null && d2.getLSA() != null)
					lsaSim = VectorAlgebra.cosineSimilarity(d1.getLSAVector(), d2.getLSAVector());
				if (d1.getLDA() != null && d2.getLDA() != null)
					ldaSim = 1
							- Maths.jensenShannonDivergence(d1.getLDAProbDistribution(), d2.getLDAProbDistribution());
				double sim = SemanticCohesion.getAggregatedSemanticMeasure(lsaSim, ldaSim);

				centralityList.add(new CompareCentralityElement(doc, sim));
			}
		}

		Collections.sort(centralityList, new Comparator<CompareCentralityElement>() {
			public int compare(CompareCentralityElement d1, CompareCentralityElement d2) {
				return -Double.compare(d1.centrality, d2.centrality);
			}
		});

		// recreate table similarity model
		if (tableCentralityModel.getRowCount() > 0) {
			for (int i = tableCentralityModel.getRowCount() - 1; i > -1; i--) {
				tableCentralityModel.removeRow(i);
			}
		}
		for (CompareCentralityElement sim : centralityList) {
			String row[] = new String[2];
			row[0] = sim.document.getTitleText();
			row[1] = Formatting.formatNumber(sim.centrality) + "";
			tableCentralityModel.addRow(row);
		}
		tableCentralityModel.fireTableDataChanged();

		// Rank size by centrality
		Function centralityRanking = appearanceModel.getNodeFunction(graph, betweennessColumn,
				RankingNodeSizeTransformer.class);
		RankingNodeSizeTransformer centralityTransformer = (RankingNodeSizeTransformer) centralityRanking
				.getTransformer();
		centralityTransformer.setMinSize(5);
		centralityTransformer.setMaxSize(40);
		appearanceController.transform(centralityRanking);

		// Rank label size - set a multiplier size
		Function centralityRanking2 = appearanceModel.getNodeFunction(graph, betweennessColumn,
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
		previewModel.getProperties().putValue(PreviewProperty.NODE_LABEL_PROPORTIONAL_SIZE, Boolean.FALSE);
		previewModel.getProperties().putValue(PreviewProperty.EDGE_CURVED, Boolean.FALSE);
		previewController.refreshPreview();

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
				List<Document> docs = new LinkedList<Document>();

				File dir = new File("in\\AbstractAnalyzer2\\AbstractAnalyzer2");
				File[] files = dir.listFiles(new FilenameFilter() {
					@Override
					public boolean accept(File dir, String name) {
						return name.endsWith(".ser");
					}
				});

				for (File file : files) {
					Document d = (Document) AbstractDocument.loadSerializedDocument(file.getPath());
					docs.add(d);
				}

				PaperCorpusSimilarityView view = new PaperCorpusSimilarityView(docs);
				view.setVisible(true);
			}
		});
	}
}