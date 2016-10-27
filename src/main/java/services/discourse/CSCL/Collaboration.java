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
package services.discourse.CSCL;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;



import data.Block;
import data.cscl.CollaborationZone;
import data.cscl.Conversation;
import data.cscl.Utterance;
import data.discourse.SemanticCohesion;
import java.util.logging.Logger;
import services.commons.Formatting;

/**
 * 
 * @author Mihai Dascalu
 */
// class responsible for evaluating collaboration
public class Collaboration {
	static Logger logger = Logger.getLogger("");
	public static final double COLLABORATION_ZONE_SLACK = 0.025;
	public static final int COLLABORATION_ZONE_MIN_SPREAD = 5;
	public static double BETA_FSCORE = 1;
	public static double COLLABORATION_SENTIMENT_SLACK = 0.5;
	public static int INVALID_SENTIMENT_VALUE = -100;

	CollaborationZones collaborations;

	public static List<CollaborationZone> getCollaborationZones(double[] distribution) {
		// generic method for determining intense collaboration zones
		int maxDistance = Math.max(1, (int) (COLLABORATION_ZONE_SLACK * distribution.length));
		double sumCollaboration = 0;
		int noCollaboration = 0;
		double avgCollaboration = 0;
		// determine average collaborative gain
		for (int i = 0; i < distribution.length; i++) {
			sumCollaboration += distribution[i];
			noCollaboration++;
		}

		if (noCollaboration != 0)
			avgCollaboration = sumCollaboration / noCollaboration;

		List<CollaborationZone> zones = new LinkedList<CollaborationZone>();

		boolean[] visited = new boolean[distribution.length];
		int indexMax;
		double valueMax;
		while (true) {
			indexMax = -1;
			valueMax = Double.MIN_VALUE;
			// determine maximum
			for (int i = 0; i < distribution.length; i++) {
				if (!visited[i]) {
					if (distribution[i] > valueMax) {
						valueMax = distribution[i];
						indexMax = i;
					}
				}
			}

			if (valueMax < avgCollaboration)
				break;

			if (indexMax != -1) {
				visited[indexMax] = true;

				// expand left
				int left = indexMax;
				while (true) {
					boolean expand = false;
					for (int i = 1; i <= maxDistance; i++)
						if (left - i >= 0 && distribution[left - i] >= avgCollaboration) {
							left = left - i;
							visited[left] = true;
							expand = true;
							break;
						}
					if (!expand)
						break;
				}

				// expand right
				int right = indexMax;
				while (true) {
					boolean expand = false;
					for (int i = 1; i <= maxDistance; i++)
						if (right + i < distribution.length && distribution[right + i] >= avgCollaboration) {
							right = right + i;
							visited[right] = true;
							expand = true;
							break;
						}
					if (!expand)
						break;
				}

				// classify intense collaboration zone
				int noBlocks = right - left + 1;
				double sumValueUtterances = 0;
				for (int i = left; i <= right; i++) {
					sumValueUtterances += distribution[i];
				}
				if (noBlocks != 0)
					sumValueUtterances /= noBlocks;
				if (noBlocks >= COLLABORATION_ZONE_MIN_SPREAD)
					zones.add(new CollaborationZone(left, right, sumValueUtterances, noBlocks));
			} else
				break;
		}
		Collections.sort(zones);
		return zones;
	}

