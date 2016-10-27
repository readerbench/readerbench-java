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
package view.widgets.document.corpora;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.List;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.table.DefaultTableModel;



import data.AbstractDocument;
import data.Block;
import data.Word;
import data.discourse.WordOverlap;
import data.document.Document;
import java.util.logging.Logger;

public class PaperKeywordAbstractOverlap extends JFrame {

    private static final long serialVersionUID = -8582615231233815258L;
    static Logger logger = Logger.getLogger("");
    public static final Color COLOR_CONCEPT = new Color(204, 204, 204); // silver

    private final List<Document> docs;

    JTable table;
    DefaultTableModel tableModel;
    WordOverlap wo;

    public PaperKeywordAbstractOverlap(List<Document> docs) {
        wo = new WordOverlap(docs);
        this.docs = wo.computeWordOverlaps();

        setTitle("Best Articles - Keyword&Abstract Overlap");
        getContentPane().setBackground(Color.WHITE);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        // adjust view to desktop size
        int margin = 50;
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setBounds(margin, margin, screenSize.width - margin * 2,
                screenSize.height - margin * 2);

        generateLayout();
    }

    private void generateLayout() {
        setBounds(50, 50, 1180, 700);
        JLabel lblTopSimilarArticles = new JLabel(
                "Keyword-Abstract overlap score");
        lblTopSimilarArticles.setFont(new Font("SansSerif", Font.BOLD, 14));
        String[] header = {"Article", "Overlap Score", "Semantic Score",
            "Aggregated Score"};
        String[][] data = new String[docs.size()][4];
        NumberFormat formatter = new DecimalFormat("#0.00");
        for (int i = 0; i < docs.size(); i++) {
            Document d = (Document) docs.get(i);
            data[i][0] = d.getTitleText();
            data[i][1] = formatter.format(wo.getDocumentOverlapScores().get(
                    docs.get(i)));
            data[i][2] = formatter.format(wo.getDocumentSemanticScores().get(
                    docs.get(i)));
            data[i][3] = formatter.format(wo.getDocumentAggregatedScores().get(
                    docs.get(i)));
        }

        tableModel = new DefaultTableModel(data, header);
        table = new JTable(tableModel) {
            private static final long serialVersionUID = 1L;

            public boolean isCellEditable(int row, int column) {
                return false;
            }
        ;
        };
		try {
            // 1.6+
            table.setAutoCreateRowSorter(true);
        } catch (Exception continuewithNoSort) {
        }
        JScrollPane tableScroll = new JScrollPane(table);
        Dimension tablePreferred = tableScroll.getPreferredSize();
        tableScroll.setPreferredSize(new Dimension(tablePreferred.width,
                tablePreferred.height));
        table.setFillsViewportHeight(true);
        table.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent me) {
                JTable table = (JTable) me.getSource();
                Point p = me.getPoint();
                int row = table.rowAtPoint(p);
                if (me.getClickCount() == 2) {
                    AbstractDocument doc = docs.get(row);
                    String keywords = "";
                    for (Word word : ((Document) doc).getInitialTopics()) {
                        keywords += (keywords.length() > 0) ? ", " : "";
                        keywords += word.getText();
                    }
                    String keywordsText = "";
                    int index = 0;
                    for (index = 0; index < keywords.length(); index += 150) {
                        keywordsText += keywords.substring(index,
                                Math.min(index + 150, keywords.length()))
                                + "<br>";
                    }
                    if (index < keywords.length() - 1) {
                        keywordsText += keywords.substring(index,
                                keywords.length());
                    }

                    Block docAbstract = null;
                    for (Block b : doc.getBlocks()) {
                        if (b != null) {
                            docAbstract = b;
                            break;
                        }
                    }
                    String abstractDoc = docAbstract.getText();
                    String fullText = "";
                    index = 0;
                    for (index = 0; index < abstractDoc.length(); index += 150) {
                        fullText += abstractDoc.substring(index,
                                Math.min(index + 150, abstractDoc.length()))
                                + "<br>";
                    }
                    if (index < abstractDoc.length() - 1) {
                        fullText += abstractDoc.substring(index,
                                abstractDoc.length());
                    }
                    JOptionPane.showMessageDialog(table,
                            "<html><b>Article:</b> " + doc.getTitleText()
                            + "<br><br> <b>Keywords:</b> "
                            + keywordsText + "<br> <b>Abstract:</b> "
                            + fullText + "</html>");
                }
            }
        });

        GroupLayout groupLayout = new GroupLayout(getContentPane());
        groupLayout.setHorizontalGroup(groupLayout.createParallelGroup(
                Alignment.LEADING).addGroup(
                        groupLayout
                        .createSequentialGroup()
                        .addContainerGap()
                        .addGroup(
                                groupLayout
                                .createParallelGroup(Alignment.LEADING)
                                .addComponent(tableScroll,
                                        Alignment.TRAILING,
                                        GroupLayout.DEFAULT_SIZE,
                                        GroupLayout.DEFAULT_SIZE,
                                        Short.MAX_VALUE)
                                .addComponent(lblTopSimilarArticles))
                        .addContainerGap()));
        groupLayout.setVerticalGroup(groupLayout.createParallelGroup(
                Alignment.LEADING).addGroup(
                        groupLayout
                        .createSequentialGroup()
                        .addContainerGap()
                        .addComponent(lblTopSimilarArticles)
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addComponent(tableScroll, GroupLayout.DEFAULT_SIZE,
                                614, Short.MAX_VALUE).addContainerGap()));
        // groupLayout.createParallelGroup().addComponent(tableScrollCentrality);
        // .addComponent(tableScrollCentrality)
        getContentPane().setLayout(groupLayout);
    }
}
