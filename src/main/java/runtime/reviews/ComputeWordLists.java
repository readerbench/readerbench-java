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
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.openide.util.Exceptions;
import services.complexity.AbstractComplexityIndex;
import services.complexity.ComplexityIndex;
import services.complexity.wordLists.WordListsIndicesFactory;
import services.semanticModels.ISemanticModel;
import utils.IndexLevel;
import webService.ReaderBenchServer;

/**
 *
 * @author stefan
 */
public class ComputeWordLists {

    public static void main(String[] args) {
        ReaderBenchServer.initializeDB();
        try (BufferedReader in = new BufferedReader(new FileReader("reviews.csv"));
                PrintWriter out = new PrintWriter("reviews-new.csv")) {
            String line = in.readLine();
            out.println(line);
            List<ComplexityIndex> indices = new WordListsIndicesFactory().build(Lang.en)
                    .stream()
                    .filter(index -> ((AbstractComplexityIndex)index).getLevel() == IndexLevel.DOC)
                    .collect(Collectors.toList());
            System.out.println(indices.size() + " word lists");
            out.println("game,review,grade," + 
                    StringUtils.join(
                            indices.stream().map(ComplexityIndex::getAcronym).collect(Collectors.toList()), 
                            ","));
            while ((line = in.readLine()) != null) {
                String[] row = line.split("\t");
                AbstractDocumentTemplate adt = AbstractDocumentTemplate.getDocumentModel(row[1]);
                Document review = new Document(adt, new ArrayList<>(), Lang.en, false);
                review.setComplexityIndices(new HashMap<>());
                for (ComplexityIndex index : indices) {
                    double value = index.compute(review);
                    review.getComplexityIndices().put(index, value);
                }    
                out.println(StringUtils.join(row, ",") + "," + 
                        StringUtils.join(
                            indices.stream()
                                    .map(index -> review.getComplexityIndices().get(index))
                                    .collect(Collectors.toList()), 
                            ","));
            }
        } catch (FileNotFoundException ex) {
            Exceptions.printStackTrace(ex);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
}
