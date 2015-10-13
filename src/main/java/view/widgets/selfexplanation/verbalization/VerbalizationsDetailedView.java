package view.widgets.selfexplanation.verbalization;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.List;
import java.util.Vector;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import services.commons.Formatting;
import services.commons.VectorAlgebra;
import services.readingStrategies.ReadingStrategies;
import view.models.verbalization.VerbalizationsTable;
import view.models.verbalization.VerbalizationsDetailedTableModel;
import DAO.document.Metacognition;

public class VerbalizationsDetailedView extends JFrame {

	private static final long serialVersionUID = 1868758739957971713L;
	private JTable tableVerbalizations;
	private DefaultTableModel modelVerbalizations;
	private List<Metacognition> loadedVervalizations;
	private int[] verbalizationIndex;
	private int[][] automaticReadingStrategies;
	private int[][] annotatedReadingStrategies;

	public VerbalizationsDetailedView(List<Metacognition> loadedVervalizations) {
		super("ReaderBench - Verbalizations Detailed Statistics");
		this.loadedVervalizations = loadedVervalizations;
		// adjust view to desktop size
		int margin = 50;
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		setBounds(margin, margin, screenSize.width - margin * 2,
				screenSize.height - margin * 2);

		createView();
		displayDetails();
	}

	private void createView() {
		int noVerbalizations = 0;
		for (Metacognition v : loadedVervalizations) {
			if (v != null) {
				noVerbalizations += v.getBlocks().size();
			}
		}
		verbalizationIndex = new int[noVerbalizations + 6]; // add 5 final rows
															// corresponding to
															// the statistics
		automaticReadingStrategies = new int[noVerbalizations][];
		annotatedReadingStrategies = new int[noVerbalizations][];

		int index = 0;
		for (int metaIndex = 0; metaIndex < loadedVervalizations.size(); metaIndex++) {
			if (loadedVervalizations.get(metaIndex) != null) {
				for (int i = 0; i < loadedVervalizations.get(metaIndex)
						.getBlocks().size(); i++) {
					verbalizationIndex[index] = metaIndex;
					automaticReadingStrategies[index] = loadedVervalizations
							.get(metaIndex).getAutomaticReadingStrategies()[i];
					annotatedReadingStrategies[index] = loadedVervalizations
							.get(metaIndex).getAnnotatedReadingStrategies()[i];
					index++;
				}
			}
		}
		verbalizationIndex[index] = -1;

		modelVerbalizations = new VerbalizationsDetailedTableModel();
		tableVerbalizations = new VerbalizationsTable(modelVerbalizations,
				verbalizationIndex);
		tableVerbalizations.getColumnModel().getColumn(0).setMinWidth(300);
		tableVerbalizations.getColumnModel().getColumn(0).setMaxWidth(300);
		tableVerbalizations.getColumnModel().getColumn(0)
				.setPreferredWidth(300);
		tableVerbalizations.setFillsViewportHeight(true);

		JScrollPane tableContainer = new JScrollPane(tableVerbalizations);
		getContentPane().add(tableContainer);
	}

