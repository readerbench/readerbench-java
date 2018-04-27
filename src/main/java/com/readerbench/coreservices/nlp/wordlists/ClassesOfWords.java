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

import com.readerbench.coreservices.nlp.TextPreprocessing;
import com.readerbench.datasourceprovider.data.AbstractDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Mihai Dascalu
 */
public class ClassesOfWords implements Serializable {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClassesOfWords.class);
    
    private Map<String, Set<String>> classes;
    
    public ClassesOfWords(String path) {
        LOGGER.info("Loading file " + path + " ...");
        classes = new TreeMap<>();
        try {
            FileInputStream inputFile = new FileInputStream(path);
            InputStreamReader ir = new InputStreamReader(inputFile, "UTF-8");
            try (BufferedReader in = new BufferedReader(ir)) {
                String line;
                String className = null;
                while ((line = in.readLine()) != null) {
                    String concept = line.toLowerCase().trim().toLowerCase();
                    if (concept.startsWith("[")) {
                        className = concept.replaceAll("\\[", "").replaceAll("\\]", "").trim();
                        if (!classes.containsKey(className)) {
                            classes.put(className, new TreeSet<>());
                        }
                    } else if (className != null && concept.length() > 0) {
                        classes.get(className).add(concept);
                    }
                }
            }
            LOGGER.info("Finished loading file " + path + " ...");
        } catch (IOException ex) {
            LOGGER.error(ex.getMessage());
        }
    }
    
    public void writeClassesOfWords(String path) {
        try {
            FileOutputStream outputFile = new FileOutputStream(path);
            OutputStreamWriter ow = new OutputStreamWriter(outputFile, "UTF-8");
            try (BufferedWriter out = new BufferedWriter(ow)) {
                for (Entry<String, Set<String>> entry : classes.entrySet()) {
                    out.write("[" + entry.getKey() + "]\n");
                    for (String w : entry.getValue()) {
                        if (w != null & w.length() > 0) {
                            out.write(w + "\n");
                        }
                    }
                }
            }
        } catch (IOException ex) {
            LOGGER.error(ex.getMessage());
        }
    }
    
    public Map<String, Set<String>> getClasses() {
        return classes;
    }
    
    public Set<String> getWords(String className) {
        return classes.get(className);
    }
    
    public Set<String> getAllWords() {
        Set<String> set = new TreeSet<>();
        classes.values().stream().forEach((words) -> {
            set.addAll(words);
        });
        return set;
    }
    
    public static int countPatternOccurrences(String text, String pattern) {
        Pattern p = Pattern.compile("(?:\\s)" + pattern.trim() + "(?:\\s)");
        Matcher matcher = p.matcher(" " + text + " ");
        
        int count = 0;
        if (matcher.find()) {
            count++;
        }
        if (count > 0) {
            while (matcher.find(matcher.end() - 1)) {
                count++;
            }
        }
        
        return count;
    }
    
    public static int countPatternOccurrences(AbstractDocument document, String pattern) {
        return document.getBlocks().stream()
                .filter(b -> b != null)
                .flatMap(b -> b.getSentences().stream())
                .mapToInt(s -> countPatternOccurrences(
                        TextPreprocessing.cleanText(s.getText(), s.getLanguage()), pattern))
                .sum();
    }
    
    public int countCategoryOccurrences(AbstractDocument document, String category) {
        return this.getClasses().get(category).stream()
                .mapToInt(p -> countPatternOccurrences(document, p))
                .sum();
    }

}
