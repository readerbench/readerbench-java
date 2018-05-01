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
package com.readerbench.coreservices.data.article;

public class ArticleAuthor implements java.io.Serializable {

    private static final long serialVersionUID = 1L;
    String authorName;
    String authorUri;
    String affiliationName;
    String affiliationUri;

    @Override
    public String toString() {
        return "{" + this.authorUri + ", " + this.authorName + ", " + this.affiliationUri + ", " + this.affiliationName + "}";
    }

    public String getAuthorName() {
        return this.authorName;
    }

    public String getAuthorUri() {
        return this.authorUri;
    }

    public String getAffiliationName() {
        return this.affiliationName;
    }

    public String getAffiliationUri() {
        return this.affiliationUri;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    public void setAuthorUri(String authorUri) {
        this.authorUri = authorUri;
    }

    public void setAffiliationName(String affiliationName) {
        this.affiliationName = affiliationName;
    }

    public void setAffiliationUri(String affiliationUri) {
        this.affiliationUri = affiliationUri;
    }

    public boolean isSameAuthor(ArticleAuthor a) {
        return this.authorUri.equals(a.authorUri);
    }

    public boolean isSameAffiliation(ArticleAuthor a) {
        return this.affiliationUri.equals(a.affiliationUri);
    }
}
