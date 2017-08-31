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
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.logging.Logger;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

import services.commons.Clustering;
import services.complexity.ComputeBalancedMeasure;
import services.complexity.DataGathering;
import data.AbstractDocument;
import data.complexity.Measurement;
import services.complexity.ComplexityClustering;
import services.complexity.ComplexityIndex;
import services.semanticModels.SimilarityType;

public class DocumentEvaluationView extends JFrame {

    private static final long serialVersionUID = -4518616508590444786L;
    static Logger logger = Logger.getLogger("");
    private List<AbstractDocument> documents;

    private JPanel contentPane;
    private JTextField textFieldDirectory;
    private JLabel lblResults;
    private JButton btnSelectComplexityFactors;
    private JButton btnTrainSVM;
    private JScrollPane scrollPane;
    private DefaultTableModel tableModel;
    private JTable table;

    private class Task extends SwingWorker<Void, Void> {

        private String path;
        private int noClasses;

        public Task(String path) {
            super();
            this.path = path;
        }

        @Override
        public Void doInBackground() {
            btnTrainSVM.setEnabled(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            try {
                // Open the file
                FileWriter fstream = new FileWriter(
                        "out/doc_complexity_evaluation_"
                        + System.currentTimeMillis() + ".csv", true);
                BufferedWriter out = new BufferedWriter(fstream);

                // determine number of classes
                Map<Double, List<Measurement>> measurements = DataGathering
                        .getMeasurements(path + "/measurements.csv");
                Set<Double> classes = measurements.keySet();
                this.noClasses = classes.size();

                if (this.noClasses == 0) {
                    JOptionPane
                            .showMessageDialog(
                                    contentPane,
                                    "There are no complexity classes within the \"measurements.csv\" file!",
                                    "Error", JOptionPane.ERROR_MESSAGE);
                    out.close();
                    return null;
                }

                logger.info("Started to train the SVM model on " + noClasses
                        + " classes");

                // build the table
                String[] names = new String[documents.size() + 1];
                names[0] = "<html>Factor<br/><br/><br/></html>";
                out.write("Factor");
                for (int i = 0; i < documents.size(); i++) {
                    names[i + 1] = "<html>"
                            + (documents.get(i).getTitleText().length() > 30 ? documents
                            .get(i).getTitleText().substring(0, 27)
                            + "..."
                            : documents.get(i).getTitleText())
                            + "<br/>" + documents.get(i).getSemanticModel(SimilarityType.LSA).getPath()
                            + "<br/>" + documents.get(i).getSemanticModel(SimilarityType.LDA).getPath()
			    + "<br/>" + documents.get(i).getSemanticModel(SimilarityType.WORD2VEC).getPath()
                            + "</html>";
                    out.write(","
                            + documents.get(i).getTitleText()
                                    .replaceAll(",", "") + "("
                            + documents.get(i).getSemanticModel(SimilarityType.LSA).getPath() + "/"
                            + documents.get(i).getSemanticModel(SimilarityType.LDA).getPath() + "/"
			    + documents.get(i).getSemanticModel(SimilarityType.WORD2VEC).getPath() + ")");
                }
                out.write("\n");

                tableModel = new DefaultTableModel(new Object[][]{}, names) {
                    private static final long serialVersionUID = 8537729224173332503L;

                    @Override
                    public boolean isCellEditable(int rowIndex, int columnIndex) {
                        return false;
                    }

                    @Override
                    public Class<?> getColumnClass(int columnIndex) {
                        if (columnIndex == 0) {
                            return String.class;
                        }
                        return Double.class;
                    }
                };

                table = new JTable(tableModel);
                table.setFillsViewportHeight(true);
                table.setAutoCreateRowSorter(true);

                for (int i = 1; i < documents.size() + 1; i++) {
                    // set width for ID and selected
                    table.getColumnModel()
                            .getColumn(i)
                            .setMinWidth(
                                    Math.min(contentPane.getWidth()
                                            / (documents.size() + 1), 200));
                    table.getColumnModel()
                            .getColumn(i)
                            .setMaxWidth(
                                    Math.min(contentPane.getWidth()
                                            / (documents.size() + 1), 200));
                    table.getColumnModel()
                            .getColumn(i)
                            .setPreferredWidth(
                                    Math.min(contentPane.getWidth()
                                            / (documents.size() + 1), 200));
                }

                double[] predictions = ComputeBalancedMeasure
                        .evaluateTextualComplexity(documents,
                                textFieldDirectory.getText(),
                                ComplexityIndicesView.getSelectedMeasurements());

                // display results
                // first line = SVM predictions
                Vector<Object> dataRow = new Vector<>();
                dataRow.add("Complexity prediction");
                out.write("Complexity prediction");
                for (int i = 0; i < documents.size(); i++) {
                    dataRow.add(predictions[i]);
                    out.write("," + predictions[i]);
                }
                tableModel.addRow(dataRow);
                out.write("\n");

                // display comparative information for all documents based on
                // the selected evaluation factors
                for (int i : ComplexityIndicesView.getSelectedMeasurements()) {
                    dataRow = new Vector<>();
                    ComplexityIndex index = ComplexityIndicesView.getAllIndices()[i];
                    dataRow.add(index.getAcronym());
                    out.write(index.getAcronym());
                    for (AbstractDocument d : documents) {
                        dataRow.add(d.getComplexityIndices().get(index));
                        out.write("," + d.getComplexityIndices().get(index));
                    }
                    tableModel.addRow(dataRow);
                    out.write("\n");
                }

                scrollPane.setViewportView(table);
                out.close();

                ComplexityClustering cc = new ComplexityClustering();
                cc.performKMeansClustering(documents,
                        Math.min(6, (documents.size() + 1) / 2));
                cc.performAglomerativeClustering(documents, "out/aglomerative_clustering.txt");
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        /*
		 * Executed in event dispatching thread
         */
        @Override
        public void done() {
            Toolkit.getDefaultToolkit().beep();
            btnTrainSVM.setEnabled(true);
            setCursor(null); // turn off the wait cursor
        }
    }

    public DocumentEvaluationView(List<AbstractDocument> documents) {
        setTitle("ReaderBench - Document Complexity Evaluation");
        setBackground(Color.WHITE);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setBounds(50, 50, 1000, 600);
        this.documents = documents;

        contentPane = new JPanel();
        contentPane.setBackground(Color.WHITE);
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        setContentPane(contentPane);

        JLabel lblPath = new JLabel("Path:");
        lblPath.setFont(new Font("SansSerif", Font.BOLD, 12));

        textFieldDirectory = new JTextField();
        textFieldDirectory.setText("in/corpus_complexity_tasa_en");
        textFieldDirectory.setColumns(10);

        JButton btnPath = new JButton("...");
        btnPath.addActionListener((ActionEvent e) -> {
            JFileChooser fc = new JFileChooser(new File("in"));
            fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            int returnVal = fc.showOpenDialog(DocumentEvaluationView.this);

            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File file = fc.getSelectedFile();
                textFieldDirectory.setText(file.getPath());
            }
        });

        lblResults = new JLabel("Results");
        lblResults.setFont(new Font("SansSerif", Font.BOLD, 12));

        btnSelectComplexityFactors = new JButton("Select complexity factors");
        btnSelectComplexityFactors.addActionListener((ActionEvent e) -> {
            ComplexityIndicesView view = new ComplexityIndicesView();
            view.setVisible(true);
        });

        btnTrainSVM = new JButton("Train SVM Model");
        btnTrainSVM.addActionListener((ActionEvent e) -> {
            String path = textFieldDirectory.getText();
            if (!new File(path).isDirectory()) {
                JOptionPane.showMessageDialog(contentPane,
                        "Specified path should be a directory!", "Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (!new File(path + "/measurements.csv").exists()) {
                JOptionPane
                        .showMessageDialog(
                                contentPane,
                                "Specified path should contain a precomputed \"measurements.csv\" file!",
                                "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            Task task = new Task(path);
            task.execute();
        });

        scrollPane = new JScrollPane();
        scrollPane
                .setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        table = new JTable();
        table.setFillsViewportHeight(true);
        scrollPane.setViewportView(table);

        JSeparator separator = new JSeparator();
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
                                                                                        .addGroup(
                                                                                                gl_contentPane
                                                                                                        .createSequentialGroup()
                                                                                                        .addComponent(
                                                                                                                lblPath)
                                                                                                        .addPreferredGap(
                                                                                                                ComponentPlacement.RELATED)
                                                                                                        .addComponent(
                                                                                                                textFieldDirectory,
                                                                                                                GroupLayout.DEFAULT_SIZE,
                                                                                                                662,
                                                                                                                Short.MAX_VALUE)
                                                                                                        .addPreferredGap(
                                                                                                                ComponentPlacement.RELATED)
                                                                                                        .addComponent(
                                                                                                                btnPath,
                                                                                                                GroupLayout.PREFERRED_SIZE,
                                                                                                                40,
                                                                                                                GroupLayout.PREFERRED_SIZE)
                                                                                                        .addGap(31))
                                                                                        .addGroup(
                                                                                                gl_contentPane
                                                                                                        .createSequentialGroup()
                                                                                                        .addComponent(
                                                                                                                lblResults)
                                                                                                        .addPreferredGap(
                                                                                                                ComponentPlacement.RELATED)))
                                                                        .addPreferredGap(
                                                                                ComponentPlacement.RELATED)
                                                                        .addGroup(
                                                                                gl_contentPane
                                                                                        .createParallelGroup(
                                                                                                Alignment.TRAILING,
                                                                                                false)
                                                                                        .addComponent(
                                                                                                btnTrainSVM,
                                                                                                GroupLayout.DEFAULT_SIZE,
                                                                                                GroupLayout.DEFAULT_SIZE,
                                                                                                Short.MAX_VALUE)
                                                                                        .addComponent(
                                                                                                btnSelectComplexityFactors,
                                                                                                GroupLayout.DEFAULT_SIZE,
                                                                                                GroupLayout.DEFAULT_SIZE,
                                                                                                Short.MAX_VALUE)))
                                                        .addComponent(
                                                                scrollPane,
                                                                Alignment.LEADING,
                                                                GroupLayout.DEFAULT_SIZE,
                                                                978,
                                                                Short.MAX_VALUE)
                                                        .addComponent(
                                                                separator,
                                                                GroupLayout.DEFAULT_SIZE,
                                                                978,
                                                                Short.MAX_VALUE))
                                        .addContainerGap()));
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
                                                                textFieldDirectory,
                                                                GroupLayout.PREFERRED_SIZE,
                                                                GroupLayout.DEFAULT_SIZE,
                                                                GroupLayout.PREFERRED_SIZE)
                                                        .addComponent(lblPath)
                                                        .addComponent(btnPath)
                                                        .addComponent(
                                                                btnSelectComplexityFactors))
                                        .addPreferredGap(
                                                ComponentPlacement.RELATED)
                                        .addGroup(
                                                gl_contentPane
                                                        .createParallelGroup(
                                                                Alignment.TRAILING)
                                                        .addComponent(
                                                                lblResults)
                                                        .addComponent(
                                                                btnTrainSVM))
                                        .addPreferredGap(
                                                ComponentPlacement.RELATED)
                                        .addComponent(separator,
                                                GroupLayout.PREFERRED_SIZE,
                                                GroupLayout.DEFAULT_SIZE,
                                                GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(
                                                ComponentPlacement.RELATED)
                                        .addComponent(scrollPane,
                                                GroupLayout.DEFAULT_SIZE, 473,
                                                Short.MAX_VALUE)
                                        .addContainerGap()));

        contentPane.setLayout(gl_contentPane);
    }
}
