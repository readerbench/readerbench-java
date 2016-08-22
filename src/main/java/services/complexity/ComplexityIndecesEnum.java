/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package services.complexity;

/**
 *
 * @author Stefan
 */
public enum ComplexityIndecesEnum {
    READABILITY_FLESCH(ComplexityIndexType.READABILITY),
    READABILITY_FOG(ComplexityIndexType.READABILITY),
    READABILITY_KINCAID(ComplexityIndexType.READABILITY),
    READABILITY_DALE_CHALL(ComplexityIndexType.READABILITY),
    
    AVERAGE_BLOCK_LENGTH(ComplexityIndexType.SURFACE),
    AVERAGE_SENTENCE_LENGTH(ComplexityIndexType.SURFACE),
    AVERAGE_WORD_LENGTH(ComplexityIndexType.SURFACE),
    WORD_LETTERS_STANDARD_DEVIATION(ComplexityIndexType.SURFACE),
    AVERAGE_COMMAS_PER_BLOCK(ComplexityIndexType.SURFACE),
    AVERAGE_COMMAS_PER_SENTENCE(ComplexityIndexType.SURFACE),
    AVERAGE_SENTENCES_IN_BLOCK(ComplexityIndexType.SURFACE),
    BLOCK_STANDARD_DEVIATION_NO_SENTENCES(ComplexityIndexType.SURFACE),
    AVERAGE_WORDS_IN_BLOCK(ComplexityIndexType.SURFACE),
    BLOCK_STANDARD_DEVIATION_NO_WORDS(ComplexityIndexType.SURFACE),
    AVERAGE_WORDS_IN_SENTENCE(ComplexityIndexType.SURFACE),
    SENTENCE_STANDARD_DEVIATION_NO_WORDS(ComplexityIndexType.SURFACE),
    AVERAGE_UNIQUE_WORDS_IN_BLOCK(ComplexityIndexType.SURFACE),
    BLOCK_STANDARD_DEVIATION_NO_UNIQUE_WORDS(ComplexityIndexType.SURFACE),
    AVERAGE_UNIQUE_WORDS_IN_SENTENCE(ComplexityIndexType.SURFACE),
    SENTENCE_STANDARD_DEVIATION_NO_UNIQUE_WORDS(ComplexityIndexType.SURFACE),
    
    WORD_ENTROPY(ComplexityIndexType.SURFACE),
    CHAR_ENTROPY(ComplexityIndexType.SURFACE),
    
    LEXICAL_DIVERSITY(ComplexityIndexType.CAF),
    LEXICAL_SOPHISTICATION(ComplexityIndexType.CAF),
    SYNTACTIC_DIVERSITY(ComplexityIndexType.CAF),
    SYNTACTIC_SOPHISTICATION(ComplexityIndexType.CAF),
    BALANCED_CAF(ComplexityIndexType.CAF),
    
    AVERAGE_NO_NOUNS_PER_BLOCK(ComplexityIndexType.SYNTAX),
    AVERAGE_NO_PRONOUNS_PER_BLOCK(ComplexityIndexType.SYNTAX),
    AVERAGE_NO_VERBS_PER_BLOCK(ComplexityIndexType.SYNTAX),
    AVERAGE_NO_ADVERBS_PER_BLOCK(ComplexityIndexType.SYNTAX),
    AVERAGE_NO_ADJECTIVES_PER_BLOCK(ComplexityIndexType.SYNTAX),
    AVERAGE_NO_PREPOSITIONS_PER_BLOCK(ComplexityIndexType.SYNTAX),
    AVERAGE_NO_NOUNS_PER_SENTENCE(ComplexityIndexType.SYNTAX),
    AVERAGE_NO_PRONOUNS_PER_SENTENCE(ComplexityIndexType.SYNTAX),
    AVERAGE_NO_VERBS_PER_SENTENCE(ComplexityIndexType.SYNTAX),
    AVERAGE_NO_ADVERBS_PER_SENTENCE(ComplexityIndexType.SYNTAX),
    AVERAGE_NO_ADJECTIVES_PER_SENTENCE(ComplexityIndexType.SYNTAX),
    AVERAGE_NO_PREPOSITIONS_PER_SENTENCE(ComplexityIndexType.SYNTAX),
    AVERAGE_NO_UNIQUE_NOUNS_PER_BLOCK(ComplexityIndexType.SYNTAX),
    AVERAGE_NO_UNIQUE_PRONOUNS_PER_BLOCK(ComplexityIndexType.SYNTAX),
    AVERAGE_NO_UNIQUE_VERBS_PER_BLOCK(ComplexityIndexType.SYNTAX),
    AVERAGE_NO_UNIQUE_ADVERBS_PER_BLOCK(ComplexityIndexType.SYNTAX),
    AVERAGE_NO_UNIQUE_ADJECTIVES_PER_BLOCK(ComplexityIndexType.SYNTAX),
    AVERAGE_NO_UNIQUE_PREPOSITIONS_PER_BLOCK(ComplexityIndexType.SYNTAX),
    PRONOUNS_BLOCK(ComplexityIndexType.SYNTAX),
    PRONOUNS_SENTENCE(ComplexityIndexType.SYNTAX),
    AVERAGE_TREE_DEPTH(ComplexityIndexType.SYNTAX),
    AVERAGE_TREE_SIZE(ComplexityIndexType.SYNTAX),
    AVERAGE_NO_SEMANTIC_DEPENDENCIES(ComplexityIndexType.SYNTAX),
    
