package services.gma.models;

import data.Word;
import data.discourse.Keyword;
import services.gma.models.Cell;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.table.AbstractTableModel;

public class GmaTableModel extends AbstractTableModel {

    private static final long serialVersionUID = 1L;

    TreeMap<Keyword, TreeMap<Word, Double>> originalTableContent;
    ArrayList<Cell> cellSet;
    private String[] columnNames;
    private String[][] tableContent;
    double ldaThreshold;
    double lsaThreshold;

    public GmaTableModel(TreeMap<Keyword, TreeMap<Word, Double>> originalTableContent,
            int maxSimilarValuesPerTopic,
            double ldaThreshold,
            double lsaThreshold) {
        columnNames = new String[originalTableContent.size()];
        this.originalTableContent = originalTableContent;
        this.ldaThreshold = ldaThreshold;
        this.lsaThreshold = lsaThreshold;
        setColumnNames();
        setTableContent(maxSimilarValuesPerTopic);
    }

    private void setColumnNames() {
        int i = 0;
        for (Map.Entry<Keyword, TreeMap<Word, Double>> e : originalTableContent.entrySet()) {
            columnNames[i] = e.getKey().getWord().getLemma();
            i++;
        }
    }

    private void setTableContent(int maxSimilarValuesPerTopic) {
        tableContent = new String[maxSimilarValuesPerTopic + 1][originalTableContent.size()];
        cellSet = new ArrayList<Cell>();

        System.out.println("maxSimilarValuesPerTopic: " + maxSimilarValuesPerTopic);
        System.out.println("Number of topics: " + originalTableContent.size());

        int j = 0;
        for (Map.Entry<Keyword, TreeMap<Word, Double>> e : originalTableContent.entrySet()) {

            TreeMap<Word, Double> similarConceptsi = e.getValue();

            int i = 0;
            for (Map.Entry<Word, Double> entry : similarConceptsi.entrySet()) {
                Word word = entry.getKey();
                if (word.ldaSimilarityToUnderlyingConcept >= ldaThreshold
                        || word.lsaSimilarityToUnderlyingConcept >= lsaThreshold
                        || (word.lsaSimilarityToUnderlyingConcept == -1
                        && word.ldaSimilarityToUnderlyingConcept == -1)) {
                    tableContent[i][j] = entry.getKey().getLemma();
                    cellSet.add(new Cell(i, j, entry.getKey(), e.getKey()));
                    i++;
                }
            }

            j++;
        }
    }

    public ArrayList<Cell> getCellSet() {
        return cellSet;
    }

    @Override
    public int getColumnCount() {

        return columnNames.length;
    }

    @Override
    public int getRowCount() {
        return tableContent.length;
    }

    @Override
    public String getColumnName(int pos) {

        return columnNames[pos];
    }

    @Override
    public Object getValueAt(int row, int column) {

        return tableContent[row][column];
    }

    @Override
    public void setValueAt(Object value, int row, int column) {

        tableContent[row][column] = (String) value;
        fireTableCellUpdated(row, column);
    }

}
