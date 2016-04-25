package services.complexity;

import org.apache.log4j.Logger;

import data.AbstractDocument;
import data.Lang;
import services.complexity.CAF.BalancedCAF;
import services.complexity.coreference.CoreferenceResolutionComplexity;
import services.complexity.discourse.ConnectivesComplexity;
import services.complexity.discourse.DialogismStatisticsComplexity;
import services.complexity.discourse.DialogismSynergyComplexity;
import services.complexity.discourse.DiscourseComplexity;
import services.complexity.discourse.LexicalCohesionComplexity;
import services.complexity.discourse.SemanticCohesionComplexity;
import services.complexity.entityDensity.EntityDensityComplexity;
import services.complexity.flow.DocFlowCriteria;
import services.complexity.flow.DocumentFlowComplexity;
import services.complexity.lexicalChains.LexicalChainsComplexity;
import services.complexity.readability.Readability;
import services.complexity.surface.EntropyComplexity;
import services.complexity.surface.LengthComplexity;
import services.complexity.surface.SurfaceStatisticsComplexity;
import services.complexity.syntax.POSComplexity;
import services.complexity.syntax.PronounsComplexity;
import services.complexity.syntax.TreeComplexity;
import services.complexity.wordComplexity.WordComplexity;
import services.nlp.listOfWords.Connectives;
import services.nlp.listOfWords.Pronouns;

/**
 * Class used to define all factors to be used within the complexity evaluation
 * model
 * 
 * @author Mihai Dascalu
 */

public class ComplexityIndices {
	static Logger logger = Logger.getLogger(ComplexityIndices.class);

	private static int id = -1;

	public static final int IDENTITY = id;

	// readability formulas
	public static final int READABILITY_FLESCH = (id += 1);
	public static final int READABILITY_FOG = (id += 1);
	public static final int READABILITY_KINCAID = (id += 1);

	// surface factors
	public static final int AVERAGE_BLOCK_LENGTH = (id += 1);
	public static final int AVERAGE_SENTENCE_LENGTH = (id += 1);
	public static final int AVERAGE_WORD_LENGTH = (id += 1);
	public static final int WORD_LETTERS_STANDARD_DEVIATION = (id += 1);

	public static final int AVERAGE_COMMAS_PER_BLOCK = (id += 1);
	public static final int AVERAGE_COMMAS_PER_SENTENCE = (id += 1);

	public static final int AVERAGE_SENTENCES_IN_BLOCK = (id += 1);
	public static final int BLOCK_STANDARD_DEVIATION_NO_SENTENCES = (id += 1);

	public static final int AVERAGE_WORDS_IN_BLOCK = (id += 1);
	public static final int BLOCK_STANDARD_DEVIATION_NO_WORDS = (id += 1);
	public static final int AVERAGE_WORDS_IN_SENTENCE = (id += 1);
	public static final int SENTENCE_STANDARD_DEVIATION_NO_WORDS = (id += 1);

	public static final int AVERAGE_UNIQUE_WORDS_IN_BLOCK = (id += 1);
	public static final int BLOCK_STANDARD_DEVIATION_NO_UNIQUE_WORDS = (id += 1);
	public static final int AVERAGE_UNIQUE_WORDS_IN_SENTENCE = (id += 1);
	public static final int SENTENCE_STANDARD_DEVIATION_NO_UNIQUE_WORDS = (id += 1);

	// Entropy
	public static final int WORD_ENTROPY = (id += 1);
	public static final int CHAR_ENTROPY = (id += 1);

	// CAF
	public static final int LEXICAL_DIVERSITY = (id += 1);
	public static final int LEXICAL_SOPHISTICATION = (id += 1);
	public static final int SYNTACTIC_DIVERSITY = (id += 1);
	public static final int SYNTACTIC_SOPHISTICATION = (id += 1);
	public static final int BALANCED_CAF = (id += 1);

	// Syntax
	public static final int AVERAGE_NO_NOUNS_PER_BLOCK = (id += 1);
	public static final int AVERAGE_NO_PRONOUNS_PER_BLOCK = (id += 1);
	public static final int AVERAGE_NO_VERBS_PER_BLOCK = (id += 1);
	public static final int AVERAGE_NO_ADVERBS_PER_BLOCK = (id += 1);
	public static final int AVERAGE_NO_ADJECTIVES_PER_BLOCK = (id += 1);
	public static final int AVERAGE_NO_PREPOSITIONS_PER_BLOCK = (id += 1);

