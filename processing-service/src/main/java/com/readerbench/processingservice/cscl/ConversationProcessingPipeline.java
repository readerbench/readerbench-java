package com.readerbench.processingservice.cscl;

import com.readerbench.coreservices.cscl.CollaborationEvaluation;
import com.readerbench.coreservices.cscl.ParticipantEvaluation;
import com.readerbench.coreservices.data.cscl.Conversation;
import com.readerbench.coreservices.data.cscl.Participant;
import com.readerbench.coreservices.data.cscl.Utterance;
import com.readerbench.coreservices.dialogism.DialogismComputations;
import com.readerbench.coreservices.dialogism.DialogismMeasures;
import com.readerbench.coreservices.keywordmining.KeywordModeling;
import com.readerbench.coreservices.nlp.parsing.Parsing;
import com.readerbench.coreservices.nlp.spellchecking.Spellchecking;
import com.readerbench.coreservices.data.AbstractDocument;
import com.readerbench.coreservices.data.AbstractDocumentTemplate;
import com.readerbench.coreservices.data.Block;
import com.readerbench.coreservices.semanticmodels.SemanticModel;
import com.readerbench.datasourceprovider.pojo.Lang;
import com.readerbench.processingservice.Annotators;
import com.readerbench.processingservice.GenericProcessingPipeline;
import com.readerbench.processingservice.exportdata.ExportDocument;
import com.readerbench.processingservice.importdata.ImportDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.DOMException;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import org.elasticsearch.client.transport.TransportClient;
//import org.json.simple.JSONObject;
//import org.json.simple.JSONArray;
import org.json.*;
import java.net.InetAddress;


import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

/**
 * @author Dorinela Sirbu <dorinela.92@gmail.com>
 */
