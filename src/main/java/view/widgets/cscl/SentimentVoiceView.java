package view.widgets.cscl;

import java.awt.Color;
import java.awt.Paint;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;
import java.util.Vector;

import javax.swing.GroupLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.GroupLayout.Alignment;
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

import java.util.logging.Logger;

import data.Block;
import data.Lang;
import data.Sentence;
import data.Word;
import data.cscl.Conversation;
import data.cscl.Participant;
import data.cscl.Utterance;
import data.discourse.SemanticChain;
import data.discourse.SentimentVoice;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations;
import edu.stanford.nlp.util.CoreMap;
import services.discourse.cohesion.SentimentAnalysis;
import services.nlp.parsing.Parsing_EN;
import view.models.document.CustomToolTipGeneratorVoice;

/**
 * @author Florea Anda
 *
 */
public class SentimentVoiceView extends JFrame {
	static final Logger LOGGER = Logger.getLogger("");
	private static final Color[] predefinedColors = { Color.RED, Color.GREEN, Color.GRAY};
	private Map<Double, Color> sentimentColors;
	private Vector<Vector<Color>> taskColors;
	private JPanel contentPane;

	private Conversation chat;
	private List<SemanticChain> chains;

	
	public SentimentVoiceView(Conversation chat, List<SemanticChain> chains) {
		super();
		setTitle("ReaderBench - Sentiment Analysis");

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
		Map<Integer, String> occurrences = new TreeMap<Integer, String>();
		Map<Integer, Participant> participantMapping = new TreeMap<Integer, Participant>();
		Map<Integer, String> utteranceMapping = new TreeMap<Integer, String>();

		this.sentimentColors = new TreeMap<Double, Color>();
		sentimentColors.put(-1.0, Color.RED);
		sentimentColors.put(0.0, Color.GRAY);
		sentimentColors.put(1.0,  Color.GREEN);
		taskColors = new Vector<Vector<Color>>();

		
		for (SemanticChain chain : chains) {
			subTasks.put(chain, new LinkedList<Block>());
			Task t = new Task(chain.toString(), new Date(0), new Date(chat.getBlocks().size()));
			s.add(t);
			Vector<Color> newColors = new Vector<Color>();
			taskColors.add(newColors);

			// build the maps
			for (Word w : chain.getWords()) {
				// find the utterance for each word
				int blockIndex = w.getBlockIndex();
				// {no_utterance: keyword
				occurrences.put(blockIndex, w.getLemma());
				// {no_utterance: participant
				if (((Utterance) chat.getBlocks().get(blockIndex)).getParticipant() != null)
					participantMapping.put(blockIndex, ((Utterance) chat.getBlocks().get(blockIndex)).getParticipant());
				// generic participant
				else {
					participantMapping.put(blockIndex, new Participant("", chat));
				}
				// {no_utterance: sentence}
				for (Sentence sentence : chat.getBlocks().get(blockIndex).getSentences()) {
					// find which sentence contains the word from voice
					if (sentence.getWords().contains(w)) {
						sentence.getSentimentEntity();
						utteranceMapping.put(blockIndex, sentence.getText());
					}
				}
			}
			// sentiment analysis
//			Properties props = new Properties();
//			props.setProperty("annotators", "tokenize, ssplit, pos, lemma, ner, parse, dcoref, sentiment");

			//StanfordCoreNLP pipeline = new StanfordCoreNLP();
			Annotation annotation;

			// find the valence of the concept using stanford
			// build a sentimentVoice structure
			Map<Integer, SentimentVoice> m = new TreeMap<Integer, SentimentVoice>();
			double valence;
			for (Integer key : utteranceMapping.keySet()) {
				String text1 = utteranceMapping.get(key);
				String text = text1.replaceAll("\\s+", " ");
				annotation = new Annotation(text);
				//pipeline.annotate(annotation);
				Parsing_EN.getInstance().getPipeline().annotate(annotation);
				CoreMap result = annotation.get(SentencesAnnotation.class).get(0);
//				String sentiment = result.get(SentimentCoreAnnotations.SentimentClass.class);
//				// the association POSITIVE = 1, NEGATIVE = -1, NEUTRU
//				// = 0
//				if (sentiment.equals("Positive")) {
//					valence = 1;
//				} else if (sentiment.equals("Negative")) {
//					valence = -1;
//				} else {
//					valence = 0;
//				}
				Sentence res = Parsing_EN.getInstance()
						.processSentence(chat.getBlocks().get(key), key, result);
				valence = res.getSentimentEntity().get(new data.sentiment.SentimentValence(10000, "Stanford", "STANFORD", false));
				SentimentVoice sv = new SentimentVoice(occurrences.get(key), valence, participantMapping.get(key), "");
				m.put(key, sv);
			}
			for (Integer index : m.keySet()) {
				Task subT = new Task(m.get(index).toString(), new Date(index), new Date(index + 1));
				t.addSubtask(subT);
				newColors.add(sentimentColors.get(m.get(index).getValence()));
			}
		}

		TaskSeriesCollection collection = new TaskSeriesCollection();
		collection.add(s);
		
		//build chart
		JFreeChart chart = ChartFactory.createGanttChart(
				"Sentiment analysis of participants' voices and of implicit (alien) voices", // chart
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
		
		 //add the chart to a panel...
		ChartPanel chartPanel = new ChartPanel(chart);
		
		GroupLayout gl_contentPane = new GroupLayout(contentPane);
		gl_contentPane.setHorizontalGroup(gl_contentPane.createParallelGroup(Alignment.TRAILING).addGroup(gl_contentPane
				.createSequentialGroup().addContainerGap()
				.addGroup(gl_contentPane.createParallelGroup(Alignment.TRAILING)
						.addComponent(chartPanel, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 978, Short.MAX_VALUE))
				.addContainerGap()));
		gl_contentPane.setVerticalGroup(gl_contentPane.createParallelGroup(Alignment.TRAILING)
				.addGroup(gl_contentPane.createSequentialGroup().addContainerGap()
						.addComponent(chartPanel, GroupLayout.DEFAULT_SIZE, 527, Short.MAX_VALUE)
						.addPreferredGap(ComponentPlacement.RELATED)));
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