	public static final int AVERAGE_NO_NOUNS_PER_SENTENCE = (id += 1);
	public static final int AVERAGE_NO_PRONOUNS_PER_SENTENCE = (id += 1);
	public static final int AVERAGE_NO_VERBS_PER_SENTENCE = (id += 1);
	public static final int AVERAGE_NO_ADVERBS_PER_SENTENCE = (id += 1);
	public static final int AVERAGE_NO_ADJECTIVES_PER_SENTENCE = (id += 1);
	public static final int AVERAGE_NO_PREPOSITIONS_PER_SENTENCE = (id += 1);

	public static final int AVERAGE_NO_UNIQUE_NOUNS_PER_BLOCK = (id += 1);
	public static final int AVERAGE_NO_UNIQUE_PRONOUNS_PER_BLOCK = (id += 1);
	public static final int AVERAGE_NO_UNIQUE_VERBS_PER_BLOCK = (id += 1);
	public static final int AVERAGE_NO_UNIQUE_ADVERBS_PER_BLOCK = (id += 1);
	public static final int AVERAGE_NO_UNIQUE_ADJECTIVES_PER_BLOCK = (id += 1);
	public static final int AVERAGE_NO_UNIQUE_PREPOSITIONS_PER_BLOCK = (id += 1);

	public static final int PRONOUNS = (id += 1);

	public static final int AVERAGE_TREE_DEPTH = (id += Pronouns.NO_PRONOUN_TYPES);
	public static final int AVERAGE_TREE_SIZE = (id += 1);
	public static final int AVERAGE_NO_SEMANTIC_DEPENDENCIES = (id += 1);

	// Word complexity
	public static final int AVERAGE_WORD_DIFF_LEMMA_STEM = (id += 1);
	public static final int AVERAGE_WORD_DIFF_WORD_STEM = (id += 1);
	public static final int AVERAGE_WORD_DEPTH_HYPERNYM_TREE = (id += 1);
	public static final int AVERAGE_WORD_POLYSEMY_COUNT = (id += 1);
	public static final int AVERAGE_WORD_SYLLABLE_COUNT = (id += 1);

	// Entity Density
	public static final int AVERAGE_NO_NAMED_ENT_PER_BLOCK = (id += 1);
	public static final int AVERAGE_NO_NOUN_NAMED_ENT_PER_BLOCK = (id += 1);
	public static final int AVERAGE_NO_UNIQUE_NAMED_ENT_PER_BLOCK = (id += 1);
	public static final int AVERAGE_NO_NAMED_ENT_PER_SENTENCE = (id += 1);

	// Co-reference inference
	public static final int TOTAL_NO_COREF_CHAINS_PER_DOC = (id += 1);
	public static final int AVERAGE_NO_COREFS_PER_CHAIN = (id += 1);
	public static final int AVERAGE_CHAIN_SPAN = (id += 1);
	public static final int NO_COREF_CHAINS_WITH_BIG_SPAN = (id += 1);
	public static final int AVERAGE_INFERENCE_DISTANCE_PER_CHAIN = (id += 1);
	public static final int NO_ACTIVE_COREF_CHAINS_PER_WORD = (id += 1);

	// Connectives
	public static final int CONNECTIVES_EN = (id += 1);
	public static final int CONNECTIVES_FR = (id += Connectives.NO_CONNECTIVE_TYPES_EN);
	public static final int CONNECTIVES_RO = (id += Connectives.NO_CONNECTIVE_TYPES_FR);

	// Cohesion (Lexical chains)
	public static final int LEXICAL_CHAINS_AVERAGE_SPAN = (id += Connectives.NO_CONNECTIVE_TYPES_RO);
	public static final int LEXICAL_CHAINS_MAX_SPAN = (id += 1);
	public static final int AVERAGE_NO_LEXICAL_CHAINS = (id += 1);
	public static final int PERCENTAGE_LEXICAL_CHAINS_COVERAGE = (id += 1);

	// Lexical cohesion
	public static final int AVERAGE_INTRA_SENTENCE_LEXICAL_COHESION = (id += 1);
	public static final int AVERAGE_LEXICAL_BLOCK_COHESION_ADJACENT_SENTENCES = (id += 1);
	public static final int AVERAGE_LEXICAL_BLOCK_COHESION = (id += 1);

