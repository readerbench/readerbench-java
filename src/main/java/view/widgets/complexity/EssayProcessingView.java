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
package view.widgets.complexity;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.io.File;

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
import javax.swing.JTextField;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;

import data.Lang;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.util.Exceptions;
import services.complexity.DataGathering;
import services.semanticModels.ISemanticModel;
import services.semanticModels.LDA.LDA;
import services.semanticModels.LSA.LSA;
import services.semanticModels.word2vec.Word2VecModel;
import utils.LocalizationUtils;
import view.widgets.ReaderBenchView;

public class EssayProcessingView extends JFrame {

    private static final long serialVersionUID = 8894652868238113117L;

    public static final String C_BASE_FOLDER_NAME = "grade";

    static final Logger LOGGER = Logger.getLogger("");

    private final JPanel contentPane;
    private JTextField textFieldPath;
    private JComboBox<String> comboBoxLSA;
    private JComboBox<String> comboBoxLDA;
    private JComboBox<String> comboBoxWord2Vec;
    private JButton btnRun;
    private JCheckBox chckbxUsePosTagging;
    private Lang lang = null;

    private class Task extends SwingWorker<Void, Void> {

        private final String path;
        private final String pathToLSA;
        private final String pathToLDA;
        private final String pathToWord2Vec;
        private final Lang lang;
        private final boolean usePOSTagging;
        private final boolean computeDialogism;

        public Task(String path, String pathToLSA, String pathToLDA, String pathToWord2Vec, Lang lang, boolean usePOSTagging,
                boolean computeDialogism) {
            super();
            this.path = path;
            this.pathToLSA = pathToLSA;
            this.pathToLDA = pathToLDA;
            this.pathToWord2Vec = pathToWord2Vec;
            this.lang = lang;
            this.usePOSTagging = usePOSTagging;
            this.computeDialogism = computeDialogism;
        }

        @Override
        public Void doInBackground() {
            try {
                LOGGER.log(Level.INFO, "Processing all documents found in {0}", path);
                try {
                    LSA lsa = LSA.loadLSA(pathToLSA, lang);
                    LDA lda = LDA.loadLDA(pathToLDA, lang);
                    Word2VecModel word2vec = Word2VecModel.loadWord2Vec(pathToWord2Vec, lang);
                    List<ISemanticModel> models = new ArrayList<>();
                    models.add(lsa);
                    models.add(lda);
                    models.add(word2vec);
                    DataGathering.processTexts(path, "", true, models, lang, usePOSTagging, computeDialogism);
                } catch (IOException e) {
                    Exceptions.printStackTrace(e);
                }
            } catch (Exception e) {
                Exceptions.printStackTrace(e);
            }
            return null;
        }

        @Override
        public void done() {
            Toolkit.getDefaultToolkit().beep();
            btnRun.setEnabled(true);
            setCursor(null); // turn off the wait cursor
        }
    }

    /**
     * Create the frame.
     */
    public EssayProcessingView() {
        super.setTitle("ReaderBench - " + LocalizationUtils.getTitle(this.getClass()));
        super.setResizable(false);
        super.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        super.setBounds(100, 100, 570, 240);
        contentPane = new JPanel();
        contentPane.setBackground(Color.WHITE);
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        super.setContentPane(contentPane);

        JLabel lblPath = new JLabel(LocalizationUtils.getGeneric("path"));

        JLabel lblLsaVectorSpace = new JLabel(LocalizationUtils.getGeneric("LSA") + ":");
        JLabel lblLdaModel = new JLabel(LocalizationUtils.getGeneric("LDA") + ":");
        JLabel lblWord2VecModel = new JLabel(LocalizationUtils.getGeneric("word2vec") + ":");

        comboBoxLSA = new JComboBox<>();
        comboBoxLSA.setEnabled(false);
        comboBoxLDA = new JComboBox<>();
        comboBoxLDA.setEnabled(false);
        comboBoxWord2Vec = new JComboBox<>();
        comboBoxWord2Vec.setEnabled(false);

        textFieldPath = new JTextField();
        textFieldPath.setText("");
        textFieldPath.setColumns(10);

        JButton btnSearch = new JButton("...");
        btnSearch.addActionListener((ActionEvent e) -> {
            JFileChooser fc = new JFileChooser(new File("in"));
            fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            int returnVal = fc.showOpenDialog(EssayProcessingView.this);

            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File file = fc.getSelectedFile();
                textFieldPath.setText(file.getPath());
            }
        });

