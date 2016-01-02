package services.commons;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.stat.regression.SimpleRegression;

import cc.mallet.util.Maths;

public class VectorAlgebra {
	public static double[] getVector(int[][] v, int dimension) {
		double[] vect = new double[v.length];
		for (int i = 0; i < v.length; i++)
			vect[i] = v[i][dimension];
		return vect;
	}

	public static double[] movingAverage(double[] v, int window, long[] t, int max) {
		if (window <= 1 || v.length != t.length)
			return v;
		double[] result = new double[v.length];

		for (int i = 0; i < v.length; i++) {
			double sum = v[i];
			INNER_LOOP: for (int j = 1; j < window; j++) {
				if (i + j >= v.length || t[i + j] > max) {
					break INNER_LOOP;
				}
				if (i + j < v.length) {
					sum += v[i + j];
				}
			}
			result[i] = sum / window;
		}
		return result;
	}

	public static double[] normalize(double[] v) {
		double sum = sumElements(v);
		double[] result = new double[v.length];
		if (sum != 0) {
			for (int i = 0; i < v.length; i++) {
				result[i] = v[i] / sum;
			}
		}
		return result;
	}

	public static double[] and(double[] v1, double[] v2) {
		if (v1.length != v2.length)
			return null;
		double[] result = new double[v1.length];

		for (int i = 0; i < v1.length; i++) {
			result[i] = Math.min(v1[i], v2[i]);
		}
		return result;
	}

	public static double cosineSimilarity(double[] v1, double[] v2) {
		if (v1 == null || v2 == null || v1.length != v2.length)
			return 0;
		// compare similarity between two vectors
		double sum = 0, sum1 = 0, sum2 = 0;
		for (int i = 0; i < v1.length; i++) {
			sum += v1[i] * v2[i];
			sum1 += v1[i] * v1[i];
			sum2 += v2[i] * v2[i];
		}
		if (sum1 > 0 && sum2 > 0) {
			sum = sum / (Math.sqrt(sum1) * Math.sqrt(sum2));
		}
		if (sum >= 0 && sum <= 1)
			return sum;
		return 0;
	}

	public static double pearsonCorrelation(double[] v1, double[] v2) {
		if (v1 == null || v2 == null || v1.length != v2.length || v1.length == 0)
			return 0;
		double mean1 = 0, mean2 = 0;
		for (int i = 0; i < v1.length; i++) {
			mean1 += v1[i];
			mean2 += v2[i];
		}
		mean1 /= v1.length;
		mean2 /= v1.length;

		// compare similarity between two vectors
		double sum = 0, sum1 = 0, sum2 = 0;
		for (int i = 0; i < v1.length; i++) {
			sum += (v1[i] - mean1) * (v2[i] - mean2);
			sum1 += (v1[i] - mean1) * (v1[i] - mean1);
			sum2 += (v2[i] - mean2) * (v2[i] - mean2);
		}
		if (sum1 > 0 && sum2 > 0) {
			sum = Double.valueOf(new DecimalFormat("#.###").format(sum / (Math.sqrt(sum1) * Math.sqrt(sum2))));
		}
		return sum;
	}

	public static double precision(double[] v1, double[] v2) {
		if (v1.length != v2.length || v1.length == 0)
			return 0;
		double sum2 = 0, sumMin = 0;
		for (int i = 0; i < v1.length; i++) {
			sum2 += v2[i];
			sumMin += Math.min(v1[i], v2[i]);
		}

		if (sum2 > 0) {
			return Double.valueOf(new DecimalFormat("#.###").format(sumMin / sum2));
		}
		return 0;
	}

	public static double recall(double[] v1, double[] v2) {
		if (v1.length != v2.length || v1.length == 0)
			return 0;
		double sum1 = 0, sumMin = 0;
		for (int i = 0; i < v1.length; i++) {
			sum1 += v1[i];
			sumMin += Math.min(v1[i], v2[i]);
		}

		if (sum1 > 0) {
			return Double.valueOf(new DecimalFormat("#.###").format(sumMin / sum1));
		}
		return 0;
	}

	public static double fscore(double[] v1, double[] v2, double beta) {
		if (v1.length != v2.length || v1.length == 0)
			return 0;
		double sum1 = 0, sum2 = 0, sumMin = 0;
		for (int i = 0; i < v1.length; i++) {
			sum1 += v1[i];
			sum2 += v2[i];
			sumMin += Math.min(v1[i], v2[i]);
		}

		if (sum1 > 0 && sum2 > 0) {
			double precision = sumMin / sum2;
			double recall = sumMin / sum1;
			return Double.valueOf(new DecimalFormat("#.###")
					.format((1 + Math.pow(beta, 2)) * precision * recall / (Math.pow(beta, 2) * precision + recall)));
		}
		return 0;
	}

	public static double dotProduct(double[] v1, double[] v2) {
		if (v1.length != v2.length)
			return 0;
		double sum = 0;
		for (int i = 0; i < v1.length; i++) {
			sum += v1[i] * v2[i];
		}
		return sum;
	}

	public static double[] sum(double[] v1, double[] v2) {
		if (v1.length != v2.length)
			return null;
		double[] result = new double[v1.length];
		for (int i = 0; i < v1.length; i++) {
			result[i] = v1[i] + v2[i];
		}
		return result;
	}

	public static double sumElements(double[] v1) {
		if (v1 == null || v1.length == 0)
			return 0;
		double sum = 0;
		for (int i = 0; i < v1.length; i++) {
			sum += v1[i];
		}
		return sum;
	}

