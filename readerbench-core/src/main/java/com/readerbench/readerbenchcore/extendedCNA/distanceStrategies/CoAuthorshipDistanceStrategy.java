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
package com.readerbench.readerbenchcore.extendedCNA.distanceStrategies;

import com.readerbench.readerbenchcore.data.article.ArticleAuthor;
import com.readerbench.readerbenchcore.data.article.ResearchArticle;
import com.readerbench.readerbenchcore.extendedCNA.ArticleContainer;
import com.readerbench.readerbenchcore.extendedCNA.SingleAuthorContainer;

import java.util.List;

public class CoAuthorshipDistanceStrategy extends AAuthorDistanceStrategy {

    private final ArticleContainer authorContainer;
    private double maxNoOfCommonAuthorsBetweenArticles;

    public CoAuthorshipDistanceStrategy(ArticleContainer authorContainer) {
        this.authorContainer = authorContainer;
        this.buildMaxNoOfCommonAuthorsBetweenArticles();
    }

    private void buildMaxNoOfCommonAuthorsBetweenArticles() {
        this.maxNoOfCommonAuthorsBetweenArticles = 0;
        List<ResearchArticle> articleList = authorContainer.getArticles();
        for (int i = 0; i < articleList.size(); i++) {
            for (int j = i + 1; j < articleList.size(); j++) {
                ResearchArticle a1 = articleList.get(i);
                ResearchArticle a2 = articleList.get(j);
                double noCommonAuthors = this.getNoOfCommonAuthorsBetween(a1, a2);
                if (noCommonAuthors > this.maxNoOfCommonAuthorsBetweenArticles) {
                    this.maxNoOfCommonAuthorsBetweenArticles = noCommonAuthors;
                }
            }
        }
    }

    private double getNoOfCommonAuthorsBetween(ResearchArticle a1, ResearchArticle a2) {
        double noCommonAuthors = 0;
        List<ArticleAuthor> a1Authors = a1.getArticleAuthorList();
        for (ArticleAuthor a1Author : a1Authors) {
            if (this.articleHasAuthor(a2, a1Author)) {
                noCommonAuthors++;
            }
        }
        return noCommonAuthors;
    }

    private boolean articleHasAuthor(ResearchArticle article, ArticleAuthor author) {
        if (article.getArticleAuthorList().stream().anyMatch((articleAuthor) -> (articleAuthor.isSameAuthor(author)))) {
            return true;
        }
        return false;
    }

    @Override
    public double computeDistanceBetween(ResearchArticle firstArticle, ResearchArticle secondArticle) {
        if (this.maxNoOfCommonAuthorsBetweenArticles == 0) {
            return 0.0;
        }
        if (firstArticle.getURI().equals(secondArticle.getURI())) {
            return 1.0;
        }
        double noCommonAuthors = this.getNoOfCommonAuthorsBetween(firstArticle, secondArticle);
        return noCommonAuthors / this.maxNoOfCommonAuthorsBetweenArticles;
    }

    @Override
    public double computeDistanceBetween(SingleAuthorContainer author, ResearchArticle article) {
        if (this.articleHasAuthor(article, author.getAuthor())) {
            return 1.0;
        }
        return 0.0;
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
        return "Co-Authorship Distance";
    }

    @Override
    public String getStrategyKey() {
        return "CoAuthorship";
    }

    @Override
    public AuthorDistanceStrategyType getStrategyType() {
        return AuthorDistanceStrategyType.CoAuthorshipDistance;
    }
}
