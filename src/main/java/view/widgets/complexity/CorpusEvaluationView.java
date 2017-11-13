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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.Logger;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JCheckBox;
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
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

import services.commons.Formatting;
import services.complexity.ComputeBalancedMeasure;
import services.complexity.DataGathering;
import data.complexity.Measurement;
import java.awt.HeadlessException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.stream.IntStream;
import org.openide.util.Exceptions;
import services.complexity.ComplexityIndexType;
import utils.LocalizationUtils;

public class CorpusEvaluationView extends JFrame {

    private static final long serialVersionUID = -4518616508590444786L;
    static final Logger LOGGER = Logger.getLogger("");

    private JPanel contentPane;
    private JTextField textFieldDirectory;
    private JCheckBox chckbxAll;
    private JCheckBox chckbxClass;
    private JLabel lblSelectiveMeasurements;
    private JSeparator separator_1;
    private JCheckBox chckbxIndividual;
    private JCheckBox chckbxAllSelected;
    private JLabel lblResults;
    private JLabel lblKCrossValidation;
    private JTextField textFieldCrossValidation;
    private JButton btnSelectComplexityIndices;
    private JSeparator separator_2;
    private JButton btnPerformMeasurements;
    private JScrollPane scrollPane;
    private DefaultTableModel tableModel;
    private JTable table;
    private JLabel lblComments;

    private class Task extends SwingWorker<Void, Void> {

        private final String path;
        private final int Kfolds;
        private int noClasses;

        public Task(String path, int kfolds) {
            super();
            this.path = path;
            this.Kfolds = kfolds;
        }

        public void Test(int[] selectedMeasurements, String testName) {
            Map<Double, List<Measurement>> measurements = DataGathering
                    .getMeasurements(path + "/measurements.csv");

            // split input into training and testing datasets
            // generate random sequence of measurements
            Map<Double, Integer[]> order = new TreeMap<>();
            for (Double classId : measurements.keySet()) {
                order.put(classId,
                        new Integer[measurements.get(classId).size()]);
                for (int j = 0; j < measurements.get(classId).size(); j++) {
                    order.get(classId)[j] = j;
                }
            }

            int aux;
            // perform random swaps of elements
            for (Double classId : measurements.keySet()) {
                for (int k = 0; k < (int) Math
                        .pow(order.get(classId).length, 2); k++) {
                    int i = (int) (Math.random() * order.get(classId).length);
                    int j = (int) (Math.random() * order.get(classId).length);
                    aux = order.get(classId)[i];
                    order.get(classId)[i] = order.get(classId)[j];
                    order.get(classId)[j] = aux;
                }
            }

            double[][] precision = new double[Kfolds][2 * noClasses];

            // perform the K folds
            for (int k = 0; k < Kfolds; k++) {
                List<Measurement> trainingMeasurements = new LinkedList<>();
                List<Measurement> testMeasurements = new LinkedList<>();

                // add entries to corresponding set
                for (Double classId : measurements.keySet()) {
                    int range = Math.round(1.0f / Kfolds
                            * measurements.get(classId).size() - 0.5f);
                    for (int i = 0; i < measurements.get(classId).size(); i++) {
                        if (i >= k * range && i < (k + 1) * range) {
                            testMeasurements.add(measurements.get(classId).get(
                                    order.get(classId)[i]));
                        } else {
                            trainingMeasurements.add(measurements.get(classId)
                                    .get(order.get(classId)[i]));
                        }
                    }
                }

                LOGGER.log(Level.INFO, "Ratio between training and testing sets: {0}/{1}", new Object[]{trainingMeasurements.size(), testMeasurements.size()});
                // run SVM
                ComputeBalancedMeasure svm = new ComputeBalancedMeasure();
                svm.gridSearch(selectedMeasurements, trainingMeasurements,
                        testMeasurements, testName, noClasses, precision, k);
            }

            // output results into corresponding file and into table
            try (BufferedWriter file = new BufferedWriter(new FileWriter(path + "/complexity.csv", true))) {
                List<Object> dataRow = new ArrayList<>();

                file.write("\n" + testName + ",");
                dataRow.add(testName);

                // determine average values for EA
                double cumulativeAvg = 0;
                for (int i = 0; i < noClasses; i++) {
                    double avg = 0;
                    for (int k = 0; k < Kfolds; k++) {
                        avg += precision[k][i];
                    }
                    avg /= Kfolds;
                    cumulativeAvg += avg;
                    file.write(Formatting.formatNumber(avg) + ",");
                    dataRow.add(Formatting.formatNumber(avg));
                }
                cumulativeAvg /= noClasses;
                file.write(Formatting.formatNumber(cumulativeAvg) + ",");
                dataRow.add(Formatting.formatNumber(cumulativeAvg));

                // determine average values for AA
                cumulativeAvg = 0;
                for (int i = 0; i < noClasses; i++) {
                    double avg = 0;
                    for (int k = 0; k < Kfolds; k++) {
                        avg += precision[k][i + noClasses];
                    }
                    avg /= Kfolds;
                    cumulativeAvg += avg;
                    file.write(Formatting.formatNumber(avg) + ",");
                    dataRow.add(Formatting.formatNumber(avg));
                }
                cumulativeAvg /= noClasses;
                file.write(Formatting.formatNumber(cumulativeAvg) + ",");
                dataRow.add(Formatting.formatNumber(cumulativeAvg));

                tableModel.addRow(dataRow.toArray());
            } catch (Exception e) {
                System.err.println("Error: " + e.getMessage());
            }
        }

