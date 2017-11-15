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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.swing.GroupLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTable;
import javax.swing.LayoutStyle;
import javax.swing.ScrollPaneConstants;
import javax.swing.table.DefaultTableModel;
import services.complexity.rhythm.tools.RhythmTool;
import view.events.LinkMouseListener;
import view.models.document.CustomRhythmTable;
import view.models.document.DocumentTableModel;

/**
 *
 * @author valentin.sergiu.cioaca@gmail.com
 */
public class AssonanceDocumentView extends JFrame {
    private final Document document;
    private DefaultTableModel modelContent;
    private DefaultTableModel modelAssonances;
    private JTable tableContent;
    private JTable tableAssonances;
    private JLabel lblTitleDescription;
    private JLabel lblSourceDescription;
    private JLabel lblURIDescription;
    
    private List<List<String>> units;
    
    @SuppressWarnings("OverridableMethodCallInConstructor")
    public AssonanceDocumentView(Document documentToDisplay) {
        super("ReaderBench - Assonance");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        getContentPane().setBackground(Color.WHITE);
        this.document = documentToDisplay;
        
        setBounds(50, 50, 880, 700);
        
        generateLayout();
        updateContent();
        updateAssonances();
    }
    
    private void generateLayout() {
        JPanel panelHeader = new JPanel();
        panelHeader.setBackground(Color.WHITE);
        
        JPanel panelContents = new JPanel();
        panelContents.setBackground(Color.WHITE);
        
        JPanel panelAssonances = new JPanel();
        panelAssonances.setBackground(Color.WHITE);
        
        GroupLayout groupLayout = new GroupLayout(getContentPane());
        groupLayout
                .setHorizontalGroup(groupLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(groupLayout.createSequentialGroup().addContainerGap()
                                .addGroup(groupLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                        .addComponent(panelHeader, GroupLayout.DEFAULT_SIZE, 868, Short.MAX_VALUE)
                                        .addComponent(panelContents, GroupLayout.DEFAULT_SIZE, 868, Short.MAX_VALUE)
                                        .addComponent(panelAssonances, GroupLayout.DEFAULT_SIZE, 868, Short.MAX_VALUE))
                                .addContainerGap()));
        groupLayout
                .setVerticalGroup(
                        groupLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(groupLayout.createSequentialGroup().addContainerGap()
                                .addComponent(panelHeader, GroupLayout.PREFERRED_SIZE, 53,
                                        GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(panelContents, GroupLayout.DEFAULT_SIZE, 305,
                                                Short.MAX_VALUE)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(panelAssonances, GroupLayout.DEFAULT_SIZE, 305, Short.MAX_VALUE)
                                .addContainerGap()));
        
        JLabel lblContents = new JLabel("Contents");
        lblContents.setFont(new Font("SansSerif", Font.BOLD, 13));

        JSeparator separator = new JSeparator();

        JScrollPane scrollPaneContent = new JScrollPane();
        scrollPaneContent.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        
        GroupLayout gl_panelContents = new GroupLayout(panelContents);
        gl_panelContents
                .setHorizontalGroup(gl_panelContents.createParallelGroup(GroupLayout.Alignment.TRAILING)
                        .addGroup(gl_panelContents.createSequentialGroup().addContainerGap()
                                .addGroup(gl_panelContents
                                        .createParallelGroup(
                                                GroupLayout.Alignment.LEADING)
                                        .addComponent(scrollPaneContent, GroupLayout.DEFAULT_SIZE, 868, Short.MAX_VALUE)
                                        .addComponent(separator, GroupLayout.DEFAULT_SIZE, 868,
                                                Short.MAX_VALUE)
                                        .addComponent(lblContents))
                                .addContainerGap()));
        gl_panelContents
                .setVerticalGroup(
                        gl_panelContents.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(gl_panelContents.createSequentialGroup().addContainerGap()
                                .addComponent(lblContents).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(separator, GroupLayout.PREFERRED_SIZE, 2,
                                        GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(scrollPaneContent, GroupLayout.DEFAULT_SIZE, 280, Short.MAX_VALUE)
                                .addContainerGap()));
        panelContents.setLayout(gl_panelContents);
        
        JLabel lblAlliterations = new JLabel("Assonances");
        lblAlliterations.setFont(new Font("SansSerif", Font.BOLD, 13));

        JScrollPane scrollPaneAlliterations = new JScrollPane();
        scrollPaneAlliterations.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        
        GroupLayout gl_panelAlliterations = new GroupLayout(panelAssonances);
        gl_panelAlliterations
                .setHorizontalGroup(gl_panelAlliterations.createParallelGroup(GroupLayout.Alignment.TRAILING)
                        .addGroup(gl_panelAlliterations.createSequentialGroup().addContainerGap()
                                .addGroup(gl_panelAlliterations
                                        .createParallelGroup(
                                                GroupLayout.Alignment.LEADING)
                                        .addComponent(scrollPaneAlliterations, GroupLayout.DEFAULT_SIZE, 868, Short.MAX_VALUE)
                                        .addComponent(separator, GroupLayout.DEFAULT_SIZE, 868,
                                                Short.MAX_VALUE)
                                        .addComponent(lblAlliterations))
                                .addContainerGap()));
        gl_panelAlliterations
                .setVerticalGroup(
                        gl_panelAlliterations.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(gl_panelAlliterations.createSequentialGroup().addContainerGap()
                                .addComponent(lblAlliterations).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(separator, GroupLayout.PREFERRED_SIZE, 2,
                                        GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(scrollPaneAlliterations, GroupLayout.DEFAULT_SIZE, 290, Short.MAX_VALUE)
                                .addContainerGap()));
        panelAssonances.setLayout(gl_panelAlliterations);
        
        JLabel lblTitle = new JLabel("Title:\n");
        lblTitle.setFont(new Font("Lucida Grande", Font.BOLD, 13));
        JLabel lblSource = new JLabel("Source:");
        JLabel lblURI = new JLabel("URI:");
        
        lblURIDescription = new JLabel("");
        lblSourceDescription = new JLabel("");
        lblTitleDescription = new JLabel("");
        lblTitleDescription.setFont(new Font("Lucida Grande", Font.BOLD, 13));
        
        JSeparator separatorDocument = new JSeparator();

        if (document.getTitleText() != null) {
            lblTitleDescription.setText(document.getTitleText());
        }
        if (document.getSource() != null) {
            lblSourceDescription.setText(document.getSource());
        }
        if (document.getURI() != null) {
            lblURIDescription.setText(document.getURI());
            lblURIDescription.addMouseListener(new LinkMouseListener());
        }

        GroupLayout gl_panelHeader = new GroupLayout(panelHeader);
        gl_panelHeader
                .setHorizontalGroup(
                        gl_panelHeader.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(
                                gl_panelHeader.createSequentialGroup().addContainerGap()
                                .addGroup(gl_panelHeader
                                        .createParallelGroup(
                                                GroupLayout.Alignment.LEADING)
                                        .addComponent(separatorDocument, GroupLayout.DEFAULT_SIZE, 868,
                                                Short.MAX_VALUE)
                                        .addGroup(gl_panelHeader.createSequentialGroup()
                                                .addComponent(lblSource)
                                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(lblSourceDescription).addGap(18)
                                                .addComponent(lblURI)
                                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(lblURIDescription))
                                        .addGroup(gl_panelHeader.createSequentialGroup().addComponent(lblTitle)
                                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED).addComponent(lblTitleDescription,
                                                GroupLayout.DEFAULT_SIZE, 868, Short.MAX_VALUE))).addContainerGap()));
        gl_panelHeader
                .setVerticalGroup(
                        gl_panelHeader
                        .createParallelGroup(
                                GroupLayout.Alignment.LEADING)
                        .addGroup(
                                gl_panelHeader.createSequentialGroup().addContainerGap()
                                .addGroup(gl_panelHeader.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                        .addComponent(lblTitle).addComponent(
                                        lblTitleDescription))
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(gl_panelHeader.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                        .addComponent(lblSource).addComponent(lblSourceDescription)
                                        .addComponent(lblURIDescription).addComponent(lblURI))
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(separatorDocument, GroupLayout.PREFERRED_SIZE, 2, GroupLayout.PREFERRED_SIZE)
                                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));
        panelHeader.setLayout(gl_panelHeader);
        getContentPane().setLayout(groupLayout);
        
        modelContent = new DocumentTableModel();
        tableContent = new CustomRhythmTable(modelContent);
        
        tableContent.getColumnModel().getColumn(0).setMinWidth(25);
        tableContent.getColumnModel().getColumn(0).setMaxWidth(25);
        tableContent.getColumnModel().getColumn(0).setPreferredWidth(25);
        
        tableContent.setFillsViewportHeight(true);
        tableContent.setTableHeader(null);
        
        scrollPaneContent.setViewportView(tableContent);
        
        modelAssonances = new DocumentTableModel();
        tableAssonances = new CustomRhythmTable(modelAssonances);
        
        tableAssonances.getColumnModel().getColumn(0).setMinWidth(25);
        tableAssonances.getColumnModel().getColumn(0).setMaxWidth(25);
        tableAssonances.getColumnModel().getColumn(0).setPreferredWidth(25);

        tableAssonances.setFillsViewportHeight(true);
        tableAssonances.setTableHeader(null);
        
        scrollPaneAlliterations.setViewportView(tableAssonances);
    }
    
    private void updateContent() {
        // clean table
        while (modelContent.getRowCount() > 0) {
            modelContent.removeRow(0);
        }
        units = new ArrayList<>();
        if (    document != null && 
                document.getBlocks() != null && 
                document.getBlocks().size() > 0) {
            int index = 1;
            for (Block b : document.getBlocks()) {
                if (null == b) {
                    continue;
                }
                for (Sentence s : b.getSentences()) {
                    String text = s.getText();
                    Object[] row = {index, text};
                    modelContent.addRow(row);
                    List<String> words = new ArrayList<>();
                    for (Word w : s.getAllWords()) {
                        words.add(w.getText());
                    }
                    units.add(words);
                    index++;
                }
            }
        }
    }
    
    private void updateAssonances() {
        // clean table
        while (modelAssonances.getRowCount() > 0) {
            modelAssonances.removeRow(0);
        }
        Map<Integer, List<List<String>>> assonances = RhythmTool.findAssonances(units);
        for (Map.Entry<Integer, List<List<String>>> entry : assonances.entrySet()) {
            String text = "";
            if (entry.getValue().isEmpty())
                continue;
            for (List<String> assonance : entry.getValue()) {
//                text += "<br>" + "(";
                text += "(";
                for (int i = 0; i < assonance.size() - 1; i++) {
                    text += assonance.get(i) + ", ";
                }
                text += assonance.get(assonance.size() - 1);
//                text += ")" + "</br> ";
                text += ") ";
            }
            Object[] row = {entry.getKey(), text};
            modelAssonances.addRow(row);
        }
//        List<List<String>> assonances = RhythmTool.findAssonances2(units);
//        int index = 1;
//        for (List<String> assonance : assonances) {
//            String text = "(";
//            for (int i = 0; i < assonance.size() - 1; i++) {
//                text += assonance.get(i) + ", ";
//            }
//            text += assonance.get(assonance.size()-1) + ")";
//            Object[] row = {index, text};
//            modelAssonances.addRow(row);
//            index++;
//        }
    }
}
