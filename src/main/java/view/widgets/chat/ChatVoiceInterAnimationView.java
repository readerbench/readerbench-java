package view.widgets.chat;

import java.awt.Color;
import java.awt.EventQueue;
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
import javax.swing.JButton;
import javax.swing.JFrame;
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
import data.Block;
import data.Word;
import data.cscl.Conversation;
import data.cscl.Participant;
import data.cscl.Utterance;
import data.discourse.SemanticChain;

public class ChatVoiceInterAnimationView extends JFrame {

	private static final long serialVersionUID = -7963939044051260680L;
	private static final Color[] predefinedColors = { Color.YELLOW, Color.BLUE,
			Color.RED, Color.GREEN, Color.ORANGE, Color.LIGHT_GRAY, Color.GRAY, Color.DARK_GRAY };

	private JPanel contentPane;

	private Map<Participant, Color> participantColors;
	private Vector<Vector<Color>> taskColors;
	private Conversation chat;
	private List<SemanticChain> chains;
	// private Random rand = new Random();
	private int colorIndex = -1;

	private Color getRandomColor() {
		// float hue = rand.nextFloat();
		// // Saturation between 0.2 and 0.4
		// float saturation = (rand.nextInt(2000) + 4000) / 10000f;
		// float luminance = 0.9f;
		// Color generatedColor = Color.getHSBColor(hue, saturation, luminance);
		colorIndex = (colorIndex + 1) % predefinedColors.length;
		return predefinedColors[colorIndex];
	}

	/**
	 * Create the frame.
	 */
	public ChatVoiceInterAnimationView(Conversation chat, List<SemanticChain> chains) {
		super();
		setTitle("ReaderBench - Inter-animation of participants' voices and of implicit (alien) voices");

		this.setSize(1000, 600);
		this.setLocation(50, 50);

		contentPane = new JPanel();
		contentPane.setBackground(Color.WHITE);
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);

		TaskSeries s = new TaskSeries("Voices");
		this.chat = chat;
		this.chains = chains;
		Map<SemanticChain, LinkedList<Block>> subTasks = new TreeMap<SemanticChain, LinkedList<Block>>();
		this.participantColors = new TreeMap<Participant, Color>();
		Participant genericParticipant = new Participant("", chat);
		participantColors.put(genericParticipant, getRandomColor());

		for (Participant p : chat.getParticipants()) {
			participantColors.put(p, getRandomColor());
		}

		taskColors = new Vector<Vector<Color>>();
		for (SemanticChain chain : chains) {
			subTasks.put(chain, new LinkedList<Block>());
			Task t = new Task(chain.toString(), new Date(0), new Date(chat
					.getBlocks().size()));
			s.add(t);
			Vector<Color> newColors = new Vector<Color>();
			taskColors.add(newColors);

			Map<Integer, String> occurrences = new TreeMap<Integer, String>();
			Map<Integer, Participant> participantMapping = new TreeMap<Integer, Participant>();

			for (Word w : chain.getWords()) {
				int blockIndex = w.getBlockIndex();
				if (occurrences.containsKey(blockIndex)) {
					String text = occurrences.get(blockIndex);
					if (text.contains(w.getLemma()))
						text.replace(w.getLemma(), w.getLemma() + "*");
					else
						text = text + " " + w.getLemma();
					occurrences.put(blockIndex, text);
				} else {
					if (((Utterance) chat.getBlocks().get(blockIndex))
							.getParticipant() != null) {
						occurrences.put(blockIndex, ((Utterance) chat
								.getBlocks().get(blockIndex)).getParticipant()
								+ ": " + w.getLemma());
						participantMapping.put(blockIndex, ((Utterance) chat
								.getBlocks().get(blockIndex)).getParticipant());
					} else {
						occurrences.put(blockIndex, w.getLemma());
						participantMapping.put(blockIndex, genericParticipant);
					}

				}
			}

			for (Integer index : occurrences.keySet()) {
				Task subT = new Task(occurrences.get(index), new Date(index),
						new Date(index + 1));
				t.addSubtask(subT);
				newColors.add(participantColors.get(participantMapping
						.get(index)));
			}
		}

