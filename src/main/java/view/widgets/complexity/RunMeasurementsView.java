package view.widgets.complexity;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.EventQueue;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileFilter;

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
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.EmptyBorder;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;

import services.complexity.DataGathering;
import services.semanticModels.LDA.LDA;
import services.semanticModels.LSA.LSA;
import utils.localization.LocalizationUtils;
import view.widgets.ReaderBenchView;
import edu.cmu.lti.jawjaw.pobj.Lang;
import edu.stanford.nlp.util.Timing;

public class RunMeasurementsView extends JFrame {
	private static final long serialVersionUID = 8894652868238113117L;

	public static final String C_BASE_FOLDER_NAME = "grade";

	static Logger logger = Logger.getLogger(RunMeasurementsView.class);

	private JPanel contentPane;
	private JTextField textFieldPath;
	private JComboBox<String> comboBoxLSA;
	private JComboBox<String> comboBoxLDA;
	private JComboBox<String> comboBoxLanguage;
	private JButton btnRun;
	private JCheckBox chckbxUsePosTagging;
	private Lang lang = null;

	private class Task extends SwingWorker<Void, Void> {
		private String path;
		private String pathToLSA;
		private String pathToLDA;
		private Lang lang;
		private boolean usePOSTagging;

		public Task(String path, String pathToLSA, String pathToLDA, Lang lang,
				boolean usePOSTagging) {
			super();
			this.path = path;
			this.pathToLSA = pathToLSA;
			this.pathToLDA = pathToLDA;
			this.lang = lang;
			this.usePOSTagging = usePOSTagging;
		}

