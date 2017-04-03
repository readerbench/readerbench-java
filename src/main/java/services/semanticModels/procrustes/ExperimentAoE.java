package services.semanticModels.procrustes;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.mahout.math.DenseMatrix;
import org.apache.mahout.math.Matrix;
import org.openide.util.Exceptions;

import data.Lang;
import data.Word;
import services.ageOfExposure.WordComplexityIndices;
import services.commons.Formatting;
import services.commons.VectorAlgebra;
import services.converters.SplitTASA;
import services.semanticModels.ISemanticModel;
import services.semanticModels.LSA.LSA;
import services.semanticModels.word2vec.Word2VecModel;

public class ExperimentAoE {

    private static final Logger LOGGER = Logger.getLogger("");
    private final double MIN_THRESHOLD = 0.4;
    private final double MAX_THRESHOLD = 0.7;
    private final double THRESHOLD_INCREMENT = 0.1;

    private Lang lang;
    private final String path;

    private int noGrades;
    private ISemanticModel[] models;

    private double[][][] MP;
    private double[][][] MRC;

    private final Map<Word, List<Double>> AoEEvolution = new HashMap<>();

    private List<Map<Word, Integer>> wordMaps;
    private List<Word> embeddingWordList;
    Matrix embeddingMatureModel;

    public ExperimentAoE(String path) {
        this.path = path;
    }

    public int getNoGrades() {
        return noGrades;
    }

    public double[][] getModelMatrixProcrustes(int index) {
        return MP[index];
    }

    public double[][] getModelMatrixRelativeCosine(int index) {
        return MRC[index];
    }

    public void loadW2VModels() {
        LOGGER.info("Loading W2V models grade levels 0-12...");

        noGrades = SplitTASA.NO_GRADE_LEVELS;
        models = new Word2VecModel[noGrades];

        for (int i = 0; i < noGrades; i++) {
            models[i] = Word2VecModel.loadWord2Vec(path + "grade" + i, lang);
        }

        lang = models[noGrades - 1].getLanguage();
    }

    public void loadLSAModels() {
        LOGGER.info("Loading LSA models grade levels 0-12...");

        noGrades = SplitTASA.NO_GRADE_LEVELS;
        models = new Word2VecModel[noGrades];

        for (int i = 0; i < noGrades; i++) {
            models[i] = LSA.loadLSA(path + "grade" + i, lang);
        }

        lang = models[noGrades - 1].getLanguage();
    }

    public void extractWordMatrices() {
        LOGGER.info("Getting word matrices from models...");

        MP = new double[noGrades][][];
        MRC = new double[noGrades][][];
        wordMaps = new ArrayList<>();

        for (int i = 0; i < noGrades; i++) {
            Map<Word, double[]> wordVectors = models[i].getWordRepresentations();
            Map<Word, Integer> wordMap = new HashMap<>();
            MP[i] = new double[wordVectors.size()][];
            MRC[i] = new double[wordVectors.size()][];

            int j = 0;
            for (Map.Entry<Word, double[]> entry : wordVectors.entrySet()) {
                MP[i][j] = entry.getValue();
                MRC[i][j] = entry.getValue();
                wordMap.put(entry.getKey(), j);
                j++;
            }

            wordMaps.add(wordMap);
        }
    }

    public void loadEmbeddingWordList(String path, int size) {
        // load top 5000 word frequency list
        LOGGER.log(Level.INFO, "Loading frequency list from {0}", path);

        ArrayList<String> freqList = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            String line;
            while ((line = br.readLine()) != null) {
                freqList.add(line);
            }
        } catch (Exception e) {
            Exceptions.printStackTrace(e);
        }

        // use grade level 0 intersected with frequency list as embedding
        embeddingWordList = new ArrayList<>();

        int count = 0;
        Set<Word> set0 = models[0].getWordSet();

        for (String concept : freqList) {
            if (count == size) {
                break;
            }

            Word word = Word.getWordFromConcept(concept, lang);
            if (set0.contains(word)) {
                embeddingWordList.add(word);
                count++;
            }
        }

