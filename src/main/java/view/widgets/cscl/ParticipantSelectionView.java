package view.widgets.cscl;

import data.cscl.Conversation;
import data.cscl.Participant;
import view.models.complexity.HeaderCheckBoxHandler;
import view.models.complexity.HeaderRenderer;
import view.models.complexity.Status;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import java.awt.Color;
import java.awt.Component;


import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;


/**
 * Created by Florea Anda-Madalina
 */
public class ParticipantSelectionView extends JFrame {

    private static final long serialVersionUID = 5263748848854752261L;

    private final static int MODEL_COLUMN_INDEX = 2;
    private JPanel contentPane;
    private final DefaultTableModel tableModel;
    private final JTable table;
    private final List<Participant> displayedParticipants;
    private final boolean[] selectedParticipants;
    private final Conversation chat;

    /**
     * Create the frame.
     *
     * @param c
     */
    public ParticipantSelectionView(Conversation c) {
        super.setTitle("ReaderBench - Participants Selection");
        super.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        super.setBounds(100, 100, 300, 300);
        contentPane = new JPanel();
        contentPane.setBackground(Color.WHITE);
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        super.setContentPane(contentPane);

        this.chat = c;
        displayedParticipants = new ArrayList<>();
        for (Participant p: chat.getParticipants()) {
            displayedParticipants.add(p);
        }

        selectedParticipants = new boolean[displayedParticipants.size()];
        if (chat.getSelectedParticipants() != null
                && chat.getSelectedParticipants().size() > 0) {
            for (Participant p : chat.getSelectedParticipants()) {
                int index = displayedParticipants.indexOf(p);
                if (index >= 0) {
                    selectedParticipants[index] = true;
                }
            }
        }

        JScrollPane scrollPaneParticipants = new JScrollPane();
        scrollPaneParticipants
                .setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        JButton btnCancel = new JButton("Cancel");
        btnCancel.addActionListener((ActionEvent e) -> {
            view.widgets.cscl.ParticipantSelectionView.this.dispose();
        });

        JButton btnOk = new JButton("OK");
        btnOk.addActionListener((ActionEvent e) -> {
            chat.setSelectedParticipants(new LinkedList<>());
            for (int index = 0; index < displayedParticipants.size(); index++) {
                if (selectedParticipants[index]) {
                    chat.getSelectedParticipants().add(
                            displayedParticipants.get(index));
                }
            }
            view.widgets.cscl.ParticipantSelectionView.this.dispose();
        });
        GroupLayout gl_contentPane = new GroupLayout(contentPane);
        gl_contentPane
                .setHorizontalGroup(gl_contentPane
                        .createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(
                                gl_contentPane
                                        .createSequentialGroup()
                                        .addContainerGap()
                                        .addGroup(
                                                gl_contentPane
                                                        .createParallelGroup(
                                                                GroupLayout.Alignment.LEADING)
                                                        .addComponent(
                                                                scrollPaneParticipants,
                                                                GroupLayout.Alignment.TRAILING,
                                                                GroupLayout.DEFAULT_SIZE,
                                                                528,
                                                                Short.MAX_VALUE)
                                                        .addGroup(
                                                                GroupLayout.Alignment.TRAILING,
                                                                gl_contentPane
                                                                        .createSequentialGroup()
                                                                        .addComponent(
                                                                                btnOk,
                                                                                GroupLayout.PREFERRED_SIZE,
                                                                                63,
                                                                                GroupLayout.PREFERRED_SIZE)
                                                                        .addPreferredGap(
                                                                                LayoutStyle.ComponentPlacement.RELATED)
                                                                        .addComponent(
                                                                                btnCancel)))
                                        .addContainerGap()));
        gl_contentPane.setVerticalGroup(gl_contentPane.createParallelGroup(
                GroupLayout.Alignment.TRAILING)
                .addGroup(
                        gl_contentPane
                                .createSequentialGroup()
                                .addContainerGap()
                                .addComponent(scrollPaneParticipants,
                                        GroupLayout.DEFAULT_SIZE, 251,
                                        Short.MAX_VALUE)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(
                                        gl_contentPane
                                                .createParallelGroup(
                                                        GroupLayout.Alignment.BASELINE)
                                                .addComponent(btnCancel)
                                                .addComponent(btnOk))
                                .addContainerGap()));

        tableModel= new DefaultTableModel(
                new Object[][]{},
                new Object[]{"ID", "Participant", Status.DESELECTED}) {
            private static final long serialVersionUID = 6850181164110466483L;

            private Class<?>[] columnTypes = new Class[]{Integer.class, // identifier
                    String.class, // Participant name
                    Boolean.class, // selected
            };

            @Override
            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return columnIndex == 2;
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return columnTypes[columnIndex];
            }
        };
        table = new JTable(tableModel) {
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

        boolean[] editableCells = new boolean[selectedParticipants.length];
        for (int i = 0; i < editableCells.length; i++) {
            editableCells[i] = true;
        }

        TableCellRenderer renderer = new HeaderRenderer(
                table.getTableHeader(), MODEL_COLUMN_INDEX, selectedParticipants);
        table.getColumnModel().getColumn(MODEL_COLUMN_INDEX)
                .setHeaderRenderer(renderer);

        // set width for ID and selected
        table.getColumnModel().getColumn(0).setMinWidth(50);
        table.getColumnModel().getColumn(0).setMaxWidth(50);
        table.getColumnModel().getColumn(0).setPreferredWidth(50);

        table.getColumnModel().getColumn(1).setMinWidth(100);
        table.getColumnModel().getColumn(1).setMaxWidth(100);
        table.getColumnModel().getColumn(1).setPreferredWidth(100);

        table.getColumnModel().getColumn(2).setMinWidth(70);
        table.getColumnModel().getColumn(2).setMaxWidth(70);
        table.getColumnModel().getColumn(2).setPreferredWidth(70);

        tableModel.addTableModelListener(new HeaderCheckBoxHandler(
                table, MODEL_COLUMN_INDEX, selectedParticipants));

        table.setFillsViewportHeight(true);
        scrollPaneParticipants.setViewportView(table);

        contentPane.setLayout(gl_contentPane);

        updateContents();
    }

    public final void updateContents() {
        // clean table
        while (tableModel.getRowCount() > 0) {
            tableModel.removeRow(0);
        }

        int id = 0;
        for (Participant p: displayedParticipants) {
            List<Object> dataRow = new ArrayList<>();
            dataRow.add(id);
            dataRow.add(p.getName());
            dataRow.add(selectedParticipants[id]);
            tableModel.addRow(dataRow.toArray());
            id++;
        }

        HeaderCheckBoxHandler.updateHeader(table, MODEL_COLUMN_INDEX);
    }
}
