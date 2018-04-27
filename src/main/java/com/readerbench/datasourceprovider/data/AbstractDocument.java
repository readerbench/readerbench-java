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
import com.readerbench.datasourceprovider.data.discourse.SemanticChain;
import com.readerbench.datasourceprovider.data.discourse.SemanticCohesion;
import com.readerbench.datasourceprovider.data.lexicalchains.DisambiguationGraph;
import com.readerbench.datasourceprovider.data.lexicalchains.LexicalChain;
import com.readerbench.datasourceprovider.data.semanticmodels.ISemanticModel;
import com.readerbench.datasourceprovider.data.semanticmodels.SimilarityType;
import com.readerbench.datasourceprovider.pojo.Lang;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
        NONE, SERIALIZED, CSV, XML
    };

    private String path;
    private String titleText;
    private Sentence title;
    private String genre;
    private List<Block> blocks;
    // cohesion between a block and the overall document
    private SemanticCohesion[] blockDocDistances;
    // inter-block cohesion values
    private SemanticCohesion[][] blockDistances;
    private SemanticCohesion[][] prunnedBlockDistances;

    // useful for time series analysis - 0 for documents and the difference in
    // - measures the distance between the current & the previous utterance, in ms
    private long[] blockOccurrencePattern;

    private List<LexicalChain> lexicalChains;
    private DisambiguationGraph disambiguationGraph;

    protected Map<IComplexityIndex, Double> complexityIndices;

    private List<SemanticChain> voices;
    private List<SemanticChain> extendedVoices;

    public AbstractDocument(String path, List<ISemanticModel> models, Lang lang) {
        this.path = path;
        setLanguage(lang);
        this.disambiguationGraph = new DisambiguationGraph(lang);
        this.blocks = new ArrayList<>();
        this.lexicalChains = new LinkedList<>();
        super.setSemanticModels(models);
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

    public long[] getBlockOccurrencePattern() {
        return blockOccurrencePattern;
    }

    public void setBlockOccurrencePattern(long[] blockOccurrencePattern) {
        this.blockOccurrencePattern = blockOccurrencePattern;
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
