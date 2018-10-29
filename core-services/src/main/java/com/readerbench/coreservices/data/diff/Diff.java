/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.readerbench.coreservices.data.diff;

import com.readerbench.coreservices.cna.DisambiguisationGraphAndLexicalChains;
import com.readerbench.coreservices.data.AbstractDocumentTemplate;
import com.readerbench.coreservices.data.AnalysisElement;
import com.readerbench.coreservices.data.Block;
import com.readerbench.coreservices.data.Sentence;
import com.readerbench.coreservices.data.Word;
import com.readerbench.coreservices.data.discourse.SemanticCohesion;
import com.readerbench.coreservices.data.document.Document;
import com.readerbench.coreservices.data.lexicalchains.LexicalChain;
import com.readerbench.coreservices.data.lexicalchains.LexicalChainLink;
import com.readerbench.coreservices.nlp.parsing.Parsing;
import com.readerbench.coreservices.semanticmodels.SemanticModel;
import com.readerbench.coreservices.semanticmodels.SimilarityType;
import com.readerbench.coreservices.semanticmodels.wordnet.OntologySupport;
import com.readerbench.datasourceprovider.pojo.Lang;

import difflib.Chunk;
import difflib.DeleteDelta;
import difflib.Delta;
import difflib.DiffRow;
import difflib.DiffUtils;
import difflib.InsertDelta;
import difflib.Patch;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import java.util.List;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Adrian-Ionuț Tănase
 */
public class Diff {

    static Double DELTA_LDA = 0.3;
    static Double DELTA_LSA = 0.3;
    static Double DELTA_W2V = 0.3;
    static float DELTAWordNetDiff = 0.7f;
    
    public static enum DiffStrategy {
        WordNetDiff, ClassicDiff, DisambiguationGraphDiff, LSA, LDA, W2V
    }
    
    private SimilarityType SIM_TYPE = SimilarityType.WU_PALMER;
    private final Lang language;
    private DiffStrategy diffStrategy;
    private boolean skipNameEntities;
    private final Document originalDoc;
    private final Document revisedDoc;
    private final List<SemanticModel> semanticModels = new ArrayList<>();

    public Diff(String original, String revised, DiffStrategy diffStrategy, Lang lang, boolean skipNameEntities) {
        this.language = lang;
        this.diffStrategy = diffStrategy;
        this.skipNameEntities = skipNameEntities;
        switch (diffStrategy) {
        case LSA:
            this.semanticModels.add(SemanticModel.loadModel("TASA", this.language, SimilarityType.LSA));
            break;
        case LDA:
            this.semanticModels.add(SemanticModel.loadModel("TASA", this.language, SimilarityType.LDA));
            break;
        case W2V:
            this.semanticModels.add(SemanticModel.loadModel("TASA", this.language, SimilarityType.WORD2VEC));
            break;
        }
        
        this.originalDoc = createDisambiguationGraph(original);
        this.revisedDoc = createDisambiguationGraph(revised);
    }
    
    public Diff(String original, String revised, DiffStrategy diffStrategy, Lang lang, SimilarityType similarityType, boolean skipNameEntities) {
        this(original, revised, diffStrategy, lang, skipNameEntities);
        this.SIM_TYPE = similarityType;
    }

    private Document createDisambiguationGraph(String text) {
        Document d;
       
        d = new Document(null, this.semanticModels, language);
        AbstractDocumentTemplate docTmp = AbstractDocumentTemplate.getDocumentModel(text);
        Parsing.parseDoc(docTmp, d, true, language);

        if (this.diffStrategy == DiffStrategy.DisambiguationGraphDiff) {
            DisambiguisationGraphAndLexicalChains.buildDisambiguationGraph(d);
            DisambiguisationGraphAndLexicalChains.pruneDisambiguationGraph(d);
            DisambiguisationGraphAndLexicalChains.buildLexicalChains(d);
        }

        return d;
    }

