package services.comprehensionModel.utils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import services.comprehensionModel.utils.indexer.graphStruct.CMNodeDO;
import services.nlp.lemmatizer.StaticLemmatizer;
import data.AbstractDocument;
import data.Lang;
import data.Sentence;
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
	public List<Word> getContentWordListFromSemanticGraph(Sentence sentence, Lang lang) {
		HashSet<Word> wordset = new HashSet<Word>();
		SemanticGraph semanticGraph = sentence.getDependencies();
		
		Map<Word, Word> pronomialReplMap = sentence.getPronimialReplacementMap();
		
		for (SemanticGraphEdge edge : semanticGraph.edgeListSorted()) {
			Word dependentNode = this.convertToWord(edge.getDependent(), lang);
			Word governorNode = this.convertToWord(edge.getGovernor(), lang);
			
			if(pronomialReplMap.containsKey(dependentNode)) {
				dependentNode = pronomialReplMap.get(dependentNode);
			}
			
			if(pronomialReplMap.containsKey(governorNode)) {
				governorNode = pronomialReplMap.get(governorNode);
			}
			
			if(dependentNode.isContentWord() && governorNode.isContentWord()){
				wordset.add(dependentNode);
				wordset.add(governorNode);
			}
		}
		
		return this.convertIteratorToList(wordset.iterator());
	}
	private List<Word> convertIteratorToList(Iterator<Word> wordIterator) {
		List<Word> wordList = new ArrayList<Word>();
		while(wordIterator.hasNext()) {
			Word w = wordIterator.next();
			if(w.isContentWord()) {
				wordList.add(w);
			}
		}
		return wordList;
	}
	public List<String> convertStringIteratorToList(Iterator<String> wordIterator) {
		List<String> wordList = new ArrayList<String>();
		while(wordIterator.hasNext()) {
			String w = wordIterator.next();
			wordList.add(w);
		}
		return wordList;
	}
	public List<CMNodeDO> convertNodeIteratorToList(Iterator<CMNodeDO> nodeIterator) {
		List<CMNodeDO> nodeList = new ArrayList<CMNodeDO>();
		while(nodeIterator.hasNext()) {
			CMNodeDO node = nodeIterator.next();
			if(node.word.isContentWord()) {
				nodeList.add(node);
			}
		}
		return nodeList;
	}
}