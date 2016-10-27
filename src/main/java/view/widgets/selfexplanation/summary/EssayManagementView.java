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
package view.widgets.selfexplanation.summary;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Date;
import java.util.LinkedList;
import java.util.Vector;
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
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileFilter;



import data.Block;
import data.document.EssayCreator;
import java.util.ArrayList;
import org.openide.util.Exceptions;
import services.semanticModels.ISemanticModel;

public class EssayManagementView extends JFrame {

    private static final long serialVersionUID = -2864356905020607155L;
    static Logger logger = Logger.getLogger("");
    public static final String VERBALIZATION_TAG = "//verbalization_breakpoint//";

    private EssayCreator loadedDocument = null;
    private JPanel contentPane;
    private JTextField textFieldTitle;
    private JTextField textFieldAuthors;
    private JTextArea textAreaContent;
    private static File lastDirectory;

    public EssayManagementView() {
        setTitle("ReaderBench - Essay Management View");
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                quitFormDialogue();
            }
        });
        setBounds(100, 100, 700, 600);

        JMenuBar menuBar = new JMenuBar();
        setJMenuBar(menuBar);

        JMenu mnFile = new JMenu("File");
        mnFile.setMnemonic(KeyEvent.VK_F);
        menuBar.add(mnFile);

        JMenuItem mntmOpen = new JMenuItem("Select files to convert",
                KeyEvent.VK_O);
        mntmOpen.addActionListener((ActionEvent e) -> {
            JFileChooser fc = null;
            if (lastDirectory == null) {
                fc = new JFileChooser(new File("in"));
                fc.setMultiSelectionEnabled(true);
            } else {
                fc = new JFileChooser(lastDirectory);
                fc.setMultiSelectionEnabled(true);
            }
            fc.setFileFilter(new FileFilter() {
                public boolean accept(File f) {
                    if (f.isDirectory()) {
                        return true;
                    }
                    return f.getName().endsWith(".txt");
                }
                
                public String getDescription() {
                    return " simple text files (*.txt) in UTF-8 format";
                }
            });
            int returnVal = fc.showOpenDialog(EssayManagementView.this);
            
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File[] files = fc.getSelectedFiles();
                lastDirectory = files[0].getParentFile();
                
                if (loadedDocument == null) {
                    loadedDocument = new EssayCreator(null, new ArrayList<>(),
                            null);
                }
                for (File f : files) {
                    String p = f.getPath().replace(".txt", ".xml");
                    loadedDocument.exportXMLasEssay(p,
                            loadedDocument.readFromTxt(f.getPath()));
                }
                
            }
        });
        mntmOpen.setAccelerator(KeyStroke.getKeyStroke("control O"));
        mnFile.add(mntmOpen);

        JMenuItem mntmQuit = new JMenuItem("Quit", KeyEvent.VK_Q);
        mntmQuit.setAccelerator(KeyStroke.getKeyStroke("control Q"));
        mntmQuit.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                quitFormDialogue();
            }
        });
        mnFile.add(mntmQuit);

        contentPane = new JPanel();
        contentPane.setBackground(Color.WHITE);
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        setContentPane(contentPane);

        JLabel lblTitle = new JLabel("Title:");
        lblTitle.setFont(new Font("Lucida Grande", Font.BOLD, 13));

        JLabel lblAuthors = new JLabel("Authors*:");
        lblAuthors.setFont(new Font("Lucida Grande", Font.BOLD, 13));

        JLabel lblAuthorsComment = new JLabel(
                "* Multiple authors should be separated by commas");
        lblAuthorsComment.setFont(new Font("Lucida Grande", Font.PLAIN, 9));

        textFieldTitle = new JTextField();
        textFieldTitle.setColumns(10);

        textFieldAuthors = new JTextField();
        textFieldAuthors.setColumns(10);

        JLabel lblText = new JLabel("Text");
        lblText.setFont(new Font("SansSerif", Font.BOLD, 12));

        JButton fileChooserBtn = new JButton("Choose txt file to import");
        fileChooserBtn.addActionListener((ActionEvent e) -> {
            JFileChooser fc = null;
            if (lastDirectory == null) {
                fc = new JFileChooser(new File("in"));
            } else {
                fc = new JFileChooser(lastDirectory);
            }
            fc.setFileFilter(new FileFilter() {
                public boolean accept(File f) {
                    if (f.isDirectory()) {
                        return true;
                    }
                    return f.getName().endsWith(".txt");
                }
                
                public String getDescription() {
                    return "Simple text files (*.txt) in UTF-8 format";
                }
            });
            int returnVal = fc.showOpenDialog(EssayManagementView.this);
            
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File file = fc.getSelectedFile();
                lastDirectory = file.getParentFile();
                if (file.getName().endsWith(".xml")) {
                    loadedDocument = EssayCreator.load(file, new ArrayList<>(), null, false);
                    loadDocument();
                } else if (file.getName().endsWith(".txt")) {
                    textFieldTitle.setText(file.getName().replace(".txt", ""));
                    
                    // read txt content
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

        JButton saveToXML = new JButton("Save essay as xml");
        saveToXML.setSize(40, 20);
        saveToXML.addActionListener((ActionEvent e) -> {
            updateDocument();
            saveDocumentDialogue();
            loadDocument();
        });

        JSeparator separator = new JSeparator();

        JScrollPane scrollPane = new JScrollPane();
        scrollPane
                .setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        GroupLayout gl_contentPane = new GroupLayout(contentPane);
        gl_contentPane.setHorizontalGroup(
                gl_contentPane.createParallelGroup(Alignment.TRAILING)
                .addGroup(gl_contentPane.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(gl_contentPane.createParallelGroup(Alignment.TRAILING)
                                .addComponent(scrollPane, Alignment.LEADING)
                                .addComponent(separator, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 678, Short.MAX_VALUE)
                                .addGroup(Alignment.LEADING, gl_contentPane.createSequentialGroup()
                                        .addComponent(lblTitle)
                                        .addPreferredGap(ComponentPlacement.RELATED)
                                        .addComponent(textFieldTitle, GroupLayout.DEFAULT_SIZE, 639, Short.MAX_VALUE))
                                .addGroup(Alignment.LEADING, gl_contentPane.createSequentialGroup()
                                        .addComponent(lblAuthors)
                                        .addPreferredGap(ComponentPlacement.RELATED)
                                        .addComponent(textFieldAuthors, GroupLayout.DEFAULT_SIZE, 609, Short.MAX_VALUE))
                                .addComponent(lblAuthorsComment, Alignment.LEADING)
                                .addGroup(Alignment.LEADING, gl_contentPane.createSequentialGroup()
                                        .addComponent(lblText)
                                        .addPreferredGap(ComponentPlacement.RELATED, 484, Short.MAX_VALUE)
                                        .addComponent(fileChooserBtn))
                                .addComponent(saveToXML, GroupLayout.PREFERRED_SIZE, 156, GroupLayout.PREFERRED_SIZE))
                        .addContainerGap())
        );
        gl_contentPane.setVerticalGroup(
                gl_contentPane.createParallelGroup(Alignment.LEADING)
                .addGroup(gl_contentPane.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(gl_contentPane.createParallelGroup(Alignment.BASELINE)
                                .addComponent(lblTitle)
                                .addComponent(textFieldTitle, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(ComponentPlacement.UNRELATED)
                        .addGroup(gl_contentPane.createParallelGroup(Alignment.BASELINE)
                                .addComponent(lblAuthors)
                                .addComponent(textFieldAuthors, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addComponent(lblAuthorsComment)
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addGroup(gl_contentPane.createParallelGroup(Alignment.BASELINE)
                                .addComponent(lblText)
                                .addComponent(fileChooserBtn))
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addComponent(separator, GroupLayout.PREFERRED_SIZE, 2, GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addComponent(scrollPane, GroupLayout.DEFAULT_SIZE, 370, Short.MAX_VALUE)
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addComponent(saveToXML)
                        .addContainerGap())
        );

        textAreaContent = new JTextArea();
        textAreaContent.setLineWrap(true);
        textAreaContent.setWrapStyleWord(true);
        scrollPane.setViewportView(textAreaContent);
        contentPane.setLayout(gl_contentPane);

    }

    public void quitFormDialogue() {
        Object[] options = {"Yes", "No", "Cancel"};
        int value = JOptionPane.showOptionDialog(this,
                "Do you want to save changes?", "Save",
                JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE,
                null, options, options[0]);
        if (value == JOptionPane.YES_OPTION) {
            this.dispose();
        }
        if (value == JOptionPane.NO_OPTION) {
            this.dispose();
        }
    }

    public void loadDocument() {
        textFieldTitle.setText(loadedDocument.getTitleText());
        this.setTitle("ReaderBench (" + loadedDocument.getPath() + ")");

        String authors = "";
        for (String author : loadedDocument.getAuthors()) {
            authors += author + ", ";
        }
        textFieldAuthors.setText(authors.length() > 2 ? authors.substring(0,
                authors.length() - 2) : "");

        String content = "";
        for (Block b : loadedDocument.getBlocks()) {
            content += b.getText() + "\n\n";
        }

        textAreaContent.setText(content.trim());
    }

    public void updateDocument() {
        if (loadedDocument == null) {
            loadedDocument = new EssayCreator(null, new ArrayList<>(), null);
        }
        loadedDocument.setTitleText(textFieldTitle.getText());
        this.setTitle("ReaderBench (" + loadedDocument.getPath() + ")");

        loadedDocument.setAuthors(new LinkedList<String>());
        String[] authors = textFieldAuthors.getText().split(",");
        for (String author : authors) {
            loadedDocument.getAuthors().add(author.trim());
        }

        loadedDocument.setDate(new Date());

        int index = 0;
        loadedDocument.setBlocks(new Vector<>());
        for (String blocks : textAreaContent.getText().trim()
                .split(VERBALIZATION_TAG)) {
            Block b = null;
            for (String block : blocks.split("(\n)+")) {
                if (block.trim().length() > 0) {
                    b = new Block(loadedDocument, index++, block.trim(),
                            loadedDocument.getSemanticModels(),
                            loadedDocument.getLanguage());
                    loadedDocument.getBlocks().add(b);
                }
            }
            // always last block is followed by a verbalization tag
            if (b != null) {
                b.setFollowedByVerbalization(true);
            }
        }
    }

    public void saveDocumentDialogue() {
        JFileChooser fc = null;
        if (lastDirectory == null) {
            fc = new JFileChooser(new File("in"));
        } else {
            fc = new JFileChooser(lastDirectory);
        }
        fc.setFileFilter(new FileFilter() {
            public boolean accept(File f) {
                if (f.isDirectory()) {
                    return true;
                }
                return f.getName().endsWith(".xml");
            }

            public String getDescription() {
                return "XML files (*.xml)";
            }
        });

        int returnVal = fc.showSaveDialog(EssayManagementView.this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = fc.getSelectedFile();

            if (!file.getName().endsWith(".xml")) {
                // add xml extension
                loadedDocument.setPath(file.getPath() + ".xml");
                loadedDocument.exportXMLasEssay(loadedDocument.getPath(), null);

            } else {
                loadedDocument.setPath(file.getPath());
                loadedDocument.exportXML(loadedDocument.getPath());
            }
        }
    }

}
