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

import data.AbstractDocument;
import java.awt.Color;
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

import utils.LocalizationUtils;
import view.models.verbalization.VerbalisationManagementTableModel;
import view.widgets.ReaderBenchView;
import view.widgets.document.DocumentProcessingView;
import data.document.Document;
import data.document.Summary;
import java.beans.PropertyVetoException;
import java.io.IOException;
import java.util.ArrayList;
import org.openide.util.Exceptions;
import services.semanticModels.SimilarityType;

public class SummaryProcessingView extends JInternalFrame {

    private static final long serialVersionUID = -8772215709851320157L;
    static final Logger LOGGER = Logger.getLogger("");

    private final JDesktopPane desktopPane;
    private final JTable summariesTable;
    private final JButton btnRemoveSummary;
    private final JButton btnAddSummary;
    private final JButton btnViewSummary;
    private final JButton btnAddSerializedSummary;
    private final DefaultTableModel summariesTableModel;
    private static File lastDirectory = null;

    private static final List<Summary> LOADED_SUMMARIES = new ArrayList<>();

    public class EssayProcessingTask extends SwingWorker<Void, Void> {

        private final String pathToDoc;
        private final Document referredDoc;
        private final boolean usePOSTagging;
        private final boolean isSerialized;

        public EssayProcessingTask(String pathToDoc, Document d, boolean usePOSTagging, boolean isSerialized) {
            super();
            this.pathToDoc = pathToDoc;
            this.referredDoc = d;
            this.usePOSTagging = usePOSTagging;
            this.isSerialized = isSerialized;
        }

        public void addSingleEssay(String pathToIndividualFile) {
            Summary e = null;
            if (isSerialized) {
                try {
                    e = (Summary) Summary.loadSerializedDocument(pathToIndividualFile);
                } catch (IOException | ClassNotFoundException ex) {
                    Exceptions.printStackTrace(ex);
                }
            } else {
                e = Summary.loadSummary(pathToIndividualFile, referredDoc, usePOSTagging);
                if (e != null) {
                    e.computeAll(usePOSTagging, false);
                    e.save(AbstractDocument.SaveType.SERIALIZED);
                }
            }

            if (e != null) {
                if (e.getLanguage() == ReaderBenchView.RUNTIME_LANGUAGE) {
                    addSummary(e);
                } else {
                    JOptionPane.showMessageDialog(desktopPane, LocalizationUtils.getGeneric("msgIncorrectLanguage") + "!",
                            "Information", JOptionPane.INFORMATION_MESSAGE);
                }
            }
        }

        @Override
        public Void doInBackground() {
            btnAddSummary.setEnabled(false);
            btnAddSerializedSummary.setEnabled(false);

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
                    addSingleEssay(f.getPath());
                } catch (Exception ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
            return null;
        }

        @Override
        public void done() {
            Toolkit.getDefaultToolkit().beep();
            btnAddSummary.setEnabled(true);
            btnAddSerializedSummary.setEnabled(true);
        }
    }

