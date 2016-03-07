package view.widgets.article.utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import data.article.ResearchArticle;
import view.widgets.article.utils.distanceStrategies.IAuthorDistanceStrategy;

public class ArticleAuthorParameterLogger {
	public static String OUT_DIRECTORY = "out/LAK_corpus";
	public static int NO_TOP_SIMILAR = 5000;
	
	private ArticleContainer authorContainer;
	
	public ArticleAuthorParameterLogger(ArticleContainer authorContainer) {
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
		this.createOutDirIfNotExists(ArticleAuthorParameterLogger.OUT_DIRECTORY);
		String outputFile = ArticleAuthorParameterLogger.OUT_DIRECTORY + "/" + referenceStrategy.getStrategyKey() + "_TopSimilar.csv";
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
	
	public void logGraphMeasures(List<GraphMeasure> graphMeasures) {
		this.createOutDirIfNotExists(ArticleAuthorParameterLogger.OUT_DIRECTORY);
		try {
			FileWriter fwrt = new FileWriter(OUT_DIRECTORY + "/ArticleAuthorMeasures.csv");
			BufferedWriter bfwrt = new BufferedWriter(fwrt);
			bfwrt.write("Type,Uri,Betwenness,Eccentricity,Closeness,Degree,Published Articles,Co-Authorship Count" );
			
			for(GraphMeasure measure : graphMeasures) {
				bfwrt.newLine();
				String lineText = measure.getNodeTypeString() + "," + measure.getUri() + "," + measure.getBetwenness() + "," +
						measure.getEccentricity() + "," + measure.getCloseness() + "," +
						measure.getDegree() + ",";
				
				if(measure.getNodeType() == GraphNodeItemType.Author) {
					lineText += this.getNoPublishedArticles(measure.getUri()) + "," + this.getAuthorNoOffReferences(measure.getUri());
				}
				else if(measure.getNodeType() == GraphNodeItemType.Article) {
					lineText += "," + this.getArticleNoOfReferences(measure.getUri());
				}
				
				bfwrt.write(lineText);
			}
			
			bfwrt.close();
			fwrt.close();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	private int getNoPublishedArticles(String authorUri) {
		for(SingleAuthorContainer authorContainer : this.authorContainer.getAuthorContainers()) {
			if(authorContainer.getAuthor().getAuthorUri().equals(authorUri)) {
				return authorContainer.getAuthorArticles().size();
			}
		}
		return 0;
	}
	private int getArticleNoOfReferences(String articleUri) {
		int count = 0;
		for(ResearchArticle article : this.authorContainer.getArticles()) {
			for(String citationUri : article.getCitationURIList()) {
				if(citationUri.equals(articleUri)) {
					count ++;
				}
			}
		}
		return count;
	}
	private int getAuthorNoOffReferences(String authorUri) {
		int count = 0;
		for(ResearchArticle article : this.authorContainer.getArticles()) {
			for(String citationUri : article.getCitationURIList()) {
				ResearchArticle citedArticle = this.getArticleByUri(citationUri);
				if(citedArticle != null) {
					for(data.article.ArticleAuthor author : citedArticle.getArticleAuthorList()) {
						if(author.getAuthorUri().equals(authorUri)) {
							count ++;
							break;
						}
					}
				}
			}
		}
		
		return count;
	}
	private ResearchArticle getArticleByUri(String articleUri) {
		for(ResearchArticle article : this.authorContainer.getArticles()) {
			if(article.getURI().equals(articleUri)) {
				return article;
			}
		}
		return null;
	}
}