	// Dialogism - semantic chains
	public static final int AVERAGE_NO_VOICES = (id += 1);
	public static final int VOICES_AVERAGE_SPAN = (id += 1);
	public static final int VOICES_MAX_SPAN = (id += 1);

	public static final int AVERAGE_VOICE_BLOCK_ENTROPY = (id += 1);
	public static final int AVERAGE_VOICE_SENTENCE_ENTROPY = (id += 1);

	public static final int AVERAGE_VOICE_BLOCK_DISTRIBUTION = (id += 1);
	public static final int VOICE_BLOCK_DISTRIBUTION_STANDARD_DEVIATION = (id += 1);
	public static final int AVERAGE_VOICE_SENTENCE_DISTRIBUTION = (id += 1);
	public static final int VOICE_SENTENCE_DISTRIBUTION_STANDARD_DEVIATION = (id += 1);

	public static final int AVERAGE_VOICE_RECURRENCE_BLOCK = (id += 1);
	public static final int VOICE_RECURRENCE_BLOCK_DISTRIBUTION_STANDARD_DEVIATION = (id += 1);
	public static final int AVERAGE_VOICE_RECURRENCE_SENTENCE = (id += 1);
	public static final int VOICE_RECURRENCE_SENTENCE_STANDARD_DEVIATION = (id += 1);

	// Dialogism - synergy
	public static final int AVERAGE_BLOCK_VOICE_CO_OCCURRENCE = (id += 1);
	public static final int BLOCK_VOICE_CO_OCCURRENCE_STANDARD_DEVIATION = (id += 1);
	public static final int AVERAGE_SENTENCE_VOICE_CO_OCCURRENCE = (id += 1);
	public static final int SENTENCE_VOICE_CO_OCCURRENCE_STANDARD_DEVIATION = (id += 1);

	public static final int AVERAGE_BLOCK_VOICE_CUMULATIVE_EFFECT = (id += 1);
	public static final int BLOCK_VOICE_CUMULATIVE_EFFECT_STANDARD_DEVIATION = (id += 1);
	public static final int AVERAGE_SENTENCE_VOICE_CUMULATIVE_EFFECT = (id += 1);
	public static final int SENTENCE_VOICE_CUMULATIVE_EFFECT_STANDARD_DEVIATION = (id += 1);

	public static final int AVERAGE_BLOCK_VOICE_MUTUAL_INFORMATION = (id += 1);
	public static final int BLOCK_VOICE_MUTUAL_INFORMATION_STANDARD_DEVIATION = (id += 1);
	public static final int AVERAGE_SENTENCE_VOICE_MUTUAL_INFORMATION = (id += 1);
	public static final int SENTENCE_VOICE_MUTUAL_INFORMATION_STANDARD_DEVIATION = (id += 1);

	// Discourse
	public static final int AVERAGE_BLOCK_SCORE = (id += 1);
	public static final int BLOCK_SCORE_STANDARD_DEVIATION = (id += 1);
	public static final int AVERAGE_SENTENCE_SCORE = (id += 1);
	public static final int SENTENCE_SCORE_STANDARD_DEVIATION = (id += 1);
	public static final int AVERAGE_TOP10_KEYWORDS_RELEVANCE = (id += 1);
	public static final int TOP10_KEYWORDS_RELEVANCE_STANDARD_DEVIATION = (id += 1);

	public static final int AVERAGE_BLOCK_DOC_COHESION = (id += 1);
	public static final int AVERAGE_SENTENCE_BLOCK_COHESION = (id += 6);

	public static final int AVERAGE_INTER_BLOCK_COHESION = (id += 6);
	public static final int AVERAGE_INTRA_BLOCK_COHESION = (id += 6);

	public static final int AVERAGE_BLOCK_ADJACENCY_COHESION = (id += 6);
	public static final int AVERAGE_SENTENCE_ADJACENCY_COHESION = (id += 6);

	public static final int AVERAGE_TRANSITION_COHESION = (id += 6);
	public static final int AVERAGE_START_MIDDLE_COHESION = (id += 6);
	public static final int AVERAGE_MIDDLE_END_COHESION = (id += 6);
	public static final int START_END_COHESION = (id += 6);

