package view.widgets.document.corpora;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JComboBox;
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
import org.gephi.preview.api.G2DTarget;
import org.gephi.preview.api.PreviewController;
import org.gephi.preview.api.PreviewModel;
import org.gephi.preview.api.PreviewProperty;
import org.gephi.preview.api.RenderTarget;
import org.gephi.preview.types.DependantOriginalColor;
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

public class PaperSimilarityView extends JFrame {
	public static final double INITIAL_DOC_THRESHOLD = 0.4;

	public static PaperSimilarityView paperSimilarityView;
	private static final long serialVersionUID = -8582615231233815258L;
	static Logger logger = Logger.getLogger(PaperSimilarityView.class);
	public static final Color COLOR_CONCEPT = new Color(204, 204, 204); // silver

	public static final double MAX_COHESION = 0.96;

	private List<Document> docs;
	private AbstractDocument referenceDoc;
	private JSlider sliderThreshold;
	private JPanel panelGraph;
	private JLabel lblThreshold;
	private int graphDepthLevel;
	JTable tableCentrality;
	DefaultTableModel tableCentralityModel;

	class CompareDocsSim implements Comparable<CompareDocsSim> {
		private AbstractDocument doc;
		private double sim;

		public CompareDocsSim(AbstractDocument doc, double sim) {
			super();
			this.doc = doc;
			this.sim = sim;
		}

		public AbstractDocument getDoc() {
			return doc;
		}

		public void setDoc(AbstractDocument doc) {
			this.doc = doc;
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
			return this.getDoc().equals(o.getDoc());
		}

		@Override
		public String toString() {
			return Formatting.formatNumber(this.getSim()) + ": " + new File(this.getDoc().getPath()).getName() + " >> "
					+ this.getDoc().getText();
		}
	}

	public PaperSimilarityView(List<Document> docs, Document referenceDoc) {
		paperSimilarityView = this;
		this.setGraphDepthLevel(1);
		setTitle("Document Centrality Graph");
		getContentPane().setBackground(Color.WHITE);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		this.docs = docs;
		this.referenceDoc = referenceDoc;

		// adjust view to desktop size
		int margin = 50;
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		setBounds(margin, margin, screenSize.width - margin * 2, screenSize.height - margin * 2);

		generateLayout();
		generateGraph();
	}