	public static void evaluateSocialKB(Conversation c) {
		try {
			logger.info("Computing collaboration zones");
			double no_diff_speaker = 0;
			double no_links = 0;

			for (Block b : c.getBlocks()) {
				if (b != null) {
					Utterance u = (Utterance) b;
					u.setPersonalKB(b.getOverallScore());
					u.setSocialKB(0);
				}
			}

			// use pruned inter-block graph
			for (int i = 0; i < c.getBlocks().size(); i++) {
				for (int j = 0; j < i; j++) {
					if (c.getPrunnedBlockDistances()[j][i] != null) {
						Utterance u1 = (Utterance) c.getBlocks().get(j);
						Utterance u2 = (Utterance) c.getBlocks().get(i);
						SemanticCohesion coh = c.getPrunnedBlockDistances()[i][j];
						if (u1.getParticipant() != null && u2.getParticipant() != null && coh.getCohesion() > 0) {
							if (u1.getParticipant().equals(u2.getParticipant())) {
								u2.setPersonalKB(u2.getPersonalKB() + u2.getIndividualScore() * coh.getCohesion());
							} else {
								u2.setSocialKB(u2.getSocialKB() + u2.getIndividualScore() * coh.getCohesion());
 								no_diff_speaker++;
							}
							no_links++;
							u2.setKB(u2.getPersonalKB() + u2.getSocialKB());
						}
					}
				}
			}

			// determine collaboration factors
			double collaboration = 0;
			double socialKB = 0;
			double scores = 0;
			double overallKB = 0;

			for (Block b : c.getBlocks()) {
				if (b != null) {
					Utterance u = (Utterance) b;
					socialKB += u.getSocialKB();
					scores += u.getOverallScore();
					overallKB += u.getKB();
				}
			}

			if (overallKB != 0)
				collaboration = socialKB / overallKB;

			c.setSocialKBPercentage(collaboration);

			collaboration = 0;

			if (scores != 0)
				collaboration = socialKB / scores;

			c.setSocialKBvsScore(collaboration);

			collaboration = 0;

			if (no_links != 0)
				collaboration = no_diff_speaker / no_links;

			c.setQuantCollabPercentage(collaboration);

			// determine intense collaboration zones
			double[] socialKBEvolution = new double[c.getBlocks().size()];
			for (int i = 0; i < c.getBlocks().size(); i++) {
				if (c.getBlocks().get(i) != null) {
					socialKBEvolution[i] = ((Utterance) c.getBlocks().get(i)).getSocialKB();
				}
			}
			c.setSocialKBEvolution(socialKBEvolution);
			c.setIntenseCollabZonesSocialKB(getCollaborationZones(socialKBEvolution));
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public static double[] overlapCollaborationZones(Conversation c, List<CollaborationZone> l1,
			List<CollaborationZone> l2) {
		// evaluate precision and recall for identified collaboration zones
		double precision = 0, recall = 0, fscore = 0;
		if (l1 != null && l2 != null && l1.size() > 0 && l2.size() > 0) {
			// initialization
			int[] zones = new int[c.getBlocks().size()];
			for (int i = 0; i < zones.length; i++)
				if (c.getBlocks().get(i) == null)
					zones[i] = -1;

			// add annotated collaboration zones
			for (CollaborationZone zone : l1) {
				for (int j = zone.getStart(); j <= zone.getEnd(); j++)
					if (zones[j] != -1)
						zones[j] = 1;
			}

			// overlap with identified intense collaboration zones
			for (CollaborationZone zone : l2) {
				for (int j = zone.getStart(); j <= zone.getEnd(); j++)
					if (zones[j] != -1)
						zones[j] += 2;
			}
			int relevant = 0;
			int retrievedUtterances = 0;
			int relevantUtterances = 0;

			for (int i = 0; i < zones.length; i++)
				if (zones[i] != -1) {
					switch (zones[i]) {
					case 1:
						relevantUtterances++;
						break;
					case 2:
						retrievedUtterances++;
						break;
					case 3:
						relevant++;
						retrievedUtterances++;
						relevantUtterances++;
						break;
					}
				}
			if (retrievedUtterances != 0)
				precision = ((double) (relevant)) / retrievedUtterances;
			if (relevantUtterances != 0)
				recall = ((double) (relevant)) / relevantUtterances;
			if (!(precision == 0 && recall == 0))
				fscore = (1 + Math.pow(BETA_FSCORE, 2)) * (precision * recall)
						/ (Math.pow(BETA_FSCORE, 2) * precision + recall);

			return new double[] { Formatting.formatNumber(precision), Formatting.formatNumber(recall),
					Formatting.formatNumber(fscore) };
		}
		return new double[3];
	}
}
