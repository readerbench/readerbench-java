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
package com.readerbench.services.extendedCNA;

import com.readerbench.data.article.ResearchArticle;
import com.readerbench.services.extendedCNA.distanceStrategies.IAuthorDistanceStrategy;

public class GraphNodeItem implements Comparable<GraphNodeItem> {

    private final GraphNodeItemType nodeType;

    ResearchArticle article;
    SingleAuthorContainer author;

    public GraphNodeItem(ResearchArticle article) {
        this.nodeType = GraphNodeItemType.Article;
        this.article = article;
    }

    public GraphNodeItem(SingleAuthorContainer author) {
        this.nodeType = GraphNodeItemType.Author;
        this.author = author;
    }

    public GraphNodeItemType getNodeType() {
        return this.nodeType;
    }

    public String getURI() {
        switch (this.nodeType) {
            case Article:
                return this.article.getURI();
            case Author:
                return this.author.getAuthor().getAuthorUri();
        }
        return "";
    }

    public String getName() {
        switch (this.nodeType) {
            case Article:
                return this.article.getTitleText();
            case Author:
                return this.author.getAuthor().getAuthorName();
        }
        return "";
    }

    public boolean isArticle() {
        return this.nodeType == GraphNodeItemType.Article;
    }

    public boolean isAuthor() {
        return this.nodeType == GraphNodeItemType.Author;
    }

    public int getNoOfReferences() {
        if (this.isArticle()) {
            return this.article.getCitationURIList().size();
        }
        return 0;
    }

    @Override
    public int hashCode() {
        return this.getURI().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == null || obj == null || !(obj instanceof GraphNodeItem)) {
            return false;
        }
        GraphNodeItem otherNode = (GraphNodeItem) obj;
        return this.nodeType == otherNode.nodeType && this.getURI().equals(otherNode.getURI());
    }

    public double computeScore(GraphNodeItem otherItem, IAuthorDistanceStrategy distanceStrategy) {
        if (this.isArticle()) {
            if (otherItem.isArticle()) {
                return distanceStrategy.computeDistanceBetween(this.article, otherItem.article);
            } else {
                return distanceStrategy.computeDistanceBetween(otherItem.author, this.article);
            }
        } else {
            if (otherItem.isArticle()) {
                return distanceStrategy.computeDistanceBetween(this.author, otherItem.article);
            } else {
                return distanceStrategy.computeDistanceBetween(otherItem.author, this.author);
            }
        }
    }

    @Override
    public int compareTo(GraphNodeItem otherNode) {
        if (this.nodeType != otherNode.nodeType) {
            if (this.isArticle()) {
                return -1;
            }
            return 1;
        }
        if (this.nodeType == GraphNodeItemType.Article) {
            return this.article.compareTo(otherNode.article);
        }
        return this.author.compareTo(otherNode.author);
    }
}