		TaskSeriesCollection collection = new TaskSeriesCollection();
		collection.add(s);

		// create the chart...
		JFreeChart chart = ChartFactory.createGanttChart(
				"Inter-animation of participants' voices and of implicit (alien) voices", // chart
				// title
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
		ChartPanel chartPanel = new ChartPanel(chart);

		JButton btnCrossCorrelations = new JButton("Cross-Correlations");
		btnCrossCorrelations.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				EventQueue.invokeLater(new Runnable() {
					@Override
					public void run() {
						JFrame view = new VoiceSynergyView(
								ChatVoiceInterAnimationView.this.chains);
						view.setVisible(true);
					}
				});
			}
		});

		JButton btnSimpleStatistics = new JButton("Simple Statistics");
		btnSimpleStatistics.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				EventQueue.invokeLater(new Runnable() {
					@Override
					public void run() {
						JFrame view = new ChatVoiceSimpleStatistics(
								ChatVoiceInterAnimationView.this.chains);
						view.setVisible(true);
					}
				});
			}
		});

		JButton btnParticipantVoiceCoverage = new JButton(
				"Visualize implicit (alien) voices");
		btnParticipantVoiceCoverage.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				EventQueue.invokeLater(new Runnable() {
					@Override
					public void run() {
						JFrame view = new ParticipantVoiceCoverageView(
								ChatVoiceInterAnimationView.this.chat,
								ChatVoiceInterAnimationView.this.chains,
								ChatVoiceInterAnimationView.this.participantColors);
						view.setVisible(true);
					}
				});
			}
		});
		
		JButton btnSentimentDistributionView = new JButton("Visualize sentiment distribution");
		btnSentimentDistributionView.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				EventQueue.invokeLater(new Runnable() {
					@Override
					public void run() {
						JFrame view = new SentimentDistributionView(
								ChatVoiceInterAnimationView.this.chat,
								ChatVoiceInterAnimationView.this.chains);
						view.setVisible(true);
					}

				});
			}
		});
		GroupLayout gl_contentPane = new GroupLayout(contentPane);
		gl_contentPane.setHorizontalGroup(
			gl_contentPane.createParallelGroup(Alignment.TRAILING)
				.addGroup(gl_contentPane.createSequentialGroup()
					.addContainerGap()
					.addGroup(gl_contentPane.createParallelGroup(Alignment.TRAILING)
						.addComponent(chartPanel, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 978, Short.MAX_VALUE)
						.addGroup(gl_contentPane.createSequentialGroup()
							.addComponent(btnSentimentDistributionView)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(btnParticipantVoiceCoverage)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(btnSimpleStatistics)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(btnCrossCorrelations)))
					.addContainerGap())
		);
		gl_contentPane.setVerticalGroup(
			gl_contentPane.createParallelGroup(Alignment.TRAILING)
				.addGroup(gl_contentPane.createSequentialGroup()
					.addContainerGap()
					.addComponent(chartPanel, GroupLayout.DEFAULT_SIZE, 527, Short.MAX_VALUE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(gl_contentPane.createParallelGroup(Alignment.BASELINE)
						.addComponent(btnCrossCorrelations)
						.addComponent(btnSimpleStatistics)
						.addComponent(btnParticipantVoiceCoverage)
						.addComponent(btnSentimentDistributionView)))
		);
		contentPane.setLayout(gl_contentPane);
	}

	private class MyRenderer extends GanttRenderer {
		private static final long serialVersionUID = 7633873447872085630L;

		private static final int PASS = 2; // assumes two passes
		private int row;
		private int col;
		private int index;

		@Override
		public Paint getItemPaint(int row, int col) {
			if (chat.getParticipants().size() == 0)
				return super.getItemPaint(row, col);
			if (this.row != row || this.col != col) {
				this.row = row;
				this.col = col;
				index = 0;
			}
			int clutIndex = index++ / PASS;
			return taskColors.get(col).get(clutIndex);
		}
	}
}
