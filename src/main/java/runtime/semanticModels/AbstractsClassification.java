/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package runtime.semanticModels;

import data.AbstractDocument;
import data.AbstractDocumentTemplate;
import data.Lang;
import data.discourse.SemanticCohesion;
import data.document.Document;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.io.FilenameUtils;
import org.openide.util.Exceptions;
import services.semanticModels.ISemanticModel;
import services.semanticModels.LDA.LDA;
import services.semanticModels.LSA.LSA;
import services.semanticModels.SimilarityType;
import services.semanticModels.word2vec.Word2VecModel;

/**
 *
 * @author Gabriel Gutu <gabriel.gutu at cs.pub.ro>
 */
public class AbstractsClassification {

    private final String abstractsPath;
    private final String categoriesPath;
    private final List<ISemanticModel> semanticModels;
    private final Lang lang;
    private final boolean usePosTagging;
    private Map<String, String> categories;
    private Map<String, String> abstractsAnnotations; // manually annotated
    private Map<String, String> abstractsClassifications; // file name, category
    private Map<String, String> abstractsTexts; // file name, content

    private Map<String, AbstractDocument> documentsCategories;
    private Map<String, AbstractDocument> documentsAbstractsClassifications;
    private final List<SimilarityType> methods;

    public AbstractsClassification(String abstractsPath, String categoriesPath, List<ISemanticModel> semanticModels, Lang lang, boolean usePosTagging, List<SimilarityType> methods) {
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
                String fileExtension = FilenameUtils.getExtension(filePath.getFileName().toString());
                if (fileExtension.compareTo("txt") == 0) {
                    try {
                        int lineNumber = 0;
                        Files.lines(filePath).forEach((String line) -> {
                            String fileName = filePath.getFileName().toString();
                            String categoryLetter = fileName.substring(0, 1);
                            categories.put(categoryLetter, line);
                        });
                    } catch (IOException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            });
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    private void extractAbstracts() {
        abstractsAnnotations = new HashMap<>();
        abstractsClassifications = new HashMap<>();
        abstractsTexts = new HashMap<>();
        try {
            Files.walk(Paths.get(abstractsPath)).forEach((Path filePath) -> {
                String fileExtension = FilenameUtils.getExtension(filePath.getFileName().toString());
                if (fileExtension.compareTo("txt") == 0) {
                    try {
                        int lineNumber = 0;
                        List<String> lines = Files.readAllLines(filePath);
                        int k = 0;
                        for (String line : lines) {
                            k++;
                            if (k == 1) {
                                continue;
                            }
                            String fileName = filePath.getFileName().toString();
                            String categoryLetter = fileName.substring(0, 1);
                            abstractsAnnotations.put(filePath.getFileName().toString(), categoryLetter);
                            abstractsClassifications.put(filePath.getFileName().toString(), "Z");
                            abstractsTexts.put(filePath.getFileName().toString(), line);
                        };
                    } catch (IOException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            });
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    private void buildDocuments() {
        System.out.println("Building documents of categories");
        documentsCategories = new HashMap<>();
        for (Map.Entry<String, String> category : categories.entrySet()) {
            AbstractDocumentTemplate templateCategory = AbstractDocumentTemplate.getDocumentModel(category.getValue());
            AbstractDocument documentCategory = new Document(null, templateCategory, semanticModels, lang, usePosTagging);
            documentsCategories.put(category.getKey(), documentCategory);
        }

        System.out.println("Building documents of abstracts");
        documentsAbstractsClassifications = new HashMap<>();
        for (String abstractFile : abstractsClassifications.keySet()) {
            System.out.println("Building document for abstract " + abstractFile);
            AbstractDocumentTemplate templateAbstract = AbstractDocumentTemplate.getDocumentModel(abstractsTexts.get(abstractFile));
            AbstractDocument documentAbstract = new Document(null, templateAbstract, semanticModels, lang, usePosTagging);
            documentsAbstractsClassifications.put(abstractFile, documentAbstract);
        }
    }

    private void categorizeAbstracts() {

        Map<SimilarityType, Integer> matchedAnnotations = new HashMap<>();
        Map<SimilarityType, FileWriter> matchedFiles = new HashMap<>();
        // abstract text, sim type, category, score
        Map<String, Map<SimilarityType, Map<String, Double>>> similarityScores = new HashMap<>();
        Map<String, Map<String, Double>> cohesionScores = new HashMap<>();
        Integer cohesionMatchedAnnotations;
        FileWriter cohesionMatchedFile;
        System.out.println("Generating similarity score matrix...");
        for (String abstractFile : abstractsClassifications.keySet()) {
            similarityScores.put(abstractFile, new HashMap<>());
            for (SimilarityType method : methods) {
                similarityScores.get(abstractFile).put(method, new HashMap<>());
            }
            cohesionScores.put(abstractFile, new HashMap<>());
        }

        try {
            // generate output files - one for each semantic model
            System.out.println("Generating output files...");
            for (SimilarityType method : methods) {
                matchedAnnotations.put(method, 0);
                matchedFiles.put(method, new FileWriter(abstractsPath + "/" + method.getAcronym() + ".txt"));
            }
            cohesionMatchedAnnotations = 0;
            cohesionMatchedFile = new FileWriter(abstractsPath + "/cohesion.txt");

            // compute similarity scores
            System.out.println("Computing similarity scores...");
            int k = 0;
            for (String abstractFile : abstractsClassifications.keySet()) {
                k++;
                AbstractDocument documentAbstract = documentsAbstractsClassifications.get(abstractFile);
                for (SimilarityType method : methods) {
                    for (Map.Entry<String, String> category : categories.entrySet()) {
                        AbstractDocument documentCategory = documentsCategories.get(category.getKey());
                        SemanticCohesion sc = new SemanticCohesion(documentAbstract, documentCategory);
                        similarityScores.get(abstractFile).get(method).put(category.getKey(), sc.getSemanticSimilarities().get(method));
                        cohesionScores.get(abstractFile).put(category.getKey(), sc.getCohesion());
                    }
                }
                // gather the maximum of the similarity scores and print it to the file
                System.out.println("Gathering maximum similarity score for abstract no " + k + "...");
                for (SimilarityType method : methods) {
                    Map.Entry<String, Double> maxSimilarityScore = Collections.max(similarityScores.get(abstractFile).get(method).entrySet(), Map.Entry.comparingByValue());
                    System.out.println("Abstract [" + method.getAcronym() + "]: " + abstractsAnnotations.get(abstractFile) + " - " + maxSimilarityScore.getKey());
                    matchedFiles.get(method).write(abstractsAnnotations.get(abstractFile) + " - " + maxSimilarityScore.getKey());
                    if (maxSimilarityScore.getKey().compareTo(abstractsAnnotations.get(abstractFile)) == 0) {
                        matchedAnnotations.put(method, matchedAnnotations.get(method) + 1);
                    }
                }
                Map.Entry<String, Double> maxSimilarityScore = Collections.max(cohesionScores.get(abstractFile).entrySet(), Map.Entry.comparingByValue());
                System.out.println("Abstract [cohesion]: " + abstractsAnnotations.get(abstractFile) + " - " + maxSimilarityScore.getKey());
                cohesionMatchedFile.write(abstractsAnnotations.get(abstractFile) + " - " + maxSimilarityScore.getKey());
                if (maxSimilarityScore.getKey().compareTo(abstractsAnnotations.get(abstractFile)) == 0) {
                    cohesionMatchedAnnotations++;
                }
            }
            // print detection percentages to dedicated files for each semantic model and close the files
            System.out.println("Printing final detection percentage rates...");
            for (SimilarityType method : methods) {
                Double score = matchedAnnotations.get(method) * 1.0 / abstractsAnnotations.size();
                matchedFiles.get(method).write(score.toString());
                matchedFiles.get(method).close();
            }
            Double score = cohesionMatchedAnnotations * 1.0 / abstractsAnnotations.size();
            cohesionMatchedFile.write(score.toString());
            cohesionMatchedFile.close();
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    private void process() {
        System.out.println("Extracting categories...");
        extractCategories();
        //System.out.println(categoriesToString());
        //System.out.println("Number of categories: " + categories.size());
        System.out.println("Extracting abstracts...");
        extractAbstracts();
        //System.out.println(abstractsToString());
        //System.out.println("Number of abstracts: " + abstractsAnnotations.size());
        System.out.println("Building documents...");
        buildDocuments();
        System.out.println("Categorizing abstracts...");
        categorizeAbstracts();
    }

    private String categoriesToString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Categories:").append("\n");
        for (Map.Entry<String, String> entry : categories.entrySet()) {
            sb.append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
        }
        return sb.toString();
    }

    private String abstractsToString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Abstracts:").append("\n");
        for (Map.Entry<String, String> entry : abstractsAnnotations.entrySet()) {
            sb.append(entry.getKey()).append(": ").append(entry.getValue())
                    .append(", ").append(abstractsClassifications.get(entry.getKey()))
                    .append("\n");
        }
        return sb.toString();
    }

    public static void main(String args[]) {
        List<ISemanticModel> semanticModels = new ArrayList<>();
        Lang lang = Lang.en;
        semanticModels.add(LSA.loadLSA("resources/config/EN/LSA/TASA", lang));
        semanticModels.add(LDA.loadLDA("resources/config/EN/LDA/TASA", lang));
        semanticModels.add(Word2VecModel.loadWord2Vec("resources/config/EN/word2vec/TASA", lang));

        List<SimilarityType> methods = new ArrayList();
        methods.add(SimilarityType.LSA);
        methods.add(SimilarityType.LDA);
        methods.add(SimilarityType.LEACOCK_CHODOROW);
        methods.add(SimilarityType.WU_PALMER);
        methods.add(SimilarityType.PATH_SIM);
        methods.add(SimilarityType.WORD2VEC);

        AbstractsClassification ac = new AbstractsClassification("resources/in/SciCorefCorpus/abstracts", "resources/in/SciCorefCorpus/categories", semanticModels, lang, true, methods);
        ac.process();
    }

}
