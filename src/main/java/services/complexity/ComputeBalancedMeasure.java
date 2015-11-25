package services.complexity;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import services.libsvm.svm;
import services.libsvm.svm_model;
import services.libsvm.svm_node;
import services.libsvm.svm_parameter;
import services.libsvm.svm_problem;
import data.AbstractDocument;
import data.complexity.Measurement;
import data.cscl.Conversation;
import data.cscl.Participant;

public class ComputeBalancedMeasure {
	static Logger logger = Logger.getLogger(ComputeBalancedMeasure.class);

	public svm_parameter getTrainingParameter(int noFactors) {
		svm_parameter param = new svm_parameter();
		param.svm_type = svm_parameter.C_SVC;
		param.kernel_type = svm_parameter.RBF;
		param.degree = 3;
		param.gamma = 1.0f / noFactors;
		param.coef0 = 0;
		param.nu = 0.5;
		param.cache_size = 256;
		param.C = 65536;
		param.eps = 1e-3;
		param.p = 0.1;
		param.shrinking = 1;
		param.probability = 0;
		return param;
	}

	public double[] gridSearch(int[] selectedMeasurements,
			List<Measurement> trainingMeasurements,
			List<Measurement> testMeasurements, String testName, int noClasses,
			double[][] precisionValues, Integer crtFold) {
		// returns the predictions for the testing set
		double[] testPredictions = new double[testMeasurements.size()];

		// Prepare Training Set
		svm_node[][] X = new svm_node[trainingMeasurements.size()][];
		double[] Y = new double[trainingMeasurements.size()];

		int index = 0;
		for (Measurement measurement : trainingMeasurements) {
			svm_node[] nodes = new svm_node[selectedMeasurements.length];
			int nodeCount = 1;
			for (int selectedMeasurement : selectedMeasurements) {
				svm_node node = new svm_node();
				node.index = nodeCount;
				node.value = measurement.getMeasurementValues()[selectedMeasurement];

				nodes[nodeCount - 1] = node;
				nodeCount++;
			}

			X[index] = nodes;
			Y[index] = measurement.getInputClass();
			index++;
		}

		// normalize
		double lower = -1;
		double upper = 1;
		double[] max = new double[X[0].length];
		double[] min = new double[X[0].length];
		// scale all dimensions linearly
		for (int i = 0; i < X[0].length; i++) {
			max[i] = Double.MIN_VALUE;
			min[i] = Double.MAX_VALUE;
		}
		for (int i = 0; i < X.length; i++)
			for (int j = 0; j < X[i].length; j++) {
				max[j] = Math.max(max[j], X[i][j].value);
				min[j] = Math.min(min[j], X[i][j].value);
			}
		for (int i = 0; i < X.length; i++)
			for (int j = 0; j < X[i].length; j++) {
				X[i][j].value = lower + (upper - lower)
						* (X[i][j].value - min[j]) / (max[j] - min[j]);
			}

		svm_problem trainingSet = new svm_problem();
		trainingSet.l = X.length;
		trainingSet.x = X;
		trainingSet.y = Y;

		// prepare testing
		svm_node[][] nodes = new svm_node[testMeasurements.size()][selectedMeasurements.length];
		index = 0;
		for (Measurement measurement : testMeasurements) {
			int nodeCount = 1;

			for (int selectedMeasurement : selectedMeasurements) {
				svm_node node = new svm_node();
				node.index = nodeCount;
				node.value = measurement.getMeasurementValues()[selectedMeasurement];

				nodes[index][nodeCount - 1] = node;
				nodeCount++;
			}

			// scale node values
			for (int i = 0; i < nodes[index].length; i++) {
				if (nodes[index][i].value < min[i])
					nodes[index][i].value = lower;
				else if (nodes[index][i].value > max[i])
					nodes[index][i].value = upper;
				else {
					nodes[index][i].value = lower + (upper - lower)
							* (nodes[index][i].value - min[i])
							/ (max[i] - min[i]);
				}
			}
			index++;
		}

		// Get Training parameter
		svm_parameter param = getTrainingParameter(selectedMeasurements.length);

		// Grid Search
		double C_optimum = 1;
		double gamma_optimum = param.gamma;
		double precision_optimum = Double.MIN_VALUE;
		double C = Math.pow(2, -5);
		while (C <= Math.pow(2, 15)) {
			double gamma = Math.pow(2, -15);

			while (gamma <= Math.pow(2, 3)) {
				param.gamma = gamma;
				param.C = C;
				svm_model model = svm.svm_train(trainingSet, param);

				double precision = 0;
				for (int i = 0; i < testMeasurements.size(); i++) {
					// Predict
					double prediction = svm.svm_predict(model, nodes[i]);

					// Check prediction
					int classNumber = (int) testMeasurements.get(i)
							.getInputClass();

					if (Math.round(prediction) == classNumber) {
						precision++;
					}
				}

				if (precision > precision_optimum) {
					precision_optimum = precision;
					C_optimum = C;
					gamma_optimum = gamma;
				}

				gamma *= 4;
			}

			C *= 4;
		}

		// select optimal parameters and perform final training
		param.gamma = gamma_optimum;
		param.C = C_optimum;
		svm_model model = svm.svm_train(trainingSet, param);
		double[] finalPrecision = new double[noClasses];
		double[] finalNearPrecision = new double[noClasses];
		double[] finalCount = new double[noClasses];

		for (int i = 0; i < testMeasurements.size(); i++) {
			// Predict and save result
			double prediction = svm.svm_predict(model, nodes[i]);
			testPredictions[i] = prediction;

			// Check prediction
			if (precisionValues != null) {
				int classNumber = (int) testMeasurements.get(i).getInputClass();
				finalCount[classNumber - 1]++;

				if (Math.round(prediction) == classNumber) {
					finalPrecision[classNumber - 1]++;
				}
				if (Math.abs(prediction - classNumber) <= 1) {
					finalNearPrecision[classNumber - 1]++;
				}
			}
		}

		for (int classNumber = 1; classNumber <= noClasses; classNumber++) {
			finalPrecision[classNumber - 1] /= finalCount[classNumber - 1];
			if (precisionValues != null)
				precisionValues[crtFold][classNumber - 1] = finalPrecision[classNumber - 1];
		}
		for (int classNumber = 1; classNumber <= noClasses; classNumber++) {
			finalNearPrecision[classNumber - 1] /= finalCount[classNumber - 1];
			if (precisionValues != null)
				precisionValues[crtFold][noClasses + classNumber - 1] = finalNearPrecision[classNumber - 1];
		}

		return testPredictions;
	}

