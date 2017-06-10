package licenta;

import java.awt.*;
import java.util.Random;

import javax.swing.*;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.util.ShapeList;
import org.jfree.util.ShapeUtilities;
import services.commons.VectorAlgebra;

public class TestSpellchecking  extends JFrame{

    private static final int N = 8;
    private static final int SIZE = 1000;
    private static final String title = "Creativity";
    private XYSeries added = new XYSeries("Added");

    public TestSpellchecking(String s, double[][] data) {
        super(s);
        final ChartPanel chartPanel = createDemoPanel(data);
        chartPanel.setPreferredSize(new Dimension(SIZE, SIZE));
        this.add(chartPanel, BorderLayout.CENTER);
    }

    private ChartPanel createDemoPanel(double[][] data) {
        JFreeChart jfreechart = ChartFactory.createScatterPlot(
                "Convergent or divergent points", "Participant 1", "Participant 2", createSampleData(data),
                PlotOrientation.VERTICAL, true, true, false);
        XYPlot xyPlot = (XYPlot) jfreechart.getPlot();
        xyPlot.setDomainCrosshairVisible(true);
        xyPlot.setRangeCrosshairVisible(true);
        XYItemRenderer renderer = xyPlot.getRenderer();
        renderer.setSeriesPaint(0, Color.red);
        renderer.setSeriesPaint(1, Color.green);
        adjustAxis((NumberAxis) xyPlot.getDomainAxis(), true, data);
        adjustAxis((NumberAxis) xyPlot.getRangeAxis(), false, data);
        xyPlot.setBackgroundPaint(Color.black);
        xyPlot.setDomainGridlinesVisible(true);
        xyPlot.setRangeGridlinesVisible(true);

        return new ChartPanel(jfreechart);
    }

    private void adjustAxis(NumberAxis axis, boolean vertical, double[][] data) {
        axis.setRange(1, data.length);
        axis.setTickUnit(new NumberTickUnit(5));
        axis.setVerticalTickLabels(vertical);
    }

    private XYDataset createSampleData(double[][] data) {
        XYSeriesCollection dataset = new XYSeriesCollection();

        //Boys (Age,weight) series
        XYSeries series1 = new XYSeries("Divergent");
        XYSeries series2 = new XYSeries("Convergent");
        for (int i = 0; i < data.length; i++) {
            for (int j = 0; j < data.length; j++) {
                if (data[i][j] > 0) {
                    series2.add(i + 1  , j + 1);
                }
                else if (data[i][j] < 0) {
                    series1.add(i + 1, j + 1);
                }
            }
        }

        dataset.addSeries(series1);
        dataset.addSeries(series2);

        return dataset;
    }

    public static void main(String[] args) {
        double[] d1 = {0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.2,0.2,0.2,0.2,0.2,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,-0.2,-0.2,-0.2,-0.6,-0.6,-0.4,-0.4,-0.4,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0};
        double[] d2 = {0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.6158883083359672,0.6158883083359672,0.6158883083359672,0.6158883083359672,0.6158883083359672,0.0,0.0,0.0,0.0,0.0,0.0,-0.2,-0.2,-0.2,-0.2,-0.2,0.0,0.0,0.0,0.0,0.3386294361119891,0.3386294361119891,0.3386294361119891,0.3386294361119891,0.3386294361119891,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,-0.2,-0.2,-0.2,-0.2,-0.2,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,-0.2,-0.2,-0.2,-0.2,-0.2,0.0,0.0,0.0,0.0,0.2,0.2,0.2,0.2,0.2,0.0,0.0,0.0,-0.2,-0.2,-0.2,-0.2,-0.2,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.2,0.2,0.2,0.2,0.2,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0
        };
        double[][] plot = VectorAlgebra.recurrencePlot(d1, d2);

        TestSpellchecking demo = new TestSpellchecking(title, plot);
        demo.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        demo.pack();
        demo.setLocationRelativeTo(null);
        demo.setVisible(true);
    }
}
