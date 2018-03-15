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

import com.readerbench.readerbenchcore.data.article.ResearchArticle;
import com.readerbench.readerbenchcore.extendedCNA.ArticleContainer;

public class SemanticAuthorPruneByCocitOrCuAuthDistanceStrategy extends SemanticAuthorDistanceStrategy {

    private final CoCitationsDistanceStrategy coCitationsStrategy;
    private final CoAuthorshipDistanceStrategy coAuthorshipStrategy;

    public SemanticAuthorPruneByCocitOrCuAuthDistanceStrategy(ArticleContainer authorContainer) {
        super(authorContainer);
        this.coCitationsStrategy = new CoCitationsDistanceStrategy(authorContainer);
        this.coAuthorshipStrategy = new CoAuthorshipDistanceStrategy(authorContainer);
    }

    @Override
    public boolean pruneArticlePair(ResearchArticle firstArticle, ResearchArticle secondArticle) {
        double coCitationsScore = this.coCitationsStrategy.computeDistanceBetween(firstArticle, secondArticle);
        double coAuthorshipScore = this.coAuthorshipStrategy.computeDistanceBetween(firstArticle, secondArticle);
        return coCitationsScore == 0 && coAuthorshipScore == 0;
    }

    @Override
    public String getStrategyName() {
        return "Semantic Distance Prunned By Co Citations or Co Authorship";
    }

    @Override
    public String getStrategyKey() {
        return "SemanticPrunnedByCoCitOrCoAuth";
    }
}
