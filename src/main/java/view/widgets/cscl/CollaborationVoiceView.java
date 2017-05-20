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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;

import services.commons.VectorAlgebra;
import services.discourse.CSCL.Collaboration;
import data.cscl.CollaborationZone;
import data.cscl.Conversation;

import javax.swing.JTextField;

public class CollaborationVoiceView extends JFrame {

    private static final long serialVersionUID = 2897644814459831682L;

    private JPanel contentPane;
    private JPanel panelMutualInformation;
    private JTextField txtOverlap;

    /**
     * Create the frame.
     */
    public CollaborationVoiceView(Conversation chat) {
        setTitle("ReaderBench - Collaboration as Voice Overlapping");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setBounds(100, 100, 650, 550);
        contentPane = new JPanel();
        contentPane.setBackground(Color.WHITE);
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        setContentPane(contentPane);

        panelMutualInformation = new JPanel();
        panelMutualInformation.setBorder(new EtchedBorder(EtchedBorder.LOWERED,
                null, null));
        panelMutualInformation.setBackground(Color.WHITE);
        panelMutualInformation.setLayout(new BorderLayout());

        JLabel label = new JLabel(
                "Automatically identified intense collaboration zones");
        label.setFont(new Font("SansSerif", Font.BOLD, 12));

        JScrollPane scrollPane = new JScrollPane();
        scrollPane
                .setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        JLabel lblOverlap = new JLabel(
                "Overlap with intense collaboration zones identified through social KB:");
        lblOverlap.setFont(new Font("Lucida Grande", Font.BOLD, 13));

        txtOverlap = new JTextField();
        txtOverlap.setEditable(false);
        txtOverlap.setColumns(10);
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
                                                                Alignment.LEADING)
                                                        .addComponent(
                                                                panelMutualInformation,
                                                                GroupLayout.DEFAULT_SIZE,
                                                                628,
                                                                Short.MAX_VALUE)
                                                        .addComponent(
                                                                scrollPane,
                                                                GroupLayout.DEFAULT_SIZE,
                                                                628,
                                                                Short.MAX_VALUE)
                                                        .addComponent(
                                                                label,
                                                                GroupLayout.PREFERRED_SIZE,
                                                                327,
                                                                GroupLayout.PREFERRED_SIZE)
                                                        .addComponent(
                                                                txtOverlap,
                                                                GroupLayout.DEFAULT_SIZE,
                                                                628,
                                                                Short.MAX_VALUE)
                                                        .addComponent(
                                                                lblOverlap,
                                                                GroupLayout.DEFAULT_SIZE,
                                                                628,
                                                                Short.MAX_VALUE))
                                        .addContainerGap()));
        gl_contentPane.setVerticalGroup(gl_contentPane.createParallelGroup(
                Alignment.TRAILING).addGroup(
                        gl_contentPane
                                .createSequentialGroup()
                                .addContainerGap()
                                .addComponent(panelMutualInformation,
                                        GroupLayout.DEFAULT_SIZE, 332, Short.MAX_VALUE)
                                .addPreferredGap(ComponentPlacement.UNRELATED)
                                .addComponent(label, GroupLayout.PREFERRED_SIZE, 15,
                                        GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(ComponentPlacement.UNRELATED)
                                .addComponent(scrollPane, GroupLayout.PREFERRED_SIZE,
                                        73, GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(ComponentPlacement.UNRELATED)
                                .addComponent(lblOverlap)
                                .addPreferredGap(ComponentPlacement.RELATED)
                                .addComponent(txtOverlap, GroupLayout.PREFERRED_SIZE,
                                        GroupLayout.DEFAULT_SIZE,
                                        GroupLayout.PREFERRED_SIZE).addContainerGap()));

        JTextPane textPane = new JTextPane();
        textPane.setText("");
        textPane.setEditable(false);
        textPane.setBackground(Color.WHITE);
        scrollPane.setViewportView(textPane);

        contentPane.setLayout(gl_contentPane);

        double[] evolution = chat.getVoicePMIEvolution();

        Double[][] values = new Double[1][evolution.length];
        double[] columns = new double[evolution.length];

        String[] names = {"Cumulated Contextual Voice Co-Occurrences"};

        for (int i = 0; i < evolution.length; i++) {
            values[0][i] = evolution[i];
            columns[i] = i;
        }

        EvolutionGraph evolutionGraph = new EvolutionGraph(
                "Cumulated Contextual Voice Co-Occurrences",
                "utterance", false, names, values, columns, Color.DARK_GRAY);

        panelMutualInformation.add(evolutionGraph.evolution());

        for (CollaborationZone zone : chat.getIntenseCollabZonesVoice()) {
            textPane.setText(textPane.getText() + zone.toStringDetailed()
                    + "\n");
        }
        textPane.setText(textPane.getText().trim());
        double[] results = Collaboration.overlapCollaborationZones(chat,
                chat.getIntenseCollabZonesSocialKB(),
                chat.getIntenseCollabZonesVoice());

        txtOverlap.setText("P = "
                + results[0]
                + "; R = "
                + results[1]
                + "; F1 score = "
                + results[2]
                + "; r = "
                + VectorAlgebra.pearsonCorrelation(chat.getVoicePMIEvolution(),
                        chat.getSocialKBEvolution()));
    }
}
