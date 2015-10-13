package view.widgets.semanticModels;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Font;
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
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileFilter;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;

import services.semanticModels.PreProcessing;
import services.semanticModels.LDA.LDA;
import services.semanticModels.LSA.CreateInputMatrix;
import services.semanticModels.LSA.ProcessSVDOutput;
import services.semanticModels.LSA.RunSVD;
import utils.localization.LocalizationUtils;
import edu.cmu.lti.jawjaw.pobj.Lang;

public class SemanticModelsTraining extends JFrame {

	private static final long serialVersionUID = 4920477447183036103L;
	static Logger logger = Logger.getLogger(SemanticModelsTraining.class);

	private static final String TERM_DOC_MATRIX_NAME = "matrix.svd";
	private static final String SVD_FOLDER_NAME = "svd_out";

	private JPanel contentPane;
	private JTextField textFieldInput;
	private JTextField textFieldOutput;
	private JTextField textFieldLDADirectory;
	private JTextField textFieldLDANoTopics;
	private JTextField textFieldLDANoIterations;
	private JTextField textFieldLDANoThreads;
	private JTextField textFieldMinWords;
	private JCheckBox chckbxLSAUseHalfSigma;
	private JComboBox<String> comboBoxFormat;
	private JComboBox<String> comboBoxLanguage;
	private JComboBox<String> comboBoxLDALanguage;
	private JComboBox<String> comboBoxLSALanguage;
	private JCheckBox chckbxUsePosTagging;
	private JTextField textFieldLSAFile;
	private JTextField textFieldLSARank;
	private JTextField textFieldLSAReduceTasks;
	private JTextField textFieldLSAPowerIterations;
	private JButton btnPreProcess;
	private JButton btnLSATrain;
	private JButton btnLDATrain;

	private class PreProcessingTask extends SwingWorker<Void, Void> {
		private String input;
		private String output;
		private Lang lang;
		private int minNoWords;
		private boolean usePosTagging;
		private int selectedCase;

		public PreProcessingTask(String input, String output, Lang lang, int minNoWords, boolean usePosTagging,
				int selectedCase) {
			super();
			this.input = input;
			this.output = output;
			this.lang = lang;
			this.minNoWords = minNoWords;
			this.usePosTagging = usePosTagging;
			this.selectedCase = selectedCase;
		}

		public Void doInBackground() {
			btnPreProcess.setEnabled(false);
			btnLSATrain.setEnabled(false);
			btnLDATrain.setEnabled(false);

			try {
				PreProcessing preprocess = new PreProcessing();
				switch (selectedCase) {
				case 1:
					preprocess.parseTasa(input, output, lang, usePosTagging, minNoWords);
					break;
				case 2:
					preprocess.parseCOCA(input, output, lang, usePosTagging, minNoWords);
					break;
				default:
					preprocess.parseGeneralCorpus(input, output, lang, usePosTagging, minNoWords);
				}
			} catch (Exception exc) {
				logger.error("Error processing input file " + exc.getMessage(), exc);
			}
			return null;
		}

		@Override
		public void done() {
			Toolkit.getDefaultToolkit().beep();
			btnPreProcess.setEnabled(true);
			btnLSATrain.setEnabled(true);
			btnLDATrain.setEnabled(true);
		}
	}

	private class LSATrainingTask extends SwingWorker<Void, Void> {
		private File input;
		private Lang lang;
		private int k;
		private int noReduceTasks;
		private int noPowerIterations;

		public LSATrainingTask(File input, Lang lang, int k, int noReduceTasks, int noPowerIterations) {
			super();
			this.input = input;
			this.lang = lang;
			this.k = k;
			this.noReduceTasks = noReduceTasks;
			this.noPowerIterations = noPowerIterations;
		}

