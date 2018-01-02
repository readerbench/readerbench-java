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

public interface IAuthorDistanceStrategy {

    public double computeDistanceBetween(SingleAuthorContainer firstAuthor, SingleAuthorContainer secondAuthor);

    public double computeDistanceBetween(ResearchArticle firstArticle, ResearchArticle secondArticle);

    public double computeDistanceBetween(SingleAuthorContainer author, ResearchArticle article);

    public boolean pruneArticlePair(ResearchArticle firstArticle, ResearchArticle secondArticle);

    public double getThreshold();

    public String getStrategyName();

    public String getStrategyKey();

    public AuthorDistanceStrategyType getStrategyType();
}
