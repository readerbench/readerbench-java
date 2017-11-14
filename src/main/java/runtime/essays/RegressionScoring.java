/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package runtime.essays;

import edu.stanford.nlp.util.Pair;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.apache.commons.math3.util.Precision;
import org.openide.util.Exceptions;
import weka.classifiers.Classifier;
import weka.classifiers.functions.MLPRegressor;
import weka.classifiers.functions.MultilayerPerceptron;
import weka.classifiers.functions.activation.Sigmoid;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ArffSaver;
import weka.core.converters.CSVLoader;
import weka.core.converters.ConverterUtils.DataSource;
import weka.filters.Filter;
import weka.filters.supervised.attribute.AttributeSelection;
import weka.filters.unsupervised.attribute.Remove;

/**
 *
 * @author Stefan
 */
public class RegressionScoring {
    
    public static final double PASS = 6;
    private static final Random random = new Random();
    
    public static List<Double> checkInterval(Classifier model, Instances instances, double min, double max) throws Exception {
        int correct = 0;
        int partially = 0;
        int responses = 0;
        int total = 0;
        for (Instance inst : instances) {
            double predicted = model.classifyInstance(inst);
            double real = inst.classValue();
            if (predicted <= min) {
                responses ++;
                if (real < PASS) {
                    partially ++;
                }
                if (real <= min) {
                    correct ++;
                }
            }
            if (predicted >= max) {
                responses ++;
                if (real >= PASS) {
                    partially ++;
                }
                if (real >= max) {
                    correct ++;
                }
            }
            if (real >= max || real <= min) {
                total ++;
            }
        }
        System.out.println(min + " - " + max + ": ");
        double precision = Precision.round(100. * partially / responses, 2);
        double recall = Precision.round(100. * correct / total, 2);
        double coverage = Precision.round(100. * responses / instances.size(), 2);
        if (responses == 0) {
            precision = 100;
        }
        if (total == 0) {
            recall = 100;
        }
        double f1 = Precision.round(2 * precision * recall / (precision + recall), 2);
        System.out.println("precision: " + precision + ", recall: " + recall + 
                ", f1: " + f1 + ", coverage: " + coverage);
        return Arrays.asList(new Double[]{precision, recall, f1, coverage});
    }
    
    public static Instances filterInstances() throws Exception {
        CSVLoader loader = new CSVLoader();
        loader.setFieldSeparator(";");
        loader.setSource(new File("resources/in/VIBOA_nl/indices.csv"));
        Instances instances = loader.getDataSet();
        //instances.setClassIndex(0);
        AttributeSelection filter = new AttributeSelection();
        filter.setInputFormat(instances);
        return Filter.useFilter(instances, filter);
    }
    
    public static Pair<Instances, Instances> splitDataset(Instances instances, double testRatio) {
        instances.sort(instances.attribute(0));
        Instances train = new Instances(instances);
        train.clear();
        Instances test = new Instances(instances);
        test.clear();
        int testSize = (int)(instances.size() * testRatio);
        for (int i = 0; i < testSize; i++) {
            int a = i * (instances.size() / testSize);
            int b = (i + 1) * (instances.size() / testSize);
            if (i == testSize - 1) {
                b = instances.size();
            }
            int index = a + random.nextInt(b - a);
            for (int j = a; j < b; j++) {
                if (j == index) {
                    test.add(instances.get(j));
                }
                else {
                    train.add(instances.get(j));
                }
            }
        }
        return new Pair<>(train, test);
    }
    
    public static void printScores(List<Double>[][] scores, int index, String fileName) {
        try (PrintWriter out = new PrintWriter(fileName)) {
            String sep = ";";
            out.println("sep=" + sep);
            String header = IntStream.range(0, scores[0].length)
                    .mapToDouble(j -> Precision.round(PASS + j * 0.1, 2))
                    .mapToObj(b -> b + "")
                    .collect(Collectors.joining(sep));
            out.println("a\\b" + sep + header);
            for (int i = 0; i < scores.length; i++) {
                out.print(Precision.round(2 + i * 0.1, 2));
                for (int j = 0; j < scores[i].length; j++) {
                    out.print(sep + scores[i][j].get(index));
                }
                out.println();
            }
        } catch (FileNotFoundException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
    
    public static void printTestResults(Classifier model, Instances test, String outputFileName) {
        try (PrintWriter out = new PrintWriter(outputFileName)) {
            out.println("sep=;");
            out.println("Target;Predicted");
            for (Instance inst : test) {
                double predicted = model.classifyInstance(inst);
                double real = inst.classValue();
                out.println(real + ";" + Precision.round(predicted, 2));
            }
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }
    }
    
    public static void main(String[] args) throws Exception {
//        Instances instances = filterInstances();
//        Instances instances = new Instances(new FileReader("resources/in/VIBOA_nl/indices.arff"));
//        Pair<Instances, Instances> split = splitDataset(instances, 0.2);
//        Instances train = split.first;
//        Instances test = split.second;
//        ArffSaver saver = new ArffSaver();
//        saver.setFile(new File("resources/in/VIBOA_nl/train.arff"));
//        saver.setInstances(train);
//        saver.writeBatch();
//        saver.setFile(new File("resources/in/VIBOA_nl/test.arff"));
//        saver.setInstances(test);
//        saver.writeBatch();
        Instances train = new Instances(new FileReader("resources/in/VIBOA_nl/train.arff"));
        train.setClassIndex(train.numAttributes() - 1);
        Instances test = new Instances(new FileReader("resources/in/VIBOA_nl/test.arff"));
        test.setClassIndex(test.numAttributes() - 1);
        MLPRegressor model = new MLPRegressor();
        model.setActivationFunction(new Sigmoid());
        model.setNumFunctions(2);
        model.buildClassifier(train);
        printTestResults(model, test, "resources/in/VIBOA_nl/test-results.csv");
//        List<Double>[][] scores = new List[(int)((PASS - 2) / 0.1 + 1)][(int)((9.5-PASS) / 0.1 + 1)];
//        for (int i = 0; 2 + i * 0.1 <= PASS; i ++) {
//            for (int j = 0; PASS + j * 0.1 <= 9.5; j ++) {
//                double a = Precision.round(2 + i * 0.1, 2);
//                double b = Precision.round(PASS + j * 0.1, 2);
//                scores[i][j] = checkInterval(model, test, a, b);
//            }
//        }
//        printScores(scores, 0, "resources/in/VIBOA_nl/precision.csv");
//        printScores(scores, 1, "resources/in/VIBOA_nl/recall.csv");
//        printScores(scores, 2, "resources/in/VIBOA_nl/f1.csv");
//        printScores(scores, 3, "resources/in/VIBOA_nl/coverage.csv");
        
    }
}
