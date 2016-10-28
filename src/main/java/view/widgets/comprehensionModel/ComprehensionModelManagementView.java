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
package view.widgets.comprehensionModel;

import data.Lang;
import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.util.logging.Logger;

import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;




import services.comprehensionModel.ComprehensionModel;
import services.semanticModels.LDA.LDA;
import utils.localization.LocalizationUtils;

public class ComprehensionModelManagementView extends JFrame {

    private static final long serialVersionUID = -2864356905020607155L;
    static Logger logger = Logger.getLogger("");

    private final JPanel contentPane;
    private final JTextField txtFieldHdpGrade;
    private final JTextField textFieldSemanticThreshold;
    private final JTextField textFieldActivationThreshold;
    private final JTextField textFieldNoActiveWords;
    private final JTextField textFieldNoActiveWordsIncrement;
    private final JTextArea textAreaContent;
    private final JTextField textFieldTopConcepts;

    /**
     * Create the frame.
     */
    public ComprehensionModelManagementView() {
        super.setTitle("Comprehension Model Parameters");
        super.setDefaultCloseOperation(EXIT_ON_CLOSE);
        super.setBounds(100, 100, 750, 700);

        contentPane = new JPanel();
        contentPane.setBackground(Color.WHITE);
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        super.setContentPane(contentPane);

        JLabel lblComplexityLevel = new JLabel("Semantic Model:");
        lblComplexityLevel.setFont(new Font("SansSerif", Font.PLAIN, 12));

        txtFieldHdpGrade = new JTextField();
        txtFieldHdpGrade.setColumns(10);
        txtFieldHdpGrade.setText("2");

        JLabel lbl_3 = new JLabel("Association threshold:");
        lbl_3.setFont(new Font("SansSerif", Font.PLAIN, 12));

        textFieldSemanticThreshold = new JTextField();
        textFieldSemanticThreshold.setColumns(10);
        textFieldSemanticThreshold.setText("0.5");

        JLabel lblText = new JLabel(LocalizationUtils.getTranslation("Text"));
        lblText.setFont(new Font("SansSerif", Font.BOLD, 12));

        textAreaContent = new JTextArea();
        textAreaContent.setLineWrap(true);
        textAreaContent.setWrapStyleWord(true);
        Border border = BorderFactory.createLineBorder(Color.BLACK);
        textAreaContent
                .setBorder(BorderFactory.createCompoundBorder(border, BorderFactory.createEmptyBorder(10, 10, 10, 10)));
        textAreaContent.setText(
                "A young knight rode through the forest. The knight was unfamiliar with the country. Suddenly, a dragon appeared. The dragon was kidnapping a beautiful princess. The knight wanted to free her. He wanted to marry her. The knight hurried after the dragon. They fought for life and death. Soon, the knight's armor was completely scorched. At last, the knight killed the dragon. He freed the princess. The princess was very thankful to the knight. She married the knight.");

        JButton btnNewButton = new JButton("Comprehension Model");
        btnNewButton.addActionListener((ActionEvent arg0) -> {
            ComprehensionModelManagementView.this.openComprehensionModel();
        });

        JLabel label = new JLabel("Activation Threshold:");
        label.setFont(new Font("SansSerif", Font.PLAIN, 12));

        textFieldActivationThreshold = new JTextField();
        textFieldActivationThreshold.setText("0.3");
        textFieldActivationThreshold.setColumns(10);

        JLabel label_1 = new JLabel("Active Words:");
        label_1.setFont(new Font("SansSerif", Font.PLAIN, 12));

        textFieldNoActiveWords = new JTextField();
        textFieldNoActiveWords.setText("3");
        textFieldNoActiveWords.setColumns(10);

        JLabel label_2 = new JLabel("A.W. Increment:");
        label_2.setFont(new Font("SansSerif", Font.PLAIN, 12));

        textFieldNoActiveWordsIncrement = new JTextField();
        textFieldNoActiveWordsIncrement.setText("1");
        textFieldNoActiveWordsIncrement.setColumns(10);

        JLabel lbl_4 = new JLabel("Number of top semantically related concepts:");
        lbl_4.setFont(new Font("SansSerif", Font.PLAIN, 12));

        textFieldTopConcepts = new JTextField();
        textFieldTopConcepts.setText("10");
        textFieldTopConcepts.setColumns(10);

        GroupLayout gl_contentPane = new GroupLayout(contentPane);
        gl_contentPane.setHorizontalGroup(gl_contentPane.createParallelGroup(Alignment.TRAILING).addGroup(gl_contentPane
                .createSequentialGroup().addContainerGap().addGroup(gl_contentPane
                        .createParallelGroup(Alignment.LEADING).addComponent(textAreaContent,
                        GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
                        Short.MAX_VALUE)
                        .addGroup(gl_contentPane.createSequentialGroup()
                                .addGroup(gl_contentPane.createParallelGroup(Alignment.LEADING)
                                        .addComponent(lblComplexityLevel).addComponent(label_1,
                                        GroupLayout.PREFERRED_SIZE, 106, GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(ComponentPlacement.RELATED)
                                .addGroup(gl_contentPane.createParallelGroup(Alignment.LEADING, false)
                                        .addComponent(textFieldNoActiveWords, 0, 0, Short.MAX_VALUE)
                                        .addComponent(txtFieldHdpGrade, GroupLayout.DEFAULT_SIZE, 42, Short.MAX_VALUE))
                                .addPreferredGap(ComponentPlacement.RELATED)
                                .addGroup(gl_contentPane.createParallelGroup(Alignment.LEADING).addComponent(lbl_3)
                                        .addComponent(label_2, GroupLayout.PREFERRED_SIZE, 129,
                                                GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(ComponentPlacement.RELATED)
                                .addGroup(gl_contentPane.createParallelGroup(Alignment.LEADING)
                                        .addComponent(textFieldNoActiveWordsIncrement, GroupLayout.PREFERRED_SIZE, 44,
                                                GroupLayout.PREFERRED_SIZE)
                                        .addComponent(textFieldSemanticThreshold, GroupLayout.PREFERRED_SIZE, 44,
                                                GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(ComponentPlacement.RELATED)
                                .addGroup(gl_contentPane.createParallelGroup(Alignment.LEADING).addComponent(lbl_4)
                                        .addComponent(label, GroupLayout.PREFERRED_SIZE, 144,
                                                GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(ComponentPlacement.RELATED)
                                .addGroup(gl_contentPane.createParallelGroup(Alignment.LEADING)
                                        .addComponent(textFieldActivationThreshold, GroupLayout.PREFERRED_SIZE, 36,
                                                GroupLayout.PREFERRED_SIZE)
                                        .addComponent(textFieldTopConcepts, GroupLayout.PREFERRED_SIZE, 38,
                                                GroupLayout.PREFERRED_SIZE)))
                        .addComponent(lblText).addComponent(btnNewButton, Alignment.TRAILING,
                        GroupLayout.PREFERRED_SIZE, 222, GroupLayout.PREFERRED_SIZE))
                .addContainerGap()));
        gl_contentPane.setVerticalGroup(gl_contentPane.createParallelGroup(Alignment.LEADING).addGroup(gl_contentPane
                .createSequentialGroup().addContainerGap()
                .addGroup(gl_contentPane.createParallelGroup(Alignment.BASELINE).addComponent(lblComplexityLevel)
                        .addComponent(txtFieldHdpGrade, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
                                GroupLayout.PREFERRED_SIZE)
                        .addComponent(lbl_3)
                        .addComponent(textFieldSemanticThreshold, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
                                GroupLayout.PREFERRED_SIZE)
                        .addComponent(label).addComponent(textFieldActivationThreshold, GroupLayout.PREFERRED_SIZE,
                        GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addGap(7)
                .addGroup(gl_contentPane.createParallelGroup(Alignment.TRAILING)
                        .addGroup(gl_contentPane.createParallelGroup(Alignment.BASELINE).addComponent(textFieldNoActiveWords, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
                                GroupLayout.PREFERRED_SIZE).addComponent(label_1)
                                .addComponent(textFieldNoActiveWordsIncrement, GroupLayout.PREFERRED_SIZE,
                                        GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                .addComponent(textFieldTopConcepts, GroupLayout.PREFERRED_SIZE,
                                        GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                .addComponent(lbl_4).addComponent(label_2)))
                .addPreferredGap(ComponentPlacement.UNRELATED)
                .addComponent(lblText, GroupLayout.PREFERRED_SIZE, 16, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(ComponentPlacement.RELATED)
                .addComponent(textAreaContent, GroupLayout.DEFAULT_SIZE, 458, Short.MAX_VALUE)
                .addPreferredGap(ComponentPlacement.RELATED).addComponent(btnNewButton)));
        contentPane.setLayout(gl_contentPane);
    }

    public void openComprehensionModel() {
        int hdpGrade = Integer.parseInt(this.txtFieldHdpGrade.getText());
        double semanticThreshold = Double.parseDouble(this.textFieldSemanticThreshold.getText());
        double activationThreshold = Double.parseDouble(this.textFieldActivationThreshold.getText());
        int noActiveWords = Integer.parseInt(this.textFieldNoActiveWords.getText());
        int noActiveWordsIncrement = Integer.parseInt(this.textFieldNoActiveWordsIncrement.getText());
        int noTopConcepts = Integer.parseInt(this.textFieldTopConcepts.getText());

        String text = this.textAreaContent.getText();

        EventQueue.invokeLater(() -> {
            ComprehensionModel cm = new ComprehensionModel(text,
                    LDA.loadLDA("resources/in/HDP/grade" + hdpGrade, Lang.en), semanticThreshold, noTopConcepts,
                    activationThreshold, noActiveWords, noActiveWordsIncrement);
            ComprehensionModelView view = new ComprehensionModelView(cm);
            view.setVisible(true);
        });
    }

}
