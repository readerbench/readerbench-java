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
import org.apache.log4j.BasicConfigurator;
import org.openide.util.Exceptions;
import services.complexity.ComplexityIndex;
import services.complexity.ComplexityIndices;
import services.semanticModels.LDA.LDA;
import services.semanticModels.LSA.LSA;
import services.semanticModels.WordNet.SimilarityType;
import webService.ReaderBenchServer;

public class ChapouRougeTest {

    static final Logger LOGGER = Logger.getLogger(ChapouRougeTest.class);

    private final String path;
    private final Document refDoc;
    private final List<Summary> loadedSummaries;

    public ChapouRougeTest(String path, Document refDoc) {
        this.path = path;
        this.loadedSummaries = new ArrayList<>();
        this.refDoc = refDoc;
    }

    public void process() {
        File folder = new File(path);
        for (File f : folder.listFiles((File dir, String name) -> name.endsWith(".xml"))) {
            LOGGER.info("Processing file " + f.getAbsolutePath() + " ...");
            Summary summary = Summary.loadSummary(f.getAbsolutePath(), refDoc, true);
            summary.computeAll(true);
            loadedSummaries.add(summary);
        }

        try (BufferedWriter out = new BufferedWriter(new FileWriter(path + "/measurements_rs.csv"))) {
            out.write("Filename");
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
                for (ComplexityIndex index : indices) {
                    out.write("," + summary.getComplexityIndices().get(index));
                }
            }
            LOGGER.info("Finished all files for processing ...");
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    public static void main(String[] args) {
        BasicConfigurator.configure();
        ReaderBenchServer.initializeDB();

        Lang lang = Lang.fr;
        String pathToOriginalFile = "resources/in/Philippe/chaprou/chaprou-original.xml";
        LSA lsa = LSA.loadLSA("resources/config/FR/LSA/Le_Monde", lang);
        LDA lda = LDA.loadLDA("resources/config/FR/LDA/Le_Monde", lang);

        Document chaprouge = Document.load(new File(pathToOriginalFile), lsa, lda, lang, true);
        ChapouRougeTest crt = new ChapouRougeTest("resources/in/Philippe/chaprou/pretest", chaprouge);
        crt.process();

        crt = new ChapouRougeTest("resources/in/Philippe/chaprou/postTest", chaprouge);
        crt.process();
    }
}
