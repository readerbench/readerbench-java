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

import data.AbstractDocument.SaveType;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import data.document.Document;
import data.document.Summary;
import data.Lang;
import data.document.ReadingStrategyType;
import java.util.logging.Level;

import org.openide.util.Exceptions;
import services.complexity.ComplexityIndex;
import services.complexity.ComplexityIndices;
import services.semanticModels.ISemanticModel;
import services.semanticModels.LDA.LDA;
import services.semanticModels.LSA.LSA;
import services.semanticModels.SimilarityType;
import webService.ReaderBenchServer;

public class FrenchSummaryProcessing {

    static final Logger LOGGER = Logger.getLogger("");

    private final String path;
    private final Document refDoc;
    private final List<Summary> loadedSummaries;

    public FrenchSummaryProcessing(String path, Document refDoc) {
        this.path = path;
        this.loadedSummaries = new ArrayList<>();
        this.refDoc = refDoc;
    }

    public void process() {
        File folder = new File(path);
        for (File f : folder.listFiles((File dir, String name) -> name.endsWith(".xml"))) {
            LOGGER.log(Level.INFO, "Processing file {0} ...", f.getAbsolutePath());
            Summary summary = Summary.loadSummary(f.getAbsolutePath(), refDoc, true);
            summary.computeAll(true, false);
            summary.save(SaveType.SERIALIZED);
            loadedSummaries.add(summary);
        }

        try (BufferedWriter out = new BufferedWriter(new FileWriter(path + "/measurements.csv"))) {
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
                    if (summary.getCohesion().getSemanticSimilarities().get(semDist) != null) {
                        out.write("," + summary.getCohesion().getSemanticSimilarities().get(semDist));
                    } else {
                        out.write(",-1");
                    }
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
        ReaderBenchServer.initializeDB();

        Lang lang = Lang.fr;
        String pathToOriginalFile = "resources/in/Philippe/DEPP/Essays/avaleur/avaleur_original.xml";
        LSA lsa = LSA.loadLSA("resources/config/FR/LSA/Le_Monde", lang);
        LDA lda = LDA.loadLDA("resources/config/FR/LDA/Le_Monde", lang);
        List<ISemanticModel> models = new ArrayList<>();
        models.add(lsa);
        models.add(lda);

        Document originalDocument = Document.load(new File(pathToOriginalFile), models, lang, true);
        FrenchSummaryProcessing crt;
        crt = new FrenchSummaryProcessing("resources/in/Philippe/DEPP/Essays/avaleur/txt-corriges-avaleur", originalDocument);
        crt.process();

        crt = new FrenchSummaryProcessing("resources/in/Philippe/DEPP/Essays/avaleur/txt-non-corriges-avaleur", originalDocument);
        crt.process();
    }
}
