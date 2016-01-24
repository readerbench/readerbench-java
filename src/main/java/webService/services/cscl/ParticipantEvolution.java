package webService.services.cscl;

import java.util.ArrayList;
import java.util.List;

import data.cscl.Conversation;
import data.cscl.Participant;
import data.cscl.Utterance;
import webService.result.ResultGraphPoint;

public class ParticipantEvolution {

	public static List<ResultGraphPoint> buildParticipantEvolutionData(Conversation c) {
		
		List<ResultGraphPoint> points = new ArrayList<ResultGraphPoint>(); 
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
				points.add(new ResultGraphPoint(names[j], i, current_value[j]));
			}
		}
		
		return points;
		
	}
	
}
