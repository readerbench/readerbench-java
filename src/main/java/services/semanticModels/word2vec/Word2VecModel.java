package services.semanticModels.word2vec;

import java.io.File;
import java.io.IOException;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;
import org.deeplearning4j.models.embeddings.loader.WordVectorSerializer;
import org.deeplearning4j.models.word2vec.Word2Vec;
import org.deeplearning4j.text.sentenceiterator.BasicLineIterator;
import org.deeplearning4j.text.sentenceiterator.SentenceIterator;
import org.deeplearning4j.text.tokenization.tokenizer.preprocessor.CommonPreprocessor;
import org.deeplearning4j.text.tokenization.tokenizerfactory.DefaultTokenizerFactory;
import org.deeplearning4j.text.tokenization.tokenizerfactory.TokenizerFactory;

import data.AnalysisElement;
import data.Lang;
import data.Word;
import services.commons.ObjectManipulation;
import services.nlp.stemmer.Stemmer;
import services.semanticModels.ISemanticModel;
import services.semanticModels.LDA.LDA;

/**
 *
 * @author Stefan
 */
public class Word2VecModel implements ISemanticModel{
	private static final String MODEL = "resources/config/Word2Vec/GoogleNews-vectors-negative300.bin";
	static Logger logger = Logger.getLogger(LDA.class);
	 
	private Word2Vec word2vec;
	private Lang lang = Lang.eng;
	
	public static Word2VecModel load() throws IOException {
		return load(MODEL);
	}
	
	public static Word2VecModel load(String fileName) throws IOException {
		File gModel = new File(fileName);
		Word2VecModel w2vm = new Word2VecModel();
		w2vm.word2vec = (Word2Vec) WordVectorSerializer.loadGoogleModel(gModel, true);
		return w2vm;
	}
	
	@Override
	public double getSimilarity(Word w1, Word w2) {
		return word2vec.similarity(w1.getText(), w2.getText());
	}

	@Override
	public double getSimilarity(AnalysisElement e1, AnalysisElement e2) {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public TreeMap<Word, Double> getSimilarConcepts(Word w, double minThreshold) {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public TreeMap<Word, Double> getSimilarConcepts(AnalysisElement e, double minThreshold) {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public Set<Word> getWordSet() {
		return word2vec.vocab().words().stream()
				.map(w -> new Word(w, w, Stemmer.stemWord(w, lang), null, null, lang))
				.collect(Collectors.toSet());
	}

	@Override
	public String getPath() {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public Lang getLanguage() {
		return lang;
	}
	
    public void processCorpus(String path) throws IOException {
    	
    	File corpusDir = new File(path);
    	String corpusFileName = "";
    	
    	if (corpusDir.isDirectory()) {
    		for (String fileName : corpusDir.list()) {
    			if (fileName.startsWith("alltexts")) {
    				corpusFileName = path + "/" + fileName;
    			}
    		}
    	}
    	
        logger.info("Corpus file is: " + corpusFileName);
        // Strip white space before and after for each line
        SentenceIterator iter = new BasicLineIterator(corpusFileName);
        // Split on white spaces in the line to get words
        TokenizerFactory t = new DefaultTokenizerFactory();
        t.setTokenPreProcessor(new CommonPreprocessor());

        logger.info("Building word2vec model");
        Word2Vec word2Vec = new Word2Vec.Builder()
                .minWordFrequency(5)
                .iterations(1)
                .layerSize(300)
                .seed(42)
                .windowSize(5)
                .negativeSample(10)
                .iterate(iter)
                .tokenizerFactory(t)
                .build();

        word2Vec.fit();

        logger.info("Writing word vectors to text file....");

        // save the trained model
        ObjectManipulation.saveObject(word2Vec, path + "/word2vec.model");
    }
    
    public static Word2Vec loadWord2Vec(String path) {
        Word2Vec word2Vec = null;
        try {
        	word2Vec = (Word2Vec) ObjectManipulation.loadObject(path + "/word2vec.model");
        } catch (Exception e) {
        	logger.error(e);
        }
        return word2Vec;
    }
}