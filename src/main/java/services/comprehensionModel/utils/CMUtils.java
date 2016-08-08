package services.comprehensionModel.utils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import services.comprehensionModel.utils.indexer.graphStruct.CMNodeDO;
import services.nlp.lemmatizer.StaticLemmatizer;
import data.AbstractDocument;
import data.Lang;
import data.Word;
import edu.stanford.nlp.ling.IndexedWord;

public class CMUtils {

    public Word convertToWord(IndexedWord node, Lang lang) {
        String wordStr = node.word().toLowerCase();
        Word word = Word.getWordFromConcept(wordStr, lang);
        word.setLemma(StaticLemmatizer.lemmaStatic(wordStr, lang));
        word.setPOS("");
        if (node.tag() != null && node.tag().length() >= 2) {
            word.setPOS(node.tag().substring(0, 2));
        }
        return word;
    }

    public Word convertStringToWord(String wordString, Lang lang) {
        Word word = Word.getWordFromConcept(wordString, lang);
        word.setLemma(StaticLemmatizer.lemmaStatic(wordString, lang));
        word.setPOS("");
        return word;
    }

    public List<Word> getContentWordListFromDocument(AbstractDocument document) {
        return this.convertIteratorToList(document.getWordOccurences().keySet().iterator());
    }

    public List<Word> convertIteratorToList(Iterator<Word> wordIterator) {
        List<Word> wordList = new ArrayList<>();
        while (wordIterator.hasNext()) {
            Word w = wordIterator.next();
            if (w.isContentWord()) {
                wordList.add(w);
            }
        }
        return wordList;
    }

    public List<String> convertStringIteratorToList(Iterator<String> wordIterator) {
        List<String> wordList = new ArrayList<>();
        while (wordIterator.hasNext()) {
            String w = wordIterator.next();
            wordList.add(w);
        }
        return wordList;
    }

    public List<CMNodeDO> convertNodeIteratorToList(Iterator<CMNodeDO> nodeIterator) {
        List<CMNodeDO> nodeList = new ArrayList<>();
        while (nodeIterator.hasNext()) {
            CMNodeDO node = nodeIterator.next();
            if (node.getWord().isContentWord()) {
                nodeList.add(node);
            }
        }
        return nodeList;
    }
}
