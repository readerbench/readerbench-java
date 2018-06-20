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

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.async.Callback;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.readerbench.coreservices.data.Word;
import com.readerbench.coreservices.data.AbstractDocumentTemplate;
import com.readerbench.coreservices.data.AbstractDocument;
import com.readerbench.coreservices.data.Block;
import com.readerbench.coreservices.data.Sentence;
import com.readerbench.coreservices.data.cscl.Conversation;
import com.readerbench.coreservices.data.cscl.Participant;
import com.readerbench.coreservices.data.cscl.Utterance;
import com.readerbench.coreservices.data.document.Document;
import com.readerbench.datasourceprovider.pojo.Lang;
import com.readerbench.coreservices.nlp.TextPreprocessing;
import com.readerbench.coreservices.nlp.stemmer.Stemmer;
import com.readerbench.datasourceprovider.commons.ReadProperty;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * General NLP parsing class relying on the Stanford Core NLP
 *
 * @author Mihai Dascalu
 */
public class Parsing {

    private static final Logger LOGGER = LoggerFactory.getLogger(Parsing.class);

    public static final int STANFORD_ID = 10000;

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

    public String convertToPenn(String pos) {
        if (pos != null && pos.length() > 2) {
            return pos.substring(0, 2);
        }
        return pos;
    }

    private static Utterance getUtterance(Conversation c, JSONObject blockJSON, Block b) throws JSONException {
        Participant activeSpeaker = null;
        if (blockJSON.has("speaker")) {
            activeSpeaker = new Participant(blockJSON.getString("speaker"), blockJSON.getString("speakerAlias"), c);
            boolean contains = false;
            for (Participant p : c.getParticipants()) {
                if (p.equals(activeSpeaker)) {
                    activeSpeaker = p;
                    contains = true;
                }
            }
            if (!contains) {
                c.getParticipants().add(activeSpeaker);
            }
        }
        Date time = null;
        if (blockJSON.has("time")) {
            try {
                time = AbstractDocumentTemplate.DATE_FORMATS[0].parse(blockJSON.getString("time"));
            } catch (ParseException ex) {
                LOGGER.error(ex.getMessage());
            }
        }
        Utterance u = new Utterance(b, activeSpeaker, time);
        return u;
    }

    public static JSONObject doc2JSON(AbstractDocumentTemplate adt, Lang lang) throws JSONException {
        JSONObject request = new JSONObject();
        JSONObject doc = new JSONObject();
        request.put("doc", doc);
        List<JSONObject> blocks = new ArrayList<>();
        for (AbstractDocumentTemplate.BlockTemplate bt : adt.getBlocks()) {
            JSONObject block = new JSONObject();
            block.put("text", bt.getContent());
            block.put("id", bt.getId());
            block.put("ref", bt.getRefId());
            if (bt.getVerbId() != null) {
                block.put("verbId", bt.getVerbId());
            }
            if (bt.getTime() != null) {
                block.put("time", AbstractDocumentTemplate.DATE_FORMATS[0].format(bt.getTime()));
            }
            if (bt.getSpeaker() != null) {
                block.put("speaker", bt.getSpeaker());
            }
            if (bt.getSpeakerAlias()!= null) {
                block.put("speakerAlias", bt.getSpeakerAlias());
            }
            blocks.add(block);
        }
        JSONArray blockArr = new JSONArray(blocks);
        doc.put("blocks", blockArr);
        doc.put("lang", lang.toString());
        return request;
    }

    public static void JSON2Doc(JSONObject json, AbstractDocument d) throws JSONException {
        JSONObject doc = json.getJSONObject("doc");
        JSONArray blocks = doc.getJSONArray("blocks");
        for (int i = 0; i < blocks.length(); i++) {
            Integer id = i;
            Integer ref = null;
            JSONObject block = blocks.getJSONObject(i);
            if (block.has("id")) {
                id = block.getInt("id");
            }
            if (block.has("ref")) {
                ref = block.getInt("ref");
            }
            boolean followedByVerbalization = block.has("verbId");
            Block b = JSON2Block(block, d, id);
            if (d instanceof Conversation) {
                b = getUtterance((Conversation) d, block, b);
            }
            b.setFollowedByVerbalization(followedByVerbalization);
            // add explicit reference, if the case
            if (ref != null && ref != -1) {
                for (Block refB : d.getBlocks()) {
                    if (refB != null && refB.getIndex() == ref) {
                        b.setRefBlock(refB);
                        break;
                    }
                }
            }
            Block.addBlock(d, b);
            
        }
        
    }
    
