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

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.IndexedWord;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations;
import edu.stanford.nlp.util.Pair;
import java.util.ArrayList;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
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

    public List<Word> getAllWords() {
        return allWords;
    }

    public void setAllWords(List<Word> allWords) {
        this.allWords = allWords;
    }
    
    private Word getWordByIndex(IndexedWord iw) {
        int index = iw.get(CoreAnnotations.IndexAnnotation.class) - 1;
        index = Math.min(index, allWords.size() - 1);
        while (index >= 0) {
            Word word = allWords.get(index);
            if (word.getText().equals(iw.get(CoreAnnotations.TextAnnotation.class))) {
                return word;
            }
            index --;
        }
        return null;
    }
    
    @Override
    public List<NGram> getBiGrams() {
        if (dependencies == null) {
            return new ArrayList<>();
        }
        List<NGram> collect = StreamSupport.stream(dependencies.edgeIterable().spliterator(), true)
                .map(edge -> new Pair<>(getWordByIndex(edge.getSource()), getWordByIndex(edge.getTarget())))
                .filter(pair -> pair.first != null && pair.second != null)
                .filter(pair -> pair.first.isContentWord() && pair.second.isContentWord())
                .map(pair -> new NGram(pair.first, pair.second))
                .collect(Collectors.toList());
        if(collect.contains(new NGram(new Word("use", "use", null, null, null, Lang.en), new Word("use", "use", null, null, null, Lang.en)))) {
            System.out.println("are: " + getText());
        }
        return collect;
    }
    
    @Override
    public List<NGram> getNGrams(int n) {
        List<NGram> result = new ArrayList<>();
        for (int i = 0; i <= words.size() - n; i++) {
            result.add(new NGram(words.subList(i, i + n)));
        }
        return result;
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
}
