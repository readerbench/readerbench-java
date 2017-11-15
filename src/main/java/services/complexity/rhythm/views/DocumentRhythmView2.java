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
public class DocumentRhythmView2 extends javax.swing.JFrame {

    /**
     * Creates new form Test
     */
    @SuppressWarnings("OverridableMethodCallInConstructor")
    public DocumentRhythmView2(Document documentToProcess) {
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

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">                          
    private void initComponents() {

        jPanel2 = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jTextField1 = new javax.swing.JTextField();
        jTextField2 = new javax.swing.JTextField();
        jTextField3 = new javax.swing.JTextField();
        jTextField4 = new javax.swing.JTextField();
        jTextField5 = new javax.swing.JTextField();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        jLabel6 = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        jTable2 = new javax.swing.JTable();
        jLabel7 = new javax.swing.JLabel();
        jSeparator1 = new javax.swing.JSeparator();
        jSeparator2 = new javax.swing.JSeparator();
        jSeparator3 = new javax.swing.JSeparator();
        jLabel8 = new javax.swing.JLabel();
        jScrollPane3 = new javax.swing.JScrollPane();
        jTable3 = new javax.swing.JTable();
        jTextField8 = new javax.swing.JTextField();
        jTextField9 = new javax.swing.JTextField();
        jLabel9 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        jTextField6 = new javax.swing.JTextField();

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

        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, "Number of sentences in document:");
        jLabel1.setFont(new Font("SansSerif", Font.BOLD, 12));
        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, "Average number of syllables in sentence:");
        jLabel2.setFont(new Font("SansSerif", Font.BOLD, 12));
        org.openide.awt.Mnemonics.setLocalizedText(jLabel3, "Average number of stressed syllables in sentence:");
        jLabel3.setFont(new Font("SansSerif", Font.BOLD, 12));
        org.openide.awt.Mnemonics.setLocalizedText(jLabel4, "Language rhythmic index:");
        jLabel4.setFont(new Font("SansSerif", Font.BOLD, 12));
        org.openide.awt.Mnemonics.setLocalizedText(jLabel5, "Language rhythmic diameter:");
        jLabel5.setFont(new Font("SansSerif", Font.BOLD, 12));
        org.openide.awt.Mnemonics.setLocalizedText(jLabel6, "Rhythmic indices");
        jLabel6.setFont(new Font("SansSerif", Font.BOLD, 12));
        org.openide.awt.Mnemonics.setLocalizedText(jLabel7, "Syllables structures frequencies");
        jLabel7.setFont(new Font("SansSerif", Font.BOLD, 12));
        org.openide.awt.Mnemonics.setLocalizedText(jLabel8, "Phonemes");
        jLabel8.setFont(new Font("SansSerif", Font.BOLD, 12));
        org.openide.awt.Mnemonics.setLocalizedText(jLabel9, "Average number of vocal sentences:");
        jLabel9.setFont(new Font("SansSerif", Font.BOLD, 12));
        org.openide.awt.Mnemonics.setLocalizedText(jLabel10, "Number of deviations from dominant syllabic structure:");
        jLabel10.setFont(new Font("SansSerif", Font.BOLD, 12));
        org.openide.awt.Mnemonics.setLocalizedText(jLabel11, "Language rhythmic coefficient:");
        jLabel11.setFont(new Font("SansSerif", Font.BOLD, 12));

