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
package view.widgets.document;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import view.models.complexity.HeaderCheckBoxHandler;
import view.models.complexity.HeaderRenderer;
import view.models.complexity.Status;
import data.AbstractDocument;
import data.discourse.SemanticChain;

public class VoiceSelectionView extends JFrame {

	private static final long serialVersionUID = 5263748848854752261L;

	private static final int SEMANTIC_CHAIN_MIN_NO_WORDS = 3;

	private static int modelColumnIndex = 3;
	private JPanel contentPane;
	private DefaultTableModel tableModelVoices = null;
	private JTable tableVoices;
	private JTextArea textAreaDetails;
	private List<SemanticChain> displayedVoices;
	private boolean[] selectedVoices;
	private AbstractDocument document;

	/**
	 * Create the frame.
	 */
	public VoiceSelectionView(AbstractDocument d) {
		setTitle("ReaderBench - Voices Selection");
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setBounds(100, 100, 700, 500);
		this.document = d;
		initialization();
		contentPane = new JPanel();
		contentPane.setBackground(Color.WHITE);
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);

		JScrollPane scrollPaneVoices = new JScrollPane();
		scrollPaneVoices
				.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

		JScrollPane scrollPaneDetails = new JScrollPane();
		scrollPaneDetails
				.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

		JButton btnCancel = new JButton("Cancel");
		btnCancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				VoiceSelectionView.this.dispose();
			}
		});

		JButton btnOk = new JButton("OK");
		btnOk.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				document.setSelectedVoices(new LinkedList<SemanticChain>());
				for (int index = 0; index < displayedVoices.size(); index++) {
					if (selectedVoices[index]) {
						document.getSelectedVoices().add(
								displayedVoices.get(index));
					}
				}
				VoiceSelectionView.this.dispose();
			}
		});
		GroupLayout gl_contentPane = new GroupLayout(contentPane);
		gl_contentPane
				.setHorizontalGroup(gl_contentPane
						.createParallelGroup(Alignment.LEADING)
						.addGroup(
								gl_contentPane
										.createSequentialGroup()
										.addContainerGap()
										.addGroup(
												gl_contentPane
														.createParallelGroup(
																Alignment.LEADING)
														.addComponent(
																scrollPaneDetails,
																Alignment.TRAILING,
																GroupLayout.DEFAULT_SIZE,
																528,
																Short.MAX_VALUE)
														.addComponent(
																scrollPaneVoices,
																Alignment.TRAILING,
																GroupLayout.DEFAULT_SIZE,
																528,
																Short.MAX_VALUE)
														.addGroup(
																Alignment.TRAILING,
																gl_contentPane
																		.createSequentialGroup()
																		.addComponent(
																				btnOk,
																				GroupLayout.PREFERRED_SIZE,
																				63,
																				GroupLayout.PREFERRED_SIZE)
																		.addPreferredGap(
																				ComponentPlacement.RELATED)
																		.addComponent(
																				btnCancel)))
										.addContainerGap()));
		gl_contentPane.setVerticalGroup(gl_contentPane.createParallelGroup(
				Alignment.TRAILING)
				.addGroup(
						gl_contentPane
								.createSequentialGroup()
								.addContainerGap()
								.addComponent(scrollPaneVoices,
										GroupLayout.DEFAULT_SIZE, 251,
										Short.MAX_VALUE)
								.addPreferredGap(ComponentPlacement.RELATED)
								.addComponent(scrollPaneDetails,
										GroupLayout.PREFERRED_SIZE, 166,
										GroupLayout.PREFERRED_SIZE)
								.addPreferredGap(ComponentPlacement.RELATED)
								.addGroup(
										gl_contentPane
												.createParallelGroup(
														Alignment.BASELINE)
												.addComponent(btnCancel)
												.addComponent(btnOk))
								.addContainerGap()));

		tableModelVoices = new DefaultTableModel(
				new Object[][] {},
				new Object[] { "ID", "Voice", "No. Concepts", Status.DESELECTED }) {
			private static final long serialVersionUID = 6850181164110466483L;

			private Class<?>[] columnTypes = new Class[] { Integer.class, // identifier
					String.class, // Voice name
					Integer.class, // No. words
					Boolean.class, // selected
			};

			@Override
			public boolean isCellEditable(int rowIndex, int columnIndex) {
				if (columnIndex == 3)
					return true;
				return false;
			}

			@Override
			public Class<?> getColumnClass(int columnIndex) {
				return columnTypes[columnIndex];
			}
		};
		tableVoices = new JTable(tableModelVoices) {
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

		boolean[] editableCells = new boolean[selectedVoices.length];
		for (int i = 0; i < editableCells.length; i++)
			editableCells[i] = true;

		TableCellRenderer renderer = new HeaderRenderer(
				tableVoices.getTableHeader(), modelColumnIndex, selectedVoices,
				editableCells);
		tableVoices.getColumnModel().getColumn(modelColumnIndex)
				.setHeaderRenderer(renderer);

		// set width for ID and selected
		tableVoices.getColumnModel().getColumn(0).setMinWidth(50);
		tableVoices.getColumnModel().getColumn(0).setMaxWidth(50);
		tableVoices.getColumnModel().getColumn(0).setPreferredWidth(50);

		tableVoices.getColumnModel().getColumn(2).setMinWidth(100);
		tableVoices.getColumnModel().getColumn(2).setMaxWidth(100);
		tableVoices.getColumnModel().getColumn(2).setPreferredWidth(100);

		tableVoices.getColumnModel().getColumn(3).setMinWidth(70);
		tableVoices.getColumnModel().getColumn(3).setMaxWidth(70);
		tableVoices.getColumnModel().getColumn(3).setPreferredWidth(70);

		tableModelVoices.addTableModelListener(new HeaderCheckBoxHandler(
				tableVoices, modelColumnIndex, selectedVoices));
		tableModelVoices.addTableModelListener(new TableModelListener() {
			private int row = -1;

			public void tableChanged(TableModelEvent e) {
				if (row == -1) {
					row = e.getFirstRow();
				} else if (e.getFirstRow() != row) {
					row = e.getFirstRow();
					VoiceSelectionView.this.textAreaDetails
							.setText(VoiceSelectionView.this.displayedVoices
									.get(row).toStringAllWords());
				}
			}
		});
		tableVoices.setFillsViewportHeight(true);
		scrollPaneVoices.setViewportView(tableVoices);

		textAreaDetails = new JTextArea();
		textAreaDetails.setEditable(false);
		textAreaDetails.setWrapStyleWord(true);
		textAreaDetails.setLineWrap(true);
		scrollPaneDetails.setViewportView(textAreaDetails);
		contentPane.setLayout(gl_contentPane);

		updateContents();
	}

	public void initialization() {
		displayedVoices = new LinkedList<SemanticChain>();
		for (SemanticChain chain : document.getVoices()) {
			if (chain.getWords().size() >= SEMANTIC_CHAIN_MIN_NO_WORDS) {
				displayedVoices.add(chain);
			}
		}
		selectedVoices = new boolean[displayedVoices.size()];
		if (document.getSelectedVoices() != null
				&& document.getSelectedVoices().size() > 0) {
			for (SemanticChain chain : document.getSelectedVoices()) {
				int index = displayedVoices.indexOf(chain);
				if (index >= 0)
					selectedVoices[index] = true;
			}
		}
	}

	public void updateContents() {
		// clean table
		while (tableModelVoices.getRowCount() > 0) {
			tableModelVoices.removeRow(0);
		}

		int id = 0;
		for (SemanticChain chain : displayedVoices) {
			Vector<Object> dataRow = new Vector<Object>();
			dataRow.add(id);
			dataRow.add(chain.toString());
			dataRow.add(chain.getNoWords());
			dataRow.add(selectedVoices[id]);
			tableModelVoices.addRow(dataRow);
			id++;
		}

		HeaderCheckBoxHandler.updateHeader(tableVoices, modelColumnIndex);
	}
}
