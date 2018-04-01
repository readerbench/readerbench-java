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
package runtime.semanticModels;

import data.Lang;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.StringTokenizer;
import java.util.TreeMap;




import data.Word;
import data.discourse.Keyword;
import data.document.Document;
import java.util.ArrayList;
import java.util.logging.Logger;
import org.openide.util.Exceptions;
import services.semanticModels.ISemanticModel;
import services.semanticModels.LDA.LDA;
import services.semanticModels.LSA.LSA;
import services.semanticModels.word2vec.Word2VecModel;
import webService.ReaderBenchServer;

public class WordAssociationTest {

    static Logger logger = Logger.getLogger("");
    private Map<Document, Map<Document, Double>> wordAssociations;
    private Map<Document, Double> simTop;
    private Map<Document, Double> simMax;
    private Map<Document, Document> simMaxConcept;

    public void initialLoad(String pathToFile, ISemanticModel semModel, int countMax) {
        wordAssociations = new TreeMap<>();
        List<ISemanticModel> models = new ArrayList<>();
        models.add(semModel);
        try {
            FileInputStream inputFile = new FileInputStream(pathToFile);
            InputStreamReader ir = new InputStreamReader(inputFile, "UTF-8");
            try (BufferedReader in = new BufferedReader(ir)) {
                String line;
                logger.info("Parsing word associations file...");
                while ((line = in.readLine()) != null) {
                    if (line.length() > 0) {
                        StringTokenizer st = new StringTokenizer(line, ",");
                        try {
                            Document doc1 = VocabularyTest.processDoc(st.nextToken(), models, semModel.getLanguage());
                            Document doc2 = VocabularyTest.processDoc(st.nextToken(), models, semModel.getLanguage());

                            Double no = Double.valueOf(st.nextToken());
                            if (!wordAssociations.containsKey(doc1)) {
                                wordAssociations.put(doc1, new TreeMap<>());
                            }
                            if (countMax != -1) {
                                if (wordAssociations.get(doc1).size() < countMax) {
                                    wordAssociations.get(doc1).put(doc2, no);
                                }
                            }
                        } catch (Exception e) {
                        }
                    }
                }
            }
        } catch (IOException e) {
            Exceptions.printStackTrace(e);
        }
    }

