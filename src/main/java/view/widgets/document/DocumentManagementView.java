package view.widgets.document;

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Date;
import java.util.LinkedList;
import java.util.Vector;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileFilter;

import org.apache.log4j.Logger;

import data.Block;
import data.document.Document;
import services.converters.Txt2XmlConverter;
import utils.localization.LocalizationUtils;
import view.widgets.ReaderBenchView;

public class DocumentManagementView extends JFrame {

	private static final long serialVersionUID = -2864356905020607155L;
	static Logger logger = Logger.getLogger(DocumentManagementView.class);
	public static final String VERBALIZATION_TAG = "//verbalization_breakpoint//";

	private Document loadedDocument = null;
	private JPanel contentPane;
	private JTextField textFieldTitle;
	private JTextField textFieldAuthors;
	private JTextField textFieldURI;
	private JTextField txtComplexityLevel;
	private JTextField textFieldSource;
	private JTextArea textAreaContent;
	private static File lastDirectory;
	private JButton btnVerbalizationBreakpoint;
	private JCheckBox chckbxIsEssay;

	/**
	 * Create the frame.
	 */
	public DocumentManagementView() {
		setTitle("ReaderBench - Document Management");
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		this.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				quitFormDialogue();
			}
		});
		setBounds(100, 100, 750, 700);

		JMenuBar menuBar = new JMenuBar();
		setJMenuBar(menuBar);

		JMenu mnFile = new JMenu(LocalizationUtils.getTranslation("File"));
		mnFile.setMnemonic(KeyEvent.VK_F);
		menuBar.add(mnFile);

		JMenuItem mntmNew = new JMenuItem(LocalizationUtils.getTranslation("New"), KeyEvent.VK_N);
		mntmNew.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				EventQueue.invokeLater(new Runnable() {
					@Override
					public void run() {
						DocumentManagementView view = new DocumentManagementView();
						view.setLocation(DocumentManagementView.this.getLocation().x + 50,
								DocumentManagementView.this.getLocation().y + 50);
						view.setVisible(true);
					}
				});
			}
		});
		mntmNew.setAccelerator(KeyStroke.getKeyStroke("control N"));
		mnFile.add(mntmNew);

		JMenuItem mntmOpen = new JMenuItem(LocalizationUtils.getTranslation("Open XML or txt"), KeyEvent.VK_O);
		mntmOpen.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFileChooser fc = null;
				if (lastDirectory == null)
					fc = new JFileChooser(new File("in"));
				else
					fc = new JFileChooser(lastDirectory);
				fc.setFileFilter(new FileFilter() {
					public boolean accept(File f) {
						if (f.isDirectory()) {
							return true;
						}
						return f.getName().endsWith(".xml") || f.getName().endsWith(".txt");
					}

					public String getDescription() {
						return "XML files (*.xml) or simple text files (*.txt) in UTF-8 format";
					}
				});
				int returnVal = fc.showOpenDialog(DocumentManagementView.this);

				if (returnVal == JFileChooser.APPROVE_OPTION) {
					File file = fc.getSelectedFile();
					lastDirectory = file.getParentFile();
					if (file.getName().endsWith(".xml")) {
						loadedDocument = Document.load(file, null, null, null, false, false);
						loadDocument();
					} else if (file.getName().endsWith(".txt")) {
						textFieldTitle.setText(file.getName().replace(".txt", ""));

						// read txt content
						try {
							FileInputStream inputFile = new FileInputStream(file);
							InputStreamReader ir = new InputStreamReader(inputFile, "UTF-8");
							BufferedReader in = new BufferedReader(ir);
							String line;
							String content = "";
							while ((line = in.readLine()) != null) {
								if (line.trim().length() > 0) {
									content += line.trim() + "\n\n";
								}
							}
							textAreaContent.setText(content.trim());
							in.close();
						} catch (IOException exc) {
							exc.printStackTrace();
						}
					}
				}
			}
		});
		mntmOpen.setAccelerator(KeyStroke.getKeyStroke("control O"));
		mnFile.add(mntmOpen);

		JMenuItem mntmSave = new JMenuItem(LocalizationUtils.getTranslation("Save"), KeyEvent.VK_S);
		mntmSave.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (loadedDocument == null) {
					updateDocument();
					saveDocumentDialogue();
				} else {
					updateDocument();
					loadedDocument.exportXML(loadedDocument.getPath());
				}
				loadDocument();
			}
		});
		mntmSave.setAccelerator(KeyStroke.getKeyStroke("control S"));
		mnFile.add(mntmSave);

		JMenuItem mntmSaveAs = new JMenuItem(LocalizationUtils.getTranslation("Save as..."), KeyEvent.VK_S);
		mntmSaveAs.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				updateDocument();
				saveDocumentDialogue();
				loadDocument();
			}
		});
		mntmSaveAs.setAccelerator(KeyStroke.getKeyStroke("control alt S"));
		mnFile.add(mntmSaveAs);

		JMenuItem mntmQuit = new JMenuItem(LocalizationUtils.getTranslation("Quit"), KeyEvent.VK_Q);
		mntmQuit.setAccelerator(KeyStroke.getKeyStroke("control Q"));
		mntmQuit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				quitFormDialogue();
			}
		});
		mnFile.add(mntmQuit);

		JMenu mnTools = new JMenu(LocalizationUtils.getTranslation("Tools"));
		menuBar.add(mnTools);

		JMenuItem mntmConvert = new JMenuItem(
				LocalizationUtils.getTranslation("Convert text files from folder to xml"));
		mntmConvert.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				openTextDocumentDialogue();
			}
		});
		mnTools.add(mntmConvert);

		contentPane = new JPanel();
		contentPane.setBackground(Color.WHITE);
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);

		JLabel lblTitle = new JLabel(LocalizationUtils.getTranslation("Title") + ":");
		lblTitle.setFont(new Font("Lucida Grande", Font.BOLD, 13));

		JLabel lblAuthors = new JLabel("Authors*:");
		lblAuthors.setFont(new Font("Lucida Grande", Font.BOLD, 13));

		JLabel lblAuthorsComment = new JLabel(
				"* " + LocalizationUtils.getTranslation("Multiple authors should be separated by commas"));
		lblAuthorsComment.setFont(new Font("Lucida Grande", Font.PLAIN, 9));

		textFieldTitle = new JTextField();
		textFieldTitle.setColumns(10);

		textFieldAuthors = new JTextField();
		textFieldAuthors.setColumns(10);

		JLabel lblUri = new JLabel("URI:");
		lblUri.setFont(new Font("SansSerif", Font.BOLD, 12));

		textFieldURI = new JTextField();
		textFieldURI.setColumns(10);

		JLabel lblComplexityLevel = new JLabel(LocalizationUtils.getTranslation("Complexity level") + ":");
		lblComplexityLevel.setFont(new Font("SansSerif", Font.BOLD, 12));

		txtComplexityLevel = new JTextField();
		txtComplexityLevel.setHorizontalAlignment(SwingConstants.RIGHT);
		txtComplexityLevel.setColumns(10);

		JLabel lblSource = new JLabel(LocalizationUtils.getTranslation("Source") + ":");
		lblSource.setFont(new Font("SansSerif", Font.BOLD, 12));

		textFieldSource = new JTextField();
		textFieldSource.setColumns(10);

		JLabel lblText = new JLabel(LocalizationUtils.getTranslation("Text"));
		lblText.setFont(new Font("SansSerif", Font.BOLD, 12));

		btnVerbalizationBreakpoint = new JButton(LocalizationUtils.getTranslation("Insert verbalization breakpoint"));
		btnVerbalizationBreakpoint.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int pos = textAreaContent.getCaretPosition();
				textAreaContent.insert(VERBALIZATION_TAG, pos);
			}
		});

		JSeparator separator = new JSeparator();

		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

		JLabel lblByDefault = new JLabel("* " + LocalizationUtils.getTranslation(
				"by default a verbalization breakpoint is inserted at the end of each file, after the first save operation"));

		chckbxIsEssay = new JCheckBox(LocalizationUtils.getTranslation("Is Essay"));
		chckbxIsEssay.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				btnVerbalizationBreakpoint.setEnabled(!chckbxIsEssay.isSelected());
			}
		});
		GroupLayout gl_contentPane = new GroupLayout(contentPane);
		gl_contentPane.setHorizontalGroup(gl_contentPane.createParallelGroup(Alignment.TRAILING)
				.addGroup(gl_contentPane.createSequentialGroup().addContainerGap()
						.addGroup(gl_contentPane.createParallelGroup(Alignment.LEADING).addComponent(scrollPane)
								.addComponent(separator, GroupLayout.DEFAULT_SIZE, 648, Short.MAX_VALUE)
								.addGroup(gl_contentPane.createSequentialGroup().addComponent(lblTitle)
										.addPreferredGap(ComponentPlacement.RELATED)
										.addComponent(textFieldTitle, GroupLayout.DEFAULT_SIZE, 611, Short.MAX_VALUE))
						.addGroup(gl_contentPane.createSequentialGroup().addComponent(lblAuthors)
								.addPreferredGap(ComponentPlacement.RELATED)
								.addComponent(textFieldAuthors, GroupLayout.DEFAULT_SIZE, 586, Short.MAX_VALUE))
						.addComponent(lblAuthorsComment)
						.addGroup(gl_contentPane.createSequentialGroup().addComponent(lblUri)
								.addPreferredGap(ComponentPlacement.RELATED)
								.addComponent(textFieldURI, GroupLayout.DEFAULT_SIZE, 621, Short.MAX_VALUE))
						.addGroup(gl_contentPane.createSequentialGroup().addComponent(lblComplexityLevel)
								.addPreferredGap(ComponentPlacement.RELATED)
								.addComponent(txtComplexityLevel, GroupLayout.PREFERRED_SIZE, 53,
										GroupLayout.PREFERRED_SIZE)
								.addPreferredGap(ComponentPlacement.RELATED).addComponent(lblSource)
								.addPreferredGap(ComponentPlacement.RELATED).addComponent(textFieldSource,
										GroupLayout.DEFAULT_SIZE, 441, Short.MAX_VALUE))
								.addGroup(gl_contentPane.createSequentialGroup().addComponent(lblText)
										.addPreferredGap(ComponentPlacement.RELATED, 287, Short.MAX_VALUE)
										.addComponent(chckbxIsEssay).addGap(18)
										.addComponent(btnVerbalizationBreakpoint))
								.addComponent(lblByDefault))
						.addContainerGap()));
		gl_contentPane.setVerticalGroup(gl_contentPane.createParallelGroup(Alignment.LEADING).addGroup(gl_contentPane
				.createSequentialGroup().addContainerGap()
				.addGroup(gl_contentPane.createParallelGroup(Alignment.BASELINE).addComponent(lblTitle).addComponent(
						textFieldTitle, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
						GroupLayout.PREFERRED_SIZE))
				.addPreferredGap(ComponentPlacement.UNRELATED)
				.addGroup(gl_contentPane.createParallelGroup(Alignment.BASELINE).addComponent(lblAuthors).addComponent(
						textFieldAuthors, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
						GroupLayout.PREFERRED_SIZE))
				.addPreferredGap(ComponentPlacement.RELATED).addComponent(lblAuthorsComment)
				.addPreferredGap(ComponentPlacement.RELATED)
				.addGroup(gl_contentPane.createParallelGroup(Alignment.BASELINE)
						.addComponent(textFieldURI, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addComponent(lblUri))
				.addPreferredGap(ComponentPlacement.RELATED)
				.addGroup(gl_contentPane.createParallelGroup(Alignment.BASELINE)
						.addComponent(txtComplexityLevel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addComponent(lblComplexityLevel).addComponent(lblSource).addComponent(textFieldSource,
								GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
				.addPreferredGap(ComponentPlacement.RELATED)
				.addGroup(gl_contentPane.createParallelGroup(Alignment.BASELINE).addComponent(lblText)
						.addComponent(btnVerbalizationBreakpoint).addComponent(chckbxIsEssay))
				.addPreferredGap(ComponentPlacement.RELATED)
				.addComponent(separator, GroupLayout.PREFERRED_SIZE, 2, GroupLayout.PREFERRED_SIZE)
				.addPreferredGap(ComponentPlacement.RELATED)
				.addComponent(scrollPane, GroupLayout.DEFAULT_SIZE, 299, Short.MAX_VALUE)
				.addPreferredGap(ComponentPlacement.RELATED).addComponent(lblByDefault)));

		textAreaContent = new JTextArea();
		textAreaContent.setLineWrap(true);
		textAreaContent.setWrapStyleWord(true);
		scrollPane.setViewportView(textAreaContent);
		contentPane.setLayout(gl_contentPane);
	}

	public void loadDocument() {
		textFieldTitle.setText(loadedDocument.getTitleText());
		this.setTitle("ReaderBench (" + loadedDocument.getPath() + ")");

		String authors = "";
		for (String author : loadedDocument.getAuthors())
			authors += author + ", ";
		textFieldAuthors.setText(authors.length() > 2 ? authors.substring(0, authors.length() - 2) : "");

		textFieldURI.setText(loadedDocument.getURI());

		txtComplexityLevel.setText(loadedDocument.getComplexityLevel());

		textFieldSource.setText(loadedDocument.getSource());

		String content = "";
		for (Block b : loadedDocument.getBlocks()) {
			if (b.isFollowedByVerbalization())
				content += b.getText() + "\n\n" + VERBALIZATION_TAG + "\n\n";
			else
				content += b.getText() + "\n\n";
		}

		textAreaContent.setText(content.trim());
	}

	public void updateDocument() {
		if (loadedDocument == null)
			loadedDocument = new Document(null, null, null, null);
		loadedDocument.setTitleText(textFieldTitle.getText());
		this.setTitle("ReaderBench (" + loadedDocument.getPath() + ")");

		loadedDocument.setAuthors(new LinkedList<String>());
		String[] authors = textFieldAuthors.getText().split(",");
		for (String author : authors)
			loadedDocument.getAuthors().add(author.trim());

		loadedDocument.setDate(new Date());

		loadedDocument.setURI(textFieldURI.getText());

		loadedDocument.setComplexityLevel(txtComplexityLevel.getText());

		loadedDocument.setSource(textFieldSource.getText());

		int index = 0;
		loadedDocument.setBlocks(new Vector<Block>());
		for (String blocks : textAreaContent.getText().trim().split(VERBALIZATION_TAG)) {
			Block b = null;
			for (String block : blocks.split("(\n)+")) {
				if (block.trim().length() > 0) {
					b = new Block(loadedDocument, index++, block.trim(), loadedDocument.getLSA(),
							loadedDocument.getLDA(), loadedDocument.getLanguage());
					loadedDocument.getBlocks().add(b);
				}
			}
			// always last block is followed by a verbalization tag
			// omit for essay!
			if (b != null && !chckbxIsEssay.isSelected())
				b.setFollowedByVerbalization(true);
		}
	}

	public void saveDocumentDialogue() {
		JFileChooser fc = null;
		if (lastDirectory == null)
			fc = new JFileChooser(new File("in"));
		else
			fc = new JFileChooser(lastDirectory);
		fc.setFileFilter(new FileFilter() {
			public boolean accept(File f) {
				if (f.isDirectory()) {
					return true;
				}
				return f.getName().endsWith(".xml");
			}

			public String getDescription() {
				return "XML files (*.xml)";
			}
		});

		int returnVal = fc.showSaveDialog(DocumentManagementView.this);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File file = fc.getSelectedFile();
			if (!file.getName().endsWith(".xml")) {
				// add xml extension
				loadedDocument.setPath(file.getPath() + ".xml");
				loadedDocument.exportXML(loadedDocument.getPath());
			} else {
				loadedDocument.setPath(file.getPath());
				loadedDocument.exportXML(loadedDocument.getPath());
			}
		}
	}

	public void openTextDocumentDialogue() {
		JFileChooser fc = null;
		if (lastDirectory == null)
			fc = new JFileChooser(new File("in"));
		else
			fc = new JFileChooser(lastDirectory);
		fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		fc.setFileFilter(new FileFilter() {
			public boolean accept(File f) {
				if (f.isDirectory()) {
					return true;
				}
				return false;
			}

			public String getDescription() {
				return "TXT files (*.txt)";
			}
		});

		int returnVal = fc.showOpenDialog(DocumentManagementView.this);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File file = fc.getSelectedFile();
			this.convertTextFilesToXml(file);
		}
	}

	public void convertTextFilesToXml(File file) {
		Txt2XmlConverter.parseTxtFiles("Converted", file.getAbsolutePath(), ReaderBenchView.RUNTIME_LANGUAGE, "UTF-8");
	}

	public void quitFormDialogue() {
		Object[] options = { LocalizationUtils.getTranslation("Yes"), LocalizationUtils.getTranslation("No"),
				LocalizationUtils.getTranslation("Cancel") };
		int value = JOptionPane.showOptionDialog(this,
				LocalizationUtils.getTranslation("Do you want to save changes") + "?",
				LocalizationUtils.getTranslation("Save"), JOptionPane.YES_NO_CANCEL_OPTION,
				JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
		if (value == JOptionPane.YES_OPTION) {
			if (loadedDocument == null) {
				updateDocument();
				saveDocumentDialogue();
			} else {
				loadedDocument.exportXML(loadedDocument.getPath());
			}
			this.dispose();
		}
		if (value == JOptionPane.NO_OPTION) {
			this.dispose();
		}
	}
}
