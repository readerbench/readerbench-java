package view.widgets;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyVetoException;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.swing.ButtonGroup;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDesktopPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JTabbedPane;
import javax.swing.KeyStroke;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.Painter;
import javax.swing.UIDefaults;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import DAO.AbstractDocument;
import DAO.document.Document;
import edu.cmu.lti.jawjaw.pobj.Lang;
import utils.localization.LocalizationUtils;
import view.events.TCPopupEventQueue;
import view.widgets.chat.VCoPView;
import view.widgets.complexity.CorpusEvaluationView;
import view.widgets.complexity.DocumentEvaluationView;
import view.widgets.complexity.EssayProcessingView;
import view.widgets.complexity.RunMeasurementsView;
import view.widgets.document.DocumentManagementView;
import view.widgets.document.DocumentProcessingView;
import view.widgets.document.DocumentSemanticSearchView;
import view.widgets.document.EssayManagementView;
import view.widgets.selfexplanation.summary.SummaryProcessingView;
import view.widgets.selfexplanation.verbalization.AnnotateVerbalizationView;
import view.widgets.selfexplanation.verbalization.CreateVerbalizationView;
import view.widgets.selfexplanation.verbalization.VerbalizationProcessingView;
import view.widgets.semanticModels.SemanticModelsTraining;

public class ReaderBenchView extends JFrame {
	private static final long serialVersionUID = 4565038532352428650L;
	public static Logger logger = Logger.getLogger(ReaderBenchView.class);
	public static Locale LOADED_LOCALE = LocalizationUtils.LOADED_LOCALE;

	public static final String[] TRAINED_LSA_SPACES_EN = {
			"resources/config/LSA/tasa_en", "resources/config/LSA/tasa_lak_en" };
	public static final String[] TRAINED_LSA_SPACES_FR = {
			"resources/config/LSA/lemonde_fr", "resources/config/LSA/textenfants_fr" };
	public static final String[] TRAINED_LSA_SPACES_IT = { "" };
	public static final String[] TRAINED_LSA_SPACES_ES = { "resources/config/LSA/joseantonio_es" };
	public static final String[] TRAINED_LDA_MODELS_EN = {
			"resources/config/LDA/tasa_en", "resources/config/LDA/tasa_lak_en",
			"resources/config/LDA/tasa_smart_cities_en" };
	public static final String[] TRAINED_LDA_MODELS_FR = {
			"resources/config/LDA/lemonde_fr", "resources/config/LDA/textenfants_fr",
			"resources/config/LDA/philosophy_fr" };
	public static final String[] TRAINED_LDA_MODELS_IT = { "resources/config/LDA/paisa_it" };
	public static final String[] TRAINED_LDA_MODELS_ES = { "resources/config/LDA/joseantonio_es" };

	public static Lang RUNTIME_LANGUAGE = null;

	private JMenuBar menuBar;
	// File menu item
	private JMenu mnFile;
	private JMenuItem mntmQuit;
	// Options menu item
	private JMenu mnOptions;
	private JMenu mnLanguage;
	private JRadioButtonMenuItem rdbtnmntmEnglish;
	private JRadioButtonMenuItem rdbtnmntmFrench;

	// tabbed panel
	private JTabbedPane tabbedPane;
	// data input panel
	private JPanel panelDataInput;
	private JButton btnCreateDocument;
	private JButton btnCreateVerbalization;
	private JButton btnAnnotateVerbalization;

	// pre-processing
	private JButton btnPreprocessingTrainSemanticModels;
	private JButton btnRunTextualComplexityIndices;
	
	// document analysis
	private JButton btnDocProcessing;
	private JButton btnDocumentSemanticSearch;
	private JButton btnPredictTextualComplexity;
	
	// textual complexity
	private JButton btnCorpusEvaluation;
	private JButton btnEssayProcessing;
	
	// self-explanations
	private JButton btnVerbaProcessing;
	private JButton btnSummaryProcessing;
	
	
	private JDesktopPane desktopPane;
	
	private GroupLayout gl_panelSettingsSpecific;
	private GroupLayout gl_panelSettingsGeneral;

