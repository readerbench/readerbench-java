package view.widgets.chat;

import java.awt.BorderLayout;
import java.awt.Color;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import DAO.cscl.Conversation;
import DAO.cscl.Participant;
import DAO.cscl.Utterance;

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