	public static double maxProduct(double[] v1, double[] v2) {
		if (v1.length != v2.length)
			return 0;
		double max = 0;
		for (int i = 0; i < v1.length; i++) {
			max = Math.max(max, v1[i] * v2[i]);
		}
		return max;
	}

	public static double[] scalarProduct(double[] v1, double v2) {
		double[] result = new double[v1.length];
		for (int i = 0; i < v1.length; i++) {
			result[i] = v1[i] * v2;
		}
		return result;
	}

	public static double avg(double[] v) {
		if (v == null)
			return -1;
		double sum = 0;
		for (int i = 0; i < v.length; i++) {
			sum += v[i];
		}
		if (v.length != 0)
			return sum / v.length;
		return 0;
	}

	public static double[] getRecurrence(double[] v) {
		List<Integer> recurrence = new ArrayList<Integer>();
		int crtIndex = -1;
		for (int i = 0; i < v.length; i++) {
			if (v[i] > 0) {
				recurrence.add(i - crtIndex - 1);
				crtIndex = i;
			}
		}
		if (crtIndex == -1)
			recurrence.add(v.length - 1);
		else
			recurrence.add(v.length - crtIndex - 1);

		double[] results = new double[recurrence.size()];
		for (int i = 0; i < recurrence.size(); i++)
			results[i] = recurrence.get(i);
		return results;

	}

	public static double slope(double[] v) {
		SimpleRegression r = new SimpleRegression(false);
		for (int i = 0; i < v.length; i++)
			r.addData(i, v[i]);
		return r.getSlope();
	}

	/**
	 * Computes the p-value of a one-sample Kolmogorov-Smirnov test evaluating
	 * the null hypothesis that v conforms to a uniform distribution of
	 * 1/length(v).
	 * 
	 * @param v
	 * @return
	 */
	public static double uniformity(double[] v) {
		if (v.length == 0)
			return 1;
		double[] uniformDistribution = new double[v.length];
		for (int i = 0; i < v.length; i++)
			uniformDistribution[i] = 1d / v.length;
		// KolmogorovSmirnovTest test = new KolmogorovSmirnovTest();
		// return test.kolmogorovSmirnovTest(VectorAlgebra.normalize(v),
		// uniformDistribution);
		return Maths.jensenShannonDivergence(normalize(v), uniformDistribution);
	}

	public static int localExtremeDetection(double[] v) {
		List<Integer> ext = new ArrayList<Integer>();
		for (int i = 0; i < v.length - 2; i++) {
			// check for sign change
			if ((!(v[i + 1] == v[i] && v[i + 2] == v[i + 1])) && (v[i + 1] - v[i]) * (v[i + 2] - v[i + 1]) <= 0) {
				ext.add(i + 1);
			}
		}
		return ext.size();
	}

	public static double entropy(double[] v) {
		if (v == null)
			return -1;
		double entropy = 0;
		double[] p = normalize(v);
		for (int i = 0; i < p.length; i++) {
			if (p[i] != 0) {
				entropy += -p[i] * Math.log(p[i]);
			}
		}
		return entropy;
	}

	public static double mutualInformation(double[] v1, double[] v2) {
		double entropy = 0;
		double[] and = and(v1, v2);
		double[] v1_normalized = normalize(v1);
		double[] v2_normalized = normalize(v2);
		double[] and_normalized = normalize(and);
		for (int i = 0; i < v1.length; i++) {
			if (and_normalized[i] != 0) {
				entropy += and_normalized[i] * Math.log(and_normalized[i] / (v1_normalized[i] * v2_normalized[i]));
			}
		}
		return Double.valueOf(new DecimalFormat("#.###").format(entropy));
	}

	public static double[] discreteMutualInformation(double[] v1, double[] v2) {
		if (v1.length != v2.length)
			return null;
		double[] result = new double[v1.length];
		double[] and = and(v1, v2);
		double[] v1_normalized = normalize(v1);
		double[] v2_normalized = normalize(v2);
		double[] and_normalized = normalize(and);
		for (int i = 0; i < v1.length; i++) {
			if (v1[i] != 0 && v2[i] != 0) {
				if (and_normalized[i] != 0)
					result[i] = Math.log(and_normalized[i] / (v1_normalized[i] * v2_normalized[i]));
				// result[i] = Math.min(v1[i], v2[i]) / (v1[i] * v2[i]);
			}
		}
		return result;
	}

	public static double stdev(double[] v1) {
		if (v1 == null)
			return -1;
		double s0 = 0, s1 = 0, s2 = 0;
		for (int i = 0; i < v1.length; i++) {
			s0++;
			s1 += v1[i];
			s2 += Math.pow(v1[i], 2);
		}
		if (s0 != 0) {
			return Math.sqrt(s0 * s2 - Math.pow(s1, 2)) / s0;
		}
		return 0;
	}

	public static void main(String[] args) {
		double[] v = { 0.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0 };
		double[] v1 = { 0, 0, 1, 1, 1, 1, 1, 1 };
		double[] v2 = { 0, 0, 0, 0, 0, 0, 1, 1 };

		System.out.println(cosineSimilarity(v1, v2));
		long[] t = { 2, 2, 2, 2, 2, 1, 2, 2 };
		for (double d : VectorAlgebra.movingAverage(v, 2, t, 2))
			System.out.print(d + " ");
		System.out.println(entropy(VectorAlgebra.movingAverage(v, 2, t, 2)));
		for (double d : VectorAlgebra.movingAverage(v, 2, t, 1))
			System.out.print(d + " ");
		System.out.println(entropy(VectorAlgebra.movingAverage(v, 2, t, 1)));
		System.out.println(mutualInformation(v, v));
		System.out.println(entropy(normalize(v)));
	}
}
