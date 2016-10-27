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
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.logging.Logger;

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

import data.Word;
import data.discourse.SemanticCohesion;
import services.commons.Formatting;
import view.models.PreviewSketch;

public class PaperConceptView extends JFrame {

    private static PaperConceptView paperConceptView;
    private static final long serialVersionUID = -8582615231233815258L;
    static Logger logger = Logger.getLogger("");
    public static final Color COLOR_TOPIC = new Color(204, 204, 204); // silver
    private static final int MIN_SIZE = 5;
    private static final int MAX_SIZE_TOPIC = 20;

    private Map<Word, Double> wordRelevanceMap;
    private JSlider sliderThreshold;
    private JPanel panelGraph;
    private String path;

    private JTable tableCentrality;
    private DefaultTableModel tableCentralityModel;

    class CompareCentralityWord {

        double centrality;
        Word word;

        public CompareCentralityWord(Word word, double centrality) {
            this.word = word;
            this.centrality = centrality;
        }
    }

    public PaperConceptView(Map<Word, Double> wordRelevanceMap, String path) {
        paperConceptView = this;
        setTitle("Network of Concepts Visualization");
        getContentPane().setBackground(Color.WHITE);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        this.wordRelevanceMap = wordRelevanceMap;
        this.path = path;

        // adjust view to desktop size
        int margin = 50;
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setBounds(margin, margin, screenSize.width - margin * 2, screenSize.height - margin * 2);

        // System.out.println("Got " + wordRelevanceMap);
        generateLayout();
        generateGraph();
    }

