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
package services.semanticModels.word2vec;

import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.logging.Logger;



import data.AnalysisElement;
import data.Word;
import java.io.FileNotFoundException;
import java.util.EnumSet;
import java.util.Map;
import services.nlp.stemmer.Stemmer;
import services.semanticModels.ISemanticModel;
import data.Lang;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.function.BiFunction;
import java.util.function.Function;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;
import org.openide.util.Exceptions;
import services.commons.VectorAlgebra;
import services.semanticModels.SimilarityType;
import thrift.Word2VecService;

/**
 *
 * @author Stefan Ruseti, Mihai Dascalu
 */
public class Word2VecModel implements ISemanticModel {
    
    static final Logger LOGGER = Logger.getLogger("");
    private static final List<Word2VecModel> LOADED_WORD2VEC_MODELS = new ArrayList<>();
    private static final Set<Lang> AVAILABLE_FOR = EnumSet.of(Lang.en);
    private static final String THRIFT_IP = "141.85.227.62";
    private static final int THRIFT_PORT = 9090;
    
    private final Lang language;
    private final String path;
    private final int noDimensions;
    private final Map<Word, double[]> wordVectors;
    
    private static Word2VecService.Client client = null;

    private Word2VecModel(String path, Lang language, Map<String, List<Double>> model) {
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
    
    private Word2VecModel(String path, Lang language, int dim) {
        this.language = language;
        this.path = path;
        this.wordVectors = new HashMap<>();
        this.noDimensions = dim;
    }

    public static Word2VecModel loadFromTextFile(String path, Lang language) {
        try (BufferedReader in = new BufferedReader(new FileReader(path))) {
            String[] line = in.readLine().split(" ");
            int nWords = Integer.parseInt(line[0]);
            int dim = Integer.parseInt(line[1]);
            Word2VecModel model = new Word2VecModel(path, language, dim);
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
    
    public static Word2VecModel loadWord2Vec(String path, Lang language) {
        for (Word2VecModel w2v : LOADED_WORD2VEC_MODELS) {
            if (path.equals(w2v.getPath())) {
                return w2v;
            }
        }
        Word2VecModel w2v = loadFromTextFile(path + "/word2vec.model", language);
        LOADED_WORD2VEC_MODELS.add(w2v);
        return w2v;
    }

    @Override
    public double getSimilarity(double[] v1, double[] v2) {
        return VectorAlgebra.cosineSimilarity(v1, v2);
    }

    @Override
    public double getSimilarity(AnalysisElement e1, AnalysisElement e2) {
        return this.getSimilarity(e1.getModelRepresentation(SimilarityType.WORD2VEC), e2.getModelRepresentation(SimilarityType.WORD2VEC));
    }

    @Override
    public TreeMap<Word, Double> getSimilarConcepts(Word w, double minThreshold) {
        return getSimilarConcepts(w.getModelRepresentation(SimilarityType.WORD2VEC), minThreshold);
    }

    @Override
    public TreeMap<Word, Double> getSimilarConcepts(AnalysisElement e, double minThreshold) {
        return getSimilarConcepts(e.getModelRepresentation(SimilarityType.WORD2VEC), minThreshold);
    }

    private TreeMap<Word, Double> getSimilarConcepts(double[] vector, double minThreshold) {
        TreeMap<Word, Double> similarConcepts = new TreeMap<>();
        double sim;
        for (Entry<Word, double[]> entry : wordVectors.entrySet()) {
            sim = VectorAlgebra.cosineSimilarity(vector, entry.getValue());
            if (sim >= minThreshold) {
                similarConcepts.put(entry.getKey(), sim);
            }
        }
        return similarConcepts;
    }

    @Override
    public Map<Word, double[]> getWordRepresentations() {
        return wordVectors;
    }

    @Override
    public double[] getWordRepresentation(Word w) {
        return wordVectors.get(w);
    }

    @Override
    public Set<Word> getWordSet() {
        return wordVectors.keySet();
    }

    @Override
    public String getPath() {
        return path;
    }

    @Override
    public Lang getLanguage() {
        return language;
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

    public static Set<Lang> getAvailableLanguages() {
        return AVAILABLE_FOR;
    }

    @Override
    public SimilarityType getType() {
        return SimilarityType.WORD2VEC;
    }

    @Override
    public BiFunction<double[], double[], Double> getSimilarityFuction() {
        return VectorAlgebra::cosineSimilarity;
    }

    @Override
    public int getNoDimensions() {
        return noDimensions;
    }
    
    public static Word2VecService.Client getClient() throws TTransportException {
        if (client == null) {
            TTransport transport = new TSocket(THRIFT_IP, THRIFT_PORT);
            transport.open();
            TProtocol protocol = new TBinaryProtocol(transport);
            client = new Word2VecService.Client(protocol);
        }
        return client;
    }
    
    public static void main(String[] args) throws FileNotFoundException {
        
        trainModel("C:\\Git\\ReaderBench\\resources\\corpora\\ES\\Corpus Jose Antonio\\Corpus Jose Antonio.txt");
//        Word2VecModel w2v = Word2VecModel.loadWord2Vec("resources/config/EN/word2vec/COCA", Lang.en);
//        System.out.println(w2v.getNoDimensions());
//        System.out.println(w2v.getWordSet().size());
    }
}
