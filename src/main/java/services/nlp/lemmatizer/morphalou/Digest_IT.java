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
package services.nlp.lemmatizer.morphalou;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeMap;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;

public class Digest_IT {

    private static final Logger logger = Logger.getLogger(Digest_IT.class);

    public static final String PATH_TO_TEXT_LEMMAS_IT = "resources/config/IT/word lists/lemmas_pos_it.txt";
    public static final String PATH_TO_MORPH_IT = "resources/corpora/IT/morph-it_048.txt";

    private static Map<String, String> lemmas;

    public static void parseMorphIt() {
        BufferedReader in = null;
        lemmas = new TreeMap<>();
        try {
            FileInputStream inputFile = new FileInputStream(PATH_TO_MORPH_IT);
            InputStreamReader ir = new InputStreamReader(inputFile, "UTF-8");
            in = new BufferedReader(ir);
            String line = null;
            while ((line = in.readLine()) != null) {
                StringTokenizer st = new StringTokenizer(line.trim(), "\t");
                String inflectedForm = null, lemma = null, pos = null;
                if (st.hasMoreTokens()) {
                    inflectedForm = st.nextToken();
                }
                if (st.hasMoreTokens()) {
                    lemma = st.nextToken();
                }
                if (st.hasMoreTokens()) {
                    pos = st.nextToken();
                }
                if (inflectedForm != null && lemma != null && pos != null) {
                    if (pos.startsWith("ADJ")) {
                        // adjectives
                        lemmas.put(inflectedForm + "_JJ", lemma);
                    } else if (pos.startsWith("ADV")) {
                        // adverbs
                        lemmas.put(inflectedForm + "_RB", lemma);
                    } else if (pos.startsWith("ART") || pos.startsWith("CON")
                            || pos.startsWith("PRE")) {
                        // prepositions or subordinating conjunctions
                        lemmas.put(inflectedForm + "_IN", lemma);
                    } else if (pos.startsWith("DET")) {
                        // nouns
                        lemmas.put(inflectedForm + "_DT", lemma);
                    } else if (pos.startsWith("AUX") || pos.startsWith("VER")) {
                        // verbs
                        lemmas.put(inflectedForm + "_VB", lemma);
                    } else if (pos.startsWith("NOUN")) {
                        // nouns
                        lemmas.put(inflectedForm + "_NN", lemma);
                    } else if (pos.startsWith("PRO")) {
                        // pronouns
                        lemmas.put(inflectedForm + "_PR", lemma);
                    } else if (pos.startsWith("INT")) {
                        // interjections
                        lemmas.put(inflectedForm + "_UH", lemma);
                    }
                }
            }

            writeLemmas();
        } catch (IOException ex) {
            logger.error(ex.getMessage());
        } finally {
            try {
                in.close();
            } catch (IOException ex) {
                logger.error(ex.getMessage());
            }
        }
    }

    public static void writeLemmas() {
        BufferedWriter out = null;
        try {
            FileOutputStream outputFile = new FileOutputStream(
                    PATH_TO_TEXT_LEMMAS_IT);
            OutputStreamWriter ow = new OutputStreamWriter(outputFile, "UTF-8");
            out = new BufferedWriter(ow);
            for (String lemma : lemmas.keySet()) {
                out.write(lemma + "|" + lemmas.get(lemma) + "\n");
            }
        } catch (IOException ex) {
            logger.error(ex.getMessage());
        } finally {
            try {
                out.close();
            } catch (IOException ex) {
                logger.error(ex.getMessage());
            }
        }
    }

    public static void main(String[] args) {
        BasicConfigurator.configure();
        parseMorphIt();
    }
}
