package runtime.optimalWeights;

import java.text.DecimalFormat;
import java.util.List;

import services.commons.Formatting;

/**
 * @author mihai.dascalu
 */
public class Chromosome implements Comparable<Chromosome> {

	public static enum DFAIndices {
		CONSTANT, WCMC, LSA_CS, LSA_PT, LSA_TITLE, W_IMPT, W_PRIOR, W_LATER, W_NEW;
	};

	public static final double[][] DFA_COEFF = {
			{ -16.427, 13.783, 11.251, 4.348, 3.070, -2.079, -1.028, -0.910, -1.501 },
			{ -18.608, 15.380, 10.376, 5.624, 3.729, -2.849, -0.763, -0.874, -1.378 },
			{ -25.059, 16.649, 11.031, 4.261, 6.283, -2.975, 0.177, -0.261, -1.057 } };

	public static final int DIMENSION = DFAIndices.values().length;

	public static final double MIN_THRESHOLD = -(17.15413029 / 13.61344538 - 1);

	public static final double MAX_THRESHOLD = -MIN_THRESHOLD;

	private double[] coefficients = new double[DIMENSION * 3];

	private double fitness;

	private double adjacentAccuracy;

	public void initialise_random() {
		for (int i = 0; i < coefficients.length; i++)
			coefficients[i] = Math.random() * (MAX_THRESHOLD - MIN_THRESHOLD) + MIN_THRESHOLD;
	}

	public double[] getCoefficients() {
		return coefficients;
	}

	public double[][] get3XCoefficients() {
		double[][] values = new double[3][DIMENSION];
		for (int i = 0; i < 3; i++)
			for (int j = 0; j < DIMENSION; j++)
				values[i][j] = coefficients[i * DIMENSION + j];
		return values;
	}

	public double fitness(List<double[]> x, List<Integer> d) {
		fitness = 0;
		adjacentAccuracy = 0;

		int no = 0;
		double[][] coefs = get3XCoefficients();
		for (double[] entry : x) {
			double[] pred = new double[3];
			for (int i = 0; i < 3; i++) {
				pred[i] = 0;
				for (int j = 0; j < DIMENSION; j++)
					pred[i] += entry[j] * DFA_COEFF[i][j] * (1 + coefs[i][j]);
			}
			double max = Double.MIN_VALUE;
			int PSS = -1;
			for (int i = 0; i < pred.length; i++)
				if (max < pred[i]) {
					max = pred[i];
					PSS = i;
				}
			if ((PSS + 1) == d.get(no))
				fitness++;
			if (Math.abs(d.get(no) - PSS - 1) <= 1)
				adjacentAccuracy++;
			no++;
		}
		if (no != 0) {
			fitness /= no;
			adjacentAccuracy /= no;
		}
		return fitness;
	}

	public String toString() {
		String s = "";
		double[][] coefs = get3XCoefficients();
		DecimalFormat formatter = new DecimalFormat("#.###");
		for (int i = 0; i < 3; i++) {
			s += "pred" + (i + 1) + ":\t";
			for (int j = 0; j < DIMENSION; j++)
				s += formatter.format((coefs[i][j]) * 100) + "% ";
			s += "\n";
		}
		s += "\tFitness: " + Formatting.formatNumber(fitness) + "; Adjacent accuracy: "
				+ Formatting.formatNumber(adjacentAccuracy);
		return s;
	}

	public boolean equals(Object obj) {
		Chromosome indiv = (Chromosome) obj;
		if (indiv.getFitness() != this.getFitness())
			return false;
		for (int i = 0; i < coefficients.length; i++)
			if (coefficients[i] != indiv.getCoefficients()[i])
				return false;
		return true;
	}

	public int compareTo(Chromosome o) {
		return (int) (Math.signum(o.getFitness() - this.getFitness()));
	}

	public double distanceFrom(Chromosome o) {
		double sum = 0;
		for (int i = 0; i < coefficients.length; i++)
			sum += Math.pow((this.getCoefficients()[i] - o.getCoefficients()[i]) / (MAX_THRESHOLD - MIN_THRESHOLD), 2);
		return 1d / coefficients.length * Math.sqrt(sum);
	}

	public double getFitness() {
		return fitness;
	}

	public void setFitness(double fitness) {
		this.fitness = fitness;
	}
}
