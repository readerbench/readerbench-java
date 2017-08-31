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
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileFilter;

import data.Lang;
import java.util.ResourceBundle;
import view.widgets.ReaderBenchView;

public class AddDocumentView extends JInternalFrame {

    private static final long serialVersionUID = 8894652868238113117L;
    static final Logger LOGGER = Logger.getLogger("");

    private final JPanel contentPane;
    private DocumentProcessingView view;
    private JTextField textFieldPath;
    private JComboBox<String> comboBoxLSA;
    private JComboBox<String> comboBoxLDA;
    private JComboBox<String> comboBoxWORD2VEC;
    private JCheckBox chckbxUsePosTagging;
    private JCheckBox chckbxSer;
    private static File lastDirectory = null;

    /**
     * Create the frame.
     *
     * @param pathToFile
     * @return
     */
    public boolean checkSerDocsByName(String pathToFile) {
        File f = new File(pathToFile);
        File[] files = f.getParentFile().listFiles();
        for (File i : files) {
            if (i.getName().equals(pathToFile.replace(".xml", ".ser"))) {
                return true;
            }
        }
        return false;
    }

    public boolean checkSerDocsInFolder(String pathToFile) {
        File f = new File(pathToFile);
        File[] files = f.getParentFile().listFiles();
        for (File i : files) {
            if (i.getName().endsWith(".ser")) {
                return true;
            }
        }
        return false;
    }

