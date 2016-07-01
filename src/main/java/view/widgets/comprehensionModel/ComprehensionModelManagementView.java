package view.widgets.comprehensionModel;

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;


import services.comprehensionModel.ComprehensionModel;
import utils.localization.LocalizationUtils;

public class ComprehensionModelManagementView extends JFrame {
	private static final long serialVersionUID = -2864356905020607155L;
	static Logger logger = Logger.getLogger(ComprehensionModelManagementView.class);

	private JPanel contentPane;
	private JTextField txtFieldHdpGrade;
	private JTextField textFieldSimilarWords;
	private JTextArea textAreaContent;

	/**
	 * Create the frame.
	 */
	public ComprehensionModelManagementView() {
		setTitle("Comprehension Model Parameters");
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setBounds(100, 100, 750, 700);

		contentPane = new JPanel();
		contentPane.setBackground(Color.WHITE);
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);

		JLabel lblComplexityLevel = new JLabel("HDP Grade:");
		lblComplexityLevel.setFont(new Font("SansSerif", Font.BOLD, 12));

		txtFieldHdpGrade = new JTextField();
		txtFieldHdpGrade.setColumns(10);
		txtFieldHdpGrade.setText("2");

		JLabel lblSource = new JLabel("Similar Words:");
		lblSource.setFont(new Font("SansSerif", Font.BOLD, 12));

		textFieldSimilarWords = new JTextField();
		textFieldSimilarWords.setColumns(10);
		textFieldSimilarWords.setText("3");

		JLabel lblText = new JLabel(LocalizationUtils.getTranslation("Text"));
		lblText.setFont(new Font("SansSerif", Font.BOLD, 12));

		JSeparator separator = new JSeparator();
		
		textAreaContent = new JTextArea();
		textAreaContent.setLineWrap(true);
		textAreaContent.setWrapStyleWord(true);
		Border border = BorderFactory.createLineBorder(Color.BLACK);
		textAreaContent.setBorder(BorderFactory.createCompoundBorder(border, 
		            BorderFactory.createEmptyBorder(10, 10, 10, 10)));
		textAreaContent.setText("I went to the coast last weekend with Sally. It was sunny. We had checked the tide schedules and planned to arrive at low tide. I just love beachcombing. Right off, I found three whole sand dollars.");
		
		JButton btnNewButton = new JButton("Comprehension Model");
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				ComprehensionModelManagementView.this.openComprehensionModel();
			}
		});
		
		GroupLayout gl_contentPane = new GroupLayout(contentPane);
		gl_contentPane.setHorizontalGroup(
			gl_contentPane.createParallelGroup(Alignment.TRAILING)
				.addGroup(gl_contentPane.createSequentialGroup()
					.addContainerGap()
					.addGroup(gl_contentPane.createParallelGroup(Alignment.TRAILING)
						.addGroup(gl_contentPane.createSequentialGroup()
							.addGroup(gl_contentPane.createParallelGroup(Alignment.TRAILING)
								.addComponent(textAreaContent, GroupLayout.DEFAULT_SIZE, 689, Short.MAX_VALUE)
								.addComponent(separator, GroupLayout.DEFAULT_SIZE, 689, Short.MAX_VALUE)
								.addComponent(btnNewButton, GroupLayout.PREFERRED_SIZE, 222, GroupLayout.PREFERRED_SIZE))
							.addGap(9))
						.addGroup(gl_contentPane.createSequentialGroup()
							.addComponent(lblComplexityLevel)
							.addPreferredGap(ComponentPlacement.UNRELATED)
							.addComponent(txtFieldHdpGrade, GroupLayout.PREFERRED_SIZE, 42, GroupLayout.PREFERRED_SIZE)
							.addGap(29)
							.addComponent(lblSource)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(textFieldSimilarWords, GroupLayout.PREFERRED_SIZE, 44, GroupLayout.PREFERRED_SIZE)
							.addGap(418))
						.addComponent(lblText))
					.addContainerGap())
		);
		gl_contentPane.setVerticalGroup(
			gl_contentPane.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_contentPane.createSequentialGroup()
					.addContainerGap()
					.addGroup(gl_contentPane.createParallelGroup(Alignment.BASELINE)
						.addComponent(lblComplexityLevel)
						.addComponent(txtFieldHdpGrade, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(lblSource)
						.addComponent(textFieldSimilarWords, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
					.addGap(23)
					.addComponent(lblText, GroupLayout.PREFERRED_SIZE, 16, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.UNRELATED)
					.addComponent(separator, GroupLayout.PREFERRED_SIZE, 2, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.UNRELATED)
					.addComponent(textAreaContent, GroupLayout.PREFERRED_SIZE, 414, GroupLayout.PREFERRED_SIZE)
					.addGap(63)
					.addComponent(btnNewButton)
					.addGap(39))
		);
		contentPane.setLayout(gl_contentPane);
	}
	
	public void openComprehensionModel() {
		int hdpGrade = Integer.parseInt(this.txtFieldHdpGrade.getText());
		int noSimilarWords = Integer.parseInt(this.textFieldSimilarWords.getText());
		String text = this.textAreaContent.getText();
		
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				ComprehensionModel ciModel = new ComprehensionModel(text, hdpGrade, noSimilarWords);
				ComprehensionModelView view = new ComprehensionModelView(ciModel);
				view.setVisible(true);
			}
		});
	}
	
	public static void main(String[] args) {
		BasicConfigurator.configure();
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				ComprehensionModelManagementView view = new ComprehensionModelManagementView();
				view.setVisible(true);
			}
		});
	}
}
