/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package data;

import java.util.List;

import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.util.CoreMap;
import java.util.ArrayList;
import services.semanticModels.LDA.LDA;
import services.semanticModels.LSA.LSA;

/**
 *
 * @author Mihai Dascalu
 */
public class Sentence extends AnalysisElement implements Comparable<Sentence> {
	private static final long serialVersionUID = 6612571737695007151L;

	private List<Word> words;
	private List<Word> allWords;
	private int POSTreeDepth;
	private int POSTreeSize;
	private transient Tree parseTree;
	private transient SemanticGraph dependencies;

	public Sentence(Block b, int index, String text, LSA lsa, LDA lda, Lang lang) {
		super(b, index, text.replaceAll("\\s", " ").trim(), lsa, lda, lang);
		this.words = new ArrayList<>();
		this.allWords = new ArrayList<>();
	}

	public void finalProcessing(Block b, CoreMap sentence) {
		// write the processedText
		StringBuilder processedText = new StringBuilder();
		for (Word word : getWords()) {
			processedText.append(word.getLemma()).append(" ");
		}
		setProcessedText(processedText.toString().trim());

		// determine LSA utterance vector
		determineSemanticDimensions();
	}

	public List<Word> getWords() {
		return words;
	}

	public void setWords(List<Word> words) {
		this.words = words;
	}

	public int getPOSTreeDepth() {
		return POSTreeDepth;
	}

	public void setPOSTreeDepth(int pOSTreeDepth) {
		POSTreeDepth = pOSTreeDepth;
	}

	public int getPOSTreeSize() {
		return POSTreeSize;
	}

	public void setPOSTreeSize(int pOSTreeSize) {
		POSTreeSize = pOSTreeSize;
	}

	public Tree getParseTree() {
		return parseTree;
	}

	public void setParseTree(Tree parseTree) {
		this.parseTree = parseTree;
	}

	public SemanticGraph getDependencies() {
		return dependencies;
	}

	public void setDependencies(SemanticGraph dependencies) {
		this.dependencies = dependencies;
	}

	public List<Word> getAllWords() {
		return allWords;
	}

	public void setAllWords(List<Word> allWords) {
		this.allWords = allWords;
	}

	@Override
	public String toString() {
		String s = "";
		for (Word w : allWords) {
			if (words.contains(w)) {
				s += w.toString() + "* ";
			} else {
				s += w.toString() + " ";
			}
		}
		s += "[" + getOverallScore() + "]";
		return s;
	}

	@Override
	public int compareTo(Sentence o) {
		return (int) (Math.signum(o.getOverallScore() - this.getOverallScore()));
	}
}
