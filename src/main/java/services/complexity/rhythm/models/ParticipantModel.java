/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package services.complexity.rhythm.models;

import javax.swing.table.DefaultTableModel;

/**
 *
 * @author valentin.sergiu.cioaca@gmail.com
 */
public class ParticipantModel extends DefaultTableModel {
    private final Class<?>[] columnTypes = new Class[] { String.class,
                    String.class,
                    String.class,
                    String.class,
                    String.class,
                    String.class
    };
    
    public ParticipantModel() {
            super(new Object[][] {}, new String[] {"Participant",
                "NoContributions", "RhythmicId", "FreqRhythmicId",
                "RhythmicDiameter", "RhythmicCoeff"});
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
