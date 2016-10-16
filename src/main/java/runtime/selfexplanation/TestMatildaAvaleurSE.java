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
package runtime.selfexplanation;

import data.AbstractDocument;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import data.document.Document;
import data.document.Metacognition;
import data.Lang;
import data.document.ReadingStrategyType;
import java.util.EnumMap;
import java.util.Map;
import org.openide.util.Exceptions;
import services.complexity.ComplexityIndex;
import services.complexity.ComplexityIndices;
import services.semanticModels.SemanticModel;
import webService.ReaderBenchServer;

public class TestMatildaAvaleurSE {

    static Logger logger = Logger.getLogger(TestMatildaAvaleurSE.class);

    public static List<Metacognition> compute(String filename, String folder) {
        List<Metacognition> verbalizations = new ArrayList<>();
        Map<SemanticModel, String> modelPaths = new EnumMap<>(SemanticModel.class);
        modelPaths.put(SemanticModel.LSA, "resources/config/FR/LSA/Le Monde");
        modelPaths.put(SemanticModel.LDA, "resources/config/FR/LDA/Le Monde");
        Document doc = Document.load(filename, modelPaths, Lang.fr, true);
        File verbFolder = new File(folder);
        for (File f : verbFolder.listFiles((File dir, String name) -> name.endsWith(".xml"))) {
            Metacognition v = Metacognition.loadVerbalization(f.getAbsolutePath(), doc, true);
            // Metacognition v = (Metacognition)
            // Metacognition.loadSerializedDocument(f.getAbsolutePath());
            v.computeAll(true);
            v.save(AbstractDocument.SaveType.SERIALIZED);
            verbalizations.add(v);
        }
        return verbalizations;
    }

    public static List<Metacognition> load(String folder) {
        List<Metacognition> verbalizations = new ArrayList<>();
        File verbFolder = new File(folder);
        for (File f : verbFolder.listFiles((File dir, String name) -> name.endsWith(".ser"))) {
            Metacognition v = (Metacognition) Metacognition.loadSerializedDocument(f.getAbsolutePath());
            v.computeAll(true);
            verbalizations.add(v);
        }
        return verbalizations;
    }

    public static void main(String[] args) {
        BasicConfigurator.configure();
        Logger.getRootLogger().setLevel(Level.INFO);
        ReaderBenchServer.initializeDB();

        try {
            String folder = "resources/in/Matilda & Avaleur";

            List<Metacognition> verbalizations = new ArrayList<>();
            try (BufferedWriter out = new BufferedWriter(new FileWriter(folder + "/output.csv"))) {
                out.write("Filename,Comprehension score,Comprehension class,Fluency");
                for (ReadingStrategyType rs : ReadingStrategyType.values()) {
                    out.write(",Annotated " + rs.getName());
                }
                for (ComplexityIndex index : ComplexityIndices.getIndices(Lang.fr)) {
                    out.write("," + index.getAcronym());
                }
                for (ReadingStrategyType rs : ReadingStrategyType.values()) {
                    out.write(",Automated " + rs.getName());
                }

                // verbalizations.addAll(compute(folder + "/Matilda.xml",folder + "/Matilda verbalizations"));
                // verbalizations.addAll(compute(folder + "/L'avaleur de nuages.xml",folder + "/Avaleur verbalizations"));
                verbalizations.addAll(load(folder + "/Matilda verbalizations"));
                verbalizations.addAll(load(folder + "/Avaleur verbalizations"));

                for (Metacognition v : verbalizations) {
                    out.write("\n" + (new File(v.getPath()).getName()) + "," + v.getAnnotatedComprehensionScore() + "," + v.getComprehensionClass() + "," + v.getAnnotatedFluency());
                    EnumMap<ReadingStrategyType, Integer> annotatedRS = v.getAllRS(v.getAnnotatedRS());
                    EnumMap<ReadingStrategyType, Integer> automatedRS = v.getAllRS(v.getAutomatedRS());
                    for (ReadingStrategyType rs : ReadingStrategyType.values()) {
                        out.write("," + annotatedRS.get(rs));
                    }
                    for (ComplexityIndex index : ComplexityIndices.getIndices(Lang.fr)) {
                        out.write("," + v.getComplexityIndices().get(index));
                    }
                    for (ReadingStrategyType rs : ReadingStrategyType.values()) {
                        out.write("," + automatedRS.get(rs));
                    }
                }
            }
            logger.info("Finished all files for processing!");
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }
    }
}
