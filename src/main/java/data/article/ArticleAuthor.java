package data.article;

public class ArticleAuthor implements java.io.Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	String authorName;
	String authorUri;
	String affiliationName;
	String affiliationUri;
	@Override
	public String toString() {
		return "{" + this.authorUri + ", " + this.authorName + ", " + this.affiliationUri + ", " + this.affiliationName + "}";
	}
	public String getAuthorName() {
		return this.authorName;
	}
	public String getAuthorUri() {
		return this.authorUri;
	}
	public String getAffiliationName() {
		return this.affiliationName;
	}
	public String getAffiliationUri() {
		return this.affiliationUri;
	}
	
	public boolean isSameAuthor(ArticleAuthor a) {
		return this.authorUri.equals(a.authorUri);
	}
	public boolean isSameAffiliation(ArticleAuthor a) {
		return this.affiliationUri.equals(a.affiliationUri);
	}
}