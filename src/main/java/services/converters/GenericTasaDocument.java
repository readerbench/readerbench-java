package services.converters;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;

import view.widgets.complexity.RunMeasurementsView;
import DAO.AbstractDocumentTemplate;
import DAO.AbstractDocumentTemplate.BlockTemplate;
import DAO.Block;
import DAO.Sentence;
import DAO.Word;
import DAO.document.Document;
import edu.cmu.lti.jawjaw.pobj.Lang;

public class GenericTasaDocument implements Comparable<GenericTasaDocument> {
	static Logger logger = Logger.getLogger(GenericTasaDocument.class);

	private String ID;
	private double DRPscore;
	private int noParagraphs;
	private String content;
	private String genre;

	public static int get6ComplexityGrades(double DRP) {
		if (DRP <= 45.990)
			return 1;
		if (DRP <= 50.999)
			return 2;
		if (DRP <= 55.998)
			return 3;
		if (DRP <= 61.000)
			return 4;
		if (DRP <= 63.999)
			return 5;
		if (DRP <= 85.8)
			return 6;
		return -1;
	}

	public static int get13GradeLevel(double DRP) {
		if (DRP < 35.377)
			return -1;
		if (DRP <= 45.990)
			return 1;
		if (DRP <= 48.973)
			return 2;
		if (DRP <= 50.999)
			return 3;
		if (DRP <= 52.995)
			return 4;
		if (DRP <= 55.998)
			return 5;
		if (DRP <= 58.984)
			return 6;
		if (DRP <= 59.999)
			return 7;
		if (DRP <= 61.000)
			return 8;
		if (DRP <= 61.998)
			return 9;
		if (DRP <= 63.999)
			return 10;
		if (DRP <= 65.997)
			return 11;
		if (DRP <= 66.998)
			return 12;
		return 13;
	}

	public GenericTasaDocument(String iD, double DRPscore, int noParagraphs,
			String content, String genre) throws UnsupportedEncodingException {
		super();
		ID = iD;
		this.DRPscore = DRPscore;
		this.noParagraphs = noParagraphs;
		this.content = new String(content);
		this.genre = genre;
	}

	public void writeTxt(String path) throws IOException {
		BufferedWriter out = new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream(new File(path + "/" + genre + "/" + ID
						+ "-DRP-" + DRPscore + ".txt")), "UTF-8"), 32768);
		out.write(content.trim());
		out.close();
	}

	public void writeContent(String path) throws IOException {
		int gradeLevel = get13GradeLevel(DRPscore);
		if (gradeLevel == -1)
			return;
		// verify if class folder exists
		File dir = new File(path + "/" + RunMeasurementsView.C_BASE_FOLDER_NAME
				+ gradeLevel);
		if (!dir.exists())
			dir.mkdir();

		Document d = getDocument(false);

		d.exportXML(path + "/" + RunMeasurementsView.C_BASE_FOLDER_NAME
				+ gradeLevel + "/" + ID + ".xml");
		writeTxt(path);
	}

	public StringBuilder getProcessedContent(boolean usePOStagging,
			boolean annotateWithPOS) {
		Document d = getDocument(usePOStagging);
		StringBuilder sb = new StringBuilder();
		for (Block b : d.getBlocks()) {
			for (Sentence s : b.getSentences()) {
				for (Word w : s.getWords()) {
					if (w.getPOS() != null && annotateWithPOS) {
						sb.append(w.getLemma() + "_" + w.getPOS() + " ");
					} else {
						sb.append(w.getLemma() + " ");
					}
				}
				if (s.getWords().size() > 0)
					sb.append(". ");
			}
		}
		return sb;
	}

	/**
	 * @param usePOStagging
	 * @return
	 */
	public Document getDocument(boolean usePOStagging) {
		if (content.length() < SplitTASA.LOWER_BOUND) {
			logger.warn(ID
					+ " has too few characters to be taken into consideration");
			return null;
		}

		// perform processing and save the new document
		StringTokenizer st = new StringTokenizer(content, "\n");
		if (st.countTokens() != noParagraphs) {
			logger.warn("Incorrect number of paragraphs for " + ID);
		}

		int crtBlock = 0;

		AbstractDocumentTemplate docTmp = new AbstractDocumentTemplate();

		docTmp.setGenre(genre);
		while (st.hasMoreTokens()) {
			BlockTemplate block = docTmp.new BlockTemplate();
			block.setId(crtBlock++);
			block.setContent(st.nextToken().trim());
			docTmp.getBlocks().add(block);
		}
		Document d = new Document(null, docTmp, null, null, Lang.eng,
				usePOStagging, false);
		d.setTitleText("TASA");
		List<String> authors = new LinkedList<String>();
		authors.add(ID.replaceAll("[^a-z,A-Z]", ""));
		d.setGenre(genre);
		d.setAuthors(authors);
		d.setDate(new Date());
		d.setSource("Touchstone Applied Science Associates, Inc.");
		d.setURI("http://lsa.colorado.edu/spaces.html");
		d.setComplexityLevel(DRPscore + "");
		return d;
	}

	public String getID() {
		return ID;
	}

	public void setID(String iD) {
		ID = iD;
	}

	public double getDRPscore() {
		return DRPscore;
	}

	public void setDRPscore(double dRPscore) {
		DRPscore = dRPscore;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getGenre() {
		return genre;
	}

	public void setGenre(String genre) {
		this.genre = genre;
	}

	public int getNoParagraphs() {
		return noParagraphs;
	}

	public void setNoParagraphs(int noParagraphs) {
		this.noParagraphs = noParagraphs;
	}

	@Override
	public int hashCode() {
		return this.getContent().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		GenericTasaDocument doc = (GenericTasaDocument) obj;
		return doc.getContent().equals(this.getContent());
	}

	@Override
	public int compareTo(GenericTasaDocument doc) {
		return (int) Math.signum(doc.getContent().length()
				- this.getContent().length());
	}

}
