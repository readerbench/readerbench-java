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
package com.readerbench.datasourceprovider.data;

import com.readerbench.datasourceprovider.dao.WordDAO;
import com.readerbench.datasourceprovider.data.discourse.SemanticChain;
import com.readerbench.datasourceprovider.data.document.ReadingStrategyType;
import com.readerbench.datasourceprovider.data.lexicalChains.LexicalChainLink;
import com.readerbench.datasourceprovider.data.semanticmodels.ISemanticModel;
import com.readerbench.datasourceprovider.data.semanticmodels.SimilarityType;
import com.readerbench.datasourceprovider.data.sentiment.SentimentEntity;
import com.readerbench.datasourceprovider.data.sentiment.SentimentValence;
import com.readerbench.datasourceprovider.pojo.EntityXValence;
import com.readerbench.datasourceprovider.pojo.Lang;
import com.readerbench.textualcomplexity.rhythm.tools.SyllabifiedCMUDict;
import com.readerbench.coreservices.nlp.listOfWords.Dictionary;
import com.readerbench.coreservices.nlp.listOfWords.StopWords;
import com.readerbench.coreservices.nlp.stemmer.Stemmer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;

/**
 *
 * @author Mihai Dascalu
 */
public class Word extends AnalysisElement implements Comparable<Word>, Serializable {

    private static final Logger LOGGER = LoggerFactory.getLogger(Word.class);

    private static final long serialVersionUID = -3809934014813200184L;

    // of (inside of the block)
    private String POS;
    private String stem;
    private String NE;
    private double idf;
    private LexicalChainLink lexicalChainLink; // the lexical chain link associated with the word after disambiguation
    private SemanticChain semanticChain;
    private EnumSet<ReadingStrategyType> usedReadingStrategies;

    public double lsaSimilarityToUnderlyingConcept = -1;
    public double ldaSimilarityToUnderlyingConcept = -1;

    private transient SentimentEntity sentiment;
    
    public transient SyllabifiedCMUDict syllabifiedCMUDict;
    private List<Syllable> syllables;

    public Word(String text, String lemma, String stem, String POS, String NE, Lang lang) {
        super.setText(text);
        super.setProcessedText(lemma);
        this.stem = stem;
        this.POS = POS;
        this.NE = NE;
        setLanguage(lang);
        this.usedReadingStrategies = EnumSet.noneOf(ReadingStrategyType.class);
        if (lang == Lang.en) {
            this.syllabifiedCMUDict = SyllabifiedCMUDict.getInstance();
            this.syllables = syllabifiedCMUDict.getDict().get(text.toLowerCase());
        }
    }

    private void loadSentimentEntity() {
        com.readerbench.datasourceprovider.pojo.Word word = WordDAO.getInstance().findByLabel(getLemma(), getLanguage());
        if (word == null) {
            return; // empty sentiment entity - no info on the current word
        }
        com.readerbench.datasourceprovider.pojo.SentimentEntity se = word.getFkSentimentEntity();
        if (se == null) {
            return;
        }
        synchronized (se){
            sentiment = new SentimentEntity();
            List<EntityXValence> exvList = se.getEntityXValenceList();
            exvList.stream().forEach((exv) -> {
                sentiment.add(SentimentValence.get(exv.getFkSentimentValence().getIndexLabel()), exv.getValue());
            });
        }
    }

    public Word(String text, String lemma, String stem, String POS, String NE, List<ISemanticModel> models, Lang lang) {
        this(text, lemma, stem, POS, NE, lang);
        super.setSemanticModels(models);
    }

    public Word(String text, String lemma, String stem, String POS, String NE, List<ISemanticModel> models, SentimentEntity sentiment, Lang lang) {
        this(text, lemma, stem, POS, NE, models, lang);
        this.sentiment = sentiment;
    }

    public Word(AnalysisElement container, String text, String lemma, String stem, String POS, String NE, List<ISemanticModel> models, Lang lang) {
        this(text, lemma, stem, POS, NE, models, lang);
        this.container = container;
    }

