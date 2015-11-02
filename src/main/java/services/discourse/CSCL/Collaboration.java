package services.discourse.CSCL;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

import org.apache.log4j.Logger;

import services.commons.Formatting;
import DAO.Block;
import DAO.cscl.Conversation;
import DAO.cscl.Utterance;
import DAO.discourse.CollaborationZone;
import DAO.discourse.SemanticCohesion;

/**
 * 
 * @author Mihai Dascalu
 */
// class responsible for evaluating collaboration
public class Collaboration {
	static Logger logger = Logger.getLogger(Collaboration.class);
	public static final double COLLABORATION_ZONE_SLACK = 0.025;
	public static final int COLLABORATION_ZONE_MIN_SPREAD = 5;
	public static double BETA_FSCORE = 1;
	public static double COLLABORATION_SENTIMENT_SLACK = 0.5;
	public static int INVALID_SENTIMENT_VALUE = -100;

	CollaborationZones collaborations;

	private static double[][] computeSentimentDistances(
			double[] sentimentDistribution) {
		// Calculates the sentiment distances between adjacent elements from the
		// vector; Value 0 = distance is under the threshold; 1 = above
		System.out
				.println("Collaboration::getCollaborationZones computeSentimentDistances");
		int size = sentimentDistribution.length;
		double[][] sentimentDistances = new double[size][2];
		double distance_left = sentimentDistribution[size - 1]
				- sentimentDistribution[size - 2];
		double distance_right = sentimentDistribution[1]
				- sentimentDistribution[0];
		sentimentDistances[0][0] = INVALID_SENTIMENT_VALUE;
		if ((sentimentDistribution[0] == INVALID_SENTIMENT_VALUE && sentimentDistribution[1] == INVALID_SENTIMENT_VALUE)
				|| distance_right > COLLABORATION_SENTIMENT_SLACK) {
			sentimentDistances[0][1] = 1;
		} else {
			sentimentDistances[0][1] = 0;
		}
		sentimentDistances[size - 1][1] = INVALID_SENTIMENT_VALUE;
		if ((sentimentDistribution[size - 1] == INVALID_SENTIMENT_VALUE && sentimentDistribution[size - 2] == INVALID_SENTIMENT_VALUE)
				|| distance_left > COLLABORATION_SENTIMENT_SLACK) {
			sentimentDistances[size - 1][0] = 1;
		} else {
			sentimentDistances[size - 1][0] = 0;
		}
		for (int i = 1; i < size - 1; i++) {
			distance_left = sentimentDistribution[i]
					- sentimentDistribution[i - 1];
			distance_right = sentimentDistribution[i + 1]
					- sentimentDistribution[i];
			if ((sentimentDistribution[i] == INVALID_SENTIMENT_VALUE && sentimentDistribution[i + 1] == INVALID_SENTIMENT_VALUE)
					|| distance_right > COLLABORATION_SENTIMENT_SLACK) {
				sentimentDistances[i][1] = 1;
			} else {
				sentimentDistances[i][1] = 0;
			}

			if ((sentimentDistribution[i - 1] == INVALID_SENTIMENT_VALUE && sentimentDistribution[i] == INVALID_SENTIMENT_VALUE)
					|| distance_left > COLLABORATION_SENTIMENT_SLACK) {
				sentimentDistances[i][0] = 1;
			} else {
				sentimentDistances[i][0] = 0;
			}

		}
		return sentimentDistances;
	}

