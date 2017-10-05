
package runtime.document;
import services.converters.PdfToTxtConverter;
import data.article.ReferencesYearsInfo;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import runtime.converters.APARefsEntriesSeparator;
import runtime.converters.IEEERefsEntriesSeparator;
import org.apache.commons.lang.StringUtils;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel; 
import org.jfree.chart.JFreeChart; 
import org.jfree.chart.plot.PlotOrientation; 
import org.jfree.ui.ApplicationFrame; 
import org.jfree.ui.RefineryUtilities; 
import java.util.EnumMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.poifs.filesystem.NPOIFSFileSystem;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.jfree.chart.ChartUtilities;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.openide.util.Exceptions;
import services.commons.VectorAlgebra;


public class ReferencesAnalyzer {
    
    public static final Logger LOGGER = Logger.getLogger("");
    
    private String path;
    private String text, refText;
    private List<Integer> ref_years;
    private int maxYear, minYear;
    private double[] yearsDistribution;
    private List<String> entries;
    private EnumMap<ReferencesYearsInfo, Double> info;
    private EnumMap<ReferencesYearsInfo, Double> info_sum, info_sum_5, info_sum_10;
    private int info_sum_count;
    private final int CURRENT_YEAR;
    private List<XYSeries> xySeries;
    
    public enum RefStandard {
	IEEE, APA
    }
    
    
    public ReferencesAnalyzer(String path) {
	this.path = path;
	CURRENT_YEAR = Calendar.getInstance().get(Calendar.YEAR);
	info_sum = new EnumMap<>(ReferencesYearsInfo.class);
	initializeInfoSum(info_sum);
	info_sum_5 = new EnumMap<>(ReferencesYearsInfo.class);
	initializeInfoSum(info_sum_5);
	info_sum_10 = new EnumMap<>(ReferencesYearsInfo.class);
	initializeInfoSum(info_sum_10);
	xySeries = new ArrayList<>();

    }
    
    private void initializeInfoSum(EnumMap<ReferencesYearsInfo, Double> info_sum) {
	info_sum.put(ReferencesYearsInfo.AVG, 0.0);
	info_sum.put(ReferencesYearsInfo.AVG_NOVELTY, 0.0);
	info_sum.put(ReferencesYearsInfo.COUNT, 0.0);
	info_sum.put(ReferencesYearsInfo.MAX_YEAR, 0.0);
	info_sum.put(ReferencesYearsInfo.MIN_YEAR, 0.0);
	info_sum.put(ReferencesYearsInfo.NORMALITY, 0.0);
	info_sum.put(ReferencesYearsInfo.NOVELTY, 0.0);
	info_sum.put(ReferencesYearsInfo.SLOPE, 0.0);
	info_sum.put(ReferencesYearsInfo.STARTING_FROM, 0.0);
	info_sum.put(ReferencesYearsInfo.STD_DEV, 0.0);
	info_sum.put(ReferencesYearsInfo.NO_MAX_YEAR, 0.0);
    }
    
    public String getPath() {
	return this.path;
    }
    
   
    public void setPath(String path) {
	this.path = path;
    }
    
