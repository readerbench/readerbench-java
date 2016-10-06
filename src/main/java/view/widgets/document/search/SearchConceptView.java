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

import data.AbstractDocument;
import data.AnalysisElement;
import data.Word;
import data.discourse.SemanticCohesion;
import data.discourse.Keyword;
import services.commons.Formatting;
import services.commons.VectorAlgebra;
import services.semanticModels.LDA.LDA;
import view.models.PreviewSketch;

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
			List<Keyword> docTopics = d.getTopics();
			Collections.sort(docTopics, new Comparator<Keyword>() {
				public int compare(Keyword t1, Keyword t2) {
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

		List<Keyword> topicL = new ArrayList<Keyword>();
		Iterator<Map.Entry<Word, Double>> mapIter = topicScoreMap.entrySet().iterator();
		while (mapIter.hasNext()) {
			Map.Entry<Word, Double> entry = mapIter.next();
			topicL.add(new Keyword(entry.getKey(), entry.getValue()));
		}
		Collections.sort(topicL);

		// get max 50 words
		List<Word> wordList = new ArrayList<Word>();
		int count = 0;
		while (true) {
			if (wordList.size() >= 50 || count >= topicL.size())
				break;

			Keyword t = topicL.get(count);
			if (!isReferencedWordStem(t.getWord()) && !containsStem(wordList, t.getWord())) {
				wordList.add(t.getWord());
			}

			count++;
		}

		similarWordL = wordList;
		referenceWordL = new ArrayList<Word>();

		for (Keyword t : this.referenceDoc.getTopics()) {
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
						e.printStackTrace();
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

		TreeMap<Word, Boolean> visibleWords = new TreeMap<Word, Boolean>();

		for (Word d : referenceWordL) {
			visibleWords.put(d, true);
		}
		for (Word d : similarWordL) {
			visibleWords.put(d, false);
		}

		logger.info("Starting to build the concept graph");
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
					Node n = graphModel.factory().newNode(text);
					n.setLabel(text);
					n.setSize(10);
					n.setColor(new Color(1.0f, 0.0f, 0.0f));
					n.setX((float) ((0.01 + Math.random()) * 1000) - 500);
					n.setY((float) ((0.01 + Math.random()) * 1000) - 500);
					graph.addNode(nodes.get(w));
					nodes.put(w, n);
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
					Node n = graphModel.factory().newNode(text);
					n.setLabel(text);
					n.setSize(10);
					n.setColor(new Color(0.0f, 1.0f, 0.0f));
					n.setX((float) ((0.01 + Math.random()) * 1000) - 500);
					n.setY((float) ((0.01 + Math.random()) * 1000) - 500);
					graph.addNode(nodes.get(w));
					nodes.put(w, n);
				}
			}
		}
		computeMinMax(scoresList, visibleWords);
		for (Word wSim : similarWordL) {
			for (Word wRef : referenceWordL) {
				if (visibleWords.get(wSim) && visibleWords.get(wRef)) {
					double sim = WordDiffContainer.getScore(scoresList, wRef, wSim);
					if (sim >= threshold && sim <= MAX_COHESION) {
						Edge e = graphModel.factory().newEdge(nodes.get(wRef), nodes.get(wSim), 0,
								(sim - scoreMin) / (scoreMax - scoreMin), false);
						e.setLabel(sim + "");
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
		GraphModel graphModel = Lookup.getDefault().lookup(GraphController.class).getGraphModel();
		UndirectedGraph graph = graphModel.getUndirectedGraph();
		AppearanceController appearanceController = Lookup.getDefault().lookup(AppearanceController.class);
		AppearanceModel appearanceModel = appearanceController.getModel();

		// build nodes
		Map<Word, Node> nodes = new TreeMap<Word, Node>();

		// visibleDocs.put(this.referenceDoc, true);
		buildConceptGraph(graph, graphModel, threshold, 1, this.referenceDoc, nodes);

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
		
		// similarity to the reference concepts
		Collections.sort(scoresList);
		if (tableCentralityModel.getRowCount() > 0) {
			for (int i = tableCentralityModel.getRowCount() - 1; i > -1; i--) {
				tableCentralityModel.removeRow(i);
			}
		}

		for (WordDiffContainer sim : scoresList) {
			String row[] = new String[3];
			row[0] = sim.getWRef().getLemma();
			row[1] = sim.getWSim().getLemma();
			row[2] = Formatting.formatNumber(sim.getSimilarity()) + "";
			tableCentralityModel.addRow(row);
		}
		tableCentralityModel.fireTableDataChanged();

		// Get Centrality
		GraphDistance distance = new GraphDistance();
		distance.setDirected(false);
		distance.execute(graphModel);

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

	public int getGraphDepthLevel() {
		return graphDepthLevel;
	}

	public void setGraphDepthLevel(int graphDepthLevel) {
		this.graphDepthLevel = graphDepthLevel;
	}
}
