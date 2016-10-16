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

import java.awt.Color;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDesktopPane;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.RowFilter;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingWorker;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.TableRowSorter;

import org.apache.log4j.Logger;

import data.AbstractDocument;
import data.cscl.Conversation;
import data.Lang;
import data.AbstractDocument.SaveType;
import java.util.EnumMap;
import java.util.Map;
import org.openide.util.Exceptions;
import services.semanticModels.SemanticModel;
import utils.localization.LocalizationUtils;
import view.models.document.ConversationManagementTableModel;
import view.widgets.ReaderBenchView;

public class ConversationProcessingView extends JInternalFrame {

    private static final long serialVersionUID = -8772215709851320157L;
    static Logger logger = Logger.getLogger(ConversationProcessingView.class);

    private final JLabel lblLanguage;
    private final JComboBox<String> comboBoxLanguage;
    private final JButton btnRemoveDocument;
    private final JButton btnAddDocument;
    private final JButton btnViewDocument;
    private final JButton btnAddSerializedDocument;
    private final JTable docTable;
    private final ConversationManagementTableModel docTableModel;
    private final TableRowSorter<ConversationManagementTableModel> docSorter;
    private final JScrollPane scrollPane;
    private final JDesktopPane desktopPane;
    private static File lastDirectory = null;

    private static final List<Conversation> LOADED_CONVERSATIONS = new ArrayList<>();
    private CustomTextField articleTextField;
    private String queryArticleName;

    public class DocumentProcessingTask extends SwingWorker<Void, Void> {

        private final String pathToDoc;
        private final String pathToLSA;
        private final String pathToLDA;
        private final boolean usePOSTagging;
        private final boolean isSerialized;

        public DocumentProcessingTask(String pathToDoc, String pathToLSA, String pathToLDA, boolean usePOSTagging,
                boolean isSerialized) {
            super();
            this.pathToDoc = pathToDoc;
            this.pathToLSA = pathToLSA;
            this.pathToLDA = pathToLDA;
            this.usePOSTagging = usePOSTagging;
            this.isSerialized = isSerialized;
        }

        public void addSingleDocument(String pathToIndividualFile) {
            AbstractDocument d = null;
            if (isSerialized) {
                d = AbstractDocument.loadSerializedDocument(pathToIndividualFile);
            } else if (AbstractDocument.checkTagsDocument(new File(pathToIndividualFile), "Utterance")) {
                Map<SemanticModel, String> modelPaths = new EnumMap<>(SemanticModel.class);
                modelPaths.put(SemanticModel.LSA, pathToLSA);
                modelPaths.put(SemanticModel.LDA, pathToLDA);
                d = Conversation.loadGenericDocument(pathToIndividualFile, modelPaths,
                        ReaderBenchView.RUNTIME_LANGUAGE, usePOSTagging, usePOSTagging, null, null, true,
                        SaveType.SERIALIZED_AND_CSV_EXPORT);
            }
            if (d == null) {
                JOptionPane.showMessageDialog(desktopPane, "File " + pathToIndividualFile + " does not have an appropriate conversation XML structure!", "Information", JOptionPane.INFORMATION_MESSAGE);
            } else if (d.getLanguage() == ReaderBenchView.RUNTIME_LANGUAGE) {
                addConversation((Conversation) d);
            } else {
                JOptionPane.showMessageDialog(desktopPane, "File " + pathToIndividualFile + "Incorrect language for the loaded document!", "Information", JOptionPane.INFORMATION_MESSAGE);
            }
        }

        @Override
        public Void doInBackground() {
            btnAddDocument.setEnabled(false);
            btnAddSerializedDocument.setEnabled(false);

            File file = new File(pathToDoc);
            File[] files = {file};
            if (isSerialized) {
                if (file.isDirectory()) {
                    // process each individual ser file
                    files = file.listFiles((File dir, String name1) -> name1.endsWith(".ser"));
                }
            } else if (file.isDirectory()) {
                // process each individual xml file
                files = file.listFiles((File dir, String name1) -> name1.endsWith(".xml"));
            }
            for (File f : files) {
                try {
                    addSingleDocument(f.getPath());
                } catch (Exception ex) {
                    logger.error(f.getName() + ": " + ex.getMessage());
                    Exceptions.printStackTrace(ex);
                }
            }
            return null;
        }

