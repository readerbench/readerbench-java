package com.readerbench.coreservices.keywordmining;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.readerbench.coreservices.data.AbstractDocument;
import com.readerbench.coreservices.data.AbstractDocumentTemplate;
import com.readerbench.coreservices.data.discourse.SemanticCohesion;
import com.readerbench.coreservices.data.document.Document;
import com.readerbench.coreservices.semanticmodels.DocumentClustering;
import com.readerbench.coreservices.semanticmodels.SemanticModel;
import com.readerbench.coreservices.semanticmodels.SimilarityType;
import com.readerbench.datasourceprovider.commons.Formatting;
import com.readerbench.datasourceprovider.pojo.Lang;
import com.readerbench.processingservice.document.DocumentProcessingPipeline;

public class KeywordsClassification {

    private static final Logger LOGGER = LoggerFactory.getLogger(KeywordsClassification.class);

    private final String abstractsPath;
    private final String categoriesPath;
    private final List<SemanticModel> semanticModels;
    private final Lang lang;
    private final boolean usePosTagging;
    private Map<String, String> categories;
    private Map<String, String> articlesAnnotations; // manually annotated
    private Map<String, String> articlesClassifications; // file name, category
    private Map<String, String> articlesTexts; // file name, content

    private Map<String, AbstractDocument> documentsCategories;
    private Map<String, AbstractDocument> documentsArticlesClassifications;

    private final List<SimilarityType> methods;

    public KeywordsClassification(String abstractsPath, String categoriesPath, List<SemanticModel> semanticModels, Lang lang, boolean usePosTagging, List<SimilarityType> methods) {
        this.abstractsPath = abstractsPath;
        this.categoriesPath = categoriesPath;
        this.semanticModels = semanticModels;
        this.lang = lang;
        this.usePosTagging = usePosTagging;
        this.methods = methods;
    }

