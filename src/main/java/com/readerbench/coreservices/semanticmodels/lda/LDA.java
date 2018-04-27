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
package com.readerbench.coreservices.semanticmodels.lda;

import cc.mallet.pipe.*;
import cc.mallet.pipe.iterator.CsvIterator;
import cc.mallet.topics.ParallelTopicModel;
import cc.mallet.types.IDSorter;
import cc.mallet.types.InstanceList;
import cc.mallet.util.Maths;
import com.readerbench.coreservices.commons.ObjectManipulation;
import com.readerbench.coreservices.commons.VectorAlgebra;
import com.readerbench.datasourceprovider.data.AnalysisElement;
import com.readerbench.datasourceprovider.data.Word;
import com.readerbench.datasourceprovider.data.semanticmodels.ISemanticModel;
import com.readerbench.datasourceprovider.data.semanticmodels.SimilarityType;
import com.readerbench.datasourceprovider.pojo.Lang;
import static com.readerbench.datasourceprovider.pojo.Lang.es;
import static com.readerbench.datasourceprovider.pojo.Lang.fr;
import static com.readerbench.datasourceprovider.pojo.Lang.it;
import static com.readerbench.datasourceprovider.pojo.Lang.la;
import static com.readerbench.datasourceprovider.pojo.Lang.nl;
import static com.readerbench.datasourceprovider.pojo.Lang.ro;
import edu.stanford.nlp.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.*;
import java.util.Map.Entry;
import java.util.function.BiFunction;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class LDA implements ISemanticModel, Serializable {

    private static final long serialVersionUID = 5981303412937874248L;

    private static final Logger LOGGER = LoggerFactory.getLogger(LDA.class);

    private static int MIN_NO_WORDS_PER_DOCUMENT = 5;

    private static List<LDA> LOADED_LDA_MODELS = new ArrayList<>();
    private static final Set<Lang> AVAILABLE_FOR = EnumSet.of(Lang.en, Lang.es, Lang.fr, Lang.it, Lang.la, Lang.nl);

    private Lang language;
    private String path;
    private Pipe pipe;
    private InstanceList instances;
    private Map<Word, double[]> wordProbDistributions;
    private int numTopics;
    private List<Pair<Word, Double>>[] sortedWords;

    public LDA(Lang language) {
        this.language = language;
        pipe = buildPipe();
    }

    private LDA(String path, Lang language) {
        this(language);
        this.path = path;
        LOGGER.info("Loading LDA model {} ...", path);
        try {
            wordProbDistributions = (Map<Word, double[]>) ObjectManipulation.loadObject(path + "/lda.model");
            numTopics = wordProbDistributions.entrySet().stream()
                    .map(e -> e.getValue().length)
                    .findFirst().orElse(0);
            sortedWords = IntStream.range(0, numTopics).parallel()
                    .mapToObj(topic -> wordProbDistributions.entrySet().stream()
                            .map(e -> new Pair<>(e.getKey(), e.getValue()[topic]))
                            .sorted((p1, p2) -> p2.second.compareTo(p1.second))
                            .collect(Collectors.toList()))
                    .toArray(size -> new List[size]);
        } catch (ClassNotFoundException | IOException ex) {
            LOGGER.error(ex.getMessage());
        }
    }

    public static LDA loadLDA(String path, Lang language) {
        for (LDA lda : LOADED_LDA_MODELS) {
            if (path.equals(lda.getPath())) {
                return lda;
            }
        }
        LDA ldaLoad = new LDA(path, language);
        LOADED_LDA_MODELS.add(ldaLoad);

        return ldaLoad;
    }

    public void readDirectory(File directory) {
        // read all TXT files within a directory
        if (directory.isDirectory()) {
            instances = new InstanceList(pipe);
            for (File f : directory.listFiles(new FileFilter() {
                private final String[] okFileExtensions = new String[]{"txt"};

                @Override
                public boolean accept(File file) {
                    for (String extension : okFileExtensions) {
                        if (file.getName().toLowerCase().endsWith(extension)) {
                            return true;
                        }
                    }
                    return false;
                }
            })) {
                // Now process each instance provided by the iterator.
                Reader fileReader;
                try {
                    fileReader = new InputStreamReader(new FileInputStream(f), "UTF-8");
                    // data, label, name fields
                    instances.addThruPipe(new CsvIterator(fileReader, Pattern.compile("^(\\S*)[\\s,]*(\\S*)[\\s,]*(.*)$"), 3, 2, 1));
                } catch (FileNotFoundException | UnsupportedEncodingException ex) {
                    LOGGER.error(ex.getMessage());
                }
            }
        }
    }

    private Pipe buildPipe() {
        // Begin by importing documents from text to feature sequences
        ArrayList<Pipe> pipeList = new ArrayList<>();

        // Pipes: lowercase, tokenize, remove stopwords, map to features
        pipeList.add(new CharSequenceLowercase());

        // Tokenize raw strings
        Pattern tokenPattern = Pattern.compile("\\p{L}[\\p{L}\\p{P}]+\\p{L}");
        pipeList.add(new CharSequence2TokenSequence(tokenPattern));

        // remove stopwords
        switch (language) {
            case fr:
                pipeList.add(new TokenSequenceRemoveStopwords(new File("resources/config/FR/word lists/stopwords_fr.txt"), "UTF-8", false, false, false));
                break;
            case it:
                pipeList.add(new TokenSequenceRemoveStopwords(new File("resources/config/IT/word lists/stopwords_it.txt"), "UTF-8", false, false, false));
                break;
            case nl:
                pipeList.add(new TokenSequenceRemoveStopwords(new File("resources/config/NL/word lists/stopwords_nl.txt"), "UTF-8", false, false, false));
                break;
            case ro:
                pipeList.add(new TokenSequenceRemoveStopwords(new File("resources/config/RO/word lists/stopwords_ro.txt"), "UTF-8", false, false, false));
                break;
            case la:
                pipeList.add(new TokenSequenceRemoveStopwords(new File("resources/config/LA/word lists/stopwords_la.txt"), "UTF-8", false, false, false));
                break;
            case es:
                pipeList.add(new TokenSequenceRemoveStopwords(new File("resources/config/ES/word lists/stopwords_es.txt"), "UTF-8", false, false, false));
                break;
            default:
                pipeList.add(new TokenSequenceRemoveStopwords(new File("resources/config/EN/word lists/stopwords_en.txt"), "UTF-8", false, false, false));
        }

        // Rather than storing tokens as strings, convert
        // them to integers by looking them up in an alphabet.
        pipeList.add(new TokenSequence2FeatureSequence());

        // Print out the features and the label
        // pipeList.add(new PrintInputAndTarget());
        return new SerialPipes(pipeList);
    }

    /**
     * Analyze number of topics
     *
     * @param path
     * @param initialTopics
     * @param numIterations
     * @return
     */
    public int createHDPModel(String path, int initialTopics, int numIterations) {
        LOGGER.info("Running HDP on {} with {} initial topics and {} iterations", new Object[]{path, initialTopics, numIterations});
        readDirectory(new File(path));

        HDP hdp = new HDP(path, 1.0, 0.01, 1D, initialTopics);
        hdp.initialize(instances);

        // set number of iterations, and display result or not
        hdp.estimate(numIterations);

        // get topic distribution for first instance
        // double[] distr = hdp.topicDistribution(0);
        // // print out
        //
        // int no = 0;
        // for (int j = 0; j < distr.length; j++) {
        // if (Math.round(distr[j]) != 0) {
        // System.out.print("!!" + j + "-" + distr[j] + "\n");
        // no++;
        // }
        // }
        // System.out.println(no);
        // for inferencer
        // readDirectory(new File(path));
        // HDPInferencer inferencer = hdp.getInferencer();
        // inferencer.setInstance(instances);
        //
        // inferencer.estimate(numIterations / 10);
        // // get topic distribution for first test instance
        // distr = inferencer.topicDistribution(0);
        // // print out
        // for (int j = 0; j < distr.length; j++) {
        // System.out.print(distr[j] + "\n");
        // }
        // // get preplexity
        // double prep = inferencer.getPreplexity();
        // System.out.println("preplexity for the test set=" + prep);
        // 10-folds cross validation, with 1000 iteration for each test.
        // hdp.runCrossValidation(10, 1000);
        hdp.printTopWord(100);
        return hdp.getNoTopics();
    }

    public ParallelTopicModel processCorpus(String path, int noTopics, int noThreads, int noIterations) throws IOException {

        readDirectory(new File(path));

        ParallelTopicModel model = new ParallelTopicModel(noTopics, 1.0, 0.01);

        model.addInstances(instances);

        model.setNumThreads(noThreads);

        // Run the model for X iterations and stop
        model.setNumIterations(noIterations);
        model.estimate();

        // save the trained model
        //ObjectManipulation.saveObject(model, path + "/LDA.model");
        ObjectManipulation.saveObject(buildWordVectors(model, language), path + "/lda.model");

        LDA lda = new LDA(path, language);
        lda.printTopics(100);
        return model;
    }

    private static Map<Word, double[]> buildWordVectors(ParallelTopicModel model, Lang language) {
        ArrayList<TreeSet<IDSorter>> topicSortedWords = model.getSortedWords();
        Map<Word, double[]> result = new TreeMap<>();

        for (int topic = 0; topic < model.getNumTopics(); topic++) {
            for (IDSorter idCountPair : topicSortedWords.get(topic)) {
                Word concept = Word.getWordFromConcept(model.getAlphabet().lookupObject(idCountPair.getID()).toString(),
                        language);
                if (!result.containsKey(concept)) {
                    result.put(concept, new double[model.getNumTopics()]);
                }
                result.get(concept)[topic] = idCountPair.getWeight();
            }
        }
        return result;
    }

    public double[] getWordProbDistribution(Word word) {
        double[] probDistribution = new double[numTopics];
        if (wordProbDistributions.containsKey(word)) {
            // words exist in learning space
            return wordProbDistributions.get(word);
        } else {
            // extract all words from the semantic space that have the same
            // stem
            int no = 0;
            for (Word w : wordProbDistributions.keySet()) {
                if (w.getStem().equals(word.getStem())) {
                    double[] vector = wordProbDistributions.get(w);
                    for (int i = 0; i < numTopics; i++) {
                        probDistribution[i] += vector[i];
                    }
                    no++;
                }
            }
            if (no != 0) {
                for (int i = 0; i < numTopics; i++) {
                    probDistribution[i] /= no;
                }
            }
        }
        return probDistribution;
    }

    public double[] getProbDistribution(AnalysisElement e) {
//        if (e.getWordOccurences().size() < MIN_NO_WORDS_PER_DOCUMENT) {
        double[] distrib = new double[numTopics];
        for (Entry<Word, Integer> entry : e.getWordOccurences().entrySet()) {
            double[] v = entry.getKey().getModelRepresentation(SimilarityType.LDA);
            if (v == null) {
                continue;
            }
            distrib = VectorAlgebra.sum(distrib, VectorAlgebra
                    .scalarProduct(v, (1 + Math.log(entry.getValue()))));
        }
        return VectorAlgebra.normalize(distrib);
//        }
//        return getProbDistribution(e.getProcessedText());
    }

    @Override
    public int getNoDimensions() {
        return numTopics;
    }

    @Override
    public double getSimilarity(double[] prob1, double[] prob2) {
        if (prob1 == null || prob2 == null) {
            return 0;
        }
        if (VectorAlgebra.avg(prob1) == 0 || VectorAlgebra.avg(prob2) == 0) {
            return 0;
        }
        if (VectorAlgebra.dotProduct(prob1, prob2) == 0) {
            return 0;
        }
        double sim = 1 - Maths.jensenShannonDivergence(VectorAlgebra.normalize(prob1), VectorAlgebra.normalize(prob2));
        if (sim >= 0) {
            return sim;
        }
        return 0;
    }

    @Override
    public double getSimilarity(AnalysisElement e1, AnalysisElement e2) {
        return this.getSimilarity(e1.getModelRepresentation(SimilarityType.LDA), e2.getModelRepresentation(SimilarityType.LDA));
    }

    @Override
    public TreeMap<Word, Double> getSimilarConcepts(Word w, double minThreshold) {
        double[] prob1 = getWordProbDistribution(w);
        return getSimilarConcepts(prob1, minThreshold);
    }

    @Override
    public TreeMap<Word, Double> getSimilarConcepts(AnalysisElement e, double minThreshold) {
        return getSimilarConcepts(e.getModelRepresentation(SimilarityType.LDA), minThreshold);
    }

    public TreeMap<Word, Double> getSimilarConcepts(double[] probDistribution, double minThreshold) {
        if (probDistribution == null) {
            return null;
        }
        TreeMap<Word, Double> similarConcepts = new TreeMap<>();
        double[] prob2;
        double sim;
        for (Word c : wordProbDistributions.keySet()) {
            prob2 = getWordProbDistribution(c);
            sim = getSimilarity(VectorAlgebra.normalize(probDistribution), VectorAlgebra.normalize(prob2));
            if (sim >= minThreshold) {
                similarConcepts.put(c, sim);
            }
        }
        return similarConcepts;
    }

    public void printTopics(int noWordsPerTopic) {
        LOGGER.info("Starting to write topics for trained model");
        // Get an array of sorted sets of word ID/count pairs
        try (BufferedWriter out = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(path + "/topics.bck"), "UTF-8"))) {

            // Show top <<noTopics>> concepts
            for (int topic = 0; topic < numTopics; topic++) {

                out.write(topic + "\t");
                for (Pair<Word, Double> e : sortedWords[topic].subList(0, noWordsPerTopic)) {
                    out.write(e.first.getLemma() + "(" + e.second + ") ");
                }
                out.write("\n\n");
            }
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage());
        }
        LOGGER.info("Successfully finished writing topics");
    }

    public String printTopic(int topic, int noWordsPerTopic) {
        StringBuilder result = new StringBuilder(topic + ":");
        // Get an array of sorted sets of word ID/count pairs
        for (Pair<Word, Double> e : sortedWords[topic].subList(0, noWordsPerTopic)) {
            result.append(e.first.getLemma()).append("(").append(e.second).append(") ");
        }
        return result.toString();
    }

    public static int findMaxResemblance(double[] v1, double[] v2) {
        double max = Double.MIN_VALUE;
        int maxIndex = -1;
        if (v1 == null || v2 == null || v1.length != v2.length) {
            return -1;
        }
        for (int i = 0; i < v1.length; i++) {
            if (max < v1[i] * v2[i]) {
                max = v1[i] * v2[i];
                maxIndex = i;
            }
        }
        return maxIndex;
    }

    public void findDeepLearningRules(Word w1, Word w2, double minThreshold) {
        double[] prob1 = getWordProbDistribution(w1);
        double[] prob2 = getWordProbDistribution(w2);
        double[] sum = new double[getNoDimensions()];
        double[] difference = new double[getNoDimensions()];
        for (int i = 0; i < getNoDimensions(); i++) {
            sum[i] = prob1[i] + prob2[i];
            difference[i] = Math.max(prob1[i] - prob2[i], 0);
        }
        TreeMap<Word, Double> similarSum = getSimilarConcepts(sum, minThreshold);
        if (!similarSum.isEmpty()) {
            for (Entry<Word, Double> sim : similarSum.entrySet()) {
                if (!sim.getKey().getStem().equals(w1.getStem()) && !sim.getKey().getStem().equals(w2.getStem())) {
                    System.out.println(w1.getLemma() + "+" + w2.getLemma() + ">>" + sim.getKey().getLemma() + " ("
                            + sim.getValue() + ")");
                }
            }
        }
        TreeMap<Word, Double> similarDiff = getSimilarConcepts(difference, minThreshold);
        if (!similarDiff.isEmpty()) {
            for (Entry<Word, Double> sim : similarDiff.entrySet()) {
                if (!sim.getKey().getStem().equals(w1.getStem()) && !sim.getKey().getStem().equals(w2.getStem())) {
                    System.out.println(w1.getLemma() + "-" + w2.getLemma() + ">>" + sim.getKey().getLemma() + " ("
                            + sim.getValue() + ")");
                }
            }
        }
    }

    @Override
    public Lang getLanguage() {
        return language;
    }

    public void setLanguage(Lang language) {
        this.language = language;
    }

    @Override
    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Pipe getPipe() {
        return pipe;
    }

    public void setPipe(Pipe pipe) {
        this.pipe = pipe;
    }

    @Override
    public Map<Word, double[]> getWordRepresentations() {
        return wordProbDistributions;
    }

    @Override
    public double[] getWordRepresentation(Word w) {
        return wordProbDistributions.get(w);
    }

    public List<Pair<Word, Double>>[] getSortedWords() {
        return sortedWords;
    }

    @Override
    public Set<Word> getWordSet() {
        return wordProbDistributions.keySet();
    }

    public void setWordProbDistributions(Map<Word, double[]> wordProbDistributions) {
        this.wordProbDistributions = wordProbDistributions;
    }

    public static Set<Lang> getAvailableLanguages() {
        return AVAILABLE_FOR;
    }

    @Override
    public SimilarityType getType() {
        return SimilarityType.LDA;
    }

    @Override
    public BiFunction<double[], double[], Double> getSimilarityFuction() {
        return (v1, v2) -> 1 - Maths.jensenShannonDivergence(VectorAlgebra.normalize(v1), VectorAlgebra.normalize(v2));
    }

    public static void convertModels() {
        File root = new File("resources/config");
        for (File lang : root.listFiles(file -> !file.getName().startsWith("."))) {
            try {
                File ldaFolder = lang.listFiles(file -> file.getName().equals("LDA"))[0];
                for (File folder : ldaFolder.listFiles(file -> !file.getName().startsWith(".") && !file.getName().equals("INL"))) {
                    ParallelTopicModel model = (ParallelTopicModel) ObjectManipulation.loadObject(folder.getPath() + "/LDA.model");
                    Map<Word, double[]> vectors = buildWordVectors(model, Lang.valueOf(lang.getName().toLowerCase()));
                    ObjectManipulation.saveObject(vectors, folder.getPath() + "/lda.model");
                    System.out.println("Converted: " + folder.getPath());
                }
            } catch (IOException | ClassNotFoundException ex) {
                LOGGER.error(ex.getMessage());
            }
        }
    }

    public static void main(String[] args) throws IOException, ClassNotFoundException {
        convertModels();
//        ParallelTopicModel model = (ParallelTopicModel) ObjectManipulation.loadObject("resources/config/NL/LDA/INL/LDA.model");
//        Map<Word, double[]> vectors = buildWordVectors(model, Lang.nl);
//        ObjectManipulation.saveObject(vectors, "resources/config/NL/LDA/INL/lda.model");
    }
}