        btnRun = new JButton(LocalizationUtils.getLocalizedString(this.getClass(), "btnGenerateMeasurementsFile"));
        btnRun.setEnabled(false);
        btnRun.addActionListener((ActionEvent e) -> {
            if (!textFieldPath.getText().equals("")) {
                Task task = new Task(textFieldPath.getText(), (String) comboBoxLSA.getSelectedItem(),
                        (String) comboBoxLDA.getSelectedItem(), (String) comboBoxWord2Vec.getSelectedItem(),
                        EssayProcessingView.this.lang, chckbxUsePosTagging.isSelected(),
                        chckbxUsePosTagging.isSelected());
                btnRun.setEnabled(false);
                setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                task.execute();
            } else {
                JOptionPane.showMessageDialog(EssayProcessingView.this,
                        LocalizationUtils.getGeneric("msgSelectInputFolder") + "!",
                        "Error", JOptionPane.WARNING_MESSAGE);
            }
        });

        chckbxUsePosTagging = new JCheckBox(LocalizationUtils.getGeneric("usePOStagging"));
        chckbxUsePosTagging.setSelected(true);

        lang = ReaderBenchView.RUNTIME_LANGUAGE;

        GroupLayout gl_contentPane = new GroupLayout(contentPane);
        gl_contentPane
                .setHorizontalGroup(gl_contentPane.createParallelGroup(Alignment.LEADING).addGroup(
                        gl_contentPane.createSequentialGroup().addContainerGap().addGroup(gl_contentPane
                                .createParallelGroup(Alignment.LEADING)
                                .addGroup(gl_contentPane.createSequentialGroup()
                                        .addGroup(gl_contentPane.createParallelGroup(Alignment.LEADING)
                                                .addComponent(lblPath).addComponent(lblLsaVectorSpace).addComponent(
                                                lblLdaModel).addComponent(lblWord2VecModel))
                                        .addGap(13)
                                        .addGroup(gl_contentPane.createParallelGroup(Alignment.TRAILING)
                                                .addComponent(comboBoxLDA, 0, 420, Short.MAX_VALUE)
                                                .addGroup(gl_contentPane.createSequentialGroup()
                                                        .addComponent(textFieldPath, GroupLayout.DEFAULT_SIZE, 372,
                                                                Short.MAX_VALUE)
                                                        .addPreferredGap(ComponentPlacement.RELATED)
                                                        .addComponent(btnSearch, GroupLayout.PREFERRED_SIZE, 41,
                                                                GroupLayout.PREFERRED_SIZE))
                                                .addComponent(comboBoxLSA, 0, 420, Short.MAX_VALUE)
                                                .addComponent(comboBoxWord2Vec, 0, 420, Short.MAX_VALUE))
                                        .addGap(6))
                                .addGroup(gl_contentPane.createSequentialGroup().addComponent(chckbxUsePosTagging)
                                        .addPreferredGap(ComponentPlacement.RELATED, 165, Short.MAX_VALUE)
                                        .addComponent(btnRun, GroupLayout.PREFERRED_SIZE, 242,
                                                GroupLayout.PREFERRED_SIZE)
                                        .addContainerGap())
                                .addGroup(gl_contentPane.createSequentialGroup()
                                        .addContainerGap(482, Short.MAX_VALUE)))));
        gl_contentPane.setVerticalGroup(gl_contentPane.createParallelGroup(Alignment.LEADING).addGroup(gl_contentPane
                .createSequentialGroup().addContainerGap()
                .addGroup(gl_contentPane.createParallelGroup(Alignment.BASELINE))
                .addPreferredGap(ComponentPlacement.RELATED)
                .addGroup(gl_contentPane.createParallelGroup(Alignment.BASELINE).addComponent(lblPath)
                        .addComponent(btnSearch).addComponent(textFieldPath, GroupLayout.PREFERRED_SIZE,
                        GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(ComponentPlacement.RELATED)
                .addGroup(gl_contentPane.createParallelGroup(Alignment.BASELINE).addComponent(lblLsaVectorSpace)
                        .addComponent(comboBoxLSA, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
                                GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(ComponentPlacement.RELATED)
                .addGroup(gl_contentPane.createParallelGroup(Alignment.BASELINE)
                        .addComponent(comboBoxLDA, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
                                GroupLayout.PREFERRED_SIZE)
                        .addComponent(lblLdaModel))
                .addPreferredGap(ComponentPlacement.RELATED)
                .addGroup(gl_contentPane.createParallelGroup(Alignment.BASELINE).addComponent(lblWord2VecModel)
                        .addComponent(comboBoxWord2Vec, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
                                GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(ComponentPlacement.RELATED)
                .addGroup(gl_contentPane
                        .createParallelGroup(Alignment.BASELINE).addComponent(chckbxUsePosTagging).addComponent(btnRun))
                .addContainerGap(38, Short.MAX_VALUE)));
        contentPane.setLayout(gl_contentPane);
    }
}
