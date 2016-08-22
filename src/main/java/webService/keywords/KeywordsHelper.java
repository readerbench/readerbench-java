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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import data.AbstractDocument;
import data.Lang;
import data.discourse.SemanticCohesion;
import services.commons.Formatting;
import services.nlp.listOfWords.ListOfWords;
import webService.query.QueryHelper;
import webService.result.ResultKeyword;

public class KeywordsHelper {

    public static List<ResultKeyword> getKeywords(
            AbstractDocument document, AbstractDocument keywordsDocument, Set<String> keywords,
            String pathToLSA, String pathToLDA, Lang lang, boolean usePOSTagging, boolean computeDialogism, double threshold) {

        ArrayList<ResultKeyword> resultKeywords = new ArrayList<>();

        ListOfWords usedList = new ListOfWords();
        usedList.setWords(keywords);

        usedList.getWords().stream().forEach((pattern) -> {
            AbstractDocument patterDocument = QueryHelper.processQuery(pattern, pathToLSA, pathToLDA, lang, usePOSTagging, computeDialogism);
            int occ = 0;
            Pattern javaPattern = Pattern.compile(" " + pattern + " ");
            Matcher matcher = javaPattern.matcher(" " + document.getText().trim() + " ");
            SemanticCohesion sc = new SemanticCohesion(patterDocument, document);
            while (matcher.find()) {
                occ++;
            }
            resultKeywords.add(new ResultKeyword(pattern, occ, Formatting.formatNumber(sc.getCohesion())));
        });

        Collections.sort(resultKeywords, ResultKeyword.ResultKeywordRelevanceComparator);
        return resultKeywords;
    }

}
