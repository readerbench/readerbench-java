/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package runtime.essays;

import edu.stanford.nlp.util.Pair;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Random;
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
    
    public static void checkInterval(Classifier model, Instances instances, double min, double max) throws Exception {
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
        double precision = 100. * partially / responses;
        double recall = 100. * correct / total;
        double coverage = 100. * responses / instances.size();
        if (responses == 0) {
            precision = 100;
        }
        if (total == 0) {
            recall = 100;
        }
        double f1 = 2 * precision * recall / (precision + recall);
        System.out.println("precision: " + precision + ", recall: " + recall + 
                ", f1: " + f1 + ", coverage: " + coverage);
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
        for (double i = 2; i < PASS; i += 0.5) {
            for (double j = PASS + 0.5; j < 9.5; j += 0.5) {
                checkInterval(model, test, i, j);
            }
        }
        
    }
}
