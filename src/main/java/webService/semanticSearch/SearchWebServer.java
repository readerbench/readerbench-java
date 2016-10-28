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
package webService.semanticSearch;

import java.io.File;
import java.io.FilenameFilter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Path;
import org.simpleframework.xml.Root;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import data.AbstractDocument;
import data.AbstractDocumentTemplate;
import data.AbstractDocumentTemplate.BlockTemplate;
import data.document.Document;
import services.commons.Formatting;
import services.semanticSearch.SemanticSearch;
import services.semanticSearch.SemanticSearchResult;
import spark.Request;
import spark.Response;
import spark.Route;
import spark.Spark;

class SearchResult implements Comparable<SearchResult> {

    private String url;
    private String content;
    private double relevance;

    public SearchResult(String url, String content, double relevance) {
        this.url = url;
        this.content = content;
        this.relevance = relevance;
    }

    public String getUrl() {
        return url;
    }

    public String getContent() {
        return content;
    }

    public double getRelevance() {
        return relevance;
    }

    @Override
    public int compareTo(SearchResult o) {
        return (int) Math.signum(o.getRelevance() - this.getRelevance());
    }
}

public class SearchWebServer {

    private static Logger logger = Logger.getLogger("");
    public static final double MIN_THRESHOLD = 0.2d;
    public static final int NO_RESULTS = 20;

    private int port;
    private List<AbstractDocument> documents;

    public SearchWebServer(int port, String path) {
        this.port = port;
        setDocuments(path);
    }

    @Root(name = "response")
    private static class QueryResult {

        @Element
        public boolean success;

        @Element(name = "errormsg")
        public String errorMsg; // custom error message (optional)

        @Path("data")
        @ElementList(inline = true, entry = "result")
        public List<SearchResult> data; // list of query results (urls)

        QueryResult() {
            success = true;
            errorMsg = "";
            data = new ArrayList<>();
        }
    }

    public void setDocuments(String path) {
        documents = new ArrayList<>();

        File dir = new File(path);
        File[] files = dir.listFiles((File dir1, String name) -> name.endsWith(".ser"));

        for (File file : files) {
            Document d = (Document) AbstractDocument.loadSerializedDocument(file.getPath());
            documents.add(d);
        }
    }

    public void start() {
        Spark.port(port);
        Spark.get("/", (request, response) -> {
            return "OK";
        });

        Spark.get("/search", new Route() {

            @Override
            public Object handle(Request request, Response response) {
                response.type("application/json");
                String q = request.queryParams("q");
                int maxContentSize = Integer.MAX_VALUE;
                String maxContentSizeStr = request.queryParams("mcs");
                if (maxContentSizeStr != null) {
                    maxContentSize = Integer.parseInt(maxContentSizeStr);
                }
                QueryResult queryResult = new QueryResult();
                queryResult.success = true;
                queryResult.errorMsg = "";
                queryResult.data = search(q, documents, maxContentSize);
                String result = convertToJson(queryResult);
                return result;
            }

            /**
             * Search for query in documents
             *
             * @param documents
             * @param query
             * @return List of urls for results
             */
            private List<SearchResult> search(String query, List<AbstractDocument> documents, int maxContentSize) {
                logger.info("Processign query:" + query);
                AbstractDocumentTemplate contents = new AbstractDocumentTemplate();
                BlockTemplate block = contents.new BlockTemplate();
                block.setId(0);
                block.setContent(query);
                contents.getBlocks().add(block);

                AbstractDocument queryDoc = new Document(null, contents, documents.get(0).getSemanticModels(), documents.get(0).getLanguage(), true);
                queryDoc.computeAll(true);

                List<SemanticSearchResult> results = SemanticSearch.search(queryDoc, documents, MIN_THRESHOLD,
                        NO_RESULTS);
                List<SearchResult> searchResults = new ArrayList<SearchResult>();
                for (SemanticSearchResult r : results) {
                    String content = r.getDoc().getText();
                    if (content.length() > maxContentSize) {
                        content = content.substring(0, maxContentSize);
                        if (content.lastIndexOf(" ") != -1) {
                            content = content.substring(0, content.lastIndexOf(" "));
                        }
                        content += "...";
                    }
                    searchResults.add(
                            new SearchResult(r.getDoc().getPath(), content, Formatting.formatNumber(r.getRelevance())));
                }

                return searchResults;
            }

            private String convertToXml(QueryResult queryResult) {
                Serializer serializer = new Persister();
                StringWriter result = new StringWriter();
                try {
                    serializer.write(queryResult, result);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                return result.toString();
            }

            private String convertToJson(QueryResult queryResult) {
                Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().create();
                String json = gson.toJson(queryResult);
                return json;
            }
        });
    }
}
