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

import data.AbstractDocumentTemplate;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.logging.Logger;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileFilter;

import utils.LocalizationUtils;
import data.Block;
import data.document.Document;
import data.document.Metacognition;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.swing.JCheckBox;
import view.widgets.ReaderBenchView;

public class CreateVerbalizationView extends JFrame {

    private static final long serialVersionUID = -2864356905020607155L;
    static final Logger LOGGER = Logger.getLogger("");

    private Document loadedDocument = null;
    private Metacognition loadedVerbalization = null;
    private static File lastDirectory;
    private int noVerbalizations;
    private final JPanel contentPane;
    private final JTextField textFieldAuthor;
    private final JTextField textFieldTeachers;
    private final JTextField textFieldDate;
    private final JTextField textFieldURI;
    private final JTextField textFieldSource;
    private final JLabel lblClassroomLevel;
    private final JTextField txtClassroomLevel;
    private JButton buttonPrevious;
    private JButton buttonNext;
    private JTabbedPane tabbedPane;
    private JSplitPane[] splitPane;
    private JScrollPane[] scrollPaneDocument;
    private JTextArea[] txtrDocument;
    private int[] verbalizationBreakpoints;
    private JScrollPane[] scrollPaneVerbalization;
    private JTextArea[] txtrVerbalization;
    private JMenuItem mntmOpenDocument;
    private JMenuItem mntmOpenVerbalization;
    private JMenuItem mntmSaveVerbalization;
    private JMenuItem mntmNewVerbalization;

    private String previouslyBlockText = "";
    private JCheckBox chckbxIsSummary;

    /**
     * Create the frame.
     */
    public CreateVerbalizationView() {
        super.setTitle("ReaderBench - " + LocalizationUtils.getTitle(this.getClass()));
        super.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        super.setBounds(100, 100, 700, 600);

        JMenuBar menuBar = new JMenuBar();
        super.setJMenuBar(menuBar);

        JMenu mnFile = new JMenu(LocalizationUtils.getGeneric("file"));
        mnFile.setMnemonic(KeyEvent.VK_F);
        menuBar.add(mnFile);

        mntmOpenDocument = new JMenuItem(LocalizationUtils.getLocalizedString(this.getClass(), "mntmOpenDocument"));
        mntmOpenDocument.addActionListener((ActionEvent e) -> {
            JFileChooser fc;
            if (lastDirectory == null) {
                fc = new JFileChooser(new File("resources/in"));
            } else {
                fc = new JFileChooser(lastDirectory);
            }
            fc.setFileFilter(new FileFilter() {
                @Override
                public boolean accept(File f) {
                    if (f.isDirectory()) {
                        return true;
                    }
                    return f.getName().endsWith(".xml");
                }

                @Override
                public String getDescription() {
                    return LocalizationUtils.getGeneric("msgXMLFileOnly");
                }
            });
            int returnVal = fc.showOpenDialog(CreateVerbalizationView.this);

            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File file = fc.getSelectedFile();
                lastDirectory = file.getParentFile();
                loadedDocument = Document.load(file, new ArrayList<>(), ReaderBenchView.RUNTIME_LANGUAGE, false);
                if (loadedDocument != null) {
                    mntmOpenDocument.setEnabled(false);
                    mntmOpenVerbalization.setEnabled(true);
                    mntmNewVerbalization.setEnabled(true);
                    mntmSaveVerbalization.setEnabled(true);
                    noVerbalizations = loadedDocument.getNoVerbalizationBreakPoints();

                    splitPane = new JSplitPane[noVerbalizations];
                    scrollPaneDocument = new JScrollPane[noVerbalizations];
                    txtrDocument = new JTextArea[noVerbalizations];
                    scrollPaneVerbalization = new JScrollPane[noVerbalizations];
                    txtrVerbalization = new JTextArea[noVerbalizations];

                    if (noVerbalizations > 0) {
                        buttonNext.setEnabled(true);
                        verbalizationBreakpoints = new int[noVerbalizations];
                    }

                    for (int i = 0; i < noVerbalizations; i++) {
                        splitPane[i] = new JSplitPane();
                        splitPane[i].setOrientation(JSplitPane.VERTICAL_SPLIT);
                        splitPane[i].setDividerLocation(200);
                        splitPane[i].setDividerSize(5);
                        tabbedPane.addTab((i + 1) + "", null, splitPane[i], null);

                        scrollPaneDocument[i] = new JScrollPane();
                        splitPane[i].setLeftComponent(scrollPaneDocument[i]);

                        scrollPaneDocument[i].setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
                        txtrDocument[i] = new JTextArea();
                        txtrDocument[i].setBackground(Color.LIGHT_GRAY);
                        txtrDocument[i].setEditable(false);
                        txtrDocument[i].setWrapStyleWord(true);
                        txtrDocument[i].setLineWrap(true);
                        scrollPaneDocument[i].setViewportView(txtrDocument[i]);

                        scrollPaneVerbalization[i] = new JScrollPane();
                        splitPane[i].setRightComponent(scrollPaneVerbalization[i]);

                        scrollPaneVerbalization[i].setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
                        txtrVerbalization[i] = new JTextArea();
                        txtrVerbalization[i].setWrapStyleWord(true);
                        txtrVerbalization[i].setLineWrap(true);
                        scrollPaneVerbalization[i].setViewportView(txtrVerbalization[i]);
                    }

                    // update text
                    String text = "";
                    int i = 0;
                    for (Block b : loadedDocument.getBlocks()) {
                        if (b != null) {
                            text += b.getText() + " ";
                            if (b.isFollowedByVerbalization()) {
                                txtrDocument[i].setText(text);
                                verbalizationBreakpoints[i] = b.getIndex();
                                i++;
                                text = "";
                            }
                        }
                    }
                }
            }
        });
        mnFile.add(mntmOpenDocument);

