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

import data.AbstractDocument;
import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
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
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingWorker;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.DefaultTableModel;

import data.document.Document;
import data.document.Metacognition;
import java.beans.PropertyVetoException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import org.openide.util.Exceptions;
import services.semanticModels.SimilarityType;
import utils.LocalizationUtils;
import view.models.verbalization.VerbalisationManagementTableModel;
import view.widgets.ReaderBenchView;
import view.widgets.document.DocumentProcessingView;

public class VerbalizationProcessingView extends JInternalFrame {

    private static final long serialVersionUID = -8772215709851320157L;
    static final Logger LOGGER = Logger.getLogger("");

    private final JDesktopPane desktopPane;
    private final JTable verbalizationsTable;
    private final DefaultTableModel verbalizationsTableModel;
    private final JButton btnRemoveVerbalization;
    private final JButton btnAddVerbalization;
    private final JButton btnViewVerbalization;
    private final JButton btnViewCummulativeStatistics;
    private final JButton btnAddSerializedVerbalization;
    private static File lastDirectory;

    private final static List<Metacognition> LOADED_VERBALIZATIONS = new ArrayList<>();

    public class VerbalizationProcessingTask extends SwingWorker<Void, Void> {

        private final String pathToDoc;
        private final Document referredDoc;
        private final boolean usePOSTagging;
        private boolean isSerialized;
        private final boolean chckSer;

        public VerbalizationProcessingTask(String pathToDoc, Document d, boolean usePOSTagging, boolean isSerialized, boolean chckSer) {
            super();
            this.pathToDoc = pathToDoc;
            this.referredDoc = d;
            this.usePOSTagging = usePOSTagging;
            this.isSerialized = isSerialized;
            this.chckSer = chckSer;
        }

        public void addSingleVerbalisation(String pathToIndividualFile) {
            Metacognition v = null;
            if (isSerialized) {
                try {
                    v = (Metacognition) Metacognition.loadSerializedDocument(pathToIndividualFile);
                } catch (IOException | ClassNotFoundException ex) {
                    Exceptions.printStackTrace(ex);
                }
            } else {
                v = Metacognition.loadVerbalization(pathToIndividualFile, referredDoc, usePOSTagging);
                v.computeAll(true, false);
                v.save(AbstractDocument.SaveType.SERIALIZED);
            }

            if (v != null) {
                if (v.getLanguage() == ReaderBenchView.RUNTIME_LANGUAGE) {
                    addVerbalization(v);
                } else {
                    JOptionPane.showMessageDialog(desktopPane, LocalizationUtils.getGeneric("msgIncorrectLanguage") + "!", "Information", JOptionPane.INFORMATION_MESSAGE);
                }
            }
        }

