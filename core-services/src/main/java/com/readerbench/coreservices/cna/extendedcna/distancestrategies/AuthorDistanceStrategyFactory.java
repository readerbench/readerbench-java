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
package com.readerbench.coreservices.cna.extendedcna.distancestrategies;

import com.readerbench.coreservices.cna.extendedcna.ArticleContainer;

public class AuthorDistanceStrategyFactory {

    private final ArticleContainer authorContainer;

    public AuthorDistanceStrategyFactory(ArticleContainer authorContainer) {
        this.authorContainer = authorContainer;
    }

    public IAuthorDistanceStrategy getDistanceStrategy(AuthorDistanceStrategyType strategyType) {
        switch (strategyType) {
            case SemanticDistance:
                return new SemanticAuthorDistanceStrategy(this.authorContainer);
            case SemanticPrunnedByCoCitOrCoAuth:
                return new SemanticAuthorPruneByCocitOrCuAuthDistanceStrategy(this.authorContainer);
            case CoAuthorshipDistance:
                return new CoAuthorshipDistanceStrategy(this.authorContainer);
            case CoCitationsDistance:
                return new CoCitationsDistanceStrategy(this.authorContainer);
        }
        return null;
    }
}
