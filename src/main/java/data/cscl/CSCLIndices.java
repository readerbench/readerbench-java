package data.cscl;

public enum CSCLIndices {
	NO_CONTRIBUTION("Contributions"),
	OVERALL_SCORE("Cumulated contribution scores"),
	PERSONAL_KB("Cumulated personal KB scores"),
	SOCIAL_KB("Cumulated social KB scores"),
	INTER_ANIMATION_DEGREE("Degree of inter-animation"),
	INDEGREE("In-degree centrality"),
	OUTDEGREE("Out-degree centrality"),
	BETWEENNESS("Betweenness centrality"),
	CLOSENESS("Closeness centrality"),
	ECCENTRICITY("Eccentricity centrality"),
	NO_NEW_THREADS("New thread initiated"),
	AVERAGE_LENGTH_NEW_THREADS("Average length of initiated threads"),
	NEW_THREADS_OVERALL_SCORE("Cumulated first contribution scores for initiated threads"),
	NEW_THREADS_INTER_ANIMATION_DEGREE("Inter-animation degree for initiated threads"),
	NEW_THREADS_CUMULATIVE_SOCIAL_KB("Social KB for initiated threads"),
	RELEVANCE_TOP10_TOPICS("Relevance of top 10 topics"),
	NO_NOUNS("Nouns"),
	NO_VERBS("Verbs");

	private String description;

	private CSCLIndices(String description) {
		this.description = description;
	}

	public String getDescription() {
		return description;
	}
}