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
import java.util.logging.Logger;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JDesktopPane;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
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

import data.AbstractDocument;
import data.cscl.Conversation;
import data.document.Document;
import data.AbstractDocument.SaveType;
import java.io.IOException;
import java.util.EnumMap;
import java.util.Map;
import java.util.logging.Level;
import org.openide.util.Exceptions;
import services.semanticModels.SimilarityType;
import utils.localization.LocalizationUtils;
import view.models.document.DocumentManagementTableModel;
import view.widgets.ReaderBenchView;
import view.widgets.document.corpora.PaperSimilarityView;

public class DocumentProcessingView extends JInternalFrame {

    private static final long serialVersionUID = -8772215709851320157L;
    static final Logger LOGGER = Logger.getLogger("");

    private final JButton btnRemoveDocument;
    private final JButton btnAddDocument;
    private final JButton btnViewDocument;
    private final JButton btnAddSerializedDocument;
    private final JTable docTable;
    private final DocumentManagementTableModel docTableModel;
    private final TableRowSorter<DocumentManagementTableModel> docSorter;
    private final JScrollPane scrollPane;
    private final JDesktopPane desktopPane;
    private static File lastDirectory = null;

    private static final List<Document> LOADED_DOCUMENTS = new ArrayList<>();
    private CustomTextField articleTextField;
    private CustomTextField authorsTextField;
    private final JButton btnViewSimilarDocs;
    private String queryArticleName;
    private String queryAuthorName;

    public class DocumentProcessingTask extends SwingWorker<Void, Void> {

        private final String pathToDoc;
        private final String pathToLSA;
        private final String pathToLDA;
        private final boolean computeDialogism;
        private final boolean usePOSTagging;
        private final boolean isSerialized;
        private final boolean checkSer;

        public DocumentProcessingTask(String pathToDoc, String pathToLSA, String pathToLDA, boolean usePOSTagging,
                boolean computeDialogism, boolean isSerialized, boolean checkSer) {
            super();
            this.pathToDoc = pathToDoc;
            this.pathToLSA = pathToLSA;
            this.pathToLDA = pathToLDA;
            this.usePOSTagging = usePOSTagging;
            this.computeDialogism = computeDialogism;
            this.isSerialized = isSerialized;
            this.checkSer = checkSer;
        }

        public DocumentProcessingTask(String pathToDoc) {
            this(pathToDoc, null, null, false, false, true, false);
        }

        public AbstractDocument processDocument(String pathToIndividualFile) {
            Map<SimilarityType, String> modelPaths = new EnumMap<>(SimilarityType.class);
            modelPaths.put(SimilarityType.LSA, pathToLSA);
            modelPaths.put(SimilarityType.LDA, pathToLDA);
            return AbstractDocument.loadGenericDocument(pathToIndividualFile, modelPaths,
                    ReaderBenchView.RUNTIME_LANGUAGE, usePOSTagging, computeDialogism, null, null, true,
                    SaveType.SERIALIZED_AND_CSV_EXPORT);
        }

