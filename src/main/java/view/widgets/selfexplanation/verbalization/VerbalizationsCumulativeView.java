/* 
 * Copyright 2016 ReaderBench.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package view.widgets.selfexplanation.verbalization;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import services.commons.Formatting;
import services.commons.VectorAlgebra;
import view.models.verbalization.VerbalizationsCumulativeTableModel;
import view.models.verbalization.VerbalizationsTable;
import data.document.Metacognition;
import data.document.ReadingStrategyType;
import java.util.ArrayList;
import org.openide.util.Exceptions;

public class VerbalizationsCumulativeView extends JFrame {

    private static final long serialVersionUID = 1868758739957971713L;

    private final List<Metacognition> loadedVervalizations;
    private JTable tableVerbalizations;
    private DefaultTableModel modelVerbalizations;
    private int[] verbalizationIndex;
    private int[][] automaticReadingStrategies;
    private int[][] annotatedReadingStrategies;

    public VerbalizationsCumulativeView(List<Metacognition> loadedVervalizations) {
        super("ReaderBench - Verbalizations Cumulative Statistics");
        this.loadedVervalizations = loadedVervalizations;
        // adjust view to desktop size
        int margin = 50;
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        super.setBounds(margin, margin, screenSize.width - margin * 2, screenSize.height - margin * 2);

        createView();
        displayDetails();
    }

    private void createView() {
        verbalizationIndex = new int[loadedVervalizations.size() + 6
                + ReadingStrategyType.values().length * 2];
        // add 5 final rows corresponding to the statistics
        automaticReadingStrategies = new int[loadedVervalizations.size()][ReadingStrategyType.values().length];
        annotatedReadingStrategies = new int[loadedVervalizations.size()][ReadingStrategyType.values().length];

        int index;
        for (index = 0; index < loadedVervalizations.size(); index++) {
            if (loadedVervalizations.get(index) != null) {
                verbalizationIndex[index] = index;
                for (int i = 0; i < loadedVervalizations.get(index).getBlocks()
                        .size(); i++) {
                    for (int j = 0; j < ReadingStrategyType.values().length; j++) {
                        automaticReadingStrategies[index][j] += loadedVervalizations
                                .get(index).getAutomatedRS().get(i).get(ReadingStrategyType.values()[j]);
                        annotatedReadingStrategies[index][j] += loadedVervalizations
                                .get(index).getAnnotatedRS().get(i).get(ReadingStrategyType.values()[j]);
                    }
                }
            }
        }
        verbalizationIndex[index] = -1;

        modelVerbalizations = new VerbalizationsCumulativeTableModel();
        tableVerbalizations = new VerbalizationsTable(modelVerbalizations,
                verbalizationIndex);
        tableVerbalizations.getColumnModel().getColumn(0).setMinWidth(300);
        tableVerbalizations.getColumnModel().getColumn(0).setMaxWidth(300);
        tableVerbalizations.getColumnModel().getColumn(0)
                .setPreferredWidth(300);
        tableVerbalizations.setFillsViewportHeight(true);

        JScrollPane tableContainer = new JScrollPane(tableVerbalizations);
        getContentPane().add(tableContainer);
    }

    private void displayDetails() { // output results into corresponding file
        // and into table
        try {
            // Open the file
            FileWriter fstream = new FileWriter(
                    "out/verbalisations_cumulative_"
                    + System.currentTimeMillis() + ".csv", true);
            BufferedWriter file = new BufferedWriter(fstream);

            file.write("Author,"
                    + "Paraphrasing (Automatic),Paraphrasing (Manual),"
                    + "Causality (Automatic),Causality (Manual),"
                    + "Bridging (Automatic),Bridging (Manual),"
                    + "Text-based Inferences (Automatic),Text-based Inferences (Manual),"
                    + "Knowledge-based Inferences (Automatic),Knowledge-based Inferences (Manual),"
                    + "Control (Automatic),Control (Manual)"
                    + "Comprehension score,Comprehension class,Fluency\n");
            List<Object> dataRow;
            for (int index = 0; index < loadedVervalizations.size(); index++) {
                if (loadedVervalizations.get(index) != null) {
                    dataRow = new ArrayList<Object>();

                    String authors = "";
                    for (String author : loadedVervalizations.get(index)
                            .getAuthors()) {
                        authors += author + "; ";
                    }
                    authors = authors.substring(0, authors.length() - 2)
                            + " ("
                            + loadedVervalizations.get(index).getReferredDoc()
                            .getTitleText() + ")";
                    dataRow.add(authors);
                    file.write(authors);

                    for (int i = 0; i < ReadingStrategyType.values().length; i++) {
                        dataRow.add(automaticReadingStrategies[index][i] + " // " + annotatedReadingStrategies[index][i]);
                        file.write("," + automaticReadingStrategies[index][i] + "," + annotatedReadingStrategies[index][i]);
                    }

                    dataRow.add(loadedVervalizations.get(index).getAnnotatedComprehensionScore());
                    dataRow.add(loadedVervalizations.get(index).getComprehensionClass());
                    dataRow.add(loadedVervalizations.get(index).getAnnotatedFluency());
                    file.write("," + loadedVervalizations.get(index).getAnnotatedComprehensionScore()
                            + "," + loadedVervalizations.get(index).getComprehensionClass()
                            + "," + loadedVervalizations.get(index).getAnnotatedFluency());

                    modelVerbalizations.addRow(dataRow.toArray());
                    file.write("\n");
                }
            }
            dataRow = new ArrayList<>();
            String out = "* for each X // Y notation: (1) X denotes the automatically identified reading strategies and (2) Y represents the annotated ones";
            dataRow.add("<i>" + out + "</i>");
            modelVerbalizations.addRow(dataRow.toArray());

            // display statistics
            dataRow = new ArrayList<>();
            dataRow.add("<b>Statistics</b>");
            modelVerbalizations.addRow(dataRow.toArray());
            file.write("\n\nStatistics," + "Paraphrasing," + "Causality,"
                    + "Bridging," + "Text-based Inferences,"
                    + "Knowledge-based Inferences," + "Control\n");

            dataRow = new ArrayList<>();
            dataRow.add("Pearson correlation");
            file.write("Pearson correlation");
            for (int i = 0; i < ReadingStrategyType.values().length; i++) {
                out = Formatting
                        .formatNumber(VectorAlgebra.pearsonCorrelation(
                                VectorAlgebra.getVector(
                                        annotatedReadingStrategies, i),
                                VectorAlgebra.getVector(
                                        automaticReadingStrategies, i)))
                        + "";
                dataRow.add(out);
                file.write("," + out);
            }
            modelVerbalizations.addRow(dataRow.toArray());
            file.write("\n");

            dataRow = new ArrayList<>();
            dataRow.add("Precision");
            file.write("Precision");
            for (int i = 0; i < ReadingStrategyType.values().length; i++) {
                out = Formatting
                        .formatNumber(VectorAlgebra.precision(VectorAlgebra
                                .getVector(annotatedReadingStrategies, i),
                                VectorAlgebra.getVector(
                                        automaticReadingStrategies, i)))
                        + "";
                dataRow.add(out);
                file.write("," + out);
            }
            modelVerbalizations.addRow(dataRow.toArray());
            file.write("\n");

            dataRow = new ArrayList<>();
            dataRow.add("Recall");
            file.write("Recall");
            for (int i = 0; i < ReadingStrategyType.values().length; i++) {
                out = Formatting
                        .formatNumber(VectorAlgebra.recall(VectorAlgebra
                                .getVector(annotatedReadingStrategies, i),
                                VectorAlgebra.getVector(
                                        automaticReadingStrategies, i)))
                        + "";
                dataRow.add(out);
                file.write("," + out);
            }
            modelVerbalizations.addRow(dataRow.toArray());
            file.write("\n");

            dataRow = new ArrayList<>();
            dataRow.add("F1 score");
            file.write("F1 score");
            for (int i = 0; i < ReadingStrategyType.values().length; i++) {
                out = Formatting.formatNumber(VectorAlgebra.fscore(
                        VectorAlgebra.getVector(annotatedReadingStrategies, i),
                        VectorAlgebra.getVector(automaticReadingStrategies, i),
                        1))
                        + "";
                dataRow.add(out);
                file.write("," + out);
            }
            modelVerbalizations.addRow(dataRow.toArray());
            file.close();
        } catch (Exception e) {
            Exceptions.printStackTrace(e);
        }
    }
}
