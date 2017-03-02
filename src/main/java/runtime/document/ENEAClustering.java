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
import java.util.logging.Logger;
import org.openide.util.Exceptions;
import services.commons.Clustering;
import services.discourse.cohesion.CohesionGraph;
import services.discourse.keywordMining.KeywordModeling;
import services.semanticModels.ISemanticModel;
import services.semanticModels.LDA.LDA;
import services.semanticModels.LSA.LSA;

/**
 *
 * @author mihaidascalu
 */
public class ENEAClustering extends Clustering {

    public static final Logger LOGGER = Logger.getLogger("");

    private final List<ISemanticModel> models;
    private final Lang lang;

    public ENEAClustering(List<ISemanticModel> models, Lang lang) {
        this.models = models;
        this.lang = lang;
    }

    public void parseTxtFile(String path, String encoding) {
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
                    KeywordModeling.determineKeywords(d);
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
        performAglomerativeClustering(responses, new File(path).getParent() + "/agglomerative_clustering.txt");

        LOGGER.info("Finished processing all files ...");
    }

    @Override
    public double compareDocs(AbstractDocument d1, AbstractDocument d2) {
        double avg = 0;
        for (ISemanticModel model : models) {
            avg += model.getSimilarity(d1, d2);
        }
        return avg / models.size();
    }

    public static void main(String[] args) {
        List<ISemanticModel> models = new ArrayList<>();
        models.add(LSA.loadLSA("resources/config/EN/LSA/ENEA_TASA", Lang.en));
        models.add(LDA.loadLDA("resources/config/EN/LDA/ENEA_TASA", Lang.en));

        ENEAClustering clustering = new ENEAClustering(models, Lang.en);
        clustering.parseTxtFile("resources/in/ENEA/responses.txt", "UTF-8");
    }
}
