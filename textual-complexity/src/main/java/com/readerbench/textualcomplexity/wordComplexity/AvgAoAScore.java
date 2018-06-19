/**
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.readerbench.textualcomplexity.wordComplexity;

import com.readerbench.coreservices.data.AbstractDocument;
import com.readerbench.coreservices.data.Word;
import com.readerbench.textualcomplexity.AbstractComplexityIndex;
import com.readerbench.textualcomplexity.ComplexityIndices;
import com.readerbench.textualcomplexity.ComplexityIndicesEnum;
import com.readerbench.textualcomplexity.IndexLevel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.*;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author stefan
 */
public class AvgAoAScore extends AbstractComplexityIndex {

    private static final Logger LOGGER = LoggerFactory.getLogger(AvgAoAScore.class);

    private final Map<String, Double> map;

    public AvgAoAScore(
            ComplexityIndicesEnum index,
            String aux,
            IndexLevel level,
            Map<String, Double> map) {
        super(index, aux, level);
        this.map = map;
    }

    @Override
    public double compute(AbstractDocument d) {
        return normalize(d, streamFunction.apply(d)
                .mapToDouble(b -> {
                    double sum = 0;
                    int count = 0;
                    for (Map.Entry<Word, Integer> e : b.getWordOccurences().entrySet()) {
                        if (map.containsKey(e.getKey().getLemma())) {
                            sum += map.get(e.getKey().getLemma()) * e.getValue();
                            count += e.getValue();
                        }
                    }
                    if (count == 0) {
                        return 0;
                    }
                    return sum / count;
                })
                .average().orElse(ComplexityIndices.IDENTITY));
    }

    public static void createAoACSV(String inputFolder, String output) {
        File folder = new File(inputFolder);
        SortedMap<String, double[]> words = new TreeMap<>();
        String[] csvs = folder.list((File dir, String name) -> name.endsWith(".csv"));
        for (int i = 0; i < csvs.length; i++) {
            try (BufferedReader in = new BufferedReader(new FileReader(inputFolder + "/" + csvs[i]))) {
                String line;
                while ((line = in.readLine()) != null) {
                    String[] split = line.split(",");
                    String word = split[0];
                    try {
                        double val = Double.parseDouble(split[1]);
                        if (!words.containsKey(word)) {
                            double[] vals = new double[csvs.length];
                            vals[i] = val;
                            words.put(word, vals);
                        } else {
                            words.get(word)[i] = val;
                        }
                    } catch (NumberFormatException ex) {

                    }
                }
            } catch (FileNotFoundException ex) {
                LOGGER.error(ex.getMessage());
            } catch (IOException ex) {
                LOGGER.error(ex.getMessage());
            }
        }
        try (PrintWriter out = new PrintWriter(output)) {
            out.println("sep=,");
            List<String> header = new ArrayList<>();
            header.add("Word");
            for (String csv : csvs) {
                header.add(csv.split("[.]")[0]);
            }
            out.println(StringUtils.join(header, ","));
            for (Map.Entry<String, double[]> e : words.entrySet()) {
                out.print(e.getKey() + ",");
                List<String> values = new ArrayList<>();
                for (double val : e.getValue()) {
                    if (val != 0) {
                        values.add(val + "");
                    } else {
                        values.add("");
                    }
                }
                out.println(StringUtils.join(values, ","));
            }
        } catch (FileNotFoundException ex) {
            LOGGER.error(ex.getMessage());
        }
    }

    public static void main(String[] args) {
        createAoACSV("resources/config/EN/word lists/AoA", "resources/config/EN/word lists/AoA.csv");
    }
}
