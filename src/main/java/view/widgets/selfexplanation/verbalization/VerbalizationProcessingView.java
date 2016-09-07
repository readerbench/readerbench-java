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
package view.widgets.selfexplanation.verbalization;

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Graphics2D;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FilenameFilter;
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

import data.document.Document;
import data.document.Metacognition;
import utils.localization.LocalizationUtils;
import view.models.verbalization.VerbalisationManagementTableModel;
import view.widgets.ReaderBenchView;
import view.widgets.complexity.ComplexityIndicesView;
import view.widgets.document.DocumentProcessingView;

public class VerbalizationProcessingView extends JInternalFrame {

    private static final long serialVersionUID = -8772215709851320157L;
    static Logger logger = Logger.getLogger(VerbalizationProcessingView.class);

    private JDesktopPane desktopPane;

    private JTable verbalisationsTable;
    private DefaultTableModel verbalizationsTableModel = null;

    private JButton btnRemoveVerbalization = null;
    private JButton btnAddVerbalization = null;
    private JButton btnViewVerbalization = null;
    private JButton btnViewCummulativeStatistics = null;
    private static File lastDirectory = null;

    private static List<Metacognition> loadedVervalizations = new Vector<Metacognition>();
    private JButton btnAddSerializedVerbalization;

    public class VerbalizationProcessingTask extends SwingWorker<Void, Void> {

        private String pathToDoc;
        private Document referredDoc;
        private boolean usePOSTagging;
        private boolean isSerialized;

        public VerbalizationProcessingTask(String pathToDoc, Document d, boolean usePOSTagging, boolean isSerialized) {
            super();
            this.pathToDoc = pathToDoc;
            this.referredDoc = d;
            this.usePOSTagging = usePOSTagging;
            this.isSerialized = isSerialized;
        }

        public void addSingleVerbalisation(String pathToIndividualFile) {
            Metacognition v = null;
            if (isSerialized) {
                v = (Metacognition) Metacognition.loadSerializedDocument(pathToIndividualFile);
            } else {
                v = Metacognition.loadVerbalization(pathToIndividualFile, referredDoc, usePOSTagging, true);
                v.computeAll(true, true);
            }

            if (v != null) {
                if (v.getLanguage() == ReaderBenchView.RUNTIME_LANGUAGE) {
                    VerbalizationProcessingView.getLoadedVervalizations().add(v);
                    addVerbalization(v);
                } else {
                    JOptionPane.showMessageDialog(desktopPane, "Incorrect language for the loaded verbalization!", "Information", JOptionPane.INFORMATION_MESSAGE);
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
                    addSingleVerbalisation(f.getPath());
                } catch (Exception e) {
                    e.printStackTrace();
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
        setTitle("ReaderBench - " + LocalizationUtils.getTranslation("Verbalization Processing"));
        setResizable(true);
        setClosable(true);
        setMaximizable(true);
        setIconifiable(true);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setBounds(100, 100, 780, 450);

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

        btnAddVerbalization = new JButton(LocalizationUtils.getTranslation("Add verbalization(s)"));
        btnAddVerbalization.setEnabled(true);
        btnAddVerbalization.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (DocumentProcessingView.getLoadedDocuments().size() > 0) {
                    try {
                        JInternalFrame frame = new AddVerbalizationView(VerbalizationProcessingView.this);
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
                            LocalizationUtils.getTranslation(
                                    "At least one document must be already loaded in order to be able to start processing verbalizations!"),
                            LocalizationUtils.getTranslation("Information"), JOptionPane.INFORMATION_MESSAGE);
                }
            }
        });

