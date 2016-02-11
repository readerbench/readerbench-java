/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package services.semanticModels.word2vec;

import data.AnalysisElement;
import data.Word;
import edu.cmu.lti.jawjaw.pobj.Lang;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;
import org.deeplearning4j.models.embeddings.loader.WordVectorSerializer;
import org.deeplearning4j.models.word2vec.Word2Vec;
import services.nlp.stemmer.Stemmer;
import services.semanticModels.ISemanticModel;

/**
 *
 * @author Stefan
 */
public class Word2VecModel implements ISemanticModel{
	private static final String MODEL = "resources/config/Word2Vec/GoogleNews-vectors-negative300.bin";
	
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
	
}
