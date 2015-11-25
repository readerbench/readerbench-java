package services.complexity.readability;

import services.complexity.ComplexityIndices;
import services.complexity.IComplexityFactors;
import services.complexity.readability.Fathom.Stats;
import data.AbstractDocument;

/**
 * <p>
 * Common indices of readability.
 * </p>
 * 
 * <p>
 * Ported from perl's Lingua::EN::Fathom by Kim Ryan.
 * </p>
 * 
 * <p>
 * <b>COPYRIGHT</b> Distributed under the same terms as Perl.
 * </p>
 * 
 * @author Kim Ryan
 * @author Larry Ogrodnek &lt;ogrodnek@gmail.com&gt;
 * 
 * @version $Revision: 1.1 $ $Date: 2009/11/04 12:20:08 $
 */
public class Readability implements IComplexityFactors {

	/**
	 * Returns the Fog index.
	 * 
	 * <p>
	 * ( words_per_sentence + percent_complex_words ) * 0.4
	 * </p>
	 * 
	 * <p>
	 * The Fog index, developed by Robert Gunning, is a well known and simple
	 * formula for measuring readability. The index indicates the number of
	 * years of formal education a reader of average intelligence would need to
	 * understand the text on the first reading.
	 * </p>
	 * 
	 * <ul>
	 * <li>18 unreadable</li>
	 * <li>14 difficult</li>
	 * <li>12 ideal</li>
	 * <li>10 acceptable</li>
	 * <li>8 childish</li>
	 * </ul>
	 * 
	 * @param stats
	 * @return Fog index.
	 */

	public static float calcFog(final Stats stats) {
		return (wordsPerSentence(stats) + percentComplexWords(stats)) * 0.4f;
	}

	public static String meaningFog(double val) {
		if (val <= 9)
			return "childish";
		if (val <= 11)
			return "acceptable";
		if (val <= 13)
			return "ideal";
		if (val <= 16)
			return "difficult";
		return "unreadable";
	}

	/**
	 * Returns the Flesch reading ease score.
	 * 
	 * <p>
	 * 206.835 - (1.015 * words_per_sentence) - (84.6 * syllables_per_word)
	 * </p>
	 * 
	 * <p>
	 * This score rates text on a 100 point scale. The higher the score, the
	 * easier it is to understand the text. A score of 60 to 70 is considered to
	 * be optimal.
	 * </p>
	 * 
	 * @param stats
	 * @return Flesch reading ease score.
	 */
	public static float calcFlesch(final Stats stats) {
		return 206.835f - (1.015f * wordsPerSentence(stats))
				- (84.6f * syllablesPerWords(stats));
	}

	public static String meaningFlesh(double val) {
		if (val >= 90)
			return "very easy";
		if (val >= 80)
			return "easy";
		if (val >= 70)
			return "fairly easy";
		if (val >= 60)
			return "standard - optimal";
		if (val >= 50)
			return "fairly difficult";
		if (val > 30)
			return "difficult";
		return "very confusing";
	}

	/**
	 * Returns the Flesch-Kincaid grade level score for the analysed text file
	 * or block.
	 * 
	 * <p>
	 * (11.8 * syllables_per_word) + (0.39 * words_per_sentence) - 15.59;
	 * </p>
	 * 
	 * <p>
	 * This score rates text on U.S. grade school level. So a score of 8.0 means
	 * that the document can be understood by an eighth grader. A score of 7.0
	 * to 8.0 is considered to be optimal.
	 * </p>
	 * 
	 * @param stats
	 * @return Flesch-Kincaid score.
	 */
	public static float calcKincaid(final Stats stats) {
		return (11.8f * syllablesPerWords(stats))
				+ (0.39f * wordsPerSentence(stats)) - 15.59f;
	}

	public static String meaningKincaid(double val) {
		int round = (int) (val + 0.5);

		if (round == 8)
			return "8th grade - optimal";
		return round + "th grade";
	}

	public static float wordsPerSentence(final Stats stats) {
		return ((float) stats.getNumWords()) / stats.getNumSentences();
	}

	public static float percentComplexWords(final Stats stats) {
		return (((float) stats.getNumComplexWords()) / stats.getNumWords()) * 100;
	}

	public static float syllablesPerWords(final Stats stats) {
		return ((float) stats.getNumSyllables()) / stats.getNumWords();
	}

	@Override
	public void setComplexityFactorNames(String[] names) {
		names[ComplexityIndices.READABILITY_FLESCH] = "Readability Flesh (EN only)";
		names[ComplexityIndices.READABILITY_FOG] = "Readability FOG (EN only)";
		names[ComplexityIndices.READABILITY_KINCAID] = "Readability Kincaid (EN only)";
	}

	@Override
	public void computeComplexityFactors(AbstractDocument d) {
		switch (d.getLanguage()) {
		case fr:
			d.getComplexityIndices()[ComplexityIndices.READABILITY_FLESCH] = ComplexityIndices.IDENTITY;
			d.getComplexityIndices()[ComplexityIndices.READABILITY_FOG] = ComplexityIndices.IDENTITY;
			d.getComplexityIndices()[ComplexityIndices.READABILITY_KINCAID] = ComplexityIndices.IDENTITY;
			break;
		default:
			Stats stats = Fathom.analyze(d.getProcessedText());
			d.getComplexityIndices()[ComplexityIndices.READABILITY_FLESCH] = Readability
					.calcFlesch(stats);
			d.getComplexityIndices()[ComplexityIndices.READABILITY_FOG] = Readability
					.calcFog(stats);
			d.getComplexityIndices()[ComplexityIndices.READABILITY_KINCAID] = Readability
					.calcKincaid(stats);
			break;
		}
	}

	@Override
	public String getClassName() {
		return "Readability Formulas (EN only)";
	}

	@Override
	public int[] getIDs() {
		return new int[] { ComplexityIndices.READABILITY_FLESCH,
				ComplexityIndices.READABILITY_FOG,
				ComplexityIndices.READABILITY_KINCAID };
	}
}
