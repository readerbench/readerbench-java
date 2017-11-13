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
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.logging.Logger;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSlider;
import javax.swing.JTable;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.DefaultTableModel;

import data.Block;
import data.Sentence;
import data.discourse.Keyword;
import data.document.Document;
import services.commons.Formatting;
import services.complexity.rhythm.views.AlliterationDocumentView;
import services.complexity.rhythm.views.AssonanceDocumentView;
//import services.complexity.rhythm.views.DocumentRhythmView;
import services.complexity.rhythm.views.DocumentRhythmView2;
import services.discourse.keywordMining.KeywordModeling;
import utils.LocalizationUtils;
import view.events.LinkMouseListener;
import view.models.document.DocumentTable;
import view.models.document.DocumentTableModel;
import view.models.document.TopicsTableModel;

/**
 *
 * @author Mihai Dascalu
 */
public class DocumentView extends JFrame {

    static Logger logger = Logger.getLogger("");

    private static final long serialVersionUID = -4709511294166379162L;
    private static final int MIN_ROW_HEIGHT = 20;
    private static final int MAX_ROW_HEIGHT = 60;

    private Document document;
    private JTable tableTopics;
    private JSlider sliderTopics;
    private JCheckBox chckbxNounTopics;
    private JCheckBox chckbxVerbTopics;
    private DefaultTableModel modelTopics;
    private DefaultTableModel modelContent;
    private JTable tableContent;
    private JLabel lblSourceDescription;
    private JLabel lblURIDescription;
    private JLabel lblTitleDescription;
    private JLabel lblSubjectivityDescription;

    public DocumentView(Document documentToDisplay) {
        super.setTitle("ReaderBench - " + LocalizationUtils.getTitle(this.getClass()));
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        getContentPane().setBackground(Color.WHITE);
        this.document = documentToDisplay;

        // adjust view to desktop size
        setBounds(50, 50, 1180, 710);

        generateLayout();
        updateContent();
        updateTopics();
    }

