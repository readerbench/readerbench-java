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
package webService.services;

import data.AbstractDocument;
import data.Block;
import data.Sentence;
import data.Word;
import data.sentiment.SentimentEntity;
import data.sentiment.SentimentValence;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Logger;
import webService.result.ResultSentiment;
import webService.result.ResultValence;

public class SentimentAnalysis {

    private static final Logger LOGGER = Logger.getLogger("");

    public static final Integer GRANULARITY_DOCUMENT = 1;
    public static final Integer GRANULARITY_PARAGRAPH = 2;
    public static final Integer GRANULARITY_SENTENCE = 3;
    public static final Integer GRANULARITY_WORD = 4;

    /**
     * Get sentiment values for the entire document and for each paragraph
     *
     * @param doc The document to be analyzed
     * @param granularity Granularity of the sentiment analysis process
     *
     * @return List of sentiment values per entity
     * @throws java.lang.Exception
     */
    public static List<ResultSentiment> computeSentiments(AbstractDocument doc, Integer granularity) throws Exception {
        if (granularity == null || !Arrays.asList(GRANULARITY_DOCUMENT, GRANULARITY_PARAGRAPH, GRANULARITY_SENTENCE, GRANULARITY_WORD).contains(granularity)) {
            granularity = GRANULARITY_DOCUMENT;
        }
        Map<SentimentValence, Double> semtiments;
        Iterator<Map.Entry<SentimentValence, Double>> it;
        List<ResultValence> localResults;

        LOGGER.info("Starting building sentiments...");
        List<ResultSentiment> sentiments = new ArrayList<>();
        if (Objects.equals(granularity, GRANULARITY_DOCUMENT)) {
            semtiments = doc.getSentimentEntity().getAggregatedValue();
            it = semtiments.entrySet().iterator();
            localResults = new ArrayList<>();
            while (it.hasNext()) {
                Map.Entry<SentimentValence, Double> pair = (Map.Entry<SentimentValence, Double>) it.next();
                SentimentValence sentimentValence = (SentimentValence) pair.getKey();
                localResults.add(new ResultValence(sentimentValence.getIndexLabel().replace("_RAGE", ""), pair.getValue()));
            }
            if (!localResults.isEmpty()) {
                Collections.sort(localResults);
                sentiments.add(new ResultSentiment(doc.getText(), localResults));
            }
        } else {
            for (Block b : doc.getBlocks()) {
                if (Objects.equals(granularity, GRANULARITY_PARAGRAPH)) {
                    semtiments = b.getSentimentEntity().getAggregatedValue();
                    it = semtiments.entrySet().iterator();
                    localResults = new ArrayList<>();
                    while (it.hasNext()) {
                        Map.Entry<SentimentValence, Double> pair = (Map.Entry<SentimentValence, Double>) it.next();
                        SentimentValence sentimentValence = (SentimentValence) pair.getKey();
                        localResults.add(new ResultValence(sentimentValence.getIndexLabel().replace("_RAGE", ""), pair.getValue()));
                    }
                    if (!localResults.isEmpty()) {
                        Collections.sort(localResults);
                        sentiments.add(new ResultSentiment(b.getText() + "\n", localResults));
                    }
                } else {
                    for (Sentence s : b.getSentences()) {
                        if (Objects.equals(granularity, GRANULARITY_SENTENCE)) {
                            semtiments = s.getSentimentEntity().getAggregatedValue();
                            it = semtiments.entrySet().iterator();
                            localResults = new ArrayList<>();
                            while (it.hasNext()) {
                                Map.Entry<SentimentValence, Double> pair = (Map.Entry<SentimentValence, Double>) it.next();
                                SentimentValence sentimentValence = (SentimentValence) pair.getKey();
                                localResults.add(new ResultValence(sentimentValence.getIndexLabel().replace("_RAGE", ""), pair.getValue()));
                            }
                            if (!localResults.isEmpty()) {
                                Collections.sort(localResults);
                                sentiments.add(new ResultSentiment(s.getText() + ((s.getIndex() == b.getSentences().size() - 1) ? "\n" : ""), localResults));
                            }
                        } else {
                            for (Word w : s.getAllWords()) {
                                SentimentEntity se = w.getSentiment();
                                if (se == null) {
                                    continue;
                                }
                                semtiments = se.getAggregatedValue();
                                it = semtiments.entrySet().iterator();
                                localResults = new ArrayList<>();
                                while (it.hasNext()) {
                                    Map.Entry<SentimentValence, Double> pair = (Map.Entry<SentimentValence, Double>) it.next();
                                    SentimentValence sentimentValence = (SentimentValence) pair.getKey();
                                    localResults.add(new ResultValence(sentimentValence.getIndexLabel().replace("_RAGE", ""), pair.getValue()));
                                }
                                if (!localResults.isEmpty()) {
                                    Collections.sort(localResults);
                                    sentiments.add(new ResultSentiment(w.getText() + (((w.getIndex() == s.getWords().size() - 1) && (s.getIndex() == b.getSentences().size() - 1)) ? "\n" : ""), localResults));
                                }
                            }
                        }
                    }
                }
            }
        }

        return sentiments;
    }
}
