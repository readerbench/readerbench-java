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
package services.semanticModels;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.StringTokenizer;
import java.util.TreeMap;



import data.AbstractDocument;
import data.AbstractDocumentTemplate;
import data.AbstractDocumentTemplate.BlockTemplate;
import data.Block;
import data.Lang;
import data.Sentence;
import data.Word;
import data.document.Document;
import java.util.logging.Logger;
import services.commons.TextPreprocessing;
import services.nlp.listOfWords.Dictionary;
import services.semanticModels.LSA.LSA;

public class PreProcessing {

    static Logger logger = Logger.getLogger("");

    public static final int MIN_NO_OCCURRENCES = 5;

    private final Map<String, Integer> newConcepts = new TreeMap<>();

    private String parseDocumentProcessing(AbstractDocument d, int noMinWordPerDoc) {
        // returns new entries to write
        StringBuilder toWrite = new StringBuilder();
        List<Word> document = new ArrayList<>();
        int no_words_to_write = 0;
        for (Block b : d.getBlocks()) {
            // combine with previous paragraph, if the case
            for (Sentence s : b.getSentences()) {
                for (Word w : s.getWords()) {
                    no_words_to_write++;
                    document.add(w);
                }
                document.add(new Word(".", ".", ".", null, null, d.getLanguage()));
            }
            if (no_words_to_write >= noMinWordPerDoc) {
                // flush the actual contents of the document
                for (Word w : document) {
                    toWrite.append(w.getLemma()).append(" ");
                }
                toWrite.append("\n");
                document.clear();
                no_words_to_write = 0;
            }
        }
        return toWrite.toString();
    }

    public String processContent(String content, Lang lang, boolean usePOSTagging, int noMinWordPerDoc) {
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
        AbstractDocument d = new Document(null, docTmp, new ArrayList<>(), lang, usePOSTagging);
        return parseDocumentProcessing(d, noMinWordPerDoc);
    }

    /**
     * @param content
     * @return
     */
    protected AbstractDocumentTemplate getDocumentModel(String content) {
        AbstractDocumentTemplate docTmp = new AbstractDocumentTemplate();
        BlockTemplate block = docTmp.new BlockTemplate();
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

    public void parseGeneralCorpus(String path, String output, Lang lang, boolean usePOSTagging, int noMinWordPerDoc)
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
            BufferedReader in = new BufferedReader(ir);
            String line = "";

            while ((line = in.readLine()) != null) {
                if (line.length() > LSA.LOWER_BOUND) {
                    total_docs_to_process++;
                }
            }
            in.close();
        }
        logger.info("Processing " + total_docs_to_process + " documents.");

