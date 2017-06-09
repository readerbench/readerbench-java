/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package runtime.semanticModels;

import data.AbstractDocument;
import data.AbstractDocumentTemplate;
import data.Lang;
import data.Word;
import data.discourse.Keyword;
import data.discourse.SemanticCohesion;
import data.document.Document;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.FilenameUtils;
import org.openide.util.Exceptions;
import services.commons.Formatting;
import services.discourse.keywordMining.KeywordModeling;
import services.semanticModels.DocumentClustering;
import services.semanticModels.ISemanticModel;
import services.semanticModels.LDA.LDA;
import services.semanticModels.LSA.LSA;
import services.semanticModels.SimilarityType;
import webService.ReaderBenchServer;

/**
 *
 * @author Gabriel Gutu <gabriel.gutu at cs.pub.ro>
 */
public class KeywordsClassification {

    public static final Logger LOGGER = Logger.getLogger("");

    private final String abstractsPath;
    private final String categoriesPath;
    private final List<ISemanticModel> semanticModels;
    private final Lang lang;
    private final boolean usePosTagging;
    private Map<String, String> categories;
    private Map<String, String> articlesAnnotations; // manually annotated
    private Map<String, String> articlesClassifications; // file name, category
    private Map<String, String> articlesTexts; // file name, content

    private final List<String> acceptedKeywords;

    private Map<String, AbstractDocument> documentsCategories;
    private Map<String, AbstractDocument> documentsArticlesClassifications;

    private Map<String, AbstractDocument> documentKeywordsArticlesClassifications;
    private final List<SimilarityType> methods;

