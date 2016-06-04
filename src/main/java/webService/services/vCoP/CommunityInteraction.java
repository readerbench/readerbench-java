package webService.services.vCoP;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import data.AbstractDocument.SaveType;
import data.cscl.Community;
import data.cscl.Conversation;
import data.cscl.Participant;
import services.commons.Formatting;
import webService.result.ResultEdge;
import webService.result.ResultNode;
import webService.result.ResultTopic;

public class CommunityInteraction {

	public static ResultTopic buildParticipantGraph(Community c) {
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
			nodes.add(new ResultNode(i, participants.get(i).getName(), Formatting.formatNumber(in), 1));
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
