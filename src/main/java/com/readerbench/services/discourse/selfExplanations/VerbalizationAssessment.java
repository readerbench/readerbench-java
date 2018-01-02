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
package com.readerbench.services.discourse.selfExplanations;

import com.readerbench.data.Block;
import com.readerbench.data.discourse.SemanticCohesion;
import com.readerbench.data.document.Metacognition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VerbalizationAssessment {

    private static final Logger LOGGER = LoggerFactory.getLogger(VerbalizationAssessment.class);

    public static void detRefBlockSimilarities(Metacognition metacognition) {
        LOGGER.info("Building metacognition block similarities");

        // determine similarities with previous blocks from referred document
        metacognition.setBlockSimilarities(new SemanticCohesion[metacognition.getReferredDoc().getBlocks().size()]);

        int startIndex = 0;
        int endIndex = 0;
        for (Block v : metacognition.getBlocks()) {
            if (v.getRefBlock() != null) {
                endIndex = v.getRefBlock().getIndex();
                for (int refBlockId = startIndex; refBlockId <= endIndex; refBlockId++) {
                    metacognition.getBlockSimilarities()[refBlockId] = new SemanticCohesion(v,
                            metacognition.getReferredDoc().getBlocks().get(refBlockId));
                }
                startIndex = endIndex + 1;
            }
        }
    }
}
