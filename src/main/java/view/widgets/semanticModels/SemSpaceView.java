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
package view.widgets.semanticModels;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.File;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.gephi.appearance.api.AppearanceController;
import org.gephi.appearance.api.AppearanceModel;
import org.gephi.appearance.api.Function;
import org.gephi.appearance.plugin.RankingElementColorTransformer;
import org.gephi.appearance.plugin.RankingNodeSizeTransformer;
import org.gephi.graph.api.Column;
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
import org.gephi.preview.types.DependantOriginalColor;
import org.gephi.project.api.ProjectController;
import org.gephi.statistics.plugin.GraphDistance;
import org.openide.util.Lookup;

import data.Lang;
import org.openide.util.Exceptions;
import services.semanticModels.GenerateSpace;
import services.semanticModels.ISemanticModel;
import services.semanticModels.LDA.LDA;
import view.models.PreviewSketch;
import view.widgets.ReaderBenchView;

public class SemSpaceView extends JFrame {

    private static final long serialVersionUID = 1L;
    public static final Color COLOR_ORIGINAL_CONCEPT = new Color(176, 46, 46);

    static Logger logger = Logger.getLogger(SemSpaceView.class);

    public static final int MIN_NODE_SIZE = 10;
    public static final int MAX_NODE_SIZE = 20;

    private ISemanticModel semModel = null;
    private JSplitPane viewSplitPane = null;
    private JPanel adjustmentsPanel = null;
    private JPanel networkPanel = null;
    private JLabel NeighborsLabel = null;
    private GenerateSpace lsaProc = null;
    private JLabel wordLabel = null;
    private JTextField wordTextField = null;
    private JButton startButton = null;
    private JSlider thresholdSlider;
    private JLabel lblMaxDepth;
    private JSlider depthSlider;
    private JPanel panel;

    /**
     * This is the default constructor
     */
    public SemSpaceView(ISemanticModel semModel) {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        logger.info("Starting configuration load");
        this.semModel = semModel;
        lsaProc = new GenerateSpace(semModel);
        logger.info("Configuration loaded");
        initialize();
    }

    /**
     * This method initializes this
     *
     * @return void
     */
    private void initialize() {
        this.setPreferredSize(new Dimension(1000, 700));
        this.setSize(new Dimension(1000, 700));
        this.setResizable(true);
        this.setContentPane(getViewSplitPane());
        this.setTitle("Vector Space Vizualization - " + semModel.getPath());
    }

