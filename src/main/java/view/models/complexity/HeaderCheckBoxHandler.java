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
package view.models.complexity;

import javax.swing.JTable;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;

public class HeaderCheckBoxHandler implements TableModelListener {

    private final JTable table;
    private final int targetColumnIndex;
    private final boolean selected[];

    public HeaderCheckBoxHandler(JTable table, int index, boolean[] selected) {
        this.table = table;
        this.targetColumnIndex = index;
        this.selected = selected;
    }

    @Override
    public void tableChanged(TableModelEvent e) {
        if (e.getType() == TableModelEvent.UPDATE
                && e.getColumn() == targetColumnIndex) {
            // update selection
            int r = e.getFirstRow();
            int c = e.getColumn();
            TableModel model = (TableModel) e.getSource();
            selected[(Integer) model.getValueAt(r, 0)] = (Boolean) model.getValueAt(r, c);

            updateHeader(table, targetColumnIndex);
        }
    }

    public static void updateHeader(JTable table, int targetColumnIndex) {
        int vci = table.convertColumnIndexToView(targetColumnIndex);
        TableColumn column = table.getColumnModel().getColumn(vci);

        boolean selected = true, deselected = true;
        TableModel m = table.getModel();
        for (int i = 0; i < m.getRowCount(); i++) {
            Boolean b = (Boolean) m.getValueAt(i, targetColumnIndex);
            selected &= b;
            deselected &= !b;
        }
        if (!(selected || deselected)) {
            column.setHeaderValue(Status.INDETERMINATE);
        } else {
            if (selected) {
                column.setHeaderValue(Status.SELECTED);
            } else {
                if (deselected) {
                    column.setHeaderValue(Status.DESELECTED);
                }
            }
        }

        JTableHeader h = table.getTableHeader();
        h.repaint(h.getHeaderRect(vci));
    }
}
