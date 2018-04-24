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
package com.readerbench.datasourceprovider.data.document;

import com.readerbench.datasourceprovider.data.AbstractDocument;
import com.readerbench.datasourceprovider.data.Word;
import com.readerbench.datasourceprovider.data.semanticmodels.ISemanticModel;
import com.readerbench.datasourceprovider.pojo.Lang;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Document extends AbstractDocument implements Comparable<Document> {

    private static final Logger LOGGER = LoggerFactory.getLogger(Document.class);

    private static final long serialVersionUID = 9219491499980439567L;

    private List<String> authors;
    private String uri;
    private String source;
    private String complexityLevel;
    private Date date;
    private List<Word> initialTopics;

    public Document(String path, List<ISemanticModel> models, Lang lang) {
        super(path, models, lang);
        this.authors = new ArrayList<>();
        this.initialTopics = new ArrayList<>();
    }

    public List<String> getAuthors() {
        return authors;
    }

    public void setAuthors(List<String> authors) {
        this.authors = authors;
    }

    public String getURI() {
        return uri;
    }

    public void setURI(String uri) {
        this.uri = uri;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getComplexityLevel() {
        return complexityLevel;
    }

    public void setComplexityLevel(String complexityLevel) {
        this.complexityLevel = complexityLevel;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getFullDescription() {
        StringBuilder descr = new StringBuilder();
        if (this.getPath() != null) {
            descr.append(this.getPath()).append("_");
        }
        if (this.getTitleText() != null) {
            descr.append(this.getTitleText()).append("_");
        }
        for (ISemanticModel model : getSemanticModelsAsList()) {
            descr.append(model.getPath()).append("_");
        }
        if (this.getAuthors() != null) {
            descr.append(this.getAuthors().stream().map((author) -> author + "_").reduce("", String::concat));
        }
        if (this.getText() != null) {
            descr.append(this.getText()).append("_");
        }
        return descr.toString();
    }

    @Override
    public int hashCode() {
        return this.getFullDescription().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == null || obj == null) {
            return false;
        }
        Document d = (Document) obj;
        return this.getFullDescription().equals(d.getFullDescription());
    }

    @Override
    public int compareTo(Document d) {
        String compare1 = "", compare2 = "";
        if (this.getPath() != null && d.getPath() != null) {
            compare1 += this.getPath() + "_";
            compare2 += d.getPath() + "_";
        }
        if (this.getTitleText() != null && d.getTitleText() != null) {
            compare1 += this.getTitleText() + "_";
            compare2 += d.getTitleText() + "_";
        }
        if (this.getAuthors() != null && d.getAuthors() != null) {
            compare1 = this.getAuthors().stream().map((author) -> author + "_").reduce(compare1, String::concat);
            compare2 = d.getAuthors().stream().map((author) -> author + "_").reduce(compare2, String::concat);
        }
        if (this.getText() != null && d.getText() != null) {
            compare1 += this.getText() + "_";
            compare2 += d.getText() + "_";
        }
        return compare1.compareTo(compare2);
    }

    public List<Word> getInitialTopics() {
        return initialTopics;
    }

    public void setInitialTopics(List<Word> initialTopics) {
        this.initialTopics = initialTopics;
    }
}
