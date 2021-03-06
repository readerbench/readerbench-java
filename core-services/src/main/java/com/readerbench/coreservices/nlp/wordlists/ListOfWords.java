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
package com.readerbench.coreservices.nlp.wordlists;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Set;
import java.util.TreeSet;

/**
 *
 * @author Mihai Dascalu
 */
public class ListOfWords {

    private static final Logger LOGGER = LoggerFactory.getLogger(ListOfWords.class);

    private Set<String> words;

    public ListOfWords(String path) {
        LOGGER.info("Loading file {} ...", path);
        words = new TreeSet<>();
        try (BufferedReader in = new BufferedReader(new InputStreamReader(this.getClass().getClassLoader().getResourceAsStream(path), "UTF-8"))) {
            String line;
            while ((line = in.readLine()) != null) {
                String word = line.toLowerCase().trim();
                if (word.length() > 0) {
                    words.add(word);
                }
            }
        } catch (IOException ex) {
            LOGGER.error(ex.getMessage());
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
            LOGGER.error(ex.getMessage());
        }
    }

    public Set<String> getWords() {
        return words;
    }

    public void setWords(Set<String> words) {
        this.words = words;
    }
}
