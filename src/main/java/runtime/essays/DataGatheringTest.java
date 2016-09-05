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
package runtime.essays;

import java.io.File;
import java.io.IOException;

import org.apache.log4j.BasicConfigurator;

import data.document.Document;
import data.Lang;
import org.openide.util.Exceptions;
import services.complexity.DataGathering;
import services.semanticModels.LDA.LDA;
import services.semanticModels.LSA.LSA;
import webService.ReaderBenchServer;

public class DataGatheringTest {

    public static void exportPlainTexts(String path) throws IOException {
        File dir = new File(path);

        if (!dir.exists()) {
            throw new IOException("Inexistent Folder: " + dir.getPath());
        }

        File[] files = dir.listFiles(
                (File pathname)
                -> pathname.getName().toLowerCase().endsWith(".xml"));

        for (File file : files) {
            try {
                Document d = Document.load(file, null, null, Lang.eng, false, true);
                d.saveTxtDocument();
            } catch (Exception e) {
                Exceptions.printStackTrace(e);
            }
        }
    }

    public static void main(String[] args) {
        BasicConfigurator.configure();

        ReaderBenchServer.initializeDB();
//        LSA lsa = LSA.loadLSA("resources/config/LA/LSA/Letters", Lang.la);
//        LDA lda = LDA.loadLDA("resources/config/LA/LDA/Letters", Lang.la);
//        LSA lsa = LSA.loadLSA("resources/config/RO/LSA/Religie", Lang.ro);
//        LDA lda = LDA.loadLDA("resources/config/RO/LDA/Religie", Lang.ro);
//        LSA lsa = LSA.loadLSA("resources/config/EN/LSA/TASA", Lang.eng);
//        LDA lda = LDA.loadLDA("resources/config/EN/LDA/TASA", Lang.eng);
//        LSA lsa = LSA.loadLSA("resources/config/EN/LSA/TASA_LAK", Lang.eng);
//        LDA lda = LDA.loadLDA("resources/config/EN/LDA/TASA_LAK", Lang.eng);
        LSA lsa = LSA.loadLSA("resources/config/FR/LSA/Le_Monde", Lang.fr);
        LDA lda = LDA.loadLDA("resources/config/FR/LDA/Le_Monde", Lang.fr);
//        LSA lsa = LSA.loadLSA("resources/config/FR/LSA/Le_Monde_Nursery", Lang.fr);
//        LDA lda = LDA.loadLDA("resources/config/FR/LDA/Le_Monde_Nursery", Lang.fr);
//        LDA lda = LDA.loadLDA("resources/config/NL/LDA/INL", Lang.nl);
        try {
//            DataGathering.processTexts("resources/in/essays/essays_FYP_en/texts", -1, true, lsa, lda, Lang.eng, true, true);
//            DataGathering.processTexts("resources/in/essays/competition_en/texts", -1, true, lsa, lda, Lang.eng, true, true);
//            DataGathering.processTexts("resources/in/essays/images_en/texts", -1, true, lsa, lda, Lang.eng, true, true);
//            DataGathering.processTexts("resources/in/essays/DC_essays_2009_en/texts", -1, true, lsa, lda, Lang.eng, true, true);
//            DataGathering.processTexts("resources/in/essays/msu_timed_en/texts", -1, true, lsa, lda, Lang.eng, true, true);
//            DataGathering.processTexts("resources/in/SEvsTA/texts", -1, true, lsa, lda, Lang.eng, true, true);
//            DataGathering.processTexts("resources/in/essays/posttest_fall_2009/texts", -1, true, lsa, lda, Lang.eng, true, true);
//            DataGathering.processTexts("resources/in/essays/pretest_spring_2010/texts", -1, true, lsa, lda, Lang.eng, true, true);
//            DataGathering.processTexts("resources/in/texts 2 for familiarity", -1, true, lsa, lda, Lang.eng, true, true);

            DataGathering.processTexts("resources/in/Philippe/chaprou/pretest", -1, true, lsa, lda, Lang.fr, true, true);
            DataGathering.processTexts("resources/in/Philippe/chaprou/posttest", -1, true, lsa, lda, Lang.fr, true, true);

//            DataGathering.processMetaDocuments("resources/in/ViBOA_nl/analysis", null, lda, Lang.nl, false, true);
//            DataGathering.processMetaDocuments("resources/in/ViBOA_nl/design task 1", null, null, Lang.nl, false, true);
//            DataGathering.processMetaDocuments("resources/in/ViBOA_nl/design task 2", null, null, Lang.nl, false, true);
//            DataGathering.processMetaDocuments("resources/in/ViBOA_nl/evaluation task 1", null, null, Lang.nl, false, true);
//            DataGathering.processMetaDocuments("resources/in/ViBOA_nl/evaluation task 2", null, null, Lang.nl, false, true);
//            DataGathering.processMetaDocuments("resources/in/ViBOA_nl/final task", null, null, Lang.nl, false, true);
//
//            DataGathering.processTexts("resources/in/essays/nursery_fr", -1, true, lsa, lda, Lang.fr, true, true);
//
//            DataGathering.processTexts("resources/in/Elvira/users 2010", -1, true, lsa, lda, Lang.eng, true, true);
//            DataGathering.processTexts("resources/in/Elvira/users 2011", -1, true, lsa, lda, Lang.eng, true, true);
//            DataGathering.processTexts("resources/in/Elvira/users 2012", -1, true, lsa, lda, Lang.eng, true, true);
//            DataGathering.processTexts("resources/in/Elvira/users 2013", -1, true, lsa, lda, Lang.eng, true, true);
//            DataGathering.processTexts("resources/in/Elvira/users 2014", -1, true, lsa, lda, Lang.eng, true, true);
//            DataGathering.processTexts("resources/in/Elvira/users 2015", -1, true, lsa, lda, Lang.eng, true, true);
//
//            DataGathering.processTexts("resources/in/Eminescu vs Bratianu/Eminescu 1877 - 1880", -1, true, lsa, lda, Lang.ro, false, false);
//            DataGathering.processTexts("resources/in/Eminescu vs Bratianu/Bratianu 1857 - 1875", -1, true, lsa, lda, Lang.ro, false, false);
//
//            DataGathering.processTexts("resources/in/comenius_la/01", 1, true, lsa, lda, Lang.la, false, false);
//            DataGathering.processTexts("resources/in/comenius_la/02", 1, true, lsa, lda, Lang.la, false, false);
//            DataGathering.processTexts("resources/in/comenius_la/03", 1, true, lsa, lda, Lang.la, false, false);
//            DataGathering.processTexts("resources/in/comenius_la/04", 1, true, lsa, lda, Lang.la, false, false);
//
//            exportPlainTexts("resources/in/Elvira/users 2010");
//            exportPlainTexts("resources/in/Elvira/users 2011");
//            exportPlainTexts("resources/in/Elvira/users 2012");
//            exportPlainTexts("resources/in/Elvira/users 2013");
//            exportPlainTexts("resources/in/Elvira/users 2014");
//            exportPlainTexts("resources/in/Elvira/users 2015");
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
}