    private void generateLayout() {
        JPanel panelConcepts = new JPanel();
        panelConcepts.setBackground(Color.WHITE);

        JPanel panelHeader = new JPanel();
        panelHeader.setBackground(Color.WHITE);

        JPanel panelContents = new JPanel();
        panelContents.setBackground(Color.WHITE);
        GroupLayout groupLayout = new GroupLayout(getContentPane());
        groupLayout
                .setHorizontalGroup(groupLayout.createParallelGroup(Alignment.LEADING)
                        .addGroup(groupLayout.createSequentialGroup().addContainerGap()
                                .addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
                                        .addComponent(panelHeader, GroupLayout.DEFAULT_SIZE, 1168, Short.MAX_VALUE)
                                        .addGroup(Alignment.TRAILING, groupLayout.createSequentialGroup()
                                                .addComponent(panelContents, GroupLayout.DEFAULT_SIZE, 868,
                                                        Short.MAX_VALUE)
                                                .addPreferredGap(ComponentPlacement.RELATED).addComponent(panelConcepts,
                                                GroupLayout.PREFERRED_SIZE, 294, GroupLayout.PREFERRED_SIZE)))
                                .addContainerGap()));
        groupLayout
                .setVerticalGroup(
                        groupLayout.createParallelGroup(Alignment.LEADING)
                                .addGroup(groupLayout.createSequentialGroup().addContainerGap()
                                        .addComponent(panelHeader, GroupLayout.PREFERRED_SIZE, 53,
                                                GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(ComponentPlacement.RELATED)
                                        .addGroup(groupLayout.createParallelGroup(Alignment.TRAILING)
                                                .addComponent(panelContents, GroupLayout.DEFAULT_SIZE, 617,
                                                        Short.MAX_VALUE)
                                                .addComponent(panelConcepts, GroupLayout.DEFAULT_SIZE, 607, Short.MAX_VALUE))
                                        .addContainerGap()));

        JLabel lblContents = new JLabel(LocalizationUtils.getLocalizedString(this.getClass(), "lblContents"));
        lblContents.setFont(new Font("SansSerif", Font.BOLD, 13));

        JSeparator separator = new JSeparator();

        JScrollPane scrollPaneContent = new JScrollPane();
        scrollPaneContent.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        JButton btnAdvancedView = new JButton("Advanced View");
        btnAdvancedView.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                EventQueue.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        SentenceView view = new SentenceView(document);
                        view.setVisible(true);
                    }
                });
            }
        });

        JButton btnVisualizeCohesionGraph = new JButton(LocalizationUtils.getLocalizedString(this.getClass(), "btnVisualizeCohesionGraph"));
        btnVisualizeCohesionGraph.addActionListener((ActionEvent arg0) -> {
            EventQueue.invokeLater(new Runnable() {
                @Override
                public void run() {
                    CohesionGraphView view = new CohesionGraphView(document);
                    view.setVisible(true);
                }
            });
        });

        JButton btnSelectVoices = new JButton(LocalizationUtils.getLocalizedString(this.getClass(), "btnSelectVoices"));
        btnSelectVoices.addActionListener((ActionEvent e) -> {
            EventQueue.invokeLater(new Runnable() {
                @Override
                public void run() {
                    JFrame frame = new VoiceSelectionView(document);
                    frame.setVisible(true);
                }
            });
        });

        JButton btnDisplayVoiceInteranimation = new JButton(LocalizationUtils.getLocalizedString(this.getClass(), "btnDisplayVoiceInteranimation"));
        btnDisplayVoiceInteranimation.addActionListener((ActionEvent e) -> {
            if (document.getSelectedVoices() != null && document.getSelectedVoices().size() > 0) {
                EventQueue.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        JFrame frame = new SentenceLevelInterAnimationView(document, document.getSelectedVoices());
                        frame.setVisible(true);
                    }
                });
            } else {
                JOptionPane.showMessageDialog(DocumentView.this, LocalizationUtils.getLocalizedString(this.getClass(), "msgSelectVoice"),
                        LocalizationUtils.getLocalizedString(this.getClass(), "msgInformation"), JOptionPane.INFORMATION_MESSAGE);
            }
        });

        JButton btnVisualizeDocumentFlow = new JButton(LocalizationUtils.getLocalizedString(this.getClass(), "btnVisualizeDocumentFlow"));
        btnVisualizeDocumentFlow.addActionListener((ActionEvent arg0) -> {
            EventQueue.invokeLater(() -> {
                JFrame view = new DocumentFlowView(document);
                view.setVisible(true);
            });
        });

        JButton btnVisualizeAlliterations = new JButton("Alliterations");
        btnVisualizeAlliterations.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                EventQueue.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        JFrame view = new AlliterationDocumentView(document);
                        view.setVisible(true);
                    }
                });
            }
        });

        JButton btnVisualizeAssonances = new JButton("Assonances");
        btnVisualizeAssonances.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                EventQueue.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        JFrame view = new AssonanceDocumentView(document);
                        view.setVisible(true);
                    }
                });
            }
        });

        JButton btnRhythmFeatures = new JButton("Rhythm");
        btnRhythmFeatures.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                EventQueue.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        JFrame view = new DocumentRhythmView2(document);
                        view.setVisible(true);
                    }
                });
            }
        });
        
        GroupLayout gl_panelContents = new GroupLayout(panelContents);
        gl_panelContents
                .setHorizontalGroup(gl_panelContents.createParallelGroup(Alignment.TRAILING)
                        .addGroup(gl_panelContents.createSequentialGroup().addContainerGap()
                                .addGroup(gl_panelContents.createParallelGroup(Alignment.LEADING)
                                        .addComponent(lblContents)
                                        .addComponent(scrollPaneContent, GroupLayout.DEFAULT_SIZE, 856, Short.MAX_VALUE)
                                        .addComponent(separator, GroupLayout.DEFAULT_SIZE, 856,
                                                Short.MAX_VALUE)
                                        .addGroup(gl_panelContents.createParallelGroup(Alignment.TRAILING, false)
                                                .addGroup(gl_panelContents.createSequentialGroup()
                                                        .addComponent(btnAdvancedView ,GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                        .addPreferredGap(ComponentPlacement.RELATED)
                                                        .addComponent(btnVisualizeCohesionGraph, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                        .addPreferredGap(ComponentPlacement.RELATED)
                                                        .addComponent(btnVisualizeDocumentFlow, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                        .addPreferredGap(ComponentPlacement.RELATED)
                                                        .addComponent(btnSelectVoices, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                        .addPreferredGap(ComponentPlacement.RELATED)
                                                        .addComponent(btnDisplayVoiceInteranimation, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                                        .addGroup(gl_panelContents.createSequentialGroup()
                                                .addComponent(btnVisualizeAlliterations).addPreferredGap(ComponentPlacement.RELATED)
                                                .addComponent(btnVisualizeAssonances).addPreferredGap(ComponentPlacement.RELATED)
                                                .addComponent(btnRhythmFeatures)))
                                .addContainerGap()));
        gl_panelContents
                .setVerticalGroup(
                        gl_panelContents.createParallelGroup(Alignment.LEADING)
                                .addGroup(gl_panelContents.createSequentialGroup().addContainerGap()
                                        .addComponent(lblContents).addPreferredGap(ComponentPlacement.RELATED)
                                        .addComponent(separator, GroupLayout.PREFERRED_SIZE, 2,
                                                GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(ComponentPlacement.RELATED)
                                        .addComponent(scrollPaneContent, GroupLayout.DEFAULT_SIZE, 530, Short.MAX_VALUE)
                                        .addPreferredGap(ComponentPlacement.RELATED)
                                        .addGroup(gl_panelContents.createParallelGroup(Alignment.BASELINE)
                                                .addComponent(btnAdvancedView)
                                                .addComponent(btnVisualizeCohesionGraph)
                                                .addComponent(btnSelectVoices)
                                                .addComponent(btnDisplayVoiceInteranimation)
                                                .addComponent(btnVisualizeDocumentFlow))
                                        .addPreferredGap(ComponentPlacement.RELATED)
                                        .addGroup(gl_panelContents.createParallelGroup(Alignment.BASELINE)
                                                .addComponent(btnVisualizeAlliterations)
                                                .addComponent(btnVisualizeAssonances)
                                                .addComponent(btnRhythmFeatures))
                                        .addContainerGap()));
        panelContents.setLayout(gl_panelContents);
        JLabel lblTitle = new JLabel(LocalizationUtils.getGeneric("title") + ":\n");
        lblTitle.setFont(new Font("Lucida Grande", Font.BOLD, 13));
        JLabel lblSource = new JLabel(LocalizationUtils.getGeneric("source") + ":");
        JLabel lblURI = new JLabel(LocalizationUtils.getGeneric("URI") + ":");
        JLabel lblSubj = new JLabel(LocalizationUtils.getLocalizedString(this.getClass(), "lblSubj") + ":");

        lblURIDescription = new JLabel("");
        lblSourceDescription = new JLabel("");
        lblTitleDescription = new JLabel("");
        lblTitleDescription.setFont(new Font("Lucida Grande", Font.BOLD, 13));
        lblSubjectivityDescription = new JLabel("");

        JSeparator separatorDocument = new JSeparator();

        if (document.getTitleText() != null) {
            lblTitleDescription.setText(document.getTitleText());
        }
        if (document.getSource() != null) {
            lblSourceDescription.setText(document.getSource());
        }
        if (document.getURI() != null) {
            lblURIDescription.setText(document.getURI());
            lblURIDescription.addMouseListener(new LinkMouseListener());
        }

        GroupLayout gl_panelHeader = new GroupLayout(panelHeader);
        gl_panelHeader
                .setHorizontalGroup(
                        gl_panelHeader.createParallelGroup(Alignment.LEADING)
                                .addGroup(
                                        gl_panelHeader.createSequentialGroup().addContainerGap()
                                                .addGroup(gl_panelHeader
                                                        .createParallelGroup(
                                                                Alignment.LEADING)
                                                        .addComponent(separatorDocument, GroupLayout.DEFAULT_SIZE, 1156,
                                                                Short.MAX_VALUE)
                                                        .addGroup(gl_panelHeader.createSequentialGroup()
                                                                .addComponent(lblSource)
                                                                .addPreferredGap(ComponentPlacement.RELATED)
                                                                .addComponent(lblSourceDescription).addGap(18)
                                                                .addComponent(lblURI)
                                                                .addPreferredGap(ComponentPlacement.RELATED)
                                                                .addComponent(lblURIDescription)
                                                                .addPreferredGap(ComponentPlacement.UNRELATED)
                                                                .addComponent(lblSubj)
                                                                .addPreferredGap(ComponentPlacement.RELATED)
                                                                .addComponent(lblSubjectivityDescription))
                                                        .addGroup(gl_panelHeader.createSequentialGroup().addComponent(lblTitle)
                                                                .addPreferredGap(ComponentPlacement.RELATED).addComponent(lblTitleDescription,
                                                                        GroupLayout.DEFAULT_SIZE, 1113, Short.MAX_VALUE))).addContainerGap()));
        gl_panelHeader
                .setVerticalGroup(
                        gl_panelHeader
                                .createParallelGroup(
                                        Alignment.LEADING)
                                .addGroup(
                                        gl_panelHeader.createSequentialGroup().addContainerGap()
                                                .addGroup(gl_panelHeader.createParallelGroup(Alignment.BASELINE)
                                                        .addComponent(lblTitle).addComponent(
                                                                lblTitleDescription))
                                                .addPreferredGap(ComponentPlacement.RELATED)
                                                .addGroup(gl_panelHeader.createParallelGroup(Alignment.BASELINE)
                                                        .addComponent(lblSource).addComponent(lblSourceDescription)
                                                        .addComponent(lblURIDescription).addComponent(lblURI)
                                                        .addComponent(lblSubj).addComponent(lblSubjectivityDescription))
                                                .addPreferredGap(ComponentPlacement.RELATED)
                                                .addComponent(separatorDocument, GroupLayout.PREFERRED_SIZE, 2, GroupLayout.PREFERRED_SIZE)
                                                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));
        panelHeader.setLayout(gl_panelHeader);

        JLabel lblTopics = new JLabel(LocalizationUtils.getGeneric("topics"));
        lblTopics.setFont(new Font("SansSerif", Font.BOLD, 12));

        JSeparator separatorTopics = new JSeparator();

        JLabel lblFilterOnly = new JLabel(LocalizationUtils.getLocalizedString(this.getClass(), "lblFilterOnly") + ":");

        chckbxVerbTopics = new JCheckBox(LocalizationUtils.getGeneric("verbs"));
        chckbxVerbTopics.setBackground(Color.WHITE);
        chckbxVerbTopics.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                updateTopics();
            }
        });
        chckbxVerbTopics.setSelected(true);

        chckbxNounTopics = new JCheckBox(LocalizationUtils.getGeneric("nouns"));
        chckbxNounTopics.setBackground(Color.WHITE);
        chckbxNounTopics.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                updateTopics();
            }
        });
        chckbxNounTopics.setSelected(true);

        // determine appropriate scale
        int noWords = (int) (document.getWordOccurences().keySet().size() * 0.2);
        int noMaxTopics = 50;
        if (noWords > 50) {
            if (noWords <= 75) {
                noMaxTopics = 75;
            } else {
                noMaxTopics = 100;
            }
        }
        sliderTopics = new JSlider(0, noMaxTopics / 5, 5);
        sliderTopics.setBackground(Color.WHITE);
        sliderTopics.setFont(new Font("SansSerif", Font.PLAIN, 10));
        sliderTopics.setMajorTickSpacing(5);
        sliderTopics.setPaintLabels(true);
        sliderTopics.setMinorTickSpacing(1);
        java.util.Hashtable<Integer, JLabel> labelTable = new java.util.Hashtable<>();
        if (noMaxTopics == 20) {
            labelTable.put(20, new JLabel("100"));
        }
        if (noMaxTopics >= 15) {
            labelTable.put(15, new JLabel("75"));
        }
        labelTable.put(10, new JLabel("50"));
        labelTable.put(5, new JLabel("25"));
        labelTable.put(0, new JLabel("0"));
        sliderTopics.setLabelTable(labelTable);
        sliderTopics.addChangeListener((ChangeEvent e) -> {
            updateTopics();
        });

        JButton btnGenerateNetwork = new JButton(LocalizationUtils.getLocalizedString(this.getClass(), "btnGenerateNetwork"));
        btnGenerateNetwork.addActionListener((ActionEvent arg0) -> {
            EventQueue.invokeLater(() -> {
                ConceptView view = new ConceptView(null, document,
                        KeywordModeling.getSublist(document.getTopics(), sliderTopics.getValue() * 5,
                                chckbxNounTopics.isSelected(), chckbxVerbTopics.isSelected()));
                view.setVisible(true);
            });
        });

        JScrollPane scrollPaneTopics = new JScrollPane();
        scrollPaneTopics.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        GroupLayout gl_panelConcepts = new GroupLayout(panelConcepts);
        gl_panelConcepts.setHorizontalGroup(gl_panelConcepts.createParallelGroup(Alignment.LEADING)
                .addGroup(Alignment.TRAILING, gl_panelConcepts.createSequentialGroup().addContainerGap()
                        .addGroup(gl_panelConcepts.createParallelGroup(Alignment.TRAILING)
                                .addComponent(scrollPaneTopics, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 282,
                                        Short.MAX_VALUE)
                                .addComponent(btnGenerateNetwork, Alignment.LEADING)
                                .addComponent(lblTopics,
                                        Alignment.LEADING)
                                .addGroup(Alignment.LEADING, gl_panelConcepts.createSequentialGroup().addGap(6)
                                        .addGroup(gl_panelConcepts.createParallelGroup(Alignment.LEADING)
                                                .addGroup(gl_panelConcepts.createSequentialGroup().addComponent(lblFilterOnly)
                                                        .addPreferredGap(ComponentPlacement.RELATED)
                                                        .addComponent(separatorTopics, GroupLayout.DEFAULT_SIZE, 203,
                                                                Short.MAX_VALUE))
                                                .addGroup(gl_panelConcepts.createSequentialGroup()
                                                        .addGroup(gl_panelConcepts.createParallelGroup(Alignment.LEADING)
                                                                .addComponent(chckbxNounTopics, GroupLayout.PREFERRED_SIZE, 105,
                                                                        GroupLayout.PREFERRED_SIZE)
                                                                .addComponent(chckbxVerbTopics))
                                                        .addPreferredGap(ComponentPlacement.RELATED).addComponent(sliderTopics,
                                                        GroupLayout.DEFAULT_SIZE, 165, Short.MAX_VALUE)))))
                        .addContainerGap()));
        gl_panelConcepts.setVerticalGroup(gl_panelConcepts.createParallelGroup(Alignment.LEADING)
                .addGroup(gl_panelConcepts.createSequentialGroup().addContainerGap().addComponent(lblTopics)
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addGroup(gl_panelConcepts.createParallelGroup(Alignment.LEADING)
                                .addComponent(separatorTopics, GroupLayout.PREFERRED_SIZE, 2,
                                        GroupLayout.PREFERRED_SIZE)
                                .addComponent(lblFilterOnly))
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addGroup(gl_panelConcepts.createParallelGroup(Alignment.LEADING, false)
                                .addGroup(gl_panelConcepts.createSequentialGroup().addComponent(chckbxNounTopics)
                                        .addPreferredGap(ComponentPlacement.RELATED).addComponent(chckbxVerbTopics))
                                .addComponent(sliderTopics, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE,
                                        Short.MAX_VALUE))
                        .addPreferredGap(ComponentPlacement.UNRELATED)
                        .addComponent(scrollPaneTopics, GroupLayout.DEFAULT_SIZE, 453, Short.MAX_VALUE)
                        .addPreferredGap(ComponentPlacement.RELATED).addComponent(btnGenerateNetwork)
                        .addContainerGap()));
        modelTopics = new TopicsTableModel();

        tableTopics = new JTable(modelTopics);
        scrollPaneTopics.setViewportView(tableTopics);
        tableTopics.setFillsViewportHeight(true);

        panelConcepts.setLayout(gl_panelConcepts);
        getContentPane().setLayout(groupLayout);

        modelContent = new DocumentTableModel();

        tableContent = new DocumentTable(modelContent);

        tableContent.getColumnModel().getColumn(0).setMinWidth(50);
        tableContent.getColumnModel().getColumn(0).setMaxWidth(50);
        tableContent.getColumnModel().getColumn(0).setPreferredWidth(50);

        tableContent.setFillsViewportHeight(true);
        tableContent.setTableHeader(null);

        scrollPaneContent.setViewportView(tableContent);
    }

    private void updateTopics() {
        // clean table
        while (modelTopics.getRowCount() > 0) {
            modelTopics.removeRow(0);
        }

        // add new topics
        List<Keyword> topTopics = KeywordModeling.getSublist(document.getTopics(), sliderTopics.getValue() * 5,
                chckbxNounTopics.isSelected(), chckbxVerbTopics.isSelected());
        for (Keyword topic : topTopics) {
            Object[] row = {topic.getWord().getLemma(), Formatting.formatNumber(topic.getRelevance())};
            modelTopics.addRow(row);
        }
    }

    private void updateContent() {
        // clean table
        while (modelContent.getRowCount() > 0) {
            modelContent.removeRow(0);
        }

        double s0 = 0, s1 = 0, s2 = 0, mean = 0, stdev = 0;

        for (Block b : document.getBlocks()) {
            if (b != null) {
                for (Sentence s : b.getSentences()) {
                    if (s != null) {
                        s0++;
                        s1 += s.getScore();
                        s2 += Math.pow(s.getScore(), 2);
                    }
                }
            }
        }

        // determine mean + stdev values
        if (s0 != 0) {
            mean = s1 / s0;
            stdev = Math.sqrt(s0 * s2 - Math.pow(s1, 2)) / s0;
        }

        if (document.getBlocks() != null && document.getBlocks().size() > 0) {
            // add content
            int index = 0;
            double maxCohesion = Double.MIN_VALUE;
            double minCohesion = Double.MAX_VALUE;

            for (; index < document.getBlocks().size() - 1; index++) {
                if (document.getBlocks().get(index) != null) {
                    String text = "";
                    for (Sentence s : document.getBlocks().get(index).getSentences()) {
                        if (s != null) {
                            if (s.getScore() >= mean + stdev) {
                                text += "<b>" + s.getText() + "</b> ";
                            } else {
                                text += s.getText() + " ";
                            }
                        }
                    }
                    Object[] row1 = {index, text + "["
                        + Formatting.formatNumber(document.getBlocks().get(index).getScore()) + "]"};
                    modelContent.addRow(row1);
                    Object[] row2 = {"", document.getBlockDistances()[index][index + 1].toString()};
                    modelContent.addRow(row2);
                    double dist = document.getBlockDistances()[index][index + 1].getCohesion();
                    maxCohesion = Math.max(maxCohesion, dist);
                    minCohesion = Math.min(minCohesion, dist);
                }
            }
            if (document.getBlocks().get(index) != null) {
                Object[] lastRow = {index, document.getBlocks().get(index).getText() + " ["
                    + Formatting.formatNumber(document.getBlocks().get(index).getScore()) + "]"};
                modelContent.addRow(lastRow);
            }

            if (document.getBlocks().size() > 1) {
                for (index = 0; index < document.getBlocks().size() - 1; index++) {
                    double dist = 1 / document.getBlockDistances()[index][index + 1].getCohesion();
                    tableContent.setRowHeight(2 * index + 1, MIN_ROW_HEIGHT + ((int) ((dist - 1 / maxCohesion)
                            / (1 / minCohesion - 1 / maxCohesion) * (MAX_ROW_HEIGHT - MIN_ROW_HEIGHT))));
                }
            }
        }
    }
}