	public static List<CollaborationZone> getCollaborationZones(
			double[] distribution, Conversation chat) {
		CollaborationZones collaborations = new CollaborationZones(chat,
				distribution);
		// generic method for determining intense collaboration zones
		int k = 0;
		Vector<Block> blocks = chat.getBlocks();
		int size = blocks.size();
		double[] sentimentDistribution = new double[size];

		Iterator<Block> it = blocks.iterator();
		while (it.hasNext()) {
			Block b = it.next();
			if (b == null) {
				sentimentDistribution[k] = INVALID_SENTIMENT_VALUE;
				k++;
			}
			if (b != null) {
				sentimentDistribution[k] = b.getSentimentEntity()
						.getAggregatedValue() - 2;
				k++;
			}
		}

		double[][] sentimentDistances = computeSentimentDistances(sentimentDistribution);

		for (k = 0; k < size; k++) {
			System.out
					.println("Collaboration::getCollaborationZones sentimentDistribution inside for iteration sentimentDistribution "
							+ k
							+ " : "
							+ +sentimentDistribution[k]
							+ " distance left: "
							+ sentimentDistances[k][0]
							+ " distance right: " + sentimentDistances[k][1]);
		}

		int maxDistance = Math.max(1,
				(int) (COLLABORATION_ZONE_SLACK * distribution.length));
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
			System.out
					.println("Collaboration::getCollaborationZones indexmax: "
							+ indexMax + " valuemax: " + valueMax);

			if (valueMax < avgCollaboration)
				break;

			if (indexMax != -1) {
				visited[indexMax] = true;

				// expand left
				int left = indexMax;
				while (true) {
					boolean expand = false;
					for (int i = 1; i <= maxDistance; i++)
						if (left - i >= 0
								&& distribution[left - i] >= avgCollaboration) {
							System.out
									.println("Collaboration::getCollaborationZones expand left!");
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
						if (right + i < distribution.length
								&& distribution[right + i] >= avgCollaboration) {
							System.out
									.println("Collaboration::getCollaborationZones expand right!");
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
				System.out
						.println("Collaboration::getCollaborationZones noBlocks: "
								+ noBlocks);
				double sumValueUtterances = 0;
				for (int i = left; i <= right; i++) {
					sumValueUtterances += distribution[i];
				}
				System.out
						.println("Collaboration::getCollaborationZones sumValueUtterances:"
								+ sumValueUtterances);
				if (noBlocks != 0)
					sumValueUtterances /= noBlocks;
				System.out
						.println("Collaboration::getCollaborationZones sumValueUtterances after if:"
								+ sumValueUtterances);
				if (noBlocks >= COLLABORATION_ZONE_MIN_SPREAD) {
					System.out
							.println("Collaboration::getCollaborationZones initial collaboration zone: "
									+ left + " - " + right);
					int newLeft = left, newRight = left;
					int newBlocks = 0;
					for (int i = left; i <= right; i++) {
						if (i == left
								&& sentimentDistances[i][1] <= COLLABORATION_SENTIMENT_SLACK) {
							System.out
									.println("Collaboration::getCollaborationZones collaboration zone ok: "
											+ newLeft + " --- " + newRight);
							newRight++;
							newBlocks++;
						} else if ((sentimentDistances[i][0] <= COLLABORATION_SENTIMENT_SLACK && sentimentDistances[i][1] <= COLLABORATION_SENTIMENT_SLACK)
								&& (i != right)) {
							System.out
									.println("Collaboration::getCollaborationZones collaboration zone ok: "
											+ newLeft + " --- " + newRight);
							newRight++;
							newBlocks++;
						} else {
							System.out
									.println("Collaboration::getCollaborationZones new collaboration zone: "
											+ newLeft
											+ " --- "
											+ newRight
											+ " nr blocks: " + newBlocks);
							if (newBlocks >= COLLABORATION_ZONE_MIN_SPREAD) {
								System.out
										.println("Collaboration::getCollaborationZones new collaboration zone added ");
								zones.add(new CollaborationZone(
										newLeft, newRight, sumValueUtterances,
										newBlocks));
							}
							newLeft = i;
							newRight = i;
							newBlocks = 0;
						}
					}
					// zones.add(new CollaborationZone(left, right,
					// sumValueUtterances, noBlocks));
				}
			} else
				break;
		}

		Collections.sort(zones);
		for (CollaborationZone z : zones) {
			if (z != null) {
				System.out
						.println("Collaboration::getCollaborationZones zone not null "
								+ z.getStart() + " -- " + z.getEnd());
			} else {
				System.out
						.println("Collaboration::getCollaborationZones zone is null");
			}
		}
		System.out.println("Collaboration::getCollaborationZones size: "
				+ zones.size());
		collaborations.setCollaborations(zones);
		return zones;
	}

	public static List<CollaborationZone> getCollaborationZones(
			double[] distribution) {
		// generic method for determining intense collaboration zones
		int maxDistance = Math.max(1,
				(int) (COLLABORATION_ZONE_SLACK * distribution.length));
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
						if (left - i >= 0
								&& distribution[left - i] >= avgCollaboration) {
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
						if (right + i < distribution.length
								&& distribution[right + i] >= avgCollaboration) {
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
					zones.add(new CollaborationZone(left, right,
							sumValueUtterances, noBlocks));
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
					u.setKB(u.getPersonalKB() + u.getSocialKB());
				}
			}

			// use pruned inter-block graph
			for (int i = 0; i < c.getBlocks().size() - 1; i++) {
				for (int j = i + 1; j < c.getBlocks().size(); j++) {
					if (c.getPrunnedBlockDistances()[j][i] != null) {
						Utterance u1 = (Utterance) c.getBlocks().get(i);
						Utterance u2 = (Utterance) c.getBlocks().get(j);
						SemanticCohesion coh = c.getPrunnedBlockDistances()[j][i];
						if (u1.getParticipant() != null
								&& u2.getParticipant() != null
								&& coh.getCohesion() > 0) {
							if (u1.getParticipant().equals(u2.getParticipant())) {
								u1.setPersonalKB(u1.getPersonalKB()
										+ u2.getOverallScore()
										* coh.getCohesion());
								u2.setPersonalKB(u2.getPersonalKB()
										+ u1.getOverallScore()
										* coh.getCohesion());
							} else {
								u1.setSocialKB(u1.getSocialKB()
										+ u2.getOverallScore()
										* coh.getCohesion());
								u2.setSocialKB(u2.getSocialKB()
										+ u1.getOverallScore()
										* coh.getCohesion());
								no_diff_speaker++;
							}
							no_links++;
							u1.setKB(u1.getPersonalKB() + u1.getSocialKB());
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
					socialKBEvolution[i] = ((Utterance) c.getBlocks().get(i))
							.getSocialKB();
				}
			}
			c.setSocialKBEvolution(socialKBEvolution);
			c.setIntenseCollabZonesSocialKB(getCollaborationZones(socialKBEvolution));
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public static double[] overlapCollaborationZones(Conversation c,
			List<CollaborationZone> l1, List<CollaborationZone> l2) {
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

			return new double[] { Formatting.formatNumber(precision),
					Formatting.formatNumber(recall),
					Formatting.formatNumber(fscore) };
		}
		return new double[3];
	}
}
