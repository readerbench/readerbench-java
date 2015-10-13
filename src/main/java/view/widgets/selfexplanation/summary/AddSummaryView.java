package view.widgets.selfexplanation.summary;

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

import view.widgets.document.DocumentProcessingView;
import DAO.AbstractDocument;

public class AddSummaryView extends JInternalFrame {
	private static final long serialVersionUID = 8894652868238113117L;
	static Logger logger = Logger.getLogger(AddSummaryView.class);

	private SummaryProcessingView view;
	private JPanel contentPane;
	private JTextField textFieldPath;
	private JComboBox<String> comboBoxDocument;
	private JCheckBox chckbxUsePosTagging;
	private static File lastDirectory = null;

	/**
	 * Create the frame.
	 */
	public AddSummaryView(SummaryProcessingView view) {
		setTitle("ReaderBench - Add a new summary");
		this.view = view;
		setResizable(false);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setBounds(100, 100, 550, 140);
		contentPane = new JPanel();
		contentPane.setBackground(Color.WHITE);
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);

		JLabel lblPath = new JLabel("Path:");

		JLabel lblDocument = new JLabel("Document:");

		String[] titles = new String[DocumentProcessingView
				.getLoadedDocuments().size()];
		int index = 0;
		for (AbstractDocument d : DocumentProcessingView.getLoadedDocuments())
			titles[index++] = d.getDescription();
		comboBoxDocument = new JComboBox<String>(titles);
		comboBoxDocument.setSelectedIndex(0);

		textFieldPath = new JTextField();
		textFieldPath.setText("");
		textFieldPath.setColumns(10);

		JButton btnSearch = new JButton("...");
		btnSearch.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFileChooser fc = null;
				if (lastDirectory == null)
					fc = new JFileChooser("in");
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
				int returnVal = fc.showOpenDialog(AddSummaryView.this);

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
				AddSummaryView.this.dispose();
			}
		});

		JButton btnOk = new JButton("Ok");
		btnOk.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (!textFieldPath.getText().equals("")) {
					SummaryProcessingView.EssayProcessingTask task = AddSummaryView.this.view.new EssayProcessingTask(
							textFieldPath.getText(), DocumentProcessingView
									.getLoadedDocuments()
									.get(comboBoxDocument.getSelectedIndex()),
							chckbxUsePosTagging.isSelected(), false);
					task.execute();
					AddSummaryView.this.dispose();
				} else
					JOptionPane
							.showMessageDialog(
									AddSummaryView.this,
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
																								lblDocument))
																		.addGap(18)
																		.addGroup(
																				gl_contentPane
																						.createParallelGroup(
																								Alignment.TRAILING)
																						.addGroup(
																								gl_contentPane
																										.createSequentialGroup()
																										.addComponent(
																												textFieldPath,
																												GroupLayout.DEFAULT_SIZE,
																												389,
																												Short.MAX_VALUE)
																										.addPreferredGap(
																												ComponentPlacement.RELATED)
																										.addComponent(
																												btnSearch,
																												GroupLayout.PREFERRED_SIZE,
																												41,
																												GroupLayout.PREFERRED_SIZE))
																						.addComponent(
																								comboBoxDocument,
																								0,
																								436,
																								Short.MAX_VALUE))
																		.addGap(6))
														.addGroup(
																gl_contentPane
																		.createSequentialGroup()
																		.addComponent(
																				chckbxUsePosTagging)
																		.addPreferredGap(
																				ComponentPlacement.RELATED,
																				256,
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
																lblDocument)
														.addComponent(
																comboBoxDocument,
																GroupLayout.PREFERRED_SIZE,
																GroupLayout.DEFAULT_SIZE,
																GroupLayout.PREFERRED_SIZE))
										.addPreferredGap(
												ComponentPlacement.UNRELATED)
										.addGroup(
												gl_contentPane
														.createParallelGroup(
																Alignment.BASELINE)
														.addComponent(btnOk)
														.addComponent(btnCancel)
														.addComponent(
																chckbxUsePosTagging))
										.addContainerGap(19, Short.MAX_VALUE)));
		contentPane.setLayout(gl_contentPane);
	}
}
