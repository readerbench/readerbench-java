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
package webService.services.vCoP;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import data.cscl.Community;
import data.cscl.Participant;
import services.commons.Formatting;
import webService.result.ResultEdge;
import webService.result.ResultNode;
import webService.result.ResultTopic;

public class CommunityInteraction {

	public static ResultTopic buildParticipantGraph(Community c, boolean needsAnonymization) {
		List<Participant> participants = new ArrayList<Participant>();
		participants.addAll(c.getParticipants());
		double[][] participantContributions = c.getParticipantContributions();

		DecimalFormat formatter = new DecimalFormat("#.##");

		List<ResultNode> nodes = new ArrayList<ResultNode>();
		List<ResultEdge> links = new ArrayList<ResultEdge>();

		// build all nodes
		for (int i = 0; i < participants.size(); i++) {
			int in = 0;
			for (int j = 0; j < participants.size(); j++) {
				if (participantContributions[j][i] != 0.0)
					in += participantContributions[j][i];
			}
                        if (!needsAnonymization) nodes.add(new ResultNode(i, participants.get(i).getName(), Formatting.formatNumber(in), 1));
                        else nodes.add(new ResultNode(i, "Participant " + (i + 1), Formatting.formatNumber(in), 1));
		}

		// determine max value
		double maxVal = Double.MIN_VALUE;
		for (int i = 0; i < participants.size(); i++) {
			for (int j = 0; j < participants.size(); j++) {
				maxVal = Math.max(maxVal, participantContributions[i][j]);
			}
		}

		for (int i = 0; i < participants.size(); i++) {
			for (int j = 0; j < participants.size(); j++) {
				if (participantContributions[i][j] > 0
				) {
					links.add(new ResultEdge(formatter.format(participantContributions[i][j]), i, j,
							(float) 10 * (participantContributions[i][j] / maxVal)));
				}
			}
		}

		return new ResultTopic(nodes, links);

	}

}