    public AddDocumentView(Lang lang, DocumentProcessingView view) {
        super.setTitle("ReaderBench - Add a new document");
        this.view = view;
        super.setResizable(false);
        super.setClosable(true);
        super.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        super.setBounds(100, 100, 600, 300);
        contentPane = new JPanel();
        contentPane.setBackground(Color.WHITE);
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        super.setContentPane(contentPane);

        JLabel lblPath = new JLabel("Path:");

        JLabel lblLsaVectorSpace = new JLabel(ResourceBundle.getBundle("utils.localization.messages")
                .getString("TableModel.LSAspace.text"));

        JLabel lblLdaModel = new JLabel(ResourceBundle.getBundle("utils.localization.messages")
                .getString("TableModel.LDAvector.text"));
	
	JLabel lblWord2VecVectorSpace = new JLabel(ResourceBundle.getBundle("utils.localization.messages")
                .getString("TableModel.Word2Vec.text"));
	
        comboBoxLSA = new JComboBox<>();
        comboBoxLDA = new JComboBox<>();
	comboBoxWORD2VEC = new JComboBox<>();

        ReaderBenchView.updateComboLanguage(comboBoxLSA, comboBoxLDA, comboBoxWORD2VEC, lang);

        textFieldPath = new JTextField();
        textFieldPath.setText("");
        textFieldPath.setColumns(10);

        JButton btnSearch = new JButton("...");
        btnSearch.addActionListener((ActionEvent e) -> {
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
                    return f.getName().endsWith(".xml");
                }

                @Override
                public String getDescription() {
                    return "XML file (*.xml) or directory";
                }
            });
            int returnVal = fc.showOpenDialog(AddDocumentView.this);

            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File file = fc.getSelectedFile();
                lastDirectory = file.getParentFile();
                textFieldPath.setText(file.getPath());
            }
        });

        JButton btnCancel = new JButton("Cancel");
        btnCancel.addActionListener((ActionEvent e) -> {
            AddDocumentView.this.dispose();
        });

        JButton btnOk = new JButton("Ok");
        btnOk.addActionListener((ActionEvent e) -> {
            if (!textFieldPath.getText().equals("")) {
                DocumentProcessingView.DocumentProcessingTask task = AddDocumentView.this.view.new DocumentProcessingTask(
                        textFieldPath.getText(), (String) comboBoxLSA.getSelectedItem(),

                        (String) comboBoxLDA.getSelectedItem(), (String) comboBoxWORD2VEC.getSelectedItem(),
			chckbxUsePosTagging.isSelected(), chckbxUsePosTagging.isSelected(), false, false, chckbxSer.isSelected());

                task.execute();

                AddDocumentView.this.dispose();
            } else {
                JOptionPane.showMessageDialog(AddDocumentView.this,
                        ResourceBundle.getBundle("utils.localization.messages")
			.getString("RunMeasurementsView.msgSelectAnotherFile.text"), "Error",
                        JOptionPane.WARNING_MESSAGE);
            }
        });

        chckbxUsePosTagging = new JCheckBox(ResourceBundle.getBundle("utils.localization.messages")
					    .getString("RunMeasurementsView.boxUsePOSTagging.text"));
        chckbxUsePosTagging.setSelected(true);

        chckbxSer = new JCheckBox(ResourceBundle.getBundle("utils.localization.messages")
				    .getString("RunMeasurementsView.boxConsiderPreProcessedFiles"));
        chckbxSer.setSelected(true);

        GroupLayout gl_contentPane = new GroupLayout(contentPane);
        gl_contentPane
                .setHorizontalGroup(gl_contentPane.createParallelGroup(Alignment.LEADING)
                        .addGroup(gl_contentPane.createSequentialGroup().addContainerGap().addGroup(gl_contentPane
                                .createParallelGroup(Alignment.TRAILING).addGroup(gl_contentPane
                                .createSequentialGroup().addGroup(
                                        gl_contentPane.createParallelGroup(Alignment.LEADING)
                                        .addComponent(lblPath).addComponent(
                                        lblLsaVectorSpace)
                                        .addComponent(lblLdaModel)
					.addComponent(lblWord2VecVectorSpace))
                                .addGap(13)
                                .addGroup(gl_contentPane.createParallelGroup(Alignment.TRAILING)
                                        .addComponent(comboBoxLDA, 0, 401, Short.MAX_VALUE)
                                        .addGroup(gl_contentPane.createSequentialGroup()
                                                .addComponent(textFieldPath, GroupLayout.DEFAULT_SIZE, 354,
                                                        Short.MAX_VALUE)
                                                .addPreferredGap(ComponentPlacement.RELATED)
                                                .addComponent(btnSearch, GroupLayout.PREFERRED_SIZE, 41,
                                                        GroupLayout.PREFERRED_SIZE))
                                        .addComponent(comboBoxLSA, 0, 401, Short.MAX_VALUE)
					.addComponent(comboBoxWORD2VEC, 0, 401, Short.MAX_VALUE))
					
                                .addGap(6))
                                .addGroup(gl_contentPane.createSequentialGroup().addComponent(chckbxUsePosTagging).addPreferredGap(ComponentPlacement.RELATED, 255, Short.MAX_VALUE).addComponent(chckbxSer)
                                        .addPreferredGap(ComponentPlacement.RELATED, 255, Short.MAX_VALUE)
                                        .addComponent(btnOk, GroupLayout.PREFERRED_SIZE, 73,
                                                GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(ComponentPlacement.RELATED).addComponent(btnCancel)
                                        .addContainerGap()))));
        gl_contentPane.setVerticalGroup(gl_contentPane.createParallelGroup(Alignment.LEADING)
                .addGroup(gl_contentPane.createSequentialGroup()
                        .addGroup(gl_contentPane.createParallelGroup(Alignment.BASELINE)
                                .addComponent(textFieldPath, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
                                        GroupLayout.PREFERRED_SIZE)
                                .addComponent(lblPath).addComponent(btnSearch))
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
			.addGroup(gl_contentPane.createParallelGroup(Alignment.BASELINE).addComponent(lblWord2VecVectorSpace)
                                .addComponent(comboBoxWORD2VEC, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
                                        GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addGroup(gl_contentPane.createParallelGroup(Alignment.BASELINE).addComponent(btnOk)
                                .addComponent(btnCancel).addComponent(chckbxUsePosTagging).addComponent(chckbxSer))
                        .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));
        contentPane.setLayout(gl_contentPane);
    }
}