        public void RunTests() {
            // run measurements, if selected, by including all existing textual             // complexity factors
            if (chckbxAll.isSelected()) {
                int[] factors = IntStream.range(0, ComplexityIndicesView.getAllIndices().length).toArray();
                Test(factors, "All Factors Combined");
            }

            // run measurements, if selected, for each class of textual
            // complexity factors (all within a specific class
            if (chckbxClass.isSelected()) {
                for (ComplexityIndexType complexityClass : ComplexityIndexType.values()) {
                    int[] factors = ComplexityIndicesView.getSelectedMeasurementsByCategory(complexityClass);
                    if (factors.length > 0) {
                        Test(factors, complexityClass.name());
                    }
                }
            }

            // run measurements, if selected, for each selected factor
            if (chckbxIndividual.isSelected()) {
                for (int index : ComplexityIndicesView.getSelectedMeasurements()) {
                    Test(new int[]{index}, ComplexityIndicesView.getAllIndices()[index].getAcronym());
                }
            }

            // run measurements, if selected, by including all selected textual
            // complexity factors
            if (chckbxAllSelected.isSelected()) {
                Test(ComplexityIndicesView.getSelectedMeasurements(),
                        "All Selected Factors Combined");
            }
        }

        @Override
        public Void doInBackground() {
            try {
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
                    return null;
                }

                LOGGER.log(Level.INFO, "Started to train SVM models on {0} classes", noClasses);

                // Empty the output file, write header
                try {
                    // Create file
                    FileWriter fstream = new FileWriter(path
                            + "/complexity.csv");
                    BufferedWriter file = new BufferedWriter(fstream);

                    file.write("Factor,");

                    for (int i = 0; i < noClasses; i++) {
                        file.write("C" + (i + 1) + "(%) EA,");
                    }
                    file.write("Avg. EA,");

                    for (int i = 0; i < noClasses; i++) {
                        file.write("C" + (i + 1) + "(%) AA,");
                    }
                    file.write("Avg. AA");

                    file.close();
                } catch (IOException e) {
                    System.err.println("Error: " + e.getMessage());
                }

                // build the table
                String[] names = new String[2 * noClasses + 3];
                names[0] = "<html>Factor<br/><br/></html>";
                names[noClasses + 1] = "<html>Avg.<br/>EA</html>";
                names[2 * noClasses + 2] = "<html>Avg.<br/>AA</html>";
                for (int i = 0; i < noClasses; i++) {
                    names[i + 1] = "<html>C" + (i + 1) + "<br/>EA</html>";
                    names[noClasses + i + 2] = "<html>C" + (i + 1)
                            + "<br/>AA</html>";
                }

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

                for (int i = 1; i < 2 * noClasses + 3; i++) {
                    // set width for ID and selected
                    table.getColumnModel().getColumn(i).setMinWidth(50);
                    table.getColumnModel().getColumn(i).setMaxWidth(50);
                    table.getColumnModel().getColumn(i).setPreferredWidth(50);
                }

                scrollPane.setViewportView(table);

                RunTests();
            } catch (HeadlessException e) {
                Exceptions.printStackTrace(e);
            }
            return null;
        }

