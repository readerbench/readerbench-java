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
package view.widgets.document.corpora;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;
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
import javax.swing.border.EtchedBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.table.DefaultTableModel;

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
import org.gephi.preview.types.DependantOriginalColor;
import org.gephi.project.api.ProjectController;
import org.gephi.statistics.plugin.GraphDistance;
import org.openide.util.Lookup;

import data.AbstractDocument;
import data.discourse.SemanticCohesion;
import data.document.Document;
import java.util.ArrayList;
import java.util.Objects;
import services.commons.Formatting;
import view.models.PreviewSketch;

public class PaperSimilarityView extends JFrame {

    private static final long serialVersionUID = -8582615231233815258L;
    static Logger logger = Logger.getLogger(PaperSimilarityView.class);
    public static final Color COLOR_CONCEPT = new Color(204, 204, 204); // silver

    private final List<Document> docs;
    private final AbstractDocument referenceDoc;
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
            return new Double(o.getSim()).compareTo(this.getSim());
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof CompareDocsSim) {
                CompareDocsSim o = (CompareDocsSim) obj;
                return this.getDoc().equals(o.getDoc());
            }
            return false;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 83 * hash + Objects.hashCode(this.doc);
            hash = 83 * hash + (int) (Double.doubleToLongBits(this.sim) ^ (Double.doubleToLongBits(this.sim) >>> 32));
            return hash;
        }

        @Override
        public String toString() {
            return Formatting.formatNumber(this.getSim()) + ": " + new File(this.getDoc().getPath()).getName() + " >> "
                    + this.getDoc().getText();
        }
    }

    public PaperSimilarityView(List<Document> docs, Document referenceDoc) {
        this.graphDepthLevel = 1;
        super.setTitle("Document Centrality Graph");
        super.getContentPane().setBackground(Color.WHITE);
        super.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        this.docs = docs;
        this.referenceDoc = referenceDoc;

        // adjust view to desktop size
        int margin = 50;
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        super.setBounds(margin, margin, screenSize.width - margin * 2, screenSize.height - margin * 2);

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
        labelTableThreshold.put(80, new JLabel("80%"));
        labelTableThreshold.put(60, new JLabel("60%"));
        labelTableThreshold.put(40, new JLabel("40%"));
        sliderThreshold.setLabelTable(labelTableThreshold);
        sliderThreshold.addChangeListener((ChangeEvent e) -> {
            generateGraph();
        });

        panelGraph = new JPanel();
        panelGraph.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
        panelGraph.setBackground(Color.WHITE);
        panelGraph.setLayout(new BorderLayout());

        JLabel lblCentrality = new JLabel("Top similar articles");
        lblCentrality.setFont(new Font("SansSerif", Font.BOLD, 14));
        String[] header2 = {"Article", "Similarity"};
        String[][] data2 = new String[0][2];
        tableCentralityModel = new DefaultTableModel(data2, header2);
        tableCentrality = new JTable(tableCentralityModel) {
            private static final long serialVersionUID = 1L;

            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
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

                        JOptionPane.showMessageDialog(PaperSimilarityView.this, "<html><b>Central Article:</b> " + docC + "<br> <b>Current Article:</b> " + doc2 + "<br> <b>Semantic Distance:</b> " + score + "</html>");
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

        String[] graphLevels = {"1", "2", "3"};

        JLabel lblComboBox = new JLabel("Depth Level");
        lblComboBox.setFont(new Font("SansSerif", Font.BOLD, 12));

        JComboBox<String> docLevelsCombo = new JComboBox<>(graphLevels);
        docLevelsCombo.setSelectedIndex(0);
        docLevelsCombo.addActionListener((ActionEvent e) -> {
            JComboBox<?> cb = (JComboBox<?>) e.getSource();
            String selectedItem = (String) cb.getSelectedItem();
            int levelSelected = Integer.parseInt(selectedItem);
            PaperSimilarityView.this.setGraphDepthLevel(levelSelected);
            generateGraph();
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
                        .addGroup(groupLayout.createSequentialGroup().addContainerGap()
                                .addComponent(lblThreshold).addPreferredGap(ComponentPlacement.RELATED)
                                .addComponent(sliderThreshold,
                                        GroupLayout.PREFERRED_SIZE, 40, GroupLayout.PREFERRED_SIZE)
                                .addContainerGap(650, Short.MAX_VALUE))
                        .addGroup(groupLayout.createSequentialGroup().addComponent(lblCentrality).addGap(10).addGroup(
                                groupLayout.createParallelGroup(Alignment.LEADING).addGroup(
                                groupLayout.createSequentialGroup().addGroup(groupLayout
                                        .createParallelGroup(Alignment.LEADING)
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
        if (currentLevel > this.graphDepthLevel) {
            return;
        }
        Map<AbstractDocument, Boolean> visibleDocs = new TreeMap<>();
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
                    simRef = SemanticCohesion.getAverageSemanticModelSimilarity(this.referenceDoc, d);
                }

                if (sim >= threshold && simRef >= threshold && !refDoc.getProcessedText().equals(d.getProcessedText())) {
                    visibleDocs.put(d, true);
                }
            }
        }

        for (AbstractDocument d : docs) {
            if (visibleDocs.get(d) == true) {
                String text = "";
                if (d.getTitleText() != null) {
                    text += d.getTitleText();
                }
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
                    double sim = SemanticCohesion.getAverageSemanticModelSimilarity(refDoc, d);
                    if (sim >= threshold && !refDoc.getProcessedText().equals(d.getProcessedText())) {
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

        // build nodes
        Map<AbstractDocument, Node> nodes = new TreeMap<>();

        // visibleDocs.put(this.referenceDoc, true);
        buildConceptGraph(graph, graphModel, threshold, 1, this.referenceDoc, nodes);

        /* similarity to the central article */
        List<CompareDocsSim> similarities = new ArrayList<>();
        Iterator<AbstractDocument> docIt = nodes.keySet().iterator();
        while (docIt.hasNext()) {
            AbstractDocument d = docIt.next();
            if (!this.referenceDoc.equals(d)) {
                double sim = SemanticCohesion.getAverageSemanticModelSimilarity(this.referenceDoc, d);;
                similarities.add(new CompareDocsSim(d, sim));
            }
        }

        Collections.sort(similarities);
        if (tableCentralityModel.getRowCount() > 0) {
            for (int i = tableCentralityModel.getRowCount() - 1; i > -1; i--) {
                tableCentralityModel.removeRow(i);
            }
        }
        for (CompareDocsSim sim : similarities) {
            String row[] = new String[2];
            row[0] = sim.doc.getTitleText();
            row[1] = Formatting.formatNumber(sim.sim).toString();
            tableCentralityModel.addRow(row);
        }
        tableCentralityModel.fireTableDataChanged();
        /* end similarity to central article */

        // Get Centrality
        GraphDistance distance = new GraphDistance();
        distance.setDirected(false);
        distance.execute(graphModel);

        logger.info("Generating preview ...");
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

    public int getGraphDepthLevel() {
        return graphDepthLevel;
    }

    public void setGraphDepthLevel(int graphDepthLevel) {
        this.graphDepthLevel = graphDepthLevel;
    }
}
