package view.widgets.article.utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import view.widgets.article.utils.distanceStrategies.IAuthorDistanceStrategy;

public class AuthorParameterLogger {
	public static String OUT_DIRECTORY = "out/LAK_corpus";
	public static int NO_TOP_SIMILAR = 5000;
	
	private ArticleContainer authorContainer;
	
	public AuthorParameterLogger(ArticleContainer authorContainer) {
		this.authorContainer = authorContainer;
	}
	
	public void logTopSimilarAuthors(IAuthorDistanceStrategy referenceStrategy, IAuthorDistanceStrategy[] allStrategies) {
		ArrayList<AuthorPairDistanceContainer> authorList = new ArrayList<AuthorPairDistanceContainer>();
		for (int i = 0; i < authorContainer.getAuthorContainers().size() - 1; i++) {
			for (int j = i + 1; j < authorContainer.getAuthorContainers().size(); j++) {
				SingleAuthorContainer a1 = authorContainer.getAuthorContainers().get(i);
				SingleAuthorContainer a2 = authorContainer.getAuthorContainers().get(j);
				double sim = referenceStrategy.computeDistanceBetween(a1, a2);
				AuthorPairDistanceContainer pair = new AuthorPairDistanceContainer(a1, a2, sim);
				authorList.add(pair);
			}
		}
		Collections.sort(authorList);
		this.createOutDirIfNotExists(AuthorParameterLogger.OUT_DIRECTORY);
		String outputFile = AuthorParameterLogger.OUT_DIRECTORY + "/" + referenceStrategy.getStrategyKey() + "_TopSimilar.csv";
		try {
			FileWriter fwrt = new FileWriter(outputFile);
			BufferedWriter bfwrt = new BufferedWriter(fwrt);
			bfwrt.write("Author 1,Author 2" + this.getCsvHeader(allStrategies));
			
			for(int i = 0; i < Math.min(authorList.size(), NO_TOP_SIMILAR); i++) {
				AuthorPairDistanceContainer pair = authorList.get(i);
				String values = pair.getFirstAuthor().getAuthor().getAuthorUri() + "," +
						pair.getSecondAuthor().getAuthor().getAuthorUri() + 
						this.getCsvValues(pair.getFirstAuthor(), pair.getSecondAuthor(), allStrategies);
				
				bfwrt.newLine();
				bfwrt.write(values);
			}
			bfwrt.close();
			fwrt.close();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	private void createOutDirIfNotExists(String dirName) {
		File theDir = new File(dirName);
		if (!theDir.exists()) {
		    try{
		        theDir.mkdir();
		    } 
		    catch(SecurityException se){
		    }
		}
	}
	private String getCsvHeader(IAuthorDistanceStrategy[] allStrategies) {
		String out = ",";
		for(IAuthorDistanceStrategy strategy : allStrategies) {
			out += strategy.getStrategyName() + ",";
		}
		return out.substring(0, out.length() - 1);
	}
	private String getCsvValues(SingleAuthorContainer firstAuthor, SingleAuthorContainer secondAuthor, IAuthorDistanceStrategy[] allStrategies) {
		String out = ",";
		for(IAuthorDistanceStrategy strategy : allStrategies) {
			out += strategy.computeDistanceBetween(firstAuthor, secondAuthor) + ",";
		}
		return out.substring(0, out.length() - 1);
	}
	
	public void logGraphMeasures(IAuthorDistanceStrategy referenceStrategy, List<GraphMeasure> graphMeasures, int threshold) {
		this.createOutDirIfNotExists(AuthorParameterLogger.OUT_DIRECTORY);
		String strategyDir = AuthorParameterLogger.OUT_DIRECTORY + "/" + referenceStrategy.getStrategyKey();
		this.createOutDirIfNotExists(strategyDir);
		try {
			FileWriter fwrt = new FileWriter(strategyDir + "/" + threshold + ".csv");
			BufferedWriter bfwrt = new BufferedWriter(fwrt);
			bfwrt.write("Author,Betwenness,Eccentricity,Closeness,Degree" );
			
			for(GraphMeasure measure : graphMeasures) {
				bfwrt.newLine();
				bfwrt.write(measure.getAuthorUri() + "," + measure.getBetwenness() + "," +
						measure.getEccentricity() + "," + measure.getCloseness() + "," +
						measure.getDegree());
			}
			
			bfwrt.close();
			fwrt.close();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
}
