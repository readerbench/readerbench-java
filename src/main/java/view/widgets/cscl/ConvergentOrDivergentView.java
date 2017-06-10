package view.widgets.cscl;

import data.cscl.Conversation;
import data.cscl.Participant;
import data.discourse.SemanticChain;
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
import services.commons.VectorAlgebra;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * Created by Florea Anda-Madalina
 */
public class ConvergentOrDivergentView extends JFrame {
    private static final int SIZE = 1000;
    private static final String title = "Creativity";

    private Conversation chat;
    private SemanticChain voice;
    private List<Participant> participants;
    private Participant p1;
    private Participant p2;

    public ConvergentOrDivergentView(Conversation chat, List<SemanticChain> selectedVoices, List<Participant>
            selectedParticipants) {

        this.chat = chat;
        this.voice = selectedVoices.get(0);
        this.participants = selectedParticipants;
        this.p1 = this.participants.get(0);
        this.p2 = this.participants.get(1);

        double[] distribution1 = this.chat.getParticipantBlockMovingAverage(this.voice, this.p1);
        double[] distribution2 = this.chat.getParticipantBlockMovingAverage(this.voice, this.p2);

        double[][] data = VectorAlgebra.recurrencePlot(distribution1, distribution2);

        final ChartPanel chartPanel = createDemoPanel(data);
        chartPanel.setPreferredSize(new Dimension(SIZE, SIZE));
        this.add(chartPanel, BorderLayout.CENTER);

        //this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.pack();
        this.setLocationRelativeTo(null);
        this.setVisible(true);
    }

    private ChartPanel createDemoPanel(double[][] data) {
        JFreeChart jfreechart = ChartFactory.createScatterPlot("Convergent or Divergent Points", p1.getName(), p2.getName(),
            createSampleData(data), PlotOrientation.VERTICAL, true, true, false);
            XYPlot xyPlot = (XYPlot) jfreechart.getPlot();
            xyPlot.setDomainCrosshairVisible(true);
            xyPlot.setRangeCrosshairVisible(true);
            XYItemRenderer renderer = xyPlot.getRenderer();

            //divergent points are represented with red color, while convergent points are represented with green color,
            //for consistency with sentiment valence view
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
}
