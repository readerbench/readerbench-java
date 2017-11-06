/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package services.complexity.rhythm.views;

import data.Block;
import data.Sentence;
import data.Word;
import data.document.Document;
import java.awt.Color;
import java.awt.Font;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;
import javax.swing.JFrame;
import javax.swing.table.DefaultTableModel;
import services.complexity.ComplexityIndices;
import services.complexity.rhythm.models.IndicesModel;
import services.complexity.rhythm.models.PhonemeModel;
import services.complexity.rhythm.models.SyllabicModel;
import services.complexity.rhythm.tools.RhythmTool;

/**
 *
 * @author valentin.sergiu.cioaca@gmail.com
 */
public class DocumentRhythmView extends JFrame {
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JTable jTable1;
    private javax.swing.JTable jTable2;
    private javax.swing.JTable jTable3;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JTextField jTextField2;
    private javax.swing.JTextField jTextField3;
    private javax.swing.JTextField jTextField4;
    private javax.swing.JTextField jTextField5;
    
    private DefaultTableModel syllabicModel;
    private DefaultTableModel phonemesModel;
    private DefaultTableModel indicesModel;
    
    private final Document document;
    private double avgNrSyll;
    private double avgNrStressedSyll;
    private double rhythmicCoeff;
    private double rhythmicIndex;
    private int rhythmicDiameter;
    private int tonicNumber;
    private Map<Integer, Integer> cntSyllabicStructures;
    private Map<Integer, Long> rhythmicIndicesCount;
    private Map<String, Double> phonemes;

    @SuppressWarnings("OverridableMethodCallInConstructor")
    public DocumentRhythmView(Document documentToProcess) {
        super("ReaderBench - Rhythm Analysis");
	setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        getContentPane().setBackground(Color.WHITE);
        this.document = documentToProcess;
        
        setBounds(50, 50, 1180, 710);
        
        initComponents();
        
        processDocument();
        
        updateLabels();
        updateRhythmicIndices();
        updateSyllabicStructures();
        updatePhonemes();
    }
    
