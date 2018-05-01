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
package com.readerbench.processingservice;

import com.readerbench.coreservices.data.AbstractDocument;
import com.readerbench.coreservices.data.Block;
import com.readerbench.coreservices.data.Sentence;
import com.readerbench.coreservices.data.Word;
import com.readerbench.coreservices.data.Sentence;
import com.readerbench.coreservices.data.Word;
import com.readerbench.coreservices.data.AbstractDocumentTemplate;
import com.readerbench.coreservices.data.AbstractDocument;
import com.readerbench.coreservices.data.Block;
import com.readerbench.coreservices.data.document.Document;
import com.readerbench.coreservices.nlp.TextPreprocessing;
import com.readerbench.coreservices.nlp.wordlists.Dictionary;
import com.readerbench.coreservices.nlp.wordlists.ListOfWords;
import com.readerbench.coreservices.semanticmodels.lsa.LSA;
import com.readerbench.datasourceprovider.pojo.Lang;
import com.readerbench.processingservice.document.DocumentProcessingPipeline;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.*;
import java.util.Map.Entry;

public class PreProcessing {

    private static final Logger LOGGER = LoggerFactory.getLogger(PreProcessing.class);

    public static final int MIN_NO_OCCURRENCES = 5;

    private final Map<String, Integer> newConcepts = new TreeMap<>();

    private String parseDocumentProcessing(AbstractDocument d, int noMinWordPerDoc, ListOfWords wordsToIgnore) {
        // returns new entries to write
        StringBuilder toWrite = new StringBuilder();
        List<Word> document = new ArrayList<>();
        int no_words_to_write = 0;
        for (Block b : d.getBlocks()) {
            // combine with previous paragraph, if the case
            for (Sentence s : b.getSentences()) {
                for (Word w : s.getWords()) {
                    if (wordsToIgnore == null || (!wordsToIgnore.getWords().contains(w.getLemma()))) {
                        no_words_to_write++;
                        document.add(w);
                    }
                }
                document.add(new Word(".", ".", ".", null, null, d.getLanguage()));
            }
            if (no_words_to_write >= noMinWordPerDoc) {
                // flush the actual contents of the document
                document.stream().forEach((w) -> {
                    toWrite.append(w.getLemma()).append(" ");
                });
                toWrite.append("\n");
                document.clear();
                no_words_to_write = 0;
            }
        }
        return toWrite.toString();
    }

    public String processContent(String content, Lang lang, boolean usePOSTagging, int noMinWordPerDoc, ListOfWords wordsToIgnore) {
        String text = TextPreprocessing.cleanText(content, lang);
        AbstractDocumentTemplate docTmp = getDocumentModel(text);

        StringTokenizer st = new StringTokenizer(text, " \\.,:;!?-+[](){}'’“”\"");

        // determine new concepts
        while (st.hasMoreTokens()) {
            String word = st.nextToken().trim();
            if (word.length() > 0 && !Dictionary.getDictionaryWords(lang).contains(word)) {
                if (newConcepts.containsKey(word)) {
                    newConcepts.put(word, newConcepts.get(word) + 1);
                } else {
                    newConcepts.put(word, 1);
                }
            }
        }
        // perform processing
        DocumentProcessingPipeline pipeline = new DocumentProcessingPipeline(Lang.en, new ArrayList<>(), new ArrayList<>());
        Document d = pipeline.createDocumentFromTemplate(docTmp);
        return parseDocumentProcessing(d, noMinWordPerDoc, wordsToIgnore);
    }

    /**
     * @param content
     * @return
     */
    protected AbstractDocumentTemplate getDocumentModel(String content) {
        AbstractDocumentTemplate docTmp = new AbstractDocumentTemplate();
        AbstractDocumentTemplate.BlockTemplate block = docTmp.new BlockTemplate();
        block.setId(0);
        block.setContent(content.trim().toLowerCase());
        docTmp.getBlocks().add(block);
        return docTmp;
    }

    private void printNewConcepts() {
        // write new concepts
        for (Entry<String, Integer> entry : newConcepts.entrySet()) {
            if (entry.getValue() >= MIN_NO_OCCURRENCES) {
                System.out.println(entry.getKey() + "\t" + entry.getValue());
            }
        }
    }

