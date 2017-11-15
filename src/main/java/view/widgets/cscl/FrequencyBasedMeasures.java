/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package view.widgets.cscl;

import data.Block;
import data.cscl.Conversation;
import data.cscl.Participant;
import data.cscl.Utterance;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EtchedBorder;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jtransforms.fft.DoubleFFT_1D;

/**
 *
 * @author admin_licenta
 */
public class FrequencyBasedMeasures extends JFrame {
    private final Conversation  chat;
    private final long          frameTime;
    private Date                chatStartTime;
    private Date                chatEndTime;
    private long                chatTime;
    
    private JPanel              panelFrequency;
    
    @SuppressWarnings("OverridableMethodCallInConstructor")
    public FrequencyBasedMeasures(Conversation chat) {
        super("ReaderBench - Frequency Analysis");
        getContentPane().setBackground(Color.WHITE);
	setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        this.chat = chat;
        this.frameTime = 3 * 60;
        chatStartTime = chatEndTime = null;
        
        setBounds(50, 50, 800, 600);
        
        generateLayout();
    }
    
    private void generateLayout() {
        panelFrequency = new JPanel();
	panelFrequency.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
	panelFrequency.setBackground(Color.WHITE);
	panelFrequency.setLayout(new BorderLayout());
        
        Map<String, List<Date>> timestamps = new TreeMap<>();
        
        for (Participant p : chat.getParticipants()) {
            List<Date> dates = new ArrayList<>();
            for (Block b : p.getContributions().getBlocks()) {
                Date d = ((Utterance) b).getTime();
                dates.add(d);
                if (chatStartTime == null && chatEndTime == null) {
                    chatStartTime = chatEndTime = d;
                } else {
                    if (d.before(chatStartTime)) {
                        chatStartTime = d;
                    }
                    if (d.after(chatEndTime)) {
                        chatEndTime = d;
                    }
                }
            }
            timestamps.put(p.getName(), dates);
        }
        chatTime = (chatEndTime.getTime() - chatStartTime.getTime()) / 1000;
        System.out.println(timestamps);
        Map<String, List<Double>> signals = convertToBinarySignals(timestamps);
//        System.out.println();
//        System.out.println(signals);
        zeroPaddingSignals(signals);
//        System.out.println();
//        System.out.println(signals);
        double[][] magnitudes = new double[signals.size()][];
        int p = 0;
        for (Map.Entry<String, List<Double>> entry : signals.entrySet()) {
            int n = entry.getValue().size();
            magnitudes[p] = new double[n];
            double[] signaleMagnitudes = getMagnitudes(entry.getValue().stream().mapToDouble(d -> d).toArray());
            System.arraycopy(signaleMagnitudes, 0, magnitudes[p++], 0, signaleMagnitudes.length);
        }
        for (int i = 0; i < magnitudes.length; ++i) {
            for (int j = 0; j < magnitudes[0].length; ++j) {
                System.out.print(magnitudes[i][j] + " ");
            }
            System.out.println();
        }
        
        XYDataset dataset = createXYDataset(magnitudes);
        JFreeChart xyLineChart = createXYLineChart(dataset);
        ChartPanel frequency = new ChartPanel(xyLineChart);
        panelFrequency.add(frequency);
        getContentPane().add(panelFrequency);
    }
    
    private Map<String, List<Double>> convertToBinarySignals(Map<String, List<Date>> timestamps) {
        Map<String, List<Double>> signals = new TreeMap<>();
        int index, nrOfFrames;
        long diff;
        
        nrOfFrames = (int)Math.ceil((double)chatTime / frameTime);
        for (Map.Entry<String, List<Date>> entry : timestamps.entrySet()) {
            List<Double> arr = new ArrayList<>(Collections.nCopies(nrOfFrames, 0.0));
            for (Date d : entry.getValue()) {
                diff = (d.getTime() - chatStartTime.getTime()) / 1000;
                index = (int)Math.floor((double)diff / frameTime);
                arr.set(index, 1.0);
            }
            signals.put(entry.getKey(), arr);
        }
        
        return signals;
    }
    
