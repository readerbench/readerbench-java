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
package com.readerbench.coreservices.sentimentanalysis;

import com.readerbench.datasourceprovider.data.AbstractDocument;
import com.readerbench.datasourceprovider.data.Block;
import com.readerbench.datasourceprovider.data.Sentence;
import com.readerbench.datasourceprovider.data.sentiment.SentimentEntity;
import com.readerbench.datasourceprovider.data.sentiment.SentimentValence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Mihai Dascalu
 */
public class SentimentAnalysis {

    private static final Logger LOGGER = LoggerFactory.getLogger(SentimentAnalysis.class);

    public static void weightSemanticValences(Sentence s) {
        if (s.getAllWords().isEmpty()) {
            return;
        }
        for (SentimentValence daoSe : SentimentValence.getAllValences()) {
            double value = s.getAllWords().stream().mapToDouble(w -> {
                SentimentEntity e = w.getSentiment();
                if (e == null) {
                    return 0.;
                }
                Double v = e.get(daoSe);
                return (v == null ? 0. : v);
            }).sum() / s.getAllWords().size();
            if (s.getSentimentEntity().getAll().containsKey(daoSe)) {
                continue;
            }
            s.getSentimentEntity().add(daoSe, value);
        }
    }

    public static void weightSemanticValences(Block b) {
        SentimentEntity se = new SentimentEntity();
        se.init();
        b.setSentimentEntity(se);
        Map<SentimentValence, Double> avgBlock = new HashMap<>();
        Map<SentimentValence, Double> sumWeightsBlock = new HashMap<>();
        for (int i = 0; i < b.getSentences().size(); i++) {
            Sentence s = b.getSentences().get(i);
            weightSemanticValences(s);
            for (Map.Entry<SentimentValence, Double> pair : s.getSentimentEntity().getAll().entrySet()) {
                SentimentValence sv = pair.getKey();
                Double value = pair.getValue();
                if (value != null) {
                    avgBlock.put(sv, (avgBlock.get(sv) == null ? 0 : avgBlock.get(sv))
                            + b.getSentenceBlockDistances()[i].getCohesion() * value);
                    sumWeightsBlock.put(sv, (sumWeightsBlock.get(sv) == null ? 0 : sumWeightsBlock.get(sv))
                            + b.getSentenceBlockDistances()[i].getCohesion());
                }
            }
        }
        avgBlock.entrySet().stream().forEach(e -> {
            b.getSentimentEntity().add(e.getKey(), e.getValue() / sumWeightsBlock.get(e.getKey()));
        });

    }

    public static void weightSemanticValences(AbstractDocument d) {
        LOGGER.info("Weighting sentiment valences ...");

        // initialize sentiment valence map for document
        SentimentEntity se = new SentimentEntity();
        se.init();
        d.setSentimentEntity(se);

        Map<SentimentValence, Double> avgDoc = new HashMap<>();
        Map<SentimentValence, Double> sumWeightsDoc = new HashMap<>();
        // perform weighted sentiment per block and per document

        for (int i = 0; i < d.getBlocks().size(); i++) {
            Block b = d.getBlocks().get(i);
            if (b != null) {
                weightSemanticValences(b);

                for (Map.Entry<SentimentValence, Double> pair : b.getSentimentEntity().getAll().entrySet()) {
                    SentimentValence sv = pair.getKey();
                    Double value = pair.getValue();
                    avgDoc.put(sv, (avgDoc.get(sv) == null ? 0 : avgDoc.get(sv))
                            + value * d.getBlockDocDistances()[i].getCohesion());
                    sumWeightsDoc.put(sv, (sumWeightsDoc.get(sv) == null ? 0 : sumWeightsDoc.get(sv))
                            + d.getBlockDocDistances()[i].getCohesion());
                }

            }
        }

        for (Map.Entry<SentimentValence, Double> pair : d.getSentimentEntity().getAll().entrySet()) {
            SentimentValence sv = pair.getKey();
            if (sumWeightsDoc.get(sv) != null) {
                d.getSentimentEntity().add(sv, avgDoc.get(sv) / sumWeightsDoc.get(sv));
            }
        }
    }
}