    WORD_DIFF_LEMMA_STEM(ComplexityIndexType.WORD_COMPLEXITY),
    WORD_DIFF_WORD_STEM(ComplexityIndexType.WORD_COMPLEXITY),
    WORD_MAX_DEPTH_HYPERNYM_TREE(ComplexityIndexType.WORD_COMPLEXITY),
    WORD_AVERAGE_DEPTH_HYPERNYM_TREE(ComplexityIndexType.WORD_COMPLEXITY),
    WORD_PATH_COUNT_HYPERNYM_TREE(ComplexityIndexType.WORD_COMPLEXITY),
    WORD_POLYSEMY_COUNT(ComplexityIndexType.WORD_COMPLEXITY),
    WORD_SYLLABLE_COUNT(ComplexityIndexType.WORD_COMPLEXITY),
    
    AVERAGE_NO_NAMED_ENT_PER_BLOCK(ComplexityIndexType.ENTITY_DENSITY),
    AVERAGE_NO_NOUN_NAMED_ENT_PER_BLOCK(ComplexityIndexType.ENTITY_DENSITY),
    AVERAGE_NO_UNIQUE_NAMED_ENT_PER_BLOCK(ComplexityIndexType.ENTITY_DENSITY),
    AVERAGE_NO_NAMED_ENT_PER_SENTENCE(ComplexityIndexType.ENTITY_DENSITY),
    
    TOTAL_NO_COREF_CHAINS_PER_DOC(ComplexityIndexType.COREFERENCE),
    AVERAGE_NO_COREFS_PER_CHAIN(ComplexityIndexType.COREFERENCE),
    AVERAGE_CHAIN_SPAN(ComplexityIndexType.COREFERENCE),
    NO_COREF_CHAINS_WITH_BIG_SPAN(ComplexityIndexType.COREFERENCE),
    AVERAGE_INFERENCE_DISTANCE_PER_CHAIN(ComplexityIndexType.COREFERENCE),
    NO_ACTIVE_COREF_CHAINS_PER_WORD(ComplexityIndexType.COREFERENCE),
    
    CONNECTIVES_BLOCK(ComplexityIndexType.CONNECTIVES),
    CONNECTIVES_SENTENCE(ComplexityIndexType.CONNECTIVES),
    
    LEXICAL_CHAINS_AVERAGE_SPAN(ComplexityIndexType.COHESION),
    LEXICAL_CHAINS_MAX_SPAN(ComplexityIndexType.COHESION),
    AVERAGE_NO_LEXICAL_CHAINS(ComplexityIndexType.COHESION),
    PERCENTAGE_LEXICAL_CHAINS_COVERAGE(ComplexityIndexType.COHESION),
    
    AVERAGE_INTRA_SENTENCE_LEXICAL_COHESION(ComplexityIndexType.COHESION),
    AVERAGE_LEXICAL_BLOCK_COHESION_ADJACENT_SENTENCES(ComplexityIndexType.COHESION),
    AVERAGE_LEXICAL_BLOCK_COHESION(ComplexityIndexType.COHESION),
    
    AVERAGE_NO_VOICES(ComplexityIndexType.DIALOGISM),
    VOICES_AVERAGE_SPAN(ComplexityIndexType.DIALOGISM),
    VOICES_MAX_SPAN(ComplexityIndexType.DIALOGISM),
    AVERAGE_VOICE_BLOCK_ENTROPY(ComplexityIndexType.DIALOGISM),
    AVERAGE_VOICE_SENTENCE_ENTROPY(ComplexityIndexType.DIALOGISM),
    AVERAGE_VOICE_BLOCK_DISTRIBUTION(ComplexityIndexType.DIALOGISM),
    VOICE_BLOCK_DISTRIBUTION_STANDARD_DEVIATION(ComplexityIndexType.DIALOGISM),
    AVERAGE_VOICE_SENTENCE_DISTRIBUTION(ComplexityIndexType.DIALOGISM),
    VOICE_SENTENCE_DISTRIBUTION_STANDARD_DEVIATION(ComplexityIndexType.DIALOGISM),
    AVERAGE_VOICE_RECURRENCE_BLOCK(ComplexityIndexType.DIALOGISM),
    VOICE_RECURRENCE_BLOCK_DISTRIBUTION_STANDARD_DEVIATION(ComplexityIndexType.DIALOGISM),
    AVERAGE_VOICE_RECURRENCE_SENTENCE(ComplexityIndexType.DIALOGISM),
    VOICE_RECURRENCE_SENTENCE_STANDARD_DEVIATION(ComplexityIndexType.DIALOGISM),
    