    public void extractText(File file) throws IOException, InvalidFormatException {
	
	if (file.getName().toLowerCase().endsWith(".pdf")) {
	    PdfToTxtConverter pdf2txt = new PdfToTxtConverter(file.getPath(), true);
	    pdf2txt.process();
	    this.text = pdf2txt.getParsedText();
	} else {
	    if (file.getName().toLowerCase().endsWith(".doc")) {
		
		NPOIFSFileSystem fs;
		StringBuilder full_text = new StringBuilder();
		fs = new NPOIFSFileSystem(file);
		WordExtractor extractor = new WordExtractor(fs.getRoot());
		for(String rawText : extractor.getParagraphText()) {
		    String paragraph = WordExtractor.stripFields(rawText);
		    full_text.append(paragraph);
		}
		this.text = full_text.toString();

		
	    } else {
		XWPFDocument doc = new XWPFDocument(OPCPackage.open(file));
		XWPFWordExtractor ex = new XWPFWordExtractor(doc);
		String text = ex.getText();
		this.text = text;
	    }
	
	
	}
    }
 
    
    public void extractRefs() {
	List<String> tokens = new ArrayList<>();
	tokens.add("References");
	tokens.add("REFERENCES");
	tokens.add("Bibliography");
	tokens.add("BIBLIOGRAPHY");
	tokens.add("Notes and References");
	tokens.add("NOTES AND REFERENCES");
	tokens.add("Note and References");
	tokens.add("NOTE AND REFERENCES");
	String patternString = "\\b(" + StringUtils.join(tokens, "|") + ")\\b";
	Pattern pattern = Pattern.compile(patternString);
	Matcher matcher = pattern.matcher(text);
	int refindex = 0;
	while (matcher.find()) {
	    refindex = matcher.end();
	}
	tokens = new ArrayList<>();
	tokens.add("Acknowledgments");
	tokens.add("ACKNOWLEDGMENTS");
	tokens.add("Afterword");
	tokens.add("AFTERWORD");
	tokens.add("Appendix A");
	tokens.add("Appendix a");
	tokens.add("Curriculum Vitae");
	patternString = "(" + StringUtils.join(tokens, "|") + ")";
	Pattern pattern2 = Pattern.compile(patternString);
	Matcher matcher2 = pattern2.matcher(text);
	// Trying to ignore acknowledgments section after references (if it exists)
	int ackindex = -1;
	while (matcher2.find()) {
	    ackindex = matcher2.start();
	    if (ackindex > refindex) {
		break;
	    }
	}
	if (ackindex > refindex) {
	    refText = text.substring(refindex, ackindex);
	} else {
	    refText = text.substring(refindex);
	}

    }
    
    
    
    public void extractYears() {
	if (entries == null) {
	    System.err.println("References not extracted yet from text.");
            return;
	}
	List<Integer> refYears = new ArrayList<>();
	//String patternString = "(\\((19\\d{2}|2[0-1]\\d{2})[a-zA-Z]?\\)|,\\s(19\\d{2}|20[0-1]\\d{1})\\.)";
	//String patternString = "\\b(?<!(\\.|\\/|–|-))(19\\d{2}|20[0-1]\\d{1})(?!(–|-))\\b";
	//String patternString = "(?<=\\(|, |[A-Z]\\. )(19\\d{2}|20[0-1]\\d{1})(?=\\)|\\.|[a-z]{0,1})";
	String patternString = "(\\(|, |[A-Z]\\. | )(19\\d{2}|20[0-2]\\d{1})(\\)| \\.|,)";
	Pattern pattern = Pattern.compile(patternString);
	for (String entry : this.entries) {
	    if (entry != null) {
		Matcher matcher = pattern.matcher(entry);
		String entryYear = "";
		while(matcher.find()) {
		    entryYear = matcher.group(0);
		    break;
		}
		if (!"".equals(entryYear)) {
		    String yearPatternString = "19\\d{2}|20[0-2]\\d{1}";
		    Pattern yearPattern = Pattern.compile(yearPatternString);
		    Matcher yearMatcher = yearPattern.matcher(entryYear);
		    if (yearMatcher.find()) {
			refYears.add(Integer.valueOf(yearMatcher.group(0)));
		    }
		}
	    }
		
	}
	this.ref_years = refYears;
	
    }
    
