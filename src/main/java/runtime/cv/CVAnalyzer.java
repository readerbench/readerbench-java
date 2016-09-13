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
package runtime.cv;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;

import data.AbstractDocument;
import data.Lang;
import data.sentiment.SentimentValence;
import java.io.IOException;
import java.util.HashMap;
import org.junit.Test;
import org.openide.util.Exceptions;
import services.commons.Formatting;
import services.complexity.ComplexityIndex;
import services.complexity.ComplexityIndexType;
import services.converters.PdfToTextConverter;
import webService.ReaderBenchServer;
import webService.cv.CVHelper;
import webService.query.QueryHelper;
import webService.result.ResultCv;
import webService.result.ResultKeyword;
import webService.result.ResultTextualComplexity;
import webService.result.ResultValence;
import webService.services.TextualComplexity;

public class CVAnalyzer {

    public Logger logger = Logger.getLogger(CVAnalyzer.class);
    
    private static String CV_PATH_SAMPLE = "resources/in/cv/cv_sample/";
    private static String CV_PATH = "resources/in/cv_new/cv_analyse/";
    
    private Map<String, String> hm;
    private String path;

    private String pathToLSA;
    private String pathToLDA;
    private Lang lang;
    private double FANthreshold = 5;
    private double FANdelta = 1;
    
    private String keywords = "prospection, prospect, développement, clients, fidélisation, chiffre d’affaires, marge, vente, portefeuille, négociation, budget, rendez-vous, proposition, terrain, téléphone, rentabilité, business, reporting, veille, secteur, objectifs, comptes, animation, suivi, création, gestion";
    private String ignore = "janvier, février, mars, avril, mai, juin, juillet, août, septembre, octobre, novembre, décembre";

    public CVAnalyzer(String path, Map<String, String> hm) {
        this.path = path;
        this.hm = hm;
    }

