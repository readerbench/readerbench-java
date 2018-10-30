/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.readerbench.coreservices.data.diff;

import com.readerbench.datasourceprovider.pojo.Lang;
import com.readerbench.coreservices.cna.DisambiguisationGraphAndLexicalChains;
import com.readerbench.coreservices.data.AbstractDocumentTemplate;
import com.readerbench.coreservices.data.AnalysisElement;
import com.readerbench.coreservices.data.Block;
import com.readerbench.coreservices.data.Sentence;
import com.readerbench.coreservices.data.Word;
import com.readerbench.coreservices.data.document.Document;
import com.readerbench.coreservices.data.lexicalchains.LexicalChain;
import com.readerbench.coreservices.data.lexicalchains.LexicalChainLink;
import com.readerbench.coreservices.nlp.parsing.Parsing;
import com.readerbench.coreservices.semanticmodels.SemanticModel;
import difflib.Chunk;
import difflib.DeleteDelta;
import difflib.Delta;
import difflib.DiffRow;
import difflib.InsertDelta;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Adrian-Ionuț Tănase
 */
public class Utils {
    
    static String readFile(String filename) {
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
     
    static List<DiffRow<AnalysisElement>> generateDiffElems(List<AnalysisElement> original, List<AnalysisElement> revised, List<Delta<AnalysisElement>> deltaList) {

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
    
    static List<AnalysisElement> toList(List<AnalysisElement> elem) {
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
    
    static String getSenseFromChain(LexicalChain lc, Word word1) {
        for (LexicalChainLink link : lc.getLinks()) {
            if (link.getWord().compareTo(word1) == 0) {
                return link.getSenseId();
            }
        }
        
        return null;
    }
    
    static Document createDisambiguationGraph(String text, List<SemanticModel> semanticModels, Lang language, Diff.DiffStrategy diffStrategy) {
        Document d;
       
        d = new Document(null, semanticModels, language);
        AbstractDocumentTemplate docTmp = AbstractDocumentTemplate.getDocumentModel(text);
        Parsing.parseDoc(docTmp, d, true, language);

        if (diffStrategy == Diff.DiffStrategy.DisambiguationGraphDiff) {
            DisambiguisationGraphAndLexicalChains.buildDisambiguationGraph(d);
            DisambiguisationGraphAndLexicalChains.pruneDisambiguationGraph(d);
            DisambiguisationGraphAndLexicalChains.buildLexicalChains(d);
        }

        return d;
    }
}
