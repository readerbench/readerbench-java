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
package view.widgets;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
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
import javax.swing.JComboBox;
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

import data.AbstractDocument;
import data.Lang;
import org.openide.util.Exceptions;
import utils.localization.LocalizationUtils;
import utils.settings.SettingsUtils;
import view.events.TCPopupEventQueue;
import view.widgets.complexity.CorpusEvaluationView;
import view.widgets.complexity.DocumentEvaluationView;
import view.widgets.complexity.EssayProcessingView;
import view.widgets.complexity.RunMeasurementsView;
import view.widgets.cscl.ConversationProcessingView;
import view.widgets.cscl.VCoPEvaluationView;
import view.widgets.cscl.VCoPView;
import view.widgets.document.DocumentManagementView;
import view.widgets.document.DocumentProcessingView;
import view.widgets.document.DocumentSemanticSearchView;
import view.widgets.selfexplanation.summary.SummaryProcessingView;
import view.widgets.selfexplanation.verbalization.AnnotateVerbalizationView;
import view.widgets.selfexplanation.verbalization.CreateVerbalizationView;
import view.widgets.selfexplanation.verbalization.VerbalizationProcessingView;
import view.widgets.semanticModels.SemanticModelsTraining;
import webService.ReaderBenchServer;

public class ReaderBenchView extends JFrame {

    private static final long serialVersionUID = 4565038532352428650L;
    public static Logger logger = Logger.getLogger(ReaderBenchView.class);
    public static Locale LOADED_LOCALE = LocalizationUtils.LOADED_LOCALE;

    public static final String[] TRAINED_LSA_SPACES_EN = {"resources/config/EN/LSA/TASA", "resources/config/EN/LSA/TASA_LAK", "resources/config/EN/LSA/COCA_newspaper", ""};
    public static final String[] TRAINED_LSA_SPACES_FR = {"resources/config/FR/LSA/Le_Monde", "resources/config/FR/LSA/Text_Enfants_Nursery", ""};
    public static final String[] TRAINED_LSA_SPACES_IT = {""};
    public static final String[] TRAINED_LSA_SPACES_ES = {"resources/config/ES/LSA/Jose_Antonio", ""};
    public static final String[] TRAINED_LSA_SPACES_LA = {"resources/config/LA/LSA/Letters", ""};
    public static final String[] TRAINED_LDA_MODELS_EN = {"resources/config/EN/LDA/TASA", "resources/config/EN/LDA/TASA_LAK", "resources/config/EN/LDA/TASA_smart_cities", "resources/config/EN/LDA/COCA_newspaper", ""};
    public static final String[] TRAINED_LDA_MODELS_FR = {"resources/config/FR/LDA/Le_Monde", "resources/config/FR/LDA/Text_Enfants", "resources/config/FR/LDA/Philosophy", ""};
    public static final String[] TRAINED_LDA_MODELS_IT = {"resources/config/IT/LDA/Paisa", ""};
    public static final String[] TRAINED_LDA_MODELS_ES = {"resources/config/ES/LDA/Jose_Antonio", ""};
    public static final String[] TRAINED_LDA_MODELS_LA = {"resources/config/LA/LDA/Letters", ""};

    public static Lang RUNTIME_LANGUAGE = Lang.eng;

    private final JMenuBar menuBar;
    // File menu item
    private final JMenu mnFile;
    private final JMenuItem mntmQuit;
    // Options menu item
    private final JMenu mnOptions;
    private final JMenu mnLanguage;
    private final JRadioButtonMenuItem rdbtnmntmEnglish;
    private final JRadioButtonMenuItem rdbtnmntmFrench;

    // tabbed panel
    private final JTabbedPane tabbedPane;
    // data input panel
    private final JPanel panelDataInput;
    private final JButton btnCreateDocument;
    private final JButton btnCreateVerbalization;
    private final JButton btnAnnotateVerbalization;

    // pre-processing
    private final JButton btnPreprocessingTrainSemanticModels;
    private final JButton btnRunTextualComplexityIndices;

    // document analysis
    private final JButton btnDocProcessing;
    private final JButton btnDocumentSemanticSearch;
    private final JButton btnPredictTextualComplexity;

    // textual complexity
    private final JButton btnCorpusEvaluation;
    private final JButton btnEssayProcessing;

    // self-explanations
    private final JButton btnVerbaProcessing;
    private final JButton btnSummaryProcessing;

    // cscl
    private final JButton btnVCoPProcessing;

    private JDesktopPane desktopPane;