    AVERAGE_BLOCK_VOICE_CO_OCCURRENCE(ComplexityIndexType.DIALOGISM),
    BLOCK_VOICE_CO_OCCURRENCE_STANDARD_DEVIATION(ComplexityIndexType.DIALOGISM),
    AVERAGE_SENTENCE_VOICE_CO_OCCURRENCE(ComplexityIndexType.DIALOGISM),
    SENTENCE_VOICE_CO_OCCURRENCE_STANDARD_DEVIATION(ComplexityIndexType.DIALOGISM),
    AVERAGE_BLOCK_VOICE_CUMULATIVE_EFFECT(ComplexityIndexType.DIALOGISM),
    BLOCK_VOICE_CUMULATIVE_EFFECT_STANDARD_DEVIATION(ComplexityIndexType.DIALOGISM),
    AVERAGE_SENTENCE_VOICE_CUMULATIVE_EFFECT(ComplexityIndexType.DIALOGISM),
    SENTENCE_VOICE_CUMULATIVE_EFFECT_STANDARD_DEVIATION(ComplexityIndexType.DIALOGISM),
    AVERAGE_BLOCK_VOICE_MUTUAL_INFORMATION(ComplexityIndexType.DIALOGISM),
    BLOCK_VOICE_MUTUAL_INFORMATION_STANDARD_DEVIATION(ComplexityIndexType.DIALOGISM),
    AVERAGE_SENTENCE_VOICE_MUTUAL_INFORMATION(ComplexityIndexType.DIALOGISM),
    SENTENCE_VOICE_MUTUAL_INFORMATION_STANDARD_DEVIATION(ComplexityIndexType.DIALOGISM),
    
    AVERAGE_BLOCK_SCORE(ComplexityIndexType.COHESION), 
    BLOCK_SCORE_STANDARD_DEVIATION(ComplexityIndexType.COHESION), 
    AVERAGE_SENTENCE_SCORE(ComplexityIndexType.COHESION), 
    SENTENCE_SCORE_STANDARD_DEVIATION(ComplexityIndexType.COHESION), 
    AVERAGE_BLOCK_DOC_COHESION(ComplexityIndexType.COHESION), 
    AVERAGE_SENTENCE_BLOCK_COHESION(ComplexityIndexType.COHESION), 
    AVERAGE_INTER_BLOCK_COHESION(ComplexityIndexType.COHESION), 
    AVERAGE_INTRA_BLOCK_COHESION(ComplexityIndexType.COHESION), 
    AVERAGE_BLOCK_ADJACENCY_COHESION(ComplexityIndexType.COHESION), 
    AVERAGE_SENTENCE_ADJACENCY_COHESION(ComplexityIndexType.COHESION), 
    AVERAGE_TRANSITION_COHESION(ComplexityIndexType.COHESION), 
    AVERAGE_START_MIDDLE_COHESION(ComplexityIndexType.COHESION), 
    AVERAGE_MIDDLE_END_COHESION(ComplexityIndexType.COHESION), 
    START_END_COHESION(ComplexityIndexType.COHESION), 
    DOC_FLOW_ABSOLUTE_POSITION_ACCURACY(ComplexityIndexType.COHESION), 
    DOC_FLOW_ABSOLUTE_DISTANCE_ACCURACY(ComplexityIndexType.COHESION), 
    DOC_FLOW_ADJACENCY_ACCURACY(ComplexityIndexType.COHESION), 
    DOC_FLOW_SPEARMAN_CORRELATION(ComplexityIndexType.COHESION), 
    DOC_FLOW_MAX_ORDERED_SEQUENCE(ComplexityIndexType.COHESION), 
    DOC_FLOW_AVERAGE_COHESION(ComplexityIndexType.COHESION);
    
    private final ComplexityIndexType type;

    public ComplexityIndexType getType() {
        return type;
    }

    private ComplexityIndecesEnum(ComplexityIndexType type) {
        this.type = type;
    }

    private ComplexityIndecesEnum() {
        this(null);
    }

}