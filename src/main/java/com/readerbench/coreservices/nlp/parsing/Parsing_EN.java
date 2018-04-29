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

public class Parsing_EN extends Parsing {

    private static Parsing_EN instance = null;

    private final StanfordCoreNLP pipeline = new StanfordCoreNLP(new ParsingParams_EN());

    private Parsing_EN() {
        lang = Lang.en;
    }

    public static Parsing_EN getInstance() {
        if (instance == null) {
            instance = new Parsing_EN();
        }
        return instance;
    }

    @Override
    public StanfordCoreNLP getPipeline() {
        return pipeline;
    }
}

/**
 * @return
 */
class ParsingParams_EN extends Properties {

    private static final long serialVersionUID = -161579346328207322L;

    public ParsingParams_EN() {
        super();
        // TODO: sentiment should be parameterized
        this.put("annotators", "tokenize, ssplit, pos,parse, lemma,depparse, ner, mention, coref, sentiment");
        this.put("coref.md.type", "dependency");
        this.put("parse.maxlen", "100");
        this.put("ner.useSUTime", "0");
    }
}
