/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package runtime.reviews;

import data.AbstractDocumentTemplate;
import data.Lang;
import data.document.Document;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.openide.util.Exceptions;
import services.complexity.ComplexityIndex;
import services.complexity.wordLists.WordListsIndicesFactory;
import services.semanticModels.ISemanticModel;
import webService.ReaderBenchServer;

/**
 *
 * @author stefan
 */
public class ComputeWordLists {

    public static void main(String[] args) {
        ReaderBenchServer.initializeDB();
        List<Document> reviews = new ArrayList<>();
        List<String[]> dataset = new ArrayList<>();
        try (BufferedReader in = new BufferedReader(new FileReader("reviews.csv"))) {
            String line = in.readLine();
            while ((line = in.readLine()) != null) {
                String[] row = line.split("\t");
                AbstractDocumentTemplate adt = AbstractDocumentTemplate.getDocumentModel(row[1]);
                reviews.add(new Document(adt, new ArrayList<>(), Lang.en, false));
                dataset.add(row);
            }
        } catch (FileNotFoundException ex) {
            Exceptions.printStackTrace(ex);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        List<ComplexityIndex> indices = new WordListsIndicesFactory().build(Lang.en);
        reviews.parallelStream()
                .forEach(review -> {
                    for (ComplexityIndex index : indices) {
                        double value = index.compute(review);
                        review.getComplexityIndices().put(index, value);
                    }
                });
        try (PrintWriter out = new PrintWriter("reviews-new.csv")) {
            out.println("sep=\t");
            out.println("game,review,grade," + 
                    StringUtils.join(
                            indices.stream().map(ComplexityIndex::getAcronym), 
                            ","));
            for (int i = 0; i < dataset.size(); i++) {
                String[] row = dataset.get(i);
                Document review = reviews.get(i);
                out.println(StringUtils.join(row, ",") + "," + 
                        StringUtils.join(
                            indices.stream()
                                    .map(ComplexityIndex::getAcronym)
                                    .map(index -> review.getComplexityIndices().get(index)), 
                            ","));
            }
        } catch (FileNotFoundException ex) {
            Exceptions.printStackTrace(ex);
        } 
        

    }
}
