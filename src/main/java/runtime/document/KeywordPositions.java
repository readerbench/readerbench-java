/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package runtime.document;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.util.Exceptions;

/**
 *
 * @author Gabriel Gutu <gabriel.gutu at cs.pub.ro>
 */
public class KeywordPositions {
    
    private static final Logger LOGGER = Logger.getLogger("");
    
    private final String path;
    private final String sourceFile;
    private final Integer charsInSourceFile;
    private final String keywordsFile;
    private final String outputFile;
    
    private final Map<String, List<Integer>> lemmaPositions;
    private final Map<String, List<Integer>> lemmaPositionDistances;
    private final Map<String, Double> lemmaAld;
    private final Map<String, List<Integer>> auxLmmaPositions;
    private final Map<String, String> wordToLemma;
    private String fullText;
    
    public KeywordPositions(String path, String sourceFile, Integer charsInSourceFile, String keywordsFile, String outputFile) {
        this.path = path;
        this.sourceFile = sourceFile;
        this.charsInSourceFile = charsInSourceFile;
        this.keywordsFile = keywordsFile;
        this.outputFile = outputFile;
        lemmaPositions = new LinkedHashMap<>();
        lemmaPositionDistances = new LinkedHashMap<>();
        lemmaAld = new HashMap<>();
        auxLmmaPositions = new LinkedHashMap<>();
        wordToLemma = new HashMap<>();
    }
    
