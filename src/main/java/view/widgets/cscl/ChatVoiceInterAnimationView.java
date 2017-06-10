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

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Paint;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.border.EmptyBorder;

import data.sentiment.SentimentEntity;
import data.sentiment.SentimentValence;
import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.semgraph.SemanticGraph;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.renderer.category.GanttRenderer;
import org.jfree.data.gantt.Task;
import org.jfree.data.gantt.TaskSeries;
import org.jfree.data.gantt.TaskSeriesCollection;

import data.Sentence;
import data.Word;
import data.cscl.Conversation;
import data.cscl.Participant;
import data.cscl.Utterance;
import data.discourse.SemanticChain;
import data.discourse.SentimentVoice;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.neural.rnn.RNNCoreAnnotations;
import services.nlp.parsing.Context;
import services.nlp.parsing.ContextSentiment;
import services.nlp.parsing.Parsing;
import view.models.document.CustomToolTipGeneratorVoice;

public class ChatVoiceInterAnimationView extends JFrame {

	static final Logger LOGGER = Logger.getLogger("");
	private static final long serialVersionUID = -7963939044051260680L;
	private static final Color[] predefinedColors = { Color.YELLOW, Color.BLUE, Color.ORANGE, Color.CYAN, Color.MAGENTA,
			Color.PINK, Color.RED, Color.GREEN, Color.GRAY };

	private JPanel contentPane;
	private ChartPanel chartPanel;
	private JCheckBox chckbxSentiment;
	private JCheckBox chckbxContext;
	private JButton btnCrossCorrelations;
	private JButton btnSimpleStatistics;
	private JButton btnParticipantVoiceCoverage;

	private Map<Participant, Color> participantColors;
	private Map<Double, Color> sentimentColors;
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

		this.chat = chat;
		this.chains = chains;
		// color is associated with the valence
		this.sentimentColors = new TreeMap<Double, Color>();
		// very negative
		sentimentColors.put(-2.0, new Color(161, 40, 48));
		// negative
		sentimentColors.put(-1.0, Color.RED);
		// neutral
		sentimentColors.put(0.0, Color.GRAY);
		// positive
		sentimentColors.put(1.0, Color.GREEN);
		// very positive
		sentimentColors.put(2.0, new Color(26, 102, 46));

		this.participantColors = new TreeMap<Participant, Color>();
		Participant genericParticipant = new Participant("", chat);
		participantColors.put(genericParticipant, getRandomColor());

		for (Participant p : chat.getParticipants()) {
			participantColors.put(p, getRandomColor());
		}

