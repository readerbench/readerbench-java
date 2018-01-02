package com.readerbench.services.gma;

import com.readerbench.data.AbstractDocument;
import com.readerbench.data.AnalysisElement;
import com.readerbench.data.Lang;
import com.readerbench.data.Word;
import com.readerbench.data.cscl.CSCLConstants;
import com.readerbench.data.discourse.Keyword;
import com.readerbench.services.gma.models.Cell;
import com.readerbench.services.gma.models.GmaCellRenderer;
import com.readerbench.services.gma.models.GmaTableModel;
import com.readerbench.services.semanticModels.LDA.LDA;
import com.readerbench.services.semanticModels.LSA.LSA;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

//!!!!!!!!!!!! modificari in OntologySupport
//!!!!!!!!!!!! modificari in Word
public class ProblemSpaceView extends JFrame {

    private static final long serialVersionUID = 1L;
    public static final double MIN_LSA_THRESHOLD = 0.7;
    public static final double MAX_LSA_THRESHOLD = 1.0;
    public static final double MIN_LDA_THRESHOLD = 0.7;
    public static final double MAX_LDA_THRESHOLD = 1.0;
    public static final double WORDNET_SIMILARITY_THRESHOLD = 1.0;
    public static final int MAX_NO_OF_LATENT_SIMILARITIES = 3;
    private static double TOPIC_RELATEDNESS_THRESHOLD = 0.6;
    public static final int RETURN_VALUE_EMPTY_CELL = -2;
    public static final int RETURN_VALUE_INPUT_NOT_SET = -1;

    // UI
    JTable table;
    JPanel bottomPanel; // buttons
    JScrollPane tablePanel;
    JSlider ldaSlider;
    JSlider lsaSlider;
    JButton btnSetDependences;
    JButton btnWikiSearch;

    int maxSimilarValuesPerTopic = 0;
    ArrayList<Keyword> basicTopics;
    double ldaThreshold = 0.8;
    double lsaThreshold = 0.7;

    // Used for avoiding to recalculate all the entries.
    // If the value of the threshold is set higher, the entries that have a
    // lower threshold will be deleted
    TreeMap<Keyword, TreeMap<Word, Double>> tableContents;
    GmaTableModel tableModel;
    ArrayList<Cell> inputCells;
    ArrayList<Cell> gmaCellSet;
    AnalysisElement doc;
    Boolean useLatentSimilarities;
    SimilarityHelper similarityHelper;
    LSA lsa;
    LDA lda;

    public ProblemSpaceView(AbstractDocument doc, ArrayList<Keyword> topics,
            Boolean useLatentSimilarities) {

        // UI related
        setTitle("Correlated Concepts");

        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        getContentPane().setBackground(Color.WHITE);

        // adjust view to desktop size
        setBounds(30, 50, 1000, 500);

        // Logic related
        this.doc = doc;
        this.basicTopics = topics;
        this.useLatentSimilarities = useLatentSimilarities;
        tableContents = new TreeMap<>();
        inputCells = new ArrayList<Cell>();

        if(useLatentSimilarities){
            lsa = LSA.loadLSA(CSCLConstants.LSA_PATH, Lang.en);
            lda = LDA.loadLDA(CSCLConstants.LDA_PATH, Lang.en);
        }

        similarityHelper = new SimilarityHelper(doc, useLatentSimilarities);
        getSimilarTopics();

        generateLayout();
        // this must be called after setting up the table layout
        findCrossConsistencyAssesments();
    }

    private void generateLayout() {

        setTopPanelLayout();
        setTableLayout();
        setBottomPannelLayout();

    }

    private void setTopPanelLayout() {

        // LDA
        ldaSlider = new JSlider(JSlider.HORIZONTAL,
                (int) (MIN_LDA_THRESHOLD * 10), (int) (MAX_LDA_THRESHOLD * 10),
                (int) (ldaThreshold * 10));

        // Create the label.
        JLabel ldaSliderLabel = new JLabel("LDA threshold", JLabel.CENTER);
        ldaSliderLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        ldaSlider.setMinorTickSpacing(1);
        ldaSlider.setPaintTicks(false);

        // LSA
        lsaSlider = new JSlider(JSlider.HORIZONTAL,
                (int) (MIN_LSA_THRESHOLD * 10), (int) (MAX_LSA_THRESHOLD * 10),
                (int) (lsaThreshold * 10));

        // Create the label.
        JLabel lsaSliderLabel = new JLabel("LSA threshold", JLabel.CENTER);
        lsaSliderLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        lsaSlider.setMinorTickSpacing(1);
        lsaSlider.setPaintTicks(false);

        if (!useLatentSimilarities) {
            lsaSlider.setEnabled(false);
            lsaSlider.setFocusable(false);
            lsaSliderLabel.setEnabled(false);

            ldaSlider.setEnabled(false);
            ldaSlider.setFocusable(false);
            ldaSliderLabel.setEnabled(false);
        }

        btnSetDependences = new JButton("Set dependeces");
        btnWikiSearch = new JButton("Search Wikipedia");

        JPanel topPanel = new JPanel();
        topPanel.add(ldaSliderLabel, BorderLayout.LINE_START);
        topPanel.add(ldaSlider, BorderLayout.LINE_START);
        topPanel.add(lsaSliderLabel, BorderLayout.CENTER);
        topPanel.add(lsaSlider, BorderLayout.CENTER);

        topPanel.add(btnSetDependences, BorderLayout.LINE_END);
        topPanel.add(btnWikiSearch);

        setListeners();
        this.add(topPanel, BorderLayout.PAGE_START);
    }

