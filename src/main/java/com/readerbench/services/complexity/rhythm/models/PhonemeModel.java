/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.readerbench.services.complexity.rhythm.models;

import javax.swing.table.DefaultTableModel;

/**
 *
 * @author valentin.sergiu.cioaca@gmail.com
 */
public class PhonemeModel extends DefaultTableModel {
    
    private final Class<?>[] columnTypes = new Class[] { String.class, // ID
                    String.class // content
    };

    public PhonemeModel() {
            super(new Object[][] {}, new String[] { "Phoneme", "Frequency" });
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
            return false;
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
            return columnTypes[columnIndex];
    }      
}
