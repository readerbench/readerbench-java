package services.readingStrategies;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.log4j.Logger;

import services.complexity.ComputeBalancedMeasure;
import data.complexity.Measurement;
import data.document.Metacognition;

public class ComprehensionPrediction extends ComputeBalancedMeasure {
	static Logger logger = Logger.getLogger(ComprehensionPrediction.class);
	public static final int NO_COMPREHENSION_CLASSES = 3;
	private List<? extends Metacognition> selfExplanations;
	private Map<Double, List<Measurement>> measurements;
	private Map<Double, Integer[]> order;

	public ComprehensionPrediction(
			List<? extends Metacognition> loadedSelfExplanations) {
		this.selfExplanations = loadedSelfExplanations;
		determineComprehensionClass();
		determineMeasurements();
	}

	/**
	 * @param loadedSelfExplanations
	 */
	private void determineComprehensionClass() {
		List<Double> values = new LinkedList<Double>();
		for (Metacognition v : selfExplanations) {
			if (v != null) {
				values.add(v.getAnnotatedComprehensionScore());
			}
		}
		Collections.sort(values);
		// determine threshold 1 and 2
		double threshold1 = values.get((int) (0.3 * values.size()));
		double threshold2 = values.get((int) (0.7 * values.size()));

		for (Metacognition v : selfExplanations) {
			if (v != null) {
				// assign a corresponding class to each verbalization file
				if (v.getAnnotatedComprehensionScore() < threshold1)
					v.setComprehensionClass(1);
				else if (v.getAnnotatedComprehensionScore() < threshold2)
					v.setComprehensionClass(2);
				else
					v.setComprehensionClass(3);
			}
		}

		// determine median
		// double median = values.get((int) (0.5 * values.size()));
		//
		// for (Metacognition v : loadedVervalizations) {
		// if (v != null) {
		// // assign a corresponding class to each verbalization file
		// if (v.getAnnotatedComprehensionScore() < median)
		// v.setComprehensionClass(1);
		// else
		// v.setComprehensionClass(2);
		// }
		// }
	}

	private void determineMeasurements() {
		measurements = new TreeMap<Double, List<Measurement>>();
		for (int index = 0; index < selfExplanations.size(); index++) {
			if (selfExplanations.get(index) != null) {
				Metacognition v = selfExplanations.get(index);
				if (!measurements.containsKey(Double.valueOf(v
						.getComprehensionClass()))) {
					measurements.put(Double.valueOf(v.getComprehensionClass()),
							new LinkedList<Measurement>());
				}

				measurements.get(Double.valueOf(v.getComprehensionClass()))
						.add(new Measurement(v.getComprehensionClass(), v
								.getComplexityIndices()));
			}
		}

		// generate random sequence of measurements
		order = new TreeMap<Double, Integer[]>();
		for (Double classId : measurements.keySet()) {
			order.put(classId, new Integer[measurements.get(classId).size()]);
			for (int j = 0; j < measurements.get(classId).size(); j++) {
				order.get(classId)[j] = j;
			}
		}

		int aux;
		// perform random swaps of elements
		for (Double classId : measurements.keySet()) {
			for (int k = 0; k < (int) Math.pow(order.get(classId).length, 2); k++) {
				int i = (int) (Math.random() * order.get(classId).length);
				int j = (int) (Math.random() * order.get(classId).length);
				aux = order.get(classId)[i];
				order.get(classId)[i] = order.get(classId)[j];
				order.get(classId)[j] = aux;
			}
		}
	}

	public double[] runSVM(int[] factors, int no_folds) {
		double[][] precision = new double[no_folds][2 * ComprehensionPrediction.NO_COMPREHENSION_CLASSES];

		// perform the K folds
		for (int k = 0; k < no_folds; k++) {
			List<Measurement> trainingMeasurements = new LinkedList<Measurement>();
			List<Measurement> testMeasurements = new LinkedList<Measurement>();

			// add entries to corresponding set

			for (Double classId : measurements.keySet()) {
				int range = Math.round(1.0f / no_folds
						* measurements.get(classId).size() - 0.5f);
				for (int i = 0; i < measurements.get(classId).size(); i++) {
					if (i >= k * range && i < (k + 1) * range)
						testMeasurements.add(measurements.get(classId).get(
								order.get(classId)[i]));
					else
						trainingMeasurements.add(measurements.get(classId).get(
								order.get(classId)[i]));
				}
			}

			// run SVM
			super.gridSearch(factors, trainingMeasurements, testMeasurements,
					"comprehension prediction",
					ComprehensionPrediction.NO_COMPREHENSION_CLASSES,
					precision, k);
		}

		double[] aggrements = new double[ComprehensionPrediction.NO_COMPREHENSION_CLASSES + 1];
		// determine average values for EA
		for (int i = 0; i < ComprehensionPrediction.NO_COMPREHENSION_CLASSES; i++) {
			for (int k = 0; k < no_folds; k++)
				aggrements[i] += precision[k][i];
			aggrements[i] /= no_folds;
			aggrements[ComprehensionPrediction.NO_COMPREHENSION_CLASSES] += aggrements[i];
		}
		aggrements[ComprehensionPrediction.NO_COMPREHENSION_CLASSES] /= ComprehensionPrediction.NO_COMPREHENSION_CLASSES;

		return aggrements;
	}
}