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
package view.widgets.complexity;

import data.Lang;
import java.awt.Color;
import java.awt.Component;
import java.awt.EventQueue;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

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
import java.util.logging.Logger;




import services.complexity.ComplexityIndices;
import utils.localization.LocalizationUtils;
import view.models.complexity.HeaderCheckBoxHandler;
import view.models.complexity.HeaderRenderer;
import view.models.complexity.Status;
import services.complexity.ComplexityIndex;
import services.complexity.ComplexityIndexType;
import view.widgets.ReaderBenchView;

public class ComplexityIndicesView extends JFrame {

    private static final long serialVersionUID = -3120119620693209906L;

    static Logger logger = Logger.getLogger("");

    private JPanel contentPane;

    private static final int modelColumnIndex = 4;
    private static JTable complexityIndicesTable;
    private static DefaultTableModel complexityIndicesTableModel = null;
    private static ComplexityIndex[] indices = ComplexityIndices.getIndices(ReaderBenchView.RUNTIME_LANGUAGE)
                .toArray(new ComplexityIndex[0]);
    private static boolean[] selectedIndices = new boolean[indices.length];

	public static void updateSelectedIndices(Lang lang) {
		indices = ComplexityIndices.getIndices(lang)
                .toArray(new ComplexityIndex[0]);
        selectedIndices = new boolean[indices.length];
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

        if (complexityIndicesTableModel == null) {
            complexityIndicesTableModel = new DefaultTableModel(
                    new Object[][]{}, new Object[]{"ID", LocalizationUtils.getTranslation("Class name"),
                        LocalizationUtils.getTranslation("Index description"),
                        LocalizationUtils.getTranslation("Index acronym"), Status.INDETERMINATE}) {
                private static final long serialVersionUID = 6850181164110466483L;

                private final Class<?>[] columnTypes = new Class[]{Integer.class, // identifier
                    String.class, // class name
                    String.class, // factor name
                    String.class, // factor name
                    Boolean.class, // selected
            };

                @Override
                public Class<?> getColumnClass(int columnIndex) {
                    return columnTypes[columnIndex];
                }
            };
        }

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
                selectedIndices);
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
            int i = 0;
            for (ComplexityIndex index : indices) {
                Object[] dataRow = new Object[]{
                    i,
                    index.getCategoryName(),
                    index.getDescription(),
                    index.getAcronym(),
                    selectedIndices[i++]
                };
                complexityIndicesTableModel.addRow(dataRow);
            }
        }

        HeaderCheckBoxHandler.updateHeader(complexityIndicesTable,
                modelColumnIndex);
    }

    public static int[] getSelectedMeasurements() {
        return IntStream.range(0, selectedIndices.length)
                .filter(i -> selectedIndices[i])
                .toArray();
    }
    
    public static int[] getSelectedMeasurementsByCategory(ComplexityIndexType cat) {
        return IntStream.range(0, selectedIndices.length)
                .filter(i -> selectedIndices[i])
                .filter(i -> indices[i].getIndex().getType() == cat)
                .toArray();
    }

    public static ComplexityIndex[] getAllIndices() {
        return indices;
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
