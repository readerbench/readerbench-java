/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package view.widgets.cscl;

import data.Block;
import data.cscl.CSCLCriteria;
import data.cscl.Conversation;
import data.cscl.Participant;
import data.cscl.Utterance;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.border.EtchedBorder;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.time.Second;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.XYDataset;

/**
 *
 * @author admin_licenta
 */
public class RegularityMeasuresView extends JFrame {
    private static final long       serialVersionUID = 7769741643777113677L;
    private final Conversation      chat;
    private final long              frameTime;
    private Date                    chatStartTime;
    private Date                    chatEndTime;
    private long                    chatTime;
    
    private JPanel                  panelRegularity;
    private JPanel                  panelEntropy;
    private JLabel                  lblFrameTime;
    private JTextField              textFieldFrameTime;

    @SuppressWarnings("OverridableMethodCallInConstructor")
    public RegularityMeasuresView(Conversation chat) {
        super("ReaderBench - Time Series Analysis");
        getContentPane().setBackground(Color.WHITE);
	setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        this.chat = chat;
        this.frameTime = 3 * 60;    // seconds
        chatStartTime = chatEndTime = null;
        
        setBounds(50, 50, 800, 600);
        
        generateLayout();
    }
    
    private void generateLayout() {
        lblFrameTime = new JLabel("Frame time duration:");
	lblFrameTime.setFont(new Font("SansSerif", Font.BOLD, 12));
        
        textFieldFrameTime = new JTextField();
        textFieldFrameTime.setEditable(false);
        textFieldFrameTime.setColumns(5);
        textFieldFrameTime.setText(this.frameTime + " sec.");
        
        panelRegularity = new JPanel();
	panelRegularity.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
	panelRegularity.setBackground(Color.WHITE);
	panelRegularity.setLayout(new BorderLayout());
        
        panelEntropy = new JPanel();
	panelEntropy.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
	panelEntropy.setBackground(Color.WHITE);
	panelEntropy.setLayout(new BorderLayout());
        
        Map<String, List<Date>> timestamps = new TreeMap<>();
        Map<String, Double> peaks = new TreeMap<>();
        
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
        
        this.chatTime = (chatEndTime.getTime() - chatStartTime.getTime()) / 1000;
        
        System.out.println("Start time: " + chatStartTime);
        System.out.println("End time: " + chatEndTime);
        System.out.println("Chat duration: " + chatTime);
        System.out.println("Frame time: " + frameTime);
        
        Map<String, List<Double>> measures = countActivities(timestamps);
        for (Map.Entry<String, List<Double>> entry : measures.entrySet()) {
            double value = CSCLCriteria.getValue(CSCLCriteria.PEAK_CHAT_FRAME, 
                    entry.getValue().stream().mapToDouble(d -> d).toArray());
            peaks.put(entry.getKey(), value);    
        }
        System.out.println(peaks);
        
        XYDataset dataset1 = createXYDataset(measures);
        JFreeChart tsChart = createTimeSeriesChart(dataset1);

        CategoryDataset dataset2 = createDataset(peaks);
	JFreeChart barChart = createEntropyChart(dataset2);

        ChartPanel regularity = new ChartPanel(tsChart);
	ChartPanel peak = new ChartPanel(barChart);
        
        panelRegularity.add(regularity);
        panelEntropy.add(peak);
        
//        JSpinner s = new JSpinner();
//        s.setValue(5);
//        s.addChangeListener(new ChangeListener() {
//            @Override
//            public void stateChanged(ChangeEvent e) {
//                JSpinner s = (JSpinner) e.getSource();
//                int ft = ((Number) s.getValue()).intValue() * 60;
//                System.err.println("ft: " + ft);
//            }
//        });
        
        GroupLayout groupLayout = new GroupLayout(getContentPane());
        groupLayout
                .setHorizontalGroup(groupLayout
                    .createParallelGroup(Alignment.LEADING)
                    .addGroup(groupLayout
                        .createSequentialGroup()
                        .addContainerGap()
                        .addGroup(groupLayout
                            .createParallelGroup(Alignment.TRAILING)
                            .addGroup(groupLayout
                                .createSequentialGroup()
                                .addComponent(lblFrameTime, GroupLayout.DEFAULT_SIZE, 100, Short.MAX_VALUE)
                                .addPreferredGap(ComponentPlacement.RELATED)
                                .addComponent(textFieldFrameTime, GroupLayout.PREFERRED_SIZE, 70, GroupLayout.PREFERRED_SIZE)
                                .addContainerGap(618, Short.MAX_VALUE))
                            .addGroup(groupLayout
                                .createParallelGroup(Alignment.TRAILING)
                                .addComponent(panelRegularity, GroupLayout.DEFAULT_SIZE, 788, Short.MAX_VALUE)
                                .addComponent(panelEntropy, GroupLayout.DEFAULT_SIZE, 788, Short.MAX_VALUE)))));
        groupLayout
                .setVerticalGroup(groupLayout
                    .createParallelGroup(Alignment.LEADING)
                        .addGroup(groupLayout
                            .createSequentialGroup()
                            .addContainerGap()
                            .addGroup(groupLayout
                                .createParallelGroup(Alignment.BASELINE)
                                .addComponent(lblFrameTime)
                                .addComponent(textFieldFrameTime, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                            .addPreferredGap(ComponentPlacement.RELATED)
                            .addComponent(panelRegularity, GroupLayout.PREFERRED_SIZE, 330, GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(ComponentPlacement.RELATED)
                            .addComponent(panelEntropy, GroupLayout.PREFERRED_SIZE, 180, GroupLayout.PREFERRED_SIZE)
                            .addContainerGap()));
        
        getContentPane().setLayout(groupLayout);
    }
    
    private Map<String, List<Double>> countActivities(Map<String, List<Date>> timestamps) {
        Map<String, List<Double>> results = new TreeMap<>();
        int index, size;
        long diff;
        
        size = (int)Math.ceil((double)chatTime / frameTime);
        for (Map.Entry<String, List<Date>> entry : timestamps.entrySet()) {
            List<Double> arr = new ArrayList<>(Collections.nCopies(size, 0.0));
            for (Date d : entry.getValue()) {
                diff = (d.getTime() - chatStartTime.getTime()) / 1000;
                index = (int)Math.floor((double)diff / frameTime);
                arr.set(index, arr.get(index) + 1);
            }
            results.put(entry.getKey(), arr);
        }
        
        return results;
    }
    
    private XYDataset createXYDataset(Map<String, List<Double>> dataInput) {
        TimeSeriesCollection timeSeriesCollection = new TimeSeriesCollection();
        int i;
	
        for (Map.Entry<String, List<Double>> entry : dataInput.entrySet()) {
            final TimeSeries timeSeries = new TimeSeries(entry.getKey());
            i = 0;
            for (Double v : entry.getValue()) {
                Date newDate = new Date(chatStartTime.getTime() + i * frameTime * 1000);
                timeSeries.add(new Second(newDate), v);
                i++;
            }
            timeSeriesCollection.addSeries(timeSeries);
        }
		
	return timeSeriesCollection;
    }
    
    private CategoryDataset createDataset(Map<String, Double> dataInput) {
       String category = "Participants";
       DefaultCategoryDataset dataset = new DefaultCategoryDataset( );  
       
       dataInput.entrySet().forEach((entry) -> {
           dataset.setValue(entry.getValue(), category, entry.getKey());
        });

       return dataset; 
    }
    
    private JFreeChart createTimeSeriesChart(XYDataset dataset) {

        JFreeChart tsChart = ChartFactory.createTimeSeriesChart (
            "Regularity distribution",
            "Time",
            "Range",
            dataset,
            true, true, true);
		
	XYPlot xyPlot = tsChart.getXYPlot();
	ValueAxis domainAxis = xyPlot.getDomainAxis();
	domainAxis.setAutoRange(false);
		
	ValueAxis yAxis = xyPlot.getRangeAxis();
	yAxis.setAutoRange(false);
		
	final XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
        renderer.setSeriesLinesVisible(0, true);
        renderer.setSeriesShapesVisible(1, true);
        for (int i = 0; i < dataset.getSeriesCount(); i++) {
            renderer.setSeriesStroke(i, new BasicStroke(2.5f));
        }
        xyPlot.setRenderer(renderer);
		
	final NumberAxis rangeAxis = (NumberAxis) xyPlot.getRangeAxis();
	rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
		
	return tsChart;
    }
    
    private static JFreeChart createEntropyChart(CategoryDataset dataset) {
        
        JFreeChart chart = ChartFactory.createBarChart(
            "Entropy based analysis",               // chart title
            "Participants",                         // domain axis label
            "Value",                                // range axis label
            dataset,                                // data
            PlotOrientation.HORIZONTAL,             // orientation
            false,                                  // include legend
            true,                                   // tooltips?
            false                                   // URLs?
        );

        // set the background color for the chart...
        chart.setBackgroundPaint(Color.white);

        // get a reference to the plot for further customisation...
        CategoryPlot plot = chart.getCategoryPlot();
        plot.setBackgroundPaint(Color.lightGray);
        plot.setDomainGridlinePaint(Color.white);
        plot.setDomainGridlinesVisible(true);
        plot.setRangeGridlinePaint(Color.white);
        
        // set the range axis to display integers only...
        final NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());

        // disable bar outlines...
        BarRenderer renderer = (BarRenderer) plot.getRenderer();
        renderer.setDrawBarOutline(false);
        renderer.setSeriesPaint(0, Color.blue);
        renderer.setMaximumBarWidth(.20);
        
        return chart;
    }
}
