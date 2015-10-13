package view.widgets.selfexplanation;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.List;
import java.util.Vector;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

import org.apache.log4j.Logger;

import services.commons.Formatting;
import services.complexity.ComplexityIndices;
import services.readingStrategies.ComprehensionPrediction;
import view.widgets.complexity.ComplexityIndicesView;
import DAO.document.Metacognition;

public class ComprehensionPredictionView extends JFrame {

	private static final long serialVersionUID = -4518616508590444786L;
	static Logger logger = Logger.getLogger(ComprehensionPredictionView.class);

	private List<? extends Metacognition> selfExplanations;
	private String path;
	private ComprehensionPrediction svm;
	private JPanel contentPane;
	private JLabel lblSelectiveMeasurements;
	private JCheckBox chckbxIndividual;
	private JCheckBox chckbxAllSelected;
	private JLabel lblResults;
	private JLabel lblKCrossValidation;
	private JTextField textFieldCrossValidation;
	private JButton btnSelectComplexityIndices;
	private JButton btnPerformMeasurements;
	private JScrollPane scrollPane;
	private DefaultTableModel tableModel;
	private JTable table;
	private JLabel lblComments;

	private class Task extends SwingWorker<Void, Void> {
		private int Kfolds;

		public Task(int kfolds) {
			super();
			this.Kfolds = kfolds;
		}

		public void Test(int[] selectedMeasurements, String testName) {
			// output results into corresponding file and into table
			try {
				// Open the file
				FileWriter fstream = new FileWriter(path, true);
				BufferedWriter file = new BufferedWriter(fstream);
				Vector<Object> dataRow = new Vector<Object>();

				file.write("\n" + testName + ",");
				dataRow.add(testName);
				double[] agreements = svm.runSVM(selectedMeasurements, Kfolds);

				// determine average values for EA
				for (int i = 0; i < agreements.length; i++) {
					file.write(Formatting.formatNumber(agreements[i]) + ",");
					dataRow.add(Formatting.formatNumber(agreements[i]));
				}
				tableModel.addRow(dataRow);
				file.close();
			} catch (Exception e) {// Catch exception if any
				System.err.println("Error: " + e.getMessage());
			}
		}

		public void RunTests() {
			// run measurements, if selected, for each selected factor
			if (chckbxIndividual.isSelected()) {
				for (int index : ComplexityIndicesView
						.getSelectedMeasurements()) {
					Test(new int[] { index },
							ComplexityIndices.TEXTUAL_COMPLEXITY_INDEX_NAMES[index]);
				}
				for (int index : ReadingStrategiesIndicesView
						.getSelectedMeasurements()) {
					Test(new int[] { ComplexityIndices.NO_COMPLEXITY_INDICES + index },
							ReadingStrategiesIndicesView.READING_STRATEGY_INDEX_NAMES[index]);
				}
			}

			// run measurements, if selected, by including all selected textual
			// complexity factors
			if (chckbxAllSelected.isSelected()) {
				int no_indices = ComplexityIndicesView
						.getSelectedMeasurements().length
						+ ReadingStrategiesIndicesView
								.getSelectedMeasurements().length;
				int[] selectedIndices = new int[no_indices];
				int index = 0;
				for (int i : ComplexityIndicesView.getSelectedMeasurements())
					selectedIndices[index++] = i;
				for (int i : ReadingStrategiesIndicesView
						.getSelectedMeasurements())
					selectedIndices[index++] = ComplexityIndices.NO_COMPLEXITY_INDICES + i;
				Test(ComplexityIndicesView.getSelectedMeasurements(),
						"All Selected Factors Combined");
			}
		}

