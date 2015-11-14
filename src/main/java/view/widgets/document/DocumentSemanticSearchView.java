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
import java.io.File;
import java.io.FilenameFilter;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
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
import javax.swing.JTextField;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.Painter;
import javax.swing.RowFilter;
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

import services.discourse.topicMining.TopicModeling;
import utils.localization.LocalizationUtils;
import view.models.document.DocumentManagementTableModel;
import view.widgets.ReaderBenchView;
import view.widgets.complexity.ComplexityIndicesView;
import view.widgets.document.corpora.PaperConceptView;
import view.widgets.document.corpora.PaperCorpusSimilarityView;
import view.widgets.document.corpora.PaperKeywordAbstractOverlap;
import view.widgets.document.search.SearchSimilarityView;
import DAO.AbstractDocument;
import DAO.cscl.Conversation;
import DAO.document.Document;
import edu.cmu.lti.jawjaw.pobj.Lang;

public class DocumentSemanticSearchView extends JInternalFrame {
	private static final long serialVersionUID = -8772215709851320157L;
	static Logger logger = Logger.getLogger(DocumentProcessingView.class);

	private JLabel lblLanguage;
	private JComboBox<String> comboBoxLanguage;
	
	private JButton btnAddSerializedDocument = null;
	private DocumentManagementTableModel docTableModel = null;
	private TableRowSorter<DocumentManagementTableModel> docSorter;
	private JDesktopPane desktopPane;
	private static File lastDirectory = null;