    private void setListeners() {
        lsaSlider.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                if (!lsaSlider.getValueIsAdjusting()) {
                    lsaThreshold = ((double) lsaSlider.getValue()) / 10;
                    System.out.println("LSA threshold " + lsaThreshold);
                    updateTable();
                }

            }
        });

        ldaSlider.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                if (!ldaSlider.getValueIsAdjusting()) {
                    ldaThreshold = ((double) ldaSlider.getValue()) / 10;
                    System.out.println("LDA threshold " + ldaThreshold);
                    updateTable();
                }

            }
        });

        btnSetDependences.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFrame view = new CorrelationsView(tableModel.getCellSet());
                view.setVisible(true);
                view.addComponentListener(new ComponentAdapter() {
                    @Override
                    public void componentHidden(ComponentEvent e) {
                        table.repaint();
                        ((JFrame) (e.getComponent())).dispose();
                    }
                });
            }
        });

        btnWikiSearch.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                JFrame view = new WikiResultsView(inputCells, doc);
                view.setVisible(true);
            }
        });
    }

    private void updateTable() {
        tableModel = new GmaTableModel(tableContents, maxSimilarValuesPerTopic,
                ldaThreshold, lsaThreshold);
        table.setModel(tableModel);
        findCrossConsistencyAssesments();
        table.setDefaultRenderer(Object.class, new GmaCellRenderer(inputCells));
        table.repaint();
    }

    private void setBottomPannelLayout() {
        bottomPanel = new JPanel();

    }

    private void setTableLayout() {

        tableModel = new GmaTableModel(tableContents, maxSimilarValuesPerTopic,
                ldaThreshold, lsaThreshold);
        table = new JTable(tableModel);
        table.setFillsViewportHeight(true);

        table.setDefaultRenderer(Object.class, new GmaCellRenderer(inputCells));
        table.setCellSelectionEnabled(true);
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {

                JTable target = (JTable) e.getSource();
                int row = target.getSelectedRow();
                int col = target.getSelectedColumn();
                if (row < 0 || col < 0) {
                    return;
                }

                Cell cell = getCell(row, col);
                // an empty cell was clicked;
                if (cell == null) {
                    return;
                }
                int index = checkIfInputCellSetContains(cell);
                // an empty cell was clicked;
                if (index == RETURN_VALUE_EMPTY_CELL) {
                    return;
                }

                // check if the cell is in the input list, if not, add it, else
                // remove it
                if (index != RETURN_VALUE_INPUT_NOT_SET) {
                    inputCells.remove(index);
                } else {
                    inputCells.add(cell);
                }

                table.setDefaultRenderer(Object.class, new GmaCellRenderer(
                        inputCells));
                table.setCellSelectionEnabled(true);
                requestFocusInWindow();
                table.getSelectionModel().clearSelection();
                table.repaint();

            }
        });

        tablePanel = new JScrollPane(table,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        this.add(tablePanel, BorderLayout.CENTER);

    }

    private Cell getCell(int row, int col) {

        for (Cell cell : tableModel.getCellSet()) {
            if (cell.row == row && cell.col == col) {
                return cell;
            }
        }
        return null;

    }

    private void getSimilarTopics() {
        
        System.out.println("lsa " + lsa.getPath());
        System.out.println("lda " + lda.getPath());
        
        for (Keyword topic : basicTopics) {

            // word & similarity
            TreeMap<Word, Double> similarTopics = similarityHelper
                    .getSimilarTopics(topic.getElement(), lsa, lda);

            if (similarTopics != null && similarTopics.size() > 0) {
                tableContents.put(topic, similarTopics);
                if (maxSimilarValuesPerTopic < similarTopics.size()) {
                    maxSimilarValuesPerTopic = similarTopics.size();
                }
            }
        }
    }

    private void findCrossConsistencyAssesments() {

        inputCells.clear();
        gmaCellSet = tableModel.getCellSet();

        for (int i = 0; i < gmaCellSet.size(); i++) {
            Cell cell1 = gmaCellSet.get(i);

            for (int j = i + 1; j < gmaCellSet.size(); j++) {
                Cell cell2 = gmaCellSet.get(j);

                if (cell1.hasSameUnderlyingTopic(cell2)) {
                    continue;
                }

                if (similarityHelper.isRelated(cell1.word, cell2.word, lsa, lda,
                        TOPIC_RELATEDNESS_THRESHOLD)) {
                    cell1.addToCorrelatedCells(cell2);
                    cell2.addToCorrelatedCells(cell1);

                    System.out.println(cell1.word.getLemma() + " "
                            + cell2.word.getLemma());
                }

            }
        }
    }

    private void printCells(ArrayList<Cell> cells) {
        if (cells != null) {
            for (Cell cell : cells) {
                System.out.println("\t\t\t" + cell.row + " " + cell.col + " "
                        + cell.word.getLemma());
            }
        }
    }

    private void printTableContents() {

        for (Map.Entry<Keyword, TreeMap<Word, Double>> e : tableContents
                .entrySet()) {

            TreeMap<Word, Double> similarConcepts = e.getValue();

            System.out.println("===================================");
            System.out.println("Found " + similarConcepts.size()
                    + " similarities for the concept "
                    + e.getKey().getWord().getLemma());

            for (Map.Entry<Word, Double> entry : similarConcepts.entrySet()) {
                System.out.println(entry.getKey().getLemma() + "\t\t"
                        + entry.getValue());
            }
        }

        System.out.println("===================================");

    }

    // return -1 => inputCells does not contain the cell
    // return -2 => the click was triggered in an empty cell
    int checkIfInputCellSetContains(Cell cell) {

        for (int i = 0; i < inputCells.size(); i++) {
            if (cell.row == inputCells.get(i).row
                    && cell.col == inputCells.get(i).col) {
                return i;
            }
        }

        return RETURN_VALUE_INPUT_NOT_SET;
    }

}
