package view.widgets.chat;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Vector;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.table.DefaultTableModel;

import data.discourse.SemanticChain;
import services.commons.Formatting;
import services.discourse.dialogism.DialogismMeasures;

public class VoiceSynergyView extends JFrame {

	private static final long serialVersionUID = 2897644814459831682L;

	private JPanel contentPane;
	private JPanel panelMutualInformation;
	private JPanel panelCoOccurrence;
	private JPanel panelMovingAverage;
	private JPanel panelSentimentEvolution;
	private DefaultTableModel tableModelCorrelationMatrix;
	private JTable tableCorrelationMatrix;
	private JComboBox<String> comboBox;
	private List<SemanticChain> displayedVoices;

	/**
	 * Create the frame.
	 */
	public VoiceSynergyView(List<SemanticChain> chains) {
		setTitle("ReaderBench - Voice Synergy and Correlation Visualization");
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		this.displayedVoices = chains;
		setBounds(100, 100, 650, 600);
		contentPane = new JPanel();
		contentPane.setBackground(Color.WHITE);
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);

		JLabel lblCorrelationType = new JLabel("Correlation type:");

		comboBox = new JComboBox<String>();
		comboBox.addItem("Utterance occurence correlation");
		comboBox.addItem("Utterance moving average correlation");
		comboBox.addItem("Mutual information");
		comboBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (comboBox.getSelectedIndex() >= 0) {
					// display different matrixes
					double[][] matrix = null;
					switch (comboBox.getSelectedIndex()) {
					case 0:
						matrix = DialogismMeasures.getBlockCorrelationMatrix(VoiceSynergyView.this.displayedVoices);
						break;
					case 1:
						matrix = DialogismMeasures
								.getMovingAverageCorrelationMatrix(VoiceSynergyView.this.displayedVoices);
						break;
					case 2:
						matrix = DialogismMeasures
								.getBlockMutualInformationMatrix(VoiceSynergyView.this.displayedVoices);
						break;
					}
					updateContents(matrix);
				}
			}
		});

		JScrollPane scrollPaneCorrelationMatrix = new JScrollPane();

		JLabel lblCorrelationMatrix = new JLabel("Correlation Matrix");
		lblCorrelationMatrix.setFont(new Font("Lucida Grande", Font.BOLD, 13));

		JLabel lblVoiceConvergence = new JLabel("Voice Synergy");
		lblVoiceConvergence.setFont(new Font("Lucida Grande", Font.BOLD, 13));

		panelMutualInformation = new JPanel();
		panelMutualInformation.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		panelMutualInformation.setBackground(Color.WHITE);
		panelMutualInformation.setLayout(new BorderLayout());

		panelCoOccurrence = new JPanel();
		panelCoOccurrence.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		panelCoOccurrence.setBackground(Color.WHITE);
		panelCoOccurrence.setLayout(new BorderLayout());

		panelMovingAverage = new JPanel();
		panelMovingAverage.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		panelMovingAverage.setBackground(Color.WHITE);
		panelMovingAverage.setLayout(new BorderLayout());

		panelSentimentEvolution = new JPanel();
		panelSentimentEvolution.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		panelSentimentEvolution.setBackground(Color.WHITE);
		panelSentimentEvolution.setLayout(new BorderLayout());

		GroupLayout gl_contentPane = new GroupLayout(contentPane);
		gl_contentPane
				.setHorizontalGroup(
						gl_contentPane.createParallelGroup(Alignment.TRAILING)
								.addGroup(gl_contentPane.createSequentialGroup().addContainerGap()
										.addGroup(gl_contentPane.createParallelGroup(Alignment.LEADING)
												.addComponent(panelMutualInformation, GroupLayout.DEFAULT_SIZE, 628,
														Short.MAX_VALUE)
										.addComponent(panelCoOccurrence, GroupLayout.DEFAULT_SIZE, 628, Short.MAX_VALUE)
										.addComponent(panelSentimentEvolution, GroupLayout.DEFAULT_SIZE, 628,
												Short.MAX_VALUE)
						.addComponent(scrollPaneCorrelationMatrix, GroupLayout.DEFAULT_SIZE, 628, Short.MAX_VALUE)
						.addGroup(gl_contentPane.createSequentialGroup().addComponent(lblCorrelationType)
								.addPreferredGap(ComponentPlacement.RELATED)
								.addComponent(comboBox, GroupLayout.PREFERRED_SIZE, 290, GroupLayout.PREFERRED_SIZE))
						.addComponent(lblCorrelationMatrix)
						.addComponent(panelMovingAverage, GroupLayout.DEFAULT_SIZE, 628, Short.MAX_VALUE)
						.addComponent(lblVoiceConvergence)).addContainerGap()));
		gl_contentPane.setVerticalGroup(gl_contentPane.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_contentPane.createSequentialGroup().addContainerGap()
						.addGroup(gl_contentPane.createParallelGroup(Alignment.BASELINE)
								.addComponent(lblCorrelationType).addComponent(comboBox, GroupLayout.PREFERRED_SIZE,
										GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
				.addPreferredGap(ComponentPlacement.RELATED).addComponent(lblCorrelationMatrix)
				.addPreferredGap(ComponentPlacement.RELATED)
				.addComponent(scrollPaneCorrelationMatrix, GroupLayout.PREFERRED_SIZE, 110, GroupLayout.PREFERRED_SIZE)
				.addPreferredGap(ComponentPlacement.RELATED).addComponent(lblVoiceConvergence)
				.addPreferredGap(ComponentPlacement.RELATED)
				.addComponent(panelCoOccurrence, GroupLayout.DEFAULT_SIZE, 120, Short.MAX_VALUE)
				.addPreferredGap(ComponentPlacement.RELATED)
				.addComponent(panelMovingAverage, GroupLayout.DEFAULT_SIZE, 111, Short.MAX_VALUE)
				.addPreferredGap(ComponentPlacement.RELATED)
				.addComponent(panelMutualInformation, GroupLayout.DEFAULT_SIZE, 120, Short.MAX_VALUE)
				.addPreferredGap(ComponentPlacement.RELATED)
				.addComponent(panelSentimentEvolution, GroupLayout.DEFAULT_SIZE, 120, Short.MAX_VALUE)
				.addContainerGap())

		);

		String[] columnNames = new String[displayedVoices.size() + 1];
		columnNames[0] = "";
		for (int i = 0; i < displayedVoices.size(); i++) {
			columnNames[i + 1] = i + "";
		}

		tableModelCorrelationMatrix = new DefaultTableModel(new Object[][] {}, columnNames) {
			private static final long serialVersionUID = -5454968717710196231L;

			@Override
			public boolean isCellEditable(int rowIndex, int columnIndex) {
				return false;
			}
		};

		tableCorrelationMatrix = new JTable(tableModelCorrelationMatrix);
		tableCorrelationMatrix.getColumnModel().getColumn(0).setMinWidth(250);
		tableCorrelationMatrix.getColumnModel().getColumn(0).setMaxWidth(250);
		tableCorrelationMatrix.getColumnModel().getColumn(0).setPreferredWidth(250);
		tableCorrelationMatrix.setFillsViewportHeight(true);

		scrollPaneCorrelationMatrix.setViewportView(tableCorrelationMatrix);

		contentPane.setLayout(gl_contentPane);

		double[][] displayedMatrix = DialogismMeasures.getBlockCorrelationMatrix(displayedVoices);
		updateContents(displayedMatrix);

		double[] evolution1 = DialogismMeasures.getCoOccurrenceBlockEvolution(displayedVoices);
		double[] evolution2 = DialogismMeasures.getCumulativeBlockMuvingAverageEvolution(displayedVoices);
		double[] evolution3 = DialogismMeasures.getAverageBlockMutualInformationEvolution(displayedVoices);

		Double[][] values1 = new Double[1][evolution1.length];
		Double[][] values2 = new Double[1][evolution2.length];
		Double[][] values3 = new Double[1][evolution3.length];
		double[] columns = new double[evolution1.length];

		String[] name1 = { "Co-occurrence" };
		String[] name2 = { "Cumulative moving average" };
		String[] name3 = { "Average Pointwise Mutual Information" };
		for (int i = 0; i < evolution1.length; i++) {
			values1[0][i] = evolution1[i];
			values2[0][i] = evolution2[i];
			values3[0][i] = evolution3[i];
			columns[i] = i;
		}

		EvolutionGraph evolutionGraph1 = new EvolutionGraph("", "Utterance/Paragraph", false, name1, values1, columns,
				Color.DARK_GRAY);
		EvolutionGraph evolutionGraph2 = new EvolutionGraph("", "Utterance/Paragraph", false, name2, values2, columns,
				Color.DARK_GRAY);
		EvolutionGraph evolutionGraph3 = new EvolutionGraph("", "Utterance/Paragraph", false, name3, values3, columns,
				Color.DARK_GRAY);

		panelCoOccurrence.add(evolutionGraph1.evolution());
		panelMovingAverage.add(evolutionGraph2.evolution());
		panelMutualInformation.add(evolutionGraph3.evolution());
	}

	private void updateContents(double[][] displayedContents) {
		while (tableModelCorrelationMatrix.getRowCount() > 0) {
			tableModelCorrelationMatrix.removeRow(0);
		}

		for (int i = 0; i < displayedVoices.size(); i++) {
			SemanticChain voice = displayedVoices.get(i);
			Vector<Object> dataRow = new Vector<Object>();

			dataRow.add(i + " - " + voice.toString());
			int j = 0;
			for (; j < i; j++) {
				dataRow.add(Formatting.formatNumber(displayedContents[i][j]));
			}
			for (; j < displayedVoices.size(); j++) {
				dataRow.add("");
			}
			tableModelCorrelationMatrix.addRow(dataRow);
		}
	}
}
