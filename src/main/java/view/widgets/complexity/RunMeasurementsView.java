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
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileFilter;

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
import edu.stanford.nlp.util.Timing;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Logger;
import services.complexity.DataGathering;
import services.semanticModels.ISemanticModel;
import services.semanticModels.LDA.LDA;
import services.semanticModels.LSA.LSA;
import utils.localization.LocalizationUtils;
import view.widgets.ReaderBenchView;

public class RunMeasurementsView extends JFrame {

    private static final long serialVersionUID = 8894652868238113117L;

    public static final String C_BASE_FOLDER_NAME = "grade";

    static Logger logger = Logger.getLogger("");

    private JPanel contentPane;
    private JTextField textFieldPath;
    private JComboBox<String> comboBoxLSA;
    private JComboBox<String> comboBoxLDA;
    private JComboBox<String> comboBoxLanguage;
    private JButton btnRun;
    private JCheckBox chckbxUsePosTagging;
    private Lang lang = null;

    private class Task extends SwingWorker<Void, Void> {

        private String path;
        private String pathToLSA;
        private String pathToLDA;
        private Lang lang;
        private boolean usePOSTagging;

        public Task(String path, String pathToLSA, String pathToLDA, Lang lang, boolean usePOSTagging) {
            super();
            this.path = path;
            this.pathToLSA = pathToLSA;
            this.pathToLDA = pathToLDA;
            this.lang = lang;
            this.usePOSTagging = usePOSTagging;
        }

        public Void doInBackground() {
            try {
                logger.info("Processing all documents found in " + path);
                try {
                    LSA lsa = LSA.loadLSA(pathToLSA, lang);
                    LDA lda = LDA.loadLDA(pathToLDA, lang);
                    List<ISemanticModel> models = new ArrayList<>();
                    models.add(lsa);
                    models.add(lda);
                    
                    // determine number of classes
                    int noGrades = (new File(path)).listFiles(new FileFilter() {
                        public boolean accept(File pathname) {
                            if (pathname.isDirectory()) {
                                return true;
                            }
                            return false;
                        }
                    }).length;

                    logger.info("Found " + noGrades + " document grade levels");
                    Timing totalTiming = new Timing();
                    totalTiming.start();

                    DataGathering.writeHeader(path, lang);

                    for (int gradeLevel = 1; gradeLevel <= noGrades; gradeLevel++) {
                        DataGathering.processTexts(path + "/" + C_BASE_FOLDER_NAME + gradeLevel, path, gradeLevel,
                                false, models, lang, usePOSTagging, usePOSTagging);
                    }

                    logger.info("Finished processing all documents");
                    logger.info("Time elasped:" + totalTiming.report() / 1000 + "s (" + totalTiming.report() / 1000 / 60
                            + "min)");
                } catch (Exception e) {
                    System.err.println("Error: " + e.getMessage());
                    e.printStackTrace();
                }
            } catch (Exception e) {
                e.printStackTrace();
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
    public RunMeasurementsView() {
        setTitle("ReaderBench - "
                + ResourceBundle.getBundle("utils.localization.messages")
                .getString("RunMeasurementsView.panelRunMeasurements.title"));
        setResizable(false);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setBounds(100, 100, 560, 220);
        contentPane = new JPanel();
        contentPane.setBackground(Color.WHITE);
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        setContentPane(contentPane);

        JLabel lblPath = new JLabel("Path:");

        JLabel lblLsaVectorSpace = new JLabel(ResourceBundle.getBundle("utils.localization.messages")
                .getString("RunMeasurementsView.lblLSAspace.text") + ":");

        JLabel lblLdaModel = new JLabel(ResourceBundle.getBundle("utils.localization.messages")
                .getString("RunMeasurementsView.lblLDAvector.text") + ":");

        comboBoxLSA = new JComboBox<String>();
        comboBoxLSA.setEnabled(false);
        comboBoxLDA = new JComboBox<String>();
        comboBoxLDA.setEnabled(false);

        textFieldPath = new JTextField();
        textFieldPath.setText("");
        textFieldPath.setColumns(10);

        JButton btnSearch = new JButton("...");
        btnSearch.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JFileChooser fc = new JFileChooser(new File("resources/in"));
                fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                int returnVal = fc.showOpenDialog(RunMeasurementsView.this);

                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    File file = fc.getSelectedFile();
                    textFieldPath.setText(file.getPath());
                }
            }
        });