		public Void doInBackground() {
			try {
				logger.info("Processing all documents found in " + path);
				try {
					LSA lsa = LSA.loadLSA(pathToLSA, lang);
					LDA lda = LDA.loadLDA(pathToLDA, lang);

					// determine number of classes
					int noGrades = (new File(path)).listFiles(new FileFilter() {
						public boolean accept(File pathname) {
							if (pathname.isDirectory())
								return true;
							return false;
						}
					}).length;

					logger.info("Found " + noGrades + " document grade levels");
					Timing totalTiming = new Timing();
					totalTiming.start();

					DataGathering.writeHeader(path);

					for (int gradeLevel = 1; gradeLevel <= noGrades; gradeLevel++) {
						DataGathering.processTexts(path + "/" + C_BASE_FOLDER_NAME
								+ gradeLevel, gradeLevel, false, lsa, lda,
								lang, usePOSTagging);
					}

					logger.info("Finished processing all documents");
					logger.info("Time elasped:" + totalTiming.report() / 1000
							+ "s (" + totalTiming.report() / 1000 / 60 + "min)");
				} catch (Exception e) {
					System.err.println("Error: " + e.getMessage());
					e.printStackTrace();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			return null;
		}

		@Override
		public void done() {
			Toolkit.getDefaultToolkit().beep();
			btnRun.setEnabled(true);
			setCursor(null); // turn off the wait cursor
		}
	}

	/**
	 * Create the frame.
	 */
	public RunMeasurementsView() {
		setTitle("ReaderBench - " + LocalizationUtils.getTranslation("Corpus Assessment in terms of Textual Complexity"));
		setResizable(false);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setBounds(100, 100, 560, 220);
		contentPane = new JPanel();
		contentPane.setBackground(Color.WHITE);
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);

		JLabel lblPath = new JLabel("Path:");

		JLabel lblLsaVectorSpace = new JLabel(LocalizationUtils.getTranslation("LSA vector space") + ":");

		JLabel lblLdaModel = new JLabel(LocalizationUtils.getTranslation("LDA model") + ":");

		comboBoxLSA = new JComboBox<String>();
		comboBoxLSA.setEnabled(false);
		comboBoxLDA = new JComboBox<String>();
		comboBoxLDA.setEnabled(false);

		textFieldPath = new JTextField();
		textFieldPath.setText("");
		textFieldPath.setColumns(10);

		JButton btnSearch = new JButton("...");
		btnSearch.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFileChooser fc = new JFileChooser(new File("in"));
				fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				int returnVal = fc.showOpenDialog(RunMeasurementsView.this);

				if (returnVal == JFileChooser.APPROVE_OPTION) {
					File file = fc.getSelectedFile();
					textFieldPath.setText(file.getPath());
				}
			}
		});

		btnRun = new JButton("Run");
		btnRun.setEnabled(false);
		btnRun.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (!textFieldPath.getText().equals("")) {
					Task task = new Task(textFieldPath.getText(),
							(String) comboBoxLSA.getSelectedItem(),
							(String) comboBoxLDA.getSelectedItem(),
							RunMeasurementsView.this.lang, chckbxUsePosTagging
									.isSelected());
					btnRun.setEnabled(false);
					setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
					task.execute();
				} else
					JOptionPane
							.showMessageDialog(
									RunMeasurementsView.this,
									LocalizationUtils.getTranslation("Please select an appropriate directory to be analysed") + "!",
									"Error", JOptionPane.WARNING_MESSAGE);
			}
		});

		chckbxUsePosTagging = new JCheckBox(LocalizationUtils.getTranslation("Use POS tagging"));
		chckbxUsePosTagging.setSelected(true);

		JLabel lblLanguage = new JLabel(LocalizationUtils.getTranslation("Language") + ":");

		comboBoxLanguage = new JComboBox<String>();
		comboBoxLanguage.addItem("<< " + LocalizationUtils.getTranslation("Please select analysis language") + " >>");
		for (String lang : Lang.SUPPORTED_LANGUAGES)
			comboBoxLanguage.addItem(lang);

		comboBoxLanguage.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (comboBoxLanguage.getSelectedIndex() > 0) {
					// set final analysis language
					lang = Lang.getLang((String) comboBoxLanguage
							.getSelectedItem());

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
					case es:
						for (String url : ReaderBenchView.TRAINED_LSA_SPACES_ES)
							comboBoxLSA.addItem(url);
						for (String url : ReaderBenchView.TRAINED_LDA_MODELS_ES)
							comboBoxLDA.addItem(url);
						break;
					default:
						for (String url : ReaderBenchView.TRAINED_LSA_SPACES_EN)
							comboBoxLSA.addItem(url);
						for (String url : ReaderBenchView.TRAINED_LDA_MODELS_EN)
							comboBoxLDA.addItem(url);
						break;
					}

					comboBoxLSA.setEnabled(true);
					comboBoxLDA.setEnabled(true);
					btnRun.setEnabled(true);
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
										.addContainerGap()
										.addGroup(
												gl_contentPane
														.createParallelGroup(
																Alignment.TRAILING)
														.addGroup(
																gl_contentPane
																		.createSequentialGroup()
																		.addGroup(
																				gl_contentPane
																						.createParallelGroup(
																								Alignment.LEADING)
																						.addComponent(
																								lblPath)
																						.addComponent(
																								lblLsaVectorSpace)
																						.addComponent(
																								lblLdaModel))
																		.addGap(13)
																		.addGroup(
																				gl_contentPane
																						.createParallelGroup(
																								Alignment.TRAILING)
																						.addComponent(
																								comboBoxLDA,
																								0,
																								404,
																								Short.MAX_VALUE)
																						.addGroup(
																								gl_contentPane
																										.createSequentialGroup()
																										.addComponent(
																												textFieldPath,
																												GroupLayout.DEFAULT_SIZE,
																												357,
																												Short.MAX_VALUE)
																										.addPreferredGap(
																												ComponentPlacement.RELATED)
																										.addComponent(
																												btnSearch,
																												GroupLayout.PREFERRED_SIZE,
																												41,
																												GroupLayout.PREFERRED_SIZE))
																						.addComponent(
																								comboBoxLSA,
																								0,
																								404,
																								Short.MAX_VALUE)
																						.addComponent(
																								comboBoxLanguage,
																								Alignment.LEADING,
																								0,
																								404,
																								Short.MAX_VALUE))
																		.addGap(6))
														.addGroup(
																Alignment.LEADING,
																gl_contentPane
																		.createSequentialGroup()
																		.addComponent(
																				chckbxUsePosTagging)
																		.addPreferredGap(
																				ComponentPlacement.RELATED,
																				319,
																				Short.MAX_VALUE)
																		.addComponent(
																				btnRun,
																				GroupLayout.PREFERRED_SIZE,
																				73,
																				GroupLayout.PREFERRED_SIZE)
																		.addContainerGap())
														.addGroup(
																gl_contentPane
																		.createSequentialGroup()
																		.addComponent(
																				lblLanguage)
																		.addContainerGap(
																				469,
																				Short.MAX_VALUE)))));
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
																lblLanguage)
														.addComponent(
																comboBoxLanguage,
																GroupLayout.PREFERRED_SIZE,
																GroupLayout.DEFAULT_SIZE,
																GroupLayout.PREFERRED_SIZE))
										.addPreferredGap(
												ComponentPlacement.RELATED)
										.addGroup(
												gl_contentPane
														.createParallelGroup(
																Alignment.BASELINE)
														.addComponent(lblPath)
														.addComponent(btnSearch)
														.addComponent(
																textFieldPath,
																GroupLayout.PREFERRED_SIZE,
																GroupLayout.DEFAULT_SIZE,
																GroupLayout.PREFERRED_SIZE))
										.addPreferredGap(
												ComponentPlacement.RELATED)
										.addGroup(
												gl_contentPane
														.createParallelGroup(
																Alignment.BASELINE)
														.addComponent(
																lblLsaVectorSpace)
														.addComponent(
																comboBoxLSA,
																GroupLayout.PREFERRED_SIZE,
																GroupLayout.DEFAULT_SIZE,
																GroupLayout.PREFERRED_SIZE))
										.addPreferredGap(
												ComponentPlacement.RELATED)
										.addGroup(
												gl_contentPane
														.createParallelGroup(
																Alignment.BASELINE)
														.addComponent(
																comboBoxLDA,
																GroupLayout.PREFERRED_SIZE,
																GroupLayout.DEFAULT_SIZE,
																GroupLayout.PREFERRED_SIZE)
														.addComponent(
																lblLdaModel))
										.addPreferredGap(
												ComponentPlacement.RELATED)
										.addGroup(
												gl_contentPane
														.createParallelGroup(
																Alignment.BASELINE)
														.addComponent(
																chckbxUsePosTagging)
														.addComponent(btnRun))
										.addContainerGap(
												GroupLayout.DEFAULT_SIZE,
												Short.MAX_VALUE)));
		contentPane.setLayout(gl_contentPane);
	}

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		BasicConfigurator.configure();

		adjustToSystemGraphics();

		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					RunMeasurementsView frame = new RunMeasurementsView();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	private static void adjustToSystemGraphics() {
		for (UIManager.LookAndFeelInfo info : UIManager
				.getInstalledLookAndFeels()) {
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