	private void displayDetails() {
		// output results into corresponding file and into table
		try {
			// Open the file
			FileWriter fstream = new FileWriter("out/verbalisations_detailed_"
					+ System.currentTimeMillis() + ".csv", true);
			BufferedWriter file = new BufferedWriter(fstream);

			file.write("Author,Verbalization ID,"
					+ "Paraphrasing (Automatic),Paraphrasing (Manual),"
					+ "Causality (Automatic),Causality (Manual),"
					+ "Bridging (Automatic),Bridging (Manual),"
					+ "Text-based Inferences (Automatic),Text-based Inferences (Manual),"
					+ "Knowledge-based Inferences (Automatic),Knowledge-based Inferences (Manual),"
					+ "Control (Automatic),Control (Manual)\n");
			Vector<Object> dataRow;
			for (Metacognition v : loadedVervalizations) {
				if (v != null) {
					for (int index = 0; index < v.getBlocks().size(); index++) {
						dataRow = new Vector<Object>();

						String authors = "";
						for (String author : v.getAuthors())
							authors += author + "; ";
						authors = authors.substring(0, authors.length() - 2)
								+ " (" + v.getReferredDoc().getTitleText()
								+ ")";
						dataRow.add(authors);
						dataRow.add(index);
						file.write(authors + "," + index);

						for (int i = 0; i < ReadingStrategies.NO_READING_STRATEGIES; i++) {
							dataRow.add(v.getAutomaticReadingStrategies()[index][i]
									+ " // "
									+ v.getAnnotatedReadingStrategies()[index][i]);
							file.write(","
									+ v.getAutomaticReadingStrategies()[index][i]
									+ ","
									+ v.getAnnotatedReadingStrategies()[index][i]);
						}
						modelVerbalizations.addRow(dataRow);
						file.write("\n");
					}
				}
			}
			dataRow = new Vector<Object>();
			String out = "* for each X // Y notation: (1) X denotes the automatically identified reading strategies and (2) Y represents the annotated ones";
			dataRow.add("<i>" + out + "</i>");
			modelVerbalizations.addRow(dataRow);

			// display statistics
			dataRow = new Vector<Object>();
			dataRow.add("<b>Statistics</b>");
			modelVerbalizations.addRow(dataRow);
			file.write("\n\nStatistics," + "Paraphrasing," + "Causality,"
					+ "Bridging," + "Text-based Inferences,"
					+ "Knowledge-based Inferences," + "Control\n");

			dataRow = new Vector<Object>();
			dataRow.add("Pearson correlation");
			dataRow.add("");
			file.write("Pearson correlation");
			for (int i = 0; i < ReadingStrategies.NO_READING_STRATEGIES; i++) {
				out = Formatting
						.formatNumber(VectorAlgebra.pearsonCorrelation(
								VectorAlgebra.getVector(
										annotatedReadingStrategies, i),
								VectorAlgebra.getVector(
										automaticReadingStrategies, i)))
						+ "";
				dataRow.add(out);
				file.write("," + out);
			}
			modelVerbalizations.addRow(dataRow);
			file.write("\n");

			dataRow = new Vector<Object>();
			dataRow.add("Precision");
			dataRow.add("");
			file.write("Precision");
			for (int i = 0; i < ReadingStrategies.NO_READING_STRATEGIES; i++) {
				out = Formatting
						.formatNumber(VectorAlgebra.precision(VectorAlgebra
								.getVector(annotatedReadingStrategies, i),
								VectorAlgebra.getVector(
										automaticReadingStrategies, i)))
						+ "";
				dataRow.add(out);
				file.write("," + out);
			}
			modelVerbalizations.addRow(dataRow);
			file.write("\n");

			dataRow = new Vector<Object>();
			dataRow.add("Recall");
			dataRow.add("");
			file.write("Recall");
			for (int i = 0; i < ReadingStrategies.NO_READING_STRATEGIES; i++) {
				out = Formatting
						.formatNumber(VectorAlgebra.recall(VectorAlgebra
								.getVector(annotatedReadingStrategies, i),
								VectorAlgebra.getVector(
										automaticReadingStrategies, i)))
						+ "";
				dataRow.add(out);
				file.write("," + out);
			}
			modelVerbalizations.addRow(dataRow);
			file.write("\n");

			dataRow = new Vector<Object>();
			dataRow.add("F1 scores");
			dataRow.add("");
			file.write("F1 score");
			for (int i = 0; i < ReadingStrategies.NO_READING_STRATEGIES; i++) {
				out = Formatting.formatNumber(VectorAlgebra.fscore(
						VectorAlgebra.getVector(annotatedReadingStrategies, i),
						VectorAlgebra.getVector(automaticReadingStrategies, i),
						1))
						+ "";
				dataRow.add(out);
				file.write("," + out);
			}
			modelVerbalizations.addRow(dataRow);
			file.close();
		} catch (Exception e) {// Catch exception if any
			System.err.println("Error: " + e.getMessage());
		}
	}
}