		@Override
		public Void doInBackground() {
			try {

				try {
					// Empty the output file, write header

					// saving all indices
					FileWriter fstream = new FileWriter(path);
					BufferedWriter file = new BufferedWriter(fstream);

					file.write("Filename,Comprehension score,Comprehension class");
					for (String s : ComplexityIndices.TEXTUAL_COMPLEXITY_INDEX_NAMES)
						file.write("," + s);
					for (String s : ReadingStrategiesIndicesView.READING_STRATEGY_INDEX_NAMES)
						file.write("," + s);
					for (Metacognition v : selfExplanations) {
						String authors = "";
						for (String author : v.getAuthors())
							authors += author + "; ";
						authors = authors.substring(0, authors.length() - 2)
								+ " (" + v.getReferredDoc().getTitleText()
								+ ")";
						file.write("\n" + authors + ","
								+ v.getAnnotatedComprehensionScore() + ","
								+ v.getComprehensionClass());
						for (double value : v.getComprehensionIndices())
							file.write("," + value);
					}

					logger.info("Started to train SVM models on "
							+ ComprehensionPrediction.NO_COMPREHENSION_CLASSES
							+ " classes");
					// Create file

					file.write("\n\nFactor,");

					for (int i = 0; i < ComprehensionPrediction.NO_COMPREHENSION_CLASSES; i++)
						file.write("C" + (i + 1) + "(%) EA,");
					file.write("Avg. EA\n");

					file.close();
				} catch (Exception e) {// Catch exception if any
					System.err.println("Error: " + e.getMessage());
				}

				// build the table
				String[] names = new String[ComprehensionPrediction.NO_COMPREHENSION_CLASSES + 2];
				names[0] = "<html>Factor<br/><br/></html>";
				names[ComprehensionPrediction.NO_COMPREHENSION_CLASSES + 1] = "<html>Avg.<br/>EA</html>";
				for (int i = 0; i < ComprehensionPrediction.NO_COMPREHENSION_CLASSES; i++) {
					names[i + 1] = "<html>C" + (i + 1) + "<br/>EA</html>";
				}

				tableModel = new DefaultTableModel(new Object[][] {}, names) {
					private static final long serialVersionUID = 8537729224173332503L;

					@Override
					public boolean isCellEditable(int rowIndex, int columnIndex) {
						return false;
					}

					@Override
					public Class<?> getColumnClass(int columnIndex) {
						if (columnIndex == 0)
							return String.class;
						return Double.class;
					}
				};

				table = new JTable(tableModel);
				table.setFillsViewportHeight(true);
				table.setAutoCreateRowSorter(true);

				for (int i = 1; i < ComprehensionPrediction.NO_COMPREHENSION_CLASSES + 2; i++) {
					// set width for ID and selected
					table.getColumnModel().getColumn(i).setMinWidth(50);
					table.getColumnModel().getColumn(i).setMaxWidth(50);
					table.getColumnModel().getColumn(i).setPreferredWidth(50);
				}

				scrollPane.setViewportView(table);

				RunTests();
			} catch (Exception e) {
				e.printStackTrace();
			}
			return null;
		}

		/*
		 * Executed in event dispatching thread
		 */
		@Override
		public void done() {
			Toolkit.getDefaultToolkit().beep();
			btnPerformMeasurements.setEnabled(true);
			setCursor(null); // turn off the wait cursor
		}
	}

	public ComprehensionPredictionView(
			List<? extends Metacognition> loadedSelfExplanations) {
		if (loadedSelfExplanations == null
				|| loadedSelfExplanations.size() == 0) {
			return;
		}
		setTitle("ReaderBench - Comprehension Prediction");
		this.selfExplanations = loadedSelfExplanations;
		this.svm = new ComprehensionPrediction(selfExplanations);
		this.path = "out/comprehension_prediction_"
				+ System.currentTimeMillis() + ".csv";
		setBackground(Color.WHITE);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setBounds(50, 50, 700, 600);
		contentPane = new JPanel();
		contentPane.setBackground(Color.WHITE);
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);

		lblSelectiveMeasurements = new JLabel("Selective measurements");
		lblSelectiveMeasurements.setFont(new Font("SansSerif", Font.BOLD, 12));

		chckbxIndividual = new JCheckBox(
				"Perform measurements for each individually selected factor");
		chckbxIndividual.setSelected(true);

		chckbxAllSelected = new JCheckBox(
				"Perform measurements for all selected factors combined");
		chckbxAllSelected.setSelected(true);

		lblResults = new JLabel("Results");
		lblResults.setFont(new Font("SansSerif", Font.BOLD, 12));

		lblKCrossValidation = new JLabel("k cross-validation folds:");
		lblKCrossValidation.setFont(new Font("SansSerif", Font.BOLD, 12));

		textFieldCrossValidation = new JTextField();
		textFieldCrossValidation.setHorizontalAlignment(SwingConstants.RIGHT);
		textFieldCrossValidation.setText("3");
		textFieldCrossValidation.setColumns(10);