        /*
		 * Executed in event dispatching thread
         */
        @Override
        public void done() {
            Toolkit.getDefaultToolkit().beep();
            btnPerformMeasurements.setEnabled(true);
            setCursor(null); // turn off the wait cursor
        }
    }

    public CorpusEvaluationView() {
        super.setTitle("ReaderBench - " + LocalizationUtils.getTitle(this.getClass()));
        super.setBackground(Color.WHITE);
        super.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        super.setBounds(50, 50, 1000, 600);
        contentPane = new JPanel();
        contentPane.setBackground(Color.WHITE);
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        super.setContentPane(contentPane);

        JLabel lblPath = new JLabel(LocalizationUtils.getGeneric("path") + ":");
        lblPath.setFont(new Font("SansSerif", Font.BOLD, 12));

        textFieldDirectory = new JTextField();
        textFieldDirectory.setText("in/corpus_complexity_tasa_en");
        textFieldDirectory.setColumns(10);

        JButton btnPath = new JButton("...");
        btnPath.addActionListener((ActionEvent e) -> {
            JFileChooser fc = new JFileChooser(new File("in"));
            fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            int returnVal = fc.showOpenDialog(CorpusEvaluationView.this);

            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File file = fc.getSelectedFile();
                textFieldDirectory.setText(file.getPath());
            }
        });

        chckbxAll = new JCheckBox(LocalizationUtils.getLocalizedString(this.getClass(), "boxPerformMeasurementsAllFactors"));
        chckbxAll.setSelected(true);

        chckbxClass = new JCheckBox(LocalizationUtils.getLocalizedString(this.getClass(), "boxPerformMeasurementsEachClass"));
        chckbxClass.setSelected(true);

        JLabel lblBaselines = new JLabel("Baselines");
        lblBaselines.setFont(new Font("SansSerif", Font.BOLD, 12));

        JSeparator separator = new JSeparator();

        lblSelectiveMeasurements = new JLabel(LocalizationUtils.getLocalizedString(this.getClass(), "lblSelecitveMeasuremets"));
        lblSelectiveMeasurements.setFont(new Font("SansSerif", Font.BOLD, 12));

        separator_1 = new JSeparator();

        chckbxIndividual = new JCheckBox(LocalizationUtils.getLocalizedString(this.getClass(), "boxPerformMeasurementsIndividualFactor"));
        chckbxIndividual.setSelected(true);

        chckbxAllSelected = new JCheckBox(LocalizationUtils.getLocalizedString(this.getClass(), "boxPerformMeasurementsAllSelectedFactors"));
        chckbxAllSelected.setSelected(true);

        lblResults = new JLabel(LocalizationUtils.getLocalizedString(this.getClass(), "lblResults"));
        lblResults.setFont(new Font("SansSerif", Font.BOLD, 12));

        lblKCrossValidation = new JLabel(LocalizationUtils.getLocalizedString(this.getClass(), "lblKCrossValidationFolds") + ":");
        lblKCrossValidation.setFont(new Font("SansSerif", Font.BOLD, 12));

        textFieldCrossValidation = new JTextField();
        textFieldCrossValidation.setHorizontalAlignment(SwingConstants.RIGHT);
        textFieldCrossValidation.setText("3");
        textFieldCrossValidation.setColumns(10);

        btnSelectComplexityIndices = new JButton(LocalizationUtils.getLocalizedString(this.getClass(), "lblKCrossValidationFolds"));
        btnSelectComplexityIndices.addActionListener((ActionEvent e) -> {
            ComplexityIndicesView view = new ComplexityIndicesView();
            view.setVisible(true);
        });

        separator_2 = new JSeparator();

        btnPerformMeasurements = new JButton(LocalizationUtils.getLocalizedString(this.getClass(), "btnPerformMeasurements"));
        btnPerformMeasurements.addActionListener((ActionEvent e) -> {
            String path = textFieldDirectory.getText();
            if (!new File(path).isDirectory()) {
                JOptionPane.showMessageDialog(contentPane,
                        LocalizationUtils.getGeneric("msgSelectInputFolder"), "Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (!new File(path + "/measurements.csv").exists()) {
                JOptionPane.showMessageDialog(contentPane,
                        LocalizationUtils.getGeneric("Generic.msgNoMeasurementsFile"),
                        "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            int noFolds;
            try {
                noFolds = Integer.valueOf(textFieldCrossValidation
                        .getText());
            } catch (NumberFormatException exception) {
                noFolds = 0;
            }
            if (noFolds < 2 || noFolds > 10) {
                JOptionPane
                        .showMessageDialog(
                                contentPane,
                                LocalizationUtils.getLocalizedString(this.getClass(), "msgKCrossValidation") + "!",
                                "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            btnPerformMeasurements.setEnabled(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

            Task task = new Task(path, noFolds);
            task.execute();
        });

        scrollPane = new JScrollPane();
        scrollPane
                .setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        table = new JTable();
        table.setFillsViewportHeight(true);
        scrollPane.setViewportView(table);

        lblComments = new JLabel(
                "* EA - " + LocalizationUtils.getLocalizedString(this.getClass(), "lblExactAgreement")
                + " ; AA - " + LocalizationUtils.getLocalizedString(this.getClass(), "lblAdjacentAgreement"));
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
                                                                Alignment.LEADING)
                                                        .addComponent(
                                                                scrollPane,
                                                                GroupLayout.DEFAULT_SIZE,
                                                                778,
                                                                Short.MAX_VALUE)
                                                        .addComponent(
                                                                separator_2,
                                                                GroupLayout.DEFAULT_SIZE,
                                                                778,
                                                                Short.MAX_VALUE)
                                                        .addComponent(
                                                                separator_1,
                                                                GroupLayout.DEFAULT_SIZE,
                                                                778,
                                                                Short.MAX_VALUE)
                                                        .addComponent(
                                                                separator,
                                                                GroupLayout.DEFAULT_SIZE,
                                                                778,
                                                                Short.MAX_VALUE)
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
                                                                                684,
                                                                                Short.MAX_VALUE)
                                                                        .addGap(18)
                                                                        .addComponent(
                                                                                btnPath,
                                                                                GroupLayout.PREFERRED_SIZE,
                                                                                40,
                                                                                GroupLayout.PREFERRED_SIZE))
                                                        .addComponent(chckbxAll)
                                                        .addComponent(
                                                                chckbxClass)
                                                        .addComponent(
                                                                lblBaselines)
                                                        .addComponent(
                                                                lblSelectiveMeasurements)
                                                        .addComponent(
                                                                lblResults)
                                                        .addGroup(
                                                                gl_contentPane
                                                                        .createSequentialGroup()
                                                                        .addComponent(
                                                                                lblKCrossValidation)
                                                                        .addPreferredGap(
                                                                                ComponentPlacement.RELATED)
                                                                        .addComponent(
                                                                                textFieldCrossValidation,
                                                                                GroupLayout.PREFERRED_SIZE,
                                                                                51,
                                                                                GroupLayout.PREFERRED_SIZE))
                                                        .addGroup(
                                                                gl_contentPane
                                                                        .createSequentialGroup()
                                                                        .addGroup(
                                                                                gl_contentPane
                                                                                        .createParallelGroup(
                                                                                                Alignment.LEADING)
                                                                                        .addComponent(
                                                                                                chckbxIndividual)
                                                                                        .addComponent(
                                                                                                chckbxAllSelected))
                                                                        .addPreferredGap(
                                                                                ComponentPlacement.RELATED,
                                                                                243,
                                                                                Short.MAX_VALUE)
                                                                        .addComponent(
                                                                                btnSelectComplexityIndices))
                                                        .addGroup(
                                                                Alignment.TRAILING,
                                                                gl_contentPane
                                                                        .createSequentialGroup()
                                                                        .addComponent(
                                                                                lblComments)
                                                                        .addPreferredGap(
                                                                                ComponentPlacement.RELATED,
                                                                                558,
                                                                                Short.MAX_VALUE)
                                                                        .addComponent(
                                                                                btnPerformMeasurements)))
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
                                                        .addComponent(btnPath))
                                        .addPreferredGap(
                                                ComponentPlacement.RELATED)
                                        .addComponent(lblBaselines)
                                        .addPreferredGap(
                                                ComponentPlacement.RELATED)
                                        .addComponent(separator,
                                                GroupLayout.PREFERRED_SIZE, 2,
                                                GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(
                                                ComponentPlacement.RELATED)
                                        .addComponent(chckbxAll)
                                        .addPreferredGap(
                                                ComponentPlacement.RELATED)
                                        .addComponent(chckbxClass)
                                        .addPreferredGap(
                                                ComponentPlacement.UNRELATED)
                                        .addComponent(lblSelectiveMeasurements)
                                        .addPreferredGap(
                                                ComponentPlacement.RELATED)
                                        .addComponent(separator_1,
                                                GroupLayout.PREFERRED_SIZE, 2,
                                                GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(
                                                ComponentPlacement.RELATED)
                                        .addGroup(
                                                gl_contentPane
                                                        .createParallelGroup(
                                                                Alignment.LEADING)
                                                        .addGroup(
                                                                gl_contentPane
                                                                        .createSequentialGroup()
                                                                        .addComponent(
                                                                                chckbxIndividual)
                                                                        .addPreferredGap(
                                                                                ComponentPlacement.RELATED)
                                                                        .addComponent(
                                                                                chckbxAllSelected)
                                                                        .addPreferredGap(
                                                                                ComponentPlacement.UNRELATED)
                                                                        .addGroup(
                                                                                gl_contentPane
                                                                                        .createParallelGroup(
                                                                                                Alignment.BASELINE)
                                                                                        .addComponent(
                                                                                                lblKCrossValidation)
                                                                                        .addComponent(
                                                                                                textFieldCrossValidation,
                                                                                                GroupLayout.PREFERRED_SIZE,
                                                                                                GroupLayout.DEFAULT_SIZE,
                                                                                                GroupLayout.PREFERRED_SIZE))
                                                                        .addGap(5)
                                                                        .addComponent(
                                                                                lblResults))
                                                        .addComponent(
                                                                btnSelectComplexityIndices))
                                        .addPreferredGap(
                                                ComponentPlacement.RELATED)
                                        .addComponent(separator_2,
                                                GroupLayout.PREFERRED_SIZE, 2,
                                                GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(
                                                ComponentPlacement.RELATED)
                                        .addComponent(scrollPane,
                                                GroupLayout.DEFAULT_SIZE, 269,
                                                Short.MAX_VALUE)
                                        .addPreferredGap(
                                                ComponentPlacement.RELATED)
                                        .addGroup(
                                                gl_contentPane
                                                        .createParallelGroup(
                                                                Alignment.BASELINE)
                                                        .addComponent(
                                                                btnPerformMeasurements)
                                                        .addComponent(
                                                                lblComments))
                                        .addContainerGap()));

        contentPane.setLayout(gl_contentPane);
    }
}
