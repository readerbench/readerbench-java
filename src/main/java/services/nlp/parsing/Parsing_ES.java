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

import java.util.Properties;

import org.apache.log4j.BasicConfigurator;

import data.AbstractDocument;
import data.AbstractDocumentTemplate;
import data.AbstractDocumentTemplate.BlockTemplate;
import data.document.Document;
import data.Lang;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import java.util.ArrayList;

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

    public static void main(String[] args) {
        BasicConfigurator.configure();
        AbstractDocumentTemplate docTmp = getDocumentModel();
        AbstractDocument d = new Document(null, docTmp, new ArrayList<>(), Lang.es, true);
        System.out.println(d);
    }

    /**
     * @return
     */
    protected static AbstractDocumentTemplate getDocumentModel() {
        AbstractDocumentTemplate docTmp = new AbstractDocumentTemplate();
        BlockTemplate block = docTmp.new BlockTemplate();
        block.setId(0);
        block.setContent(
                "Yo sí soy muy macho -grita uno- Yo me voy. Agarra sus muebles, sus hijos, sus animales, los mete en una carreta y atraviesa la calle central donde todo el pueblo lo ve. Hasta que todos dicen: Si este se atreve, pues nosotros también nos vamos. Y empiezan a desmantelar literalmente el pueblo. Se llevan las cosas, los animales, todo.Y uno de los últimos que abandona el pueblo, dice: Que no venga la desgracia a caer sobre lo que queda de nuestra casa, y entonces la incendia y otros incendian también sus casas.Huyen en un tremendo y verdadero pánico, como en un éxodo de guerra, y en medio de ellos va la señora que tuvo el presagio, le dice a su hijo que está a su lado: Vistes m'hijo, que algo muy grave iba a suceder en este pueblo?");
        docTmp.getBlocks().add(block);
        return docTmp;
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
        this.put("ner.useSUTime", "false");
        this.put("parse.model", "edu/stanford/nlp/models/lexparser/spanishPCFG.ser.gz");
        this.put("annotators", "tokenize, ssplit, pos, ner, parse");
    }
}
