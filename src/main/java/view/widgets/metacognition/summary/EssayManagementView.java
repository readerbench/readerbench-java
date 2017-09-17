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
package view.widgets.metacognition.summary;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
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
import utils.LocalizationUtils;

public class EssayManagementView extends JFrame {
    
    private static final long serialVersionUID = -2864356905020607155L;
    static final Logger LOGGER = Logger.getLogger("");
    public static final String VERBALIZATION_TAG = "//verbalization_breakpoint//";
    
    private EssayCreator loadedDocument = null;
    private final JPanel contentPane;
    private final JTextField textFieldTitle;
    private final JTextField textFieldAuthors;
    private JTextArea textAreaContent;
    private static File lastDirectory;
    
    public EssayManagementView() {
        super.setTitle("ReaderBench - " + LocalizationUtils.getTitle(this.getClass()));
        super.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        
        super.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                quitFormDialogue();
            }
        });
        super.setBounds(100, 100, 700, 600);
        
        JMenuBar menuBar = new JMenuBar();
        super.setJMenuBar(menuBar);
        
        JMenu mnFile = new JMenu(LocalizationUtils.getGeneric("file"));
        mnFile.setMnemonic(KeyEvent.VK_F);
        menuBar.add(mnFile);
        
        JMenuItem mntmConvert = new JMenuItem(LocalizationUtils.getLocalizedString(this.getClass(), "mntmConvert"), KeyEvent.VK_C);
        mntmConvert.addActionListener((ActionEvent e) -> {
            JFileChooser fc;
            if (lastDirectory == null) {
                fc = new JFileChooser(new File("in"));
                fc.setMultiSelectionEnabled(true);
            } else {
                fc = new JFileChooser(lastDirectory);
                fc.setMultiSelectionEnabled(true);
            }
            fc.setFileFilter(new FileFilter() {
                @Override
                public boolean accept(File f) {
                    if (f.isDirectory()) {
                        return true;
                    }
                    return f.getName().endsWith(".txt");
                }
                
                @Override
                public String getDescription() {
                    return LocalizationUtils.getLocalizedString(this.getClass(), "msgTXTFile");
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
        mntmConvert.setAccelerator(KeyStroke.getKeyStroke("control C"));
        mnFile.add(mntmConvert);
        
        JMenuItem mntmQuit = new JMenuItem(LocalizationUtils.getGeneric("quit"), KeyEvent.VK_Q);
        mntmQuit.setAccelerator(KeyStroke.getKeyStroke("control Q"));
        mntmQuit.addActionListener((ActionEvent e) -> {
            quitFormDialogue();
        });
        mnFile.add(mntmQuit);
        
        contentPane = new JPanel();
        contentPane.setBackground(Color.WHITE);
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        super.setContentPane(contentPane);
        
        JLabel lblTitle = new JLabel(LocalizationUtils.getGeneric("title") + ":");
        lblTitle.setFont(new Font("Lucida Grande", Font.BOLD, 13));
        
        JLabel lblAuthors = new JLabel(LocalizationUtils.getGeneric("authors") + ":");
        lblAuthors.setFont(new Font("Lucida Grande", Font.BOLD, 13));
        
        JLabel lblAuthorsComment = new JLabel(LocalizationUtils.getGeneric("authorsComment"));
        lblAuthorsComment.setFont(new Font("Lucida Grande", Font.PLAIN, 9));
        
        textFieldTitle = new JTextField();
        textFieldTitle.setColumns(10);
        
        textFieldAuthors = new JTextField();
        textFieldAuthors.setColumns(10);
        
        JLabel lblText = new JLabel(LocalizationUtils.getGeneric("authorsComment"));
        lblText.setFont(new Font("SansSerif", Font.BOLD, 12));
        
        JButton btnFileChooser = new JButton(LocalizationUtils.getLocalizedString(this.getClass(), "btnFileChooser"));
        btnFileChooser.addActionListener((ActionEvent e) -> {
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
                    return f.getName().endsWith(".txt");
                }
                
                @Override
                public String getDescription() {
                    return LocalizationUtils.getGeneric("msgTXTFile");
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
        
        JButton btnSaveToXML = new JButton(LocalizationUtils.getLocalizedString(this.getClass(), "btnSaveToXML"));
        btnSaveToXML.setSize(40, 20);
        btnSaveToXML.addActionListener((ActionEvent e) -> {
            updateDocument();
            saveDocumentDialogue();
            loadDocument();
        });
        
        JSeparator separator = new JSeparator();
        
        JScrollPane scrollPane = new JScrollPane();
        scrollPane
                .setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        
        GroupLayout gl_contentPane = new GroupLayout(contentPane);
        gl_contentPane.setHorizontalGroup(gl_contentPane.createParallelGroup(Alignment.TRAILING)
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
                                        .addComponent(btnFileChooser))
                                .addComponent(btnSaveToXML, GroupLayout.PREFERRED_SIZE, 156, GroupLayout.PREFERRED_SIZE))
                        .addContainerGap())
        );
        gl_contentPane.setVerticalGroup(gl_contentPane.createParallelGroup(Alignment.LEADING)
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
                                .addComponent(btnFileChooser))
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addComponent(separator, GroupLayout.PREFERRED_SIZE, 2, GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addComponent(scrollPane, GroupLayout.DEFAULT_SIZE, 370, Short.MAX_VALUE)
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addComponent(btnSaveToXML)
                        .addContainerGap())
        );
        
        textAreaContent = new JTextArea();
        textAreaContent.setLineWrap(true);
        textAreaContent.setWrapStyleWord(true);
        scrollPane.setViewportView(textAreaContent);
        contentPane.setLayout(gl_contentPane);
        
    }
    
    public void quitFormDialogue() {
        Object[] options = {LocalizationUtils.getGeneric("yes"), LocalizationUtils.getGeneric("no"), LocalizationUtils.getGeneric("cancel")};
        int value = JOptionPane.showOptionDialog(this,
                LocalizationUtils.getGeneric("questionSave"), LocalizationUtils.getGeneric("save"),
                JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
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
        
        loadedDocument.setAuthors(new ArrayList<>());
        String[] authors = textFieldAuthors.getText().split(",");
        for (String author : authors) {
            loadedDocument.getAuthors().add(author.trim());
        }
        
        loadedDocument.setDate(new Date());
        
        int index = 0;
        loadedDocument.setBlocks(new ArrayList<>());
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
