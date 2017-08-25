/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package runtime.document;

import data.Lang;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.logging.Logger;
import org.openide.util.Exceptions;
import services.semanticModels.ISemanticModel;
import services.semanticModels.PreProcessing;

/**
 *
 * @author Simona
 */
public class ClassificationParagraphVectors {

    static final Logger LOGGER = Logger.getLogger("");
    private static List<ISemanticModel> models;
    private static Lang lang = Lang.en;

    /*
     iterate over folders from TASA corpus and create a separate processed file for each category/folder
     */
    private void preProcessDocuments(String inputPath) {
        File inputFolder = new File(inputPath);
        File[] categorisedFolder = inputFolder.listFiles();
        for (File folder : categorisedFolder) {
            if (folder.isFile()) {
                continue;
            }
            try {
                PreProcessing task = new PreProcessing();
                task.parseTasa(folder.getAbsolutePath(), folder.getName() + "_out" + ".txt", this.lang, false, 0, null);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }

    /*
    iterate over processed files from previous stage 
    and create a separate folder with docs for each paragraph from input file
     */
    private void splitDocumentInDocuments(String inputPath, String encoding) {
        // process each file
        String line;
        int lineCounter = 0;

        File inputFolder = new File(inputPath);
        File[] categorisedFiles = inputFolder.listFiles();
        for (File processedFile : categorisedFiles) {
            if (!processedFile.isFile()) {
                continue;
            }
            String folderName = processedFile.getName().substring(0, processedFile.getName().length() - 8);
            try (BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(processedFile), encoding))) {
                while ((line = in.readLine()) != null) {
                    if (line.trim().length() > 0) {
                        try (BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(inputPath).getParent() + "/Tasa_processed/" + folderName + "/file" + lineCounter + ".txt"), "UTF-8"), 32768)) {
                            out.write(line + "\n");
                            out.close();
                        }
                    }
                    lineCounter++;
                }
            } catch (FileNotFoundException e) {
                Exceptions.printStackTrace(e);
            } catch (UnsupportedEncodingException e) {
                Exceptions.printStackTrace(e);
            } catch (IOException e) {
                Exceptions.printStackTrace(e);
            }
        }
    }

    public static void main(String[] args) {

        ClassificationParagraphVectors cpv = new ClassificationParagraphVectors();
//        cpv.preProcessDocuments("resources/config/EN/Tasa");
//        cpv.splitDocumentInDocuments("resources/config/EN/Tasa", "UTF-8");

        //need a folder named "labeled" which contains folders with preprocessed documents
//        ParagraphVectorsModel.trainModelSupervised("resources/config/EN/Tasa_processed");
//        String modelPath = new File("resources/config/EN").getAbsolutePath();
//        models = new ArrayList<>();
//        models.add(ParagraphVectorsModel.loadParagraphVectors(modelPath, Lang.en));
//        LOGGER.log(Level.INFO, "Loaded ParagraphVectors model {0} ...", modelPath);
//
//        TokenizerFactory tokenizerFactory = new DefaultTokenizerFactory();
//        tokenizerFactory.setTokenPreProcessor(new CommonPreprocessor());
//        FileLabelAwareIterator unClassifiedIterator = new FileLabelAwareIterator.Builder()
//                .addSourceFolder(new File("resources/config/EN/Tasa_processed_unlabeled"))
//                .build();
//        MeansBuilder meansBuilder = new MeansBuilder(
//                (InMemoryLookupTable<VocabWord>) ParagraphVectorsModel.weightLookupTable,
//                tokenizerFactory);
//        LabelSeeker seeker = new LabelSeeker(ParagraphVectorsModel.labels,
//                (InMemoryLookupTable<VocabWord>) ParagraphVectorsModel.weightLookupTable);
//        LOGGER.log(Level.INFO, "Starting classification with ParagraphVectors model ...");
//
//        int correctClassifiedNumber = 0;
//        while (unClassifiedIterator.hasNextDocument()) {
//            LabelledDocument document = unClassifiedIterator.nextDocument();
//            INDArray documentAsCentroid = meansBuilder.documentAsVector(document);
//            List<Pair<String, Double>> scores = seeker.getScores(documentAsCentroid);
//
//            Double maxScore = Double.MIN_VALUE;
//            String obtainedLabel = new String();
//            for (Pair<String, Double> score : scores) {
//                if (score.getSecond() > maxScore) {
//                    maxScore = score.getSecond();
//                    obtainedLabel = score.getFirst();
//                }
//            }
//            if (document.getLabel().equals(obtainedLabel)) {
//                correctClassifiedNumber++;
//            }
//            System.out.println("Doc from: '" + document.getLabel() + "' was classified to: '" + obtainedLabel + "'");
//        }
//        LOGGER.log(Level.INFO, "Ending right classification with ParagraphVectors model for {0} files...", correctClassifiedNumber);
    }
}
