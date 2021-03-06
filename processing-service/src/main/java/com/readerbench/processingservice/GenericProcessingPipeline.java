package com.readerbench.processingservice;

import com.readerbench.coreservices.data.cscl.Conversation;
import com.readerbench.coreservices.data.AbstractDocument;
import com.readerbench.processingservice.exportdata.ExportDocument;
import com.readerbench.coreservices.cna.CohesionGraph;
import com.readerbench.processingservice.document.DocumentProcessingPipeline;
import com.readerbench.coreservices.cna.DisambiguisationGraphAndLexicalChains;
import com.readerbench.coreservices.cna.Scoring;
import com.readerbench.coreservices.data.AbstractDocumentTemplate;
import com.readerbench.coreservices.dialogism.DialogismComputations;
import com.readerbench.coreservices.keywordmining.KeywordModeling;
import com.readerbench.coreservices.data.document.Document;
import com.readerbench.coreservices.nlp.parsing.Parsing;
import com.readerbench.coreservices.semanticmodels.SemanticModel;
import com.readerbench.datasourceprovider.pojo.Lang;
import com.readerbench.processingservice.cscl.ConversationProcessingPipeline;
import com.readerbench.textualcomplexity.ComplexityIndices;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * @author Dorinela Sirbu <dorinela.92@gmail.com>
 */
public abstract class GenericProcessingPipeline {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractDocument.class);

    private final Lang lang;

    private final List<SemanticModel> models;

    private final List<Annotators> annotators;

    public GenericProcessingPipeline(Lang lang, List<SemanticModel> models, List<Annotators> annotators) {
        this.lang = lang;
        this.models = models;
        this.annotators = annotators;
    }

    /**
     *
     * @param abstractDocument
     */
    public void processDocument(AbstractDocument abstractDocument) {
        processDocumentTitle(abstractDocument);

        // build coherence graph
        CohesionGraph.buildCohesionGraph(abstractDocument);

        LOGGER.info("Determine topics...");
        KeywordModeling.determineKeywords(abstractDocument, annotators.contains(Annotators.USE_BIGRAMS));
        // TopicModel.determineTopicsLDA(this);

        LOGGER.info("Perform CNA scoring...");
        Scoring.score(abstractDocument);

        if (annotators.contains(Annotators.DIALOGISM)) {
            // build disambiguisation graph and lexical chains
            LOGGER.info("Build disambiguation graph...");
            DisambiguisationGraphAndLexicalChains.buildDisambiguationGraph(abstractDocument);
            LOGGER.info("Prune disambiguation graph...");
            DisambiguisationGraphAndLexicalChains.pruneDisambiguationGraph(abstractDocument);
            // System.out.println(d.disambiguationGraph);

            LOGGER.info("Build lexical chains...");
            DisambiguisationGraphAndLexicalChains.buildLexicalChains(abstractDocument);
            // for (LexicalChain chain : lexicalChains) {
            // System.out.println(chain);
            // }

            // determine semantic chains / voices
            LOGGER.info("Determine semantic chains / voices...");
            DialogismComputations.determineVoices(abstractDocument);
            DialogismComputations.determineExtendedVoices(abstractDocument);

            // DialogismComputations.findSentimentUsingContext(this);
            // determine voice distributions & importance
            LOGGER.info("Determine voice distributions & importance...");
            DialogismComputations.determineVoiceDistributions(abstractDocument);
            // DialogismComputations.determineExtendedVoiceDistributions(this);
        }

//        if (annotators.contains(Annotators.SENTIMENT_ANALYSIS)) {
//            // assign sentiment values
//            LOGGER.info("Assign sentiment values...");
//            SentimentAnalysis.weightSemanticValences(abstractDocument);
//        }
        if (annotators.contains(Annotators.TEXTUAL_COMPLEXITY)) {
            LOGGER.info("Computing textual complexity indices...");
            ComplexityIndices.computeComplexityFactors(abstractDocument);
        }

        LOGGER.info("Finished all discourse analysis processes");
    }

    public Document createDocumentFromTemplate(AbstractDocumentTemplate docTmp) {
        Document d = new Document(null, getModels(), getLanguage());
        Parsing.parseDoc(docTmp, d, getAnnotators().contains(Annotators.NLP_PREPROCESSING), getLanguage());
        return d;
    }

    public void processDocumentTitle(AbstractDocument abstractDocument) {
        if (abstractDocument.getTitleText() != null && abstractDocument.getTitleText().isEmpty()) {
            String processedText = abstractDocument.getTitleText().replaceAll("\\s+", " ");

            if (processedText.length() > 0) {
                AbstractDocumentTemplate adt = AbstractDocumentTemplate.getDocumentModel(processedText);
                Document doc = createDocumentFromTemplate(adt);
                // add corresponding block
                abstractDocument.setTitle(doc.getBlocks().get(0).getSentences().get(0));
            }
        }
    }

    public boolean checkTagsDocument(String path, String tag) {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        InputSource input;
        try {
            input = new InputSource(new FileInputStream(new File(path)));
            input.setEncoding("UTF-8");
            DocumentBuilder db = dbf.newDocumentBuilder();
            org.w3c.dom.Document dom = db.parse(input);

            Element doc = dom.getDocumentElement();

            // determine whether the document is a document or a chat
            NodeList nl;
            nl = doc.getElementsByTagName(tag);
            if (nl.getLength() > 0) {
                return true;
            }
        } catch (FileNotFoundException ex) {
            LOGGER.error(ex.getMessage());
        } catch (ParserConfigurationException | SAXException | IOException ex) {
            LOGGER.error(ex.getMessage());
        }
        return false;
    }

    public AbstractDocument loadGenericDocument(String path, List<AbstractDocument.SaveType> saveOutputs) {
        // parse the XML file
        LOGGER.info("Loading {} file for processing", path);
        boolean isDocument = checkTagsDocument(path, "p");

        boolean isChat = checkTagsDocument(path, "Utterance");

        if (isChat && isDocument) {
            throw new RuntimeException("Input file " + path + " has an innapropriate structure as it contains tags for both documents and chats!");
        }
        if (!isChat && !isDocument) {
            throw new RuntimeException("Input file " + path + " has an innapropriate structure as it not contains any tags for documents or chats!");
        }

        if (isDocument) {
            DocumentProcessingPipeline pipeline = new DocumentProcessingPipeline(lang, models, annotators);
            ExportDocument ed = new ExportDocument();
            Document d = pipeline.createDocumentFromXML(path);
            pipeline.processDocument(d);
            ed.export(d, saveOutputs);
            return d;
        }
        if (isChat) {
            ConversationProcessingPipeline pipeline = new ConversationProcessingPipeline(lang, models, annotators);
            ExportDocument ed = new ExportDocument();
            Conversation c = pipeline.createConversationFromXML(path);
            pipeline.processDocument(c);
            ed.export(c, saveOutputs);
            return c;
        }

        return null;
    }

    public Lang getLanguage() {
        return lang;
    }

    public List<SemanticModel> getModels() {
        return models;
    }

    public List<Annotators> getAnnotators() {
        return annotators;
    }
}