		contentPane = new JPanel();
		contentPane.setBackground(Color.WHITE);
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);

		chckbxSentiment = new JCheckBox("SentimentAnalysis");
		chckbxSentiment.setBackground(Color.WHITE);
		chckbxSentiment.setSelected(false);

		chckbxContext = new JCheckBox("using context");
		chckbxContext.setBackground(Color.WHITE);
		chckbxContext.setSelected(false);
		chckbxContext.setEnabled(false);

		TaskSeries s = compute(chat, chains);

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
		chartPanel = new ChartPanel(chart);

		chckbxContext.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				TaskSeries s = compute(chat, chains);
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

				chartPanel = new ChartPanel(chart);
				contentPane.removeAll();
				addButtons();
				paintView();
				contentPane.repaint();
			}
		});


		chckbxSentiment.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if (chckbxSentiment.isSelected()) {
					chckbxContext.setEnabled(true);
				}
				else {
					chckbxContext.setSelected(false);
					chckbxContext.setEnabled(false);
				}
				TaskSeries s = compute(chat, chains);
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

				chartPanel = new ChartPanel(chart);
				contentPane.removeAll();
				addButtons();
				paintView();
				contentPane.repaint();
			}
		});

		addButtons();

		paintView();
	}

	private void addButtons() {
		btnCrossCorrelations = new JButton("Cross-Correlations");
		btnCrossCorrelations.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				EventQueue.invokeLater(new Runnable() {
					@Override
					public void run() {
						JFrame view;
						view = new VoiceSynergyView(ChatVoiceInterAnimationView.this.chains);
						view.setVisible(true);
					}
				});
			}
		});

		btnSimpleStatistics = new JButton("Simple Statistics");
		btnSimpleStatistics.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				EventQueue.invokeLater(new Runnable() {
					@Override
					public void run() {
						JFrame view = new ChatVoiceSimpleStatistics(ChatVoiceInterAnimationView.this.chains);
						view.setVisible(true);
					}
				});
			}
		});

		btnParticipantVoiceCoverage = new JButton("Visualize implicit (alien) voices");
		btnParticipantVoiceCoverage.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				EventQueue.invokeLater(new Runnable() {
					@Override
					public void run() {
						JFrame view = new ParticipantVoiceCoverageView(ChatVoiceInterAnimationView.this.chat,
								ChatVoiceInterAnimationView.this.chains,
								ChatVoiceInterAnimationView.this.participantColors);
						view.setVisible(true);
					}
				});
			}
		});
	}

	private void paintView() {
		GroupLayout gl_contentPane = new GroupLayout(contentPane);
		gl_contentPane.setHorizontalGroup(gl_contentPane.createParallelGroup(Alignment.TRAILING).addGroup(gl_contentPane
				.createSequentialGroup().addContainerGap()
				.addGroup(gl_contentPane.createParallelGroup(Alignment.TRAILING)
						.addComponent(chartPanel, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 978, Short.MAX_VALUE)
						.addGroup(gl_contentPane.createSequentialGroup().addComponent(chckbxSentiment)
								.addPreferredGap(ComponentPlacement.RELATED).addComponent(chckbxContext)
								.addPreferredGap(ComponentPlacement.RELATED).addComponent(btnParticipantVoiceCoverage)
								.addPreferredGap(ComponentPlacement.RELATED).addComponent(btnSimpleStatistics)
								.addPreferredGap(ComponentPlacement.RELATED).addComponent(btnCrossCorrelations)))
				.addContainerGap()));
		gl_contentPane.setVerticalGroup(gl_contentPane.createParallelGroup(Alignment.TRAILING)
				.addGroup(gl_contentPane.createSequentialGroup().addContainerGap()
						.addComponent(chartPanel, GroupLayout.DEFAULT_SIZE, 527, Short.MAX_VALUE)
						.addPreferredGap(ComponentPlacement.RELATED)
						.addGroup(gl_contentPane.createParallelGroup(Alignment.BASELINE).addComponent(chckbxSentiment)
								.addComponent(chckbxContext).addComponent(btnCrossCorrelations).addComponent(btnSimpleStatistics)
								.addComponent(btnParticipantVoiceCoverage))));
		contentPane.setLayout(gl_contentPane);
	}

	private TaskSeries compute(Conversation chat, List<SemanticChain> chains) {
		TaskSeries s = new TaskSeries("Voices");
		taskColors = new Vector<Vector<Color>>();

		for (SemanticChain chain : chains) {
			Task t = new Task(chain.toString(), new Date(0), new Date(chat.getBlocks().size()));
			s.add(t);
			Vector<Color> newColors = new Vector<Color>();
			taskColors.add(newColors);

			Map<Integer, String> occurrences = new TreeMap<Integer, String>();
			Map<Integer, Participant> participantMapping = new TreeMap<Integer, Participant>();
			// build a sentimentVoice structure
			Map<Integer, SentimentVoice> m = new TreeMap<Integer, SentimentVoice>();

			for (Word w : chain.getWords()) {
				int blockIndex = w.getBlockIndex();

				if (!chckbxSentiment.isSelected()) {
					if (occurrences.containsKey(blockIndex)) {
						String text = occurrences.get(blockIndex);
						if (text.contains(w.getLemma()))
							text.replace(w.getLemma(), w.getLemma() + "*");
						else
							text = text + " " + w.getLemma();
						occurrences.put(blockIndex, text);
					} else {
						if (((Utterance) chat.getBlocks().get(blockIndex)).getParticipant() != null) {
							occurrences.put(blockIndex, ((Utterance) chat.getBlocks().get(blockIndex)).getParticipant()
									+ ": " + w.getLemma());
							participantMapping.put(blockIndex,
									((Utterance) chat.getBlocks().get(blockIndex)).getParticipant());
						} else {
							occurrences.put(blockIndex, w.getLemma());
							participantMapping.put(blockIndex, new Participant("", chat));
						}
					}
				}

				// sentiment analysis is checked
				else {
					double valence;
					int noSentences = 0;
					double sumValences = 0;
					String sentenceOrContext = "";

					for (Sentence sentence : chat.getBlocks().get(blockIndex).getSentences()) {
						List<Word> words = sentence.getWords();
						// find which sentence contains the word from voice
						for (Word aux : words) {
							if (aux.getText().equals(w.getText())) {
								//for adj it is considered only its polarity
								if (!w.isNoun() && !w.isVerb()) {
									valence = 0;
									SentimentEntity sentiments = w.getSentiment();
									// this adj is in LIWC list with positive words
									if (sentiments.contains(new data.sentiment.SentimentValence(
											310, "Posemo_LIWC", "Posemo_LIWC", false))) {
										valence = 1.0;
									}
									// this adj is in LIWC list with negative words
									else if (sentiments.contains(new data.sentiment.SentimentValence(
											311, "Negemo_LIWC", "Negemo_LIWC", false))) {
										valence = -1.0;
									}
									noSentences++;
									sumValences += valence;
									sentenceOrContext = w.getText();
									//when the context is analyzed, the valence is concatenated
									if (chckbxContext.isSelected()) {
										sentenceOrContext += " " + valence;
									}
									break;
								}

								if (chckbxContext.isSelected()) {
									// sentiment analysis using the subgraph
									// dominated by voice
									Context ctx = new Context();
									List<ContextSentiment> ctxTrees = sentence.getContextMap().get(w);

									int noCtxTrees = ctxTrees.size();
									double valenceForContext = 0;
									//compute the average valence for contextTrees
									for (ContextSentiment ctxTree: ctxTrees) {
										valenceForContext += ctxTree.getValence();
									}
									valence = Math.round(valenceForContext/noCtxTrees);

									//save in order to calculate the average valence for utterance
									noSentences++;
									sumValences += valence;


									//display the context: only the words which are in the subTree
									String[] sentenceWords = sentence.getAlternateText().split("[\\p{Punct}\\s]+");
									for (ContextSentiment ctxTree:ctxTrees) {
										Tree subTree = ctxTree.getContextTree();
										for (int wordIndex = 0; wordIndex < sentenceWords.length; wordIndex++) {
											String wordInSentence = sentenceWords[wordIndex];
											//the word is the label of a node in the tree
											if (ctx.findNodeInTree(subTree, wordInSentence).size() > 0) {
												sentenceOrContext += wordInSentence + " ";
											}
										}
										sentenceOrContext += ctxTree.getValence() +" \n ";
									}

								}
								// sentiment analysis using sentence
								else {
									noSentences++;
									valence = sentence.getSentimentEntity().get(new data.sentiment.SentimentValence(
											Parsing.STANFORD_ID, "Stanford", "STANFORD", false));
									sumValences += valence;
									sentenceOrContext += sentence.getText();
								}
								break;
							}
						}
					}
					// if at least one sentence from utterance contains word
					// from voice, valence is considered the average
					if (noSentences > 0) {
						valence = sumValences / noSentences;
						valence = Math.round(valence);
						SentimentVoice sv = new SentimentVoice(w.getLemma(), valence,
								((Utterance) chat.getBlocks().get(blockIndex)).getParticipant(), sentenceOrContext);
						m.put(blockIndex, sv);
					}
				}
			}
			if (!chckbxSentiment.isSelected()) {
				for (Integer index : occurrences.keySet()) {
					Task subT = new Task(occurrences.get(index), new Date(index), new Date(index + 1));
					t.addSubtask(subT);
					newColors.add(participantColors.get(participantMapping.get(index)));
				}
			} else {
				for (Integer index : m.keySet()) {
					Task subT = new Task(m.get(index).toString(), new Date(index), new Date(index + 1));
					t.addSubtask(subT);
					newColors.add(sentimentColors.get(m.get(index).getValence()));
				}
			}
		}
		return s;
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
