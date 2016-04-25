package view.widgets.document;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.util.Vector;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.event.RowSorterEvent;
import javax.swing.event.RowSorterListener;
import javax.swing.table.DefaultTableModel;

import services.commons.Formatting;
import view.models.document.SentenceTable;
import view.models.document.SentenceTableModel;
import data.AbstractDocument;
import data.Block;
import data.Sentence;
import data.discourse.SemanticCohesion;
import services.semanticModels.WordNet.SimilarityType;

public class SentenceView extends JFrame {

    private static final long serialVersionUID = -6137537395545580069L;
    private JTable tableUtterances;
    private DefaultTableModel modelUtterances;
    private AbstractDocument doc;
    private int[] utteranceBlockIndex;

    public SentenceView(AbstractDocument documentToAnalyze) {
        super("ReaderBench - Advanced Visualization");
        this.doc = documentToAnalyze;
        // adjust view to desktop size
        int margin = 50;
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setBounds(margin, margin, screenSize.width - margin * 2,
                screenSize.height - margin * 2);

        createView();
        displayDetails();
    }

    private void createView() {
        int noUtterances = 0;
        for (Block b : doc.getBlocks()) {
            if (b != null) {
                noUtterances += b.getSentences().size();
            }
        }
        utteranceBlockIndex = new int[noUtterances];

        int index = 0;
        for (int blockIndex = 0; blockIndex < doc.getBlocks().size(); blockIndex++) {
            if (doc.getBlocks().get(blockIndex) != null) {
                for (int utteranceIndex = 0; utteranceIndex < doc.getBlocks()
                        .get(blockIndex).getSentences().size(); utteranceIndex++) {
                    utteranceBlockIndex[index++] = blockIndex;
                }
            }
        }

        modelUtterances = new SentenceTableModel();
        tableUtterances = new SentenceTable(modelUtterances,
                utteranceBlockIndex);
        tableUtterances.setAutoCreateRowSorter(true);
        tableUtterances.getColumnModel().getColumn(2).setMinWidth(400);
        tableUtterances.getColumnModel().getColumn(2).setMaxWidth(400);
        tableUtterances.getColumnModel().getColumn(2).setPreferredWidth(400);
        tableUtterances.setFillsViewportHeight(true);

        tableUtterances.getRowSorter().addRowSorterListener(
                new RowSorterListener() {
            @Override
            public void sorterChanged(RowSorterEvent rsevent) {
                // tableUtterances.repaint();
            }
        });

        JScrollPane tableContainer = new JScrollPane(tableUtterances);
        getContentPane().add(tableContainer);
    }

    private void displayDetails() {
        int globalIndex = 0;
        for (Block b : doc.getBlocks()) {
            if (b != null) {
                for (int index = 0; index < b.getSentences().size(); index++) {
                    Vector<Object> dataRow = new Vector<Object>();
                    Sentence u = b.getSentences().get(index);

                    dataRow.add(globalIndex++);
                    dataRow.add(b.getIndex());
                    dataRow.add(u.getText() + " ["
                            + Formatting.formatNumber(u.getOverallScore())
                            + "]");
                    if (index > 0) {
                        SemanticCohesion coh = b.getSentenceDistances()[index - 1][index];
                        dataRow.add(Formatting.formatNumber(coh.getLSASim()));
                        dataRow.add(Formatting.formatNumber(coh.getLDASim()));
                        dataRow.add(Formatting.formatNumber(coh
                                .getOntologySim().get(SimilarityType.LEACOCK_CHODOROW)));
                        dataRow.add(Formatting.formatNumber(coh
                                .getOntologySim().get(SimilarityType.WU_PALMER)));
                        dataRow.add(Formatting.formatNumber(coh
                                .getOntologySim().get(SimilarityType.PATH_SIM)));
                        dataRow.add(Formatting.formatNumber(coh.getCohesion()));
                        dataRow.add((int) (coh.getCohesion() * 100));
                    } else {
                        dataRow.add(0.0d);
                        dataRow.add(0.0d);
                        dataRow.add(0.0d);
                        dataRow.add(0.0d);
                        dataRow.add(0.0d);
                        dataRow.add(0.0d);
                        dataRow.add(0);
                    }
                    modelUtterances.addRow(dataRow);
                }
            }
        }
    }

}
