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


import data.AbstractDocument;
import data.Lang;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import org.openide.util.Exceptions;
import services.semanticModels.ISemanticModel;
import services.semanticModels.word2vec.Word2VecModel;
import webService.ReaderBenchServer;

public class ToeflTest {

    static Logger logger = Logger.getLogger("");

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
                    sb.append("sep=,\nconcept,correct?,sim,most similar concept,sim\n");

                    logger.info("Processing file: " + f.getName());
                    String line = null;
                    BufferedReader br = new BufferedReader(new FileReader(f));
                    AbstractDocument concepts[] = new AbstractDocument[5];

                    outer:
                    while (true) {
                        // read each line
                        for (int i = 0; i < 5; i++) {
                            if ((line = br.readLine()) == null) {
                                break outer;
                            }
                            concepts[i] = VocabularyTest.processDoc(line, models, semModel.getLanguage());
                        }

                        // read blank line
                        br.readLine();

                        double maxSim = Double.MIN_VALUE;
                        int maxIndex = -1;

                        for (int i = 1; i < 5; i++) {
                            double sim = semModel.getSimilarity(concepts[0], concepts[i]);
                            if (sim > maxSim) {
                                maxSim = sim;
                                maxIndex = i;
                            }
                        }

                        if (maxIndex != 1) {
                            sb.append(concepts[0].getText().trim()).append(",0,").append(semModel.getSimilarity(concepts[0], concepts[1])).append(",").append((maxIndex != -1) ? concepts[maxIndex].getText().trim() : "").append(",").append(maxSim).append("\n");
                        } else {
                            sb.append(concepts[0].getText().trim()).append(",1,").append(maxSim).append("\n");
                        }
                    }
                    br.close();
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
        } catch (Exception e) {
            Exceptions.printStackTrace(e);
        }
    }

    public static void main(String[] args) {
        ReaderBenchServer.initializeDB();

        ToeflTest test = new ToeflTest();

//        ISemanticModel lsa1 = LSA.loadLSA("resources/config/EN/LSA/TASA", Lang.en);
//        test.process("resources/in/toefl_test/", lsa1);
//        lsa1 = LSA.loadLSA("resources/config/EN/LSA/COCA newspaper", Lang.en);
//        test.process("resources/in/toefl_test/", lsa1);
//        ISemanticModel lda1 = LDA.loadLDA("resources/config/EN/LDA/TASA", Lang.en);
//        test.process("resources/in/toefl_test/", lda1);
//        lda1 = LDA.loadLDA("resources/config/EN/LDA/COCA newspaper", Lang.en);
//        test.process("resources/in/toefl_test/", lda1);
        Word2VecModel w2v1 = Word2VecModel.loadWord2Vec("resources/config/EN/word2vec/TASA_epoch3", Lang.en);
        test.process("resources/in/toefl_test/", w2v1);
        w2v1 = Word2VecModel.loadWord2Vec("resources/config/EN/word2vec/TASA_epoch3_iter3", Lang.en);
        test.process("resources/in/toefl_test/", w2v1);
        w2v1 = Word2VecModel.loadWord2Vec("resources/config/EN/word2vec/TASA_iter5", Lang.en);
        test.process("resources/in/toefl_test/", w2v1);
//        w2v1 = Word2VecModel.loadGoogleNewsModel();
//        test.process("resources/in/toefl_test", w2v1);
    }
}
