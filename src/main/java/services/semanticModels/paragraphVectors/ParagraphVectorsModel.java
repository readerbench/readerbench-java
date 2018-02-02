/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package services.semanticModels.paragraphVectors;

import data.AnalysisElement;
import data.Lang;
import data.Word;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.TreeMap;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.logging.Logger;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;
import org.openide.util.Exceptions;
import services.commons.VectorAlgebra;
import services.nlp.stemmer.Stemmer;
import services.semanticModels.ISemanticModel;
import services.semanticModels.SimilarityType;
import thrift.ParagraphVectorsService;

/**
 *
 * @author Simona Roboiu
 */
public class ParagraphVectorsModel implements ISemanticModel {

    static final Logger LOGGER = Logger.getLogger("");
    private static final List<ParagraphVectorsModel> LOADED_PARAGRAPHVECTORS_MODELS = new ArrayList<>();
    private static final Set<Lang> AVAILABLE_FOR = EnumSet.of(Lang.en);
    private static final String THRIFT_IP = "141.85.227.62";
    private static final int THRIFT_PORT = 9090;

    private final Lang language;
    private final String path;
    private final int noDimensions;
    private final Map<Word, double[]> wordVectors;
    
    private static ParagraphVectorsService.Client client = null;

    private ParagraphVectorsModel(String path, Lang language, Map<String, List<Double>> model) {
        this.language = language;
        this.path = path;
        this.wordVectors = model.keySet().stream()
                .map(w -> new Word(w, w, Stemmer.stemWord(w, language), null, null, language))
                .collect(Collectors.toMap(
                        Function.identity(),
                        w -> model.get(w.getLemma()).stream().mapToDouble(d -> d).toArray()));
        this.noDimensions = wordVectors.values().stream()
                .findFirst()
                .map(v -> v.length)
                .get();
    }
    
    private ParagraphVectorsModel(String path, Lang language, int dim) {
        this.language = language;
        this.path = path;
        this.wordVectors = new HashMap<>();
        this.noDimensions = dim;
    }

    public static ParagraphVectorsModel loadFromTextFile(String path, Lang language) {
        try (BufferedReader in = new BufferedReader(new FileReader(path))) {
            String[] line = in.readLine().split(" ");
            int nWords = Integer.parseInt(line[0]);
            int dim = Integer.parseInt(line[1]);
            ParagraphVectorsModel model = new ParagraphVectorsModel(path, language, dim);
            for (int i = 0; i < nWords; i++) {
                line = in.readLine().split(" ");
                String label = line[0];
                Word word = new Word(label, label, Stemmer.stemWord(label, language), null, null, language);
                model.wordVectors.put(word, Arrays.stream(line, 1, line.length)
                        .mapToDouble(Double::parseDouble)
                        .toArray());
            }
            return model;
        } catch (FileNotFoundException ex) {
            Exceptions.printStackTrace(ex);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        return null;
    }
    
    public static ParagraphVectorsModel loadParagraphVectors(String path, Lang language) {
        for (ParagraphVectorsModel pv : LOADED_PARAGRAPHVECTORS_MODELS) {
            if (path.equals(pv.getPath())) {
                return pv;
            }
        }
        ParagraphVectorsModel pv = loadFromTextFile(path + "/paragraphVectors.model", language);
        LOADED_PARAGRAPHVECTORS_MODELS.add(pv);
        return pv;
    }

    @Override
    public double getSimilarity(AnalysisElement e1, AnalysisElement e2) {
        return this.getSimilarity(e1.getModelRepresentation(SimilarityType.PARAGRAPHVECTORS), e2.getModelRepresentation(SimilarityType.PARAGRAPHVECTORS));

    }

    @Override
    public double getSimilarity(double[] v1, double[] v2) {
        return VectorAlgebra.cosineSimilarity(v1, v2);
    }

    @Override
    public String getPath() {
        return path;
    }

    @Override
    public Lang getLanguage() {
        return language;
    }

    public static Set<Lang> getAvailableLanguages() {
        return AVAILABLE_FOR;
    }

    @Override
    public SimilarityType getType() {
        return SimilarityType.PARAGRAPHVECTORS;
    }

    @Override
    public BiFunction<double[], double[], Double> getSimilarityFuction() {
        return VectorAlgebra::cosineSimilarity;
    }

    @Override
    public int getNoDimensions() {
        return noDimensions;
    }

    @Override
    public TreeMap<Word, Double> getSimilarConcepts(Word w, double minThreshold) {
        return getSimilarConcepts(w.getModelRepresentation(SimilarityType.PARAGRAPHVECTORS), minThreshold);
    }

    @Override
    public TreeMap<Word, Double> getSimilarConcepts(AnalysisElement e, double minThreshold) {
        return getSimilarConcepts(e.getModelRepresentation(SimilarityType.PARAGRAPHVECTORS), minThreshold);
    }

    private TreeMap<Word, Double> getSimilarConcepts(double[] vector, double minThreshold) {
        TreeMap<Word, Double> similarConcepts = new TreeMap<>();
        double sim;
        for (Map.Entry<Word, double[]> entry : wordVectors.entrySet()) {
            sim = VectorAlgebra.cosineSimilarity(vector, entry.getValue());
            if (sim >= minThreshold) {
                similarConcepts.put(entry.getKey(), sim);
            }
        }
        return similarConcepts;
    }
    
    @Override
    public Set<Word> getWordSet() {
        return wordVectors.keySet();
    }

    @Override
    public Map<Word, double[]> getWordRepresentations() {
        return wordVectors;
    }

    @Override
    public double[] getWordRepresentation(Word w) {
        return wordVectors.get(w);
    }

    public static void trainModel(String inputFile) throws FileNotFoundException {
        trainModel(inputFile, 6, 300);
    }
    
    
    public static void trainModel(String inputFile, int noEpochs, int layerSize) throws FileNotFoundException {
        if (!inputFile.startsWith("resources")) {
            if (inputFile.contains("resources" + File.separatorChar)) {
                inputFile = inputFile.replaceAll(".*resources", "resources").replaceAll("[\\\\/]", "/");
            }
            else {
                inputFile = inputFile.replaceAll(".*ReaderBench\\/", "resources/");
            }
        }
        try {
            getClient().trainModel(inputFile, noEpochs, layerSize);
            client = null;
            LOGGER.info("Input file sent for training");
        } catch (TException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
    public static ParagraphVectorsService.Client getClient() throws TTransportException {
        if (client == null) {
            TTransport transport = new TSocket(THRIFT_IP, THRIFT_PORT);
            transport.open();
            TProtocol protocol = new TBinaryProtocol(transport);
            client = new ParagraphVectorsService.Client(protocol);
        }
        return client;
    }
    
    public static void main(String[] args) throws FileNotFoundException {
        
//        trainModel("resources/config/EN/TasaHClustering/train.txt");
//        ParagraphVectorsModel pv = ParagraphVectorsModel.loadParagraphVectors("resources/config/EN/TasaHClustering", Lang.en);
//        System.out.println(pv.getNoDimensions());
//        System.out.println(pv.getWordSet().size());
        try {
            System.out.println(getClient().loadModel("resources/config/EN/TasaHClustering"));
        } catch (TTransportException ex) {
            Exceptions.printStackTrace(ex);
        } catch (TException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
}