	private static List<AbstractDocument> allLoadedDocuments = new LinkedList<AbstractDocument>();
	private static List<Document> loadedDocuments = new LinkedList<Document>();
	private JButton btnKeywordsOverlap;
	private JButton btnConceptView;
	private JButton btnSimilarityView;
	private JButton btnSearch;
	private CustomTextField articleTextField;
	private CustomTextField authorsTextField;
	private String queryArticleName;
	private String queryAuthorName;
	private JLabel lblSearchQuery;
	private JTextField textFieldQuery;

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
				DocumentProcessingView.getAllLoadedDocuments().add(d);
				if (d instanceof Document)
					DocumentProcessingView.getLoadedDocuments().add((Document) d);
				addDocument(d);
			} else {
				JOptionPane.showMessageDialog(desktopPane, "Incorrect language for the loaded document!", "Information",
						JOptionPane.INFORMATION_MESSAGE);
			}
		}

		public Void doInBackground() {
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
					e.printStackTrace();
				}
			}
			return null;
		}

		@Override
		public void done() {
			Toolkit.getDefaultToolkit().beep();
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
	public DocumentSemanticSearchView() {
		setTitle("ReaderBench - " + LocalizationUtils.getTranslation("Semantic Search"));
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setResizable(true);
		setClosable(true);
		setMaximizable(true);
		setIconifiable(true);
		setBounds(20, 20, 819, 300);
		queryAuthorName = "";
		queryArticleName = "";

		lblLanguage = new JLabel(LocalizationUtils.getTranslation("Language") + ":");
		lblLanguage.setFont(new Font("SansSerif", Font.BOLD, 12));
		lblLanguage.setForeground(Color.BLACK);

		comboBoxLanguage = new JComboBox<String>();
		comboBoxLanguage.addItem("<< " + LocalizationUtils.getTranslation("Please select analysis language") + " >>");
		for (String lang : Lang.SUPPORTED_LANGUAGES)
			comboBoxLanguage.addItem(lang);

		btnAddSerializedDocument = new JButton(LocalizationUtils.getTranslation("Add preprocessed document(s)"));
		btnAddSerializedDocument.setEnabled(false);
		btnAddSerializedDocument.addActionListener(new ActionListener() {
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
						return f.getName().endsWith(".ser");
					}

					public String getDescription() {
						return "Serialized documents (*.ser) or directory";
					}
				});
				int returnVal = fc.showOpenDialog(DocumentSemanticSearchView.this);

				if (returnVal == JFileChooser.APPROVE_OPTION) {
					File file = fc.getSelectedFile();
					lastDirectory = file.getParentFile();
					DocumentSemanticSearchView.DocumentProcessingTask task = DocumentSemanticSearchView.this.new DocumentProcessingTask(
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
		docSorter = new TableRowSorter<DocumentManagementTableModel>(docTableModel);

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

		JPanel panelAllDocs = new JPanel();
		panelAllDocs.setBackground(Color.WHITE);
		panelAllDocs.setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null),
				LocalizationUtils.getTranslation("Generalized analysis for all loaded documents"), TitledBorder.LEADING, TitledBorder.TOP, null, null));

		JPanel panelSingleDoc = new JPanel();
		panelSingleDoc.setBackground(Color.WHITE);
		panelSingleDoc.setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null),
				LocalizationUtils.getTranslation("Specific document operations"), TitledBorder.LEFT, TitledBorder.TOP, null, null));

		GroupLayout gl_desktopPane = new GroupLayout(desktopPane);
		gl_desktopPane.setHorizontalGroup(
			gl_desktopPane.createParallelGroup(Alignment.TRAILING)
				.addGroup(gl_desktopPane.createSequentialGroup()
					.addContainerGap()
					.addGroup(gl_desktopPane.createParallelGroup(Alignment.LEADING)
						.addGroup(gl_desktopPane.createSequentialGroup()
							.addComponent(lblLanguage)
							.addGap(2)
							.addComponent(comboBoxLanguage, 0, 718, Short.MAX_VALUE))
						.addComponent(panelSingleDoc, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
						.addComponent(panelAllDocs, Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, 779, Short.MAX_VALUE))
					.addContainerGap())
		);
		gl_desktopPane.setVerticalGroup(
			gl_desktopPane.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_desktopPane.createSequentialGroup()
					.addContainerGap()
					.addGroup(gl_desktopPane.createParallelGroup(Alignment.LEADING, false)
						.addComponent(lblLanguage, GroupLayout.PREFERRED_SIZE, 25, GroupLayout.PREFERRED_SIZE)
						.addComponent(comboBoxLanguage, GroupLayout.PREFERRED_SIZE, 25, GroupLayout.PREFERRED_SIZE))
					.addGap(28)
					.addComponent(panelSingleDoc, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
					.addGap(18)
					.addComponent(panelAllDocs, GroupLayout.PREFERRED_SIZE, 93, GroupLayout.PREFERRED_SIZE)
					.addContainerGap(383, Short.MAX_VALUE))
		);

		articleTextField = new CustomTextField(1);
		articleTextField.setPlaceholder(LocalizationUtils.getTranslation("Insert Article Name"));
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
		authorsTextField.setPlaceholder(LocalizationUtils.getTranslation("Insert Author Name"));
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
		articleTextField.setColumns(25);
		authorsTextField.setColumns(25);
		GroupLayout gl_panelSingleDoc = new GroupLayout(panelSingleDoc);
		gl_panelSingleDoc.setHorizontalGroup(
			gl_panelSingleDoc.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panelSingleDoc.createSequentialGroup()
					.addComponent(btnAddSerializedDocument)
					.addGap(578))
		);
		gl_panelSingleDoc.setVerticalGroup(
			gl_panelSingleDoc.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panelSingleDoc.createSequentialGroup()
					.addGap(5)
					.addComponent(btnAddSerializedDocument))
		);
		panelSingleDoc.setLayout(gl_panelSingleDoc);

		btnSimilarityView = new JButton(LocalizationUtils.getTranslation("Similarity View"));
		btnSimilarityView.setEnabled(false);
		btnSimilarityView.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if (loadedDocuments == null || loadedDocuments.size() == 0) {
					JOptionPane.showMessageDialog(DocumentSemanticSearchView.this, "Please load appropriate documents!",
							"Error", JOptionPane.WARNING_MESSAGE);
					return;
				}

				PaperCorpusSimilarityView view = new PaperCorpusSimilarityView(loadedDocuments);
				view.setVisible(true);
			}
		});

		lblSearchQuery = new JLabel(LocalizationUtils.getTranslation("Find semantically related docs to query") + ":");

		textFieldQuery = new JTextField();
		textFieldQuery.setColumns(10);

		btnConceptView = new JButton(LocalizationUtils.getTranslation("Concept View"));
		btnConceptView.setEnabled(false);
		btnConceptView.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (loadedDocuments == null || loadedDocuments.size() == 0) {
					JOptionPane.showMessageDialog(DocumentSemanticSearchView.this,
							"Please load at least one appropriate document!", "Error", JOptionPane.WARNING_MESSAGE);
					return;
				}
				PaperConceptView conceptView = new PaperConceptView(TopicModeling.getCollectionTopics(loadedDocuments),
						"out/concepts_" + new Timestamp(new Date().getTime()) + ".pdf");
				conceptView.setVisible(true);
			}
		});

		btnKeywordsOverlap = new JButton(LocalizationUtils.getTranslation("Keyword-Abstract overlaps"));
		btnKeywordsOverlap.setEnabled(false);
		btnKeywordsOverlap.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (loadedDocuments == null || loadedDocuments.size() == 0) {
					JOptionPane.showMessageDialog(DocumentSemanticSearchView.this, "Please load appropriate documents!",
							"Error", JOptionPane.WARNING_MESSAGE);
					return;
				}
				PaperKeywordAbstractOverlap view = new PaperKeywordAbstractOverlap(loadedDocuments);
				view.setVisible(true);
			}
		});

		btnSearch = new JButton(LocalizationUtils.getTranslation("Search"));
		btnSearch.setEnabled(false);
		btnSearch.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (loadedDocuments == null || loadedDocuments.size() == 0) {
					JOptionPane.showMessageDialog(DocumentSemanticSearchView.this, "Please load appropriate documents!",
							"Error", JOptionPane.WARNING_MESSAGE);
					return;
				}
				if (textFieldQuery.getText().length() == 0) {
					JOptionPane.showMessageDialog(DocumentSemanticSearchView.this, "Please input a query string!", "Error",
							JOptionPane.WARNING_MESSAGE);
					return;
				}
				SearchSimilarityView view = new SearchSimilarityView(loadedDocuments, textFieldQuery.getText());
				view.setVisible(true);
			}
		});
		GroupLayout gl_panelAllDocs = new GroupLayout(panelAllDocs);
		gl_panelAllDocs.setHorizontalGroup(gl_panelAllDocs.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panelAllDocs.createSequentialGroup()
						.addGroup(gl_panelAllDocs.createParallelGroup(Alignment.LEADING)
								.addGroup(gl_panelAllDocs.createSequentialGroup().addContainerGap()
										.addComponent(lblSearchQuery).addPreferredGap(ComponentPlacement.RELATED)
										.addComponent(textFieldQuery, GroupLayout.DEFAULT_SIZE, 575, Short.MAX_VALUE))
						.addGroup(gl_panelAllDocs.createSequentialGroup().addComponent(btnSimilarityView)
								.addPreferredGap(ComponentPlacement.RELATED).addComponent(btnConceptView)
								.addPreferredGap(ComponentPlacement.RELATED).addComponent(btnKeywordsOverlap)))
						.addPreferredGap(ComponentPlacement.RELATED).addComponent(btnSearch).addContainerGap()));
		gl_panelAllDocs.setVerticalGroup(gl_panelAllDocs.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panelAllDocs.createSequentialGroup().addGap(5)
						.addGroup(gl_panelAllDocs.createParallelGroup(Alignment.BASELINE).addComponent(lblSearchQuery)
								.addComponent(textFieldQuery, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
										GroupLayout.PREFERRED_SIZE)
								.addComponent(btnSearch))
				.addPreferredGap(ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
				.addGroup(gl_panelAllDocs.createParallelGroup(Alignment.BASELINE).addComponent(btnSimilarityView)
						.addComponent(btnConceptView).addComponent(btnKeywordsOverlap)).addContainerGap()));
		panelAllDocs.setLayout(gl_panelAllDocs);
		desktopPane.setLayout(gl_desktopPane);

		setContentPane(desktopPane);
		updateContents();
	}

	public static List<AbstractDocument> getAllLoadedDocuments() {
		return allLoadedDocuments;
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

	public void addDocument(AbstractDocument d) {
		if (docTableModel != null) {
			synchronized (docTableModel) {

				synchronized (allLoadedDocuments) {
					// add rows as loaded documents
					Vector<Object> dataRow = new Vector<Object>();

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
					
					docTableModel.addRow(dataRow);
				}

				if (allLoadedDocuments.size() > 0) {
					btnKeywordsOverlap.setEnabled(true);
					btnConceptView.setEnabled(true);
					btnSimilarityView.setEnabled(true);
					btnSearch.setEnabled(true);
				} else {
					btnKeywordsOverlap.setEnabled(false);
					btnConceptView.setEnabled(false);
					btnSimilarityView.setEnabled(false);
					btnSearch.setEnabled(false);
				}

				docTableModel.fireTableDataChanged();
			}
		}
	}

	public void updateContents() {
		if (docTableModel != null) {
			synchronized (docTableModel) {
				// clean table
				while (docTableModel.getRowCount() > 0) {
					docTableModel.removeRow(0);
				}

				synchronized (allLoadedDocuments) {
					for (AbstractDocument d : allLoadedDocuments) {
						// add rows as loaded documents
						Vector<Object> dataRow = new Vector<Object>();

						if (d instanceof Document) {
							String title = d.getTitleText();
							String authors = getStringFromList(((Document) d).getAuthors());

							if (!title.toLowerCase().contains(queryArticleName.toLowerCase()))
								continue;
							if (!authors.toLowerCase().contains(queryAuthorName.toLowerCase()))
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

					if (allLoadedDocuments.size() > 0) {
						btnKeywordsOverlap.setEnabled(true);
						btnConceptView.setEnabled(true);
						btnSimilarityView.setEnabled(true);
					} else {
						btnKeywordsOverlap.setEnabled(false);
						btnConceptView.setEnabled(false);
						btnSimilarityView.setEnabled(false);
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
