package services.comprehensionModel.utils.indexer;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import services.comprehensionModel.utils.distanceStrategies.FullSemanticSpaceWordDistanceStrategy;
import services.comprehensionModel.utils.distanceStrategies.SyntacticWordDistanceStrategy;
import services.comprehensionModel.utils.distanceStrategies.utils.CMCorefIndexer;
import services.comprehensionModel.utils.distanceStrategies.utils.CMSyntacticGraph;
import services.comprehensionModel.utils.indexer.graphStruct.CMNodeDO;
import services.comprehensionModel.utils.indexer.graphStruct.CMNodeType;
import services.semanticModels.ISemanticModel;
import data.AbstractDocument;
import data.AbstractDocumentTemplate;
import data.Sentence;
import data.Word;
import data.document.Document;

/**
 *
 * @author Ionut Paraschiv
 */
public class CMIndexer {

    private final ISemanticModel semanticModel;
    private final String text;
    public AbstractDocument document;

    private WordDistanceIndexer semanticIndexer;
    private List<WordDistanceIndexer> syntacticIndexerList;
    private final Map<CMNodeDO, Double> nodeActivationScoreMap;

    public CMIndexer(String text, ISemanticModel semanticModel, double threshold, int noTopSimilarWords) {
        this.text = text;
        this.semanticModel = semanticModel;
        this.nodeActivationScoreMap = new TreeMap<>();
        this.loadDocument();
        this.indexFullSemanticSpaceDistances(threshold, noTopSimilarWords);
        this.indexSyntacticDistances();
    }

    private void loadDocument() {
        AbstractDocumentTemplate contents = AbstractDocumentTemplate.getDocumentModel(this.text);
        this.document = new Document(contents, this.semanticModel, true, false);
    }

    private void indexFullSemanticSpaceDistances(double threshold, int noTopSimilarWords) {
        FullSemanticSpaceWordDistanceStrategy wdStrategy = new FullSemanticSpaceWordDistanceStrategy(this.semanticModel, threshold, noTopSimilarWords);
        this.semanticIndexer = new WordDistanceIndexer(wdStrategy.getWordList(), wdStrategy);
        this.addWordListToWordActivationScoreMap(this.semanticIndexer.getWordList());
    }

    private void indexSyntacticDistances() {
        CMCorefIndexer corefContainer = new CMCorefIndexer(this.document, this.semanticModel.getLanguage());

        List<Sentence> sentenceList = this.document.getSentencesInDocument();
        Iterator<Sentence> sentenceIterator = sentenceList.iterator();
        this.syntacticIndexerList = new ArrayList<>();
        int sentenceNum = 0;
        while (sentenceIterator.hasNext()) {
            Sentence sentence = sentenceIterator.next();

            CMSyntacticGraph syntacticGraph = corefContainer.getCMSyntacticGraph(sentence, sentenceNum);
            SyntacticWordDistanceStrategy syntacticStrategy = new SyntacticWordDistanceStrategy(syntacticGraph);

            WordDistanceIndexer wdIndexer = new WordDistanceIndexer(syntacticGraph.getWordList(), syntacticStrategy);
            this.syntacticIndexerList.add(wdIndexer);
            this.addWordListToWordActivationScoreMap(wdIndexer.getWordList());
            sentenceNum++;
        }
    }

    private void addWordListToWordActivationScoreMap(List<Word> wordList) {
        for (int i = 0; i < wordList.size(); i++) {
            CMNodeDO node = new CMNodeDO(wordList.get(i), CMNodeType.Inferred);
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
