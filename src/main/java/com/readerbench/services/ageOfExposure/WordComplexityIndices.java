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
package com.readerbench.services.ageOfExposure;

import org.apache.commons.math3.fitting.PolynomialCurveFitter;
import org.apache.commons.math3.fitting.WeightedObservedPoints;
import org.apache.commons.math3.stat.regression.SimpleRegression;

import java.util.Arrays;
import java.util.List;

public class WordComplexityIndices {

	public static double getInverseAverage(List<Double> stats) {
		double sum = 0;
		for (Double d : stats) {
			sum += d;
		}
		if (stats.size() != 0)
			return 1D - sum / stats.size();
		return -1;
	}

	public static double getInverseLinearRegressionSlope(List<Double> stats) {
		SimpleRegression r = new SimpleRegression(false);
		int index = 0;
		r.addData(((double) index++) / stats.size(), 0);
		for (Double d : stats) {
			r.addData(((double) index++) / stats.size(), d);
		}
		r.addData(1, 1);
		if (r.getSlope() != 0)
			return 1 / r.getSlope();
		return -1;
	}

	public static double getIndexAboveThreshold(List<Double> stats,
			double threshold) {
		int index = 0;
		for (; index < stats.size(); index++) {
			if (stats.get(index) >= threshold) {
				return index;
			}
		}
		return index;
	}

	public static double getIndexPolynomialFitAboveThreshold(
			List<Double> stats, double threshold) {
		final WeightedObservedPoints obs = new WeightedObservedPoints();
		int index = 0;
		obs.add(index++, 0);
		for (Double d : stats) {
			obs.add(index++, d);
		}
		obs.add(index, 1);
		// Instantiate a third-degree polynomial fitter.
		final PolynomialCurveFitter fitter = PolynomialCurveFitter.create(3);

		// Retrieve fitted parameters (coefficients of the polynomial function).
		final double[] coeff = fitter.fit(obs.toList());

		int level = 0;
		for (; level <= stats.size(); level++) {
			if (coeff[3] * Math.pow(level, 3) + coeff[2] * Math.pow(level, 2)
					+ coeff[1] * level + coeff[0] >= threshold) {
				return level;
			}
		}
		return level;
	}

	public static double getInflectionPointPolynomial(List<Double> stats) {
		final WeightedObservedPoints obs = new WeightedObservedPoints();
		int index = 0;
		for (int i = 0; i <= stats.size(); i++) {
			obs.add(index++, 0);
		}
		for (Double d : stats) {
			obs.add(index++, d);
		}
		for (int i = 0; i <= stats.size(); i++) {
			obs.add(index++, 1);
		}
		// Instantiate a third-degree polynomial fitter.
		final PolynomialCurveFitter fitter = PolynomialCurveFitter.create(3);

		// Retrieve fitted parameters (coefficients of the polynomial function).
		final double[] coeff = fitter.fit(obs.toList());
		return (-coeff[2] / (3 * coeff[3])) - stats.size();
	}

	public static void main(String[] args) {
		Double[] v1 = { 1D, 1D, 1D, 1D, 1D, 1D, 1D };
		Double[] v2 = { 0D, 0D, 0D, 0D, 0D, 0D, 1D };
		List<Double> stats1 = Arrays.asList(v1);
		List<Double> stats2 = Arrays.asList(v2);
		System.out.println(getInverseAverage(stats1) + " - "
				+ getInverseAverage(stats2));
		System.out.println(getInverseLinearRegressionSlope(stats1) + " - "
				+ getInverseLinearRegressionSlope(stats2));
		System.out.println(getIndexAboveThreshold(stats1, 0.5) + " - "
				+ getIndexAboveThreshold(stats2, 0.5));
		System.out.println(getIndexPolynomialFitAboveThreshold(stats1, 0.5)
				+ " - " + getIndexPolynomialFitAboveThreshold(stats2, 0.5));
		System.out.println(getInflectionPointPolynomial(stats1) + " - "
				+ getInflectionPointPolynomial(stats2));
	}
}