    private void initComponents() {

        jScrollPane2 = new javax.swing.JScrollPane();
        jTable2 = new javax.swing.JTable();
        jScrollPane3 = new javax.swing.JScrollPane();
        jTable3 = new javax.swing.JTable();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jTextField1 = new javax.swing.JTextField();
        jTextField2 = new javax.swing.JTextField();
        jTextField3 = new javax.swing.JTextField();
        jTextField4 = new javax.swing.JTextField();
        jTextField5 = new javax.swing.JTextField();
        jSeparator1 = new javax.swing.JSeparator();

//        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        
        syllabicModel = new SyllabicModel();
        jTable2.setModel(syllabicModel);
        jScrollPane2.setViewportView(jTable2);
        jTable2.setFillsViewportHeight(true);
        
        phonemesModel = new PhonemeModel();
        jTable3.setModel(phonemesModel);
        jScrollPane3.setViewportView(jTable3);
        jTable3.setFillsViewportHeight(true);
        
        indicesModel = new IndicesModel();
        jTable1.setModel(indicesModel);
        jScrollPane1.setViewportView(jTable1);
        jTable1.setFillsViewportHeight(true);

        org.openide.awt.Mnemonics.setLocalizedText(jLabel1,"Rhythmic indices");
        jLabel1.setFont(new Font("SansSerif", Font.BOLD, 12));
        org.openide.awt.Mnemonics.setLocalizedText(jLabel2,"Syllabis structures");
        jLabel2.setFont(new Font("SansSerif", Font.BOLD, 12));
        org.openide.awt.Mnemonics.setLocalizedText(jLabel3,"Phonemes");
        jLabel3.setFont(new Font("SansSerif", Font.BOLD, 12));
        org.openide.awt.Mnemonics.setLocalizedText(jLabel4,"Average number of syllables in sentence:");
        jLabel4.setFont(new Font("SansSerif", Font.BOLD, 12));
        org.openide.awt.Mnemonics.setLocalizedText(jLabel5,"Average number of stressed syllables in sentence:");
        jLabel5.setFont(new Font("SansSerif", Font.BOLD, 12));
        org.openide.awt.Mnemonics.setLocalizedText(jLabel6,"Language rhythmic index:");
        jLabel6.setFont(new Font("SansSerif", Font.BOLD, 12));
        org.openide.awt.Mnemonics.setLocalizedText(jLabel7,"Language rhythmic diameter:");
        jLabel7.setFont(new Font("SansSerif", Font.BOLD, 12));
        org.openide.awt.Mnemonics.setLocalizedText(jLabel8,"Language rhythmic coefficient:");
        jLabel8.setFont(new Font("SansSerif", Font.BOLD, 12));
        
//        DecimalFormat df = new DecimalFormat("#.##");
//        jTextField1.setText(df.format(avgNrSyll));
        jTextField1.setEditable(false);
        jTextField1.setColumns(5);
//        jTextField2.setText(df.format(avgNrStressedSyll));
        jTextField2.setEditable(false);
        jTextField2.setColumns(5);
//        jTextField3.setText(df.format(rhythmicIndex));
        jTextField3.setEditable(false);
        jTextField3.setColumns(5);
//        jTextField4.setText(df.format(rhythmicDiameter));
        jTextField4.setEditable(false);
        jTextField4.setColumns(5);
//        jTextField5.setText(df.format(rhythmicCoeff));
        jTextField5.setEditable(false);
        jTextField5.setColumns(5);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGap(33, 33, 33)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 137, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel1))
                        .addGap(46, 46, 46)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 158, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel2)))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(jLabel4)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(jLabel5)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jTextField2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel7)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jTextField4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel8)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jTextField5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jSeparator1)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel6)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jTextField3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 72, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel3)
                    .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 154, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap(21, Short.MAX_VALUE)
                .addComponent(jLabel3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel4)
                            .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel5)
                            .addComponent(jTextField2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel6)
                            .addComponent(jTextField3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel7)
                            .addComponent(jTextField4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel8)
                            .addComponent(jTextField5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 13, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addComponent(jLabel2)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 155, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addComponent(jLabel1)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 155, javax.swing.GroupLayout.PREFERRED_SIZE))))
                    .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 376, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(23, 23, 23))
        );

        pack();
    }
    
    private void updateLabels() {
        DecimalFormat df = new DecimalFormat("#.##");
        jTextField1.setText(df.format(avgNrSyll));
        jTextField2.setText(df.format(avgNrStressedSyll));
        jTextField3.setText(df.format(rhythmicIndex));
        jTextField4.setText(df.format(rhythmicDiameter));
        jTextField5.setText(df.format(rhythmicCoeff));
    }
    
    private void updateRhythmicIndices() {
        for (Map.Entry<Integer, Long> entry : rhythmicIndicesCount.entrySet()) {
            Object[] row = {entry.getKey(), entry.getValue()};
            indicesModel.addRow(row);
        }
    }
    
    private void updateSyllabicStructures() {
        DecimalFormat df = new DecimalFormat("#.##");
        for (Map.Entry<Integer, Integer> entry : cntSyllabicStructures.entrySet()) {
            Object[] row = {entry.getKey(), tonicNumber, entry.getValue(), 
                            df.format(1.0 * entry.getValue() / tonicNumber)};
            
            syllabicModel.addRow(row);
        }
    }
    
    private void updatePhonemes() {
        DecimalFormat df = new DecimalFormat("#.###");
        Map<Double, String> freqPhonemes = new TreeMap<>(Collections.reverseOrder());
        int noValues = phonemes.values().stream().mapToInt(Number::intValue).sum();
        for (Map.Entry<String, Double> entry : phonemes.entrySet()) {
            freqPhonemes.put(entry.getValue() / noValues, entry.getKey());
        }
        for (Map.Entry<Double, String> entry : freqPhonemes.entrySet()) {
            Object[] row = {entry.getValue(), df.format(entry.getKey())};
            phonemesModel.addRow(row);
        }
    }
    
    private void processDocument() {
        List<Integer> rhythmicIndices = new ArrayList<>();
        List<Integer> countSyllables = new ArrayList<>();
        List<Integer> countStressedSyllables = new ArrayList<>();
        int infRhythmicLimit, supRhythmicLimit, deviations;
        cntSyllabicStructures = new TreeMap<>();
        phonemes = new TreeMap<>();
        phonemes.put("AA", 0.0);
        phonemes.put("AE", 0.0);
        phonemes.put("AH", 0.0);
        phonemes.put("AO", 0.0);
        phonemes.put("AW", 0.0);
        phonemes.put("AY", 0.0);
        phonemes.put("B", 0.0);
        phonemes.put("CH", 0.0);
        phonemes.put("D", 0.0);
        phonemes.put("DH", 0.0);
        phonemes.put("EH", 0.0);
        phonemes.put("ER", 0.0);
        phonemes.put("EY", 0.0);
        phonemes.put("F", 0.0);
        phonemes.put("G", 0.0);
        phonemes.put("HH", 0.0);
        phonemes.put("IH", 0.0);
        phonemes.put("IY", 0.0);
        phonemes.put("JH", 0.0);
        phonemes.put("K", 0.0);
        phonemes.put("L", 0.0);
        phonemes.put("M", 0.0);
        phonemes.put("N", 0.0);
        phonemes.put("NG", 0.0);
        phonemes.put("OW", 0.0);
        phonemes.put("OY", 0.0);
        phonemes.put("P", 0.0);
        phonemes.put("R", 0.0);
        phonemes.put("S", 0.0);
        phonemes.put("SH", 0.0);
        phonemes.put("T", 0.0);
        phonemes.put("TH", 0.0);
        phonemes.put("UH", 0.0);
        phonemes.put("UW", 0.0);
        phonemes.put("V", 0.0);
        phonemes.put("W", 0.0);
        phonemes.put("Y", 0.0);
        phonemes.put("Z", 0.0);
        phonemes.put("ZH", 0.0);
        
        infRhythmicLimit = Integer.MAX_VALUE;
        rhythmicDiameter = supRhythmicLimit = 0;
        deviations = 0;
        for (Block b : document.getBlocks()) {
            if (null == b) {
                continue;
            }
            for (Sentence s : b.getSentences()) {
                List<Word> unit = s.getAllWords();
                // get rhythmic structure SM aproach
                List<Integer> rhythmicStructure = RhythmTool.getRhythmicStructureSM(unit);
                // calculate rhythmic index
                int unitRhythmicIndex = RhythmTool.calcRhythmicIndexSM(unit.size(), rhythmicStructure.size());
                // get phonemes
                RhythmTool.updatePhonemes(unit, phonemes);
                // add rhythmic index to list
                if (unitRhythmicIndex != RhythmTool.UNDEFINED) {
                    rhythmicIndices.add(unitRhythmicIndex);
                }
                // count syllables
                // calculate language rhythmic diameter
                if (!rhythmicStructure.isEmpty()) {
                    int nrSyllUnit = rhythmicStructure.stream().mapToInt(Integer::intValue).sum();
                    countSyllables.add(nrSyllUnit);
                    // update language rhythmic diameter
                    int min = Collections.min(rhythmicStructure);
                    int max = Collections.max(rhythmicStructure);
                    infRhythmicLimit = Math.min(infRhythmicLimit, min);
                    supRhythmicLimit = Math.max(supRhythmicLimit, max);
                    rhythmicDiameter = Math.max(rhythmicDiameter, max - min);
                }
                String[] newRhythmicUnits = s.getText().split("[\\p{Punct}]+");
                int nrStressedSyll = 0;
                for (String newUnit : newRhythmicUnits) {
                    List<String> unitList = Arrays.asList(newUnit.trim().split("\\s+"));
                    List<Integer> repr = RhythmTool.testNewUnitDefinition(unitList);
                    if (!repr.isEmpty()) {
                        // populate map with syllabic structures and their number of appearences
                        for (Integer nr : repr) {
                            if (nr == 0) continue;
                            nrStressedSyll++;
                            cntSyllabicStructures.put(nr,
                            cntSyllabicStructures.containsKey(nr) ? cntSyllabicStructures.get(nr)+1 : 1);
                        }
                        // calculate number of deviations
                        deviations += RhythmTool.calcDeviations(repr);
                    }
                }
                countStressedSyllables.add(nrStressedSyll);
                // get numerical representation
//                List<Integer> nrRepresentation = RhythmTool.getNumericalRepresentation(unit);
//                if (!nrRepresentation.isEmpty()) {
//                    int nrStressedSyll = 0;
//                    // populate map with syllabic structures and their number of appearences
//                    for (Integer nr : nrRepresentation) {
//                        if (nr == 0) continue;
//                        nrStressedSyll++;
//                        cntSyllabicStructures.put(nr,
//                        cntSyllabicStructures.containsKey(nr) ? cntSyllabicStructures.get(nr)+1 : 1);
//                    }
//                    // calculate number of deviations
//                    deviations += RhythmTool.calcDeviations(nrRepresentation);
//                    // add number of stressed syllables
//                    countStressedSyllables.add(nrStressedSyll);
//                }
            }
        }
        rhythmicIndicesCount = rhythmicIndices.stream().collect(Collectors.groupingBy(e -> e, Collectors.counting()));
        // get max rhythmic index
        rhythmicIndex = Collections.max(rhythmicIndices);
        // average number of syllables per sentence
        avgNrSyll = countSyllables.parallelStream()
                    .mapToInt(a -> a)
                    .average().orElse(ComplexityIndices.IDENTITY);
        // average number of stressed syllables per sentence
        avgNrStressedSyll = countStressedSyllables.parallelStream()
                                .mapToInt(a -> a)
                                .average().orElse(ComplexityIndices.IDENTITY);
        // calculate rhythmic coefficient
        tonicNumber = cntSyllabicStructures.values().stream().reduce(0, Integer::sum);
        int dominantInd = RhythmTool.getDominantIndex(cntSyllabicStructures.values().stream()
                .collect(Collectors.toList()));
        int keyOfMaxVal = cntSyllabicStructures.keySet().stream()
                .collect(Collectors.toList()).get(dominantInd);
        int sum = cntSyllabicStructures.get(keyOfMaxVal);
        sum += (cntSyllabicStructures.containsKey(keyOfMaxVal-1)) ? cntSyllabicStructures.get(keyOfMaxVal-1) : 0;
        sum += (cntSyllabicStructures.containsKey(keyOfMaxVal+1)) ? cntSyllabicStructures.get(keyOfMaxVal+1) : 0;
        rhythmicCoeff = 1.0 * (deviations + tonicNumber - sum) / tonicNumber;
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(DocumentRhythmView.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(DocumentRhythmView.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(DocumentRhythmView.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(DocumentRhythmView.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new DocumentRhythmView(null).setVisible(true);
            }
        });
    }                 
}