    public static Block JSON2Block(JSONObject blockJSON, AbstractDocument d, int id) throws JSONException {
        Block block = new Block(d, id, blockJSON.getString("text"), d.getSemanticModelsAsList(), d.getLanguage());
        JSONArray sentences = blockJSON.getJSONArray("sentences");
        for (int i = 0; i < sentences.length(); i++) {
            JSONObject sentence = sentences.getJSONObject(i);
            block.getSentences().add(JSON2Sentence(sentence, block, i));
        }
        block.finalProcessing();
        return block;
    }
    
    public static Sentence JSON2Sentence(JSONObject sentenceJSON, Block parent, int id) throws JSONException {
        Sentence sentence = new Sentence(parent, id, sentenceJSON.getString("text"), parent.getSemanticModelsAsList(), parent.getLanguage());
        JSONArray words = sentenceJSON.getJSONArray("words");
        Map<Integer, Word> wordIndex = new HashMap<>();
        
        for (int i = 0; i < words.length(); i++) {
            JSONObject word = words.getJSONObject(i);
            String text = word.getString("text");
            String pos = word.getString("pos");
            String lemma = word.getString("lemma");
            String ner = null;
            if (word.has("ner")) {
                ner = word.getString("ner");   
            }
            if (TextPreprocessing.isWord(text, parent.getLanguage())) {
                Word w = new Word(sentence, text, lemma, Stemmer.stemWord(text, sentence.getLanguage()), 
                        pos, ner, sentence.getSemanticModelsAsList(), sentence.getLanguage());
                wordIndex.put(word.getInt("index"), w);
                sentence.getAllWords().add(w);
                if (w.isContentWord()) {
                    sentence.getWords().add(w);
                    if (sentence.getWordOccurences().containsKey(w)) {
                        sentence.getWordOccurences().put(w, sentence.getWordOccurences().get(w) + 1);
                    } else {
                        sentence.getWordOccurences().put(w, 1);
                    }
                }
            }
        }
        for (int i = 0; i < words.length(); i++) {
            JSONObject word = words.getJSONObject(i);
            String dep = word.getString("dep");
            Word w1 = wordIndex.getOrDefault(word.getInt("index"), null);
            Word w2 = wordIndex.getOrDefault(word.getInt("head"), null);
            if (w1 == null || w2 == null || w1 == w2) {
                continue;
            }
            sentence.getDependencies().add(new ImmutableTriple<>(w1, w2, dep));
            w1.setHead(w2);
            w1.setDep(dep);
        }
        sentence.finalProcessing();
        return sentence;
    }
    
    public static void parseDoc(AbstractDocumentTemplate adt, AbstractDocument d, boolean usePOSTagging, Lang lang) {
        d.setTitleText(adt.getTitle());

        for (AbstractDocumentTemplate.BlockTemplate bt : adt.getBlocks()) {
            bt.setContent(TextPreprocessing.basicTextCleaning(bt.getContent(), lang));
        }
        try {

            if (!adt.getBlocks().isEmpty()) {
                JSONObject doc = doc2JSON(adt, lang);
                String ip = ReadProperty.getProperties("paths.properties").get("PYTHON_ADDRESS").toString();
                String port = ReadProperty.getProperties("paths.properties").get("PYTHON_PORT").toString();
                HttpResponse<JsonNode> response = Unirest
                        .post("http://" + ip + ":" + port + "/spacy")
                        .header("accept", "application/json")
                        .header("Content-Type", "application/json")
                        .body(doc)
                        .asJson();
                JSON2Doc(response.getBody().getObject(), d);
            }
        } catch (JSONException ex) {
            java.util.logging.Logger.getLogger(Parsing.class.getName()).log(Level.SEVERE, null, ex);
        } catch (UnirestException ex) {
            java.util.logging.Logger.getLogger(Parsing.class.getName()).log(Level.SEVERE, null, ex);
        }
        // determine overall word occurrences
        d.determineWordOccurences(d.getBlocks());
        d.determineSemanticDimensions();
    }
}
