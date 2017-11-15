/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package services.complexity.rhythm;

import services.complexity.rhythm.tools.RhythmTool;
import data.Block;
import data.Sentence;
import data.Word;
import data.document.Document;
import java.awt.Color;
import java.awt.Font;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;
import javax.swing.GroupLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.LayoutStyle;
import javax.swing.ScrollPaneConstants;
import javax.swing.table.DefaultTableModel;
import services.complexity.ComplexityIndices;
import view.models.document.DocumentTableModel;

/**
 *
 * @author valentin.sergiu.cioaca@gmail.com
 */
public class RhythmDocumentViewOld extends JFrame {
    private final Document document;
    private JLabel lblAvgNrSyll;
    private JLabel lblAvgNrStressedSyll;
    private JLabel lblRhythmicIndex;
    private JLabel lblRhythmicDiameter;
    private JLabel lblRhythmicCoeff;
    
    private JTextField textFieldAvgNrSyll;
    private JTextField textFieldAvgNrStressedSyll;
    private JTextField textFieldRhythmicIndex;
    private JTextField textFieldRhythmicDiameter;
    private JTextField textFieldRhythmicCoeff;
    
    private JTable tableRhythmicIndices;
    private DefaultTableModel modelContent;
    
    private double avgNrSyll;
    private double avgNrStressedSyll;
    private double rhythmicCoeff;
    private double rhythmicIndex;
    private int rhythmicDiameter;
    private Map<Integer, Long> indicesMap;
    
    @SuppressWarnings("OverridableMethodCallInConstructor")
    public RhythmDocumentViewOld(Document d) {
        super("ReaderBench - Rhythm Analysis");
        getContentPane().setBackground(Color.WHITE);
	setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        this.document = d;
        
        setBounds(50, 50, 800, 600);
        
        generateLayout();
    }
    
