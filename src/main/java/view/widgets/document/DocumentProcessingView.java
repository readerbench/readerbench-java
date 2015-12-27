package view.widgets.document;

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDesktopPane;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.Painter;
import javax.swing.RowFilter;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingWorker;
import javax.swing.UIDefaults;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.TableRowSorter;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;

import data.AbstractDocument;
import data.cscl.Conversation;
import data.document.Document;
import edu.cmu.lti.jawjaw.pobj.Lang;
import utils.localization.LocalizationUtils;
import view.models.document.DocumentManagementTableModel;
import view.widgets.ReaderBenchView;
import view.widgets.complexity.ComplexityIndicesView;
import view.widgets.document.corpora.PaperSimilarityView;

public class DocumentProcessingView extends JInternalFrame {
	private static final long serialVersionUID = -8772215709851320157L;
	static Logger logger = Logger.getLogger(DocumentProcessingView.class);

	private JLabel lblLanguage;
	private JComboBox<String> comboBoxLanguage;
	private JButton btnRemoveDocument = null;
	private JButton btnAddDocument = null;
	private JButton btnViewDocument = null;
	private JButton btnAddSerializedDocument = null;
	private JTable docTable;
	private DocumentManagementTableModel docTableModel = null;
	private TableRowSorter<DocumentManagementTableModel> docSorter;
	private JScrollPane scrollPane;
	private JDesktopPane desktopPane;
	private static File lastDirectory = null;

	private static List<Document> loadedDocuments = new LinkedList<Document>();
	private CustomTextField articleTextField;
	private CustomTextField authorsTextField;
	private JButton btnViewSimilarDocs;
	private String queryArticleName;
	private String queryAuthorName;

	public class DocumentProcessingTask extends SwingWorker<Void, Void> {
		private String pathToDoc;
		private String pathToLSA;
		private String pathToLDA;
		private boolean usePOSTagging;
		private boolean isSerialized;

		public DocumentProcessingTask(String pathToDoc, String pathToLSA, String pathToLDA, boolean usePOSTagging,
				boolean isSerialized) {
			super();
			this.pathToDoc = pathToDoc;
			this.pathToLSA = pathToLSA;
			this.pathToLDA = pathToLDA;
			this.usePOSTagging = usePOSTagging;
			this.isSerialized = isSerialized;
		}

		public void addSingleDocument(String pathToIndividualFile) {
			AbstractDocument d = null;
			if (isSerialized) {
				d = AbstractDocument.loadSerializedDocument(pathToIndividualFile);
			} else {
				d = AbstractDocument.loadGenericDocument(pathToIndividualFile, pathToLSA, pathToLDA,
						ReaderBenchView.RUNTIME_LANGUAGE, usePOSTagging, true);
			}
			if (d.getLanguage() == ReaderBenchView.RUNTIME_LANGUAGE) {
				if (d instanceof Document) {
					addDocument((Document) d);
				} else {
					JOptionPane.showMessageDialog(desktopPane, "Please load only a document!", "Information",
							JOptionPane.INFORMATION_MESSAGE);
				}

			} else {
				JOptionPane.showMessageDialog(desktopPane, "Incorrect language for the loaded document!", "Information",
						JOptionPane.INFORMATION_MESSAGE);
			}
		}

		public Void doInBackground() {
			btnAddDocument.setEnabled(false);
			btnAddSerializedDocument.setEnabled(false);

			File file = new File(pathToDoc);
			File[] files = { file };
			if (isSerialized) {
				if (file.isDirectory()) {
					// process each individual ser file
					files = file.listFiles(new FilenameFilter() {
						@Override
						public boolean accept(File dir, String name) {
							return name.endsWith(".ser");
						}
					});
				}
			} else {
				if (file.isDirectory()) {
					// process each individual xml file
					files = file.listFiles(new FilenameFilter() {
						@Override
						public boolean accept(File dir, String name) {
							return name.endsWith(".xml");
						}
					});
				}
			}
			for (File f : files) {
				try {
					addSingleDocument(f.getPath());
				} catch (Exception e) {
					// e.printStackTrace();
					logger.error(f.getName() + ": " + e.getMessage());
				}
			}
			return null;
		}

