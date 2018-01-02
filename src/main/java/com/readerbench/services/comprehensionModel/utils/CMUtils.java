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
package com.readerbench.services.comprehensionModel.utils;

import com.readerbench.data.AbstractDocument;
import com.readerbench.data.Lang;
import com.readerbench.data.Word;
import edu.stanford.nlp.ling.IndexedWord;
import com.readerbench.services.comprehensionModel.utils.indexer.graphStruct.CMNodeDO;
import com.readerbench.services.nlp.lemmatizer.StaticLemmatizer;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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
