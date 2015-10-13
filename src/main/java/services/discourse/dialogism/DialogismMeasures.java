package services.discourse.dialogism;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

import services.commons.VectorAlgebra;
import services.discourse.CSCL.Collaboration;
import DAO.chat.Chat;
import DAO.chat.Participant;
import DAO.discourse.SemanticChain;

public class DialogismMeasures {
	static Logger logger = Logger.getLogger(DialogismMeasures.class);

	public static double[][] getSentenceCorrelationMatrix(
			List<SemanticChain> voices) {
		double[][] correlations = new double[voices.size()][voices.size()];

		for (int i = 0; i < voices.size(); i++) {
			for (int j = 0; j < i; j++) {
				correlations[i][j] = VectorAlgebra.pearsonCorrelation(voices
						.get(i).getSentenceDistribution(), voices.get(j)
						.getSentenceDistribution());
			}
		}
		return correlations;
	}

	public static double[][] getBlockCorrelationMatrix(
			List<SemanticChain> voices) {
		double[][] correlations = new double[voices.size()][voices.size()];

		for (int i = 0; i < voices.size(); i++) {
			for (int j = 0; j < i; j++) {
				correlations[i][j] = VectorAlgebra.pearsonCorrelation(voices
						.get(i).getBlockDistribution(), voices.get(j)
						.getBlockDistribution());
			}
		}
		return correlations;
	}

	public static double[][] getMovingAverageCorrelationMatrix(
			List<SemanticChain> voices) {
		double[][] correlations = new double[voices.size()][voices.size()];

		for (int i = 0; i < voices.size(); i++) {
			for (int j = 0; j < i; j++) {
				correlations[i][j] = VectorAlgebra.pearsonCorrelation(voices
						.get(i).getBlockMovingAverage(), voices.get(j)
						.getBlockMovingAverage());
			}
		}
		return correlations;
	}

	public static double[][] getSentenceMutualInformationMatrix(
			List<SemanticChain> voices) {
		double[][] correlations = new double[voices.size()][voices.size()];

		for (int i = 0; i < voices.size(); i++) {
			for (int j = 0; j < i; j++) {
				correlations[i][j] = VectorAlgebra.mutualInformation(voices
						.get(j).getSentenceDistribution(), voices.get(j)
						.getSentenceDistribution());
			}
		}
		return correlations;
	}

	public static double[][] getBlockMutualInformationMatrix(
			List<SemanticChain> voices) {
		double[][] correlations = new double[voices.size()][voices.size()];

		for (int i = 0; i < voices.size(); i++) {
			for (int j = 0; j < i; j++) {
				correlations[i][j] = VectorAlgebra.mutualInformation(voices
						.get(i).getBlockMovingAverage(), voices.get(j)
						.getBlockMovingAverage());
			}
		}
		return correlations;
	}

	public static double[] getAverageBlockMutualInformationEvolution(
			List<SemanticChain> voices) {
		if (voices == null || voices.size() == 0)
			return null;
		double[] evolution = new double[voices.get(0).getBlockMovingAverage().length];

		int no = 0;
		for (int i = 1; i < voices.size(); i++) {
			for (int j = 0; j < i; j++) {
				double[] mi = VectorAlgebra.discreteMutualInformation(voices
						.get(i).getBlockMovingAverage(), voices.get(j)
						.getBlockMovingAverage());
				for (int k = 0; k < evolution.length; k++) {
					evolution[k] += mi[k];
				}
				no++;
			}
		}

		if (no > 0) {
			for (int k = 0; k < evolution.length; k++) {
				evolution[k] /= no;
			}
			return evolution;
		}
		return null;
	}

