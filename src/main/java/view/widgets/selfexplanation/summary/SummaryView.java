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
package view.widgets.selfexplanation.summary;

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.logging.Logger;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.ScrollPaneConstants;



import data.Block;
import data.document.Summary;
import view.widgets.document.DocumentView;

/**
 * 
 * @author Mihai Dascalu
 */
public class SummaryView extends JFrame {
	private static final long serialVersionUID = -4709511294166379162L;

	static Logger logger = Logger.getLogger("");

	private Summary summary;

	public SummaryView(Summary summaryToDisplay) {
		super("ReaderBench - Summary Visualization");
		this.summary = summaryToDisplay;
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		getContentPane().setBackground(Color.WHITE);

		JPanel panelHeader = new JPanel();
		panelHeader.setBackground(Color.WHITE);

		JPanel panelContents = new JPanel();
		panelContents.setBackground(Color.WHITE);
		GroupLayout groupLayout = new GroupLayout(getContentPane());
		groupLayout.setHorizontalGroup(groupLayout.createParallelGroup(Alignment.TRAILING)
				.addGroup(groupLayout.createSequentialGroup().addContainerGap()
						.addGroup(groupLayout.createParallelGroup(Alignment.TRAILING)
								.addComponent(panelContents, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 1168,
										Short.MAX_VALUE)
						.addComponent(panelHeader, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 1168, Short.MAX_VALUE))
				.addContainerGap()));
		groupLayout
				.setVerticalGroup(
						groupLayout.createParallelGroup(Alignment.LEADING)
								.addGroup(groupLayout.createSequentialGroup().addContainerGap()
										.addComponent(panelHeader, GroupLayout.PREFERRED_SIZE, 73,
												GroupLayout.PREFERRED_SIZE)
										.addPreferredGap(ComponentPlacement.RELATED)
										.addComponent(panelContents, GroupLayout.DEFAULT_SIZE, 587, Short.MAX_VALUE)
										.addContainerGap()));

		JLabel scrollPaneContent = new JLabel("Content");
		scrollPaneContent.setFont(new Font("SansSerif", Font.BOLD, 13));

		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		GroupLayout gl_panelContents = new GroupLayout(panelContents);
		gl_panelContents.setHorizontalGroup(gl_panelContents.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panelContents.createSequentialGroup()
						.addGroup(gl_panelContents.createParallelGroup(Alignment.LEADING)
								.addGroup(gl_panelContents.createSequentialGroup().addContainerGap()
										.addComponent(scrollPaneContent))
						.addGroup(gl_panelContents.createSequentialGroup().addGap(10).addComponent(scrollPane,
								GroupLayout.DEFAULT_SIZE, 1152, Short.MAX_VALUE)))
						.addContainerGap()));
		gl_panelContents.setVerticalGroup(gl_panelContents.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panelContents.createSequentialGroup().addContainerGap().addComponent(scrollPaneContent)
						.addPreferredGap(ComponentPlacement.RELATED)
						.addComponent(scrollPane, GroupLayout.DEFAULT_SIZE, 565, Short.MAX_VALUE).addContainerGap()));

		JEditorPane textAreaContent = new JEditorPane();
		// textAreaContent.sett(true);
		textAreaContent.setContentType("text/html");
		// textAreaContent.setLineWrap(true);
		scrollPane.setViewportView(textAreaContent);
		panelContents.setLayout(gl_panelContents);

		JLabel lblDocumentTitle = new JLabel("Document title:");
		lblDocumentTitle.setFont(new Font("Lucida Grande", Font.BOLD, 13));

		JLabel lblSummary = new JLabel("Summary:");
		lblSummary.setFont(new Font("Lucida Grande", Font.BOLD, 13));

		JLabel lblDocumentTitleDescription = new JLabel("");
		lblDocumentTitleDescription.setFont(new Font("SansSerif", Font.PLAIN, 13));
		if (summary != null && summary.getReferredDoc() != null) {
			lblDocumentTitleDescription.setText(summary.getReferredDoc().getDescription());
		}

		JLabel lblVerbalizationDescription = new JLabel("");
		lblVerbalizationDescription.setFont(new Font("SansSerif", Font.PLAIN, 13));
		String textToRender = "";
		if (summary != null) {
			String descr = "";
			if (summary.getAuthors() != null && summary.getAuthors().size() > 0) {
				for (String author : summary.getAuthors())
					descr += author + ", ";
				descr = descr.substring(0, descr.length() - 2);
			}
			if (summary.getPath() != null) {
				descr += " (" + (new File(summary.getPath()).getName()) + ")";
			}
			lblVerbalizationDescription.setText(descr);

			for (Block b : summary.getBlocks()) {
				textToRender += b.getAlternateText() + "\n";
			}

			textToRender += summaryToDisplay.getAlternateText();
			textAreaContent.setText(textToRender);
		}

		JButton btnViewDocument = new JButton("View document");
		btnViewDocument.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				EventQueue.invokeLater(new Runnable() {
					@Override
					public void run() {
						if (summary.getReferredDoc() != null) {
							DocumentView view = new DocumentView(summary.getReferredDoc());
							view.setVisible(true);
						}
					}
				});
			}
		});
		GroupLayout gl_panelHeader = new GroupLayout(panelHeader);
		gl_panelHeader.setHorizontalGroup(gl_panelHeader.createParallelGroup(Alignment.TRAILING)
				.addGroup(gl_panelHeader.createSequentialGroup().addContainerGap()
						.addGroup(gl_panelHeader.createParallelGroup(Alignment.LEADING)
								.addGroup(gl_panelHeader.createSequentialGroup().addComponent(lblSummary)
										.addPreferredGap(ComponentPlacement.RELATED)
										.addComponent(lblVerbalizationDescription, GroupLayout.DEFAULT_SIZE, 941,
												Short.MAX_VALUE)
										.addGap(122))
						.addGroup(Alignment.TRAILING,
								gl_panelHeader.createSequentialGroup().addComponent(lblDocumentTitle)
										.addPreferredGap(ComponentPlacement.RELATED)
										.addComponent(lblDocumentTitleDescription, GroupLayout.DEFAULT_SIZE, 932,
												Short.MAX_VALUE)
										.addPreferredGap(ComponentPlacement.RELATED).addComponent(btnViewDocument)))));
		gl_panelHeader.setVerticalGroup(gl_panelHeader.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panelHeader.createSequentialGroup().addContainerGap()
						.addGroup(gl_panelHeader.createParallelGroup(Alignment.BASELINE).addComponent(lblDocumentTitle)
								.addComponent(lblDocumentTitleDescription).addComponent(btnViewDocument))
				.addPreferredGap(ComponentPlacement.RELATED)
				.addGroup(gl_panelHeader.createParallelGroup(Alignment.BASELINE).addComponent(lblSummary).addComponent(
						lblVerbalizationDescription, GroupLayout.PREFERRED_SIZE, 17, GroupLayout.PREFERRED_SIZE))
				.addContainerGap(29, Short.MAX_VALUE)));
		panelHeader.setLayout(gl_panelHeader);
		getContentPane().setLayout(groupLayout);

		// adjust view to desktop size
		setBounds(50, 50, 900, 400);
	}
}
