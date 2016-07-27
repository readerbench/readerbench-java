package services.comprehensionModel.utils.indexer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import services.comprehensionModel.utils.CMUtils;
import services.comprehensionModel.utils.distanceStrategies.FullSemanticSpaceWordDistanceStrategy;
import services.comprehensionModel.utils.distanceStrategies.SemanticWordDistanceStrategy;
import services.comprehensionModel.utils.distanceStrategies.SyntacticWordDistanceStrategy;
import services.comprehensionModel.utils.indexer.graphStruct.CiNodeDO;
import services.comprehensionModel.utils.indexer.graphStruct.CiNodeType;
import services.semanticModels.LDA.LDA;
import services.semanticModels.LSA.LSA;
import data.AbstractDocument;
import data.AbstractDocumentTemplate;
import data.Lang;
import data.Sentence;
import data.Word;
import data.document.Document;
import edu.stanford.nlp.semgraph.SemanticGraph;
import services.semanticModels.SpaceStatistics;
import runtime.semanticModels.utils.WordDistance;

public class QueryIndexer {
	public static String LsaPath = "resources/config/LSA/tasa_en";
	public static String LdaPath = "resources/config/LDA/tasa_en";
	public static Lang lang = Lang.eng;
	
	private int hdpGrade;
	private int noTopSimilarWords;
	private String text;
	public AbstractDocument document;
	
	private CMUtils cMUtils;
	private WordDistanceIndexer semanticIndexer;
	private List<WordDistanceIndexer> syntacticIndexerList;
	private Map<CiNodeDO, Double> nodeActivationScoreMap;
	
	public QueryIndexer(String text, int hdpGrade, int noTopSimilarWords) {
		this.hdpGrade = hdpGrade;
		this.noTopSimilarWords = noTopSimilarWords;
		this.text = text;
		this.cMUtils = new CMUtils();
		this.nodeActivationScoreMap = new HashMap<CiNodeDO, Double>();
		this.loadDocument();
		// this.indexSemanticDistances();
		this.indexFullSemanticSpaceDistances();
		this.indexSyntacticDistances();
	}
	private void loadDocument() {
		AbstractDocumentTemplate contents = AbstractDocumentTemplate.getDocumentModel(this.text);
		this.document = new Document(null, contents, LSA.loadLSA(LsaPath, lang),
				LDA.loadLDA(LdaPath, lang), lang, true, false);
	}
	
	private void indexSemanticDistances() {		
		List<Word> wordList = this.cMUtils.getContentWordListFromDocument(this.document);
		SemanticWordDistanceStrategy semanticStrategy = new SemanticWordDistanceStrategy(this.document);

		this.semanticIndexer = new WordDistanceIndexer(wordList, semanticStrategy);
		this.semanticIndexer.cutByAvgPlusStddev(0.3);
		this.addWordListToWordActivationScoreMap(this.semanticIndexer.wordList);
	}
	private void indexFullSemanticSpaceDistances() {
		FullSemanticSpaceWordDistanceStrategy wdStrategy = new FullSemanticSpaceWordDistanceStrategy(QueryIndexer.lang, this.hdpGrade, this.noTopSimilarWords);
		
		this.semanticIndexer = new WordDistanceIndexer(wdStrategy.getWordList(), wdStrategy);
		this.addWordListToWordActivationScoreMap(this.semanticIndexer.wordList);
	}
	
	private void indexSyntacticDistances() {
		List<Sentence> sentenceList = this.document.getSentencesInDocument();
		Iterator<Sentence> sentenceIterator = sentenceList.iterator();
		this.syntacticIndexerList = new ArrayList<WordDistanceIndexer>();
		while(sentenceIterator.hasNext()) {
			Sentence sentence = sentenceIterator.next();
			
			SemanticGraph semanticGraph = sentence.getDependencies();
			List<Word> wordList = this.cMUtils.getContentWordListFromSemanticGraph(semanticGraph, QueryIndexer.lang);
			SyntacticWordDistanceStrategy syntacticStrategy = new SyntacticWordDistanceStrategy(semanticGraph, QueryIndexer.lang);
			
			WordDistanceIndexer wdIndexer = new WordDistanceIndexer(wordList, syntacticStrategy);
			this.syntacticIndexerList.add(wdIndexer);
			this.addWordListToWordActivationScoreMap(wdIndexer.wordList);
		}
	}
	private void addWordListToWordActivationScoreMap(List<Word> wordList) {
		for(int i = 0; i < wordList.size(); i++) {
			CiNodeDO node = new CiNodeDO();
			node.word = wordList.get(i);
			node.nodeType = CiNodeType.Semantic;
			this.nodeActivationScoreMap.put(node, 0.0);
		}
	}
	
	public WordDistanceIndexer getSemanticIndexer() {
		return this.semanticIndexer;
	}
	public List<WordDistanceIndexer> getSyntacticIndexerList() {
		return this.syntacticIndexerList;
	}
	public Map<CiNodeDO, Double> getNodeActivationScoreMap() {
		return this.nodeActivationScoreMap;
	}
}