    private void extractKeywords(int noIgnoreLines) {
        LOGGER.info("Extracting keywords...");
        try (BufferedReader br = new BufferedReader(new FileReader(path + "/" + keywordsFile))) {
            String line;
            for (int i = 0; i < noIgnoreLines; i++) br.readLine();
            while ((line = br.readLine()) != null) {
                //LOGGER.log(Level.INFO, "Read line {0}", line);
                if (line.length() <= 1) break;
                //LOGGER.log(Level.INFO, "Line: {0} (length = {1})", new Object[]{line, line.length()});
                String[] data = line.split(";");
                String word = data[1];
                String lemma = data[2];
                if (lemmaPositions.get(lemma) == null) {
                    lemmaPositions.put(lemma, new ArrayList<>());
                }
                LOGGER.log(Level.INFO, "Adding lemma {0}", lemma);
                if (wordToLemma.get(word) == null) {
                    wordToLemma.put(word, lemma);
                }
                if (data[0].compareTo("ngram") == 0) {
                    String[] words = data[1].split(" ");
                    String[] lemmas = data[2].split("_");
                    for (int i = 0; i < 2; i++) {
                        if (auxLmmaPositions.get(lemmas[i]) == null) {
                            auxLmmaPositions.put(lemmas[i], new ArrayList<>());
                        }
                        if (wordToLemma.get(words[i]) == null) {
                            wordToLemma.put(words[i], lemmas[i]);
                        }
                    }
                }
            }
            lemmaPositions.put("end_here", new ArrayList<>());
            for (Map.Entry<String, List<Integer>> lemmaPos : auxLmmaPositions.entrySet()) {
                if (lemmaPositions.get(lemmaPos.getKey())  == null) {
                    lemmaPositions.put(lemmaPos.getKey(), new ArrayList<>());
                }
//                for (Integer pos : lemmaPos.getValue()) {
//                    lemmaPositions.get(lemmaPos.getKey()).add(pos);
//                }
            }
        } catch (FileNotFoundException ex) {
            Exceptions.printStackTrace(ex);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
    
    private void countPositions() {
        LOGGER.info("Counting positions...");
        for(String word : wordToLemma.keySet()) {
            LOGGER.log(Level.INFO, "Counting positions for word {0}...", word);
            int lastIndex = 0;
            do {
                lastIndex = fullText.indexOf(word, lastIndex);
                if (lastIndex != -1) {
                    lemmaPositions.get(wordToLemma.get(word)).add(lastIndex);
                    lastIndex += word.length();
                }
            } while (lastIndex != -1);
        }
    }
    
    private void orderPositions() {
        for (Map.Entry<String, List<Integer>> lemmaPos : lemmaPositions.entrySet()) {
            Collections.sort(lemmaPos.getValue());
        }
    }
    
    private void countPositionDifferences() {
        for (String lemma : lemmaPositions.keySet()) {
            lemmaPositionDistances.put(lemma, new ArrayList<>());
        }
        for (Map.Entry<String, List<Integer>> lemmaPos : lemmaPositions.entrySet()) {
            boolean isFirst = true;
            Integer oldPos = 0;
            Double sumDistances = 0.0;
            if (lemmaPos.getValue().size() == 1) {
                lemmaPositionDistances.get(lemmaPos.getKey()).add(fullText.length());
                sumDistances += fullText.length() * Math.log10(fullText.length());
            }
            else {
                for (Integer pos : lemmaPos.getValue()) {
                    int dist;
                    if (isFirst) {
                        dist = pos + fullText.length() - lemmaPos.getValue().get(lemmaPos.getValue().size() - 1) + 1;
                        isFirst = false;
                        LOGGER.log(Level.INFO, "Distance: {0} (computed of: pos = {1}, fulltext = {2}, prev = {3}", new Object[]{dist, pos, fullText.length(), lemmaPos.getValue().get(lemmaPos.getValue().size() - 1)});
                    }
                    else {
                        dist = pos - oldPos + 1;
                        if (oldPos > pos) {
                            LOGGER.log(Level.INFO, "Positions for lemma: {0}: {1}", new Object[]{lemmaPos.getKey(), lemmaPos.getValue()});
                        }
                        LOGGER.log(Level.INFO, "Distance: {0} (computed of: pos = {1}, oldpos = {2}", new Object[]{dist, pos, oldPos});                        
                    }
                    lemmaPositionDistances.get(lemmaPos.getKey()).add(dist);
                    double logDistance = dist * Math.log10(dist);
                    LOGGER.log(Level.INFO, "Log distance: {0}", logDistance);
                    sumDistances += logDistance;
                    LOGGER.log(Level.INFO, "Last sum distances {0}", sumDistances);
                    oldPos = pos;
                }
            }
            LOGGER.log(Level.INFO, "Computed distance as being {0}", sumDistances);
            if (lemmaPos.getValue().size() > 0) lemmaAld.put(lemmaPos.getKey(), sumDistances / charsInSourceFile);
            else lemmaAld.put(lemmaPos.getKey(), -1.0);
        }
    }
    
    private void printPositions(boolean printPositions) {
        LOGGER.info("Printing positions...");
        try (BufferedWriter outFile = new BufferedWriter(new FileWriter(path + "/" + outputFile, true))) {
            StringBuilder sb = new StringBuilder();
            sb.append("lemma;occ;relevance;positions\n");
            outFile.write(sb.toString());
            outFile.flush();
            for (Map.Entry<String, List<Integer>> lemmaPos : lemmaPositions.entrySet()) {
                sb.append(lemmaPos.getKey()).append(";");
                sb.append(lemmaPos.getValue().size()).append(";");
                if (lemmaAld.get(lemmaPos.getKey()) != -1) {
                    sb.append(lemmaAld.get(lemmaPos.getKey())).append(";");
                }
                else {
                    sb.append(";");
                }
                if (printPositions) {
                    int k = 0;
                    for (Integer pos : lemmaPos.getValue()) {
                        sb.append(pos).append(";");
                        sb.append(lemmaPositionDistances.get(lemmaPos.getKey()).get(k++)).append(";");
                    }
                }
                sb.append("\n");
                outFile.write(sb.toString());
                outFile.flush();
                sb.setLength(0);
            }
            
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
    
    private void readSourceFile() {
        LOGGER.info("Reading source file...");
        StringBuilder sb = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader(path + "/" + sourceFile))) {
            String line;
            while ((line = br.readLine()) != null) {
               sb.append(line).append("\n");
            }
        } catch (FileNotFoundException ex) {
            Exceptions.printStackTrace(ex);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        fullText = sb.toString();
    }
    
    public static void main(String args[]) {
        String path = "resources/in/SciCorefCorpus/fulltexts/all";
        String sourceFile = "all_texts.txt";
        Integer charsInSourceFile = 10814972;
        String keywordsFile = "keywords_lsa_sciref_10_percent.csv";
        String outputFile = "keywords_updated_relevance_lsa_sciref_10_percent.csv";
        KeywordPositions kp = new KeywordPositions(path, sourceFile, charsInSourceFile, keywordsFile, outputFile);
        kp.readSourceFile();
        kp.extractKeywords(2);
        kp.countPositions();
        kp.orderPositions();
        kp.countPositionDifferences();
        kp.printPositions(false);        
    }
    
}
