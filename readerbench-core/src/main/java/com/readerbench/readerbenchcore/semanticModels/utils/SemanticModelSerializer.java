/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.readerbench.readerbenchcore.semanticModels.utils;

import com.readerbench.data.Lang;
import com.readerbench.data.Word;
import org.openide.util.Exceptions;
import com.readerbench.readerbenchcore.semanticModels.ISemanticModel;
import com.readerbench.readerbenchcore.semanticModels.word2vec.Word2VecModel;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

/**
 *
 * @author stefan
 */
public class SemanticModelSerializer {
    public static void exportToCSV(ISemanticModel model, String fileName) {
        exportToCSV(model, fileName, ' ');
    }
    
    public static void exportToCSV(ISemanticModel model, String fileName, char sep) {
        try (PrintWriter out = new PrintWriter(fileName)) {
            out.println("sep=" + sep);
            out.println(model.getNoDimensions());
            for (Map.Entry<Word, double[]> entry : model.getWordRepresentations().entrySet()) {
                String v = Arrays.stream(entry.getValue())
                        .mapToObj(d -> d + "")
                        .collect(Collectors.joining(sep + ""));
                out.println(entry.getKey().getText() + sep + v);
            }
        } catch (FileNotFoundException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    public static void main(String[] args) {
        Word2VecModel w2v = Word2VecModel.loadWord2Vec("resources/config/EN/word2vec/COCA", Lang.en);
        exportToCSV(w2v, "resources/config/EN/word2vec/COCA/export.csv");
    }
}
