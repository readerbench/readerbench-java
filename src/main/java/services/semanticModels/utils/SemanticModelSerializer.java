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
import services.semanticModels.LSA.LSA;

/**
 *
 * @author stefan
 */
public class SemanticModelSerializer {
    
    public static void exportToText(ISemanticModel model, String fileName) {
        try (PrintWriter out = new PrintWriter(fileName)) {
            out.println(model.getNoDimensions());
            for (Map.Entry<Word, double[]> entry : model.getWordRepresentations().entrySet()) {
                String v = Arrays.stream(entry.getValue())
                        .mapToObj(d -> d + "")
                        .collect(Collectors.joining(" "));
                out.println(entry.getKey().getText() + "\t" + v);
            }
        } catch (FileNotFoundException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
    
    public static void main(String[] args) {
        LSA lsa = LSA.loadLSA("resources/config/EN/LSA/SciRef", Lang.en);
        exportToText(lsa, "text.txt");
    }
    
}
