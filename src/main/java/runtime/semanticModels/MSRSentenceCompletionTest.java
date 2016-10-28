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
import java.util.logging.Logger;

import org.apache.commons.io.FileUtils;


import data.AbstractDocument;
import data.AbstractDocumentTemplate;
import data.document.Document;
import data.Lang;
import java.util.ArrayList;
import java.util.List;
import org.openide.util.Exceptions;
import services.commons.Formatting;
import services.semanticModels.ISemanticModel;
import services.semanticModels.word2vec.Word2VecModel;
import webService.ReaderBenchServer;

public class MSRSentenceCompletionTest {

    static Logger logger = Logger.getLogger("");

    int questionId;

    public void process(String path, ISemanticModel semModel) {
        logger.info("Starting sentence completion test...");

        List<ISemanticModel> models = new ArrayList<>();
        models.add(semModel);

        try {
            String line;
            int[] correct;
            try ( // read answers
                    BufferedReader answers = new BufferedReader(new FileReader(path + "/Holmes.human_format.answers.txt"))) {
                correct = new int[1041];
                questionId = 1;
                while ((line = answers.readLine()) != null) {
                    if (line.contains("[a]")) {
                        correct[questionId] = 0;
                    } else if (line.contains("[b]")) {
                        correct[questionId] = 1;
                    } else if (line.contains("[c]")) {
                        correct[questionId] = 2;
                    } else if (line.contains("[d]")) {
                        correct[questionId] = 3;
                    } else if (line.contains("[e]")) {
                        correct[questionId] = 4;
                    }
                    questionId++;
                }
            }

            questionId = 1;
            StringBuilder sb = new StringBuilder();
            sb.append("sep=,\nQuestion ID,guessed answer,sim,correct answer,is correct?");

            try (BufferedReader questions = new BufferedReader(new FileReader(path + "/Holmes.human_format.questions.txt"))) {
                AbstractDocument concepts[] = new AbstractDocument[6];

                int noCorrects = 0;
                outer:
                while (true) {
                    // read each line
                    for (int i = 0; i < 6; i++) {
                        if ((line = questions.readLine()) == null) {
                            break outer;
                        }
                        concepts[i] = processDoc(line, models, semModel.getLanguage());
                    }

                    // read blank lines
                    questions.readLine();
                    questions.readLine();

                    double maxSim = Double.MIN_VALUE;
                    int maxIndex = -1;

                    for (int i = 1; i < 6; i++) {
                        double sim = semModel.getSimilarity(concepts[0], concepts[i]);
                        if (sim > maxSim) {
                            maxSim = sim;
                            maxIndex = i - 1;
                        }
                    }

                    sb.append("\n").append(questionId).append(",").append(maxIndex).append(",").append(maxSim).append(",").append(correct[questionId]).append(",").append((maxIndex == correct[questionId]) ? 1 : 0);
                    if (maxIndex == correct[questionId]) {
                        noCorrects++;
                    }
                    questionId++;
                }
                logger.info("Finished using " + semModel.getPath() + " semantic model: " + noCorrects
                        + " correct predictions and " + Formatting.formatNumber(noCorrects / 1040d * 100) + "% accuracy");
            }

            File file = new File(path + "/out_" + semModel.getPath().replace("resources/config/", "").replaceAll("/", "_") + ".csv");
            try {
                FileUtils.writeStringToFile(file, sb.toString());
            } catch (Exception e) {
                Exceptions.printStackTrace(e);
            }
        } catch (Exception e) {
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

        MSRSentenceCompletionTest test = new MSRSentenceCompletionTest();

//        ISemanticModel lsa1 = LSA.loadLSA("resources/config/EN/LSA/TASA", Lang.en);
//        test.process("resources/in/MSR sentence completion", lsa1);
//        lsa1 = LSA.loadLSA("resources/config/EN/LSA/COCA newspaper", Lang.en);
//        test.process("resources/in/MSR sentence completion", lsa1);
//        ISemanticModel lda1 = LDA.loadLDA("resources/config/EN/LDA/TASA", Lang.en);
//        test.process("resources/in/MSR sentence completion", lda1);
//        lda1 = LDA.loadLDA("resources/config/EN/LDA/COCA newspaper", Lang.en);
//        test.process("resources/in/MSR sentence completion", lda1);
        Word2VecModel w2v1 = Word2VecModel.loadWord2Vec("resources/config/EN/word2vec/TASA_epoch3", Lang.en);
        test.process("resources/in/MSR sentence completion/", w2v1);
        w2v1 = Word2VecModel.loadWord2Vec("resources/config/EN/word2vec/TASA_epoch3_iter3", Lang.en);
        test.process("resources/in/MSR sentence completion/", w2v1);
        w2v1 = Word2VecModel.loadWord2Vec("resources/config/EN/word2vec/TASA_iter5", Lang.en);
        test.process("resources/in/MSR sentence completion/", w2v1);
//        w2v1 = Word2VecModel.loadGoogleNewsModel();
//        test.process("resources/in/MSR sentence completion", w2v1);
    }
}
