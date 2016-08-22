/* 
 * Copyright 2016 ReaderBench.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package data;

import java.io.Serializable;

import services.discourse.cohesion.CohesionGraph;
import services.nlp.listOfWords.Dictionary;
import services.nlp.listOfWords.StopWords;
import services.nlp.stemmer.Stemmer;
import services.readingStrategies.ReadingStrategies;
import services.semanticModels.LDA.LDA;
import services.semanticModels.LSA.LSA;
import dao.WordDAO;
import data.discourse.SemanticChain;
import data.lexicalChains.LexicalChain;
import data.lexicalChains.LexicalChainLink;
import data.sentiment.SentimentEntity;
import data.sentiment.SentimentValence;
import data.pojo.EntityXValence;

import org.apache.log4j.Logger;

/**
 * 
 * @author Mihai Dascalu
 */
public class Word implements Comparable<Word>, Serializable {

	static Logger logger = Logger.getLogger(CohesionGraph.class);

	private static final long serialVersionUID = -3809934014813200184L;

	private int blockIndex;// the number of the block the word is part of
	private int utteranceIndex; // the number of the utterance the word is part
								// of (inside of the block)
	private transient LSA lsa;
	private transient LDA lda;
	private Lang language;
	private String text;
	private String POS;
	private String stem;
	private String NE;
	private String lemma;
	private double[] lsaVector;
	private double[] ldaProbDistribution;
	private double idf;
	private LexicalChainLink lexicalChainLink; // the lexical chain link
												// associated with the word
												// after disambiguation
	private SemanticChain semanticChain;
	private boolean[] readingStrategies;

	private transient SentimentEntity sentiment;

	public Word(String text, String lemma, String stem, String POS, String NE, Lang lang) {
		this.text = text;
		this.lemma = lemma;
		this.stem = stem;
		this.POS = POS;
		this.NE = NE;
		this.language = lang;
		this.readingStrategies = new boolean[ReadingStrategies.NO_READING_STRATEGIES];
		// loadSentimentEntity();
	}

	private void loadSentimentEntity() {
		data.pojo.Word word = WordDAO.getInstance().findByLabel(text, language);
		if (word == null)
			return; // sentiment entity gol - nu avem info despre cuvant
		data.pojo.SentimentEntity se = word.getFkSentimentEntity();
		if (se == null)
			return;
		sentiment = new SentimentEntity();
		for (EntityXValence exv : se.getEntityXValenceList()) {
			sentiment.add(SentimentValence.get(exv.getFkSentimentValence().getIndexLabel()), exv.getValue());
		}
	}

	public Word(String text, String lemma, String stem, String POS, String NE, LSA lsa, LDA lda, Lang lang) {
		this(text, lemma, stem, POS, NE, lang);
		this.lsa = lsa;
		this.lda = lda;
		if (lsa != null) {
			this.idf = lsa.getWordIDf(this);
			this.lsaVector = lsa.getWordVector(this);
		}
		if (lda != null) {
			this.ldaProbDistribution = lda.getWordProbDistribution(this);
		}
	}

	public Word(String text, String lemma, String stem, String POS, String NE, LSA lsa, LDA lda,
			SentimentEntity sentiment, Lang lang) {
		this(text, lemma, stem, POS, NE, lsa, lda, lang);
		this.sentiment = sentiment;
	}

	public Word(int blockIndex, int utteranceIndex, String text, String lemma, String stem, String POS, String NE,
			LSA lsa, LDA lda, Lang lang) {
		this(text, lemma, stem, POS, NE, lsa, lda, lang);
		this.blockIndex = blockIndex;
		this.utteranceIndex = utteranceIndex;
	}

	public Word(int blockIndex, int utteranceIndex, String text, String lemma, String stem, String POS, String NE,
			LSA lsa, LDA lda, SentimentEntity sentiment, Lang lang) {
		this(blockIndex, utteranceIndex, text, lemma, stem, POS, NE, lsa, lda, lang);
		this.sentiment = sentiment;
	}

	public static Word getWordFromConcept(String concept, Lang lang) {
		Word w = null;
		if (concept.indexOf("_") > 0) {
			String word = concept.substring(0, concept.indexOf("_"));
			String POS = concept.substring(concept.indexOf("_") + 1);
			w = new Word(word, word, Stemmer.stemWord(word, lang), POS, null, lang);
		} else {
			w = new Word(concept, concept, Stemmer.stemWord(concept, lang), null, null, lang);
		}
		return w;
	}

	public double getDistanceInChain(Word word) {
		if (!partOfSameLexicalChain(word)) {
			return Double.MAX_VALUE;
		} else {
			LexicalChain chain = word.getLexicalChainLink().getLexicalChain();
			return chain.getDistance(word.getLexicalChainLink(), word.getLexicalChainLink());
		}
	}

	public boolean isNoun() {
		return POS.startsWith("NN");
	}

