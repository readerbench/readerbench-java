/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package runtime.essays;

import data.Lang;
import data.document.Document;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.FilenameUtils;
import org.openide.util.Exceptions;
import services.commons.VectorAlgebra;
import services.complexity.ComplexityIndex;
import services.complexity.ComplexityIndices;
import services.converters.Txt2XmlConverter;
import services.semanticModels.ISemanticModel;
import services.semanticModels.LDA.LDA;
import services.semanticModels.SimilarityType;
import webService.ReaderBenchServer;

/**
 *
 * @author Mihai Dascalu
 */
public class StudentDiscusssions {

    static final Logger LOGGER = Logger.getLogger("");

    public static void processLDA(String processingPath, LDA lda, boolean usePOSTagging) throws IOException {
        File dir = new File(processingPath);

        if (!dir.exists()) {
            throw new IOException("Inexistent Folder: " + dir.getPath());
        }

        File[] files = dir.listFiles((File pathname) -> {
            return pathname.getName().toLowerCase().endsWith(".xml");
        });

        //write header
        try (BufferedWriter out = new BufferedWriter(new FileWriter(processingPath + "/measurements.csv", false))) {
            out.write("Filename");
            for (int i = 0; i < lda.getNoDimensions(); i++) {
                out.append(",").append("Topic " + i);
            }
        }

        List<ISemanticModel> models = new ArrayList<>();
        models.add(lda);

        for (File file : files) {
            LOGGER.log(Level.INFO, "Processing {0} file", file.getName());
            // Create file

            Document d = null;
            try {
                d = Document.load(file, models, lda.getLanguage(), usePOSTagging);
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Runtime error while processing {0}: {1}", new Object[]{file.getName(), e.getMessage()});
                Exceptions.printStackTrace(e);
            }

            if (d != null) {
                try (BufferedWriter out = new BufferedWriter(new FileWriter(processingPath + "/measurements.csv", true))) {
                    StringBuilder concat = new StringBuilder();
                    String fileName = FilenameUtils.removeExtension(file.getName().replaceAll(",", ""));
                    concat.append("\n").append(fileName);
                    double[] probDistribution = VectorAlgebra.normalize(d.getModelVectors().get(SimilarityType.LDA));
                    for (int i = 0; i < lda.getNoDimensions(); i++) {
                        concat.append(",").append(probDistribution[i]);
                    }
                    out.write(concat.toString());
                } catch (IOException ex) {
                    LOGGER.severe("Runtime error while creating measurements.csv file");
                    Exceptions.printStackTrace(ex);
                }
            }
        }
    }

    public static void main(String[] args) throws IOException {
        ReaderBenchServer.initializeDB();

        LDA lda = LDA.loadLDA("resources/config/EN/LDA/Scott", Lang.en);

        String path = "/Users/mihaidascalu/Desktop/Archive Scott";
        Txt2XmlConverter.parseTxtFiles("", path, Lang.en, "UTF-8");
        processLDA(path, lda, true);
    }
}
