package view.widgets.article.utils;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;

import data.AbstractDocument;
import data.article.ArticleAuthor;
import data.article.ResearchArticle;

public class AuthorContainer {
	private List<SingleAuthorContainer> authorContainers;
	
	public AuthorContainer(List<ResearchArticle> articles) {
		this.indexAuthorsFromArticles(articles);
		
	}
	private void indexAuthorsFromArticles(List<ResearchArticle> articles) {
		this.authorContainers = new ArrayList<SingleAuthorContainer>();
		for(ResearchArticle article : articles) {
			List<ArticleAuthor> authorsForArticle = article.getArticleAuthorList();
			for(ArticleAuthor author : authorsForArticle) {
				SingleAuthorContainer authorContainer = this.getSingleAuthorContainerAssociatedWith(author);
				if(authorContainer == null) {
					List<ResearchArticle> authorArticles = new ArrayList<ResearchArticle>();
					authorArticles.add(article);
					this.authorContainers.add(new SingleAuthorContainer(author, authorArticles));
				}
				else {
					authorContainer.addResearchArticle(article);
				}
			}
		}
	}
	private SingleAuthorContainer getSingleAuthorContainerAssociatedWith(ArticleAuthor author) {
		for(SingleAuthorContainer authorContainer : this.authorContainers) {
			if(authorContainer.isSameAuthor(author)) {
				return authorContainer;
			}
		}
		return null;
	}
	public List<SingleAuthorContainer> getAuthorContainers() { 
		return this.authorContainers;
	}

	
	
	
	public static void main(String[] args) {
		File dir = new File("in/LAK_corpus/parsed-documents2");
		File[] files = dir.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.endsWith(".ser");
			}
		});
		
		List<ResearchArticle> articles = new ArrayList<ResearchArticle>();
		for (File file : files) {
			ResearchArticle d = (ResearchArticle) AbstractDocument
					.loadSerializedDocument(file.getPath());
			articles.add(d);
			System.out.println("-------");
			System.out.println(d.getTitleText());
			System.out.println(d.getURI());
			System.out.println(d.getArticleAuthorList());
			System.out.println(d.getAuthors());
			System.out.println(d.getInitialTopics());
			System.out.println(d.getCitationURIList());
			System.out.println(d.getDate());
			if(d.getBlocks().size() > 0)
				System.out.println(d.getBlocks().get(0).getText());
			System.out.println("--------");
		}
	}
}
