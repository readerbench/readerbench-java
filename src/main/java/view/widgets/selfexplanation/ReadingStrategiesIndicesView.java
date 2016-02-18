package view.widgets.selfexplanation;

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

import data.discourse.SemanticCohesion;
import services.readingStrategies.ReadingStrategies;
import view.models.complexity.HeaderCheckBoxHandler;
import view.models.complexity.HeaderRenderer;
import view.models.complexity.Status;

public class ReadingStrategiesIndicesView extends JFrame {
	private static final long serialVersionUID = -3120119620693209906L;

	static Logger logger = Logger.getLogger(ReadingStrategiesIndicesView.class);

	private JPanel contentPane;

	private static int modelColumnIndex = 2;
	private static JTable readingStrategiesTable;
	private static DefaultTableModel readingStrategiesTableModel = null;
	// all reading strategies, plus cohesion and fluency
	private static boolean[] selectedFactors = new boolean[ReadingStrategies.NO_READING_STRATEGIES + 2];
	public static String[] READING_STRATEGY_INDEX_NAMES = new String[ReadingStrategies.NO_READING_STRATEGIES
			+ SemanticCohesion.NO_COHESION_DIMENSIONS];

	static {
		int i;
		for (i = 0; i < selectedFactors.length; i++) {
			selectedFactors[i] = true;
		}
		for (i = 0; i < ReadingStrategies.NO_READING_STRATEGIES; i++) {
			READING_STRATEGY_INDEX_NAMES[i] = ReadingStrategies.STRATEGY_NAMES[i];
		}
		for (int k = 0; k < SemanticCohesion.getSemanticDistanceNames().length; k++)
			READING_STRATEGY_INDEX_NAMES[i + k] = "Cohesion (" + SemanticCohesion.getSemanticDistanceNames()[k] + ")";
	}

	public ReadingStrategiesIndicesView() {
		setTitle("ReaderBench - Reading Strategies Indices");
		setBackground(Color.WHITE);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setBounds(100, 100, 400, 300);
		contentPane = new JPanel();
		contentPane.setBackground(Color.WHITE);
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);

		JScrollPane scrollPane = new JScrollPane();

		if (readingStrategiesTableModel == null)
			readingStrategiesTableModel = new DefaultTableModel(new Object[][] {},
					new Object[] { "ID", "Index name", Status.INDETERMINATE }) {
				private static final long serialVersionUID = 6850181164110466483L;

				private Class<?>[] columnTypes = new Class[] { Integer.class, // identifier
						String.class, // index name
						Boolean.class, // selected
				};

				@Override
				public boolean isCellEditable(int rowIndex, int columnIndex) {
					if (columnIndex == 2)
						return true;
					return false;
				}

				@Override
				public Class<?> getColumnClass(int columnIndex) {
					return columnTypes[columnIndex];
				}
			};

		readingStrategiesTable = new JTable(readingStrategiesTableModel) {
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
			public Component prepareEditor(TableCellEditor editor, int row, int column) {
				Component c = super.prepareEditor(editor, row, column);
				if (c instanceof JCheckBox) {
					JCheckBox b = (JCheckBox) c;
					b.setBackground(getSelectionBackground());
					b.setBorderPainted(true);
				}
				return c;
			}
		};

		boolean[] editableCells = new boolean[selectedFactors.length];
		for (int i = 0; i < editableCells.length; i++)
			editableCells[i] = true;

		TableCellRenderer renderer = new HeaderRenderer(readingStrategiesTable.getTableHeader(), modelColumnIndex,
				selectedFactors, editableCells);
		readingStrategiesTable.getColumnModel().getColumn(modelColumnIndex).setHeaderRenderer(renderer);

		readingStrategiesTableModel.addTableModelListener(
				new HeaderCheckBoxHandler(readingStrategiesTable, modelColumnIndex, selectedFactors));
		readingStrategiesTable.setFillsViewportHeight(true);

		// set width for ID and selected
		readingStrategiesTable.getColumnModel().getColumn(0).setMinWidth(40);
		readingStrategiesTable.getColumnModel().getColumn(0).setMaxWidth(40);
		readingStrategiesTable.getColumnModel().getColumn(0).setPreferredWidth(40);

		readingStrategiesTable.getColumnModel().getColumn(2).setMinWidth(70);
		readingStrategiesTable.getColumnModel().getColumn(2).setMaxWidth(70);
		readingStrategiesTable.getColumnModel().getColumn(2).setPreferredWidth(70);

		TableRowSorter<TableModel> sorter = new TableRowSorter<TableModel>(readingStrategiesTable.getModel());
		sorter.setSortable(modelColumnIndex, false);
		readingStrategiesTable.setRowSorter(sorter);

		scrollPane.setViewportView(readingStrategiesTable);
		GroupLayout gl_contentPane = new GroupLayout(contentPane);
		gl_contentPane
				.setHorizontalGroup(gl_contentPane.createParallelGroup(Alignment.TRAILING).addGroup(Alignment.LEADING,
						gl_contentPane.createSequentialGroup().addContainerGap()
								.addComponent(scrollPane, GroupLayout.DEFAULT_SIZE, 778, Short.MAX_VALUE)
								.addContainerGap()));
		gl_contentPane
				.setVerticalGroup(gl_contentPane.createParallelGroup(Alignment.TRAILING).addGroup(Alignment.LEADING,
						gl_contentPane.createSequentialGroup().addContainerGap()
								.addComponent(scrollPane, GroupLayout.DEFAULT_SIZE, 356, Short.MAX_VALUE)
								.addContainerGap()));
		contentPane.setLayout(gl_contentPane);

		updateContents();
	}

	public static void updateContents() {
		if (readingStrategiesTableModel != null) {
			// clean table
			while (readingStrategiesTableModel.getRowCount() > 0) {
				readingStrategiesTableModel.removeRow(0);
			}

			Vector<Object> dataRow;
			int index = 0;
			for (; index < ReadingStrategies.NO_READING_STRATEGIES; index++) {
				dataRow = new Vector<Object>();
				dataRow.add(index);
				dataRow.add(ReadingStrategies.STRATEGY_NAMES[index]);
				dataRow.add(selectedFactors[index]);
				readingStrategiesTableModel.addRow(dataRow);
			}
			dataRow = new Vector<Object>();
			dataRow.add(index);
			dataRow.add("Cohesion with initial text");
			dataRow.add(selectedFactors[index]);
			readingStrategiesTableModel.addRow(dataRow);
			index++;

			HeaderCheckBoxHandler.updateHeader(readingStrategiesTable, modelColumnIndex);
		}
	}

	public static int[] getSelectedMeasurements() {
		List<Integer> selected = new LinkedList<Integer>();
		int i = 0;
		for (; i < selectedFactors.length - 1; i++)
			if (selectedFactors[i])
				selected.add(i);
		// final check for cohesion
		if (selectedFactors[i]) {
			for (int j = 0; j < SemanticCohesion.NO_COHESION_DIMENSIONS; j++)
				selected.add(i + j);
		}
		return ArrayUtils.toPrimitive(selected.toArray(new Integer[selected.size()]));
	}

	public static void main(String[] args) {
		BasicConfigurator.configure();

		adjustToSystemGraphics();

		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					JFrame frame = new ReadingStrategiesIndicesView();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
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
}