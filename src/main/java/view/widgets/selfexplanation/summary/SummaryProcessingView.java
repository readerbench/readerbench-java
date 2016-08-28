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
import java.awt.Graphics2D;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FilenameFilter;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDesktopPane;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.Painter;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingWorker;
import javax.swing.UIDefaults;
import javax.swing.UIManager;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.DefaultTableModel;

import org.apache.log4j.Logger;

import utils.localization.LocalizationUtils;
import view.models.verbalization.VerbalisationManagementTableModel;
import view.widgets.ReaderBenchView;
import view.widgets.complexity.ComplexityIndicesView;
import view.widgets.document.DocumentProcessingView;
import data.document.Document;
import data.document.Summary;

public class SummaryProcessingView extends JInternalFrame {

    private static final long serialVersionUID = -8772215709851320157L;
    static Logger logger = Logger.getLogger(SummaryProcessingView.class);

    private JDesktopPane desktopPane;

    private JTable summariesTable;
    private JButton btnRemoveSummary = null;
    private JButton btnAddSummary = null;
    private JButton btnViewSummary = null;
    private DefaultTableModel summariesTableModel = null;
    private static File lastDirectory = null;

    private static List<Summary> loadedSummaries = Collections.synchronizedList(new LinkedList<Summary>());
    private JButton btnAddSerializedSummary;

    public class EssayProcessingTask extends SwingWorker<Void, Void> {

        private String pathToDoc;
        private Document referredDoc;
        private boolean usePOSTagging;
        private boolean isSerialized;

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
                e = (Summary) Summary.loadSerializedDocument(pathToIndividualFile);
            } else {
                e = Summary.loadEssay(pathToIndividualFile, referredDoc, usePOSTagging, true);
                if (e != null) {
                    e.computeAll(usePOSTagging, true);
                }
            }

