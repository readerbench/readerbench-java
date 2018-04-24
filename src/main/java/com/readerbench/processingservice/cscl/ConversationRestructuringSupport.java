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
package com.readerbench.processingservice.cscl;

import com.readerbench.datasourceprovider.data.AbstractDocumentTemplate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author ReaderBench
 */
public class ConversationRestructuringSupport {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(ConversationRestructuringSupport.class);

    private Map<Integer, Integer> initialMapping;
    private List<AbstractDocumentTemplate.BlockTemplate> newBlocks;

    public List<AbstractDocumentTemplate.BlockTemplate> getNewBlocks() {
        return newBlocks;
    }

    public void setNewBlocks(List<AbstractDocumentTemplate.BlockTemplate> newBlocks) {
        this.newBlocks = newBlocks;
    }

    public Map<Integer, Integer> getInitialMapping() {
        return initialMapping;
    }

    public void setInitialMapping(Map<Integer, Integer> initialMapping) {
        this.initialMapping = initialMapping;
    }

    public void mergeAdjacentContributions(List<AbstractDocumentTemplate.BlockTemplate> blocks) {
        //initialization: create mapping between block IDs and initial index positions in array
        initialMapping = new TreeMap<>();
        for (int i = 0; i < blocks.size(); i++) {
            if (blocks.get(i) != null && blocks.get(i).getId() != null) {
                initialMapping.put(blocks.get(i).getId(), i);
            }
        }

        //first iteration: merge contributions which have same speaker and timeframe <= 1 minute and no explicit ref other than previous contribution
        for (int i = blocks.size() - 1; i > 0; i--) {
            if (blocks.get(i) != null && blocks.get(i - 1) != null) {
                AbstractDocumentTemplate.BlockTemplate crt = blocks.get(i);
                AbstractDocumentTemplate.BlockTemplate prev = blocks.get(i - 1);
                if (crt.getTime() == null || prev.getTime() == null) {
                    continue;
                }
                long diffMinutes = (crt.getTime().getTime() - prev.getTime().getTime()) / (60 * 1000);

                //check if an explicit ref exists; in that case, perform merge only if link is between crt and previous contribution
                boolean explicitRefCriterion = true;
                if (crt.getRefId() != null && crt.getRefId() != 0 && (!crt.getRefId().equals(prev.getId()))) {
                    explicitRefCriterion = false;
                }
                if (crt.getSpeaker().equals(prev.getSpeaker()) && diffMinutes <= 1 && explicitRefCriterion) {
                    LOGGER.info("Merging contributions with IDs {} and {}", new Object[]{prev.getId(), crt.getId()});
                    prev.setContent(prev.getContent() + ". " + crt.getContent());
                    blocks.set(i, null);
                }
            }
        }

        //update refId
        for (AbstractDocumentTemplate.BlockTemplate b : blocks) {
            if (b != null) {
                if (b.getRefId() != null && b.getRefId() > 0) {
                    b.setRefId(initialMapping.get(b.getRefId()));
                } else {
                    b.setRefId(null);
                }
            }
        }

        //second iteration: fix explicit links that point now to null blocks
        for (AbstractDocumentTemplate.BlockTemplate b : blocks) {
            if (b != null && b.getRefId() != null && blocks.get(b.getRefId()) == null) {
                //determine first block which is not null above the referenced block
                int index = b.getRefId() - 1;
                while (blocks.get(index) == null && index >= 0) {
                    index--;
                }
                if (index >= 0) {
                    b.setRefId(index);
                } else {
                    b.setRefId(null);
                }
            }
        }

        //third iteration: remove null blocks and perform a compacting operation on the whole conversation
        newBlocks = new ArrayList<>();
        Map<Integer, Integer> newMapping = new TreeMap<>();
        int noCrt = 0;
        for (int i = 0; i < blocks.size(); i++) {
            if (blocks.get(i) != null) {
                newMapping.put(i, noCrt);
                AbstractDocumentTemplate.BlockTemplate crt = blocks.get(i);
                crt.setId(noCrt);
                if (crt.getRefId() != null) {
                    crt.setRefId(newMapping.get(crt.getRefId()));
                }
                newBlocks.add(crt);
                noCrt++;
            }
        }

        //update mappings
        for (Map.Entry<Integer, Integer> e : initialMapping.entrySet()) {
            if (newMapping.containsKey(e.getValue())) {
                initialMapping.put(e.getKey(), newMapping.get(e.getValue()));
            } else {
                //search for closest match
                int index = e.getValue() - 1;
                while (!newMapping.containsKey(index) && index >= 0) {
                    index--;
                }
                initialMapping.put(e.getKey(), index);
            }
        }
    }
}