    public void process() {

        try {

            StringBuilder sb = new StringBuilder();
            sb.append("CV,pages,images,avg images per page,colors,avg colors per page,paragraphs,avg paragraphs per page,sentences,avg sentences per page,words,avg words per page,content words,avg content words per page,")
                    .append("font types,avg font types per page,simple font types,simple font types per page,font sizes,avg font sizes per page,min font size,max font size,bold characters,avg bold characters per page,bold chars by total chars,italic characters,italic characters per pace,italic chars by total chars,bold italic characters,bold italic characters per page,bold italic chars by total chars,")
                    .append("positive words (FAN >= ")
                    .append(FANthreshold + FANdelta)
                    .append("),pos words percentage," + "negative words (FAN <= ")
                    .append(FANthreshold - FANdelta)
                    .append("),neg words percentage," + "neutral words (FAN > ")
                    .append(FANthreshold - FANdelta)
                    .append(" & FAN < ")
                    .append(FANthreshold + FANdelta)
                    .append("),neutral words percentage,")
                    .append("FAN weighted average,");
            //affect,positive emotion,negative emotion,anxiety,anger,sadness,");

            List<SentimentValence> sentimentValences = SentimentValence.getAllValences();
            for (SentimentValence svLiwc : sentimentValences) {
                if (svLiwc != null && svLiwc.getName().contains("LIWC")) {
                    sb.append(svLiwc.getName());
                    sb.append(",");
                    sb.append(svLiwc.getName());
                    sb.append(" percentage,");
                }
            }

            // textual complexity factors
            lang = Lang.getLang(hm.get("lang"));
            TextualComplexity textualComplexity = new TextualComplexity(lang, Boolean.parseBoolean(hm.get("postagging")), Boolean.parseBoolean(hm.get("dialogism")));
            for (ComplexityIndexType cat : textualComplexity.getList()) {
                for (ComplexityIndex index : cat.getFactory().build(lang)) {
                    sb.append(index.getAcronym());
                    sb.append(',');
                }
            }
            sb.append("keywords document relevance,");
            // keywords
            sb.append(
                    "prospection_sim,prospection_no,prospect_sim,prospect_no,développement_sim,développement_no,clients_sim,clients_no,fidélisation_sim,fidélisation_no,chiffre d’affaires_sim,chiffre d’affaires_no,marge_sim,marge_no,vente_sim,vente_no,portefeuille_sim,portefeuille_no,négociation_sim,négociation_no,budget_sim,budget_no,rendez-vous_sim,rendez-vous_no,proposition_sim,proposition_no,terrain_sim,terrain_no,téléphone_sim,téléphone_no,rentabilité_sim,rentabilité_no,business_sim,business_no,reporting_sim,reporting_no,veille_sim,veille_no,secteur_sim,secteur_no,objectifs_sim,objectifs_no,comptes_sim,comptes_no,animation_sim,animation_no,suivi_sim,suivi_no,création_sim,création_no,gestion_sim,gestion_no,");
            // concepts
            /*for (int i = 0; i < 25; i++) {
            sb.append("concept" + i + ',');
            sb.append("rel" + i + ',');
            }*/
            sb.append("\n");

            //test
            Set<String> keywordsList = new HashSet<>(Arrays.asList(keywords.split(",")));
            Set<String> ignoreList = new HashSet<>(Arrays.asList(ignore.split(",[ ]*")));

            System.out.println("Incep procesarea CV-urilor");
            // iterate through all PDF CV files
            Files.walk(Paths.get(path)).forEach(filePath -> {
                String filePathString = filePath.toString();
                if (filePathString.contains(".pdf")) {

                    PdfToTextConverter pdfConverter = new PdfToTextConverter();
                    String cvContent = pdfConverter.pdftoText(filePathString, true);

                    logger.info("Continut cv: " + cvContent);
                    
                    hm.put("text", cvContent);
                    AbstractDocument cvDocument = QueryHelper.processQuery(hm);
                    hm.put("text", keywords);
                    AbstractDocument keywordsDocument = QueryHelper.processQuery(hm);

                    logger.info("Lista de ignore contine " + ignore);
                    hm.put("text", cvContent);
                    ResultCv result = CVHelper.process(cvDocument, keywordsDocument, pdfConverter, keywordsList, ignoreList,
                            hm, FANthreshold, FANdelta);

                    // CV
                    sb.append(filePath.getFileName().toString());
                    sb.append(",");

                    // pages
                    sb.append(result.getPages());
                    sb.append(',');

                    // images
                    sb.append(result.getImages());
                    sb.append(',');

                    // average images per page
                    sb.append(Formatting.formatNumber(result.getImages() * 1.0 / result.getPages()));
                    sb.append(',');

                    // colors
                    sb.append(result.getColors());
                    sb.append(',');

                    // average colors per page
                    sb.append(Formatting.formatNumber(result.getColors() * 1.0 / result.getPages()));
                    sb.append(',');

                    // paragraphs
                    sb.append(result.getParagraphs());
                    sb.append(',');

                    // avg paragraphs per page
                    sb.append(Formatting.formatNumber(result.getParagraphs() * 1.0 / result.getPages()));
                    sb.append(',');

                    // sentences
                    sb.append(result.getSentences());
                    sb.append(',');

                    // avg sentences per page
                    sb.append(Formatting.formatNumber(result.getSentences() * 1.0 / result.getPages()));
                    sb.append(',');

                    // words
                    sb.append(result.getWords());
                    sb.append(',');

                    // avg words per page
                    sb.append(Formatting.formatNumber(result.getWords() * 1.0 / result.getPages()));
                    sb.append(',');

                    // content words
                    sb.append(result.getContentWords());
                    sb.append(',');

                    // avg content words per page
                    sb.append(Formatting.formatNumber(result.getContentWords() * 1.0 / result.getPages()));
                    sb.append(',');
                    
                    sb.append(result.getFontTypes());
                    sb.append(',');
                    
                    sb.append(Formatting.formatNumber(result.getFontTypes() * 1.0 / result.getPages()));
                    sb.append(',');
                    
                    sb.append(result.getFontTypesSimple());
                    sb.append(',');
                    
                    sb.append(Formatting.formatNumber(result.getFontTypesSimple() * 1.0 / result.getPages()));
                    sb.append(',');
                    
                    sb.append(result.getFontSizes());
                    sb.append(',');
                    
                    sb.append(Formatting.formatNumber(result.getFontSizes() * 1.0 / result.getPages()));
                    sb.append(',');
                    
                    sb.append(result.getMinFontSize());
                    sb.append(',');
                    
                    sb.append(result.getMaxFontSize());
                    sb.append(',');
                    
                    sb.append(result.getBoldCharacters());
                    sb.append(',');
                    
                    sb.append(Formatting.formatNumber(result.getBoldCharacters() * 1.0 / result.getPages()));
                    sb.append(',');
                    
                    sb.append(Formatting.formatNumber(result.getBoldCharacters() * 1.0 / result.getTotalCharacters()));
                    sb.append(',');
                    
                    sb.append(result.getItalicCharacters());
                    sb.append(',');
                    
                    sb.append(Formatting.formatNumber(result.getItalicCharacters() * 1.0 / result.getPages()));
                    sb.append(',');
                    
                    sb.append(Formatting.formatNumber(result.getItalicCharacters() * 1.0 / result.getTotalCharacters()));
                    sb.append(',');
                    
                    sb.append(result.getBoldItalicCharacters());
                    sb.append(',');
                    
                    sb.append(Formatting.formatNumber(result.getBoldItalicCharacters() * 1.0 / result.getPages()));
                    sb.append(',');
                    
                    sb.append(Formatting.formatNumber(result.getBoldItalicCharacters() * 1.0 / result.getTotalCharacters()));
                    sb.append(',');

                    // positive words
                    sb.append(result.getPositiveWords().size());
                    sb.append(',');

                    // positive words norm.
                    sb.append(Formatting.formatNumber(result.getPositiveWords().size() * 1.0 / result.getWords()));
                    sb.append(',');

                    // negative words
                    sb.append(result.getNegativeWords().size());
                    sb.append(',');

                    // negative words norm.
                    sb.append(Formatting.formatNumber(result.getNegativeWords().size() * 1.0 / result.getWords()));
                    sb.append(',');

                    // neutral words
                    sb.append(result.getNeutralWords().size());
                    sb.append(',');

                    // neutral words norm.
                    sb.append(Formatting.formatNumber(result.getNeutralWords().size() * 1.0 / result.getWords()));
                    sb.append(',');

                    // FAN weighted average
                    sb.append(Formatting.formatNumber((result.getFanWeightedAverage())));
                    sb.append(',');

                    // LIWC emotions
                    for (Map.Entry<String, List<String>> entry : result.getLiwcEmotions().entrySet()) {
                        // String emotion = entry.getKey();
                        // sb.append(emotion);
                        sb.append(entry.getValue().size());
                        sb.append(',');
                        sb.append(Formatting.formatNumber(entry.getValue().size() * 1.0 / result.getWords()));
                        sb.append(',');
                    }

                    // textual complexity factors
                    List<ResultTextualComplexity> complexityFactors = result.getTextualComplexity();
                    for (ResultTextualComplexity category : complexityFactors) {
                        // sb.append(category.getContent() + ": ");
                        for (ResultValence factor : category.getValences()) {
                            // sb.append(factor.getContent() + '(' +
                            // factor.getScore() + ')');
                            sb.append(factor.getScore());
                            sb.append(',');
                        }
                        // sb.append('|');
                    }
                    // sb.append(',');

                    // (keywords, document) relevance
                    sb.append(result.getKeywordsDocumentRelevance());
                    sb.append(',');

                    // keywords
                    for (ResultKeyword keyword : result.getKeywords()) {
                        // sb.append(keyword.getName() + '(' +
                        // keyword.getRelevance() + ") - " +
                        // keyword.getNoOccurences() + " occurences,");
                        sb.append(keyword.getRelevance());
                        sb.append(',');
                        sb.append(keyword.getNoOccurences());
                        sb.append(',');
                    }
                    // sb.append(',');

                    // concepts
                    /*ResultTopic resultTopic = result.getConcepts();
                    List<ResultNode> resultNodes = resultTopic.getNodes();
                    int i = 0;
                    for (ResultNode resultNode : resultNodes) {
                    // sb.append(resultNode.getName() + '(' +
                    // resultNode.getValue() + ')');
                    sb.append(resultNode.getName());
                    sb.append(',');
                    sb.append(resultNode.getValue());
                    sb.append(',');
                    i++;
                    if (i == 25)
                    break;
                    }*/
                    sb.append("\n");

                }
            });

            File file = new File(path + "stats.csv");
            try {
                FileUtils.writeStringToFile(file, sb.toString(), "UTF-8");
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            logger.info("Printed CV stats to CSV file: " + file.getAbsolutePath());
        } catch (IOException ex) {
            logger.info("Exception: " + ex.getMessage());
            Exceptions.printStackTrace(ex);
        }

    }
    
    private static Map<String, String> loadDefaultParameters() {
        Map<String, String> hm = new HashMap<>();
        hm.put("lsa", "resources/config/FR/LSA/Le_Monde");
        hm.put("lda", "resources/config/FR/LDA/Le_Monde");
        hm.put("lang", "French");
        hm.put("postagging", "true");
        hm.put("dialogism", "true");
        hm.put("threshold", "0.3");
        return hm;
    }

    public static void main(String[] args) {
        BasicConfigurator.configure();
        ReaderBenchServer.initializeDB();
             
        Map<String, String> hm = loadDefaultParameters();
        CVAnalyzer cvAnalyzer = new CVAnalyzer(CV_PATH, hm);
        cvAnalyzer.process();
    }
    
    @Test
    public static void cvSampleTest() {
        BasicConfigurator.configure();
        ReaderBenchServer.initializeDB();
        
        Map<String, String> hm = loadDefaultParameters();
        CVAnalyzer cvAnalyzer = new CVAnalyzer(CV_PATH_SAMPLE, hm);
        cvAnalyzer.process();
    }

}
