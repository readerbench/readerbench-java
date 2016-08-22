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
package view.widgets.document;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.border.EtchedBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.apache.log4j.Logger;
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
import org.gephi.project.api.ProjectController;
import org.gephi.statistics.plugin.GraphDistance;
import org.openide.util.Lookup;

import data.AbstractDocument;
import data.Word;
import data.cscl.Participant;
import data.discourse.SemanticCohesion;
import data.discourse.Topic;
import services.discourse.topicMining.TopicModeling;
import view.models.PreviewSketch;

public class ConceptView extends JFrame {
	private static final long serialVersionUID = -8582615231233815258L;
	static Logger logger = Logger.getLogger(ConceptView.class);
	public static final Color COLOR_TOPIC = new Color(204, 204, 204); // silver
	public static final Color COLOR_INFERRED_CONCEPT = new Color(102, 102, 255); // orchid
	private static final int MIN_SIZE = 10;
	private static final int MAX_SIZE_TOPIC = 20;
	private static final int MAX_SIZE_INFERRED_CONCEPT = 20;

	private AbstractDocument doc;
	private List<Topic> topics;
	private JSlider sliderThreshold;
	private JSlider sliderInferredConcepts;
	private JPanel panelGraph;
	private JTextArea textAreaInferredConcepts;
	private JCheckBox checkBoxNoun;
	private JCheckBox checkBoxVerb;
	private JTextField txtInferredConcepts;
	private JTextField txtTopics;

	public ConceptView(Participant p, AbstractDocument d, List<Topic> topics) {
		if (p != null && p.getName().length() > 0)
			setTitle("ReaderBench - Visualization of " + p.getName() + "'s network of concepts");
		else
			setTitle("ReaderBench - Network of concepts visualization");
		getContentPane().setBackground(Color.WHITE);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		this.doc = d;
		this.topics = topics;

		// adjust view to desktop size
		int margin = 50;
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		setBounds(margin, margin, screenSize.width - margin * 2, screenSize.height - margin * 2);

		TopicModeling.determineInferredConcepts(doc, topics, TopicModeling.SIMILARITY_THRESHOLD);

		generateLayout();
		generateGraph();
	}

