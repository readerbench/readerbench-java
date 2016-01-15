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

	public static AuthorContainer buildAuthorContainerFromDirectory(String dirName) {
		List<ResearchArticle> articles = new ArrayList<ResearchArticle>();
		
		File dir = new File(dirName);
		File[] files = dir.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.endsWith(".ser");
			}
		});
		for (File file : files) {
			ResearchArticle d = (ResearchArticle) AbstractDocument
					.loadSerializedDocument(file.getPath());
			if(d.getBlocks().size() == 0) {
				System.out.println("Omitting article " + d.getTitleText() + " because it has no abstract");
				continue;
			}
			articles.add(d);
		}
		return new AuthorContainer(articles);
	}
	
	
	public static void main(String[] args) {
		String inDir = "in/LAK_corpus/parsed-documents2";
		AuthorContainer container = AuthorContainer.buildAuthorContainerFromDirectory(inDir);
		System.out.println(container.authorContainers);
	}
}
