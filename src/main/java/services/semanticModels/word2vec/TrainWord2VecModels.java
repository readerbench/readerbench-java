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
package services.semanticModels.word2vec;

import java.io.File;
import java.io.IOException;
import org.openide.util.Exceptions;

public class TrainWord2VecModels {

    public static void trainAoEModels(String path) throws IOException {
        // determine number of grade levels
        int noGrades = (new File(path)).listFiles(File::isDirectory).length;

        for (int i = 0; i < noGrades; i++) {
            String gradePath = path + "/grade" + i;

            Word2VecModel.trainModel(gradePath + "/alltexts[1-" + (i + 1) + "].txt");
        }
    }

    public static void main(String[] args) {
        try {
            Word2VecModel.trainModel("resources/config/EN/word2vec/TASA/out.txt");
            Word2VecModel.trainModel("resources/config/EN/word2vec/COCA_newspaper/out.txt");
//            TrainWord2VecModels.trainAoEModels("resources/in/AoE 100");
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
}
