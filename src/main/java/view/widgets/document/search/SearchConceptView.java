package view.widgets.document.search;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
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
import java.util.Iterator;
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
import javax.swing.border.EtchedBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.DefaultTableModel;

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

import data.AbstractDocument;
import data.AnalysisElement;
import data.Word;
import data.discourse.SemanticCohesion;
import data.discourse.Topic;
import processing.core.PApplet;
import services.commons.VectorAlgebra;
import services.semanticModels.LDA.LDA;

public class SearchConceptView extends JFrame {
	public static final double INITIAL_DOC_THRESHOLD = 0.4;

	public static SearchConceptView paperSimilarityView;
	private static final long serialVersionUID = -8582615231233815258L;
	static Logger logger = Logger.getLogger(SearchConceptView.class);
	public static final Color COLOR_CONCEPT = new Color(204, 204, 204); // silver

	public static final double MAX_COHESION = 0.96;

	private List<AbstractDocument> docs;
	private AbstractDocument referenceDoc;
	List<WordDiffContainer> scoresList;
	private double scoreMin;
	private double scoreMax;

	List<Word> referenceWordL;
	List<Word> similarWordL;

	private JSlider sliderThreshold;
	private JPanel panelGraph;
	private JLabel lblThreshold;
	private int graphDepthLevel;
	JTable tableCentrality;
	DefaultTableModel tableCentralityModel;

	public static double calculateScore(Word w1, Word w2) {
		double lsaSim = 0;
		double ldaSim = 0;
		if (w1.getLSA() != null)
			lsaSim = w1.getLSA().getSimilarity(w1, w2);
		if (w2.getLDA() != null)
			ldaSim = w2.getLDA().getSimilarity(w1, w2);
		double sim = SemanticCohesion.getAggregatedSemanticMeasure(lsaSim, ldaSim);
		return sim;
	}

	private boolean isReferencedWordStem(Word w) {
		for (int i = 0; i < this.referenceDoc.getTopics().size(); i++) {
			if (this.referenceDoc.getTopics().get(i).getWord().getStem().equalsIgnoreCase(w.getStem())) {
				return true;
			}
		}
		return false;
	}

	private boolean containsStem(List<Word> wordList, Word w) {
		for (Word wInside : wordList) {
			if (wInside.getStem().equalsIgnoreCase(w.getStem()))
				return true;
		}
		return false;
	}

	private double computeDistanceFromRefDoc(Word word, AnalysisElement e) {
		try {
			double lsa, lda;

			double[] probDistrib = e.getLDA().getWordProbDistribution(word);

			// determine importance within analysis element
			lsa = VectorAlgebra.cosineSimilarity(word.getLSAVector(), e.getLSAVector());
			lda = LDA.getSimilarity(probDistrib, e.getLDAProbDistribution());
			return SemanticCohesion.getAggregatedSemanticMeasure(lsa, lda);
		} catch (Exception ex) {
			return 0.0;
		}
	}

	private void computeSimilarTerms() {
		Map<Word, Double> topicScoreMap = new TreeMap<Word, Double>();
		for (AbstractDocument d : docs) {
			List<Topic> docTopics = d.getTopics();
			Collections.sort(docTopics, new Comparator<Topic>() {
				public int compare(Topic t1, Topic t2) {
					return -Double.compare(t1.getRelevance(), t2.getRelevance());
				}
			});
			for (int i = 0; i < Math.min(20, docTopics.size()); i++) {
				if (!topicScoreMap.containsKey(docTopics.get(i).getWord())) {
					topicScoreMap.put(docTopics.get(i).getWord(), docTopics.get(i).getRelevance());
				} else {
					double topicRel = topicScoreMap.get(docTopics.get(i).getWord()) + docTopics.get(i).getRelevance();
					topicScoreMap.put(docTopics.get(i).getWord(), topicRel);
				}
			}
		}

		List<Topic> topicL = new ArrayList<Topic>();
		Iterator<Map.Entry<Word, Double>> mapIter = topicScoreMap.entrySet().iterator();
		while (mapIter.hasNext()) {
			Map.Entry<Word, Double> entry = mapIter.next();
			topicL.add(new Topic(entry.getKey(), entry.getValue()));
		}
		Collections.sort(topicL);

		// get max 50 words
		List<Word> wordList = new ArrayList<Word>();
		int count = 0;
		while (true) {
			if (wordList.size() >= 50 || count >= topicL.size())
				break;

			Topic t = topicL.get(count);
			if (!isReferencedWordStem(t.getWord()) && !containsStem(wordList, t.getWord())) {
				wordList.add(t.getWord());
			}

			count++;
		}

		similarWordL = wordList;
		referenceWordL = new ArrayList<Word>();

		for (Topic t : this.referenceDoc.getTopics()) {
			referenceWordL.add(t.getWord());
		}

		for (Word w : similarWordL) {
			double s = computeDistanceFromRefDoc(w, this.referenceDoc);
			System.out.println(w + " -> " + s);
		}

		System.out.println("Reference List = " + referenceWordL);
		System.out.println("Similar Wird List = " + similarWordL);
	}

