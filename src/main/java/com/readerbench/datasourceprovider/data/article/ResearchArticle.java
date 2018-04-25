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
package com.readerbench.datasourceprovider.data.article;

import com.readerbench.datasourceprovider.data.document.Document;
import com.readerbench.datasourceprovider.data.semanticmodels.ISemanticModel;
import com.readerbench.datasourceprovider.pojo.Lang;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.ArrayList;
import java.util.List;

public class ResearchArticle extends Document {

    private static final long serialVersionUID = 9219491499980439567L;

    private static final Logger LOGGER = LoggerFactory.getLogger(ResearchArticle.class);

    private List<String> citationURIList = new ArrayList<>();
    private List<ArticleAuthor> articleAuthorList = new ArrayList<>();

    public ResearchArticle(String path, List<ISemanticModel> models, Lang lang) {
        super(path, models, lang);
    }

    public List<String> getCitationURIList() {
        return citationURIList;
    }

    public void setCitationURIList(List<String> citationURIList) {
        this.citationURIList = citationURIList;
    }

    public List<ArticleAuthor> getArticleAuthorList() {
        return articleAuthorList;
    }

    public void setArticleAuthorList(List<ArticleAuthor> articleAuthorList) {
        this.articleAuthorList = articleAuthorList;
    }

    @Override
    public int hashCode() {
        return this.getURI().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == null || obj == null || !(obj instanceof ResearchArticle)) {
            return false;
        }
        ResearchArticle a = (ResearchArticle) obj;
        return this.getURI().equals(a.getURI());
    }
}