		public Void doInBackground() {
			btnPreProcess.setEnabled(false);
			btnLSATrain.setEnabled(false);
			btnLDATrain.setEnabled(false);

			try {
				// create initial matrix
				logger.info("Starting to create term-doc matrix");
				CreateInputMatrix lsaTraining = new CreateInputMatrix();
				lsaTraining.parseCorpus(input.getParent(), input.getName(), TERM_DOC_MATRIX_NAME, lang);

				logger.info("Finished building term-doc matrix");
				// perform SVD
				RunSVD.runSSVDOnSparseVectors(input.getParent() + "/" + TERM_DOC_MATRIX_NAME,
						input.getParent() + "/" + SVD_FOLDER_NAME, k, Math.min((int) k / 2, 150),
						Math.min(200000,
								(int) (3 * k * 0.01
										* Math.max(lsaTraining.getNoDocuments(), lsaTraining.getNoWords()))),
						noReduceTasks, noPowerIterations, chckbxLSAUseHalfSigma.isSelected());
				// RunSVD.runSSVDOnSparseVectors(input.getParent() + "/"
				// + TERM_DOC_MATRIX_NAME, input.getParent() + "/"
				// + SVD_FOLDER_NAME, k, 15, 200000, noReduceTasks, 1,
				// chckbxLSAUseHalfSigma.isSelected());

				logger.info("Finished performing SVD decomposition");
				// post-process
				ProcessSVDOutput processing = new ProcessSVDOutput();
				processing.performPostProcessing(input.getParent(), lang, chckbxLSAUseHalfSigma.isSelected());
				logger.info("Finished building the LSA model");
			} catch (Exception exc) {
				logger.error("Error procesing " + textFieldLDADirectory.getText() + " directory: " + exc.getMessage());
			}
			return null;
		}

		@Override
		public void done() {
			Toolkit.getDefaultToolkit().beep();
			btnPreProcess.setEnabled(true);
			btnLSATrain.setEnabled(true);
			btnLDATrain.setEnabled(true);
		}
	}

	private class LDATrainingTask extends SwingWorker<Void, Void> {
		private String input;
		private Lang lang;
		private int noTopics;
		private int noThreads;
		private int noIterations;

		public LDATrainingTask(String input, Lang lang, int noTopics, int noThreads, int noIterations) {
			super();
			this.input = input;
			this.lang = lang;
			this.noTopics = noTopics;
			this.noThreads = noThreads;
			this.noIterations = noIterations;
		}

		public Void doInBackground() {
			btnPreProcess.setEnabled(false);
			btnLSATrain.setEnabled(false);
			btnLDATrain.setEnabled(false);

			try {
				LDA lda = new LDA(lang);
				lda.processCorpus(input, noTopics, noThreads, noIterations);
				lda.printTopics(textFieldLDADirectory.getText(), 100);
			} catch (Exception exc) {
				logger.error("Error procesing " + textFieldLDADirectory.getText() + " directory: " + exc.getMessage());
			}
			return null;
		}

		@Override
		public void done() {
			Toolkit.getDefaultToolkit().beep();
			btnPreProcess.setEnabled(true);
			btnLSATrain.setEnabled(true);
			btnLDATrain.setEnabled(true);
		}
	}

	/**
	 * Create the frame.
	 */
	public SemanticModelsTraining() {
		setResizable(false);
		setTitle(LocalizationUtils.getTranslation("Semantic Models Training"));
		
		setBounds(100, 100, 450, 240);
		contentPane = new JPanel();
		contentPane.setBackground(Color.WHITE);
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPane);

		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		tabbedPane.setBackground(Color.WHITE);
		contentPane.add(tabbedPane, BorderLayout.CENTER);

		JPanel panelPreProcessing = new JPanel();
		panelPreProcessing.setBackground(Color.WHITE);
		tabbedPane.addTab(LocalizationUtils.getTranslation("Preprocessing"), null, panelPreProcessing, null);

		JLabel lblSelectInput = new JLabel(LocalizationUtils.getTranslation("Select input folder") + ":");

		textFieldInput = new JTextField();
		textFieldInput.setText(LocalizationUtils.getTranslation("incorpora preprocessing"));
		textFieldInput.setColumns(10);

		JLabel lblFormat = new JLabel(LocalizationUtils.getTranslation("Format") + ":");

