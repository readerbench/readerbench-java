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

	private static int nr = 0;

	public CustomToolTipGenerator() {
		super();
	}

	public String generateToolTip(CategoryDataset dataset, int row, int column) {
		String res = "";
		String DATE_FORMAT = "hh:mm:ss";
		SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);

		if (((GanttCategoryDataset) dataset).getSubIntervalCount(row, column) == 0) {
			nr = 0;
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
			res += ": "
					+ ((Task) ((((TaskSeriesCollection) dataset).getSeries(0)
							.getTasks().get(column)))).getDescription();
		} else {
			GregorianCalendar cal = new GregorianCalendar();
			if (dataset instanceof IntervalCategoryDataset) {
				IntervalCategoryDataset icd = (IntervalCategoryDataset) dataset;
				TaskSeriesCollection collection = (TaskSeriesCollection) icd;
				Number start = collection.getStartValue(row, column, nr);
				Number end = collection.getEndValue(row, column, nr);
				cal.setTimeInMillis(start.longValue());
				res += sdf.format(cal.getTime());
				cal.setTimeInMillis(end.longValue());
				res += " -> " + sdf.format(cal.getTime());
			}
			res += ": " + ((Task) ((((TaskSeriesCollection) dataset).getSeries(0)
					.getTasks().get(column)))).getSubtask(nr).getDescription();
			nr = (nr + 1)
					% ((GanttCategoryDataset) dataset).getSubIntervalCount(row,
							column);
		}
		return res;
	}
}
