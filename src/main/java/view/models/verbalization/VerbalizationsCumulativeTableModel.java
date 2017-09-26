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
package view.models.verbalization;

import javax.swing.table.DefaultTableModel;
import utils.LocalizationUtils;

public class VerbalizationsCumulativeTableModel extends DefaultTableModel {

    private static final long serialVersionUID = 5068916492406925880L;

    private final Class<?>[] columnTypes = new Class[]{String.class, // Verbalization author
        String.class, // Paraphrasing Annotated / Automatic
        String.class, // Causality Annotated / Automatic
        String.class, // Bridging Annotated / Automatic
        String.class, // Text-based Inferences Annotated / Automatic
        String.class, // Control Annotated / Automatic
        String.class, // KI Annotated / Automatic
        String.class, // Comprehension score
        String.class // Comprehension class
};

    public VerbalizationsCumulativeTableModel() {
        super(new Object[][]{}, new String[]{
            LocalizationUtils.getGeneric("Author"),
            LocalizationUtils.getGeneric("RS.paraphrasing"),
            LocalizationUtils.getGeneric("RS.causality"),
            LocalizationUtils.getGeneric("RS.bridging"),
            LocalizationUtils.getGeneric("RS.TI"),
            LocalizationUtils.getGeneric("RS.KI"),
            LocalizationUtils.getGeneric("RS.control"),
            LocalizationUtils.getGeneric("comprehensionScore"),
            LocalizationUtils.getGeneric("comprehensionClass")
        });
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