    private void extractCategories() {
        categories = new HashMap<>();
        try {
            Files.walk(Paths.get(categoriesPath)).forEach((Path filePath) -> {
                //String fileExtension = FilenameUtils.getExtension(filePath.getFileName().toString());
                if (filePath.getFileName().toString().endsWith(".txt")) {
                    try {
                        Files.lines(filePath).forEach((String line) -> {
                            String fileName = filePath.getFileName().toString();
                            String categoryLetter = fileName.substring(0, 1);
                            categories.put(categoryLetter, line);
                        });
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
            });
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void extractArticles(boolean ignoreArticlesFirstLine) {
        articlesAnnotations = new HashMap<>();
        articlesClassifications = new HashMap<>();
        articlesTexts = new HashMap<>();
        try {
            Files.walk(Paths.get(abstractsPath)).forEach((Path filePath) -> {
//                String fileExtension = FilenameUtils.getExtension(filePath.getFileName().toString());
                if (filePath.getFileName().toString().endsWith(".txt")) {
                    try {
                        List<String> lines = Files.readAllLines(filePath);
                        int k = 0;
                        StringBuilder sb = new StringBuilder();
                        for (String line : lines) {
                            k++;
                            if (ignoreArticlesFirstLine && k == 1) {
                                continue;
                            }
                            sb.append(line).append("\n");
                        }
                        String fileName = filePath.getFileName().toString();
                        String categoryLetter = fileName.substring(0, 1);
                        articlesAnnotations.put(filePath.getFileName().toString(), categoryLetter);
                        articlesClassifications.put(filePath.getFileName().toString(), "Z");
                        articlesTexts.put(filePath.getFileName().toString(), sb.toString());
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
            });
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void buildCategoriesDocuments(boolean useSerialized) {
        LOGGER.info("Building documents of categories");
        documentsCategories = new HashMap<>();
        if (useSerialized) {
        	LOGGER.error("Use serialized not supported");
        	return;
//            File dir = new File(categoriesPath);
//            if (!dir.exists()) {
//                throw new RuntimeException("Inexistent Folder: " + dir.getPath());
//            }
//            File[] files = dir.listFiles((File pathname) -> {
//                return pathname.getName().toLowerCase().endsWith(".ser");
//            });
//            for (File file : files) {
//                Document d = null;
//                try {
//                    d = (Document) AbstractDocument.loadSerializedDocument(file.getPath());
//                    documentsCategories.put(file.getName().substring(0, 1), d);
//                } catch (IOException | ClassNotFoundException ex) {
//                    ex.printStackTrace();
//                }
//            }
        } else {
            for (Map.Entry<String, String> category : categories.entrySet()) {
                AbstractDocumentTemplate templateCategory = AbstractDocumentTemplate.getDocumentModel(category.getValue());
                DocumentProcessingPipeline dpp = new DocumentProcessingPipeline(lang, semanticModels, new ArrayList<>());
                AbstractDocument documentCategory = dpp.createDocumentFromTemplate(templateCategory);
                documentsCategories.put(category.getKey(), documentCategory);
            }
        }
    }

    private void buildArticlesDocuments() {
        LOGGER.info("Building documents of abstracts");
        documentsArticlesClassifications = new HashMap<>();
        double percentage;
        int index = 0;
        for (String abstractFile : articlesClassifications.keySet()) {
            percentage = index++ * 1.0 / articlesClassifications.size() * 100;
            //LOGGER.info("Building document for article {} (" + percentage + "%" + ")", abstractFile);
            AbstractDocumentTemplate templateAbstract = AbstractDocumentTemplate.getDocumentModel(articlesTexts.get(abstractFile));
            DocumentProcessingPipeline dpp = new DocumentProcessingPipeline(lang, semanticModels, new ArrayList<>());
            AbstractDocument documentAbstract = dpp.createDocumentFromTemplate(templateAbstract);
            documentAbstract.setTitleText(abstractFile);
            documentsArticlesClassifications.put(abstractFile, documentAbstract);
        }
    }

    private void categorizeArticles() {

        Map<SimilarityType, Integer> matchedAnnotations = new HashMap<>();
        Map<SimilarityType, FileWriter> matchedFiles = new HashMap<>();
        // abstract text, sim type, category, score
        Map<String, Map<SimilarityType, Map<String, Double>>> similarityScores = new HashMap<>();
        Map<String, Map<String, Double>> cohesionScores = new HashMap<>();
        Integer cohesionMatchedAnnotations;
        FileWriter cohesionMatchedFile;
        LOGGER.info("Generating similarity score matrix...");
        for (String abstractFile : articlesClassifications.keySet()) {
            similarityScores.put(abstractFile, new HashMap<>());
            for (SimilarityType method : methods) {
                similarityScores.get(abstractFile).put(method, new HashMap<>());
            }
            cohesionScores.put(abstractFile, new HashMap<>());
        }

        try {
            // generate output files - one for each semantic model
            LOGGER.info("Generating output files...");
            for (SimilarityType method : methods) {
                matchedAnnotations.put(method, 0);
                matchedFiles.put(method, new FileWriter(abstractsPath + "/" + method.getAcronym() + ".out"));
            }
            cohesionMatchedAnnotations = 0;
            cohesionMatchedFile = new FileWriter(abstractsPath + "/cohesion.out");

            // compute similarity scores
            LOGGER.info("Computing similarity scores...");
            int k = 0;
            for (String abstractFile : articlesClassifications.keySet()) {
                k++;
                AbstractDocument documentKeywords = documentsArticlesClassifications.get(abstractFile);
                for (SimilarityType method : methods) {
                    for (Map.Entry<String, String> category : categories.entrySet()) {
                        AbstractDocument documentCategory = documentsCategories.get(category.getKey());
                        SemanticCohesion sc = new SemanticCohesion(documentKeywords, documentCategory);
                        similarityScores.get(abstractFile).get(method).put(category.getKey(), sc.getSemanticSimilarities().get(method));
                        cohesionScores.get(abstractFile).put(category.getKey(), sc.getCohesion());
                    }
                }
                // gather the maximum of the similarity scores and print it to the file
                LOGGER.info("Gathering maximum similarity score for abstract no {}...", k);
                for (SimilarityType method : methods) {
                    Map.Entry<String, Double> maxSimilarityScore = Collections.max(similarityScores.get(abstractFile).get(method).entrySet(), Map.Entry.comparingByValue());
                    LOGGER.info("Abstract [{}]: {} - {}", new Object[]{method.getAcronym(), articlesAnnotations.get(abstractFile), maxSimilarityScore.getKey()});
                    matchedFiles.get(method).write("(" + articlesAnnotations.get(abstractFile) + ", " + maxSimilarityScore.getKey() + ") " + ((maxSimilarityScore.getKey().compareTo(articlesAnnotations.get(abstractFile)) == 0) ? "1" : "0") + "\n");
                    if (maxSimilarityScore.getKey().compareTo(articlesAnnotations.get(abstractFile)) == 0) {
                        matchedAnnotations.put(method, matchedAnnotations.get(method) + 1);
                    }
                }
                Map.Entry<String, Double> maxSimilarityScore = Collections.max(cohesionScores.get(abstractFile).entrySet(), Map.Entry.comparingByValue());
                LOGGER.info("Abstract [cohesion]: {} - {}", new Object[]{articlesAnnotations.get(abstractFile), maxSimilarityScore.getKey()});
                cohesionMatchedFile.write("(" + articlesAnnotations.get(abstractFile) + ", " + maxSimilarityScore.getKey() + ") " + ((maxSimilarityScore.getKey().compareTo(articlesAnnotations.get(abstractFile)) == 0) ? "1" : "0") + "\n");
                if (maxSimilarityScore.getKey().compareTo(articlesAnnotations.get(abstractFile)) == 0) {
                    cohesionMatchedAnnotations++;
                }
            }
            // print detection percentages to dedicated files for each semantic model and close the files
            LOGGER.info("Printing final detection percentage rates...");
            for (SimilarityType method : methods) {
                Double score = matchedAnnotations.get(method) * 1.0 / articlesAnnotations.size();
                matchedFiles.get(method).write("Total matched: " + matchedAnnotations.get(method) + " of " + articlesAnnotations.size() + "\n");
                matchedFiles.get(method).write("Detection percentage: " + Formatting.formatNumber(score) + "\n");
                matchedFiles.get(method).close();
            }
            Double score = cohesionMatchedAnnotations * 1.0 / articlesAnnotations.size();
            cohesionMatchedFile.write("Total matched: " + cohesionMatchedAnnotations + " of " + articlesAnnotations.size() + "\n");
            cohesionMatchedFile.write("Detection percentage: " + Formatting.formatNumber(score) + "\n");
            cohesionMatchedFile.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void clusterizeArticles(int noCats) {

        Map<SemanticModel, FileWriter> clusterFiles = new HashMap<>();
        Map<SemanticModel, DocumentClustering> dc = new HashMap<>();

        try {
            // generate output files - one for each semantic model
            LOGGER.info("Generating output files...");
            List<AbstractDocument> docs = new ArrayList<>(documentsArticlesClassifications.values());
            LOGGER.info("{} docs available", docs.size());
            LOGGER.info("{} semantic models", semanticModels.size());
            for (SemanticModel model : semanticModels) {
                LOGGER.info("Creating file for {}", model.getName());
                clusterFiles.put(model, new FileWriter(abstractsPath + "/" + model.getName() + ".cluster"));
                dc.put(model, new DocumentClustering(model));
                LOGGER.info("Performing clustering for {}", model.getName());
                dc.get(model).performKMeansClustering(docs, noCats);
                LOGGER.info("Found {} clustroids", dc.get(model).getClustroids().size());

                LOGGER.info("Printing clusters to file {}", model.getName());
                for (int i = 0; i < dc.get(model).getClustroids().size(); i++) {
                    LOGGER.info("Printing cluster {}", (i + 1));
                    clusterFiles.get(model).write("Cluster " + (i + 1) + "\n");
                    for (AbstractDocument d : dc.get(model).getClusters().get(i)) {
                        LOGGER.info("Checking whether document {} is clustroid.", d.getTitleText());
                        if (dc.get(model).getClustroids().contains(d)) {
                            clusterFiles.get(model).write("(" + d.getTitleText() + ")\n");
                        } else {
                            clusterFiles.get(model).write(d.getTitleText() + "\n");
                        }
                    }
                }
                clusterFiles.get(model).close();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private String categoriesToString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Categories:").append("\n");
        for (Map.Entry<String, String> entry : categories.entrySet()) {
            sb.append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
        }
        return sb.toString();
    }

    private String articlesToString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Abstracts:").append("\n");
        for (Map.Entry<String, String> entry : articlesAnnotations.entrySet()) {
            sb.append(entry.getKey()).append(": ").append(entry.getValue())
                    .append(", ").append(articlesClassifications.get(entry.getKey()))
                    .append("\n");
        }
        return sb.toString();
    }

    private String keywordsToString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Abstracts:").append("\n");
        for (Map.Entry<String, String> entry : articlesClassifications.entrySet()) {
            sb.append(entry.getKey()).append(": ")
                    .append(documentsArticlesClassifications.get(entry.getKey()).getText())
                    .append("\n");
        }
        return sb.toString();
    }

    public static void main(String args[]) {
        //SQLiteDatabase.initializeDB();

        List<SemanticModel> semanticModels = new ArrayList<>();
        Lang lang = Lang.en;
        SemanticModel lsa = SemanticModel.loadModel("TASA", Lang.en, SimilarityType.LSA);
        //SemanticModel lda = SemanticModel.loadModel("TASA", Lang.en, SimilarityType.LDA);
        semanticModels.add(lsa);
        //semanticModels.add(lda);
        //semanticModels.add(LDA.loadLDA("resources/config/EN/LDA/SciRef", lang));
        //semanticModels.add(Word2VecModel.loadWord2Vec("resources/config/EN/word2vec/TASA", lang));

        List<SimilarityType> methods = new ArrayList<>();
        methods.add(SimilarityType.LSA);
        //methods.add(SimilarityType.LDA);
//        methods.add(SimilarityType.LDA);
//        methods.add(SimilarityType.LEACOCK_CHODOROW);
//        methods.add(SimilarityType.WU_PALMER);
//        methods.add(SimilarityType.PATH_SIM);
        //methods.add(SimilarityType.WORD2VEC);

        boolean ignoreArticlesFirstLine = false;

        KeywordsClassification ac = new KeywordsClassification("keywords", "categories", semanticModels, lang, true, methods);

        // build categories documents
//        LOGGER.info("Extracting categories...");
//        ac.extractCategories();
//        LOGGER.info("Building categories documents...");
//        ac.buildCategoriesDocuments(false);
//        LOGGER.info(ac.categoriesToString());
//        LOGGER.info("Number of categories: {}", ac.categories.size());

        // build articles documents
        LOGGER.info("Extracting articles...");
        ac.extractArticles(ignoreArticlesFirstLine);
        LOGGER.info("Building articles documents...");
        ac.buildArticlesDocuments();
        LOGGER.info(ac.articlesToString());
        LOGGER.info("Number of articles: {}", ac.articlesAnnotations.size());

//        LOGGER.info("Categorizing abstracts...");
//        ac.categorizeArticles();
        ac.clusterizeArticles(4);
        //ac.categorizeArticles();
    }

}

