/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package runtime.document;

import data.AbstractDocument;
import data.AbstractDocumentTemplate;
import data.Lang;
import data.document.Document;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import org.openide.util.Exceptions;
import static runtime.document.ENEAClustering.LOGGER;
import services.commons.Clustering;
import services.discourse.cohesion.CohesionGraph;
import services.discourse.keywordMining.KeywordModeling;
import services.semanticModels.ISemanticModel;
import services.semanticModels.PreProcessing;
import services.semanticModels.paragraphVectors.ParagraphVectorsModel;

/**
 *
 * @author Simona
 */
public class HierarhicalClusteringPV extends Clustering {
    
    private static List<ISemanticModel> models;
    private static Lang lang = Lang.en;
    
    private void parseTxtFilesAndConcatenate(String inputPath, String outputFile) {
        File inputFolder = new File(inputPath);
        try {
            PreProcessing task = new PreProcessing();
            task.parseTasa(inputFolder.getAbsolutePath(), outputFile, this.lang, false, 0, null);
            //task.parseGeneralCorpus(inputFolder.getAbsolutePath(), outputFile, this.lang, false, 0, null);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
    
    private void callHierarchicalClustering(String path, String encoding) {
        // process each file
        String line;
        List<AbstractDocument> responses = new ArrayList<>();
        int lineCounter = 0;

        try (BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(new File(path)), encoding))) {
            while ((line = in.readLine()) != null) {
                if (line.trim().length() > 0) {
                    Document d = new Document(AbstractDocumentTemplate.getDocumentModel(line), models, lang, true);
                    d.setTitleText((lineCounter + 1) + "");
                    CohesionGraph.buildCohesionGraph(d);
                    //KeywordModeling.determineKeywords(d);
                    if (d.getNoContentWords() >= 5) {
                        responses.add(d);
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

        LOGGER.info("Performing clustering ...");
        //performAglomerativeClustering(responses, new File(path).getParent() + "/agglomerative_clustering.txt");
        performKMeansClustering(responses, 3);

        LOGGER.info("Finished processing all files ...");
    }
    
    public static void main(String[] args) {

        HierarhicalClusteringPV hcPV = new HierarhicalClusteringPV();
        hcPV.parseTxtFilesAndConcatenate("resources/config/EN/TasaHClustering/train", "train.txt");
        LOGGER.info("Computed train.txt file for training model...");

        try {
            ParagraphVectorsModel.trainModel("resources/config/EN/TasaHClustering/train.txt");
        } catch (FileNotFoundException ex) {
            Exceptions.printStackTrace(ex);
        }
        LOGGER.info("Finished training model ...");
        
        String modelPath = new File("resources/config/EN/TasaHClustering").getAbsolutePath();
        models = new ArrayList<>();
        models.add(ParagraphVectorsModel.loadParagraphVectors(modelPath, Lang.en));
        LOGGER.info("Finished loading all files ...");
        
        hcPV.parseTxtFilesAndConcatenate("resources/config/EN/TasaHClustering/test", "test.txt");
        LOGGER.info("Computed test.txt file for testing model...");

        hcPV.callHierarchicalClustering("resources/config/EN/TasaHClustering/test.txt", "UTF-8");
    }

    @Override
    public double compareDocs(AbstractDocument d1, AbstractDocument d2) {
        double avg = 0;
        //avg = models.stream().map((model) -> model.getSimilarity(d1, d2)).reduce(avg, (accumulator, _item) -> accumulator + _item);
        for (ISemanticModel model : models) {
            avg += model.getSimilarity(d1, d2);
        }
        return avg / models.size();
        
        //return model.getSimilarity(d1, d2);
    }
}
