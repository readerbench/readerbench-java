/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package services.semanticModels.paragraphVectors;

import data.AnalysisElement;
import data.Lang;
import data.Word;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.TreeMap;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.uima.resource.ResourceInitializationException;
import org.deeplearning4j.models.embeddings.WeightLookupTable;
import org.deeplearning4j.models.embeddings.loader.WordVectorSerializer;
import org.deeplearning4j.models.paragraphvectors.ParagraphVectors;
import org.deeplearning4j.text.sentenceiterator.SentenceIterator;
import org.deeplearning4j.text.sentenceiterator.UimaSentenceIterator;
import org.deeplearning4j.text.tokenization.tokenizer.preprocessor.CommonPreprocessor;
import org.deeplearning4j.text.tokenization.tokenizerfactory.DefaultTokenizerFactory;
import org.deeplearning4j.text.tokenization.tokenizerfactory.TokenizerFactory;
import org.deeplearning4j.text.tokenization.tokenizerfactory.UimaTokenizerFactory;
import org.deeplearning4j.text.uima.UimaResource;
import org.openide.util.Exceptions;
import services.commons.VectorAlgebra;
import services.nlp.stemmer.Stemmer;
import services.semanticModels.ISemanticModel;
import services.semanticModels.SimilarityType;

/**
 *
 * @author Simona Roboiu
 */
public class ParagraphVectorsModel implements ISemanticModel {

    static final Logger LOGGER = Logger.getLogger("");
    private static final List<ParagraphVectorsModel> LOADED_PARAGRAPHVECTORS_MODELS = new ArrayList<>();
    private static final Set<Lang> AVAILABLE_FOR = EnumSet.of(Lang.en);

    private final Lang language;
    private final String path;
    private final int noDimensions;
    private final Map<Word, double[]> wordVectors;
    //private final Map<AbstractDocument, double[]> paragraphVectors;
    
    public static WeightLookupTable weightLookupTable;

    private ParagraphVectorsModel(String path, Lang language, ParagraphVectors paragraphVectors) {
        this.language = language;
        this.path = path;
        this.wordVectors = paragraphVectors.vocab().words().stream()
                .map(w -> new Word(w, w, Stemmer.stemWord(w, language), null, null, language))
                .distinct()
                .collect(Collectors.toMap(
                        Function.identity(),
                        w -> paragraphVectors.getWordVector(w.getLemma())));
        this.noDimensions = paragraphVectors.getLayerSize();
        //this.paragraphVectors = null;
        weightLookupTable = paragraphVectors.getLookupTable();

    }

    private static ParagraphVectors loadParagraphVectors(String path) {
        try {
            LOGGER.log(Level.INFO, "Loading ParagraphVectors model {0} ...", path);
            return WordVectorSerializer.readParagraphVectors(path + "/paragraphVectors.model");            
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
        ParagraphVectorsModel pvm = new ParagraphVectorsModel(path, language, loadParagraphVectors(path));
        LOADED_PARAGRAPHVECTORS_MODELS.add(pvm);
        return pvm;
    }

    public static ParagraphVectorsModel loadGoogleNewsModel() throws IOException {
        String path = "resources/config/EN/paragraphVectors/Google news";
        for (ParagraphVectorsModel pv : LOADED_PARAGRAPHVECTORS_MODELS) {
            if (path.equals(pv.getPath())) {
                return pv;
            }
        }
        ParagraphVectorsModel pvm = new ParagraphVectorsModel(path, Lang.en, WordVectorSerializer.readParagraphVectors(path + "/GoogleNews-vectors-negative300.bin"));
        return pvm;
    }

    public static void trainModel(String inputFile) {
        try {
            SentenceIterator iterator = new UimaSentenceIterator(inputFile,
                    new UimaResource(UimaTokenizerFactory.defaultAnalysisEngine()));
            TokenizerFactory t = new DefaultTokenizerFactory();
            t.setTokenPreProcessor(new CommonPreprocessor());
            LOGGER.info("Building paragraph vectors model ...");
            ParagraphVectors paragraphVectors = new ParagraphVectors.Builder()
                    .learningRate(0.025)
                    .minLearningRate(0.001)
                    .batchSize(900)
                    .minWordFrequency(5)
                    .epochs(10)
                    .iterate(iterator)
                    .layerSize(100)
                    .seed(42)
                    .windowSize(5)
                    .negativeSample(10)
                    .tokenizerFactory(t)
                    .build();
            paragraphVectors.fit();

            LOGGER.info("Writing word vectors to text file ...");
            String outputPath = new File(inputFile).getParentFile().getAbsolutePath() + "/paragraphVectors.model";
            WordVectorSerializer.writeParagraphVectors(paragraphVectors, outputPath);
        } catch (ResourceInitializationException ex) {
            Exceptions.printStackTrace(ex);
        }
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

}
