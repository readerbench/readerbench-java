/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
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
    private final StanfordCoreNLP pipeline = new StanfordCoreNLP(new ParsingParams_NL());

    private Parsing_NL() {
        lang = Lang.ro;
    }

    public static Parsing_NL getInstance() {
        if (instance == null) {
            instance = new Parsing_NL();
        }
        return instance;
    }

    @Override
    public StanfordCoreNLP getPipeline() {
        return pipeline;
    }

    class ParsingParams_NL extends Properties {

        private static final long serialVersionUID = -161579346323407322L;

        public ParsingParams_NL() {
            super();
            //this.put("pos.model", "resources/config/POSmodels/italian.tagger");
            //this.put("annotators", "tokenize, ssplit, pos");
        }
    }

}
