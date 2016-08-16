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
import org.apache.log4j.Logger;

/**
 *
 * @author Stefan
 */
public class Parsing_NL extends Parsing {

    static Logger logger = Logger.getLogger(Parsing_NL.class);

    private static Parsing_NL instance = null;
    private StanfordCoreNLP pipeline = null;

    private Parsing_NL() {
        lang = Lang.nl;
    }

    public static Parsing_NL getInstance() {
        if (instance == null) {
            instance = new Parsing_NL();
        }
        return instance;
    }

    @Override
    public StanfordCoreNLP getPipeline() {
        if (pipeline == null) {
            pipeline = new StanfordCoreNLP(new ParsingParams_NL());
        }
        return pipeline;
    }

    class ParsingParams_NL extends Properties {

        private static final long serialVersionUID = -161579346323407322L;

        public ParsingParams_NL() {
            super();
            //this.put("pos.model", "resources/config/POSmodels/italian.tagger");
            this.put("annotators", "");
        }
    }

}
