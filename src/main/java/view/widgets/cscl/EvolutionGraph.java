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
package view.widgets.cscl;

import java.awt.BasicStroke;
import java.awt.Color;
import java.io.File;
import java.io.IOException;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYSplineRenderer;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

/**
 * @author Mihai Dascalu
 */
public class EvolutionGraph {
	private String[] names;
	private Double[][] values;
	private double[] columns;
	private String title;
	private Color color;
	private JFreeChart chart;
	private boolean isSpline;
	private String XAxes;

	public EvolutionGraph(String title, String XAxes, boolean isSpline,
			String[] names, Double[][] values, double[] columns, Color color) {
		this.title = title;
		this.isSpline = isSpline;
		this.names = names;
		this.values = values;
		this.columns = columns;
		this.color = color;
		this.XAxes = XAxes;
	}

	public ChartPanel evolution() {
		XYDataset dataset = createDataset();
		chart = createChart(dataset);
		ChartPanel chartPanel = new ChartPanel(chart);
		return chartPanel;
	}

	private XYDataset createDataset() {
		XYSeriesCollection dataset = new XYSeriesCollection();
		// create and add the XY series
		for (int i = 0; i < names.length; i++) {
			XYSeries series = new XYSeries(names[i]);
			for (int j = 0; j < columns.length; j++)
				if (values[i][j] != null) {
					series.add(columns[j], values[i][j]);
				}
			dataset.addSeries(series);
		}
		return dataset;
	}

	private JFreeChart createChart(XYDataset dataset) {

		// create the chart...
		chart = ChartFactory.createXYLineChart(title, XAxes, "Value", dataset,
				PlotOrientation.VERTICAL, true, true, true);

		// set the background color for the chart...
		final XYPlot plot = chart.getXYPlot();
		plot.setBackgroundPaint(Color.white);
		plot.setDomainGridlinePaint(Color.lightGray);
		plot.setRangeGridlinePaint(Color.lightGray);
		if (color != null)
			plot.getRenderer().setSeriesPaint(0, color);

		// set the range axis to display integers only...
		NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
		rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());

		// set the domain axis to display integers only...
		NumberAxis domainAxis = (NumberAxis) plot.getDomainAxis();
		domainAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());

		// set plot render: Spline Renderer
		if (isSpline)
			plot.setRenderer(new XYSplineRenderer());

		BasicStroke stroke = new BasicStroke(3f);
		for (int i = 0; i < plot.getDataset().getSeriesCount(); i++)
			plot.getRenderer().setSeriesStroke(i, stroke);

		// OPTIONAL CUSTOMISATION COMPLETED.
		return chart;
	}

	public void saveAsJPG(String path, int width, int height) {
		try {
			ChartUtilities
					.saveChartAsJPEG(new File(path), chart, width, height);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void saveAsPNG(String path, int width, int height) {
		try {
			ChartUtilities.saveChartAsPNG(new File(path), chart, width, height);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
