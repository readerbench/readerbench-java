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
package services.nlp.parsing;

import data.Lang;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import java.util.Properties;
import java.util.logging.Logger;


/**
 *
 * @author Stefan
 */
public class Parsing_LA extends Parsing {

    static final Logger logger = Logger.getLogger("");

    private static Parsing_LA instance = null;
    private final StanfordCoreNLP pipeline = new StanfordCoreNLP(new ParsingParams_LA());

    private Parsing_LA() {
        lang = Lang.la;
    }

    public static Parsing_LA getInstance() {
        if (instance == null) {
            instance = new Parsing_LA();
        }
        return instance;
    }

    @Override
    public StanfordCoreNLP getPipeline() {
        return pipeline;
    }

    class ParsingParams_LA extends Properties {

        private static final long serialVersionUID = -1561330268167277821L;

        public ParsingParams_LA() {
            super();
            this.put("annotators", "");
        }
    }
}
