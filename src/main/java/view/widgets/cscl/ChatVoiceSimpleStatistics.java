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
package view.widgets.cscl;

import java.awt.Color;
import java.util.List;
import java.util.Vector;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import services.commons.Formatting;

import data.discourse.SemanticChain;
import utils.LocalizationUtils;

public class ChatVoiceSimpleStatistics extends JFrame {

	private static final long serialVersionUID = 4348984196444604647L;

	private JPanel contentPane;
	private DefaultTableModel tableModel;
	private JTable table;
	private List<SemanticChain> displayedVoices;

	/**
	 * Create the frame.
	 */
	public ChatVoiceSimpleStatistics(List<SemanticChain> chains) {
		super.setTitle("ReaderBench - " + LocalizationUtils.getTitle(this.getClass()));
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setBounds(100, 100, 1000, 400);
		contentPane = new JPanel();
		contentPane.setBackground(Color.WHITE);
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		this.displayedVoices = chains;

		JScrollPane scrollPane = new JScrollPane();
		scrollPane
				.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		GroupLayout gl_contentPane = new GroupLayout(contentPane);
		gl_contentPane.setHorizontalGroup(gl_contentPane.createParallelGroup(
				Alignment.LEADING).addGroup(
				gl_contentPane
						.createSequentialGroup()
						.addContainerGap()
						.addComponent(scrollPane, GroupLayout.DEFAULT_SIZE,
								578, Short.MAX_VALUE).addContainerGap()));
		gl_contentPane.setVerticalGroup(gl_contentPane.createParallelGroup(
				Alignment.LEADING).addGroup(
				gl_contentPane
						.createSequentialGroup()
						.addContainerGap()
						.addComponent(scrollPane, GroupLayout.DEFAULT_SIZE,
								356, Short.MAX_VALUE).addContainerGap()));

		tableModel = new DefaultTableModel(new Object[][] {}, new Object[] {
				LocalizationUtils.getLocalizedString(this.getClass(), "tableColumn1"),
				LocalizationUtils.getLocalizedString(this.getClass(), "tableColumn2"),
				LocalizationUtils.getLocalizedString(this.getClass(), "tableColumn3"),
				LocalizationUtils.getLocalizedString(this.getClass(), "tableColumn4"),
				LocalizationUtils.getLocalizedString(this.getClass(), "tableColumn5"),
				LocalizationUtils.getLocalizedString(this.getClass(), "tableColumn6"),
				LocalizationUtils.getLocalizedString(this.getClass(), "tableColumn7"),
				LocalizationUtils.getLocalizedString(this.getClass(), "tableColumn8"),
				LocalizationUtils.getLocalizedString(this.getClass(), "tableColumn9") }) {
			private static final long serialVersionUID = 6850181164110466483L;

			private Class<?>[] columnTypes = new Class[] { Integer.class, // identifier
					String.class, // Voice name
					Integer.class, // No. words
					Integer.class, // No. utterances
					Double.class, // Average utterance importance score
					Double.class, // Entropy block moving average
					Double.class, // Average Recurrence
					Double.class, // Stdev Recurrence
					Double.class, // Average sentiment
					Double.class, // Stdev sentiment
			};

			@Override
			public boolean isCellEditable(int rowIndex, int columnIndex) {
				return false;
			}

			@Override
			public Class<?> getColumnClass(int columnIndex) {
				return columnTypes[columnIndex];
			}
		};

		table = new JTable(tableModel);
		// set width for ID and selected
		// set width for ID and selected
		table.getColumnModel().getColumn(0).setMinWidth(50);
		table.getColumnModel().getColumn(0).setMaxWidth(50);
		table.getColumnModel().getColumn(0).setPreferredWidth(50);

		table.getColumnModel().getColumn(1).setMinWidth(220);
		table.getColumnModel().getColumn(1).setMaxWidth(220);
		table.getColumnModel().getColumn(1).setPreferredWidth(220);

		TableRowSorter<TableModel> sorter = new TableRowSorter<TableModel>(
				table.getModel());
		table.setRowSorter(sorter);

		table.setFillsViewportHeight(true);

		scrollPane.setViewportView(table);
		contentPane.setLayout(gl_contentPane);

		updateContents();
	}

	private void updateContents() {
		while (tableModel.getRowCount() > 0) {
			tableModel.removeRow(0);
		}

		for (int id = 0; id < displayedVoices.size(); id++) {
			SemanticChain voice = displayedVoices.get(id);
			Vector<Object> dataRow = new Vector<Object>();

			dataRow.add(id);
			dataRow.add(voice.toString());
			dataRow.add(voice.getNoWords());
			dataRow.add(Formatting.formatNumber(voice
					.getAverageImportanceScore()));
			dataRow.add(Formatting.formatNumber(voice.getEntropyBlock(true)));
			dataRow.add(Formatting.formatNumber(voice.getAvgRecurrenceBlock()));
			dataRow.add(Formatting.formatNumber(voice.getStdevRecurrenceBlock()));
			//dataRow.add(Formatting.formatNumber(voice.getSentimentAverage()));
			dataRow.add(Formatting.formatNumber(voice.getStdevSentiment()));

			tableModel.addRow(dataRow);
		}
	}
}
