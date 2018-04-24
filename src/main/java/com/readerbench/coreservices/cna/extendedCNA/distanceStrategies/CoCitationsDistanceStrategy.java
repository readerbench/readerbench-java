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
package com.readerbench.coreservices.cna.extendedCNA.distanceStrategies;

import com.readerbench.datasourceprovider.data.article.ResearchArticle;
import com.readerbench.coreservices.cna.extendedCNA.ArticleContainer;
import com.readerbench.coreservices.cna.extendedCNA.SingleAuthorContainer;

import java.util.List;

public class CoCitationsDistanceStrategy extends AAuthorDistanceStrategy {

    private final ArticleContainer authorContainer;
    private double maxCoCitationsBetweenArticles;

    public CoCitationsDistanceStrategy(ArticleContainer authorContainer) {
        this.authorContainer = authorContainer;
        this.buildMaxCoCitationsBetweenArticles();
    }

    private void buildMaxCoCitationsBetweenArticles() {
        this.maxCoCitationsBetweenArticles = 0.0;
        List<ResearchArticle> articleList = authorContainer.getArticles();
        for (int i = 0; i < articleList.size(); i++) {
            for (int j = i + 1; j < articleList.size(); j++) {
                ResearchArticle a1 = articleList.get(i);
                ResearchArticle a2 = articleList.get(j);

                double noOfCoCitations = SingleAuthorContainer.getNoOfCoCitationsBetweenArticles(a1, a2);
                if (noOfCoCitations > this.maxCoCitationsBetweenArticles) {
                    this.maxCoCitationsBetweenArticles = noOfCoCitations;
                }
            }
        }
    }

    @Override
    public double computeDistanceBetween(ResearchArticle firstArticle, ResearchArticle secondArticle) {
        if (this.maxCoCitationsBetweenArticles == 0) {
            return 0.0;
        }
        if (firstArticle.getURI().equals(secondArticle.getURI())) {
            return 1.0;
        }
        double noOfCoCitations = SingleAuthorContainer.getNoOfCoCitationsBetweenArticles(firstArticle, secondArticle);
        return noOfCoCitations / this.maxCoCitationsBetweenArticles;
    }

    @Override
    public boolean pruneArticlePair(ResearchArticle firstArticle, ResearchArticle secondArticle) {
        return false;
    }

    @Override
    public double getThreshold() {
        return 0.000000000001;
    }

    @Override
    public String getStrategyName() {
        return "Co-Citations Distance";
    }

    @Override
    public String getStrategyKey() {
        return "CoCitations";
    }

    @Override
    public AuthorDistanceStrategyType getStrategyType() {
        return AuthorDistanceStrategyType.CoCitationsDistance;
    }
}