		btnSelectComplexityIndices = new JButton("Select complexity indices");
		btnSelectComplexityIndices.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ComplexityIndicesView view = new ComplexityIndicesView();
				view.setVisible(true);
			}
		});

		btnPerformMeasurements = new JButton("Perform measurements");
		btnPerformMeasurements.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int noFolds = 0;
				try {
					noFolds = Integer.valueOf(textFieldCrossValidation
							.getText());
				} catch (Exception exception) {
					noFolds = 0;
				}
				if (noFolds < 2 || noFolds > 10) {
					JOptionPane
							.showMessageDialog(
									contentPane,
									"Specified number of k cross-validation folds should be a number in the [2; 10] interval!",
									"Error", JOptionPane.ERROR_MESSAGE);
					return;
				}

				btnPerformMeasurements.setEnabled(false);
				setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

				Task task = new Task(noFolds);
				task.execute();
			}
		});

		scrollPane = new JScrollPane();
		scrollPane
				.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		table = new JTable();
		table.setFillsViewportHeight(true);
		scrollPane.setViewportView(table);

		lblComments = new JLabel("* EA - Exact Agreement");

		JButton btnSelectReadingStrategyIndices = new JButton(
				"Select reading strategy indices");
		btnSelectReadingStrategyIndices.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ReadingStrategiesIndicesView view = new ReadingStrategiesIndicesView();
				view.setVisible(true);
			}
		});
		GroupLayout gl_contentPane = new GroupLayout(contentPane);
		gl_contentPane
				.setHorizontalGroup(gl_contentPane
						.createParallelGroup(Alignment.LEADING)
						.addGroup(
								gl_contentPane
										.createSequentialGroup()
										.addGroup(
												gl_contentPane
														.createParallelGroup(
																Alignment.LEADING)
														.addComponent(
																lblSelectiveMeasurements)
														.addGroup(
																Alignment.TRAILING,
																gl_contentPane
																		.createSequentialGroup()
																		.addComponent(
																				lblComments)
																		.addPreferredGap(
																				ComponentPlacement.RELATED,
																				322,
																				Short.MAX_VALUE)
																		.addComponent(
																				btnPerformMeasurements,
																				GroupLayout.PREFERRED_SIZE,
																				214,
																				GroupLayout.PREFERRED_SIZE))
														.addComponent(
																lblResults)
														.addGroup(
																gl_contentPane
																		.createSequentialGroup()
																		.addContainerGap()
																		.addComponent(
																				scrollPane,
																				GroupLayout.DEFAULT_SIZE,
																				678,
																				Short.MAX_VALUE))
														.addGroup(
																gl_contentPane
																		.createSequentialGroup()
																		.addGroup(
																				gl_contentPane
																						.createParallelGroup(
																								Alignment.LEADING)
																						.addComponent(
																								chckbxIndividual)
																						.addComponent(
																								chckbxAllSelected)
																						.addGroup(
																								gl_contentPane
																										.createSequentialGroup()
																										.addContainerGap()
																										.addComponent(
																												lblKCrossValidation)
																										.addPreferredGap(
																												ComponentPlacement.RELATED)
																										.addComponent(
																												textFieldCrossValidation,
																												GroupLayout.PREFERRED_SIZE,
																												56,
																												GroupLayout.PREFERRED_SIZE)))
																		.addPreferredGap(
																				ComponentPlacement.RELATED,
																				42,
																				Short.MAX_VALUE)
																		.addGroup(
																				gl_contentPane
																						.createParallelGroup(
																								Alignment.LEADING,
																								false)
																						.addComponent(
																								btnSelectComplexityIndices,
																								GroupLayout.DEFAULT_SIZE,
																								GroupLayout.DEFAULT_SIZE,
																								Short.MAX_VALUE)
																						.addComponent(
																								btnSelectReadingStrategyIndices,
																								GroupLayout.DEFAULT_SIZE,
																								GroupLayout.DEFAULT_SIZE,
																								Short.MAX_VALUE))))
										.addContainerGap()));
		gl_contentPane
				.setVerticalGroup(gl_contentPane
						.createParallelGroup(Alignment.LEADING)
						.addGroup(
								gl_contentPane
										.createSequentialGroup()
										.addComponent(lblSelectiveMeasurements)
										.addGap(5)
										.addGroup(
												gl_contentPane
														.createParallelGroup(
																Alignment.BASELINE)
														.addComponent(
																chckbxAllSelected)
														.addComponent(
																btnSelectComplexityIndices))
										.addGap(5)
										.addGroup(
												gl_contentPane
														.createParallelGroup(
																Alignment.BASELINE)
														.addComponent(
																chckbxIndividual)
														.addComponent(
																btnSelectReadingStrategyIndices))
										.addPreferredGap(
												ComponentPlacement.RELATED)
										.addGroup(
												gl_contentPane
														.createParallelGroup(
																Alignment.BASELINE)
														.addComponent(
																lblKCrossValidation)
														.addComponent(
																textFieldCrossValidation,
																GroupLayout.PREFERRED_SIZE,
																GroupLayout.DEFAULT_SIZE,
																GroupLayout.PREFERRED_SIZE))
										.addPreferredGap(
												ComponentPlacement.RELATED)
										.addComponent(lblResults)
										.addPreferredGap(
												ComponentPlacement.RELATED)
										.addComponent(scrollPane,
												GroupLayout.DEFAULT_SIZE, 383,
												Short.MAX_VALUE)
										.addPreferredGap(
												ComponentPlacement.RELATED)
										.addGroup(
												gl_contentPane
														.createParallelGroup(
																Alignment.BASELINE)
														.addComponent(
																btnPerformMeasurements)
														.addComponent(
																lblComments))
										.addGap(1)));
		contentPane.setLayout(gl_contentPane);
	}
}
