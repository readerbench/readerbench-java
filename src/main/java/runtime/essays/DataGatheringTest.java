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

import data.document.Document;
import data.Lang;
import java.util.ArrayList;
import java.util.List;
import org.openide.util.Exceptions;
import services.complexity.DataGathering;
import services.converters.Txt2XmlConverter;
import services.semanticModels.ISemanticModel;
import services.semanticModels.LDA.LDA;
import services.semanticModels.LSA.LSA;
import services.semanticModels.word2vec.Word2VecModel;
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
                Document d = Document.load(file, new ArrayList<>(), Lang.en, false);
                d.saveTxtDocument();
            } catch (Exception e) {
                Exceptions.printStackTrace(e);
            }
        }
    }

    public static void convertAndProcess(String path, Lang lang, List<ISemanticModel> models) {
        Txt2XmlConverter.parseTxtFiles("", path, Lang.en, "UTF-8");
        try {
            DataGathering.processTexts(path, "", true, models, lang, true, true);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    public static void main(String[] args) throws IOException {
        ReaderBenchServer.initializeDB();

//        LSA lsa = LSA.loadLSA("resources/config/LA/LSA/Letters", Lang.la);
//        LDA lda = LDA.loadLDA("resources/config/LA/LDA/Letters", Lang.la);
//        LSA lsa = LSA.loadLSA("resources/config/RO/LSA/Religie", Lang.ro);
//        LDA lda = LDA.loadLDA("resources/config/RO/LDA/Religie", Lang.ro);
//        LSA lsa = LSA.loadLSA("resources/config/EN/LSA/TASA", Lang.en);
//        LDA lda = LDA.loadLDA("resources/config/EN/LDA/TASA", Lang.en);
//        Word2VecModel w2v = Word2VecModel.loadWord2Vec("resources/config/EN/word2vec/TASA", Lang.en);
        LSA lsa = LSA.loadLSA("resources/config/EN/LSA/COCA_newspaper", Lang.en);
        LDA lda = LDA.loadLDA("resources/config/EN/LDA/COCA_newspaper", Lang.en);
        Word2VecModel w2v = Word2VecModel.loadWord2Vec("resources/config/EN/word2vec/COCA_newspaper", Lang.en);
//        LSA lsa = LSA.loadLSA("resources/config/EN/LSA/TASA_LAK", Lang.en);
//        LDA lda = LDA.loadLDA("resources/config/EN/LDA/TASA_LAK", Lang.en);
//        LSA lsa = LSA.loadLSA("resources/config/ES/LSA/Jose_Antonio", Lang.es);
//        LDA lda = LDA.loadLDA("resources/config/ES/LDA/Jose_Antonio", Lang.es);

//        LSA lsa = LSA.loadLSA("resources/config/FR/LSA/Le_Monde", Lang.fr);
//        LDA lda = LDA.loadLDA("resources/config/FR/LDA/Le_Monde", Lang.fr);
//        LSA lsa = LSA.loadLSA("resources/config/FR/LSA/Le_Monde_Nursery", Lang.fr);
//        LDA lda = LDA.loadLDA("resources/config/FR/LDA/Le_Monde_Nursery", Lang.fr);
//        LDA lda = LDA.loadLDA("resources/config/NL/LDA/INL", Lang.nl);
//        Word2VecModel w2v = Word2VecModel.loadWord2Vec("resources/config/NL/word2vec/INL", Lang.nl);
        List<ISemanticModel> models = new ArrayList<>();
        models.add(lsa);
        models.add(lda);
        models.add(w2v);

//        convertAndProcess("resources/in/Cohesion/artificial cohesion_es", Lang.es, models);
        
//        convertAndProcess("resources/in/Cohesion/artificial cohesion_en", Lang.en, models);

//        convertAndProcess("resources/in/creativity/individual creativity tasks (txt)", Lang.en, models);
//        convertAndProcess("resources/in/pairwise/texts", Lang.en, models);
//        convertAndProcess("resources/in/cohesion/Archive/texts", Lang.en, models);
//        convertAndProcess("resources/in/cohesion/CohMetrix/texts", Lang.en, models);
//        convertAndProcess("resources/in/cohesion/msu timed/posttest essays fall 2009", Lang.en, models);
//        convertAndProcess("resources/in/cohesion/msu timed/pretest spring 2010/1113 pretest essays", Lang.en, models);
        Txt2XmlConverter.parseTxtFiles("", "resources/in/essays/iStart mini-games/texts", Lang.en, "UTF-8");
//        Txt2XmlConverter.parseTxtFiles("", "resources/in/essays/stairstepper_en/texts", Lang.en, "ISO-8859-1");
//        DataGathering.processTexts("resources/in/essays/stairstepper_en/texts", "", true, models, Lang.en, true, true);
//        DataGathering.processTexts("resources/in/essays/essays_FYP_en/texts", -1, true, lsa, lda, Lang.en, true, true);
        DataGathering.processTexts("resources/in/essays/iStart mini-games/texts", "", true, models, Lang.en, true, true);
//        DataGathering.processTexts("resources/in/essays/images_en/texts", -1, true, lsa, lda, Lang.en, true, true);
//        DataGathering.processTexts("resources/in/essays/DC_essays_2009_en/texts", -1, true, lsa, lda, Lang.en, true, true);
//        DataGathering.processTexts("resources/in/essays/msu_timed_en/texts", -1, true, lsa, lda, Lang.en, true, true);
//        DataGathering.processTexts("resources/in/SEvsTA/texts", "", true, models, Lang.en, true, true);
//        DataGathering.processTexts("resources/in/essays/posttest_fall_2009/texts", -1, true, lsa, lda, Lang.en, true, true);
//        DataGathering.processTexts("resources/in/essays/pretest_spring_2010/texts", -1, true, lsa, lda, Lang.en, true, true);
//        DataGathering.processTexts("resources/in/texts 2 for familiarity", -1, true, lsa, lda, Lang.en, true, true);
//        DataGathering.processTexts("resources/in/essays/iStart mini-games/texts", -1, true, lsa, lda, Lang.en, true, true);
//        DataGathering.processTexts("resources/in/grenoble/sciedu/pdessus/cours/cours-qcm", -1, true, models, Lang.en, true, true);
//        DataGathering.processTexts("resources/in/Philippe/chaprou/pretest", -1, true, lsa, lda, Lang.fr, true, true);
//        DataGathering.processTexts("resources/in/Philippe/chaprou/posttest", -1, true, lsa, lda, Lang.fr, true, true);
//        DataGathering.processMetaDocuments("resources/in/ViBOA_nl/analysis H1", models, Lang.nl, false, true);
//        DataGathering.processMetaDocuments("resources/in/ViBOA_nl/design task 1", null, null, Lang.nl, false, true);
//        DataGathering.processMetaDocuments("resources/in/ViBOA_nl/design task 2", null, null, Lang.nl, false, true);
//        DataGathering.processMetaDocuments("resources/in/ViBOA_nl/evaluation task 1", null, null, Lang.nl, false, true);
//        DataGathering.processMetaDocuments("resources/in/ViBOA_nl/evaluation task 2", null, null, Lang.nl, false, true);
//        DataGathering.processMetaDocuments("resources/in/ViBOA_nl/final task", null, null, Lang.nl, false, true);
//
//        DataGathering.processTexts("resources/in/essays/nursery_fr", -1, true, lsa, lda, Lang.fr, true, true);
//
//        DataGathering.processTexts("resources/in/Elvira/users 2010", -1, true, lsa, lda, Lang.en, true, true);
//        DataGathering.processTexts("resources/in/Elvira/users 2011", -1, true, lsa, lda, Lang.en, true, true);
//        DataGathering.processTexts("resources/in/Elvira/users 2012", -1, true, lsa, lda, Lang.en, true, true);
//        DataGathering.processTexts("resources/in/Elvira/users 2013", -1, true, lsa, lda, Lang.en, true, true);
//        DataGathering.processTexts("resources/in/Elvira/users 2014", -1, true, lsa, lda, Lang.en, true, true);
//        DataGathering.processTexts("resources/in/Elvira/users 2015", -1, true, lsa, lda, Lang.en, true, true);
//
//        DataGathering.processTexts("resources/in/Eminescu vs Bratianu/Eminescu 1877 - 1880", -1, true, lsa, lda, Lang.ro, false, false);
//        DataGathering.processTexts("resources/in/Eminescu vs Bratianu/Bratianu 1857 - 1875", -1, true, lsa, lda, Lang.ro, false, false);
//
//        DataGathering.processTexts("resources/in/comenius_la/01", 1, true, lsa, lda, Lang.la, false, false);
//        DataGathering.processTexts("resources/in/comenius_la/02", 1, true, lsa, lda, Lang.la, false, false);
//        DataGathering.processTexts("resources/in/comenius_la/03", 1, true, lsa, lda, Lang.la, false, false);
//        DataGathering.processTexts("resources/in/comenius_la/04", 1, true, lsa, lda, Lang.la, false, false);
//
//        exportPlainTexts("resources/in/Elvira/users 2010");
//        exportPlainTexts("resources/in/Elvira/users 2011");
//        exportPlainTexts("resources/in/Elvira/users 2012");
//        exportPlainTexts("resources/in/Elvira/users 2013");
//        exportPlainTexts("resources/in/Elvira/users 2014");
//        exportPlainTexts("resources/in/Elvira/users 2015");
    }
}
