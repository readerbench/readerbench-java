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

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDesktopPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.Painter;
import javax.swing.UIDefaults;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

import data.AbstractDocument;
import data.Lang;
import services.nlp.parsing.Parsing;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import org.openide.util.Exceptions;
import utils.localization.LocalizationUtils;
import utils.settings.SettingsUtils;
import view.events.TCPopupEventQueue;
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
import view.widgets.selfexplanation.verbalization.CreateVerbalizationView;
import view.widgets.selfexplanation.verbalization.VerbalizationProcessingView;
import view.widgets.semanticModels.SemanticModelsTraining;
import webService.ReaderBenchServer;

public class ReaderBenchView extends JFrame {

    private static final long serialVersionUID = 4565038532352428650L;
    public static final Logger LOGGER = Logger.getLogger("");

    public static final Map<Lang, List<String>> LSA_SPACES = new HashMap();
    public static final Map<Lang, List<String>> LDA_SPACES = new HashMap();

    static {
        identifyModels(LSA_SPACES, "LSA");
        identifyModels(LDA_SPACES, "LDA");
    }

    public static Lang RUNTIME_LANGUAGE;
    public static Locale LOADED_LOCALE;

    // File menu item
    // tabbed panel
    private final JTabbedPane tabbedPane;
    // data input panel
    private final JPanel panelDataInput;
    private final JButton btnCreateDocument;
    private final JButton btnCreateVerbalization;

    // pre-processing
    private final JButton btnPreprocessingTrainSemanticModels;

    // document analysis
    private final JButton btnDocProcessing;
    private final JButton btnDocumentSemanticSearch;
    private final JButton btnPredictTextualComplexity;

    // textual complexity
    private final JButton btnRunTextualComplexity;
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

    private static void identifyModels(Map<Lang, List<String>> models, String type) {
        for (Lang lang : Lang.values()) {
            List<String> paths = new ArrayList<>();
            File path = new File("resources/config/" + lang.toString().toUpperCase() + "/" + type);
            if (path.exists() && path.isDirectory()) {
                for (File folder : path.listFiles((File current, String name) -> new File(current, name).isDirectory())) {
                    paths.add(folder.getPath().replace("\\", "/"));
                }
            }
            paths.add("");
            models.put(lang, paths);
        }
    }