public class ConversationProcessingPipeline extends GenericProcessingPipeline {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConversationProcessingPipeline.class);

    private final Spellchecking spellChecker = new Spellchecking();
    private final ConversationRestructuringSupport support = new ConversationRestructuringSupport();

    public ConversationProcessingPipeline(Lang lang, List<SemanticModel> models, List<Annotators> annotators) {
        super(lang, models, annotators);
    }

    //consider the usage of the NLP pipeline when creating a new conversation
    public Conversation createConversationFromTemplate(AbstractDocumentTemplate docTmp) {
        Conversation c = new Conversation(null, getModels(), getLanguage());
        Parsing.parseDoc(docTmp, c, getAnnotators().contains(Annotators.NLP_PREPROCESSING), getLanguage());
        return c;
    }

    //consider the usage of the NLP pipeline when creating a new conversation
    public Conversation createConversationFromXML(String path) {
        Conversation c = createConversationFromTemplate(extractConvTemplateFromXML(path));
        addInformationFromXML(path, c);
        return c;
    }

    //process all XMLs from a folder and save corresponding serialized files
    public List<Conversation> processConversations(String path) {
        LOGGER.info("Processing all conversations in {}", path);

        List<Conversation> conversations = new ArrayList<>();

        FileFilter filter = (File f) -> f.getName().endsWith(".xml");
        File dir = new File(path);
        if (!dir.isDirectory()) {
            return null;
        }
        File[] filesTODO = dir.listFiles(filter);
        ImportDocument id = new ImportDocument();
        ExportDocument ed = new ExportDocument();
        for (File f : filesTODO) {
            Conversation c = createConversationFromXML(f.getPath());
            processConversation(c);
            ed.export(c, Arrays.asList(new AbstractDocument.SaveType[]{AbstractDocument.SaveType.SERIALIZED}));
            conversations.add(c);
        }

        return conversations;
    }

    public List<Conversation> importConversations(String path) {
        LOGGER.info("Importing all serialized conversations in {}", path);

        List<Conversation> conversations = new ArrayList<>();

        FileFilter filter = (File f) -> f.getName().endsWith(".ser");
        File dir = new File(path);
        if (!dir.isDirectory()) {
            return null;
        }
        File[] filesTODO = dir.listFiles(filter);
        ImportDocument id = new ImportDocument();
        for (File f : filesTODO) {
            Conversation c;
            try {
                c = (Conversation) id.importSerializedDocument(f.getPath());
                conversations.add(c);
            } catch (IOException | ClassNotFoundException ex) {
                LOGGER.error(ex.getMessage());
            }
        }

        return conversations;
    }

    public void processConversation(Conversation c) {
        super.processDocument(c);

        determineParticipantContributions(c);

        c.getParticipants().stream().forEach((p) -> {
            KeywordModeling.determineKeywords(p.getContributions(), getAnnotators().contains(Annotators.USE_BIGRAMS));
        });

        CollaborationEvaluation.evaluateSocialKB(c);

        if (getAnnotators().contains(Annotators.DIALOGISM)) {
            c.setVoicePMIEvolution(DialogismMeasures.getCollaborationEvolution(c));
            DialogismComputations.determineParticipantInterAnimation(c);
        }

        // evaluate participants
        ParticipantEvaluation.evaluateInteraction(c);
        ParticipantEvaluation.evaluateInvolvement(c);
        ParticipantEvaluation.performSNA(c);
        ParticipantEvaluation.evaluateUsedConcepts(c);

//        ParticipantEvaluation.extractRhythmicIndex(this);
//        ParticipantEvaluation.extractRhythmicCoefficient(this);
//        ParticipantEvaluation.computeEntropyForRegularityMeasure(this);
    }

    /**
     *
     */
    private void determineParticipantContributions(Conversation c) {
        if (c.getParticipants().size() > 0) {
            for (Participant p : c.getParticipants()) {
                p.setContributions(new Conversation(null, c.getSemanticModelsAsList(), c.getLanguage()));
                p.setSignificantContributions(new Conversation(null, c.getSemanticModelsAsList(), c.getLanguage()));
            }
            for (Block b : c.getBlocks()) {
                if (b != null && ((Utterance) b).getParticipant() != null) {
                    Block.addBlock(((Utterance) b).getParticipant().getContributions(), b);
                    if (b.isSignificant()) {
                        Block.addBlock(((Utterance) b).getParticipant().getSignificantContributions(), b);
                    }
                }
            }
            for (Participant p : c.getParticipants()) {
                p.getContributions().determineWordOccurences(p.getContributions().getBlocks());
                p.getContributions().determineSemanticDimensions();
            }
        }
    }

    
    public AbstractDocumentTemplate extractConvTemplateFromEsJson(JSONObject discussionThread) {
        AbstractDocumentTemplate contents = new AbstractDocumentTemplate();
        List<AbstractDocumentTemplate.BlockTemplate> blocks = new ArrayList<>();
        String title = "";

        try {
            JSONArray participants = discussionThread.getJSONArray("participants");
            title = discussionThread.getString("title");
            JSONArray body = discussionThread.getJSONArray("body");

            for (int index = 0; index < body.length(); index++) {
                JSONObject comment = body.getJSONObject(index);
                AbstractDocumentTemplate.BlockTemplate block = contents.new BlockTemplate();

                block.setSpeaker(comment.getString("nickname"));

                block.setTime(comment.getString("time"));
                block.setId(Integer.parseInt(comment.getString("genid")));
                block.setRefId(Integer.parseInt(comment.getString("refid")));

                String text = comment.getString("text");
                text = spellChecker.checkText(text, getLanguage(), "");
                block.setContent(text);
                if (text.length() > 0) {
                    blocks.add(block);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        support.mergeAdjacentContributions(blocks);
        contents.setBlocks(support.getNewBlocks());
        contents.setTitle(title);
        
        return contents;
    }
  

    public AbstractDocumentTemplate extractConvTemplateFromXML(String path) {
        // parse the XML file
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        // determine contents
        AbstractDocumentTemplate contents = new AbstractDocumentTemplate();
        List<AbstractDocumentTemplate.BlockTemplate> blocks = new ArrayList<>();

        try {
            InputSource input = new InputSource(new FileInputStream(new File(path)));
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
                                text = spellChecker.checkText(text, getLanguage(), "");
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

            support.mergeAdjacentContributions(blocks);
            contents.setBlocks(support.getNewBlocks());

            //establish the title as the concatenation of all topics
            String title = "";
            nl1 = doc.getElementsByTagName("Topic");
            if (nl1 != null && nl1.getLength() > 0) {
                for (int i = 0; i < nl1.getLength(); i++) {
                    el = (Element) nl1.item(i);
                    title += el.getFirstChild().getNodeValue() + " ";
                }
                contents.setTitle(title);
            }
            return contents;
        } catch (FileNotFoundException | ParserConfigurationException | NumberFormatException | DOMException ex) {
            LOGGER.error(ex.getMessage());
        } catch (SAXException | IOException ex) {
            LOGGER.error(ex.getMessage());
        }
        return null;
    }

    private void addInformationFromXML(String path, Conversation c) {
        // parse the XML file
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

        try {
            InputSource input = new InputSource(new FileInputStream(new File(path)));
            input.setEncoding("UTF-8");

            DocumentBuilder db = dbf.newDocumentBuilder();
            org.w3c.dom.Document dom = db.parse(input);

            Element doc = dom.getDocumentElement();
            Element el;
            // obtain annotator grades
            NodeList nl = doc.getElementsByTagName("Grades");
            if (nl != null && nl.getLength() > 0) {
                for (int i = 0; i < nl.getLength(); i++) {
                    el = (Element) nl.item(i);
                    nl = el.getElementsByTagName("General_grade");
                    if (nl != null && nl.getLength() > 0) {
                        for (int j = 0; j < nl.getLength(); j++) {
                            el = (Element) nl.item(j);
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
            nl = doc.getElementsByTagName("Collab_regions");
            if (nl != null && nl.getLength() > 0) {
                for (int i = 0; i < nl.getLength(); i++) {
                    el = (Element) nl.item(i);
                    nl = el.getElementsByTagName("Collab_regions_annotation");
                    if (nl != null && nl.getLength() > 0) {
                        for (int j = 0; j < nl.getLength(); j++) {
                            el = (Element) nl.item(j);
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
                        c.setAnnotatedCollabZones(CollaborationEvaluation.getCollaborationZones(collabEv));
                    }
                }
            }
            c.setAnnotatedCollabEvolution(collabEv);

        } catch (FileNotFoundException | ParserConfigurationException | NumberFormatException | DOMException ex) {
            LOGGER.error("Error evaluating input file " + path + "!");
            LOGGER.error(ex.getMessage());
        } catch (SAXException | IOException ex) {
            LOGGER.error(ex.getMessage());
        }
    }
}
