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
package com.readerbench.coreservices.data.document;

import com.readerbench.coreservices.data.AbstractDocument;
import com.readerbench.coreservices.semanticmodels.data.ISemanticModel;
import com.readerbench.datasourceprovider.pojo.Lang;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 *
 * @author Stefan Ruseti
 */
public class MetaDocument extends Document {

    private static final Logger LOGGER = LoggerFactory.getLogger(MetaDocument.class);

    public static enum DocumentLevel {
        Document, Chapter, Section, Subsection
    };

    private final List<AbstractDocument> children = new ArrayList<>();
    private DocumentLevel level;

    public MetaDocument(String path, List<ISemanticModel> models, Lang lang) {
        super(path, models, lang);
    }

    private Stream<Document> getLeavesStream() {
        Stream doc;
        if (getBlocks().isEmpty()) {
            doc = Stream.empty();
        } else {
            doc = Stream.of(this);
        }
        return Stream.concat(doc, children.stream().flatMap(child -> {
            if (child instanceof MetaDocument) {
                MetaDocument md = (MetaDocument) child;
                return md.getLeavesStream();
            }
            return Stream.of((Document) child);
        }));
    }

    public List<Document> getLeaves() {
        return getLeavesStream().collect(Collectors.toList());
    }

    public void addInfo(Document doc) {
        for (Method getter : Document.class.getMethods()) {
            if (getter.getName().startsWith("get") && getter.getParameterCount() == 0) {
                try {
                    Method setter = MetaDocument.class.getMethod("s" + getter.getName().substring(1), getter.getReturnType());
                    setter.invoke(this, getter.invoke(doc));
                } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                    LOGGER.error(ex.getMessage());
                }
            }
        }
    }

    @Override
    public int getNoBlocks() {
        return super.getNoBlocks()
                + children.stream().mapToInt(AbstractDocument::getNoBlocks).sum();
    }

    @Override
    public int getNoSentences() {
        return super.getNoSentences()
                + children.stream().mapToInt(AbstractDocument::getNoSentences).sum();
    }

    @Override
    public int getNoWords() {
        return super.getNoWords()
                + children.stream().mapToInt(AbstractDocument::getNoWords).sum();
    }

    @Override
    public int getNoContentWords() {
        return super.getNoContentWords()
                + children.stream().mapToInt(AbstractDocument::getNoContentWords).sum();
    }

    public void setLevel(DocumentLevel level) {
        this.level = level;
    }

    public DocumentLevel getLevel() {
        return level;
    }

    public List<AbstractDocument> getChildren() {
        return children;
    }

    public static DocumentLevel getDocumentLevelOfElement(Element elem) {
        String level = elem.getAttribute("type");
        switch (level.toLowerCase()) {
            case "document":
                return DocumentLevel.Document;
            case "chapter":
                return DocumentLevel.Chapter;
            case "subsection":
                return DocumentLevel.Subsection;
            default:
                return DocumentLevel.Section;
        }
    }
}
