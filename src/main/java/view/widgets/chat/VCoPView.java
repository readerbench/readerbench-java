package view.widgets.chat;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.EventQueue;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;

import services.discourse.topicMining.TopicCoverage.TopicClass;
import services.replicatedWorker.SerialCorpusAssessment;
import view.widgets.ReaderBenchView;
import DAO.chat.Community;
import edu.cmu.lti.jawjaw.pobj.Lang;

public class VCoPView extends JFrame {
	private static final long serialVersionUID = 8894652868238113117L;
	static Logger logger = Logger.getLogger(VCoPView.class);

	private JPanel contentPane;
	private JTextField textFieldPath;
	private JComboBox<String> comboBoxLanguage;
	private JComboBox<String> comboBoxLSA;
	private JComboBox<String> comboBoxLDA;
	private JCheckBox chckbxUsePosTagging;
	private JComboBox<String> comboBoxSpecificity;

	private static Lang lang = null;
	private static File lastDirectory = null;

	/**
	 * Create the frame.
	 */
	public VCoPView() {
		setTitle("ReaderBench - virtual Communities of Practice");
		setResizable(false);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setBounds(100, 100, 675, 375);
		contentPane = new JPanel();
		contentPane.setBackground(Color.WHITE);
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);

		JLabel lblPath = new JLabel("Path:");

		textFieldPath = new JTextField();
		textFieldPath.setText("in/forum_Nic");
		textFieldPath.setColumns(10);

		JButton btnSearch = new JButton("...");
		btnSearch.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFileChooser fc = null;
				if (lastDirectory == null)
					fc = new JFileChooser(new File("in"));
				else
					fc = new JFileChooser(lastDirectory);
				fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				int returnVal = fc.showOpenDialog(VCoPView.this);

