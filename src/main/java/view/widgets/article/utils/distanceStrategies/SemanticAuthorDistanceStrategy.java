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
package view.widgets.article.utils.distanceStrategies;

import java.util.List;

import data.article.ResearchArticle;
import data.discourse.SemanticCohesion;
import view.widgets.article.utils.ArticleContainer;

public class SemanticAuthorDistanceStrategy extends AAuthorDistanceStrategy {

    private static final double MIN_SEMANTIC_DISTANCE = 0.3;

    protected ArticleContainer authorContainer;

    private double threshold = 0.3;

    public SemanticAuthorDistanceStrategy(ArticleContainer authorContainer) {
        this.authorContainer = authorContainer;
        this.computeTreshold();
    }

    private void computeTreshold() {
        double totalDist = 0.0, numCompared = 0, stddevPartial = 0.0;

        List<ResearchArticle> articleList = authorContainer.getArticles();
        for (int i = 0; i < articleList.size(); i++) {
            for (int j = i + 1; j < articleList.size(); j++) {
                ResearchArticle a1 = articleList.get(i);
                ResearchArticle a2 = articleList.get(j);
                double distance = SemanticCohesion.getAverageSemanticModelSimilarity(a1, a2);
                if (distance >= MIN_SEMANTIC_DISTANCE) {
                    numCompared++;
                    totalDist += distance;
                    stddevPartial += Math.pow(distance, 2);
                }
            }
        }
        if (numCompared != 0) {
            double avg = totalDist / numCompared;
            double stddev = Math.sqrt(numCompared * stddevPartial - Math.pow(totalDist, 2)) / numCompared;
            this.threshold = avg + stddev;
        }
    }

    public boolean pruneArticlePair(ResearchArticle firstArticle, ResearchArticle secondArticle) {
        return false;
    }

    @Override
    public double computeDistanceBetween(ResearchArticle firstArticle, ResearchArticle secondArticle) {
        if (firstArticle.getURI().equals(secondArticle.getURI())) {
            return 1.0;
        }
        boolean pruneFlag = this.pruneArticlePair(firstArticle, secondArticle);
        if (pruneFlag) {
            return 0.0;
        }
        return SemanticCohesion.getAverageSemanticModelSimilarity(firstArticle, secondArticle);
    }

    public double getThreshold() {
        return this.threshold;
    }

    @Override
    public String getStrategyName() {
        return "Semantic Distance";
    }

    @Override
    public String getStrategyKey() {
        return "Semantic";
    }

    @Override
    public AuthorDistanceStrategyType getStrategyType() {
        return AuthorDistanceStrategyType.SemanticDistance;
    }
}
