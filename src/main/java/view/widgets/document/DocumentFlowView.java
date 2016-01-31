package view.widgets.document;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

import data.Block;
import data.discourse.SemanticCohesion;
import data.document.Document;
import services.commons.Formatting;
import services.complexity.ComplexityIndices;
import services.complexity.flow.DocFlowCriteria;
import services.complexity.flow.DocumentFlow;
import view.models.document.DocumentFlowTable;
import view.models.document.DocumentTableModel;

public class DocumentFlowView extends JFrame {
	private static final long serialVersionUID = 7928942538031770509L;

	private Document doc;
	private DocumentFlow df;
	private JPanel contentPane;
	private DefaultTableModel modelContent;
	private JTable tableContent;
	private JComboBox<String> comboBoxSemDistance;
	private JComboBox<DocFlowCriteria> comboBoxCriteria;
	private JTextArea textAreaStats;

	/**
	 * Create the frame.
	 */
	public DocumentFlowView(Document doc) {
		setTitle("ReaderBench - Document Flow");
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setBounds(100, 100, 800, 600);
		this.doc = doc;
		contentPane = new JPanel();
		contentPane.setBackground(Color.WHITE);
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);

		JLabel lblSemanticDistance = new JLabel("Semantic distance:");

		comboBoxSemDistance = new JComboBox<String>();
		for (int i = 0; i < SemanticCohesion.NO_COHESION_DIMENSIONS; i++)
			comboBoxSemDistance.addItem(SemanticCohesion.getSemanticDistanceNames()[i]);

		JLabel lblCriteria = new JLabel("Criteria:");
		comboBoxCriteria = new JComboBox<DocFlowCriteria>();
		for (DocFlowCriteria crit : DocFlowCriteria.values())
			comboBoxCriteria.addItem(crit);