	public static final int DOC_FLOW_ABSOLUTE_POSITION_ACCURACY = (id += 6);
	public static final int DOC_FLOW_ABSOLUTE_DISTANCE_ACCURACY = (id += 12);
	public static final int DOC_FLOW_ADJACENCY_ACCURACY = (id += 12);
	public static final int DOC_FLOW_SPEARMAN_CORRELATION = (id += 12);
	public static final int DOC_FLOW_MAX_ORDERED_SEQUENCE = (id += 12);
	public static final int DOC_FLOW_AVERAGE_COHESION = (id += 12);

	public static final int NO_COMPLEXITY_INDICES = (id += 12);

	public static final IComplexityFactors[] TEXTUAL_COMPLEXITY_FACTORS = { new Readability(), new LengthComplexity(),
			new SurfaceStatisticsComplexity(), new EntropyComplexity(), new BalancedCAF(), new POSComplexity(),
			new PronounsComplexity(), new TreeComplexity(), new WordComplexity(), new EntityDensityComplexity(),
			new CoreferenceResolutionComplexity(), new ConnectivesComplexity(Lang.eng),
			new ConnectivesComplexity(Lang.fr), new ConnectivesComplexity(Lang.ro), new LexicalChainsComplexity(),
			new LexicalCohesionComplexity(), new DialogismStatisticsComplexity(), new DialogismSynergyComplexity(),
			new DiscourseComplexity(), new SemanticCohesionComplexity(0), new SemanticCohesionComplexity(1),
			new SemanticCohesionComplexity(2), new SemanticCohesionComplexity(3), new SemanticCohesionComplexity(4),
			new SemanticCohesionComplexity(5), new DocumentFlowComplexity(0, DocFlowCriteria.ABOVE_MEAN_PLUS_STDEV),
			new DocumentFlowComplexity(0, DocFlowCriteria.MAX_VALUE),
			new DocumentFlowComplexity(1, DocFlowCriteria.ABOVE_MEAN_PLUS_STDEV),
			new DocumentFlowComplexity(1, DocFlowCriteria.MAX_VALUE),
			new DocumentFlowComplexity(2, DocFlowCriteria.ABOVE_MEAN_PLUS_STDEV),
			new DocumentFlowComplexity(2, DocFlowCriteria.MAX_VALUE),
			new DocumentFlowComplexity(3, DocFlowCriteria.ABOVE_MEAN_PLUS_STDEV),
			new DocumentFlowComplexity(3, DocFlowCriteria.MAX_VALUE),
			new DocumentFlowComplexity(4, DocFlowCriteria.ABOVE_MEAN_PLUS_STDEV),
			new DocumentFlowComplexity(4, DocFlowCriteria.MAX_VALUE),
			new DocumentFlowComplexity(5, DocFlowCriteria.ABOVE_MEAN_PLUS_STDEV),
			new DocumentFlowComplexity(5, DocFlowCriteria.MAX_VALUE) };

	public static String TEXTUAL_COMPLEXITY_INDEX_DESCRIPTIONS[] = new String[NO_COMPLEXITY_INDICES];

	static {
		for (IComplexityFactors f : TEXTUAL_COMPLEXITY_FACTORS)
			f.setComplexityIndexDescription(TEXTUAL_COMPLEXITY_INDEX_DESCRIPTIONS);
	}

	public static String TEXTUAL_COMPLEXITY_INDEX_ACRONYMS[] = new String[NO_COMPLEXITY_INDICES];

	static {
		for (IComplexityFactors f : TEXTUAL_COMPLEXITY_FACTORS)
			f.setComplexityIndexAcronym(TEXTUAL_COMPLEXITY_INDEX_ACRONYMS);
	}

	public static void computeComplexityFactors(AbstractDocument d) {
		d.setComplexityIndices(new double[NO_COMPLEXITY_INDICES]);
		for (IComplexityFactors f : TEXTUAL_COMPLEXITY_FACTORS)
			f.computeComplexityFactors(d);
	}

	public static void main(String[] args) {
		for (IComplexityFactors factors : TEXTUAL_COMPLEXITY_FACTORS) {
			System.out.println(factors.getClassName());
			for (int i : factors.getIDs())
				System.out.println(i + "\t" + TEXTUAL_COMPLEXITY_INDEX_ACRONYMS[i] + "\n\t\t"
						+ TEXTUAL_COMPLEXITY_INDEX_DESCRIPTIONS[i]);
		}
		System.out.println("TOTAL:" + NO_COMPLEXITY_INDICES + " factors");
	}
}