		@Override
		public void done() {
			Toolkit.getDefaultToolkit().beep();
			btnAddDocument.setEnabled(true);
			btnAddSerializedDocument.setEnabled(true);
		}
	}

	private class CustomTextField extends JTextField {
		private static final long serialVersionUID = 1L;

		private Font originalFont;
		private Color originalForeground;
		/**
		 * Grey by default*
		 */
		private Color placeholderForeground = new Color(160, 160, 160);
		private boolean textWrittenIn;

		public CustomTextField(int columns) {
			super(columns);
		}

		@Override
		public void setFont(Font f) {
			super.setFont(f);
			if (!isTextWrittenIn()) {
				originalFont = f;
			}
		}

		@Override
		public void setForeground(Color fg) {
			super.setForeground(fg);
			if (!isTextWrittenIn()) {
				originalForeground = fg;
			}
		}

		public Color getPlaceholderForeground() {
			return placeholderForeground;
		}

		public void setPlaceholderForeground(Color placeholderForeground) {
			this.placeholderForeground = placeholderForeground;
		}

		public boolean isTextWrittenIn() {
			return textWrittenIn;
		}

		public void setTextWrittenIn(boolean textWrittenIn) {
			this.textWrittenIn = textWrittenIn;
		}

		public void setPlaceholder(final String text) {
			this.customizeText(text);
			this.getDocument().addDocumentListener(new DocumentListener() {
				@Override
				public void insertUpdate(DocumentEvent e) {
					warn();
				}

				@Override
				public void removeUpdate(DocumentEvent e) {
					warn();
				}

				@Override
				public void changedUpdate(DocumentEvent e) {
					warn();
				}

				public void warn() {
					if (getText().trim().length() != 0) {
						setFont(originalFont);
						setForeground(originalForeground);
						setTextWrittenIn(true);
					}
				}
			});

			this.addFocusListener(new FocusListener() {
				@Override
				public void focusGained(FocusEvent e) {
					if (!isTextWrittenIn()) {
						setText("");
					}
				}

				@Override
				public void focusLost(FocusEvent e) {
					if (getText().trim().length() == 0) {
						customizeText(text);
					}
				}
			});
		}

		private void customizeText(String text) {
			setText(text);
			setFont(new Font(getFont().getFamily(), getFont().getStyle(), getFont().getSize()));
			setForeground(getPlaceholderForeground());
			setTextWrittenIn(false);
		}
	}

