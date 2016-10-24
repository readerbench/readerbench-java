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
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Vector;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
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
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.DefaultTableModel;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;

import services.readingStrategies.ReadingStrategies;
import utils.localization.LocalizationUtils;
import view.models.verbalization.VerbalizationAnnotationTable;
import view.models.verbalization.VerbalizationAnnotationTableModel;
import data.Block;
import data.document.Document;
import data.document.Metacognition;
import data.document.ReadingStrategyType;
import java.util.ArrayList;

public class AnnotateVerbalizationView extends JFrame {

    private static final long serialVersionUID = -2864356905020607155L;
    static Logger logger = Logger.getLogger(AnnotateVerbalizationView.class);

    private Metacognition loadedVerbalization = null;
    private Document loadedDocument = null;
    private static File lastDirectory;
    private JPanel contentPane;
    private JTextField textFieldAuthor;
    private JTextField textFieldTeachers;
    private JTextField textFieldDate;
    private JTextField textFieldURI;
    private JTextField textFieldSource;
    private JScrollPane scrollPane;
    private JLabel lblClassroomLevel;
    private JTextField txtClassroomLevel;
    private JMenuItem mntmOpenDocument;
    private JMenuItem mntmOpenVerbalization;
    private JMenuItem mntmSaveVerbalization;
    private JTable tableContents;
    private DefaultTableModel modelContents;
    private boolean[] isVerbalisation;