	public void computeMinMax(List<WordDiffContainer> list, TreeMap<Word, Boolean> visibleWords) {
		scoreMin = 1.0d;
		scoreMax = 0.0d;
		for (WordDiffContainer c : list) {
			if (visibleWords.get(c.getWRef()) && visibleWords.get(c.getWSim())) {
				scoreMin = Math.min(c.getSimilarity(), scoreMin);
				scoreMax = Math.max(c.getSimilarity(), scoreMin);
			}
		}
	}

	public SearchConceptView(List<AbstractDocument> docs, AbstractDocument referenceDoc) {
		paperSimilarityView = this;
		this.setGraphDepthLevel(1);
		setTitle("Query Expansion Graph");
		getContentPane().setBackground(Color.WHITE);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		this.docs = docs;
		this.referenceDoc = referenceDoc;

		this.computeSimilarTerms();
		// adjust view to desktop size
		int margin = 50;
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		setBounds(margin, margin, screenSize.width - margin * 2, screenSize.height - margin * 2);

		generateLayout();
		generateGraph();
	}

	private void generateLayout() {
		lblThreshold = new JLabel("Threshold among concepts");
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

		JLabel lblCentrality = new JLabel("Top similar words");
		lblCentrality.setFont(new Font("SansSerif", Font.BOLD, 14));
		String[] header2 = { "Reference Word", "Word", "Score" };
		String[][] data2 = new String[0][3];
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

		GroupLayout groupLayout = new GroupLayout(getContentPane());
		groupLayout.setHorizontalGroup(groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup().addContainerGap()
						.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
								.addComponent(sliderThreshold, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
										GroupLayout.PREFERRED_SIZE)
								.addComponent(lblThreshold)
								.addComponent(panelGraph, GroupLayout.DEFAULT_SIZE, 1168, Short.MAX_VALUE))
				.addPreferredGap(ComponentPlacement.RELATED)
				.addGroup(groupLayout.createParallelGroup(Alignment.LEADING).addComponent(lblCentrality).addComponent(
						tableScrollCentrality, GroupLayout.PREFERRED_SIZE, 331, GroupLayout.PREFERRED_SIZE))
				.addContainerGap()));
		groupLayout.setVerticalGroup(groupLayout.createParallelGroup(Alignment.LEADING).addGroup(groupLayout
				.createSequentialGroup()
				.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
						.addGroup(groupLayout.createSequentialGroup().addContainerGap().addComponent(lblThreshold)
								.addPreferredGap(ComponentPlacement.RELATED).addComponent(sliderThreshold,
										GroupLayout.PREFERRED_SIZE, 40, GroupLayout.PREFERRED_SIZE))
						.addGroup(groupLayout.createSequentialGroup().addComponent(lblCentrality).addGap(10)
								.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
										.addGroup(groupLayout.createSequentialGroup().addGap(57).addComponent(
												panelGraph, GroupLayout.DEFAULT_SIZE, 627, Short.MAX_VALUE))
										.addGroup(groupLayout.createSequentialGroup().addGap(13).addComponent(
												tableScrollCentrality, GroupLayout.DEFAULT_SIZE, 671,
												Short.MAX_VALUE)))))
				.addContainerGap()));
		getContentPane().setLayout(groupLayout);
	}

	public void buildConceptGraph(UndirectedGraph graph, GraphModel graphModel, double threshold, int currentLevel,
			AbstractDocument refDoc, Map<Word, Node> nodes) {
		if (currentLevel > this.graphDepthLevel)
			return;

		// referenceWordL
		// similarWordL

		TreeMap<Word, Boolean> visibleWords = new TreeMap<Word, Boolean>();

		for (Word d : referenceWordL) {
			visibleWords.put(d, true);
		}
		for (Word d : similarWordL) {
			visibleWords.put(d, false);
		}

		logger.info("Starting to build the concept graph");
		// build connected graph

		// determine similarities in order to determine eligible candidates for
		// visualisation

		scoresList = new ArrayList<WordDiffContainer>();

		for (Word wSim : similarWordL) {

			for (Word wRef : referenceWordL) {
				System.out.println(wRef + " vs. " + wSim);
				double similarity = calculateScore(wRef, wSim);

				scoresList.add(new WordDiffContainer(wRef, wSim, similarity));

				if (similarity >= threshold) {
					visibleWords.put(wSim, true);
				}
			}
		}

		for (Word w : referenceWordL) {
			if (visibleWords.get(w) == true) {
				String text = "";
				if (w.getLemma() != null)
					text += w.getLemma();
				text = (text.length() > 40) ? (text.substring(0, 40) + "..") : text;
				if (nodes.get(w) == null) {
					nodes.put(w, graphModel.factory().newNode(text));
					nodes.get(w).getNodeData().setLabel(text);
					nodes.get(w).getNodeData().setSize(10);
					nodes.get(w).getNodeData().setColor(1.0f, 0.0f, 0.0f);
					graph.addNode(nodes.get(w));
				}
			}
		}

		for (Word w : similarWordL) {
			if (visibleWords.get(w) == true) {
				String text = "";
				if (w.getLemma() != null)
					text += w.getLemma();
				text = (text.length() > 40) ? (text.substring(0, 40) + "..") : text;
				if (nodes.get(w) == null) {
					nodes.put(w, graphModel.factory().newNode(text));
					nodes.get(w).getNodeData().setLabel(text);
					nodes.get(w).getNodeData().setSize(10);
					nodes.get(w).getNodeData().setColor(0.0f, 1.0f, 0.0f);
					graph.addNode(nodes.get(w));
				}
			}
		}
		computeMinMax(scoresList, visibleWords);
		System.out.println("min=" + scoreMin + ", max=" + scoreMax);
		for (Word wSim : similarWordL) {
			for (Word wRef : referenceWordL) {
				if (visibleWords.get(wSim) && visibleWords.get(wRef)) {
					double sim = WordDiffContainer.getScore(scoresList, wRef, wSim);
					if (sim >= threshold && sim <= MAX_COHESION) {
						Edge e = graphModel.factory().newEdge(nodes.get(wRef), nodes.get(wSim));
						float currentSim = (float) ((sim - scoreMin) / (scoreMax - scoreMin));
						System.out.print(currentSim + " ");
						e.setWeight(currentSim);
						e.getEdgeData().setLabel(sim + "");
						graph.addEdge(e);
					}
				}
			}
		}
		logger.info("Generated graph with " + graph.getNodeCount() + " nodes and " + graph.getEdgeCount() + " edges");

	}

	private void generateGraph() {
		double threshold = ((double) sliderThreshold.getValue()) / 100;

		ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
		pc.newProject();

		// get models
		GraphModel graphModel = Lookup.getDefault().lookup(GraphController.class).getModel();
		AttributeModel attributeModel = Lookup.getDefault().lookup(AttributeController.class).getModel();
		UndirectedGraph graph = graphModel.getUndirectedGraph();

		// build nodes
		Map<Word, Node> nodes = new TreeMap<Word, Node>();

		// visibleDocs.put(this.referenceDoc, true);
		buildConceptGraph(graph, graphModel, threshold, 1, this.referenceDoc, nodes);

		/* similarity to the reference concepts */

		Collections.sort(scoresList);

		if (tableCentralityModel.getRowCount() > 0) {
			for (int i = tableCentralityModel.getRowCount() - 1; i > -1; i--) {
				tableCentralityModel.removeRow(i);
			}
		}

		NumberFormat formatter = new DecimalFormat("#0.00");
		for (WordDiffContainer sim : scoresList) {
			String row[] = new String[3];
			row[0] = sim.getWRef().getLemma();
			row[1] = sim.getWSim().getLemma();
			row[2] = formatter.format(sim.getSimilarity());
			tableCentralityModel.addRow(row);
		}
		tableCentralityModel.fireTableDataChanged();
		/* end similarity to central article */

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
		previewModel.getProperties().putValue(PreviewProperty.NODE_LABEL_FONT,
				previewModel.getProperties().getFontValue(PreviewProperty.NODE_LABEL_FONT).deriveFont(Font.BOLD, 30));
		previewModel.getProperties().putValue(PreviewProperty.NODE_LABEL_PROPORTIONAL_SIZE, Boolean.FALSE);
		previewModel.getProperties().putValue(PreviewProperty.EDGE_CURVED, Boolean.FALSE);
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
		if (panelGraph.getComponents().length > 0) {
			panelGraph.removeAll();
			panelGraph.revalidate();
		}
		panelGraph.add(applet, BorderLayout.CENTER);
		panelGraph.validate();
		panelGraph.repaint();

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

	public int getGraphDepthLevel() {
		return graphDepthLevel;
	}

	public void setGraphDepthLevel(int graphDepthLevel) {
		this.graphDepthLevel = graphDepthLevel;
	}
}
