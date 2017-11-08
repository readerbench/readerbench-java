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
package view.widgets.metacognition.verbalization;

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.logging.Logger;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.ScrollPaneConstants;
import javax.swing.table.DefaultTableModel;

import services.commons.Formatting;
import view.models.verbalization.VerbalizationTable;
import view.models.verbalization.VerbalizationTableModel;
import view.widgets.document.DocumentView;
import data.Sentence;
import data.discourse.SemanticCohesion;
import data.document.Metacognition;
import data.document.ReadingStrategyType;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import utils.LocalizationUtils;

/**
 *
 * @author Mihai Dascalu
 */
public class VerbalizationView extends JFrame {

    private static final long serialVersionUID = -4709511294166379162L;

    static final Logger LOGGER = Logger.getLogger("");

    private Metacognition verbalization;
    private final DefaultTableModel modelContents;
    private final JTable tableContents;
    private boolean[] isVerbalisation;

    public VerbalizationView(Metacognition verbalizationToDisplay) {
        super.setTitle("ReaderBench - " + LocalizationUtils.getTitle(this.getClass()));
        this.verbalization = verbalizationToDisplay;
        super.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        super.getContentPane().setBackground(Color.WHITE);

        JPanel panelHeader = new JPanel();
        panelHeader.setBackground(Color.WHITE);

        JPanel panelContents = new JPanel();
        panelContents.setBackground(Color.WHITE);
        GroupLayout groupLayout = new GroupLayout(super.getContentPane());
        groupLayout
                .setHorizontalGroup(groupLayout
                        .createParallelGroup(Alignment.TRAILING)
                        .addGroup(
                                groupLayout
                                        .createSequentialGroup()
                                        .addContainerGap()
                                        .addGroup(
                                                groupLayout
                                                        .createParallelGroup(
                                                                Alignment.TRAILING)
                                                        .addComponent(
                                                                panelContents,
                                                                Alignment.LEADING,
                                                                GroupLayout.DEFAULT_SIZE,
                                                                1168,
                                                                Short.MAX_VALUE)
                                                        .addComponent(
                                                                panelHeader,
                                                                Alignment.LEADING,
                                                                GroupLayout.DEFAULT_SIZE,
                                                                1168,
                                                                Short.MAX_VALUE))
                                        .addContainerGap()));
        groupLayout.setVerticalGroup(groupLayout.createParallelGroup(
                Alignment.LEADING).addGroup(
                        groupLayout
                                .createSequentialGroup()
                                .addContainerGap()
                                .addComponent(panelHeader, GroupLayout.PREFERRED_SIZE,
                                        73, GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(ComponentPlacement.RELATED)
                                .addComponent(panelContents, GroupLayout.DEFAULT_SIZE,
                                        587, Short.MAX_VALUE).addContainerGap()));

        JLabel lblContents = new JLabel(LocalizationUtils.getLocalizedString(this.getClass(), "lblContents"));
        lblContents.setFont(new Font("SansSerif", Font.BOLD, 13));

        JScrollPane scrollPane = new JScrollPane();
        scrollPane
                .setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        GroupLayout gl_panelContents = new GroupLayout(panelContents);
        gl_panelContents
                .setHorizontalGroup(gl_panelContents
                        .createParallelGroup(Alignment.LEADING)
                        .addGroup(
                                gl_panelContents
                                        .createSequentialGroup()
                                        .addGroup(
                                                gl_panelContents
                                                        .createParallelGroup(
                                                                Alignment.LEADING)
                                                        .addGroup(
                                                                gl_panelContents
                                                                        .createSequentialGroup()
                                                                        .addContainerGap()
                                                                        .addComponent(
                                                                                lblContents))
                                                        .addGroup(
                                                                gl_panelContents
                                                                        .createSequentialGroup()
                                                                        .addGap(10)
                                                                        .addComponent(
                                                                                scrollPane,
                                                                                GroupLayout.DEFAULT_SIZE,
                                                                                1152,
                                                                                Short.MAX_VALUE)))
                                        .addContainerGap()));
        gl_panelContents.setVerticalGroup(gl_panelContents.createParallelGroup(
                Alignment.LEADING).addGroup(
                        gl_panelContents
                                .createSequentialGroup()
                                .addContainerGap()
                                .addComponent(lblContents)
                                .addPreferredGap(ComponentPlacement.RELATED)
                                .addComponent(scrollPane, GroupLayout.DEFAULT_SIZE,
                                        565, Short.MAX_VALUE).addContainerGap()));
        panelContents.setLayout(gl_panelContents);

        JLabel lblDocumentTitle = new JLabel(LocalizationUtils.getGeneric("docName"));
        lblDocumentTitle.setFont(new Font("Lucida Grande", Font.BOLD, 13));

        JLabel lblVerbalization = new JLabel(LocalizationUtils.getLocalizedString(this.getClass(), "lblVerbalization") + ":");
        lblVerbalization.setFont(new Font("Lucida Grande", Font.BOLD, 13));

        JLabel lblDocumentTitleDescription = new JLabel("");
        lblDocumentTitleDescription.setFont(new Font("SansSerif", Font.PLAIN,
                13));
        if (verbalization != null && verbalization.getReferredDoc() != null) {
            lblDocumentTitleDescription.setText(verbalization.getReferredDoc()
                    .getDescription());
        }

        JLabel lblVerbalizationDescription = new JLabel("");
        lblVerbalizationDescription.setFont(new Font("SansSerif", Font.PLAIN,
                13));
        if (verbalization != null) {
            String authors = "";
            authors = verbalization.getAuthors().stream().map((author) -> author + ", ").reduce(authors, String::concat);
            authors = authors.substring(0, authors.length() - 2);
            lblVerbalizationDescription.setText(authors + " ("
                    + (new File(verbalization.getPath()).getName()) + ")");
        }

        JButton btnViewDocument = new JButton(LocalizationUtils.getLocalizedString(this.getClass(), "btnViewDocument"));
        btnViewDocument.addActionListener((ActionEvent e) -> {
            EventQueue.invokeLater(() -> {
                if (verbalization.getReferredDoc() != null) {
                    DocumentView view = new DocumentView(verbalization
                            .getReferredDoc());
                    view.setVisible(true);
                }
            });
        });
        GroupLayout gl_panelHeader = new GroupLayout(panelHeader);
        gl_panelHeader
                .setHorizontalGroup(gl_panelHeader
                        .createParallelGroup(Alignment.TRAILING)
                        .addGroup(
                                gl_panelHeader
                                        .createSequentialGroup()
                                        .addContainerGap()
                                        .addGroup(
                                                gl_panelHeader
                                                        .createParallelGroup(
                                                                Alignment.LEADING)
                                                        .addGroup(
                                                                gl_panelHeader
                                                                        .createSequentialGroup()
                                                                        .addComponent(
                                                                                lblVerbalization)
                                                                        .addPreferredGap(
                                                                                ComponentPlacement.RELATED)
                                                                        .addComponent(
                                                                                lblVerbalizationDescription,
                                                                                GroupLayout.DEFAULT_SIZE,
                                                                                941,
                                                                                Short.MAX_VALUE)
                                                                        .addGap(122))
                                                        .addGroup(
                                                                Alignment.TRAILING,
                                                                gl_panelHeader
                                                                        .createSequentialGroup()
                                                                        .addComponent(
                                                                                lblDocumentTitle)
                                                                        .addPreferredGap(
                                                                                ComponentPlacement.RELATED)
                                                                        .addComponent(
                                                                                lblDocumentTitleDescription,
                                                                                GroupLayout.DEFAULT_SIZE,
                                                                                932,
                                                                                Short.MAX_VALUE)
                                                                        .addPreferredGap(
                                                                                ComponentPlacement.RELATED)
                                                                        .addComponent(
                                                                                btnViewDocument)))));
        gl_panelHeader
                .setVerticalGroup(gl_panelHeader
                        .createParallelGroup(Alignment.LEADING)
                        .addGroup(
                                gl_panelHeader
                                        .createSequentialGroup()
                                        .addContainerGap()
                                        .addGroup(
                                                gl_panelHeader
                                                        .createParallelGroup(
                                                                Alignment.BASELINE)
                                                        .addComponent(
                                                                lblDocumentTitle)
                                                        .addComponent(
                                                                lblDocumentTitleDescription)
                                                        .addComponent(
                                                                btnViewDocument))
                                        .addPreferredGap(
                                                ComponentPlacement.RELATED)
                                        .addGroup(
                                                gl_panelHeader
                                                        .createParallelGroup(
                                                                Alignment.BASELINE)
                                                        .addComponent(
                                                                lblVerbalization)
                                                        .addComponent(
                                                                lblVerbalizationDescription,
                                                                GroupLayout.PREFERRED_SIZE,
                                                                17,
                                                                GroupLayout.PREFERRED_SIZE))
                                        .addContainerGap(29, Short.MAX_VALUE)));
        panelHeader.setLayout(gl_panelHeader);
        super.getContentPane().setLayout(groupLayout);

        // adjust view to desktop size
        super.setBounds(50, 50, 1180, 700);

        modelContents = new VerbalizationTableModel();
        determineRowType();
        tableContents = new VerbalizationTable(modelContents, isVerbalisation);
        tableContents.setFillsViewportHeight(true);
        tableContents.getColumnModel().getColumn(0).setPreferredWidth(600);

        scrollPane.setViewportView(tableContents);

        updateContent();
    }

    private void determineRowType() {
        // entire document + verbalization + overall row
        isVerbalisation = new boolean[verbalization.getBlocks().size()
                + verbalization.getReferredDoc().getBlocks().size() + 1];
        for (int i = 0; i < isVerbalisation.length; i++) {
            isVerbalisation[i] = false;
        }
        verbalization.getBlocks().stream().forEach((v) -> {
            isVerbalisation[v.getIndex() + v.getRefBlock().getIndex() + 1] = true;
        });
    }

    private void updateContent() {
        if (verbalization != null && verbalization.getReferredDoc() != null) {
            // clean table
            while (modelContents.getRowCount() > 0) {
                modelContents.removeRow(0);
            }

            int startIndex = 0;
            int endIndex;
            for (int index = 0; index < verbalization.getBlocks().size(); index++) {
                endIndex = verbalization.getBlocks().get(index).getRefBlock()
                        .getIndex();
                for (int refBlockId = startIndex; refBlockId <= endIndex; refBlockId++) {
                    // add rows as blocks within the document
                    List<Object> dataRow = new ArrayList<>();

                    SemanticCohesion coh = verbalization.getBlockSimilarities()[refBlockId];
                    // add block text
                    String text = "";
                    for (Sentence s : verbalization.getReferredDoc()
                            .getBlocks().get(refBlockId).getSentences()) {
                        text += s.getAlternateText() + " ";
                    }
                    dataRow.add(text);

                    for (ReadingStrategyType rs : ReadingStrategyType.values()) {
                        dataRow.add("");
                    }

                    // add cohesion
                    dataRow.add(Formatting.formatNumber(coh.getCohesion())
                            .toString());
                    modelContents.addRow(dataRow.toArray());
                }
                startIndex = endIndex + 1;

                // add corresponding verbalization
                List<Object> dataRow = new ArrayList<>();

                dataRow.add(verbalization.getBlocks().get(index)
                        .getAlternateText().trim());

                for (ReadingStrategyType rs : ReadingStrategyType.values()) {
                    dataRow.add(verbalization.getAutomatedRS().get(index).get(rs));
                }
                dataRow.add("");
                modelContents.addRow(dataRow.toArray());
            }

            // add final row
            List<Object> dataRow = new ArrayList<>();
            dataRow.add(LocalizationUtils.getLocalizedString(this.getClass(), "dataRowOverall"));

            EnumMap<ReadingStrategyType, Integer> allRS = verbalization.getAllRS(verbalization.getAutomatedRS());
            for (ReadingStrategyType rs : ReadingStrategyType.values()) {
                allRS.get(rs);
            }
            dataRow.add("");
            modelContents.addRow(dataRow.toArray());
        }
    }
}