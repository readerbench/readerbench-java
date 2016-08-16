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
package services.nlp.listOfWords;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Set;
import java.util.TreeSet;

import org.apache.log4j.Logger;

/**
 *
 * @author Mihai Dascalu
 */
public class ListOfWords {

    private static Logger logger = Logger.getLogger(ListOfWords.class);

    private Set<String> words;
    
    public ListOfWords() {
    	
    }

    public ListOfWords(String path) {
        logger.info("Loading " + path + "...");
        BufferedReader in = null;
        words = new TreeSet<String>();
        try {
            FileInputStream inputFile = new FileInputStream(path);
            //InputStreamReader ir = new InputStreamReader(inputFile, "ISO-8859-1");
            InputStreamReader ir = new InputStreamReader(inputFile, "UTF-8");
            in = new BufferedReader(ir);
            String line = null;
            while ((line = in.readLine()) != null) {
                String word = line.toLowerCase().trim();
                if (word.length() > 0) {
                    words.add(word);
                }
            }
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

    public void writeListOfWords(String path) {
        BufferedWriter out = null;
        try {
            FileOutputStream outputFile = new FileOutputStream(path);
            OutputStreamWriter ow = new OutputStreamWriter(outputFile, "UTF-8");
            out = new BufferedWriter(ow);
            for (String w : words) {
                if (w != null & w.length() > 0) {
                    out.write(w + "\n");
                }
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

    public Set<String> getWords() {
        return words;
    }

    public void setWords(Set<String> words) {
        this.words = words;
    }

    public static void main(String[] args) {

        ListOfWords dict1 = new ListOfWords("resources/config/Dictionary/dict_nl_full.txt");
        //ListOfWords dict2 = new ListOfWords("resources/config/Dictionary/EN/names_en.txt");

        //Set<String> finalWords = new TreeSet<String>();
        //finalWords.addAll(dict1.getWords());
        //finalWords.addAll(dict2.getWords());
        // for (String w : dict1.getWords()) {
        // if (!dict2.getWords().contains(w))
        // finalWords.add(w);
        // if (w.matches("[A-Z].*") && !dict2.getWords().contains(w))
        // finalWords.add(w);
        // if (w.matches("[a-z].*"))
        // finalWords.add(w);
        // }
        //dict1.setWords(finalWords);
        dict1.writeListOfWords("resources/config/Dictionary/dict_nl.txt");
    }
}