	// sentence level
	public static double[] getAverageSentenceMutualInformationEvolution(
			List<SemanticChain> voices) {
		if (voices == null || voices.size() == 0)
			return null;
		double[] evolution = new double[voices.get(0).getSentenceDistribution().length];

		int no = 0;
		for (int i = 1; i < voices.size(); i++) {
			for (int j = 0; j < i; j++) {
				double[] mi = VectorAlgebra.discreteMutualInformation(voices
						.get(i).getSentenceDistribution(), voices.get(j)
						.getSentenceDistribution());
				for (int k = 0; k < evolution.length; k++) {
					evolution[k] += mi[k];
				}
				no++;
			}
		}

		if (no > 0) {
			for (int k = 0; k < evolution.length; k++) {
				evolution[k] /= no;
			}
			return evolution;
		}
		return null;
	}

	public static double[] getCoOccurrenceBlockEvolution(
			List<SemanticChain> voices) {
		if (voices == null || voices.size() == 0)
			return null;
		double[] evolution = new double[voices.get(0).getBlockDistribution().length];

		for (int k = 0; k < evolution.length; k++) {
			for (int i = 0; i < voices.size(); i++) {
				evolution[k] += voices.get(i).getBlockDistribution()[k] > 0 ? 1
						: 0;
			}
		}

		return evolution;
	}

	// sentence level
	public static double[] getCoOccurrenceSentenceEvolution(
			List<SemanticChain> voices) {
		if (voices == null || voices.size() == 0)
			return null;
		double[] evolution = new double[voices.get(0).getSentenceDistribution().length];

		for (int k = 0; k < evolution.length; k++) {
			for (int i = 0; i < voices.size(); i++) {
				evolution[k] += voices.get(i).getSentenceDistribution()[k] > 0 ? 1
						: 0;
			}
		}

		return evolution;
	}

	public static double[] getCumulativeBlockMuvingAverageEvolution(
			List<SemanticChain> voices) {
		if (voices == null || voices.size() == 0)
			return null;
		double[] evolution = new double[voices.get(0).getBlockMovingAverage().length];

		for (int k = 0; k < evolution.length; k++) {
			for (int i = 0; i < voices.size(); i++) {
				evolution[k] += voices.get(i).getBlockMovingAverage()[k];
			}
		}

		return evolution;
	}

	// sentence level
	public static double[] getCumulativeSentenceEvolution(
			List<SemanticChain> voices) {
		if (voices == null || voices.size() == 0)
			return null;
		double[] evolution = new double[voices.get(0).getSentenceDistribution().length];

		for (int k = 0; k < evolution.length; k++) {
			for (int i = 0; i < voices.size(); i++) {
				evolution[k] += voices.get(i).getSentenceDistribution()[k];
			}
		}

		return evolution;
	}

	public static double[] getCollaborationEvolution(Chat c) {
		if (c.getVoices() == null || c.getVoices().size() == 0)
			return null;
		double[] evolution = new double[c.getVoices().get(0)
				.getBlockMovingAverage().length];

		Iterator<Participant> it = c.getParticipants().iterator();
		List<Participant> lsPart = new ArrayList<Participant>();
		while (it.hasNext()) {
			Participant part = it.next();
			lsPart.add(part);
		}

		// take all voices
		for (int i = 0; i < c.getVoices().size(); i++) {
			// for different participants build collaboration based on
			// inter-twined voices
			for (int p1 = 0; p1 < lsPart.size() - 1; p1++) {
				for (int p2 = p1 + 1; p2 < lsPart.size(); p2++) {
					double[] ditrib1 = c.getParticipantBlockMovingAverage(c
							.getVoices().get(i), lsPart.get(p1));
					double[] ditrib2 = c.getParticipantBlockMovingAverage(c
							.getVoices().get(i), lsPart.get(p2));
					double[] mi = VectorAlgebra.discreteMutualInformation(
							ditrib1, ditrib2);
					for (int j = 0; j < evolution.length; j++) {
						evolution[j] += mi[j];
					}
				}
			}
		}
		c.setIntenseCollabZonesVoice(Collaboration
				.getCollaborationZones(evolution));

		return evolution;
	}
}