				if (returnVal == JFileChooser.APPROVE_OPTION) {
					File file = fc.getSelectedFile();
					lastDirectory = file.getParentFile();
					textFieldPath.setText(file.getPath());
				}
			}
		});

		JPanel panelViewCommunity = new JPanel();
		panelViewCommunity.setBackground(Color.WHITE);
		panelViewCommunity.setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null), "View",
				TitledBorder.LEADING, TitledBorder.TOP, null, null));

		JPanel panelEvaluate = new JPanel();
		panelEvaluate.setBackground(Color.WHITE);
		panelEvaluate.setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null), "Evaluate",
				TitledBorder.LEADING, TitledBorder.TOP, null, null));

		GroupLayout gl_contentPane = new GroupLayout(contentPane);
		gl_contentPane
				.setHorizontalGroup(
						gl_contentPane.createParallelGroup(Alignment.LEADING)
								.addGroup(gl_contentPane.createSequentialGroup().addContainerGap()
										.addGroup(gl_contentPane.createParallelGroup(Alignment.LEADING)
												.addGroup(gl_contentPane.createSequentialGroup().addComponent(lblPath)
														.addGap(110)
														.addComponent(textFieldPath, GroupLayout.DEFAULT_SIZE, 448,
																Short.MAX_VALUE)
										.addPreferredGap(ComponentPlacement.RELATED)
										.addComponent(btnSearch, GroupLayout.PREFERRED_SIZE, 41,
												GroupLayout.PREFERRED_SIZE).addGap(6))
						.addGroup(Alignment.TRAILING,
								gl_contentPane.createSequentialGroup()
										.addGroup(gl_contentPane.createParallelGroup(Alignment.TRAILING)
												.addComponent(panelViewCommunity, Alignment.LEADING,
														GroupLayout.DEFAULT_SIZE, 636, Short.MAX_VALUE)
										.addComponent(panelEvaluate, GroupLayout.DEFAULT_SIZE, 636, Short.MAX_VALUE))
								.addContainerGap()))));
		gl_contentPane.setVerticalGroup(gl_contentPane.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_contentPane.createSequentialGroup()
						.addGroup(gl_contentPane.createParallelGroup(Alignment.BASELINE).addComponent(lblPath)
								.addComponent(btnSearch).addComponent(textFieldPath, GroupLayout.PREFERRED_SIZE,
										GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
				.addPreferredGap(ComponentPlacement.UNRELATED)
				.addComponent(panelEvaluate, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
						GroupLayout.PREFERRED_SIZE).addPreferredGap(ComponentPlacement.UNRELATED)
				.addComponent(panelViewCommunity, GroupLayout.PREFERRED_SIZE, 101, GroupLayout.PREFERRED_SIZE)
				.addContainerGap(132, Short.MAX_VALUE)));

		JLabel lblLanguage = new JLabel("Language:");

		comboBoxLanguage = new JComboBox<String>();
		comboBoxLanguage.addItem("<< Please select analysis language >>");
		for (String lang : Lang.SUPPORTED_LANGUAGES)
			comboBoxLanguage.addItem(lang);

		comboBoxLanguage.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (comboBoxLanguage.getSelectedIndex() > 0) {
					// set language
					lang = Lang.getLang((String) comboBoxLanguage.getSelectedItem());

					comboBoxLSA.removeAllItems();
					comboBoxLDA.removeAllItems();

					switch (lang) {
					case fr:
						for (String url : ReaderBenchView.TRAINED_LSA_SPACES_FR)
							comboBoxLSA.addItem(url);
						for (String url : ReaderBenchView.TRAINED_LDA_MODELS_FR)
							comboBoxLDA.addItem(url);
						break;
					case it:
						for (String url : ReaderBenchView.TRAINED_LSA_SPACES_IT)
							comboBoxLSA.addItem(url);
						for (String url : ReaderBenchView.TRAINED_LDA_MODELS_IT)
							comboBoxLDA.addItem(url);
						break;
					default:
						for (String url : ReaderBenchView.TRAINED_LSA_SPACES_EN)
							comboBoxLSA.addItem(url);
						for (String url : ReaderBenchView.TRAINED_LDA_MODELS_EN)
							comboBoxLDA.addItem(url);
						break;
					}
				} else {
					lang = null;
					comboBoxLSA.removeAllItems();
					comboBoxLSA.addItem("A Processing language needs to be previously selected");
					comboBoxLDA.removeAllItems();
					comboBoxLDA.addItem("A Processing language needs to be previously selected");
				}
			}
		});

		JLabel lblLsaVectorSpace = new JLabel("LSA vector space:");
		comboBoxLSA = new JComboBox<String>();
		comboBoxLSA.addItem("A Processing language needs to be previously selected");

		JLabel lblLdaModel = new JLabel("LDA model:");
		comboBoxLDA = new JComboBox<String>();
		comboBoxLDA.addItem("A Processing language needs to be previously selected");

		chckbxUsePosTagging = new JCheckBox("Use POS tagging");
		chckbxUsePosTagging.setSelected(true);

		JButton btnEvaluateCorpus = new JButton("Evaluate all corpus documents");
		btnEvaluateCorpus.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (!textFieldPath.getText().equals("")) {
					if (comboBoxLanguage.getSelectedIndex() > 0) {
						setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

						SerialCorpusAssessment.processCorpus(textFieldPath.getText(),
								(String) comboBoxLSA.getSelectedItem(), (String) comboBoxLDA.getSelectedItem(), lang,
								chckbxUsePosTagging.isSelected(), true, null, null, true);

						Toolkit.getDefaultToolkit().beep();
						logger.info("Finished processing all files");
						setCursor(null); // turn off the wait cursor
					} else {
						JOptionPane.showMessageDialog(VCoPView.this,
								"Please select an appropriate language for processing!", "Error",
								JOptionPane.WARNING_MESSAGE);
					}
				} else
					JOptionPane.showMessageDialog(VCoPView.this,
							"Please select an appropriate input folder to be evaluated!", "Error",
							JOptionPane.WARNING_MESSAGE);
			}
		});
		GroupLayout gl_panelEvaluate = new GroupLayout(panelEvaluate);
		gl_panelEvaluate
				.setHorizontalGroup(
						gl_panelEvaluate.createParallelGroup(Alignment.LEADING)
								.addGroup(
										gl_panelEvaluate.createSequentialGroup().addContainerGap()
												.addGroup(gl_panelEvaluate
														.createParallelGroup(
																Alignment.LEADING)
														.addGroup(
																gl_panelEvaluate.createSequentialGroup()
																		.addGroup(
																				gl_panelEvaluate
																						.createParallelGroup(
																								Alignment.LEADING)
																						.addComponent(lblLsaVectorSpace)
																						.addComponent(lblLanguage)
																						.addComponent(lblLdaModel))
																		.addPreferredGap(
																				ComponentPlacement.UNRELATED)
								.addGroup(gl_panelEvaluate.createParallelGroup(Alignment.LEADING)
										.addGroup(gl_panelEvaluate.createSequentialGroup()
												.addGroup(gl_panelEvaluate.createParallelGroup(Alignment.LEADING)
														.addComponent(comboBoxLanguage, 0, GroupLayout.DEFAULT_SIZE,
																Short.MAX_VALUE)
														.addComponent(comboBoxLSA, 0, 495, Short.MAX_VALUE))
												.addGap(7))
										.addGroup(gl_panelEvaluate.createSequentialGroup()
												.addComponent(comboBoxLDA, 0, 496, Short.MAX_VALUE).addContainerGap())))
						.addGroup(gl_panelEvaluate.createSequentialGroup().addComponent(chckbxUsePosTagging)
								.addPreferredGap(ComponentPlacement.RELATED, 247, Short.MAX_VALUE)
								.addComponent(btnEvaluateCorpus).addContainerGap()))));
		gl_panelEvaluate.setVerticalGroup(gl_panelEvaluate.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panelEvaluate.createSequentialGroup().addContainerGap()
						.addGroup(gl_panelEvaluate.createParallelGroup(Alignment.BASELINE).addComponent(lblLanguage)
								.addComponent(comboBoxLanguage, GroupLayout.PREFERRED_SIZE, 25,
										GroupLayout.PREFERRED_SIZE))
						.addPreferredGap(ComponentPlacement.UNRELATED)
						.addGroup(gl_panelEvaluate.createParallelGroup(Alignment.BASELINE)
								.addComponent(lblLsaVectorSpace).addComponent(comboBoxLSA, GroupLayout.PREFERRED_SIZE,
										GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
						.addPreferredGap(ComponentPlacement.UNRELATED)
						.addGroup(gl_panelEvaluate.createParallelGroup(Alignment.BASELINE).addComponent(lblLdaModel)
								.addComponent(comboBoxLDA, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
										GroupLayout.PREFERRED_SIZE))
						.addPreferredGap(ComponentPlacement.UNRELATED)
						.addGroup(gl_panelEvaluate.createParallelGroup(Alignment.BASELINE)
								.addComponent(chckbxUsePosTagging).addComponent(btnEvaluateCorpus))
						.addContainerGap(11, Short.MAX_VALUE)));
		panelEvaluate.setLayout(gl_panelEvaluate);

		JButton btnViewCommunity = new JButton("View community");

		JLabel lblSpecificity = new JLabel("Specificity:");

		comboBoxSpecificity = new JComboBox<String>();
		comboBoxSpecificity.addItem("None");

		for (TopicClass topicClass : TopicClass.values()) {
			comboBoxSpecificity.addItem(topicClass.toString());
		}

		GroupLayout gl_panelViewCommunity = new GroupLayout(panelViewCommunity);
		gl_panelViewCommunity.setHorizontalGroup(gl_panelViewCommunity.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panelViewCommunity.createSequentialGroup().addContainerGap()
						.addGroup(gl_panelViewCommunity.createParallelGroup(Alignment.LEADING)
								.addGroup(gl_panelViewCommunity.createSequentialGroup().addComponent(lblSpecificity)
										.addPreferredGap(ComponentPlacement.UNRELATED)
										.addComponent(comboBoxSpecificity, 0, 548, Short.MAX_VALUE))
						.addComponent(btnViewCommunity, Alignment.TRAILING)).addContainerGap()));
		gl_panelViewCommunity.setVerticalGroup(gl_panelViewCommunity.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panelViewCommunity.createSequentialGroup().addContainerGap()
						.addGroup(gl_panelViewCommunity.createParallelGroup(Alignment.BASELINE)
								.addComponent(lblSpecificity).addComponent(comboBoxSpecificity,
										GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
										GroupLayout.PREFERRED_SIZE))
						.addPreferredGap(ComponentPlacement.RELATED, 20, Short.MAX_VALUE).addComponent(btnViewCommunity)
						.addContainerGap()));
		panelViewCommunity.setLayout(gl_panelViewCommunity);
		btnViewCommunity.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (!textFieldPath.getText().equals("")) {
					setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
					if (comboBoxSpecificity.getSelectedIndex() == 0) {
						Community.processDocumentCollection(textFieldPath.getText(), null);
					} else {
						Community.processDocumentCollection(textFieldPath.getText(),
								TopicClass.valueOf((String) comboBoxSpecificity.getSelectedItem()));
					}
					Toolkit.getDefaultToolkit().beep();
					setCursor(null); // turn off the wait cursor
				} else
					JOptionPane.showMessageDialog(VCoPView.this,
							"Please select an appropriate input folder to be visualized!", "Error",
							JOptionPane.WARNING_MESSAGE);
			}
		});
		contentPane.setLayout(gl_contentPane);
	}

	public static void main(String[] args) {
		BasicConfigurator.configure();

		adjustToSystemGraphics();

		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				VCoPView view = new VCoPView();
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