        public void addSingleDocument(String pathToIndividualFile) {
            AbstractDocument d = null;
            if (isSerialized) {
                try {
                    d = AbstractDocument.loadSerializedDocument(pathToIndividualFile);
                } catch (IOException | ClassNotFoundException ex) {
                    JOptionPane.showMessageDialog(desktopPane, "Error loading serialized file " + pathToIndividualFile + ". Please reprocess the file using the add document functionality.", "Error", JOptionPane.ERROR);
                    Exceptions.printStackTrace(ex);
                }
            } else if (AbstractDocument.checkTagsDocument(new File(pathToIndividualFile), "p")) {
                //check if file was already preprocessed
                if (checkSer) {
                    File ser = new File(pathToIndividualFile.replace(".xml", ".ser"));
                    if (ser.exists()) {
                        try {
                            d = AbstractDocument.loadSerializedDocument(ser.getAbsolutePath());
                        } catch (IOException | ClassNotFoundException ex) {
                            LOGGER.log(Level.INFO, "Obsolete ser file for {0}. Reprocessing file ...", pathToIndividualFile);
                            d = processDocument(pathToIndividualFile);
                        }
                    } else {
                        d = processDocument(pathToIndividualFile);
                    }
                } else {
                    d = processDocument(pathToIndividualFile);
                }
            }
            if (d == null) {
                JOptionPane.showMessageDialog(desktopPane, "File " + pathToIndividualFile + " does not have an appropriate document XML structure!", "Information", JOptionPane.INFORMATION_MESSAGE);
            } else if (d.getLanguage() == ReaderBenchView.RUNTIME_LANGUAGE) {
                addDocument((Document) d);
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
                    LOGGER.log(Level.SEVERE, "{0}: {1}", new Object[]{f.getName(), ex.getMessage()});
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
    public DocumentProcessingView() {
        super.setTitle("ReaderBench - " + LocalizationUtils.getTranslation("Document Processing"));
        super.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        super.setResizable(true);
        super.setClosable(true);
        super.setMaximizable(true);
        super.setIconifiable(true);
        super.setBounds(20, 20, 840, 450);
        desktopPane = new JDesktopPane();
        desktopPane.setBackground(Color.WHITE);

        queryAuthorName = "";
        queryArticleName = "";

        btnAddDocument = new JButton(LocalizationUtils.getTranslation("Add document(s)"));
        btnAddDocument.addActionListener((ActionEvent e) -> {
            try {
                JInternalFrame frame = new AddDocumentView(ReaderBenchView.RUNTIME_LANGUAGE, DocumentProcessingView.this);
                frame.setVisible(true);
                desktopPane.add(frame);
                frame.setSelected(true);
            } catch (Exception ex) {
                Exceptions.printStackTrace(ex);
            }
        });

        btnAddSerializedDocument = new JButton(LocalizationUtils.getTranslation("Add preprocessed document(s)"));
        btnAddSerializedDocument.addActionListener((ActionEvent e) -> {
            JFileChooser fc;
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
            int returnVal = fc.showOpenDialog(DocumentProcessingView.this);

            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File file = fc.getSelectedFile();
                lastDirectory = file.getParentFile();
                DocumentProcessingView.DocumentProcessingTask task = DocumentProcessingView.this.new DocumentProcessingTask(file.getPath());
                task.execute();
            }
        });

        docTableModel = new DocumentManagementTableModel();
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
                    if (row >= 0 && row < LOADED_DOCUMENTS.size()) {
                        int modelRow = target.convertRowIndexToModel(target.getSelectedRow());
                        AbstractDocument d = LOADED_DOCUMENTS.get(modelRow);
                        if (d instanceof Document) {
                            DocumentView view = new DocumentView((Document) d);
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
                LocalizationUtils.getTranslation("Specific document operations"), TitledBorder.LEFT, TitledBorder.TOP,
                null, null));

        JPanel panelSearch = new JPanel();
        panelSearch.setBackground(Color.WHITE);
        panelSearch.setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null), "Filter",
                TitledBorder.LEADING, TitledBorder.TOP, null, new Color(59, 59, 59)));

        GroupLayout gl_desktopPane = new GroupLayout(desktopPane);
        gl_desktopPane
                .setHorizontalGroup(
                        gl_desktopPane.createParallelGroup(Alignment.TRAILING).addGroup(Alignment.LEADING,
                        gl_desktopPane.createSequentialGroup().addContainerGap()
                        .addGroup(gl_desktopPane
                                .createParallelGroup(Alignment.LEADING).addComponent(scrollPane,
                                GroupLayout.DEFAULT_SIZE, 801, Short.MAX_VALUE)
                                .addGroup(gl_desktopPane.createSequentialGroup().addGroup(gl_desktopPane
                                        .createParallelGroup(Alignment.LEADING)
                                        .addComponent(panelSearch, GroupLayout.DEFAULT_SIZE, 801,
                                                Short.MAX_VALUE)
                                        .addGroup(gl_desktopPane.createSequentialGroup()))
                                        .addContainerGap())
                                .addGroup(gl_desktopPane.createSequentialGroup()
                                        .addComponent(panelSingleDoc, GroupLayout.DEFAULT_SIZE,
                                                GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addGap(11)))));
        gl_desktopPane.setVerticalGroup(gl_desktopPane.createParallelGroup(Alignment.LEADING).addGroup(gl_desktopPane
                .createSequentialGroup().addContainerGap()
                .addGroup(gl_desktopPane.createParallelGroup(Alignment.LEADING, false))
                .addPreferredGap(ComponentPlacement.RELATED)
                .addComponent(panelSearch, GroupLayout.PREFERRED_SIZE, 62, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(ComponentPlacement.RELATED)
                .addComponent(scrollPane, GroupLayout.DEFAULT_SIZE, 226, Short.MAX_VALUE)
                .addPreferredGap(ComponentPlacement.RELATED).addComponent(panelSingleDoc, GroupLayout.PREFERRED_SIZE,
                GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addContainerGap()));

        JSplitPane splitPane = new JSplitPane();
        splitPane.setBackground(Color.WHITE);
        panelSearch.add(splitPane);

        articleTextField = new CustomTextField(1);
        articleTextField.setPlaceholder("Insert Article Name");
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
                if (queryArticleName.equalsIgnoreCase("Insert Article Name")) {
                    queryArticleName = "";
                }
                newFilter();
            }
        });