    private void generateNetwork() {
        networkPanel.removeAll();
        double threshold = ((double) thresholdSlider.getValue()) / 10;
        int depth = depthSlider.getValue();

        // Init a project - and therefore a workspace
        ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
        pc.newProject();

        // Get a graph model - it exists because we have a workspace
        GraphModel graphModel = Lookup.getDefault().lookup(GraphController.class).getGraphModel();
        UndirectedGraph graph = graphModel.getUndirectedGraph();
        AppearanceController appearanceController = Lookup.getDefault().lookup(AppearanceController.class);
        AppearanceModel appearanceModel = appearanceController.getModel();

        if (wordTextField.getText().length() == 0) {
            JOptionPane.showMessageDialog(viewSplitPane, "Please enter a word!", "Error", JOptionPane.WARNING_MESSAGE);
            this.pack();
            return;
        } else {
            lsaProc.buildGraph(graph, graphModel, wordTextField.getText(), threshold, depth);
            logger.info(
                    wordTextField.getText() + " - nodes: " + graph.getNodeCount() + " edges: " + graph.getEdgeCount());
        }

        // Run YifanHuLayout for 100 passes
        YifanHuLayout layout = new YifanHuLayout(null, new StepDisplacement(1f));
        layout.setGraphModel(graphModel);
        layout.resetPropertiesValues();
        layout.setOptimalDistance(1000f);

        layout.initAlgo();
        for (int i = 0; i < 100 && layout.canAlgo(); i++) {
            layout.goAlgo();
        }
        layout.endAlgo();

        // ForceAtlas2 layout = new ForceAtlas2(null);
        // layout.setGraphModel(graphModel);
        // layout.resetPropertiesValues();
        //
        // layout.setOutboundAttractionDistribution(false);
        // layout.setEdgeWeightInfluence(1.5d);
        // layout.setGravity(10d);
        // layout.setJitterTolerance(.02);
        // layout.setScalingRatio(15.0);
        // layout.initAlgo();
        // Rank color by Degree
        Function degreeRanking = appearanceModel.getNodeFunction(graph, AppearanceModel.GraphFunction.NODE_DEGREE,
                RankingElementColorTransformer.class);
        RankingElementColorTransformer degreeTransformer = (RankingElementColorTransformer) degreeRanking
                .getTransformer();
        degreeTransformer.setColors(new Color[]{new Color(0x9C9C9C), new Color(0xEDEDED)});
        degreeTransformer.setColorPositions(new float[]{0f, 1f});
        appearanceController.transform(degreeRanking);

        logger.info("Performing SNA...");
        // Perform SNA
        GraphDistance distance = new GraphDistance();
        distance.setDirected(false);
        distance.execute(graphModel);

        logger.info("Ranking size...");
        // Rank size by centrality
        Column centralityColumn = graphModel.getNodeTable().getColumn(GraphDistance.BETWEENNESS);
        Function centralityRanking = appearanceModel.getNodeFunction(graph, centralityColumn,
                RankingNodeSizeTransformer.class);
        RankingNodeSizeTransformer centralityTransformer = (RankingNodeSizeTransformer) centralityRanking
                .getTransformer();
        centralityTransformer.setMinSize(MIN_NODE_SIZE);
        centralityTransformer.setMaxSize(MAX_NODE_SIZE);
        appearanceController.transform(centralityRanking);

        // augment the central node and make it more visible
        for (Node n : graph.getNodes()) {
            if (n.getLabel().equals(wordTextField.getText())) {
                n.setSize(MAX_NODE_SIZE);
                n.setColor(new Color((float) (COLOR_ORIGINAL_CONCEPT.getRed()) / 256,
                        (float) (COLOR_ORIGINAL_CONCEPT.getGreen()) / 256,
                        (float) (COLOR_ORIGINAL_CONCEPT.getBlue()) / 256));
                break;
            }
        }

        logger.info("Generating preview...");
        // Preview configuration
        PreviewController previewController = Lookup.getDefault().lookup(PreviewController.class);
        PreviewModel previewModel = previewController.getModel();
        previewModel.getProperties().putValue(PreviewProperty.SHOW_NODE_LABELS, Boolean.TRUE);
        previewModel.getProperties().putValue(PreviewProperty.NODE_LABEL_COLOR,
                new DependantOriginalColor(Color.BLACK));
        previewModel.getProperties().putValue(PreviewProperty.EDGE_RADIUS, 10f);
        previewModel.getProperties().putValue(PreviewProperty.SHOW_EDGE_LABELS, Boolean.TRUE);
        previewModel.getProperties().putValue(PreviewProperty.NODE_LABEL_PROPORTIONAL_SIZE, Boolean.TRUE);
        previewModel.getProperties().putValue(PreviewProperty.EDGE_CURVED, Boolean.TRUE);

        // New Processing target, get the PApplet
        G2DTarget target = (G2DTarget) previewController.getRenderTarget(RenderTarget.G2D_TARGET);
        PreviewSketch previewSketch = new PreviewSketch(target);
        previewController.refreshPreview();
        previewSketch.resetZoom();
        networkPanel.add(previewSketch, BorderLayout.CENTER);

        // Export
        logger.info("Saving export...");
        ExportController ec = Lookup.getDefault().lookup(ExportController.class);
        try {
            ec.exportFile(new File("out/graph_" + wordTextField.getText() + "_" + (new File(semModel.getPath()).getName()) + ".pdf"));
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
            return;
        }
        this.pack();
    }

    /**
     * This method initializes viewSplitPane
     *
     * @return javax.swing.JSplitPane
     */
    private JSplitPane getViewSplitPane() {
        if (viewSplitPane == null) {
            viewSplitPane = new JSplitPane();
            viewSplitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
            viewSplitPane.setContinuousLayout(true);
            viewSplitPane.setBackground(new Color(250, 250, 250));
            viewSplitPane.setTopComponent(getAdjustmentsPanel());
            viewSplitPane.setBottomComponent(getNetworkPanel());
            viewSplitPane.setDividerSize(5);
        }
        return viewSplitPane;
    }

