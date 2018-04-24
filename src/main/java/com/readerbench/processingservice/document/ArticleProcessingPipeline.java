/*
 * Copyright 2018 ReaderBench.
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
package com.readerbench.processingservice.document;

import com.readerbench.coreservices.cna.extendedCNA.ArticleContainer;
import com.readerbench.datasourceprovider.data.article.ResearchArticle;
import com.readerbench.processingservice.ImportDocument;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 *
 * @author ReaderBench
 */
public class ArticleProcessingPipeline {

    public static ArticleContainer buildAuthorContainerFromDirectory(String dirName) {
        File dir = new File(dirName);
        File[] files = dir.listFiles((File dir1, String name) -> name.endsWith(".ser"));
        final ImportDocument id = new ImportDocument();
        List<ResearchArticle> articles = Stream.of(files)
                .parallel()
                .map(file -> {
                    try {
                        return (ResearchArticle) id.importSerializedDocument(file.getPath());
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