        btnRemoveVerbalization = new JButton(LocalizationUtils.getTranslation("Remove verbalization"));
        btnRemoveVerbalization.setEnabled(false);
        btnRemoveVerbalization.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (verbalisationsTable.getSelectedRow() != -1) {
                    loadedVervalizations.remove(verbalisationsTable.getSelectedRow());
                    updateContents();
                } else {
                    JOptionPane.showMessageDialog(VerbalizationProcessingView.this,
                            LocalizationUtils.getTranslation("Please select a row to be deleted!"), "Information",
                            JOptionPane.INFORMATION_MESSAGE);
                }
            }
        });

        btnViewVerbalization = new JButton(LocalizationUtils.getTranslation("View verbalization"));
        btnViewVerbalization.setEnabled(false);
        btnViewVerbalization.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (verbalisationsTable.getSelectedRow() != -1) {
                    Metacognition v = loadedVervalizations.get(verbalisationsTable.getSelectedRow());
                    VerbalizationView view = new VerbalizationView(v);
                    view.setVisible(true);
                } else {
                    JOptionPane.showMessageDialog(desktopPane, "Please select a verbalization to be viewed!",
                            "Information", JOptionPane.INFORMATION_MESSAGE);
                }
            }
        });

        if (verbalizationsTableModel == null) {
            verbalizationsTableModel = new VerbalisationManagementTableModel();
        }

        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        verbalisationsTable = new JTable(verbalizationsTableModel);
        verbalisationsTable.setFillsViewportHeight(true);

        verbalisationsTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                if (event.getClickCount() == 2) {
                    JTable target = (JTable) event.getSource();
                    int row = target.getSelectedRow();
                    if (row >= 0 && row < loadedVervalizations.size()) {
                        Metacognition v = loadedVervalizations.get(row);
                        VerbalizationView view = new VerbalizationView(v);
                        view.setVisible(true);
                    }
                }
            }
        });

        scrollPane.setViewportView(verbalisationsTable);

        btnAddSerializedVerbalization = new JButton("Add serialized verbalization(s)");
        btnAddSerializedVerbalization.addActionListener(new ActionListener() {
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
                int returnVal = fc.showOpenDialog(VerbalizationProcessingView.this);

                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    File file = fc.getSelectedFile();
                    lastDirectory = file.getParentFile();
                    VerbalizationProcessingTask task = VerbalizationProcessingView.this.new VerbalizationProcessingTask(
                            file.getPath(), null, false, true);
                    task.execute();
                }
            }
        });

        btnViewCummulativeStatistics = new JButton(LocalizationUtils.getTranslation("View cummulative statistics"));
        btnViewCummulativeStatistics.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                EventQueue.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        VerbalizationsCumulativeView view = new VerbalizationsCumulativeView(loadedVervalizations);
                        view.setVisible(true);
                    }
                });
            }
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
        return loadedVervalizations;
    }

    public static synchronized void setLoadedVervalizations(List<Metacognition> loadedVervalizations) {
        VerbalizationProcessingView.loadedVervalizations = loadedVervalizations;
    }

    private void updateButtons(boolean isEnabled) {
        btnRemoveVerbalization.setEnabled(isEnabled);
        btnViewVerbalization.setEnabled(isEnabled);
        btnViewCummulativeStatistics.setEnabled(isEnabled);
    }

    public synchronized void addVerbalization(Metacognition v) {
        if (verbalizationsTableModel != null) {
            synchronized (verbalizationsTableModel) {
                synchronized (loadedVervalizations) {
                    Vector<Object> dataRow = new Vector<Object>();

                    String authors = "";
                    for (String author : v.getAuthors()) {
                        authors += author + ", ";
                    }
                    authors = authors.substring(0, authors.length() - 2);
                    dataRow.add(authors);
                    dataRow.add(v.getReferredDoc().getTitleText());
                    dataRow.add(v.getReferredDoc().getLSA().getPath());
                    dataRow.add(v.getReferredDoc().getLDA().getPath());
                    verbalizationsTableModel.addRow(dataRow);
                }
                if (loadedVervalizations.size() > 0) {
                    updateButtons(true);
                } else {
                    updateButtons(false);
                }
            }
        }
    }

    public synchronized void updateContents() {
        if (verbalizationsTableModel != null) {
            verbalisationsTable.clearSelection();
            // clean table
            synchronized (verbalizationsTableModel) {
                while (verbalizationsTableModel.getRowCount() > 0) {
                    verbalizationsTableModel.removeRow(0);
                }

                synchronized (loadedVervalizations) {
                    for (Metacognition v : loadedVervalizations) {
                        // add rows as loaded documents
                        Vector<Object> dataRow = new Vector<Object>();

                        String authors = "";
                        for (String author : v.getAuthors()) {
                            authors += author + ", ";
                        }
                        authors = authors.substring(0, authors.length() - 2);
                        dataRow.add(authors);
                        dataRow.add(v.getReferredDoc().getTitleText());
                        dataRow.add(v.getReferredDoc().getLSA().getPath());
                        dataRow.add(v.getReferredDoc().getLDA().getPath());
                        verbalizationsTableModel.addRow(dataRow);
                    }

                    if (loadedVervalizations.size() > 0) {
                        updateButtons(true);
                    } else {
                        updateButtons(false);
                    }
                }
            }
        }
    }
}
