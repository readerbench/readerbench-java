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
package com.readerbench.services.nlp.parsing;

import com.readerbench.data.AbstractDocument;
import com.readerbench.data.AbstractDocumentTemplate;
import com.readerbench.data.AbstractDocumentTemplate.BlockTemplate;
import com.readerbench.data.Lang;
import com.readerbench.data.document.Document;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;

import java.util.ArrayList;
import java.util.Properties;

public class Parsing_FR extends Parsing {

    private static Parsing_FR instance = null;

    private final StanfordCoreNLP pipeline = new StanfordCoreNLP(new ParsingParams_FR());

    private Parsing_FR() {
        lang = Lang.fr;
    }

    public static Parsing_FR getInstance() {
        if (instance == null) {
            instance = new Parsing_FR();
        }
        return instance;
    }

    public static void main(String[] args) {
        
        AbstractDocumentTemplate docTmp = getDocumentModel();
        AbstractDocument d = new Document(null, docTmp, new ArrayList<>(), Lang.fr, true);
        System.out.println(d);
    }

    /**
     * @return
     */
    protected static AbstractDocumentTemplate getDocumentModel() {
        AbstractDocumentTemplate docTmp = new AbstractDocumentTemplate();
        BlockTemplate block = docTmp.new BlockTemplate();
        block.setId(0);
        block.setContent("Ce soir-là, la famille de Matilda "
                + "dînait rapidement comme d'habitude devant la télévision.\nTout est bon.");
        docTmp.getBlocks().add(block);
        return docTmp;
    }

    @Override
    public String convertToPenn(String pos) {
        // rename French POS according to the Pen TreeBank POSs
        // http://www.ling.upenn.edu/courses/Fall_2003/ling001/penn_treebank_pos.html
        if (pos.startsWith("N")) {
            return "NN";
        }
        if (pos.startsWith("V")) {
            return "VB";
        }
        if (pos.startsWith("CL")) {
            return "PR";
        }
        if (pos.startsWith("C")) {
            return "CC";
        }
        if (pos.startsWith("D")) {
            return "IN";
        }
        if (pos.startsWith("ADV")) {
            return "RB";
        }
        if (pos.startsWith("A")) {
            return "JJ";
        }
        return pos;
    }

    @Override
    public StanfordCoreNLP getPipeline() {
        return pipeline;
    }
}

class ParsingParams_FR extends Properties {

    private static final long serialVersionUID = -161579346328207322L;

    public ParsingParams_FR() {
        this.put("tokenize.language", "fr");
        this.put("pos.model", "edu/stanford/nlp/models/pos-tagger/french/french.tagger");
        //this.put("parse.model", "edu/stanford/nlp/models/lexparser/frenchFactored.ser.gz");
        this.put("depparse.model", "edu/stanford/nlp/models/parser/nndep/UD_French.gz");
        this.put("annotators", "tokenize, ssplit, pos, depparse");
        this.put("parse.maxlen", "100");
    }
}