    private void compareIndividual(ISemanticModel semModel) {
        try {
            logger.info("Comparing all word pairs...");
            try (BufferedWriter out = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(semModel.getPath() + "/compare_individual.csv"), "UTF-8"))) {
                simTop = new TreeMap<>();
                simMax = new TreeMap<>();
                simMaxConcept = new TreeMap<>();
                int no = 0;

                out.write("Word1,Word2,Similarity\n");
                for (Document doc1 : wordAssociations.keySet()) {
                    int sumWeights = 0;
                    double sumSimilarities = 0;
                    double max = 0;
                    Document maxSim = null;
                    for (Document doc2 : wordAssociations.get(doc1).keySet()) {

                        double sim = semModel.getSimilarity(doc1, doc2);
                        // sumWeights += wordAssociations.get(word1).get(word2);
                        // sumSimilarities += wordAssociations.get(word1).get(word2)
                        // * sim;
                        sumWeights++;
                        sumSimilarities += sim;
                        out.write(doc1.getText().trim() + "," + doc2.getText().trim() + "," + sim + "\n");
                        if (sim > max) {
                            max = sim;
                            maxSim = doc2;
                        }
                    }
                    if ((++no) % 1000 == 0) {
                        logger.info("Finished comparing " + no + " words...");
                    }
                    if (sumWeights != 0 && maxSim != null) {
                        simTop.put(doc1, sumSimilarities / sumWeights);
                        simMax.put(doc1, max);
                        simMaxConcept.put(doc1, maxSim);
                    } else {
                        simTop.put(doc1, new Double(0));
                        simMax.put(doc1, new Double(0));
                    }
                }
            }
        } catch (IOException e) {
            Exceptions.printStackTrace(e);
        }
    }

    public void compare(String pathToFile, ISemanticModel semModel, int countMax, boolean printSimilarConcepts, int noConcepts, double minThreshold) {
        initialLoad(pathToFile, semModel, countMax);

        compareIndividual(semModel);

        logger.info("Performing comparisons of word associations...");

        try {
            try (BufferedWriter out = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(semModel.getPath() + "/compare_aggregated.csv"), "UTF-8"))) {
                out.write("Concept,Average Similarity,Max Similarity,Most similar word association\n");
                for (Document doc : wordAssociations.keySet()) {
                    out.write(doc.getText().trim() + "," + simTop.get(doc) + "," + simMax.get(doc)
                            + (simMaxConcept.containsKey(doc) ? (",(" + simMaxConcept.get(doc).getText().trim() + ")")
                            : ","));
                    if (printSimilarConcepts) {
                        // determine most similar concepts;
                        List<Keyword> similarConcepts = new ArrayList<>();
                        TreeMap<Word, Double> listLSA = semModel.getSimilarConcepts(doc, minThreshold);
                        for (Entry<Word, Double> entry : listLSA.entrySet()) {
                            for (Word word : doc.getWordOccurences().keySet()) {
                                if (!entry.getKey().getLemma().equals(word.getLemma())
                                        && !entry.getKey().getStem().equals(word.getStem())) {
                                    similarConcepts.add(new Keyword(entry.getKey(), entry.getValue()));
                                }
                            }
                        }
                        Collections.sort(similarConcepts);
                        // output top 5 concepts
                        for (int i = 0; i < Math.min(noConcepts, similarConcepts.size()); i++) {
                            out.write("," + similarConcepts.get(i).getWord().getLemma() + ","
                                    + similarConcepts.get(i).getRelevance());
                        }
                    }
                    out.write("\n");
                }
            }

            logger.info("Finished all comparisons for word associations...");
        } catch (IOException e) {
            Exceptions.printStackTrace(e);
        }
    }

    public Map<Document, Map<Document, Double>> getWordAssociations() {
        return wordAssociations;
    }

    public static void main(String[] args) {
        

        ReaderBenchServer.initializeDB();

        WordAssociationTest comp = new WordAssociationTest();

//        LSA lsa1 = LSA.loadLSA("resources/config/EN/LSA/TASA", Lang.en);
//        comp.compare("resources/config/EN/word lists/Nelson norms_en.csv", lsa1, 3, true, 20, 0.3);
//        lsa1 = LSA.loadLSA("resources/config/ES/LSA/Jose Antonio", Lang.es);
//        comp.compare("resources/config/ES/word lists/Normas Palabras C4819_es.csv", lsa1, 3, true, 20, 0.3);
//        LDA lda1 = LDA.loadLDA("resources/config/ES/LDA/Jose Antonio", Lang.es);
//        comp.compare("resources/config/EN/word lists/Nelson norms_en.csv", lda1, 3, false, 20, 0.3);
//        lda1 = LDA.loadLDA("resources/config/EN/LDA/TASA", Lang.en);
//        comp.compare("resources/config/ES/word lists/Normas Palabras C4819_es.csv", lda1, 3, true, 20, 0.3);
        Word2VecModel w2v1 = Word2VecModel.loadWord2Vec("resources/in/AoE w2v/grade2", Lang.en);
        comp.compare("resources/config/EN/word lists/Nelson norms_en.csv", w2v1, 3, true, 20, 0.3);
//        w2v1 = Word2VecModel.loadWord2Vec("resources/config/EN/word2vec/TASA_epoch3_iter3", Lang.en);
//        comp.compare("resources/config/EN/word lists/Nelson norms_en.csv", w2v1, 3, true, 20, 0.3);
//        w2v1 = Word2VecModel.loadWord2Vec("resources/config/EN/word2vec/TASA_iter5", Lang.en);
//        comp.compare("resources/config/EN/word lists/Nelson norms_en.csv", w2v1, 3, true, 20, 0.3);
//        w2v1 = Word2VecModel.loadGoogleNewsModel();
//        comp.compare("resources/config/EN/word lists/Nelson norms_en.csv", w2v1, 3, true, 20, 0.3);
    }
}
