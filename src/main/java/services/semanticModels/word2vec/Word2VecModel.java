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

import java.io.File;
import java.io.IOException;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.logging.Logger;


import org.deeplearning4j.models.embeddings.loader.WordVectorSerializer;
import org.deeplearning4j.models.word2vec.Word2Vec;
import org.deeplearning4j.text.sentenceiterator.BasicLineIterator;
import org.deeplearning4j.text.sentenceiterator.SentenceIterator;
import org.deeplearning4j.text.tokenization.tokenizer.preprocessor.CommonPreprocessor;
import org.deeplearning4j.text.tokenization.tokenizerfactory.DefaultTokenizerFactory;
import org.deeplearning4j.text.tokenization.tokenizerfactory.TokenizerFactory;

import data.AnalysisElement;
import data.Word;
import java.io.FileNotFoundException;
import java.util.EnumSet;
import java.util.Map;
import services.nlp.stemmer.Stemmer;
import services.semanticModels.ISemanticModel;
import data.Lang;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.logging.Level;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.resource.ResourceInitializationException;
import org.deeplearning4j.text.annotator.SentenceAnnotator;
import org.deeplearning4j.text.annotator.TokenizerAnnotator;
import org.deeplearning4j.text.sentenceiterator.UimaSentenceIterator;
import org.deeplearning4j.text.tokenization.tokenizerfactory.UimaTokenizerFactory;
import org.deeplearning4j.text.uima.UimaResource;
import org.openide.util.Exceptions;
import services.commons.VectorAlgebra;
import services.semanticModels.SimilarityType;

/**
 *
 * @author Stefan Ruseti, Mihai Dascalu
 */
public class Word2VecModel implements ISemanticModel {

    static final Logger LOGGER = Logger.getLogger("");
    private static final List<Word2VecModel> LOADED_WORD2VEC_MODELS = new ArrayList<>();
    private static final Set<Lang> AVAILABLE_FOR = EnumSet.of(Lang.en);

    private final Lang language;
    private final String path;
    private final int noDimensions;
    private final Map<Word, double[]> wordVectors;

    private Word2VecModel(String path, Lang language, Word2Vec word2vec) {
        this.language = language;
        this.path = path;
        this.wordVectors = word2vec.vocab().words().stream()
                .map(w -> new Word(w, w, Stemmer.stemWord(w, language), null, null, language))
                .distinct()
                .collect(Collectors.toMap(
                        Function.identity(),
                        w -> word2vec.getWordVector(w.getLemma())));
        this.noDimensions = word2vec.getLayerSize();
    }

    private static Word2Vec loadWord2Vec(String path) {
        LOGGER.log(Level.INFO, "Loading word2vec model {0} ...", path);
        Word2Vec word2Vec = WordVectorSerializer.readWord2VecModel(path + "/word2vec.model");
        return word2Vec;
    }

    public static Word2VecModel loadWord2Vec(String path, Lang language) {
        for (Word2VecModel w2v : LOADED_WORD2VEC_MODELS) {
            if (path.equals(w2v.getPath())) {
                return w2v;
            }
        }
        Word2VecModel w2v = new Word2VecModel(path, language, loadWord2Vec(path));
        LOADED_WORD2VEC_MODELS.add(w2v);

        return w2v;
    }

    public static Word2VecModel loadGoogleNewsModel() {
        String path = "resources/config/EN/word2vec/Google news";
        for (Word2VecModel w2v : LOADED_WORD2VEC_MODELS) {
            if (path.equals(w2v.getPath())) {
                return w2v;
            }
        }
        Word2VecModel w2vm = new Word2VecModel(path, Lang.en, WordVectorSerializer.readWord2VecModel(path + "/GoogleNews-vectors-negative300.bin"));
        return w2vm;
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
        try {
            SentenceIterator iter = new UimaSentenceIterator(inputFile,
                    new UimaResource(UimaTokenizerFactory.defaultAnalysisEngine()));
            // Split on white spaces in the line to get words
            TokenizerFactory t = new DefaultTokenizerFactory();
            t.setTokenPreProcessor(new CommonPreprocessor());
            LOGGER.info("Building word2vec model ...");
            Word2Vec word2Vec = new Word2Vec.Builder()
                    .batchSize(100)
                    .minWordFrequency(5)
                    .epochs(6)
                    .layerSize(300)
                    .seed(42)
                    .windowSize(5)
                    .negativeSample(10)
                    .iterate(iter)
                    .tokenizerFactory(t)
                    .build();
            word2Vec.fit();
            
            LOGGER.info("Writing word vectors to text file ...");
            String outputPath = new File(inputFile).getParentFile().getAbsolutePath() + "/word2vec.model";
            WordVectorSerializer.writeWord2VecModel(word2Vec, outputPath);
        } catch (ResourceInitializationException ex) {
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
}
