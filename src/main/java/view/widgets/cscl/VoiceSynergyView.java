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
package view.widgets.cscl;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.ArrayList;
import java.util.TreeMap;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.table.DefaultTableModel;

import org.jfree.data.gantt.TaskSeries;

import data.discourse.SemanticChain;
import data.discourse.SentimentVoice;
import services.commons.Formatting;
import services.discourse.dialogism.DialogismMeasures;
import utils.LocalizationUtils;

public class VoiceSynergyView extends JFrame {

    private static final long serialVersionUID = 2897644814459831682L;

    private JPanel contentPane;
    private JPanel panelMutualInformation;
    private JPanel panelCoOccurrence;
    private JPanel panelMovingAverage;
    private JPanel panelSentimentEvolution;
    private DefaultTableModel tableModelCorrelationMatrix;
    private JTable tableCorrelationMatrix;
    private JComboBox<String> comboBox;
    private List<SemanticChain> displayedVoices;

    /**
     * Create the frame.
     */
    public VoiceSynergyView(List<SemanticChain> chains) {
        super.setTitle("ReaderBench - " + LocalizationUtils.getTitle(this.getClass()));
        super.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        this.displayedVoices = chains;
        super.setBounds(100, 100, 650, 600);
        contentPane = new JPanel();
        contentPane.setBackground(Color.WHITE);
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        super.setContentPane(contentPane);

        JLabel lblCorrelationType = new JLabel(LocalizationUtils.getLocalizedString(this.getClass(), "lblCorrelationType") + ":");

        comboBox = new JComboBox<String>();
        comboBox.addItem(LocalizationUtils.getLocalizedString(this.getClass(), "addItem1"));
        comboBox.addItem(LocalizationUtils.getLocalizedString(this.getClass(), "addItem2"));
        comboBox.addItem(LocalizationUtils.getLocalizedString(this.getClass(), "addItem3"));
        comboBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (comboBox.getSelectedIndex() >= 0) {
                    // display different matrixes
                    double[][] matrix = null;
                    switch (comboBox.getSelectedIndex()) {
                        case 0:
                            matrix = DialogismMeasures.getBlockCorrelationMatrix(VoiceSynergyView.this.displayedVoices);
                            break;
                        case 1:
                            matrix = DialogismMeasures
                                    .getMovingAverageCorrelationMatrix(VoiceSynergyView.this.displayedVoices);
                            break;
                        case 2:
                            matrix = DialogismMeasures
                                    .getBlockMutualInformationMatrix(VoiceSynergyView.this.displayedVoices);
                            break;
                    }
                    updateContents(matrix);
                }
            }
        });

        JScrollPane scrollPaneCorrelationMatrix = new JScrollPane();

        JLabel lblCorrelationMatrix = new JLabel(LocalizationUtils.getLocalizedString(this.getClass(), "lblCorrelationMatrix"));
        lblCorrelationMatrix.setFont(new Font("Lucida Grande", Font.BOLD, 13));

        JLabel lblVoiceConvergence = new JLabel(LocalizationUtils.getLocalizedString(this.getClass(), "lblVoiceConvergence"));
        lblVoiceConvergence.setFont(new Font("Lucida Grande", Font.BOLD, 13));

        panelMutualInformation = new JPanel();
        panelMutualInformation.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
        panelMutualInformation.setBackground(Color.WHITE);
        panelMutualInformation.setLayout(new BorderLayout());

        panelCoOccurrence = new JPanel();
        panelCoOccurrence.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
        panelCoOccurrence.setBackground(Color.WHITE);
        panelCoOccurrence.setLayout(new BorderLayout());

        panelMovingAverage = new JPanel();
        panelMovingAverage.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
        panelMovingAverage.setBackground(Color.WHITE);
        panelMovingAverage.setLayout(new BorderLayout());

        panelSentimentEvolution = new JPanel();
        panelSentimentEvolution.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
        panelSentimentEvolution.setBackground(Color.WHITE);
        panelSentimentEvolution.setLayout(new BorderLayout());

        GroupLayout gl_contentPane = new GroupLayout(contentPane);
        gl_contentPane
                .setHorizontalGroup(
                        gl_contentPane.createParallelGroup(Alignment.TRAILING)
                                .addGroup(gl_contentPane.createSequentialGroup().addContainerGap()
                                        .addGroup(gl_contentPane.createParallelGroup(Alignment.LEADING)
                                                .addComponent(panelMutualInformation, GroupLayout.DEFAULT_SIZE, 628,
                                                        Short.MAX_VALUE)
                                                .addComponent(panelCoOccurrence, GroupLayout.DEFAULT_SIZE, 628, Short.MAX_VALUE)
                                                .addComponent(panelSentimentEvolution, GroupLayout.DEFAULT_SIZE, 628,
                                                        Short.MAX_VALUE)
                                                .addComponent(scrollPaneCorrelationMatrix, GroupLayout.DEFAULT_SIZE, 628, Short.MAX_VALUE)
                                                .addGroup(gl_contentPane.createSequentialGroup().addComponent(lblCorrelationType)
                                                        .addPreferredGap(ComponentPlacement.RELATED)
                                                        .addComponent(comboBox, GroupLayout.PREFERRED_SIZE, 290, GroupLayout.PREFERRED_SIZE))
                                                .addComponent(lblCorrelationMatrix)
                                                .addComponent(panelMovingAverage, GroupLayout.DEFAULT_SIZE, 628, Short.MAX_VALUE)
                                                .addComponent(lblVoiceConvergence)).addContainerGap()));
        gl_contentPane.setVerticalGroup(gl_contentPane.createParallelGroup(Alignment.LEADING)
                .addGroup(gl_contentPane.createSequentialGroup().addContainerGap()
                        .addGroup(gl_contentPane.createParallelGroup(Alignment.BASELINE)
                                .addComponent(lblCorrelationType).addComponent(comboBox, GroupLayout.PREFERRED_SIZE,
                                GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(ComponentPlacement.RELATED).addComponent(lblCorrelationMatrix)
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addComponent(scrollPaneCorrelationMatrix, GroupLayout.PREFERRED_SIZE, 110, GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(ComponentPlacement.RELATED).addComponent(lblVoiceConvergence)
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addComponent(panelCoOccurrence, GroupLayout.DEFAULT_SIZE, 120, Short.MAX_VALUE)
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addComponent(panelMovingAverage, GroupLayout.DEFAULT_SIZE, 111, Short.MAX_VALUE)
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addComponent(panelMutualInformation, GroupLayout.DEFAULT_SIZE, 120, Short.MAX_VALUE)
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addComponent(panelSentimentEvolution, GroupLayout.DEFAULT_SIZE, 120, Short.MAX_VALUE)
                        .addContainerGap())
        );

        String[] columnNames = new String[displayedVoices.size() + 1];
        columnNames[0] = "";
        for (int i = 0; i < displayedVoices.size(); i++) {
            columnNames[i + 1] = i + "";
        }

        tableModelCorrelationMatrix = new DefaultTableModel(new Object[][]{}, columnNames) {
            private static final long serialVersionUID = -5454968717710196231L;

            @Override
            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return false;
            }
        };

        tableCorrelationMatrix = new JTable(tableModelCorrelationMatrix);
        tableCorrelationMatrix.getColumnModel().getColumn(0).setMinWidth(250);
        tableCorrelationMatrix.getColumnModel().getColumn(0).setMaxWidth(250);
        tableCorrelationMatrix.getColumnModel().getColumn(0).setPreferredWidth(250);
        tableCorrelationMatrix.setFillsViewportHeight(true);

        scrollPaneCorrelationMatrix.setViewportView(tableCorrelationMatrix);

        contentPane.setLayout(gl_contentPane);

        double[][] displayedMatrix = DialogismMeasures.getBlockCorrelationMatrix(displayedVoices);
        updateContents(displayedMatrix);

        double[] evolution1 = DialogismMeasures.getCoOccurrenceBlockEvolution(displayedVoices);
        double[] evolution2 = DialogismMeasures.getCumulativeBlockMuvingAverageEvolution(displayedVoices);
        double[] evolution3 = DialogismMeasures.getAverageBlockMutualInformationEvolution(displayedVoices);

        Double[][] values1 = new Double[1][evolution1.length];
        Double[][] values2 = new Double[1][evolution2.length];
        Double[][] values3 = new Double[1][evolution3.length];
        double[] columns = new double[evolution1.length];

		String[] name1 = {LocalizationUtils.getLocalizedString(this.getClass(), "name1")};
		String[] name2 = {LocalizationUtils.getLocalizedString(this.getClass(), "name2")};
		String[] name3 = {LocalizationUtils.getLocalizedString(this.getClass(), "name3")};
        EvolutionGraph evolutionGraph1 = new EvolutionGraph("", LocalizationUtils.getLocalizedString(this.getClass(), "evoGraph"), false, name1, values1, columns,
                Color.DARK_GRAY);
        EvolutionGraph evolutionGraph2 = new EvolutionGraph("", LocalizationUtils.getLocalizedString(this.getClass(), "evoGraph"), false, name2, values2, columns,
                Color.DARK_GRAY);
        EvolutionGraph evolutionGraph3 = new EvolutionGraph("", LocalizationUtils.getLocalizedString(this.getClass(), "evoGraph"), false, name3, values3, columns,
                Color.DARK_GRAY);

        panelCoOccurrence.add(evolutionGraph1.evolution());
        panelMovingAverage.add(evolutionGraph2.evolution());
        panelMutualInformation.add(evolutionGraph3.evolution());
    }

    private void updateContents(double[][] displayedContents) {
        while (tableModelCorrelationMatrix.getRowCount() > 0) {
            tableModelCorrelationMatrix.removeRow(0);
        }

        for (int i = 0; i < displayedVoices.size(); i++) {
            SemanticChain voice = displayedVoices.get(i);
            Vector<Object> dataRow = new Vector<Object>();

            dataRow.add(i + " - " + voice.toString());
            int j = 0;
            for (; j < i; j++) {
                dataRow.add(Formatting.formatNumber(displayedContents[i][j]));
            }
            for (; j < displayedVoices.size(); j++) {
                dataRow.add("");
            }
            tableModelCorrelationMatrix.addRow(dataRow);
        }
    }
}