    public Word(AnalysisElement container, String text, String lemma, String stem, String POS, String NE, List<ISemanticModel> models, SentimentEntity sentiment, Lang lang) {
        this(container, text, lemma, stem, POS, NE, models, lang);
        this.sentiment = sentiment;
    }

    public static Word getWordFromConcept(String concept, Lang lang) {
        Word w;
        if (concept.indexOf("_") > 0) {
            String word = concept.substring(0, concept.indexOf("_"));
            String POS = concept.substring(concept.indexOf("_") + 1);
            w = new Word(word, word, Stemmer.stemWord(word, lang), POS, null, lang);
        } else {
            w = new Word(concept, concept, Stemmer.stemWord(concept, lang), null, null, lang);
        }
        return w;
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
        if (container == null) {
            return 0;
        }
        if (container.container == null) {
            return 0;
        }
        return container.container.getIndex();
    }

    public int getUtteranceIndex() {
        if (container == null) {
            return 0;
        }
        return container.getIndex();
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

    public double getIdf() {
        return idf;
    }

    public void setIdf(double idf) {
        this.idf = idf;
    }

    public String getLemma() {
        return getProcessedText();
    }

    public void setLemma(String lemma) {
        setProcessedText(lemma);
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

    public EnumSet<ReadingStrategyType> getReadingStrategies() {
        return usedReadingStrategies;
    }

    public void resetReadingStrategies() {
        usedReadingStrategies = EnumSet.noneOf(ReadingStrategyType.class);
    }

    public SemanticChain getSemanticChain() {
        return semanticChain;
    }

    public void setSemanticChain(SemanticChain semanticChain) {
        this.semanticChain = semanticChain;
    }
    
    public List<Syllable> getSyllables() {
        return syllables;
    }
    
    public void setSyllables(List<Syllable> syllables) {
        this.syllables = syllables;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Word)) {
            return false;
        }
        Word w = (Word) obj;
        if (this.getPOS() != null && w.getPOS() != null) {
            return this.getLemma().equals(w.getLemma()) && this.getPOS().equals(w.getPOS());
        }
        return this.getLemma().equals(w.getLemma());
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 37 * hash + Objects.hashCode(getLemma());
        return hash;
    }

    @Override
    public String toString() {
        return getText() + "(" + getLemma() + ", " + this.stem + ", " + this.POS + ")";
    }

    public String getExtendedLemma() {
        if (this.getPOS() != null) {
            return this.getLemma() + "_" + this.getPOS();
        }
        return this.getLemma();
    }

    public boolean isContentWord() {
        if (this.getText().length() > 1
                && !StopWords.isStopWord(this.getText(), getLanguage())
                && !StopWords.isStopWord(this.getLemma(), getLanguage())
                && (Dictionary.isDictionaryWord(this.getText(), getLanguage())
                || Dictionary.isDictionaryWord(this.getLemma(), getLanguage()))) {
            if (this.getPOS() != null) {
                return this.getPOS().equals("NN") || this.getPOS().equals("VB") || this.getPOS().equals("JJ") || this.getPOS().equals("RB");
            }
            return true;
        }
        return false;
    }

    @Override
    public int compareTo(Word o) {
        if (this.getPOS() != null && o.getPOS() != null) {
            return (this.getLemma() + "_" + this.getPOS()).compareTo(o.getLemma() + "_" + o.getPOS());
        }
        return this.getLemma().compareTo(o.getLemma());
    }

    public SentimentEntity getSentiment() {
        if (sentiment == null) {
            loadSentimentEntity();
        }
        return sentiment;
    }

    public void setSentiment(SentimentEntity sentimentEntity) {
        this.sentiment = sentimentEntity;
    }

    @Override
    public double[] getModelRepresentation(SimilarityType type) {
        if (!modelVectors.containsKey(type)) {
            modelVectors.put(type, semanticModels.get(type).getWordRepresentation(this));
        }
        return modelVectors.get(type);
    }

}
