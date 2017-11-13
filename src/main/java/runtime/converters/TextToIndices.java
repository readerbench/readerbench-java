/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package runtime.converters;

import data.AbstractDocumentTemplate;
import data.Lang;
import data.document.Document;
import edu.stanford.nlp.util.Pair;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.openide.util.Exceptions;
import services.complexity.CAF.CAFFactory;
import services.complexity.ComplexityIndex;
import services.complexity.ComplexityIndexType;
import services.complexity.ComplexityIndices;
import services.complexity.cohesion.CohesionFactory;
import services.complexity.connectives.ConnectivesFactory;
import services.complexity.coreference.CoreferenceFactory;
import services.complexity.dependencies.SyntacticDependenciesFactory;
import services.complexity.dialogism.DialogismFactory;
import services.complexity.entityDensity.EntityDensityFactory;
import services.complexity.readability.ReadabilityFactory;
import services.complexity.surface.SurfaceFactory;
import services.complexity.syntax.SyntaxFactory;
import services.complexity.wordComplexity.WordComplexityFactory;
import services.complexity.wordLists.WordListsIndicesFactory;
import webService.ReaderBenchServer;

/**
 *
 * @author stefan
 */
public class TextToIndices {

    private List<ComplexityIndex> indices;

    public TextToIndices(Lang lang) {
        ComplexityIndexType[] types = {
            ComplexityIndexType.READABILITY,
            ComplexityIndexType.SURFACE,
            ComplexityIndexType.SYNTAX,
            ComplexityIndexType.WORD_COMPLEXITY,
            ComplexityIndexType.ENTITY_DENSITY,
            ComplexityIndexType.CONNECTIVES,
            ComplexityIndexType.SEMANTIC_DEPENDENCIES,
        };
        indices = Arrays.stream(types)
                .filter(cat -> cat.getFactory() != null)
                .map(cat -> cat.getFactory())
                .flatMap(f -> f.build(lang).stream())
                .collect(Collectors.toList());
    }

    public double[] computeIndices(String text) {
        AbstractDocumentTemplate adt = AbstractDocumentTemplate.getDocumentModel(text);
        Document doc = new Document(adt, new ArrayList<>(), Lang.en, false);
        return indices.stream()
                .mapToDouble(ind -> ind.compute(doc))
                .toArray();
    }

    public static void textToCSV(String inputFile, String outputFile) {
        TextToIndices t2i = new TextToIndices(Lang.en);
        try (BufferedReader in = new BufferedReader(new FileReader(inputFile));
                PrintWriter out = new PrintWriter(outputFile)) {
            String sep = ";";
            out.println("sep=" + sep);
            out.println("target" + sep + t2i.indices.stream()
                    .map(ComplexityIndex::getAcronym)
                    .collect(Collectors.joining(sep)));
            in.readLine();
            String line = in.readLine();
            List<Pair<String, String>> texts = new ArrayList<>();
            while ((line = in.readLine()) != null) {
                String[] split = line.split(";");
                if (split.length < 8) {
                    continue;
                }
                String target = split[7];
                String question = split[6];
                texts.add(new Pair<>(target, question));
            }
            List<Pair<String, double[]>> processed = texts.parallelStream()
                    .map(pair -> new Pair<>(pair.first, t2i.computeIndices(pair.second)))
                    .collect(Collectors.toList());
            for (Pair<String, double[]> pair : processed) {
                out.println(pair.first + sep + Arrays.stream(pair.second)
                        .mapToObj(x -> x + "")
                        .collect(Collectors.joining(sep)));
            }
        } catch (FileNotFoundException ex) {
            Exceptions.printStackTrace(ex);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
    
    public static void main(String[] args) {
        ReaderBenchServer.initializeDB();
        textToCSV("resources/in/Q&A/Question corpus/train.csv", "resources/in/Q&A/Question corpus/train-indices.csv");
    }
}