    public KeywordsClassification(String abstractsPath, String categoriesPath, List<String> acceptedKeywords, List<ISemanticModel> semanticModels, Lang lang, boolean usePosTagging, List<SimilarityType> methods) {
        this.abstractsPath = abstractsPath;
        this.categoriesPath = categoriesPath;
        this.acceptedKeywords = acceptedKeywords;
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

    private void extractArticles(boolean ignoreArticlesFirstLine) {
        articlesAnnotations = new HashMap<>();
        articlesClassifications = new HashMap<>();
        articlesTexts = new HashMap<>();
        try {
            Files.walk(Paths.get(abstractsPath)).forEach((Path filePath) -> {
                String fileExtension = FilenameUtils.getExtension(filePath.getFileName().toString());
                if (fileExtension.compareTo("txt") == 0) {
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
                        Exceptions.printStackTrace(ex);
                    }
                }
            });
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    private void buildCategoriesDocuments(boolean useSerialized) {
        LOGGER.info("Building documents of categories");
        documentsCategories = new HashMap<>();
        if (useSerialized) {
            File dir = new File(categoriesPath);
            if (!dir.exists()) {
                throw new RuntimeException("Inexistent Folder: " + dir.getPath());
            }
            File[] files = dir.listFiles((File pathname) -> {
                return pathname.getName().toLowerCase().endsWith(".ser");
            });
            for (File file : files) {
                Document d = null;
                try {
                    d = (Document) AbstractDocument.loadSerializedDocument(file.getPath());
                    documentsCategories.put(file.getName().substring(0, 1), d);
                } catch (IOException | ClassNotFoundException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        } else {
            for (Map.Entry<String, String> category : categories.entrySet()) {
                AbstractDocumentTemplate templateCategory = AbstractDocumentTemplate.getDocumentModel(category.getValue());
                AbstractDocument documentCategory = new Document(category.getKey(), templateCategory, semanticModels, lang, usePosTagging);
                documentCategory.saveSerializedDocument();
                documentCategory.save(AbstractDocument.SaveType.SERIALIZED);
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
            LOGGER.log(Level.INFO, "Building document for article {0} (" + percentage + "%" + ")", abstractFile);
            AbstractDocumentTemplate templateAbstract = AbstractDocumentTemplate.getDocumentModel(articlesTexts.get(abstractFile));
            AbstractDocument documentAbstract = new Document(abstractFile, templateAbstract, semanticModels, lang, usePosTagging);
            //documentAbstract.computeAll(false);
            KeywordModeling.determineKeywords(documentAbstract);
            documentsArticlesClassifications.put(abstractFile, documentAbstract);
        }
    }

    private void buildKeywordsDocuments(boolean keepOnlyAcceptedKeywords) {
        LOGGER.info("Building documents of keywords");
        documentKeywordsArticlesClassifications = new HashMap<>();
        double percentage;
        int index = 0;
        for (String abstractFile : articlesClassifications.keySet()) {
            percentage = index++ * 1.0 / articlesClassifications.size() * 100;
            LOGGER.log(Level.INFO, "Building keywords document for article {0} (" + percentage + "%" + ")", abstractFile);
            StringBuilder sb = new StringBuilder();
            List<Keyword> keywords = documentsArticlesClassifications.get(abstractFile).getTopics();

            for (Keyword keyword : keywords) {
                // add here condition to include if only in relevant keywords list
                if (keyword.getElement() instanceof Word) {
                    if (keepOnlyAcceptedKeywords) {
                        if (this.acceptedKeywords.contains(keyword.getWord().getLemma())) {
                            sb.append(keyword.getWord().toString()).append(" ");
                        }
                    } else {
                        sb.append(keyword.getWord().toString()).append(" ");
                    }
                }

            }

            AbstractDocumentTemplate templateKeywords = AbstractDocumentTemplate.getDocumentModel(sb.toString());
            AbstractDocument documentKeywords = new Document(abstractFile, templateKeywords, semanticModels, lang, usePosTagging);
            documentKeywordsArticlesClassifications.put(abstractFile, documentKeywords);
        }
        for (String abstractFile : articlesClassifications.keySet()) {
            LOGGER.log(Level.INFO, "Keywords for {0}:{1}", new Object[]{abstractFile, documentKeywordsArticlesClassifications.get(abstractFile).getText()});
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
                AbstractDocument documentKeywords = documentKeywordsArticlesClassifications.get(abstractFile);
                for (SimilarityType method : methods) {
                    for (Map.Entry<String, String> category : categories.entrySet()) {
                        AbstractDocument documentCategory = documentsCategories.get(category.getKey());
                        SemanticCohesion sc = new SemanticCohesion(documentKeywords, documentCategory);
                        similarityScores.get(abstractFile).get(method).put(category.getKey(), sc.getSemanticSimilarities().get(method));
                        cohesionScores.get(abstractFile).put(category.getKey(), sc.getCohesion());
                    }
                }
                // gather the maximum of the similarity scores and print it to the file
                LOGGER.log(Level.INFO, "Gathering maximum similarity score for abstract no {0}...", k);
                for (SimilarityType method : methods) {
                    Map.Entry<String, Double> maxSimilarityScore = Collections.max(similarityScores.get(abstractFile).get(method).entrySet(), Map.Entry.comparingByValue());
                    LOGGER.log(Level.INFO, "Abstract [{0}]: {1} - {2}", new Object[]{method.getAcronym(), articlesAnnotations.get(abstractFile), maxSimilarityScore.getKey()});
                    matchedFiles.get(method).write("(" + articlesAnnotations.get(abstractFile) + ", " + maxSimilarityScore.getKey() + ") " + ((maxSimilarityScore.getKey().compareTo(articlesAnnotations.get(abstractFile)) == 0) ? "1" : "0") + "\n");
                    if (maxSimilarityScore.getKey().compareTo(articlesAnnotations.get(abstractFile)) == 0) {
                        matchedAnnotations.put(method, matchedAnnotations.get(method) + 1);
                    }
                }
                Map.Entry<String, Double> maxSimilarityScore = Collections.max(cohesionScores.get(abstractFile).entrySet(), Map.Entry.comparingByValue());
                LOGGER.log(Level.INFO, "Abstract [cohesion]: {0} - {1}", new Object[]{articlesAnnotations.get(abstractFile), maxSimilarityScore.getKey()});
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
            Exceptions.printStackTrace(ex);
        }
    }

    private void clusterizeArticles(int noCats) {

        Map<ISemanticModel, FileWriter> clusterFiles = new HashMap<>();
        Map<ISemanticModel, DocumentClustering> dc = new HashMap<>();

        try {
            // generate output files - one for each semantic model
            LOGGER.info("Generating output files...");
            List<AbstractDocument> docs = new ArrayList<>(documentKeywordsArticlesClassifications.values());
            LOGGER.log(Level.INFO, "{0} docs available", docs.size());
            LOGGER.log(Level.INFO, "{0} semantic models", semanticModels.size());
            for (ISemanticModel model : semanticModels) {
                LOGGER.log(Level.INFO, "Creating file for {0}", model.getType().getAcronym());
                clusterFiles.put(model, new FileWriter(abstractsPath + "/" + model.getType().getAcronym() + "_keywords.cluster"));
                dc.put(model, new DocumentClustering(model));
                LOGGER.log(Level.INFO, "Performing clustering for {0}", model.getType().getAcronym());
                dc.get(model).performKMeansClustering(docs, noCats);
                LOGGER.log(Level.INFO, "Found {0} clustroids", dc.get(model).getClustroids().size());

                LOGGER.log(Level.INFO, "Printing clusters to file {0}", model.getType().getAcronym());
                for (int i = 0; i < dc.get(model).getClustroids().size(); i++) {
                    LOGGER.log(Level.INFO, "Printing cluster {0}", (i + 1));
                    clusterFiles.get(model).write("Cluster " + (i + 1) + "\n");
                    for (AbstractDocument d : dc.get(model).getClusters().get(i)) {
                        LOGGER.log(Level.INFO, "Checking whether document {0} is clustroid.", d.getPath());
                        if (dc.get(model).getClustroids().contains(d)) {
                            clusterFiles.get(model).write("(" + d.getPath() + ")\n");
                        } else {
                            clusterFiles.get(model).write(d.getPath() + "\n");
                        }
                    }
                }
                clusterFiles.get(model).close();
            }
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
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
                    .append(documentKeywordsArticlesClassifications.get(entry.getKey()).getText())
                    .append("\n");
        }
        return sb.toString();
    }

    public static void main(String args[]) {
        ReaderBenchServer.initializeDB();

        List<ISemanticModel> semanticModels = new ArrayList<>();
        Lang lang = Lang.en;
        semanticModels.add(LSA.loadLSA("resources/config/EN/LSA/SciRef", lang));
        //semanticModels.add(LDA.loadLDA("resources/config/EN/LDA/SciRef", lang));
        //semanticModels.add(Word2VecModel.loadWord2Vec("resources/config/EN/word2vec/TASA", lang));

        List<SimilarityType> methods = new ArrayList();
        methods.add(SimilarityType.LSA);
//        methods.add(SimilarityType.LDA);
//        methods.add(SimilarityType.LEACOCK_CHODOROW);
//        methods.add(SimilarityType.WU_PALMER);
//        methods.add(SimilarityType.PATH_SIM);
        //methods.add(SimilarityType.WORD2VEC);

        boolean ignoreArticlesFirstLine = false;

        String keywordsString = "supply, reserve, clean, hybrid, clickthrough, miss, browse, regard, insert, multicast, shift, relax, overload, stream, quantify, delete, primitive, facilitate, degrade, leverage, virtual, conflict, attack, click, middleware, finish, appeal, secure, multiagent, preserve, forward, unify, substitute, ignore, stop, subject, switch, embed, keyword, wireless, span, academic, remark, sound, randomise, testbed, transmit, hash, waste, judge, decentralize, cache, break, fall, digital, dataset, queue, reverse, encounter, fine, shape, electronic, reward, project, collaborative, spend, american, sensor, impose, operate, combinatorial, bear, advertise, remote, hide, discount, trigger, economic, boolean, client, deep, augment, square, encourage, copy, adopt, complicate, double, correlate, analyse, automate, face, concentrate, contract, generalize, tune, engineer, retrieve, artificial, incentive, spread, enhance, intelligent, display, disjoint, default, guide, host, benchmark, delay, carry, favor, bias, representative, mobile, physical, retrieval, chain, encode, scalable, risk, refine, plot, equilibrium, block, national, survey, communicate, mix, trust, buy, middle, promise, cooperative, evidence, force, slow, briefly, peer, auction, verify, gather, discrete, concern, novel, center, outline, split, hope, comment, route, normalise, empty, neighbor, polynomial, surprise, monitor, coordinate, reflect, centralize, strategic, autonomous, simplify, intuitively, sample, flow, year, overhead, schedule, half, advance, max, statistical, characteristic, motivate, personal, score, filter, root, drive, sell, cluster, integrate, experience, probabilistic, transfer, overlap, wish, parallel, particularly, intend, sort, execute, speed, semantic, modify, adaptive, bring, abstract, exhibit, claim, wait, meet, exchange, demand, care, clearly, optimize, simulate, adjust, attribute, increment, extract, arise, simultaneously, realistic, balance, popular, seem, essentially, namely, player, consideration, record, fundamental, keep, presence, attention, social, potentially, public, past, complement, initially, submit, possibility, aggregate, internet, discover, notice, straightforward, theoretical, perspective, effectively, perfect, variant, pass, think, grow, exponential, live, obvious, conduct, bid, upper, distinguish, basis, specifically, factor, human, review, suppose, extreme, challenge, proceed, automatic, drop, robust, characterize, market, post, empirical, incorporate, interval, logical, arbitrary, request, author, label, document, answer, practical, early, minimize, possibly, distinct, quite, continue, message, output, completely, topic, target, recently, influence, literature, protocol, previously, online, objective, manner, underlie, nature, little, press, fast, connect, whole, kind, store, slightly, rank, meaning, relatively, efficiently, reveal, detect, free, understand, explain, establish, investigate, generally, content, theorem, explicitly, examine, number, load, reasonable, draw, especially, handle, highly, practice, check, explore, treat, procedure, impact, minimum, course, uniform, threshold, repeat, desire, equivalent, lack, calculate, technical, necessarily, yield, discussion, exploit, proof, accurate, relationship, bound, recall, enable, locate, proceeding, capture, international, setting, track, write, aspect, employ, game, major, index, typically, central, believe, currently, manage, similarly, access, accept, purpose, mention, comparison, vary, price, guarantee, sense, service, entire, exactly, account, requirement, separate, rule, illustrate, trade, file, theory, fail, combine, actually, layer, link"; // add keywords string here
        List<String> keywords = Arrays.asList(keywordsString.split("\\s*,\\s*"));

        KeywordsClassification ac = new KeywordsClassification("resources/in/SciCorefCorpus/fulltexts", "resources/in/SciCorefCorpus/categories", keywords, semanticModels, lang, true, methods);
        
        // build categories documents
        LOGGER.info("Extracting categories...");
        ac.extractCategories();
        LOGGER.info("Building categories documents...");
        ac.buildCategoriesDocuments(false);
        LOGGER.info(ac.categoriesToString());
        LOGGER.log(Level.INFO, "Number of categories: {0}", ac.categories.size());
        
        // build articles documents
        LOGGER.info("Extracting articles...");
        ac.extractArticles(ignoreArticlesFirstLine);
        LOGGER.info("Building articles documents...");
        ac.buildArticlesDocuments();
        LOGGER.info(ac.articlesToString());
        LOGGER.log(Level.INFO, "Number of articles: {0}", ac.articlesAnnotations.size());
        
        // build keywords documents
        LOGGER.info("Determining keywords...");
        ac.buildKeywordsDocuments(false);
        LOGGER.info(ac.keywordsToString());
        LOGGER.log(Level.INFO, "Number of keywords articles: {0}", ac.documentKeywordsArticlesClassifications.size());
        
//        LOGGER.info("Categorizing abstracts...");
//        ac.categorizeArticles();
        ac.clusterizeArticles(4);
    }

}