    private void generateLayout() {
        JLabel lblThreshold = new JLabel("Threshold");
        lblThreshold.setFont(new Font("SansSerif", Font.BOLD, 12));

        sliderThreshold = new JSlider(4, 8, 5);
        sliderThreshold.setBackground(Color.WHITE);
        sliderThreshold.setPaintTicks(true);
        sliderThreshold.setFont(new Font("SansSerif", Font.PLAIN, 10));
        sliderThreshold.setPaintLabels(true);
        sliderThreshold.setMinorTickSpacing(1);
        sliderThreshold.setMajorTickSpacing(5);
        java.util.Hashtable<Integer, JLabel> labelTableThreshold = new java.util.Hashtable<Integer, JLabel>();
        labelTableThreshold.put(new Integer(8), new JLabel("80%"));
        labelTableThreshold.put(new Integer(6), new JLabel("60%"));
        labelTableThreshold.put(new Integer(4), new JLabel("40%"));
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

        JLabel lblCentrality = new JLabel("Relevance");
        lblCentrality.setFont(new Font("SansSerif", Font.BOLD, 14));
        String[] header2 = {"Concept", "Cumulative relevance"};
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
        JScrollPane tableScrollCentrality = new JScrollPane(tableCentrality);
        Dimension tablePreferredCentrality = tableScrollCentrality.getPreferredSize();
        tableScrollCentrality.setPreferredSize(
                new Dimension(tablePreferredCentrality.width / 2, tablePreferredCentrality.height / 3));

        tableCentrality.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent me) {
                JTable table = (JTable) me.getSource();
                Point p = me.getPoint();
                int row = table.rowAtPoint(p);
                if (me.getClickCount() == 2) {
                    String word = tableCentrality.getValueAt(row, 0).toString();
                    String centrality = tableCentrality.getValueAt(row, 1).toString();

                    JOptionPane.showMessageDialog(paperConceptView, "<html><br> <b>Current Word:</b> " + word
                            + "<br> <b>Word Cetrality:</b> " + centrality + "</html>");
                }
            }
        });

        GroupLayout groupLayout = new GroupLayout(getContentPane());
        groupLayout.setHorizontalGroup(groupLayout.createParallelGroup(Alignment.TRAILING).addGroup(groupLayout
                .createSequentialGroup().addContainerGap()
                .addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
                        .addComponent(panelGraph, GroupLayout.DEFAULT_SIZE, 733, Short.MAX_VALUE)
                        .addGroup(groupLayout.createSequentialGroup()
                                .addGroup(groupLayout.createParallelGroup(Alignment.LEADING).addComponent(lblThreshold)
                                        .addComponent(sliderThreshold, GroupLayout.PREFERRED_SIZE,
                                                GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(ComponentPlacement.RELATED, 532, Short.MAX_VALUE)))
                .addPreferredGap(ComponentPlacement.RELATED)
                .addGroup(groupLayout.createParallelGroup(Alignment.LEADING).addComponent(lblCentrality).addComponent(
                        tableScrollCentrality, GroupLayout.PREFERRED_SIZE, 167, GroupLayout.PREFERRED_SIZE))
                .addContainerGap()));
        groupLayout.setVerticalGroup(groupLayout.createParallelGroup(Alignment.LEADING)
                .addGroup(groupLayout.createSequentialGroup().addGap(7)
                        .addGroup(groupLayout.createParallelGroup(Alignment.BASELINE).addComponent(lblThreshold)
                                .addComponent(lblCentrality))
                        .addPreferredGap(ComponentPlacement.UNRELATED)
                        .addComponent(sliderThreshold, GroupLayout.PREFERRED_SIZE, 52, GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(ComponentPlacement.UNRELATED)
                        .addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
                                .addComponent(panelGraph, GroupLayout.DEFAULT_SIZE, 445, Short.MAX_VALUE)
                                .addComponent(tableScrollCentrality, GroupLayout.DEFAULT_SIZE, 445, Short.MAX_VALUE))
                        .addContainerGap()));

        getContentPane().setLayout(groupLayout);
    }

    public HashMap<Node, Word> buildConceptGraph(UndirectedGraph graph, GraphModel graphModel, double threshold) {
        HashMap<Node, Word> outMap = new HashMap<Node, Word>();
        Map<Word, Boolean> visibleConcepts = new TreeMap<Word, Boolean>();
        Map<Word, Node> nodes = new TreeMap<Word, Node>();

        Iterator<Word> wordIt = wordRelevanceMap.keySet().iterator();
        while (wordIt.hasNext()) {
            Word w = wordIt.next();
            visibleConcepts.put(w, false);
        }

        wordIt = wordRelevanceMap.keySet().iterator();
        while (wordIt.hasNext()) {
            Word w1 = wordIt.next();
            Iterator<Word> wordIt2 = wordRelevanceMap.keySet().iterator();
            while (wordIt2.hasNext()) {
                Word w2 = wordIt2.next();
                double sim = SemanticCohesion.getAverageSemanticModelSimilarity(w1, w2);

                if (!w1.equals(w2) && sim >= threshold) {
                    visibleConcepts.put(w1, true);
                    visibleConcepts.put(w2, true);
                }
            }
        }

        Iterator<Map.Entry<Word, Double>> mapRelevanceIter = wordRelevanceMap.entrySet().iterator();
        double min = Double.MAX_VALUE, max = Double.MIN_VALUE;
        while (mapRelevanceIter.hasNext()) {
            Map.Entry<Word, Double> entry = mapRelevanceIter.next();
            Word w = entry.getKey();
            double relevance = entry.getValue();
            if (visibleConcepts.get(w) && relevance >= 0) {
                min = Math.min(min, Math.log(1 + relevance));
                max = Math.max(max, Math.log(1 + relevance));
            }
        }

        mapRelevanceIter = wordRelevanceMap.entrySet().iterator();
        Set<String> labels = new TreeSet<String>();
        while (mapRelevanceIter.hasNext()) {
            Map.Entry<Word, Double> entry = mapRelevanceIter.next();
            Word w = entry.getKey();
            double relevance = entry.getValue();

            if (visibleConcepts.get(w) && !labels.contains(w.getLemma())) {
                labels.add(w.getLemma());
                Node n = graphModel.factory().newNode(w.getLemma());
                n.setLabel(w.getLemma());
                n.setX((float) ((0.01 + Math.random()) * 1000) - 500);
                n.setY((float) ((0.01 + Math.random()) * 1000) - 500);
                if (max != min && relevance >= 0) {
                    n.setSize((float) (MIN_SIZE
                            + (Math.log(1 + relevance) - min) / (max - min) * (MAX_SIZE_TOPIC - MIN_SIZE)));
                } else {
                    n.setSize(MIN_SIZE);
                }
                n.setColor(COLOR_TOPIC);
                graph.addNode(n);
                nodes.put(w, n);
                outMap.put(n, w);
            }
        }

        // determine similarities
        for (Word w1 : visibleConcepts.keySet()) {
            for (Word w2 : visibleConcepts.keySet()) {
                if (!w1.equals(w2) && visibleConcepts.get(w1) && visibleConcepts.get(w2)) {
                    double sim = SemanticCohesion.getAverageSemanticModelSimilarity(w1, w2);

                    if (sim >= threshold) {
                        Edge e = graphModel.factory().newEdge(nodes.get(w1), nodes.get(w2), 0, 1 - sim, false);
                        e.setLabel(sim + "");
                        graph.addEdge(e);
                    }
                }
            }
        }

        logger.info("Generated graph with " + graph.getNodeCount() + " nodes and " + graph.getEdgeCount() + " edges");
        return outMap;
    }

    private void generateGraph() {
        double threshold = ((double) sliderThreshold.getValue()) / 10;

        ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
        pc.newProject();

        // get models
        GraphModel graphModel = Lookup.getDefault().lookup(GraphController.class).getGraphModel();
        UndirectedGraph graph = graphModel.getUndirectedGraph();

        HashMap<Node, Word> nodeMap = buildConceptGraph(graph, graphModel, threshold);

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

        List<CompareCentralityWord> wordList = new ArrayList<>();
        for (Node n : graph.getNodes()) {
            Word w = nodeMap.get(n);
            Double centrality = wordRelevanceMap.get(w);
            wordList.add(new CompareCentralityWord(w, centrality));
        }
        Collections.sort(wordList, (CompareCentralityWord d1, CompareCentralityWord d2) -> -Double.compare(d1.centrality, d2.centrality));
        // recreate table similarity model
        if (tableCentralityModel.getRowCount() > 0) {
            for (int i = tableCentralityModel.getRowCount() - 1; i > -1; i--) {
                tableCentralityModel.removeRow(i);
            }
        }
        for (CompareCentralityWord sim : wordList) {
            // if(sim.centrality > 0.0) {
            String row[] = new String[2];
            row[0] = sim.word.getLemma();
            row[1] = Formatting.formatNumber(sim.centrality) + "";
            tableCentralityModel.addRow(row);
            // }
        }
        tableCentralityModel.fireTableDataChanged();

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

        if (panelGraph.getComponents().length > 0) {
            panelGraph.removeAll();
            panelGraph.revalidate();
        }
        panelGraph.add(previewSketch, BorderLayout.CENTER);

        // Export
        ExportController ec = Lookup.getDefault().lookup(ExportController.class);
        try {
            ec.exportFile(new File(path));
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
