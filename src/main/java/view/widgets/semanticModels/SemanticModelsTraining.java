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
package view.widgets.semanticModels;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.logging.Logger;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileFilter;

import data.Lang;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ResourceBundle;
import java.util.logging.Level;
import org.openide.util.Exceptions;
import services.semanticModels.PreProcessing;
import services.semanticModels.LDA.LDA;
import services.semanticModels.LSA.CreateInputMatrix;
import services.semanticModels.LSA.RunSVD;
import services.semanticModels.word2vec.Word2VecModel;
import utils.LocalizationUtils;
import view.widgets.ReaderBenchView;

public class SemanticModelsTraining extends JFrame {

    private static final long serialVersionUID = 4920477447183036103L;
    static final Logger LOGGER = Logger.getLogger("");

    private static final String TERM_DOC_MATRIX_NAME = "matrix.svd";

    private JPanel contentPane;
    private JTextField textFieldInput;
    private JTextField textFieldOutput;
    private JTextField textFieldLDADirectory;
    private JTextField textFieldLDANoTopics;
    private JTextField textFieldLDANoIterations;
    private JTextField textFieldLDANoThreads;
    private JTextField textFieldMinWords;
    private JComboBox<String> comboBoxFormat;
    private JCheckBox chckbxUsePosTagging;
    private JTextField textFieldLSAFile;
    private JTextField textFieldLSARank;
    private JTextField textFieldLSAPowerIterations;
    private JTextField textFieldWord2VecFile;
    private JTextField textFieldWord2VecEpochs;
    private JTextField textFieldWord2VecLayerSize;
    private JButton btnPreProcess;
    private JButton btnLSATrain;
    private JButton btnLDATrain;
    private JButton btnWord2VecTrain;

    private class PreProcessingTask extends SwingWorker<Void, Void> {

        private final String input;
        private final String output;
        private final Lang lang;
        private final int minNoWords;
        private final boolean usePosTagging;
        private final int selectedCase;

        public PreProcessingTask(String input, String output, Lang lang, int minNoWords, boolean usePosTagging,
                int selectedCase) {
            super();
            this.input = input;
            this.output = output;
            this.lang = lang;
            this.minNoWords = minNoWords;
            this.usePosTagging = usePosTagging;
            this.selectedCase = selectedCase;
        }

        @Override
        public Void doInBackground() {
            btnPreProcess.setEnabled(false);
            btnLSATrain.setEnabled(false);
            btnLDATrain.setEnabled(false);
            btnWord2VecTrain.setEnabled(false);
            try {
                PreProcessing preprocess = new PreProcessing();
                switch (selectedCase) {
                    case 1:
                        preprocess.parseTasa(input, output, lang, usePosTagging, minNoWords, null);
                        break;
                    case 2:
                        preprocess.parseCOCA(input, output, lang, usePosTagging, minNoWords, null);
                        break;
                    default:
                        preprocess.parseGeneralCorpus(input, output, lang, usePosTagging, minNoWords, null);
//                        preprocess.parseGeneralCorpus(input, output, lang, usePosTagging, minNoWords, new ListOfWords("resources/corpora/EN/english_names.txt"));
                }
            } catch (IOException exc) {
                LOGGER.log(Level.SEVERE, "Error processing input file " + exc.getMessage(), exc);
            }
            return null;
        }

        @Override
        public void done() {
            Toolkit.getDefaultToolkit().beep();
            btnPreProcess.setEnabled(true);
            btnLSATrain.setEnabled(true);
            btnLDATrain.setEnabled(true);
            btnWord2VecTrain.setEnabled(true);
        }
    }

    private class LSATrainingTask extends SwingWorker<Void, Void> {

        private final File input;
        private final Lang lang;
        private final int k;
        private final int noPowerIterations;

        public LSATrainingTask(File input, Lang lang, int k, int noPowerIterations) {
            super();
            this.input = input;
            this.lang = lang;
            this.k = k;
            this.noPowerIterations = noPowerIterations;
        }

        @Override
        public Void doInBackground() {
            btnPreProcess.setEnabled(false);
            btnLSATrain.setEnabled(false);
            btnLDATrain.setEnabled(false);
            btnWord2VecTrain.setEnabled(false);

            try {
                // create initial matrix
                LOGGER.info("Starting to create term-doc matrix ...");
                CreateInputMatrix lsaTraining = new CreateInputMatrix();
                lsaTraining.parseCorpus(input.getParent(), input.getName(), TERM_DOC_MATRIX_NAME, lang);

                LOGGER.info("Finished building term-doc matrix ...");
                // perform SVD
                RunSVD.runSSVDOnSparseVectors(input.getParent() + "/" + TERM_DOC_MATRIX_NAME,
                        input.getParent(), k, k, noPowerIterations);

                LOGGER.info("Finished performing SVD decomposition ...");
            } catch (IOException exc) {
                LOGGER.log(Level.SEVERE, "Error procesing {0} directory: {1}", new Object[]{input, exc.getMessage()});
            }
            return null;
        }

        @Override
        public void done() {
            Toolkit.getDefaultToolkit().beep();
            btnPreProcess.setEnabled(true);
            btnLSATrain.setEnabled(true);
            btnLDATrain.setEnabled(true);
            btnWord2VecTrain.setEnabled(true);
        }
    }