	public static double[] evaluateTextualComplexity(
			List<AbstractDocument> documents, String path,
			int[] selectedMeasurements) {
		// determine number of classes
		Map<Double, List<Measurement>> measurements = DataGathering
				.getMeasurements(path + "/measurements.csv");
		Set<Double> classes = measurements.keySet();
		int noClasses = classes.size();
		logger.info("Started to train custom SVM model on " + noClasses
				+ " classes on " + path + " corpus");

		// define training and testing sets
		// training = entire corpus
		List<Measurement> trainingMeasurements = new LinkedList<Measurement>();
		// testing = list of documents
		List<Measurement> testMeasurements = new LinkedList<Measurement>();

		for (Double classId : measurements.keySet()) {
			for (int i = 0; i < measurements.get(classId).size(); i++)
				trainingMeasurements.add(measurements.get(classId).get(i));
		}

		for (AbstractDocument d : documents) {
			ComplexityIndices.computeComplexityFactors(d);
			testMeasurements.add(new Measurement(-1, d.getComplexityIndices()));
		}

		// run SVM classifier
		ComputeBalancedMeasure svm = new ComputeBalancedMeasure();
		return svm.gridSearch(selectedMeasurements, trainingMeasurements,
				testMeasurements, "Custom Complexity Model", noClasses, null,
				null);
	}

	public static void evaluateTextualComplexityParticipants(Conversation chat,
			String path, int[] selectedMeasurements) {
		List<AbstractDocument> participantInterventions = new LinkedList<AbstractDocument>();

		Iterator<Participant> it = chat.getParticipants().iterator();
		while (it.hasNext()) {
			Participant part = it.next();
			participantInterventions.add(part.getSignificantInterventions());
		}

		double[] results = evaluateTextualComplexity(participantInterventions,
				path, selectedMeasurements);

		int i = 0;
		while (it.hasNext()) {
			Participant part = it.next();
			part.setTextualComplexityLevel(results[i++]);
		}
	}
}