        btnRun = new JButton("Run");
        btnRun.setEnabled(false);
        btnRun.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (!textFieldPath.getText().equals("")) {
                    Task task = new Task(textFieldPath.getText(), (String) comboBoxLSA.getSelectedItem(),
                            (String) comboBoxLDA.getSelectedItem(), RunMeasurementsView.this.lang,
                            chckbxUsePosTagging.isSelected());
                    btnRun.setEnabled(false);
                    setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                    task.execute();
                } else {
                    JOptionPane
                            .showMessageDialog(RunMeasurementsView.this,
                                    ResourceBundle.getBundle("utils.localization.messages")
                .getString("RunMeasurementsView.msgSelectAnotherDirectory.text") + "!",
                                    "Error", JOptionPane.WARNING_MESSAGE);
                }
            }
        });

        chckbxUsePosTagging = new JCheckBox(ResourceBundle.getBundle("utils.localization.messages")
                .getString("RunMeasurementsView.boxUsePOSTagging.text"));
        chckbxUsePosTagging.setSelected(true);

        JLabel lblLanguage = new JLabel(ResourceBundle.getBundle("utils.localization.messages")
                .getString("RunMeasurementsView.lblLanguage.text") + ":");

        comboBoxLanguage = new JComboBox<>();
        comboBoxLanguage.addItem("<< " + ResourceBundle.getBundle("utils.localization.messages")
                .getString("RunMeasurementsView.cmbboxPleaseSelectLanguage") + " >>");
        for (Lang l : Lang.values()) {
            comboBoxLanguage.addItem(l.getDescription());
        }

        comboBoxLanguage.addActionListener((ActionEvent e) -> {
            if (comboBoxLanguage.getSelectedIndex() > 0) {
                // set final analysis language
                lang = Lang.getLang((String) comboBoxLanguage.getSelectedItem());

                ReaderBenchView.updateComboLanguage(comboBoxLSA, comboBoxLDA, lang);

                btnRun.setEnabled(true);
            }
        });

        GroupLayout gl_contentPane = new GroupLayout(contentPane);
        gl_contentPane
                .setHorizontalGroup(gl_contentPane.createParallelGroup(Alignment.LEADING).addGroup(
                        gl_contentPane.createSequentialGroup().addContainerGap().addGroup(gl_contentPane
                                .createParallelGroup(Alignment.TRAILING)
                                .addGroup(gl_contentPane.createSequentialGroup()
                                        .addGroup(gl_contentPane.createParallelGroup(Alignment.LEADING)
                                                .addComponent(lblPath).addComponent(lblLsaVectorSpace).addComponent(
                                                lblLdaModel))
                                        .addGap(13)
                                        .addGroup(gl_contentPane.createParallelGroup(Alignment.TRAILING)
                                                .addComponent(comboBoxLDA, 0, 404, Short.MAX_VALUE)
                                                .addGroup(gl_contentPane.createSequentialGroup()
                                                        .addComponent(textFieldPath, GroupLayout.DEFAULT_SIZE, 357,
                                                                Short.MAX_VALUE)
                                                        .addPreferredGap(ComponentPlacement.RELATED)
                                                        .addComponent(btnSearch, GroupLayout.PREFERRED_SIZE, 41,
                                                                GroupLayout.PREFERRED_SIZE))
                                                .addComponent(comboBoxLSA, 0, 404, Short.MAX_VALUE).addComponent(
                                                comboBoxLanguage, Alignment.LEADING, 0, 404, Short.MAX_VALUE))
                                        .addGap(6))
                                .addGroup(Alignment.LEADING,
                                        gl_contentPane.createSequentialGroup().addComponent(chckbxUsePosTagging)
                                        .addPreferredGap(ComponentPlacement.RELATED, 319, Short.MAX_VALUE)
                                        .addComponent(btnRun, GroupLayout.PREFERRED_SIZE, 73,
                                                GroupLayout.PREFERRED_SIZE)
                                        .addContainerGap())
                                .addGroup(gl_contentPane.createSequentialGroup().addComponent(lblLanguage)
                                        .addContainerGap(469, Short.MAX_VALUE)))));
        gl_contentPane.setVerticalGroup(gl_contentPane.createParallelGroup(Alignment.LEADING).addGroup(gl_contentPane
                .createSequentialGroup().addContainerGap()
                .addGroup(gl_contentPane.createParallelGroup(Alignment.BASELINE).addComponent(lblLanguage).addComponent(
                        comboBoxLanguage, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
                        GroupLayout.PREFERRED_SIZE))
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
                .addPreferredGap(ComponentPlacement.RELATED).addGroup(gl_contentPane
                .createParallelGroup(Alignment.BASELINE).addComponent(chckbxUsePosTagging).addComponent(btnRun))
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));
        contentPane.setLayout(gl_contentPane);
    }
}