    private final GroupLayout gl_panelSettingsSpecific;
    private final JButton btnVcopprocessingevaluation;
    private final JButton btnConversationProcessing;

    public ReaderBenchView() {
        super.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        super.getContentPane().setBackground(Color.WHITE);
        super.getContentPane().setLayout(new BorderLayout(0, 0));
        super.setTitle("ReaderBench " + SettingsUtils.getReaderBenchVersion());
        super.setIconImage(Toolkit.getDefaultToolkit().getImage("resources/config/Logos/reader_bench_icon.png"));
        Locale.setDefault(LOADED_LOCALE);

        // adjust view to desktop size
        super.setBounds(50, 50, 1180, 750);

        desktopPane = new JDesktopPane() {
            private static final long serialVersionUID = 8453433109734630086L;

            @Override
            public void updateUI() {
                if ("Nimbus".equals(UIManager.getLookAndFeel().getName())) {
                    UIDefaults map = new UIDefaults();
                    Painter<JComponent> painter = (Graphics2D g, JComponent c, int w, int h) -> {
                        g.setColor(Color.WHITE);
                        g.fillRect(0, 0, w, h);
                    };
                    map.put("DesktopPane[Enabled].backgroundPainter", painter);
                    putClientProperty("Nimbus.Overrides", map);
                }
                super.updateUI();
            }
        };
        desktopPane.setBackground(Color.WHITE);
        super.getContentPane().add(desktopPane, BorderLayout.CENTER);

        tabbedPane = new JTabbedPane(JTabbedPane.TOP);

        panelDataInput = new JPanel();
        panelDataInput.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
        panelDataInput.setBackground(Color.WHITE);
        tabbedPane.addTab(
                ResourceBundle.getBundle("utils.localization.messages") //$NON-NLS-1$
                .getString("ReaderBenchView.panelDataInput.title"), //$NON-NLS-1$
                null, panelDataInput, null);

        btnCreateDocument = new JButton(ResourceBundle.getBundle("utils.localization.messages") //$NON-NLS-1$
                .getString("ReaderBenchView.btnCreateDocument.text")); //$NON-NLS-1$
        btnCreateDocument.addActionListener((ActionEvent arg0) -> {
            DocumentManagementView view = new DocumentManagementView();
            view.setVisible(true);
        });

        btnCreateVerbalization = new JButton(ResourceBundle.getBundle("utils.localization.messages") //$NON-NLS-1$
                .getString("ReaderBenchView.btnCreateVerbalization.text")); //$NON-NLS-1$
        btnCreateVerbalization.addActionListener((ActionEvent e) -> {
            CreateVerbalizationView view = new CreateVerbalizationView();
            view.setVisible(true);
        });

        btnAnnotateVerbalization = new JButton(ResourceBundle.getBundle("utils.localization.messages") //$NON-NLS-1$
                .getString("ReaderBenchView.btnAnnotateVerbalization.text")); //$NON-NLS-1$
        btnAnnotateVerbalization.addActionListener((ActionEvent e) -> {
            AnnotateVerbalizationView view = new AnnotateVerbalizationView();
            view.setVisible(true);
        });
        GroupLayout gl_panelDataInput = new GroupLayout(panelDataInput);
        gl_panelDataInput
                .setHorizontalGroup(gl_panelDataInput.createParallelGroup(Alignment.LEADING)
                        .addGroup(gl_panelDataInput.createSequentialGroup().addContainerGap()
                                .addComponent(btnCreateDocument, GroupLayout.PREFERRED_SIZE, 220,
                                        GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(ComponentPlacement.UNRELATED)
                                .addComponent(btnCreateVerbalization, GroupLayout.PREFERRED_SIZE, 220,
                                        GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(ComponentPlacement.UNRELATED).addComponent(btnAnnotateVerbalization,
                                GroupLayout.PREFERRED_SIZE, 220, GroupLayout.PREFERRED_SIZE)
                                .addContainerGap(690, Short.MAX_VALUE)));
        gl_panelDataInput.setVerticalGroup(gl_panelDataInput.createParallelGroup(Alignment.LEADING)
                .addGroup(gl_panelDataInput.createSequentialGroup().addContainerGap()
                        .addGroup(gl_panelDataInput.createParallelGroup(Alignment.BASELINE)
                                .addComponent(btnCreateDocument).addComponent(btnAnnotateVerbalization)
                                .addComponent(btnCreateVerbalization))
                        .addContainerGap(53, Short.MAX_VALUE)));
        panelDataInput.setLayout(gl_panelDataInput);

        JPanel panelPreProcessing = new JPanel();
        panelPreProcessing.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
        panelPreProcessing.setBackground(Color.WHITE);
        tabbedPane.addTab(
                LocalizationUtils.getLocalizedString(this.getClass(), LocalizationUtils.TITLE, "panelPreProcessing"),
                null, panelPreProcessing, null);

        btnPreprocessingTrainSemanticModels = new JButton(ResourceBundle.getBundle("utils.localization.messages") //$NON-NLS-1$
                .getString("ReaderBenchView.btnPreprocessingTrainSemanticModels.text")); //$NON-NLS-1$
        btnPreprocessingTrainSemanticModels.addActionListener((ActionEvent e) -> {
            EventQueue.invokeLater(() -> {
                try {
                    SemanticModelsTraining frame = new SemanticModelsTraining();
                    frame.setVisible(true);
                } catch (Exception ex) {
                    Exceptions.printStackTrace(ex);
                }
            });
        });

        btnRunTextualComplexityIndices = new JButton(ResourceBundle.getBundle("utils.localization.messages") //$NON-NLS-1$
                .getString("ReaderBenchView.btnRunTextualComplexityIndices.text")); //$NON-NLS-1$
        btnRunTextualComplexityIndices.addActionListener((ActionEvent e) -> {
            RunMeasurementsView frame = new RunMeasurementsView();
            frame.setVisible(true);
        });
        GroupLayout gl_panelPreProcessing = new GroupLayout(panelPreProcessing);
        gl_panelPreProcessing.setHorizontalGroup(gl_panelPreProcessing.createParallelGroup(Alignment.LEADING)
                .addGroup(gl_panelPreProcessing.createSequentialGroup().addContainerGap()
                        .addComponent(btnPreprocessingTrainSemanticModels, GroupLayout.PREFERRED_SIZE, 220,
                                GroupLayout.PREFERRED_SIZE)
                        .addGap(18).addComponent(btnRunTextualComplexityIndices, GroupLayout.PREFERRED_SIZE, 220,
                        GroupLayout.PREFERRED_SIZE)
                        .addContainerGap(874, Short.MAX_VALUE)));
        gl_panelPreProcessing.setVerticalGroup(gl_panelPreProcessing.createParallelGroup(Alignment.LEADING)
                .addGroup(gl_panelPreProcessing.createSequentialGroup().addContainerGap()
                        .addGroup(gl_panelPreProcessing.createParallelGroup(Alignment.BASELINE)
                                .addComponent(btnPreprocessingTrainSemanticModels, GroupLayout.PREFERRED_SIZE, 25,
                                        GroupLayout.PREFERRED_SIZE)
                                .addComponent(btnRunTextualComplexityIndices))
                        .addContainerGap(53, Short.MAX_VALUE)));
        panelPreProcessing.setLayout(gl_panelPreProcessing);

        JPanel panelDocument = new JPanel();
        // tabbedPane.addTab("New tab", null, panelDocSpecific, null);
        tabbedPane.addTab(
                LocalizationUtils.getLocalizedString(this.getClass(), LocalizationUtils.TITLE, "panelDocument"), //$NON-NLS-1$ //$NON-NLS-2$
                null, panelDocument, null);

        panelDocument.setBackground(Color.WHITE);
        panelDocument.setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null), "",
                TitledBorder.LEADING, TitledBorder.TOP, null, null));

