package view.widgets.chat;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;

import data.cscl.Community;
import utils.localization.LocalizationUtils;

public class VCoPView extends JFrame {
	private static final long serialVersionUID = 8894652868238113117L;
	static Logger logger = Logger.getLogger(VCoPView.class);

	private JPanel contentPane;
	private JTextField textFieldPath;

	private static File lastDirectory = null;

	private class CustomTextField extends JFormattedTextField {
		private static final long serialVersionUID = 1L;

		private Font originalFont;
		private Color originalForeground;
		/**
		 * Grey by default*
		 */
		private Color placeholderForeground = new Color(160, 160, 160);
		private boolean textWrittenIn;

		public CustomTextField(DateFormat df) {
			super(df);
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

	JTextField frmtdtxtfldInputdatetext;
	JTextField frmtdtxtfldFinaldatetext;

	/**
	 * Create the frame.
	 */
	public VCoPView() {
		setTitle("ReaderBench - " + LocalizationUtils.getTranslation("View virtual Communities of Practice"));
		setResizable(false);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setBounds(100, 100, 675, 325);
		contentPane = new JPanel();
		contentPane.setBackground(Color.WHITE);
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);

		JLabel lblPath = new JLabel(LocalizationUtils.getTranslation("Path") + ":");

		textFieldPath = new JTextField();
		textFieldPath.setText("resources/in/MOOC/forum_posts&comments");
		textFieldPath.setColumns(10);

		JButton btnSearch = new JButton("...");
		btnSearch.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFileChooser fc = null;
				if (lastDirectory == null)
					fc = new JFileChooser(new File("resources/in"));
				else
					fc = new JFileChooser(lastDirectory);
				fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				int returnVal = fc.showOpenDialog(VCoPView.this);

				if (returnVal == JFileChooser.APPROVE_OPTION) {
					File file = fc.getSelectedFile();
					lastDirectory = file.getParentFile();
					textFieldPath.setText(file.getPath());
				}
			}
		});

		JPanel panelViewCommunity = new JPanel();
		panelViewCommunity.setBackground(Color.WHITE);
		panelViewCommunity.setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null),
				LocalizationUtils.getTranslation("View"), TitledBorder.LEADING, TitledBorder.TOP, null, null));

		GroupLayout gl_contentPane = new GroupLayout(contentPane);
		gl_contentPane
				.setHorizontalGroup(
						gl_contentPane.createParallelGroup(Alignment.LEADING)
								.addGroup(gl_contentPane.createSequentialGroup().addContainerGap()
										.addGroup(gl_contentPane.createParallelGroup(Alignment.LEADING)
												.addGroup(gl_contentPane.createSequentialGroup()
														.addComponent(panelViewCommunity, GroupLayout.DEFAULT_SIZE, 635,
																Short.MAX_VALUE)
														.addContainerGap())
								.addGroup(
										gl_contentPane.createSequentialGroup().addComponent(lblPath).addGap(110)
												.addComponent(textFieldPath, GroupLayout.DEFAULT_SIZE, 435,
														Short.MAX_VALUE)
												.addPreferredGap(ComponentPlacement.RELATED).addComponent(btnSearch,
														GroupLayout.PREFERRED_SIZE, 41, GroupLayout.PREFERRED_SIZE)
								.addGap(6)))));
		gl_contentPane.setVerticalGroup(gl_contentPane.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_contentPane.createSequentialGroup()
						.addGroup(gl_contentPane.createParallelGroup(Alignment.BASELINE).addComponent(lblPath)
								.addComponent(btnSearch).addComponent(textFieldPath, GroupLayout.PREFERRED_SIZE,
										GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
						.addGap(27)
						.addComponent(panelViewCommunity, GroupLayout.PREFERRED_SIZE, 214, GroupLayout.PREFERRED_SIZE)
						.addContainerGap(64, Short.MAX_VALUE)));

		JButton btnViewCommunity = new JButton(LocalizationUtils.getTranslation("View community"));

		JLabel lblInitialDate = new JLabel(LocalizationUtils.getTranslation("Initial Date") + ":");
		JLabel lblFinalDate = new JLabel(LocalizationUtils.getTranslation("Final Date") + ":");
		DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
		frmtdtxtfldInputdatetext = new CustomTextField(dateFormat);
		((CustomTextField) frmtdtxtfldInputdatetext).setPlaceholder("dd-MM-yyyy");

		frmtdtxtfldFinaldatetext = new CustomTextField(dateFormat);
		((CustomTextField) frmtdtxtfldFinaldatetext).setPlaceholder("dd-MM-yyyy");

		GroupLayout gl_panelViewCommunity = new GroupLayout(panelViewCommunity);
		gl_panelViewCommunity.setHorizontalGroup(gl_panelViewCommunity.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panelViewCommunity.createSequentialGroup().addContainerGap()
						.addGroup(gl_panelViewCommunity.createParallelGroup(Alignment.LEADING)
								.addComponent(btnViewCommunity, Alignment.TRAILING)
								.addGroup(gl_panelViewCommunity.createSequentialGroup()
										.addGroup(gl_panelViewCommunity.createParallelGroup(Alignment.TRAILING)
												.addComponent(lblInitialDate))
								.addPreferredGap(ComponentPlacement.UNRELATED)
								.addGroup(gl_panelViewCommunity.createParallelGroup(Alignment.LEADING)
										.addGroup(gl_panelViewCommunity.createParallelGroup(Alignment.TRAILING, false)
												.addComponent(frmtdtxtfldFinaldatetext, Alignment.LEADING)
												.addComponent(frmtdtxtfldInputdatetext, Alignment.LEADING,
														GroupLayout.PREFERRED_SIZE, 217, GroupLayout.PREFERRED_SIZE))))
						.addComponent(lblFinalDate)).addContainerGap()));
		gl_panelViewCommunity
				.setVerticalGroup(
						gl_panelViewCommunity
								.createParallelGroup(
										Alignment.LEADING)
								.addGroup(gl_panelViewCommunity.createSequentialGroup().addContainerGap()
										.addGroup(gl_panelViewCommunity.createParallelGroup(Alignment.BASELINE))
										.addGap(32)
										.addGroup(gl_panelViewCommunity.createParallelGroup(Alignment.BASELINE)
												.addComponent(lblInitialDate).addComponent(frmtdtxtfldInputdatetext,
														GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
														GroupLayout.PREFERRED_SIZE))
						.addPreferredGap(ComponentPlacement.RELATED, 40, Short.MAX_VALUE)
						.addGroup(
								gl_panelViewCommunity.createParallelGroup(Alignment.BASELINE).addComponent(lblFinalDate)
										.addComponent(frmtdtxtfldFinaldatetext, GroupLayout.PREFERRED_SIZE,
												GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
										.addGap(13).addComponent(btnViewCommunity).addContainerGap()));
		panelViewCommunity.setLayout(gl_panelViewCommunity);
		btnViewCommunity.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (!textFieldPath.getText().equals("")) {
					String date1Str = frmtdtxtfldInputdatetext.getText();
					Date date1 = parseDate(date1Str);
					String date2Str = frmtdtxtfldFinaldatetext.getText();
					Date date2 = parseDate(date2Str);

					System.out.println("date1=" + date1 + ", date2=" + date2);

					setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
					Community.processDocumentCollection(textFieldPath.getText(), false, date1, date2, 0, 7);
					Toolkit.getDefaultToolkit().beep();
					setCursor(null); // turn off the wait cursor
				} else
					JOptionPane.showMessageDialog(VCoPView.this,
							"Please select an appropriate input folder to be visualized!", "Error",
							JOptionPane.WARNING_MESSAGE);
			}
		});
		contentPane.setLayout(gl_contentPane);
	}

	private static Date parseDate(String str) {
		DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
		try {
			return dateFormat.parse(str);
		} catch (Exception e) {
			return null;
		}
	}

	public static void main(String[] args) {
		BasicConfigurator.configure();

		adjustToSystemGraphics();

		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				VCoPView view = new VCoPView();
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