    public void parseGeneralCorpus(String path, String output, Lang lang, boolean usePOSTagging, int noMinWordPerDoc, ListOfWords wordsToIgnore)
            throws FileNotFoundException, IOException {
        // determine number of documents

        if (!new File(path).isDirectory()) {
            return;
        }
        File[] filesToProcess = new File(path).listFiles((File pathname) -> pathname.getName().endsWith(".txt"));

        int total_docs_to_process = 0;
        int current_doc_to_process = 0;

        for (File f : filesToProcess) {
            FileInputStream inputFile = new FileInputStream(f);
            InputStreamReader ir = new InputStreamReader(inputFile, "UTF-8");
            try (BufferedReader in = new BufferedReader(ir)) {
                String line;
                while ((line = in.readLine()) != null) {
                    if (line.length() > LSA.LOWER_BOUND) {
                        total_docs_to_process++;
                    }
                }
            }
        }
        LOGGER.info("Processing {} documents.", total_docs_to_process);

        // read corpus
        try (BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(path).getParent() + "/" + output), "UTF-8"), 32768)) {
            for (File f : filesToProcess) {
                LOGGER.info("Processing file: {}", f.getName());
                try (BufferedReader in = new BufferedReader(new FileReader(f))) {
                    String line, toWrite;
                    while ((line = in.readLine()) != null) {
                        if (line.length() > LSA.LOWER_BOUND) {
                            // toWrite = processContent(
                            // TextPreprocessing.replaceFrCorpusAdnotations(StringEscapeUtils.escapeXml(line)),
                            // lang, usePOSTagging, noMinWordPerDoc);
                            toWrite = processContent(line, lang, usePOSTagging, noMinWordPerDoc, wordsToIgnore);

                            if (toWrite.length() > 0) {
                                out.write(toWrite + "\n");
                            }
                            if ((++current_doc_to_process) % 1000 == 0) {
                                LOGGER.info("Finished processing {} block documents of {}", new Object[]{current_doc_to_process, total_docs_to_process});
                            }
                        }
                    }
                }
                LOGGER.info("Finished pre-processing {}", f.getName());
            }
            printNewConcepts();
        }
        LOGGER.info("Finished all pre-processing");
    }

    public void parseTasa(String path, String output, Lang lang, boolean usePOSTagging, int noMinWordPerDoc, ListOfWords wordsToIgnore)
            throws FileNotFoundException, IOException {
        // determine number of documents

        if (!new File(path).isDirectory()) {
            return;
        }
        File[] filesToProcess = new File(path).listFiles((File pathname) -> pathname.getName().endsWith(".txt"));

        int total_docs_to_process = 0;
        int current_doc_to_process = 0;

        for (File f : filesToProcess) {
            // determine number of documents
            FileInputStream inputFile = new FileInputStream(f);
            InputStreamReader ir = new InputStreamReader(inputFile, "UTF-8");
            try (BufferedReader in = new BufferedReader(ir)) {
                String line;
                while ((line = in.readLine()) != null) {
                    if (line.startsWith("<ID") || line.startsWith("<text")) {
                        total_docs_to_process++;
                    }
                }
            }
        }
        LOGGER.info("Processing {} documents.", total_docs_to_process);
        try (BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(path).getParent() + "/" + output), "UTF-8"), 32768)) {
            for (File f : filesToProcess) {
                try (BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(f), "UTF-8"))) {
                    String line, toWrite, content = "";
                    while ((line = in.readLine()) != null) {
                        if (line.length() == 0 || line.startsWith("<")) {
                            if (content.length() > LSA.LOWER_BOUND) {
                                toWrite = processContent(content, lang, usePOSTagging, noMinWordPerDoc, wordsToIgnore);
                                if (toWrite.length() > 0) {
                                    out.write(toWrite + "\n");
                                }
                                // flush content
                                content = "";
                                if ((++current_doc_to_process) % 1000 == 0) {
                                    LOGGER.info("Finished processing {} block documents of {}", new Object[]{current_doc_to_process, total_docs_to_process});
                                }
                            }
                        } else // process content
                        {
                            if (line.length() > 0) {
                                if (line.startsWith("  ")) {
                                    content += "\n" + line.trim();
                                } else {
                                    content += " " + line.trim();
                                }
                            }
                        }
                    }
                    if (content.length() > LSA.LOWER_BOUND) {
                        LOGGER.info("Processing last block document {} of {}", new Object[]{++current_doc_to_process, total_docs_to_process});
                        toWrite = processContent(content, lang, usePOSTagging, noMinWordPerDoc, wordsToIgnore);
                        if (toWrite.length() > 0) {
                            out.write(toWrite);
                        }
                    }
                    printNewConcepts();
                }
            }
        }
        LOGGER.info("Finished all pre-processing");
    }

    public void parseCOCA(String path, String output, Lang lang, boolean usePOSTagging, int noMinWordPerDoc, ListOfWords wordsToIgnore)
            throws FileNotFoundException, IOException {
        // determine number of documents

        if (!new File(path).isDirectory()) {
            return;
        }
        File[] filesToProcess = new File(path).listFiles((File pathname) -> pathname.getName().endsWith(".txt"));

        int total_docs_to_process = 0;
        int current_doc_to_process = 0;

        for (File f : filesToProcess) {
            // determine number of documents
            FileInputStream inputFile = new FileInputStream(f);
            InputStreamReader ir = new InputStreamReader(inputFile, "UTF-8");
            try (BufferedReader in = new BufferedReader(ir)) {
                String line;
                while ((line = in.readLine()) != null) {
                    if (line.startsWith("##")) {
                        total_docs_to_process++;
                    }
                }
            }
        }
        LOGGER.info("Processing {} documents.", total_docs_to_process);

        try (BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(path).getParent() + "/" + output), "UTF-8"), 32768)) {
            for (File f : filesToProcess) {
                try (BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(f), "UTF-8"))) {
                    String line, toWrite;

                    while ((line = in.readLine()) != null) {
                        if (line.length() > LSA.LOWER_BOUND) {
                            line = line.replaceAll("##[0-9]* ]", "");
                            line = line.replaceAll("<p>", "\n");
                            line = line.replaceAll(" // ", "\n");
                            line = line.replaceAll(" # ", "\n");
                            line = line.replaceAll("@ @ @ @ @ @ @ @ @ @", " ");

                            toWrite = processContent(line, lang, usePOSTagging, noMinWordPerDoc, wordsToIgnore);

                            if (toWrite.length() > 0) {
                                out.write(toWrite + "\n");
                            }
                            if ((++current_doc_to_process) % 1000 == 0) {
                                LOGGER.info("Finished processing {} block documents of {}", new Object[]{current_doc_to_process, total_docs_to_process});
                            }
                        }
                    }
                    printNewConcepts();
                }
            }
        }
        LOGGER.info("Finished all pre-processing");
    }
}
