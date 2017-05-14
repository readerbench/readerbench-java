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
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.logging.Logger;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JComboBox;
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
import data.AbstractDocumentTemplate;
import data.AbstractDocumentTemplate.BlockTemplate;
import data.Word;
import data.discourse.SemanticCohesion;
import data.discourse.Keyword;
import data.document.Document;
import services.commons.Formatting;
import view.models.PreviewSketch;

public class SearchSimilarityView extends JFrame {

    public static final double INITIAL_DOC_THRESHOLD = 0.4;

    private static final long serialVersionUID = -8582615231233815258L;
    static Logger logger = Logger.getLogger("");
    public static final Color COLOR_CONCEPT = new Color(204, 204, 204); // silver

    private List<Document> docs;
    private AbstractDocument query;
    private Map<AbstractDocument, Boolean> visibleDocs;
    private JSlider sliderThreshold;
    private JPanel panelGraph;
    private JLabel lblThreshold;
    private int graphDepthLevel;
    private JTable tableCentrality;
    private DefaultTableModel tableCentralityModel;

    private class CompareDocsSim implements Comparable<CompareDocsSim> {

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

        public double getSim() {
            return sim;
        }

        @Override
        public int compareTo(CompareDocsSim o) {
            return new Double(o.getSim()).compareTo(this.getSim());
        }

        @Override
        public boolean equals(Object obj) {
            if (this == null || obj == null) {
                return false;
            }
            CompareDocsSim o = (CompareDocsSim) obj;
            return this.getDoc().equals(o.getDoc());
        }

        @Override
        public String toString() {
            return Formatting.formatNumber(this.getSim()) + ": " + new File(this.getDoc().getPath()).getName() + " >> "
                    + this.getDoc().getText();
        }
    }

    private void computeSimilarTopics() {
        Map<Word, Double> topicScoreMap = new TreeMap<>();

        // List<Topic> topicL = new ArrayList<Topic>();
        for (AbstractDocument d : docs) {
            List<Keyword> docTopics = d.getTopics();
            Collections.sort(docTopics, (Keyword t1, Keyword t2) -> -Double.compare(t1.getRelevance(), t2.getRelevance()));
            for (int i = 0; i < Math.min(20, docTopics.size()); i++) {
                if (!topicScoreMap.containsKey(docTopics.get(i).getWord())) {
                    topicScoreMap.put(docTopics.get(i).getWord(), docTopics.get(i).getRelevance());
                } else {
                    double topicRel = topicScoreMap.get(docTopics.get(i).getWord()) + docTopics.get(i).getRelevance();
                    topicScoreMap.put(docTopics.get(i).getWord(), topicRel);
                }
            }
        }

        List<Keyword> topicL = new ArrayList<>();
        Iterator<Map.Entry<Word, Double>> mapIter = topicScoreMap.entrySet().iterator();
        while (mapIter.hasNext()) {
            Map.Entry<Word, Double> entry = mapIter.next();
            topicL.add(new Keyword(entry.getKey(), entry.getValue()));
        }
        Collections.sort(topicL);
        for (Keyword t : topicL) {
            double relevance = SemanticCohesion.getAverageSemanticModelSimilarity(t.getElement(), this.query);
            t.setRelevance(relevance);
            System.out.print(relevance + " ");
        }
        Collections.sort(topicL, (Keyword t1, Keyword t2) -> -Double.compare(t1.getRelevance(), t2.getRelevance()));

        for (Keyword t : topicL) {
            System.out.print(t.getWord().getText() + "->" + t.getRelevance() + " ");
        }

        for (Keyword t : this.query.getTopics()) {
            System.out.println("-> " + t.getWord());
        }
    }

