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

import com.readerbench.data.AbstractDocument;
import com.readerbench.data.article.ArticleAuthor;
import com.readerbench.data.article.ResearchArticle;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ArticleContainer {

    private final List<SingleAuthorContainer> authorContainers;
    private final List<ResearchArticle> articles;

    public ArticleContainer(List<ResearchArticle> articles) {
        this.articles = articles;
        this.authorContainers = new ArrayList<>();
        this.indexAuthorsFromArticles();
    }

    private void indexAuthorsFromArticles() {
        for (ResearchArticle article : articles) {
            List<ArticleAuthor> authorsForArticle = article.getArticleAuthorList();
            for (ArticleAuthor author : authorsForArticle) {
                SingleAuthorContainer authorContainer = this.getSingleAuthorContainerAssociatedWith(author);
                if (authorContainer == null) {
                    List<ResearchArticle> authorArticles = new ArrayList<>();
                    authorArticles.add(article);
                    this.authorContainers.add(new SingleAuthorContainer(author, authorArticles));
                } else {
                    authorContainer.addResearchArticle(article);
                }
            }
        }
    }

    private SingleAuthorContainer getSingleAuthorContainerAssociatedWith(ArticleAuthor author) {
        for (SingleAuthorContainer authorContainer : this.authorContainers) {
            if (authorContainer.isSameAuthor(author)) {
                return authorContainer;
            }
        }
        return null;
    }

    public List<SingleAuthorContainer> getAuthorContainers() {
        return this.authorContainers;
    }

    public List<ResearchArticle> getArticles() {
        return this.articles;
    }

    public static ArticleContainer buildAuthorContainerFromDirectory(String dirName) {
        File dir = new File(dirName);
        File[] files = dir.listFiles((File dir1, String name) -> name.endsWith(".ser"));
        List<ResearchArticle> articles = Stream.of(files)
                .parallel()
                .map(file -> {
                    try {
                        return (ResearchArticle) AbstractDocument.loadSerializedDocument(file.getPath());
                    } catch (IOException | ClassNotFoundException ex) {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .filter(d -> !d.getBlocks().isEmpty())
                .collect(Collectors.toList());
        return new ArticleContainer(articles);
    }

}
