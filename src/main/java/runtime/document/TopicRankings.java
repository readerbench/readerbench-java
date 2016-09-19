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

import data.Lang;
import data.Word;
import data.discourse.Topic;
import data.document.Document;
import data.document.MetaDocument;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
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
import services.discourse.topicMining.TopicModeling;
import services.semanticModels.LDA.LDA;
import services.semanticModels.LSA.LSA;
import webService.ReaderBenchServer;

/**
 *
 * @author Mihai Dascalu
 */
public class TopicRankings {

    static final Logger LOGGER = Logger.getLogger(TopicRankings.class);
    public static final int NO_TOP_KEYWORDS = 30;

    private final String processingPath;
    private final LSA lsa;
    private final LDA lda;
    private final Lang lang;
    private final boolean usePOSTagging;
    private final boolean computeDialogism;
    private final boolean meta;

    public TopicRankings(String processingPath, LSA lsa, LDA lda, Lang lang, boolean usePOSTagging, boolean computeDialogism, boolean meta) {
        this.processingPath = processingPath;
        this.lsa = lsa;
        this.lda = lda;
        this.lang = lang;
        this.usePOSTagging = usePOSTagging;
        this.computeDialogism = computeDialogism;
        this.meta = meta;
    }

    public Set<Word> getCommonKeywords(List<Document> documents) {
        Set<Word> words = new TreeSet<>();

        for (Document d : documents) {
            List<Topic> topics = TopicModeling.getSublist(d.getTopics(), NO_TOP_KEYWORDS, false, false);
            for (Topic t : topics) {
                words.add(t.getWord());
            }
        }
        return words;
    }

    public Map<Word, Double> getRelevance(Document d, Set<Word> keywords) {
        Map<Word, Double> keywordOccurrences = new TreeMap<>();

        List<Topic> topics = d.getTopics();
        for (int i = 0; i < topics.size(); i++) {
            if (keywords.contains(topics.get(i).getWord())) {
                keywordOccurrences.put(topics.get(i).getWord(), topics.get(i).getRelevance());
            }
        }
        return keywordOccurrences;
    }

    public Map<Word, Integer> getIndex(Document d, Set<Word> keywords) {
        Map<Word, Integer> keywordOccurrences = new TreeMap<>();

        List<Topic> topics = d.getTopics();
        for (int i = 0; i < topics.size(); i++) {
            if (keywords.contains(topics.get(i).getWord())) {
                keywordOccurrences.put(topics.get(i).getWord(), i);
            }
        }
        return keywordOccurrences;
    }

    public void processTexts() {
        File dir = new File(processingPath);

        if (!dir.exists()) {
            throw new RuntimeException("Inexistent Folder: " + dir.getPath());
        }

        File[] files = dir.listFiles((File pathname) -> {
            return pathname.getName().toLowerCase().endsWith(".xml");
        });

        List<Document> documents = new ArrayList<>();
        for (File file : files) {
            LOGGER.info("Processing " + file.getName() + " file");
            // Create file

            Document d;
            try {
                if (meta) {
                    d = MetaDocument.load(file, lsa, lda, lang, usePOSTagging, true, MetaDocument.DocumentLevel.Subsection, 5);
                } else {
                    d = Document.load(file, lsa, lda, lang, usePOSTagging, true);
                }
                d.computeAll(computeDialogism, null, null);
                documents.add(d);
                d.saveSerializedDocument();
            } catch (Exception e) {
                LOGGER.error("Runtime error while processing " + file.getName() + ": " + e.getMessage());
                Exceptions.printStackTrace(e);
            }
        }

        //determing joint keywords
        Set<Word> keywords = getCommonKeywords(documents);

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
                header.append(FilenameUtils.removeExtension(d.getPath()));
            });
            outRelevance.write(header.toString());
            outIndex.write(header.toString());

            for (Word w : keywords) {
                StringBuilder lineRelevance = new StringBuilder("\n" + w.toString());
                StringBuilder lineIndex = new StringBuilder("\n" + w.toString());

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
            LOGGER.error("Runtime error while analyzing selected folder");
            Exceptions.printStackTrace(ex);
        }
    }

    public static void main(String[] args) {
        BasicConfigurator.configure();
        ReaderBenchServer.initializeDB();

        LSA lsa = LSA.loadLSA("resources/config/FR/LSA/Le_Monde", Lang.fr);
        LDA lda = LDA.loadLDA("resources/config/FR/LDA/Le_Monde", Lang.fr);
        TopicRankings tr = new TopicRankings("resources/in/Philippe/Linard_Travaux/Textes longs", lsa, lda, Lang.fr, true, true, false);
        tr.processTexts();
    }
}
