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

import data.AbstractDocument;
import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
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
import java.util.logging.Logger;

import data.Block;
import data.cscl.Conversation;
import data.cscl.Participant;
import data.cscl.Utterance;
import data.discourse.Keyword;
import gma.ProblemSpaceView;
import java.io.IOException;
import java.util.LinkedList;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import org.apache.log4j.BasicConfigurator;
import org.openide.util.Exceptions;
import services.commons.Formatting;
import services.discourse.keywordMining.KeywordModeling;
import view.models.document.ChatTable;
import view.models.document.DocumentTableModel;
import view.models.document.TopicsTableModel;
import view.widgets.document.ConceptView;
import view.widgets.document.VoiceSelectionView;

/**
 *
 * @author Mihai Dascalu
 */
public class ChatView extends JFrame {

    static Logger logger = Logger.getLogger("");
    private static final long serialVersionUID = -4709511294166379162L;
    private static final int MAX_LENGTH_TITLE = 100;

    private final Conversation chat;
    private JLabel lblChatTitle;
    private JTable tableTopics;
    private JSlider sliderTopics;
    private JCheckBox chckbxNounTopics;
    private JCheckBox chckbxVerbTopics;
    private DefaultTableModel modelTopics;
    private DefaultTableModel modelContent;
    private JTable tableContent;
    private JComboBox<String> comboBoxCategory;
    private List<Keyword> topTopics;

