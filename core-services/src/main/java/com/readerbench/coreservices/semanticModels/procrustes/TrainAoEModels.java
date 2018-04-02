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
package com.readerbench.coreservices.semanticModels.procrustes;

import com.readerbench.data.Lang;
import org.openide.util.Exceptions;
import com.readerbench.coreservices.semanticModels.LSA.CreateInputMatrix;
import com.readerbench.coreservices.semanticModels.LSA.RunSVD;
import com.readerbench.coreservices.semanticModels.word2vec.Word2VecModel;

import java.io.File;
import java.io.IOException;

public class TrainAoEModels {

    public static void trainW2VModels(String path) throws IOException {
        // determine number of grade levels
        int noGrades = (new File(path)).listFiles(File::isDirectory).length;

        for (int i = 0; i < noGrades; i++) {
            String gradePath = path + "/grade" + i;

            Word2VecModel.trainModel(gradePath + "/alltexts[1-" + (i + 1) + "].txt");
        }
    }

    public static void trainLSAModels(String path) throws IOException {
        // determine number of grade levels
        int noGrades = (new File(path)).listFiles(File::isDirectory).length;
        String term_doc_matrix = "matrix.svd";
        int k = 300;
        int noPowerIterations = 1;

        for (int i = 0; i < noGrades; i++) {
            File gradePath = new File(path + "/grade" + i + "/alltexts[1-" + (i + 1) + "].txt");
            CreateInputMatrix lsaTraining = new CreateInputMatrix();
            lsaTraining.parseCorpus(gradePath.getParent(), gradePath.getName(), term_doc_matrix, Lang.en);

            RunSVD.runSSVDOnSparseVectors(gradePath.getParent() + "/" + term_doc_matrix,
                    gradePath.getParent(), k, k, noPowerIterations);
        }
    }

    public static void main(String[] args) {

        try {
//            Word2VecModel.trainModel("resources/in/AoE w2v/grade2/alltexts[1-3].txt");
            Word2VecModel.trainModel("COCA_newspaper/coca_newspaper.txt");
//            TrainAoEModels.trainW2VModels("resources/in/AoE w2v");
//            TrainAoEModels.trainLSAModels("resources/in/AoE LSA");
        } catch (IOException ex) {
            LOGGER.error(ex.getMessage());
        }
    }
}
