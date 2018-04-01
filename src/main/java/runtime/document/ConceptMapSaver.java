/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package runtime.document;

import data.AbstractDocument;
import data.Lang;
import data.document.Document;
import data.document.MetaDocument;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.util.Exceptions;
import services.semanticModels.ISemanticModel;
import services.semanticModels.LDA.LDA;
import services.semanticModels.LSA.LSA;
import webService.ReaderBenchServer;
import webService.queryResult.QueryResultTopic;
import webService.result.ResultNode;
import webService.result.ResultTopic;
import webService.services.ConceptMap;

/**
 *
 * @author Gabriel Gutu <gabriel.gutu at cs.pub.ro>
 */
public class ConceptMapSaver {

    static final Logger LOGGER = Logger.getLogger("");

    private final String processingPath;
    private final LSA lsa;
    private final LDA lda;
    private final Lang lang;
    private final boolean usePOSTagging;
    private final boolean computeDialogism;
    private final double threshold;
    private final boolean meta;

    public ConceptMapSaver(String processingPath, LSA lsa, LDA lda, Lang lang, boolean usePOSTagging, boolean computeDialogism, double threshold, boolean meta) {
        this.processingPath = processingPath;
        this.lsa = lsa;
        this.lda = lda;
        this.lang = lang;
        this.usePOSTagging = usePOSTagging;
        this.computeDialogism = computeDialogism;
        this.threshold = threshold;
        this.meta = meta;
    }

    private void processTexts(boolean useSerialized, boolean saveCsv, boolean saveJson) {
        if (saveCsv == saveJson == false) return;
        File dir = new File(processingPath);

        if (!dir.exists()) {
            throw new RuntimeException("Inexistent Folder: " + dir.getPath());
        }

        List<Document> documents = new ArrayList<>();
        if (useSerialized) {
            File[] files = dir.listFiles((File pathname) -> {
                return pathname.getName().toLowerCase().endsWith(".ser");
            });

            for (File file : files) {
                Document d = null;
                try {
                    d = (Document) AbstractDocument.loadSerializedDocument(file.getPath());
                    documents.add(d);
                    d.exportDocument();
                } catch (IOException | ClassNotFoundException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        } else {
            File[] files = dir.listFiles((File pathname) -> {
                return pathname.getName().toLowerCase().endsWith(".xml");
            });
            List<ISemanticModel> models = new ArrayList<>();
            models.add(lsa);
            models.add(lda);

            for (File file : files) {
                LOGGER.log(Level.INFO, "Processing {0} file", file.getName());
                // Create file

                Document d;
                try {
                    if (meta) {
                        d = MetaDocument.load(file, models, lang, usePOSTagging, MetaDocument.DocumentLevel.Subsection, 5);
                    } else {
                        d = Document.load(file, models, lang, usePOSTagging);
                    }
                    d.computeAll(computeDialogism);
                    d.save(AbstractDocument.SaveType.SERIALIZED_AND_CSV_EXPORT);
                    documents.add(d);
                } catch (Exception e) {
                    LOGGER.log(Level.SEVERE, "Runtime error while processing {0}: {1} ...", new Object[]{file.getName(), e.getMessage()});
                    Exceptions.printStackTrace(e);
                }
            }
        }

        for (Document d : documents) {
            try {
                ResultTopic resultTopic = ConceptMap.getKeywords(d, threshold, null);
                if (saveCsv) {
                    BufferedWriter outCsv = new BufferedWriter(new FileWriter(d.getPath() + "_concepts.csv"));
                    StringBuilder sb = new StringBuilder();
                    for (ResultNode node : resultTopic.getNodes()) {
                        sb.append(node.getName()).append(',').append(node.getValue()).append(',');
                    }
                    outCsv.write(sb.toString());
                    outCsv.close();
                }

                if (saveJson) {
                    QueryResultTopic queryResult = new QueryResultTopic();
                    queryResult.setData(resultTopic);
                    BufferedWriter outJson = new BufferedWriter(new FileWriter(d.getPath() + "_response.json"));
                    outJson.write(queryResult.convertToJson());
                    outJson.close();
                }
            } catch (IOException ex) {
                LOGGER.log(Level.SEVERE, "Runtime error while initializing {0} concept map file", d.getPath());
                Exceptions.printStackTrace(ex);
            }
        }
    }

    public static void main(String[] args) {
        ReaderBenchServer.initializeDB();

        LSA lsa = LSA.loadLSA("resources/config/EN/LSA/TASA", Lang.en);
        LDA lda = LDA.loadLDA("resources/config/EN/LDA/TASA", Lang.en);
        ConceptMapSaver cmj = new ConceptMapSaver("resources/in/tasa_search_en", lsa, lda, Lang.en, true, true, 0.3, false);
        cmj.processTexts(false, false, false);
    }

}
