package view.widgets.chat;

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileFilter;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
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

import services.commons.Formatting;
import services.discourse.topicMining.TopicModeling;
import view.models.document.ChatTable;
import view.models.document.DocumentTableModel;
import view.models.document.TopicsTableModel;
import view.widgets.document.ConceptView;
import view.widgets.document.VoiceSelectionView;
import data.Block;
import data.cscl.Conversation;
import data.cscl.Participant;
import data.cscl.Utterance;
import data.discourse.Topic;
import edu.cmu.lti.jawjaw.pobj.Lang;

/**
 * 
 * @author Mihai Dascalu
 */
public class ChatView extends JFrame {
	static Logger logger = Logger.getLogger(ChatView.class);
	private static final long serialVersionUID = -4709511294166379162L;
	private static final int MAX_LENGTH_TITLE = 100;

	private Conversation chat;
	private JLabel lblChatTitle;
	private JTable tableTopics;
	private JSlider sliderTopics;
	private JCheckBox chckbxNounTopics;
	private JCheckBox chckbxVerbTopics;
	private DefaultTableModel modelTopics;
	private DefaultTableModel modelContent;
	private JTable tableContent;
	private JComboBox<String> comboBoxCategory;
	private List<Topic> topTopics;

	public ChatView(Conversation documentToDisplay) {
		super();
		setTitle("ReaderBench - Chat Visualization");
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		getContentPane().setBackground(Color.WHITE);
		this.chat = documentToDisplay;

		// adjust view to desktop size
		setBounds(50, 50, 1180, 700);

		generateLayout();
		updateContent();
		updateTopics(0);
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
								.addGroup(groupLayout.createParallelGroup(Alignment.TRAILING)
										.addComponent(panelHeader, GroupLayout.DEFAULT_SIZE, 1168, Short.MAX_VALUE)
										.addGroup(groupLayout.createSequentialGroup()
												.addComponent(panelContents, GroupLayout.DEFAULT_SIZE, 916,
														Short.MAX_VALUE)
												.addPreferredGap(ComponentPlacement.RELATED).addComponent(panelConcepts,
														GroupLayout.PREFERRED_SIZE, 246, GroupLayout.PREFERRED_SIZE)))
				.addContainerGap()));
		groupLayout.setVerticalGroup(groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup().addContainerGap()
						.addComponent(panelHeader, GroupLayout.PREFERRED_SIZE, 32, GroupLayout.PREFERRED_SIZE)
						.addPreferredGap(ComponentPlacement.RELATED)
						.addGroup(groupLayout.createParallelGroup(Alignment.TRAILING)
								.addComponent(panelConcepts, 0, 0, Short.MAX_VALUE)
								.addComponent(panelContents, GroupLayout.DEFAULT_SIZE, 633, Short.MAX_VALUE))
				.addGap(1)));

		JLabel lblContents = new JLabel("Contents");
		lblContents.setFont(new Font("SansSerif", Font.BOLD, 12));

		JSeparator separator = new JSeparator();

		JScrollPane scrollPaneConcept = new JScrollPane();
		scrollPaneConcept.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

		JButton btnParticipantInvolvement = new JButton("Participant involvement");
		btnParticipantInvolvement.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				EventQueue.invokeLater(new Runnable() {
					@Override
					public void run() {
						Iterator<Participant> it = chat.getParticipants().iterator();
						List<Participant> lsPart = new ArrayList<Participant>();
						while (it.hasNext()) {
							Participant part = it.next();
							lsPart.add(part);
						}

						ParticipantInvolvementView view = new ParticipantInvolvementView("Participant", "out/graph.pdf",
								lsPart, chat.getParticipantContributions(), true, false);
						view.setVisible(true);
					}
				});
			}
		});

		JButton btnCollaborationSocialKB = new JButton("Collaboration - Social KB");
		btnCollaborationSocialKB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				EventQueue.invokeLater(new Runnable() {
					@Override
					public void run() {
						JFrame view = new CollaborationKBView(chat);
						view.setVisible(true);
					}
				});
			}
		});

		JButton btnTimeEvolution = new JButton("Time evolution");
		btnTimeEvolution.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				EventQueue.invokeLater(new Runnable() {
					@Override
					public void run() {
						JFrame frame = new ChatTimeEvolution(chat);
						frame.setVisible(true);
					}
				});
			}
		});

		JButton btnSelectVoices = new JButton("Select voices");
		btnSelectVoices.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				EventQueue.invokeLater(new Runnable() {
					@Override
					public void run() {
						JFrame frame = new VoiceSelectionView(chat);
						frame.setVisible(true);
					}
				});
			}
		});

		JButton btnDisplayVoiceInteranimation = new JButton("Display voice inter-animation");
		btnDisplayVoiceInteranimation.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (chat.getSelectedVoices() != null && chat.getSelectedVoices().size() > 0) {
					EventQueue.invokeLater(new Runnable() {
						@Override
						public void run() {
							JFrame frame = new ChatVoiceInterAnimationView(chat, chat.getSelectedVoices());
							frame.setVisible(true);
						}
					});
				} else {
					JOptionPane.showMessageDialog(ChatView.this, "At least one voice must be selected!", "Information",
							JOptionPane.INFORMATION_MESSAGE);
				}
			}
		});

		JButton btnParticipantEvolution = new JButton("Participant evolution");
		btnParticipantEvolution.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				EventQueue.invokeLater(new Runnable() {
					@Override
					public void run() {
						JFrame frame = new ParticipantEvolutionView(chat);
						frame.setVisible(true);
					}
				});
			}
		});

		JButton btnCollaborationVoice = new JButton("Collaboration - Voice Overlapping");
		btnCollaborationVoice.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				EventQueue.invokeLater(new Runnable() {
					@Override
					public void run() {
						JFrame view = new CollaborationVoiceView(chat);
						view.setVisible(true);
					}
				});
			}
		});

		GroupLayout gl_panelContents = new GroupLayout(panelContents);
		gl_panelContents.setHorizontalGroup(gl_panelContents.createParallelGroup(Alignment.TRAILING)
				.addGroup(gl_panelContents.createSequentialGroup().addContainerGap()
						.addGroup(gl_panelContents.createParallelGroup(Alignment.TRAILING)
								.addComponent(scrollPaneConcept, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 970,
										Short.MAX_VALUE)
						.addComponent(separator, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 970, Short.MAX_VALUE)
						.addComponent(lblContents, Alignment.LEADING).addGroup(Alignment.LEADING,
								gl_panelContents.createSequentialGroup()
										.addGroup(gl_panelContents.createParallelGroup(Alignment.TRAILING, false)
												.addComponent(btnParticipantEvolution, GroupLayout.DEFAULT_SIZE,
														GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
										.addComponent(btnParticipantInvolvement, GroupLayout.DEFAULT_SIZE,
												GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
								.addPreferredGap(ComponentPlacement.RELATED).addComponent(btnTimeEvolution)
								.addPreferredGap(ComponentPlacement.RELATED)
								.addGroup(gl_panelContents.createParallelGroup(Alignment.LEADING, false)
										.addComponent(btnCollaborationVoice, Alignment.TRAILING,
												GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
										.addComponent(btnCollaborationSocialKB, Alignment.TRAILING,
												GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
								.addPreferredGap(ComponentPlacement.RELATED)
								.addGroup(gl_panelContents.createParallelGroup(Alignment.LEADING, false)
										.addComponent(btnSelectVoices, GroupLayout.DEFAULT_SIZE,
												GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
										.addComponent(btnDisplayVoiceInteranimation, GroupLayout.DEFAULT_SIZE,
												GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
						.addContainerGap()));
		gl_panelContents
				.setVerticalGroup(
						gl_panelContents.createParallelGroup(Alignment.LEADING)
								.addGroup(gl_panelContents.createSequentialGroup().addContainerGap()
										.addComponent(lblContents).addPreferredGap(ComponentPlacement.RELATED)
										.addComponent(separator, GroupLayout.PREFERRED_SIZE, 2,
												GroupLayout.PREFERRED_SIZE)
										.addPreferredGap(ComponentPlacement.RELATED)
										.addComponent(scrollPaneConcept, GroupLayout.DEFAULT_SIZE, 522, Short.MAX_VALUE)
										.addPreferredGap(ComponentPlacement.RELATED)
										.addGroup(gl_panelContents.createParallelGroup(Alignment.BASELINE)
												.addComponent(btnParticipantInvolvement).addComponent(btnTimeEvolution)
												.addComponent(btnCollaborationSocialKB).addComponent(btnSelectVoices))
						.addPreferredGap(ComponentPlacement.RELATED)
						.addGroup(gl_panelContents.createParallelGroup(Alignment.BASELINE)
								.addComponent(btnParticipantEvolution).addComponent(btnCollaborationVoice)
								.addComponent(btnDisplayVoiceInteranimation)).addContainerGap()));
		panelContents.setLayout(gl_panelContents);
		JLabel lblTitle = new JLabel("Discussion topic:");
		lblTitle.setFont(new Font("Lucida Grande", Font.BOLD, 13));

		lblChatTitle = new JLabel("");
		lblChatTitle.setFont(new Font("Lucida Grande", Font.BOLD, 13));
		if (this.chat.getTitleText() != null) {
			String title = chat.getTitleText();
			if (title.length() > MAX_LENGTH_TITLE) {
				title = title.substring(0, title.indexOf(" ", MAX_LENGTH_TITLE)) + "...";
			}
			lblChatTitle.setText(title);
		}

		GroupLayout gl_panelHeader = new GroupLayout(panelHeader);
		gl_panelHeader.setHorizontalGroup(gl_panelHeader.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panelHeader.createSequentialGroup().addContainerGap().addComponent(lblTitle)
						.addPreferredGap(ComponentPlacement.RELATED)
						.addComponent(lblChatTitle, GroupLayout.DEFAULT_SIZE, 1117, Short.MAX_VALUE)
						.addContainerGap()));
		gl_panelHeader.setVerticalGroup(gl_panelHeader.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panelHeader.createSequentialGroup()
						.addContainerGap().addGroup(gl_panelHeader.createParallelGroup(Alignment.BASELINE)
								.addComponent(lblTitle).addComponent(lblChatTitle))
				.addContainerGap(10, Short.MAX_VALUE)));
		panelHeader.setLayout(gl_panelHeader);

		JLabel lblTopics = new JLabel("Topics");
		lblTopics.setFont(new Font("SansSerif", Font.BOLD, 12));

		JSeparator separatorTopics = new JSeparator();

		JLabel lblFilterOnly = new JLabel("Filter only:");

		chckbxVerbTopics = new JCheckBox("Verbs");
		chckbxVerbTopics.setBackground(Color.WHITE);
		chckbxVerbTopics.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				updateTopics(comboBoxCategory.getSelectedIndex());
			}
		});
		chckbxVerbTopics.setSelected(true);

		chckbxNounTopics = new JCheckBox("Nouns");
		chckbxNounTopics.setBackground(Color.WHITE);
		chckbxNounTopics.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				updateTopics(comboBoxCategory.getSelectedIndex());
			}
		});
		chckbxNounTopics.setSelected(true);

		// determine appropriate scale
		int noWords = (int) (chat.getWordOccurences().keySet().size() * 0.2);
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
				updateTopics(comboBoxCategory.getSelectedIndex());
			}
		});

		JButton btnGenerateNetwork = new JButton("Generate network of concepts");
		btnGenerateNetwork.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				EventQueue.invokeLater(new Runnable() {
					@Override
					public void run() {
						ArrayList<Participant> ls = extractArrayListfromSet();
						int index = comboBoxCategory.getSelectedIndex();
						ConceptView view = null;
						if (index == 0) {
							view = new ConceptView(null, chat,
									TopicModeling.getSublist(chat.getTopics(), sliderTopics.getValue() * 5,
											chckbxNounTopics.isSelected(), chckbxVerbTopics.isSelected()));
						} else {
							view = new ConceptView(ls.get(index - 1), chat,
									TopicModeling.getSublist(chat.getTopics(), sliderTopics.getValue() * 5,
											chckbxNounTopics.isSelected(), chckbxVerbTopics.isSelected()));
						}
						view.setVisible(true);
					}
				});
			}
		});

		JScrollPane scrollPaneTopics = new JScrollPane();
		scrollPaneTopics.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

		JLabel lblCategory = new JLabel("Category:");

		comboBoxCategory = new JComboBox<String>();
		comboBoxCategory.addItem("Entire discussion");
		for (Participant p : chat.getParticipants())
			comboBoxCategory.addItem(p.getName());
		comboBoxCategory.addActionListener(new ActionListener() {
			@SuppressWarnings("unchecked")
			public void actionPerformed(ActionEvent e) {
				JComboBox<String> cb = (JComboBox<String>) e.getSource();
				int index = cb.getSelectedIndex();
				updateTopics(index);
			}
		});

		GroupLayout gl_panelConcepts = new GroupLayout(panelConcepts);
		gl_panelConcepts
				.setHorizontalGroup(gl_panelConcepts.createParallelGroup(Alignment.LEADING)
						.addGroup(gl_panelConcepts.createSequentialGroup().addContainerGap()
								.addGroup(gl_panelConcepts.createParallelGroup(Alignment.TRAILING).addComponent(
										scrollPaneTopics, GroupLayout.DEFAULT_SIZE, 234, Short.MAX_VALUE)
						.addComponent(separatorTopics, GroupLayout.DEFAULT_SIZE, 234, Short.MAX_VALUE)
						.addGroup(gl_panelConcepts.createSequentialGroup().addGroup(gl_panelConcepts
								.createParallelGroup(Alignment.LEADING)
								.addComponent(lblCategory, GroupLayout.DEFAULT_SIZE, 104, Short.MAX_VALUE)
								.addGroup(gl_panelConcepts.createSequentialGroup().addGap(6)
										.addGroup(gl_panelConcepts.createParallelGroup(Alignment.LEADING)
												.addComponent(chckbxVerbTopics, Alignment.TRAILING,
														GroupLayout.DEFAULT_SIZE, 98, Short.MAX_VALUE)
										.addComponent(chckbxNounTopics, GroupLayout.DEFAULT_SIZE, 98, Short.MAX_VALUE)
										.addComponent(lblFilterOnly))))
								.addPreferredGap(ComponentPlacement.RELATED)
								.addGroup(gl_panelConcepts.createParallelGroup(Alignment.TRAILING)
										.addComponent(sliderTopics, GroupLayout.DEFAULT_SIZE, 124, Short.MAX_VALUE)
										.addComponent(comboBoxCategory, GroupLayout.PREFERRED_SIZE, 158,
												GroupLayout.PREFERRED_SIZE)))
										.addComponent(lblTopics).addComponent(btnGenerateNetwork))
								.addContainerGap()));
		gl_panelConcepts
				.setVerticalGroup(
						gl_panelConcepts.createParallelGroup(Alignment.LEADING)
								.addGroup(
										gl_panelConcepts.createSequentialGroup().addContainerGap()
												.addComponent(lblTopics).addPreferredGap(ComponentPlacement.RELATED)
												.addComponent(separatorTopics, GroupLayout.PREFERRED_SIZE, 2,
														GroupLayout.PREFERRED_SIZE)
								.addGap(18)
								.addGroup(gl_panelConcepts.createParallelGroup(Alignment.BASELINE)
										.addComponent(lblCategory, GroupLayout.PREFERRED_SIZE, 17,
												GroupLayout.PREFERRED_SIZE)
										.addComponent(comboBoxCategory, GroupLayout.PREFERRED_SIZE,
												GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
						.addPreferredGap(ComponentPlacement.RELATED)
						.addGroup(gl_panelConcepts.createParallelGroup(Alignment.LEADING, false)
								.addGroup(gl_panelConcepts.createSequentialGroup().addComponent(lblFilterOnly)
										.addPreferredGap(ComponentPlacement.RELATED)
										.addComponent(chckbxNounTopics, GroupLayout.PREFERRED_SIZE, 15,
												GroupLayout.PREFERRED_SIZE)
										.addPreferredGap(ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE,
												Short.MAX_VALUE)
										.addComponent(chckbxVerbTopics, GroupLayout.PREFERRED_SIZE, 15,
												GroupLayout.PREFERRED_SIZE))
								.addComponent(sliderTopics, GroupLayout.PREFERRED_SIZE, 64, GroupLayout.PREFERRED_SIZE))
						.addPreferredGap(ComponentPlacement.UNRELATED)
						.addComponent(scrollPaneTopics, GroupLayout.DEFAULT_SIZE, 436, Short.MAX_VALUE)
						.addPreferredGap(ComponentPlacement.RELATED).addComponent(btnGenerateNetwork)
						.addContainerGap()));
		modelTopics = new TopicsTableModel();

		tableTopics = new JTable(modelTopics);
		scrollPaneTopics.setViewportView(tableTopics);
		tableTopics.setFillsViewportHeight(true);

		panelConcepts.setLayout(gl_panelConcepts);
		getContentPane().setLayout(groupLayout);

		modelContent = new DocumentTableModel();

		tableContent = new ChatTable(modelContent);

		tableContent.getColumnModel().getColumn(0).setMinWidth(50);
		tableContent.getColumnModel().getColumn(0).setMaxWidth(50);
		tableContent.getColumnModel().getColumn(0).setPreferredWidth(50);

		tableContent.setFillsViewportHeight(true);
		tableContent.setTableHeader(null);

		scrollPaneConcept.setViewportView(tableContent);
	}

	private void updateTopics(int index) {
		ArrayList<Participant> ls = extractArrayListfromSet();
		// clean table
		while (modelTopics.getRowCount() > 0) {
			modelTopics.removeRow(0);
		}

		// add new topics
		// discussion topics
		if (index == 0) {
			topTopics = TopicModeling.getSublist(chat.getTopics(), sliderTopics.getValue() * 5,
					chckbxNounTopics.isSelected(), chckbxVerbTopics.isSelected());
			for (Topic topic : topTopics) {
				Object[] row = { topic.getWord().getLemma(),
						Double.valueOf(new DecimalFormat("#.##").format(topic.getRelevance())) };
				modelTopics.addRow(row);
			}
		} else {
			topTopics = TopicModeling.getSublist(ls.get(index - 1).getInterventions().getTopics(),
					sliderTopics.getValue() * 5, chckbxNounTopics.isSelected(), chckbxVerbTopics.isSelected());
			for (Topic topic : topTopics) {
				Object[] row = { topic.getWord().getLemma(), (int) Math.round(topic.getRelevance()) };
				modelTopics.addRow(row);
			}
		}
	}

	/**
	 * @return
	 * 
	 */
	protected ArrayList<Participant> extractArrayListfromSet() {
		ArrayList<Participant> ls = new ArrayList<Participant>();
		for (Participant p : chat.getParticipants()) {
			ls.add(p);
		}
		return ls;
	}

	private void updateContent() {
		// clean table
		while (modelContent.getRowCount() > 0) {
			modelContent.removeRow(0);
		}

		double s0 = 0, s1 = 0, s2 = 0, mean = 0, stdev = 0;

		for (Block b : chat.getBlocks()) {
			if (b != null) {
				s0++;
				s1 += b.getOverallScore();
				s2 += Math.pow(b.getOverallScore(), 2);
			}
		}

		// determine mean + stdev values
		if (s0 != 0) {
			mean = s1 / s0;
			stdev = Math.sqrt(s0 * s2 - Math.pow(s1, 2)) / s0;
		}

		if (chat.getBlocks() != null && chat.getBlocks().size() > 0) {
			// add content
			for (int index = 0; index < chat.getBlocks().size(); index++) {
				if (chat.getBlocks().get(index) != null) {
					String text = "";
					if (((Utterance) chat.getBlocks().get(index)).getParticipant() != null) {
						text += "<b>" + ((Utterance) chat.getBlocks().get(index)).getParticipant().getName();
						if (((Utterance) chat.getBlocks().get(index)).getTime() != null) {
							SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yy HH:mm");
							text += " (" + formatter.format(((Utterance) chat.getBlocks().get(index)).getTime()) + ")";
						}
						text += ": </b>";
					}
					if (chat.getBlocks().get(index).getOverallScore() >= mean + stdev)
						text += "<b>" + chat.getBlocks().get(index).getText() + "</b>";
					else
						text += chat.getBlocks().get(index).getText();
					Object[] row = { index + "", text + " ["
							+ Formatting.formatNumber(chat.getBlocks().get(index).getOverallScore()) + "]" };
					modelContent.addRow(row);
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
				List<Conversation> chats = new LinkedList<Conversation>();

				Conversation c1 = Conversation.load("in/cscl/echipa4.xml", "resources/config/LSA/tasa_en", "resources/config/LDA/tasa_en", Lang.eng, true,
						true);
				c1.computeAll(null, null, true);
				chats.add(c1);

				Conversation c2 = Conversation.load("in/cscl/echipa34.xml", "resources/config/LSA/tasa_en", "resources/config/LDA/tasa_en", Lang.eng, true,
						true);
				c2.computeAll(null, null, true);
				chats.add(c2);

				Conversation c3 = Conversation.load("in/cscl/echipa36.xml", "resources/config/LSA/tasa_en", "resources/config/LDA/tasa_en", Lang.eng, true,
						true);
				c3.computeAll(null, null, true);
				// Chat c3 = (Chat) AbstractDocument
				// .loadSerializedDocument("in/cscl/echipa4.ser");
				// DialogismComputations.determineVoiceDistributions(c3);
				// DialogismComputations.implicitLinksCohesion(c3);
				// DialogismMeasures.getParticipantCollaboration(c3);
				chats.add(c3);

				// process all files
				File dir = new File("in/CoP-Annick-Philippe");

				File[] files = dir.listFiles(new FileFilter() {
					public boolean accept(File pathname) {
						if (pathname.getName().toLowerCase().endsWith(".xml"))
							return true;
						return false;
					}
				});

				for (File file : files) {
					try {
						logger.info("Processing " + file.getPath() + " file");
						Conversation c = Conversation.load(file.getPath(), "resources/config/LSA/lemonde_fr", "resources/config/LDA/lemonde_fr", Lang.fr,
								true, true);
						c.computeAll(null, null, true);
						chats.add(c);
					} catch (Exception e) {
						logger.error("Runtime error while processing " + file.getName() + ": " + e.getMessage());
						e.printStackTrace();
						throw e;
					}
				}

				ChatView view = new ChatView(chats.get(0));
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
