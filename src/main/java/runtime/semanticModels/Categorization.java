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
package runtime.semanticModels;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;

import dao.CategoryDAO;
import data.AbstractDocument;
import data.Lang;
import data.discourse.SemanticCohesion;
import data.pojo.Category;
import data.pojo.CategoryPhrase;
import services.commons.Formatting;
import services.complexity.ComplexityIndices;
import webService.query.QueryHelper;
import webService.result.ResultCategory;

public class Categorization {

    private static final Logger logger = Logger.getLogger(Categorization.class);

    public static void performCategorization(String description) {
        AbstractDocument queryDoc = QueryHelper.processQuery(description, "resources/config/EN/LSA/TASA LAK", "resources/config/EN/LDA/TASA LAK", Lang.getLang("eng"), false, false);

        logger.info("Built document has " + queryDoc.getBlocks().size() + " blocks.");
        queryDoc.computeAll(false, null, null);
        ComplexityIndices.computeComplexityFactors(queryDoc);

        List<ResultCategory> resultCategories = new ArrayList<>();

        List<Category> dbCategories = CategoryDAO.getInstance().findAll();

        dbCategories.stream().map((cat) -> cat.getCategoryPhraseList()).forEach((categoryPhrases) -> {
            StringBuilder sb = new StringBuilder();
            categoryPhrases.stream().map((categoryPhrase) -> {
                sb.append(categoryPhrase.getLabel());
                return categoryPhrase;
            }).forEach((_item) -> {
                sb.append(", ");
            });
        });

        dbCategories.stream().forEach((cat) -> {
            List<CategoryPhrase> categoryPhrases = cat.getCategoryPhraseList();
            StringBuilder sb = new StringBuilder();
            categoryPhrases.stream().map((categoryPhrase) -> {
                sb.append(categoryPhrase.getLabel());
                return categoryPhrase;
            }).forEach((_item) -> {
                sb.append(" ");
            });

            AbstractDocument queryCategory = QueryHelper.processQuery(sb.toString(), "resources/config/EN/LSA/TASA LAK", "resources/config/EN/LDA/TASA LAK", Lang.eng, false, false);
            SemanticCohesion sc = new SemanticCohesion(queryCategory, queryDoc);
            resultCategories.add(new ResultCategory(cat.getLabel(), Formatting.formatNumber(sc.getCohesion())));
        });

        Collections.sort(resultCategories, ResultCategory.ResultCategoryRelevanceComparator);

        resultCategories.stream().forEach((res) -> {
            System.out.println(res.getName() + ":\t" + Formatting.formatNumber(res.getRelevance()));
        });
    }

    public static void main(String[] args) {
        String description1 = "Description\nThis asset endows the agent with the ability to subjectively evaluate the events that happen in the game world and to \"feel\" emotions in response. This subjective evaluation is done from multiple perspectives, including how desirable was the event for the agent's goals or how blameworthy was the event according to the agent's standards. All events are stored in the agent's memory along with the associated emotional state. This allows the agent to remember and share his emotional experience with the user.\nEmotion Generation:\nEmotions are generated based on the proposed links found in emotion theory between appraisal variables (e.g. desirability, praiseworthiness, etc.) and certain emotion types (e.g. joy, distress, shame). To generate these appraisal variables the asset must be initialized with a set of evaluation rules for all the emotionally-relevant events. These rules can refer to events/actions that are very specific to a given domain (e.g. \"eliminate the queen\" in chess), or they can refer to \"meta-events\" that are internally generated by the agent such as \"goal failure\" or \"norm violated\". The latter type of rules is strongly encouraged to promote generalizability across domains.\nEmotional Thresholds and decay rates:\nApart from the evaluation rules, this asset allows the configuration of an emotional threshold for each possible type of emotion that the agent may experience as well as a decay rate. This allows for an additional level of differentiation between agents concerning their emotional responses. For instance, even if two characters share the same evaluation rules for judging what kind of events makes them angry, one of them can experience much less anger compared to the other just by differentiating their respective anger thresholds. Similarly, one of them can be angry for much longer than the other by differentiating the decay rate they have associated to anger.\nFinally, apart from emotions, the agent's emotional state contains a notion of the agent mood. While emotions have a specific type (satisfaction, distress, etc.) and are usually brief, the mood of the agent only has a valence (positive/negative) and decays much slower to a neutral state. Positive emotions increase the mood of the agent and negative emotions decrease it. When the agent is in a negative mood, it is harder for the agent to experience positive emotions (because the potential will be lower, and the emotion is more likely to be cut off). An analogous effect occurs with characters in good mood.";
        performCategorization(description1);
    }
}