    /**
     * This method initializes adjustmentsPanel
     *
     * @return javax.swing.JPanel
     */
    private JPanel getAdjustmentsPanel() {
        if (adjustmentsPanel == null) {

            GridBagConstraints gridBagConstraints8 = new GridBagConstraints();
            gridBagConstraints8.gridx = 1;
            gridBagConstraints8.gridy = 5;

            adjustmentsPanel = new JPanel();
            // adjustmentsPanel.setLayout(null);

            GridBagLayout gbl_adjustmentsPanel = new GridBagLayout();
            gbl_adjustmentsPanel.rowWeights = new double[]{1.0, 0.0, 0.0};
            gbl_adjustmentsPanel.columnWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0};

            adjustmentsPanel.setLayout(gbl_adjustmentsPanel);
            adjustmentsPanel.setBackground(new Color(250, 250, 250));

            GridBagConstraints gridBagConstraints11 = new GridBagConstraints();
            gridBagConstraints11.gridx = 0;
            gridBagConstraints11.anchor = GridBagConstraints.WEST;
            gridBagConstraints11.insets = new Insets(5, 10, 5, 5);
            gridBagConstraints11.gridy = 0;

            wordLabel = new JLabel();
            wordLabel.setText("Word:");
            wordLabel.setHorizontalTextPosition(SwingConstants.LEFT);
            wordLabel.setHorizontalAlignment(SwingConstants.LEFT);
            adjustmentsPanel.add(wordLabel, gridBagConstraints11);

            GridBagConstraints gbc_panel = new GridBagConstraints();
            gbc_panel.gridwidth = 5;
            gbc_panel.insets = new Insets(5, 5, 5, 5);
            gbc_panel.fill = GridBagConstraints.BOTH;
            gbc_panel.gridx = 1;
            gbc_panel.gridy = 0;
            adjustmentsPanel.add(getPanel(), gbc_panel);

            GridBagConstraints gridBagConstraints = new GridBagConstraints();
            gridBagConstraints.gridx = 0;
            gridBagConstraints.anchor = GridBagConstraints.WEST;
            gridBagConstraints.insets = new Insets(5, 10, 5, 5);
            gridBagConstraints.gridy = 2;
            NeighborsLabel = new JLabel();
            NeighborsLabel.setText("Threshold:");
            NeighborsLabel.setHorizontalAlignment(SwingConstants.CENTER);
            adjustmentsPanel.add(NeighborsLabel, gridBagConstraints);

            GridBagConstraints gbc_thresholdSlider = new GridBagConstraints();
            gbc_thresholdSlider.anchor = GridBagConstraints.WEST;
            gbc_thresholdSlider.insets = new Insets(0, 0, 5, 5);
            gbc_thresholdSlider.gridx = 1;
            gbc_thresholdSlider.gridy = 2;
            adjustmentsPanel.add(getThresholdSlider(), gbc_thresholdSlider);

            GridBagConstraints gbc_lblMaxDepth = new GridBagConstraints();
            gbc_lblMaxDepth.insets = new Insets(0, 0, 5, 5);
            gbc_lblMaxDepth.gridx = 2;
            gbc_lblMaxDepth.gridy = 2;
            adjustmentsPanel.add(getLblMaxDepth(), gbc_lblMaxDepth);

            GridBagConstraints gridBagConstraints4 = new GridBagConstraints();
            gridBagConstraints4.gridheight = 3;
            gridBagConstraints4.gridx = 6;
            gridBagConstraints4.insets = new Insets(5, 10, 0, 10);
            gridBagConstraints4.gridy = 0;
            adjustmentsPanel.add(getStartButton(), gridBagConstraints4);

            GridBagConstraints gbc_depthSlider = new GridBagConstraints();
            gbc_depthSlider.insets = new Insets(0, 0, 5, 5);
            gbc_depthSlider.gridx = 3;
            gbc_depthSlider.gridy = 2;
            adjustmentsPanel.add(getDepthSlider(), gbc_depthSlider);
        }
        return adjustmentsPanel;
    }

    private JPanel getNetworkPanel() {
        if (networkPanel == null) {
            networkPanel = new JPanel();
            networkPanel.setLayout(new BorderLayout());
            networkPanel.setBackground(new Color(250, 250, 250));
        }
        return networkPanel;
    }

    /**
     * This method initializes wordTextField
     *
     * @return javax.swing.JTextField
     */
    private JComponent getWordTextField() {
        if (wordTextField == null) {
            wordTextField = new JTextField();
            wordTextField.setSize(new Dimension(300, 20));
            wordTextField.setPreferredSize(new Dimension(300, 20));
        }
        return wordTextField;
    }

    /**
     * This method initializes startButton
     *
     * @return javax.swing.JButton
     */
    private JButton getStartButton() {
        if (startButton == null) {
            startButton = new JButton();
            startButton.setText("Start");
            startButton.setFont(new Font("Dialog", Font.PLAIN, 14));
            startButton.setPreferredSize(new Dimension(120, 25));
            startButton.addActionListener((java.awt.event.ActionEvent e) -> {
                generateNetwork();
            });
        }
        return startButton;
    }

    private JSlider getThresholdSlider() {
        if (thresholdSlider == null) {
            thresholdSlider = new JSlider(0, 10, 6);
            thresholdSlider.setMajorTickSpacing(5);
            thresholdSlider.setMinorTickSpacing(1);
            thresholdSlider.setBackground(Color.WHITE);
            thresholdSlider.setForeground(Color.BLACK);
            java.util.Hashtable<Integer, JLabel> labelTable = new java.util.Hashtable<>();
            labelTable.put(10, new JLabel("100%"));
            labelTable.put(5, new JLabel("50%"));
            labelTable.put(0, new JLabel("0"));
            thresholdSlider.setLabelTable(labelTable);
            thresholdSlider.setPaintTicks(true);
            thresholdSlider.setPaintLabels(true);
        }
        return thresholdSlider;
    }

    private JLabel getLblMaxDepth() {
        if (lblMaxDepth == null) {
            lblMaxDepth = new JLabel("Max depth:");
        }
        return lblMaxDepth;
    }

    private JSlider getDepthSlider() {
        if (depthSlider == null) {
            depthSlider = new JSlider(0, 10, 1);
            depthSlider.setForeground(Color.BLACK);
            depthSlider.setBackground(Color.WHITE);
            depthSlider.setMajorTickSpacing(5);
            depthSlider.setMinorTickSpacing(1);
            depthSlider.setPaintTicks(true);
            depthSlider.setPaintLabels(true);
            depthSlider.setMajorTickSpacing(5);
            java.util.Hashtable<Integer, JLabel> labelTable = new java.util.Hashtable<>();
            labelTable.put(10, new JLabel("10"));
            labelTable.put(5, new JLabel("5"));
            labelTable.put(0, new JLabel("0"));
            depthSlider.setLabelTable(labelTable);

            // depthSlider.addChangeListener(new ChangeListener() {
            // public void stateChanged(ChangeEvent e) {
            // generateNetwork();
            // }
            // });
        }
        return depthSlider;
    }

    private JPanel getPanel() {
        if (panel == null) {
            panel = new JPanel();
            panel.setBackground(Color.WHITE);
            panel.setLayout(new BorderLayout(0, 0));
            panel.add(getWordTextField());
        }
        return panel;
    }

    public static void main(String[] args) {
        BasicConfigurator.configure();

        ReaderBenchView.adjustToSystemGraphics();

        JFrame frame = new SemSpaceView(LDA.loadLDA("resources/in/HDP/grade12", Lang.en));
//		JFrame frame = new SemSpaceView(LDA.loadLDA("resources/config/LA/LDA/Letters", Lang.la));
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        frame.setVisible(true);

        // frame = new
        // SemSpaceView(LSA.loadLSA("resources/config/LA/LSA/Letters",
        // Lang.la));
        // frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }

}
