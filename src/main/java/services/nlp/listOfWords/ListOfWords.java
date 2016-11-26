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
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Mihai Dascalu
 */
public class ListOfWords {

    private static Logger LOGGER = Logger.getLogger("");

    private Set<String> words;

    public ListOfWords() {
    }

    public ListOfWords(String path) {
        LOGGER.log(Level.INFO, "Loading file {0} ...", path);
        words = new TreeSet<>();
        try {
            FileInputStream inputFile = new FileInputStream(path);
            //InputStreamReader ir = new InputStreamReader(inputFile, "ISO-8859-1");
            InputStreamReader ir = new InputStreamReader(inputFile, "UTF-8");
            try (BufferedReader in = new BufferedReader(ir)) {
                String line;
                while ((line = in.readLine()) != null) {
                    String word = line.toLowerCase().trim();
                    if (word.length() > 0) {
                        words.add(word);
                    }
                }
            }
        } catch (IOException ex) {
            LOGGER.severe(ex.getMessage());
        }
    }

    public void writeListOfWords(String path) {
        try {
            FileOutputStream outputFile = new FileOutputStream(path);
            OutputStreamWriter ow = new OutputStreamWriter(outputFile, "UTF-8");
            try (BufferedWriter out = new BufferedWriter(ow)) {
                for (String w : words) {
                    if (w != null & w.length() > 0) {
                        out.write(w + "\n");
                    }
                }
            }
        } catch (IOException ex) {
            LOGGER.severe(ex.getMessage());
        }
    }

    public Set<String> getWords() {
        return words;
    }

    public void setWords(Set<String> words) {
        this.words = words;
    }
}
