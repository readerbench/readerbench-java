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

import data.AbstractDocument;
import data.Lang;
import data.sentiment.SentimentValence;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.FileUtils;
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

    public static final Logger LOGGER = Logger.getLogger("");

    private Map<String, String> hm;
    private String path;
    private String keywords;
    private String ignoreWords;

    public CVAnalyzer(Map<String, String> hm) {
        this.hm = hm;
        this.path = null;
        this.keywords = null;
        this.ignoreWords = null;
    }
    
    public CVAnalyzer() {
        this(null);
    }

    public Map<String, String> getHm() {
        return hm;
    }

    public void setHm(Map<String, String> hm) {
        this.hm = hm;
    }   

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getKeywords() {
        return keywords;
    }

    public void setKeywords(String keywords) {
        this.keywords = keywords;
    }

    public String getIgnoreWords() {
        return ignoreWords;
    }

    public void setIgnoreWords(String ignoreWords) {
        this.ignoreWords = ignoreWords;
    }

    private String csvBuildHeader() {
        StringBuilder sb = new StringBuilder();

        sb.append("CV,pages,images,avg images per page,colors,avg colors per page,paragraphs,avg paragraphs per page,sentences,avg sentences per page,words,avg words per page,content words,avg content words per page,")
                .append("font types,avg font types per page,simple font types,simple font types per page,font sizes,avg font sizes per page,min font size,max font size,bold characters,avg bold characters per page,bold chars by total chars,italic characters,italic characters per pace,italic chars by total chars,bold italic characters,bold italic characters per page,bold italic chars by total chars,")
                .append("positive words (FAN >= ").append(CVConstants.FAN_DELTA)
                .append("),pos words percentage," + "negative words (FAN <= ").append(-CVConstants.FAN_DELTA)
                .append("),neg words percentage," + "neutral words (FAN > ").append(-CVConstants.FAN_DELTA)
                .append(" & FAN < ").append(CVConstants.FAN_DELTA)
                .append("),neutral words percentage,")
                .append("FAN weighted average,");

        // LIWC sentiment valences
        List<SentimentValence> sentimentValences = SentimentValence.getAllValences();
        for (SentimentValence svLiwc : sentimentValences) {
            if (svLiwc != null && svLiwc.getName().contains("LIWC")) {
                sb.append(svLiwc.getName()).append(CVConstants.CSV_DELIM);
                sb.append(svLiwc.getName()).append(" percentage").append(CVConstants.CSV_DELIM);
            }
        }

        // textual complexity factors
        Lang lang = Lang.getLang(hm.get("lang"));
        TextualComplexity textualComplexity = new TextualComplexity(lang, Boolean.parseBoolean(hm.get("postagging")), Boolean.parseBoolean(hm.get("dialogism")));
        for (ComplexityIndexType cat : textualComplexity.getList()) {
            for (ComplexityIndex index : cat.getFactory().build(lang)) {
                sb.append(index.getAcronym()).append(CVConstants.CSV_DELIM);
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
        return sb.toString();
    }
    
    private void cvError() {        
    }

    private String csvBuildRow(String fileName, ResultCv result) {
        StringBuilder sb = new StringBuilder();
        sb.append(fileName).append(CVConstants.CSV_DELIM);
        sb.append(result.getPages()).append(CVConstants.CSV_DELIM);
        sb.append(result.getImages()).append(CVConstants.CSV_DELIM);
        sb.append(Formatting.formatNumber(result.getImages() * 1.0
                / result.getPages())).append(CVConstants.CSV_DELIM);
        sb.append(result.getColors()).append(CVConstants.CSV_DELIM);
        sb.append(Formatting.formatNumber(result.getColors() * 1.0
                / result.getPages())).append(CVConstants.CSV_DELIM);
        sb.append(result.getParagraphs()).append(CVConstants.CSV_DELIM);
        sb.append(Formatting.formatNumber(result.getParagraphs() * 1.0
                / result.getPages())).append(CVConstants.CSV_DELIM);
        sb.append(result.getSentences()).append(CVConstants.CSV_DELIM);
        sb.append(Formatting.formatNumber(result.getSentences() * 1.0
                / result.getPages())).append(CVConstants.CSV_DELIM);
        sb.append(result.getWords()).append(CVConstants.CSV_DELIM);
        sb.append(Formatting.formatNumber(result.getWords() * 1.0
                / result.getPages())).append(CVConstants.CSV_DELIM);
        sb.append(result.getContentWords()).append(CVConstants.CSV_DELIM);
        sb.append(Formatting.formatNumber(result.getContentWords() * 1.0
                / result.getPages())).append(CVConstants.CSV_DELIM);
        sb.append(result.getFontTypes()).append(CVConstants.CSV_DELIM);
        sb.append(Formatting.formatNumber(result.getFontTypes() * 1.0
                / result.getPages())).append(CVConstants.CSV_DELIM);
        sb.append(result.getFontTypesSimple()).append(CVConstants.CSV_DELIM);
        sb.append(Formatting.formatNumber(result.getFontTypesSimple() * 1.0
                / result.getPages())).append(CVConstants.CSV_DELIM);
        sb.append(result.getFontSizes()).append(CVConstants.CSV_DELIM);
        sb.append(Formatting.formatNumber(result.getFontSizes() * 1.0
                / result.getPages())).append(CVConstants.CSV_DELIM);
        sb.append(result.getMinFontSize()).append(CVConstants.CSV_DELIM);
        sb.append(result.getMaxFontSize()).append(CVConstants.CSV_DELIM);
        sb.append(result.getBoldCharacters()).append(CVConstants.CSV_DELIM);
        sb.append(Formatting.formatNumber(result.getBoldCharacters() * 1.0
                / result.getPages())).append(CVConstants.CSV_DELIM);
        sb.append(Formatting.formatNumber(result.getBoldCharacters() * 1.0
                / result.getTotalCharacters())).append(CVConstants.CSV_DELIM);
        sb.append(result.getItalicCharacters()).append(CVConstants.CSV_DELIM);
        sb.append(Formatting.formatNumber(result.getItalicCharacters() * 1.0
                / result.getPages())).append(CVConstants.CSV_DELIM);
        sb.append(Formatting.formatNumber(result.getItalicCharacters() * 1.0
                / result.getTotalCharacters())).append(CVConstants.CSV_DELIM);
        sb.append(result.getBoldItalicCharacters()).append(CVConstants.CSV_DELIM);
        sb.append(Formatting.formatNumber(result.getBoldItalicCharacters()
                * 1.0 / result.getPages())).append(CVConstants.CSV_DELIM);
        sb.append(Formatting.formatNumber(result.getBoldItalicCharacters()
                * 1.0 / result.getTotalCharacters())).append(CVConstants.CSV_DELIM);
        sb.append(result.getPositiveWords().size()).append(CVConstants.CSV_DELIM);
        sb.append(Formatting.formatNumber(result.getPositiveWords().size()
                * 1.0 / result.getWords())).append(CVConstants.CSV_DELIM);
        sb.append(result.getNegativeWords().size()).append(CVConstants.CSV_DELIM);
        sb.append(Formatting.formatNumber(result.getNegativeWords().size()
                * 1.0 / result.getWords())).append(CVConstants.CSV_DELIM);
        sb.append(result.getNeutralWords().size()).append(CVConstants.CSV_DELIM);
        sb.append(Formatting.formatNumber(result.getNeutralWords().size()
                * 1.0 / result.getWords())).append(CVConstants.CSV_DELIM);
        sb.append(Formatting.formatNumber((result.getFanWeightedAverage()))).append(CVConstants.CSV_DELIM);
        // LIWC emotions
        for (Map.Entry<String, List<String>> entry
                : result.getLiwcEmotions().entrySet()) {
            sb.append(entry.getValue().size()).append(CVConstants.CSV_DELIM);
            sb.append(Formatting.formatNumber(entry.getValue().size() * 1.0
                    / result.getWords())).append(CVConstants.CSV_DELIM);
        }
        // textual complexity factors
        List<ResultTextualComplexity> complexityFactors
                = result.getTextualComplexity();
        for (ResultTextualComplexity category : complexityFactors) {
            // sb.append(category.getContent() + ": ");
            for (ResultValence factor : category.getValences()) {
                // sb.append(factor.getContent() + '(' +
                // factor.getScore() + ')');
                sb.append(factor.getScore()).append(CVConstants.CSV_DELIM);
            }
            // sb.append('|');
        }
        // sb.append(CVConstants.CSV_DELIM);
        sb.append(result.getKeywordsDocumentRelevance()).append(CVConstants.CSV_DELIM);
        // keywords
        for (ResultKeyword keyword : result.getKeywords()) {
            // sb.append(keyword.getName() + '(' +
            // keyword.getRelevance() + ") - " +
            // keyword.getNoOccurences() + " occurences,");
            sb.append(keyword.getRelevance()).append(CVConstants.CSV_DELIM);
            sb.append(keyword.getNoOccurences()).append(CVConstants.CSV_DELIM);
        }
        // sb.append(CVConstants.CSV_DELIM);
        // concepts
        /*ResultTopic resultTopic = result.getConcepts();
        List<ResultNode> resultNodes = resultTopic.getNodes();
        int i = 0;
        for (ResultNode resultNode : resultNodes) {
        // sb.append(resultNode.getName() + '(' +
        // resultNode.getValue() + ')');
        sb.append(resultNode.getName());
        sb.append(CVConstants.CSV_DELIM);
        sb.append(resultNode.getValue());
        sb.append(CVConstants.CSV_DELIM);
        i++;
        if (i == 25)
        break;
        }*/
        sb.append("\n");
        return sb.toString();
    }

    public ResultCv processFile(String filePath, Set<String> keywordsList,
            Set<String> ignoreList, PdfToTextConverter pdfConverter) {
        String cvContent = pdfConverter.pdftoText(filePath, true);
        //logger.info("CV textual content: " + cvContent);
        hm.put("text", cvContent);
        AbstractDocument cvDocument = QueryHelper.processQuery(hm);
        hm.put("text", keywords);
        AbstractDocument keywordsDocument = QueryHelper.processQuery(hm);
        hm.put("text", cvContent);
        ResultCv result = CVHelper.process(cvDocument, keywordsDocument, pdfConverter, keywordsList, ignoreList,
                hm, CVConstants.FAN_DELTA, CVConstants.NO_CONCEPTS);

        return result;
    }

    public void processPath() {
        if (path == null) {
            System.err.println("Path not set. Nothing to process.");
            System.exit(0);
        }
        Set<String> keywordsList = new HashSet<>(Arrays.asList(keywords.split(",")));
        Set<String> ignoreList = new HashSet<>(Arrays.asList(ignoreWords.split(",[ ]*")));

        PdfToTextConverter pdfConverter = new PdfToTextConverter();
        StringBuilder sb = new StringBuilder();
        sb.append(csvBuildHeader());
        LOGGER.log(Level.INFO, "Processing path: {0}", path);
        File globalStatsFile = new File(path + CVConstants.STATS_FILE);
        try {
            // iterate through all PDF CV files
            Files.walk(Paths.get(path)).forEach(filePath -> {
                // TODO: replace with mimetype check
                if (filePath.toString().contains(".pdf")) {
                    sb.append(csvBuildRow(filePath.getFileName().toString(),
                            processFile(filePath.toString(), keywordsList,
                                    ignoreList, pdfConverter)));
                }
            });
            FileUtils.writeStringToFile(globalStatsFile, sb.toString(), "UTF-8");
            sb.setLength(0);
            LOGGER.log(Level.INFO, "Printed global stats to file: {0}", globalStatsFile.getAbsolutePath());
        } catch (IOException ex) {
            LOGGER.log(Level.INFO, "Exception: {0}", ex.getMessage());
            Exceptions.printStackTrace(ex);
        }
    }

    private static Map<String, String> loadDefaultParameters() {
        Map<String, String> hm = new HashMap<>();
        hm.put("lsa",           CVConstants.LSA_PATH_FR);
        hm.put("lda",           CVConstants.LDA_PATH_FR);
        hm.put("lang",          CVConstants.LANG_FR);
        hm.put("postagging",    CVConstants.POS_TAGGING);
        hm.put("dialogism",     CVConstants.DIALOGISM);
        hm.put("threshold",     CVConstants.THRESHOLD);
        return hm;
    }

    public static void main(String[] args) {
        ReaderBenchServer.initializeDB();
        Map<String, String> hm = loadDefaultParameters();
        CVAnalyzer frenchCVAnalyzer = new CVAnalyzer(hm);
        frenchCVAnalyzer.setKeywords(CVConstants.KEYWORDS);
        frenchCVAnalyzer.setIgnoreWords(CVConstants.IGNORE);
        frenchCVAnalyzer.setPath(CVConstants.CV_PATH_SAMPLE);
        frenchCVAnalyzer.processPath();
    }

    @Test
    public static void cvSampleTest() {
        ReaderBenchServer.initializeDB();
        Map<String, String> hm = loadDefaultParameters();
        CVAnalyzer frenchCVAnalyzer = new CVAnalyzer(hm);
        frenchCVAnalyzer.setKeywords(CVConstants.KEYWORDS);
        frenchCVAnalyzer.setIgnoreWords(CVConstants.IGNORE);
        frenchCVAnalyzer.setPath(CVConstants.CV_PATH_SAMPLE);
        frenchCVAnalyzer.processPath();
    }
}