	/**
	 * Create the frame.
	 */
	public DocumentProcessingView() {
		setTitle("ReaderBench - " + LocalizationUtils.getTranslation("Document Processing"));
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setResizable(true);
		setClosable(true);
		setMaximizable(true);
		setIconifiable(true);
		setBounds(20, 20, 840, 450);
		queryAuthorName = "";
		queryArticleName = "";

		lblLanguage = new JLabel(LocalizationUtils.getTranslation("Language") + ":");
		lblLanguage.setFont(new Font("SansSerif", Font.BOLD, 12));
		lblLanguage.setForeground(Color.BLACK);

		comboBoxLanguage = new JComboBox<String>();
		comboBoxLanguage.addItem("<< " + LocalizationUtils.getTranslation("Please select analysis language") + " >>");
		for (String lang : Lang.SUPPORTED_LANGUAGES)
			comboBoxLanguage.addItem(lang);

		btnAddDocument = new JButton(LocalizationUtils.getTranslation("Add document(s)"));
		btnAddDocument.setEnabled(false);
		btnAddDocument.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					JInternalFrame frame = new AddDocumentView(ReaderBenchView.RUNTIME_LANGUAGE,
							DocumentProcessingView.this);
					frame.setVisible(true);
					desktopPane.add(frame);
					try {
						frame.setSelected(true);
					} catch (java.beans.PropertyVetoException exception) {
						exception.printStackTrace();
					}
				} catch (Exception exception) {
					exception.printStackTrace();
				}
			}
		});

		btnAddSerializedDocument = new JButton(LocalizationUtils.getTranslation("Add preprocessed document(s)"));
		btnAddSerializedDocument.setEnabled(false);
		btnAddSerializedDocument.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFileChooser fc = null;
				if (lastDirectory == null)
					fc = new JFileChooser(new File("resources/in"));
				else
					fc = new JFileChooser(lastDirectory);
				fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
				fc.setFileFilter(new FileFilter() {
					public boolean accept(File f) {
						if (f.isDirectory()) {
							return true;
						}
						return f.getName().endsWith(".ser");
					}

					public String getDescription() {
						return "Serialized documents (*.ser) or directory";
					}
				});
				int returnVal = fc.showOpenDialog(DocumentProcessingView.this);

				if (returnVal == JFileChooser.APPROVE_OPTION) {
					File file = fc.getSelectedFile();
					lastDirectory = file.getParentFile();
					DocumentProcessingView.DocumentProcessingTask task = DocumentProcessingView.this.new DocumentProcessingTask(
							file.getPath(), null, null, false, true);
					task.execute();
				}
			}
		});

		comboBoxLanguage.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (comboBoxLanguage.getSelectedIndex() > 0) {
					// set final analysis language
					ReaderBenchView.RUNTIME_LANGUAGE = Lang.getLang((String) comboBoxLanguage.getSelectedItem());
					ComplexityIndicesView.updateSelectedIndices(ReaderBenchView.RUNTIME_LANGUAGE);
					comboBoxLanguage.setEnabled(false);
					btnAddDocument.setEnabled(true);
					btnAddSerializedDocument.setEnabled(true);
				}
			}
		});

		if (ReaderBenchView.RUNTIME_LANGUAGE != null) {
			comboBoxLanguage.setEnabled(false);
			switch (ReaderBenchView.RUNTIME_LANGUAGE) {
			case fr:
				comboBoxLanguage.setSelectedItem("French");
				break;
			case it:
				comboBoxLanguage.setSelectedItem("Italian");
				break;
			case es:
				comboBoxLanguage.setSelectedItem("Spanish");
				break;
			default:
				comboBoxLanguage.setSelectedItem("English");
			}
		}

		docTableModel = new DocumentManagementTableModel();
		docTable = new JTable(docTableModel);
		docTable.setFillsViewportHeight(true);
		docSorter = new TableRowSorter<DocumentManagementTableModel>(docTableModel);
		docTable.setRowSorter(docSorter);

		docTable.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent event) {
				if (event.getClickCount() == 2) {
					JTable target = (JTable) event.getSource();
					int row = target.getSelectedRow();
					if (row >= 0 && row < loadedDocuments.size()) {
						int modelRow = target.convertRowIndexToModel(target.getSelectedRow());
						AbstractDocument d = loadedDocuments.get(modelRow);
						if (d instanceof Document) {
							DocumentView view = new DocumentView((Document) d);
							view.setVisible(true);
						}
					}
				}
			}
		});

		scrollPane = new JScrollPane();
		scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		scrollPane.setViewportView(docTable);

		desktopPane = new JDesktopPane() {
			private static final long serialVersionUID = 8453433109734630086L;

			@Override
			public void updateUI() {
				if ("Nimbus".equals(UIManager.getLookAndFeel().getName())) {
					UIDefaults map = new UIDefaults();
					Painter<JComponent> painter = new Painter<JComponent>() {
						@Override
						public void paint(Graphics2D g, JComponent c, int w, int h) {
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

		JPanel panelSingleDoc = new JPanel();
		panelSingleDoc.setBackground(Color.WHITE);
		panelSingleDoc.setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null),
				LocalizationUtils.getTranslation("Specific document operations"), TitledBorder.LEFT, TitledBorder.TOP,
				null, null));

		JPanel panelSearch = new JPanel();
		panelSearch.setBackground(Color.WHITE);
		panelSearch.setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null), "Filter",
				TitledBorder.LEADING, TitledBorder.TOP, null, new Color(59, 59, 59)));

		GroupLayout gl_desktopPane = new GroupLayout(desktopPane);
		gl_desktopPane.setHorizontalGroup(gl_desktopPane.createParallelGroup(Alignment.TRAILING).addGroup(
				Alignment.LEADING,
				gl_desktopPane.createSequentialGroup().addContainerGap().addGroup(gl_desktopPane
						.createParallelGroup(Alignment.LEADING)
						.addComponent(scrollPane, GroupLayout.DEFAULT_SIZE, 801, Short.MAX_VALUE)
						.addGroup(gl_desktopPane.createSequentialGroup()
								.addGroup(gl_desktopPane.createParallelGroup(Alignment.LEADING)
										.addComponent(panelSearch, GroupLayout.DEFAULT_SIZE, 801, Short.MAX_VALUE)
										.addGroup(gl_desktopPane.createSequentialGroup().addComponent(lblLanguage)
												.addGap(2).addComponent(comboBoxLanguage, 0, 740, Short.MAX_VALUE)))
								.addContainerGap())
						.addGroup(gl_desktopPane.createSequentialGroup().addComponent(panelSingleDoc,
								GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE).addGap(11)))));
		gl_desktopPane.setVerticalGroup(gl_desktopPane.createParallelGroup(Alignment.LEADING).addGroup(gl_desktopPane
				.createSequentialGroup().addContainerGap()
				.addGroup(gl_desktopPane.createParallelGroup(Alignment.LEADING, false)
						.addComponent(lblLanguage, GroupLayout.PREFERRED_SIZE, 25, GroupLayout.PREFERRED_SIZE)
						.addComponent(comboBoxLanguage, GroupLayout.PREFERRED_SIZE, 25, GroupLayout.PREFERRED_SIZE))
				.addPreferredGap(ComponentPlacement.RELATED)
				.addComponent(panelSearch, GroupLayout.PREFERRED_SIZE, 62, GroupLayout.PREFERRED_SIZE)
				.addPreferredGap(ComponentPlacement.RELATED)
				.addComponent(scrollPane, GroupLayout.DEFAULT_SIZE, 226, Short.MAX_VALUE)
				.addPreferredGap(ComponentPlacement.RELATED).addComponent(panelSingleDoc, GroupLayout.PREFERRED_SIZE,
						GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
				.addContainerGap()));

		JSplitPane splitPane = new JSplitPane();
		splitPane.setBackground(Color.WHITE);
		panelSearch.add(splitPane);

		articleTextField = new CustomTextField(1);
		articleTextField.setPlaceholder("Insert Article Name");
		articleTextField.setFont(new Font("SansSerif", Font.ITALIC, 13));
		articleTextField.setPlaceholderForeground(Color.gray);
		articleTextField.getDocument().addDocumentListener(new DocumentListener() {
			public void changedUpdate(DocumentEvent e) {
				warn();
			}

			public void removeUpdate(DocumentEvent e) {
				warn();
			}

			public void insertUpdate(DocumentEvent e) {
				warn();
			}

			public void warn() {
				queryArticleName = articleTextField.getText();
				if (queryArticleName.equalsIgnoreCase("Insert Article Name"))
					queryArticleName = "";
				newFilter();
			}
		});

		authorsTextField = new CustomTextField(1);
		authorsTextField.setPlaceholder("Insert Author Name");
		authorsTextField.setFont(new Font("SansSerif", Font.ITALIC, 13));
		authorsTextField.setPlaceholderForeground(Color.gray);
		authorsTextField.getDocument().addDocumentListener(new DocumentListener() {
			public void changedUpdate(DocumentEvent e) {
				warn();
			}

			public void removeUpdate(DocumentEvent e) {
				warn();
			}

			public void insertUpdate(DocumentEvent e) {
				warn();
			}

			public void warn() {
				queryAuthorName = authorsTextField.getText();
				if (queryAuthorName.equalsIgnoreCase("Insert Author Name"))
					queryAuthorName = "";
				newFilter();
			}
		});
		splitPane.setLeftComponent(articleTextField);
		articleTextField.setColumns(25);

		splitPane.setRightComponent(authorsTextField);
		authorsTextField.setColumns(25);

		btnViewDocument = new JButton(LocalizationUtils.getTranslation("View document"));
		btnViewDocument.setEnabled(false);
		btnViewDocument.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (docTable.getSelectedRow() != -1) {
					int modelRow = docTable.convertRowIndexToModel(docTable.getSelectedRow());
					AbstractDocument d = loadedDocuments.get(modelRow);
					if (d instanceof Document) {
						DocumentView view = new DocumentView((Document) d);
						view.setVisible(true);
					}
				} else {
					JOptionPane.showMessageDialog(desktopPane, "Please select a document to be viewed!", "Information",
							JOptionPane.INFORMATION_MESSAGE);
				}
			}
		});

		btnViewSimilarDocs = new JButton(LocalizationUtils.getTranslation("View similar docs"));
		btnViewSimilarDocs.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (docTable.getSelectedRow() != -1) {
					AbstractDocument d = loadedDocuments.get(docTable.getSelectedRow());
					if (d instanceof Document) {
						PaperSimilarityView view = new PaperSimilarityView(loadedDocuments, (Document) d);
						view.setVisible(true);
					} else {
						JOptionPane.showMessageDialog(desktopPane,
								"Please select a document (not a conversation) from the list!", "Information",
								JOptionPane.INFORMATION_MESSAGE);
					}
				} else {
					JOptionPane.showMessageDialog(desktopPane,
							"Please select a document (not a conversation) from the list!", "Information",
							JOptionPane.INFORMATION_MESSAGE);
				}
			}
		});
		btnViewSimilarDocs.setEnabled(false);

		btnRemoveDocument = new JButton(LocalizationUtils.getTranslation("Remove document"));
		btnRemoveDocument.setEnabled(false);
		GroupLayout gl_panelSingleDoc = new GroupLayout(panelSingleDoc);
		gl_panelSingleDoc.setHorizontalGroup(gl_panelSingleDoc.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panelSingleDoc.createSequentialGroup().addGap(2).addComponent(btnViewDocument).addGap(5)
						.addComponent(btnViewSimilarDocs).addPreferredGap(ComponentPlacement.RELATED)
						.addComponent(btnAddDocument).addPreferredGap(ComponentPlacement.RELATED)
						.addComponent(btnAddSerializedDocument).addPreferredGap(ComponentPlacement.RELATED)
						.addComponent(btnRemoveDocument).addGap(26)));
		gl_panelSingleDoc
				.setVerticalGroup(gl_panelSingleDoc.createParallelGroup(Alignment.LEADING)
						.addGroup(gl_panelSingleDoc.createSequentialGroup().addGap(5)
								.addGroup(gl_panelSingleDoc.createParallelGroup(Alignment.BASELINE)
										.addComponent(btnViewDocument).addComponent(btnViewSimilarDocs)
										.addComponent(btnAddDocument).addComponent(btnAddSerializedDocument)
										.addComponent(btnRemoveDocument))));
		panelSingleDoc.setLayout(gl_panelSingleDoc);
		btnRemoveDocument.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (docTable.getSelectedRow() != -1) {
					int modelRow = docTable.convertRowIndexToModel(docTable.getSelectedRow());
					AbstractDocument toRemove = loadedDocuments.get(modelRow);
					loadedDocuments.remove(toRemove);
					docTableModel.removeRow(modelRow);
				} else {
					JOptionPane.showMessageDialog(desktopPane, "Please load appropriate documents!", "Information",
							JOptionPane.INFORMATION_MESSAGE);
				}
			}
		});
		desktopPane.setLayout(gl_desktopPane);

		setContentPane(desktopPane);
		updateContents();
	}

	public static List<Document> getLoadedDocuments() {
		return loadedDocuments;
	}

	private String getStringFromList(List<String> l) {
		String out = "";
		for (String str : l) {
			out += (out.length() > 0) ? " " : "";
			out += str;
		}
		return out;
	}

	public void addDocument(Document d) {
		if (docTableModel != null) {
			synchronized (loadedDocuments) {
				// add rows as loaded documents
				Vector<Object> dataRow = new Vector<Object>();

				dataRow.add(d.getTitleText());
				dataRow.add(getStringFromList(((Document) d).getAuthors()));
				if (d.getLSA() != null) {
					dataRow.add(d.getLSA().getPath());
				} else {
					dataRow.add("");
				}
				if (d.getLDA() != null) {
					dataRow.add(d.getLDA().getPath());
				} else {
					dataRow.add("");
				}

				docTableModel.addRow(dataRow);
				loadedDocuments.add(d);
			}

			if (loadedDocuments.size() > 0) {
				btnRemoveDocument.setEnabled(true);
				btnViewDocument.setEnabled(true);
				btnViewSimilarDocs.setEnabled(true);
			} else {
				btnRemoveDocument.setEnabled(false);
				btnViewDocument.setEnabled(false);
				btnViewSimilarDocs.setEnabled(false);
			}

			docTableModel.fireTableDataChanged();

		}
	}

	public void updateContents() {
		if (docTableModel != null) {
			synchronized (docTableModel) {
				// clean table
				while (docTableModel.getRowCount() > 0) {
					docTableModel.removeRow(0);
				}

				synchronized (loadedDocuments) {
					for (AbstractDocument d : loadedDocuments) {
						// add rows as loaded documents
						Vector<Object> dataRow = new Vector<Object>();

						if (d instanceof Document) {
							String title = d.getTitleText();
							String authors = getStringFromList(((Document) d).getAuthors());

							if (!title.toLowerCase().contains(queryArticleName.toLowerCase()))
								continue;
							if (!authors.toLowerCase().contains(queryAuthorName.toLowerCase()))
								continue;
						} else {
							continue;
						}

						dataRow.add(d.getTitleText());
						if (d instanceof Document) {
							dataRow.add(getStringFromList(((Document) d).getAuthors()));
						} else {
							dataRow.add("");
						}
						if (d.getLSA() != null) {
							dataRow.add(d.getLSA().getPath());
						} else {
							dataRow.add("");
						}
						if (d.getLDA() != null) {
							dataRow.add(d.getLDA().getPath());
						} else {
							dataRow.add("");
						}
						if (d instanceof Conversation)
							dataRow.add(true);
						else
							dataRow.add(false);
						docTableModel.addRow(dataRow);
					}

					if (loadedDocuments.size() > 0) {
						btnRemoveDocument.setEnabled(true);
						btnViewDocument.setEnabled(true);
						btnViewSimilarDocs.setEnabled(true);
					} else {
						btnRemoveDocument.setEnabled(false);
						btnViewDocument.setEnabled(false);
						btnViewSimilarDocs.setEnabled(false);
					}

					docTableModel.fireTableDataChanged();
				}
			}
		}
	}

	private void newFilter() {
		List<RowFilter<DocumentManagementTableModel, Object>> rfs = new ArrayList<RowFilter<DocumentManagementTableModel, Object>>(
				2);
		RowFilter<DocumentManagementTableModel, Object> rf = null;
		// If current expression doesn't parse, don't update.
		try {
			rf = RowFilter.regexFilter("(?i)" + queryArticleName, 0);
			rfs.add(rf);
			rf = RowFilter.regexFilter("(?i)" + queryAuthorName, 1);
			rfs.add(rf);
		} catch (java.util.regex.PatternSyntaxException e) {
			return;
		}
		rf = RowFilter.andFilter(rfs);
		docSorter.setRowFilter(rf);
	}

	public static void main(String[] args) {
		BasicConfigurator.configure();

		adjustToSystemGraphics();

		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				DocumentProcessingView view = new DocumentProcessingView();
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
