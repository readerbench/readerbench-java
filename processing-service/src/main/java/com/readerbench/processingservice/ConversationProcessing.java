package com.readerbench.processingservice;

import com.readerbench.coreservices.cscl.Collaboration;
import com.readerbench.coreservices.nlp.spellchecking.Spellchecking;
import com.readerbench.datasourceprovider.data.AbstractDocumentTemplate;
import com.readerbench.datasourceprovider.data.cscl.Conversation;
import com.readerbench.datasourceprovider.data.cscl.Participant;
import com.readerbench.datasourceprovider.data.semanticmodels.ISemanticModel;
import com.readerbench.datasourceprovider.pojo.Lang;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.DOMException;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * @author Dorinela Sirbu <dorinela.92@gmail.com>
 */
public class ConversationProcessing {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConversationProcessing.class);

    private static Spellchecking spellChecker = new Spellchecking();

    /**
     * Load a conversation
     *
     * @param docFile
     * @param models
     * @param lang
     * @param usePOSTagging
     * @return
     */
    public static Conversation load(File docFile, List<ISemanticModel> models, Lang lang, boolean usePOSTagging) {
        // parse the XML file
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        Conversation c = null;
        // determine contents
        AbstractDocumentTemplate contents = new AbstractDocumentTemplate();
        List<AbstractDocumentTemplate.BlockTemplate> blocks = new ArrayList<>();

        try {
            InputSource input = new InputSource(new FileInputStream(docFile));
            input.setEncoding("UTF-8");

            DocumentBuilder db = dbf.newDocumentBuilder();
            org.w3c.dom.Document dom = db.parse(input);

            Element doc = dom.getDocumentElement();
            Element turn, el;
            NodeList nl1, nl2;

            // reformat input accordingly to evaluation model
            nl1 = doc.getElementsByTagName("Turn");
            if (nl1 != null && nl1.getLength() > 0) {
                for (int i = 0; i < nl1.getLength(); i++) {
                    turn = (Element) nl1.item(i);
                    nl2 = turn.getElementsByTagName("Utterance");
                    if (nl2 != null && nl2.getLength() > 0) {
                        for (int j = 0; j < nl2.getLength(); j++) {
                            AbstractDocumentTemplate.BlockTemplate block = contents.new BlockTemplate();
                            if (turn.hasAttribute("nickname") && turn.getAttribute("nickname").trim().length() > 0) {
                                block.setSpeaker(turn.getAttribute("nickname").trim());
                            } else {
                                block.setSpeaker("unregistered member");
                            }
                            el = (Element) nl2.item(j);
                            if (el.getFirstChild() != null) {
                                if (el.hasAttribute("time")) {
                                    block.setTime(el.getAttribute("time"));
                                }
                                if (el.hasAttribute("genid")) {
                                    block.setId(Integer.parseInt(el.getAttribute("genid")));
                                }
                                if (el.hasAttribute("ref")) {
                                    if (el.getAttribute("ref").isEmpty()) {
                                        block.setRefId(0);
                                    } else {
                                        try {
                                            block.setRefId(Integer.parseInt(el.getAttribute("ref")));
                                        } catch (NumberFormatException e) {
                                            block.setRefId(0);
                                        }
                                    }
                                }

                                String text = el.getFirstChild().getNodeValue();
                                text = spellChecker.checkText(text, lang, "");
                                block.setContent(text);
                                if (text.length() > 0
                                        && !el.getFirstChild().getNodeValue().trim().equals("joins the room")
                                        && !el.getFirstChild().getNodeValue().trim().equals("leaves the room")) {
                                    blocks.add(block);
                                }
                            }
                        }
                    }
                }
            }

            Conversation.ConversationRestructuringSupport support = new Conversation.ConversationRestructuringSupport();
            support.mergeAdjacentContributions(blocks);
            contents.setBlocks(support.getNewBlocks());
            c = new Conversation(docFile.getAbsolutePath(), contents, models, lang, usePOSTagging);
            // set title as a concatenation of topics
            String title = "";
            nl1 = doc.getElementsByTagName("Topic");
            if (nl1 != null && nl1.getLength() > 0) {
                for (int i = 0; i < nl1.getLength(); i++) {
                    el = (Element) nl1.item(i);
                    title += el.getFirstChild().getNodeValue() + " ";
                }
                c.processDocumentTitle(title, models, lang, usePOSTagging);
            }

            if (title.length() == 0) {
                c.processDocumentTitle(docFile.getName(), models, lang, usePOSTagging);
            }

            // obtain annotator grades
            nl1 = doc.getElementsByTagName("Grades");
            if (nl1 != null && nl1.getLength() > 0) {
                for (int i = 0; i < nl1.getLength(); i++) {
                    el = (Element) nl1.item(i);
                    nl1 = el.getElementsByTagName("General_grade");
                    if (nl1 != null && nl1.getLength() > 0) {
                        for (int j = 0; j < nl1.getLength(); j++) {
                            el = (Element) nl1.item(j);
                            if (!el.getAttribute("nickname").equals("")) {
                                double nr;
                                try {
                                    nr = Double.valueOf(el.getAttribute("value"));
                                } catch (NumberFormatException e) {
                                    nr = 0;
                                }
                                for (Participant p : c.getParticipants()) {
                                    if (p.getName().equals(el.getAttribute("nickname"))) {
                                        p.setGradeAnnotator(nr);
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
            }

            double[] collabEv = new double[c.getBlocks().size()];
            nl1 = doc.getElementsByTagName("Collab_regions");
            if (nl1 != null && nl1.getLength() > 0) {
                for (int i = 0; i < nl1.getLength(); i++) {
                    el = (Element) nl1.item(i);
                    nl1 = el.getElementsByTagName("Collab_regions_annotation");
                    if (nl1 != null && nl1.getLength() > 0) {
                        for (int j = 0; j < nl1.getLength(); j++) {
                            el = (Element) nl1.item(j);
                            String text = el.getFirstChild().getNodeValue();
                            // split annotated intense collaboration zones
                            StringTokenizer stZones = new StringTokenizer(text, ",");
                            while (stZones.hasMoreTokens()) {
                                StringTokenizer stZone = new StringTokenizer(
                                        stZones.nextToken().replaceAll("\\[", "").replaceAll("\\]", ""), ";");
                                try {
                                    int start = Integer.valueOf(stZone.nextToken());
                                    int end = Integer.valueOf(stZone.nextToken());
                                    if (support.getInitialMapping().containsKey(start)) {
                                        start = support.getInitialMapping().get(start);
                                    } else {
                                        start = -1;
                                    }

                                    if (support.getInitialMapping().containsKey(end)) {
                                        end = Math.min(support.getInitialMapping().get(end), c.getBlocks().size() - 1);
                                    } else {
                                        end = -1;
                                    }
                                    // increment accordingly the intense collaboration zones distribution
                                    if (start >= 0 && start < end) {
                                        for (int k = start; k <= end; k++) {
                                            collabEv[k]++;
                                        }
                                    }
                                } catch (NumberFormatException e) {
                                    LOGGER.warn("Incorrect annotated collaboration zone format...");
                                }
                            }
                        }
                        c.setAnnotatedCollabZones(Collaboration.getCollaborationZones(collabEv));
                    }
                }
            }
            c.setAnnotatedCollabEvolution(collabEv);
        } catch (FileNotFoundException | ParserConfigurationException | NumberFormatException | DOMException ex) {
            LOGGER.error("Error evaluating input file " + docFile.getPath() + "!");
            LOGGER.error(ex.getMessage());
        } catch (SAXException | IOException ex) {
            LOGGER.error(ex.getMessage());
        }
        return c;
    }

}
