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
package com.readerbench.ageofexposure;

import com.readerbench.datasourceprovider.data.AbstractDocumentTemplate;
import com.readerbench.datasourceprovider.data.Block;
import com.readerbench.datasourceprovider.data.Sentence;
import com.readerbench.datasourceprovider.data.Word;
import com.readerbench.datasourceprovider.data.document.Document;
import com.readerbench.datasourceprovider.pojo.Lang;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.*;

public class GenericTasaDocument implements Comparable<GenericTasaDocument> {

    private static final Logger LOGGER = LoggerFactory.getLogger(GenericTasaDocument.class);

    private String ID;
    private double DRPscore;
    private int noParagraphs;
    private String content;
    private String genre;

    public static final String C_BASE_FOLDER_NAME = "grade";

    public static int get6ComplexityGrades(double DRP) {
        if (DRP <= 45.990) {
            return 1;
        }
        if (DRP <= 50.999) {
            return 2;
        }
        if (DRP <= 55.998) {
            return 3;
        }
        if (DRP <= 61.000) {
            return 4;
        }
        if (DRP <= 63.999) {
            return 5;
        }
        if (DRP <= 85.8) {
            return 6;
        }
        return -1;
    }

    public static int get13GradeLevel(double DRP) {
        if (DRP < 35.377) {
            return -1;
        }
        if (DRP <= 45.990) {
            return 1;
        }
        if (DRP <= 48.973) {
            return 2;
        }
        if (DRP <= 50.999) {
            return 3;
        }
        if (DRP <= 52.995) {
            return 4;
        }
        if (DRP <= 55.998) {
            return 5;
        }
        if (DRP <= 58.984) {
            return 6;
        }
        if (DRP <= 59.999) {
            return 7;
        }
        if (DRP <= 61.000) {
            return 8;
        }
        if (DRP <= 61.998) {
            return 9;
        }
        if (DRP <= 63.999) {
            return 10;
        }
        if (DRP <= 65.997) {
            return 11;
        }
        if (DRP <= 66.998) {
            return 12;
        }
        return 13;
    }

    public GenericTasaDocument(String iD, double DRPscore, int noParagraphs,
            String content, String genre) throws UnsupportedEncodingException {
        super();
        ID = iD;
        this.DRPscore = DRPscore;
        this.noParagraphs = noParagraphs;
        this.content = new String(content);
        this.genre = genre;
    }

    public void writeTxt(String path) throws IOException {
        BufferedWriter out = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(new File(path + "/" + genre + "/" + ID
                        + "-DRP-" + DRPscore + ".txt")), "UTF-8"), 32768);
        out.write(content.trim());
        out.close();
    }

    public void writeContent(String path) throws IOException {
        int gradeLevel = get13GradeLevel(DRPscore);
        if (gradeLevel == -1) {
            return;
        }
        // verify if class folder exists
        File dir = new File(path + "/" + C_BASE_FOLDER_NAME
                + gradeLevel);
        if (!dir.exists()) {
            dir.mkdir();
        }

        Document d = getDocument(false);

        d.exportXML(path + "/" + C_BASE_FOLDER_NAME
                + gradeLevel + "/" + ID + ".xml");
        writeTxt(path);
    }

    public StringBuilder getProcessedContent(boolean usePOStagging,
            boolean annotateWithPOS) {
        Document d = getDocument(usePOStagging);
        StringBuilder sb = new StringBuilder();
        for (Block b : d.getBlocks()) {
            for (Sentence s : b.getSentences()) {
                for (Word w : s.getWords()) {
                    if (w.getPOS() != null && annotateWithPOS) {
                        sb.append(w.getLemma()).append("_").append(w.getPOS()).append(" ");
                    } else {
                        sb.append(w.getLemma()).append(" ");
                    }
                }
                if (s.getWords().size() > 0) {
                    sb.append(". ");
                }
            }
        }
        return sb;
    }

    /**
     * @param usePOStagging
     * @return
     */
    public Document getDocument(boolean usePOStagging) {
        if (content.length() < SplitTASA.LOWER_BOUND) {
            LOGGER.warn(ID
                    + " has too few characters to be taken into consideration");
            return null;
        }

        // perform processing and save the new document
        StringTokenizer st = new StringTokenizer(content, "\n");
        if (st.countTokens() != noParagraphs) {
            LOGGER.warn("Incorrect number of paragraphs for " + ID);
        }

        int crtBlock = 0;

        AbstractDocumentTemplate docTmp = new AbstractDocumentTemplate();

        docTmp.setGenre(genre);
        while (st.hasMoreTokens()) {
            AbstractDocumentTemplate.BlockTemplate block = docTmp.new BlockTemplate();
            block.setId(crtBlock++);
            block.setContent(st.nextToken().trim());
            docTmp.getBlocks().add(block);
        }
        Document d = new Document(null, docTmp, new ArrayList<>(), Lang.en, usePOStagging);
        d.setTitleText("TASA");
        List<String> authors = new LinkedList<>();
        authors.add(ID.replaceAll("[^a-z,A-Z]", ""));
        d.setGenre(genre);
        d.setAuthors(authors);
        d.setDate(new Date());
        d.setSource("Touchstone Applied Science Associates, Inc.");
        d.setURI("http://lsa.colorado.edu/spaces.html");
        d.setComplexityLevel(DRPscore + "");
        return d;
    }

    public String getID() {
        return ID;
    }

    public void setID(String iD) {
        ID = iD;
    }

    public double getDRPscore() {
        return DRPscore;
    }

    public void setDRPscore(double dRPscore) {
        DRPscore = dRPscore;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public int getNoParagraphs() {
        return noParagraphs;
    }

    public void setNoParagraphs(int noParagraphs) {
        this.noParagraphs = noParagraphs;
    }

    @Override
    public int hashCode() {
        return this.getContent().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        GenericTasaDocument doc = (GenericTasaDocument) obj;
        return doc.getContent().equals(this.getContent());
    }

    @Override
    public int compareTo(GenericTasaDocument doc) {
        return (int) Math.signum(doc.getContent().length()
                - this.getContent().length());
    }

}
