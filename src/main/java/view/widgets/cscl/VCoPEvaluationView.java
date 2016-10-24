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
import java.awt.Cursor;
import java.awt.EventQueue;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import services.replicatedWorker.SerialCorpusAssessment;
import utils.localization.LocalizationUtils;
import view.widgets.ReaderBenchView;
import data.Lang;
import data.AbstractDocument.SaveType;

public class VCoPEvaluationView extends JFrame {

    private static final long serialVersionUID = 8894652868238113117L;
    static Logger logger = Logger.getLogger(VCoPView.class);

    private JPanel contentPane;
    private JTextField textFieldPath;
    private JComboBox<String> comboBoxLSA;
    private JComboBox<String> comboBoxLDA;
    private JCheckBox chckbxUsePosTagging;

    private static Lang lang = null;
    private static File lastDirectory = null;

    /**
     * Create the frame.
     */
    public VCoPEvaluationView() {
        setTitle("ReaderBench - " + LocalizationUtils.getTranslation("Evaluate virtual Communities of Practice"));
        setResizable(false);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setBounds(100, 100, 675, 275);
        contentPane = new JPanel();
        contentPane.setBackground(Color.WHITE);
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        setContentPane(contentPane);

        JLabel lblPath = new JLabel(LocalizationUtils.getTranslation("Path") + ":");

        textFieldPath = new JTextField();
        textFieldPath.setText("resources/in/forum_Nic");
        textFieldPath.setColumns(10);

        JButton btnSearch = new JButton("...");
        btnSearch.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JFileChooser fc = null;
                if (lastDirectory == null) {
                    fc = new JFileChooser(new File("resources/in"));
                } else {
                    fc = new JFileChooser(lastDirectory);
                }
                fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                int returnVal = fc.showOpenDialog(VCoPEvaluationView.this);

                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    File file = fc.getSelectedFile();
                    lastDirectory = file.getParentFile();
                    textFieldPath.setText(file.getPath());
                }
            }
        });

        JPanel panelEvaluate = new JPanel();
        panelEvaluate.setBackground(Color.WHITE);
        panelEvaluate.setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null),
                LocalizationUtils.getTranslation("Evaluate"), TitledBorder.LEADING, TitledBorder.TOP, null, null));

        GroupLayout gl_contentPane = new GroupLayout(contentPane);
        gl_contentPane
                .setHorizontalGroup(
                        gl_contentPane.createParallelGroup(Alignment.LEADING)
                        .addGroup(
                                gl_contentPane.createSequentialGroup().addContainerGap()
                                .addGroup(gl_contentPane.createParallelGroup(Alignment.TRAILING)
                                        .addGroup(gl_contentPane.createSequentialGroup()
                                                .addComponent(lblPath).addGap(110)
                                                .addComponent(textFieldPath, GroupLayout.DEFAULT_SIZE,
                                                        435, Short.MAX_VALUE)
                                                .addPreferredGap(ComponentPlacement.RELATED)
                                                .addComponent(btnSearch, GroupLayout.PREFERRED_SIZE, 41,
                                                        GroupLayout.PREFERRED_SIZE)
                                                .addGap(6))
                                        .addGroup(gl_contentPane
                                                .createSequentialGroup().addComponent(panelEvaluate,
                                                        GroupLayout.DEFAULT_SIZE, 635, Short.MAX_VALUE)
                                                .addContainerGap()))));
        gl_contentPane.setVerticalGroup(gl_contentPane.createParallelGroup(Alignment.LEADING)
                .addGroup(gl_contentPane.createSequentialGroup()
                        .addGroup(gl_contentPane.createParallelGroup(Alignment.BASELINE).addComponent(lblPath)
                                .addComponent(btnSearch).addComponent(textFieldPath, GroupLayout.PREFERRED_SIZE,
                                GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(ComponentPlacement.UNRELATED).addComponent(panelEvaluate,
                        GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addContainerGap(114, Short.MAX_VALUE)));

        lang = ReaderBenchView.RUNTIME_LANGUAGE;
 
        JLabel lblLsaVectorSpace = new JLabel(LocalizationUtils.getTranslation("LSA vector space") + ":");
        comboBoxLSA = new JComboBox<>();
        comboBoxLSA.addItem(LocalizationUtils.getTranslation("A Processing language needs to be previously selected"));

        JLabel lblLdaModel = new JLabel(LocalizationUtils.getTranslation("LDA model") + ":");
        comboBoxLDA = new JComboBox<String>();
        comboBoxLDA.addItem(LocalizationUtils.getTranslation("A Processing language needs to be previously selected"));

        chckbxUsePosTagging = new JCheckBox(LocalizationUtils.getTranslation("Use POS tagging"));
        chckbxUsePosTagging.setSelected(true);

        JButton btnEvaluateCorpus = new JButton(LocalizationUtils.getTranslation("Evaluate all corpus documents"));
        btnEvaluateCorpus.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (!textFieldPath.getText().equals("")) {
                        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

                        SerialCorpusAssessment.processCorpus(textFieldPath.getText(),
                                (String) comboBoxLSA.getSelectedItem(), (String) comboBoxLDA.getSelectedItem(), lang,
                                chckbxUsePosTagging.isSelected(), true, true, SaveType.SERIALIZED_AND_CSV_EXPORT);

                        Toolkit.getDefaultToolkit().beep();
                        logger.info("Finished processing all files");
                        setCursor(null); // turn off the wait cursor
                } else {
                    JOptionPane.showMessageDialog(VCoPEvaluationView.this,
                            "Please select an appropriate input folder to be evaluated!", "Error",
                            JOptionPane.WARNING_MESSAGE);
                }
            }
        });
        GroupLayout gl_panelEvaluate = new GroupLayout(panelEvaluate);
        gl_panelEvaluate.setHorizontalGroup(gl_panelEvaluate.createParallelGroup(Alignment.LEADING)
                .addGroup(gl_panelEvaluate.createSequentialGroup().addContainerGap().addGroup(gl_panelEvaluate
                        .createParallelGroup(Alignment.LEADING).addGroup(
                        gl_panelEvaluate
                        .createSequentialGroup()
                        .addGroup(gl_panelEvaluate.createParallelGroup(Alignment.LEADING)
                                .addComponent(lblLsaVectorSpace)
                                .addComponent(lblLdaModel))
                        .addPreferredGap(ComponentPlacement.UNRELATED)
                        .addGroup(gl_panelEvaluate.createParallelGroup(Alignment.LEADING)
                                .addGroup(gl_panelEvaluate.createSequentialGroup()
                                        .addGroup(gl_panelEvaluate
                                                .createParallelGroup(Alignment.LEADING)
                                                .addComponent(comboBoxLSA, 0, 495, Short.MAX_VALUE))
                                        .addGap(7))
                                .addGroup(gl_panelEvaluate.createSequentialGroup()
                                        .addComponent(comboBoxLDA, 0, 496, Short.MAX_VALUE)
                                        .addContainerGap())))
                        .addGroup(gl_panelEvaluate.createSequentialGroup().addComponent(chckbxUsePosTagging)
                                .addPreferredGap(ComponentPlacement.RELATED, 247, Short.MAX_VALUE)
                                .addComponent(btnEvaluateCorpus).addContainerGap()))));
        gl_panelEvaluate.setVerticalGroup(gl_panelEvaluate.createParallelGroup(Alignment.LEADING)
                .addGroup(gl_panelEvaluate.createSequentialGroup().addContainerGap()
                        .addGroup(gl_panelEvaluate.createParallelGroup(Alignment.BASELINE))
                        .addPreferredGap(ComponentPlacement.UNRELATED)
                        .addGroup(gl_panelEvaluate.createParallelGroup(Alignment.BASELINE)
                                .addComponent(lblLsaVectorSpace).addComponent(comboBoxLSA, GroupLayout.PREFERRED_SIZE,
                                GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(ComponentPlacement.UNRELATED)
                        .addGroup(gl_panelEvaluate.createParallelGroup(Alignment.BASELINE).addComponent(lblLdaModel)
                                .addComponent(comboBoxLDA, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
                                        GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(ComponentPlacement.UNRELATED)
                        .addGroup(gl_panelEvaluate.createParallelGroup(Alignment.BASELINE)
                                .addComponent(chckbxUsePosTagging).addComponent(btnEvaluateCorpus))
                        .addContainerGap(11, Short.MAX_VALUE)));
        panelEvaluate.setLayout(gl_panelEvaluate);

        contentPane.setLayout(gl_contentPane);
    }

    private static void adjustToSystemGraphics() {
        for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
            if ("Nimbus".equals(info.getName())) {
                try {
                    UIManager.setLookAndFeel(info.getClassName());
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                } catch (InstantiationException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (UnsupportedLookAndFeelException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
