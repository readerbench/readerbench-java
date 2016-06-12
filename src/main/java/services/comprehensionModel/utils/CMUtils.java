package services.comprehensionModel.utils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import services.comprehensionModel.utils.indexer.graphStruct.CiNodeDO;
import services.nlp.lemmatizer.StaticLemmatizer;
import data.AbstractDocument;
import data.Lang;
import data.Word;
import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphEdge;

public class CMUtils {
	public Word convertToWord(IndexedWord node, Lang lang) {
		String wordStr = node.word().toLowerCase();
		Word word = Word.getWordFromConcept(wordStr, lang);
		word.setLemma(StaticLemmatizer.lemmaStatic(wordStr, lang));
		word.setPOS("");
		if(node.tag() != null && node.tag().length() >= 2) {
			word.setPOS(node.tag().substring(0, 2));
		}
		return word;
	}
	public Word convertStringToWord(String wordString, Lang lang) {
		Word word = Word.getWordFromConcept(wordString, lang);
		word.setLemma(StaticLemmatizer.lemmaStatic(wordString, lang));
		word.setPOS("");
		return word;
	}
	public List<Word> getContentWordListFromDocument(AbstractDocument document) {
		return this.convertIteratorToList(document.getWordOccurences().keySet().iterator());
	}
	public List<Word> getContentWordListFromSemanticGraph(SemanticGraph semanticGraph, Lang lang) {
		HashSet<Word> wordset = new HashSet<Word>();
		
		for (SemanticGraphEdge edge : semanticGraph.edgeListSorted()) {
			Word dependentEdge = this.convertToWord(edge.getDependent(), lang);
			Word governorEdge = this.convertToWord(edge.getGovernor(), lang);
			
			if(dependentEdge.isContentWord() && governorEdge.isContentWord()){
				wordset.add(dependentEdge);
				wordset.add(governorEdge);
			}
		}
		return this.convertIteratorToList(wordset.iterator());
	}
	public List<Word> convertIteratorToList(Iterator<Word> wordIterator) {
		List<Word> wordList = new ArrayList<Word>();
		while(wordIterator.hasNext()) {
			Word w = wordIterator.next();
			if(w.isContentWord()) {
				wordList.add(w);
			}
		}
		return wordList;
	}
	public List<CiNodeDO> convertNodeIteratorToList(Iterator<CiNodeDO> nodeIterator) {
		List<CiNodeDO> nodeList = new ArrayList<CiNodeDO>();
		while(nodeIterator.hasNext()) {
			CiNodeDO node = nodeIterator.next();
			if(node.word.isContentWord()) {
				nodeList.add(node);
			}
		}
		return nodeList;
	}
}