	public ReaderBenchView() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		getContentPane().setBackground(Color.WHITE);
		getContentPane().setLayout(new BorderLayout(0, 0));
		setTitle("ReaderBench");
		setIconImage(Toolkit.getDefaultToolkit().getImage("resources/config/RB_icon.png"));
		Locale.setDefault(LOADED_LOCALE);

		// adjust view to desktop size
		setBounds(50, 50, 1180, 750);

		desktopPane = new JDesktopPane() {
			private static final long serialVersionUID = 8453433109734630086L;

			@Override
			public void updateUI() {
				if ("Nimbus".equals(UIManager.getLookAndFeel().getName())) {
					UIDefaults map = new UIDefaults();
					Painter<JComponent> painter = new Painter<JComponent>() {
						@Override
						public void paint(Graphics2D g, JComponent c, int w,
								int h) {
							g.setColor(Color.WHITE);
							g.fillRect(0, 0, w, h);
						}
					};
					map.put("DesktopPane[Enabled].backgroundPainter", painter);
					putClientProperty("Nimbus.Overrides", map);
				}
				super.updateUI();
			}
		};
		desktopPane.setBackground(Color.WHITE);
		getContentPane().add(desktopPane, BorderLayout.CENTER);

		tabbedPane = new JTabbedPane(JTabbedPane.TOP);

