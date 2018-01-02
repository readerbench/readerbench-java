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
package com.readerbench.services.extendedCNA.distanceStrategies;

import com.readerbench.data.article.ResearchArticle;
import com.readerbench.services.extendedCNA.SingleAuthorContainer;

import java.util.List;

public abstract class AAuthorDistanceStrategy implements IAuthorDistanceStrategy {

    @Override
    public double computeDistanceBetween(SingleAuthorContainer firstAuthor, SingleAuthorContainer secondAuthor) {
        double totalScore = 0.0, totalArticles = 0.0;
        for (ResearchArticle firstAuthorArticle : firstAuthor.getAuthorArticles()) {
            for (ResearchArticle secondAuthorArticle : secondAuthor.getAuthorArticles()) {
                double score = this.computeDistanceBetween(firstAuthorArticle, secondAuthorArticle);
                boolean pruneFlag = this.pruneArticlePair(firstAuthorArticle, secondAuthorArticle);
                if (score > this.getThreshold() && !pruneFlag) {
                    totalScore += score;
                    totalArticles++;
                }
            }
        }
        if (totalArticles == 0) {
            return 0.0;
        }
        return (totalScore / totalArticles);
    }

    @Override
    public double computeDistanceBetween(SingleAuthorContainer author, ResearchArticle article) {
        if (author.getAuthorArticles().isEmpty()) {
            return 0.0;
        }
        List<ResearchArticle> authorArticleList = author.getAuthorArticles();
        double totalScore = 0.0, totalArticles = 0.0;
        for (ResearchArticle authorArticle : authorArticleList) {
            double score = this.computeDistanceBetween(authorArticle, article);
            boolean pruneFlag = this.pruneArticlePair(authorArticle, article);
            if (score > this.getThreshold() && !pruneFlag) {
                totalScore += score;
                totalArticles++;
            }
        }
        if (totalArticles == 0) {
            return 0.0;
        }
        return (totalScore / totalArticles);
    }

    @Override
    public abstract double computeDistanceBetween(ResearchArticle firstArticle, ResearchArticle secondArticle);

    @Override
    public abstract boolean pruneArticlePair(ResearchArticle firstArticle, ResearchArticle secondArticle);

    @Override
    public abstract double getThreshold();

    @Override
    public abstract String getStrategyName();

    @Override
    public abstract String getStrategyKey();
}
