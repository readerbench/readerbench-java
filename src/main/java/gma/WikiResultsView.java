package gma;

import data.AnalysisElement;
import java.awt.Color;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import gma.models.Cell;
import view.widgets.chat.models.WikiResult;

public class WikiResultsView extends JFrame {
	private static final long serialVersionUID = 1L;

	ArrayList<WikiResult> results;
	private final String USER_AGENT = "Chrome/39.0.2171.95";
	private AnalysisElement doc;
	public double cohesion = 0.0;

	/*
	 * the document we calculate the cohesion against. If called from the table
	 * model the chat file is passed
	 */
	public WikiResultsView(ArrayList<Cell> inputCells, AnalysisElement doc) {

		this.doc = doc;

		// UI related
		setTitle("Search results");

		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		getContentPane().setBackground(Color.WHITE);

		// adjust view
		setBounds(30, 50, 1000, 500);
		
		if (inputCells != null && inputCells.size() > 0) {
				results = WikiSearchUtils.gmaWikiSearch(inputCells);
		}

		setupView();

	}

	private void setupView() {
		JPanel wikiResultsPanel = new JPanel();
		wikiResultsPanel.setBackground(Color.WHITE);
		GridLayout gridLayout = new GridLayout(0, 3);
		gridLayout.setVgap(30);
		int borderSize = 20;
		wikiResultsPanel.setLayout(gridLayout);
		wikiResultsPanel.setBorder(BorderFactory.createEmptyBorder(borderSize,
				borderSize, borderSize, borderSize));

		double cohesion = 0;
		for (int i = 0; i < results.size(); i++) {

			 cohesion = new
			 DocumentCohesionComputer(results.get(i).getSnippet(), doc)
			 .getCohesion();
			 
			 results.get(i).cohesion = cohesion;
			 
			 if(cohesion<0.15){
				 continue;
			 }
			 
			 

			JLabel label = new JLabel(results.get(i).getTitle());
			label.setFont(new Font("Verdana", Font.BOLD, 14));
			wikiResultsPanel.add(label);

			JLabel cohesionLabel = new JLabel("Estimated relevance: "
					+ cohesion);
			cohesionLabel.setFont(new Font("Verdana", Font.ITALIC, 12));
			wikiResultsPanel.add(cohesionLabel);

			JButton button = new JButton();
			String urlString = results.get(i).getLink();

			button.setText("Go to page >>");
			button.setSize(new Dimension(40, 10));
			wikiResultsPanel.add(button);

			try {
				button.addActionListener(new OpenUrlAction(urlString));
			} catch (URISyntaxException e) {
				e.printStackTrace();
			}

			// wikiResultsPanel.add(panel);
		}
		JScrollPane scorllPane = new JScrollPane(wikiResultsPanel);
		this.add(scorllPane);
	}

	class OpenUrlAction implements ActionListener {

		final URI uri;

		public OpenUrlAction(String urlString) throws URISyntaxException {
			uri = new URI(urlString);
		}

		public void actionPerformed(ActionEvent e) {
			open(uri);
		}
	}

	private static void open(URI uri) {
		if (Desktop.isDesktopSupported()) {
			try {
				Desktop.getDesktop().browse(uri);
			} catch (IOException e) {
			}
		} else {
		}
	}
}
