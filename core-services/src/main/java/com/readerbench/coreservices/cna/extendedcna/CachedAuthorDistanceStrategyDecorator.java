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
package com.readerbench.coreservices.cna.extendedcna;

import com.readerbench.coreservices.data.article.ResearchArticle;
import com.readerbench.coreservices.cna.extendedcna.distancestrategies.AuthorDistanceStrategyType;
import com.readerbench.coreservices.cna.extendedcna.distancestrategies.IAuthorDistanceStrategy;

import java.util.HashMap;
import java.util.Map;

public class CachedAuthorDistanceStrategyDecorator implements IAuthorDistanceStrategy {

    private final IAuthorDistanceStrategy distanceStrategy;
    private final Map<String, Double> singleAuthorContainerDistanceCache;
    private final Map<String, Double> researchArticleDistanceCache;
    private final Map<String, Double> authorToArticleDistanceCache;

    public CachedAuthorDistanceStrategyDecorator(ArticleContainer authorContainer, IAuthorDistanceStrategy distanceStrategy) {
        this.distanceStrategy = distanceStrategy;
        this.singleAuthorContainerDistanceCache = new HashMap<>();
        this.researchArticleDistanceCache = new HashMap<>();
        this.authorToArticleDistanceCache = new HashMap<>();
    }

    @Override
    public double computeDistanceBetween(SingleAuthorContainer firstAuthor, SingleAuthorContainer secondAuthor) {
        String key1 = firstAuthor.getAuthor().getAuthorUri() + secondAuthor.getAuthor().getAuthorUri();
        if (singleAuthorContainerDistanceCache.containsKey(key1)) {
            return singleAuthorContainerDistanceCache.get(key1);
        }

        String key2 = secondAuthor.getAuthor().getAuthorUri() + firstAuthor.getAuthor().getAuthorUri();
        if (singleAuthorContainerDistanceCache.containsKey(key2)) {
            return singleAuthorContainerDistanceCache.get(key2);
        }

        Double distance = this.distanceStrategy.computeDistanceBetween(firstAuthor, secondAuthor);
        singleAuthorContainerDistanceCache.put(key1, distance);
        return distance;
    }

    @Override
    public double computeDistanceBetween(ResearchArticle firstArticle, ResearchArticle secondArticle) {
        String key1 = firstArticle.getURI() + secondArticle.getURI();
        if (this.researchArticleDistanceCache.containsKey(key1)) {
            return this.researchArticleDistanceCache.get(key1);
        }

        String key2 = secondArticle.getURI() + firstArticle.getURI();
        if (this.researchArticleDistanceCache.containsKey(key2)) {
            return this.researchArticleDistanceCache.get(key2);
        }

        Double distance = this.distanceStrategy.computeDistanceBetween(firstArticle, secondArticle);
        researchArticleDistanceCache.put(key1, distance);
        return distance;
    }

    @Override
    public double computeDistanceBetween(SingleAuthorContainer author, ResearchArticle article) {
        String key = author.getAuthor().getAuthorUri() + article.getURI();
        if (this.authorToArticleDistanceCache.containsKey(key)) {
            return this.authorToArticleDistanceCache.get(key);
        }

        Double distance = this.distanceStrategy.computeDistanceBetween(author, article);
        authorToArticleDistanceCache.put(key, distance);
        return distance;
    }

    @Override
    public boolean pruneArticlePair(ResearchArticle firstArticle, ResearchArticle secondArticle) {
        return this.distanceStrategy.pruneArticlePair(firstArticle, secondArticle);
    }

    @Override
    public double getThreshold() {
        return this.distanceStrategy.getThreshold();
    }

    @Override
    public String getStrategyName() {
        return this.distanceStrategy.getStrategyName();
    }

    @Override
    public String getStrategyKey() {
        return this.distanceStrategy.getStrategyKey();
    }

    @Override
    public AuthorDistanceStrategyType getStrategyType() {
        return this.distanceStrategy.getStrategyType();
    }
}
