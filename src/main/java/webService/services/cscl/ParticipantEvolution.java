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