        @Override
        public Void doInBackground() {
            btnAddVerbalization.setEnabled(false);
            btnAddSerializedVerbalization.setEnabled(false);

            File file = new File(pathToDoc);
            File[] files = {file};
            if (isSerialized) {
                if (file.isDirectory()) {
                    files = file.listFiles((File dir, String name1) -> name1.endsWith(".ser"));
                }
            } else if (chckSer) {
                if (file.isDirectory()) {
                    boolean found = false;
                    int size = 0;
                    files = new File[file.listFiles().length];
                    File[] XMLfiles = file.listFiles((File dir, String name1) -> name1.endsWith(".xml"));
                    File[] SERfiles = file.listFiles((File dir, String name1) -> name1.endsWith(".ser"));
                    for (File i : XMLfiles) {
                        for (File j : SERfiles) {
                            if (i.getName().replace(".xml", "").equals(j.getName().replace(".ser", ""))) {
                                files[size] = j;
                                size++;
                                found = true;
                                break;
                            }
                        }
                        if (found == false) {
                            files[size] = i;
                            size++;
                        }
                    }
                } else {
                    File[] parent = file.getParentFile().listFiles();
                    for (File i : parent) {
                        if (i.getName().equals(file.getName().replace(".xml", ".ser"))) {
                            files[0] = i;
                        }
                    }
                }
            } else if (file.isDirectory()) {
                // process each individual xml file
                files = file.listFiles((File dir, String name1) -> name1.endsWith(".xml"));
            }
            for (File f : files) {
                try {
                    isSerialized = f.getName().contains(".ser");
                    addSingleVerbalisation(f.getPath());
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
            btnAddVerbalization.setEnabled(true);
            btnAddSerializedVerbalization.setEnabled(true);
        }
    }

    /**
     * Create the frame.
     */
    public VerbalizationProcessingView() {
        super.setTitle("ReaderBench - " + LocalizationUtils.getTitle(this.getClass()));
        super.setResizable(true);
        super.setClosable(true);
        super.setMaximizable(true);
        super.setIconifiable(true);
        super.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        super.setBounds(100, 100, 780, 450);

        desktopPane = new JDesktopPane();
        desktopPane.setBackground(Color.WHITE);
        super.setContentPane(desktopPane);

        verbalizationsTableModel = new VerbalisationManagementTableModel();
        verbalizationsTable = new JTable(verbalizationsTableModel);
        verbalizationsTable.setFillsViewportHeight(true);

        verbalizationsTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                if (event.getClickCount() == 2) {
                    JTable target = (JTable) event.getSource();
                    int row = target.getSelectedRow();
                    if (row >= 0 && row < LOADED_VERBALIZATIONS.size()) {
                        Metacognition v = LOADED_VERBALIZATIONS.get(row);
                        VerbalizationView view = new VerbalizationView(v);
                        view.setVisible(true);
                    }
                }
            }
        });

