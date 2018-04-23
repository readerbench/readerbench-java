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
package com.readerbench.datasourceprovider.data;

import com.readerbench.datasourceprovider.data.complexity.IComplexityIndex;
import com.readerbench.datasourceprovider.data.cscl.Conversation;
import com.readerbench.datasourceprovider.data.discourse.SemanticChain;
import com.readerbench.datasourceprovider.data.discourse.SemanticCohesion;
import com.readerbench.datasourceprovider.data.document.Document;
import com.readerbench.datasourceprovider.data.lexicalChains.DisambiguationGraph;
import com.readerbench.datasourceprovider.data.lexicalChains.LexicalChain;
import com.readerbench.datasourceprovider.data.semanticmodels.ISemanticModel;
import com.readerbench.datasourceprovider.data.semanticmodels.SimilarityType;
import com.readerbench.datasourceprovider.pojo.Lang;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 *
 * @author Mihai Dascalu
 */
public abstract class AbstractDocument extends AnalysisElement {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractDocument.class);

    private static final long serialVersionUID = -6173684658096015060L;

    public static enum DocumentType {
        DOCUMENT, CONVERSATION, ESSAY_CREATOR, METACOGNITION, SUMMARY
    };

    public static enum SaveType {
        NONE, SERIALIZED, SERIALIZED_AND_CSV_EXPORT, FULL
    };

    private String path;
    private String titleText;
    private Sentence title;
    private List<Block> blocks;
    // cohesion between a block and the overall document
    private SemanticCohesion[] blockDocDistances;
    // inter-block cohesion values
    private SemanticCohesion[][] blockDistances;
    private SemanticCohesion[][] prunnedBlockDistances;

    private AbstractDocumentTemplate docTmp;
    private String genre;
    // useful for time series analysis - 0 for documents and the difference in
    // - measures the distance between the current & the previous utterance, in ms
    private long[] blockOccurrencePattern;

    private List<LexicalChain> lexicalChains;
    private DisambiguationGraph disambiguationGraph;

    protected Map<IComplexityIndex, Double> complexityIndices;

    private List<SemanticChain> voices;
    private List<SemanticChain> extendedVoices;
    private transient List<SemanticChain> selectedVoices;
    private int noNouns;
    private int noVerbs;
    private int noConvergentPoints;
    private int noDivergentPoints;
    private int noPerspectives;
    private int noNounsInPerspectives;
    private int noVerbsInPerspectives;
    private double recurrenceRate;
    private double determinism;
    private double convergenceRate;
    private double divergenceRate;
    private double convergenceOrDivergenceRate;
    private int maxLine;
    private double averageLine;

    protected Map<SimilarityType, String> modelPaths;

    public AbstractDocument() {
        super();
        this.blocks = new ArrayList<>();
        this.lexicalChains = new LinkedList<>();
    }

    public AbstractDocument(String path, List<ISemanticModel> models, Lang lang) {
        this();
        this.path = path;
        setLanguage(lang);
        this.disambiguationGraph = new DisambiguationGraph(lang);
        super.setSemanticModels(models);
    }

    public AbstractDocument(List<ISemanticModel> models, Lang lang) {
        this();
        setLanguage(lang);
        this.disambiguationGraph = new DisambiguationGraph(lang);
        super.setSemanticModels(models);
    }






    public static boolean checkTagsDocument(File f, String tag) {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        InputSource input;
        try {
            input = new InputSource(new FileInputStream(f));
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

    public static AbstractDocument loadGenericDocument(File docFile, List<ISemanticModel> models,
            Lang lang, boolean usePOSTagging, boolean computeDialogism, boolean useBigrams,
            String pathToComplexityModel, int[] selectedComplexityFactors,
            boolean cleanInput, SaveType saveOutput) {
        // parse the XML file
        LOGGER.info("Loading {} file for processing", docFile.getPath());
        boolean isDocument = checkTagsDocument(docFile, "p");

        boolean isChat = checkTagsDocument(docFile, "Utterance");

        if (isChat && isDocument) {
            throw new RuntimeException("Input file " + docFile.getPath() + " has an innapropriate structure as it contains tags for both documents and chats!");
        }
        if (!isChat && !isDocument) {
            throw new RuntimeException("Input file " + docFile.getPath() + " has an innapropriate structure as it not contains any tags for documents or chats!");
        }

        if (isDocument) {
            Document d = Document.load(docFile, models, lang, usePOSTagging);
            //d.computeAll(computeDialogism, useBigrams);
            d.save(saveOutput);
            return d;
        }
        if (isChat) {
            Conversation c = Conversation.load(docFile, models, lang, usePOSTagging);
            c.computeAll(computeDialogism, useBigrams);
            c.save(saveOutput);
            return c;
        }

        return null;
    }
    
    public void setModelPaths() {
        modelPaths = new EnumMap<>(SimilarityType.class);
        for (Map.Entry<SimilarityType, ISemanticModel> e : semanticModels.entrySet()) {
            modelPaths.put(e.getKey(), e.getValue().getPath());
        }
    }

    public Map<SimilarityType, String> getModelPaths() {
        return modelPaths;
    }

    public void saveSerializedDocument() {
        LOGGER.info("Saving serialized document ...");
        try {
            setModelPaths();
            FileOutputStream fos;
            fos = new FileOutputStream(new File(getPath().replace(".xml", ".ser")));
            try (ObjectOutputStream out = new ObjectOutputStream(fos)) {
                out.writeObject(this);
            } catch (Exception ex) {
                ex.printStackTrace();
            } catch (Error ex) {
                ex.printStackTrace();
            }

        } catch (IOException ex) {
            LOGGER.error(ex.getMessage());
        }
    }

    public void saveTxtDocument() {
        LOGGER.info("Saving plain text document ...");
        File output = new File(path.replace(".xml", ".txt"));
        try (BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(output), "UTF-8"),
                32768)) {
            for (Block block : this.getBlocks()) {
                if (block != null) {
                    out.write(block.getText() + "\n");
                }
            }
        } catch (IOException ex) {
            LOGGER.error(ex.getMessage());
        }
    }

    public static AbstractDocument loadSerializedDocument(String path) throws FileNotFoundException, IOException, ClassNotFoundException {
        LOGGER.info("Loading serialized document {} ...", path);
        AbstractDocument d;
        FileInputStream fIn = new FileInputStream(new File(path));
        ObjectInputStream oIn = new ObjectInputStream(fIn);
        d = (AbstractDocument) oIn.readObject();
        d.rebuildSemanticSpaces(SimilarityType.loadVectorModels(d.modelPaths, d.getLanguage()));
        return d;
    }

    @Override
    public String toString() {
        String s = "";
        if (title != null) {
            s += title + "\n";
        }
        s = blocks.stream().filter((b) -> (b != null)).map((b) -> b + "\n").reduce(s, String::concat);
        return s;
    }

    // Get the list of sentences of a document
    public List<Sentence> getSentencesInDocument() {
        List<Sentence> sentences = new ArrayList<>();
        this.getBlocks().stream().filter((block) -> (block != null)).forEach((block) -> {
            sentences.addAll(block.getSentences());
        });
        return sentences;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Sentence getTitle() {
        return title;
    }

    public String getTitleText() {
        return titleText;
    }

    public List<Block> getBlocks() {
        return blocks;
    }

    public void setBlocks(List<Block> blocks) {
        this.blocks = blocks;
    }

    public void setTitleText(String titleText) {
        this.titleText = titleText;
    }

    public void setTitle(Sentence title) {
        this.title = title;
    }

    public SemanticCohesion[][] getBlockDistances() {
        return blockDistances;
    }

    public void setBlockDistances(SemanticCohesion[][] blockDistances) {
        this.blockDistances = blockDistances;
    }

    public SemanticCohesion[][] getPrunnedBlockDistances() {
        return prunnedBlockDistances;
    }

    public void setPrunnedBlockDistances(SemanticCohesion[][] prunnedBlockDistances) {
        this.prunnedBlockDistances = prunnedBlockDistances;
    }

    public SemanticCohesion[] getBlockDocDistances() {
        return blockDocDistances;
    }

    public void setBlockDocDistances(SemanticCohesion[] blockDocDistances) {
        this.blockDocDistances = blockDocDistances;
    }

    public List<LexicalChain> getLexicalChains() {
        return lexicalChains;
    }

    public void setLexicalChains(List<LexicalChain> lexicalChains) {
        this.lexicalChains = lexicalChains;
    }

    public DisambiguationGraph getDisambiguationGraph() {
        return disambiguationGraph;
    }

    public final void setDisambiguationGraph(DisambiguationGraph disambiguationGraph) {
        this.disambiguationGraph = disambiguationGraph;
    }

    public Map<IComplexityIndex, Double> getComplexityIndices() {
        return complexityIndices;
    }

    public void setComplexityIndices(Map<IComplexityIndex, Double> complexityFactors) {
        this.complexityIndices = complexityFactors;
    }

    public List<SemanticChain> getVoices() {
        return voices;
    }

    public void setVoices(List<SemanticChain> voices) {
        this.voices = voices;
    }

    public List<SemanticChain> getExtendedVoices() {
        return extendedVoices;
    }

    public void setExtendedVoices(List<SemanticChain> extendedVoices) {
        this.extendedVoices = extendedVoices;
    }

    public List<SemanticChain> getSelectedVoices() {
        return selectedVoices;
    }

    public void setSelectedVoices(List<SemanticChain> selectedVoices) {
        this.selectedVoices = selectedVoices;
    }

    public long[] getBlockOccurrencePattern() {
        return blockOccurrencePattern;
    }

    public void setBlockOccurrencePattern(long[] blockOccurrencePattern) {
        this.blockOccurrencePattern = blockOccurrencePattern;
    }

    public void setNoNouns(int no) {
        this.noNouns = no;
    }

    public int getNoNouns() {
        return noNouns;
    }

    public void setNoVerbs(int no) {
        this.noVerbs = no;
    }

    public int getNoVerbs() {
        return noVerbs;
    }

    public void setNoConvergentPoints(int no) {
        this.noConvergentPoints = no;
    }

    public int getNoConvergentPoints() {
        return noConvergentPoints;
    }

    public void setNoDivergentPoints(int no) {
        this.noDivergentPoints = no;
    }

    public int getNoDivergentPoints() {
        return noDivergentPoints;
    }

    public void setNoPerspectives(int noPerspectives) {
        this.noPerspectives = noPerspectives;
    }

    public int getNoPerspectives() {
        return noPerspectives;
    }

    public int getNoNounsInPerspectives() {
        return noNounsInPerspectives;
    }

    public void setNoNounsInPerspectives(int noNounsInPerspectives) {
        this.noNounsInPerspectives = noNounsInPerspectives;
    }

    public int getNoVerbsInPerspectives() {
        return noVerbsInPerspectives;
    }

    public void setNoVerbsInPerspectives(int noVerbsInPerspectives) {
        this.noVerbsInPerspectives = noVerbsInPerspectives;
    }

    public double getRecurrenceRate() {
        return recurrenceRate;
    }

    public void setRecurrenceRate(double recurrenceRate) {
        this.recurrenceRate = recurrenceRate;
    }

    public double getDeterminism() {
        return determinism;
    }

    public void setDeterminism(double determinism) {
        this.determinism = determinism;
    }

    public double getConvergenceOrDivergenceRate() {
        return convergenceOrDivergenceRate;
    }

    public void setConvergenceOrDivergenceRate(double convergenceOrDivergenceRate) {
        this.convergenceOrDivergenceRate = convergenceOrDivergenceRate;
    }

    public double getConvergenceRate() {
        return convergenceRate;
    }

    public void setConvergenceRate(double convergenceRate) {
        this.convergenceRate = convergenceRate;
    }

    public double getDivergenceRate() {
        return divergenceRate;
    }

    public void setDivergenceRate(double divergenceRate) {
        this.divergenceRate = divergenceRate;
    }

    public int getMaxLine() {
        return maxLine;
    }

    public void setMaxLine(int maxLine) {
        this.maxLine = maxLine;
    }

    public double getAverageLine() {
        return averageLine;
    }

    public void setAverageLine(double averageLine) {
        this.averageLine = averageLine;
    }

    public String getDescription() {
        String s = this.getTitleText();

        if (!getSemanticModelsAsList().isEmpty()) {
            StringJoiner sj = new StringJoiner(", ", " [", "]");
            getSemanticModelsAsList().stream().forEach((model) -> {
                sj.add(model.getPath());
            });
            s += sj;
        }
        return s;
    }

    public AbstractDocumentTemplate getDocTmp() {
        return docTmp;
    }

    public final void setDocTmp(AbstractDocumentTemplate docTmp) {
        this.docTmp = docTmp;
    }

    public static long getSerialversionuid() {
        return serialVersionUID;
    }

    public int getNoBlocks() {
        return (int) getBlocks().stream().filter(b -> b != null).count();
    }

    public int getNoSentences() {
        return getBlocks().parallelStream()
                .filter(b -> b != null)
                .mapToInt(b -> b.getSentences().size())
                .sum();

    }

    public int getNoWords() {
        return getBlocks().parallelStream()
                .filter(b -> b != null)
                .flatMap(b -> b.getSentences().stream())
                .mapToInt(s -> s.getAllWords().size())
                .sum();
    }

    public int getNoContentWords() {
        return getBlocks().parallelStream()
                .filter(b -> b != null)
                .flatMap(b -> b.getSentences().stream())
                .flatMap(s -> s.getWordOccurences().values().stream())
                .mapToInt(x -> x)
                .sum();
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public boolean canUseSimType(SimilarityType simType) {
        return !simType.isLoadable() || getModelVectors().keySet().contains(simType);
    }

    @Override
    public List<NGram> getBiGrams() {
        return blocks.stream()
                .flatMap(s -> s.getBiGrams().stream())
                .collect(Collectors.toList());
    }

    @Override
    public List<NGram> getNGrams(int n) {
        return blocks.stream()
                .flatMap(s -> s.getNGrams(n).stream())
                .collect(Collectors.toList());
    }
}
