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
package runtime.view;

import java.awt.Color;
import java.io.File;
import java.io.IOException;

import org.gephi.appearance.api.AppearanceController;
import org.gephi.appearance.api.AppearanceModel;
import org.gephi.appearance.api.Function;
import org.gephi.appearance.plugin.RankingElementColorTransformer;
import org.gephi.appearance.plugin.RankingLabelSizeTransformer;
import org.gephi.appearance.plugin.RankingNodeSizeTransformer;
import org.gephi.graph.api.Column;
import org.gephi.graph.api.DirectedGraph;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Node;
import org.gephi.io.exporter.api.ExportController;
import org.gephi.layout.plugin.forceAtlas2.ForceAtlas2;
import org.gephi.preview.api.PreviewController;
import org.gephi.preview.api.PreviewModel;
import org.gephi.preview.api.PreviewProperty;
import org.gephi.preview.types.EdgeColor;
import org.gephi.project.api.ProjectController;
import org.gephi.project.api.Workspace;
import org.gephi.statistics.plugin.GraphDistance;
import org.openide.util.Lookup;

public class GephiTest {
	public void script() {
		// Init a project - and therefore a workspace
		ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
		pc.newProject();
		Workspace workspace = pc.getCurrentWorkspace();

		// Get a graph model - it exists because we have a workspace
		GraphModel graphModel = Lookup.getDefault().lookup(GraphController.class).getGraphModel(workspace);
		DirectedGraph graph = graphModel.getDirectedGraph();
		PreviewModel model = Lookup.getDefault().lookup(PreviewController.class).getModel();
		AppearanceController appearanceController = Lookup.getDefault().lookup(AppearanceController.class);
		AppearanceModel appearanceModel = appearanceController.getModel();

		// Create three nodes
		Node[] n = new Node[100];
		for (int i = 0; i < 100; i++) {
			n[i] = graphModel.factory().newNode(i + "");
			n[i].setLabel("Node " + i);
			n[i].setX((float) ((0.01 + Math.random()) * 1000) - 500);
			n[i].setY((float) ((0.01 + Math.random()) * 1000) - 500);
			graph.addNode(n[i]);
		}

		for (int i = 0; i < 1000; i++) {
			int j = (int) (Math.random() * 100);
			int k = (int) (Math.random() * 100);
			Edge e = graphModel.factory().newEdge(n[j], n[k], 0, 1.0, true);
			graph.addEdge(e);
		}

		Edge e = graphModel.factory().newEdge(n[99], n[0], 0, 1.0, true);
		graph.addEdge(e);

		// Count nodes and edges
		System.out.println("Nodes: " + graph.getNodeCount() + " Edges: " + graph.getEdgeCount());

		// // Run YifanHuLayout for 100 passes - The layout always takes the
		// // current visible view
		// YifanHuLayout layout = new YifanHuLayout(null, new
		// StepDisplacement(1f));
		// layout.setGraphModel(graphModel);
		// layout.resetPropertiesValues();
		// layout.setOptimalDistance(200f);
		//
		// layout.initAlgo();
		// for (int i = 0; i < 100 && layout.canAlgo(); i++) {
		// layout.goAlgo();
		// }
		// layout.endAlgo();

		// run ForceAtlas 2 layout
		ForceAtlas2 layout = new ForceAtlas2(null);
		layout.setGraphModel(graphModel);
		layout.resetPropertiesValues();

		layout.setOutboundAttractionDistribution(false);
		layout.setEdgeWeightInfluence(1.5d);
		layout.setGravity(10d);
		layout.setJitterTolerance(.02);
		layout.setScalingRatio(15.0);
		layout.initAlgo();

		// Get Centrality
		GraphDistance distance = new GraphDistance();
		distance.setDirected(true);
		distance.execute(graphModel);

		// Rank color by Degree
		Function degreeRanking = appearanceModel.getNodeFunction(graph, AppearanceModel.GraphFunction.NODE_DEGREE,
				RankingElementColorTransformer.class);
		RankingElementColorTransformer degreeTransformer = (RankingElementColorTransformer) degreeRanking
				.getTransformer();
		degreeTransformer.setColors(new Color[] { new Color(0xFEF0D9), new Color(0xB30000) });
		degreeTransformer.setColorPositions(new float[] { 0f, 1f });
		appearanceController.transform(degreeRanking);

		// Rank size by centrality
		Column centralityColumn = graphModel.getNodeTable().getColumn(GraphDistance.BETWEENNESS);
		Function centralityRanking = appearanceModel.getNodeFunction(graph, centralityColumn,
				RankingNodeSizeTransformer.class);
		RankingNodeSizeTransformer centralityTransformer = (RankingNodeSizeTransformer) centralityRanking
				.getTransformer();
		centralityTransformer.setMinSize(3);
		centralityTransformer.setMaxSize(10);
		appearanceController.transform(centralityRanking);

		// Rank label size - set a multiplier size
		Function centralityRanking2 = appearanceModel.getNodeFunction(graph, centralityColumn,
				RankingLabelSizeTransformer.class);
		RankingLabelSizeTransformer labelSizeTransformer = (RankingLabelSizeTransformer) centralityRanking2
				.getTransformer();
		labelSizeTransformer.setMinSize(1);
		labelSizeTransformer.setMaxSize(3);
		appearanceController.transform(centralityRanking2);

		// Preview
		model.getProperties().putValue(PreviewProperty.SHOW_NODE_LABELS, Boolean.TRUE);
		model.getProperties().putValue(PreviewProperty.EDGE_COLOR, new EdgeColor(Color.GRAY));
		model.getProperties().putValue(PreviewProperty.EDGE_THICKNESS, new Float(0.1f));
		model.getProperties().putValue(PreviewProperty.NODE_LABEL_FONT,
				model.getProperties().getFontValue(PreviewProperty.NODE_LABEL_FONT).deriveFont(8));

		// Export
		ExportController ec = Lookup.getDefault().lookup(ExportController.class);
		try {
			ec.exportFile(new File("out/test.pdf"));
		} catch (IOException ex) {
			ex.printStackTrace();
			return;
		}
	}

	public static void main(String[] args) {
		GephiTest t = new GephiTest();
		t.script();
	}
}
