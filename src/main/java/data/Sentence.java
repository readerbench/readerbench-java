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

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.trees.Tree;

import java.util.ArrayList;

import services.nlp.parsing.ContextSentiment;
import services.semanticModels.ISemanticModel;

/**
 *
 * @author Mihai Dascalu
 */
public class Sentence extends AnalysisElement implements Comparable<Sentence> {

    private static final long serialVersionUID = 6612571737695007151L;

    private List<Word> words;
    private List<Word> allWords;
    private transient SemanticGraph dependencies;
    private transient Map<Word, List<ContextSentiment>> contextMap;
    private transient Tree tree;
    private final Map<Word, Word> pronimialReplacementMap;

    public Sentence(Block b, int index, String text, List<ISemanticModel> models, Lang lang) {
        super(b, index, text.replaceAll("\\s", " ").trim(), models, lang);
        this.words = new ArrayList<>();
        this.allWords = new ArrayList<>();
        this.pronimialReplacementMap = new TreeMap<>();
    }

    public void addPronimialReplacement(Word pronoun, Word referencedWord) {
        this.pronimialReplacementMap.put(pronoun, referencedWord);
    }

    public Map<Word, Word> getPronimialReplacementMap() {
        return this.pronimialReplacementMap;
    }

    public void finalProcessing() {
        // write the processedText
        StringBuilder processedText = new StringBuilder();
        getWords().stream().forEach((word) -> {
            processedText.append(word.getLemma()).append(" ");
        });
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

    public SemanticGraph getDependencies() {
        return dependencies;
    }

    public void setDependencies(SemanticGraph dependencies) {
        this.dependencies = dependencies;
    }
    
    public Tree getTree() {
    	return tree;
    }
    
    public void setTree(Tree tree) {
    	this.tree = tree;
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
        s += "[" + getScore() + "]";
        return s;
    }

    @Override
    public int compareTo(Sentence o) {
        return (int) (Math.signum(o.getScore() - this.getScore()));
    }

    public void setContextMap(Map<Word, List<ContextSentiment>> contextMap) {
        this.contextMap = contextMap;
    }

    public Map<Word, List<ContextSentiment>> getContextMap() {
        return contextMap;
    }
}
