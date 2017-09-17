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
package view.widgets.document;

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.border.EmptyBorder;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.renderer.category.GanttRenderer;
import org.jfree.data.gantt.Task;
import org.jfree.data.gantt.TaskSeries;
import org.jfree.data.gantt.TaskSeriesCollection;

import view.models.document.CustomToolTipGeneratorVoice;
import view.widgets.cscl.ChatVoiceSimpleStatistics;
import view.widgets.cscl.VoiceSynergyView;
import data.Block;
import data.Word;
import data.discourse.SemanticChain;
import data.document.Document;

public class SentenceLevelInterAnimationView extends JFrame {

    private static final long serialVersionUID = -7963939044051260680L;

    private JPanel contentPane;
    private Document document;

    /**
     * Create the frame.
     */
    public SentenceLevelInterAnimationView(Document d,
            List<SemanticChain> chains) {
        super("ReaderBench - Voice Inter-Animation");

        this.setSize(1000, 600);
        this.setLocation(50, 50);

        contentPane = new JPanel();
        contentPane.setBackground(Color.WHITE);
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        setContentPane(contentPane);

        TaskSeries s = new TaskSeries("Voices");
        this.document = d;
        Map<SemanticChain, LinkedList<Block>> subTasks = new TreeMap<SemanticChain, LinkedList<Block>>();

        // determine distribution of each semantic chain
        int noDim = 0;
        int[][] traceability = new int[d.getBlocks().size()][];
        for (int i = 0; i < d.getBlocks().size(); i++) {
            if (d.getBlocks().get(i) != null) {
                traceability[i] = new int[d.getBlocks().get(i).getSentences()
                        .size()];
                for (int j = 0; j < d.getBlocks().get(i).getSentences().size(); j++) {
                    traceability[i][j] = noDim++;
                }
            }
        }
        for (SemanticChain chain : chains) {
            subTasks.put(chain, new LinkedList<Block>());
            Task t = new Task(chain.toString(), new Date(0), new Date(noDim));
            s.add(t);

            Map<Integer, String> occurrences = new TreeMap<Integer, String>();

            for (Word w : chain.getWords()) {
                int blockIndex = w.getBlockIndex();
                int sentenceIndex = w.getUtteranceIndex();
                int index = traceability[blockIndex][sentenceIndex];
                if (occurrences.containsKey(index)) {
                    String text = occurrences.get(index);
                    if (text.contains(w.getLemma())) {
                        text.replace(w.getLemma(), w.getLemma() + "*");
                    } else {
                        text = text + " " + w.getLemma();
                    }
                    occurrences.put(index, text);
                } else {
                    occurrences.put(index, w.getLemma());
                }
            }

            for (Integer index : occurrences.keySet()) {
                Task subT = new Task(occurrences.get(index), new Date(index),
                        new Date(index + 1));
                t.addSubtask(subT);
            }
        }

        TaskSeriesCollection collection = new TaskSeriesCollection();
        collection.add(s);

        // create the chart...
        JFreeChart chart = ChartFactory.createGanttChart(
                "Voice Inter-Animation", // chart
                // title
                "Voice", // domain axis label
                "Sentence", // range axis label
                collection, // data
                false, // include legend
                false, // tooltips
                false // urls
        );

        CategoryPlot plot = (CategoryPlot) chart.getPlot();
        DateAxis range = (DateAxis) plot.getRangeAxis();
        DateFormat formatter = new SimpleDateFormat("S");
        range.setDateFormatOverride(formatter);

        GanttRenderer renderer = new GanttRenderer();
        renderer.setBaseToolTipGenerator(new CustomToolTipGeneratorVoice());
        plot.setRenderer(renderer);

        // add the chart to a panel...
        ChartPanel chartPanel = new ChartPanel(chart);

        JButton btnCrossCorrelations = new JButton("Cross-Correlations");
        btnCrossCorrelations.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                EventQueue.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        JFrame view = new VoiceSynergyView(document
                                .getSelectedVoices());
                        view.setVisible(true);
                    }
                });
            }
        });

        JButton btnSimpleStatistics = new JButton("Simple Statistics");
        btnSimpleStatistics.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                EventQueue.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        JFrame view = new ChatVoiceSimpleStatistics(document
                                .getSelectedVoices());
                        view.setVisible(true);
                    }
                });
            }
        });
        GroupLayout gl_contentPane = new GroupLayout(contentPane);
        gl_contentPane
                .setHorizontalGroup(gl_contentPane
                        .createParallelGroup(Alignment.LEADING)
                        .addGroup(
                                Alignment.TRAILING,
                                gl_contentPane
                                        .createSequentialGroup()
                                        .addContainerGap()
                                        .addGroup(
                                                gl_contentPane
                                                        .createParallelGroup(
                                                                Alignment.TRAILING)
                                                        .addComponent(
                                                                chartPanel,
                                                                Alignment.LEADING,
                                                                GroupLayout.DEFAULT_SIZE,
                                                                678,
                                                                Short.MAX_VALUE)
                                                        .addGroup(
                                                                gl_contentPane
                                                                        .createSequentialGroup()
                                                                        .addComponent(
                                                                                btnSimpleStatistics)
                                                                        .addPreferredGap(
                                                                                ComponentPlacement.RELATED)
                                                                        .addComponent(
                                                                                btnCrossCorrelations)))
                                        .addContainerGap()));
        gl_contentPane.setVerticalGroup(gl_contentPane.createParallelGroup(
                Alignment.LEADING)
                .addGroup(
                        Alignment.TRAILING,
                        gl_contentPane
                                .createSequentialGroup()
                                .addContainerGap()
                                .addComponent(chartPanel,
                                        GroupLayout.DEFAULT_SIZE, 427,
                                        Short.MAX_VALUE)
                                .addPreferredGap(ComponentPlacement.RELATED)
                                .addGroup(
                                        gl_contentPane
                                                .createParallelGroup(
                                                        Alignment.BASELINE)
                                                .addComponent(
                                                        btnCrossCorrelations)
                                                .addComponent(
                                                        btnSimpleStatistics))));
        contentPane.setLayout(gl_contentPane);
    }
}