    private List<Delta<AnalysisElement>> reviseDelta(Delta<AnalysisElement> delta, AnalysisElement w1, AnalysisElement w2) {
        LinkedList<Delta<AnalysisElement>> deltas = new LinkedList<>();
        // delete other words from list
        if (delta.getOriginal().getLines().size() > 1) {
            int index = delta.getOriginal().getLines().indexOf(w1);
            // delete after 
            delta.getOriginal().getLines().remove(w1);
            if (index == 0) {
                deltas.add(new DeleteDelta<>(
                        new Chunk<>(delta.getOriginal().getPosition() + 1, delta.getOriginal().getLines()),
                        new Chunk<>(0, new LinkedList<>())));
                return deltas;
            }
            // delete before
            if (index == delta.getOriginal().getLines().size()) {
                deltas.add(new DeleteDelta<>(
                        new Chunk<>(delta.getOriginal().getPosition(), delta.getOriginal().getLines()),
                        new Chunk<>(0, new LinkedList<>())));
                return deltas;
            }
            // delete others
            deltas.add(new DeleteDelta<>(
                    new Chunk<>(delta.getOriginal().getPosition(), delta.getOriginal().getLines().subList(0, index)),
                    new Chunk<>(0, new LinkedList<>())));

            deltas.add(new DeleteDelta<>(
                    new Chunk<>(delta.getOriginal().getPosition() + index + 1, delta.getOriginal().getLines().subList(index, delta.getOriginal().getLines().size())),
                    new Chunk<>(0, new LinkedList<>())));
            return deltas;
        } else {
            // insert other words from list
            if (delta.getRevised().getLines().size() > 1) {
                int index = delta.getRevised().getLines().indexOf(w2);
                // insert before
                delta.getRevised().getLines().remove(w2);
                if (index == 0) {
                    deltas.add(new InsertDelta<>(
                            new Chunk<>(delta.getOriginal().getPosition() + 1, new LinkedList<>()),
                            new Chunk<>(0, delta.getRevised().getLines())));
                    return deltas;
                }
                // insert after
                if (index == delta.getRevised().getLines().size()) {
                    deltas.add(new InsertDelta<>(
                            new Chunk<>(delta.getOriginal().getPosition(), new LinkedList<>()),
                            new Chunk<>(0, delta.getRevised().getLines())));
                    return deltas;
                }
                // insert both parts
                deltas.add(new InsertDelta<>(
                        new Chunk<>(delta.getOriginal().getPosition(), new LinkedList<>()),
                        new Chunk<>(0, delta.getRevised().getLines().subList(0, index))));

                deltas.add(new InsertDelta<>(
                        new Chunk<>(delta.getOriginal().getPosition() + 1, new LinkedList<>()),
                        new Chunk<>(0, delta.getRevised().getLines().subList(index, delta.getRevised().getLines().size()))));
                return deltas;
            }
        }

        return null;
    }

    private String getSenseFromChain(LexicalChain lc, Word word1) {
        for (LexicalChainLink link : lc.getLinks()) {
            if (link.getWord().compareTo(word1) == 0) {
                return link.getSenseId();
            }
        }
        
        return null;
    }

    private List<Delta<AnalysisElement>> ResolveChangedDeltasWords(Delta<AnalysisElement> delta) {
        LinkedList<Delta<AnalysisElement>> deltas = new LinkedList<>();
        SemanticCohesion sc;
        
        for (AnalysisElement w1 : delta.getOriginal().getLines()) {
            for (AnalysisElement w2 : delta.getRevised().getLines()) {
                switch (this.diffStrategy) {
                    case ClassicDiff:
                        deltas.add(delta);
                        return deltas;
                    case WordNetDiff:
//                        System.out.println(OntologySupport.semanticSimilarity(w1, w2, SIM_TYPE));
                        if (OntologySupport.semanticSimilarity((Word) w1, (Word) w2, SIM_TYPE) > DELTAWordNetDiff) {
                            return reviseDelta(delta, w1, w2);
                        }
                        
                        break;
                    case DisambiguationGraphDiff:
                        String senseIdOriginal = null, senseIdRevised = null;

                        for (LexicalChain lc : this.originalDoc.getLexicalChains()) {
                            if ((senseIdOriginal = getSenseFromChain(lc, (Word) w1)) != null) {
                                break;
                            }
                        }

                        for (LexicalChain lc : revisedDoc.getLexicalChains()) {
                            if ((senseIdRevised = getSenseFromChain(lc, (Word) w2)) != null) {
                                break;
                            }
                        }

                        if (OntologySupport.areSynonyms(senseIdOriginal, senseIdRevised, language)
                                || OntologySupport.areDirectHypernyms(senseIdOriginal, senseIdRevised, language)
                                || OntologySupport.areDirectHyponyms(senseIdOriginal, senseIdRevised, language)
                                || OntologySupport.areSiblings(senseIdOriginal, senseIdRevised, language)) {
                            return reviseDelta(delta, w1, w2);
                        }
                        
                        break;
                    case LSA:
                        sc = new SemanticCohesion(w1, w2);
//                        System.out.println(sc.getSemanticSimilarities().get(SimilarityType.LSA));
                        if (sc.getSemanticSimilarities().get(SimilarityType.LSA) > DELTA_LSA) {
                            return reviseDelta(delta, w1, w2);
                        }
                        
                        break;
                    case LDA:
                        sc = new SemanticCohesion(w1, w2);
//                        System.out.println(sc.getSemanticSimilarities().get(SimilarityType.LDA));
                        if (sc.getSemanticSimilarities().get(SimilarityType.LDA) > DELTA_LDA) {
                            return reviseDelta(delta, w1, w2);
                        }
                        
                        break;
                    case W2V:
                        sc = new SemanticCohesion(w1, w2);
//                        System.out.println(sc.getSemanticSimilarities().get(SimilarityType.WORD2VEC));
                        if (sc.getSemanticSimilarities().get(SimilarityType.WORD2VEC) > DELTA_W2V) {
                            return reviseDelta(delta, w1, w2);
                        }
                        
                        break;
                }
            }
        }
        
        deltas.add(delta);
        return deltas;
    }
    