	private void generateLayout() {
		lblThreshold = new JLabel("Threshold among documents");
		lblThreshold.setFont(new Font("SansSerif", Font.BOLD, 12));

		sliderThreshold = new JSlider(40, 80, 50);
		sliderThreshold.setBackground(Color.WHITE);
		sliderThreshold.setPaintTicks(true);
		sliderThreshold.setFont(new Font("SansSerif", Font.PLAIN, 10));
		sliderThreshold.setPaintLabels(true);
		sliderThreshold.setMinorTickSpacing(10);
		sliderThreshold.setMajorTickSpacing(50);
		java.util.Hashtable<Integer, JLabel> labelTableThreshold = new java.util.Hashtable<Integer, JLabel>();
		labelTableThreshold.put(new Integer(80), new JLabel("80%"));
		labelTableThreshold.put(new Integer(60), new JLabel("60%"));
		labelTableThreshold.put(new Integer(40), new JLabel("40%"));
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

		JLabel lblCentrality = new JLabel("Top similar articles");
		lblCentrality.setFont(new Font("SansSerif", Font.BOLD, 14));
		String[] header2 = { "Article", "Similarity" };
		String[][] data2 = new String[0][2];
		tableCentralityModel = new DefaultTableModel(data2, header2);
		tableCentrality = new JTable(tableCentralityModel) {
			private static final long serialVersionUID = 1L;

			public boolean isCellEditable(int row, int column) {
				return false;
			};
		};
		try {
			// 1.6+
			tableCentrality.setAutoCreateRowSorter(true);
		} catch (Exception continuewithNoSort) {
		}
		tableCentrality.setFillsViewportHeight(true);
		tableCentrality.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent me) {
				JTable table = (JTable) me.getSource();
				Point p = me.getPoint();
				int row = table.rowAtPoint(p);
				if (me.getClickCount() == 2) {
					try {
						String docC = referenceDoc.getTitleText();
						String doc2 = tableCentrality.getValueAt(row, 0).toString();
						String score = tableCentrality.getValueAt(row, 1).toString();

						JOptionPane.showMessageDialog(paperSimilarityView,
								"<html><b>Central Article:</b> " + docC + "<br> <b>Current Article:</b> " + doc2
										+ "<br> <b>Semantic Distance:</b> " + score + "</html>");
					} catch (Exception e) {
						// e.printStackTrace();
					}
				}
			}
		});

		JScrollPane tableScrollCentrality = new JScrollPane(tableCentrality);
		tableScrollCentrality.setBackground(Color.white);
		Dimension tablePreferredCentrality = tableScrollCentrality.getPreferredSize();
		tableScrollCentrality.setPreferredSize(
				new Dimension(tablePreferredCentrality.width / 2, tablePreferredCentrality.height / 3));

		String[] graphLevels = { "1", "2", "3" };

		JLabel lblComboBox = new JLabel("Depth Level");
		lblComboBox.setFont(new Font("SansSerif", Font.BOLD, 12));

		JComboBox<String> docLevelsCombo = new JComboBox<String>(graphLevels);
		docLevelsCombo.setSelectedIndex(0);
		docLevelsCombo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JComboBox<?> cb = (JComboBox<?>) e.getSource();
				String selectedItem = (String) cb.getSelectedItem();
				int levelSelected = Integer.parseInt(selectedItem);
				paperSimilarityView.setGraphDepthLevel(levelSelected);
				generateGraph();
			}
		});

		GroupLayout groupLayout = new GroupLayout(getContentPane());
		groupLayout.setHorizontalGroup(groupLayout.createParallelGroup(Alignment.LEADING).addGroup(groupLayout
				.createSequentialGroup().addContainerGap()
				.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
						.addGroup(groupLayout.createSequentialGroup()
								.addComponent(sliderThreshold, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
										GroupLayout.PREFERRED_SIZE)
								.addPreferredGap(ComponentPlacement.RELATED).addComponent(lblComboBox)
								.addPreferredGap(ComponentPlacement.RELATED).addComponent(docLevelsCombo,
										GroupLayout.PREFERRED_SIZE, 51, GroupLayout.PREFERRED_SIZE))
						.addComponent(lblThreshold)
						.addComponent(panelGraph, GroupLayout.DEFAULT_SIZE, 1168, Short.MAX_VALUE))
				.addPreferredGap(ComponentPlacement.RELATED)
				.addGroup(groupLayout.createParallelGroup(Alignment.LEADING).addComponent(lblCentrality).addComponent(
						tableScrollCentrality, GroupLayout.PREFERRED_SIZE, 331, GroupLayout.PREFERRED_SIZE))
				.addContainerGap()));
		groupLayout
				.setVerticalGroup(groupLayout.createParallelGroup(Alignment.LEADING)
						.addGroup(groupLayout.createSequentialGroup().addContainerGap().addComponent(lblThreshold)
								.addPreferredGap(ComponentPlacement.RELATED)
								.addComponent(sliderThreshold, GroupLayout.PREFERRED_SIZE, 40,
										GroupLayout.PREFERRED_SIZE)
								.addContainerGap(650, Short.MAX_VALUE))
				.addGroup(groupLayout.createSequentialGroup().addComponent(lblCentrality).addGap(10)
						.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
								.addGroup(groupLayout.createSequentialGroup()
										.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
												.addComponent(lblComboBox).addComponent(docLevelsCombo,
														GroupLayout.PREFERRED_SIZE, 30, GroupLayout.PREFERRED_SIZE))
								.addGap(27).addComponent(panelGraph, GroupLayout.DEFAULT_SIZE, 627, Short.MAX_VALUE))
								.addGroup(groupLayout.createSequentialGroup().addGap(13).addComponent(
										tableScrollCentrality, GroupLayout.DEFAULT_SIZE, 671, Short.MAX_VALUE)))
						.addContainerGap()));
		getContentPane().setLayout(groupLayout);
	}

	public void buildConceptGraph(UndirectedGraph graph, GraphModel graphModel, double threshold, int currentLevel,
			AbstractDocument refDoc, Map<AbstractDocument, Node> nodes) {
		if (currentLevel > this.graphDepthLevel)
			return;
		Map<AbstractDocument, Boolean> visibleDocs = new TreeMap<AbstractDocument, Boolean>();
		for (AbstractDocument d : docs) {
			visibleDocs.put(d, false);
		}
		visibleDocs.put(refDoc, true);

		logger.info("Starting to build the document graph");
		// build connected graph

		// determine similarities in order to determine eligible candidates for
		// visualisation
		for (AbstractDocument d : docs) {
			if (!refDoc.equals(d)) {
				// difference between documents
				double lsaSim = 0;
				double ldaSim = 0;
				if (refDoc.getLSA() != null && d.getLSA() != null)
					lsaSim = VectorAlgebra.cosineSimilarity(refDoc.getLSAVector(), d.getLSAVector());
				if (refDoc.getLDA() != null && d.getLDA() != null)
					ldaSim = 1 - Maths.jensenShannonDivergence(refDoc.getLDAProbDistribution(),
							d.getLDAProbDistribution());
				double sim = SemanticCohesion.getAggregatedSemanticMeasure(lsaSim, ldaSim);

				// difference to initial document
				double simRef = 1.0;
				if (currentLevel > 1) {
					lsaSim = 0;
					ldaSim = 0;
					if (this.referenceDoc.getLSA() != null && d.getLSA() != null)
						lsaSim = VectorAlgebra.cosineSimilarity(this.referenceDoc.getLSAVector(), d.getLSAVector());
					if (this.referenceDoc.getLDA() != null && d.getLDA() != null)
						ldaSim = 1 - Maths.jensenShannonDivergence(this.referenceDoc.getLDAProbDistribution(),
								d.getLDAProbDistribution());
					simRef = SemanticCohesion.getAggregatedSemanticMeasure(lsaSim, ldaSim);
				}

				if (sim >= threshold && simRef >= INITIAL_DOC_THRESHOLD && sim <= MAX_COHESION
						&& !refDoc.getProcessedText().equals(d.getProcessedText())) {
					visibleDocs.put(d, true);
				}
			}
		}

		for (AbstractDocument d : docs) {
			if (visibleDocs.get(d) == true) {
				String text = "";
				if (d.getTitleText() != null)
					text += d.getTitleText();
				text += "(" + new File(d.getPath()).getName() + ")";
				text = (text.length() > 40) ? (text.substring(0, 40) + "..") : text;
				if (nodes.get(d) == null) {
					Node n = graphModel.factory().newNode(text);
					n.setLabel(text);
					n.setSize(10);
					n.setColor(new Color(1.0f - ((float) (COLOR_CONCEPT.getRed()) / (256 * currentLevel)),
							1.0f - ((float) (COLOR_CONCEPT.getGreen()) / (256 * currentLevel)),
							1.0f - ((float) (COLOR_CONCEPT.getBlue()) / (256 * currentLevel))));
					n.setX((float) ((0.01 + Math.random()) * 1000) - 500);
					n.setY((float) ((0.01 + Math.random()) * 1000) - 500);
					graph.addNode(n);
					nodes.put(d, n);
				}
			}
		}

		// determine similarities
		for (AbstractDocument d : docs) {
			if (!refDoc.equals(d)) {
				if (visibleDocs.get(d)) {
					double lsaSim = 0;
					double ldaSim = 0;
					if (refDoc.getLSA() != null && d.getLSA() != null)
						lsaSim = VectorAlgebra.cosineSimilarity(refDoc.getLSAVector(), d.getLSAVector());
					if (refDoc.getLDA() != null && d.getLDA() != null)
						ldaSim = 1 - Maths.jensenShannonDivergence(refDoc.getLDAProbDistribution(),
								d.getLDAProbDistribution());
					double sim = SemanticCohesion.getAggregatedSemanticMeasure(lsaSim, ldaSim);
					if (sim >= threshold && sim <= MAX_COHESION
							&& !refDoc.getProcessedText().equals(d.getProcessedText())) {
						Edge e = graphModel.factory().newEdge(nodes.get(refDoc), nodes.get(d), 0, sim, false);
						e.setLabel(sim + "");
						graph.addEdge(e);
					}
				}
			}
		}

		logger.info("Generated graph with " + graph.getNodeCount() + " nodes and " + graph.getEdgeCount() + " edges");

		for (AbstractDocument d : docs) {
			if (visibleDocs.get(d) == true) {
				buildConceptGraph(graph, graphModel, threshold, currentLevel + 1, d, nodes);
			}
		}
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

		// build nodes
		Map<AbstractDocument, Node> nodes = new TreeMap<AbstractDocument, Node>();

		// visibleDocs.put(this.referenceDoc, true);
		buildConceptGraph(graph, graphModel, threshold, 1, this.referenceDoc, nodes);

		/* similarity to the central article */
		List<CompareDocsSim> similarities = new LinkedList<CompareDocsSim>();
		Iterator<AbstractDocument> docIt = nodes.keySet().iterator();
		while (docIt.hasNext()) {
			AbstractDocument d = docIt.next();
			if (!this.referenceDoc.equals(d)) {
				double lsaSim = 0;
				double ldaSim = 0;
				if (this.referenceDoc.getLSA() != null && d.getLSA() != null)
					lsaSim = VectorAlgebra.cosineSimilarity(this.referenceDoc.getLSAVector(), d.getLSAVector());
				if (this.referenceDoc.getLDA() != null && d.getLDA() != null)
					ldaSim = 1 - Maths.jensenShannonDivergence(this.referenceDoc.getLDAProbDistribution(),
							d.getLDAProbDistribution());
				double sim = SemanticCohesion.getAggregatedSemanticMeasure(lsaSim, ldaSim);
				similarities.add(new CompareDocsSim(d, sim));
			}
		}

		Collections.sort(similarities);
		if (tableCentralityModel.getRowCount() > 0) {
			for (int i = tableCentralityModel.getRowCount() - 1; i > -1; i--) {
				tableCentralityModel.removeRow(i);
			}
		}
		NumberFormat formatter = new DecimalFormat("#0.00");
		for (CompareDocsSim sim : similarities) {
			String row[] = new String[2];
			row[0] = sim.doc.getTitleText();
			row[1] = formatter.format(sim.sim);
			tableCentralityModel.addRow(row);
		}
		tableCentralityModel.fireTableDataChanged();
		/* end similarity to central article */

		// Get Centrality
		GraphDistance distance = new GraphDistance();
		distance.setDirected(false);
		distance.execute(graphModel);

		// Rank size by centrality
		/*
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
		*/

		logger.info("Generating preview...");
		// Preview configuration
		PreviewController previewController = Lookup.getDefault().lookup(PreviewController.class);
		PreviewModel previewModel = previewController.getModel();
		previewModel.getProperties().putValue(PreviewProperty.SHOW_NODE_LABELS, Boolean.TRUE);
		previewModel.getProperties().putValue(PreviewProperty.NODE_LABEL_COLOR,
				new DependantOriginalColor(Color.BLACK));
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
		ExportController ec = Lookup.getDefault().lookup(ExportController.class);
		try {
			ec.exportFile(new File("out/graph_doc_centered_view.pdf"));
		} catch (IOException ex) {
			ex.printStackTrace();
			return;
		}
		this.pack();
		logger.info("Finished building the graph " + this.graphDepthLevel);
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

				File dir = new File("D:\\PhdWorkspace\\Workspace\\ReaderBenchDev2\\in\\chaprou_fr\\chaprou_posttest_fr");
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

				Document refDoc = (Document) AbstractDocument.loadSerializedDocument(
						"D:\\PhdWorkspace\\Workspace\\ReaderBenchDev2\\in\\chaprou_fr\\chaprou_posttest_fr\\109 PERUGI-LANDRE Adrien.ser");
				docs.add(refDoc);

				PaperSimilarityView view = new PaperSimilarityView(docs, refDoc);
				view.setVisible(true);
			}
		});
	}

	public int getGraphDepthLevel() {
		return graphDepthLevel;
	}

	public void setGraphDepthLevel(int graphDepthLevel) {
		this.graphDepthLevel = graphDepthLevel;
	}
}