    private void generateLayout() {
        DecimalFormat df = new DecimalFormat("#.##");
        lblAvgNrSyll = new JLabel("Average number of syllables in rhythmic unit:");
        lblAvgNrSyll.setFont(new Font("SansSerif", Font.BOLD, 12));
        textFieldAvgNrSyll = new JTextField();
        textFieldAvgNrSyll.setEditable(false);
        textFieldAvgNrSyll.setColumns(5);
        textFieldAvgNrSyll.setText(df.format(avgNrSyll));
        
        lblAvgNrStressedSyll = new JLabel("Average number of stressed syllables in rhythmic unit:");
        lblAvgNrStressedSyll.setFont(new Font("SansSerif", Font.BOLD, 12));
        textFieldAvgNrStressedSyll = new JTextField();
        textFieldAvgNrStressedSyll.setEditable(false);
        textFieldAvgNrStressedSyll.setColumns(5);
        textFieldAvgNrStressedSyll.setText(df.format(avgNrStressedSyll));
        
        lblRhythmicIndex = new JLabel("Language rhythmic index:");
        lblRhythmicIndex.setFont(new Font("SansSerif", Font.BOLD, 12));
        textFieldRhythmicIndex = new JTextField();
        textFieldRhythmicIndex.setEditable(false);
        textFieldRhythmicIndex.setColumns(5);
        textFieldRhythmicIndex.setText(df.format(rhythmicIndex));
        
        lblRhythmicDiameter = new JLabel("Language rhythmic diameter:");
        lblRhythmicDiameter.setFont(new Font("SansSerif", Font.BOLD, 12));
        textFieldRhythmicDiameter = new JTextField();
        textFieldRhythmicDiameter.setEditable(false);
        textFieldRhythmicDiameter.setColumns(5);
        textFieldRhythmicDiameter.setText(df.format(rhythmicDiameter));
        
        lblRhythmicCoeff = new JLabel("Language rhythmic coefficient:");
        lblRhythmicCoeff.setFont(new Font("SansSerif", Font.BOLD, 12));
        textFieldRhythmicCoeff = new JTextField();
        textFieldRhythmicCoeff.setEditable(false);
        textFieldRhythmicCoeff.setColumns(5);
        textFieldRhythmicCoeff.setText(df.format(rhythmicCoeff));
        
        JPanel panelRhythmicIndices = new JPanel();
        panelRhythmicIndices.setBackground(Color.WHITE);
        
        GroupLayout groupLayout = new GroupLayout(getContentPane());
        groupLayout
                .setHorizontalGroup(groupLayout
                    .createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addGroup(groupLayout
                        .createSequentialGroup()
                        .addContainerGap()
                        .addGroup(groupLayout
                            .createParallelGroup(GroupLayout.Alignment.TRAILING)
                            .addGroup(groupLayout.createSequentialGroup()
                                .addComponent(lblAvgNrSyll, GroupLayout.DEFAULT_SIZE, 100, Short.MAX_VALUE)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(textFieldAvgNrSyll, GroupLayout.PREFERRED_SIZE, 70, GroupLayout.PREFERRED_SIZE)
                                .addContainerGap(618, Short.MAX_VALUE))
                            .addGroup(groupLayout.createSequentialGroup()
                                .addComponent(lblAvgNrStressedSyll, GroupLayout.DEFAULT_SIZE, 100, Short.MAX_VALUE)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(textFieldAvgNrStressedSyll, GroupLayout.PREFERRED_SIZE, 70, GroupLayout.PREFERRED_SIZE)
                                .addContainerGap(618, Short.MAX_VALUE))
                            .addGroup(groupLayout.createSequentialGroup()
                                .addComponent(lblRhythmicIndex, GroupLayout.DEFAULT_SIZE, 100, Short.MAX_VALUE)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(textFieldRhythmicIndex, GroupLayout.PREFERRED_SIZE, 70, GroupLayout.PREFERRED_SIZE)
                                .addContainerGap(618, Short.MAX_VALUE))
                            .addGroup(groupLayout.createSequentialGroup()
                                .addComponent(lblRhythmicDiameter, GroupLayout.DEFAULT_SIZE, 100, Short.MAX_VALUE)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(textFieldRhythmicDiameter, GroupLayout.PREFERRED_SIZE, 70, GroupLayout.PREFERRED_SIZE)
                                .addContainerGap(618, Short.MAX_VALUE))
                            .addGroup(groupLayout.createSequentialGroup()
                                .addComponent(lblRhythmicCoeff, GroupLayout.DEFAULT_SIZE, 100, Short.MAX_VALUE)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(textFieldRhythmicCoeff, GroupLayout.PREFERRED_SIZE, 70, GroupLayout.PREFERRED_SIZE)
                                .addContainerGap(618, Short.MAX_VALUE))
                            .addGroup(groupLayout.createSequentialGroup()
                                .addComponent(panelRhythmicIndices, GroupLayout.DEFAULT_SIZE, 50, Short.MAX_VALUE)
                                .addContainerGap(400, Short.MAX_VALUE)))));
        groupLayout
                .setVerticalGroup(groupLayout
                    .createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(groupLayout.createSequentialGroup()
                            .addContainerGap()
                            .addGroup(groupLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                .addComponent(lblAvgNrSyll)
                                .addComponent(textFieldAvgNrSyll, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                            .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                            .addGroup(groupLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                .addComponent(lblAvgNrStressedSyll)
                                .addComponent(textFieldAvgNrStressedSyll, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                            .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                            .addGroup(groupLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                .addComponent(lblRhythmicIndex)
                                .addComponent(textFieldRhythmicIndex, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                            .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                            .addGroup(groupLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                .addComponent(lblRhythmicDiameter)
                                .addComponent(textFieldRhythmicDiameter, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                            .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                            .addGroup(groupLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                .addComponent(lblRhythmicCoeff)
                                .addComponent(textFieldRhythmicCoeff, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                            .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(panelRhythmicIndices, GroupLayout.DEFAULT_SIZE, 50, Short.MAX_VALUE)
                            .addContainerGap(100, Short.MAX_VALUE)));
        
        JLabel lblRhythmicIndices = new JLabel("Rhythmic indices");
        lblRhythmicIndices.setFont(new Font("SansSerif", Font.BOLD, 13));

        JSeparator separator = new JSeparator();

        JScrollPane scrollPaneRhythmicIndices = new JScrollPane();
        scrollPaneRhythmicIndices.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        
        GroupLayout gl_panelContents = new GroupLayout(panelRhythmicIndices);
        gl_panelContents
                .setHorizontalGroup(gl_panelContents.createParallelGroup(GroupLayout.Alignment.TRAILING)
                        .addGroup(gl_panelContents.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(gl_panelContents.createParallelGroup(GroupLayout.Alignment.LEADING)
                                        .addComponent(scrollPaneRhythmicIndices, GroupLayout.DEFAULT_SIZE, 50, Short.MAX_VALUE)
                                        .addComponent(separator, GroupLayout.DEFAULT_SIZE, 50, Short.MAX_VALUE)
                                        .addComponent(lblRhythmicIndices))
                                .addContainerGap()));
        gl_panelContents
                .setVerticalGroup(
                        gl_panelContents.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(gl_panelContents.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(lblRhythmicIndices).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(separator, GroupLayout.PREFERRED_SIZE, 2,GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(scrollPaneRhythmicIndices, GroupLayout.DEFAULT_SIZE, 50, Short.MAX_VALUE)
                                .addContainerGap()));
        panelRhythmicIndices.setLayout(gl_panelContents);
        getContentPane().setLayout(groupLayout);
        
        modelContent = new DocumentTableModel();
        tableRhythmicIndices = new JTable(modelContent);
        
        tableRhythmicIndices.getColumnModel().getColumn(0).setMinWidth(25);
        tableRhythmicIndices.getColumnModel().getColumn(0).setMaxWidth(25);
        tableRhythmicIndices.getColumnModel().getColumn(0).setPreferredWidth(25);
        
        tableRhythmicIndices.getColumnModel().getColumn(1).setMinWidth(25);
        tableRhythmicIndices.getColumnModel().getColumn(1).setMaxWidth(25);
        tableRhythmicIndices.getColumnModel().getColumn(1).setPreferredWidth(25);
        
        tableRhythmicIndices.setFillsViewportHeight(true);
        tableRhythmicIndices.setTableHeader(null);
        
        scrollPaneRhythmicIndices.setViewportView(tableRhythmicIndices);
    }
    
    private void processDocument(Document d) {
        List<Integer> rhythmicIndices = new ArrayList<>();
        List<Integer> countSyllables = new ArrayList<>();
        List<Integer> countStressedSyllables = new ArrayList<>();
        Map<Integer, Integer> cntSyllabicStructures = new TreeMap<>();
        int infRhythmicLimit, supRhythmicLimit, deviations;
        
        infRhythmicLimit = Integer.MAX_VALUE;
        rhythmicDiameter = supRhythmicLimit = 0;
        deviations = 0;
        for (Block b : d.getBlocks()) {
            if (null == b) {
                continue;
            }
            for (Sentence s : b.getSentences()) {
                List<Word> unit = s.getAllWords();
                // get rhythmic structure SM aproach
                List<Integer> rhythmicStructure = RhythmTool.getRhythmicStructureSM(unit);
                // get numerical representation
                List<Integer> nrRepresentation = RhythmTool.getNumericalRepresentation(unit);
                // calculate rhythmic index
                int unitRhythmicIndex = RhythmTool.calcRhythmicIndexSM(unit.size(), rhythmicStructure.size());
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
                if (!nrRepresentation.isEmpty()) {
                    int nrStressedSyll = 0;
                    // populate map with syllabic structures and their number of appearences
                    for (Integer nr : nrRepresentation) {
                        if (nr == 0) continue;
                        nrStressedSyll++;
                        cntSyllabicStructures.put(nr,
                        cntSyllabicStructures.containsKey(nr) ? cntSyllabicStructures.get(nr)+1 : 1);
                    }
                    // calculate number of deviations
                    deviations += RhythmTool.calcDeviations(nrRepresentation);
                    // add number of stressed syllables
                    countStressedSyllables.add(nrStressedSyll);
                }
            }
        }
        indicesMap = rhythmicIndices.stream().collect(Collectors.groupingBy(e -> e, Collectors.counting()));
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
        int totalNumber = cntSyllabicStructures.values().stream().reduce(0, Integer::sum);
        int dominantInd = RhythmTool.getDominantIndex(cntSyllabicStructures.values().stream()
                .collect(Collectors.toList()));
        int keyOfMaxVal = cntSyllabicStructures.keySet().stream()
                .collect(Collectors.toList()).get(dominantInd);
        int sum = cntSyllabicStructures.get(keyOfMaxVal);
        sum += (cntSyllabicStructures.containsKey(keyOfMaxVal-1)) ? cntSyllabicStructures.get(keyOfMaxVal-1) : 0;
        sum += (cntSyllabicStructures.containsKey(keyOfMaxVal+1)) ? cntSyllabicStructures.get(keyOfMaxVal+1) : 0;
        rhythmicCoeff = 1.0 * (deviations + totalNumber - sum) / totalNumber;
    }
    
    public static void main(String[] args) {
        JFrame r = new RhythmDocumentViewOld((null));
        r.setVisible(true);
    }
}
