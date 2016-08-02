package services.comprehensionModel.utils.indexer;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import services.comprehensionModel.utils.CMUtils;
import services.comprehensionModel.utils.distanceStrategies.FullSemanticSpaceWordDistanceStrategy;
import services.comprehensionModel.utils.distanceStrategies.SyntacticWordDistanceStrategy;
import services.comprehensionModel.utils.indexer.graphStruct.CMNodeDO;
import services.comprehensionModel.utils.indexer.graphStruct.CMNodeType;
import services.semanticModels.ISemanticModel;
import data.AbstractDocument;
import data.AbstractDocumentTemplate;
import data.Block;
import data.Lang;
import data.Sentence;
import data.Word;
import data.document.Document;
import edu.stanford.nlp.hcoref.data.CorefChain;
import edu.stanford.nlp.hcoref.data.Dictionaries;
import edu.stanford.nlp.semgraph.SemanticGraph;

public class QueryIndexer {
	public static String LsaPath = "resources/config/LSA/tasa_en";
	public static String LdaPath = "resources/config/LDA/tasa_en";
	public static Lang lang = Lang.eng;
	
	private ISemanticModel semanticModel;
	private int noTopSimilarWords;
	private String text;
	public AbstractDocument document;
	
	private CMUtils cMUtils;
	private WordDistanceIndexer semanticIndexer;
	private List<WordDistanceIndexer> syntacticIndexerList;
	private Map<CMNodeDO, Double> nodeActivationScoreMap;
	
	public QueryIndexer(String text, ISemanticModel semanticModel, int noTopSimilarWords) {
		this.noTopSimilarWords = noTopSimilarWords;
		this.text = text;
		this.semanticModel = semanticModel;
		this.cMUtils = new CMUtils();
		this.nodeActivationScoreMap = new TreeMap<CMNodeDO, Double>();
		this.loadDocument();
		this.indexFullSemanticSpaceDistances();
		this.indexSyntacticDistances();
	}
	private void loadDocument() {
		AbstractDocumentTemplate contents = AbstractDocumentTemplate.getDocumentModel(this.text);
		this.document = new Document(contents, this.semanticModel, true, false);
	}
	
	private void indexFullSemanticSpaceDistances() {
		FullSemanticSpaceWordDistanceStrategy wdStrategy = new FullSemanticSpaceWordDistanceStrategy(this.semanticModel, this.noTopSimilarWords);
		
		this.semanticIndexer = new WordDistanceIndexer(wdStrategy.getWordList(), wdStrategy);
		this.addWordListToWordActivationScoreMap(this.semanticIndexer.wordList);
	}
	
	private void indexSyntacticDistances() {
		List<Block> blockList = this.document.getBlocks();
		for(Block block : blockList) {
			List<Sentence> sentenceList = block.getSentences();
			Map<Integer, CorefChain> blockCorefs =  block.getCorefs();
			blockCorefs.keySet().forEach(corefIndex -> {
				CorefChain chain = blockCorefs.get(corefIndex);
				
				for(CorefChain.CorefMention mention : chain.getMentionsInTextualOrder()) {
					if(mention.mentionType == Dictionaries.MentionType.PRONOMINAL) {
						String referenceWord = "xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx";
						for(CorefChain.CorefMention inner : chain.getMentionsInTextualOrder()) {
							if(inner.mentionType == Dictionaries.MentionType.NOMINAL && inner.mentionSpan.length() < referenceWord.length()) {
								referenceWord = inner.mentionSpan;
							}
						}
						System.out.println(mention.mentionSpan);
						System.out.println(referenceWord);
						
						//block.getStanfordSentences().get(mention.sentNum).
						
						System.out.println("-------------------------");
						
						sentenceList.get(mention.sentNum - 1).addPronimialReplacement(Word.getWordFromConcept(mention.mentionSpan, lang) , 
								Word.getWordFromConcept(referenceWord, lang));
					}
				}
			});
		}
		
		List<Sentence> sentenceList = this.document.getSentencesInDocument();
		Iterator<Sentence> sentenceIterator = sentenceList.iterator();
		this.syntacticIndexerList = new ArrayList<WordDistanceIndexer>();
		int sentenceNum = 0;
		while(sentenceIterator.hasNext()) {
			Sentence sentence = sentenceIterator.next();
			
			SemanticGraph semanticGraph = sentence.getDependencies();
			List<Word> wordList = this.cMUtils.getContentWordListFromSemanticGraph(sentence, QueryIndexer.lang);
			SyntacticWordDistanceStrategy syntacticStrategy = new SyntacticWordDistanceStrategy(semanticGraph, QueryIndexer.lang);
			
			WordDistanceIndexer wdIndexer = new WordDistanceIndexer(wordList, syntacticStrategy);
			this.syntacticIndexerList.add(wdIndexer);
			this.addWordListToWordActivationScoreMap(wdIndexer.wordList);
			sentenceNum ++;
		}
	}
	private void addWordListToWordActivationScoreMap(List<Word> wordList) {
		for(int i = 0; i < wordList.size(); i++) {
			CMNodeDO node = new CMNodeDO();
			node.word = wordList.get(i);
			node.nodeType = CMNodeType.Semantic;
			this.nodeActivationScoreMap.put(node, 0.0);
		}
	}
	
	public WordDistanceIndexer getSemanticIndexer() {
		return this.semanticIndexer;
	}
	public List<WordDistanceIndexer> getSyntacticIndexerList() {
		return this.syntacticIndexerList;
	}
	public Map<CMNodeDO, Double> getNodeActivationScoreMap() {
		return this.nodeActivationScoreMap;
	}
}