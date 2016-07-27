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
	private JTextField textFieldActivationThreshold;
	private JTextField textFieldNoActiveWords;
	private JTextField textFieldNoActiveWordsIncrement;
	private JTextArea textAreaContent;
	private JLabel label_2;

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

		JLabel lblComplexityLevel = new JLabel("Semantic Model:");
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
		
		JLabel label = new JLabel("Activation Threshold:");
		label.setFont(new Font("SansSerif", Font.BOLD, 12));
		
		textFieldActivationThreshold = new JTextField();
		textFieldActivationThreshold.setText("0.3");
		textFieldActivationThreshold.setColumns(10);
		
		JLabel label_1 = new JLabel("Active Words:");
		label_1.setFont(new Font("SansSerif", Font.BOLD, 12));
		
		textFieldNoActiveWords = new JTextField();
		textFieldNoActiveWords.setText("3");
		textFieldNoActiveWords.setColumns(10);
		
		label_2 = new JLabel("A.W. Increment:");
		label_2.setFont(new Font("SansSerif", Font.BOLD, 12));
		
		textFieldNoActiveWordsIncrement = new JTextField();
		textFieldNoActiveWordsIncrement.setText("1");
		textFieldNoActiveWordsIncrement.setColumns(10);
		
		
		GroupLayout gl_contentPane = new GroupLayout(contentPane);
		gl_contentPane.setHorizontalGroup(
			gl_contentPane.createParallelGroup(Alignment.TRAILING)
				.addGroup(gl_contentPane.createSequentialGroup()
					.addContainerGap()
					.addGroup(gl_contentPane.createParallelGroup(Alignment.LEADING)
						.addGroup(gl_contentPane.createSequentialGroup()
							.addGap(471)
							.addComponent(btnNewButton, GroupLayout.PREFERRED_SIZE, 222, GroupLayout.PREFERRED_SIZE))
						.addComponent(textAreaContent, GroupLayout.PREFERRED_SIZE, 693, GroupLayout.PREFERRED_SIZE)
						.addComponent(lblText)
						.addGroup(gl_contentPane.createSequentialGroup()
							.addGroup(gl_contentPane.createParallelGroup(Alignment.LEADING)
								.addGroup(gl_contentPane.createSequentialGroup()
									.addComponent(lblComplexityLevel)
									.addPreferredGap(ComponentPlacement.UNRELATED)
									.addComponent(txtFieldHdpGrade, GroupLayout.PREFERRED_SIZE, 42, GroupLayout.PREFERRED_SIZE))
								.addGroup(gl_contentPane.createSequentialGroup()
									.addComponent(label_1, GroupLayout.PREFERRED_SIZE, 94, GroupLayout.PREFERRED_SIZE)
									.addPreferredGap(ComponentPlacement.UNRELATED)
									.addComponent(textFieldNoActiveWords, GroupLayout.PREFERRED_SIZE, 42, GroupLayout.PREFERRED_SIZE)))
							.addGroup(gl_contentPane.createParallelGroup(Alignment.LEADING)
								.addGroup(gl_contentPane.createSequentialGroup()
									.addGap(29)
									.addComponent(lblSource)
									.addPreferredGap(ComponentPlacement.RELATED)
									.addComponent(textFieldSimilarWords, GroupLayout.PREFERRED_SIZE, 44, GroupLayout.PREFERRED_SIZE)
									.addGap(35)
									.addComponent(label, GroupLayout.PREFERRED_SIZE, 129, GroupLayout.PREFERRED_SIZE)
									.addPreferredGap(ComponentPlacement.RELATED)
									.addComponent(textFieldActivationThreshold, GroupLayout.PREFERRED_SIZE, 44, GroupLayout.PREFERRED_SIZE))
								.addGroup(gl_contentPane.createSequentialGroup()
									.addGap(18)
									.addComponent(label_2, GroupLayout.PREFERRED_SIZE, 94, GroupLayout.PREFERRED_SIZE)
									.addPreferredGap(ComponentPlacement.RELATED)
									.addComponent(textFieldNoActiveWordsIncrement, GroupLayout.PREFERRED_SIZE, 44, GroupLayout.PREFERRED_SIZE)))))
					.addContainerGap(17, Short.MAX_VALUE))
		);
		gl_contentPane.setVerticalGroup(
			gl_contentPane.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_contentPane.createSequentialGroup()
					.addContainerGap()
					.addGroup(gl_contentPane.createParallelGroup(Alignment.BASELINE)
						.addComponent(lblComplexityLevel)
						.addComponent(txtFieldHdpGrade, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(lblSource)
						.addComponent(textFieldSimilarWords, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(label)
						.addComponent(textFieldActivationThreshold, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
					.addGap(28)
					.addGroup(gl_contentPane.createParallelGroup(Alignment.BASELINE)
						.addComponent(label_1)
						.addComponent(textFieldNoActiveWords, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(label_2)
						.addComponent(textFieldNoActiveWordsIncrement, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
					.addGap(27)
					.addComponent(lblText, GroupLayout.PREFERRED_SIZE, 16, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.UNRELATED)
					.addComponent(textAreaContent, GroupLayout.PREFERRED_SIZE, 414, GroupLayout.PREFERRED_SIZE)
					.addGap(35)
					.addComponent(btnNewButton)
					.addGap(38))
		);
		contentPane.setLayout(gl_contentPane);
	}
	
	public void openComprehensionModel() {
		int hdpGrade = Integer.parseInt(this.txtFieldHdpGrade.getText());
		int noSimilarWords = Integer.parseInt(this.textFieldSimilarWords.getText());
		double activationThreshold = Double.parseDouble(this.textFieldActivationThreshold.getText());
		int noActiveWords = Integer.parseInt(this.textFieldNoActiveWords.getText());
		int noActiveWordsIncrement = Integer.parseInt(this.textFieldNoActiveWordsIncrement.getText());
		
		String text = this.textAreaContent.getText();
		
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				ComprehensionModel ciModel = new ComprehensionModel(text, hdpGrade, noSimilarWords, activationThreshold, noActiveWords, noActiveWordsIncrement);
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
