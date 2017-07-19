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
package webService.keywords;

import data.AbstractDocument;
import data.Lang;
import data.discourse.SemanticCohesion;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.io.FileUtils;
import org.openide.util.Exceptions;
import services.commons.Formatting;
import services.nlp.listOfWords.ListOfWords;
import services.semanticModels.ISemanticModel;
import webService.query.QueryHelper;
import webService.result.ResultEdge;
import webService.result.ResultKeyword;
import webService.result.ResultNode;
import webService.result.ResultTopic;

public class KeywordsHelper {

    public static final Logger LOGGER = Logger.getLogger("");

    public static List<ResultKeyword> getKeywords(
            AbstractDocument document,
            AbstractDocument keywordsDocument,
            Set<String> keywords,
            Lang lang, List<ISemanticModel> models, Boolean usePosTagging, Boolean computeDialogism, Double minThreshold) {
        ArrayList<ResultKeyword> resultKeywords = new ArrayList<>();
        ListOfWords usedList = new ListOfWords();
        usedList.setWords(keywords);
        usedList.getWords().stream().forEach((pattern) -> {
            AbstractDocument patterDocument;
            try {
                patterDocument = QueryHelper.generateDocument(pattern, lang, models, usePosTagging, computeDialogism);
                int occ = 0;
                Pattern javaPattern = Pattern.compile(" " + pattern + " ");
                Matcher matcher = javaPattern.matcher(" " + document.getText().trim() + " ");
                SemanticCohesion sc = new SemanticCohesion(patterDocument, document);
                double cohesion = sc.getCohesion();
                while (matcher.find()) {
                    occ++;
                }
                if (occ > 0 && cohesion >= minThreshold) {
                    resultKeywords.add(new ResultKeyword(pattern, occ, cohesion));
                }
            } catch (Exception ex) {
                Exceptions.printStackTrace(ex);
            }
        });

        Collections.sort(resultKeywords, ResultKeyword.ResultKeywordRelevanceComparator);
        return resultKeywords;
    }

    public static String keywordsCSVHeader() {
        StringBuilder sb = new StringBuilder();
        sb.append("Word Lemma").append(",");
        sb.append("POS").append(",");
        sb.append("#").append(",");
        sb.append("Links").append(",");
        sb.append("Degree").append(",");
        sb.append("Relevance").append(",");
        sb.append("TF").append(",");
        sb.append("IDF").append(",");
        sb.append("LSA").append(",");
        sb.append("LDA").append(",");
        sb.append("Word2Vec").append(",");
        sb.append("Avg. Dist. to Root").append(",");
        sb.append("Max. Dist. to Root").append(",");
        sb.append("Polysemy Count").append(",");
        sb.append("Related words").append(",");
        sb.append("\n");
        return sb.toString();
    }

    public static String keywordsCSVBody(ResultTopic resultTopic) {
        StringBuilder sb = new StringBuilder();
        for (ResultNode node : resultTopic.getNodes()) {
            sb.append(node.getLemma()).append(",");
            sb.append(node.getPos()).append(",");
            sb.append(node.getNoOcc()).append(",");
            sb.append(node.getNoLinks()).append(",");
            sb.append(node.getDegree()).append(",");
            sb.append(node.getValue()).append(",");
            sb.append(node.getTf()).append(",");
            sb.append(node.getIdf()).append(",");
            if (node.getSemanticSimilarities().size() > 0) {
                if (node.getSemanticSimilarities().size() > 0) {
                    sb.append(node.getSemanticSimilarities().get(0).getScore()).append(",");
                } else {
                    sb.append(",");
                }
                if (node.getSemanticSimilarities().size() > 1) {
                    sb.append(node.getSemanticSimilarities().get(1).getScore()).append(",");
                } else {
                    sb.append(",");
                }
                if (node.getSemanticSimilarities().size() > 2) {
                    sb.append(node.getSemanticSimilarities().get(2).getScore()).append(",");
                } else {
                    sb.append(",");
                }
            } else {
                sb.append(",,,");
            }
            sb.append(node.getAverageDistanceToHypernymTreeRoot()).append(",");
            sb.append(node.getMaxDistanceToHypernymTreeRoot()).append(",");
            sb.append(node.getPolysemyCount()).append(",");
            for (ResultEdge edge : resultTopic.getLinks()) {
                if (edge.getSource() == node.getId()) {
                    sb.append(resultTopic.getNodes().get(edge.getTarget()).getLemma()).append(",");
                    sb.append(resultTopic.getNodes().get(edge.getTarget()).getPos()).append(",");
                    sb.append(edge.getScore()).append(",");
                }
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    public static void saveKeywordsCSVFile(String path, String fileName, ResultTopic resultTopic) {
        StringBuilder sb = new StringBuilder();
        sb.append(keywordsCSVHeader());
        sb.append(keywordsCSVBody(resultTopic));
        try {
            File csvFile = new File(path + fileName);
            FileUtils.writeStringToFile(csvFile, sb.toString(), "UTF-8");
            LOGGER.log(Level.INFO, "Printed CSV file at {0}", csvFile.getAbsolutePath());
        } catch (IOException ex) {
            LOGGER.log(Level.INFO, "Exception: {0}", ex.getMessage());
            Exceptions.printStackTrace(ex);
        }
    }

}
