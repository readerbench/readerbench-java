package view.widgets.chat;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Paint;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Vector;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.border.EmptyBorder;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.renderer.category.GanttRenderer;
import org.jfree.data.gantt.Task;
import org.jfree.data.gantt.TaskSeries;
import org.jfree.data.gantt.TaskSeriesCollection;

import view.models.document.CustomToolTipGeneratorVoice;
import DAO.Block;
import DAO.chat.Chat;
import DAO.chat.Participant;
import DAO.chat.Utterance;
import DAO.discourse.SemanticChain;

public class ParticipantVoiceCoverageView extends JFrame {

	private static final long serialVersionUID = -7963939044051260680L;

	private JPanel contentPane;

	private Map<Participant, Color> participantColors;
	private Vector<Vector<Color>> taskColors;
	private Chat chat;
	private List<SemanticChain> chains;
	private JComboBox<String> comboBoxSelectVoice;
	private ChartPanel chartPanel;
	private JPanel panel;

	/**
	 * Create the frame.
	 */
	public ParticipantVoiceCoverageView(Chat chat, List<SemanticChain> chains,
			Map<Participant, Color> participantColors) {
		super("ReaderBench - Implicit (alien) voices");
		this.chat = chat;
		this.chains = chains;
		this.participantColors = participantColors;
		this.setSize(1000, 600);
		this.setLocation(50, 50);

		contentPane = new JPanel();
		contentPane.setBackground(Color.WHITE);
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);

		panel = new JPanel();

		updateGraph(chains.get(0));

		JLabel lblSelectVoice = new JLabel("Select voice:");

		comboBoxSelectVoice = new JComboBox<String>();
		for (SemanticChain v : chains) {
			comboBoxSelectVoice.addItem(v.toString());
		}

		comboBoxSelectVoice.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (comboBoxSelectVoice.getSelectedIndex() >= 0) {
					panel.removeAll();
					panel.revalidate();
					updateGraph(ParticipantVoiceCoverageView.this.chains
							.get(comboBoxSelectVoice.getSelectedIndex()));
					panel.repaint();
				}
			}
		});

		GroupLayout gl_contentPane = new GroupLayout(contentPane);
		gl_contentPane
				.setHorizontalGroup(gl_contentPane
						.createParallelGroup(Alignment.LEADING)
						.addGroup(
								gl_contentPane
										.createSequentialGroup()
										.addGroup(
												gl_contentPane
														.createParallelGroup(
																Alignment.LEADING)
														.addGroup(
																gl_contentPane
																		.createSequentialGroup()
																		.addGap(6)
																		.addComponent(
																				lblSelectVoice)
																		.addPreferredGap(
																				ComponentPlacement.UNRELATED)
																		.addComponent(
																				comboBoxSelectVoice,
																				GroupLayout.PREFERRED_SIZE,
																				379,
																				GroupLayout.PREFERRED_SIZE))
														.addGroup(
																gl_contentPane
																		.createSequentialGroup()
																		.addContainerGap()
																		.addComponent(
																				panel,
																				GroupLayout.DEFAULT_SIZE,
																				978,
																				Short.MAX_VALUE)))
										.addContainerGap()));
		gl_contentPane
				.setVerticalGroup(gl_contentPane
						.createParallelGroup(Alignment.LEADING)
						.addGroup(
								gl_contentPane
										.createSequentialGroup()
										.addContainerGap()
										.addGroup(
												gl_contentPane
														.createParallelGroup(
																Alignment.BASELINE)
														.addComponent(
																lblSelectVoice)
														.addComponent(
																comboBoxSelectVoice,
																GroupLayout.PREFERRED_SIZE,
																GroupLayout.DEFAULT_SIZE,
																GroupLayout.PREFERRED_SIZE))
										.addPreferredGap(
												ComponentPlacement.RELATED)
										.addComponent(panel,
												GroupLayout.DEFAULT_SIZE, 523,
												Short.MAX_VALUE)
										.addContainerGap()));

		contentPane.setLayout(gl_contentPane);
	}

	private void updateGraph(SemanticChain v) {
		TaskSeries s = new TaskSeries("Voices");
		Map<Participant, LinkedList<Block>> subTasks = new TreeMap<Participant, LinkedList<Block>>();

		taskColors = new Vector<Vector<Color>>();
		for (Participant p : chat.getParticipants()) {
			subTasks.put(p, new LinkedList<Block>());
			Task t = new Task(p.getName(), new Date(0), new Date(chat
					.getBlocks().size()));

			Vector<Color> newColors = new Vector<Color>();
			taskColors.add(newColors);

			Map<Integer, Participant> participantMapping = new TreeMap<Integer, Participant>();

			double[] voiceCoverage = chat.getParticipantBlockDistribution(v, p);

			int noSubtasks = 0;
			for (int blockIndex = 0; blockIndex < voiceCoverage.length; blockIndex++) {
				if (chat.getBlocks().get(blockIndex) != null
						&& voiceCoverage[blockIndex] > 0) {
					participantMapping.put(blockIndex, ((Utterance) chat
							.getBlocks().get(blockIndex)).getParticipant());
					Task subT = new Task(chat.getBlocks().get(blockIndex)
							.getText(), new Date(blockIndex), new Date(
							blockIndex + 1));
					t.addSubtask(subT);
					newColors.add(participantColors.get(participantMapping
							.get(blockIndex)));
					noSubtasks++;
				}
			}
			if (noSubtasks > 0)
				s.add(t);
		}

		TaskSeriesCollection collection = new TaskSeriesCollection();
		collection.add(s);

		// create the chart...
		JFreeChart chart = ChartFactory.createGanttChart(
				"Implicit (alien) voices", // chart title
				"Voice", // domain axis label
				"Utterance", // range axis label
				collection, // data
				false, // include legend
				false, // tooltips
				false // urls
				);

		CategoryPlot plot = (CategoryPlot) chart.getPlot();
		DateAxis range = (DateAxis) plot.getRangeAxis();
		DateFormat formatter = new SimpleDateFormat("S");
		range.setDateFormatOverride(formatter);

		GanttRenderer renderer = new MyRenderer();
		renderer.setBaseToolTipGenerator(new CustomToolTipGeneratorVoice());
		plot.setRenderer(renderer);

		// add the chart to a panel...
		chartPanel = new ChartPanel(chart);
		panel.setLayout(new BorderLayout(0, 0));
		panel.add(chartPanel, BorderLayout.CENTER);
	}

	private class MyRenderer extends GanttRenderer {
		private static final long serialVersionUID = 7633873447872085630L;

		private static final int PASS = 2; // assumes two passes
		private int row;
		private int col;
		private int index;

		@Override
		public Paint getItemPaint(int row, int col) {
			if (chat.getParticipants().size() == 0 || taskColors.size() == 0)
				return super.getItemPaint(row, col);
			if (this.row != row || this.col != col) {
				this.row = row;
				this.col = col;
				index = 0;
			}
			int clutIndex = index++ / PASS;
			if (taskColors.size() > 0 && taskColors.get(col).size() > 0)
				return taskColors.get(col).get(clutIndex);
			return Color.BLACK;
		}
	}
}