        mntmNewVerbalization = new JMenuItem(LocalizationUtils.getGeneric("new"), KeyEvent.VK_N);
        mntmNewVerbalization.setAccelerator(KeyStroke.getKeyStroke("control N"));
        mntmNewVerbalization.setEnabled(false);
        mnFile.add(mntmNewVerbalization);

        mntmOpenVerbalization = new JMenuItem(LocalizationUtils.getLocalizedString(this.getClass(), "mntmOpenVerbalization"), KeyEvent.VK_O);
        mntmOpenVerbalization.setAccelerator(KeyStroke.getKeyStroke("control O"));
        mntmOpenVerbalization.setEnabled(false);
        mntmOpenVerbalization.addActionListener((ActionEvent e) -> {
            if (loadedDocument != null) {
                JFileChooser fc;
                if (lastDirectory == null) {
                    fc = new JFileChooser(new File("resources/in"));
                } else {
                    fc = new JFileChooser(lastDirectory);
                }
                fc.setFileFilter(new FileFilter() {
                    @Override
                    public boolean accept(File f) {
                        if (f.isDirectory()) {
                            return true;
                        }
                        return f.getName().endsWith(".xml");
                    }

                    @Override
                    public String getDescription() {
                        return LocalizationUtils.getGeneric("msgXMLFile");
                    }
                });
                int returnVal = fc.showOpenDialog(CreateVerbalizationView.this);

                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    File file = fc.getSelectedFile();
                    lastDirectory = file.getParentFile();
                    loadedVerbalization = Metacognition.loadVerbalization(file.getAbsolutePath(), loadedDocument, false);
                    mntmOpenDocument.setEnabled(false);
                    mntmSaveVerbalization.setEnabled(true);

                    updateContent();

                    if (txtrVerbalization.length != loadedVerbalization.getBlocks().size()) {
                        JOptionPane.showMessageDialog(
                                CreateVerbalizationView.this,
                                LocalizationUtils.getLocalizedString(this.getClass(), "msgIncorrectNoMeta") + "!",
                                "Information", JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        for (int i = 0; i < loadedVerbalization.getBlocks().size(); i++) {
                            txtrVerbalization[i].setText(loadedVerbalization.getBlocks().get(i).getText());
                        }
                    }
                }
            } else {
                JOptionPane.showMessageDialog(
                        CreateVerbalizationView.this,
                        LocalizationUtils.getLocalizedString(this.getClass(), "msgTemplate") + "!",
                        "Information", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        mnFile.add(mntmOpenVerbalization);

        mntmSaveVerbalization = new JMenuItem(LocalizationUtils.getGeneric("save"), KeyEvent.VK_S);
        mntmSaveVerbalization.setEnabled(false);
        mntmSaveVerbalization.setAccelerator(KeyStroke.getKeyStroke("control S"));
        mntmSaveVerbalization.addActionListener((ActionEvent e) -> {
            JFileChooser fc;
            if (lastDirectory == null) {
                fc = new JFileChooser(new File("resources/in"));
            } else {
                fc = new JFileChooser(lastDirectory);
            }
            fc.setFileFilter(new FileFilter() {
                @Override
                public boolean accept(File f) {
                    if (f.isDirectory()) {
                        return true;
                    }
                    return f.getName().endsWith(".xml");
                }

                @Override
                public String getDescription() {
                    return LocalizationUtils.getGeneric("msgXMLFileOnly");
                }
            });

            int returnVal = fc.showSaveDialog(CreateVerbalizationView.this);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File file = fc.getSelectedFile();
                String path;
                if (!file.getName().endsWith(".xml")) {
                    // add xml extension
                    path = file.getPath() + ".xml";
                } else {
                    path = file.getPath();
                }
                AbstractDocumentTemplate contents = new AbstractDocumentTemplate();

                List<Block> verbalizationBlocks = new ArrayList<>(noVerbalizations);
                for (int i = 0; i < noVerbalizations; i++) {
                    AbstractDocumentTemplate.BlockTemplate block = contents.new BlockTemplate();
                    block.setId(i);
                    block.setRefId(verbalizationBreakpoints[i]);
                    block.setContent(txtrVerbalization[i].getText());
                    contents.getBlocks().add(block);
                }
                Metacognition verbalization = new Metacognition(path, contents, loadedDocument, false);

                verbalization.setDate(new Date());
                verbalization.exportXML(path);
            }

        });

        mnFile.add(mntmSaveVerbalization);

        JMenuItem mntmQuit = new JMenuItem(LocalizationUtils.getGeneric("quit"), KeyEvent.VK_Q);
        mntmQuit.setAccelerator(KeyStroke.getKeyStroke("control Q"));
        mntmQuit.addActionListener((ActionEvent e) -> {
            setVisible(false);
            dispose();
        });
        mnFile.add(mntmQuit);

        contentPane = new JPanel();
        contentPane.setBackground(Color.WHITE);
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        super.setContentPane(contentPane);

        JLabel lblAuthor = new JLabel(LocalizationUtils.getGeneric("author") + ":");
        lblAuthor.setFont(new Font("Lucida Grande", Font.BOLD, 13));

        JLabel lblTeachers = new JLabel(LocalizationUtils.getLocalizedString(this.getClass(), "lblTeachers") + "*:");
        lblTeachers.setFont(new Font("Lucida Grande", Font.BOLD, 13));

        JLabel lblTeachersComment = new JLabel("* " + LocalizationUtils.getLocalizedString(this.getClass(), "lblTeachersComment"));
        lblTeachersComment.setFont(new Font("Lucida Grande", Font.PLAIN, 9));

        textFieldAuthor = new JTextField();
        textFieldAuthor.setColumns(10);

        textFieldTeachers = new JTextField();
        textFieldTeachers.setColumns(10);

        JLabel lblDate = new JLabel(LocalizationUtils.getGeneric("date") + ":");
        lblDate.setFont(new Font("SansSerif", Font.BOLD, 12));

        textFieldDate = new JTextField();
        textFieldDate.setBackground(Color.LIGHT_GRAY);
        textFieldDate.setEditable(false);
        textFieldDate.setHorizontalAlignment(SwingConstants.CENTER);
        textFieldDate.setColumns(10);

        JLabel lblUri = new JLabel(LocalizationUtils.getGeneric("URI") + ":");
        lblUri.setFont(new Font("SansSerif", Font.BOLD, 12));

        textFieldURI = new JTextField();
        textFieldURI.setColumns(10);

        JLabel lblSource = new JLabel(LocalizationUtils.getGeneric("source") + ":");
        lblSource.setFont(new Font("SansSerif", Font.BOLD, 12));

        textFieldSource = new JTextField();
        textFieldSource.setColumns(10);

        JLabel lblVerbalization = new JLabel(LocalizationUtils.getLocalizedString(this.getClass(), "lblVerbalization"));
        lblVerbalization.setFont(new Font("SansSerif", Font.BOLD, 12));

        JSeparator separator = new JSeparator();

        txtClassroomLevel = new JTextField();
        txtClassroomLevel.setHorizontalAlignment(SwingConstants.CENTER);
        txtClassroomLevel.setColumns(10);

        lblClassroomLevel = new JLabel(LocalizationUtils.getLocalizedString(this.getClass(), "lblClassroomLevel") + ":");
        lblClassroomLevel.setFont(new Font("SansSerif", Font.BOLD, 12));

        tabbedPane = new JTabbedPane(JTabbedPane.LEFT);
        tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);

        buttonPrevious = new JButton("<");
        buttonNext = new JButton(">");

        buttonNext.addActionListener((ActionEvent e) -> {
            if (tabbedPane.getSelectedIndex() < noVerbalizations - 1) {
                tabbedPane.setSelectedIndex(tabbedPane.getSelectedIndex() + 1);
                buttonPrevious.setEnabled(true);
                if (tabbedPane.getSelectedIndex() == noVerbalizations - 1) {
                    buttonNext.setEnabled(false);
                }
            }
        });

        buttonPrevious.addActionListener((ActionEvent e) -> {
            if (tabbedPane.getSelectedIndex() > 0) {
                tabbedPane.setSelectedIndex(tabbedPane.getSelectedIndex() - 1);
                buttonNext.setEnabled(true);
                if (tabbedPane.getSelectedIndex() == 0) {
                    buttonPrevious.setEnabled(false);
                }
            }
        });
        buttonNext.setEnabled(false);
        buttonPrevious.setEnabled(false);

        chckbxIsSummary = new JCheckBox(LocalizationUtils.getLocalizedString(this.getClass(), "chckbxIsSummary"));
        chckbxIsSummary.addItemListener((ItemEvent e) -> {
            if (loadedDocument != null) {
                tabbedPane.setEnabled(!chckbxIsSummary.isSelected());
                buttonNext.setEnabled(!chckbxIsSummary.isSelected());
                buttonPrevious.setEnabled(!chckbxIsSummary.isSelected());
                if (!chckbxIsSummary.isSelected()) {
                    if (tabbedPane.getSelectedIndex() == 0) {
                        buttonPrevious.setEnabled(false);
                    }
                    if (tabbedPane.getSelectedIndex() == noVerbalizations - 1) {
                        buttonNext.setEnabled(false);
                    }
                    txtrDocument[tabbedPane.getSelectedIndex()].setText(previouslyBlockText);
                } else {
                    previouslyBlockText = txtrDocument[tabbedPane.getSelectedIndex()].getText();
                    txtrDocument[tabbedPane.getSelectedIndex()].setText(loadedDocument.getText());
                }
            }
        });

        GroupLayout gl_contentPane = new GroupLayout(contentPane);
        gl_contentPane.setHorizontalGroup(
                gl_contentPane.createParallelGroup(Alignment.LEADING)
                        .addGroup(gl_contentPane.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(lblVerbalization)
                                .addGap(18)
                                .addComponent(chckbxIsSummary)
                                .addContainerGap(455, Short.MAX_VALUE))
                        .addGroup(gl_contentPane.createSequentialGroup()
                                .addGap(2)
                                .addComponent(separator, GroupLayout.DEFAULT_SIZE, 670, Short.MAX_VALUE))
                        .addGroup(gl_contentPane.createSequentialGroup()
                                .addGroup(gl_contentPane.createParallelGroup(Alignment.LEADING)
                                        .addComponent(tabbedPane, GroupLayout.DEFAULT_SIZE, 670, Short.MAX_VALUE)
                                        .addGroup(gl_contentPane.createSequentialGroup()
                                                .addContainerGap()
                                                .addComponent(lblSource)
                                                .addPreferredGap(ComponentPlacement.RELATED)
                                                .addComponent(textFieldSource, GroupLayout.DEFAULT_SIZE, 609, Short.MAX_VALUE))
                                        .addGroup(gl_contentPane.createSequentialGroup()
                                                .addContainerGap()
                                                .addComponent(lblDate)
                                                .addPreferredGap(ComponentPlacement.RELATED)
                                                .addComponent(textFieldDate, GroupLayout.PREFERRED_SIZE, 96, GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(ComponentPlacement.RELATED)
                                                .addComponent(lblUri)
                                                .addGap(8)
                                                .addComponent(textFieldURI, GroupLayout.DEFAULT_SIZE, 493, Short.MAX_VALUE))
                                        .addGroup(gl_contentPane.createSequentialGroup()
                                                .addContainerGap()
                                                .addComponent(lblTeachersComment))
                                        .addGroup(gl_contentPane.createSequentialGroup()
                                                .addContainerGap()
                                                .addComponent(buttonPrevious)
                                                .addPreferredGap(ComponentPlacement.RELATED)
                                                .addComponent(buttonNext))
                                        .addGroup(gl_contentPane.createSequentialGroup()
                                                .addContainerGap()
                                                .addComponent(lblAuthor)
                                                .addPreferredGap(ComponentPlacement.RELATED)
                                                .addComponent(textFieldAuthor, GroupLayout.DEFAULT_SIZE, 443, Short.MAX_VALUE)
                                                .addPreferredGap(ComponentPlacement.RELATED)
                                                .addComponent(lblClassroomLevel)
                                                .addPreferredGap(ComponentPlacement.RELATED)
                                                .addComponent(txtClassroomLevel, GroupLayout.PREFERRED_SIZE, 60, GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(ComponentPlacement.RELATED))
                                        .addGroup(gl_contentPane.createSequentialGroup()
                                                .addContainerGap()
                                                .addComponent(lblTeachers)
                                                .addPreferredGap(ComponentPlacement.RELATED)
                                                .addComponent(textFieldTeachers, GroupLayout.DEFAULT_SIZE, 586, Short.MAX_VALUE)))
                                .addGap(2))
        );
        gl_contentPane.setVerticalGroup(
                gl_contentPane.createParallelGroup(Alignment.LEADING)
                        .addGroup(gl_contentPane.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(gl_contentPane.createParallelGroup(Alignment.LEADING)
                                        .addComponent(lblAuthor)
                                        .addGroup(gl_contentPane.createParallelGroup(Alignment.BASELINE)
                                                .addComponent(textFieldAuthor, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                                .addComponent(lblClassroomLevel)
                                                .addComponent(txtClassroomLevel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)))
                                .addPreferredGap(ComponentPlacement.UNRELATED)
                                .addGroup(gl_contentPane.createParallelGroup(Alignment.BASELINE)
                                        .addComponent(lblTeachers)
                                        .addComponent(textFieldTeachers, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(ComponentPlacement.RELATED)
                                .addComponent(lblTeachersComment)
                                .addPreferredGap(ComponentPlacement.UNRELATED)
                                .addGroup(gl_contentPane.createParallelGroup(Alignment.BASELINE)
                                        .addComponent(textFieldURI, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                        .addComponent(lblDate)
                                        .addComponent(textFieldDate, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                        .addComponent(lblUri))
                                .addPreferredGap(ComponentPlacement.UNRELATED)
                                .addGroup(gl_contentPane.createParallelGroup(Alignment.BASELINE)
                                        .addComponent(lblSource)
                                        .addComponent(textFieldSource, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(ComponentPlacement.UNRELATED)
                                .addGroup(gl_contentPane.createParallelGroup(Alignment.BASELINE)
                                        .addComponent(lblVerbalization)
                                        .addComponent(chckbxIsSummary))
                                .addPreferredGap(ComponentPlacement.RELATED)
                                .addComponent(separator, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(ComponentPlacement.RELATED)
                                .addComponent(tabbedPane, GroupLayout.DEFAULT_SIZE, 277, Short.MAX_VALUE)
                                .addPreferredGap(ComponentPlacement.RELATED)
                                .addGroup(gl_contentPane.createParallelGroup(Alignment.TRAILING)
                                        .addComponent(buttonPrevious)
                                        .addComponent(buttonNext))
                                .addGap(4))
        );

        contentPane.setLayout(gl_contentPane);
    }

    private void updateContent() {
        if (loadedVerbalization != null
                && loadedVerbalization.getReferredDoc() != null) {
            if (loadedVerbalization.getAuthors().size() > 0) {
                textFieldAuthor.setText(loadedVerbalization.getAuthors().get(0));
            }
            String tutors = "";

            tutors = loadedVerbalization.getTutors().stream().map((tutor) -> tutor + ", ").reduce(tutors, String::concat);
            textFieldTeachers.setText(tutors.length() > 2 ? tutors.substring(0, tutors.length() - 2) : "");

            DateFormat df = new SimpleDateFormat("dd-mm-yyyy");
            if (loadedVerbalization.getDate() != null) {
                textFieldDate.setText(df.format(loadedVerbalization.getDate()));
            }
            textFieldURI.setText(loadedVerbalization.getURI());
            textFieldSource.setText(loadedVerbalization.getSource());
        }
    }
}