    /**
     * Create the frame.
     */
    public SummaryProcessingView() {
        super.setTitle("ReaderBench - " + LocalizationUtils.getTitle(this.getClass()));
        super.setResizable(true);
        super.setClosable(true);
        super.setMaximizable(true);
        super.setIconifiable(true);
        super.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        super.setBounds(100, 100, 900, 450);

        desktopPane = new JDesktopPane();
        desktopPane.setBackground(Color.WHITE);
        super.setContentPane(desktopPane);

        summariesTableModel = new VerbalisationManagementTableModel();
        summariesTable = new JTable(summariesTableModel);
        summariesTable.setFillsViewportHeight(true);

        summariesTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                if (event.getClickCount() == 2) {
                    JTable target = (JTable) event.getSource();
                    int row = target.getSelectedRow();
                    if (row >= 0 && row < LOADED_SUMMARIES.size()) {
                        Summary summary = LOADED_SUMMARIES.get(summariesTable.getSelectedRow());
                        SummaryView view = new SummaryView(summary);
                        view.setVisible(true);
                    }
                }
            }
        });

        btnAddSummary = new JButton(LocalizationUtils.getLocalizedString(this.getClass(), "btnAddSummary"));
        btnAddSummary.setEnabled(true);
        btnAddSummary.addActionListener((ActionEvent e) -> {
            if (DocumentProcessingView.getLoadedDocuments().size() > 0) {
                try {
                    JInternalFrame frame = new AddSummaryView(SummaryProcessingView.this);
                    frame.setVisible(true);
                    desktopPane.add(frame);
                    frame.setSelected(true);
                } catch (PropertyVetoException ex) {
                    Exceptions.printStackTrace(ex);
                }
            } else {
                JOptionPane.showMessageDialog(desktopPane, LocalizationUtils.getLocalizedString(this.getClass(), "msgLoadedDoc"), "Information", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        btnRemoveSummary = new JButton(LocalizationUtils.getLocalizedString(this.getClass(), "btnRemoveSummary"));
        btnRemoveSummary.setEnabled(false);
        btnRemoveSummary.addActionListener((ActionEvent e) -> {
            if (summariesTable.getSelectedRow() != -1) {
                int[] rows = summariesTable.getSelectedRows();
                for (int i = 0; i < rows.length; i++) {
                    int modelRow = rows[i] - i;
                    Summary toRemove = LOADED_SUMMARIES.get(modelRow);
                    LOADED_SUMMARIES.remove(toRemove);
                    summariesTableModel.removeRow(modelRow);
                }
            } else {
                JOptionPane.showMessageDialog(SummaryProcessingView.this, LocalizationUtils.getGeneric("msgSelectDoc"), "Information", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        scrollPane.setViewportView(summariesTable);

        btnAddSerializedSummary = new JButton(LocalizationUtils.getLocalizedString(this.getClass(), "btnAddSerializedSummary"));
        btnAddSerializedSummary.addActionListener((ActionEvent e) -> {
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
                    return LocalizationUtils.getLocalizedString(this.getClass(), "msgSerFile");
                }
            });
            int returnVal = fc.showOpenDialog(SummaryProcessingView.this);

            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File file = fc.getSelectedFile();
                lastDirectory = file.getParentFile();
                EssayProcessingTask task = SummaryProcessingView.this.new EssayProcessingTask(file.getPath(), null, false, true);
                task.execute();
            }
        });

        btnViewSummary = new JButton(LocalizationUtils.getGeneric("msgViewDoc"));
        btnViewSummary.setEnabled(false);
        btnViewSummary.addActionListener((ActionEvent e) -> {
            if (summariesTable.getSelectedRow() != -1) {
                Summary s = LOADED_SUMMARIES.get(summariesTable.getSelectedRow());
                SummaryView view = new SummaryView(s);
                view.setVisible(true);
            } else {
                JOptionPane.showMessageDialog(desktopPane, LocalizationUtils.getLocalizedString(this.getClass(), "msgViewSummary"), "Information", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        GroupLayout gl_desktopPane = new GroupLayout(desktopPane);
        gl_desktopPane
                .setHorizontalGroup(
                        gl_desktopPane
                                .createParallelGroup(
                                        Alignment.TRAILING)
                                .addGroup(
                                        gl_desktopPane.createSequentialGroup().addContainerGap()
                                                .addGroup(
                                                        gl_desktopPane.createParallelGroup(Alignment.TRAILING)
                                                                .addComponent(scrollPane, GroupLayout.DEFAULT_SIZE, 876,
                                                                        Short.MAX_VALUE)
                                                                .addGroup(
                                                                        gl_desktopPane.createSequentialGroup()
                                                                                .addComponent(btnViewSummary)
                                                                                .addPreferredGap(
                                                                                        ComponentPlacement.RELATED)
                                                                                .addComponent(btnAddSummary)
                                                                                .addPreferredGap(ComponentPlacement.RELATED)
                                                                                .addComponent(btnAddSerializedSummary)
                                                                                .addPreferredGap(ComponentPlacement.RELATED)
                                                                                .addComponent(btnRemoveSummary)))
                                                .addContainerGap()));
        gl_desktopPane.setVerticalGroup(gl_desktopPane.createParallelGroup(Alignment.TRAILING).addGroup(gl_desktopPane
                .createSequentialGroup().addContainerGap()
                .addComponent(scrollPane, GroupLayout.DEFAULT_SIZE, 374, Short.MAX_VALUE)
                .addPreferredGap(ComponentPlacement.RELATED)
                .addGroup(gl_desktopPane.createParallelGroup(Alignment.BASELINE)
                        .addComponent(btnRemoveSummary)
                        .addComponent(btnAddSerializedSummary).addComponent(btnAddSummary).addComponent(btnViewSummary))
                .addContainerGap()));
        desktopPane.setLayout(gl_desktopPane);

        updateContents();
    }

    public static synchronized List<Summary> getLoadedSummaries() {
        return LOADED_SUMMARIES;
    }

    public void addSummary(Summary e) {
        if (summariesTableModel != null) {
            synchronized (summariesTableModel) {
                synchronized (LOADED_SUMMARIES) {
                    //remove already existent copy
                    for (int i = 0; i < LOADED_SUMMARIES.size(); i++) {
                        int modelRow = summariesTable.convertRowIndexToModel(i);
                        Summary toRemove = LOADED_SUMMARIES.get(modelRow);
                        if (toRemove.getPath().equals(e.getPath()) && toRemove.getSemanticModel(SimilarityType.LSA).getPath().equals(e.getSemanticModel(SimilarityType.LSA).getPath())
                                && toRemove.getSemanticModel(SimilarityType.LDA).getPath().equals(e.getSemanticModel(SimilarityType.LDA).getPath())
                                && toRemove.getSemanticModel(SimilarityType.WORD2VEC).getPath().equals(e.getSemanticModel(SimilarityType.WORD2VEC).getPath())) {
                            LOADED_SUMMARIES.remove(toRemove);
                            summariesTableModel.removeRow(modelRow);
                        }
                    }

                    // add row with loaded document
                    List<Object> dataRow = new ArrayList<>();

                    String authors = "";
                    for (String author : e.getAuthors()) {
                        authors += author + ", ";
                    }
                    if (authors.length() >= 2) {
                        authors = authors.substring(0, authors.length() - 2);
                    }
                    dataRow.add(authors);
                    dataRow.add(e.getReferredDoc().getTitleText());
                    dataRow.add(e.getReferredDoc().getSemanticModel(SimilarityType.LSA).getPath());
                    dataRow.add(e.getReferredDoc().getSemanticModel(SimilarityType.LDA).getPath());
                    dataRow.add(e.getReferredDoc().getSemanticModel(SimilarityType.WORD2VEC).getPath());
                    summariesTableModel.addRow(dataRow.toArray());
                    LOADED_SUMMARIES.add(e);
                }
                if (LOADED_SUMMARIES.size() > 0) {
                    btnRemoveSummary.setEnabled(true);
                    btnViewSummary.setEnabled(true);
                } else {
                    btnRemoveSummary.setEnabled(false);
                    btnViewSummary.setEnabled(false);
                }
            }
        }
    }

    private void updateContents() {
        if (summariesTableModel != null) {
            summariesTable.clearSelection();
            // clean table
            while (summariesTableModel.getRowCount() > 0) {
                summariesTableModel.removeRow(0);
            }

            for (Summary e : LOADED_SUMMARIES) {
                // add rows as loaded documents
                List<Object> dataRow = new ArrayList<>();

                String authors = "";
                for (String author : e.getAuthors()) {
                    authors += author + ", ";
                }
                authors = authors.substring(0, authors.length() - 2);
                dataRow.add(authors);
                dataRow.add(e.getReferredDoc().getTitleText());
                dataRow.add(e.getReferredDoc().getSemanticModel(SimilarityType.LSA).getPath());
                dataRow.add(e.getReferredDoc().getSemanticModel(SimilarityType.LDA).getPath());
                dataRow.add(e.getReferredDoc().getSemanticModel(SimilarityType.WORD2VEC).getPath());
                summariesTableModel.addRow(dataRow.toArray());
            }

            if (LOADED_SUMMARIES.size() > 0) {
                btnRemoveSummary.setEnabled(true);
                btnViewSummary.setEnabled(true);
            } else {
                btnRemoveSummary.setEnabled(false);
                btnViewSummary.setEnabled(false);
            }
        }
    }
}
