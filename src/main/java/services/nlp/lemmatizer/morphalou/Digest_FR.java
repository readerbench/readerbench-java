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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.digester3.Digester;
import org.apache.log4j.Logger;
import org.openide.util.Exceptions;
import org.xml.sax.SAXException;

public class Digest_FR {

    static Logger logger = Logger.getLogger(Digest_FR.class);
    public static final String PATH_TO_TEXT_LEMMAS_FR = "resources/config/FR/word lists/lemmas_pos_fr.txt";
    public static final String PATH_TO_MORPHALOU_FR = "resources/corpora/FR/Morphalou-2.0.xml";

    public static void parseMorpholau() throws IOException, SAXException {
        logger.info("Parsing Morphalou");
        double time = System.currentTimeMillis();
        Digester digester = new Digester();
        digester.setValidating(false);
        digester.addObjectCreate("lexicon", Lexicon.class);
        digester.addObjectCreate("lexicon/lexicalEntry", LexicalEntry.class);
        digester.addBeanPropertySetter(
                "lexicon/lexicalEntry/formSet/lemmatizedForm/orthography",
                "orthography");
        digester.addBeanPropertySetter(
                "lexicon/lexicalEntry/formSet/lemmatizedForm/grammaticalCategory",
                "grammaticalCategory");
        digester.addBeanPropertySetter(
                "lexicon/lexicalEntry/formSet/lemmatizedForm/grammaticalGender",
                "grammaticalGender");
        digester.addObjectCreate("lexicon/lexicalEntry/formSet/inflectedForm",
                InflectedForm.class);
        digester.addBeanPropertySetter(
                "lexicon/lexicalEntry/formSet/inflectedForm/orthography",
                "orthography");
        digester.addBeanPropertySetter(
                "lexicon/lexicalEntry/formSet/inflectedForm/grammaticalNumber",
                "grammaticalNumber");
        digester.addBeanPropertySetter(
                "lexicon/lexicalEntry/formSet/inflectedForm/grammaticalGender",
                "grammaticalGender");
        digester.addBeanPropertySetter(
                "lexicon/lexicalEntry/formSet/inflectedForm/grammaticalMood",
                "grammaticalMood");
        digester.addBeanPropertySetter(
                "lexicon/lexicalEntry/formSet/inflectedForm/grammaticalTense",
                "grammaticalTense");
        digester.addBeanPropertySetter(
                "lexicon/lexicalEntry/formSet/inflectedForm/grammaticalPerson",
                "grammaticalPerson");
        digester.addSetNext("lexicon/lexicalEntry/formSet/inflectedForm",
                "addInflectedForm");
        digester.addSetNext("lexicon/lexicalEntry", "addLexicalEntry");
        File inputFile = new File(PATH_TO_MORPHALOU_FR);
        Lexicon lexicon = (Lexicon) digester.parse(inputFile);
        logger.info("Finished in " + (System.currentTimeMillis() - time) / 1000);

        writeLemmas(lexicon);
    }

    public static Object loadObject(String name) {
        try {
            ObjectInputStream iIn = new ObjectInputStream(new FileInputStream(
                    name));
            Object a = iIn.readObject();
            iIn.close();
            return a;
        } catch (FileNotFoundException | ClassNotFoundException ex) {
            Exceptions.printStackTrace(ex);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        return null;
    }

    private static void writeLemmas(Lexicon lexicon) throws IOException {
        logger.info("Starting to write output");
        BufferedWriter out = new BufferedWriter(new FileWriter(
                PATH_TO_TEXT_LEMMAS_FR));
        Map<String, String> lemmas = new TreeMap<String, String>();
        for (LexicalEntry le : lexicon.getLexicalEntries()) {
            if (le.getGrammaticalCategory() != null) {
                lemmas.put(
                        le.getOrthography().replaceAll("se ", "") + "_"
                        + le.getGrammaticalCategory(), le
                        .getOrthography().replaceAll("se ", ""));
            } else {
                lemmas.put(le.getOrthography().replaceAll("se ", ""), le
                        .getOrthography().replaceAll("se ", ""));
            }
            if (le.getInflectedForms().size() > 0) {
                for (InflectedForm iform : le.getInflectedForms()) {
                    if (le.getGrammaticalCategory() != null) {
                        lemmas.put(iform.getOrthography().replaceAll("se ", "")
                                + "_" + le.getGrammaticalCategory(), le
                                .getOrthography().replaceAll("se ", ""));
                    } else {
                        lemmas.put(
                                iform.getOrthography().replaceAll("se ", ""),
                                le.getOrthography().replaceAll("se ", ""));
                    }
                }
            }
        }

        for (String lemma : lemmas.keySet()) {
            out.write(lemma + "|" + lemmas.get(lemma) + "\n");
        }
        out.close();
        logger.info("Finished writing output file");
    }

    public static void main(String[] args) {
        // BasicConfigurator.configure();
        try {
            parseMorpholau();
        } catch (Exception exc) {
            exc.printStackTrace();
        }
    }
}
