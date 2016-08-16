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
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import data.cscl.Conversation;
import data.cscl.Participant;
import data.cscl.Utterance;

public class ParticipantEvolutionView extends JFrame {

	private static final long serialVersionUID = 6679713899556912227L;
	private JPanel contentPane;

	/**
	 * Create the frame.
	 */
	public ParticipantEvolutionView(Conversation c) {
		setTitle("ReaderBench - Participant Evolution");
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setBounds(100, 100, 700, 500);
		contentPane = new JPanel();
		contentPane.setBackground(Color.WHITE);
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPane);

		ArrayList<Participant> ls = new ArrayList<Participant>();
		for (Participant p : c.getParticipants()) {
			ls.add(p);
		}
		// participant evolution
		String[] names = new String[c.getParticipants().size()];
		for (int j = 0; j < ls.size(); j++) {
			names[j] = ls.get(j).getName();
		}
		Double[][] values = new Double[ls.size()][c.getBlocks().size()];
		double[] columns = new double[c.getBlocks().size()];
		double[] current_value = new double[c.getParticipants().size()];

		for (int i = 0; i < c.getBlocks().size(); i++) {
			if (c.getBlocks().get(i) != null)
				current_value[ls.indexOf(((Utterance) c.getBlocks().get(i))
						.getParticipant())] += c.getBlocks().get(i)
						.getOverallScore();
			columns[i] = i;
			for (int j = 0; j < ls.size(); j++) {
				values[j][i] = current_value[j];
			}
		}

		EvolutionGraph evolution = new EvolutionGraph("Participant evolution",
				"Utterance", false, names, values, columns, null);

		contentPane.add(evolution.evolution(), BorderLayout.CENTER);
	}

}
