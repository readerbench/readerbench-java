/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package services.semanticModels.utils;

import data.Lang;
import data.Word;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;
import org.openide.util.Exceptions;
import services.semanticModels.ISemanticModel;
import services.semanticModels.LDA.LDA;
import services.semanticModels.LSA.LSA;
import services.semanticModels.word2vec.Word2VecModel;
import webService.ReaderBenchServer;

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
        ReaderBenchServer.initializeDB();

        LSA lsa = LSA.loadLSA("resources/config/EN/LSA/COCA_newspaper_magazine", Lang.en);
        exportToText(lsa, "resources/config/EN/LSA/COCA_newspaper_magazine/COCA_newspaper_magazine_export.csv");

        LDA lda = LDA.loadLDA("resources/config/EN/LDA/COCA_newspaper_magazine", Lang.en);
        exportToText(lda, "resources/config/EN/LDA/COCA_newspaper_magazine/COCA_newspaper_magazine_export.csv");

        Word2VecModel w2v = Word2VecModel.loadWord2Vec("resources/config/EN/word2vec/COCA_newspaper_magazine", Lang.en);
        exportToText(w2v, "resources/config/EN/word2vec/COCA_newspaper_magazine/COCA_newspaper_magazine_export.csv");
    }
}
