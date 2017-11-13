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

import view.widgets.document.DocumentProcessingView;
import data.AbstractDocument;
import utils.LocalizationUtils;

public class AddSummaryView extends JInternalFrame {

    private static final long serialVersionUID = 8894652868238113117L;
    static final Logger LOGGER = Logger.getLogger("");

    private SummaryProcessingView view;
    private final JPanel contentPane;
    private JTextField textFieldPath;
    private JComboBox<String> comboBoxDocument;
    private JCheckBox chckbxUsePosTagging;
    private static File lastDirectory = null;

    /**
     * Create the frame.
     *
     * @param view
     */
    public AddSummaryView(SummaryProcessingView view) {
        super.setTitle("ReaderBench - " + LocalizationUtils.getTitle(this.getClass()));
        this.view = view;
        super.setResizable(false);
        super.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        super.setBounds(100, 100, 550, 140);
        contentPane = new JPanel();
        contentPane.setBackground(Color.WHITE);
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        super.setContentPane(contentPane);

        JLabel lblPath = new JLabel(LocalizationUtils.getGeneric("path") + ":");

        JLabel lblDocument = new JLabel(LocalizationUtils.getGeneric("docName") + ":");

        String[] titles = new String[DocumentProcessingView
                .getLoadedDocuments().size()];
        int index = 0;
        for (AbstractDocument d : DocumentProcessingView.getLoadedDocuments()) {
            titles[index++] = d.getDescription();
        }
        comboBoxDocument = new JComboBox<>(titles);
        comboBoxDocument.setSelectedIndex(0);

        textFieldPath = new JTextField();
        textFieldPath.setText("");
        textFieldPath.setColumns(10);

        JButton btnSearch = new JButton("...");
        btnSearch.addActionListener((ActionEvent e) -> {
            JFileChooser fc;
            if (lastDirectory == null) {
                fc = new JFileChooser("resources/in");
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
                    return LocalizationUtils.getGeneric("msgXMLFile");
                }
            });
            int returnVal = fc.showOpenDialog(AddSummaryView.this);

            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File file = fc.getSelectedFile();
                lastDirectory = file.getParentFile();
                textFieldPath.setText(file.getPath());
            }
        });

        JButton btnCancel = new JButton(LocalizationUtils.getGeneric("cancel"));
        btnCancel.addActionListener((ActionEvent e) -> {
            AddSummaryView.this.dispose();
        });

        JButton btnOk = new JButton(LocalizationUtils.getGeneric("ok"));
        btnOk.addActionListener((ActionEvent e) -> {
            if (!textFieldPath.getText().equals("")) {
                SummaryProcessingView.EssayProcessingTask task = AddSummaryView.this.view.new EssayProcessingTask(
                        textFieldPath.getText(), DocumentProcessingView
                        .getLoadedDocuments()
                        .get(comboBoxDocument.getSelectedIndex()),
                        chckbxUsePosTagging.isSelected(), false);
                task.execute();
                AddSummaryView.this.dispose();
            } else {
                JOptionPane
                        .showMessageDialog(AddSummaryView.this,
                                LocalizationUtils.getGeneric("msgSelectInputFile") + "!",
                                "Error", JOptionPane.WARNING_MESSAGE);
            }
        });

        chckbxUsePosTagging = new JCheckBox(LocalizationUtils.getGeneric("usePOStagging"));
        chckbxUsePosTagging.setSelected(true);

        GroupLayout gl_contentPane = new GroupLayout(contentPane);
        gl_contentPane
                .setHorizontalGroup(gl_contentPane
                        .createParallelGroup(Alignment.LEADING)
                        .addGroup(
                                gl_contentPane
                                        .createSequentialGroup()
                                        .addContainerGap()
                                        .addGroup(
                                                gl_contentPane
                                                        .createParallelGroup(
                                                                Alignment.TRAILING)
                                                        .addGroup(
                                                                gl_contentPane
                                                                        .createSequentialGroup()
                                                                        .addGroup(
                                                                                gl_contentPane
                                                                                        .createParallelGroup(
                                                                                                Alignment.LEADING)
                                                                                        .addComponent(
                                                                                                lblPath)
                                                                                        .addComponent(
                                                                                                lblDocument))
                                                                        .addGap(18)
                                                                        .addGroup(
                                                                                gl_contentPane
                                                                                        .createParallelGroup(
                                                                                                Alignment.TRAILING)
                                                                                        .addGroup(
                                                                                                gl_contentPane
                                                                                                        .createSequentialGroup()
                                                                                                        .addComponent(
                                                                                                                textFieldPath,
                                                                                                                GroupLayout.DEFAULT_SIZE,
                                                                                                                389,
                                                                                                                Short.MAX_VALUE)
                                                                                                        .addPreferredGap(
                                                                                                                ComponentPlacement.RELATED)
                                                                                                        .addComponent(
                                                                                                                btnSearch,
                                                                                                                GroupLayout.PREFERRED_SIZE,
                                                                                                                41,
                                                                                                                GroupLayout.PREFERRED_SIZE))
                                                                                        .addComponent(
                                                                                                comboBoxDocument,
                                                                                                0,
                                                                                                436,
                                                                                                Short.MAX_VALUE))
                                                                        .addGap(6))
                                                        .addGroup(
                                                                gl_contentPane
                                                                        .createSequentialGroup()
                                                                        .addComponent(
                                                                                chckbxUsePosTagging)
                                                                        .addPreferredGap(
                                                                                ComponentPlacement.RELATED,
                                                                                256,
                                                                                Short.MAX_VALUE)
                                                                        .addComponent(
                                                                                btnOk,
                                                                                GroupLayout.PREFERRED_SIZE,
                                                                                73,
                                                                                GroupLayout.PREFERRED_SIZE)
                                                                        .addPreferredGap(
                                                                                ComponentPlacement.RELATED)
                                                                        .addComponent(
                                                                                btnCancel)
                                                                        .addContainerGap()))));
        gl_contentPane
                .setVerticalGroup(gl_contentPane
                        .createParallelGroup(Alignment.LEADING)
                        .addGroup(
                                gl_contentPane
                                        .createSequentialGroup()
                                        .addGroup(
                                                gl_contentPane
                                                        .createParallelGroup(
                                                                Alignment.BASELINE)
                                                        .addComponent(
                                                                textFieldPath,
                                                                GroupLayout.PREFERRED_SIZE,
                                                                GroupLayout.DEFAULT_SIZE,
                                                                GroupLayout.PREFERRED_SIZE)
                                                        .addComponent(lblPath)
                                                        .addComponent(btnSearch))
                                        .addPreferredGap(
                                                ComponentPlacement.RELATED)
                                        .addGroup(
                                                gl_contentPane
                                                        .createParallelGroup(
                                                                Alignment.BASELINE)
                                                        .addComponent(
                                                                lblDocument)
                                                        .addComponent(
                                                                comboBoxDocument,
                                                                GroupLayout.PREFERRED_SIZE,
                                                                GroupLayout.DEFAULT_SIZE,
                                                                GroupLayout.PREFERRED_SIZE))
                                        .addPreferredGap(
                                                ComponentPlacement.UNRELATED)
                                        .addGroup(
                                                gl_contentPane
                                                        .createParallelGroup(
                                                                Alignment.BASELINE)
                                                        .addComponent(btnOk)
                                                        .addComponent(btnCancel)
                                                        .addComponent(
                                                                chckbxUsePosTagging))
                                        .addContainerGap(19, Short.MAX_VALUE)));
        contentPane.setLayout(gl_contentPane);
    }
}
