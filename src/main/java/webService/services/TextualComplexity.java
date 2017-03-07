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
package webService.services;

import data.AbstractDocument;
import data.Lang;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import services.complexity.ComplexityIndex;
import services.complexity.ComplexityIndexType;
import services.complexity.ComplexityIndices;
import webService.result.*;

public class TextualComplexity {

    private AbstractDocument d;
    private Lang lang;
    private final boolean posTagging;
    private final boolean computeDialogism;
    private List<ComplexityIndexType> list;

    public TextualComplexity(Lang lang, boolean posTagging, boolean computeDialogism) {
        this.lang = lang;
        this.posTagging = posTagging;
        this.computeDialogism = computeDialogism;
        initializeList();
    }

    public TextualComplexity(AbstractDocument d, Lang lang, boolean posTagging, boolean computeDialogism) {
        this.d = d;
        this.lang = lang;
        this.posTagging = posTagging;
        this.computeDialogism = computeDialogism;
        initializeList();
    }

    public List<ComplexityIndexType> getList() {
        return list;
    }

    public Lang getLang() {
        return lang;
    }

    public void setLang(Lang lang) {
        this.lang = lang;
    }

    private void initializeList() {

        list = Arrays.stream(ComplexityIndexType.values())
                .filter(t -> t.getFactory() != null)
                .filter(t -> !t.getFactory().build(lang).isEmpty())
                .collect(Collectors.toList());
       
//		if (posTagging) {
//			list.add(new POSComplexity());
//			list.add(new TreeComplexity());
//		}
//
//		if (computeDialogism) {
//			list.add(new LexicalChainsComplexity());
//			list.add(new LexicalCohesionComplexity());
//			list.add(new DialogismStatisticsComplexity());
//			list.add(new DialogismSynergyComplexity());
//		}
    }

    /**
     * Get values for all textual complexity indices applied on the entire
     * document
     *
     * @param query
     * @return List of sentiment values per entity
     */
    public List<ResultTextualComplexity> getComplexityIndices() {

        List<ResultTextualComplexity> resultsComplexity = new ArrayList<>();

        // complexity indices computation
        ComplexityIndices.computeComplexityFactors(d);
        
        // complexity indices save to result list
        for (ComplexityIndexType cat : list) {
            List<ResultComplexityIndex> localResults = new ArrayList<>();
            for (ComplexityIndex index : cat.getFactory().build(lang)) {
                localResults.add(new ResultComplexityIndex(index.getDescription(), d.getComplexityIndices().get(index)));
            }
            resultsComplexity.add(new ResultTextualComplexity(cat.name(), localResults));
        }

        /*
		 * List<ResultValence> localResults; for (IComplexityFactors
		 * complexityClass : ComplexityIndices.TEXTUAL_COMPLEXITY_FACTORS) {
		 * localResults = new ArrayList<ResultValence>(); for (int id :
		 * complexityClass.getIDs()) { localResults.add(new
		 * ResultValence(ComplexityIndices.TEXTUAL_COMPLEXITY_INDEX_DESCRIPTIONS
		 * [id], Formatting.formatNumber(queryDoc.getComplexityIndices()[id])));
		 * } resultsComplexity.add(new
		 * ResultTextualComplexity(complexityClass.getClassName(),
		 * localResults)); }
         */
        return resultsComplexity;
    }

}
