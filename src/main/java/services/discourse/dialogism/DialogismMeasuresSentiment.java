package services.discourse.dialogism;

import java.util.List;

import org.apache.log4j.Logger;

import services.commons.VectorAlgebra;
import DAO.discourse.SemanticChain;

public class DialogismMeasuresSentiment {
	static Logger logger = Logger.getLogger(DialogismMeasuresSentiment.class);

	public static double[][] getSentimentCorrelationMatrix(List<SemanticChain> voices) {
		double[][] correlations = new double[voices.size()][voices.size()];
		for (int i = 0; i < voices.size(); i++) {
			for (int j = 0; j < i; j++) {
				correlations[i][j] = VectorAlgebra.pearsonCorrelation(voices.get(i).getSentimentDistribution(),
						voices.get(j).getSentimentDistribution());
			}
		}
		return correlations;
	}

	public static double[][] getSentimentMovingAverageCorrelationMatrix(List<SemanticChain> voices) {
		double[][] correlations = new double[voices.size()][voices.size()];
		for (int i = 0; i < voices.size(); i++) {
			for (int j = 0; j < i; j++) {
				correlations[i][j] = VectorAlgebra.pearsonCorrelation(voices.get(i).getBlockMovingAverage(),
						voices.get(j).getBlockMovingAverage());
			}
		}

		for (int i = 0; i < correlations.length; i++) {
			for (int j = 0; j < correlations[i].length; j++) {
				double sentiment = voices.get(i).getSentimentAverage();
				if (sentiment != 0) {
					correlations[i][j] *= sentiment;
				}
			}
		}
		return correlations;
	}

	public static double[][] getSentimentMutualInformationMatrix(List<SemanticChain> voices) {
		double[][] correlations = new double[voices.size()][voices.size()];

		for (int i = 0; i < voices.size(); i++) {
			for (int j = 0; j < i; j++) {
				correlations[i][j] = VectorAlgebra.mutualInformation(voices.get(i).getSentenceDistribution(),
						voices.get(j).getSentenceDistribution());
			}
		}
		return correlations;
	}

	public static double[] getAverageSentimentMutualInformationEvolution(List<SemanticChain> voices) {
		if (voices == null || voices.size() == 0)
			return null;
		double[] evolution = new double[voices.get(0).getBlockMovingAverage().length];
		int no = 0;
		for (int i = 1; i < voices.size(); i++) {
			for (int j = 0; j < i; j++) {
				double[] mi = VectorAlgebra.discreteMutualInformation(voices.get(i).getBlockMovingAverage(),
						voices.get(j).getBlockMovingAverage());
				for (int k = 0; k < evolution.length; k++) {
					evolution[k] += mi[k];
				}
				no++;
			}
		}

		if (no > 0) {
			for (int k = 0; k < evolution.length; k++) {
				evolution[k] /= no;
				for (SemanticChain s : voices) {
					if (s.getSentimentAverage() != 0)
						evolution[k] *= s.getSentimentAverage();
				}
			}
			return evolution;
		}
		return null;
	}
}