    public void generateYearsDistribution() {
	if (ref_years == null) {
	    System.err.println("Years not extracted yet");
	    return;
	}
	
	maxYear = Collections.max(ref_years);
	minYear = Collections.min(ref_years);
	double[] yearsDistrArray = new double[maxYear - minYear + 1];
	for (int i = 0; i < yearsDistrArray.length; i++) {
	    yearsDistrArray[i] = (double)Collections.frequency(ref_years, minYear + i);
	}
	this.yearsDistribution = yearsDistrArray;
	
    }
    
    
    public void process() throws InvalidFormatException {
	File[] pdf_files;
	File[] doc_files;
	File[] docx_files;
	BufferedWriter outputWriter = null;
	ArrayList<File> all_files = new ArrayList<>();
	File file = new File(path);
	boolean isDir = file.isDirectory();
	try {
	    if (isDir) {
		
		if (!file.exists()) {
		    throw new IOException("Inexistent Folder: " + file.getPath());
		}
		pdf_files = file.listFiles(
			(File pathname)
			-> pathname.getName().toLowerCase().endsWith(".pdf"));
		doc_files = file.listFiles(
			(File pathname)
			-> pathname.getName().toLowerCase().endsWith(".doc"));
		docx_files = file.listFiles(
			(File pathname)
			-> pathname.getName().toLowerCase().endsWith(".docx"));
		all_files.addAll(Arrays.asList(pdf_files));
		all_files.addAll(Arrays.asList(doc_files));
		all_files.addAll(Arrays.asList(docx_files));
	    } else {
		
		if (!file.exists()) {
		    throw new IOException("Inexistent File: " + file.getPath());
		}
		all_files.add(file);

	    }
	    String outputFileName = generateOutputPathName(isDir);
	    outputWriter = new BufferedWriter(new FileWriter(outputFileName));
	    this.writeCSVHeader(outputWriter);
	    for (File testfile : all_files) {
		LOGGER.log(Level.INFO, "Processing file: {0}", testfile.getName());
		this.clearInfo();
		extractText(testfile);
		extractRefs();
		RefStandard standard = this.detectStandard();
		if (standard == RefStandard.IEEE) {
		    this.entries = IEEERefsEntriesSeparator.separate(this.refText);
		}
		if (standard == RefStandard.APA) {
		    this.entries = APARefsEntriesSeparator.separate(this.refText);
		}
		if (standard == null) {
		    System.err.println("No standard was detected");
		    LOGGER.log(Level.INFO, "NO STANDARD DETECTED ON THIS FILE");
		} else {
		    LOGGER.log(Level.INFO, "Standard is: {0}", standard);
		}

		// Extract the years and generate the distribution
		extractYears();
		generateYearsDistribution();
		outputWriter.write(testfile.getName().replace(',', ' '));
		if (this.yearsDistribution != null) {
		    calculateRefsInformation(this.minYear);
		    fillInfoSum(info_sum);
		    outputWriter.write(",");
		    this.writeCSVInfo(outputWriter);
		    calculateRefsInformation(this.CURRENT_YEAR - 4);
		    fillInfoSum(info_sum_5);
		    outputWriter.write(",");
		    this.writeCSVInfo(outputWriter);
		    calculateRefsInformation(this.CURRENT_YEAR - 9);
		    fillInfoSum(info_sum_10);
		    outputWriter.write(",");
		    this.writeCSVInfo(outputWriter);
		    outputWriter.write("," + this.yearsDistribution[this.yearsDistribution.length - 1]);
		    outputWriter.write("\n");
		    info_sum_count++;
		    outputWriter.write("Average,");
		    writeCSVInfoAVG(outputWriter, info_sum);
		    outputWriter.write(",");
		    writeCSVInfoAVG(outputWriter, info_sum_5);
		    outputWriter.write(",");
		    writeCSVInfoAVG(outputWriter, info_sum_10);
		    outputWriter.write("," + info_sum.get(ReferencesYearsInfo.NO_MAX_YEAR) / (double)info_sum_count +",\n");
		    String chartOutputPath = generateOutputChartName(isDir);
		    createChart(chartOutputPath);
		} else {
		    LOGGER.log(Level.INFO, "Years could not be extracted {0}");
		    outputWriter.write(",Error\n");
		}
		addXYSeries(testfile.getName());
	    }
	    
	} catch (IOException ex) {
	    LOGGER.log(Level.INFO, "Exception: {0}", ex.getMessage());
	    Exceptions.printStackTrace(ex);
	    System.exit(0);
	} finally {
	    if (outputWriter != null) {
		try {
		    outputWriter.close();
		} catch (IOException ex) {
		    Exceptions.printStackTrace(ex);
		}
	    }
	   
	}
	
	
	
	
	
    }

    
    
    
    public void calculateRefsInformation(int filterYear) { 
    // filterYear = minimum year from which to beging calculating info
	if (this.yearsDistribution == null) {
	    return;
	}
	double[] filteredDistribution;
	int starting_from;
	
	if (filterYear <= minYear) {
	    if (filterYear == minYear) {
		filteredDistribution = this.yearsDistribution;
	    } else {
		filteredDistribution = new double[this.CURRENT_YEAR - filterYear + 1];
		for (int i = 0; i < filteredDistribution.length; i++) {
		    if (i + filterYear < this.minYear || i + filterYear > this.maxYear) {
			filteredDistribution[i] = 0;
		    }
		    if (i + filterYear >= this.minYear && i + filterYear <= this.maxYear) {
			filteredDistribution[i] = this.yearsDistribution[this.yearsDistribution.length - (maxYear - (i + filterYear)) - 1];
		    }

		}
	    }
	    //filteredDistribution = this.yearsDistribution;
	    starting_from = filterYear;
	    
	    
	} else {
	    int newIndex = filterYear - minYear;
	    filteredDistribution = new double[this.yearsDistribution.length - newIndex + this.CURRENT_YEAR - this.maxYear];
	    starting_from = filterYear;
	    for (int i = newIndex; i < this.yearsDistribution.length; i++) {
		filteredDistribution[i - newIndex] = this.yearsDistribution[i];
		if (i == this.yearsDistribution.length - 1) {
		    for (int j = i + 1; j < filteredDistribution.length; j++) {
			filteredDistribution[j] = 0;
		    }
		}	
	    }
	    
	}
	

	int min_year = 0;
	for (int i = 0; i < filteredDistribution.length; i++) {
	    //System.out.println("(Year: " + (i + starting_from) + " Appearences: " + filteredDistribution[i] + ")");
	    if (filteredDistribution[i] != 0) {
		min_year = i + starting_from;
		break;
	    }
	}
	double maxYear;
	if (this.maxYear < filterYear) {
	    maxYear = 0;
	} else {
	    maxYear = this.maxYear;
	}
	//System.out.println("FILTERED DISTRIBUTION");
	double count = 0.0;
	for (int i = 0; i < filteredDistribution.length; i++) {
	    //System.out.println("(Year: " + (i + starting_from) + " Appearences: " + filteredDistribution[i] + ")");
	    count += filteredDistribution[i];
	}
	
	
	info = new EnumMap<>(ReferencesYearsInfo.class);
	info.put(ReferencesYearsInfo.MAX_YEAR, (double)maxYear);
	info.put(ReferencesYearsInfo.MIN_YEAR, (double)min_year);
	info.put(ReferencesYearsInfo.STARTING_FROM, (double)starting_from);
	info.put(ReferencesYearsInfo.STD_DEV, VectorAlgebra.stdev(filteredDistribution));
	info.put(ReferencesYearsInfo.NORMALITY, VectorAlgebra.norm2(filteredDistribution));
	info.put(ReferencesYearsInfo.SLOPE, VectorAlgebra.slope(filteredDistribution));
	info.put(ReferencesYearsInfo.NOVELTY, (double)(CURRENT_YEAR - maxYear));
	info.put(ReferencesYearsInfo.AVG, calculateAverage(filteredDistribution, info));
	info.put(ReferencesYearsInfo.AVG_NOVELTY, calculateAvgNovelty(filteredDistribution, info));
	info.put(ReferencesYearsInfo.COUNT, count);
	
	//this.printCalculatedInfo();
	
    }
    
