package view.widgets.document;

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;
import java.util.LinkedList;
import java.util.List;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSlider;
import javax.swing.JTable;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.ScrollPaneConstants;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.DefaultTableModel;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;

import DAO.Block;
import DAO.Sentence;
import DAO.discourse.Topic;
import DAO.document.Document;
import edu.cmu.lti.jawjaw.pobj.Lang;
import services.commons.Formatting;
import services.discourse.topicMining.TopicModeling;
import view.events.LinkMouseListener;
import view.models.document.DocumentTable;
import view.models.document.DocumentTableModel;
import view.models.document.TopicsTableModel;

/**
 * 
 * @author Mihai Dascalu
 */
public class DocumentView extends JFrame {
	static Logger logger = Logger.getLogger(DocumentView.class);

	private static final long serialVersionUID = -4709511294166379162L;
	private static final int MIN_ROW_HEIGHT = 20;
	private static final int MAX_ROW_HEIGHT = 60;

	private Document document;
	private JTable tableTopics;
	private JSlider sliderTopics;
	private JCheckBox chckbxNounTopics;
	private JCheckBox chckbxVerbTopics;
	private DefaultTableModel modelTopics;
	private DefaultTableModel modelContent;
	private JTable tableContent;
	private JLabel lblSourceDescription;
	private JLabel lblURIDescription;
	private JLabel lblTitleDescription;
	private JLabel lblSubjectivityDescription;

	public DocumentView(Document documentToDisplay) {
		super("ReaderBench - Document Visualization");
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		getContentPane().setBackground(Color.WHITE);
		this.document = documentToDisplay;

		// adjust view to desktop size
		setBounds(50, 50, 1180, 700);

		generateLayout();
		updateContent();
		updateTopics();
	}

	private void generateLayout() {
		JPanel panelConcepts = new JPanel();
		panelConcepts.setBackground(Color.WHITE);

		JPanel panelHeader = new JPanel();
		panelHeader.setBackground(Color.WHITE);

		JPanel panelContents = new JPanel();
		panelContents.setBackground(Color.WHITE);
		GroupLayout groupLayout = new GroupLayout(getContentPane());
		groupLayout
				.setHorizontalGroup(groupLayout.createParallelGroup(Alignment.LEADING)
						.addGroup(groupLayout.createSequentialGroup().addContainerGap()
								.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
										.addComponent(panelHeader, GroupLayout.DEFAULT_SIZE, 1168, Short.MAX_VALUE)
										.addGroup(Alignment.TRAILING, groupLayout.createSequentialGroup()
												.addComponent(panelContents, GroupLayout.DEFAULT_SIZE, 868,
														Short.MAX_VALUE)
												.addPreferredGap(ComponentPlacement.RELATED).addComponent(panelConcepts,
														GroupLayout.PREFERRED_SIZE, 294, GroupLayout.PREFERRED_SIZE)))
				.addContainerGap()));
		groupLayout
				.setVerticalGroup(
						groupLayout.createParallelGroup(Alignment.LEADING)
								.addGroup(groupLayout.createSequentialGroup().addContainerGap()
										.addComponent(panelHeader, GroupLayout.PREFERRED_SIZE, 53,
												GroupLayout.PREFERRED_SIZE)
										.addPreferredGap(ComponentPlacement.RELATED)
										.addGroup(groupLayout.createParallelGroup(Alignment.TRAILING)
												.addComponent(panelContents, GroupLayout.DEFAULT_SIZE, 607,
														Short.MAX_VALUE)
										.addComponent(panelConcepts, GroupLayout.DEFAULT_SIZE, 607, Short.MAX_VALUE))
				.addContainerGap()));

		JLabel lblContents = new JLabel("Contents");
		lblContents.setFont(new Font("SansSerif", Font.BOLD, 13));

		JSeparator separator = new JSeparator();

		JScrollPane scrollPaneContent = new JScrollPane();
		scrollPaneContent.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

