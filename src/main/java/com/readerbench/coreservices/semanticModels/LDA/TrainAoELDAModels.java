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
package com.readerbench.coreservices.semanticModels.LDA;

import com.readerbench.datasourceprovider.pojo.Lang;

import java.io.File;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TrainAoELDAModels {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(TrainAoELDAModels.class);

    public static void trainModels(String path, int noThreads, int noIterations) throws IOException {
        // determine number of classes
        int noGrades = (new File(path)).listFiles((File pathname) -> {
            if (pathname.isDirectory()) {
                return true;
            }
            return false;
        }).length;

        // proportional method
        // 3612 6530 9078 12022 16810 21824 23378 24912 26499 29465 32160 33409
        // 41866
        // int[] noTopics = { 5, 12, 19, 26, 38, 50, 54, 58, 62, 69, 76, 79,
        // 100 };
        // 5-topics increments
        // int[] noTopics = { 5, 10, 15, 20, 25, 30, 35, 40, 45, 50, 55, 60,
        // 100 };
        int noTopics = 100;

        for (int i = 0; i < noGrades; i++) {
            String classPath = path + "/grade" + i;
            LDA lda = new LDA(Lang.en);
            lda.processCorpus(classPath, noTopics, noThreads, noIterations);
        }
    }

    public static void main(String[] args) {
        try {
            TrainAoELDAModels.trainModels("resources/in/AoE 100", 8, 20000);
        } catch (IOException e) {
            LOGGER.error(e.getMessage());
        }
    }
}
