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
package runtime.diachrony;

import java.io.IOException;

import org.apache.log4j.BasicConfigurator;

import data.Lang;
import org.openide.util.Exceptions;
import services.complexity.DataGathering;
import services.converters.Txt2XmlConverter;
import services.semanticModels.LDA.LDA;
import services.semanticModels.LSA.LSA;
import webService.ReaderBenchServer;

public class TestDiachrony {

    public static void main(String[] args) {
        BasicConfigurator.configure();

        ReaderBenchServer.initializeDB();

        LSA lsa = LSA.loadLSA("resources/config/RO/LSA/Religie", Lang.ro);
        LDA lda = LDA.loadLDA("resources/config/RO/LDA/Religie", Lang.ro);
        String[] periods = {"1941-1991", "dupa 1992"};
        String[] regions = {"Basarabia", "Romania"};
        String path = "resources/in/diacronie_ro";

        for (String period : periods) {
            for (String region : regions) {
                String localPath = path + "/" + period + "/" + region;
                Txt2XmlConverter.parseTxtFiles("", localPath, Lang.ro, "UTF-8");
                try {
                    DataGathering.processTexts(localPath, -1, true, lsa, lda, Lang.ro, false, false);
                } catch (IOException e) {
                    Exceptions.printStackTrace(e);
                }
            }
        }
    }
}
