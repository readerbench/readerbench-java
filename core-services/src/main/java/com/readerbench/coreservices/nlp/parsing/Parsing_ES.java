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

import com.readerbench.datasourceprovider.pojo.Lang;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;

import java.util.Properties;

public class Parsing_ES extends Parsing {

    private static Parsing_ES instance = null;

    private final StanfordCoreNLP pipeline = new StanfordCoreNLP(new ParsingParams_ES());

    private Parsing_ES() {
        lang = Lang.es;
    }

    public static Parsing_ES getInstance() {
        if (instance == null) {
            instance = new Parsing_ES();
        }
        return instance;
    }

    @Override
    public String convertToPenn(String pos) {
        // rename Spanish POS -
        // http://nlp.lsi.upc.edu/freeling/doc/tagsets/tagset-es.html according
        // to the Pen TreeBank POSs
        // http://www.ling.upenn.edu/courses/Fall_2003/ling001/penn_treebank_pos.html
        if (pos.startsWith("d")) {
            return "DT";
        }
        if (pos.startsWith("n")) {
            return "NN";
        }
        if (pos.startsWith("v")) {
            return "VB";
        }
        if (pos.startsWith("p")) {
            return "PR";
        }
        if (pos.startsWith("cc")) {
            return "CC";
        }
        if (pos.startsWith("cs") || pos.startsWith("s")) {
            return "IN";
        }
        if (pos.startsWith("i")) {
            return "UH";
        }
        if (pos.startsWith("r")) {
            return "RB";
        }
        if (pos.startsWith("a")) {
            return "JJ";
        }
        return pos;
    }

    @Override
    public StanfordCoreNLP getPipeline() {
        return pipeline;
    }
}

class ParsingParams_ES extends Properties {

    private static final long serialVersionUID = -161579346328207322L;

    public ParsingParams_ES() {
        this.put("tokenize.language", "es");
        this.put("pos.model", "edu/stanford/nlp/models/pos-tagger/spanish/spanish-distsim.tagger");
        this.put("ner.model", "edu/stanford/nlp/models/ner/spanish.ancora.distsim.s512.crf.ser.gz");
        this.put("ner.applyNumericClassifiers", "false");
        this.put("parse.model", "edu/stanford/nlp/models/lexparser/spanishPCFG.ser.gz");
        this.put("annotators", "tokenize, ssplit, pos, ner, parse");
        this.put("parse.maxlen", "100");
        this.put("ner.useSUTime", "0");
    }
}