	public boolean isVerb() {
		return POS.startsWith("VB");
	}

	public boolean partOfSameLexicalChain(Word word) {
		if (word.getLexicalChainLink() == null || word.getLexicalChainLink().getLexicalChain() == null
				|| lexicalChainLink == null || lexicalChainLink.getLexicalChain() == null) {
			// some words do not have a lexical chain link associated since they
			// were not found in WordNet
			return false;
		}
		return lexicalChainLink.getLexicalChain().equals(word.getLexicalChainLink().getLexicalChain());
	}

	public int getBlockIndex() {
		return blockIndex;
	}

	public void setBlockIndex(int blockIndex) {
		this.blockIndex = blockIndex;
	}

	public int getUtteranceIndex() {
		return utteranceIndex;
	}

	public void setUtteranceIndex(int utteranceIndex) {
		this.utteranceIndex = utteranceIndex;
	}

	public String getPOS() {
		return POS;
	}

	public void setPOS(String POS) {
		this.POS = POS;
	}

	public String getStem() {
		return stem;
	}

	public void setStem(String stem) {
		this.stem = stem;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public double getIdf() {
		return idf;
	}

	public void setIdf(double idf) {
		this.idf = idf;
	}

	public double[] getLSAVector() {
		return lsaVector;
	}

	public void setLSAVector(double[] lsaVector) {
		this.lsaVector = lsaVector;
	}

	public double[] getLDAProbDistribution() {
		return ldaProbDistribution;
	}

	public void setLDAProbDistribution(double[] ldaProbDistribution) {
		this.ldaProbDistribution = ldaProbDistribution;
	}

	public String getLemma() {
		return lemma;
	}

	public void setLemma(String lemma) {
		this.lemma = lemma;
	}

	public LSA getLSA() {
		return lsa;
	}

	public void setLSA(LSA lsa) {
		this.lsa = lsa;
	}

	public LDA getLDA() {
		return lda;
	}

	public void setLDA(LDA lda) {
		this.lda = lda;
	}

	public Lang getLanguage() {
		return language;
	}

	public void setLanguage(Lang language) {
		this.language = language;
	}

	public String getNE() {
		return NE;
	}

	public void setNE(String nE) {
		NE = nE;
	}

	public LexicalChainLink getLexicalChainLink() {
		return lexicalChainLink;
	}

	public void setLexicalChainLink(LexicalChainLink lexicalChainLink) {
		this.lexicalChainLink = lexicalChainLink;
	}

	public boolean[] getReadingStrategies() {
		return readingStrategies;
	}

	public void setReadingStrategies(boolean[] readingStrategies) {
		this.readingStrategies = readingStrategies;
	}

	public SemanticChain getSemanticChain() {
		return semanticChain;
	}

	public void setSemanticChain(SemanticChain semanticChain) {
		this.semanticChain = semanticChain;
	}

	@Override
	public boolean equals(Object obj) {
		Word w = (Word) obj;
		if (this.getPOS() != null && w.getPOS() != null)
			return this.getLemma().equals(w.getLemma()) && this.getPOS().equals(w.getPOS());
		return this.getLemma().equals(w.getLemma());
	}

	@Override
	public String toString() {
		return this.text + "(" + this.lemma + ", " + this.stem + ", " + this.POS + ")";
	}

	public String getExtendedLemma() {
		if (this.getPOS() != null) {
			return this.getLemma() + "_" + this.getPOS();
		}
		return this.getLemma();
	}

	public boolean isContentWord() {
		if (this.getText().length() > 1 
				&& !StopWords.isStopWord(this.getText(), this.language)
				&& !StopWords.isStopWord(this.getLemma(), this.language) 
				&& 	(Dictionary.isDictionaryWord(this.getText(), this.language)
					|| Dictionary.isDictionaryWord(this.getLemma(), this.language))) {
			if (this.getPOS() != null) {
				if (this.getPOS().equals("NN") || this.getPOS().equals("VB") || this.getPOS().equals("JJ")
						|| this.getPOS().equals("RB")) {
					return true;
				}
				return false;
			}
			return true;
		}
		return false;
	}

	@Override
	public int compareTo(Word o) {
		if (this.getPOS() != null && o.getPOS() != null)
			return (this.getLemma() + "_" + this.getPOS()).compareTo(o.getLemma() + "_" + o.getPOS());
		return this.getLemma().compareTo(o.getLemma());
	}

	public SentimentEntity getSentiment() {
		if (sentiment == null) {
			// logger.info("Pentru cuvantul " + this + " nu avem initializate
			// sentimentele");
			loadSentimentEntity();
		}
		// logger.info("Pentru cuvantul " + this + " avem initializate
		// sentimentele");
		return sentiment;
	}

	public void setSentiment(SentimentEntity sentimentEntity) {
		this.sentiment = sentimentEntity;
	}

}
