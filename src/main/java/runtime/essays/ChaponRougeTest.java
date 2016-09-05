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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import data.document.Document;
import data.document.Summary;
import data.Lang;
import data.document.ReadingStrategyType;
import org.openide.util.Exceptions;
import static runtime.essays.TestMatildaAvaleurSE.logger;
import services.complexity.ComplexityIndex;
import services.complexity.ComplexityIndices;
import services.discourse.selfExplanations.VerbalizationAssessment;
import services.readingStrategies.ReadingStrategies;
import services.semanticModels.LDA.LDA;
import services.semanticModels.LSA.LSA;
import services.semanticModels.WordNet.SimilarityType;

public class ChaponRougeTest {

    static final Logger LOGGER = Logger.getLogger(ChaponRougeTest.class);

    private final String path;
    private final Document refDoc;
    private final List<Summary> loadedSummaries;

    public ChaponRougeTest(String path, Document refDoc) {
        this.path = path;
        this.loadedSummaries = new ArrayList<>();
        this.refDoc = refDoc;
    }

    public void process() {
        File folder = new File(path);
        for (File f : folder.listFiles((File dir, String name) -> name.endsWith(".xml"))) {
            Summary summary = Summary.loadSummary(f.getAbsolutePath(), refDoc, true, true);
            VerbalizationAssessment.detRefBlockSimilarities(summary);
            ReadingStrategies.detReadingStrategies(summary);

            ComplexityIndices.computeComplexityFactors(summary);
            summary.determineCohesion();
            loadedSummaries.add(summary);
        }

        try (BufferedWriter out = new BufferedWriter(new FileWriter(path + "/measurements_rs.csv"))) {
            out.write("Filename,");
            for (ReadingStrategyType rs : ReadingStrategyType.values()) {
                out.write("," + rs.getName());
            }
            for (SimilarityType semDist : SimilarityType.values()) {
                out.write("," + semDist.getAcronym());
            }
            List<ComplexityIndex> indices = ComplexityIndices.getIndices(refDoc.getLanguage());
            for (ComplexityIndex index : indices) {
                out.write("," + index.getAcronym());
            }

            for (Summary summary : loadedSummaries) {
                out.write("\n" + (new File(summary.getPath()).getName()));
                for (ReadingStrategyType rs : ReadingStrategyType.values()) {
                    out.write("," + summary.getAllRS(summary.getAutomatedRS()).get(rs));
                }
                for (SimilarityType semDist : SimilarityType.values()) {
                    out.write("," + summary.getCohesion().getSemanticSimilarities().get(semDist));
                }
                for (ComplexityIndex index : ComplexityIndices.getIndices(refDoc.getLanguage())) {
                    out.write("," + summary.getComplexityIndices().get(index));
                }

                for (ComplexityIndex index : indices) {
                    out.write("," + summary.getComplexityIndices().get(index));
                }
                out.write("\n");
            }
            logger.info("Finished all files for processing!");
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    public static void main(String[] args) {

        Lang lang = Lang.fr;
        String pathToOriginalFile = "resources/in/Philippe/chaprou/chaprou-original.xml";
        LSA lsa = LSA.loadLSA("resources/config/FR/LSA/Le_Monde", lang);
        LDA lda = LDA.loadLDA("resources/config/FR/LDA/Le_Monde", lang);

        Document chaprouge = Document.load(new File(pathToOriginalFile), lsa, lda, lang, true, true);
        ChaponRougeTest crt = new ChaponRougeTest("resources/in/Philippe/chaprou/pretest", chaprouge);
        crt.process();

//        crt = new ChaponRougeTest("resources/in/Philippe/chaprou/postTest", chaprouge);
//        crt.process();
    }
}
