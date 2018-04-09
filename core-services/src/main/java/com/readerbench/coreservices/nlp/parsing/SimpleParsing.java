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
package com.readerbench.coreservices.nlp.parsing;

import com.readerbench.datasourceprovider.data.AbstractDocument;
import com.readerbench.datasourceprovider.data.Block;
import com.readerbench.datasourceprovider.data.Sentence;
import com.readerbench.datasourceprovider.data.Word;
import com.readerbench.datasourceprovider.data.sentiment.SentimentEntity;
import com.readerbench.coreservices.nlp.lemmatizer.StaticLemmatizer;
import com.readerbench.coreservices.nlp.listOfWords.Dictionary;
import com.readerbench.coreservices.nlp.listOfWords.StopWords;
import com.readerbench.coreservices.nlp.stemmer.Stemmer;
import com.readerbench.datasourceprovider.pojo.Lang;

import java.util.StringTokenizer;

public class SimpleParsing {

    public static Block processBlock(AbstractDocument d, int blockIndex, String paragraph) {
        Block b = new Block(d, blockIndex, paragraph, d.getSemanticModels(), d.getLanguage());
        // parse the text using a simple String Tokenizer
        StringTokenizer st = new StringTokenizer(b.getText(), ".!?");
        int utteranceCounter = 0;
        StringBuilder processedText = new StringBuilder();
        while (st.hasMoreTokens()) {
            String content = st.nextToken().trim();
            if (content.length() > 0) {
                Sentence u = processSentence(b, utteranceCounter++, content);
                // add utterance to block
                b.getSentences().add(u);
                processedText.append(u.getProcessedText()).append(". ");
            }
        }
        b.setProcessedText(processedText.toString());
        b.finalProcessing();
        return b;
    }

    public static Sentence processSentence(Block b, int utteranceIndex, String sentence) {
        // basic parsing
        Lang lang = b.getLanguage();
        Sentence s = new Sentence(b, utteranceIndex, sentence, b.getSemanticModels(), lang);

        Word w;
        StringTokenizer st = new StringTokenizer(s.getText(), " ,:;'-");
        while (st.hasMoreTokens()) {
            String wordText = st.nextToken().toLowerCase();
            String stem = Stemmer.stemWord(wordText, lang);
            String lemma = StaticLemmatizer.lemmaStatic(wordText, lang);
            w = new Word(s, wordText, lemma, stem, null, null, s.getSemanticModels(),
                    lang);
            s.getAllWords().add(w);

            // add content words
            if (!StopWords.isStopWord(w.getText(), lang) && !StopWords.isStopWord(w.getLemma(), lang)
                    && (Dictionary.isDictionaryWord(w.getText(), lang)
                    || Dictionary.isDictionaryWord(w.getLemma(), lang))
                    && wordText.length() > 2) {
                s.getWords().add(w);
                if (s.getWordOccurences().containsKey(w)) {
                    s.getWordOccurences().put(w, s.getWordOccurences().get(w) + 1);
                } else {
                    s.getWordOccurences().put(w, 1);
                }
            }
        }

        if (s.getLanguage().equals(Lang.en)) {
            SentimentEntity se = new SentimentEntity();
            s.setSentimentEntity(se);
        }

        s.finalProcessing();
        return s;
    }
}
