/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package services.semanticModels.utils;

import data.Lang;
import data.Word;
import edu.stanford.nlp.util.StringUtils;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.openide.util.Exceptions;
import services.semanticModels.ISemanticModel;
import services.semanticModels.LDA.LDA;
import services.semanticModels.LSA.LSA;
import services.semanticModels.word2vec.Word2VecModel;

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
