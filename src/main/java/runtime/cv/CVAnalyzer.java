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
import services.converters.PdfToTxtConverter;
import services.semanticModels.ISemanticModel;
import services.semanticModels.SimilarityType;
import webService.ReaderBenchServer;
import webService.cv.CVHelper;
import webService.query.QueryHelper;
import webService.result.ResultComplexityIndex;
import webService.result.ResultCv;
import webService.result.ResultTextualComplexity;
import webService.services.TextualComplexity;

public class CVAnalyzer {

    public static final Logger LOGGER = Logger.getLogger("");

    private String path;
    private String keywords;
    private String ignoreWords;

    private Lang lang;
    private List<ISemanticModel> models;
    private boolean usePosTagging;
    private boolean computeDialogism;
    private double threshold;

    public CVAnalyzer(Lang lang, List<ISemanticModel> models, Boolean usePosTagging, Boolean computeDialogism, Double minThreshold) {
        this.path = null;
        this.keywords = null;
        this.ignoreWords = null;
        this.lang = lang;
        this.models = models;
        this.usePosTagging = usePosTagging;
        this.computeDialogism = computeDialogism;
        threshold = minThreshold;
    }

    public CVAnalyzer() {
        this(null, null, true, false, 0.0);
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

        sb.append("cv,pages,images,avg_images_per_page,colors,avg_colors_per_page,paragraphs,avg_paragraphs_per_page,sentences,avg_sentences_per_page,words,avg_words_per_page,content_words,avg_content_words_per_page,")
                .append("font_types,avg_font_types_per_page,simple_font_types,simple_font_types_per_page,font_sizes,avg_font_sizes_per_page,min_font_size,max_font_size,bold_characters,avg_bold_characters_per_page,bold_chars_by_total_chars,italic_characters,italic_characters_per_page,italic_chars_by_total_chars,bold_italic_characters,bold_italic_characters_per_page,bold_italic_chars_by_total_chars,")
                .append("positive_words_FAN_gte_").append(CVConstants.FAN_DELTA)
                .append(",pos_words_percentage," + "negative_words_FAN_lte_").append(-CVConstants.FAN_DELTA)
                .append(",neg_words_percentage," + "neutral_words_FAN_gt_").append(-CVConstants.FAN_DELTA)
                .append("_FAN_lt_").append(CVConstants.FAN_DELTA)
                .append(",neutral_words_percentage,")
                .append("FAN_weighted_average,");

        // LIWC sentiment valences
        List<SentimentValence> sentimentValences = SentimentValence.getAllValences();
        for (SentimentValence svLiwc : sentimentValences) {
            if (svLiwc != null && svLiwc.getName().contains("LIWC")) {
                sb.append(svLiwc.getName()).append(CVConstants.CSV_DELIM);
                sb.append(svLiwc.getName()).append("_percentage").append(CVConstants.CSV_DELIM);
            }
        }

        // textual complexity factors
        TextualComplexity textualComplexity = new TextualComplexity(lang, usePosTagging, computeDialogism);
        for (ComplexityIndexType cat : textualComplexity.getList()) {
            for (ComplexityIndex index : cat.getFactory().build(lang)) {
                sb.append(index.getAcronym()).append(CVConstants.CSV_DELIM);
            }
        }
        sb.append("keywords_document_relevance").append(CVConstants.CSV_DELIM);

        sb.append("keywords_doc_sim_lsa").append(CVConstants.CSV_DELIM);
        sb.append("keywords_doc_sim_lda").append(CVConstants.CSV_DELIM);
        sb.append("keywords_doc_sim_word2vec").append(CVConstants.CSV_DELIM);
        sb.append("keywords_doc_sim_leacock").append(CVConstants.CSV_DELIM);
        sb.append("keywords_doc_sim_wu").append(CVConstants.CSV_DELIM);
        sb.append("keywords_doc_sim_path").append(CVConstants.CSV_DELIM);

        // keywords
//        sb.append("prospection_sim").append(CVConstants.CSV_DELIM)
//                .append("prospection_no").append(CVConstants.CSV_DELIM)
//                .append("developpement_sim").append(CVConstants.CSV_DELIM)
//                .append("developpement_no").append(CVConstants.CSV_DELIM)
//                .append("clients_sim").append(CVConstants.CSV_DELIM)
//                .append("clients_no").append(CVConstants.CSV_DELIM)
//                .append("fidelisation_sim").append(CVConstants.CSV_DELIM)
//                .append("fidelisation_no").append(CVConstants.CSV_DELIM)
//                .append("chiffre_d_affaires_sim").append(CVConstants.CSV_DELIM)
//                .append("chiffre_d_affaires_sim_no").append(CVConstants.CSV_DELIM)
//                .append("marge_sim").append(CVConstants.CSV_DELIM)
//                .append("marge_no").append(CVConstants.CSV_DELIM)
//                .append("vente_sim").append(CVConstants.CSV_DELIM)
//                .append("vente_no").append(CVConstants.CSV_DELIM)
//                .append("negociation_sim").append(CVConstants.CSV_DELIM)
//                .append("negociation_no").append(CVConstants.CSV_DELIM)
//                .append("budget_sim").append(CVConstants.CSV_DELIM)
//                .append("budget_no").append(CVConstants.CSV_DELIM)
//                .append("rendez-vous_sim").append(CVConstants.CSV_DELIM)
//                .append("rendez-vous_no").append(CVConstants.CSV_DELIM)
//                .append("proposition_sim").append(CVConstants.CSV_DELIM)
//                .append("proposition_no").append(CVConstants.CSV_DELIM)
//                .append("terrain_sim").append(CVConstants.CSV_DELIM)
//                .append("terrain_no").append(CVConstants.CSV_DELIM)
//                .append("telephone_sim").append(CVConstants.CSV_DELIM)
//                .append("telephone_no").append(CVConstants.CSV_DELIM)
//                .append("rentabilite_sim").append(CVConstants.CSV_DELIM)
//                .append("rentabilite_no").append(CVConstants.CSV_DELIM)
//                .append("business_sim").append(CVConstants.CSV_DELIM)
//                .append("business_no").append(CVConstants.CSV_DELIM)
//                .append("reporting_sim").append(CVConstants.CSV_DELIM)
//                .append("reporting_no").append(CVConstants.CSV_DELIM)
//                .append("veille_sim").append(CVConstants.CSV_DELIM)
//                .append("veille_no").append(CVConstants.CSV_DELIM)
//                .append("secteur_sim").append(CVConstants.CSV_DELIM)
//                .append("secteur_no").append(CVConstants.CSV_DELIM)
//                .append("objectifs_sim").append(CVConstants.CSV_DELIM)
//                .append("objectifs_no").append(CVConstants.CSV_DELIM)
//                .append("comptes_sim").append(CVConstants.CSV_DELIM)
//                .append("comptes_no").append(CVConstants.CSV_DELIM)
//                .append("animation_sim").append(CVConstants.CSV_DELIM)
//                .append("animation_no").append(CVConstants.CSV_DELIM)
//                .append("suivi_sim").append(CVConstants.CSV_DELIM)
//                .append("suivi_no").append(CVConstants.CSV_DELIM)
//                .append("creation_sim").append(CVConstants.CSV_DELIM)
//                .append("creation_no").append(CVConstants.CSV_DELIM)
//                .append("gestion_sim").append(CVConstants.CSV_DELIM)
//                .append("gestion_no").append(CVConstants.CSV_DELIM);
        // concepts
        /*for (int i = 0; i < 25; i++) {
            sb.append("concept" + i + ',');
            sb.append("rel" + i + ',');
            }*/
        sb.append(CVConstants.CSV_NEW_LINE_DELIM);
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
            for (ResultComplexityIndex factor : category.getValences()) {
                // sb.append(factor.getContent() + '(' +
                // factor.getScore() + ')');
                sb.append(factor.getValue()).append(CVConstants.CSV_DELIM);
            }
            // sb.append('|');
        }
        // sb.append(CVConstants.CSV_DELIM);
        sb.append(result.getKeywordsDocumentRelevance()).append(CVConstants.CSV_DELIM);
        Map<String, Double> keywordsDocumentSimilarity = result.getKeywordsDocumentSimilarity();
        sb.append(keywordsDocumentSimilarity.get(SimilarityType.LSA.getAcronym())).append(CVConstants.CSV_DELIM);
        sb.append(keywordsDocumentSimilarity.get(SimilarityType.LDA.getAcronym())).append(CVConstants.CSV_DELIM);
        sb.append(keywordsDocumentSimilarity.get(SimilarityType.WORD2VEC.getAcronym())).append(CVConstants.CSV_DELIM);
        sb.append(keywordsDocumentSimilarity.get(SimilarityType.LEACOCK_CHODOROW.getAcronym())).append(CVConstants.CSV_DELIM);
        sb.append(keywordsDocumentSimilarity.get(SimilarityType.WU_PALMER.getAcronym())).append(CVConstants.CSV_DELIM);
        sb.append(keywordsDocumentSimilarity.get(SimilarityType.PATH_SIM.getAcronym())).append(CVConstants.CSV_DELIM);
        // keywords
//        for (ResultKeyword keyword : result.getKeywords()) {
//            // sb.append(keyword.getName() + '(' +
//            // keyword.getRelevance() + ") - " +
//            // keyword.getNoOccurences() + " occurences,");
//            sb.append(keyword.getRelevance()).append(CVConstants.CSV_DELIM);
//            sb.append(keyword.getNoOccurences()).append(CVConstants.CSV_DELIM);
//        }
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
        sb.append(CVConstants.CSV_NEW_LINE_DELIM);
        return sb.toString();
    }

    public ResultCv processFile(String filePath, Set<String> keywordsList, Set<String> ignoreList,
            Lang lang, List<ISemanticModel> models, boolean usePosTagging, boolean computeDialogism, double threshold) {

        PdfToTxtConverter pdfToTxtConverter = new PdfToTxtConverter(filePath, true);
        pdfToTxtConverter.process();
        AbstractDocument cvDocument = QueryHelper.generateDocument(pdfToTxtConverter.getParsedText(), lang, models, usePosTagging, computeDialogism);
        AbstractDocument keywordsDocument = QueryHelper.generateDocument(keywords, lang, models, usePosTagging, computeDialogism);
        return CVHelper.process(cvDocument, keywordsDocument, pdfToTxtConverter, keywordsList, ignoreList,
                lang, models, usePosTagging, computeDialogism, threshold, CVConstants.FAN_DELTA);
    }

    public void processPath() {
        if (path == null) {
            System.err.println("Path not set. Nothing to process.");
            System.exit(0);
        }
        Set<String> keywordsList = new HashSet<>(Arrays.asList(keywords.split(",")));
        Set<String> ignoreList = new HashSet<>(Arrays.asList(ignoreWords.split(",[ ]*")));

        StringBuilder sb = new StringBuilder();
        sb.append(csvBuildHeader());
        LOGGER.log(Level.INFO, "Processing path: {0}", path);
        File globalStatsFile = new File(path + CVConstants.STATS_FILE);
        try {
            // iterate through all PDF CV files
            Files.walk(Paths.get(path)).forEach(filePath -> {
                // TODO: replace with mimetype check
                if (filePath.toString().contains(".pdf")) {
                    String fileName = filePath.getFileName().toString().replaceAll("\\s+", "_");
                    int extensionStart = fileName.lastIndexOf(".");
                    sb.append(csvBuildRow(fileName.substring(0, extensionStart),
                            processFile(filePath.toString(), keywordsList, ignoreList, lang, models, usePosTagging, computeDialogism, threshold)));
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

    public static void main(String[] args) {
        ReaderBenchServer.initializeDB();
        Lang lang = Lang.getLang(CVConstants.LANG_FR);
        Boolean usePosTagging = CVConstants.POS_TAGGING;
        Boolean computeDialogism = CVConstants.DIALOGISM;
        String lsaCorpora = CVConstants.LSA_PATH_FR;
        String ldaCorpora = CVConstants.LDA_PATH_FR;
        String w2vCorpora = null;//CVConstants.WOR2VEC_PATH_FR;
        Double minThreshold = CVConstants.THRESHOLD;
        String lsaCorporaName = CVConstants.FR_NAME;
        String ldaCorporaName = CVConstants.FR_NAME;
        List<ISemanticModel> models = QueryHelper.loadSemanticModels(lang, lsaCorporaName, ldaCorporaName, w2vCorpora);
        CVAnalyzer frenchCVAnalyzer = new CVAnalyzer(lang, models, usePosTagging, computeDialogism, minThreshold);
        frenchCVAnalyzer.setKeywords(CVConstants.KEYWORDS);
        frenchCVAnalyzer.setIgnoreWords(CVConstants.IGNORE);
        frenchCVAnalyzer.setPath(CVConstants.CV_PATH);
        frenchCVAnalyzer.processPath();
    }

    @Test
    public static void cvSampleTest() {
        ReaderBenchServer.initializeDB();
        Lang lang = Lang.getLang(CVConstants.LANG_FR);
        Boolean usePosTagging = CVConstants.POS_TAGGING;
        Boolean computeDialogism = CVConstants.DIALOGISM;
        String lsaCorpora = CVConstants.LSA_PATH_FR;
        String ldaCorpora = CVConstants.LDA_PATH_FR;
        String w2vCorpora = CVConstants.WOR2VEC_PATH_FR;
        Double minThreshold = CVConstants.THRESHOLD;

        List<ISemanticModel> models = QueryHelper.loadSemanticModels(lang, lsaCorpora, ldaCorpora, w2vCorpora);
        CVAnalyzer frenchCVAnalyzer = new CVAnalyzer(lang, models, usePosTagging, computeDialogism, minThreshold);
        frenchCVAnalyzer.setKeywords(CVConstants.KEYWORDS);
        frenchCVAnalyzer.setIgnoreWords(CVConstants.IGNORE);
        frenchCVAnalyzer.setPath(CVConstants.CV_PATH_SAMPLE);
        frenchCVAnalyzer.processPath();
    }
}