    private List<AnalysisElement> toList(List<AnalysisElement> elem) {
        List<AnalysisElement> list = new LinkedList<>();

        elem.forEach((block) -> {
            if (block instanceof Sentence) {
                list.addAll(((Sentence)block).getAllWords());
            }
            if (block instanceof Block) {
                list.addAll(((Block)block).getSentences());
            }
        });

        return list;
    }

    private boolean diff(List<AnalysisElement> originalElem, List<AnalysisElement> revisedElem) {
        List<Delta<AnalysisElement>> revisedDelta = new LinkedList<>();

        Patch<AnalysisElement> patch;

        patch = DiffUtils.diff(originalElem, revisedElem,
                (AnalysisElement originalSentence, AnalysisElement revisedSentence) -> {
                    return originalSentence.getText().equals(revisedSentence.getText());
                });

        patch.getDeltas().forEach((delta) -> {
            if (originalElem.get(0) instanceof Word) {    
                LinkedList<Delta<AnalysisElement>> newDelta = null;
                if (delta.getType() != Delta.TYPE.CHANGE) {
                    revisedDelta.add(delta);
                }

                if (delta.getType() == Delta.TYPE.CHANGE
                        && (newDelta = (LinkedList<Delta<AnalysisElement>>) ResolveChangedDeltasWords(delta)) != null) {
                    revisedDelta.addAll(newDelta);
                }
            } else {
                if (delta.getType() == Delta.TYPE.CHANGE) {
                    boolean equal = diff(
                            toList(delta.getOriginal().getLines()),
                            toList(delta.getRevised().getLines()));
                    if (!equal) {
                        revisedDelta.add(delta);
                    }
                } else {
                    revisedDelta.add(delta);
                }
            }
        });

        List<DiffRow<AnalysisElement>> diffs = generateDiffElems(
                originalElem, revisedElem, revisedDelta);

        AnalysisElement lastBlock = ((AnalysisElement)originalElem.get(0)).getContainer();
        
        for (DiffRow<AnalysisElement> diff : diffs) {
            if (diff.getTag() == DiffRow.Tag.DELETE
                    || diff.getTag() == DiffRow.Tag.EQUAL
                    || diff.getTag() == DiffRow.Tag.CHANGE) {

                if (diff.getTag() == DiffRow.Tag.CHANGE && ((Object) diff.getOldLine()) instanceof String) {
                    lastBlock.getChanges().add((DiffRow<AnalysisElement>) diff);
                } else {
                    ((AnalysisElement) diff.getOldLine()).getContainer().getChanges().add((DiffRow<AnalysisElement>) diff);
                    lastBlock = ((AnalysisElement)diff.getOldLine()).getContainer();
                }
            } else {
                lastBlock.getChanges().add((DiffRow<AnalysisElement>) diff);
            }
        }

        return revisedDelta.isEmpty();
    }
    