		comboBoxFormat = new JComboBox<String>();
		comboBoxFormat.addItem(LocalizationUtils.getTranslation("One document per line"));
		comboBoxFormat.addItem(LocalizationUtils.getTranslation("TASA specific format id tags"));
		comboBoxFormat.addItem(LocalizationUtils.getTranslation("COCA specific format"));

		JButton btnBrowse = new JButton("...");
		btnBrowse.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFileChooser fc = null;
				fc = new JFileChooser("in");
				fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				int returnVal = fc.showOpenDialog(SemanticModelsTraining.this);

				if (returnVal == JFileChooser.APPROVE_OPTION) {
					File file = fc.getSelectedFile();
					textFieldInput.setText(file.getPath());
				}
			}
		});

		chckbxUsePosTagging = new JCheckBox(LocalizationUtils.getTranslation("Use POS tagging"));

		JLabel lblLanguage = new JLabel(LocalizationUtils.getTranslation("Language") + ":");

		comboBoxLanguage = new JComboBox<String>();
		for (String lang : Lang.SUPPORTED_LANGUAGES)
			comboBoxLanguage.addItem(lang);

		JLabel lblOutputFileName = new JLabel(LocalizationUtils.getTranslation("Output file name") + ":");

		textFieldOutput = new JTextField();
		textFieldOutput.setText("out.txt");
		textFieldOutput.setColumns(10);

		btnPreProcess = new JButton(LocalizationUtils.getTranslation("Process"));
		btnPreProcess.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (textFieldInput.getText().equals("")) {
					JOptionPane.showMessageDialog(SemanticModelsTraining.this,
							LocalizationUtils.getTranslation("Please select an appropriate input folder to be preprocessed") + "!", "Error",
							JOptionPane.WARNING_MESSAGE);
					return;
				}
				File input = new File(textFieldInput.getText());
				if (textFieldOutput.getText().equals("")) {
					JOptionPane.showMessageDialog(SemanticModelsTraining.this,
							LocalizationUtils.getTranslation("Please select an appropriate output file to save the preprocessing results"), "Error",
							JOptionPane.WARNING_MESSAGE);
					return;
				}
				Lang lang = Lang.getLang((String) (SemanticModelsTraining.this.comboBoxLanguage.getSelectedItem()));
				int minNoWords = 20;
				try {
					minNoWords = Integer.parseInt(textFieldMinWords.getText());
				} catch (Exception exc) {
					minNoWords = 20;
				}

				PreProcessingTask task = new PreProcessingTask(input.getPath(), textFieldOutput.getText(), lang,
						minNoWords, chckbxUsePosTagging.isSelected(),
						SemanticModelsTraining.this.comboBoxFormat.getSelectedIndex());
				task.execute();
			}
		});

		JLabel lblMinWords = new JLabel(LocalizationUtils.getTranslation("Min no words") + ":");

		textFieldMinWords = new JTextField();
		textFieldMinWords.setText("20");
		textFieldMinWords.setHorizontalAlignment(SwingConstants.RIGHT);
		textFieldMinWords.setColumns(10);

		GroupLayout gl_panelPreProcessing = new GroupLayout(panelPreProcessing);
		gl_panelPreProcessing.setHorizontalGroup(gl_panelPreProcessing.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panelPreProcessing.createSequentialGroup().addContainerGap()
						.addGroup(gl_panelPreProcessing.createParallelGroup(Alignment.LEADING)
								.addComponent(lblSelectInput).addComponent(lblOutputFileName)
								.addGroup(gl_panelPreProcessing.createParallelGroup(Alignment.TRAILING)
										.addComponent(lblFormat, Alignment.LEADING)
										.addComponent(lblLanguage, Alignment.LEADING))
								.addComponent(lblMinWords))
						.addGap(18)
						.addGroup(
								gl_panelPreProcessing.createParallelGroup(Alignment.TRAILING)
										.addGroup(gl_panelPreProcessing.createSequentialGroup()
												.addComponent(textFieldInput, GroupLayout.DEFAULT_SIZE, 250,
														Short.MAX_VALUE)
												.addPreferredGap(ComponentPlacement.RELATED).addComponent(btnBrowse))
								.addGroup(Alignment.LEADING, gl_panelPreProcessing.createSequentialGroup()
										.addComponent(textFieldMinWords, GroupLayout.PREFERRED_SIZE, 97,
												GroupLayout.PREFERRED_SIZE)
										.addPreferredGap(ComponentPlacement.RELATED).addComponent(chckbxUsePosTagging)
										.addPreferredGap(ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE,
												Short.MAX_VALUE)
										.addComponent(btnPreProcess))
								.addComponent(comboBoxFormat, Alignment.LEADING, 0, GroupLayout.DEFAULT_SIZE,
										Short.MAX_VALUE)
								.addComponent(comboBoxLanguage, Alignment.LEADING, 0, 245, Short.MAX_VALUE)
								.addComponent(textFieldOutput, Alignment.LEADING))
						.addContainerGap()));
		gl_panelPreProcessing.setVerticalGroup(gl_panelPreProcessing.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panelPreProcessing.createSequentialGroup().addContainerGap()
						.addGroup(gl_panelPreProcessing.createParallelGroup(Alignment.BASELINE)
								.addComponent(textFieldInput, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
										GroupLayout.PREFERRED_SIZE)
								.addComponent(lblSelectInput).addComponent(btnBrowse))
						.addPreferredGap(ComponentPlacement.RELATED)
						.addGroup(gl_panelPreProcessing.createParallelGroup(Alignment.BASELINE)
								.addComponent(lblOutputFileName).addComponent(textFieldOutput,
										GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
										GroupLayout.PREFERRED_SIZE))
						.addPreferredGap(ComponentPlacement.RELATED)
						.addGroup(gl_panelPreProcessing.createParallelGroup(Alignment.BASELINE)
								.addComponent(comboBoxLanguage, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
										GroupLayout.PREFERRED_SIZE)
								.addComponent(lblLanguage))
						.addPreferredGap(ComponentPlacement.RELATED)
						.addGroup(gl_panelPreProcessing.createParallelGroup(Alignment.BASELINE)
								.addComponent(comboBoxFormat, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
										GroupLayout.PREFERRED_SIZE)
								.addComponent(lblFormat))
						.addPreferredGap(ComponentPlacement.RELATED)
						.addGroup(
								gl_panelPreProcessing.createParallelGroup(Alignment.BASELINE).addComponent(lblMinWords)
										.addComponent(textFieldMinWords, GroupLayout.PREFERRED_SIZE,
												GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
								.addComponent(chckbxUsePosTagging).addComponent(btnPreProcess))
						.addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));
		panelPreProcessing.setLayout(gl_panelPreProcessing);

		JPanel panelLSATraining = new JPanel();
		panelLSATraining.setToolTipText(LocalizationUtils.getTranslation("LSA Training"));
		panelLSATraining.setBackground(Color.WHITE);
		tabbedPane.addTab("LSA Training", null, panelLSATraining, null);

		JLabel lblLSAInputFile = new JLabel(LocalizationUtils.getTranslation("Input file") + "*:");

		textFieldLSAFile = new JTextField();
		textFieldLSAFile.setText("config/LSA");
		textFieldLSAFile.setColumns(10);

		JLabel lblTxtOnly = new JLabel("* " + LocalizationUtils.getTranslation("Only a single TXT file is used for building the TermDoc matrix"));
		lblTxtOnly.setFont(new Font("SansSerif", Font.ITALIC, 10));

		JButton btnLSAFile = new JButton("...");
		btnLSAFile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFileChooser fc = new JFileChooser("config/LSA");
				fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
				fc.setFileFilter(new FileFilter() {
					public boolean accept(File f) {
						if (f.isDirectory()) {
							return true;
						}
						return f.getName().endsWith(".txt");
					}

					public String getDescription() {
						return "Text documents (*.txt)";
					}
				});
				int returnVal = fc.showOpenDialog(SemanticModelsTraining.this);

				if (returnVal == JFileChooser.APPROVE_OPTION) {
					File file = fc.getSelectedFile();
					textFieldLSAFile.setText(file.getPath());
				}
			}
		});

		JLabel lblLSALanguage = new JLabel(LocalizationUtils.getTranslation("Language") + ":");

		JLabel lblLSARank = new JLabel(LocalizationUtils.getTranslation("LSA rank") + ":");

		JLabel lblLSAReduceTasks = new JLabel(LocalizationUtils.getTranslation("No reduce tasks") + ":");

		textFieldLSARank = new JTextField();
		textFieldLSARank.setText("300");
		textFieldLSARank.setHorizontalAlignment(SwingConstants.RIGHT);
		textFieldLSARank.setColumns(10);

		textFieldLSAReduceTasks = new JTextField();
		textFieldLSAReduceTasks.setText("1");
		textFieldLSAReduceTasks.setHorizontalAlignment(SwingConstants.RIGHT);
		textFieldLSAReduceTasks.setColumns(10);

		JLabel lblLSAPowerInterations = new JLabel(LocalizationUtils.getTranslation("No power interations") + ":");

		textFieldLSAPowerIterations = new JTextField();
		textFieldLSAPowerIterations.setText("1");
		textFieldLSAPowerIterations.setHorizontalAlignment(SwingConstants.RIGHT);
		textFieldLSAPowerIterations.setColumns(10);

		comboBoxLSALanguage = new JComboBox<String>();
		for (String lang : Lang.SUPPORTED_LANGUAGES)
			comboBoxLSALanguage.addItem(lang);

		chckbxLSAUseHalfSigma = new JCheckBox(LocalizationUtils.getTranslation("Use half sigma for final comutations"));
		chckbxLSAUseHalfSigma.setSelected(true);

		btnLSATrain = new JButton("Train LSA model");
		btnLSATrain.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int k = 300;
				int noPowerIterations = 1;
				int noReduceTasks = 1;
				Lang lang = null;
				if (textFieldLSAFile.getText().equals("") || !textFieldLSAFile.getText().endsWith(".txt")
						|| !new File(textFieldLSAFile.getText()).exists()) {
					JOptionPane.showMessageDialog(SemanticModelsTraining.this,
							"Please select an appropriate text file as input for the LSA model!", "Error",
							JOptionPane.WARNING_MESSAGE);
					return;
				}
				File input = new File(textFieldLSAFile.getText());
				try {
					noPowerIterations = Integer.parseInt(textFieldLSAPowerIterations.getText());
				} catch (Exception exc) {
					noPowerIterations = 1;
				}
				try {
					noReduceTasks = Integer.parseInt(textFieldLSAReduceTasks.getText());
				} catch (Exception exc) {
					noReduceTasks = 1;
				}
				try {
					k = Integer.parseInt(textFieldLSARank.getText());
				} catch (Exception exc) {
					k = 300;
				}
				lang = Lang.getLang((String) (SemanticModelsTraining.this.comboBoxLSALanguage.getSelectedItem()));

				LSATrainingTask task = new LSATrainingTask(input, lang, k, noReduceTasks, noPowerIterations);
				task.execute();
			}
		});

		GroupLayout gl_panelLSATraining = new GroupLayout(panelLSATraining);
		gl_panelLSATraining.setHorizontalGroup(gl_panelLSATraining.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panelLSATraining.createSequentialGroup().addContainerGap()
						.addGroup(gl_panelLSATraining.createParallelGroup(Alignment.LEADING)
								.addGroup(Alignment.TRAILING, gl_panelLSATraining.createSequentialGroup()
										.addGroup(gl_panelLSATraining.createParallelGroup(Alignment.LEADING)
												.addGroup(gl_panelLSATraining.createSequentialGroup()
														.addComponent(lblLSAInputFile)
														.addPreferredGap(ComponentPlacement.RELATED)
														.addComponent(textFieldLSAFile, GroupLayout.DEFAULT_SIZE, 316,
																Short.MAX_VALUE))
												.addComponent(lblTxtOnly))
										.addPreferredGap(ComponentPlacement.RELATED).addComponent(
												btnLSAFile))
						.addGroup(Alignment.TRAILING,
								gl_panelLSATraining.createSequentialGroup().addGroup(gl_panelLSATraining
										.createParallelGroup(Alignment.LEADING).addComponent(lblLSALanguage)
										.addComponent(lblLSARank).addComponent(lblLSAReduceTasks))
								.addGap(29)
								.addGroup(gl_panelLSATraining.createParallelGroup(Alignment.LEADING)
										.addGroup(gl_panelLSATraining.createSequentialGroup()
												.addGroup(gl_panelLSATraining.createParallelGroup(Alignment.LEADING)
														.addComponent(textFieldLSAReduceTasks, GroupLayout.DEFAULT_SIZE,
																66, Short.MAX_VALUE)
														.addComponent(textFieldLSARank, Alignment.TRAILING,
																GroupLayout.DEFAULT_SIZE, 66, Short.MAX_VALUE))
												.addPreferredGap(ComponentPlacement.RELATED)
												.addGroup(gl_panelLSATraining.createParallelGroup(Alignment.TRAILING)
														.addGroup(gl_panelLSATraining.createSequentialGroup()
																.addComponent(lblLSAPowerInterations)
																.addPreferredGap(ComponentPlacement.RELATED)
																.addComponent(textFieldLSAPowerIterations,
																		GroupLayout.PREFERRED_SIZE, 91,
																		GroupLayout.PREFERRED_SIZE))
														.addComponent(btnLSATrain)))
										.addComponent(comboBoxLSALanguage, 0, 297, Short.MAX_VALUE)))
						.addComponent(chckbxLSAUseHalfSigma)).addContainerGap()));
		gl_panelLSATraining.setVerticalGroup(gl_panelLSATraining.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panelLSATraining.createSequentialGroup().addContainerGap()
						.addGroup(gl_panelLSATraining.createParallelGroup(Alignment.BASELINE)
								.addComponent(lblLSAInputFile).addComponent(btnLSAFile).addComponent(textFieldLSAFile,
										GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
										GroupLayout.PREFERRED_SIZE))
						.addPreferredGap(ComponentPlacement.RELATED).addComponent(lblTxtOnly)
						.addPreferredGap(ComponentPlacement.RELATED)
						.addGroup(
								gl_panelLSATraining.createParallelGroup(Alignment.BASELINE).addComponent(lblLSALanguage)
										.addComponent(comboBoxLSALanguage, GroupLayout.PREFERRED_SIZE,
												GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
						.addPreferredGap(ComponentPlacement.RELATED)
						.addGroup(gl_panelLSATraining.createParallelGroup(Alignment.BASELINE).addComponent(lblLSARank)
								.addComponent(textFieldLSAPowerIterations, GroupLayout.PREFERRED_SIZE,
										GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
								.addComponent(lblLSAPowerInterations).addComponent(textFieldLSARank,
										GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
										GroupLayout.PREFERRED_SIZE))
						.addPreferredGap(ComponentPlacement.RELATED)
						.addGroup(gl_panelLSATraining.createParallelGroup(Alignment.TRAILING)
								.addGroup(gl_panelLSATraining.createSequentialGroup()
										.addGroup(gl_panelLSATraining.createParallelGroup(Alignment.BASELINE)
												.addComponent(textFieldLSAReduceTasks, GroupLayout.PREFERRED_SIZE,
														GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
												.addComponent(lblLSAReduceTasks))
										.addPreferredGap(ComponentPlacement.RELATED)
										.addComponent(chckbxLSAUseHalfSigma))
								.addComponent(btnLSATrain))
						.addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));
		panelLSATraining.setLayout(gl_panelLSATraining);

		JPanel panelLDATraining = new JPanel();
		panelLDATraining.setToolTipText(LocalizationUtils.getTranslation("LDA Training") + "\n");
		panelLDATraining.setBackground(Color.WHITE);
		tabbedPane.addTab("LDA Training", null, panelLDATraining, null);

		JLabel lblLDAInputDirectory = new JLabel(LocalizationUtils.getTranslation("Input directory") + "*:");

		JButton btnLDADirectory = new JButton("...");
		btnLDADirectory.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFileChooser fc = new JFileChooser("config/LDA");
				fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				int returnVal = fc.showOpenDialog(SemanticModelsTraining.this);

				if (returnVal == JFileChooser.APPROVE_OPTION) {
					File file = fc.getSelectedFile();
					textFieldLDADirectory.setText(file.getPath());
				}
			}
		});

		textFieldLDADirectory = new JTextField();
		textFieldLDADirectory.setText("config/LDA");
		textFieldLDADirectory.setColumns(10);

		JLabel lblAllTxt = new JLabel("* " + LocalizationUtils.getTranslation("All TXT files within the provided directory will be taken into consideration"));
		lblAllTxt.setFont(new Font("SansSerif", Font.ITALIC, 10));

		JLabel lblLDALanguage = new JLabel(LocalizationUtils.getTranslation("Language") + ":");

		comboBoxLDALanguage = new JComboBox<String>();
		for (String lang : Lang.SUPPORTED_LANGUAGES) {
			comboBoxLDALanguage.addItem(lang);
		}

		JLabel lblLDANoTopics = new JLabel(LocalizationUtils.getTranslation("No topics") + ":");

		textFieldLDANoTopics = new JTextField();
		textFieldLDANoTopics.setHorizontalAlignment(SwingConstants.RIGHT);
		textFieldLDANoTopics.setText("100");
		textFieldLDANoTopics.setColumns(10);

		JLabel lblLDANoIterations = new JLabel(LocalizationUtils.getTranslation("No iterations") + ":");

		textFieldLDANoIterations = new JTextField();
		textFieldLDANoIterations.setHorizontalAlignment(SwingConstants.RIGHT);
		textFieldLDANoIterations.setText("10000");
		textFieldLDANoIterations.setColumns(10);

		JLabel lblLDANoThreads = new JLabel(LocalizationUtils.getTranslation("No threads") + ":");

		textFieldLDANoThreads = new JTextField();
		textFieldLDANoThreads.setHorizontalAlignment(SwingConstants.RIGHT);
		textFieldLDANoThreads.setText("2");
		textFieldLDANoThreads.setColumns(10);

		btnLDATrain = new JButton(LocalizationUtils.getTranslation("Train LDA model"));
		btnLDATrain.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int noTopics = 100;
				int noIterations = 10000;
				int noThreads = 2;
				if (textFieldLDADirectory.getText().equals("")) {
					JOptionPane.showMessageDialog(SemanticModelsTraining.this,
							"Please select an appropriate directory as input for the LDA model!", "Error",
							JOptionPane.WARNING_MESSAGE);
					return;
				}
				try {
					noTopics = Integer.parseInt(textFieldLDANoTopics.getText());
				} catch (Exception exc) {
					noTopics = 100;
				}
				try {
					noIterations = Integer.parseInt(textFieldLDANoIterations.getText());
				} catch (Exception exc) {
					noIterations = 10000;
				}
				try {
					noThreads = Integer.parseInt(textFieldLDANoThreads.getText());
				} catch (Exception exc) {
					noThreads = 2;
				}
				Lang lang = Lang.getLang((String) (SemanticModelsTraining.this.comboBoxLanguage.getSelectedItem()));

				LDATrainingTask task = new LDATrainingTask(textFieldLDADirectory.getText(), lang, noTopics, noThreads,
						noIterations);
				task.execute();
			}
		});
		GroupLayout gl_panelLDATraining = new GroupLayout(panelLDATraining);
		gl_panelLDATraining.setHorizontalGroup(gl_panelLDATraining.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panelLDATraining.createSequentialGroup().addContainerGap().addGroup(gl_panelLDATraining
						.createParallelGroup(Alignment.TRAILING)
						.addGroup(gl_panelLDATraining.createSequentialGroup().addGroup(gl_panelLDATraining
								.createParallelGroup(Alignment.TRAILING)
								.addGroup(Alignment.LEADING,
										gl_panelLDATraining.createSequentialGroup().addComponent(lblLDAInputDirectory)
												.addPreferredGap(ComponentPlacement.RELATED)
												.addComponent(textFieldLDADirectory, GroupLayout.DEFAULT_SIZE, 282,
														Short.MAX_VALUE))
								.addComponent(lblAllTxt)).addPreferredGap(ComponentPlacement.RELATED)
								.addComponent(btnLDADirectory))
						.addGroup(gl_panelLDATraining.createSequentialGroup()
								.addGroup(gl_panelLDATraining.createParallelGroup(Alignment.LEADING)
										.addComponent(lblLDALanguage).addComponent(lblLDANoTopics)
										.addComponent(lblLDANoThreads))
								.addGap(29)
								.addGroup(gl_panelLDATraining.createParallelGroup(Alignment.LEADING)
										.addGroup(gl_panelLDATraining.createSequentialGroup()
												.addGroup(gl_panelLDATraining.createParallelGroup(Alignment.TRAILING)
														.addComponent(textFieldLDANoTopics, GroupLayout.DEFAULT_SIZE,
																88, Short.MAX_VALUE)
												.addComponent(textFieldLDANoThreads, 0, 0, Short.MAX_VALUE))
												.addPreferredGap(ComponentPlacement.RELATED)
												.addGroup(gl_panelLDATraining.createParallelGroup(Alignment.TRAILING)
														.addGroup(gl_panelLDATraining.createSequentialGroup()
																.addComponent(lblLDANoIterations).addGap(61)
																.addComponent(textFieldLDANoIterations,
																		GroupLayout.PREFERRED_SIZE, 91,
																		GroupLayout.PREFERRED_SIZE))
														.addComponent(btnLDATrain)))
										.addComponent(comboBoxLDALanguage, 0, 327, Short.MAX_VALUE))))
						.addContainerGap()));
		gl_panelLDATraining.setVerticalGroup(gl_panelLDATraining.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panelLDATraining.createSequentialGroup().addContainerGap()
						.addGroup(gl_panelLDATraining.createParallelGroup(Alignment.BASELINE)
								.addComponent(lblLDAInputDirectory).addComponent(btnLDADirectory)
								.addComponent(textFieldLDADirectory, GroupLayout.PREFERRED_SIZE,
										GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
						.addPreferredGap(ComponentPlacement.RELATED).addComponent(lblAllTxt)
						.addPreferredGap(ComponentPlacement.RELATED)
						.addGroup(
								gl_panelLDATraining.createParallelGroup(Alignment.BASELINE).addComponent(lblLDALanguage)
										.addComponent(comboBoxLDALanguage, GroupLayout.PREFERRED_SIZE,
												GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
						.addPreferredGap(ComponentPlacement.RELATED)
						.addGroup(
								gl_panelLDATraining.createParallelGroup(Alignment.BASELINE).addComponent(lblLDANoTopics)
										.addComponent(textFieldLDANoTopics, GroupLayout.PREFERRED_SIZE,
												GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
								.addComponent(lblLDANoIterations)
								.addComponent(textFieldLDANoIterations, GroupLayout.PREFERRED_SIZE,
										GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
						.addPreferredGap(ComponentPlacement.RELATED)
						.addGroup(gl_panelLDATraining.createParallelGroup(Alignment.BASELINE)
								.addComponent(textFieldLDANoThreads, GroupLayout.PREFERRED_SIZE,
										GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
								.addComponent(lblLDANoThreads).addComponent(btnLDATrain))
						.addContainerGap(40, Short.MAX_VALUE)));
		panelLDATraining.setLayout(gl_panelLDATraining);
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
					SemanticModelsTraining frame = new SemanticModelsTraining();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
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
