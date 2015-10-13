package services.complexity;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import weka.classifiers.Classifier;
import weka.classifiers.evaluation.Evaluation;
import weka.classifiers.evaluation.NominalPrediction;
import weka.classifiers.evaluation.Prediction;
import weka.classifiers.evaluation.output.prediction.PlainText;
import weka.classifiers.rules.DecisionTable;
import weka.classifiers.rules.OneR;
import weka.classifiers.rules.PART;
import weka.classifiers.rules.ZeroR;
import weka.classifiers.trees.DecisionStump;
import weka.classifiers.trees.HoeffdingTree;
import weka.classifiers.trees.J48;
import weka.classifiers.trees.LMT;
import weka.classifiers.trees.RandomTree;
import weka.core.Attribute;
import weka.core.Instances;

public class WekaPrediction {

	public Instances getDataSet(String path) {
		Instances dataSet = null;
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(path));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		try {
			dataSet = new Instances(reader);
			reader.close();
			dataSet.setClassIndex(0);
		} catch (IOException e) {
			e.printStackTrace();
		}

		return dataSet;
	}

	public Instances getDataSet2(String path) {
		Instances dataSet = null;
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(path));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		try {
			dataSet = new Instances(reader);
			reader.close();

			dataSet.setClassIndex(0);
			System.out.println(Attribute.NOMINAL);

		} catch (IOException e) {
			e.printStackTrace();
		}

		return dataSet;
	}

	public Instances[] getTrainingSet(Instances dataSet, int kFolds) {
		Instances[] trainingSet = new Instances[kFolds];
		for (int i = 0; i < kFolds; i++) {
			trainingSet[i] = dataSet.trainCV(kFolds, i);
		}
		return trainingSet;
	}

	public Instances[] getTestingSet(Instances dataSet, int kFolds) {
		Instances[] testingSet = new Instances[kFolds];
		for (int i = 0; i < kFolds; i++) {
			testingSet[i] = dataSet.testCV(kFolds, i);
		}

		return testingSet;
	}

	public double calculateAccuracy(List<List<Prediction>> predictions) {
		double correct = 0;
		double totalNr = 0;
		for (int i = 0; i < predictions.size(); i++) {
			for (int j = 0; j < predictions.get(i).size(); j++) {
				totalNr++;
				NominalPrediction np = (NominalPrediction) predictions.get(i)
						.get(j);

				if (np.predicted() == np.actual()) {
					correct++;
				}
			}
		}

		return correct / totalNr;
	}

	public String makePrediction(String path, String algorithm, int kFolds) {
		Instances dataSet = getDataSet(path);
		Instances[] testingSet = getTestingSet(dataSet, kFolds);
		Instances[] trainingSet = getTrainingSet(dataSet, kFolds);
		Classifier classifier = null;
		String result = "";

		// selectam algoritmul
		switch (algorithm) {
		case "J48":
			classifier = new J48();
			break;

		case "ZeroR":
			classifier = new ZeroR();
			break;

		case "OneR":
			classifier = new OneR();
			break;

		case "PART":
			classifier = new PART();
			break;

		case "DecisionTable":
			classifier = new DecisionTable();
			break;

		case "DecisionStump":
			classifier = new DecisionStump();
			break;

		case "HoeffdingTree":
			classifier = new HoeffdingTree();
			break;

		case "LMT":
			classifier = new LMT();
			break;
		case "RandomTree":

			classifier = new RandomTree();
			break;
		default:
			classifier = new ZeroR();
			break;
		}

		Evaluation eval = null;
		try {
			eval = new Evaluation(dataSet);
		} catch (Exception e1) {

			e1.printStackTrace();
		}
		List<Evaluation> evals = new ArrayList<Evaluation>();
		List<List<Prediction>> preds = new ArrayList<List<Prediction>>();
		PlainText output = new PlainText();
		output.setOutputFile(new File("D:/Disertatie/TEst/output"));

		try {
			for (int i = 0; i < kFolds; i++) {
				Evaluation eval2 = new Evaluation(trainingSet[i]);
				classifier.buildClassifier(trainingSet[i]);

				eval.evaluateModel(classifier, testingSet[i]);
				eval2.evaluateModel(classifier, testingSet[i]);
				evals.add(eval);
				preds.add(eval.predictions());
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		try {
			result += eval.toSummaryString() + "\n";
		} catch (Exception e) {
			e.printStackTrace();
		}

		try {
			result += eval.toClassDetailsString() + "\n";
		} catch (Exception e) {
			e.printStackTrace();
		}

		try {
			result += eval.toCumulativeMarginDistributionString() + "\n";
		} catch (Exception e) {
			e.printStackTrace();
		}

		DecimalFormat df = new DecimalFormat("#.00");
		try {
			result += eval.toMatrixString() + "\n";
			result += "Precision: " + df.format(calculateAccuracy(preds)) + "%";
		} catch (Exception e) {
			e.printStackTrace();
		}

		return result;
	}

}