    public ReaderBenchView() {
        super.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        super.getContentPane().setBackground(Color.WHITE);
        super.getContentPane().setLayout(new BorderLayout(0, 0));
        super.setTitle("ReaderBench " + SettingsUtils.getReaderBenchVersion() + " (" + RUNTIME_LANGUAGE.getDescription()
                + ")");
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
        tabbedPane.addTab(ResourceBundle.getBundle("utils.localization.messages")
                .getString("ReaderBenchView.panelDataInput.title"), null, panelDataInput, null);

        btnCreateDocument = new JButton(ResourceBundle.getBundle("utils.localization.messages")
                .getString("ReaderBenchView.btnCreateDocument.text"));
        btnCreateDocument.addActionListener((ActionEvent arg0) -> {
            DocumentManagementView view = new DocumentManagementView();
            view.setVisible(true);
        });

        btnCreateVerbalization = new JButton(ResourceBundle.getBundle("utils.localization.messages")
                .getString("ReaderBenchView.btnCreateVerbalization.text"));
        btnCreateVerbalization.addActionListener((ActionEvent e) -> {
            CreateVerbalizationView view = new CreateVerbalizationView();
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
                                .addPreferredGap(ComponentPlacement.UNRELATED)));
        gl_panelDataInput.setVerticalGroup(gl_panelDataInput.createParallelGroup(Alignment.LEADING)
                .addGroup(gl_panelDataInput.createSequentialGroup().addContainerGap()
                        .addGroup(gl_panelDataInput.createParallelGroup(Alignment.BASELINE)
                                .addComponent(btnCreateDocument).addComponent(btnCreateVerbalization))
                        .addContainerGap(53, Short.MAX_VALUE)));
        panelDataInput.setLayout(gl_panelDataInput);

        JPanel panelDocument = new JPanel();
        tabbedPane.addTab(
                LocalizationUtils.getLocalizedString(this.getClass(), LocalizationUtils.TITLE, "panelDocument"), null,
                panelDocument, null);

        panelDocument.setBackground(Color.WHITE);
        panelDocument.setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null), "",
                TitledBorder.LEADING, TitledBorder.TOP, null, null));

        btnPredictTextualComplexity = new JButton(
                LocalizationUtils.getLocalizedString(this.getClass(), LocalizationUtils.TEXT, "btnTrainSVMDocs"));
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
                        "At least one document must be loaded for evaluating its textual complexity!", "Information",
                        JOptionPane.INFORMATION_MESSAGE);
            }
        });

        btnDocProcessing = new JButton(
                LocalizationUtils.getLocalizedString(this.getClass(), LocalizationUtils.TEXT, "btnDocProcessing"));
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

        btnDocumentSemanticSearch = new JButton(ResourceBundle.getBundle("utils.localization.messages")
                .getString("ReaderBenchView.btnDocumentSemanticSearch.text"));
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
        tabbedPane.addTab(LocalizationUtils.getLocalizedString(this.getClass(), LocalizationUtils.TITLE,
                "panelTextualComplexity"), null, panelTextualComplexity, null);
        panelTextualComplexity.setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null), "",
                TitledBorder.LEADING, TitledBorder.TOP, null, null));
        panelTextualComplexity.setBackground(Color.WHITE);

        btnRunTextualComplexity = new JButton(ResourceBundle.getBundle("utils.localization.messages")
                .getString("ReaderBenchView.btnRunTextualComplexityIndices.text"));
        btnRunTextualComplexity.addActionListener((ActionEvent e) -> {
            RunMeasurementsView frame = new RunMeasurementsView();
            frame.setVisible(true);
        });

        btnEssayProcessing = new JButton(ResourceBundle.getBundle("utils.localization.messages")
                .getString("ReaderBenchView.btnEssayProcessing.text"));
        btnEssayProcessing.addActionListener((ActionEvent e) -> {
            EssayProcessingView frame = new EssayProcessingView();
            frame.setVisible(true);
        });
        GroupLayout gl_panel = new GroupLayout(panelTextualComplexity);
        gl_panel.setHorizontalGroup(gl_panel.createParallelGroup(Alignment.LEADING)
                .addGroup(gl_panel.createSequentialGroup().addContainerGap()
                        .addComponent(btnRunTextualComplexity, GroupLayout.PREFERRED_SIZE, 220,
                                GroupLayout.PREFERRED_SIZE)
                        .addGap(18)
                        .addComponent(btnEssayProcessing, GroupLayout.PREFERRED_SIZE, 220, GroupLayout.PREFERRED_SIZE)
                        .addContainerGap(874, Short.MAX_VALUE)));
        gl_panel.setVerticalGroup(gl_panel.createParallelGroup(Alignment.LEADING)
                .addGroup(gl_panel.createSequentialGroup().addContainerGap()
                        .addGroup(gl_panel.createParallelGroup(Alignment.BASELINE)
                                .addComponent(btnRunTextualComplexity, GroupLayout.PREFERRED_SIZE, 25,
                                        GroupLayout.PREFERRED_SIZE)
                                .addComponent(btnEssayProcessing))
                        .addContainerGap(53, Short.MAX_VALUE)));
        panelTextualComplexity.setLayout(gl_panel);

        JPanel panelSelfExplanations = new JPanel();
        tabbedPane.addTab(
                LocalizationUtils.getLocalizedString(this.getClass(), LocalizationUtils.TITLE, "panelSelfExplanations"),
                null, panelSelfExplanations, null);
        panelSelfExplanations.setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null), "",
                TitledBorder.LEADING, TitledBorder.TOP, null, null));
        panelSelfExplanations.setBackground(Color.WHITE);

        btnVerbaProcessing = new JButton(ResourceBundle.getBundle("utils.localization.messages")
                .getString("ReaderBenchView.btnVerbaProcessing.text"));
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

        btnSummaryProcessing = new JButton(ResourceBundle.getBundle("utils.localization.messages")
                .getString("ReaderBenchView.btnSummaryProcessing.text"));
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
        tabbedPane.addTab(LocalizationUtils.getLocalizedString(this.getClass(), LocalizationUtils.TITLE, "panelCSCL"),
                null, panelCSCL, null);
        panelCSCL.setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null), "",
                TitledBorder.LEADING, TitledBorder.TOP, null, null));
        panelCSCL.setBackground(Color.WHITE);

        btnVCoPProcessing = new JButton(ResourceBundle.getBundle("utils.localization.messages")
                .getString("ReaderBenchView.btnVCoPProcessing.text"));
        btnVCoPProcessing.addActionListener((ActionEvent e) -> {
            VCoPView frame = new VCoPView();
            frame.setVisible(true);
        });

        btnVcopprocessingevaluation = new JButton(ResourceBundle.getBundle("utils.localization.messages")
                .getString("ReaderBenchView.btnVcopprocessingevaluation.text"));
        btnVcopprocessingevaluation.addActionListener((ActionEvent e) -> {
            VCoPEvaluationView frame = new VCoPEvaluationView();
            frame.setVisible(true);
        });

        btnConversationProcessing = new JButton(ResourceBundle.getBundle("utils.localization.messages")
                .getString("ReaderBenchView.btnConversationProcessing.text"));
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

        JPanel panelPreProcessing = new JPanel();
        panelPreProcessing.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
        panelPreProcessing.setBackground(Color.WHITE);
        tabbedPane.addTab("Additional", null, panelPreProcessing, null);

        btnPreprocessingTrainSemanticModels = new JButton(ResourceBundle.getBundle("utils.localization.messages")
                .getString("ReaderBenchView.btnPreprocessingTrainSemanticModels.text"));
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
        GroupLayout gl_panelPreProcessing = new GroupLayout(panelPreProcessing);
        gl_panelPreProcessing.setHorizontalGroup(gl_panelPreProcessing.createParallelGroup(Alignment.LEADING)
                .addGroup(gl_panelPreProcessing
                        .createSequentialGroup().addContainerGap().addComponent(btnPreprocessingTrainSemanticModels,
                                GroupLayout.PREFERRED_SIZE, 220, GroupLayout.PREFERRED_SIZE)
                        .addContainerGap(917, Short.MAX_VALUE)));
        gl_panelPreProcessing.setVerticalGroup(gl_panelPreProcessing.createParallelGroup(Alignment.LEADING)
                .addGroup(gl_panelPreProcessing
                        .createSequentialGroup().addContainerGap().addComponent(btnPreprocessingTrainSemanticModels,
                                GroupLayout.PREFERRED_SIZE, 25, GroupLayout.PREFERRED_SIZE)
                        .addContainerGap(40, Short.MAX_VALUE)));
        panelPreProcessing.setLayout(gl_panelPreProcessing);

        desktopPane.setLayout(gl_desktopPane);

    }

    public static void updateComboLanguage(JComboBox<String> comboBoxLSA, JComboBox<String> comboBoxLDA, Lang lang) {
        comboBoxLSA.removeAllItems();
        comboBoxLDA.removeAllItems();
      

        ReaderBenchView.LSA_SPACES.get(lang).stream().forEach((url) -> {
            comboBoxLSA.addItem(url);
        });
        ReaderBenchView.LDA_SPACES.get(lang).stream().forEach((url) -> {
            comboBoxLDA.addItem(url);
        });

        comboBoxLSA.setEnabled(true);
        comboBoxLDA.setEnabled(true);
    }

    public static void setRuntimeLang(String text) {
        RUNTIME_LANGUAGE = Lang.getLang(text);
    }

    public static void setLoadedLocale() {
        LOADED_LOCALE = RUNTIME_LANGUAGE.getLocale();
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

    public static void main(String[] args) {
        if (args.length > 0) {
            ReaderBenchView.setRuntimeLang(args[0]);
        } else {
            RUNTIME_LANGUAGE = Lang.en;
        }
        ReaderBenchView.setLoadedLocale();

        Toolkit.getDefaultToolkit().getSystemEventQueue().push(new TCPopupEventQueue());

        ReaderBenchServer.initializeDB();

        adjustToSystemGraphics();

        EventQueue.invokeLater(() -> {
            ReaderBenchView view = new ReaderBenchView();
            view.setVisible(true);
        });
    }
}