		JButton btnAdvancedView = new JButton("Advanced View");
		btnAdvancedView.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				EventQueue.invokeLater(new Runnable() {
					@Override
					public void run() {
						SentenceView view = new SentenceView(document);
						view.setVisible(true);
					}
				});
			}
		});

		JButton btnVisualizeCohesionGraph = new JButton("Multi-Layered Cohesion Graph");
		btnVisualizeCohesionGraph.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				EventQueue.invokeLater(new Runnable() {
					@Override
					public void run() {
						CohesionGraphView view = new CohesionGraphView(document);
						view.setVisible(true);
					}
				});
			}
		});

		JButton btnSelectVoices = new JButton("Select Voices");
		btnSelectVoices.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				EventQueue.invokeLater(new Runnable() {
					@Override
					public void run() {
						JFrame frame = new VoiceSelectionView(document);
						frame.setVisible(true);
					}
				});
			}
		});

		JButton btnDisplayVoiceInteranimation = new JButton("Voice Inter-animation");
		btnDisplayVoiceInteranimation.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (document.getSelectedVoices() != null && document.getSelectedVoices().size() > 0) {
					EventQueue.invokeLater(new Runnable() {
						@Override
						public void run() {
							JFrame frame = new SentenceLevelInterAnimationView(document, document.getSelectedVoices());
							frame.setVisible(true);
						}
					});
				} else {
					JOptionPane.showMessageDialog(DocumentView.this, "At least one voice must be selected!",
							"Information", JOptionPane.INFORMATION_MESSAGE);
				}
			}
		});

		JButton btnVisualizeDocumentFlow = new JButton("Document Flow");
		btnVisualizeDocumentFlow.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				EventQueue.invokeLater(new Runnable() {
					@Override
					public void run() {
						JFrame view = new DocumentFlowView(document);
						view.setVisible(true);
					}
				});
			}
		});
		GroupLayout gl_panelContents = new GroupLayout(panelContents);
		gl_panelContents
				.setHorizontalGroup(gl_panelContents.createParallelGroup(Alignment.TRAILING)
						.addGroup(gl_panelContents.createSequentialGroup().addContainerGap()
								.addGroup(gl_panelContents
										.createParallelGroup(
												Alignment.LEADING)
										.addComponent(scrollPaneContent, GroupLayout.DEFAULT_SIZE, 856, Short.MAX_VALUE)
										.addComponent(separator, GroupLayout.DEFAULT_SIZE, 856,
												Short.MAX_VALUE)
						.addGroup(gl_panelContents.createSequentialGroup().addComponent(btnAdvancedView)
								.addPreferredGap(ComponentPlacement.RELATED).addComponent(btnVisualizeCohesionGraph)
								.addPreferredGap(ComponentPlacement.RELATED).addComponent(btnVisualizeDocumentFlow)
								.addPreferredGap(ComponentPlacement.RELATED).addComponent(btnSelectVoices)
								.addPreferredGap(ComponentPlacement.RELATED)
								.addComponent(btnDisplayVoiceInteranimation)).addComponent(lblContents))
						.addContainerGap()));
		gl_panelContents
				.setVerticalGroup(
						gl_panelContents.createParallelGroup(Alignment.LEADING)
								.addGroup(gl_panelContents.createSequentialGroup().addContainerGap()
										.addComponent(lblContents).addPreferredGap(ComponentPlacement.RELATED)
										.addComponent(separator, GroupLayout.PREFERRED_SIZE, 2,
												GroupLayout.PREFERRED_SIZE)
										.addPreferredGap(ComponentPlacement.RELATED)
										.addComponent(scrollPaneContent, GroupLayout.DEFAULT_SIZE, 530, Short.MAX_VALUE)
										.addPreferredGap(ComponentPlacement.RELATED)
										.addGroup(gl_panelContents.createParallelGroup(Alignment.BASELINE)
												.addComponent(btnAdvancedView).addComponent(btnVisualizeCohesionGraph)
												.addComponent(btnSelectVoices)
												.addComponent(btnDisplayVoiceInteranimation)
												.addComponent(btnVisualizeDocumentFlow))
										.addContainerGap()));
		panelContents.setLayout(gl_panelContents);
		JLabel lblTitle = new JLabel("Title:\n");
		lblTitle.setFont(new Font("Lucida Grande", Font.BOLD, 13));
		JLabel lblSource = new JLabel("Source:");
		JLabel lblURI = new JLabel("URI:");
		JLabel lblSubj = new JLabel("Sentiment polarity:");

		lblURIDescription = new JLabel("");
		lblSourceDescription = new JLabel("");
		lblTitleDescription = new JLabel("");
		lblTitleDescription.setFont(new Font("Lucida Grande", Font.BOLD, 13));
		lblSubjectivityDescription = new JLabel("");

		JSeparator separatorDocument = new JSeparator();

		if (document.getTitleText() != null) {
			lblTitleDescription.setText(document.getTitleText());
		}
		if (document.getSource() != null) {
			lblSourceDescription.setText(document.getSource());
		}
		if (document.getURI() != null) {
			lblURIDescription.setText(document.getURI());
			lblURIDescription.addMouseListener(new LinkMouseListener());
		}

		GroupLayout gl_panelHeader = new GroupLayout(panelHeader);
		gl_panelHeader
				.setHorizontalGroup(
						gl_panelHeader.createParallelGroup(Alignment.LEADING)
								.addGroup(
										gl_panelHeader.createSequentialGroup().addContainerGap()
												.addGroup(gl_panelHeader
														.createParallelGroup(
																Alignment.LEADING)
														.addComponent(separatorDocument, GroupLayout.DEFAULT_SIZE, 1156,
																Short.MAX_VALUE)
														.addGroup(gl_panelHeader.createSequentialGroup()
																.addComponent(lblSource)
																.addPreferredGap(ComponentPlacement.RELATED)
																.addComponent(lblSourceDescription).addGap(18)
																.addComponent(lblURI)
																.addPreferredGap(ComponentPlacement.RELATED)
																.addComponent(lblURIDescription)
																.addPreferredGap(ComponentPlacement.UNRELATED)
																.addComponent(lblSubj)
																.addPreferredGap(ComponentPlacement.RELATED)
																.addComponent(lblSubjectivityDescription))
								.addGroup(gl_panelHeader.createSequentialGroup().addComponent(lblTitle)
										.addPreferredGap(ComponentPlacement.RELATED).addComponent(lblTitleDescription,
												GroupLayout.DEFAULT_SIZE, 1113, Short.MAX_VALUE))).addContainerGap()));
		gl_panelHeader
				.setVerticalGroup(
						gl_panelHeader
								.createParallelGroup(
										Alignment.LEADING)
								.addGroup(
										gl_panelHeader.createSequentialGroup().addContainerGap()
												.addGroup(gl_panelHeader.createParallelGroup(Alignment.BASELINE)
														.addComponent(lblTitle).addComponent(
																lblTitleDescription))
												.addPreferredGap(ComponentPlacement.RELATED)
												.addGroup(gl_panelHeader.createParallelGroup(Alignment.BASELINE)
														.addComponent(lblSource).addComponent(lblSourceDescription)
														.addComponent(lblURIDescription).addComponent(lblURI)
														.addComponent(lblSubj).addComponent(lblSubjectivityDescription))
				.addPreferredGap(ComponentPlacement.RELATED)
				.addComponent(separatorDocument, GroupLayout.PREFERRED_SIZE, 2, GroupLayout.PREFERRED_SIZE)
				.addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));
		panelHeader.setLayout(gl_panelHeader);

		JLabel lblTopics = new JLabel("Topics");
		lblTopics.setFont(new Font("SansSerif", Font.BOLD, 12));

		JSeparator separatorTopics = new JSeparator();

		JLabel lblFilterOnly = new JLabel("Filter only:");

		chckbxVerbTopics = new JCheckBox("Verbs");
		chckbxVerbTopics.setBackground(Color.WHITE);
		chckbxVerbTopics.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				updateTopics();
			}
		});
		chckbxVerbTopics.setSelected(true);

		chckbxNounTopics = new JCheckBox("Nouns");
		chckbxNounTopics.setBackground(Color.WHITE);
		chckbxNounTopics.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				updateTopics();
			}
		});
		chckbxNounTopics.setSelected(true);

		// determine appropriate scale
		int noWords = (int) (document.getWordOccurences().keySet().size() * 0.2);
		int noMaxTopics = 50;
		if (noWords > 50)
			if (noWords <= 75)
				noMaxTopics = 75;
			else
				noMaxTopics = 100;
		sliderTopics = new JSlider(0, noMaxTopics / 5, 5);
		sliderTopics.setBackground(Color.WHITE);
		sliderTopics.setFont(new Font("SansSerif", Font.PLAIN, 10));
		sliderTopics.setMajorTickSpacing(5);
		sliderTopics.setPaintLabels(true);
		sliderTopics.setMinorTickSpacing(1);
		java.util.Hashtable<Integer, JLabel> labelTable = new java.util.Hashtable<Integer, JLabel>();
		if (noMaxTopics == 20)
			labelTable.put(new Integer(20), new JLabel("100"));
		if (noMaxTopics >= 15)
			labelTable.put(new Integer(15), new JLabel("75"));
		labelTable.put(new Integer(10), new JLabel("50"));
		labelTable.put(new Integer(5), new JLabel("25"));
		labelTable.put(new Integer(0), new JLabel("0"));
		sliderTopics.setLabelTable(labelTable);
		sliderTopics.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				updateTopics();
			}
		});

		JButton btnGenerateNetwork = new JButton("Generate network of concepts");
		btnGenerateNetwork.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				EventQueue.invokeLater(new Runnable() {
					@Override
					public void run() {
						ConceptView view = new ConceptView(null, document,
								TopicModeling.getSublist(document.getTopics(), sliderTopics.getValue() * 5,
										chckbxNounTopics.isSelected(), chckbxVerbTopics.isSelected()));
						view.setVisible(true);
					}
				});
			}
		});

		JScrollPane scrollPaneTopics = new JScrollPane();
		scrollPaneTopics.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

		GroupLayout gl_panelConcepts = new GroupLayout(panelConcepts);
		gl_panelConcepts.setHorizontalGroup(gl_panelConcepts.createParallelGroup(Alignment.LEADING)
				.addGroup(Alignment.TRAILING, gl_panelConcepts.createSequentialGroup().addContainerGap()
						.addGroup(gl_panelConcepts.createParallelGroup(Alignment.TRAILING)
								.addComponent(scrollPaneTopics, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 282,
										Short.MAX_VALUE)
								.addComponent(btnGenerateNetwork, Alignment.LEADING)
								.addComponent(lblTopics,
										Alignment.LEADING)
						.addGroup(Alignment.LEADING, gl_panelConcepts.createSequentialGroup().addGap(6)
								.addGroup(gl_panelConcepts.createParallelGroup(Alignment.LEADING)
										.addGroup(gl_panelConcepts.createSequentialGroup().addComponent(lblFilterOnly)
												.addPreferredGap(ComponentPlacement.RELATED)
												.addComponent(separatorTopics, GroupLayout.DEFAULT_SIZE, 203,
														Short.MAX_VALUE))
										.addGroup(gl_panelConcepts.createSequentialGroup()
												.addGroup(gl_panelConcepts.createParallelGroup(Alignment.LEADING)
														.addComponent(chckbxNounTopics, GroupLayout.PREFERRED_SIZE, 105,
																GroupLayout.PREFERRED_SIZE)
												.addComponent(chckbxVerbTopics))
												.addPreferredGap(ComponentPlacement.RELATED).addComponent(sliderTopics,
														GroupLayout.DEFAULT_SIZE, 165, Short.MAX_VALUE)))))
						.addContainerGap()));
		gl_panelConcepts.setVerticalGroup(gl_panelConcepts.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panelConcepts.createSequentialGroup().addContainerGap().addComponent(lblTopics)
						.addPreferredGap(ComponentPlacement.RELATED)
						.addGroup(gl_panelConcepts.createParallelGroup(Alignment.LEADING)
								.addComponent(separatorTopics, GroupLayout.PREFERRED_SIZE, 2,
										GroupLayout.PREFERRED_SIZE)
								.addComponent(lblFilterOnly))
						.addPreferredGap(ComponentPlacement.RELATED)
						.addGroup(gl_panelConcepts.createParallelGroup(Alignment.LEADING, false)
								.addGroup(gl_panelConcepts.createSequentialGroup().addComponent(chckbxNounTopics)
										.addPreferredGap(ComponentPlacement.RELATED).addComponent(chckbxVerbTopics))
								.addComponent(sliderTopics, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE,
										Short.MAX_VALUE))
						.addPreferredGap(ComponentPlacement.UNRELATED)
						.addComponent(scrollPaneTopics, GroupLayout.DEFAULT_SIZE, 453, Short.MAX_VALUE)
						.addPreferredGap(ComponentPlacement.RELATED).addComponent(btnGenerateNetwork)
						.addContainerGap()));
		modelTopics = new TopicsTableModel();

		tableTopics = new JTable(modelTopics);
		scrollPaneTopics.setViewportView(tableTopics);
		tableTopics.setFillsViewportHeight(true);

		panelConcepts.setLayout(gl_panelConcepts);
		getContentPane().setLayout(groupLayout);

		modelContent = new DocumentTableModel();

		tableContent = new DocumentTable(modelContent);

		tableContent.getColumnModel().getColumn(0).setMinWidth(50);
		tableContent.getColumnModel().getColumn(0).setMaxWidth(50);
		tableContent.getColumnModel().getColumn(0).setPreferredWidth(50);

		tableContent.setFillsViewportHeight(true);
		tableContent.setTableHeader(null);

		scrollPaneContent.setViewportView(tableContent);
	}

	private void updateTopics() {
		// clean table
		while (modelTopics.getRowCount() > 0) {
			modelTopics.removeRow(0);
		}

		// add new topics
		List<Topic> topTopics = TopicModeling.getSublist(document.getTopics(), sliderTopics.getValue() * 5,
				chckbxNounTopics.isSelected(), chckbxVerbTopics.isSelected());
		for (Topic topic : topTopics) {
			Object[] row = { topic.getWord().getLemma(),
					Double.valueOf(new DecimalFormat("#.##").format(topic.getRelevance())) };
			modelTopics.addRow(row);
		}
	}

	private void updateContent() {
		// clean table
		while (modelContent.getRowCount() > 0) {
			modelContent.removeRow(0);
		}

		double s0 = 0, s1 = 0, s2 = 0, mean = 0, stdev = 0;

		for (Block b : document.getBlocks()) {
			if (b != null) {
				for (Sentence s : b.getSentences()) {
					if (s != null) {
						s0++;
						s1 += s.getOverallScore();
						s2 += Math.pow(s.getOverallScore(), 2);
					}
				}
			}
		}

		// determine mean + stdev values
		if (s0 != 0) {
			mean = s1 / s0;
			stdev = Math.sqrt(s0 * s2 - Math.pow(s1, 2)) / s0;
		}

		if (document.getBlocks() != null && document.getBlocks().size() > 0) {
			// add content
			int index = 0;
			double maxCohesion = Double.MIN_VALUE;
			double minCohesion = Double.MAX_VALUE;

			for (; index < document.getBlocks().size() - 1; index++) {
				if (document.getBlocks().get(index) != null) {
					String text = "";
					for (Sentence s : document.getBlocks().get(index).getSentences()) {
						if (s != null) {
							if (s.getOverallScore() >= mean + stdev) {
								text += "<b>" + s.getText() + "</b> ";
							} else {
								text += s.getText() + " ";
							}
						}
					}
					Object[] row1 = { index, text + "["
							+ Formatting.formatNumber(document.getBlocks().get(index).getOverallScore()) + "]" };
					modelContent.addRow(row1);
					Object[] row2 = { "", document.getBlockDistances()[index][index + 1].toString() };
					modelContent.addRow(row2);
					double dist = document.getBlockDistances()[index][index + 1].getCohesion();
					maxCohesion = Math.max(maxCohesion, dist);
					minCohesion = Math.min(minCohesion, dist);
				}
			}
			if (document.getBlocks().get(index) != null) {
				Object[] lastRow = { index, document.getBlocks().get(index).getText() + " ["
						+ Formatting.formatNumber(document.getBlocks().get(index).getOverallScore()) + "]" };
				modelContent.addRow(lastRow);
			}

			if (document.getBlocks().size() > 1) {
				for (index = 0; index < document.getBlocks().size() - 1; index++) {
					double dist = 1 / document.getBlockDistances()[index][index + 1].getCohesion();
					tableContent.setRowHeight(2 * index + 1, MIN_ROW_HEIGHT + ((int) ((dist - 1 / maxCohesion)
							/ (1 / minCohesion - 1 / maxCohesion) * (MAX_ROW_HEIGHT - MIN_ROW_HEIGHT))));
				}
			}
		}
	}

	public static void main(String[] args) {
		BasicConfigurator.configure();

		adjustToSystemGraphics();

		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				List<Document> docs = new LinkedList<Document>();

				// int[] factors = new int[] {
				// // readability
				// ComplexityFactors.READABILITY_FLESCH,
				// ComplexityFactors.READABILITY_FOG,
				// ComplexityFactors.READABILITY_KINCAID,
				// // surface factors
				// ComplexityFactors.NORMALIZED_NO_COMMAS,
				// ComplexityFactors.NORMALIZED_NO_BLOCKS,
				// ComplexityFactors.AVERAGE_BLOCK_SIZE,
				// ComplexityFactors.NORMALIZED_NO_SENTENCES,
				// ComplexityFactors.AVERAGE_SENTENCE_LENGTH,
				// ComplexityFactors.NORMALIZED_NO_WORDS,
				// ComplexityFactors.AVERAGE_WORD_LENGTH,
				// ComplexityFactors.NO_WORDS_PER_SENTENCE,
				// ComplexityFactors.NO_SYLLABLES_PER_WORD,
				// ComplexityFactors.PERCENT_COMPLEX_WORDS,
				// // Entropy
				// ComplexityFactors.WORD_ENTROPY,
				// ComplexityFactors.CHAR_ENTROPY,
				// // CAF
				// ComplexityFactors.LEXICAL_DIVERSITY,
				// ComplexityFactors.LEXICAL_SOPHISTICATION,
				// ComplexityFactors.SYNTACTIC_DIVERSITY,
				// ComplexityFactors.SYNTACTIC_SOPHISTICATION,
				// ComplexityFactors.BALANCED_CAF,
				// // Morphology
				// ComplexityFactors.AVERAGE_NO_NOUNS,
				// ComplexityFactors.AVERAGE_NO_PRONOUNS,
				// ComplexityFactors.AVERAGE_NO_VERBS,
				// ComplexityFactors.AVERAGE_NO_ADVERBS,
				// ComplexityFactors.AVERAGE_NO_ADJECTIVES,
				// ComplexityFactors.AVERAGE_NO_PREPOSITIONS,
				// ComplexityFactors.AVERAGE_TREE_DEPTH,
				// ComplexityFactors.AVERAGE_TREE_SIZE,
				// // Entity Density
				// ComplexityFactors.TOTAL_NO_NAMED_ENT,
				// ComplexityFactors.TOTAL_NO_ENT_PER_DOC,
				// ComplexityFactors.TOTAL_NO_UNIQUE_ENT_PER_DOC,
				// ComplexityFactors.PERCENTAGE_ENT_PER_DOC,
				// ComplexityFactors.PERCENTAGE_UNIQUE_ENT_PER_DOC,
				// ComplexityFactors.AVERAGE_NO_ENT_PER_SENT,
				// ComplexityFactors.AVERAGE_NO_UNIQUE_ENT_PER_SENTENCE,
				// ComplexityFactors.PERCENTAGE_NAMED_ENT_PER_DOC,
				// ComplexityFactors.AVERAGE_NO_NAMED_ENT_PER_SENTENCE,
				// ComplexityFactors.PERCENTAGE_NAMED_ENT_IN_TOTAL_ENT,
				// ComplexityFactors.PERCENTAGE_NOUNS_IN_TOTAL_ENT,
				// ComplexityFactors.PERCENTAGE_NOUNS_PER_DOC,
				// ComplexityFactors.AVERAGE_NO_NOUNS_PER_SENTENCE,
				// ComplexityFactors.PERCENTAGE_REMAINING_NOUNS_PER_DOC,
				// ComplexityFactors.AVERAGE_NO_REMAINING_NOUNS_PER_SENTENCE,
				// ComplexityFactors.PERCENTAGE_OVERLAPPING_NOUNS_PER_DOC,
				// ComplexityFactors.AVERAGE_NO_OVERLAPPING_NOUNS_PER_SENTENCE,
				// // Coreference inference
				// ComplexityFactors.TOTAL_NO_COREF_CHAINS_PER_DOC,
				// ComplexityFactors.AVERAGE_NO_COREFS_PER_CHAIN,
				// ComplexityFactors.AVERAGE_CHAIN_SPAN,
				// ComplexityFactors.NO_COREF_CHAINS_WITH_BIG_SPAN,
				// ComplexityFactors.AVERAGE_INFERENCE_DISTANCE_PER_CHAIN,
				// ComplexityFactors.NO_ACTIVE_COREF_CHAINS_PER_WORD,
				// ComplexityFactors.NO_ACTIVE_COREF_CHAINS_PER_ENT,
				// // Word complexity
				// ComplexityFactors.WORD_DIFF_LEMMA_STEM_MEAN,
				// ComplexityFactors.WORD_DIFF_WORD_STEM,
				// ComplexityFactors.WORD_DISTANCE_HYPERNYM_TREE,
				// ComplexityFactors.WORD_POLYSEMY_COUNT_MEAN,
				// ComplexityFactors.WORD_SYLLABLE_COUNT_MEAN,
				// // Lexical chains
				// ComplexityFactors.LEXICAL_CHAINS_AVERAGE_SPAN,
				// ComplexityFactors.LEXICAL_CHAINS_MAX_SPAN,
				// ComplexityFactors.LEXICAL_CHAINS_NO_IMPORTANT,
				// ComplexityFactors.LEXICAL_CHAINS_COVERAGE,
				// // Discourse
				// ComplexityFactors.AVERAGE_SCORE,
				// ComplexityFactors.OVERALL_SCORE,
				// ComplexityFactors.AVERAGE_BLOCK_DOC_COHESION,
				// ComplexityFactors.AVERAGE_SENTENCE_BLOCK_COHESION,
				// ComplexityFactors.AVERAGE_INTER_BLOCK_COHESION,
				// ComplexityFactors.AVERAGE_INTRA_BLOCK_COHESION };
				//
				Document d1 = Document.load("in/NLP2012/reading_material_en.xml", "config/LSA/tasa_en",
						"config/LDA/tasa_en", Lang.eng, true, true);
				d1.computeAll(null, null, true);
				// Document d1 = (Document) AbstractDocument
				// .loadSerializedDocument("in/NLP2012/reading_material_en.ser");
				docs.add(d1);

				// Document d1 = (Document) AbstractDocument
				// .loadSerializedDocument("in/Elephant Miguel/Miguel.ser");
				// System.out.println(d1.getOverallScore());
				// docs.add(d1);
				//
				// Document d2 = (Document) AbstractDocument
				// .loadSerializedDocument("in/Elephant Miguel/Elephant.ser");
				// System.out.println(d2.getOverallScore());
				// docs.add(d2);

				// Document d2 = Document
				// .load("in/Matilda/Verbalization extracts AIED/MATILDA grain
				// moyen.xml",
				// "config/LSA/lemonde_fr",
				// "config/LDA/lemonde_fr", Lang.fr, true, true);
				// d2.computeAll(null, null, true);
				// AbstractDocument d2 = AbstractDocument
				// .loadSerializedDocument("in/Matilda/Verbalization extracts
				// AIED/MATILDA grain moyen.ser");
				// docs.add((Document) d2);

				// Document d4 = Document.loadGenericDocument(
				// "in/Avaleur_de_Nuages/L'avaleur de nuages.xml",
				// "config/LSA/lemonde_fr", "config/LDA/lemonde_fr",
				// Lang.fr, true, true);
				// d4.computeAll(null, null, true);
				// docs.add(d4);

				// String lsaSpace = "config/LSA/lemonde_fr";
				// String ldaSpace = "config/LDA/lemonde_fr";
				// String lsaSpace = "config/LSA/textenfants_fr";
				// String ldaSpace = "config/LDA/textenfants_fr";

				// Document d11 = Document.loadGenericDocument(
				// "in/textes jugement thematique/avaleurnuages.xml",
				// lsaSpace, ldaSpace, Lang.fr, true, true);
				// d11.computeAll(null, null, true);
				// System.out.println(d11);
				// docs.add(d11);
				//
				// Document d12 = Document.loadGenericDocument(
				// "in/textes jugement thematique/bibamboulor.xml",
				// lsaSpace, ldaSpace, Lang.fr, true, true);
				// d12.computeAll(null, null, true);
				// System.out.println(d12);
				// docs.add(d12);
				//
				// Document d13 = Document.loadGenericDocument(
				// "in/textes jugement thematique/boudha.xml", lsaSpace,
				// ldaSpace, Lang.fr, true, true);
				// d13.computeAll(null, null, true);
				// System.out.println(d13);
				// docs.add(d13);
				//
				// Document d14 = Document.loadGenericDocument(
				// "in/textes jugement thematique/Destins_croises.xml",
				// lsaSpace, ldaSpace, Lang.fr, true, true);
				// d14.computeAll(null, null, true);
				// System.out.println(d14);
				// docs.add(d14);
				//
				// Document d15 = Document.loadGenericDocument(
				// "in/textes jugement thematique/le_roi_crapaud.xml",
				// lsaSpace, ldaSpace, Lang.fr, true, true);
				// d15.computeAll(null, null, true);
				// System.out.println(d15);
				// docs.add(d15);
				//
				// Document d16 = Document.loadGenericDocument(
				// "in/textes jugement thematique/maltilda.xml", lsaSpace,
				// ldaSpace, Lang.fr, true, true);
				// d16.computeAll(null, null, true);
				// System.out.println(d16);
				// docs.add(d16);
				//
				// Document d17 = Document.loadGenericDocument(
				// "in/textes jugement thematique/mondedenhaut.xml",
				// lsaSpace, ldaSpace, Lang.fr, true, true);
				// d17.computeAll(null, null, true);
				// System.out.println(d17);
				// docs.add(d17);
				//
				// Document d18 = Document.loadGenericDocument(
				// "in/textes jugement thematique/petit_garcon.xml",
				// lsaSpace, ldaSpace, Lang.fr, true, true);
				// d18.computeAll(null, null, true);
				// System.out.println(d18);
				// docs.add(d18);
				//
				// Document d19 = Document.loadGenericDocument(
				// "in/textes jugement thematique/sept_corbeaux.xml",
				// lsaSpace, ldaSpace, Lang.fr, true, true);
				// d19.computeAll(null, null, true);
				// System.out.println(d19);
				// docs.add(d19);

				// Document d20 = Document.loadGenericDocument(
				// "in/textes jugement thematique/Tom_et_Lea.xml",
				// lsaSpace, ldaSpace, Lang.fr, true, true);
				// d20.computeAll(null, null, true);
				// System.out.println(d20);
				// docs.add(d20);

				// Document d21 = Document.loadGenericDocument(
				// "in/textes jugement thematique/lorange.xml", lsaSpace,
				// ldaSpace, Lang.fr, true, true);
				// d21.computeAll(null, null, true);
				// System.out.println(d21);
				// docs.add(d21);
				//
				// Document d31 = Document.loadGenericDocument(
				// "in/Textes DEPP/arbreventoiseau.xml", lsaSpace,
				// ldaSpace, Lang.fr, true, true);
				// d31.computeAll(null, null, true);
				// System.out.println(d31);
				// docs.add(d31);
				//
				// Document d32 = Document.loadGenericDocument(
				// "in/Textes DEPP/archimeme.xml", lsaSpace, ldaSpace,
				// Lang.fr, true, true);
				// d32.computeAll(null, null, true);
				// System.out.println(d32);
				// docs.add(d32);
				//
				// Document d33 = Document.loadGenericDocument(
				// "in/Textes DEPP/bmcDonald.xml", lsaSpace, ldaSpace,
				// Lang.fr, true, true);
				// d33.computeAll(null, null, true);
				// System.out.println(d33);
				// docs.add(d33);
				//
				// Document d34 = Document.loadGenericDocument(
				// "in/Textes DEPP/lapinafricain.xml", lsaSpace, ldaSpace,
				// Lang.fr, true, true);
				// d34.computeAll(null, null, true);
				// System.out.println(d34);
				// docs.add(d34);
				//
				// Document d35 = Document.loadGenericDocument(
				// "in/Textes DEPP/leséléphants.xml", lsaSpace, ldaSpace,
				// Lang.fr, true, true);
				// d35.computeAll(null, null, true);
				// System.out.println(d35);
				// docs.add(d35);
				//
				// Document d36 = Document.loadGenericDocument(
				// "in/Textes DEPP/uneviedarbre.xml", lsaSpace, ldaSpace,
				// Lang.fr, true, true);
				// d36.computeAll(null, null, true);
				// System.out.println(d36);
				// docs.add(d36);

				DocumentView view = new DocumentView(docs.get(0));
				view.setVisible(true);
			}
		});
	}

	private static void adjustToSystemGraphics() {
		for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
			if ("Nimbus".equals(info.getName())) {
				try {
					UIManager.setLookAndFeel(info.getClassName());
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				} catch (InstantiationException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				} catch (UnsupportedLookAndFeelException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