    private class LDATrainingTask extends SwingWorker<Void, Void> {

        private final String input;
        private final Lang lang;
        private final int noTopics;
        private final int noThreads;
        private final int noIterations;

        public LDATrainingTask(String input, Lang lang, int noTopics, int noThreads, int noIterations) {
            super();
            this.input = input;
            this.lang = lang;
            this.noTopics = noTopics;
            this.noThreads = noThreads;
            this.noIterations = noIterations;
        }

        @Override
        public Void doInBackground() {
            btnPreProcess.setEnabled(false);
            btnLSATrain.setEnabled(false);
            btnLDATrain.setEnabled(false);
            btnWord2VecTrain.setEnabled(false);

            try {
                LDA lda = new LDA(lang);
                lda.processCorpus(input, noTopics, noThreads, noIterations);
            } catch (IOException exc) {
                LOGGER.log(Level.SEVERE, "Error procesing {0} directory: {1}", new Object[]{input, exc.getMessage()});
                Exceptions.printStackTrace(exc);
            }
            return null;
        }

        @Override
        public void done() {
            Toolkit.getDefaultToolkit().beep();
            btnPreProcess.setEnabled(true);
            btnLSATrain.setEnabled(true);
            btnLDATrain.setEnabled(true);
            btnWord2VecTrain.setEnabled(true);
        }
    }

    private class Word2VecTrainingTask extends SwingWorker<Void, Void> {

        private final String input;
        private final int noEpochs;
        private final int layerSize;

        public Word2VecTrainingTask(String input, int noEpochs, int layerSize) {
            super();
            this.input = input;
            this.noEpochs = noEpochs;
            this.layerSize = layerSize;
        }

        @Override
        public Void doInBackground() {
            btnPreProcess.setEnabled(false);
            btnLSATrain.setEnabled(false);
            btnLDATrain.setEnabled(false);
            btnWord2VecTrain.setEnabled(false);

            try {
                Word2VecModel.trainModel(input, noEpochs, layerSize);
            } catch (FileNotFoundException exc) {
                LOGGER.log(Level.SEVERE, "Error procesing {0} directory: {1}", new Object[]{input, exc.getMessage()});
                Exceptions.printStackTrace(exc);
            }
            return null;
        }

        @Override
        public void done() {
            Toolkit.getDefaultToolkit().beep();
            btnPreProcess.setEnabled(true);
            btnLSATrain.setEnabled(true);
            btnLDATrain.setEnabled(true);
            btnWord2VecTrain.setEnabled(true);
        }
    }

    /**
     * Create the frame.
     */
    public SemanticModelsTraining() {
        super.setResizable(false);
        super.setTitle(ResourceBundle.getBundle("utils.localization.messages")
                .getString("SemanticModelsTraining.title"));

        super.setBounds(100, 100, 540, 280);
        contentPane = new JPanel();
        contentPane.setBackground(Color.WHITE);
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        contentPane.setLayout(new BorderLayout(0, 0));
        super.setContentPane(contentPane);

        JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
        tabbedPane.setBackground(Color.WHITE);
        contentPane.add(tabbedPane, BorderLayout.CENTER);

        JPanel panelPreProcessing = new JPanel();
        panelPreProcessing.setBackground(Color.WHITE);
        tabbedPane.addTab(ResourceBundle.getBundle("utils.localization.messages")
                .getString("SemanticModelsTraining.tabPreprocessing"), null, panelPreProcessing, null);

        JLabel lblSelectInput = new JLabel(ResourceBundle.getBundle("utils.localization.messages")
                .getString("SemanticModelsTraining.lblSelectInputFolder") + ":");

        textFieldInput = new JTextField();
        textFieldInput.setText("resources/config");
        textFieldInput.setColumns(10);

        JLabel lblFormat = new JLabel(ResourceBundle.getBundle("utils.localization.messages")
                .getString("SemanticModelsTraining.lblFormat") + ":");

        comboBoxFormat = new JComboBox<>();
        comboBoxFormat.addItem(ResourceBundle.getBundle("utils.localization.messages")
                .getString("SemanticModelsTraining.btnOneDocPerLine"));
        comboBoxFormat.addItem(ResourceBundle.getBundle("utils.localization.messages")
                .getString("SemanticModelsTraining.btnTasaSpecificFormat"));
        comboBoxFormat.addItem(ResourceBundle.getBundle("utils.localization.messages")
                .getString("SemanticModelsTraining.btnCocaSpecificFormat"));

        JButton btnBrowse = new JButton("...");
        btnBrowse.addActionListener((ActionEvent e) -> {
            JFileChooser fc;
            fc = new JFileChooser("resources/config");
            fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            int returnVal = fc.showOpenDialog(SemanticModelsTraining.this);

            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File file = fc.getSelectedFile();
                textFieldInput.setText(file.getPath());
            }
        });

        chckbxUsePosTagging = new JCheckBox(ResourceBundle.getBundle("utils.localization.messages")
                .getString("SemanticModelsTraining.boxUsePOSTagging"));