    private void zeroPaddingSignals(Map<String, List<Double>> signals) {
        if (signals.isEmpty())
            return;
        for (List<Double> value : signals.values()) {
            // carefull with overflow
            int n = value.size();
            int nextPowOfTwo = n > 1 ? Integer.highestOneBit(n-1)<<1 : 1;
            for (int i = 0; i < nextPowOfTwo - n; i++) {
                value.add(0.0);
            }
        }
    }
    
    private double[] computeFFT(double[] input) {
        DoubleFFT_1D fftDo = new DoubleFFT_1D(input.length);
        double[] fft = new double[input.length * 2];
        System.arraycopy(input, 0, fft, 0, input.length);
        fftDo.realForwardFull(fft);
        return fft;
    }
    
    private double[] getMagnitudes(double[] signal) {
        double[] fft = computeFFT(signal);
        double[] magnitudes = new double[signal.length];
        for (int i = 0; i < fft.length; i += 2) {
            double re = fft[i];
            double im = fft[i+1];
            magnitudes[i/2] = Math.sqrt(Math.pow(re, 2) + Math.pow(im, 2));
        }
        return magnitudes;
    }
    
    private XYDataset createXYDataset(double[][] dataInput) {
        final XYSeriesCollection dataset = new XYSeriesCollection();
	
        int Fs = 1;
        for (int i = 0; i < dataInput.length; i++) {
            final XYSeries part = new XYSeries("Participant " + (i+1));
            int N = dataInput[i].length;
            for (int j = 0; j < N; j++) {
                double freq = 1.0 * j * Fs / N;
                part.add(freq, dataInput[i][j]);
            }
            dataset.addSeries(part);
        }
	return dataset;
    }
    
    private JFreeChart createXYLineChart(XYDataset dataset) {
        JFreeChart xylineChart = ChartFactory.createXYLineChart(
            "Frequency Based Measures" ,
            "Frequency" ,
            "Magnitude" ,
            dataset,
            PlotOrientation.VERTICAL ,
            true , true , false);
        
        XYPlot xyPlot = xylineChart.getXYPlot();
	ValueAxis domainAxis = xyPlot.getDomainAxis();
	domainAxis.setAutoRange(true);
		
	ValueAxis yAxis = xyPlot.getRangeAxis();
	yAxis.setAutoRange(true);
		
	final XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
        renderer.setSeriesLinesVisible(0, true);
        renderer.setSeriesShapesVisible(1, true);
        for (int i = 0; i < dataset.getSeriesCount(); i++) {
            renderer.setSeriesStroke(i, new BasicStroke(1.0f));
        }
        xyPlot.setRenderer(renderer);
		
	final NumberAxis rangeAxis = (NumberAxis) xyPlot.getRangeAxis();
	rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        
        return xylineChart;
    }

    public static void main(String[] args) {
        double[] input1 = new double[]{1.0, 0.0, 0.0, 1.0, 0.0, 1.0, 0.0, 0.0, 1.0, 1.0, 1.0, 1.0, 1.0, 0.0, 1.0, 1.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0};
        double[] input2 = new double[]{1.0, 0.0, 0.0, 1.0, 0.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 0.0, 1.0, 1.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0};
        double[] input3 = new double[]{1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 0.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0};
        double[] input4 = new double[]{1.0, 1.0, 0.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0};
        
        DoubleFFT_1D fftDo = new DoubleFFT_1D(input3.length);
        double[] fft = new double[input3.length * 2];
        System.arraycopy(input3, 0, fft, 0, input3.length);
        fftDo.realForwardFull(fft);
        
        int fs = 1;
        int N = fft.length;
        for(int i = 0; i < fft.length; i += 2) {
            double re = fft[i];
            double im = fft[i+1];
            System.out.println(re + "\t" + im + "\t" + Math.sqrt(re*re + im*im) + "\t" + 1.0 * i * fs / N);
        }
        
    }
}
