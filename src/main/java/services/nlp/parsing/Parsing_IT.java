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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Properties;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;

import data.Lang;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import org.apache.log4j.BasicConfigurator;

public class Parsing_IT extends Parsing {

    static final Logger logger = Logger.getLogger(Parsing_IT.class);

    private static Parsing_IT instance = null;

    private Parsing_IT() {
        lang = Lang.it;
    }

    public static Parsing_IT getInstance() {
        if (instance == null) {
            instance = new Parsing_IT();
        }
        return instance;
    }

    public static void parseTrainingCorpus(String pathInput, String pathOutput) {
        try {
            FileInputStream inputFile = new FileInputStream(pathInput);
            InputStreamReader ir = new InputStreamReader(inputFile, "UTF-8");
            BufferedWriter out;
            try (BufferedReader in = new BufferedReader(ir)) {
                FileOutputStream outputFile = new FileOutputStream(pathOutput);
                OutputStreamWriter ow = new OutputStreamWriter(outputFile, "UTF-8");
                out = new BufferedWriter(ow);
                String line;
                String outputLine = "";
                while ((line = in.readLine()) != null) {
                    StringTokenizer st = new StringTokenizer(line.trim(), "\t");
                    String word = null, pos = null;
                    if (st.hasMoreTokens()) {
                        word = st.nextToken().toLowerCase();
                    }
                    if (st.hasMoreTokens()) {
                        pos = st.nextToken();
                    }
                    if (word != null && pos != null && !word.contains(" ")) {
                        if (pos.startsWith("ADJ")) {
                            // adjectives
                            outputLine += word + "_JJ" + " ";
                        } else if (pos.startsWith("ADV")) {
                            // adverbs
                            outputLine += word + "_RB" + " ";
                        } else if (pos.startsWith("ART") || pos.startsWith("CON") || pos.startsWith("PRE")) {
                            // prepositions or subordinating conjunctions
                            outputLine += word + "_IN" + " ";
                        } else if (pos.startsWith("DET")) {
                            // nouns
                            outputLine += word + "_DT" + " ";
                        } else if (pos.startsWith("AUX") || pos.startsWith("VER")) {
                            // verbs
                            outputLine += word + "_VB" + " ";
                        } else if (pos.startsWith("NOUN") || pos.startsWith("NPR")) {
                            // nouns
                            outputLine += word + "_NN" + " ";
                        } else if (pos.startsWith("PRO")) {
                            // pronouns
                            outputLine += word + "_PR" + " ";
                        } else if (pos.startsWith("INT")) {
                            // interjections
                            outputLine += word + "_UH" + " ";
                        } else if (pos.startsWith("PUN")) {
                            // interjections
                            outputLine += word + "_" + word + " ";
                        } else if (pos.startsWith("SENT")) {
                            // end of sentences
                            outputLine += word + "_" + word + "\n";
                            out.write(outputLine);
                            outputLine = "";
                        }
                    }
                }
            }
            out.close();
        } catch (IOException ex) {
            logger.error(ex.getMessage());
        }
    }

    private final StanfordCoreNLP pipeline = new StanfordCoreNLP(new ParsingParams_IT());

    @Override
    public StanfordCoreNLP getPipeline() {
        return pipeline;
    }

    public static void main(String[] args) {
        BasicConfigurator.configure();
        parseTrainingCorpus("resources/corpora/IT/train_it.txt", "resources/corpora/IT/train_PENN_it.txt");
    }
}

class ParsingParams_IT extends Properties {

    private static final long serialVersionUID = -161579346328207322L;

    public ParsingParams_IT() {
        super();
        this.put("pos.model", "resources/config/IT/tagger/italian.tagger");
        this.put("annotators", "tokenize, ssplit, pos");
    }
}