        @Override
        public void done() {
            Toolkit.getDefaultToolkit().beep();
            btnAddDocument.setEnabled(true);
            btnAddSerializedDocument.setEnabled(true);
        }
    }

    private class CustomTextField extends JTextField {

        private static final long serialVersionUID = 1L;

        private Font originalFont;
        private Color originalForeground;
        /**
         * Grey by default*
         */
        private Color placeholderForeground = new Color(160, 160, 160);
        private boolean textWrittenIn;

        public CustomTextField(int columns) {
            super(columns);
        }

        @Override
        public void setFont(Font f) {
            super.setFont(f);
            if (!isTextWrittenIn()) {
                originalFont = f;
            }
        }

        @Override
        public void setForeground(Color fg) {
            super.setForeground(fg);
            if (!isTextWrittenIn()) {
                originalForeground = fg;
            }
        }

        public Color getPlaceholderForeground() {
            return placeholderForeground;
        }

        public void setPlaceholderForeground(Color placeholderForeground) {
            this.placeholderForeground = placeholderForeground;
        }

        public boolean isTextWrittenIn() {
            return textWrittenIn;
        }

        public void setTextWrittenIn(boolean textWrittenIn) {
            this.textWrittenIn = textWrittenIn;
        }

        public void setPlaceholder(final String text) {
            this.customizeText(text);
            this.getDocument().addDocumentListener(new DocumentListener() {
                @Override
                public void insertUpdate(DocumentEvent e) {
                    warn();
                }

                @Override
                public void removeUpdate(DocumentEvent e) {
                    warn();
                }

                @Override
                public void changedUpdate(DocumentEvent e) {
                    warn();
                }

                public void warn() {
                    if (getText().trim().length() != 0) {
                        setFont(originalFont);
                        setForeground(originalForeground);
                        setTextWrittenIn(true);
                    }
                }
            });

            this.addFocusListener(new FocusListener() {
                @Override
                public void focusGained(FocusEvent e) {
                    if (!isTextWrittenIn()) {
                        setText("");
                    }
                }

                @Override
                public void focusLost(FocusEvent e) {
                    if (getText().trim().length() == 0) {
                        customizeText(text);
                    }
                }
            });
        }

        private void customizeText(String text) {
            setText(text);
            setFont(new Font(getFont().getFamily(), getFont().getStyle(), getFont().getSize()));
            setForeground(getPlaceholderForeground());
            setTextWrittenIn(false);
        }
    }

