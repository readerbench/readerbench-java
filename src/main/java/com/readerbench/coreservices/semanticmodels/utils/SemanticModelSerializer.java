/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.readerbench.coreservices.semanticmodels.utils;

import com.readerbench.datasourceprovider.data.Word;
import com.readerbench.datasourceprovider.data.semanticmodels.ISemanticModel;
import com.readerbench.coreservices.semanticmodels.word2vec.Word2VecModel;
import com.readerbench.datasourceprovider.pojo.Lang;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    private static final Logger LOGGER = LoggerFactory.getLogger(SemanticModelSerializer.class);

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
            LOGGER.error(ex.getMessage());
        }
    }

    public static void main(String[] args) {
        Word2VecModel w2v = Word2VecModel.loadWord2Vec("resources/config/EN/word2vec/COCA", Lang.en);
        exportToCSV(w2v, "resources/config/EN/word2vec/COCA/export.csv");
    }
}