    private List<DiffRow<AnalysisElement>> generateDiffElems(List<AnalysisElement> original, List<AnalysisElement> revised, List<Delta<AnalysisElement>> deltaList) {

        List<DiffRow<AnalysisElement>> diffRows = new ArrayList<>();
        int endPos = 0;
        
        for (int i = 0; i < deltaList.size(); i++) {
            Delta<AnalysisElement> delta = deltaList.get(i);
            Chunk<AnalysisElement> orig = delta.getOriginal();
            Chunk<AnalysisElement> rev = delta.getRevised();

            // catch the equal prefix for each chunk
            for (AnalysisElement line : original.subList(endPos, orig.getPosition())) {
                diffRows.add(new DiffRow(DiffRow.Tag.EQUAL, line, line));
            }

            // Inserted DiffRow
            if (delta.getClass().equals(InsertDelta.class)) {
                endPos = orig.last() + 1;
                ((List<AnalysisElement>) rev.getLines()).forEach((line) -> {
                    diffRows.add(new DiffRow(DiffRow.Tag.INSERT, "", line));
                });
                continue;
            }

            // Deleted DiffRow
            if (delta.getClass().equals(DeleteDelta.class)) {
                endPos = orig.last() + 1;
                ((List<AnalysisElement>) orig.getLines()).forEach((line) -> {
                    diffRows.add(new DiffRow(DiffRow.Tag.DELETE, line, ""));
                });
                continue;
            }

            // the changed size is match
            if (orig.size() == rev.size()) {
                for (int j = 0; j < orig.size(); j++) {
                    diffRows.add(new DiffRow(DiffRow.Tag.CHANGE, (AnalysisElement) orig.getLines().get(j),
                            (AnalysisElement) rev.getLines().get(j)));
                }
            } else if (orig.size() > rev.size()) {
                for (int j = 0; j < orig.size(); j++) {
                    diffRows.add(new DiffRow(DiffRow.Tag.CHANGE, (AnalysisElement) orig.getLines().get(j), rev
                            .getLines().size() > j ? (AnalysisElement) rev.getLines().get(j) : ""));
                }
            } else {
                for (int j = 0; j < rev.size(); j++) {
                    diffRows.add(new DiffRow(DiffRow.Tag.CHANGE, orig.getLines().size() > j ? (AnalysisElement) orig
                            .getLines().get(j) : "", (AnalysisElement) rev.getLines().get(j)));
                }
            }
            endPos = orig.last() + 1;
        }

        // Copy the final matching chunk if any.
        for (AnalysisElement line : original.subList(endPos, original.size())) {
            diffRows.add(new DiffRow(DiffRow.Tag.EQUAL, line, line));
        }

        return diffRows;
    }
    
    private String diffToString(AnalysisElement oldLine) {
        StringBuilder sb = new StringBuilder();

        oldLine.getChanges().stream().map((block) -> {
            if (block.getTag() == DiffRow.Tag.CHANGE) {
                if (oldLine instanceof Sentence){
                    if (((Object) block.getOldLine()) instanceof String) {
                    } else {
                        sb.append(colorText(block.getOldLine().getText(), "red"))
                                .append(" ");
                    }

                    if (((Object) block.getNewLine()) instanceof String) {
                    } else {
                        sb.append(colorText(block.getNewLine().getText(), "blue"))
                                .append(" ");
                    }
                } else {
                    if (((Object)block.getOldLine()) instanceof String)
                    {
                        sb.append(block.getOldLine());
                    } else 
                    {
                        sb.append(diffToString(block.getOldLine()));
                    }
                }
            }
            return block;
        }).map((block) -> {
            if (block.getTag() == DiffRow.Tag.EQUAL) {
                sb.append(block.getOldLine().getText());
            }
            return block;
        }).map((block) -> {
            if (block.getTag() == DiffRow.Tag.DELETE) {
                sb.append(colorText(block.getOldLine().getText(), "orange"));
            }
            return block;
        }).map((block) -> {
            if (block.getTag() == DiffRow.Tag.INSERT) {
                sb.append(colorText(block.getNewLine().getText(), "green"));
            }
            return block;
        }).forEachOrdered((_item) -> {
            if (oldLine instanceof Document) {
                sb.append("<br>");
            } else {
                sb.append(" ");
            }
        });

        return sb.toString();
    }

    private static String colorText(String text, String color) {
        String phrase = " <font color='" + color + "'><b>" + text + "</b></font> ";
        return phrase.trim();
    }

    private static String readFile(String filename) {
        BufferedReader bufferedReader;

        try {
            bufferedReader = new BufferedReader(new FileReader(filename));
            StringBuilder stringBuffer;
            stringBuffer = new StringBuilder();
            String line;

            while ((line = bufferedReader.readLine()) != null) {
                stringBuffer.append(line).append("\n");
            }

            return stringBuffer.toString();

        } catch (FileNotFoundException ex) {
            Logger.getLogger(Diff.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Diff.class.getName()).log(Level.SEVERE, null, ex);
        }

        return "";
    }

    public String diffToString() {
        return diffToString(originalDoc);
    }

    public Document diff() {
        this.diff((new LinkedList<>(originalDoc.getBlocks())), (new LinkedList<>(revisedDoc.getBlocks())));
        return originalDoc;
    }
    
    public static void main(String[] args) {
        String originalFilePath = "/home/tzuie/Desktop/dizertatie/diffData/dis1";
        String revisedFilePath = "/home/tzuie/Desktop/dizertatie/diffData/dis2";

        String original = readFile(originalFilePath);
        String revised = readFile(revisedFilePath);

        Diff diff;
        diff = new Diff(original, revised, DiffStrategy.WordNetDiff, Lang.en, true);
        diff.diff();
        System.out.println(diff.diffToString());
    }
}