            if (e != null) {
                if (ReaderBenchView.RUNTIME_LANGUAGE == null) {
                    ReaderBenchView.RUNTIME_LANGUAGE = e.getLanguage();
                    ComplexityIndicesView.updateSelectedIndices(ReaderBenchView.RUNTIME_LANGUAGE);
                }
                if (e.getLanguage() == ReaderBenchView.RUNTIME_LANGUAGE) {
                    SummaryProcessingView.getLoadedSummaries().add(e);
                    addSummary(e);
                } else {
                    JOptionPane.showMessageDialog(desktopPane, "Incorrect language for the loaded verbalization!",
                            "Information", JOptionPane.INFORMATION_MESSAGE);
                }
            }
        }

        public Void doInBackground() {
            btnAddSummary.setEnabled(false);
            btnAddSerializedSummary.setEnabled(false);

            File file = new File(pathToDoc);
            File[] files = {file};
            if (isSerialized) {
                if (file.isDirectory()) {
                    // process each individual ser file
                    files = file.listFiles(new FilenameFilter() {
                        @Override
                        public boolean accept(File dir, String name) {
                            return name.endsWith(".ser");
                        }
                    });
                }
            } else if (file.isDirectory()) {
                // process each individual xml file
                files = file.listFiles(new FilenameFilter() {
                    @Override
                    public boolean accept(File dir, String name) {
                        return name.endsWith(".xml");
                    }
                });
            }
            for (File f : files) {
                try {
                    addSingleEssay(f.getPath());
                } catch (Exception e) {
                    e.printStackTrace();
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
        setTitle("ReaderBench - " + LocalizationUtils.getTranslation("Summary Processing"));
        setResizable(true);
        setClosable(true);
        setMaximizable(true);
        setIconifiable(true);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setBounds(100, 100, 900, 450);

        desktopPane = new JDesktopPane() {
            private static final long serialVersionUID = 8453433109734630086L;

            @Override
            public void updateUI() {
                if ("Nimbus".equals(UIManager.getLookAndFeel().getName())) {
                    UIDefaults map = new UIDefaults();
                    Painter<JComponent> painter = new Painter<JComponent>() {
                        @Override
                        public void paint(Graphics2D g, JComponent c, int w, int h) {
                            g.setColor(Color.WHITE);
                            g.fillRect(0, 0, w, h);
                        }
                    };
                    map.put("DesktopPane[Enabled].backgroundPainter", painter);
                    putClientProperty("Nimbus.Overrides", map);
                }
                super.updateUI();
            }
        };
        desktopPane.setBackground(Color.WHITE);
        setContentPane(desktopPane);

        btnAddSummary = new JButton(LocalizationUtils.getTranslation("Add summary(s)"));
        btnAddSummary.setEnabled(true);
        btnAddSummary.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (DocumentProcessingView.getLoadedDocuments().size() > 0) {
                    try {
                        JInternalFrame frame = new AddSummaryView(SummaryProcessingView.this);
                        frame.setVisible(true);
                        desktopPane.add(frame);
                        try {
                            frame.setSelected(true);
                        } catch (java.beans.PropertyVetoException exception) {
                            exception.printStackTrace();
                        }
                    } catch (Exception exception) {
                        exception.printStackTrace();
                    }
                } else {
                    JOptionPane.showMessageDialog(desktopPane,
                            "At least one document must be already loaded in order to be able to start loading summaries!",
                            "Information", JOptionPane.INFORMATION_MESSAGE);
                }
            }
        });

        btnRemoveSummary = new JButton(LocalizationUtils.getTranslation("Remove summary"));
        btnRemoveSummary.setEnabled(false);
        btnRemoveSummary.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (summariesTable.getSelectedRow() != -1) {
                    loadedSummaries.remove(summariesTable.getSelectedRow());
                    summariesTableModel.removeRow(summariesTable.getSelectedRow());
                } else {
                    JOptionPane.showMessageDialog(SummaryProcessingView.this, "Please select a row to be deleted!",
                            "Information", JOptionPane.INFORMATION_MESSAGE);
                }
            }
        });

        if (summariesTableModel == null) {
            summariesTableModel = new VerbalisationManagementTableModel();
        }

        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        summariesTable = new JTable(summariesTableModel);
        summariesTable.setFillsViewportHeight(true);

        summariesTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                if (event.getClickCount() == 2) {
                    JTable target = (JTable) event.getSource();
                    int row = target.getSelectedRow();
                    if (row >= 0 && row < loadedSummaries.size()) {
                        Summary summary = loadedSummaries.get(summariesTable.getSelectedRow());
                        SummaryView view = new SummaryView(summary);
                        view.setVisible(true);
                    }
                }
            }
        });

        scrollPane.setViewportView(summariesTable);

        btnAddSerializedSummary = new JButton(LocalizationUtils.getTranslation("Add serialized summary(s)"));
        btnAddSerializedSummary.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JFileChooser fc = null;
                if (lastDirectory == null) {
                    fc = new JFileChooser(new File("resources/in"));
                } else {
                    fc = new JFileChooser(lastDirectory);
                }
                fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
                fc.setFileFilter(new FileFilter() {
                    public boolean accept(File f) {
                        if (f.isDirectory()) {
                            return true;
                        }
                        return f.getName().endsWith(".ser");
                    }

                    public String getDescription() {
                        return "Serialized document (*.ser) or directory";
                    }
                });
                int returnVal = fc.showOpenDialog(SummaryProcessingView.this);

                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    File file = fc.getSelectedFile();
                    lastDirectory = file.getParentFile();
                    EssayProcessingTask task = SummaryProcessingView.this.new EssayProcessingTask(file.getPath(), null,
                            false, true);
                    task.execute();
                }
            }
        });

        btnViewSummary = new JButton(LocalizationUtils.getTranslation("View summary"));
        btnViewSummary.setEnabled(false);
        btnViewSummary.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (summariesTable.getSelectedRow() != -1) {
                    Summary s = loadedSummaries.get(summariesTable.getSelectedRow());
                    SummaryView view = new SummaryView(s);
                    view.setVisible(true);
                } else {
                    JOptionPane.showMessageDialog(desktopPane, "Please select a summary to be viewed!", "Information",
                            JOptionPane.INFORMATION_MESSAGE);
                }
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
        return loadedSummaries;
    }

    public static synchronized void setLoadedSummaries(List<Summary> loadedVervalizations) {
        SummaryProcessingView.loadedSummaries = loadedVervalizations;
    }

    public synchronized void addSummary(Summary e) {
        if (summariesTableModel != null) {
            synchronized (summariesTableModel) {
                synchronized (loadedSummaries) {
                    // add rows as loaded documents
                    Vector<Object> dataRow = new Vector<Object>();

                    String authors = "";
                    for (String author : e.getAuthors()) {
                        authors += author + ", ";
                    }
                    if (authors.length() >= 2) {
                        authors = authors.substring(0, authors.length() - 2);
                    }
                    dataRow.add(authors);
                    dataRow.add(e.getReferredDoc().getTitleText());
                    dataRow.add(e.getReferredDoc().getLSA().getPath());
                    dataRow.add(e.getReferredDoc().getLDA().getPath());
                    summariesTableModel.addRow(dataRow);
                }
                if (loadedSummaries.size() > 0) {
                    btnRemoveSummary.setEnabled(true);
                    btnViewSummary.setEnabled(true);
                } else {
                    btnRemoveSummary.setEnabled(false);
                    btnViewSummary.setEnabled(false);
                }
            }
        }
    }

    public synchronized void updateContents() {
        if (summariesTableModel != null) {
            summariesTable.clearSelection();
            // clean table
            synchronized (summariesTableModel) {
                while (summariesTableModel.getRowCount() > 0) {
                    summariesTableModel.removeRow(0);
                }

                synchronized (loadedSummaries) {
                    for (Summary e : loadedSummaries) {
                        // add rows as loaded documents
                        Vector<Object> dataRow = new Vector<Object>();

                        String authors = "";
                        for (String author : e.getAuthors()) {
                            authors += author + ", ";
                        }
                        authors = authors.substring(0, authors.length() - 2);
                        dataRow.add(authors);
                        dataRow.add(e.getReferredDoc().getTitleText());
                        dataRow.add(e.getReferredDoc().getLSA().getPath());
                        dataRow.add(e.getReferredDoc().getLDA().getPath());
                        summariesTableModel.addRow(dataRow);
                    }

                    if (loadedSummaries.size() > 0) {
                        btnRemoveSummary.setEnabled(true);
                        btnViewSummary.setEnabled(true);
                    } else {
                        btnRemoveSummary.setEnabled(false);
                        btnViewSummary.setEnabled(false);
                    }
                }
            }
        }
    }
}
