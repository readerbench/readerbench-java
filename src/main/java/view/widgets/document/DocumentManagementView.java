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
import java.awt.event.ItemEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Date;
import java.util.logging.Logger;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JCheckBox;
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
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileFilter;

import data.Block;
import data.document.Document;
import java.util.ArrayList;
import org.openide.util.Exceptions;
import services.converters.Txt2XmlConverter;
import utils.LocalizationUtils;
import view.widgets.ReaderBenchView;

public class DocumentManagementView extends JFrame {

    private static final long serialVersionUID = -2864356905020607155L;
    static final Logger LOGGER = Logger.getLogger("");
    public static final String VERBALIZATION_TAG = "//verbalization_breakpoint//";

    private Document loadedDocument = null;
    private final JPanel contentPane;
    private final JTextField textFieldTitle;
    private final JTextField textFieldAuthors;
    private final JTextField textFieldURI;
    private final JTextField txtComplexityLevel;
    private final JTextField textFieldSource;
    private JTextArea textAreaContent;
    private static File lastDirectory;
    private JButton btnVerbalizationBreakpoint;
    private JCheckBox chckbxIsEssay;

    /**
     * Create the frame.
     */
    public DocumentManagementView() {
        super.setTitle("ReaderBench - " + LocalizationUtils.getTitle(this.getClass()));
        super.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        super.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                quitFormDialogue();
            }
        });
        super.setBounds(100, 100, 750, 700);

        contentPane = new JPanel();
        contentPane.setBackground(Color.WHITE);
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        super.setContentPane(contentPane);

        JLabel lblTitle = new JLabel(LocalizationUtils.getGeneric("title") + ":");
        lblTitle.setFont(new Font("Lucida Grande", Font.BOLD, 13));

        JLabel lblAuthors = new JLabel(LocalizationUtils.getGeneric("authors") + ":");
        lblAuthors.setFont(new Font("Lucida Grande", Font.BOLD, 13));

        JLabel lblAuthorsComment = new JLabel("* " + LocalizationUtils.getGeneric("authorsComment"));
        lblAuthorsComment.setFont(new Font("Lucida Grande", Font.PLAIN, 9));

        JLabel lblUri = new JLabel(LocalizationUtils.getGeneric("URI") + ":");
        lblUri.setFont(new Font("SansSerif", Font.BOLD, 12));

        textFieldTitle = new JTextField();
        textFieldTitle.setColumns(10);

        textFieldAuthors = new JTextField();
        textFieldAuthors.setColumns(10);

        textFieldURI = new JTextField();
        textFieldURI.setColumns(10);

        JLabel lblComplexityLevel = new JLabel(LocalizationUtils.getLocalizedString(this.getClass(), "lblComplexityLevel") + ":");
        lblComplexityLevel.setFont(new Font("SansSerif", Font.BOLD, 12));

        txtComplexityLevel = new JTextField();
        txtComplexityLevel.setHorizontalAlignment(SwingConstants.RIGHT);
        txtComplexityLevel.setColumns(10);

        JLabel lblSource = new JLabel(LocalizationUtils.getGeneric("source") + ":");
        lblSource.setFont(new Font("SansSerif", Font.BOLD, 12));

        textFieldSource = new JTextField();
        textFieldSource.setColumns(10);

        JLabel lblText = new JLabel(LocalizationUtils.getGeneric("text"));
        lblText.setFont(new Font("SansSerif", Font.BOLD, 12));

        btnVerbalizationBreakpoint = new JButton(LocalizationUtils.getLocalizedString(this.getClass(), "btnVerbalizationBreakpoint"));
        btnVerbalizationBreakpoint.addActionListener((ActionEvent e) -> {
            int pos = textAreaContent.getCaretPosition();
            textAreaContent.insert(VERBALIZATION_TAG, pos);
        });

        JSeparator separator = new JSeparator();

        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        JLabel lblByDefault = new JLabel("* " + LocalizationUtils.getLocalizedString(this.getClass(), "lblByDefault"));

        chckbxIsEssay = new JCheckBox(LocalizationUtils.getLocalizedString(this.getClass(), "chckbxIsEssay"));
        chckbxIsEssay.addItemListener((ItemEvent e) -> {
            btnVerbalizationBreakpoint.setEnabled(!chckbxIsEssay.isSelected());
        });
        GroupLayout gl_contentPane = new GroupLayout(contentPane);
        gl_contentPane.setHorizontalGroup(gl_contentPane.createParallelGroup(Alignment.TRAILING)
                .addGroup(gl_contentPane.createSequentialGroup().addContainerGap()
                        .addGroup(gl_contentPane.createParallelGroup(Alignment.LEADING).addComponent(scrollPane)
                                .addComponent(separator, GroupLayout.DEFAULT_SIZE, 648, Short.MAX_VALUE)
                                .addGroup(gl_contentPane.createSequentialGroup().addComponent(lblTitle)
                                        .addPreferredGap(ComponentPlacement.RELATED)
                                        .addComponent(textFieldTitle, GroupLayout.DEFAULT_SIZE, 611, Short.MAX_VALUE))
                                .addGroup(gl_contentPane.createSequentialGroup().addComponent(lblAuthors)
                                        .addPreferredGap(ComponentPlacement.RELATED)
                                        .addComponent(textFieldAuthors, GroupLayout.DEFAULT_SIZE, 586, Short.MAX_VALUE))
                                .addComponent(lblAuthorsComment)
                                .addGroup(gl_contentPane.createSequentialGroup().addComponent(lblUri)
                                        .addPreferredGap(ComponentPlacement.RELATED)
                                        .addComponent(textFieldURI, GroupLayout.DEFAULT_SIZE, 621, Short.MAX_VALUE))
                                .addGroup(gl_contentPane.createSequentialGroup().addComponent(lblComplexityLevel)
                                        .addPreferredGap(ComponentPlacement.RELATED)
                                        .addComponent(txtComplexityLevel, GroupLayout.PREFERRED_SIZE, 53,
                                                GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(ComponentPlacement.RELATED).addComponent(lblSource)
                                        .addPreferredGap(ComponentPlacement.RELATED).addComponent(textFieldSource,
                                        GroupLayout.DEFAULT_SIZE, 441, Short.MAX_VALUE))
                                .addGroup(gl_contentPane.createSequentialGroup().addComponent(lblText)
                                        .addPreferredGap(ComponentPlacement.RELATED, 287, Short.MAX_VALUE)
                                        .addComponent(chckbxIsEssay).addGap(18)
                                        .addComponent(btnVerbalizationBreakpoint))
                                .addComponent(lblByDefault))
                        .addContainerGap()));
        gl_contentPane.setVerticalGroup(gl_contentPane.createParallelGroup(Alignment.LEADING).addGroup(gl_contentPane
                .createSequentialGroup().addContainerGap()
                .addGroup(gl_contentPane.createParallelGroup(Alignment.BASELINE).addComponent(lblTitle).addComponent(
                        textFieldTitle, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
                        GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(ComponentPlacement.UNRELATED)
                .addGroup(gl_contentPane.createParallelGroup(Alignment.BASELINE).addComponent(lblAuthors).addComponent(
                        textFieldAuthors, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
                        GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(ComponentPlacement.RELATED).addComponent(lblAuthorsComment)
                .addPreferredGap(ComponentPlacement.RELATED)
                .addGroup(gl_contentPane.createParallelGroup(Alignment.BASELINE)
                        .addComponent(textFieldURI, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
                                GroupLayout.PREFERRED_SIZE)
                        .addComponent(lblUri))
                .addPreferredGap(ComponentPlacement.RELATED)
                .addGroup(gl_contentPane.createParallelGroup(Alignment.BASELINE)
                        .addComponent(txtComplexityLevel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
                                GroupLayout.PREFERRED_SIZE)
                        .addComponent(lblComplexityLevel).addComponent(lblSource).addComponent(textFieldSource,
                        GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(ComponentPlacement.RELATED)
                .addGroup(gl_contentPane.createParallelGroup(Alignment.BASELINE).addComponent(lblText)
                        .addComponent(btnVerbalizationBreakpoint).addComponent(chckbxIsEssay))
                .addPreferredGap(ComponentPlacement.RELATED)
                .addComponent(separator, GroupLayout.PREFERRED_SIZE, 2, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(ComponentPlacement.RELATED)
                .addComponent(scrollPane, GroupLayout.DEFAULT_SIZE, 299, Short.MAX_VALUE)
                .addPreferredGap(ComponentPlacement.RELATED).addComponent(lblByDefault)));

        textAreaContent = new JTextArea();
        textAreaContent.setLineWrap(true);
        textAreaContent.setWrapStyleWord(true);
        scrollPane.setViewportView(textAreaContent);

        JMenuBar menuBar = new JMenuBar();
        super.setJMenuBar(menuBar);

        JMenu mnFile = new JMenu(LocalizationUtils.getGeneric("file"));
        mnFile.setMnemonic(KeyEvent.VK_F);
        menuBar.add(mnFile);

        JMenuItem mntmNew = new JMenuItem(LocalizationUtils.getGeneric("new"), KeyEvent.VK_N);
        mntmNew.addActionListener((ActionEvent arg0) -> {
            EventQueue.invokeLater(() -> {
                DocumentManagementView view = new DocumentManagementView();
                view.setLocation(DocumentManagementView.this.getLocation().x + 50,
                        DocumentManagementView.this.getLocation().y + 50);
                view.setVisible(true);
            });
        });
        mntmNew.setAccelerator(KeyStroke.getKeyStroke("control N"));
        mnFile.add(mntmNew);

        JMenuItem mntmOpen = new JMenuItem(LocalizationUtils.getGeneric("open"), KeyEvent.VK_O);
        mntmOpen.addActionListener((ActionEvent e) -> {
            JFileChooser fc;
            if (lastDirectory == null) {
                fc = new JFileChooser(new File("in"));
            } else {
                fc = new JFileChooser(lastDirectory);
            }
            fc.setFileFilter(new FileFilter() {
                @Override
                public boolean accept(File f) {
                    if (f.isDirectory()) {
                        return true;
                    }
                    return f.getName().endsWith(".xml") || f.getName().endsWith(".txt");
                }

                @Override
                public String getDescription() {
                    return LocalizationUtils.getGeneric("msgUTF8File");
                }
            });

            int returnVal = fc.showOpenDialog(DocumentManagementView.this);

            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File file = fc.getSelectedFile();
                lastDirectory = file.getParentFile();
                if (file.getName().endsWith(".xml")) {
                    loadedDocument = Document.load(file, new ArrayList<>(), null, false);
                    loadDocument();
                } else if (file.getName().endsWith(".txt")) {
                    textFieldTitle.setText(file.getName().replace(".txt", ""));
                    try (BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"))) {
                        String line;
                        String content = "";
                        while ((line = in.readLine()) != null) {
                            if (line.trim().length() > 0) {
                                content += line.trim() + "\n\n";
                            }
                        }
                        textAreaContent.setText(content.trim());
                    } catch (IOException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            }
        });
        mntmOpen.setAccelerator(KeyStroke.getKeyStroke("control O"));
        mnFile.add(mntmOpen);

        JMenuItem mntmSave = new JMenuItem(LocalizationUtils.getGeneric("save"), KeyEvent.VK_S);
        mntmSave.addActionListener((ActionEvent e) -> {
            if (loadedDocument == null) {
                updateDocument();
                saveDocumentDialogue();
            } else {
                updateDocument();
                loadedDocument.exportXML(loadedDocument.getPath());
            }
            loadDocument();
        });
        mntmSave.setAccelerator(KeyStroke.getKeyStroke("control S"));
        mnFile.add(mntmSave);

        JMenuItem mntmSaveAs = new JMenuItem(LocalizationUtils.getGeneric("saveAs"), KeyEvent.VK_S);
        mntmSaveAs.addActionListener((ActionEvent e) -> {
            updateDocument();
            saveDocumentDialogue();
            loadDocument();
        });
        mntmSaveAs.setAccelerator(KeyStroke.getKeyStroke("control alt S"));
        mnFile.add(mntmSaveAs);

        JMenuItem mntmQuit = new JMenuItem(LocalizationUtils.getGeneric("quit"), KeyEvent.VK_Q);
        mntmQuit.setAccelerator(KeyStroke.getKeyStroke("control Q"));
        mntmQuit.addActionListener((ActionEvent e) -> {
            quitFormDialogue();
        });
        mnFile.add(mntmQuit);

        JMenu mnTools = new JMenu(LocalizationUtils.getLocalizedString(this.getClass(), "mnTools"));
        menuBar.add(mnTools);

        JMenuItem mntmConvert = new JMenuItem(LocalizationUtils.getLocalizedString(this.getClass(), "mntmConvert"));
        mntmConvert.addActionListener((ActionEvent e) -> {
            openTextDocumentDialogue();
        });
        mnTools.add(mntmConvert);

        contentPane.setLayout(gl_contentPane);
    }

    public void loadDocument() {
        textFieldTitle.setText(loadedDocument.getTitleText());
        this.setTitle("ReaderBench (" + loadedDocument.getPath() + ")");

        String authors = "";
        authors = loadedDocument.getAuthors().stream().map((author) -> author + ", ").reduce(authors, String::concat);
        textFieldAuthors.setText(authors.length() > 2 ? authors.substring(0, authors.length() - 2) : "");

        textFieldURI.setText(loadedDocument.getURI());

        txtComplexityLevel.setText(loadedDocument.getComplexityLevel());

        textFieldSource.setText(loadedDocument.getSource());

        String content = "";
        for (Block b : loadedDocument.getBlocks()) {
            if (b.isFollowedByVerbalization()) {
                content += b.getText() + "\n\n" + VERBALIZATION_TAG + "\n\n";
            } else {
                content += b.getText() + "\n\n";
            }
        }

        textAreaContent.setText(content.trim());
    }

    public void updateDocument() {
        if (loadedDocument == null) {
            loadedDocument = new Document(null, new ArrayList<>(), null);
        }
        loadedDocument.setTitleText(textFieldTitle.getText());
        this.setTitle("ReaderBench (" + loadedDocument.getPath() + ")");

        loadedDocument.setAuthors(new ArrayList<>());
        String[] authors = textFieldAuthors.getText().split(",");
        for (String author : authors) {
            loadedDocument.getAuthors().add(author.trim());
        }

        loadedDocument.setDate(new Date());

        loadedDocument.setURI(textFieldURI.getText());

        loadedDocument.setComplexityLevel(txtComplexityLevel.getText());

        loadedDocument.setSource(textFieldSource.getText());

        int index = 0;
        loadedDocument.setBlocks(new ArrayList<>());
        for (String blocks : textAreaContent.getText().trim().split(VERBALIZATION_TAG)) {
            Block b = null;
            for (String block : blocks.split("(\n)+")) {
                if (block.trim().length() > 0) {
                    b = new Block(loadedDocument, index++, block.trim(),
                            loadedDocument.getSemanticModels(), loadedDocument.getLanguage());
                    loadedDocument.getBlocks().add(b);
                }
            }
            // always last block is followed by a verbalization tag; omit for essay
            if (b != null && !chckbxIsEssay.isSelected()) {
                b.setFollowedByVerbalization(true);
            }
        }
    }

    public void saveDocumentDialogue() {
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

        int returnVal = fc.showSaveDialog(DocumentManagementView.this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = fc.getSelectedFile();
            String path;
            if (!file.getName().endsWith(".xml")) {
                // add xml extension
                path = file.getPath() + ".xml";
            } else {
                path = file.getPath();
            }
            loadedDocument.setPath(path);
            loadedDocument.exportXML(path);
        }
    }

    public void openTextDocumentDialogue() {
        JFileChooser fc;
        if (lastDirectory == null) {
            fc = new JFileChooser(new File("in"));
        } else {
            fc = new JFileChooser(lastDirectory);
        }
        fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fc.setFileFilter(new FileFilter() {
            @Override
            public boolean accept(File f) {
                return f.isDirectory();
            }

            @Override
            public String getDescription() {
                return LocalizationUtils.getGeneric("msgFolder");
            }
        });

        int returnVal = fc.showOpenDialog(DocumentManagementView.this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = fc.getSelectedFile();
            this.convertTextFilesToXml(file);
        }
    }

    public void convertTextFilesToXml(File file) {
        Txt2XmlConverter.parseTxtFiles("Converted", file.getAbsolutePath(), ReaderBenchView.RUNTIME_LANGUAGE, "UTF-8");
    }

    public void quitFormDialogue() {
        Object[] options = {LocalizationUtils.getGeneric("yes"), LocalizationUtils.getGeneric("no"), LocalizationUtils.getGeneric("cancel")};
        int value = JOptionPane.showOptionDialog(this,
                LocalizationUtils.getGeneric("questionSave"),
                LocalizationUtils.getGeneric("save"), JOptionPane.YES_NO_CANCEL_OPTION,
                JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
        if (value == JOptionPane.YES_OPTION) {
            if (loadedDocument == null) {
                updateDocument();
                saveDocumentDialogue();
            } else {
                loadedDocument.exportXML(loadedDocument.getPath());
            }
            this.dispose();
        }
        if (value == JOptionPane.NO_OPTION) {
            this.dispose();
        }
    }
}
