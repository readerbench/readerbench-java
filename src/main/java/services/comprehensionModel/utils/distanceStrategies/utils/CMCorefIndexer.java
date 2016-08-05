package services.comprehensionModel.utils.distanceStrategies.utils;

import java.util.ArrayList;
import java.util.List;

import data.Block;
import data.AbstractDocument;
import data.Lang;
import data.Sentence;
import data.Word;
import edu.stanford.nlp.hcoref.CorefCoreAnnotations;
import edu.stanford.nlp.hcoref.data.CorefChain;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.ling.CoreAnnotations.PartOfSpeechAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphEdge;
import edu.stanford.nlp.util.CoreMap;
import services.comprehensionModel.utils.CMUtils;
import services.nlp.lemmatizer.StaticLemmatizer;
import services.nlp.parsing.Parsing;

public class CMCorefIndexer {
	private CMUtils cmUtils;
	
	private AbstractDocument document;
	private Lang lang;
	private List<CMCoref> corefList;
	
	public CMCorefIndexer(AbstractDocument document, Lang lang) {
		this.cmUtils = new CMUtils();
		this.document = document;
		this.lang = lang;
		this.indexCoreferences();
	}
	
	private void indexCoreferences() {
		this.corefList = new ArrayList<>();
		List<Block> blockList = this.document.getBlocks();
        for (Block block : blockList) {
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

                        CorefChain.CorefMention reprMent = chain.getRepresentativeMention();
                        for (int i = reprMent.startIndex; i < reprMent.endIndex; i++) {
                            CoreLabel matchedLabel = corefSentenceTokens.get(i - 1);
                            pos = Parsing.getParser(lang).convertToPenn(matchedLabel.get(PartOfSpeechAnnotation.class));
                            if (pos.equals("NN")) {
                                this.corefList.add(new CMCoref(token, matchedLabel));
                            }
                        }
                        
                    }
                }
            }
        }
	}
	
	public CMSyntacticGraph getCMSyntacticGraph(Sentence sentence, int sentenceIndex) {
		SemanticGraph semanticGraph = sentence.getDependencies();
		CMSyntacticGraph syntacticGraph = new CMSyntacticGraph();
		
		for (SemanticGraphEdge edge : semanticGraph.edgeListSorted()) {
			Word dependentNode = this.getActualWord(edge.getDependent(), sentenceIndex);
			Word governorNode = this.getActualWord(edge.getGovernor(), sentenceIndex);
			if(dependentNode.isContentWord() && governorNode.isContentWord()){
				syntacticGraph.indexEdge(dependentNode, governorNode);
			}
		}
		
		return syntacticGraph;
	}
	
	private Word getActualWord(IndexedWord indexedWord, int sentenceIndex) {
		Word word = this.cmUtils.convertToWord(indexedWord, lang);
		if(word.getPOS().equals("PR")) {
			CMCoref dependentCoref = this.getCMCoref(indexedWord, sentenceIndex);
			if(dependentCoref != null) {
				System.out.println("[Sentence " + sentenceIndex + "] Replacing " + indexedWord.word() + " with " + dependentCoref.referencedToken.word() + "");
				return this.convertToWord(dependentCoref.referencedToken);
			}
		}
		return word;
	}
	
	private Word convertToWord(CoreLabel node) {
		String wordStr = node.word().toLowerCase();
		Word word = Word.getWordFromConcept(wordStr, lang);
		word.setLemma(StaticLemmatizer.lemmaStatic(wordStr, lang));
		word.setPOS("");
		if(node.tag() != null && node.tag().length() >= 2) {
			word.setPOS(node.tag().substring(0, 2));
		}
		return word;
	}
	private CMCoref getCMCoref(IndexedWord word, int sentenceIndex) {
		for(CMCoref coref : this.corefList) {
			if(coref.getSentenceIndex() != sentenceIndex) {
				continue;
			}
			if(coref.token.index() == word.index()) {
				return coref;
			}
		}
		return null;
	}
}