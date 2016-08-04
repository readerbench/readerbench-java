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
import edu.stanford.nlp.hcoref.CorefCoreAnnotations;
import edu.stanford.nlp.hcoref.data.CorefChain;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreAnnotations.PartOfSpeechAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.util.CoreMap;
import services.nlp.parsing.Parsing;

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
        for (Block block : blockList) {
            List<String> resolved = new ArrayList<>();

            for (CoreMap sentence : block.getStanfordSentences()) {
                List<CoreLabel> tokens = sentence.get(CoreAnnotations.TokensAnnotation.class);
                for (CoreLabel token : tokens) {
                    Integer corefClustId = token.get(CorefCoreAnnotations.CorefClusterIdAnnotation.class);
                    CorefChain chain = block.getCorefs().get(corefClustId);
                    String pos = Parsing.getParser(lang).convertToPenn(token.get(PartOfSpeechAnnotation.class));
                    
                    if (pos.equals("PR") && chain != null && chain.getMentionsInTextualOrder().size() > 1) {
                        int sentINdx = chain.getRepresentativeMention().sentNum - 1;
                        CoreMap corefSentence = block.getStanfordSentences().get(sentINdx);
                        List<CoreLabel> corefSentenceTokens = corefSentence.get(TokensAnnotation.class);

                        String newwords = "";
                        CorefChain.CorefMention reprMent = chain.getRepresentativeMention();
                        for (int i = reprMent.startIndex; i < reprMent.endIndex; i++) {
                            CoreLabel matchedLabel = corefSentenceTokens.get(i - 1);
                            pos = Parsing.getParser(lang).convertToPenn(matchedLabel.get(PartOfSpeechAnnotation.class));
                            if (pos.equals("NN")) {
                                resolved.add(matchedLabel.word());
                                newwords += matchedLabel.word() + " ";
                            }
                        }
                        System.out.println("converting " + token.word() + " to " + newwords);
                    } else {
                        resolved.add(token.word());
                    }
                }
            }

            String resolvedStr = "";
            System.out.println();
            for (String str : resolved) {
                resolvedStr += str + " ";
            }
            System.out.println(resolvedStr);
        }

        List<Sentence> sentenceList = this.document.getSentencesInDocument();
        Iterator<Sentence> sentenceIterator = sentenceList.iterator();
        this.syntacticIndexerList = new ArrayList<>();
        int sentenceNum=0;
        while (sentenceIterator.hasNext()) {
            Sentence sentence = sentenceIterator.next();

            SemanticGraph semanticGraph = sentence.getDependencies();
            List<Word> wordList = this.cMUtils.getContentWordListFromSemanticGraph(sentence, QueryIndexer.lang);
            SyntacticWordDistanceStrategy syntacticStrategy = new SyntacticWordDistanceStrategy(semanticGraph, QueryIndexer.lang);

            WordDistanceIndexer wdIndexer = new WordDistanceIndexer(wordList, syntacticStrategy);
            this.syntacticIndexerList.add(wdIndexer);
            this.addWordListToWordActivationScoreMap(wdIndexer.wordList);
            sentenceNum++;
        }
    }

    private void addWordListToWordActivationScoreMap(List<Word> wordList) {
        for (int i = 0; i < wordList.size(); i++) {
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