    private void fillInfoSum(EnumMap<ReferencesYearsInfo, Double> info_sum) {
	info_sum.put(ReferencesYearsInfo.MAX_YEAR, info_sum.get(ReferencesYearsInfo.MAX_YEAR) 
		+ info.get(ReferencesYearsInfo.MAX_YEAR));
	info_sum.put(ReferencesYearsInfo.MIN_YEAR, info_sum.get(ReferencesYearsInfo.MIN_YEAR) 
		+ info.get(ReferencesYearsInfo.MIN_YEAR));
	if("NaN".equals(info.get(ReferencesYearsInfo.SLOPE).toString()) || info.get(ReferencesYearsInfo.SLOPE) == 0.00) {
	    info_sum.put(ReferencesYearsInfo.SLOPE, info_sum.get(ReferencesYearsInfo.SLOPE) 
		+ 0);
	} else {
	    info_sum.put(ReferencesYearsInfo.SLOPE, info_sum.get(ReferencesYearsInfo.SLOPE) 
		+ info.get(ReferencesYearsInfo.SLOPE));
	}
	if("NaN".equals(info.get(ReferencesYearsInfo.STD_DEV).toString()) || info.get(ReferencesYearsInfo.STD_DEV) == 0.00) {
	    info_sum.put(ReferencesYearsInfo.STD_DEV, info_sum.get(ReferencesYearsInfo.STD_DEV) 
		+ 0);
	} else {
	    info_sum.put(ReferencesYearsInfo.STD_DEV, info_sum.get(ReferencesYearsInfo.STD_DEV) 
		+ info.get(ReferencesYearsInfo.STD_DEV));
	} 
	if("NaN".equals(info.get(ReferencesYearsInfo.NORMALITY).toString()) || info.get(ReferencesYearsInfo.NORMALITY) == 0.00) {
	    info_sum.put(ReferencesYearsInfo.NORMALITY, info_sum.get(ReferencesYearsInfo.NORMALITY) 
		+ 0);
	} else {
	    info_sum.put(ReferencesYearsInfo.NORMALITY, info_sum.get(ReferencesYearsInfo.NORMALITY) 
		+ info.get(ReferencesYearsInfo.NORMALITY));
	}
	info_sum.put(ReferencesYearsInfo.NOVELTY, info_sum.get(ReferencesYearsInfo.NOVELTY) 
		+ info.get(ReferencesYearsInfo.NOVELTY));
	if("NaN".equals(info.get(ReferencesYearsInfo.AVG_NOVELTY).toString())) {
	    info_sum.put(ReferencesYearsInfo.AVG_NOVELTY, info_sum.get(ReferencesYearsInfo.AVG_NOVELTY) 
		+ 0);
	} else {
	    info_sum.put(ReferencesYearsInfo.AVG_NOVELTY, info_sum.get(ReferencesYearsInfo.AVG_NOVELTY) 
		+ info.get(ReferencesYearsInfo.AVG_NOVELTY));
	} 
	
	info_sum.put(ReferencesYearsInfo.COUNT, info_sum.get(ReferencesYearsInfo.COUNT) 
		+ info.get(ReferencesYearsInfo.COUNT));
	info_sum.put(ReferencesYearsInfo.NO_MAX_YEAR, info_sum.get(ReferencesYearsInfo.NO_MAX_YEAR) 
		+ this.yearsDistribution[yearsDistribution.length - 1]);
	
	
    }
    
