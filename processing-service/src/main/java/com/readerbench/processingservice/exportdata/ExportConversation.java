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
package com.readerbench.processingservice.exportdata;

import com.readerbench.coreservices.data.cscl.Conversation;
import com.readerbench.coreservices.data.cscl.Utterance;
import com.readerbench.coreservices.data.Block;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author ReaderBench
 */
public class ExportConversation {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExportConversation.class);

    public void exportIM(Conversation c) {
        LOGGER.info("Writing document export in IM format");
        File output = new File(c.getPath().replace(".xml", "_IM.txt"));
        try (BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(output), "UTF-8"),
                32768)) {
            out.write("ID\tReference ID\tName\tTime\tText\n");
            SimpleDateFormat df = new SimpleDateFormat("yyyy-mm-dd hh:mm");
            for (Block b : c.getBlocks()) {
                if (b != null) {
                    out.write(b.getIndex() + "\t" + ((Utterance) b).getRefBlock().getIndex() + "\t"
                            + ((Utterance) b).getParticipant().getName() + "\t" + df.format(((Utterance) b).getTime())
                            + "\t" + b.getText() + "\n");
                }
            }
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage());
        }
    }
}
