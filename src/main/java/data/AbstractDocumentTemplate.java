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
package data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class AbstractDocumentTemplate implements Serializable {

    private static final long serialVersionUID = 6486392022508461270L;
    private String genre;
    private List<BlockTemplate> blocks = new ArrayList<BlockTemplate>();

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
        private String time;
        private Integer id;
        private Integer refId;
        private Integer verbId;
        private String content;

        public String getSpeaker() {
            return speaker;
        }

        public void setSpeaker(String speaker) {
            this.speaker = speaker;
        }

        public String getTime() {
            return time;
        }

        public void setTime(String time) {
            this.time = time;
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

        @Override
        public String toString() {
            return "BlockTemplate [speaker=" + speaker + ", time=" + time + ", id=" + id + ", refId=" + refId
                    + ", verbId=" + verbId + ", content=" + content + "]";
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
        for (BlockTemplate temp : blocks) {
            build.append(temp.getContent() + "\n");
        }
        return build.toString();
    }

    @Override
    public String toString() {
        return "DocumentTemplate [blocks=" + blocks + "]";
    }

}
