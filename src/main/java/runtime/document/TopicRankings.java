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
package runtime.document;

import data.AbstractDocument;
import data.Lang;
import data.Word;
import data.discourse.Keyword;
import data.document.Document;
import data.document.MetaDocument;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.openide.util.Exceptions;
import services.commons.Formatting;
import services.discourse.keywordMining.KeywordModeling;
import services.semanticModels.LDA.LDA;
import services.semanticModels.LSA.LSA;
import webService.ReaderBenchServer;

/**
 *
 * @author Mihai Dascalu
 */
public class TopicRankings {

    static final Logger LOGGER = Logger.getLogger(TopicRankings.class);

    private final String processingPath;
    private final int noTopKeyWords;
    private final LSA lsa;
    private final LDA lda;
    private final Lang lang;
    private final boolean usePOSTagging;
    private final boolean computeDialogism;
    private final boolean meta;

    public TopicRankings(String processingPath, int noTopKeyWords, LSA lsa, LDA lda, Lang lang, boolean usePOSTagging, boolean computeDialogism, boolean meta) {
        this.processingPath = processingPath;
        this.noTopKeyWords = noTopKeyWords;
        this.lsa = lsa;
        this.lda = lda;
        this.lang = lang;
        this.usePOSTagging = usePOSTagging;
        this.computeDialogism = computeDialogism;
        this.meta = meta;
    }

    public Set<Word> getTopKeywords(List<Document> documents, int noTopKeyWords) {
        Set<Word> words = new TreeSet<>();

        for (Document d : documents) {
            List<Keyword> topics = KeywordModeling.getSublist(d.getTopics(), noTopKeyWords, false, false);
            for (Keyword t : topics) {
                words.add(t.getWord());
            }
        }
        return words;
    }

    public Map<Word, Double> getRelevance(Document d, Set<Word> keywords) {
        Map<Word, Double> keywordOccurrences = new TreeMap<>();

        List<Keyword> topics = d.getTopics();
        for (int i = 0; i < topics.size(); i++) {
            for (Word keyword : keywords) {
                //determine identical stem
                if (keyword.getLemma().equals(topics.get(i).getWord().getLemma())) {
                    keywordOccurrences.put(topics.get(i).getWord(), topics.get(i).getRelevance());
                }
            }
        }
        return keywordOccurrences;
    }

    public Map<Word, Integer> getIndex(Document d, Set<Word> keywords) {
        Map<Word, Integer> keywordOccurrences = new TreeMap<>();

        List<Keyword> topics = d.getTopics();
        for (int i = 0; i < topics.size(); i++) {
            for (Word keyword : keywords) {
                //determine identical stem
                if (keyword.getLemma().equals(topics.get(i).getWord().getLemma())) {
                    keywordOccurrences.put(topics.get(i).getWord(), i);
                }
            }
        }
        return keywordOccurrences;
    }

    public void processTexts(boolean useSerialized) {
        File dir = new File(processingPath);

        if (!dir.exists()) {
            throw new RuntimeException("Inexistent Folder: " + dir.getPath());
        }

        List<Document> documents = new ArrayList<>();

        if (useSerialized) {
            File[] files = dir.listFiles((File pathname) -> {
                return pathname.getName().toLowerCase().endsWith(".ser");
            });

            for (File file : files) {
                Document d = (Document) AbstractDocument.loadSerializedDocument(file.getPath());
                documents.add(d);
                d.exportDocument();
            }
        } else {
            File[] files = dir.listFiles((File pathname) -> {
                return pathname.getName().toLowerCase().endsWith(".xml");
            });

            for (File file : files) {
                LOGGER.info("Processing " + file.getName() + " file");
                // Create file

                Document d;
                try {
                    if (meta) {
                        d = MetaDocument.load(file, lsa, lda, lang, usePOSTagging, MetaDocument.DocumentLevel.Subsection, 5);
                    } else {
                        d = Document.load(file, lsa, lda, lang, usePOSTagging);
                    }
                    d.computeAll(computeDialogism);
                    d.save(AbstractDocument.SaveType.SERIALIZED_AND_CSV_EXPORT);
                    documents.add(d);
                } catch (Exception e) {
                    LOGGER.error("Runtime error while processing " + file.getName() + ": " + e.getMessage() + " ...");
                    Exceptions.printStackTrace(e);
                }
            }
        }

        //determing joint keywords
        Set<Word> keywords = getTopKeywords(documents, noTopKeyWords);
        Map<Document, Map<Word, Double>> docRelevance = new TreeMap<>();
        Map<Document, Map<Word, Integer>> docIndex = new TreeMap<>();
        for (Document d : documents) {
            docRelevance.put(d, getRelevance(d, keywords));
            docIndex.put(d, getIndex(d, keywords));
        }

        try (BufferedWriter outRelevance = new BufferedWriter(new FileWriter(processingPath + "/relevance.csv", true));
                BufferedWriter outIndex = new BufferedWriter(new FileWriter(processingPath + "/index.csv", true));) {
            StringBuilder header = new StringBuilder("Filename");
            documents.stream().forEach((d) -> {
                header.append(",").append(FilenameUtils.removeExtension(Paths.get(d.getPath()).getFileName().toString()));
            });
            outRelevance.write(header.toString());
            outIndex.write(header.toString());

            for (Word w : keywords) {
                StringBuilder lineRelevance = new StringBuilder("\n" + w.getExtendedLemma());
                StringBuilder lineIndex = new StringBuilder("\n" + w.getExtendedLemma());

                for (Document d : documents) {
                    lineRelevance.append(",");
                    lineIndex.append(",");
                    if (docRelevance.get(d).containsKey(w)) {
                        lineRelevance.append(Formatting.formatNumber(docRelevance.get(d).get(w)));
                        lineIndex.append(docIndex.get(d).get(w));
                    }
                }
                outRelevance.write(lineRelevance.toString());
                outIndex.write(lineIndex.toString());
            }

        } catch (IOException ex) {
            LOGGER.error("Runtime error while analyzing selected folder ...");
            Exceptions.printStackTrace(ex);
        }
    }

    public static void main(String[] args) {
        BasicConfigurator.configure();
        ReaderBenchServer.initializeDB();

        LSA lsa = LSA.loadLSA("resources/config/FR/LSA/Le_Monde", Lang.fr);
        LDA lda = LDA.loadLDA("resources/config/FR/LDA/Le_Monde", Lang.fr);
        TopicRankings tr = new TopicRankings("resources/in/Philippe/Linard_Travaux/Textes longs", 30, lsa, lda, Lang.fr, true, true, false);
        tr.processTexts(true);
    }
}