		panelDataInput = new JPanel();
		panelDataInput.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null,
				null));
		panelDataInput.setBackground(Color.WHITE);
		tabbedPane
				.addTab(ResourceBundle
						.getBundle("utils.localization.messages").getString("ReaderBenchView.panelDataInput.title"), //$NON-NLS-1$ //$NON-NLS-2$
						null, panelDataInput, null);

		btnCreateDocument = new JButton(
				ResourceBundle
						.getBundle("utils.localization.messages").getString("ReaderBenchView.btnCreateDocument.text")); //$NON-NLS-1$ //$NON-NLS-2$
		btnCreateDocument.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				DocumentManagementView view = new DocumentManagementView();
				view.setVisible(true);
			}
		});

		btnCreateVerbalization = new JButton(ResourceBundle.getBundle(
				"utils.localization.messages") //$NON-NLS-1$
				.getString("ReaderBenchView.btnCreateVerbalization.text")); //$NON-NLS-1$
		btnCreateVerbalization.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				CreateVerbalizationView view = new CreateVerbalizationView();
				view.setVisible(true);
			}
		});

		btnAnnotateVerbalization = new JButton(ResourceBundle.getBundle(
				"utils.localization.messages") //$NON-NLS-1$
				.getString("ReaderBenchView.btnAnnotateVerbalization.text")); //$NON-NLS-1$
		btnAnnotateVerbalization.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				AnnotateVerbalizationView view = new AnnotateVerbalizationView();
				view.setVisible(true);
			}
		});
		GroupLayout gl_panelDataInput = new GroupLayout(panelDataInput);
		gl_panelDataInput.setHorizontalGroup(gl_panelDataInput
				.createParallelGroup(Alignment.LEADING).addGroup(
						gl_panelDataInput
								.createSequentialGroup()
								.addContainerGap()
								.addComponent(btnCreateDocument)
								.addPreferredGap(ComponentPlacement.RELATED)
								.addComponent(btnCreateVerbalization)
								.addGap(18)
								.addComponent(btnAnnotateVerbalization,
										GroupLayout.DEFAULT_SIZE,
										GroupLayout.DEFAULT_SIZE,
										Short.MAX_VALUE).addGap(801)));
		gl_panelDataInput
				.setVerticalGroup(gl_panelDataInput
						.createParallelGroup(Alignment.LEADING)
						.addGroup(
								gl_panelDataInput
										.createSequentialGroup()
										.addContainerGap()
										.addGroup(
												gl_panelDataInput
														.createParallelGroup(
																Alignment.BASELINE)
														.addComponent(
																btnCreateDocument)
														.addComponent(
																btnCreateVerbalization)
														.addComponent(
																btnAnnotateVerbalization))
										.addContainerGap(53, Short.MAX_VALUE)));
		panelDataInput.setLayout(gl_panelDataInput);

		JPanel panelPreProcessing = new JPanel();
		panelPreProcessing.setBorder(new EtchedBorder(EtchedBorder.LOWERED,
				null, null));
		panelPreProcessing.setBackground(Color.WHITE);
		tabbedPane.addTab(LocalizationUtils.getLocalizedString(this.getClass(),
				LocalizationUtils.TITLE, "panelPreProcessing"), null,
				panelPreProcessing, null);

		btnPreprocessingTrainSemanticModels = new JButton(
				ResourceBundle
						.getBundle("utils.localization.messages").getString("ReaderBenchView.btnTrainSemanticModels.text")); //$NON-NLS-1$ //$NON-NLS-2$
		btnPreprocessingTrainSemanticModels
				.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
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
				});

		btnRunTextualComplexityIndices = new JButton(
				ResourceBundle
						.getBundle("utils.localization.messages").getString("ReaderBenchView.btnRunTextualComplexityIndices.text")); //$NON-NLS-1$ //$NON-NLS-2$
		btnRunTextualComplexityIndices.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				RunMeasurementsView frame = new RunMeasurementsView();
				frame.setVisible(true);
			}
		});
		GroupLayout gl_panelPreProcessing = new GroupLayout(panelPreProcessing);
		gl_panelPreProcessing.setHorizontalGroup(
			gl_panelPreProcessing.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panelPreProcessing.createSequentialGroup()
					.addContainerGap()
					.addComponent(btnPreprocessingTrainSemanticModels, GroupLayout.PREFERRED_SIZE, 200, GroupLayout.PREFERRED_SIZE)
					.addGap(18)
					.addComponent(btnRunTextualComplexityIndices, GroupLayout.PREFERRED_SIZE, 262, GroupLayout.PREFERRED_SIZE)
					.addContainerGap(893, Short.MAX_VALUE))
		);
		gl_panelPreProcessing.setVerticalGroup(
			gl_panelPreProcessing.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panelPreProcessing.createSequentialGroup()
					.addContainerGap()
					.addGroup(gl_panelPreProcessing.createParallelGroup(Alignment.BASELINE)
						.addComponent(btnPreprocessingTrainSemanticModels, GroupLayout.PREFERRED_SIZE, 25, GroupLayout.PREFERRED_SIZE)
						.addComponent(btnRunTextualComplexityIndices))
					.addContainerGap(53, Short.MAX_VALUE))
		);
		panelPreProcessing.setLayout(gl_panelPreProcessing);

		JPanel panelDocument = new JPanel();
		panelDocument.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null,
				null));
		tabbedPane.addTab(LocalizationUtils.getLocalizedString(this.getClass(),
				LocalizationUtils.TITLE, "panelDocument"), //$NON-NLS-1$ //$NON-NLS-2$
				null, panelDocument, null);

		JPanel panelDocSpecific = new JPanel();
		panelDocSpecific.setBackground(Color.WHITE);
		panelDocSpecific.setBorder(new TitledBorder(new EtchedBorder(
				EtchedBorder.LOWERED, null, null), "",
				TitledBorder.LEADING, TitledBorder.TOP, null, null));

		btnPredictTextualComplexity = new JButton(
				LocalizationUtils.getLocalizedString(this.getClass(),
						LocalizationUtils.TEXT, "btnTrainSVMDocs")); //$NON-NLS-1$ //$NON-NLS-2$
		btnPredictTextualComplexity.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (DocumentProcessingView.getLoadedDocuments().size() > 0) {
					List<AbstractDocument> abstractDocs = new LinkedList<AbstractDocument>();
					for (Document d : DocumentProcessingView
							.getLoadedDocuments())
						abstractDocs.add(d);
					DocumentEvaluationView view = new DocumentEvaluationView(
							abstractDocs);
					view.setVisible(true);
				} else {
					JOptionPane
							.showMessageDialog(
									desktopPane,
									"At least one document must be loaded for evaluating its textual complexity!",
									"Information",
									JOptionPane.INFORMATION_MESSAGE);
				}
			}
		});

		btnDocProcessing = new JButton();
		btnDocProcessing.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				DocumentProcessingView frame = new DocumentProcessingView();
				frame.setVisible(true);
				desktopPane.add(frame);
				try {
					frame.setSelected(true);
				} catch (PropertyVetoException e1) {
					e1.printStackTrace();
				}
			}
		});
		
		btnDocumentSemanticSearch = new JButton(ResourceBundle.getBundle("utils.localization.messages").getString("ReaderBenchView.btnDocumentSemanticSearch.text")); //$NON-NLS-1$ //$NON-NLS-2$
		btnDocumentSemanticSearch.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				DocumentSemanticSearchView frame = new DocumentSemanticSearchView();
				frame.setVisible(true);
				desktopPane.add(frame);
				try {
					frame.setSelected(true);
				} catch (PropertyVetoException e1) {
					e1.printStackTrace();
				}
			}
		});

		GroupLayout gl_panelDocSpecific = new GroupLayout(panelDocSpecific);
		gl_panelDocSpecific.setHorizontalGroup(
			gl_panelDocSpecific.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panelDocSpecific.createSequentialGroup()
					.addContainerGap()
					.addComponent(btnDocProcessing, GroupLayout.PREFERRED_SIZE, 200, GroupLayout.PREFERRED_SIZE)
					.addGap(18)
					.addComponent(btnDocumentSemanticSearch, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
					.addGap(31)
					.addComponent(btnPredictTextualComplexity)
					.addGap(717))
		);
		gl_panelDocSpecific.setVerticalGroup(
			gl_panelDocSpecific.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panelDocSpecific.createSequentialGroup()
					.addContainerGap()
					.addGroup(gl_panelDocSpecific.createParallelGroup(Alignment.BASELINE)
						.addComponent(btnDocProcessing)
						.addComponent(btnDocumentSemanticSearch)
						.addComponent(btnPredictTextualComplexity))
					.addContainerGap(38, Short.MAX_VALUE))
		);
		panelDocSpecific.setLayout(gl_panelDocSpecific);
		panelDocument.setLayout(new BorderLayout(0, 0));
		panelDocument.add(panelDocSpecific, BorderLayout.CENTER);
		
				JPanel panelTextualComplexity = new JPanel();
				panelTextualComplexity.setToolTipText("Textual Complexity");
				tabbedPane
						.addTab(ResourceBundle.getBundle("utils.localization.messages") //$NON-NLS-1$
								.getString(
										"ReaderBenchView.panelTextualComplexity.title"), //$NON-NLS-1$
								null, panelTextualComplexity, null);
				panelTextualComplexity.setLayout(new BorderLayout(0, 0));
				panelTextualComplexity.setBorder(new EtchedBorder(EtchedBorder.LOWERED,
						null, null));
				
				JPanel panelTextualComplexityGeneral = new JPanel();
				panelTextualComplexityGeneral.setBorder(new TitledBorder(
						new EtchedBorder(EtchedBorder.LOWERED, null, null), "",
						TitledBorder.LEADING, TitledBorder.TOP, null, null));
				panelTextualComplexityGeneral.setBackground(Color.WHITE);
				panelTextualComplexity.add(panelTextualComplexityGeneral,
						BorderLayout.CENTER);
								
				btnCorpusEvaluation = new JButton(ResourceBundle.getBundle(
						"utils.localization.messages") //$NON-NLS-1$
						.getString("ReaderBenchView.btnCorpusEvaluation.text")); //$NON-NLS-1$
				btnCorpusEvaluation.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						CorpusEvaluationView frame = new CorpusEvaluationView();
						frame.setVisible(true);
					}
				});
				
				btnEssayProcessing = new JButton(ResourceBundle.getBundle("utils.localization.messages").getString("ReaderBenchView.btnEssayProcessing.text")); //$NON-NLS-1$ //$NON-NLS-2$
				btnEssayProcessing.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						EssayProcessingView frame = new EssayProcessingView();
						frame.setVisible(true);
					}
				});
				GroupLayout gl_panel = new GroupLayout(panelTextualComplexityGeneral);
				gl_panel.setHorizontalGroup(
					gl_panel.createParallelGroup(Alignment.LEADING)
						.addGroup(gl_panel.createSequentialGroup()
							.addContainerGap()
							.addComponent(btnCorpusEvaluation, GroupLayout.PREFERRED_SIZE, 171, GroupLayout.PREFERRED_SIZE)
							.addGap(18)
							.addComponent(btnEssayProcessing, GroupLayout.PREFERRED_SIZE, 179, GroupLayout.PREFERRED_SIZE)
							.addContainerGap(1001, Short.MAX_VALUE))
				);
				gl_panel.setVerticalGroup(
					gl_panel.createParallelGroup(Alignment.TRAILING)
						.addGroup(gl_panel.createSequentialGroup()
							.addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
							.addGroup(gl_panel.createParallelGroup(Alignment.BASELINE)
								.addComponent(btnCorpusEvaluation, GroupLayout.PREFERRED_SIZE, 25, GroupLayout.PREFERRED_SIZE)
								.addComponent(btnEssayProcessing))
							.addGap(49))
				);
				panelTextualComplexityGeneral.setLayout(gl_panel);

		JPanel panelSelfExplanations = new JPanel();
		panelSelfExplanations.setBorder(new EtchedBorder(EtchedBorder.LOWERED,
				null, null));
		panelSelfExplanations.setBackground(UIManager
				.getColor("nimbusBlueGrey"));
		tabbedPane
				.addTab(ResourceBundle.getBundle("utils.localization.messages") //$NON-NLS-1$
						.getString(
								"ReaderBenchView.panelSelfExplanations.title"), null, panelSelfExplanations, null);

		JPanel panelVerbSpecific = new JPanel();
		panelVerbSpecific.setBorder(new TitledBorder(new EtchedBorder(
				EtchedBorder.LOWERED, null, null), "",
				TitledBorder.LEADING, TitledBorder.TOP, null, null));
		panelVerbSpecific.setBackground(Color.WHITE);

		btnVerbaProcessing = new JButton(
				ResourceBundle
						.getBundle("utils.localization.messages").getString("ReaderBenchView.btnVerbaProcessing.text")); //$NON-NLS-1$ //$NON-NLS-2$
		btnVerbaProcessing.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				VerbalizationProcessingView frame = new VerbalizationProcessingView();
				frame.setVisible(true);
				desktopPane.add(frame);
				try {
					frame.setSelected(true);
				} catch (PropertyVetoException e1) {
					e1.printStackTrace();
				}
			}
		});

		btnSummaryProcessing = new JButton(ResourceBundle.getBundle(
				"utils.localization.messages") //$NON-NLS-1$
				.getString("ReaderBenchView.btnSummaryProcessing.text")); //$NON-NLS-1$
		btnSummaryProcessing.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				SummaryProcessingView frame = new SummaryProcessingView();
				frame.setVisible(true);
				desktopPane.add(frame);
				try {
					frame.setSelected(true);
				} catch (PropertyVetoException e1) {
					e1.printStackTrace();
				}
			}
		});
		GroupLayout gl_panelVerbSpecific = new GroupLayout(panelVerbSpecific);
		gl_panelVerbSpecific.setHorizontalGroup(
			gl_panelVerbSpecific.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panelVerbSpecific.createSequentialGroup()
					.addContainerGap()
					.addComponent(btnVerbaProcessing, GroupLayout.PREFERRED_SIZE, 200, GroupLayout.PREFERRED_SIZE)
					.addGap(37)
					.addComponent(btnSummaryProcessing, GroupLayout.DEFAULT_SIZE, 208, Short.MAX_VALUE)
					.addGap(938))
		);
		gl_panelVerbSpecific.setVerticalGroup(
			gl_panelVerbSpecific.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panelVerbSpecific.createSequentialGroup()
					.addContainerGap()
					.addGroup(gl_panelVerbSpecific.createParallelGroup(Alignment.BASELINE)
						.addComponent(btnVerbaProcessing)
						.addComponent(btnSummaryProcessing))
					.addContainerGap(49, Short.MAX_VALUE))
		);
		panelVerbSpecific.setLayout(gl_panelVerbSpecific);
		panelSelfExplanations.setLayout(new BorderLayout(0, 0));
		panelSelfExplanations.add(panelVerbSpecific, BorderLayout.CENTER);

		JLabel lblReaderbench = new JLabel();
		lblReaderbench.setIcon(new ImageIcon("resources/config/RB_logo.png"));
		lblReaderbench.setForeground(Color.BLACK);
		lblReaderbench.setFont(new Font("Helvetica", Font.BOLD, 40));

		GroupLayout gl_desktopPane = new GroupLayout(desktopPane);
		gl_desktopPane.setHorizontalGroup(gl_desktopPane.createParallelGroup(
				Alignment.LEADING).addGroup(
				gl_desktopPane
						.createSequentialGroup()
						.addContainerGap()
						.addGroup(
								gl_desktopPane
										.createParallelGroup(Alignment.LEADING)
										.addComponent(tabbedPane,
												GroupLayout.DEFAULT_SIZE, 1168,
												Short.MAX_VALUE)
										.addComponent(lblReaderbench,
												Alignment.TRAILING))
						.addContainerGap()));
		gl_desktopPane.setVerticalGroup(gl_desktopPane.createParallelGroup(
				Alignment.LEADING).addGroup(
				gl_desktopPane
						.createSequentialGroup()
						.addContainerGap()
						.addComponent(tabbedPane, GroupLayout.PREFERRED_SIZE,
								125, GroupLayout.PREFERRED_SIZE)
						.addPreferredGap(ComponentPlacement.RELATED, 477,
								Short.MAX_VALUE).addComponent(lblReaderbench)
						.addContainerGap()));

		JPanel panelAdditional = new JPanel();
		panelAdditional.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null,
				null));
		tabbedPane
				.addTab(ResourceBundle
						.getBundle("utils.localization.messages").getString("ReaderBenchView.panelAdditional.title"), //$NON-NLS-1$ //$NON-NLS-2$
						null, panelAdditional, null);
		panelAdditional.setLayout(new BorderLayout(0, 0));

		JPanel panelSettingsGeneral = new JPanel();
		panelSettingsGeneral.setBorder(new TitledBorder(new EtchedBorder(
				EtchedBorder.LOWERED, null, null), "General",
				TitledBorder.LEADING, TitledBorder.TOP, null, null));
		panelSettingsGeneral.setBackground(Color.WHITE);
		panelAdditional.add(panelSettingsGeneral, BorderLayout.WEST);

		JButton btnTrainSemanticModels = new JButton(ResourceBundle.getBundle(
				"utils.localization.messages") //$NON-NLS-1$
				.getString("ReaderBenchView.btnTrainSemanticModels.text")); //$NON-NLS-1$
		btnTrainSemanticModels.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
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
		});
		gl_panelSettingsGeneral = new GroupLayout(panelSettingsGeneral);
		gl_panelSettingsGeneral.setHorizontalGroup(gl_panelSettingsGeneral
				.createParallelGroup(Alignment.LEADING)
				.addGap(0, 200, Short.MAX_VALUE)
				.addGroup(
						gl_panelSettingsGeneral
								.createSequentialGroup()
								.addContainerGap()
								.addComponent(btnTrainSemanticModels,
										GroupLayout.DEFAULT_SIZE, 200,
										Short.MAX_VALUE).addContainerGap()));
		gl_panelSettingsGeneral.setVerticalGroup(gl_panelSettingsGeneral
				.createParallelGroup(Alignment.LEADING)
				.addGap(0, 92, Short.MAX_VALUE)
				.addGroup(
						gl_panelSettingsGeneral
								.createSequentialGroup()
								.addComponent(btnTrainSemanticModels,
										GroupLayout.PREFERRED_SIZE, 27,
										GroupLayout.PREFERRED_SIZE)
								.addContainerGap(42, Short.MAX_VALUE)));
		panelSettingsGeneral.setLayout(gl_panelSettingsGeneral);

		JPanel panelSettingsSpecific = new JPanel();
		panelSettingsSpecific.setBorder(new TitledBorder(new EtchedBorder(
				EtchedBorder.LOWERED, null, null), "Specific",
				TitledBorder.LEADING, TitledBorder.TOP, null, null));
		panelSettingsSpecific.setBackground(Color.WHITE);
		panelAdditional.add(panelSettingsSpecific, BorderLayout.CENTER);
		gl_panelSettingsSpecific = new GroupLayout(panelSettingsSpecific);
		gl_panelSettingsSpecific.setHorizontalGroup(gl_panelSettingsSpecific
				.createParallelGroup(Alignment.LEADING)
				.addGap(0, 940, Short.MAX_VALUE)
				.addGap(0, 928, Short.MAX_VALUE));
		gl_panelSettingsSpecific.setVerticalGroup(gl_panelSettingsSpecific
				.createParallelGroup(Alignment.LEADING)
				.addGap(0, 92, Short.MAX_VALUE).addGap(0, 69, Short.MAX_VALUE));
		panelSettingsSpecific.setLayout(gl_panelSettingsSpecific);
		desktopPane.setLayout(gl_desktopPane);

		menuBar = new JMenuBar();
		setJMenuBar(menuBar);

		mnFile = new JMenu(
				ResourceBundle
						.getBundle("utils.localization.messages").getString("ReaderBenchView.mnFile.text")); //$NON-NLS-1$
		menuBar.add(mnFile);

		String quitText = ResourceBundle.getBundle(
				"utils.localization.messages").getString(
				"ReaderBenchView.mntmQuit.text");
		mntmQuit = new JMenuItem(quitText, quitText.charAt(0));
		mntmQuit.setAccelerator(KeyStroke.getKeyStroke("control "
				+ quitText.charAt(0)));
		mntmQuit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				System.exit(0);
			}
		});
		mnFile.add(mntmQuit);

		mnOptions = new JMenu(
				ResourceBundle
						.getBundle("utils.localization.messages").getString("ReaderBenchView.mnOptions.text")); //$NON-NLS-1$
		menuBar.add(mnOptions);

		mnLanguage = new JMenu(
				ResourceBundle
						.getBundle("utils.localization.messages").getString("ReaderBenchView.mnLanguage.text")); //$NON-NLS-1$
		mnOptions.add(mnLanguage);

		// ButtonGroup for radio buttons
		ButtonGroup languageGroup = new ButtonGroup();

		rdbtnmntmEnglish = new JRadioButtonMenuItem(
				ResourceBundle
						.getBundle("utils.localization.messages").getString("ReaderBenchView.rdbtnmntmEnglish.text"), //$NON-NLS-1$
				true);
		rdbtnmntmEnglish.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (LOADED_LOCALE != Locale.ENGLISH) {
					updateLocale(Locale.ENGLISH);
				}
				LocalizationUtils.saveLocaleInBundle(Locale.ENGLISH);
			}
		});
		mnLanguage.add(rdbtnmntmEnglish);
		languageGroup.add(rdbtnmntmEnglish);

		rdbtnmntmFrench = new JRadioButtonMenuItem(
				ResourceBundle
						.getBundle("utils.localization.messages").getString("ReaderBenchView.rdbtnmntmFrench.text")); //$NON-NLS-1$
		rdbtnmntmFrench.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (LOADED_LOCALE != Locale.FRENCH) {
					updateLocale(Locale.FRENCH);
				}
				LocalizationUtils.saveLocaleInBundle(Locale.FRENCH);
			}
		});
		mnLanguage.add(rdbtnmntmFrench);
		languageGroup.add(rdbtnmntmFrench);

		updateNames();
	}

	public void updateNames() {
		this.mnFile.setText(LocalizationUtils.getLocalizedString(
				this.getClass(), LocalizationUtils.TEXT, "mnFile"));
		this.mntmQuit.setText(LocalizationUtils.getLocalizedString(
				this.getClass(), LocalizationUtils.TEXT, "mntmQuit"));
		this.mnOptions.setText(LocalizationUtils.getLocalizedString(
				this.getClass(), LocalizationUtils.TEXT, "mnOptions"));
		this.mnLanguage.setText(LocalizationUtils.getLocalizedString(
				this.getClass(), LocalizationUtils.TEXT, "mnLanguage"));
		this.rdbtnmntmEnglish.setText(LocalizationUtils.getLocalizedString(
				this.getClass(), LocalizationUtils.TEXT, "rdbtnmntmEnglish"));
		this.rdbtnmntmFrench.setText(LocalizationUtils.getLocalizedString(
				this.getClass(), LocalizationUtils.TEXT, "rdbtnmntmFrench"));

		// data input panel

		this.tabbedPane.setTitleAt(0, LocalizationUtils.getLocalizedString(
				this.getClass(), LocalizationUtils.TITLE, "panelDataInput"));
		this.btnCreateDocument.setText(LocalizationUtils.getLocalizedString(
				this.getClass(), LocalizationUtils.TEXT, "btnCreateDocument"));
		this.btnCreateVerbalization.setText(LocalizationUtils
				.getLocalizedString(this.getClass(), LocalizationUtils.TEXT,
						"btnCreateVerbalization"));
		this.btnAnnotateVerbalization.setText(LocalizationUtils
				.getLocalizedString(this.getClass(), LocalizationUtils.TEXT,
						"btnAnnotateVerbalization"));

		// document analysis
		this.tabbedPane
				.setTitleAt(1, LocalizationUtils.getLocalizedString(
						this.getClass(), LocalizationUtils.TITLE,
						"panelPreProcessing"));
		this.btnPreprocessingTrainSemanticModels.setText(LocalizationUtils
				.getLocalizedString(this.getClass(), LocalizationUtils.TEXT,
						"btnPreprocessingTrainSemanticModels"));
		this.btnRunTextualComplexityIndices.setText(LocalizationUtils
				.getLocalizedString(this.getClass(), LocalizationUtils.TEXT,
						"btnRunTextualComplexityIndices"));

		// panelDocument
		this.tabbedPane
		.setTitleAt(2, LocalizationUtils.getLocalizedString(
				this.getClass(), LocalizationUtils.TITLE,
				"panelDocument"));
		this.btnDocProcessing.setText(LocalizationUtils
				.getLocalizedString(this.getClass(), LocalizationUtils.TEXT,
						"btnDocProcessing"));
		this.btnDocumentSemanticSearch.setText(LocalizationUtils
				.getLocalizedString(this.getClass(), LocalizationUtils.TEXT,
						"btnDocumentSemanticSearch"));
		this.btnPredictTextualComplexity.setText(LocalizationUtils
				.getLocalizedString(this.getClass(), LocalizationUtils.TEXT,
						"btnPredictTextualComplexity"));
		
		// textual complexity
		this.tabbedPane
		.setTitleAt(3, LocalizationUtils.getLocalizedString(
				this.getClass(), LocalizationUtils.TITLE,
				"panelTextualComplexity"));
		this.btnCorpusEvaluation.setText(LocalizationUtils
				.getLocalizedString(this.getClass(), LocalizationUtils.TEXT,
						"btnCorpusEvaluation"));
		this.btnEssayProcessing.setText(LocalizationUtils
				.getLocalizedString(this.getClass(), LocalizationUtils.TEXT,
						"btnEssayProcessing"));
		
		// self explanations
		this.tabbedPane
		.setTitleAt(4, LocalizationUtils.getLocalizedString(
				this.getClass(), LocalizationUtils.TITLE,
				"panelSelfExplanations"));
		this.btnVerbaProcessing.setText(LocalizationUtils
				.getLocalizedString(this.getClass(), LocalizationUtils.TEXT,
						"btnVerbaProcessing"));
		this.btnSummaryProcessing.setText(LocalizationUtils
				.getLocalizedString(this.getClass(), LocalizationUtils.TEXT,
						"btnSummaryProcessing"));
	}

	public void updateLocale(Locale newLocale) {
		LOADED_LOCALE = newLocale;
		Locale.setDefault(LOADED_LOCALE);
		ResourceBundle.clearCache();
		updateNames();
		ReaderBenchView.this.revalidate();
	}

	public static void main(String[] args) {
		BasicConfigurator.configure();
		Logger.getRootLogger().setLevel(Level.INFO); // changing log level

		Toolkit.getDefaultToolkit().getSystemEventQueue()
				.push(new TCPopupEventQueue());

		adjustToSystemGraphics();

		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				ReaderBenchView view = new ReaderBenchView();
				view.setIconImage(Toolkit.getDefaultToolkit().getImage(
						"resources/config/RB_logo.png"));
				view.setVisible(true);
			}
		});
	}

	public static void adjustToSystemGraphics() {
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
