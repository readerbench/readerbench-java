package view.widgets.document;

import java.awt.Color;
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
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileFilter;

import org.apache.log4j.Logger;

import view.widgets.ReaderBenchView;
import edu.cmu.lti.jawjaw.pobj.Lang;

public class AddDocumentView extends JInternalFrame {
	private static final long serialVersionUID = 8894652868238113117L;
	static Logger logger = Logger.getLogger(AddDocumentView.class);

	private JPanel contentPane;
	private DocumentProcessingView view;
	private JTextField textFieldPath;
	private JComboBox<String> comboBoxLSA;
	private JComboBox<String> comboBoxLDA;
	private JCheckBox chckbxUsePosTagging;
	private static File lastDirectory = null;

	/**
	 * Create the frame.
	 */
	public AddDocumentView(Lang lang, DocumentProcessingView view) {
		setTitle("ReaderBench - Add a new document");
		this.view = view;
		setResizable(false);
		this.setClosable(true);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setBounds(100, 100, 549, 166);
		contentPane = new JPanel();
		contentPane.setBackground(Color.WHITE);
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);

		JLabel lblPath = new JLabel("Path:");

		JLabel lblLsaVectorSpace = new JLabel("LSA vector space:");

		JLabel lblLdaModel = new JLabel("LDA model:");

		comboBoxLSA = new JComboBox<String>();
		comboBoxLDA = new JComboBox<String>();

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

		textFieldPath = new JTextField();
		textFieldPath.setText("");
		textFieldPath.setColumns(10);

		JButton btnSearch = new JButton("...");
		btnSearch.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFileChooser fc = null;
				if (lastDirectory == null)
					fc = new JFileChooser(new File("in"));
				else
					fc = new JFileChooser(lastDirectory);
				fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
				fc.setFileFilter(new FileFilter() {
					public boolean accept(File f) {
						if (f.isDirectory()) {
							return true;
						}
						return f.getName().endsWith(".xml");
					}

					public String getDescription() {
						return "XML file (*.xml) or directory";
					}
				});
				int returnVal = fc.showOpenDialog(AddDocumentView.this);

				if (returnVal == JFileChooser.APPROVE_OPTION) {
					File file = fc.getSelectedFile();
					lastDirectory = file.getParentFile();
					textFieldPath.setText(file.getPath());
				}
			}
		});

		JButton btnCancel = new JButton("Cancel");
		btnCancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				AddDocumentView.this.dispose();
			}
		});

		JButton btnOk = new JButton("Ok");
		btnOk.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (!textFieldPath.getText().equals("")) {
					DocumentProcessingView.DocumentProcessingTask task = AddDocumentView.this.view.new DocumentProcessingTask(
							textFieldPath.getText(), (String) comboBoxLSA
									.getSelectedItem(), (String) comboBoxLDA
									.getSelectedItem(), chckbxUsePosTagging
									.isSelected(), false);
					task.execute();
					AddDocumentView.this.dispose();
				} else
					JOptionPane
							.showMessageDialog(
									AddDocumentView.this,
									"Please select an appropriate input file to be analysed!",
									"Error", JOptionPane.WARNING_MESSAGE);
			}
		});

		chckbxUsePosTagging = new JCheckBox("Use POS tagging");
		chckbxUsePosTagging.setSelected(true);

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
																								401,
																								Short.MAX_VALUE)
																						.addGroup(
																								gl_contentPane
																										.createSequentialGroup()
																										.addComponent(
																												textFieldPath,
																												GroupLayout.DEFAULT_SIZE,
																												354,
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
																								401,
																								Short.MAX_VALUE))
																		.addGap(6))
														.addGroup(
																gl_contentPane
																		.createSequentialGroup()
																		.addComponent(
																				chckbxUsePosTagging)
																		.addPreferredGap(
																				ComponentPlacement.RELATED,
																				255,
																				Short.MAX_VALUE)
																		.addComponent(
																				btnOk,
																				GroupLayout.PREFERRED_SIZE,
																				73,
																				GroupLayout.PREFERRED_SIZE)
																		.addPreferredGap(
																				ComponentPlacement.RELATED)
																		.addComponent(
																				btnCancel)
																		.addContainerGap()))));
		gl_contentPane
				.setVerticalGroup(gl_contentPane
						.createParallelGroup(Alignment.LEADING)
						.addGroup(
								gl_contentPane
										.createSequentialGroup()
										.addGroup(
												gl_contentPane
														.createParallelGroup(
																Alignment.BASELINE)
														.addComponent(
																textFieldPath,
																GroupLayout.PREFERRED_SIZE,
																GroupLayout.DEFAULT_SIZE,
																GroupLayout.PREFERRED_SIZE)
														.addComponent(lblPath)
														.addComponent(btnSearch))
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
														.addComponent(btnOk)
														.addComponent(btnCancel)
														.addComponent(
																chckbxUsePosTagging))
										.addContainerGap(
												GroupLayout.DEFAULT_SIZE,
												Short.MAX_VALUE)));
		contentPane.setLayout(gl_contentPane);
	}
}