    public ChatView(Conversation documentToDisplay) {
        super();
        super.setTitle("ReaderBench - Chat Visualization");
        super.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        super.getContentPane().setBackground(Color.WHITE);
        this.chat = documentToDisplay;

        // adjust view to desktop size
        setBounds(50, 50, 1180, 700);

        generateLayout();
        updateContent();
        updateTopics(0);
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
                                .addGroup(groupLayout.createParallelGroup(Alignment.TRAILING)
                                        .addComponent(panelHeader, GroupLayout.DEFAULT_SIZE, 1168, Short.MAX_VALUE)
                                        .addGroup(groupLayout.createSequentialGroup()
                                                .addComponent(panelContents, GroupLayout.DEFAULT_SIZE, 916,
                                                        Short.MAX_VALUE)
                                                .addPreferredGap(ComponentPlacement.RELATED).addComponent(panelConcepts,
                                                GroupLayout.PREFERRED_SIZE, 246, GroupLayout.PREFERRED_SIZE)))
                                .addContainerGap()));
        groupLayout.setVerticalGroup(groupLayout.createParallelGroup(Alignment.LEADING)
                .addGroup(groupLayout.createSequentialGroup().addContainerGap()
                        .addComponent(panelHeader, GroupLayout.PREFERRED_SIZE, 32, GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addGroup(groupLayout.createParallelGroup(Alignment.TRAILING)
                                .addComponent(panelConcepts, 0, 0, Short.MAX_VALUE).addComponent(panelContents,
                                GroupLayout.DEFAULT_SIZE, 633, Short.MAX_VALUE))
                        .addGap(1)));

        JLabel lblContents = new JLabel("Contents");
        lblContents.setFont(new Font("SansSerif", Font.BOLD, 12));

        JSeparator separator = new JSeparator();

        JScrollPane scrollPaneConcept = new JScrollPane();
        scrollPaneConcept.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        JButton btnCorrelatedConcepts = new JButton(
                "    View correlated concepts    ");
        btnCorrelatedConcepts.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                ArrayList<Keyword> selectedTopics = getSelectedTopics();
                if (selectedTopics.size() > 0) {
                    JFrame view;
                    int dialogResult = JOptionPane
                            .showConfirmDialog(
                                    null,
                                    "Would you like "
                                    + "to use LDA and LSA for searching similar concepts? This could take a while.",
                                    "Warning", JOptionPane.YES_NO_OPTION);
                    if (dialogResult == JOptionPane.YES_OPTION) {
                        view = new ProblemSpaceView(chat, selectedTopics, true);
                    } else {
                        view = new ProblemSpaceView(chat, selectedTopics, false);
                    }
                    view.setVisible(true);
                } else {
                    JOptionPane.showMessageDialog(null,
                            "You must select at least one topic.", "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        JButton btnParticipantInvolvement = new JButton("Participant interaction");
        btnParticipantInvolvement.addActionListener((ActionEvent arg0) -> {
            EventQueue.invokeLater(() -> {
                Iterator<Participant> it = chat.getParticipants().iterator();
                List<Participant> lsPart = new ArrayList<Participant>();
                while (it.hasNext()) {
                    Participant part = it.next();
                    lsPart.add(part);
                }

                ParticipantInteractionView view = new ParticipantInteractionView("out/graph.pdf", lsPart,
                        chat.getParticipantContributions(), true, false);
                view.setVisible(true);
            });
        });

        JButton btnCollaborationSocialKB = new JButton("Collaboration - Social KB");
        btnCollaborationSocialKB.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                EventQueue.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        JFrame view = new CollaborationKBView(chat);
                        view.setVisible(true);
                    }
                });
            }
        });

        JButton btnTimeEvolution = new JButton("Time evolution");
        btnTimeEvolution.addActionListener((ActionEvent arg0) -> {
            EventQueue.invokeLater(() -> {
                JFrame frame = new ChatTimeEvolution(chat);
                frame.setVisible(true);
            });
        });

        JButton btnSelectVoices = new JButton("Select voices");
        btnSelectVoices.addActionListener((ActionEvent e) -> {
            EventQueue.invokeLater(() -> {
                JFrame frame = new VoiceSelectionView(chat);
                frame.setVisible(true);
            });
        });

        JButton btnDisplayVoiceInteranimation = new JButton("Display voice inter-animation");
        btnDisplayVoiceInteranimation.addActionListener((ActionEvent e) -> {
            if (chat.getSelectedVoices() != null && chat.getSelectedVoices().size() > 0) {
                EventQueue.invokeLater(() -> {
                    JFrame frame = new ChatVoiceInterAnimationView(chat, chat.getSelectedVoices());
                    frame.setVisible(true);
                });
            } else {
                JOptionPane.showMessageDialog(ChatView.this, "At least one voice must be selected!", "Information",
                        JOptionPane.INFORMATION_MESSAGE);
            }
        });

        JButton btnParticipantEvolution = new JButton("Participant evolution");
        btnParticipantEvolution.addActionListener((ActionEvent e) -> {
            EventQueue.invokeLater(new Runnable() {
                @Override
                public void run() {
                    JFrame frame = new ParticipantEvolutionView(chat);
                    frame.setVisible(true);
                }
            });
        });

        JButton btnCollaborationVoice = new JButton("Collaboration - Voice Overlapping");
        btnCollaborationVoice.addActionListener((ActionEvent arg0) -> {
            EventQueue.invokeLater(() -> {
                JFrame view = new CollaborationVoiceView(chat);
                view.setVisible(true);
            });
        });

        GroupLayout gl_panelContents = new GroupLayout(panelContents);
        gl_panelContents.setHorizontalGroup(gl_panelContents.createParallelGroup(Alignment.TRAILING)
                .addGroup(gl_panelContents.createSequentialGroup().addContainerGap().addGroup(gl_panelContents
                        .createParallelGroup(Alignment.TRAILING)
                        .addComponent(scrollPaneConcept, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 970,
                                Short.MAX_VALUE)
                        .addComponent(separator, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 970, Short.MAX_VALUE)
                        .addComponent(lblContents, Alignment.LEADING).addGroup(Alignment.LEADING,
                        gl_panelContents.createSequentialGroup()
                                .addGroup(gl_panelContents.createParallelGroup(Alignment.TRAILING, false)
                                        .addComponent(btnParticipantEvolution, GroupLayout.DEFAULT_SIZE,
                                                GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(btnParticipantInvolvement, GroupLayout.DEFAULT_SIZE,
                                                GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addPreferredGap(ComponentPlacement.RELATED)
                                .addComponent(btnTimeEvolution).addPreferredGap(ComponentPlacement.RELATED)
                                .addGroup(gl_panelContents.createParallelGroup(Alignment.LEADING, false)
                                        .addComponent(btnCollaborationVoice, Alignment.TRAILING,
                                                GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE,
                                                Short.MAX_VALUE)
                                        .addComponent(btnCollaborationSocialKB, Alignment.TRAILING,
                                                GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE,
                                                Short.MAX_VALUE))
                                .addPreferredGap(ComponentPlacement.RELATED)
                                .addGroup(gl_panelContents.createParallelGroup(Alignment.LEADING, false)
                                        .addComponent(btnSelectVoices, GroupLayout.DEFAULT_SIZE,
                                                GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(btnDisplayVoiceInteranimation, GroupLayout.DEFAULT_SIZE,
                                                GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                        .addContainerGap()));
        gl_panelContents
                .setVerticalGroup(
                        gl_panelContents.createParallelGroup(Alignment.LEADING)
                                .addGroup(gl_panelContents.createSequentialGroup().addContainerGap()
                                        .addComponent(lblContents).addPreferredGap(ComponentPlacement.RELATED)
                                        .addComponent(separator, GroupLayout.PREFERRED_SIZE, 2,
                                                GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(ComponentPlacement.RELATED)
                                        .addComponent(scrollPaneConcept, GroupLayout.DEFAULT_SIZE, 522, Short.MAX_VALUE)
                                        .addPreferredGap(ComponentPlacement.RELATED)
                                        .addGroup(gl_panelContents.createParallelGroup(Alignment.BASELINE)
                                                .addComponent(btnParticipantInvolvement).addComponent(btnTimeEvolution)
                                                .addComponent(btnCollaborationSocialKB).addComponent(btnSelectVoices))
                                        .addPreferredGap(ComponentPlacement.RELATED)
                                        .addGroup(gl_panelContents.createParallelGroup(Alignment.BASELINE)
                                                .addComponent(btnParticipantEvolution)
                                                .addComponent(btnCollaborationVoice)
                                                .addComponent(btnDisplayVoiceInteranimation))
                                        .addContainerGap()));
        panelContents.setLayout(gl_panelContents);
        JLabel lblTitle = new JLabel("Discussion topic:");
        lblTitle.setFont(new Font("Lucida Grande", Font.BOLD, 13));

        lblChatTitle = new JLabel("");
        lblChatTitle.setFont(new Font("Lucida Grande", Font.BOLD, 13));
        if (this.chat.getTitleText() != null) {
            String title = chat.getTitleText();
            if (title.length() > MAX_LENGTH_TITLE) {
                title = title.substring(0, title.indexOf(" ", MAX_LENGTH_TITLE)) + "...";
            }
            lblChatTitle.setText(title);
        }

        GroupLayout gl_panelHeader = new GroupLayout(panelHeader);
        gl_panelHeader.setHorizontalGroup(gl_panelHeader.createParallelGroup(Alignment.LEADING)
                .addGroup(gl_panelHeader.createSequentialGroup().addContainerGap().addComponent(lblTitle)
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addComponent(lblChatTitle, GroupLayout.DEFAULT_SIZE, 1117, Short.MAX_VALUE)
                        .addContainerGap()));
        gl_panelHeader.setVerticalGroup(gl_panelHeader.createParallelGroup(Alignment.LEADING)
                .addGroup(gl_panelHeader.createSequentialGroup()
                        .addContainerGap().addGroup(gl_panelHeader.createParallelGroup(Alignment.BASELINE)
                                .addComponent(lblTitle).addComponent(lblChatTitle))
                        .addContainerGap(10, Short.MAX_VALUE)));
        panelHeader.setLayout(gl_panelHeader);

        JLabel lblTopics = new JLabel("Topics");
        lblTopics.setFont(new Font("SansSerif", Font.BOLD, 12));

        JSeparator separatorTopics = new JSeparator();

        JLabel lblFilterOnly = new JLabel("Filter only:");

        chckbxVerbTopics = new JCheckBox("Verbs");
        chckbxVerbTopics.setBackground(Color.WHITE);
        chckbxVerbTopics.addActionListener((ActionEvent arg0) -> {
            updateTopics(comboBoxCategory.getSelectedIndex());
        });
        chckbxVerbTopics.setSelected(true);

        chckbxNounTopics = new JCheckBox("Nouns");
        chckbxNounTopics.setBackground(Color.WHITE);
        chckbxNounTopics.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                updateTopics(comboBoxCategory.getSelectedIndex());
            }
        });
        chckbxNounTopics.setSelected(true);

        // determine appropriate scale
        int noWords = (int) (chat.getWordOccurences().keySet().size() * 0.2);
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
            updateTopics(comboBoxCategory.getSelectedIndex());
        });

        JButton btnGenerateNetwork = new JButton("Generate network of concepts");
        btnGenerateNetwork.addActionListener((ActionEvent arg0) -> {
            EventQueue.invokeLater(() -> {
                ArrayList<Participant> ls = extractArrayListfromSet();
                int index = comboBoxCategory.getSelectedIndex();
                ConceptView view = null;
                if (index == 0) {
                    view = new ConceptView(null, chat,
                            KeywordModeling.getSublist(chat.getTopics(), sliderTopics.getValue() * 5,
                                    chckbxNounTopics.isSelected(), chckbxVerbTopics.isSelected()));
                } else {
                    view = new ConceptView(ls.get(index - 1), chat,
                            KeywordModeling.getSublist(chat.getTopics(), sliderTopics.getValue() * 5,
                                    chckbxNounTopics.isSelected(), chckbxVerbTopics.isSelected()));
                }
                view.setVisible(true);
            });
        });

        JScrollPane scrollPaneTopics = new JScrollPane();
        scrollPaneTopics.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        JLabel lblCategory = new JLabel("Category:");

        comboBoxCategory = new JComboBox<>();
        comboBoxCategory.addItem("Entire discussion");
        chat.getParticipants().stream().forEach((p) -> {
            comboBoxCategory.addItem(p.getName());
        });
        comboBoxCategory.addActionListener((ActionEvent e) -> {
            JComboBox<String> cb = (JComboBox<String>) e.getSource();
            int index = cb.getSelectedIndex();
            updateTopics(index);
        });

        GroupLayout gl_panelConcepts = new GroupLayout(panelConcepts);
        gl_panelConcepts.setHorizontalGroup(gl_panelConcepts.createParallelGroup(Alignment.LEADING)
                .addGroup(gl_panelConcepts.createSequentialGroup().addContainerGap().addGroup(gl_panelConcepts
                        .createParallelGroup(Alignment.TRAILING)
                        .addComponent(scrollPaneTopics, GroupLayout.DEFAULT_SIZE, 234, Short.MAX_VALUE)
                        .addComponent(separatorTopics, GroupLayout.DEFAULT_SIZE, 234, Short.MAX_VALUE)
                        .addGroup(gl_panelConcepts.createSequentialGroup().addGroup(gl_panelConcepts
                                .createParallelGroup(Alignment.LEADING)
                                .addComponent(lblCategory, GroupLayout.DEFAULT_SIZE, 104, Short.MAX_VALUE)
                                .addGroup(gl_panelConcepts.createSequentialGroup().addGap(6).addGroup(gl_panelConcepts
                                        .createParallelGroup(Alignment.LEADING)
                                        .addComponent(chckbxVerbTopics, Alignment.TRAILING, GroupLayout.DEFAULT_SIZE,
                                                98, Short.MAX_VALUE)
                                        .addComponent(chckbxNounTopics, GroupLayout.DEFAULT_SIZE, 98, Short.MAX_VALUE)
                                        .addComponent(lblFilterOnly))))
                                .addPreferredGap(ComponentPlacement.RELATED)
                                .addGroup(gl_panelConcepts.createParallelGroup(Alignment.TRAILING)
                                        .addComponent(sliderTopics, GroupLayout.DEFAULT_SIZE, 124, Short.MAX_VALUE)
                                        .addComponent(comboBoxCategory, GroupLayout.PREFERRED_SIZE, 158,
                                                GroupLayout.PREFERRED_SIZE)))
                        .addComponent(lblTopics).addComponent(btnGenerateNetwork))
                .addComponent(lblTopics).addComponent(btnCorrelatedConcepts)));
        gl_panelConcepts
                .setVerticalGroup(gl_panelConcepts.createParallelGroup(Alignment.LEADING)
                        .addGroup(gl_panelConcepts.createSequentialGroup().addContainerGap().addComponent(lblTopics)
                                .addPreferredGap(ComponentPlacement.RELATED)
                                .addComponent(separatorTopics, GroupLayout.PREFERRED_SIZE, 2,
                                        GroupLayout.PREFERRED_SIZE)
                                .addGap(18)
                                .addGroup(gl_panelConcepts.createParallelGroup(Alignment.BASELINE)
                                        .addComponent(lblCategory, GroupLayout.PREFERRED_SIZE, 17,
                                                GroupLayout.PREFERRED_SIZE)
                                        .addComponent(comboBoxCategory, GroupLayout.PREFERRED_SIZE,
                                                GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(ComponentPlacement.RELATED)
                                .addGroup(gl_panelConcepts.createParallelGroup(Alignment.LEADING, false)
                                        .addGroup(gl_panelConcepts.createSequentialGroup().addComponent(lblFilterOnly)
                                                .addPreferredGap(ComponentPlacement.RELATED)
                                                .addComponent(chckbxNounTopics, GroupLayout.PREFERRED_SIZE, 15,
                                                        GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE,
                                                        Short.MAX_VALUE)
                                                .addComponent(chckbxVerbTopics, GroupLayout.PREFERRED_SIZE, 15,
                                                        GroupLayout.PREFERRED_SIZE))
                                        .addComponent(sliderTopics, GroupLayout.PREFERRED_SIZE, 64,
                                                GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(ComponentPlacement.UNRELATED)
                                .addComponent(scrollPaneTopics, GroupLayout.DEFAULT_SIZE, 436, Short.MAX_VALUE)
                                .addPreferredGap(ComponentPlacement.RELATED).addComponent(btnGenerateNetwork)
                                .addPreferredGap(ComponentPlacement.RELATED).addComponent(btnCorrelatedConcepts)
                                .addContainerGap()));
        modelTopics = new TopicsTableModel();

        tableTopics = new JTable(modelTopics);
        scrollPaneTopics.setViewportView(tableTopics);
        tableTopics.setFillsViewportHeight(true);

        panelConcepts.setLayout(gl_panelConcepts);
        getContentPane().setLayout(groupLayout);

        modelContent = new DocumentTableModel();

        tableContent = new ChatTable(modelContent);

        tableContent.getColumnModel().getColumn(0).setMinWidth(50);
        tableContent.getColumnModel().getColumn(0).setMaxWidth(50);
        tableContent.getColumnModel().getColumn(0).setPreferredWidth(50);

        tableContent.setFillsViewportHeight(true);
        tableContent.setTableHeader(null);

        scrollPaneConcept.setViewportView(tableContent);
    }

    private ArrayList<Keyword> getSelectedTopics() {
        ArrayList<Keyword> selectedTopics = new ArrayList<Keyword>();

        int[] selectedIndices = tableTopics.getSelectedRows();
        for (int i : selectedIndices) {
            selectedTopics.add(topTopics.get(i));
        }

        return selectedTopics;
    }

    private void updateTopics(int index) {
        ArrayList<Participant> ls = extractArrayListfromSet();
        // clean table
        while (modelTopics.getRowCount() > 0) {
            modelTopics.removeRow(0);
        }

        // add new topics
        // discussion topics
        if (index == 0) {
            topTopics = KeywordModeling.getSublist(chat.getTopics(), sliderTopics.getValue() * 5,
                    chckbxNounTopics.isSelected(), chckbxVerbTopics.isSelected());
            for (Keyword topic : topTopics) {
                Object[] row = {topic.getWord().getLemma(), Formatting.formatNumber(topic.getRelevance())};
                modelTopics.addRow(row);
            }
        } else {
            topTopics = KeywordModeling.getSublist(ls.get(index - 1).getContributions().getTopics(),
                    sliderTopics.getValue() * 5, chckbxNounTopics.isSelected(), chckbxVerbTopics.isSelected());
            for (Keyword topic : topTopics) {
                Object[] row = {topic.getWord().getLemma(), (int) Math.round(topic.getRelevance())};
                modelTopics.addRow(row);
            }
        }
    }

    /**
     * @return
     *
     */
    protected ArrayList<Participant> extractArrayListfromSet() {
        ArrayList<Participant> ls = new ArrayList<>();
        for (Participant p : chat.getParticipants()) {
            ls.add(p);
        }
        return ls;
    }

    private void updateContent() {
        // clean table
        while (modelContent.getRowCount() > 0) {
            modelContent.removeRow(0);
        }

        double s0 = 0, s1 = 0, s2 = 0, mean = 0, stdev = 0;

        for (Block b : chat.getBlocks()) {
            if (b != null) {
                s0++;
                s1 += b.getOverallScore();
                s2 += Math.pow(b.getOverallScore(), 2);
            }
        }

        // determine mean + stdev values
        if (s0 != 0) {
            mean = s1 / s0;
            stdev = Math.sqrt(s0 * s2 - Math.pow(s1, 2)) / s0;
        }

        if (chat.getBlocks() != null && chat.getBlocks().size() > 0) {
            // add content
            for (int index = 0; index < chat.getBlocks().size(); index++) {
                if (chat.getBlocks().get(index) != null) {
                    String text = "";
                    if (((Utterance) chat.getBlocks().get(index)).getParticipant() != null) {
                        text += "<b>" + ((Utterance) chat.getBlocks().get(index)).getParticipant().getName();
                        if (((Utterance) chat.getBlocks().get(index)).getTime() != null) {
                            SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yy HH:mm");
                            text += " (" + formatter.format(((Utterance) chat.getBlocks().get(index)).getTime()) + ")";
                        }
                        text += ": </b>";
                    }
                    if (chat.getBlocks().get(index).getOverallScore() >= mean + stdev) {
                        text += "<b>" + chat.getBlocks().get(index).getText() + "</b>";
                    } else {
                        text += chat.getBlocks().get(index).getText();
                    }
                    Object[] row = {index + "", text + " ["
                        + Formatting.formatNumber(chat.getBlocks().get(index).getOverallScore()) + "]"};
                    modelContent.addRow(row);
                }
            }
        }
    }
}