        JLabel lblOutputFileName = new JLabel(ResourceBundle.getBundle("utils.localization.messages")
                .getString("SemanticModelsTraining.lblOutputFileName") + ":");

        textFieldOutput = new JTextField();
        textFieldOutput.setText("out.txt");
        textFieldOutput.setColumns(10);

        btnPreProcess = new JButton(ResourceBundle.getBundle("utils.localization.messages")
                .getString("SemanticModelsTraining.btnPreProcess"));
        btnPreProcess.addActionListener((ActionEvent e) -> {
            if (textFieldInput.getText().equals("")) {
                JOptionPane.showMessageDialog(SemanticModelsTraining.this,
                        LocalizationUtils.getGeneric("msgSelectInputFolder") + "!",
                        "Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
            File input = new File(textFieldInput.getText());
            if (textFieldOutput.getText().equals("")) {
                JOptionPane.showMessageDialog(SemanticModelsTraining.this,
                        LocalizationUtils.getGeneric("msgSelectOutputFile") + "!",
                        "Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
            Lang lang = ReaderBenchView.RUNTIME_LANGUAGE;
            int minNoWords;
            try {
                minNoWords = Integer.parseInt(textFieldMinWords.getText());
            } catch (NumberFormatException exc) {
                minNoWords = 20;
            }

            PreProcessingTask task = new PreProcessingTask(input.getPath(), textFieldOutput.getText(), lang,
                    minNoWords, chckbxUsePosTagging.isSelected(),
                    SemanticModelsTraining.this.comboBoxFormat.getSelectedIndex());
            task.execute();
        });

        JLabel lblMinWords = new JLabel(ResourceBundle.getBundle("utils.localization.messages")
                .getString("SemanticModelsTraining.lblMinWords") + ":");

        textFieldMinWords = new JTextField();
        textFieldMinWords.setText("20");
        textFieldMinWords.setHorizontalAlignment(SwingConstants.RIGHT);
        textFieldMinWords.setColumns(10);

        GroupLayout gl_panelPreProcessing = new GroupLayout(panelPreProcessing);
        gl_panelPreProcessing
                .setHorizontalGroup(gl_panelPreProcessing.createParallelGroup(Alignment.LEADING)
                        .addGroup(gl_panelPreProcessing.createSequentialGroup().addContainerGap()
                                .addGroup(gl_panelPreProcessing.createParallelGroup(Alignment.LEADING)
                                        .addComponent(lblSelectInput)
                                        .addComponent(
                                                lblOutputFileName)
                                        .addComponent(lblFormat).addComponent(lblMinWords))
                                .addGap(18)
                                .addGroup(gl_panelPreProcessing.createParallelGroup(Alignment.LEADING)
                                        .addGroup(gl_panelPreProcessing.createSequentialGroup()
                                                .addComponent(textFieldInput, GroupLayout.DEFAULT_SIZE, 255,
                                                        Short.MAX_VALUE)
                                                .addPreferredGap(ComponentPlacement.RELATED).addComponent(btnBrowse))
                                        .addComponent(comboBoxFormat, 0, 336, Short.MAX_VALUE)
                                        .addComponent(textFieldOutput, 336, 336, 336)
                                        .addGroup(gl_panelPreProcessing.createSequentialGroup()
                                                .addComponent(textFieldMinWords, GroupLayout.PREFERRED_SIZE, 97,
                                                        GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(ComponentPlacement.RELATED)
                                                .addGroup(gl_panelPreProcessing.createParallelGroup(Alignment.LEADING)
                                                        .addGroup(gl_panelPreProcessing.createSequentialGroup()
                                                                .addComponent(chckbxUsePosTagging)
                                                                .addPreferredGap(ComponentPlacement.RELATED,
                                                                        GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                                .addComponent(btnPreProcess)))))
                                .addContainerGap()));
        gl_panelPreProcessing.setVerticalGroup(gl_panelPreProcessing.createParallelGroup(Alignment.LEADING)
                .addGroup(gl_panelPreProcessing.createSequentialGroup().addContainerGap()
                        .addGroup(gl_panelPreProcessing.createParallelGroup(Alignment.BASELINE)
                                .addComponent(textFieldInput, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
                                        GroupLayout.PREFERRED_SIZE)
                                .addComponent(lblSelectInput).addComponent(btnBrowse))
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addGroup(gl_panelPreProcessing.createParallelGroup(Alignment.BASELINE)
                                .addComponent(lblOutputFileName).addComponent(textFieldOutput,
                                GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
                                GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addGroup(gl_panelPreProcessing.createParallelGroup(Alignment.BASELINE))
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addGroup(gl_panelPreProcessing.createParallelGroup(Alignment.BASELINE)
                                .addComponent(comboBoxFormat, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
                                        GroupLayout.PREFERRED_SIZE)
                                .addComponent(lblFormat))
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addGroup(
                                gl_panelPreProcessing.createParallelGroup(Alignment.BASELINE).addComponent(lblMinWords)
                                        .addComponent(textFieldMinWords, GroupLayout.PREFERRED_SIZE,
                                                GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                        .addComponent(chckbxUsePosTagging).addComponent(btnPreProcess))
                        .addPreferredGap(ComponentPlacement.RELATED).addContainerGap(14, Short.MAX_VALUE)));
        panelPreProcessing.setLayout(gl_panelPreProcessing);

        JPanel panelLSATraining = new JPanel();
        panelLSATraining.setToolTipText(ResourceBundle.getBundle("utils.localization.messages")
                .getString("SemanticModelsTraining.pnlLSATraining"));
        panelLSATraining.setBackground(Color.WHITE);
        tabbedPane.addTab(LocalizationUtils.getLocalizedString(this.getClass(), "tabLSA"), null, panelLSATraining, null);

        JLabel lblLSAInputFile = new JLabel(ResourceBundle.getBundle("utils.localization.messages")
                .getString("SemanticModelsTraining.inputFile") + "*:");

        textFieldLSAFile = new JTextField();
        textFieldLSAFile.setText("resources/config");
        textFieldLSAFile.setColumns(10);

        JLabel lblTxtOnly = new JLabel("* " + ResourceBundle.getBundle("utils.localization.messages")
                .getString("SemanticModelsTraining.lblTextOnly1"));
        lblTxtOnly.setFont(new Font("SansSerif", Font.ITALIC, 10));

        JButton btnLSAFile = new JButton("...");
        btnLSAFile.addActionListener((ActionEvent e) -> {
            JFileChooser fc = new JFileChooser("resources/config");
            fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
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
                    return "Text documents (*.txt)";
                }
            });
            int returnVal = fc.showOpenDialog(SemanticModelsTraining.this);

            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File file = fc.getSelectedFile();
                textFieldLSAFile.setText(file.getPath());
            }
        });

        JLabel lblLSARank = new JLabel(ResourceBundle.getBundle("utils.localization.messages")
                .getString("SemanticModelsTraining.lblLSARank") + ":");

        textFieldLSARank = new JTextField();
        textFieldLSARank.setText("300");
        textFieldLSARank.setHorizontalAlignment(SwingConstants.RIGHT);
        textFieldLSARank.setColumns(10);

        JLabel lblLSAPowerInterations = new JLabel(ResourceBundle.getBundle("utils.localization.messages")
                .getString("SemanticModelsTraining.lblLSAPowerIterations") + ":");

        textFieldLSAPowerIterations = new JTextField();
        textFieldLSAPowerIterations.setText("1");
        textFieldLSAPowerIterations.setHorizontalAlignment(SwingConstants.RIGHT);
        textFieldLSAPowerIterations.setColumns(10);

        btnLSATrain = new JButton(ResourceBundle.getBundle("utils.localization.messages")
                .getString("SemanticModelsTraining.btnLSATrain"));
        btnLSATrain.addActionListener((ActionEvent e) -> {
            int k;
            int noPowerIterations;
            Lang lang;
            if (textFieldLSAFile.getText().equals("") || !textFieldLSAFile.getText().endsWith(".txt")
                    || !new File(textFieldLSAFile.getText()).exists()) {
                JOptionPane.showMessageDialog(SemanticModelsTraining.this,
                        LocalizationUtils.getGeneric("msgSelectInputFile") + "!", "Error",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
            File input = new File(textFieldLSAFile.getText());
            try {
                noPowerIterations = Integer.parseInt(textFieldLSAPowerIterations.getText());
            } catch (NumberFormatException exc) {
                noPowerIterations = 1;
            }
            try {
                k = Integer.parseInt(textFieldLSARank.getText());
            } catch (NumberFormatException exc) {
                k = 300;
            }
            lang = ReaderBenchView.RUNTIME_LANGUAGE;

            LSATrainingTask task = new LSATrainingTask(input, lang, k, noPowerIterations);
            task.execute();
        });

        GroupLayout gl_panelLSATraining = new GroupLayout(panelLSATraining);
        gl_panelLSATraining
                .setHorizontalGroup(
                        gl_panelLSATraining.createParallelGroup(Alignment.LEADING)
                                .addGroup(
                                        gl_panelLSATraining.createSequentialGroup()
                                                .addContainerGap().addGroup(gl_panelLSATraining
                                                        .createParallelGroup(Alignment.LEADING).addGroup(
                                                        Alignment.TRAILING, gl_panelLSATraining
                                                                .createSequentialGroup()
                                                                .addGroup(gl_panelLSATraining
                                                                        .createParallelGroup(Alignment.LEADING)
                                                                        .addGroup(gl_panelLSATraining
                                                                                .createSequentialGroup()
                                                                                .addComponent(lblLSAInputFile)
                                                                                .addPreferredGap(
                                                                                        ComponentPlacement.RELATED)
                                                                                .addComponent(textFieldLSAFile,
                                                                                        GroupLayout.DEFAULT_SIZE,
                                                                                        316, Short.MAX_VALUE))
                                                                        .addComponent(lblTxtOnly))
                                                                .addPreferredGap(ComponentPlacement.RELATED)
                                                                .addComponent(btnLSAFile))
                                                        .addGroup(Alignment.TRAILING, gl_panelLSATraining
                                                                .createSequentialGroup()
                                                                .addGroup(gl_panelLSATraining
                                                                        .createParallelGroup(Alignment.LEADING)
                                                                        .addComponent(lblLSARank))
                                                                .addGap(29)
                                                                .addGroup(gl_panelLSATraining
                                                                        .createParallelGroup(Alignment.LEADING)
                                                                        .addGroup(gl_panelLSATraining
                                                                                .createSequentialGroup()
                                                                                .addGroup(gl_panelLSATraining
                                                                                        .createParallelGroup(
                                                                                                Alignment.LEADING)
                                                                                        .addComponent(textFieldLSARank,
                                                                                                Alignment.TRAILING,
                                                                                                GroupLayout.DEFAULT_SIZE,
                                                                                                66, Short.MAX_VALUE))
                                                                                .addPreferredGap(
                                                                                        ComponentPlacement.RELATED)
                                                                                .addGroup(gl_panelLSATraining
                                                                                        .createParallelGroup(
                                                                                                Alignment.TRAILING)
                                                                                        .addGroup(gl_panelLSATraining
                                                                                                .createSequentialGroup()
                                                                                                .addComponent(
                                                                                                        lblLSAPowerInterations)
                                                                                                .addPreferredGap(
                                                                                                        ComponentPlacement.RELATED)
                                                                                                .addComponent(
                                                                                                        textFieldLSAPowerIterations,
                                                                                                        GroupLayout.PREFERRED_SIZE,
                                                                                                        91,
                                                                                                        GroupLayout.PREFERRED_SIZE))
                                                                                        .addComponent(btnLSATrain))))))
                                                .addContainerGap()));
        gl_panelLSATraining
                .setVerticalGroup(
                        gl_panelLSATraining.createParallelGroup(Alignment.LEADING)
                                .addGroup(gl_panelLSATraining.createSequentialGroup().addContainerGap()
                                        .addGroup(gl_panelLSATraining.createParallelGroup(Alignment.BASELINE)
                                                .addComponent(lblLSAInputFile).addComponent(btnLSAFile)
                                                .addComponent(textFieldLSAFile, GroupLayout.PREFERRED_SIZE,
                                                        GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                                        .addPreferredGap(ComponentPlacement.RELATED).addComponent(lblTxtOnly)
                                        .addPreferredGap(ComponentPlacement.RELATED)
                                        .addGroup(gl_panelLSATraining.createParallelGroup(Alignment.BASELINE))
                                        .addPreferredGap(ComponentPlacement.RELATED)
                                        .addGroup(gl_panelLSATraining.createParallelGroup(Alignment.BASELINE)
                                                .addComponent(lblLSARank).addComponent(
                                                textFieldLSAPowerIterations, GroupLayout.PREFERRED_SIZE,
                                                GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                                .addComponent(lblLSAPowerInterations).addComponent(textFieldLSARank,
                                                GroupLayout.PREFERRED_SIZE,
                                                GroupLayout.DEFAULT_SIZE,
                                                GroupLayout.PREFERRED_SIZE))
                                        .addPreferredGap(ComponentPlacement.RELATED)
                                        .addGroup(gl_panelLSATraining.createParallelGroup(Alignment.TRAILING).addGroup(
                                                gl_panelLSATraining.createSequentialGroup().addGroup(gl_panelLSATraining
                                                        .createParallelGroup(Alignment.BASELINE))
                                                        .addPreferredGap(ComponentPlacement.RELATED))
                                                .addComponent(btnLSATrain))
                                        .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));
        panelLSATraining.setLayout(gl_panelLSATraining);

        JPanel panelLDATraining = new JPanel();
        panelLDATraining.setToolTipText(ResourceBundle.getBundle("utils.localization.messages")
                .getString("SemanticModelsTraining.pnlLDATraining") + "\n");
        panelLDATraining.setBackground(Color.WHITE);
        tabbedPane.addTab(LocalizationUtils.getLocalizedString(this.getClass(), "tabLDA"), null, panelLDATraining, null);

        JLabel lblLDAInputDirectory = new JLabel(ResourceBundle.getBundle("utils.localization.messages")
                .getString("SemanticModelsTraining.inputDirectory") + "*:");

        JButton btnLDADirectory = new JButton("...");
        btnLDADirectory.addActionListener((ActionEvent e) -> {
            JFileChooser fc = new JFileChooser("resources/config");
            fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            int returnVal = fc.showOpenDialog(SemanticModelsTraining.this);

            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File file = fc.getSelectedFile();
                textFieldLDADirectory.setText(file.getPath());
            }
        });

        textFieldLDADirectory = new JTextField();
        textFieldLDADirectory.setText("resources/config");
        textFieldLDADirectory.setColumns(10);

        JLabel lblAllTxt = new JLabel("* " + ResourceBundle.getBundle("utils.localization.messages")
                .getString("SemanticModelsTraining.lblAllTxt"));
        lblAllTxt.setFont(new Font("SansSerif", Font.ITALIC, 10));

        JLabel lblLDANoTopics = new JLabel(ResourceBundle.getBundle("utils.localization.messages")
                .getString("SemanticModelsTraining.lblLDANoTopics") + ":");

        textFieldLDANoTopics = new JTextField();
        textFieldLDANoTopics.setHorizontalAlignment(SwingConstants.RIGHT);
        textFieldLDANoTopics.setText("100");
        textFieldLDANoTopics.setColumns(10);

        JLabel lblLDANoIterations = new JLabel(ResourceBundle.getBundle("utils.localization.messages")
                .getString("SemanticModelsTraining.lblLDANoIterations") + ":");

        textFieldLDANoIterations = new JTextField();
        textFieldLDANoIterations.setHorizontalAlignment(SwingConstants.RIGHT);
        textFieldLDANoIterations.setText("10000");
        textFieldLDANoIterations.setColumns(10);

        JLabel lblLDANoThreads = new JLabel(ResourceBundle.getBundle("utils.localization.messages")
                .getString("SemanticModelsTraining.lblLDANoThreads") + ":");

        textFieldLDANoThreads = new JTextField();
        textFieldLDANoThreads.setHorizontalAlignment(SwingConstants.RIGHT);
        textFieldLDANoThreads.setText("2");
        textFieldLDANoThreads.setColumns(10);

        btnLDATrain = new JButton(ResourceBundle.getBundle("utils.localization.messages")
                .getString("SemanticModelsTraining.btnLDATrain"));
        btnLDATrain.addActionListener((ActionEvent e) -> {
            int noTopics;
            int noIterations;
            int noThreads;
            if (textFieldLDADirectory.getText().equals("")) {
                JOptionPane.showMessageDialog(SemanticModelsTraining.this,
                        LocalizationUtils.getGeneric("msgSelectInputFolder") + "!", "Error",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
            try {
                noTopics = Integer.parseInt(textFieldLDANoTopics.getText());
            } catch (NumberFormatException exc) {
                noTopics = 100;
            }
            try {
                noIterations = Integer.parseInt(textFieldLDANoIterations.getText());
            } catch (NumberFormatException exc) {
                noIterations = 10000;
            }
            try {
                noThreads = Integer.parseInt(textFieldLDANoThreads.getText());
            } catch (NumberFormatException exc) {
                noThreads = 2;
            }
            Lang lang = ReaderBenchView.RUNTIME_LANGUAGE;

            LDATrainingTask task = new LDATrainingTask(textFieldLDADirectory.getText(), lang, noTopics, noThreads,
                    noIterations);
            task.execute();
        });
        GroupLayout gl_panelLDATraining = new GroupLayout(panelLDATraining);
        gl_panelLDATraining.setHorizontalGroup(
                gl_panelLDATraining.createParallelGroup(Alignment.LEADING).addGroup(gl_panelLDATraining
                        .createSequentialGroup().addContainerGap().addGroup(gl_panelLDATraining
                                .createParallelGroup(
                                        Alignment.TRAILING)
                                .addGroup(gl_panelLDATraining.createSequentialGroup()
                                        .addGroup(gl_panelLDATraining
                                                .createParallelGroup(Alignment.TRAILING).addGroup(Alignment.LEADING,
                                                gl_panelLDATraining.createSequentialGroup()
                                                        .addComponent(lblLDAInputDirectory)
                                                        .addPreferredGap(ComponentPlacement.RELATED)
                                                        .addComponent(textFieldLDADirectory,
                                                                GroupLayout.DEFAULT_SIZE, 282, Short.MAX_VALUE))
                                                .addComponent(lblAllTxt))
                                        .addPreferredGap(ComponentPlacement.RELATED).addComponent(btnLDADirectory))
                                .addGroup(gl_panelLDATraining.createSequentialGroup()
                                        .addGroup(gl_panelLDATraining.createParallelGroup(Alignment.LEADING)
                                                .addComponent(lblLDANoTopics)
                                                .addComponent(lblLDANoThreads))
                                        .addGap(29)
                                        .addGroup(gl_panelLDATraining.createParallelGroup(Alignment.LEADING)
                                                .addGroup(gl_panelLDATraining.createSequentialGroup()
                                                        .addGroup(gl_panelLDATraining
                                                                .createParallelGroup(Alignment.TRAILING)
                                                                .addComponent(textFieldLDANoTopics,
                                                                        GroupLayout.DEFAULT_SIZE, 88, Short.MAX_VALUE)
                                                                .addComponent(textFieldLDANoThreads, 0, 0,
                                                                        Short.MAX_VALUE))
                                                        .addPreferredGap(ComponentPlacement.RELATED)
                                                        .addGroup(gl_panelLDATraining
                                                                .createParallelGroup(Alignment.TRAILING)
                                                                .addGroup(gl_panelLDATraining.createSequentialGroup()
                                                                        .addComponent(lblLDANoIterations).addGap(61)
                                                                        .addComponent(textFieldLDANoIterations,
                                                                                GroupLayout.PREFERRED_SIZE, 91,
                                                                                GroupLayout.PREFERRED_SIZE))
                                                                .addComponent(btnLDATrain))))))
                        .addContainerGap()));
        gl_panelLDATraining
                .setVerticalGroup(
                        gl_panelLDATraining.createParallelGroup(Alignment.LEADING)
                                .addGroup(gl_panelLDATraining.createSequentialGroup().addContainerGap()
                                        .addGroup(gl_panelLDATraining.createParallelGroup(Alignment.BASELINE)
                                                .addComponent(lblLDAInputDirectory).addComponent(btnLDADirectory)
                                                .addComponent(textFieldLDADirectory, GroupLayout.PREFERRED_SIZE,
                                                        GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                                        .addPreferredGap(ComponentPlacement.RELATED).addComponent(lblAllTxt)
                                        .addPreferredGap(ComponentPlacement.RELATED)
                                        .addGroup(gl_panelLDATraining.createParallelGroup(Alignment.BASELINE))
                                        .addPreferredGap(ComponentPlacement.RELATED)
                                        .addGroup(gl_panelLDATraining.createParallelGroup(Alignment.BASELINE)
                                                .addComponent(lblLDANoTopics).addComponent(
                                                textFieldLDANoTopics, GroupLayout.PREFERRED_SIZE,
                                                GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                                .addComponent(lblLDANoIterations)
                                                .addComponent(textFieldLDANoIterations, GroupLayout.PREFERRED_SIZE,
                                                        GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                                        .addPreferredGap(ComponentPlacement.RELATED)
                                        .addGroup(gl_panelLDATraining.createParallelGroup(Alignment.BASELINE)
                                                .addComponent(textFieldLDANoThreads, GroupLayout.PREFERRED_SIZE,
                                                        GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                                .addComponent(lblLDANoThreads).addComponent(btnLDATrain))
                                        .addContainerGap(40, Short.MAX_VALUE)));
        panelLDATraining.setLayout(gl_panelLDATraining);

        JPanel panelWord2VecTraining = new JPanel();
        panelWord2VecTraining.setToolTipText(ResourceBundle.getBundle("utils.localization.messages")
                .getString("SemanticModelsTraining.lblWord2VecTraining"));
        panelWord2VecTraining.setBackground(Color.WHITE);
        tabbedPane.addTab(LocalizationUtils.getLocalizedString(this.getClass(), "tabWord2Vec"), null, panelWord2VecTraining, null);

        JLabel lblWord2VecInputFile = new JLabel(ResourceBundle.getBundle("utils.localization.messages")
                .getString("SemanticModelsTraining.inputFile") + "*:");

        textFieldWord2VecFile = new JTextField();
        textFieldWord2VecFile.setText("resources/config");
        textFieldWord2VecFile.setColumns(10);

        JLabel lblTxtOnly2 = new JLabel("* "
                + ResourceBundle.getBundle("utils.localization.messages")
                        .getString("SemanticModelsTraining.lblTextOnly2"));
        lblTxtOnly2.setFont(new Font("SansSerif", Font.ITALIC, 10));

        JButton btnWord2VecFile = new JButton("...");
        btnWord2VecFile.addActionListener((ActionEvent e) -> {
            JFileChooser fc = new JFileChooser("resources/config");
            fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
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
                    return "Text documents (*.txt)";
                }
            });
            int returnVal = fc.showOpenDialog(SemanticModelsTraining.this);

            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File file = fc.getSelectedFile();
                textFieldWord2VecFile.setText(file.getPath());
            }
        });

        JLabel lblWord2VecEpochs = new JLabel(ResourceBundle.getBundle("utils.localization.messages")
                .getString("SemanticModelsTraining.lblWord2VecEpochs") + ":");

        textFieldWord2VecEpochs = new JTextField();
        textFieldWord2VecEpochs.setText("6");
        textFieldWord2VecEpochs.setHorizontalAlignment(SwingConstants.RIGHT);
        textFieldWord2VecEpochs.setColumns(10);

        JLabel lblWord2VecLayerSize = new JLabel(ResourceBundle.getBundle("utils.localization.messages")
                .getString("SemanticModelsTraining.lblWord2VecLayerSize") + ":");

        textFieldWord2VecLayerSize = new JTextField();
        textFieldWord2VecLayerSize.setText("300");
        textFieldWord2VecLayerSize.setHorizontalAlignment(SwingConstants.RIGHT);
        textFieldWord2VecLayerSize.setColumns(10);

        btnWord2VecTrain = new JButton(ResourceBundle.getBundle("utils.localization.messages")
                .getString("SemanticModelsTraining.btnWord2VecTrain"));
        btnWord2VecTrain.addActionListener((ActionEvent e) -> {
            int layerSize;
            int noEpochs;
            if (textFieldWord2VecFile.getText().equals("") || !textFieldWord2VecFile.getText().endsWith(".txt")
                    || !new File(textFieldWord2VecFile.getText()).exists()) {
                JOptionPane.showMessageDialog(SemanticModelsTraining.this,
                        LocalizationUtils.getGeneric("msgSelectInputFile") + "!", "Error",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
            try {
                noEpochs = Integer.parseInt(textFieldWord2VecEpochs.getText());
            } catch (NumberFormatException exc) {
                noEpochs = 6;
            }
            try {
                layerSize = Integer.parseInt(textFieldWord2VecLayerSize.getText());
            } catch (NumberFormatException exc) {
                layerSize = 300;
            }

            Word2VecTrainingTask task = new Word2VecTrainingTask(textFieldWord2VecFile.getText(), noEpochs, layerSize);
            task.execute();
        });

        GroupLayout gl_panelWord2VecTraining = new GroupLayout(panelWord2VecTraining);
        gl_panelWord2VecTraining
                .setHorizontalGroup(
                        gl_panelWord2VecTraining.createParallelGroup(Alignment.LEADING)
                                .addGroup(
                                        gl_panelWord2VecTraining.createSequentialGroup()
                                                .addContainerGap().addGroup(gl_panelWord2VecTraining
                                                        .createParallelGroup(Alignment.LEADING).addGroup(
                                                        Alignment.TRAILING, gl_panelWord2VecTraining
                                                                .createSequentialGroup()
                                                                .addGroup(gl_panelWord2VecTraining
                                                                        .createParallelGroup(Alignment.LEADING)
                                                                        .addGroup(gl_panelWord2VecTraining
                                                                                .createSequentialGroup()
                                                                                .addComponent(lblWord2VecInputFile)
                                                                                .addPreferredGap(
                                                                                        ComponentPlacement.RELATED)
                                                                                .addComponent(textFieldWord2VecFile,
                                                                                        GroupLayout.DEFAULT_SIZE,
                                                                                        316, Short.MAX_VALUE))
                                                                        .addComponent(lblTxtOnly2))
                                                                .addPreferredGap(ComponentPlacement.RELATED)
                                                                .addComponent(btnWord2VecFile))
                                                        .addGroup(Alignment.TRAILING, gl_panelWord2VecTraining
                                                                .createSequentialGroup()
                                                                .addGroup(gl_panelWord2VecTraining
                                                                        .createParallelGroup(Alignment.LEADING)
                                                                        .addComponent(lblWord2VecEpochs))
                                                                .addGap(29)
                                                                .addGroup(gl_panelWord2VecTraining
                                                                        .createParallelGroup(Alignment.LEADING)
                                                                        .addGroup(gl_panelWord2VecTraining
                                                                                .createSequentialGroup()
                                                                                .addGroup(gl_panelWord2VecTraining
                                                                                        .createParallelGroup(
                                                                                                Alignment.LEADING)
                                                                                        .addComponent(textFieldWord2VecEpochs,
                                                                                                Alignment.TRAILING,
                                                                                                GroupLayout.DEFAULT_SIZE,
                                                                                                66, Short.MAX_VALUE))
                                                                                .addPreferredGap(
                                                                                        ComponentPlacement.RELATED)
                                                                                .addGroup(gl_panelWord2VecTraining
                                                                                        .createParallelGroup(
                                                                                                Alignment.TRAILING)
                                                                                        .addGroup(gl_panelWord2VecTraining
                                                                                                .createSequentialGroup()
                                                                                                .addComponent(
                                                                                                        lblWord2VecLayerSize)
                                                                                                .addPreferredGap(
                                                                                                        ComponentPlacement.RELATED)
                                                                                                .addComponent(
                                                                                                        textFieldWord2VecLayerSize,
                                                                                                        GroupLayout.PREFERRED_SIZE,
                                                                                                        91,
                                                                                                        GroupLayout.PREFERRED_SIZE))
                                                                                        .addComponent(btnWord2VecTrain))))))
                                                .addContainerGap()));
        gl_panelWord2VecTraining
                .setVerticalGroup(
                        gl_panelWord2VecTraining.createParallelGroup(Alignment.LEADING)
                                .addGroup(gl_panelWord2VecTraining.createSequentialGroup().addContainerGap()
                                        .addGroup(gl_panelWord2VecTraining.createParallelGroup(Alignment.BASELINE)
                                                .addComponent(lblWord2VecInputFile).addComponent(btnWord2VecFile)
                                                .addComponent(textFieldWord2VecFile, GroupLayout.PREFERRED_SIZE,
                                                        GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                                        .addPreferredGap(ComponentPlacement.RELATED).addComponent(lblTxtOnly2)
                                        .addPreferredGap(ComponentPlacement.RELATED)
                                        .addGroup(gl_panelWord2VecTraining.createParallelGroup(Alignment.BASELINE))
                                        .addPreferredGap(ComponentPlacement.RELATED)
                                        .addGroup(gl_panelWord2VecTraining.createParallelGroup(Alignment.BASELINE)
                                                .addComponent(lblWord2VecEpochs).addComponent(
                                                textFieldWord2VecLayerSize, GroupLayout.PREFERRED_SIZE,
                                                GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                                .addComponent(lblWord2VecLayerSize).addComponent(textFieldWord2VecEpochs,
                                                GroupLayout.PREFERRED_SIZE,
                                                GroupLayout.DEFAULT_SIZE,
                                                GroupLayout.PREFERRED_SIZE))
                                        .addPreferredGap(ComponentPlacement.RELATED)
                                        .addGroup(gl_panelWord2VecTraining.createParallelGroup(Alignment.TRAILING).addGroup(
                                                gl_panelWord2VecTraining.createSequentialGroup().addGroup(gl_panelWord2VecTraining
                                                        .createParallelGroup(Alignment.BASELINE))
                                                        .addPreferredGap(ComponentPlacement.RELATED))
                                                .addComponent(btnWord2VecTrain))
                                        .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));
        panelWord2VecTraining.setLayout(gl_panelWord2VecTraining);
    }
}