    private double calculateAverage(double[] distribution, EnumMap<ReferencesYearsInfo, Double> distr_info) {
	double sum = 0.0;
	double count = 0.0;
	for (int i = 0; i < distribution.length; i++) {
	    sum += distribution[i] * (i + distr_info.get(ReferencesYearsInfo.STARTING_FROM));
	    count += distribution[i];
	}
	return sum / count;
    }
    
    private double calculateAvgNovelty(double[] distribution, EnumMap<ReferencesYearsInfo, Double> distr_info) {
	double sum = 0.0;
	double count = 0.0;
	for (int i = 0; i < distribution.length; i++) {
	    sum += distribution[i] * (CURRENT_YEAR - i - distr_info.get(ReferencesYearsInfo.STARTING_FROM));
	    count += distribution[i];
	}
	return sum / count;
	
	
    }
    
    
    
    
    public RefStandard detectStandard() {
	String patternString = "^\\s*(\\d{1,2}\\.|\\[\\d{1,2}\\])";
	Pattern pattern = Pattern.compile(patternString);
	Matcher matcher = pattern.matcher(this.refText);
	if (matcher.find()) {
	    //System.out.println("IEEE Standard Detected");
	    return RefStandard.IEEE;
	    
	}
	patternString = "^\\s*[A-Z][a-zA-Z-Á]*,\\s[A-ZÁ]\\.";
	pattern = Pattern.compile(patternString);
	matcher = pattern.matcher(this.refText);
	if (matcher.find()) {
	    //System.out.println("APA Standard Detected");
	    return RefStandard.APA;
	}
	
	return null;
	
	
    }
    
    
    public List<Integer> getRefYears() {
	return ref_years;
    }
    
