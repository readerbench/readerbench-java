package view.widgets.complexity;

import java.awt.Color;
import java.awt.Component;
import java.awt.EventQueue;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;

import services.complexity.ComplexityIndices;
import services.complexity.IComplexityFactors;
import utils.localization.LocalizationUtils;
import view.models.complexity.HeaderCheckBoxHandler;
import view.models.complexity.HeaderRenderer;
import view.models.complexity.Status;
import data.Lang;
import java.util.HashSet;
import java.util.Set;
import services.complexity.ComplexityIndex;

public class ComplexityIndicesView extends JFrame {
	private static final long serialVersionUID = -3120119620693209906L;

	static Logger logger = Logger.getLogger(ComplexityIndicesView.class);

	private JPanel contentPane;

	private static int modelColumnIndex = 4;
	private static JTable complexityIndicesTable;
	private static DefaultTableModel complexityIndicesTableModel = null;
	private static Set<ComplexityIndex> selectedIndices = new HashSet<>();
	private static Set<ComplexityIndex> editableIndices = new HashSet<>();

	public static void updateSelectedIndices(Lang lang) {
		if (lang.equals(Lang.fr)) {
			// readability formulas
			selectedIndices[ComplexityIndices.READABILITY_FLESCH] = false;
			selectedIndices[ComplexityIndices.READABILITY_FOG] = false;
			selectedIndices[ComplexityIndices.READABILITY_KINCAID] = false;
			// Word complexity
			selectedIndices[ComplexityIndices.WORD_SYLLABLE_COUNT] = false;
			// Syntax
			selectedIndices[ComplexityIndices.AVERAGE_NO_SEMANTIC_DEPENDENCIES] = false;
			// Entity Density
			selectedIndices[ComplexityIndices.AVERAGE_NO_NAMED_ENT_PER_BLOCK] = false;
			selectedIndices[ComplexityIndices.AVERAGE_NO_NOUN_NAMED_ENT_PER_BLOCK] = false;
			selectedIndices[ComplexityIndices.AVERAGE_NO_UNIQUE_NAMED_ENT_PER_BLOCK] = false;
			selectedIndices[ComplexityIndices.AVERAGE_NO_NAMED_ENT_PER_SENTENCE] = false;
			// Co-reference inference
			selectedIndices[ComplexityIndices.TOTAL_NO_COREF_CHAINS_PER_DOC] = false;
			selectedIndices[ComplexityIndices.AVERAGE_NO_COREFS_PER_CHAIN] = false;
			selectedIndices[ComplexityIndices.AVERAGE_CHAIN_SPAN] = false;
			selectedIndices[ComplexityIndices.NO_COREF_CHAINS_WITH_BIG_SPAN] = false;
			selectedIndices[ComplexityIndices.AVERAGE_INFERENCE_DISTANCE_PER_CHAIN] = false;
			selectedIndices[ComplexityIndices.NO_ACTIVE_COREF_CHAINS_PER_WORD] = false;

			// readability formulas
			editableIndices[ComplexityIndices.READABILITY_FLESCH] = false;
			editableIndices[ComplexityIndices.READABILITY_FOG] = false;
			editableIndices[ComplexityIndices.READABILITY_KINCAID] = false;
			// Word complexity
			editableIndices[ComplexityIndices.WORD_SYLLABLE_COUNT] = false;
			// Syntax
			editableIndices[ComplexityIndices.AVERAGE_NO_SEMANTIC_DEPENDENCIES] = false;
			// Entity Density
			editableIndices[ComplexityIndices.AVERAGE_NO_NAMED_ENT_PER_BLOCK] = false;
			editableIndices[ComplexityIndices.AVERAGE_NO_NOUN_NAMED_ENT_PER_BLOCK] = false;
			editableIndices[ComplexityIndices.AVERAGE_NO_UNIQUE_NAMED_ENT_PER_BLOCK] = false;
			editableIndices[ComplexityIndices.AVERAGE_NO_NAMED_ENT_PER_SENTENCE] = false;
			// Co-reference inference
			editableIndices[ComplexityIndices.TOTAL_NO_COREF_CHAINS_PER_DOC] = false;
			editableIndices[ComplexityIndices.AVERAGE_NO_COREFS_PER_CHAIN] = false;
			editableIndices[ComplexityIndices.AVERAGE_CHAIN_SPAN] = false;
			editableIndices[ComplexityIndices.NO_COREF_CHAINS_WITH_BIG_SPAN] = false;
			editableIndices[ComplexityIndices.AVERAGE_INFERENCE_DISTANCE_PER_CHAIN] = false;
			editableIndices[ComplexityIndices.NO_ACTIVE_COREF_CHAINS_PER_WORD] = false;
		}
	}

	public ComplexityIndicesView() {
		setTitle("ReaderBench - " + LocalizationUtils.getTranslation("Textual Complexity Indices"));
		setBackground(Color.WHITE);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setBounds(100, 100, 800, 400);
		contentPane = new JPanel();
		contentPane.setBackground(Color.WHITE);
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);

		JScrollPane scrollPane = new JScrollPane();