        btnPredictTextualComplexity = new JButton(
                LocalizationUtils.getLocalizedString(this.getClass(), LocalizationUtils.TEXT, "btnTrainSVMDocs")); //$NON-NLS-1$ //$NON-NLS-2$
        btnPredictTextualComplexity.addActionListener((ActionEvent e) -> {
            if (DocumentProcessingView.getLoadedDocuments().size() > 0) {
                List<AbstractDocument> abstractDocs = new LinkedList<>();
                DocumentProcessingView.getLoadedDocuments().stream().forEach((d) -> {
                    abstractDocs.add(d);
                });
                DocumentEvaluationView view = new DocumentEvaluationView(abstractDocs);
                view.setVisible(true);
            } else {
                JOptionPane.showMessageDialog(desktopPane,
                        "At least one document must be loaded for evaluating its textual complexity!",
                        "Information", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        btnDocProcessing = new JButton();
        btnDocProcessing.addActionListener((ActionEvent e) -> {
            DocumentProcessingView frame = new DocumentProcessingView();
            frame.setVisible(true);
            desktopPane.add(frame);
            try {
                frame.setSelected(true);
            } catch (PropertyVetoException ex) {
                Exceptions.printStackTrace(ex);
            }
        });

        btnDocumentSemanticSearch = new JButton(ResourceBundle.getBundle("utils.localization.messages") //$NON-NLS-1$
                .getString("ReaderBenchView.btnDocumentSemanticSearch.text")); //$NON-NLS-1$
        btnDocumentSemanticSearch.addActionListener((ActionEvent e) -> {
            DocumentSemanticSearchView frame = new DocumentSemanticSearchView();
            frame.setVisible(true);
            desktopPane.add(frame);
            try {
                frame.setSelected(true);
            } catch (PropertyVetoException ex) {
                Exceptions.printStackTrace(ex);
            }
        });

        GroupLayout gl_panelDocSpecific = new GroupLayout(panelDocument);
        gl_panelDocSpecific
                .setHorizontalGroup(gl_panelDocSpecific.createParallelGroup(Alignment.LEADING)
                        .addGroup(gl_panelDocSpecific.createSequentialGroup().addContainerGap()
                                .addComponent(btnDocProcessing, GroupLayout.PREFERRED_SIZE, 220,
                                        GroupLayout.PREFERRED_SIZE)
                                .addGap(18)
                                .addComponent(btnDocumentSemanticSearch, GroupLayout.PREFERRED_SIZE, 220,
                                        GroupLayout.PREFERRED_SIZE)
                                .addGap(18).addComponent(btnPredictTextualComplexity, GroupLayout.PREFERRED_SIZE, 220,
                                GroupLayout.PREFERRED_SIZE)
                                .addContainerGap(669, Short.MAX_VALUE)));
        gl_panelDocSpecific.setVerticalGroup(gl_panelDocSpecific.createParallelGroup(Alignment.LEADING)
                .addGroup(gl_panelDocSpecific.createSequentialGroup().addContainerGap()
                        .addGroup(gl_panelDocSpecific.createParallelGroup(Alignment.BASELINE)
                                .addComponent(btnDocProcessing).addComponent(btnDocumentSemanticSearch)
                                .addComponent(btnPredictTextualComplexity))
                        .addContainerGap(49, Short.MAX_VALUE)));
        panelDocument.setLayout(gl_panelDocSpecific);

        JPanel panelTextualComplexity = new JPanel();
        tabbedPane.addTab("New tab", null, panelTextualComplexity, null);
        panelTextualComplexity.setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null), "",
                TitledBorder.LEADING, TitledBorder.TOP, null, null));
        panelTextualComplexity.setBackground(Color.WHITE);

        btnCorpusEvaluation = new JButton(ResourceBundle.getBundle("utils.localization.messages") //$NON-NLS-1$
                .getString("ReaderBenchView.btnCorpusEvaluation.text")); //$NON-NLS-1$
        btnCorpusEvaluation.addActionListener((ActionEvent e) -> {
            CorpusEvaluationView frame = new CorpusEvaluationView();
            frame.setVisible(true);
        });

        btnEssayProcessing = new JButton(ResourceBundle.getBundle("utils.localization.messages") //$NON-NLS-1$
                .getString("ReaderBenchView.btnEssayProcessing.text")); //$NON-NLS-1$
        btnEssayProcessing.addActionListener((ActionEvent e) -> {
            EssayProcessingView frame = new EssayProcessingView();
            frame.setVisible(true);
        });
        GroupLayout gl_panel = new GroupLayout(panelTextualComplexity);
        gl_panel.setHorizontalGroup(gl_panel.createParallelGroup(Alignment.LEADING)
                .addGroup(gl_panel.createSequentialGroup().addContainerGap()
                        .addComponent(btnCorpusEvaluation, GroupLayout.PREFERRED_SIZE, 220, GroupLayout.PREFERRED_SIZE)
                        .addGap(18)
                        .addComponent(btnEssayProcessing, GroupLayout.PREFERRED_SIZE, 220, GroupLayout.PREFERRED_SIZE)
                        .addContainerGap(874, Short.MAX_VALUE)));
        gl_panel.setVerticalGroup(gl_panel.createParallelGroup(Alignment.LEADING)
                .addGroup(gl_panel.createSequentialGroup().addContainerGap()
                        .addGroup(gl_panel.createParallelGroup(Alignment.BASELINE)
                                .addComponent(btnCorpusEvaluation, GroupLayout.PREFERRED_SIZE, 25,
                                        GroupLayout.PREFERRED_SIZE)
                                .addComponent(btnEssayProcessing))
                        .addContainerGap(53, Short.MAX_VALUE)));
        panelTextualComplexity.setLayout(gl_panel);

        JPanel panelSelfExplanations = new JPanel();
        tabbedPane.addTab("New tab", null, panelSelfExplanations, null);
        panelSelfExplanations.setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null), "",
                TitledBorder.LEADING, TitledBorder.TOP, null, null));
        panelSelfExplanations.setBackground(Color.WHITE);

        btnVerbaProcessing = new JButton(ResourceBundle.getBundle("utils.localization.messages") //$NON-NLS-1$
                .getString("ReaderBenchView.btnVerbaProcessing.text")); //$NON-NLS-1$
        btnVerbaProcessing.addActionListener((ActionEvent e) -> {
            VerbalizationProcessingView frame = new VerbalizationProcessingView();
            frame.setVisible(true);
            desktopPane.add(frame);
            try {
                frame.setSelected(true);
            } catch (PropertyVetoException exception) {
                Exceptions.printStackTrace(exception);
            }
        });

        btnSummaryProcessing = new JButton(ResourceBundle.getBundle("utils.localization.messages") //$NON-NLS-1$
                .getString("ReaderBenchView.btnSummaryProcessing.text")); //$NON-NLS-1$
        btnSummaryProcessing.addActionListener((ActionEvent e) -> {
            SummaryProcessingView frame = new SummaryProcessingView();
            frame.setVisible(true);
            desktopPane.add(frame);
            try {
                frame.setSelected(true);
            } catch (PropertyVetoException exception) {
                Exceptions.printStackTrace(exception);
            }
        });
        GroupLayout gl_panelVerbSpecific = new GroupLayout(panelSelfExplanations);
        gl_panelVerbSpecific.setHorizontalGroup(gl_panelVerbSpecific.createParallelGroup(Alignment.LEADING)
                .addGroup(gl_panelVerbSpecific.createSequentialGroup().addContainerGap()
                        .addComponent(btnVerbaProcessing, GroupLayout.PREFERRED_SIZE, 220, GroupLayout.PREFERRED_SIZE)
                        .addGap(18)
                        .addComponent(btnSummaryProcessing, GroupLayout.PREFERRED_SIZE, 220, GroupLayout.PREFERRED_SIZE)
                        .addContainerGap(902, Short.MAX_VALUE)));
        gl_panelVerbSpecific.setVerticalGroup(gl_panelVerbSpecific.createParallelGroup(Alignment.LEADING)
                .addGroup(gl_panelVerbSpecific.createSequentialGroup().addContainerGap()
                        .addGroup(gl_panelVerbSpecific.createParallelGroup(Alignment.BASELINE)
                                .addComponent(btnVerbaProcessing).addComponent(btnSummaryProcessing))
                        .addContainerGap(53, Short.MAX_VALUE)));
        panelSelfExplanations.setLayout(gl_panelVerbSpecific);

        JLabel lblReaderbench = new JLabel();
        lblReaderbench.setIcon(new ImageIcon("resources/config/Logos/reader_bench_logo_transparent_256.png"));
        lblReaderbench.setForeground(Color.BLACK);
        lblReaderbench.setFont(new Font("Helvetica", Font.BOLD, 40));

        GroupLayout gl_desktopPane = new GroupLayout(desktopPane);
        gl_desktopPane
                .setHorizontalGroup(gl_desktopPane.createParallelGroup(Alignment.LEADING).addGroup(Alignment.TRAILING,
                        gl_desktopPane.createSequentialGroup().addContainerGap()
                        .addGroup(gl_desktopPane.createParallelGroup(Alignment.TRAILING)
                                .addComponent(lblReaderbench, GroupLayout.PREFERRED_SIZE, 256,
                                        GroupLayout.PREFERRED_SIZE)
                                .addComponent(tabbedPane, GroupLayout.DEFAULT_SIZE, 1168, Short.MAX_VALUE))
                        .addContainerGap()));
        gl_desktopPane.setVerticalGroup(gl_desktopPane.createParallelGroup(Alignment.LEADING)
                .addGroup(gl_desktopPane.createSequentialGroup().addContainerGap()
                        .addComponent(tabbedPane, GroupLayout.PREFERRED_SIZE, 125, GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(ComponentPlacement.RELATED, 302, Short.MAX_VALUE)
                        .addComponent(lblReaderbench, GroupLayout.PREFERRED_SIZE, 266, GroupLayout.PREFERRED_SIZE)
                        .addContainerGap()));

        JPanel panelCSCL = new JPanel();
        tabbedPane.addTab("New tab", null, panelCSCL, null);
        panelCSCL.setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null), "",
                TitledBorder.LEADING, TitledBorder.TOP, null, null));
        panelCSCL.setBackground(Color.WHITE);

        btnVCoPProcessing = new JButton(ResourceBundle.getBundle("utils.localization.messages") //$NON-NLS-1$
                .getString("ReaderBenchView.btnVCoPProcessing.text")); //$NON-NLS-1$
        btnVCoPProcessing.addActionListener((ActionEvent e) -> {
            VCoPView frame = new VCoPView();
            frame.setVisible(true);
        });

        btnVcopprocessingevaluation = new JButton(ResourceBundle.getBundle("utils.localization.messages") //$NON-NLS-1$
                .getString("ReaderBenchView.btnVcopprocessingevaluation.text")); //$NON-NLS-1$
        btnVcopprocessingevaluation.addActionListener((ActionEvent e) -> {
            VCoPEvaluationView frame = new VCoPEvaluationView();
            frame.setVisible(true);
        });

        btnConversationProcessing = new JButton(ResourceBundle.getBundle("utils.localization.messages") //$NON-NLS-1$
                .getString("ReaderBenchView.btnConversationProcessing.text")); //$NON-NLS-1$
        btnConversationProcessing.addActionListener((ActionEvent e) -> {
            ConversationProcessingView frame = new ConversationProcessingView();
            frame.setVisible(true);
            desktopPane.add(frame);
            try {
                frame.setSelected(true);
            } catch (PropertyVetoException exception) {
                Exceptions.printStackTrace(exception);
            }
        });
        gl_panelSettingsSpecific = new GroupLayout(panelCSCL);
        gl_panelSettingsSpecific.setHorizontalGroup(gl_panelSettingsSpecific.createParallelGroup(Alignment.LEADING)
                .addGroup(gl_panelSettingsSpecific.createSequentialGroup().addContainerGap()
                        .addComponent(btnConversationProcessing, GroupLayout.PREFERRED_SIZE, 220,
                                GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(ComponentPlacement.UNRELATED)
                        .addComponent(btnVcopprocessingevaluation, GroupLayout.PREFERRED_SIZE, 220,
                                GroupLayout.PREFERRED_SIZE)
                        .addGap(18)
                        .addComponent(btnVCoPProcessing, GroupLayout.PREFERRED_SIZE, 220, GroupLayout.PREFERRED_SIZE)
                        .addContainerGap(863, Short.MAX_VALUE)));
        gl_panelSettingsSpecific.setVerticalGroup(gl_panelSettingsSpecific.createParallelGroup(Alignment.LEADING)
                .addGroup(gl_panelSettingsSpecific.createSequentialGroup().addContainerGap()
                        .addGroup(gl_panelSettingsSpecific.createParallelGroup(Alignment.BASELINE)
                                .addComponent(btnConversationProcessing).addComponent(btnVcopprocessingevaluation)
                                .addComponent(btnVCoPProcessing))
                        .addContainerGap(49, Short.MAX_VALUE)));
        panelCSCL.setLayout(gl_panelSettingsSpecific);
        desktopPane.setLayout(gl_desktopPane);

        menuBar = new JMenuBar();
        super.setJMenuBar(menuBar);

        mnFile = new JMenu(
                ResourceBundle.getBundle("utils.localization.messages").getString("ReaderBenchView.mnFile.text")); //$NON-NLS-1$
        menuBar.add(mnFile);

        String quitText = ResourceBundle.getBundle("utils.localization.messages")
                .getString("ReaderBenchView.mntmQuit.text");
        mntmQuit = new JMenuItem(quitText, quitText.charAt(0));
        mntmQuit.setAccelerator(KeyStroke.getKeyStroke("control " + quitText.charAt(0)));
        mntmQuit.addActionListener((ActionEvent e) -> {
            System.exit(0);
        });
        mnFile.add(mntmQuit);

        mnOptions = new JMenu(
                ResourceBundle.getBundle("utils.localization.messages").getString("ReaderBenchView.mnOptions.text")); //$NON-NLS-1$
        menuBar.add(mnOptions);

        mnLanguage = new JMenu(
                ResourceBundle.getBundle("utils.localization.messages").getString("ReaderBenchView.mnLanguage.text")); //$NON-NLS-1$
        mnOptions.add(mnLanguage);

        // ButtonGroup for radio buttons
        ButtonGroup languageGroup = new ButtonGroup();

        rdbtnmntmEnglish = new JRadioButtonMenuItem(ResourceBundle.getBundle("utils.localization.messages") //$NON-NLS-1$
                .getString("ReaderBenchView.rdbtnmntmEnglish.text"), true);
        rdbtnmntmEnglish.addActionListener((ActionEvent e) -> {
            if (LOADED_LOCALE != Locale.ENGLISH) {
                updateLocale(Locale.ENGLISH);
            }
            LocalizationUtils.saveLocaleInBundle(Locale.ENGLISH);
        });
        mnLanguage.add(rdbtnmntmEnglish);
        languageGroup.add(rdbtnmntmEnglish);

        rdbtnmntmFrench = new JRadioButtonMenuItem(ResourceBundle.getBundle("utils.localization.messages") //$NON-NLS-1$
                .getString("ReaderBenchView.rdbtnmntmFrench.text"));
        rdbtnmntmFrench.addActionListener((ActionEvent e) -> {
            if (LOADED_LOCALE != Locale.FRENCH) {
                updateLocale(Locale.FRENCH);
            }
            LocalizationUtils.saveLocaleInBundle(Locale.FRENCH);
        });
        mnLanguage.add(rdbtnmntmFrench);
        languageGroup.add(rdbtnmntmFrench);

        updateNames();
    }

    private void updateNames() {
        this.mnFile.setText(LocalizationUtils.getLocalizedString(this.getClass(), LocalizationUtils.TEXT, "mnFile"));
        this.mntmQuit
                .setText(LocalizationUtils.getLocalizedString(this.getClass(), LocalizationUtils.TEXT, "mntmQuit"));
        this.mnOptions
                .setText(LocalizationUtils.getLocalizedString(this.getClass(), LocalizationUtils.TEXT, "mnOptions"));
        this.mnLanguage
                .setText(LocalizationUtils.getLocalizedString(this.getClass(), LocalizationUtils.TEXT, "mnLanguage"));
        this.rdbtnmntmEnglish.setText(
                LocalizationUtils.getLocalizedString(this.getClass(), LocalizationUtils.TEXT, "rdbtnmntmEnglish"));
        this.rdbtnmntmFrench.setText(
                LocalizationUtils.getLocalizedString(this.getClass(), LocalizationUtils.TEXT, "rdbtnmntmFrench"));

        // data input panel
        this.tabbedPane.setTitleAt(0,
                LocalizationUtils.getLocalizedString(this.getClass(), LocalizationUtils.TITLE, "panelDataInput"));
        this.btnCreateDocument.setText(
                LocalizationUtils.getLocalizedString(this.getClass(), LocalizationUtils.TEXT, "btnCreateDocument"));
        this.btnCreateVerbalization.setText(LocalizationUtils.getLocalizedString(this.getClass(),
                LocalizationUtils.TEXT, "btnCreateVerbalization"));
        this.btnAnnotateVerbalization.setText(LocalizationUtils.getLocalizedString(this.getClass(),
                LocalizationUtils.TEXT, "btnAnnotateVerbalization"));

        // document analysis
        this.tabbedPane.setTitleAt(1,
                LocalizationUtils.getLocalizedString(this.getClass(), LocalizationUtils.TITLE, "panelPreProcessing"));
        this.btnPreprocessingTrainSemanticModels.setText(LocalizationUtils.getLocalizedString(this.getClass(),
                LocalizationUtils.TEXT, "btnPreprocessingTrainSemanticModels"));
        this.btnRunTextualComplexityIndices.setText(LocalizationUtils.getLocalizedString(this.getClass(),
                LocalizationUtils.TEXT, "btnRunTextualComplexityIndices"));
        this.btnDocProcessing.setText(
                LocalizationUtils.getLocalizedString(this.getClass(), LocalizationUtils.TEXT, "btnDocProcessing"));
        this.btnDocumentSemanticSearch.setText(LocalizationUtils.getLocalizedString(this.getClass(),
                LocalizationUtils.TEXT, "btnDocumentSemanticSearch"));

        this.tabbedPane.setTitleAt(3, LocalizationUtils.getLocalizedString(this.getClass(), LocalizationUtils.TITLE,
                "panelTextualComplexity"));
        this.btnPredictTextualComplexity.setText(LocalizationUtils.getLocalizedString(this.getClass(),
                LocalizationUtils.TEXT, "btnPredictTextualComplexity"));
        this.tabbedPane.setTitleAt(2,
                LocalizationUtils.getLocalizedString(this.getClass(), LocalizationUtils.TITLE, "panelDocument"));
        this.btnCorpusEvaluation.setText(
                LocalizationUtils.getLocalizedString(this.getClass(), LocalizationUtils.TEXT, "btnCorpusEvaluation"));
        this.btnEssayProcessing.setText(
                LocalizationUtils.getLocalizedString(this.getClass(), LocalizationUtils.TEXT, "btnEssayProcessing"));
        this.tabbedPane.setTitleAt(4, LocalizationUtils.getLocalizedString(this.getClass(), LocalizationUtils.TITLE,
                "panelSelfExplanations"));
        this.btnVerbaProcessing.setText(
                LocalizationUtils.getLocalizedString(this.getClass(), LocalizationUtils.TEXT, "btnVerbaProcessing"));
        this.btnSummaryProcessing.setText(
                LocalizationUtils.getLocalizedString(this.getClass(), LocalizationUtils.TEXT, "btnSummaryProcessing"));
        this.tabbedPane.setTitleAt(5,
                LocalizationUtils.getLocalizedString(this.getClass(), LocalizationUtils.TITLE, "panelCSCL"));
        this.btnVCoPProcessing.setText(
                LocalizationUtils.getLocalizedString(this.getClass(), LocalizationUtils.TEXT, "btnVCoPProcessing"));
        this.btnVcopprocessingevaluation.setText(LocalizationUtils.getLocalizedString(this.getClass(),
                LocalizationUtils.TEXT, "btnVcopprocessingevaluation"));
        this.btnConversationProcessing.setText(LocalizationUtils.getLocalizedString(this.getClass(),
                LocalizationUtils.TEXT, "btnConversationProcessing"));
    }

    public void updateLocale(Locale newLocale) {
        LOADED_LOCALE = newLocale;
        Locale.setDefault(LOADED_LOCALE);
        ResourceBundle.clearCache();
        updateNames();
        ReaderBenchView.this.revalidate();
    }

    public static void updateComboLanguage(JComboBox<String> comboBoxLSA, JComboBox<String> comboBoxLDA, Lang lang) {
        comboBoxLSA.removeAllItems();
        comboBoxLDA.removeAllItems();

        switch (lang) {
            case fr:
                for (String url : ReaderBenchView.TRAINED_LSA_SPACES_FR) {
                    comboBoxLSA.addItem(url);
                }
                for (String url : ReaderBenchView.TRAINED_LDA_MODELS_FR) {
                    comboBoxLDA.addItem(url);
                }
                break;
            case it:
                for (String url : ReaderBenchView.TRAINED_LSA_SPACES_IT) {
                    comboBoxLSA.addItem(url);
                }
                for (String url : ReaderBenchView.TRAINED_LDA_MODELS_IT) {
                    comboBoxLDA.addItem(url);
                }
                break;
            case es:
                for (String url : ReaderBenchView.TRAINED_LSA_SPACES_ES) {
                    comboBoxLSA.addItem(url);
                }
                for (String url : ReaderBenchView.TRAINED_LDA_MODELS_ES) {
                    comboBoxLDA.addItem(url);
                }
                break;
            case la:
                for (String url : ReaderBenchView.TRAINED_LSA_SPACES_LA) {
                    comboBoxLSA.addItem(url);
                }
                for (String url : ReaderBenchView.TRAINED_LDA_MODELS_LA) {
                    comboBoxLDA.addItem(url);
                }
                break;
            default:
                for (String url : ReaderBenchView.TRAINED_LSA_SPACES_EN) {
                    comboBoxLSA.addItem(url);
                }
                for (String url : ReaderBenchView.TRAINED_LDA_MODELS_EN) {
                    comboBoxLDA.addItem(url);
                }
                break;
        }

        comboBoxLSA.setEnabled(true);
        comboBoxLDA.setEnabled(true);
    }

    public static void main(String[] args) {
        BasicConfigurator.configure();
        Logger.getRootLogger().setLevel(Level.INFO); // changing log level

        Toolkit.getDefaultToolkit().getSystemEventQueue().push(new TCPopupEventQueue());

        ReaderBenchServer.initializeDB();

        adjustToSystemGraphics();

        EventQueue.invokeLater(() -> {
            ReaderBenchView view = new ReaderBenchView();
            view.setVisible(true);
        });
    }

    public static void adjustToSystemGraphics() {
        for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
            if ("Nimbus".equals(info.getName())) {
                try {
                    UIManager.setLookAndFeel(info.getClassName());
                } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
                    Exceptions.printStackTrace(e);
                }
            }
        }
    }
}