    public String getReferencesText() {
	return refText;
    }
    
    public double[] getYearsDistribution() {
	return yearsDistribution;
    }
    
    private void printYearsDistribution() {
	if (yearsDistribution == null) {
	    System.out.println("Distribution not generated yet.");
	    return;
	}
	
	System.out.println("Years distribution: ");
	for (int i = 0; i < yearsDistribution.length; i++) {
	    System.out.println("(Year: " + (i + this.minYear) + " Appearences: " + yearsDistribution[i] + ")");
	}
    }
    public void printCalculatedInfo() {
	if (info == null) {
	    System.out.println("Info not calculated yet.");
	    return;
	}
	System.out.println("MIN_YEAR = " + info.get(ReferencesYearsInfo.MIN_YEAR));
	System.out.println("MAX_YEAR = " + info.get(ReferencesYearsInfo.MAX_YEAR));
	System.out.println("AVG_YEAR = " + info.get(ReferencesYearsInfo.AVG));
	System.out.println("Slope = " + info.get(ReferencesYearsInfo.SLOPE));
	System.out.println("Normality = " + info.get(ReferencesYearsInfo.NORMALITY));
	System.out.println("Novelty = " + info.get(ReferencesYearsInfo.NOVELTY));
	System.out.println("AVG Actuality = " + info.get(ReferencesYearsInfo.AVG_NOVELTY));
	System.out.println("Std_Dev = " + info.get(ReferencesYearsInfo.STD_DEV));
	
    }
    private void writeCSVInfoAVG(BufferedWriter csvFile, EnumMap<ReferencesYearsInfo,Double> info_sum) throws IOException {
	if(info_sum.get(ReferencesYearsInfo.MIN_YEAR) == 0.00) {
	    csvFile.write(",");
	} else {
	    csvFile.write(info_sum.get(ReferencesYearsInfo.MIN_YEAR) / (double)info_sum_count + ",");
	} 
	if(info_sum.get(ReferencesYearsInfo.MAX_YEAR) == 0.00) {
	    csvFile.write(",");
	} else {
	    csvFile.write(info_sum.get(ReferencesYearsInfo.MAX_YEAR) / (double)info_sum_count + ",");
	} 
	
	if(info_sum.get(ReferencesYearsInfo.SLOPE) == 0.00) {
	    csvFile.write(",");
	} else {
	    csvFile.write(info_sum.get(ReferencesYearsInfo.SLOPE) / (double)info_sum_count + ",");
	}
	if(info_sum.get(ReferencesYearsInfo.STD_DEV) == 0.00) {
	    csvFile.write(",");
	} else {
	    csvFile.write(info_sum.get(ReferencesYearsInfo.STD_DEV) / (double)info_sum_count + ",");
	} 
	if(info_sum.get(ReferencesYearsInfo.NORMALITY) == 0.00) {
	    csvFile.write(",");
	} else {
	    csvFile.write(info_sum.get(ReferencesYearsInfo.NORMALITY) / (double)info_sum_count + ",");
	}
	if(info_sum.get(ReferencesYearsInfo.NOVELTY) == (double)info_sum_count * 2017.00) {
	    csvFile.write(",");
	} else {
	    csvFile.write(info_sum.get(ReferencesYearsInfo.NOVELTY) / (double)info_sum_count + ",");
	}
	if(info_sum.get(ReferencesYearsInfo.AVG_NOVELTY) == 0.0) {
	    csvFile.write(",");
	} else {
	    csvFile.write(info_sum.get(ReferencesYearsInfo.AVG_NOVELTY) / (double)info_sum_count + ",");
	} 
	
	csvFile.write(info_sum.get(ReferencesYearsInfo.COUNT) / (double)info_sum_count + "");
    }
    
