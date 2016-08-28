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
package view.widgets.selfexplanation.verbalization;

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Vector;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.ScrollPaneConstants;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.table.DefaultTableModel;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;

import services.commons.Formatting;
import services.readingStrategies.ReadingStrategies;
import view.models.verbalization.VerbalizationTable;
import view.models.verbalization.VerbalizationTableModel;
import view.widgets.document.DocumentView;
import data.Block;
import data.Sentence;
import data.discourse.SemanticCohesion;
import data.document.Metacognition;

/**
 * 
 * @author Mihai Dascalu
 */
public class VerbalizationView extends JFrame {
	private static final long serialVersionUID = -4709511294166379162L;

	static Logger logger = Logger.getLogger(VerbalizationView.class);

	private Metacognition verbalization;
	private DefaultTableModel modelContents;
	private JTable tableContents;
	private boolean[] isVerbalisation;

	public VerbalizationView(Metacognition verbalizationToDisplay) {
		super("ReaderBench - Verbalization Visualization");
		this.verbalization = verbalizationToDisplay;
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		getContentPane().setBackground(Color.WHITE);

		JPanel panelHeader = new JPanel();
		panelHeader.setBackground(Color.WHITE);

		JPanel panelContents = new JPanel();
		panelContents.setBackground(Color.WHITE);
		GroupLayout groupLayout = new GroupLayout(getContentPane());
		groupLayout
				.setHorizontalGroup(groupLayout
						.createParallelGroup(Alignment.TRAILING)
						.addGroup(
								groupLayout
										.createSequentialGroup()
										.addContainerGap()
										.addGroup(
												groupLayout
														.createParallelGroup(
																Alignment.TRAILING)
														.addComponent(
																panelContents,
																Alignment.LEADING,
																GroupLayout.DEFAULT_SIZE,
																1168,
																Short.MAX_VALUE)
														.addComponent(
																panelHeader,
																Alignment.LEADING,
																GroupLayout.DEFAULT_SIZE,
																1168,
																Short.MAX_VALUE))
										.addContainerGap()));
		groupLayout.setVerticalGroup(groupLayout.createParallelGroup(
				Alignment.LEADING).addGroup(
				groupLayout
						.createSequentialGroup()
						.addContainerGap()
						.addComponent(panelHeader, GroupLayout.PREFERRED_SIZE,
								73, GroupLayout.PREFERRED_SIZE)
						.addPreferredGap(ComponentPlacement.RELATED)
						.addComponent(panelContents, GroupLayout.DEFAULT_SIZE,
								587, Short.MAX_VALUE).addContainerGap()));

		JLabel lblContents = new JLabel("Contents");
		lblContents.setFont(new Font("SansSerif", Font.BOLD, 13));

		JScrollPane scrollPane = new JScrollPane();
		scrollPane
				.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		GroupLayout gl_panelContents = new GroupLayout(panelContents);
		gl_panelContents
				.setHorizontalGroup(gl_panelContents
						.createParallelGroup(Alignment.LEADING)
						.addGroup(
								gl_panelContents
										.createSequentialGroup()
										.addGroup(
												gl_panelContents
														.createParallelGroup(
																Alignment.LEADING)
														.addGroup(
																gl_panelContents
																		.createSequentialGroup()
																		.addContainerGap()
																		.addComponent(
																				lblContents))
														.addGroup(
																gl_panelContents
																		.createSequentialGroup()
																		.addGap(10)
																		.addComponent(
																				scrollPane,
																				GroupLayout.DEFAULT_SIZE,
																				1152,
																				Short.MAX_VALUE)))
										.addContainerGap()));
		gl_panelContents.setVerticalGroup(gl_panelContents.createParallelGroup(
				Alignment.LEADING).addGroup(
				gl_panelContents
						.createSequentialGroup()
						.addContainerGap()
						.addComponent(lblContents)
						.addPreferredGap(ComponentPlacement.RELATED)
						.addComponent(scrollPane, GroupLayout.DEFAULT_SIZE,
								565, Short.MAX_VALUE).addContainerGap()));
		panelContents.setLayout(gl_panelContents);

		JLabel lblDocumentTitle = new JLabel("Document title:");
		lblDocumentTitle.setFont(new Font("Lucida Grande", Font.BOLD, 13));

		JLabel lblVerbalization = new JLabel("Verbalization:");
		lblVerbalization.setFont(new Font("Lucida Grande", Font.BOLD, 13));

		JLabel lblDocumentTitleDescription = new JLabel("");
		lblDocumentTitleDescription.setFont(new Font("SansSerif", Font.PLAIN,
				13));
		if (verbalization != null && verbalization.getReferredDoc() != null) {
			lblDocumentTitleDescription.setText(verbalization.getReferredDoc()
					.getDescription());
		}

		JLabel lblVerbalizationDescription = new JLabel("");
		lblVerbalizationDescription.setFont(new Font("SansSerif", Font.PLAIN,
				13));
		if (verbalization != null) {
			String authors = "";
			for (String author : verbalization.getAuthors())
				authors += author + ", ";
			authors = authors.substring(0, authors.length() - 2);
			lblVerbalizationDescription.setText(authors + " ("
					+ (new File(verbalization.getPath()).getName()) + ")");
		}

		JButton btnViewDocument = new JButton("View document");
		btnViewDocument.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				EventQueue.invokeLater(new Runnable() {
					@Override
					public void run() {
						if (verbalization.getReferredDoc() != null) {
							DocumentView view = new DocumentView(verbalization
									.getReferredDoc());
							view.setVisible(true);
						}
					}
				});
			}
		});
		GroupLayout gl_panelHeader = new GroupLayout(panelHeader);
		gl_panelHeader
				.setHorizontalGroup(gl_panelHeader
						.createParallelGroup(Alignment.TRAILING)
						.addGroup(
								gl_panelHeader
										.createSequentialGroup()
										.addContainerGap()
										.addGroup(
												gl_panelHeader
														.createParallelGroup(
																Alignment.LEADING)
														.addGroup(
																gl_panelHeader
																		.createSequentialGroup()
																		.addComponent(
																				lblVerbalization)
																		.addPreferredGap(
																				ComponentPlacement.RELATED)
																		.addComponent(
																				lblVerbalizationDescription,
																				GroupLayout.DEFAULT_SIZE,
																				941,
																				Short.MAX_VALUE)
																		.addGap(122))
														.addGroup(
																Alignment.TRAILING,
																gl_panelHeader
																		.createSequentialGroup()
																		.addComponent(
																				lblDocumentTitle)
																		.addPreferredGap(
																				ComponentPlacement.RELATED)
																		.addComponent(
																				lblDocumentTitleDescription,
																				GroupLayout.DEFAULT_SIZE,
																				932,
																				Short.MAX_VALUE)
																		.addPreferredGap(
																				ComponentPlacement.RELATED)
																		.addComponent(
																				btnViewDocument)))));
		gl_panelHeader
				.setVerticalGroup(gl_panelHeader
						.createParallelGroup(Alignment.LEADING)
						.addGroup(
								gl_panelHeader
										.createSequentialGroup()
										.addContainerGap()
										.addGroup(
												gl_panelHeader
														.createParallelGroup(
																Alignment.BASELINE)
														.addComponent(
																lblDocumentTitle)
														.addComponent(
																lblDocumentTitleDescription)
														.addComponent(
																btnViewDocument))
										.addPreferredGap(
												ComponentPlacement.RELATED)
										.addGroup(
												gl_panelHeader
														.createParallelGroup(
																Alignment.BASELINE)
														.addComponent(
																lblVerbalization)
														.addComponent(
																lblVerbalizationDescription,
																GroupLayout.PREFERRED_SIZE,
																17,
																GroupLayout.PREFERRED_SIZE))
										.addContainerGap(29, Short.MAX_VALUE)));
		panelHeader.setLayout(gl_panelHeader);
		getContentPane().setLayout(groupLayout);

		// adjust view to desktop size
		setBounds(50, 50, 1180, 700);

		modelContents = new VerbalizationTableModel();
		determineRowType();
		tableContents = new VerbalizationTable(modelContents, isVerbalisation);
		tableContents.setFillsViewportHeight(true);
		tableContents.getColumnModel().getColumn(0).setPreferredWidth(600);

		scrollPane.setViewportView(tableContents);

		updateContent();
	}

	private void determineRowType() {
		// entire document + verbalization + overall row
		isVerbalisation = new boolean[verbalization.getBlocks().size()
				+ verbalization.getReferredDoc().getBlocks().size() + 1];
		for (int i = 0; i < isVerbalisation.length; i++) {
			isVerbalisation[i] = false;
		}
		for (Block v : verbalization.getBlocks()) {
			isVerbalisation[v.getIndex() + v.getRefBlock().getIndex() + 1] = true;
		}
	}

	private void updateContent() {
		if (verbalization != null && verbalization.getReferredDoc() != null) {
			// clean table
			while (modelContents.getRowCount() > 0) {
				modelContents.removeRow(0);
			}

			int[] cummulativeStrategies = new int[ReadingStrategies.NO_READING_STRATEGIES];

			int startIndex = 0;
			int endIndex = 0;
			for (int index = 0; index < verbalization.getBlocks().size(); index++) {
				endIndex = verbalization.getBlocks().get(index).getRefBlock()
						.getIndex();
				for (int refBlockId = startIndex; refBlockId <= endIndex; refBlockId++) {
					// add rows as blocks within the document
					Vector<Object> dataRow = new Vector<Object>();

					SemanticCohesion coh = verbalization.getBlockSimilarities()[refBlockId];
					// add block text
					String text = "";
					for (Sentence s : verbalization.getReferredDoc()
							.getBlocks().get(refBlockId).getSentences()) {
						text += s.getAlternateText() + " ";
					}
					dataRow.add(text);

					for (int i = 0; i < ReadingStrategies.NO_READING_STRATEGIES; i++) {
						dataRow.add("");
					}

					// add cohesion
					dataRow.add(Formatting.formatNumber(coh.getCohesion())
							.toString());
					modelContents.addRow(dataRow);
				}
				startIndex = endIndex + 1;

				// add corresponding verbalization
				Vector<Object> dataRow = new Vector<Object>();

				dataRow.add(verbalization.getBlocks().get(index)
						.getAlternateText().trim());

				for (int i = 0; i < ReadingStrategies.NO_READING_STRATEGIES; i++) {
					dataRow.add(verbalization.getAutomatedRS()[index][i]);
					cummulativeStrategies[i] += verbalization
							.getAutomatedRS()[index][i];
				}
				dataRow.add("");
				modelContents.addRow(dataRow);
			}

			// add final row
			Vector<Object> dataRow = new Vector<Object>();
			dataRow.add("Overall reading strategies");

			for (int i = 0; i < ReadingStrategies.NO_READING_STRATEGIES; i++) {
				dataRow.add(cummulativeStrategies[i]);
			}
			dataRow.add("");
			modelContents.addRow(dataRow);
		}
	}
}