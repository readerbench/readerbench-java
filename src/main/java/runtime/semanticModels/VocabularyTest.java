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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import data.AbstractDocument;
import data.AbstractDocumentTemplate;
import data.document.Document;
import data.Lang;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.openide.util.Exceptions;
import services.ageOfExposure.TopicMatchGraph;
import services.commons.Formatting;
import services.semanticModels.ISemanticModel;
import services.semanticModels.word2vec.Word2VecModel;
import webService.ReaderBenchServer;

public class VocabularyTest {

    static Logger logger = Logger.getLogger(VocabularyTest.class);

    int questionId;

    public void process(String path, ISemanticModel semModel) {
        logger.info("Starting vocabulary tests processing...");

        List<ISemanticModel> models = new ArrayList<>();
        models.add(semModel);

        try {
            if (!new File(path).isDirectory()) {
                return;
            }
            for (File f : new File(path).listFiles()) {
                if (f.getName().endsWith(".txt")) {
                    questionId = 1;
                    StringBuilder sb = new StringBuilder();
                    sb.append("sep=,\nid,a,sim,b,sim,c,sim\n");

                    logger.info("Processing file: " + f.getName());

                    String line;
                    try (BufferedReader br = new BufferedReader(new FileReader(f))) {
                        AbstractDocument rhs[] = new AbstractDocument[6];
                        AbstractDocument lhs[] = new AbstractDocument[3];

                        while ((line = br.readLine()) != null) {
                            if (line.startsWith(">>")) {
                                sb.append(line).append("\n");
                            } else if (line.startsWith("----")) {
                                int id = Integer.valueOf(line.replaceAll("-", ""));
                                if (questionId != id) {
                                    logger.error("Manual indexing corrupted at question " + questionId + "/" + id);
                                }
                            } else if (line.startsWith("1.")) {
                                rhs[0] = processDoc(line.substring(2), models, semModel.getLanguage());
                            } else if (line.startsWith("2.")) {
                                rhs[1] = processDoc(line.substring(2), models, semModel.getLanguage());
                            } else if (line.startsWith("3.")) {
                                rhs[2] = processDoc(line.substring(2), models, semModel.getLanguage());
                            } else if (line.startsWith("4.")) {
                                rhs[3] = processDoc(line.substring(2), models, semModel.getLanguage());
                            } else if (line.startsWith("5.")) {
                                rhs[4] = processDoc(line.substring(2), models, semModel.getLanguage());
                            } else if (line.startsWith("6.")) {
                                rhs[5] = processDoc(line.substring(2), models, semModel.getLanguage());
                            } else if (line.startsWith("a.")) {
                                lhs[0] = processDoc(line.substring(2), models, semModel.getLanguage());
                            } else if (line.startsWith("b.")) {
                                lhs[1] = processDoc(line.substring(2), models, semModel.getLanguage());
                            } else if (line.startsWith("c.")) {
                                lhs[2] = processDoc(line.substring(2), models, semModel.getLanguage());

                                // process current question
                                TopicMatchGraph graph = new TopicMatchGraph(9);
                                for (int i = 0; i < 3; i++) {
                                    for (int j = 0; j < 6; j++) {
                                        double sim = semModel.getSimilarity(lhs[i], rhs[j]);
                                        graph.addEdge(i, j + lhs.length, 1 - sim);
                                    }
                                }

                                Integer[] assoc = graph.computeAssociations(6);

                                sb.append(questionId).append(",");
                                questionId += 1;
                                for (int i = 0; i < assoc.length; i++) {
                                    sb.append(assoc[i] - lhs.length + 1).append(",").append(Formatting.formatNumber(1 - graph.getEdge(i, assoc[i]))).append(",");
                                }

                                sb.append("\n");
                            }
                        }
                    }
                    logger.info("Finished processing file: " + f);

                    File file = new File(f.getPath().replace(".txt",
                            "_" + semModel.getPath().replace("resources/config/", "").replaceAll("/", "_") + ".csv"));
                    try {
                        FileUtils.writeStringToFile(file, sb.toString());
                    } catch (Exception e) {
                        Exceptions.printStackTrace(e);
                    }
                    logger.info("Printed information to: " + file.getAbsolutePath());
                }
            }
        } catch (IOException | NumberFormatException e) {
            Exceptions.printStackTrace(e);
        }
    }

    public static Document processDoc(String line, List<ISemanticModel> models, Lang lang) {
        AbstractDocumentTemplate contents = AbstractDocumentTemplate.getDocumentModel(line.trim());
        Document doc = new Document(null, contents, models, lang, true);
        return doc;
    }

    public static void main(String[] args) {
        ReaderBenchServer.initializeDB();

        VocabularyTest test = new VocabularyTest();

//        ISemanticModel lsa1 = LSA.loadLSA("resources/config/EN/LSA/tasa_en", Lang.eng);
//        test.process("resources/in/vocabulary_test/", lsa1);
//        lsa1 = LSA.loadLSA("resources/config/EN/LSA/COCA newspaper", Lang.eng);
//        test.process("resources/in/vocabulary_test/", lsa1);
//        ISemanticModel lda1 = LDA.loadLDA("resources/config/EN/LDA/TASA", Lang.en);
//        test.process("resources/in/vocabulary_test/", lda1);
//        lda1 = LDA.loadLDA("resources/config/EN/LDA/COCA newspaper", Lang.en);
//        test.process("resources/in/vocabulary_test/", lda1);
        Word2VecModel w2v1 = Word2VecModel.loadWord2Vec("resources/config/EN/word2vec/TASA_epoch3", Lang.en);
        test.process("resources/in/vocabulary_test/", w2v1);
        w2v1 = Word2VecModel.loadWord2Vec("resources/config/EN/word2vec/TASA_epoch3_iter3", Lang.en);
        test.process("resources/in/vocabulary_test/", w2v1);
        w2v1 = Word2VecModel.loadWord2Vec("resources/config/EN/word2vec/TASA_iter5", Lang.en);
        test.process("resources/in/vocabulary_test/", w2v1);
        w2v1 = Word2VecModel.loadGoogleNewsModel();
        test.process("resources/in/vocabulary_test/", w2v1);
    }
}