        // read corpus
        BufferedWriter out = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(new File(path).getParent() + "/" + output), "UTF-8"),
                32768);
        for (File f : filesToProcess) {
            logger.info("Processing file: " + f.getName());
            BufferedReader in = new BufferedReader(new FileReader(f));

            String line = "";
            String toWrite = "";

            while ((line = in.readLine()) != null) {
                if (line.length() > LSA.LOWER_BOUND) {
                    // toWrite = processContent(
                    // TextPreprocessing.replaceFrCorpusAdnotations(StringEscapeUtils.escapeXml(line)),
                    // lang, usePOSTagging, noMinWordPerDoc);
                    toWrite = processContent(line, lang, usePOSTagging, noMinWordPerDoc);

                    if (toWrite.length() > 0) {
                        out.write(toWrite + "\n");
                    }
                    if ((++current_doc_to_process) % 1000 == 0) {
                        logger.info("Finished processing " + (current_doc_to_process) + " block documents of "
                                + total_docs_to_process);
                    }
                }
            }
            in.close();
            logger.info("Finished pre-processing " + f.getName());
        }
        printNewConcepts();

        out.close();
        logger.info("Finished all pre-processing");
    }

    public void parseTasa(String path, String output, Lang lang, boolean usePOSTagging, int noMinWordPerDoc)
            throws FileNotFoundException, IOException {
        // determine number of documents

        if (!new File(path).isDirectory()) {
            return;
        }
        File[] filesToProcess = new File(path).listFiles(new FileFilter() {
            public boolean accept(File pathname) {
                return pathname.getName().endsWith(".txt");
            }
        });

        int total_docs_to_process = 0;
        int current_doc_to_process = 0;

        for (File f : filesToProcess) {
            // determine number of documents
            FileInputStream inputFile = new FileInputStream(f);
            InputStreamReader ir = new InputStreamReader(inputFile, "UTF-8");
            BufferedReader in = new BufferedReader(ir);
            String line = "";

            while ((line = in.readLine()) != null) {
                if (line.startsWith("<ID") || line.startsWith("<text")) {
                    total_docs_to_process++;
                }
            }
            in.close();
        }
        logger.info("Processing " + total_docs_to_process + " documents.");

        // read corpus
        BufferedWriter out = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(new File(path).getParent() + "/" + output), "UTF-8"),
                32768);

        for (File f : filesToProcess) {
            // read corpus
            BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(f), "UTF-8"));

            String line = "";
            String toWrite = "";
            String content = "";

            while ((line = in.readLine()) != null) {
                if (line.length() == 0 || line.startsWith("<")) {
                    if (content.length() > LSA.LOWER_BOUND) {
                        toWrite = processContent(content, lang, usePOSTagging, noMinWordPerDoc);
                        if (toWrite.length() > 0) {
                            out.write(toWrite + "\n");
                        }
                        // flush content
                        content = "";
                        if ((++current_doc_to_process) % 1000 == 0) {
                            logger.info("Finished processing " + (current_doc_to_process) + " block documents of "
                                    + total_docs_to_process);
                        }
                    }
                } else // process content
                 if (line.length() > 0) {
                        if (line.startsWith("  ")) {
                            content += "\n" + line.trim();
                        } else {
                            content += " " + line.trim();
                        }
                    }
            }
            if (content.length() > LSA.LOWER_BOUND) {
                logger.info("Processing last block document " + (++current_doc_to_process) + " of "
                        + total_docs_to_process);
                toWrite = processContent(content, lang, usePOSTagging, noMinWordPerDoc);
                if (toWrite.length() > 0) {
                    out.write(toWrite);
                }
            }
            printNewConcepts();
            in.close();
        }
        out.close();
        logger.info("Finished all pre-processing");
    }

    public void parseCOCA(String path, String output, Lang lang, boolean usePOSTagging, int noMinWordPerDoc)
            throws FileNotFoundException, IOException {
        // determine number of documents

        if (!new File(path).isDirectory()) {
            return;
        }
        File[] filesToProcess = new File(path).listFiles(new FileFilter() {
            public boolean accept(File pathname) {
                return pathname.getName().endsWith(".txt");
            }
        });

        int total_docs_to_process = 0;
        int current_doc_to_process = 0;

        for (File f : filesToProcess) {
            // determine number of documents
            FileInputStream inputFile = new FileInputStream(f);
            InputStreamReader ir = new InputStreamReader(inputFile, "UTF-8");
            BufferedReader in = new BufferedReader(ir);
            String line = "";

            while ((line = in.readLine()) != null) {
                if (line.startsWith("##")) {
                    total_docs_to_process++;
                }
            }
            in.close();
        }
        logger.info("Processing " + total_docs_to_process + " documents.");

        // read corpus
        BufferedWriter out = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(new File(path).getParent() + "/" + output), "UTF-8"),
                32768);

        for (File f : filesToProcess) {
            // read corpus
            BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(f), "UTF-8"));

            String line = "";
            String toWrite = "";

            while ((line = in.readLine()) != null) {
                if (line.length() > LSA.LOWER_BOUND) {
                    line = line.replaceAll("##[0-9]* ]", "");
                    line = line.replaceAll("<p>", "\n");
                    line = line.replaceAll(" // ", "\n");
                    line = line.replaceAll(" # ", "\n");
                    line = line.replaceAll("@ @ @ @ @ @ @ @ @ @", " ");

                    toWrite = processContent(line, lang, usePOSTagging, noMinWordPerDoc);

                    if (toWrite.length() > 0) {
                        out.write(toWrite + "\n");
                    }
                    if ((++current_doc_to_process) % 1000 == 0) {
                        logger.info("Finished processing " + (current_doc_to_process) + " block documents of "
                                + total_docs_to_process);
                    }
                }
            }
            printNewConcepts();
            in.close();
        }
        out.close();
        logger.info("Finished all pre-processing");
    }
}