		if (complexityIndicesTableModel == null)
			complexityIndicesTableModel = new DefaultTableModel(
					new Object[][] {}, new Object[] { "ID", LocalizationUtils.getTranslation("Class name"),
							LocalizationUtils.getTranslation("Index description"), 
							LocalizationUtils.getTranslation("Index acronym"), Status.INDETERMINATE }) {
				private static final long serialVersionUID = 6850181164110466483L;

				private Class<?>[] columnTypes = new Class[] { Integer.class, // identifier
						String.class, // class name
						String.class, // factor name
						String.class, // factor name
						Boolean.class, // selected
				};

				@Override
				public boolean isCellEditable(int rowIndex, int columnIndex) {
					if (columnIndex == modelColumnIndex && editableIndices[rowIndex])
						return true;
					return false;
				}

				@Override
				public Class<?> getColumnClass(int columnIndex) {
					return columnTypes[columnIndex];
				}
			};

		complexityIndicesTable = new JTable(complexityIndicesTableModel) {
			private static final long serialVersionUID = -1615491716083330592L;

			@Override
			public void updateUI() {
				super.updateUI();
				TableCellRenderer r = getDefaultRenderer(Boolean.class);
				if (r instanceof JComponent) {
					((JComponent) r).updateUI();
				}
			}

			@Override
			public Component prepareEditor(TableCellEditor editor, int row,
					int column) {
				Component c = super.prepareEditor(editor, row, column);
				if (c instanceof JCheckBox) {
					JCheckBox b = (JCheckBox) c;
					b.setBackground(getSelectionBackground());
					b.setBorderPainted(true);
				}
				return c;
			}
		};

		TableCellRenderer renderer = new HeaderRenderer(
				complexityIndicesTable.getTableHeader(), modelColumnIndex,
				selectedIndices, editableIndices);
		complexityIndicesTable.getColumnModel().getColumn(modelColumnIndex)
				.setHeaderRenderer(renderer);

		complexityIndicesTableModel
				.addTableModelListener(new HeaderCheckBoxHandler(
						complexityIndicesTable, modelColumnIndex,
						selectedIndices));
		complexityIndicesTable.setFillsViewportHeight(true);

		// set width for ID and selected
		complexityIndicesTable.getColumnModel().getColumn(0).setMinWidth(40);
		complexityIndicesTable.getColumnModel().getColumn(0).setMaxWidth(40);
		complexityIndicesTable.getColumnModel().getColumn(0)
				.setPreferredWidth(40);

		complexityIndicesTable.getColumnModel().getColumn(3).setMinWidth(70);
		complexityIndicesTable.getColumnModel().getColumn(3).setMaxWidth(70);
		complexityIndicesTable.getColumnModel().getColumn(3)
				.setPreferredWidth(70);

		TableRowSorter<TableModel> sorter = new TableRowSorter<TableModel>(
				complexityIndicesTable.getModel());
		sorter.setSortable(modelColumnIndex, false);
		complexityIndicesTable.setRowSorter(sorter);

		scrollPane.setViewportView(complexityIndicesTable);
		GroupLayout gl_contentPane = new GroupLayout(contentPane);
		gl_contentPane.setHorizontalGroup(gl_contentPane.createParallelGroup(
				Alignment.TRAILING).addGroup(
				Alignment.LEADING,
				gl_contentPane
						.createSequentialGroup()
						.addContainerGap()
						.addComponent(scrollPane, GroupLayout.DEFAULT_SIZE,
								778, Short.MAX_VALUE).addContainerGap()));
		gl_contentPane.setVerticalGroup(gl_contentPane.createParallelGroup(
				Alignment.TRAILING).addGroup(
				Alignment.LEADING,
				gl_contentPane
						.createSequentialGroup()
						.addContainerGap()
						.addComponent(scrollPane, GroupLayout.DEFAULT_SIZE,
								356, Short.MAX_VALUE).addContainerGap()));
		contentPane.setLayout(gl_contentPane);

		updateContents();
	}

	public static void updateContents() {
		if (complexityIndicesTableModel != null) {
			// clean table
			while (complexityIndicesTableModel.getRowCount() > 0) {
				complexityIndicesTableModel.removeRow(0);
			}

			for (IComplexityFactors complexityClass : ComplexityIndices.TEXTUAL_COMPLEXITY_FACTORS) {
				for (int id : complexityClass.getIDs()) {
					Vector<Object> dataRow = new Vector<Object>();

					dataRow.add(id);
					dataRow.add(complexityClass.getClassName());
					dataRow.add(ComplexityIndices.TEXTUAL_COMPLEXITY_INDEX_DESCRIPTIONS[id]);
					dataRow.add(ComplexityIndices.TEXTUAL_COMPLEXITY_INDEX_ACRONYMS[id]);
					dataRow.add(selectedIndices[id]);
					complexityIndicesTableModel.addRow(dataRow);
				}
			}

			HeaderCheckBoxHandler.updateHeader(complexityIndicesTable,
					modelColumnIndex);
		}
	}

	public static int[] getSelectedMeasurements() {
		List<Integer> selected = new LinkedList<Integer>();
		for (int i = 0; i < selectedIndices.length; i++)
			if (selectedIndices[i])
				selected.add(i);
		return ArrayUtils.toPrimitive(selected.toArray(new Integer[selected
				.size()]));
	}

	public static void main(String[] args) {
		BasicConfigurator.configure();

		adjustToSystemGraphics();

		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					JFrame frame = new ComplexityIndicesView();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	private static void adjustToSystemGraphics() {
		for (UIManager.LookAndFeelInfo info : UIManager
				.getInstalledLookAndFeels()) {
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
}