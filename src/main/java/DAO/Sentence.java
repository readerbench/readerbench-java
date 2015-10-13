/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package DAO;

import java.util.LinkedList;
import java.util.List;

import services.semanticModels.LDA.LDA;
import services.semanticModels.LSA.LSA;
import DAO.discourse.SemanticCohesion;
import edu.cmu.lti.jawjaw.pobj.Lang;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.trees.Tree;

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
	private SemanticCohesion titleSimilarity;

	public Sentence(Block b, int index, String text, LSA lsa, LDA lda, Lang lang) {
		super(b, index, text.replaceAll("\\s", " ").trim(), lsa, lda, lang);
		this.words = new LinkedList<Word>();
		this.allWords = new LinkedList<Word>();
	}

	public void finalProcessing() {
		// write the processedText
		String processedText = "";
		for (Word word : getWords()) {
			processedText += word.getLemma() + " ";
		}
		setProcessedText(processedText.trim());

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

	public SemanticCohesion getTitleSimilarity() {
		return titleSimilarity;
	}

	public void setTitleSimilarity(SemanticCohesion titleSimilarity) {
		this.titleSimilarity = titleSimilarity;
	}

	@Override
	public String toString() {
		String s = "";
		// if (text.indexOf(" ", 40) > 0) {
		// s = "[" + text.substring(0, text.indexOf(" ", 40)) + "...]\n";
		// } else {
		// s = "[" + text + "]\n";
		// }
		for (Word w : allWords) {
			if (words.contains(w))
				s += w.toString() + "* ";
			else
				s += w.toString() + " ";
		}
		s += "[" + getOverallScore() + "]";
		return s;
	}

	@Override
	public int compareTo(Sentence o) {
		return (int) (Math.signum(o.getOverallScore() - this.getOverallScore()));
	}
}
