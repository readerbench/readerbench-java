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
package view.models.document;

import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;

import org.jfree.chart.labels.IntervalCategoryToolTipGenerator;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.IntervalCategoryDataset;
import org.jfree.data.gantt.GanttCategoryDataset;
import org.jfree.data.gantt.Task;
import org.jfree.data.gantt.TaskSeriesCollection;

public class CustomToolTipGenerator extends IntervalCategoryToolTipGenerator {

    private static final long serialVersionUID = 8900504035403010692L;

    private static int no = 0;

    public CustomToolTipGenerator() {
        super();
    }

    @Override
    public String generateToolTip(CategoryDataset dataset, int row, int column) {
        String res = "";
        String DATE_FORMAT = "hh:mm:ss";
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);

        if (((GanttCategoryDataset) dataset).getSubIntervalCount(row, column) == 0) {
            no = 0;
            GregorianCalendar cal = new GregorianCalendar();

            if (dataset instanceof IntervalCategoryDataset) {
                IntervalCategoryDataset icd = (IntervalCategoryDataset) dataset;
                TaskSeriesCollection collection = (TaskSeriesCollection) icd;
                Number start = collection.getStartValue(row, column);
                Number end = collection.getEndValue(row, column);
                cal.setTimeInMillis(start.longValue());
                res += sdf.format(cal.getTime());
                cal.setTimeInMillis(end.longValue());
                res += " -> " + sdf.format(cal.getTime());
            }
            res += ": " + ((Task) ((((TaskSeriesCollection) dataset).getSeries(0).getTasks().get(column)))).getDescription();
        } else {
            GregorianCalendar cal = new GregorianCalendar();
            if (dataset instanceof IntervalCategoryDataset) {
                IntervalCategoryDataset icd = (IntervalCategoryDataset) dataset;
                TaskSeriesCollection collection = (TaskSeriesCollection) icd;
                Number start = collection.getStartValue(row, column, no);
                Number end = collection.getEndValue(row, column, no);
                cal.setTimeInMillis(start.longValue());
                res += sdf.format(cal.getTime());
                cal.setTimeInMillis(end.longValue());
                res += " -> " + sdf.format(cal.getTime());
            }
            res += ": " + ((Task) ((((TaskSeriesCollection) dataset).getSeries(0).getTasks().get(column)))).getSubtask(no).getDescription();
            no = (no + 1) % ((GanttCategoryDataset) dataset).getSubIntervalCount(row, column);
        }
        return res;
    }
}
