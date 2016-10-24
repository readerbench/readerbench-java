/* 
 * Copyright 2016 ReaderBench.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package view.widgets.article.utils;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;

import data.AbstractDocument;
import data.article.ArticleAuthor;
import data.article.ResearchArticle;

public class ArticleContainer {
	private List<SingleAuthorContainer> authorContainers;
	private List<ResearchArticle> articles;
	
	public ArticleContainer(List<ResearchArticle> articles) {
		this.articles = articles;
		this.indexAuthorsFromArticles();
	}
	private void indexAuthorsFromArticles() {
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
	public List<ResearchArticle> getArticles() {
		return this.articles;
	}

	public static ArticleContainer buildAuthorContainerFromDirectory(String dirName) {
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
		return new ArticleContainer(articles);
	}
	
}