        LOGGER.log(Level.INFO, "Embedding word list has {0} words.", count);
    }

    public void extractMatureModelEmbedding() {
        LOGGER.log(Level.INFO, "Getting embedding matrix for mature model ({0})...", noGrades - 1);

        double[][] Y = new double[embeddingWordList.size()][];
        for (int i = 0; i < embeddingWordList.size(); i++) {
            Y[i] = MP[noGrades - 1][wordMaps.get(noGrades - 1).get(embeddingWordList.get(i))];
        }

        embeddingMatureModel = new DenseMatrix(Y);
    }

    public void rotateModelToMatureModel(int index) {
        double[][] X = new double[embeddingWordList.size()][];

        for (int i = 0; i < embeddingWordList.size(); i++) {
            X[i] = MP[index][wordMaps.get(index).get(embeddingWordList.get(i))];
        }

        LOGGER.log(Level.INFO, "Computing rotation matrix for {0}-12...", index);
        Matrix R = ProcrustesUtils.calcRotationMatrix(embeddingMatureModel, new DenseMatrix(X));

        LOGGER.log(Level.INFO, "Rotating model {0}-12...", index);
        MP[index] = ProcrustesUtils.get2dArrayFromMatrix((new DenseMatrix(MP[index]).times(R)));
    }

    public void rotateModelToMatureModelFull(int index) {
        double[][] X = new double[MP[index].length][];
        double[][] Y = new double[MP[index].length][];

        int count = 0;
        for (Word w : models[index].getWordSet()) {
            X[count] = MP[index][wordMaps.get(index).get(w)];
            Y[count] = MP[noGrades - 1][wordMaps.get(noGrades - 1).get(w)];
            count++;
        }

        LOGGER.log(Level.INFO, "Computing rotation matrix (full) for {0}-12...", index);
        Matrix R = ProcrustesUtils.calcRotationMatrix(new DenseMatrix(Y), new DenseMatrix(X));

        LOGGER.log(Level.INFO, "Rotating (full) model {0}-12...", index);
        MP[index] = ProcrustesUtils.get2dArrayFromMatrix((new DenseMatrix(MP[index]).times(R)));
    }

    public void test() {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

        while (true) {
            Integer idx;
            String line = "";

            try {
                line = br.readLine();
            } catch (IOException e) {
                Exceptions.printStackTrace(e);
            }

            Word word = Word.getWordFromConcept(line, lang);
            idx = wordMaps.get(12).get(word);

            if (idx == null) {
                System.out.println(line + " not found.");
                continue;
            }

            double[] v1;
            double[] v2 = MP[12][idx];

            System.out.println("Rotation + cosine:");
            for (int i = 1; i < noGrades - 1; i++) {
                Integer idx2 = wordMaps.get(i).get(word);

                if (idx2 == null) {
                    System.out.println(i + ": 0.0");
                } else {
                    v1 = MP[i][idx2];
                    System.out.println(i + ": " + VectorAlgebra.cosineSimilarity(v1, v2));
                }
            }
            System.out.println();

            System.out.println("Relative cosine:");
            double[] v3 = new double[embeddingWordList.size()];

            for (int i = 0; i < embeddingWordList.size(); i++) {
                v3[i] = VectorAlgebra.cosineSimilarity(MRC[12][wordMaps.get(12).get(embeddingWordList.get(i))],
                        MRC[12][idx]);
            }

            for (int i = 1; i < noGrades - 1; i++) {
                double[] v4 = new double[embeddingWordList.size()];
                Integer idx2 = wordMaps.get(i).get(word);

                if (idx2 == null) {
                    System.out.println(i + ": 0.0");
                } else {
                    for (int j = 0; j < embeddingWordList.size(); j++) {
                        v4[j] = VectorAlgebra.cosineSimilarity(MRC[i][wordMaps.get(i).get(embeddingWordList.get(j))],
                                MRC[i][idx2]);
                    }

                    System.out.println(i + ": " + VectorAlgebra.cosineSimilarity(v3, v4));
                }
            }
            System.out.println();
        }
    }

    public void calculateWordEvolutionsProcrustes() {
        LOGGER.info("Calculating word evolutions for procrustes method...");

        for (Word w : models[noGrades - 1].getWordRepresentations().keySet()) {
            List<Double> stats = new ArrayList<>();
            double[] vMature = MP[noGrades - 1][wordMaps.get(noGrades - 1).get(w)];

            for (int i = 1; i < noGrades - 1; i++) {
                Integer idx = wordMaps.get(i).get(w);

                if (idx == null) {
                    stats.add(0.0);
                } else {
                    stats.add(VectorAlgebra.cosineSimilarity(MP[i][idx], vMature));
                }
            }

            AoEEvolution.put(w, stats);
        }
    }

    public void calculateWordEvolutionsRelativePearson() {
        LOGGER.info("Calculating word evolutions for relative Pearson method...");

        for (Word w : models[noGrades - 1].getWordRepresentations().keySet()) {
            List<Double> stats = new ArrayList<>();
            double[] vMature = new double[embeddingWordList.size()];

            for (int i = 0; i < embeddingWordList.size(); i++) {
                vMature[i] = VectorAlgebra.cosineSimilarity(
                        MRC[noGrades - 1][wordMaps.get(noGrades - 1).get(embeddingWordList.get(i))],
                        MRC[noGrades - 1][wordMaps.get(noGrades - 1).get(w)]);
            }

            for (int i = 1; i < noGrades - 1; i++) {
                double[] v = new double[embeddingWordList.size()];
                Integer idx = wordMaps.get(i).get(w);

                if (idx == null) {
                    stats.add(0.0);
                } else {
                    for (int j = 0; j < embeddingWordList.size(); j++) {
                        v[j] = VectorAlgebra.cosineSimilarity(MRC[i][wordMaps.get(i).get(embeddingWordList.get(j))], MRC[i][idx]);
                    }

                    stats.add(VectorAlgebra.pearsonCorrelation(v, vMature));
                }
            }

            AoEEvolution.put(w, stats);
        }
    }

    public static Map<String, Double> getWordAcquisitionAge(String normFile) {
        Map<String, Double> aoaWords = new HashMap<>();
        LOGGER.log(Level.INFO, "Loading file {0}...", normFile);

        /* Compute the AgeOfAcquisition Dictionary */
        String tokens[];
        String line;
        String word;
        try (BufferedReader br = new BufferedReader(new FileReader("resources/config/EN/word lists/AoA/" + normFile))) {
            while ((line = br.readLine()) != null) {
                tokens = line.split(",");
                word = tokens[0].trim().replaceAll(" ", "");

                if (tokens[1].equals("NA")) {
                    continue;
                }

                Double.parseDouble(tokens[1]);
                aoaWords.put(word, Double.parseDouble(tokens[1]));
            }
        } catch (IOException | NumberFormatException e) {
            Exceptions.printStackTrace(e);
        }
        return aoaWords;
    }

    public void writeResults(String statsfile, String wordfile) {
        // determine word acquisition ages
        Map<String, Double> birdAoA = getWordAcquisitionAge("Bird.csv");
        Map<String, Double> bristolAoA = getWordAcquisitionAge("Bristol.csv");
        Map<String, Double> corteseAoA = getWordAcquisitionAge("Cortese.csv");
        Map<String, Double> kupermanAoA = getWordAcquisitionAge("Kuperman.csv");
        Map<String, Double> shockAoA = getWordAcquisitionAge("Shock.csv");

        try {
            BufferedWriter loweValues;
            try (BufferedWriter loweStats = new BufferedWriter(
                    new OutputStreamWriter(new FileOutputStream(new File(path + "/" + statsfile)), "UTF-8"), 32768)) {
                loweValues = new BufferedWriter(
                        new OutputStreamWriter(new FileOutputStream(new File(path + "/" + wordfile)), "UTF-8"), 32768);
                // create header
                String content = "Word,Bird_AoA,Bristol_AoA,Cortese_AoA,Kuperman_AoA,Shock_AoA";
                loweStats.write(content);
                loweValues.write(content);
                for (int i = 1; i < noGrades - 1; i++) {
                    loweStats.write(",Grades_1_" + i);
                }
                content = ",InverseAverage,InverseLinearRegressionSlope";
                for (double i = MIN_THRESHOLD; i <= MAX_THRESHOLD; i += THRESHOLD_INCREMENT) {
                    content += ",IndexAboveThreshold(" + i + ")";
                }
                for (double i = MIN_THRESHOLD; i <= MAX_THRESHOLD; i += THRESHOLD_INCREMENT) {
                    content += ",IndexPolynomialFitAboveThreshold(" + i + ")";
                }
                content += ",InflectionPointPolynomial\n";
                loweStats.write(content);
                loweValues.write(content);
                List<Double> stats;
                for (Word analyzedWord : models[noGrades - 1].getWordRepresentations().keySet()) {
                    stats = AoEEvolution.get(analyzedWord);
                    content = analyzedWord.getExtendedLemma() + ",";
                    // AoA indices
                    if (birdAoA.containsKey(analyzedWord.getLemma())) {
                        content += birdAoA.get(analyzedWord.getLemma());
                    }
                    content += ",";
                    if (bristolAoA.containsKey(analyzedWord.getLemma())) {
                        content += bristolAoA.get(analyzedWord.getLemma());
                    }
                    content += ",";
                    if (corteseAoA.containsKey(analyzedWord.getLemma())) {
                        content += corteseAoA.get(analyzedWord.getLemma());
                    }
                    content += ",";
                    if (kupermanAoA.containsKey(analyzedWord.getLemma())) {
                        content += kupermanAoA.get(analyzedWord.getLemma());
                    }
                    content += ",";
                    if (shockAoA.containsKey(analyzedWord.getLemma())) {
                        content += shockAoA.get(analyzedWord.getLemma());
                    }
                    loweStats.write(content);
                    loweValues.write(content);
                    for (Double d : stats) {
                        loweStats.write("," + Formatting.formatNumber(d));
                    }
                    double value = WordComplexityIndices.getInverseAverage(stats);
                    if (Math.round(value * 100) / 100 != 1) {
                        content = "," + WordComplexityIndices.getInverseAverage(stats);
                        content += "," + WordComplexityIndices.getInverseLinearRegressionSlope(stats);
                        for (double i = MIN_THRESHOLD; i <= MAX_THRESHOLD; i += THRESHOLD_INCREMENT) {
                            value = WordComplexityIndices.getIndexAboveThreshold(stats, i);
                            if (value != -1) {
                                content += "," + value;
                            } else {
                                content += ",";
                            }
                        }

                        for (double i = MIN_THRESHOLD; i <= MAX_THRESHOLD; i += THRESHOLD_INCREMENT) {
                            value = WordComplexityIndices.getIndexPolynomialFitAboveThreshold(stats, i);
                            if (value != -1) {
                                content += "," + value;
                            } else {
                                content += ",";
                            }
                        }
                        content += "," + WordComplexityIndices.getInflectionPointPolynomial(stats);
                        loweStats.write(content);
                        loweValues.write(content);
                    }
                    content = "\n";
                    loweStats.write(content);
                    loweValues.write(content);
                }
            }
            loweValues.close();
        } catch (IOException e) {
            Exceptions.printStackTrace(e);
        }
    }

    public static void main(String[] args) {
        // load w2v models
        ExperimentAoE exp = new ExperimentAoE("resources/in/AoE w2v/");
        exp.loadW2VModels();

        // load w2v models
//        ExperimentAoE exp = new ExperimentAoE("resources/in/AoE LSA/");
//        exp.loadLSAModels();
        // get word vectors from models
        exp.extractWordMatrices();

        // mean center & normalize models
        //LOGGER.info("Mean centering + normalizing models...");
        //for (int i = 0; i < exp.getNoGrades(); i++) {
        //	ProcrustesUtils.meanCenter(exp.getModelMatrixProcrustes(i));
        //	ProcrustesUtils.normalize(exp.getModelMatrixProcrustes(i));
        //}
        // load embedding word list
        exp.loadEmbeddingWordList("resources/config/EN/word lists/5000coca_en.txt", 1000);

        // get embedding of mature model
        //exp.extractMatureModelEmbedding();
        // rotate models 1-11 to model 12
        //for (int i = 1; i < exp.getNoGrades() - 1; i++) {
        //	exp.rotateModelToMatureModelFull(i);
        //}
        LOGGER.info("Done!");

        // tests
        // exp.test();
        // calculate evolutions
        //exp.calculateWordEvolutionsProcrustes();
        exp.calculateWordEvolutionsRelativePearson();

        // write results
        exp.writeResults("AoE stats.csv", "AoE word.csv");
    }

}
