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
package com.readerbench.coreservices.data;

import com.readerbench.coreservices.nlp.wordlists.Dictionary;
import com.readerbench.coreservices.nlp.wordlists.StopWords;
import com.readerbench.coreservices.semanticmodels.SimilarityType;
import com.readerbench.coreservices.data.discourse.SemanticChain;
import com.readerbench.coreservices.data.document.ReadingStrategyType;
import com.readerbench.coreservices.data.lexicalchains.LexicalChainLink;
import com.readerbench.datasourceprovider.pojo.Lang;
import com.readerbench.coreservices.nlp.wordlists.SyllabifiedDictionary;
import com.readerbench.coreservices.semanticmodels.SemanticModel;
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
    private List<Syllable> syllables;
    private Word head;
    private String dep;
    
    public Word(String text, String lemma, String stem, String POS, String NE, Lang lang) {
        super.setText(text);
        super.setProcessedText(lemma);
        this.stem = stem;
        this.POS = POS;
        this.NE = NE;
        setLanguage(lang);
        this.usedReadingStrategies = EnumSet.noneOf(ReadingStrategyType.class);
        if (SyllabifiedDictionary.getDictionary(lang) != null) {
            this.syllables = SyllabifiedDictionary.getDictionary(lang).get(text.toLowerCase());
        }
    }

    public Word(String text, String lemma, String stem, String POS, String NE, List<SemanticModel> models, Lang lang) {
        this(text, lemma, stem, POS, NE, lang);
        super.setSemanticModels(models);
    }

    public Word(AnalysisElement container, String text, String lemma, String stem, String POS, String NE, List<SemanticModel> models, Lang lang) {
        this(text, lemma, stem, POS, NE, models, lang);
        this.container = container;
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

    public Word getHead() {
        return head;
    }

    public void setHead(Word head) {
        this.head = head;
    }

    public String getDep() {
        return dep;
    }

    public void setDep(String dep) {
        this.dep = dep;
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

    @Override
    public double[] getModelRepresentation(SimilarityType type) {
        if (!modelVectors.containsKey(type)) {
            modelVectors.put(type, semanticModels.get(type).getWordRepresentation(this));
        }
        return modelVectors.get(type);
    }

}
