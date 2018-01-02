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
package com.readerbench.data;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.*;

public class AbstractDocumentTemplate implements Serializable {

    private static final long serialVersionUID = 6486392022508461270L;

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractDocumentTemplate.class);

    public static final SimpleDateFormat[] DATE_FORMATS = {
        new SimpleDateFormat("dd/MM/yyyy HH:mm:ss"),
        new SimpleDateFormat("EEE MM/dd/yyyy HH:mm aaa", Locale.ENGLISH),
        new SimpleDateFormat("kk.mm.ss"),
        new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH),
        new SimpleDateFormat("dd MMMMMMMM yyyy HH:mm", Locale.FRANCE),
        new SimpleDateFormat("HH:mm:ss"),
        new SimpleDateFormat("hh:mm a", Locale.ENGLISH),
        new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy", Locale.ENGLISH)
    };

    private static Date lastDate;
    private static int lastDateOffset;

    private String genre;
    private List<BlockTemplate> blocks = new ArrayList<>();

    public static AbstractDocumentTemplate getDocumentModel(String text) {
        AbstractDocumentTemplate docTmp = new AbstractDocumentTemplate();
        String[] blocks = text.split("\n");
        for (int i = 0; i < blocks.length; i++) {
            BlockTemplate block = docTmp.new BlockTemplate();
            block.setId(i);
            block.setContent(blocks[i]);
            docTmp.getBlocks().add(block);
        }
        return docTmp;
    }

    public class BlockTemplate implements Serializable {

        private static final long serialVersionUID = -4411300040028049069L;

        private String speaker;
        private Date time;
        private Integer id;
        private Integer refId;
        private Integer verbId;
        private String content;
        private String speakerAlias;

        public String getSpeaker() {
            return speaker;
        }

        public void setSpeaker(String speaker) {
            this.speaker = speaker;
        }

        public Date getTime() {
            return time;
        }

        public void setTime(String time) {
            Date aux = null;
            for (SimpleDateFormat format : DATE_FORMATS) {
                try {
                    aux = format.parse(time);
                    break;
                } catch (Exception e) {
                }
            }
            if (aux == null) {
                try {
                    Long longTime = Long.parseLong(time);
                    aux = new Date(longTime);
                } catch (NumberFormatException e) {
                    LOGGER.error("Unparsable date: {}", time);
                }
            }

//            // Adds a millisecond between utterances with the same timestamp
//            if (lastDate == null) { // first utterance
//                lastDate = aux;
//            } else {
//                long tLast = lastDate.getTime(); // time of last utterance in the file + lastDateOffset
//                long crtDate = aux.getTime() + lastDateOffset;
//                if (tLast == crtDate) {
//                    lastDateOffset += 1; // time in file + lastDateOffset + 1
//                    aux = new Date(aux.getTime() + lastDateOffset);
//                } else {
//                    lastDateOffset = 0; // reset offset if there is a new hour:minute time
//                }
//                lastDate = aux; // the current becomes the last
//            }
            this.time = aux;
        }

        public Integer getId() {
            return id;
        }

        public void setId(Integer id) {
            this.id = id;
        }

        public Integer getRefId() {
            return refId;
        }

        public void setRefId(Integer refId) {
            this.refId = refId;
        }

        public Integer getVerbId() {
            return verbId;
        }

        public void setVerbId(Integer verbId) {
            this.verbId = verbId;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public String getSpeakerAlias() {
            return speakerAlias;
        }

        public void setSpeakerAlias(String speakerAlias) {
            this.speakerAlias = speakerAlias;
        }

        @Override
        public String toString() {
            return "BlockTemplate [speaker=" + speaker + ", speakerAlias=" + speakerAlias + ", time=" + time + ", " +
                    "id=" + id + ", refId=" + refId + ", verbId=" + verbId + ", content=" + content + "]";
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 23 * hash + Objects.hashCode(this.id);
            hash = 23 * hash + Objects.hashCode(this.content);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final BlockTemplate other = (BlockTemplate) obj;
            if (!Objects.equals(this.content, other.content)) {
                return false;
            }
            if (!Objects.equals(this.id, other.id)) {
                return false;
            }
            return true;
        }
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public List<BlockTemplate> getBlocks() {
        return blocks;
    }

    public void setBlocks(List<BlockTemplate> blocks) {
        this.blocks = blocks;
    }

    public String getText() {
        StringBuilder build = new StringBuilder();
        blocks.stream().forEach((temp) -> {
            build.append(temp.getContent()).append("\n");
        });
        return build.toString();
    }

    @Override
    public String toString() {
        return "DocumentTemplate [blocks=" + blocks + "]";
    }
}
