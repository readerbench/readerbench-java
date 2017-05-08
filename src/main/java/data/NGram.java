/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package data;

import data.document.Document;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import services.semanticModels.ISemanticModel;
import services.semanticModels.SimilarityType;
import services.semanticModels.word2vec.Word2VecModel;

/**
 *
 * @author stefan
 */
public class NGram extends AnalysisElement {
    private List<Word> words;
    private Word unified;
    
    public NGram(Word first, Word second) {
        words = new ArrayList<>();
        words.add(first);
        words.add(second);
        initialize();
    }
    
    public NGram(Word first, Word second, Word third) {
        words = new ArrayList<>();
        words.add(first);
        words.add(second);
        words.add(third);
        initialize();
    }

    public List<Word> getWords() {
        return words;
    }
    
    private void initialize() {
//        setWordOccurences();
        setWordOccurences(words.stream()
                .collect(Collectors.groupingBy(
                        Function.identity(), 
                        Collectors.reducing(0, e -> 1, Integer::sum))));
        semanticModels = words.get(0).semanticModels;
        determineSemanticDimensions();
        String label = words.stream()
                .map(Word::getLemma)
                .collect(Collectors.joining("_"));
        unified = new Word(label, label, label, null, null, words.get(0).getLanguage());
        if (semanticModels.containsKey(SimilarityType.WORD2VEC)) {
            Word2VecModel model = (Word2VecModel)semanticModels.get(SimilarityType.WORD2VEC);
            double[] v = model.getWordRepresentation(unified);
            if (v != null) {
                modelVectors.put(SimilarityType.WORD2VEC, v);
            }
        }
    }

    public Word getUnified() {
        return unified;
    }
    
    @Override
    public String toString() {
        return unified.getLemma();
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 59 * hash + Objects.hashCode(this.words);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final NGram other = (NGram) obj;
        if (!Objects.equals(this.words, other.words)) {
            return false;
        }
        return true;
    }
    
    @Override
    public String getText() {
        return unified.getText();
    }
    
    public static void main(String[] args) {
        AbstractDocumentTemplate docTmp = AbstractDocumentTemplate.getDocumentModel(
                "What is the private language hypothesis, and what is its importance? According to this hypothesis, the meanings of the terms of the private language are the very sensory experiences to which they refer. These experiences are private to the subject in that he alone is directly aware of them. As classically expressed, the premise is that we have knowledge by acquaintance of our sensory experiences. As the private experiences are the meanings of the words of the language, a fortiori the language itself is private. Such a hypothesis, if successfully defended, promises to solve two important philosophical problems: It explains the connection between language and reality - there is a class of expressions that are special in that their meanings are given immediately in experience and not in further verbal definition. More generally, these experiences constitute the basic semantic units in which all discursive meaning is rooted. I shall refer to this solution as the thesis of semantic autonomy. This hypothesis also provides a solution to the problem of knowledge. For the same reason that sensory experience seems such an appropriate candidate for the ultimate source of all meaning, so it seems appropriate as the ultimate foundation for all knowledge. It is the alleged character of sensory experience, as that which is immediately and directly knowable, that makes it the prime candidate for both the ultimate semantic and epistemic unit. This I shall refer to as the thesis of non-propositional knowledge (or knowledge by acquaintance). Human machine interface for ABC computer applications."
                + " A survey of user opinion of computer system response time."
                + " The EPS user interface management system. "
                + "System and human system engineering testing of EPS haven’t got a clue. "
                + "Relation of user perceived response time to error measurement.");

        AbstractDocument d = new Document(null, docTmp, new ArrayList<>(), Lang.en, true);
        d.getBlocks().stream()
                .flatMap(b -> b.getSentences().stream())
                .forEach(s -> {
                    System.out.println(s.getBiGrams());
                });
    }
}
