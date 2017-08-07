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
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.FilenameUtils;

import org.openide.util.Exceptions;
import services.commons.Formatting;
import services.converters.Txt2XmlConverter;
import services.discourse.keywordMining.KeywordModeling;
import services.semanticModels.ISemanticModel;
import services.semanticModels.LDA.LDA;
import services.semanticModels.LSA.LSA;
import services.semanticModels.word2vec.Word2VecModel;
import webService.ReaderBenchServer;

/**
 *
 * @author Mihai Dascalu
 */
public class TopicRankings {

    static final Logger LOGGER = Logger.getLogger("");

    private final String processingPath;
    private final int noTopKeyWords;
    private final List<ISemanticModel> models;
    private final Lang lang;
    private final boolean usePOSTagging;
    private final boolean computeDialogism;
    private final boolean useBigrams;
    private final boolean meta;

    public TopicRankings(String processingPath, int noTopKeyWords, List<ISemanticModel> models, Lang lang, boolean usePOSTagging, boolean computeDialogism, boolean useBigrams, boolean meta) {
        this.processingPath = processingPath;
        this.noTopKeyWords = noTopKeyWords;
        this.models = models;
        this.lang = lang;
        this.usePOSTagging = usePOSTagging;
        this.computeDialogism = computeDialogism;
        this.useBigrams = useBigrams;
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
                Document d = null;
                try {
                    d = (Document) AbstractDocument.loadSerializedDocument(file.getPath());
                    documents.add(d);
                    d.exportDocument();
                } catch (IOException | ClassNotFoundException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        } else {
            File[] files = dir.listFiles((File pathname) -> {
                return pathname.getName().toLowerCase().endsWith(".xml");
            });

            for (File file : files) {
                LOGGER.log(Level.INFO, "Processing {0} file", file.getName());
                // Create file

                Document d;
                try {
                    if (meta) {
                        d = MetaDocument.load(file, models, lang, usePOSTagging, MetaDocument.DocumentLevel.Subsection, 5);
                    } else {
                        d = Document.load(file, models, lang, usePOSTagging);
                    }
                    d.computeAll(computeDialogism, useBigrams);
                    d.save(AbstractDocument.SaveType.SERIALIZED_AND_CSV_EXPORT);
                    documents.add(d);
                } catch (Exception e) {
                    LOGGER.log(Level.SEVERE, "Runtime error while processing {0}: {1} ...", new Object[]{file.getName(), e.getMessage()});
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
            StringBuilder header = new StringBuilder("SEP=,\nFilename");
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
            LOGGER.severe("Runtime error while analyzing selected folder ...");
            Exceptions.printStackTrace(ex);
        }
    }

    public static void main(String[] args) {

        ReaderBenchServer.initializeDB();

        LSA lsa = LSA.loadLSA("resources/config/EN/LSA/TASA", Lang.en);
        LDA lda = LDA.loadLDA("resources/config/EN/LDA/TASA", Lang.en);
        List<ISemanticModel> models = new ArrayList<>();
        models.add(lsa);
        models.add(lda);
        Txt2XmlConverter.parseTxtFiles("", "resources/in/ENEA copy/responses per profession", Lang.en, "UTF-8");
        TopicRankings tr = new TopicRankings("resources/in/ENEA copy/responses per profession", 50, models, Lang.en, true, true, false, false);
        tr.processTexts(false);
    }
}