        btnAddVerbalization = new JButton(LocalizationUtils.getLocalizedString(this.getClass(), "btnAddVerbalization"));
        btnAddVerbalization.setEnabled(true);
        btnAddVerbalization.addActionListener((ActionEvent e) -> {
            if (DocumentProcessingView.getLoadedDocuments().size() > 0) {
                try {
                    JInternalFrame frame = new AddVerbalizationView(VerbalizationProcessingView.this);
                    frame.setVisible(true);
                    desktopPane.add(frame);
                    frame.setSelected(true);
                } catch (PropertyVetoException ex) {
                    Exceptions.printStackTrace(ex);
                }
            } else {
                JOptionPane.showMessageDialog(desktopPane,
                        LocalizationUtils.getLocalizedString(this.getClass(), "msgLoadedDoc") + "!",
                        "Information", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        btnRemoveVerbalization = new JButton(LocalizationUtils.getLocalizedString(this.getClass(), "btnRemoveVerbalization"));
        btnRemoveVerbalization.setEnabled(false);
        btnRemoveVerbalization.addActionListener((ActionEvent e) -> {
            if (verbalizationsTable.getSelectedRow() != -1) {
                int[] rows = verbalizationsTable.getSelectedRows();
                for (int i = 0; i < rows.length; i++) {
                    int modelRow = rows[i] - i;
                    Metacognition toRemove = LOADED_VERBALIZATIONS.get(modelRow);
                    LOADED_VERBALIZATIONS.remove(toRemove);
                    verbalizationsTableModel.removeRow(modelRow);
                }
            } else {
                JOptionPane.showMessageDialog(VerbalizationProcessingView.this,
                        LocalizationUtils.getGeneric("msgSelectDoc") + "!", "Information",
                        JOptionPane.INFORMATION_MESSAGE);
            }
        });

        btnViewVerbalization = new JButton(LocalizationUtils.getLocalizedString(this.getClass(), "btnViewVerbalization"));
        btnViewVerbalization.setEnabled(false);
        btnViewVerbalization.addActionListener((ActionEvent e) -> {
            if (verbalizationsTable.getSelectedRow() != -1) {
                Metacognition v = LOADED_VERBALIZATIONS.get(verbalizationsTable.getSelectedRow());
                VerbalizationView view = new VerbalizationView(v);
                view.setVisible(true);
            } else {
                JOptionPane.showMessageDialog(desktopPane, LocalizationUtils.getGeneric("msgViewDoc"), "Information", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setViewportView(verbalizationsTable);

        btnAddSerializedVerbalization = new JButton(LocalizationUtils.getLocalizedString(this.getClass(), "btnAddSerializedVerbalization"));
        btnAddSerializedVerbalization.addActionListener((ActionEvent e) -> {
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
                    return LocalizationUtils.getGeneric("msgSerFile");
                }
            });
            int returnVal = fc.showOpenDialog(VerbalizationProcessingView.this);

            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File file = fc.getSelectedFile();
                lastDirectory = file.getParentFile();
                VerbalizationProcessingTask task = VerbalizationProcessingView.this.new VerbalizationProcessingTask(
                        file.getPath(), null, false, true, false);
                task.execute();
            }
        });

        btnViewCummulativeStatistics = new JButton(LocalizationUtils.getLocalizedString(this.getClass(), "btnViewCummulativeStatistics"));
        btnViewCummulativeStatistics.addActionListener((ActionEvent e) -> {
            EventQueue.invokeLater(() -> {
                VerbalizationsCumulativeView view = new VerbalizationsCumulativeView(LOADED_VERBALIZATIONS);
                view.setVisible(true);
            });
        });
        btnViewCummulativeStatistics.setEnabled(false);

        GroupLayout gl_desktopPane = new GroupLayout(desktopPane);
        gl_desktopPane.setHorizontalGroup(gl_desktopPane.createParallelGroup(Alignment.TRAILING)
                .addGroup(gl_desktopPane.createSequentialGroup().addContainerGap()
                        .addGroup(gl_desktopPane.createParallelGroup(Alignment.TRAILING)
                                .addComponent(scrollPane, GroupLayout.DEFAULT_SIZE, 726, Short.MAX_VALUE)
                                .addGroup(gl_desktopPane.createSequentialGroup()
                                        .addPreferredGap(ComponentPlacement.RELATED, 33, Short.MAX_VALUE)
                                        .addComponent(btnViewVerbalization, GroupLayout.PREFERRED_SIZE, 142,
                                                GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(ComponentPlacement.RELATED)
                                        .addGroup(gl_desktopPane.createParallelGroup(Alignment.TRAILING, false)
                                                .addComponent(btnAddVerbalization, Alignment.LEADING, GroupLayout.DEFAULT_SIZE,
                                                        GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                        .addPreferredGap(ComponentPlacement.RELATED)
                                        .addGroup(gl_desktopPane.createParallelGroup(Alignment.TRAILING, false)
                                                .addComponent(btnViewCummulativeStatistics, GroupLayout.DEFAULT_SIZE,
                                                        GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                .addComponent(btnAddSerializedVerbalization, GroupLayout.DEFAULT_SIZE,
                                                        GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                        .addPreferredGap(ComponentPlacement.RELATED)
                                        .addGroup(gl_desktopPane.createParallelGroup(Alignment.LEADING, false)
                                                .addComponent(btnRemoveVerbalization, GroupLayout.PREFERRED_SIZE, 184,
                                                        GroupLayout.PREFERRED_SIZE))))
                        .addContainerGap()));
        gl_desktopPane.setVerticalGroup(gl_desktopPane.createParallelGroup(Alignment.LEADING)
                .addGroup(gl_desktopPane.createSequentialGroup().addContainerGap()
                        .addComponent(scrollPane, GroupLayout.DEFAULT_SIZE, 339, Short.MAX_VALUE)
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addGroup(gl_desktopPane.createParallelGroup(Alignment.BASELINE)
                                .addComponent(btnRemoveVerbalization, GroupLayout.PREFERRED_SIZE, 27,
                                        GroupLayout.PREFERRED_SIZE)
                                .addComponent(btnAddSerializedVerbalization)
                                .addComponent(btnAddVerbalization, GroupLayout.PREFERRED_SIZE, 27,
                                        GroupLayout.PREFERRED_SIZE)
                                .addComponent(btnViewVerbalization))
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addGroup(
                                gl_desktopPane.createParallelGroup(Alignment.BASELINE)
                                        .addComponent(btnViewCummulativeStatistics))
                        .addGap(8)));
        desktopPane.setLayout(gl_desktopPane);

        updateContents();
    }

    public static synchronized List<Metacognition> getLoadedVervalizations() {
        return LOADED_VERBALIZATIONS;
    }

    private void updateButtons(boolean isEnabled) {
        btnRemoveVerbalization.setEnabled(isEnabled);
        btnViewVerbalization.setEnabled(isEnabled);
        btnViewCummulativeStatistics.setEnabled(isEnabled);
    }

    private void addVerbalization(Metacognition v) {
        if (verbalizationsTableModel != null) {
            synchronized (verbalizationsTableModel) {
                synchronized (LOADED_VERBALIZATIONS) {
                    //remove already existent copy
                    for (int i = 0; i < LOADED_VERBALIZATIONS.size(); i++) {
                        int modelRow = verbalizationsTable.convertRowIndexToModel(i);
                        Metacognition toRemove = LOADED_VERBALIZATIONS.get(modelRow);
                        if (toRemove.getPath().equals(v.getPath()) && toRemove.getSemanticModel(SimilarityType.LSA).getPath().equals(v.getSemanticModel(SimilarityType.LSA).getPath())
                                && toRemove.getSemanticModel(SimilarityType.LDA).getPath().equals(v.getSemanticModel(SimilarityType.LDA).getPath())
                                && toRemove.getSemanticModel(SimilarityType.WORD2VEC).getPath().equals(v.getSemanticModel(SimilarityType.WORD2VEC).getPath())) {
                            LOADED_VERBALIZATIONS.remove(toRemove);
                            verbalizationsTableModel.removeRow(modelRow);
                        }
                    }
                    // add row with loaded document
                    List<Object> dataRow = new ArrayList<>();

                    String authors = "";
                    authors = v.getAuthors().stream().map((author) -> author + ", ").reduce(authors, String::concat);
                    authors = authors.substring(0, authors.length() - 2);
                    dataRow.add(authors);
                    dataRow.add(v.getReferredDoc().getTitleText());
                    dataRow.add(v.getReferredDoc().getSemanticModel(SimilarityType.LSA).getPath());
                    dataRow.add(v.getReferredDoc().getSemanticModel(SimilarityType.LDA).getPath());
                    dataRow.add(v.getReferredDoc().getSemanticModel(SimilarityType.WORD2VEC).getPath());
                    verbalizationsTableModel.addRow(dataRow.toArray());
                    LOADED_VERBALIZATIONS.add(v);
                }
                if (LOADED_VERBALIZATIONS.size() > 0) {
                    updateButtons(true);
                } else {
                    updateButtons(false);
                }
            }
        }
    }

    private void updateContents() {
        if (verbalizationsTableModel != null) {
            verbalizationsTable.clearSelection();
            // clean table
            while (verbalizationsTableModel.getRowCount() > 0) {
                verbalizationsTableModel.removeRow(0);
            }

            for (Metacognition v : LOADED_VERBALIZATIONS) {
                // add rows as loaded documents
                List<Object> dataRow = new ArrayList<>();

                String authors = "";
                for (String author : v.getAuthors()) {
                    authors += author + ", ";
                }
                authors = authors.substring(0, authors.length() - 2);
                dataRow.add(authors);
                dataRow.add(v.getReferredDoc().getTitleText());
                dataRow.add(v.getReferredDoc().getSemanticModel(SimilarityType.LSA).getPath());
                dataRow.add(v.getReferredDoc().getSemanticModel(SimilarityType.LDA).getPath());
                dataRow.add(v.getReferredDoc().getSemanticModel(SimilarityType.WORD2VEC).getPath());
                verbalizationsTableModel.addRow(dataRow.toArray());
            }

            if (LOADED_VERBALIZATIONS.size() > 0) {
                updateButtons(true);
            } else {
                updateButtons(false);
            }
        }
    }
}
