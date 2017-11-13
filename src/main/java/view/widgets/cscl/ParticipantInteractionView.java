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
package view.widgets.cscl;

import org.gephi.appearance.api.AppearanceController;
import org.gephi.appearance.api.AppearanceModel;
import org.gephi.appearance.api.Function;
import org.gephi.appearance.plugin.RankingNodeSizeTransformer;
import org.gephi.graph.api.Column;
import org.gephi.graph.api.DirectedGraph;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
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

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

import javax.swing.*;
import javax.swing.GroupLayout.Alignment;

import data.cscl.Participant;
import services.discourse.CSCL.ParticipantEvaluation;
import utils.LocalizationUtils;
import view.models.PreviewSketch;

public class ParticipantInteractionView extends JFrame {

    private static final long serialVersionUID = 2571577554857108582L;

    static Logger logger = Logger.getLogger("");

    private List<Participant> participants;
    private double[][] participantContributions;
    private JPanel panelGraph;
    private String path;

    public ParticipantInteractionView(String path, List<Participant> participants,
            double[][] participantContributions, boolean displayEdgeLabels, boolean needsAnonymization) {
        super();
        super.setTitle("ReaderBench - " + LocalizationUtils.getTitle(this.getClass()));
        super.setBackground(Color.WHITE);
        this.participants = participants;
        this.path = path;
        this.participantContributions = participantContributions;
        super.getContentPane().setBackground(Color.WHITE);

        panelGraph = new JPanel();
        panelGraph.setBackground(Color.WHITE);
        panelGraph.setLayout(new BorderLayout(0, 0));
        GroupLayout groupLayout = new GroupLayout(getContentPane());
        groupLayout.setHorizontalGroup(groupLayout.createParallelGroup(Alignment.LEADING).addComponent(panelGraph,
                Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, 784, Short.MAX_VALUE));
        groupLayout.setVerticalGroup(groupLayout.createParallelGroup(Alignment.LEADING).addComponent(panelGraph,
                Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, 562, Short.MAX_VALUE));
        getContentPane().setLayout(groupLayout);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        setBounds(50, 50, 800, 600);

        generateGraph(displayEdgeLabels, needsAnonymization);
    }

    private void generateGraph(boolean displayEdgeLabels, boolean isAnonymized) {
        logger.info("Generating participant interaction view");
        ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
        pc.newProject();

        // get models
        GraphModel graphModel = Lookup.getDefault().lookup(GraphController.class).getGraphModel();
        DirectedGraph graph = graphModel.getDirectedGraph();
        AppearanceController appearanceController = Lookup.getDefault().lookup(AppearanceController.class);
        AppearanceModel appearanceModel = appearanceController.getModel();

        ParticipantEvaluation.buildParticipantGraph(graph, graphModel, this.participants,
                this.participantContributions, displayEdgeLabels, isAnonymized);

        // Run YifanHuLayout for 100 passes
        YifanHuLayout layout = new YifanHuLayout(null, new StepDisplacement(1f));
        layout.setGraphModel(graphModel);
        layout.resetPropertiesValues();
        // determine max weight
        float max = 0;
        for (Edge e : graph.getEdges()) {
            max = (float) Math.max(max, e.getWeight());
        }
        for (Edge e : graph.getEdges()) {
            e.setWeight(e.getWeight() / max);
        }
        layout.setOptimalDistance(max * 10);

        layout.initAlgo();

        GraphDistance distance = new GraphDistance();
        distance.setDirected(false);
        distance.execute(graphModel);

        Column centralityColumn = graphModel.getNodeTable().getColumn(GraphDistance.BETWEENNESS);
        Function centralityRanking = appearanceModel.getNodeFunction(graph, centralityColumn,
                RankingNodeSizeTransformer.class);
        RankingNodeSizeTransformer centralityTransformer = (RankingNodeSizeTransformer) centralityRanking
                .getTransformer();
        centralityTransformer.setMinSize(3);
        centralityTransformer.setMaxSize(10);
        appearanceController.transform(centralityRanking);

        for (int i = 0; i < 100 && layout.canAlgo(); i++) {
            layout.goAlgo();
        }
        layout.endAlgo();

        // Preview configuration
        PreviewController previewController = Lookup.getDefault().lookup(PreviewController.class);
        PreviewModel previewModel = previewController.getModel();
        previewModel.getProperties().putValue(PreviewProperty.SHOW_NODE_LABELS, Boolean.TRUE);
        previewModel.getProperties().putValue(PreviewProperty.NODE_LABEL_COLOR,
                new DependantOriginalColor(DependantOriginalColor.Mode.ORIGINAL));
//        previewModel.getProperties().putValue(PreviewProperty.EDGE_LABEL_COLOR,
//                new DependantOriginalColor(DependantOriginalColor.Mode.ORIGINAL));
        previewModel.getProperties().putValue(PreviewProperty.EDGE_CURVED, Boolean.TRUE);
        previewModel.getProperties().putValue(PreviewProperty.EDGE_RADIUS, 10f);
        previewModel.getProperties().putValue(PreviewProperty.SHOW_EDGE_LABELS, Boolean.FALSE);
        previewModel.getProperties().putValue(PreviewProperty.NODE_LABEL_PROPORTIONAL_SIZE, Boolean.FALSE);

        // New Processing target, get the PApplet
        G2DTarget target = (G2DTarget) previewController.getRenderTarget(RenderTarget.G2D_TARGET);
        PreviewSketch previewSketch = new PreviewSketch(target);
        previewController.refreshPreview();
        previewSketch.resetZoom();

        panelGraph.add(previewSketch, BorderLayout.CENTER);
        panelGraph.validate();
        panelGraph.repaint();

        // Export
        ExportController ec = Lookup.getDefault().lookup(ExportController.class);
        try {
            ec.exportFile(new File(path));
        } catch (IOException ex) {
            ex.printStackTrace();
            return;
        }
        this.pack();
    }
}
