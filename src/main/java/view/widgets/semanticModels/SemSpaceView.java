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
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.gephi.data.attributes.api.AttributeColumn;
import org.gephi.data.attributes.api.AttributeController;
import org.gephi.data.attributes.api.AttributeModel;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
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
import org.gephi.ranking.plugin.transformer.AbstractColorTransformer;
import org.gephi.ranking.plugin.transformer.AbstractSizeTransformer;
import org.gephi.statistics.plugin.GraphDistance;
import org.openide.util.Lookup;

import edu.cmu.lti.jawjaw.pobj.Lang;
import processing.core.PApplet;
import services.semanticModels.GenerateSpace;
import services.semanticModels.ISemanticModel;
import services.semanticModels.LDA.LDA;

public class SemSpaceView extends JFrame {

	private static final long serialVersionUID = 1L;
	static Logger logger = Logger.getLogger(SemSpaceView.class);

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

		ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
		pc.newProject();

		// Get models
		GraphModel graphModel = Lookup.getDefault().lookup(GraphController.class).getModel();
		AttributeModel attributeModel = Lookup.getDefault().lookup(AttributeController.class).getModel();
		UndirectedGraph graph = graphModel.getUndirectedGraph();

		if (wordTextField.getText().length() == 0) {
			JOptionPane.showMessageDialog(viewSplitPane, "Please enter a word!", "Error", JOptionPane.WARNING_MESSAGE);
			this.pack();
			return;
		} else {
			lsaProc.buildGraph(graph, graphModel, wordTextField.getText(), threshold, depth);
			logger.info(
					wordTextField.getText() + " - nodes: " + graph.getNodeCount() + " edges: " + graph.getEdgeCount());
			// Iterate over nodes
			// for (Node n : graph.getNodes()) {
			// Node[] neighbors = graph.getNeighbors(n).toArray();
			// logger.info(n.getNodeData().getLabel() + " has " +
			// neighbors.length
			// + " neighbors");
			// }
		}

		// Rank color by Degree
		RankingController rankingController = Lookup.getDefault().lookup(RankingController.class);
		Ranking<?> degreeRanking = rankingController.getModel().getRanking(Ranking.NODE_ELEMENT,
				Ranking.DEGREE_RANKING);
		AbstractColorTransformer<?> colorTransformer = (AbstractColorTransformer<?>) rankingController.getModel()
				.getTransformer(Ranking.NODE_ELEMENT, Transformer.RENDERABLE_COLOR);

		colorTransformer.setColors(new Color[] { new Color(0xD6D6D6), new Color(0x858585) });
		rankingController.transform(degreeRanking, colorTransformer);

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
		networkPanel.add(applet, BorderLayout.CENTER);

		// Export
		ExportController ec = Lookup.getDefault().lookup(ExportController.class);
		try {
			ec.exportFile(new File("out/graph.pdf"));
		} catch (IOException ex) {
			ex.printStackTrace();
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
			gbl_adjustmentsPanel.rowWeights = new double[] { 1.0, 0.0, 0.0 };
			gbl_adjustmentsPanel.columnWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0 };

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
			startButton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					generateNetwork();
				}
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
			java.util.Hashtable<Integer, JLabel> labelTable = new java.util.Hashtable<Integer, JLabel>();
			labelTable.put(new Integer(10), new JLabel("100%"));
			labelTable.put(new Integer(5), new JLabel("50%"));
			labelTable.put(new Integer(0), new JLabel("0"));
			thresholdSlider.setLabelTable(labelTable);
			thresholdSlider.setPaintTicks(true);
			thresholdSlider.setPaintLabels(true);
			// thresholdSlider.addChangeListener(new ChangeListener() {
			// public void stateChanged(ChangeEvent e) {
			// generateNetwork();
			// }
			// });
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
			java.util.Hashtable<Integer, JLabel> labelTable = new java.util.Hashtable<Integer, JLabel>();
			labelTable.put(new Integer(10), new JLabel("10"));
			labelTable.put(new Integer(5), new JLabel("5"));
			labelTable.put(new Integer(0), new JLabel("0"));
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

		adjustToSystemGraphics();

		JFrame frame = new SemSpaceView(LDA.loadLDA("in/HDP/grade12", Lang.eng));
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		frame.setVisible(true);
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
} // @jve:decl-index=0:visual-constraint="10,10"