    private void writeCSVInfo(BufferedWriter csvFile) throws IOException {
	if(info.get(ReferencesYearsInfo.MIN_YEAR) == 0.00) {
	    csvFile.write(",");
	} else {
	    csvFile.write(info.get(ReferencesYearsInfo.MIN_YEAR) + ",");
	} 
	if(info.get(ReferencesYearsInfo.MAX_YEAR) == 0.00) {
	    csvFile.write(",");
	} else {
	    csvFile.write(info.get(ReferencesYearsInfo.MAX_YEAR) + ",");
	} 
	
	if("NaN".equals(info.get(ReferencesYearsInfo.SLOPE).toString()) || info.get(ReferencesYearsInfo.SLOPE) == 0.00) {
	    csvFile.write(",");
	} else {
	    csvFile.write(info.get(ReferencesYearsInfo.SLOPE) + ",");
	}
	if("NaN".equals(info.get(ReferencesYearsInfo.STD_DEV).toString()) || info.get(ReferencesYearsInfo.STD_DEV) == 0.00) {
	    csvFile.write(",");
	} else {
	    csvFile.write(info.get(ReferencesYearsInfo.STD_DEV) + ",");
	} 
	if("NaN".equals(info.get(ReferencesYearsInfo.NORMALITY).toString()) || info.get(ReferencesYearsInfo.NORMALITY) == 0.00) {
	    csvFile.write(",");
	} else {
	    csvFile.write(info.get(ReferencesYearsInfo.NORMALITY) + ",");
	}
	if(info.get(ReferencesYearsInfo.NOVELTY) == 2017.00) {
	    csvFile.write(",");
	} else {
	    csvFile.write(info.get(ReferencesYearsInfo.NOVELTY).toString() + ",");
	}
	if("NaN".equals(info.get(ReferencesYearsInfo.AVG_NOVELTY).toString())) {
	    csvFile.write(",");
	} else {
	    csvFile.write(info.get(ReferencesYearsInfo.AVG_NOVELTY) + ",");
	} 
	
	csvFile.write(info.get(ReferencesYearsInfo.COUNT).toString());
    }
    
    
    