    public SearchSimilarityView(List<Document> docs, String query) {
        this.setGraphDepthLevel(1);
        setTitle("Search Graph");
        getContentPane().setBackground(Color.WHITE);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        this.docs = docs;
        AbstractDocumentTemplate contents = new AbstractDocumentTemplate();
        BlockTemplate block = contents.new BlockTemplate();
        block.setId(0);
        block.setContent(query);
        contents.getBlocks().add(block);

        this.query = new Document(null, contents, docs.get(0).getSemanticModels(), docs.get(0).getLanguage(), true);
        this.query.computeAll(true);
        this.query.setTitleText(query);

        this.computeSimilarTopics();
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

        sliderThreshold = new JSlider(30, 80, 50);
        sliderThreshold.setBackground(Color.WHITE);
        sliderThreshold.setPaintTicks(true);
        sliderThreshold.setFont(new Font("SansSerif", Font.PLAIN, 10));
        sliderThreshold.setPaintLabels(true);
        sliderThreshold.setMinorTickSpacing(10);
        sliderThreshold.setMajorTickSpacing(50);
        java.util.Hashtable<Integer, JLabel> labelTableThreshold = new java.util.Hashtable<Integer, JLabel>();
        labelTableThreshold.put(new Integer(80), new JLabel("80%"));
        labelTableThreshold.put(new Integer(50), new JLabel("50%"));
        labelTableThreshold.put(new Integer(30), new JLabel("30%"));
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

        JLabel lblCentrality = new JLabel("Top similar documents");
        lblCentrality.setFont(new Font("SansSerif", Font.BOLD, 14));
        String[] header2 = {"Article", "Similarity"};
        String[][] data2 = new String[0][2];
        tableCentralityModel = new DefaultTableModel(data2, header2);
        tableCentrality = new JTable(tableCentralityModel) {
            private static final long serialVersionUID = 1L;

            public boolean isCellEditable(int row, int column) {
                return false;
            }
        ;
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
                        String docC = query.getText();
                        String doc2 = tableCentrality.getValueAt(row, 0).toString();
                        String score = tableCentrality.getValueAt(row, 1).toString();

                        JOptionPane.showMessageDialog(SearchSimilarityView.this,
                                "<html><b>Query:</b> " + docC + "<br> <b>Selected document:</b> " + doc2
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

        Integer[] graphLevels = {1, 2, 3};

        JLabel lblComboBox = new JLabel("Depth Level");
        lblComboBox.setFont(new Font("SansSerif", Font.BOLD, 12));

        JComboBox<Integer> docLevelsCombo = new JComboBox<Integer>(graphLevels);
        docLevelsCombo.setSelectedIndex(0);
        docLevelsCombo.addActionListener((ActionEvent e) -> {
            JComboBox<?> cb = (JComboBox<?>) e.getSource();
            int levelSelected = (Integer) cb.getSelectedItem();
            SearchSimilarityView.this.setGraphDepthLevel(levelSelected);
            generateGraph();
        });

        JButton btnNewButton = new JButton("Show Concepts");
        btnNewButton.addActionListener((ActionEvent e) -> {
            if (visibleDocs == null) {
                return;
            }
            List<AbstractDocument> docList = new ArrayList<>();
            Iterator<Entry<AbstractDocument, Boolean>> docIterator = visibleDocs.entrySet().iterator();
            while (docIterator.hasNext()) {
                Entry<AbstractDocument, Boolean> docEntry = docIterator.next();
                if (docEntry.getValue() && !query.equals(docEntry.getKey())) {
                    docList.add(docEntry.getKey());
                }
            }

            SearchConceptView view = new SearchConceptView(docList, query);
            view.setVisible(true);
        });

        GroupLayout groupLayout = new GroupLayout(getContentPane());
        groupLayout.setHorizontalGroup(groupLayout.createParallelGroup(Alignment.LEADING).addGroup(groupLayout
                .createSequentialGroup().addContainerGap()
                .addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
                        .addComponent(panelGraph, GroupLayout.DEFAULT_SIZE, 1168, Short.MAX_VALUE)
                        .addGroup(groupLayout.createSequentialGroup()
                                .addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
                                        .addGroup(groupLayout.createSequentialGroup()
                                                .addComponent(sliderThreshold, GroupLayout.PREFERRED_SIZE,
                                                        GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(ComponentPlacement.RELATED).addComponent(lblComboBox))
                                        .addComponent(lblThreshold))
                                .addPreferredGap(ComponentPlacement.RELATED)
                                .addComponent(docLevelsCombo, GroupLayout.PREFERRED_SIZE, 51,
                                        GroupLayout.PREFERRED_SIZE)
                                .addGap(55).addComponent(btnNewButton, GroupLayout.PREFERRED_SIZE, 182,
                                GroupLayout.PREFERRED_SIZE)))
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
                                        .addGroup(groupLayout.createSequentialGroup()
                                                .addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
                                                        .addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
                                                                .addComponent(lblComboBox).addComponent(docLevelsCombo,
                                                                GroupLayout.PREFERRED_SIZE, 30,
                                                                GroupLayout.PREFERRED_SIZE))
                                                        .addComponent(btnNewButton, GroupLayout.PREFERRED_SIZE, 27,
                                                                GroupLayout.PREFERRED_SIZE))
                                                .addGap(27).addComponent(panelGraph, GroupLayout.DEFAULT_SIZE, 627,
                                                Short.MAX_VALUE))
                                        .addGroup(groupLayout.createSequentialGroup().addGap(13).addComponent(
                                                tableScrollCentrality, GroupLayout.DEFAULT_SIZE, 671,
                                                Short.MAX_VALUE)))))
                .addContainerGap()));
        getContentPane().setLayout(groupLayout);
    }

    public void buildConceptGraph(UndirectedGraph graph, GraphModel graphModel, double threshold, int currentLevel,
            AbstractDocument refDoc, Map<AbstractDocument, Node> nodes) {
        if (currentLevel > this.graphDepthLevel) {
            return;
        }
        visibleDocs = new TreeMap<>();
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
                double sim = SemanticCohesion.getAverageSemanticModelSimilarity(refDoc, d);

                // difference to initial document
                double simRef = 1.0;
                if (currentLevel > 1) {
                    simRef = SemanticCohesion.getAverageSemanticModelSimilarity(this.query, d);
                }

                if (sim >= threshold && simRef >= INITIAL_DOC_THRESHOLD
                        && !refDoc.getProcessedText().equals(d.getProcessedText())) {
                    visibleDocs.put(d, true);
                }
            }
        }

        for (AbstractDocument d : docs) {
            if (nodes.get(d) == null && visibleDocs.get(d) == true) {
                createGraphNode(graph, graphModel, currentLevel, nodes, d, false);
            }
        }

        // determine similarities
        for (AbstractDocument d : docs) {
            if (!refDoc.equals(d) && visibleDocs.get(d)) {
                double sim = SemanticCohesion.getAverageSemanticModelSimilarity(refDoc, d);
                if (sim >= threshold && !refDoc.getProcessedText().equals(d.getProcessedText())) {
                    Edge e = graphModel.factory().newEdge(nodes.get(refDoc), nodes.get(d), 0, sim, false);
                    e.setLabel(sim + "");
                    graph.addEdge(e);
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

    /**
     * @param graph
     * @param graphModel
     * @param currentLevel
     * @param nodes
     * @param d
     * @param isQuery
     */
    private void createGraphNode(UndirectedGraph graph, GraphModel graphModel, int currentLevel,
            Map<AbstractDocument, Node> nodes, AbstractDocument d, boolean isQuery) {
        if (!nodes.containsKey(d)) {
            if (isQuery) {
                Node n = graphModel.factory().newNode(d.getTitleText());
                n.setLabel(d.getTitleText());
                n.setSize(20);
                n.setColor(new Color(1.0f, 0.0f, 0.0f));
                n.setX((float) ((0.01 + Math.random()) * 1000) - 500);
                n.setY((float) ((0.01 + Math.random()) * 1000) - 500);
                graph.addNode(n);
                nodes.put(d, n);
            } else {
                String text = "";
                if (d.getTitleText() != null) {
                    text += d.getTitleText();
                }
                text += "(" + d.getText();
                text = ((text.length() > 40) ? (text.substring(0, 40) + "...") : text) + ")";

                Node n = graphModel.factory().newNode(text);
                n.setLabel(text);
                n.setSize(10);
                n.setX((float) ((0.01 + Math.random()) * 1000) - 500);
                n.setY((float) ((0.01 + Math.random()) * 1000) - 500);
                n.setColor(new Color(1.0f - ((float) (COLOR_CONCEPT.getRed()) / (256 * (currentLevel + 1))),
                        1.0f - ((float) (COLOR_CONCEPT.getGreen()) / (256 * (currentLevel + 1))),
                        1.0f - ((float) (COLOR_CONCEPT.getBlue()) / (256 * (currentLevel + 1)))));
                graph.addNode(nodes.get(d));
                nodes.put(d, n);
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
        Map<AbstractDocument, Node> nodes = new TreeMap<>();

        // create root node
        createGraphNode(graph, graphModel, 0, nodes, this.query, true);
        // visibleDocs.put(this.referenceDoc, true);
        buildConceptGraph(graph, graphModel, threshold, 1, this.query, nodes);

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

        /* similarity to the central article */
        List<CompareDocsSim> similarities = new ArrayList<>();
        Iterator<AbstractDocument> docIt = nodes.keySet().iterator();
        while (docIt.hasNext()) {
            AbstractDocument d = docIt.next();
            if (d != this.query) {
                double sim = SemanticCohesion.getAverageSemanticModelSimilarity(this.query, d);
                similarities.add(new CompareDocsSim(d, sim));
            }
        }

        Collections.sort(similarities);
        if (tableCentralityModel.getRowCount() > 0) {
            for (int i = tableCentralityModel.getRowCount() - 1; i >= 0; i--) {
                tableCentralityModel.removeRow(i);
            }
        }
        for (CompareDocsSim sim : similarities) {
            String row[] = new String[2];
            row[0] = sim.doc.getTitleText();
            row[1] = Formatting.formatNumber(sim.sim) + "";
            tableCentralityModel.addRow(row);
        }
        tableCentralityModel.fireTableDataChanged();
        /* end similarity to central article */

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

        previewModel.getProperties().putValue(PreviewProperty.NODE_LABEL_FONT,
                previewModel.getProperties().getFontValue(PreviewProperty.NODE_LABEL_FONT).deriveFont(Font.PLAIN, 30));
        previewModel.getProperties().putValue(PreviewProperty.NODE_LABEL_PROPORTIONAL_SIZE, Boolean.FALSE);
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