	private void generateLayout() {
		JLabel lblInferredConcepts = new JLabel("Inferred concepts");
		lblInferredConcepts.setFont(new Font("SansSerif", Font.BOLD, 12));

		JLabel lblThreshold = new JLabel("Threshold");
		lblThreshold.setFont(new Font("SansSerif", Font.BOLD, 12));

		JLabel lblIdentifyOnly = new JLabel("Identify only:");

		checkBoxVerb = new JCheckBox("Verbs");
		checkBoxVerb.setBackground(Color.WHITE);
		checkBoxVerb.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				generateGraph();
			}
		});
		checkBoxVerb.setSelected(true);

		checkBoxNoun = new JCheckBox("Nouns");
		checkBoxNoun.setBackground(Color.WHITE);
		checkBoxNoun.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				generateGraph();
			}
		});
		checkBoxNoun.setSelected(true);

		sliderInferredConcepts = new JSlider(0, 10, 2);
		sliderInferredConcepts.setBackground(Color.WHITE);
		sliderInferredConcepts.setPaintTicks(true);
		sliderInferredConcepts.setPaintLabels(true);
		sliderInferredConcepts.setMinorTickSpacing(1);
		sliderInferredConcepts.setMajorTickSpacing(5);
		sliderInferredConcepts.setFont(new Font("SansSerif", Font.PLAIN, 10));
		java.util.Hashtable<Integer, JLabel> labelTableConcepts = new java.util.Hashtable<Integer, JLabel>();
		labelTableConcepts.put(new Integer(10), new JLabel("100"));
		labelTableConcepts.put(new Integer(5), new JLabel("50"));
		labelTableConcepts.put(new Integer(0), new JLabel("0"));
		sliderInferredConcepts.setLabelTable(labelTableConcepts);
		sliderInferredConcepts.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				generateGraph();
			}
		});

		sliderThreshold = new JSlider(0, 10, 4);
		sliderThreshold.setBackground(Color.WHITE);
		sliderThreshold.setPaintTicks(true);
		sliderThreshold.setFont(new Font("SansSerif", Font.PLAIN, 10));
		sliderThreshold.setPaintLabels(true);
		sliderThreshold.setMinorTickSpacing(1);
		sliderThreshold.setMajorTickSpacing(5);
		java.util.Hashtable<Integer, JLabel> labelTableThreshold = new java.util.Hashtable<Integer, JLabel>();
		labelTableThreshold.put(new Integer(10), new JLabel("100%"));
		labelTableThreshold.put(new Integer(5), new JLabel("50%"));
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

		JLabel lblListOfInferred = new JLabel("List of displayed inferred concepts");
		lblListOfInferred.setFont(new Font("SansSerif", Font.BOLD, 12));

		JScrollPane scrollPaneInferredConcepts = new JScrollPane();
		scrollPaneInferredConcepts.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

		txtInferredConcepts = new JTextField();
		txtInferredConcepts.setEditable(false);
		txtInferredConcepts.setBackground(COLOR_INFERRED_CONCEPT);
		txtInferredConcepts.setHorizontalAlignment(SwingConstants.CENTER);
		txtInferredConcepts.setFont(new Font("SansSerif", Font.BOLD, 10));
		txtInferredConcepts.setText("Inferred Concepts");
		txtInferredConcepts.setColumns(10);

		txtTopics = new JTextField();
		txtTopics.setEditable(false);
		txtTopics.setHorizontalAlignment(SwingConstants.CENTER);
		txtTopics.setBackground(COLOR_TOPIC);
		txtTopics.setFont(new Font("SansSerif", Font.BOLD, 10));
		txtTopics.setText("Topics");
		txtTopics.setColumns(10);

		GroupLayout groupLayout = new GroupLayout(getContentPane());
		groupLayout.setHorizontalGroup(groupLayout.createParallelGroup(Alignment.TRAILING).addGroup(groupLayout
				.createSequentialGroup().addContainerGap()
				.addGroup(groupLayout.createParallelGroup(Alignment.TRAILING)
						.addComponent(scrollPaneInferredConcepts, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 1168,
								Short.MAX_VALUE)
						.addComponent(panelGraph, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 1168, Short.MAX_VALUE)
						.addGroup(
								Alignment.LEADING, groupLayout.createSequentialGroup().addComponent(lblListOfInferred)
										.addPreferredGap(ComponentPlacement.RELATED, 824, Short.MAX_VALUE)
										.addComponent(txtTopics, GroupLayout.PREFERRED_SIZE, 74,
												GroupLayout.PREFERRED_SIZE)
										.addPreferredGap(ComponentPlacement.RELATED).addComponent(txtInferredConcepts,
												GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
												GroupLayout.PREFERRED_SIZE))
						.addGroup(Alignment.LEADING, groupLayout
								.createSequentialGroup().addGroup(groupLayout.createParallelGroup(
										Alignment.LEADING)
										.addGroup(groupLayout.createSequentialGroup()
												.addGap(6)
												.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
														.addComponent(lblIdentifyOnly)
														.addComponent(checkBoxNoun, GroupLayout.PREFERRED_SIZE, 90,
																GroupLayout.PREFERRED_SIZE)
														.addComponent(checkBoxVerb))
												.addPreferredGap(ComponentPlacement.RELATED)
												.addComponent(sliderInferredConcepts, GroupLayout.PREFERRED_SIZE, 177,
														GroupLayout.PREFERRED_SIZE))
										.addComponent(lblInferredConcepts))
								.addGap(18)
								.addGroup(groupLayout.createParallelGroup(Alignment.LEADING).addComponent(lblThreshold)
										.addComponent(sliderThreshold, GroupLayout.PREFERRED_SIZE,
												GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))))
				.addContainerGap()));
		groupLayout.setVerticalGroup(groupLayout.createParallelGroup(Alignment.LEADING).addGroup(groupLayout
				.createSequentialGroup().addGap(7)
				.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE).addComponent(lblInferredConcepts)
						.addComponent(lblThreshold))
				.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
						.addGroup(groupLayout.createSequentialGroup().addPreferredGap(ComponentPlacement.UNRELATED)
								.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE, false)
										.addComponent(sliderInferredConcepts, GroupLayout.DEFAULT_SIZE, 57,
												Short.MAX_VALUE)
										.addComponent(sliderThreshold, GroupLayout.PREFERRED_SIZE, 52,
												GroupLayout.PREFERRED_SIZE)))
						.addGroup(groupLayout.createSequentialGroup().addGap(6).addComponent(lblIdentifyOnly)
								.addPreferredGap(ComponentPlacement.RELATED).addComponent(checkBoxNoun)
								.addPreferredGap(ComponentPlacement.RELATED).addComponent(checkBoxVerb)))
				.addPreferredGap(ComponentPlacement.UNRELATED)
				.addComponent(panelGraph, GroupLayout.DEFAULT_SIZE, 473, Short.MAX_VALUE)
				.addPreferredGap(ComponentPlacement.RELATED)
				.addGroup(groupLayout.createParallelGroup(Alignment.TRAILING).addComponent(lblListOfInferred)
						.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
								.addComponent(txtInferredConcepts, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
										GroupLayout.PREFERRED_SIZE)
								.addComponent(txtTopics, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
										GroupLayout.PREFERRED_SIZE)))
				.addPreferredGap(ComponentPlacement.RELATED)
				.addComponent(scrollPaneInferredConcepts, GroupLayout.PREFERRED_SIZE, 59, GroupLayout.PREFERRED_SIZE)
				.addContainerGap()));

		textAreaInferredConcepts = new JTextArea();
		scrollPaneInferredConcepts.setViewportView(textAreaInferredConcepts);
		textAreaInferredConcepts.setWrapStyleWord(true);
		textAreaInferredConcepts.setText("");
		textAreaInferredConcepts.setLineWrap(true);
		textAreaInferredConcepts.setEditable(false);
		textAreaInferredConcepts.setBackground(Color.WHITE);
		getContentPane().setLayout(groupLayout);
	}

	public void buildConceptGraph(UndirectedGraph graph, GraphModel graphModel, AbstractDocument d, double threshold) {
		// build connected graph
		Map<Word, Boolean> visibleConcepts = new TreeMap<Word, Boolean>();
		// build nodes
		Map<Word, Node> nodes = new TreeMap<Word, Node>();

		List<Topic> subListInferredConcepts = TopicModeling.getSublist(doc.getInferredConcepts(),
				sliderInferredConcepts.getValue() * 10, checkBoxNoun.isSelected(), checkBoxVerb.isSelected());

		for (Topic t : topics) {
			visibleConcepts.put(t.getWord(), false);
		}
		for (Topic t : subListInferredConcepts) {
			visibleConcepts.put(t.getWord(), false);
		}

		// determine similarities
		for (Word w1 : visibleConcepts.keySet()) {
			for (Word w2 : visibleConcepts.keySet()) {
				double lsaSim = 0;
				double ldaSim = 0;
				if (d.getLSA() != null)
					lsaSim = d.getLSA().getSimilarity(w1, w2);
				if (d.getLDA() != null)
					ldaSim = d.getLDA().getSimilarity(w1, w2);
				double sim = SemanticCohesion.getAggregatedSemanticMeasure(lsaSim, ldaSim);
				if (!w1.equals(w2) && sim >= threshold) {
					visibleConcepts.put(w1, true);
					visibleConcepts.put(w2, true);
				}
			}
		}

		// determine optimal sizes
		double min = Double.MAX_VALUE, max = Double.MIN_VALUE;
		for (Topic t : topics) {
			if (visibleConcepts.get(t.getWord()) && t.getRelevance() >= 0) {
				min = Math.min(min, Math.log(1 + t.getRelevance()));
				max = Math.max(max, Math.log(1 + t.getRelevance()));
			}
		}

		for (Topic t : topics) {
			if (visibleConcepts.get(t.getWord())) {
				Node n = graphModel.factory().newNode(t.getWord().getLemma());
				n.setLabel(t.getWord().getLemma());
				if (max != min && t.getRelevance() >= 0) {
					n.setSize((float) (MIN_SIZE
							+ (Math.log(1 + t.getRelevance()) - min) / (max - min) * (MAX_SIZE_TOPIC - MIN_SIZE)));
				} else {
					n.setSize(MIN_SIZE);
				}
				n.setColor(new Color((float) (COLOR_TOPIC.getRed()) / 256, (float) (COLOR_TOPIC.getGreen()) / 256,
						(float) (COLOR_TOPIC.getBlue()) / 256));
				n.setX((float) ((0.01 + Math.random()) * 1000) - 500);
				n.setY((float) ((0.01 + Math.random()) * 1000) - 500);
				graph.addNode(n);
				nodes.put(t.getWord(), n);
			}
		}

		min = Double.MAX_VALUE;
		max = Double.MIN_VALUE;

		for (Topic t : subListInferredConcepts) {
			if (visibleConcepts.get(t.getWord()) && t.getRelevance() >= 0) {
				min = Math.min(min, Math.log(1 + t.getRelevance()));
				max = Math.max(max, Math.log(1 + t.getRelevance()));
			}
		}

		for (Topic t : subListInferredConcepts) {
			if (visibleConcepts.get(t.getWord()) && !nodes.containsKey(t.getWord())) {
				Node n = graphModel.factory().newNode(t.getWord().getLemma());
				n.setLabel(t.getWord().getLemma());

				if (max != min && t.getRelevance() >= 0) {
					n.setSize((float) (MIN_SIZE + (Math.log(1 + t.getRelevance()) - min) / (max - min)
							* (MAX_SIZE_INFERRED_CONCEPT - MIN_SIZE)));
				} else {
					n.setSize(MIN_SIZE);
				}
				n.setColor(new Color((float) (COLOR_INFERRED_CONCEPT.getRed()) / 256,
						(float) (COLOR_INFERRED_CONCEPT.getGreen()) / 256,
						(float) (COLOR_INFERRED_CONCEPT.getBlue()) / 256));
				nodes.put(t.getWord(), n);
				graph.addNode(n);
			}
		}

		// determine similarities
		for (Word w1 : visibleConcepts.keySet()) {
			for (Word w2 : visibleConcepts.keySet()) {
				if (!w1.equals(w2) && visibleConcepts.get(w1) && visibleConcepts.get(w2)) {
					double lsaSim = 0;
					double ldaSim = 0;
					if (d.getLSA() != null) {
						lsaSim = d.getLSA().getSimilarity(w1, w2);
					}
					if (d.getLDA() != null) {
						ldaSim = d.getLDA().getSimilarity(w1, w2);
					}
					double sim = SemanticCohesion.getAggregatedSemanticMeasure(lsaSim, ldaSim);
					if (sim >= threshold) {
						Edge e = graphModel.factory().newEdge(nodes.get(w1), nodes.get(w2), 0, 1 - sim, false);
						e.setLabel(sim + "");
						graph.addEdge(e);
					}
				}
			}
		}

		textAreaInferredConcepts.setText("");

		for (Topic t : subListInferredConcepts) {
			if (visibleConcepts.get(t.getWord())) {
				textAreaInferredConcepts.append(t.getWord().getLemma() + " ");
			}
		}

		logger.info("Generated graph with " + graph.getNodeCount() + " nodes and " + graph.getEdgeCount() + " edges");
	}

	private void generateGraph() {
		double threshold = ((double) sliderThreshold.getValue()) / 10;

		ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
		pc.newProject();

		// get models
		GraphModel graphModel = Lookup.getDefault().lookup(GraphController.class).getGraphModel();
		UndirectedGraph graph = graphModel.getUndirectedGraph();

		buildConceptGraph(graph, graphModel, doc, threshold);

		// RankingController rankingController = Lookup.getDefault().lookup(
		// RankingController.class);

		// Get Centrality
		GraphDistance distance = new GraphDistance();
		distance.setDirected(false);
		distance.execute(graphModel);

		// Preview configuration
		PreviewController previewController = Lookup.getDefault().lookup(PreviewController.class);
		PreviewModel previewModel = previewController.getModel();
		previewModel.getProperties().putValue(PreviewProperty.SHOW_NODE_LABELS, Boolean.TRUE);
		previewModel.getProperties().putValue(PreviewProperty.NODE_LABEL_PROPORTIONAL_SIZE, Boolean.TRUE);
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
			ec.exportFile(new File("out/graph.pdf"));
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
}