        jTextField1.setText("f1");
        jTextField1.setEditable(false);
        jTextField1.setColumns(5);
        jTextField2.setText("f2");
        jTextField2.setEditable(false);
        jTextField2.setColumns(5);
        jTextField3.setText("f3");
        jTextField3.setEditable(false);
        jTextField3.setColumns(5);
        jTextField4.setText("f4");
        jTextField4.setEditable(false);
        jTextField4.setColumns(5);
        jTextField5.setText("f5");
        jTextField5.setEditable(false);
        jTextField5.setColumns(5);
        jTextField6.setText("f6");
        jTextField6.setEditable(false);
        jTextField6.setColumns(5);
        jTextField8.setText("f8");
        jTextField8.setEditable(false);
        jTextField8.setColumns(5);
        jTextField9.setText("f9");
        jTextField9.setEditable(false);
        jTextField9.setColumns(5);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGap(9, 9, 9)
                                .addComponent(jLabel6)
                                .addGap(125, 125, 125)
                                .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, 173, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(jLabel1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(jLabel4, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(jLabel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(jLabel5, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(jLabel9, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(jLabel10, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jTextField2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jTextField3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jTextField4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jTextField5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jTextField8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jTextField9, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jTextField6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 9, Short.MAX_VALUE)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 148, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 128, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(56, 56, 56)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE, 129, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 174, javax.swing.GroupLayout.PREFERRED_SIZE))))
                        .addGap(47, 47, 47))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jSeparator3, javax.swing.GroupLayout.PREFERRED_SIZE, 387, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel11))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel8, javax.swing.GroupLayout.PREFERRED_SIZE, 157, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 157, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(142, 142, 142))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel8))
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(9, 9, 9)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel2)
                            .addComponent(jTextField2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(9, 9, 9)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel3)
                            .addComponent(jTextField3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(9, 9, 9)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel4)
                            .addComponent(jTextField4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(9, 9, 9)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel5)
                            .addComponent(jTextField5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jTextField8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel9))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jTextField9, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel10))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel11)
                            .addComponent(jTextField6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jSeparator3, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel6, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel7, javax.swing.GroupLayout.Alignment.TRAILING))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jSeparator1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jSeparator2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 175, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jScrollPane2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 175, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 443, Short.MAX_VALUE)))
                .addGap(22, 22, 22))
        );

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, 650, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 11, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        pack();
    }// </editor-fold>                        

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
            java.util.logging.Logger.getLogger(DocumentRhythmView2.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(DocumentRhythmView2.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(DocumentRhythmView2.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(DocumentRhythmView2.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new DocumentRhythmView2(null).setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify                     
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JSeparator jSeparator3;
    private javax.swing.JTable jTable1;
    private javax.swing.JTable jTable2;
    private javax.swing.JTable jTable3;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JTextField jTextField2;
    private javax.swing.JTextField jTextField3;
    private javax.swing.JTextField jTextField4;
    private javax.swing.JTextField jTextField5;
    private javax.swing.JTextField jTextField6;
    private javax.swing.JTextField jTextField8;
    private javax.swing.JTextField jTextField9;
    
    private DefaultTableModel syllabicModel;
    private DefaultTableModel phonemesModel;
    private DefaultTableModel indicesModel;
    
    private final Document document;
    private int noSentences;
    private double avgNoVocalSen;
    private double avgNoSyll;
    private double avgNoStressedSyll;
    private int deviations;
    private double rhythmicCoeff;
    private int rhythmicIndex;
    private int rhythmicDiameter;
    private int tonicNumber;
    private Map<Integer, Integer> cntSyllabicStructures;
    private Map<Integer, Long> rhythmicIndicesCount;
    private Map<String, Double> phonemes;
    // End of variables declaration                   

    private void processDocument() {
        List<Integer> rhythmicIndices = new ArrayList<>();
        List<Integer> countSyllables = new ArrayList<>();
        List<Integer> countStressedSyllables = new ArrayList<>();
        int infRhythmicLimit, supRhythmicLimit;
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
        avgNoVocalSen = 0.0;
        noSentences = document.getNoSentences();
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
                avgNoVocalSen += newRhythmicUnits.length;
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
            }
        }
        avgNoVocalSen /= noSentences;
        rhythmicIndicesCount = rhythmicIndices.stream().collect(Collectors.groupingBy(e -> e, Collectors.counting()));
        // get max rhythmic index
        rhythmicIndex = Collections.max(rhythmicIndices);
        // average number of syllables per sentence
        avgNoSyll = countSyllables.parallelStream()
                    .mapToInt(a -> a)
                    .average().orElse(ComplexityIndices.IDENTITY);
        // average number of stressed syllables per sentence
        avgNoStressedSyll = countStressedSyllables.parallelStream()
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
        deviations += tonicNumber - sum;
        rhythmicCoeff = 1.0 * deviations / tonicNumber;
    }

    private void updateLabels() {
        DecimalFormat df = new DecimalFormat("#.##");
        jTextField1.setText(String.valueOf(noSentences));
        jTextField2.setText(df.format(avgNoSyll));
        jTextField3.setText(df.format(avgNoStressedSyll));
        jTextField4.setText(String.valueOf(rhythmicIndex));
        jTextField5.setText(String.valueOf(rhythmicDiameter));
        jTextField6.setText(df.format(rhythmicCoeff));
        jTextField8.setText(df.format(avgNoVocalSen));
        jTextField9.setText(String.valueOf(deviations));
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
}