    private void writeCSVHeader(BufferedWriter csvFile) throws IOException {
	csvFile.write("SEP=,\nFilename,minYear,maxYear,slope,std_dev,normality,novelty,avg_novelty,count,"
		+ "minYear_5,maxYear_5,slope_5,std_dev_5,normality_5,novelty_5,avg_novelty_5,count_5,"
		+ "minYear_10,maxYear_10,slope_10,std_dev_10,normality_10,novelty_10,avg_novelty_10,count_10"
		+ ",maxYear_appearences");
	
	
	csvFile.write("\n");
    }
    private void addXYSeries(String filename) {
	final XYSeries yearsSeries = new XYSeries(filename);
	for (int i = 0; i < yearsDistribution.length; i++) {
	    yearsSeries.add(minYear + i, yearsDistribution[i]);
	}
	this.xySeries.add(yearsSeries);
	
    }
    
    
    public void createChart(String outputPath) throws IOException {
	if (yearsDistribution == null) {
	    System.err.println("Years distribution not generated. Cannot create chart");
	    return;
	}
	final XYSeriesCollection dataset = new XYSeriesCollection();
	xySeries.forEach((xySerie) -> {
	    dataset.addSeries(xySerie);
	});
	
	
	ApplicationFrame frame = new ApplicationFrame("Years Distribution Chart");
	JFreeChart lineChart;
	lineChart = ChartFactory.createXYLineChart("Years Distributions",
		"Year", "Occurrences", dataset, PlotOrientation.VERTICAL, true, true, true);
	
	
	int width = 640;   /* Width of the image */
	int height = 480;  /* Height of the image */ 
	File XYChart = new File( outputPath ); 
	ChartUtilities.saveChartAsJPEG( XYChart, lineChart, width, height);
	
	ChartPanel chartPanel = new ChartPanel( lineChart );        
	chartPanel.setPreferredSize(new java.awt.Dimension( 560 , 367 ) ); 
	frame.setContentPane( chartPanel );
	frame.pack();
	RefineryUtilities.centerFrameOnScreen( frame );        
	frame.setVisible( true ); 
	
    }
  
    private String generateOutputPathName(boolean isDir) {
	if (isDir) {
	    if (path.endsWith(File.separator)) {
		return path + "analysis.csv";
	    } else {
		return path + File.separator + "analysis.csv";
	    }
	} else {
	    String directory = path.substring(0, path.lastIndexOf(File.separator));
	    return directory + "analysis.csv";
	}
    }
    
    private String generateOutputChartName(boolean isDir) {
	if (isDir) {
	    if (path.endsWith(File.separator)) {
		return path + "lineChart.jpeg";
	    } else {
		return path + File.separator + "lineChart.jpeg";
	    }
	} else {
	    String directory = path.substring(0, path.lastIndexOf(File.separator));
	    return directory + "lineChart.jpeg";
	}
    }
    
    
    private void clearInfo() {
	this.info = null;
	this.refText = null;
	this.text = null;
	this.ref_years = null;
	this.yearsDistribution = null;
	this.entries = null;
    }
    
    public static void main(String args[]){
	try {
	    ReferencesAnalyzer rscanalyzer1 = new ReferencesAnalyzer("A:\\Facultate\\TestDocuments3\\5 Excellent Student DianaF");
	    rscanalyzer1.process();
	    ReferencesAnalyzer rscanalyzer2 = new ReferencesAnalyzer("A:\\Facultate\\TestDocuments3\\4 Very Good Student JoanB");
	    rscanalyzer2.process();
	    ReferencesAnalyzer rscanalyzer3 = new ReferencesAnalyzer("A:\\Facultate\\TestDocuments3\\3 Good Student MarciaC");
	    rscanalyzer3.process();
	    ReferencesAnalyzer rscanalyzer4 = new ReferencesAnalyzer("A:\\Facultate\\TestDocuments3\\2 Acceptable Student MeikeM");
	    rscanalyzer4.process();
	    ReferencesAnalyzer rscanalyzer5 = new ReferencesAnalyzer("A:\\Facultate\\TestDocuments3\\1 Weak Student JulieF");
	    rscanalyzer5.process();
	    ReferencesAnalyzer rscanalyzer6 = new ReferencesAnalyzer("A:\\Facultate\\TestDocuments3\\0 ADHD Student ChantellM");
	    rscanalyzer6.process();
	} catch (Exception e) {
	    Exceptions.printStackTrace(e);
	}

	
	
	


	
	
	
	

	
	
    }
    
}
