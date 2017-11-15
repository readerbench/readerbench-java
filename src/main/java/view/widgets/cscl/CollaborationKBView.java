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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.util.logging.Logger;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.EtchedBorder;



import services.commons.Formatting;
import data.cscl.CollaborationZone;
import data.cscl.Conversation;
import data.cscl.Utterance;
import utils.LocalizationUtils;

public class CollaborationKBView extends JFrame {
	private static final long serialVersionUID = -461457535432534468L;

	static Logger logger = Logger.getLogger("");

	private Conversation chat;
	private JPanel panelPersonalKB;
	private JPanel panelSocialKB;
	private JTextField textFieldQuantCollab;
	private JTextField textFieldSocialKB;
	private JTextField textFieldSocialKBvsScore;

	public CollaborationKBView(Conversation chat) {
		setTitle("ReaderBench - " + LocalizationUtils.getTitle(this.getClass()));
		getContentPane().setBackground(Color.WHITE);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		this.chat = chat;

		// adjust view to desktop size
		setBounds(50, 50, 800, 600);

		generateLayout();
	}

	private void generateLayout() {
		JLabel lblQuantCollab = new JLabel(
				LocalizationUtils.getLocalizedString(this.getClass(), "lblQuantCollab") + ":");
		lblQuantCollab.setFont(new Font("SansSerif", Font.BOLD, 12));

		JLabel lblSocialKB = new JLabel(
				LocalizationUtils.getLocalizedString(this.getClass(), "lblSocialKB") + ":");
		lblSocialKB.setFont(new Font("SansSerif", Font.BOLD, 12));
		java.util.Hashtable<Integer, JLabel> labelTableConcepts = new java.util.Hashtable<Integer, JLabel>();
		labelTableConcepts.put(new Integer(10), new JLabel("100"));
		labelTableConcepts.put(new Integer(5), new JLabel("50"));
		labelTableConcepts.put(new Integer(0), new JLabel("0"));
		java.util.Hashtable<Integer, JLabel> labelTableThreshold = new java.util.Hashtable<Integer, JLabel>();
		labelTableThreshold.put(new Integer(10), new JLabel("100%"));
		labelTableThreshold.put(new Integer(5), new JLabel("50%"));
		labelTableThreshold.put(new Integer(0), new JLabel("0"));

		panelPersonalKB = new JPanel();
		panelPersonalKB.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null,
				null));
		panelPersonalKB.setBackground(Color.WHITE);
		panelPersonalKB.setLayout(new BorderLayout());

		Double[][] valuesColab = new Double[1][chat.getBlocks().size()];
		double[] columnsColab = new double[chat.getBlocks().size()];

		String[] namesColab = { LocalizationUtils.getLocalizedString(this.getClass(), "namesColab") };

		for (int i = 0; i < chat.getBlocks().size(); i++) {
			if (chat.getBlocks().get(i) != null) {
				valuesColab[0][i] = ((Utterance) chat.getBlocks().get(i))
						.getPersonalKB();
			}
			columnsColab[i] = i;
		}

		EvolutionGraph evolutionGraph = new EvolutionGraph(
				LocalizationUtils.getLocalizedString(this.getClass(), "graphPersKnow"),
				LocalizationUtils.getLocalizedString(this.getClass(), "graphUtterance"), false, namesColab,
				valuesColab, columnsColab, Color.RED);

		panelPersonalKB.add(evolutionGraph.evolution());

		JLabel lblSocialKBvsScore = new JLabel(
				LocalizationUtils.getLocalizedString(this.getClass(), "lblSocialKBvsScore") + ":");
		lblSocialKBvsScore.setFont(new Font("SansSerif", Font.BOLD, 12));

		textFieldQuantCollab = new JTextField();
		textFieldQuantCollab.setEditable(false);
		textFieldQuantCollab.setColumns(10);
		textFieldQuantCollab.setText(Formatting.formatNumber(chat
				.getQuantCollabPercentage() * 100) + "%");

		textFieldSocialKB = new JTextField();
		textFieldSocialKB.setEditable(false);
		textFieldSocialKB.setColumns(10);
		textFieldSocialKB.setText(Formatting.formatNumber(chat
				.getSocialKBPercentage() * 100) + "%");

		textFieldSocialKBvsScore = new JTextField();
		textFieldSocialKBvsScore.setEditable(false);
		textFieldSocialKBvsScore.setColumns(10);
		textFieldSocialKBvsScore.setText(Formatting.formatNumber(chat
				.getSocialKBvsScore()) + "");

		JScrollPane scrollPane = new JScrollPane();
		scrollPane
				.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

		JLabel lblAutomaticallyIdentifiedIntense = new JLabel(
				LocalizationUtils.getLocalizedString(this.getClass(), "lblAutomaticallyIdentifiedIntense"));
		lblAutomaticallyIdentifiedIntense.setFont(new Font("SansSerif",
				Font.BOLD, 12));

		panelSocialKB = new JPanel();
		panelSocialKB.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null,
				null));
		panelSocialKB.setBackground(Color.WHITE);
		panelSocialKB.setLayout(new BorderLayout());

		namesColab[0] = LocalizationUtils.getLocalizedString(this.getClass(), "namesColab0");

		for (int i = 0; i < chat.getBlocks().size(); i++) {
			if (chat.getBlocks().get(i) != null) {
				valuesColab[0][i] = ((Utterance) chat.getBlocks().get(i))
						.getSocialKB();
			}
			columnsColab[i] = i;
		}

		EvolutionGraph evolution = new EvolutionGraph(
				LocalizationUtils.getLocalizedString(this.getClass(), "graphSocialKnow"), 
				LocalizationUtils.getLocalizedString(this.getClass(), "graphUtterance"), false, namesColab,
				valuesColab, columnsColab, Color.BLUE);

		panelSocialKB.add(evolution.evolution());

		GroupLayout groupLayout = new GroupLayout(getContentPane());
		groupLayout
				.setHorizontalGroup(groupLayout
						.createParallelGroup(Alignment.LEADING)
						.addGroup(
								groupLayout
										.createSequentialGroup()
										.addContainerGap()
										.addGroup(
												groupLayout
														.createParallelGroup(
																Alignment.TRAILING)
														.addGroup(
																groupLayout
																		.createSequentialGroup()
																		.addGroup(
																				groupLayout
																						.createParallelGroup(
																								Alignment.LEADING)
																						.addComponent(
																								lblSocialKB,
																								GroupLayout.DEFAULT_SIZE,
																								480,
																								Short.MAX_VALUE)
																						.addComponent(
																								lblSocialKBvsScore,
																								GroupLayout.DEFAULT_SIZE,
																								480,
																								Short.MAX_VALUE)
																						.addComponent(
																								lblQuantCollab))
																		.addPreferredGap(
																				ComponentPlacement.RELATED)
																		.addGroup(
																				groupLayout
																						.createParallelGroup(
																								Alignment.LEADING)
																						.addComponent(
																								textFieldSocialKB,
																								GroupLayout.PREFERRED_SIZE,
																								80,
																								GroupLayout.PREFERRED_SIZE)
																						.addComponent(
																								textFieldQuantCollab,
																								GroupLayout.PREFERRED_SIZE,
																								80,
																								GroupLayout.PREFERRED_SIZE)
																						.addComponent(
																								textFieldSocialKBvsScore,
																								GroupLayout.PREFERRED_SIZE,
																								80,
																								GroupLayout.PREFERRED_SIZE))
																		.addContainerGap(
																				228,
																				Short.MAX_VALUE))
														.addGroup(
																groupLayout
																		.createSequentialGroup()
																		.addComponent(
																				lblAutomaticallyIdentifiedIntense)
																		.addContainerGap(
																				467,
																				Short.MAX_VALUE))
														.addGroup(
																groupLayout
																		.createSequentialGroup()
																		.addGroup(
																				groupLayout
																						.createParallelGroup(
																								Alignment.TRAILING)
																						.addComponent(
																								panelSocialKB,
																								Alignment.LEADING,
																								GroupLayout.DEFAULT_SIZE,
																								788,
																								Short.MAX_VALUE)
																						.addComponent(
																								panelPersonalKB,
																								GroupLayout.DEFAULT_SIZE,
																								788,
																								Short.MAX_VALUE)
																						.addComponent(
																								scrollPane,
																								GroupLayout.DEFAULT_SIZE,
																								788,
																								Short.MAX_VALUE))
																		.addContainerGap()))));
		groupLayout
				.setVerticalGroup(groupLayout
						.createParallelGroup(Alignment.LEADING)
						.addGroup(
								groupLayout
										.createSequentialGroup()
										.addGap(7)
										.addGroup(
												groupLayout
														.createParallelGroup(
																Alignment.BASELINE)
														.addComponent(
																lblQuantCollab)
														.addComponent(
																textFieldQuantCollab,
																GroupLayout.PREFERRED_SIZE,
																GroupLayout.DEFAULT_SIZE,
																GroupLayout.PREFERRED_SIZE))
										.addPreferredGap(
												ComponentPlacement.RELATED)
										.addGroup(
												groupLayout
														.createParallelGroup(
																Alignment.BASELINE)
														.addComponent(
																lblSocialKB,
																GroupLayout.PREFERRED_SIZE,
																23,
																GroupLayout.PREFERRED_SIZE)
														.addComponent(
																textFieldSocialKB,
																GroupLayout.PREFERRED_SIZE,
																GroupLayout.DEFAULT_SIZE,
																GroupLayout.PREFERRED_SIZE))
										.addPreferredGap(
												ComponentPlacement.RELATED)
										.addGroup(
												groupLayout
														.createParallelGroup(
																Alignment.BASELINE)
														.addComponent(
																lblSocialKBvsScore,
																GroupLayout.PREFERRED_SIZE,
																22,
																GroupLayout.PREFERRED_SIZE)
														.addComponent(
																textFieldSocialKBvsScore,
																GroupLayout.PREFERRED_SIZE,
																GroupLayout.DEFAULT_SIZE,
																GroupLayout.PREFERRED_SIZE))
										.addGap(8)
										.addComponent(panelPersonalKB,
												GroupLayout.DEFAULT_SIZE, 189,
												Short.MAX_VALUE)
										.addPreferredGap(
												ComponentPlacement.RELATED)
										.addComponent(panelSocialKB,
												GroupLayout.DEFAULT_SIZE, 185,
												Short.MAX_VALUE)
										.addPreferredGap(
												ComponentPlacement.RELATED)
										.addComponent(
												lblAutomaticallyIdentifiedIntense)
										.addPreferredGap(
												ComponentPlacement.RELATED)
										.addComponent(scrollPane,
												GroupLayout.PREFERRED_SIZE, 57,
												GroupLayout.PREFERRED_SIZE)
										.addContainerGap()));

		JTextPane textPane = new JTextPane();
		textPane.setEditable(false);
		textPane.setBackground(Color.WHITE);
		for (CollaborationZone zone : chat.getIntenseCollabZonesSocialKB())
			textPane.setText(textPane.getText() + zone.toStringDetailed()
					+ "\n");
		textPane.setText(textPane.getText().trim());

		scrollPane.setViewportView(textPane);
		getContentPane().setLayout(groupLayout);
	}
}