    /**
     * Create the frame.
     */
    public ConversationProcessingView() {
        super.setTitle("ReaderBench - " + LocalizationUtils.getTranslation("Conversation Processing"));
        super.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        super.setResizable(true);
        super.setClosable(true);
        super.setMaximizable(true);
        super.setIconifiable(true);
        super.setBounds(20, 20, 950, 350);
        desktopPane = new JDesktopPane();
        desktopPane.setBackground(Color.WHITE);

        queryArticleName = "";
        lblLanguage = new JLabel(LocalizationUtils.getTranslation("Language") + ":");
        lblLanguage.setFont(new Font("SansSerif", Font.BOLD, 12));
        lblLanguage.setForeground(Color.BLACK);

        comboBoxLanguage = new JComboBox<>();
        for (Lang l : Lang.values()) {
            comboBoxLanguage.addItem(l.getDescription());
        }
        comboBoxLanguage.setEnabled(false);
        comboBoxLanguage.setSelectedItem(ReaderBenchView.RUNTIME_LANGUAGE.getDescription());

        btnAddDocument = new JButton(LocalizationUtils.getTranslation("Add conversation(s)"));
        btnAddDocument.addActionListener((ActionEvent e) -> {
            try {
                JInternalFrame frame = new AddConversationView(ReaderBenchView.RUNTIME_LANGUAGE,
                        ConversationProcessingView.this);
                frame.setVisible(true);
                desktopPane.add(frame);
                frame.setSelected(true);
            } catch (Exception ex) {
                Exceptions.printStackTrace(ex);
            }
        });

        btnAddSerializedDocument = new JButton(LocalizationUtils.getTranslation("Add preprocessed conversation(s)"));
        btnAddSerializedDocument.addActionListener((ActionEvent e) -> {
            JFileChooser fc = null;
            if (lastDirectory == null) {
                fc = new JFileChooser(new File("resources/in"));
            } else {
                fc = new JFileChooser(lastDirectory);
            }
            fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
            fc.setFileFilter(new FileFilter() {
                @Override
                public boolean accept(File f) {
                    if (f.isDirectory()) {
                        return true;
                    }
                    return f.getName().endsWith(".ser");
                }

                @Override
                public String getDescription() {
                    return "Serialized documents (*.ser) or directory";
                }
            });
            int returnVal = fc.showOpenDialog(ConversationProcessingView.this);

            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File file = fc.getSelectedFile();
                lastDirectory = file.getParentFile();
                ConversationProcessingView.DocumentProcessingTask task = ConversationProcessingView.this.new DocumentProcessingTask(file.getPath(), null, null, false, true);
                task.execute();
            }
        });

        docTableModel = new ConversationManagementTableModel();
        docTable = new JTable(docTableModel);
        docTable.setFillsViewportHeight(true);
        docSorter = new TableRowSorter<>(docTableModel);
        docTable.setRowSorter(docSorter);

        docTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                if (event.getClickCount() == 2) {
                    JTable target = (JTable) event.getSource();
                    int row = target.getSelectedRow();
                    if (row >= 0 && row < LOADED_CONVERSATIONS.size()) {
                        int modelRow = target.convertRowIndexToModel(target.getSelectedRow());
                        AbstractDocument d = LOADED_CONVERSATIONS.get(modelRow);
                        if (d instanceof Conversation) {
                            ChatView view = new ChatView((Conversation) d);
                            view.setVisible(true);
                        }
                    }
                }
            }
        });

        scrollPane = new JScrollPane();
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setViewportView(docTable);

        JPanel panelSingleDoc = new JPanel();
        panelSingleDoc.setBackground(Color.WHITE);
        panelSingleDoc.setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null),
                LocalizationUtils.getTranslation("Specific conversation operations"), TitledBorder.LEFT,
                TitledBorder.TOP, null, null));

        JPanel panelSearch = new JPanel();
        panelSearch.setBackground(Color.WHITE);
        panelSearch.setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null), "Filter",
                TitledBorder.LEADING, TitledBorder.TOP, null, new Color(59, 59, 59)));

        GroupLayout gl_desktopPane = new GroupLayout(desktopPane);
        gl_desktopPane.setHorizontalGroup(gl_desktopPane.createParallelGroup(Alignment.TRAILING).addGroup(gl_desktopPane
                .createSequentialGroup().addContainerGap()
                .addGroup(gl_desktopPane.createParallelGroup(Alignment.TRAILING)
                        .addComponent(scrollPane, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 865, Short.MAX_VALUE)
                        .addComponent(panelSearch, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 865, Short.MAX_VALUE)
                        .addGroup(Alignment.LEADING,
                                gl_desktopPane.createSequentialGroup().addComponent(lblLanguage).addGap(2)
                                .addComponent(comboBoxLanguage, 0, 804, Short.MAX_VALUE))
                        .addComponent(panelSingleDoc, Alignment.LEADING, GroupLayout.DEFAULT_SIZE,
                                GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap()));
        gl_desktopPane.setVerticalGroup(gl_desktopPane.createParallelGroup(Alignment.LEADING).addGroup(gl_desktopPane
                .createSequentialGroup().addContainerGap()
                .addGroup(gl_desktopPane.createParallelGroup(Alignment.LEADING, false)
                        .addComponent(lblLanguage, GroupLayout.PREFERRED_SIZE, 25, GroupLayout.PREFERRED_SIZE)
                        .addComponent(comboBoxLanguage, GroupLayout.PREFERRED_SIZE, 25, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(ComponentPlacement.RELATED)
                .addComponent(panelSearch, GroupLayout.PREFERRED_SIZE, 62, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(ComponentPlacement.RELATED)
                .addComponent(scrollPane, GroupLayout.DEFAULT_SIZE, 126, Short.MAX_VALUE)
                .addPreferredGap(ComponentPlacement.RELATED).addComponent(panelSingleDoc, GroupLayout.PREFERRED_SIZE,
                GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addContainerGap()));

        articleTextField = new CustomTextField(1);
        articleTextField.setPlaceholder("Insert Conversation Name");
        articleTextField.setFont(new Font("SansSerif", Font.ITALIC, 13));
        articleTextField.setPlaceholderForeground(Color.gray);
        articleTextField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void changedUpdate(DocumentEvent e) {
                warn();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                warn();
            }

            @Override
            public void insertUpdate(DocumentEvent e) {
                warn();
            }

            public void warn() {
                queryArticleName = articleTextField.getText();
                if (queryArticleName.equalsIgnoreCase("Insert Conversation Name")) {
                    queryArticleName = "";
                }
                newFilter();
            }
        });

        // JSplitPane splitPane = new JSplitPane();
        // splitPane.setBackground(Color.WHITE);
        panelSearch.add(articleTextField);
        // splitPane.setLeftComponent(articleTextField);
        articleTextField.setColumns(25);

        btnViewDocument = new JButton(LocalizationUtils.getTranslation("View conversation"));
        btnViewDocument.setEnabled(false);
        btnViewDocument.addActionListener((ActionEvent e) -> {
            if (docTable.getSelectedRow() != -1) {
                int modelRow = docTable.convertRowIndexToModel(docTable.getSelectedRow());
                AbstractDocument d = LOADED_CONVERSATIONS.get(modelRow);
                if (d instanceof Conversation) {
                    ChatView view = new ChatView((Conversation) d);
                    view.setVisible(true);
                }
            } else {
                JOptionPane.showMessageDialog(desktopPane, "Please select a document to be viewed!", "Information",
                        JOptionPane.INFORMATION_MESSAGE);
            }
        });

        btnRemoveDocument = new JButton(LocalizationUtils.getTranslation("Remove conversation"));
        btnRemoveDocument.setEnabled(false);
        GroupLayout gl_panelSingleDoc = new GroupLayout(panelSingleDoc);
        gl_panelSingleDoc.setHorizontalGroup(gl_panelSingleDoc.createParallelGroup(Alignment.LEADING)
                .addGroup(gl_panelSingleDoc.createSequentialGroup().addGap(2).addComponent(btnViewDocument).addGap(18)
                        .addComponent(btnAddDocument).addGap(18).addComponent(btnAddSerializedDocument).addGap(18)
                        .addComponent(btnRemoveDocument).addGap(131)));
        gl_panelSingleDoc.setVerticalGroup(gl_panelSingleDoc.createParallelGroup(Alignment.LEADING)
                .addGroup(gl_panelSingleDoc.createSequentialGroup().addGap(5)
                        .addGroup(gl_panelSingleDoc.createParallelGroup(Alignment.BASELINE)
                                .addComponent(btnViewDocument).addComponent(btnAddDocument)
                                .addComponent(btnAddSerializedDocument).addComponent(btnRemoveDocument))));
        panelSingleDoc.setLayout(gl_panelSingleDoc);
        btnRemoveDocument.addActionListener((ActionEvent e) -> {
            if (docTable.getSelectedRow() != -1) {
                int[] rows = docTable.getSelectedRows();
                for (int i = 0; i < rows.length; i++) {
                    int modelRow = docTable.convertRowIndexToModel(rows[i] - i);
                    Conversation toRemove = LOADED_CONVERSATIONS.get(modelRow);
                    LOADED_CONVERSATIONS.remove(toRemove);
                    docTableModel.removeRow(modelRow);
                }
            } else {
                JOptionPane.showMessageDialog(desktopPane, "Please load at least an appropriate conversation!", "Information", JOptionPane.INFORMATION_MESSAGE);
            }
        });
        desktopPane.setLayout(gl_desktopPane);

        super.setContentPane(desktopPane);
        updateContents();
    }

    public static List<Conversation> getLoadedConversations() {
        return LOADED_CONVERSATIONS;
    }

    public void addConversation(Conversation c) {
        if (docTableModel != null) {
            synchronized (LOADED_CONVERSATIONS) {
                //remove already existent copy
                for (int i = 0; i < LOADED_CONVERSATIONS.size(); i++) {
                    int modelRow = docTable.convertRowIndexToModel(i);
                    Conversation toRemove = LOADED_CONVERSATIONS.get(modelRow);
                    if (toRemove.getPath().equals(c.getPath()) && toRemove.getLSA().getPath().equals(c.getLSA().getPath()) && toRemove.getLDA().getPath().equals(c.getLDA().getPath())) {
                        LOADED_CONVERSATIONS.remove(toRemove);
                        docTableModel.removeRow(modelRow);
                    }
                }

                // add rows as loaded documents
                List<Object> dataRow = new ArrayList<>();

                dataRow.add(c.getTitleText());

                if (c.getLSA() != null) {
                    dataRow.add(c.getLSA().getPath());
                } else {
                    dataRow.add("");
                }
                if (c.getLDA() != null) {
                    dataRow.add(c.getLDA().getPath());
                } else {
                    dataRow.add("");
                }

                docTableModel.addRow(dataRow.toArray());
                LOADED_CONVERSATIONS.add(c);
            }

            if (LOADED_CONVERSATIONS.size() > 0) {
                btnRemoveDocument.setEnabled(true);
                btnViewDocument.setEnabled(true);
            } else {
                btnRemoveDocument.setEnabled(false);
                btnViewDocument.setEnabled(false);
            }

            docTableModel.fireTableDataChanged();
        }
    }

    private void updateContents() {
        if (docTableModel != null) {
            synchronized (docTableModel) {
                // clean table
                while (docTableModel.getRowCount() > 0) {
                    docTableModel.removeRow(0);
                }

                synchronized (LOADED_CONVERSATIONS) {
                    for (Conversation c : LOADED_CONVERSATIONS) {
                        // add rows as loaded documents
                        List<Object> dataRow = new ArrayList<>();
                        dataRow.add(c.getTitleText());
                        if (c.getLSA() != null) {
                            dataRow.add(c.getLSA().getPath());
                        } else {
                            dataRow.add("");
                        }
                        if (c.getLDA() != null) {
                            dataRow.add(c.getLDA().getPath());
                        } else {
                            dataRow.add("");
                        }
                        docTableModel.addRow(dataRow.toArray());
                    }

                    if (LOADED_CONVERSATIONS.size() > 0) {
                        btnRemoveDocument.setEnabled(true);
                        btnViewDocument.setEnabled(true);
                    } else {
                        btnRemoveDocument.setEnabled(false);
                        btnViewDocument.setEnabled(false);
                    }

                    docTableModel.fireTableDataChanged();
                }
            }
        }
    }

    private void newFilter() {
        List<RowFilter<ConversationManagementTableModel, Object>> rfs = new ArrayList<>(2);
        RowFilter<ConversationManagementTableModel, Object> rf;
        // If current expression doesn't parse, don't update.
        try {
            rf = RowFilter.regexFilter("(?i)" + queryArticleName, 0);
            rfs.add(rf);
        } catch (java.util.regex.PatternSyntaxException e) {
            return;
        }
        rf = RowFilter.andFilter(rfs);
        docSorter.setRowFilter(rf);
    }
}
