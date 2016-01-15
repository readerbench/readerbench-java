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
	
	public boolean isSameAuthor(ArticleAuthor a) {
		return this.authorUri.equals(a.authorUri);
	}
	public boolean isSameAffiliation(ArticleAuthor a) {
		return this.affiliationUri.equals(a.affiliationUri);
	}
}