    /**
     * Create the frame.
     */
    public AnnotateVerbalizationView() {
        super.setTitle("ReaderBench - " + LocalizationUtils.getTranslation("Annotate SelfExplanations"));
        super.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        super.setBounds(100, 100, 900, 600);

        JMenuBar menuBar = new JMenuBar();
        super.setJMenuBar(menuBar);

        JMenu mnFile = new JMenu(LocalizationUtils.getTranslation("File"));
        mnFile.setMnemonic(KeyEvent.VK_F);
        menuBar.add(mnFile);

        mntmOpenDocument = new JMenuItem(LocalizationUtils.getTranslation("Open Document"));
        mntmOpenDocument.addActionListener((ActionEvent e) -> {
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
                    return f.getName().endsWith(".xml");
                }

                @Override
                public String getDescription() {
                    return "XML files (*.xml)";
                }
            });
            int returnVal = fc.showOpenDialog(AnnotateVerbalizationView.this);

            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File file = fc.getSelectedFile();
                lastDirectory = file.getParentFile();
                loadedDocument = Document.load(file, new ArrayList<>(), null, false);
                if (loadedDocument != null) {
                    mntmOpenDocument.setEnabled(false);
                    mntmOpenVerbalization.setEnabled(true);

                    if (loadedDocument.getAuthors().size() > 0) {
                        textFieldAuthor
                                .setText(loadedDocument.getAuthors().get(0));
                    }

                    DateFormat df = new SimpleDateFormat("dd-mm-yyyy");
                    textFieldDate.setText(df.format(loadedDocument.getDate()));
                    textFieldURI.setText(loadedDocument.getURI());
                    textFieldSource.setText(loadedDocument.getSource());
                }
            }
        });
        mnFile.add(mntmOpenDocument);

        mntmOpenVerbalization = new JMenuItem(LocalizationUtils.getTranslation("Open Verbalization"));
        mntmOpenVerbalization.setEnabled(false);
        mntmOpenVerbalization.addActionListener((ActionEvent e) -> {
            if (loadedDocument != null) {
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
                        return f.getName().endsWith(".xml");
                    }

                    @Override
                    public String getDescription() {
                        return "XML files (*.xml)";
                    }
                });
                int returnVal = fc
                        .showOpenDialog(AnnotateVerbalizationView.this);

                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    File file = fc.getSelectedFile();
                    lastDirectory = file.getParentFile();
                    loadedVerbalization = Metacognition.loadVerbalization(file.getAbsolutePath(), loadedDocument, false);
                    if (tableContents == null) {
                        determineRowType();
                        modelContents = new VerbalizationAnnotationTableModel(
                                isVerbalisation);
                        tableContents = new VerbalizationAnnotationTable(
                                modelContents, isVerbalisation);
                        tableContents.setFillsViewportHeight(true);
                        tableContents.getColumnModel().getColumn(0)
                                .setPreferredWidth(600);
                        scrollPane.setViewportView(tableContents);
                        mntmOpenDocument.setEnabled(false);
                        mntmSaveVerbalization.setEnabled(true);
                    }
                    updateContent();
                }
            } else {
                JOptionPane.showMessageDialog(AnnotateVerbalizationView.this, "The document template must be loaded for annotating verbalizations!", "Information", JOptionPane.INFORMATION_MESSAGE);
            }
        });
        mnFile.add(mntmOpenVerbalization);

        mntmSaveVerbalization = new JMenuItem(LocalizationUtils.getTranslation("Save Verbalization"),
                KeyEvent.VK_S);
        mntmSaveVerbalization.setEnabled(false);
        mntmSaveVerbalization.setAccelerator(KeyStroke
                .getKeyStroke("control S"));
        mnFile.add(mntmSaveVerbalization);

        JMenuItem mntmQuit = new JMenuItem(LocalizationUtils.getTranslation("Quit"), KeyEvent.VK_Q);
        mntmQuit.setAccelerator(KeyStroke.getKeyStroke("control Q"));
        mntmQuit.addActionListener((ActionEvent e) -> {
            AnnotateVerbalizationView.this.dispose();
        });
        mnFile.add(mntmQuit);

        contentPane = new JPanel();
        contentPane.setBackground(Color.WHITE);
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        setContentPane(contentPane);

        JLabel lblAuthor = new JLabel(LocalizationUtils.getTranslation("Author") + ":");
        lblAuthor.setFont(new Font("Lucida Grande", Font.BOLD, 13));

        JLabel lblTeachers = new JLabel(LocalizationUtils.getTranslation("Teachers") + "*:");
        lblTeachers.setFont(new Font("Lucida Grande", Font.BOLD, 13));

        JLabel lblTeachersComment = new JLabel(
                "* " + LocalizationUtils.getTranslation("Multiple teachers should be separated by commas"));
        lblTeachersComment.setFont(new Font("Lucida Grande", Font.PLAIN, 9));

        textFieldAuthor = new JTextField();
        textFieldAuthor.setColumns(10);

        textFieldTeachers = new JTextField();
        textFieldTeachers.setColumns(10);

        JLabel lblDate = new JLabel(LocalizationUtils.getTranslation("Date") + ":");
        lblDate.setFont(new Font("SansSerif", Font.BOLD, 12));

        textFieldDate = new JTextField();
        textFieldDate.setBackground(Color.LIGHT_GRAY);
        textFieldDate.setEditable(false);
        textFieldDate.setHorizontalAlignment(SwingConstants.CENTER);
        textFieldDate.setColumns(10);

        JLabel lblUri = new JLabel("URI:");
        lblUri.setFont(new Font("SansSerif", Font.BOLD, 12));

        textFieldURI = new JTextField();
        textFieldURI.setColumns(10);

        JLabel lblSource = new JLabel(LocalizationUtils.getTranslation("Source") + ":");
        lblSource.setFont(new Font("SansSerif", Font.BOLD, 12));

        textFieldSource = new JTextField();
        textFieldSource.setColumns(10);

        JLabel lblVerbalization = new JLabel(LocalizationUtils.getTranslation("Verbalization"));
        lblVerbalization.setFont(new Font("SansSerif", Font.BOLD, 12));

        txtClassroomLevel = new JTextField();
        txtClassroomLevel.setHorizontalAlignment(SwingConstants.CENTER);
        txtClassroomLevel.setText("CM2\n");
        txtClassroomLevel.setColumns(10);

        lblClassroomLevel = new JLabel(LocalizationUtils.getTranslation("Classroom level") + ":");
        lblClassroomLevel.setFont(new Font("SansSerif", Font.BOLD, 12));

        JSeparator separator = new JSeparator();

        scrollPane = new JScrollPane();
        scrollPane
                .setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        GroupLayout gl_contentPane = new GroupLayout(contentPane);
        gl_contentPane
                .setHorizontalGroup(gl_contentPane
                        .createParallelGroup(Alignment.TRAILING)
                        .addGroup(
                                gl_contentPane
                                .createSequentialGroup()
                                .addContainerGap()
                                .addGroup(
                                        gl_contentPane
                                        .createParallelGroup(
                                                Alignment.TRAILING)
                                        .addComponent(
                                                scrollPane,
                                                Alignment.LEADING,
                                                GroupLayout.DEFAULT_SIZE,
                                                678,
                                                Short.MAX_VALUE)
                                        .addComponent(
                                                separator,
                                                Alignment.LEADING,
                                                GroupLayout.DEFAULT_SIZE,
                                                678,
                                                Short.MAX_VALUE)
                                        .addGroup(
                                                Alignment.LEADING,
                                                gl_contentPane
                                                .createSequentialGroup()
                                                .addComponent(
                                                        lblAuthor)
                                                .addPreferredGap(
                                                        ComponentPlacement.RELATED)
                                                .addComponent(
                                                        textFieldAuthor,
                                                        GroupLayout.DEFAULT_SIZE,
                                                        456,
                                                        Short.MAX_VALUE)
                                                .addPreferredGap(
                                                        ComponentPlacement.RELATED)
                                                .addComponent(
                                                        lblClassroomLevel)
                                                .addPreferredGap(
                                                        ComponentPlacement.RELATED)
                                                .addComponent(
                                                        txtClassroomLevel,
                                                        GroupLayout.PREFERRED_SIZE,
                                                        51,
                                                        GroupLayout.PREFERRED_SIZE))
                                        .addGroup(
                                                Alignment.LEADING,
                                                gl_contentPane
                                                .createSequentialGroup()
                                                .addComponent(
                                                        lblTeachers)
                                                .addPreferredGap(
                                                        ComponentPlacement.RELATED)
                                                .addComponent(
                                                        textFieldTeachers,
                                                        GroupLayout.DEFAULT_SIZE,
                                                        601,
                                                        Short.MAX_VALUE))
                                        .addComponent(
                                                lblTeachersComment,
                                                Alignment.LEADING)
                                        .addGroup(
                                                Alignment.LEADING,
                                                gl_contentPane
                                                .createSequentialGroup()
                                                .addComponent(
                                                        lblDate)
                                                .addPreferredGap(
                                                        ComponentPlacement.RELATED)
                                                .addComponent(
                                                        textFieldDate,
                                                        GroupLayout.PREFERRED_SIZE,
                                                        96,
                                                        GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(
                                                        ComponentPlacement.RELATED)
                                                .addComponent(
                                                        lblUri)
                                                .addPreferredGap(
                                                        ComponentPlacement.RELATED)
                                                .addComponent(
                                                        textFieldURI,
                                                        GroupLayout.DEFAULT_SIZE,
                                                        508,
                                                        Short.MAX_VALUE))
                                        .addGroup(
                                                Alignment.LEADING,
                                                gl_contentPane
                                                .createSequentialGroup()
                                                .addComponent(
                                                        lblSource)
                                                .addPreferredGap(
                                                        ComponentPlacement.RELATED)
                                                .addComponent(
                                                        textFieldSource,
                                                        GroupLayout.DEFAULT_SIZE,
                                                        628,
                                                        Short.MAX_VALUE))
                                        .addComponent(
                                                lblVerbalization,
                                                Alignment.LEADING))
                                .addContainerGap()));
        gl_contentPane
                .setVerticalGroup(gl_contentPane
                        .createParallelGroup(Alignment.LEADING)
                        .addGroup(
                                gl_contentPane
                                .createSequentialGroup()
                                .addContainerGap()
                                .addGroup(
                                        gl_contentPane
                                        .createParallelGroup(
                                                Alignment.BASELINE)
                                        .addComponent(lblAuthor)
                                        .addComponent(
                                                textFieldAuthor,
                                                GroupLayout.PREFERRED_SIZE,
                                                GroupLayout.DEFAULT_SIZE,
                                                GroupLayout.PREFERRED_SIZE)
                                        .addComponent(
                                                txtClassroomLevel,
                                                GroupLayout.PREFERRED_SIZE,
                                                GroupLayout.DEFAULT_SIZE,
                                                GroupLayout.PREFERRED_SIZE)
                                        .addComponent(
                                                lblClassroomLevel))
                                .addPreferredGap(
                                        ComponentPlacement.RELATED)
                                .addGroup(
                                        gl_contentPane
                                        .createParallelGroup(
                                                Alignment.BASELINE)
                                        .addComponent(
                                                lblTeachers)
                                        .addComponent(
                                                textFieldTeachers,
                                                GroupLayout.PREFERRED_SIZE,
                                                GroupLayout.DEFAULT_SIZE,
                                                GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(
                                        ComponentPlacement.RELATED)
                                .addComponent(lblTeachersComment)
                                .addPreferredGap(
                                        ComponentPlacement.RELATED)
                                .addGroup(
                                        gl_contentPane
                                        .createParallelGroup(
                                                Alignment.BASELINE)
                                        .addComponent(lblDate)
                                        .addComponent(
                                                textFieldDate,
                                                GroupLayout.PREFERRED_SIZE,
                                                GroupLayout.DEFAULT_SIZE,
                                                GroupLayout.PREFERRED_SIZE)
                                        .addComponent(lblUri)
                                        .addComponent(
                                                textFieldURI,
                                                GroupLayout.PREFERRED_SIZE,
                                                GroupLayout.DEFAULT_SIZE,
                                                GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(
                                        ComponentPlacement.RELATED)
                                .addGroup(
                                        gl_contentPane
                                        .createParallelGroup(
                                                Alignment.BASELINE)
                                        .addComponent(lblSource)
                                        .addComponent(
                                                textFieldSource,
                                                GroupLayout.PREFERRED_SIZE,
                                                GroupLayout.DEFAULT_SIZE,
                                                GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(
                                        ComponentPlacement.UNRELATED)
                                .addComponent(lblVerbalization)
                                .addPreferredGap(
                                        ComponentPlacement.RELATED)
                                .addComponent(separator,
                                        GroupLayout.PREFERRED_SIZE,
                                        GroupLayout.DEFAULT_SIZE,
                                        GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(
                                        ComponentPlacement.RELATED)
                                .addComponent(scrollPane,
                                        GroupLayout.DEFAULT_SIZE, 358,
                                        Short.MAX_VALUE)
                                .addContainerGap()));

        contentPane.setLayout(gl_contentPane);
    }

    private void determineRowType() {
        // entire document + verbalization + overall row
        isVerbalisation = new boolean[loadedVerbalization.getBlocks().size()
                + loadedVerbalization.getReferredDoc().getBlocks().size() + 1];
        for (int i = 0; i < isVerbalisation.length; i++) {
            isVerbalisation[i] = false;
        }
        for (Block v : loadedVerbalization.getBlocks()) {
            isVerbalisation[v.getIndex() + v.getRefBlock().getIndex() + 1] = true;
        }
    }

    private void updateContent() {
        if (loadedVerbalization != null
                && loadedVerbalization.getReferredDoc() != null) {
            if (loadedVerbalization.getAuthors().size() > 0) {
                textFieldAuthor
                        .setText(loadedVerbalization.getAuthors().get(0));
            }
            String teachers = "";
            for (String teacher : loadedVerbalization.getTutors()) {
                teachers += teacher + ", ";
            }
            textFieldTeachers.setText(teachers.length() > 2 ? teachers
                    .substring(0, teachers.length() - 2) : "");

            DateFormat df = new SimpleDateFormat("dd-mm-yyyy");
            textFieldDate.setText(df.format(loadedVerbalization.getDate()));

            textFieldURI.setText(loadedVerbalization.getURI());

            textFieldSource.setText(loadedVerbalization.getSource());

            // clean table
            while (modelContents.getRowCount() > 0) {
                modelContents.removeRow(0);
            }

            int startIndex = 0;
            int endIndex = 0;
            for (int index = 0; index < loadedVerbalization.getBlocks().size(); index++) {
                endIndex = loadedVerbalization.getBlocks().get(index)
                        .getRefBlock().getIndex();
                for (int refBlockId = startIndex; refBlockId <= endIndex; refBlockId++) {
                    // add rows as blocks within the document
                    Vector<Object> dataRow = new Vector<>();

                    // add block text
                    dataRow.add(loadedVerbalization.getReferredDoc()
                            .getBlocks().get(refBlockId).getAlternateText());

                    for (ReadingStrategyType value : ReadingStrategyType.values()) {
                        dataRow.add("");
                    }

                    modelContents.addRow(dataRow);
                }
                startIndex = endIndex + 1;

                // add corresponding verbalization
                Vector<Object> dataRow = new Vector<>();

                dataRow.add(loadedVerbalization.getBlocks().get(index)
                        .getAlternateText());

                for (ReadingStrategyType value : ReadingStrategyType.values()) {
                    dataRow.add("");
                }
                modelContents.addRow(dataRow);
            }
        }
    }

    private static void adjustToSystemGraphics() {
        for (UIManager.LookAndFeelInfo info : UIManager
                .getInstalledLookAndFeels()) {
            if ("Nimbus".equals(info.getName())) {
                try {
                    UIManager.setLookAndFeel(info.getClassName());
                } catch (ClassNotFoundException |
                        InstantiationException |
                        IllegalAccessException |
                        UnsupportedLookAndFeelException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