        authorsTextField = new CustomTextField(1);
        authorsTextField.setPlaceholder("Insert Author Name");
        authorsTextField.setFont(new Font("SansSerif", Font.ITALIC, 13));
        authorsTextField.setPlaceholderForeground(Color.gray);
        authorsTextField.getDocument().addDocumentListener(new DocumentListener() {
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
                queryAuthorName = authorsTextField.getText();
                if (queryAuthorName.equalsIgnoreCase("Insert Author Name")) {
                    queryAuthorName = "";
                }
                newFilter();
            }
        });
        splitPane.setLeftComponent(articleTextField);
        articleTextField.setColumns(25);

        splitPane.setRightComponent(authorsTextField);
        authorsTextField.setColumns(25);

        btnViewDocument = new JButton(LocalizationUtils.getTranslation("View document"));
        btnViewDocument.setEnabled(false);
        btnViewDocument.addActionListener((ActionEvent e) -> {
            if (docTable.getSelectedRow() != -1) {
                int modelRow = docTable.convertRowIndexToModel(docTable.getSelectedRow());
                AbstractDocument d = LOADED_DOCUMENTS.get(modelRow);
                if (d instanceof Document) {
                    DocumentView view = new DocumentView((Document) d);
                    view.setVisible(true);
                }
            } else {
                JOptionPane.showMessageDialog(desktopPane, "Please select a document to be viewed!", "Information",
                        JOptionPane.INFORMATION_MESSAGE);
            }
        });

        btnViewSimilarDocs = new JButton(LocalizationUtils.getTranslation("View similar docs"));
        btnViewSimilarDocs.addActionListener((ActionEvent e) -> {
            if (docTable.getSelectedRow() != -1) {
                Document d = LOADED_DOCUMENTS.get(docTable.getSelectedRow());
                PaperSimilarityView view = new PaperSimilarityView(LOADED_DOCUMENTS, (Document) d);
                view.setVisible(true);
            }
        });
        btnViewSimilarDocs.setEnabled(false);

        btnRemoveDocument = new JButton(LocalizationUtils.getTranslation("Remove document"));
        btnRemoveDocument.setEnabled(false);
        GroupLayout gl_panelSingleDoc = new GroupLayout(panelSingleDoc);
        gl_panelSingleDoc.setHorizontalGroup(gl_panelSingleDoc.createParallelGroup(Alignment.LEADING)
                .addGroup(gl_panelSingleDoc.createSequentialGroup().addGap(2).addComponent(btnViewDocument).addGap(5)
                        .addComponent(btnViewSimilarDocs).addPreferredGap(ComponentPlacement.RELATED)
                        .addComponent(btnAddDocument).addPreferredGap(ComponentPlacement.RELATED)
                        .addComponent(btnAddSerializedDocument).addPreferredGap(ComponentPlacement.RELATED)
                        .addComponent(btnRemoveDocument).addGap(26)));
        gl_panelSingleDoc
                .setVerticalGroup(gl_panelSingleDoc.createParallelGroup(Alignment.LEADING)
                        .addGroup(gl_panelSingleDoc.createSequentialGroup().addGap(5)
                                .addGroup(gl_panelSingleDoc.createParallelGroup(Alignment.BASELINE)
                                        .addComponent(btnViewDocument).addComponent(btnViewSimilarDocs)
                                        .addComponent(btnAddDocument).addComponent(btnAddSerializedDocument)
                                        .addComponent(btnRemoveDocument))));
        panelSingleDoc.setLayout(gl_panelSingleDoc);
        btnRemoveDocument.addActionListener((ActionEvent e) -> {
            if (docTable.getSelectedRow() != -1) {
                int[] rows = docTable.getSelectedRows();
                for (int i = 0; i < rows.length; i++) {
                    int modelRow = docTable.convertRowIndexToModel(rows[i] - i);
                    Document toRemove = LOADED_DOCUMENTS.get(modelRow);
                    LOADED_DOCUMENTS.remove(toRemove);
                    docTableModel.removeRow(modelRow);
                }
            } else {
                JOptionPane.showMessageDialog(desktopPane, "Please load at least an appropriate document!", "Information", JOptionPane.INFORMATION_MESSAGE);
            }
        });
        desktopPane.setLayout(gl_desktopPane);

        super.setContentPane(desktopPane);
        updateContents();
    }

    public static List<Document> getLoadedDocuments() {
        return LOADED_DOCUMENTS;
    }

    private String getStringFromList(List<String> l) {
        String out = "";
        for (String str : l) {
            out += (out.length() > 0) ? " " : "";
            out += str;
        }
        return out;
    }

    private void addDocument(Document d) {
        if (docTableModel != null) {
            synchronized (LOADED_DOCUMENTS) {
                //remove already existent copy
                for (int i = 0; i < LOADED_DOCUMENTS.size(); i++) {
                    int modelRow = docTable.convertRowIndexToModel(i);
                    Document toRemove = LOADED_DOCUMENTS.get(modelRow);
                    if (toRemove.getPath().equals(d.getPath()) && toRemove.getSemanticModel(SimilarityType.LSA).getPath().equals(d.getSemanticModel(SimilarityType.LSA).getPath()) && toRemove.getSemanticModel(SimilarityType.LDA).getPath().equals(d.getSemanticModel(SimilarityType.LDA).getPath())) {
                        LOADED_DOCUMENTS.remove(toRemove);
                        docTableModel.removeRow(modelRow);
                    }
                }

                // add row with loaded document
                List<Object> dataRow = new ArrayList<>();

                dataRow.add(d.getTitleText());
                dataRow.add(getStringFromList(((Document) d).getAuthors()));
                if (d.getSemanticModel(SimilarityType.LSA) != null) {
                    dataRow.add(d.getSemanticModel(SimilarityType.LSA).getPath());
                } else {
                    dataRow.add("");
                }
                if (d.getSemanticModel(SimilarityType.LDA) != null) {
                    dataRow.add(d.getSemanticModel(SimilarityType.LDA).getPath());
                } else {
                    dataRow.add("");
                }

                docTableModel.addRow(dataRow.toArray());
                LOADED_DOCUMENTS.add(d);
            }

            if (LOADED_DOCUMENTS.size() > 0) {
                btnRemoveDocument.setEnabled(true);
                btnViewDocument.setEnabled(true);
                btnViewSimilarDocs.setEnabled(true);
            } else {
                btnRemoveDocument.setEnabled(false);
                btnViewDocument.setEnabled(false);
                btnViewSimilarDocs.setEnabled(false);
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

                synchronized (LOADED_DOCUMENTS) {
                    for (AbstractDocument d : LOADED_DOCUMENTS) {
                        // add rows as loaded documents
                        List<Object> dataRow = new ArrayList<>();

                        if (d instanceof Document) {
                            String docTitle = d.getTitleText();
                            String authors = getStringFromList(((Document) d).getAuthors());

                            if (!docTitle.toLowerCase().contains(queryArticleName.toLowerCase())) {
                                continue;
                            }
                            if (!authors.toLowerCase().contains(queryAuthorName.toLowerCase())) {
                                continue;
                            }
                        } else {
                            continue;
                        }

                        dataRow.add(d.getTitleText());
                        if (d instanceof Document) {
                            dataRow.add(getStringFromList(((Document) d).getAuthors()));
                        } else {
                            dataRow.add("");
                        }
                        if (d.getSemanticModel(SimilarityType.LSA) != null) {
                            dataRow.add(d.getSemanticModel(SimilarityType.LSA).getPath());
                        } else {
                            dataRow.add("");
                        }
                        if (d.getSemanticModel(SimilarityType.LDA) != null) {
                            dataRow.add(d.getSemanticModel(SimilarityType.LDA).getPath());
                        } else {
                            dataRow.add("");
                        }
                        if (d instanceof Conversation) {
                            dataRow.add(true);
                        } else {
                            dataRow.add(false);
                        }
                        docTableModel.addRow(dataRow.toArray());
                    }

                    if (LOADED_DOCUMENTS.size() > 0) {
                        btnRemoveDocument.setEnabled(true);
                        btnViewDocument.setEnabled(true);
                        btnViewSimilarDocs.setEnabled(true);
                    } else {
                        btnRemoveDocument.setEnabled(false);
                        btnViewDocument.setEnabled(false);
                        btnViewSimilarDocs.setEnabled(false);
                    }

                    docTableModel.fireTableDataChanged();
                }
            }
        }
    }

    private void newFilter() {
        List<RowFilter<DocumentManagementTableModel, Object>> rfs = new ArrayList<>(2);
        RowFilter<DocumentManagementTableModel, Object> rf;
        // If current expression doesn't parse, don't update.
        try {
            rf = RowFilter.regexFilter("(?i)" + queryArticleName, 0);
            rfs.add(rf);
            rf = RowFilter.regexFilter("(?i)" + queryAuthorName, 1);
            rfs.add(rf);
        } catch (java.util.regex.PatternSyntaxException e) {
            return;
        }
        rf = RowFilter.andFilter(rfs);
        docSorter.setRowFilter(rf);
    }
}