		comboBoxSemDistance.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				updateContents(comboBoxSemDistance.getSelectedIndex(),
						(DocFlowCriteria) comboBoxCriteria.getSelectedItem());
			}
		});

		comboBoxCriteria.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				updateContents(comboBoxSemDistance.getSelectedIndex(),
						(DocFlowCriteria) comboBoxCriteria.getSelectedItem());
			}
		});

		JScrollPane scrollPaneContent = new JScrollPane();
		scrollPaneContent.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

		JScrollPane scrollPaneStats = new JScrollPane();
		scrollPaneStats.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

		JButton btnViewDocumentFlow = new JButton("View Document Flow");
		btnViewDocumentFlow.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFrame view = new DocumentFlowGraphView(DocumentFlowView.this.doc, df);
				view.setVisible(true);
			}
		});
		GroupLayout gl_contentPane = new GroupLayout(contentPane);
		gl_contentPane.setHorizontalGroup(gl_contentPane.createParallelGroup(Alignment.TRAILING)
				.addGroup(gl_contentPane.createSequentialGroup().addContainerGap()
						.addGroup(gl_contentPane.createParallelGroup(Alignment.TRAILING)
								.addComponent(scrollPaneStats, Alignment.LEADING)
								.addComponent(scrollPaneContent, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 778,
										Short.MAX_VALUE)
						.addGroup(Alignment.LEADING,
								gl_contentPane.createSequentialGroup()
										.addGroup(gl_contentPane.createParallelGroup(Alignment.LEADING)
												.addComponent(lblSemanticDistance).addComponent(lblCriteria))
								.addPreferredGap(ComponentPlacement.RELATED)
								.addGroup(gl_contentPane.createParallelGroup(Alignment.LEADING)
										.addComponent(comboBoxCriteria, 0, 473, Short.MAX_VALUE)
										.addComponent(comboBoxSemDistance, 0, 473, Short.MAX_VALUE))
								.addPreferredGap(ComponentPlacement.RELATED).addComponent(btnViewDocumentFlow)))
				.addContainerGap()));
		gl_contentPane.setVerticalGroup(gl_contentPane.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_contentPane.createSequentialGroup()
						.addGroup(gl_contentPane.createParallelGroup(Alignment.LEADING)
								.addGroup(gl_contentPane.createSequentialGroup().addContainerGap()
										.addGroup(gl_contentPane.createParallelGroup(Alignment.BASELINE)
												.addComponent(lblSemanticDistance).addComponent(comboBoxSemDistance,
														GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
														GroupLayout.PREFERRED_SIZE))
								.addPreferredGap(ComponentPlacement.RELATED).addGroup(
										gl_contentPane.createParallelGroup(Alignment.BASELINE).addComponent(lblCriteria)
												.addComponent(comboBoxCriteria, GroupLayout.PREFERRED_SIZE,
														GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)))
						.addGroup(gl_contentPane.createSequentialGroup().addGap(21).addComponent(btnViewDocumentFlow)))
				.addPreferredGap(ComponentPlacement.RELATED)
				.addComponent(scrollPaneContent, GroupLayout.PREFERRED_SIZE, 318, GroupLayout.PREFERRED_SIZE)
				.addPreferredGap(ComponentPlacement.RELATED)
				.addComponent(scrollPaneStats, GroupLayout.DEFAULT_SIZE, 166, Short.MAX_VALUE).addContainerGap()));

		textAreaStats = new JTextArea();
		textAreaStats.setWrapStyleWord(true);
		textAreaStats.setLineWrap(true);
		scrollPaneStats.setViewportView(textAreaStats);

		modelContent = new DocumentTableModel();

		tableContent = new DocumentFlowTable(modelContent);
		tableContent.getColumnModel().getColumn(0).setMinWidth(50);
		tableContent.getColumnModel().getColumn(0).setMaxWidth(50);
		tableContent.getColumnModel().getColumn(0).setPreferredWidth(50);

		tableContent.setFillsViewportHeight(true);
		tableContent.setTableHeader(null);
		scrollPaneContent.setViewportView(tableContent);
		contentPane.setLayout(gl_contentPane);

		updateContents(comboBoxSemDistance.getSelectedIndex(), (DocFlowCriteria) comboBoxCriteria.getSelectedItem());
	}

	private void updateContents(int semanticDistIndex, DocFlowCriteria crit) {
		// clean table
		while (modelContent.getRowCount() > 0) {
			modelContent.removeRow(0);
		}

		df = new DocumentFlow(doc, semanticDistIndex, crit);

		for (Integer index : df.getOrderedParagraphs()) {
			Block refBlock = doc.getBlocks().get(index);
			Object[] row = { index,
					refBlock.getText() + "[" + Formatting.formatNumber(refBlock.getOverallScore()) + "]" };
			modelContent.addRow(row);
		}

		textAreaStats.setText("Edges in the cohesion graph:\n");
		for (int i = 0; i < df.getGraph().length - 1; i++) {
			for (int j = i + 1; j < df.getGraph().length; j++) {
				if (df.getGraph()[i][j] > 0) {
					textAreaStats
							.append("\t" + i + ">>" + j + ":\t" + Formatting.formatNumber(df.getGraph()[i][j]) + "\n");
				}
			}
		}
		textAreaStats.append("\nCohesion flow specific indices:\n");
		textAreaStats.append(Formatting.formatNumber(df.getAbsolutePositionAccuracy()) + "\t"
				+ ComplexityIndices.TEXTUAL_COMPLEXITY_INDEX_DESCRIPTIONS[ComplexityIndices.DOC_FLOW_ABSOLUTE_POSITION_ACCURACY
						+ semanticDistIndex + SemanticCohesion.NO_COHESION_DIMENSIONS
								* (crit.equals(DocFlowCriteria.ABOVE_MEAN_PLUS_STDEV) ? 0 : 1)]
				+ "\n");
		textAreaStats.append(Formatting.formatNumber(df.getAbsoluteDistanceAccuracy()) + "\t"
				+ ComplexityIndices.TEXTUAL_COMPLEXITY_INDEX_DESCRIPTIONS[ComplexityIndices.DOC_FLOW_ABSOLUTE_DISTANCE_ACCURACY
						+ semanticDistIndex + SemanticCohesion.NO_COHESION_DIMENSIONS
								* (crit.equals(DocFlowCriteria.ABOVE_MEAN_PLUS_STDEV) ? 0 : 1)]
				+ "\n");
		textAreaStats.append(Formatting.formatNumber(df.getAdjacencyAccuracy()) + "\t"
				+ ComplexityIndices.TEXTUAL_COMPLEXITY_INDEX_DESCRIPTIONS[ComplexityIndices.DOC_FLOW_ADJACENCY_ACCURACY
						+ semanticDistIndex + SemanticCohesion.NO_COHESION_DIMENSIONS
								* (crit.equals(DocFlowCriteria.ABOVE_MEAN_PLUS_STDEV) ? 0 : 1)]
				+ "\n");
		textAreaStats.append(Formatting.formatNumber(df.getSpearmanCorrelation()) + "\t"
				+ ComplexityIndices.TEXTUAL_COMPLEXITY_INDEX_DESCRIPTIONS[ComplexityIndices.DOC_FLOW_SPEARMAN_CORRELATION
						+ semanticDistIndex + SemanticCohesion.NO_COHESION_DIMENSIONS
								* (crit.equals(DocFlowCriteria.ABOVE_MEAN_PLUS_STDEV) ? 0 : 1)]
				+ "\n");
		textAreaStats.append(Formatting.formatNumber(df.getMaxOrderedSequence()) + "\t"
				+ ComplexityIndices.TEXTUAL_COMPLEXITY_INDEX_DESCRIPTIONS[ComplexityIndices.DOC_FLOW_MAX_ORDERED_SEQUENCE
						+ semanticDistIndex + SemanticCohesion.NO_COHESION_DIMENSIONS
								* (crit.equals(DocFlowCriteria.ABOVE_MEAN_PLUS_STDEV) ? 0 : 1)]
				+ "\n");
		textAreaStats.append(Formatting.formatNumber(df.getAverageFlowCohesion()) + "\t"
				+ ComplexityIndices.TEXTUAL_COMPLEXITY_INDEX_DESCRIPTIONS[ComplexityIndices.DOC_FLOW_AVERAGE_COHESION
						+ semanticDistIndex + SemanticCohesion.NO_COHESION_DIMENSIONS
								* (crit.equals(DocFlowCriteria.ABOVE_MEAN_PLUS_STDEV) ? 0 : 1)]);
	}
}
