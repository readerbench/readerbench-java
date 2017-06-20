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
package view.widgets.document;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDesktopPane;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.Painter;
import javax.swing.RowFilter;
import javax.swing.UIDefaults;
import javax.swing.UIManager;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.TableRowSorter;



import services.discourse.keywordMining.KeywordModeling;
import utils.localization.LocalizationUtils;
import view.models.document.DocumentManagementTableModel;
import view.widgets.document.corpora.DocConceptView;
import view.widgets.document.corpora.DocCorpusSimilarityView;
import view.widgets.document.corpora.DocKeywordAbstractOverlap;
import view.widgets.document.search.SearchSimilarityView;

public class DocumentSemanticSearchView extends JInternalFrame {
	private static final long serialVersionUID = -8772215709851320157L;
	static Logger logger = Logger.getLogger("");

	private TableRowSorter<DocumentManagementTableModel> docSorter;
	private JDesktopPane desktopPane;

	private JButton btnKeywordsOverlap;
	private JButton btnConceptView;
	private JButton btnSimilarityView;
	private JButton btnSearch;
	private CustomTextField articleTextField;
	private String queryArticleName;
	private String queryAuthorName;
	private JLabel lblSearchQuery;
	private JTextField textFieldQuery;

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
		setClosable(true);
		setMaximizable(true);
		setIconifiable(true);
		setBounds(20, 20, 830, 132);
		queryAuthorName = "";
		queryArticleName = "";

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
				LocalizationUtils.getLocalizedString(this.getClass(), LocalizationUtils.TEXT, "panelSearchDocs"),
				TitledBorder.LEADING, TitledBorder.TOP, null, null));

		GroupLayout gl_desktopPane = new GroupLayout(desktopPane);
		gl_desktopPane
				.setHorizontalGroup(gl_desktopPane.createParallelGroup(Alignment.TRAILING).addGroup(Alignment.LEADING,
						gl_desktopPane.createSequentialGroup().addContainerGap()
								.addComponent(panelAllDocs, GroupLayout.DEFAULT_SIZE, 806, Short.MAX_VALUE)
								.addContainerGap()));
		gl_desktopPane.setVerticalGroup(gl_desktopPane.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_desktopPane.createSequentialGroup().addContainerGap()
						.addComponent(panelAllDocs, GroupLayout.PREFERRED_SIZE, 89, Short.MAX_VALUE)
						.addContainerGap()));

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

		articleTextField.setColumns(25);

		btnSimilarityView = new JButton(LocalizationUtils.getTranslation("Similarity View"));
		btnSimilarityView.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if (DocumentProcessingView.getLoadedDocuments() == null
						|| DocumentProcessingView.getLoadedDocuments().size() == 0) {
					JOptionPane.showMessageDialog(DocumentSemanticSearchView.this,
							"Please load appropriate documents from the Document Processing view!", "Error",
							JOptionPane.WARNING_MESSAGE);
					return;
				}

				DocCorpusSimilarityView view = new DocCorpusSimilarityView(
						DocumentProcessingView.getLoadedDocuments());
				view.setVisible(true);
			}
		});

		lblSearchQuery = new JLabel(LocalizationUtils.getTranslation("Find semantically related docs to query") + ":");

		textFieldQuery = new JTextField();
		textFieldQuery.setColumns(10);

		btnConceptView = new JButton(LocalizationUtils.getTranslation("Concept View"));
		btnConceptView.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (DocumentProcessingView.getLoadedDocuments() == null
						|| DocumentProcessingView.getLoadedDocuments().size() == 0) {
					JOptionPane.showMessageDialog(DocumentSemanticSearchView.this,
							"Please load appropriate documents from the Document Processing view!", "Error",
							JOptionPane.WARNING_MESSAGE);
					return;
				}
				DocConceptView conceptView = new DocConceptView(
						KeywordModeling.getCollectionTopics(DocumentProcessingView.getLoadedDocuments()),
						"out/concepts_" + new Timestamp(new Date().getTime()) + ".pdf");
				conceptView.setVisible(true);
			}
		});

		btnKeywordsOverlap = new JButton(LocalizationUtils.getTranslation("Keyword-Abstract overlaps"));
		btnKeywordsOverlap.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (DocumentProcessingView.getLoadedDocuments() == null
						|| DocumentProcessingView.getLoadedDocuments().size() == 0) {
					JOptionPane.showMessageDialog(DocumentSemanticSearchView.this,
							"Please load appropriate documents from the Document Processing view!", "Error",
							JOptionPane.WARNING_MESSAGE);
					return;
				}
				DocKeywordAbstractOverlap view = new DocKeywordAbstractOverlap(
						DocumentProcessingView.getLoadedDocuments());
				view.setVisible(true);
			}
		});

		btnSearch = new JButton(LocalizationUtils.getTranslation("Search"));
		btnSearch.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (DocumentProcessingView.getLoadedDocuments() == null
						|| DocumentProcessingView.getLoadedDocuments().size() == 0) {
					JOptionPane.showMessageDialog(DocumentSemanticSearchView.this,
							"Please load appropriate documents from the Document Processing view!", "Error",
							JOptionPane.WARNING_MESSAGE);
					return;
				}
				if (textFieldQuery.getText().length() == 0) {
					JOptionPane.showMessageDialog(DocumentSemanticSearchView.this, "Please input a query string!",
							"Error", JOptionPane.WARNING_MESSAGE);
					return;
				}
				SearchSimilarityView view = new SearchSimilarityView(DocumentProcessingView.getLoadedDocuments(),
						textFieldQuery.getText());
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
}
